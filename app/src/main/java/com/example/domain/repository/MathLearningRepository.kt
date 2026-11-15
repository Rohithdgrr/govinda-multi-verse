package com.example.domain.repository

import com.example.domain.model.BookmarkedQuestion
import com.example.domain.model.DailyProgress
import com.example.domain.model.MathCategory
import com.example.domain.model.QuestionAttempt
import com.example.domain.model.ReviewItem
import com.example.domain.model.SkillProfile
import com.example.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface MathLearningRepository {
    fun observeSkillProfiles(): Flow<List<SkillProfile>>
    fun observeRecentAttempts(limit: Int = 50): Flow<List<QuestionAttempt>>
    fun observeReviewQueue(): Flow<List<ReviewItem>>
    fun observeBookmarks(): Flow<List<BookmarkedQuestion>>
    fun observeDailyProgress(limit: Int = 14): Flow<List<DailyProgress>>
    fun observePreferences(): Flow<UserPreferences>

    suspend fun getSkillProfiles(): List<SkillProfile>
    suspend fun getSkillProfile(category: MathCategory): SkillProfile
    suspend fun getRecentAttemptsForCategory(category: MathCategory, limit: Int): List<QuestionAttempt>
    suspend fun getReviewQueue(): List<ReviewItem>
    suspend fun getRecentFingerprints(limit: Int): Set<String>
    suspend fun saveFingerprint(fingerprint: String, category: MathCategory, generatorVersion: Int)

    suspend fun recordAttemptTransaction(
        attempt: QuestionAttempt,
        updatedProfile: SkillProfile,
        dateKey: String,
        xpEarned: Int,
        reviewItemToUpsert: ReviewItem?,
        resolvedReviewCategory: MathCategory?
    )

    suspend fun toggleBookmark(question: BookmarkedQuestion): Boolean
    suspend fun removeBookmark(questionId: String)
    suspend fun isBookmarked(questionId: String): Boolean
    suspend fun dismissReviewItem(id: String)

    suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences)
    suspend fun exportAllDataAsJson(): String
    suspend fun resetAllLearningData()
}
