package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.database.GovindaDatabase
import com.example.data.local.entity.BookmarkedQuestionEntity
import com.example.data.local.entity.DailyProgressEntity
import com.example.data.local.entity.GenerationHistoryEntity
import com.example.data.local.entity.QuestionAttemptEntity
import com.example.data.local.entity.ReviewQueueEntity
import com.example.data.local.entity.SkillProfileEntity
import com.example.data.preferences.UserPreferencesDataStore
import com.example.domain.model.BookmarkedQuestion
import com.example.domain.model.DailyProgress
import com.example.domain.model.MathCategory
import com.example.domain.model.QuestionAttempt
import com.example.domain.model.ReviewItem
import com.example.domain.model.SkillProfile
import com.example.domain.model.UserPreferences
import com.example.domain.repository.MathLearningRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class RoomMathLearningRepository(
    private val database: GovindaDatabase,
    private val preferencesDataStore: UserPreferencesDataStore
) : MathLearningRepository {

    private val profileDao = database.skillProfileDao()
    private val attemptDao = database.questionAttemptDao()
    private val historyDao = database.generationHistoryDao()
    private val reviewDao = database.reviewQueueDao()
    private val dailyDao = database.dailyProgressDao()
    private val bookmarkDao = database.bookmarkedQuestionDao()

    override fun observeSkillProfiles(): Flow<List<SkillProfile>> =
        profileDao.observeAll().map { entities ->
            val byCategory = entities.associateBy { it.category }
            MathCategory.entries.map { category ->
                byCategory[category]?.toDomain() ?: SkillProfile.initial(category)
            }
        }

    override fun observeRecentAttempts(limit: Int): Flow<List<QuestionAttempt>> =
        attemptDao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override fun observeReviewQueue(): Flow<List<ReviewItem>> =
        reviewDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeBookmarks(): Flow<List<BookmarkedQuestion>> =
        bookmarkDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeDailyProgress(limit: Int): Flow<List<DailyProgress>> =
        dailyDao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override fun observePreferences(): Flow<UserPreferences> =
        preferencesDataStore.preferencesFlow

    override suspend fun getSkillProfiles(): List<SkillProfile> {
        ensureSeededProfiles()
        val byCategory = profileDao.getAll().associateBy { it.category }
        return MathCategory.entries.map { category ->
            byCategory[category]?.toDomain() ?: SkillProfile.initial(category)
        }
    }

    override suspend fun getSkillProfile(category: MathCategory): SkillProfile {
        ensureSeededProfiles()
        return profileDao.getByCategory(category)?.toDomain() ?: SkillProfile.initial(category)
    }

    override suspend fun getRecentAttemptsForCategory(
        category: MathCategory,
        limit: Int
    ): List<QuestionAttempt> =
        attemptDao.getRecentForCategory(category, limit).map { it.toDomain() }.reversed()

    override suspend fun getReviewQueue(): List<ReviewItem> =
        reviewDao.getAll().map { it.toDomain() }

    override suspend fun getRecentFingerprints(limit: Int): Set<String> =
        historyDao.getRecentFingerprints(limit).toSet()

    override suspend fun saveFingerprint(
        fingerprint: String,
        category: MathCategory,
        generatorVersion: Int
    ) {
        historyDao.insert(
            GenerationHistoryEntity(
                fingerprint = fingerprint,
                category = category,
                generatedAt = System.currentTimeMillis(),
                generatorVersion = generatorVersion
            )
        )
        historyDao.trimOldHistory()
    }

    override suspend fun recordAttemptTransaction(
        attempt: QuestionAttempt,
        updatedProfile: SkillProfile,
        dateKey: String,
        xpEarned: Int,
        reviewItemToUpsert: ReviewItem?,
        resolvedReviewCategory: MathCategory?
    ) {
        database.withTransaction {
            attemptDao.insert(attempt.toEntity())
            attemptDao.trimOldAttempts()
            profileDao.upsert(updatedProfile.toEntity())

            val existingDaily = dailyDao.getByDate(dateKey)
            val updatedDaily = if (existingDaily == null) {
                DailyProgressEntity(
                    date = dateKey,
                    attemptedCount = 1,
                    correctCount = if (attempt.isCorrect) 1 else 0,
                    totalXp = xpEarned,
                    practiceDurationMs = attempt.responseTimeMs
                )
            } else {
                existingDaily.copy(
                    attemptedCount = existingDaily.attemptedCount + 1,
                    correctCount = existingDaily.correctCount + if (attempt.isCorrect) 1 else 0,
                    totalXp = existingDaily.totalXp + xpEarned,
                    practiceDurationMs = existingDaily.practiceDurationMs + attempt.responseTimeMs
                )
            }
            dailyDao.upsert(updatedDaily)

            if (reviewItemToUpsert != null) {
                val existingReview = reviewDao.getByConceptKey(reviewItemToUpsert.conceptKey)
                val merged = if (existingReview == null) {
                    reviewItemToUpsert.toEntity()
                } else {
                    existingReview.copy(
                        promptSnapshot = reviewItemToUpsert.promptSnapshot,
                        expectedAnswer = reviewItemToUpsert.expectedAnswer,
                        explanationSnapshot = reviewItemToUpsert.explanationSnapshot,
                        reviewAt = reviewItemToUpsert.reviewAtEpochMs,
                        priority = existingReview.priority + 1,
                        failureCount = existingReview.failureCount + 1
                    )
                }
                reviewDao.upsert(merged)
            } else if (resolvedReviewCategory != null) {
                reviewDao.resolveOldestForCategory(resolvedReviewCategory)
            }
        }
    }

    override suspend fun toggleBookmark(question: BookmarkedQuestion): Boolean {
        return if (bookmarkDao.exists(question.id)) {
            bookmarkDao.deleteById(question.id)
            false
        } else {
            bookmarkDao.insert(question.toEntity())
            true
        }
    }

    override suspend fun removeBookmark(questionId: String) {
        bookmarkDao.deleteById(questionId)
    }

    override suspend fun isBookmarked(questionId: String): Boolean =
        bookmarkDao.exists(questionId)

    override suspend fun dismissReviewItem(id: String) {
        reviewDao.deleteById(id)
    }

    override suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        preferencesDataStore.update(transform)
    }

    override suspend fun exportAllDataAsJson(): String {
        val profiles = getSkillProfiles()
        val attempts = attemptDao.getAll()
        val daily = dailyDao.getAll()
        val reviews = reviewDao.getAll()

        val root = JSONObject().apply {
            put("app", "GOVINDA")
            put("exportedAtEpochMs", System.currentTimeMillis())
            put(
                "skillProfiles",
                JSONArray().apply {
                    profiles.forEach { p ->
                        put(
                            JSONObject().apply {
                                put("category", p.category.name)
                                put("skillScore", p.skillScore)
                                put("accuracy", p.accuracy)
                                put("averageResponseTimeMs", p.averageResponseTimeMs)
                                put("currentDifficulty", p.currentDifficulty.name)
                                put("attemptsCount", p.attemptsCount)
                                put("correctStreak", p.correctStreak)
                            }
                        )
                    }
                }
            )
            put(
                "dailyProgress",
                JSONArray().apply {
                    daily.forEach { d ->
                        put(
                            JSONObject().apply {
                                put("date", d.date)
                                put("attemptedCount", d.attemptedCount)
                                put("correctCount", d.correctCount)
                                put("totalXp", d.totalXp)
                                put("practiceDurationMs", d.practiceDurationMs)
                            }
                        )
                    }
                }
            )
            put(
                "recentAttempts",
                JSONArray().apply {
                    attempts.take(200).forEach { a ->
                        put(
                            JSONObject().apply {
                                put("id", a.id)
                                put("category", a.category.name)
                                put("difficulty", a.difficulty.name)
                                put("prompt", a.prompt)
                                put("expectedAnswer", a.expectedAnswer)
                                put("submittedAnswer", a.submittedAnswer)
                                put("isCorrect", a.isCorrect)
                                put("responseTimeMs", a.responseTimeMs)
                                put("attemptedAt", a.attemptedAt)
                            }
                        )
                    }
                }
            )
            put("pendingReviewCount", reviews.size)
        }
        return root.toString(2)
    }

    override suspend fun resetAllLearningData() {
        database.withTransaction {
            attemptDao.clearAll()
            profileDao.clearAll()
            historyDao.clearAll()
            reviewDao.clearAll()
            dailyDao.clearAll()
            bookmarkDao.clearAll()
            ensureSeededProfiles()
        }
        preferencesDataStore.resetDefaults()
    }

    private suspend fun ensureSeededProfiles() {
        val initialEntities = MathCategory.entries.map { category ->
            SkillProfile.initial(category).toEntity()
        }
        profileDao.insertAllIgnore(initialEntities)
    }
}

private fun SkillProfileEntity.toDomain(): SkillProfile = SkillProfile(
    category = category,
    skillScore = skillScore,
    accuracy = accuracy,
    averageResponseTimeMs = averageResponseTimeMs,
    currentDifficulty = currentDifficulty,
    correctStreak = correctStreak,
    wrongStreak = wrongStreak,
    attemptsCount = attemptsCount,
    lastPracticedAtEpochMs = lastPracticedAt
)

private fun SkillProfile.toEntity(): SkillProfileEntity = SkillProfileEntity(
    category = category,
    skillScore = skillScore,
    accuracy = accuracy,
    averageResponseTimeMs = averageResponseTimeMs,
    currentDifficulty = currentDifficulty,
    correctStreak = correctStreak,
    wrongStreak = wrongStreak,
    attemptsCount = attemptsCount,
    lastPracticedAt = lastPracticedAtEpochMs
)

private fun QuestionAttemptEntity.toDomain(): QuestionAttempt = QuestionAttempt(
    id = id,
    questionId = questionId,
    category = category,
    difficulty = difficulty,
    prompt = prompt,
    expectedAnswer = expectedAnswer,
    submittedAnswer = submittedAnswer,
    isCorrect = isCorrect,
    responseTimeMs = responseTimeMs,
    attemptedAtEpochMs = attemptedAt
)

private fun QuestionAttempt.toEntity(): QuestionAttemptEntity = QuestionAttemptEntity(
    id = id,
    questionId = questionId,
    category = category,
    difficulty = difficulty,
    prompt = prompt,
    expectedAnswer = expectedAnswer,
    submittedAnswer = submittedAnswer,
    isCorrect = isCorrect,
    responseTimeMs = responseTimeMs,
    attemptedAt = attemptedAtEpochMs
)

private fun ReviewQueueEntity.toDomain(): ReviewItem = ReviewItem(
    id = id,
    category = category,
    difficulty = difficulty,
    conceptKey = conceptKey,
    promptSnapshot = promptSnapshot,
    expectedAnswer = expectedAnswer,
    explanationSnapshot = explanationSnapshot,
    reviewAtEpochMs = reviewAt,
    priority = priority,
    failureCount = failureCount
)

private fun ReviewItem.toEntity(): ReviewQueueEntity = ReviewQueueEntity(
    id = id,
    category = category,
    difficulty = difficulty,
    conceptKey = conceptKey,
    promptSnapshot = promptSnapshot,
    expectedAnswer = expectedAnswer,
    explanationSnapshot = explanationSnapshot,
    reviewAt = reviewAtEpochMs,
    priority = priority,
    failureCount = failureCount
)

private fun DailyProgressEntity.toDomain(): DailyProgress = DailyProgress(
    dateKey = date,
    attemptedCount = attemptedCount,
    correctCount = correctCount,
    totalXp = totalXp,
    practiceDurationMs = practiceDurationMs
)

private fun BookmarkedQuestionEntity.toDomain(): BookmarkedQuestion = BookmarkedQuestion(
    id = id,
    category = category,
    difficulty = difficulty,
    prompt = prompt,
    answer = answer,
    explanation = explanation,
    savedAtEpochMs = savedAt
)

private fun BookmarkedQuestion.toEntity(): BookmarkedQuestionEntity = BookmarkedQuestionEntity(
    id = id,
    category = category,
    difficulty = difficulty,
    prompt = prompt,
    answer = answer,
    explanation = explanation,
    savedAt = savedAtEpochMs
)
