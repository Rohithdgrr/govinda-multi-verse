package com.example.domain.usecase

import com.example.domain.adaptive.AdaptiveController
import com.example.domain.adaptive.AdaptiveUpdateResult
import com.example.domain.evaluator.AnswerEvaluator
import com.example.domain.evaluator.EvaluationResult
import com.example.domain.generator.GeneratorConfig
import com.example.domain.generator.GeneratorRegistry
import com.example.domain.model.Difficulty
import com.example.domain.model.MathCategory
import com.example.domain.model.MathQuestion
import com.example.domain.model.QuestionAttempt
import com.example.domain.model.ReviewItem
import com.example.domain.model.UserPreferences
import com.example.domain.repository.MathLearningRepository
import com.example.domain.selector.TopicSelector
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class GenerateNextQuestionUseCase(
    private val repository: MathLearningRepository,
    private val registry: GeneratorRegistry,
    private val topicSelector: TopicSelector,
    private val adaptiveController: AdaptiveController = AdaptiveController(),
    private val random: Random = Random.Default
) {
    suspend operator fun invoke(
        preferences: UserPreferences,
        forcedCategory: MathCategory? = null,
        lastCategory: MathCategory? = null
    ): MathQuestion {
        val profiles = repository.getSkillProfiles()
        val reviewQueue = repository.getReviewQueue()
        val nowMs = System.currentTimeMillis()

        val selectedCategory = forcedCategory ?: topicSelector.selectNextCategory(
            profiles = profiles,
            reviewQueue = reviewQueue,
            preferences = preferences,
            lastCategory = lastCategory,
            nowEpochMs = nowMs,
            random = random
        )

        val profile = profiles.firstOrNull { it.category == selectedCategory }
            ?: repository.getSkillProfile(selectedCategory)
        val recentForCategory = repository.getRecentAttemptsForCategory(selectedCategory, limit = 10)

        val plan = adaptiveController.resolveQuestionPlan(
            category = selectedCategory,
            categoryProfile = profile,
            allProfiles = profiles,
            recentAttemptsForCategory = recentForCategory,
            preferences = preferences,
            random = random
        )

        val maxLevel = maxOf(preferences.minDifficulty.level, preferences.maxDifficulty.level)
        val targetDifficulty = plan.difficulty

        val generator = registry.generatorFor(selectedCategory)
        val config = GeneratorConfig(
            allowNegativeSubtraction = preferences.allowNegativeSubtraction,
            complexityTier = plan.complexityTier,
            questionStyle = plan.questionStyle,
            focusTag = preferences.surveyFocusTag
        )
        val recentFingerprints = repository.getRecentFingerprints(limit = 100)

        var chosen: MathQuestion? = null
        val maxRetries = 12
        for (attempt in 0 until maxRetries) {
            val candidate = runCatching {
                generator.generate(
                    difficulty = targetDifficulty,
                    config = config,
                    random = random
                )
            }.getOrNull() ?: continue
            if (candidate.fingerprint !in recentFingerprints) {
                chosen = candidate
                break
            }
            if (attempt == maxRetries - 1) {
                val altDifficulty = if (targetDifficulty.level < maxLevel) {
                    targetDifficulty.stepUp()
                } else {
                    targetDifficulty
                }
                chosen = runCatching { generator.generate(altDifficulty, config, random) }.getOrNull() ?: candidate
            }
        }

        val finalQuestion = chosen ?: registry.generatorFor(selectedCategory).generate(
            difficulty = targetDifficulty,
            config = config,
            random = random
        )
        runCatching {
            repository.saveFingerprint(
                fingerprint = finalQuestion.fingerprint,
                category = finalQuestion.category,
                generatorVersion = finalQuestion.generatorVersion
            )
        }
        return finalQuestion
    }
}

data class SubmissionOutcome(
    val evaluation: EvaluationResult,
    val adaptiveResult: AdaptiveUpdateResult,
    val xpEarned: Int
)

class SubmitAnswerUseCase(
    private val repository: MathLearningRepository,
    private val evaluator: AnswerEvaluator,
    private val adaptiveController: AdaptiveController
) {
    suspend operator fun invoke(
        question: MathQuestion,
        rawInput: String,
        responseTimeMs: Long,
        preferences: UserPreferences
    ): SubmissionOutcome {
        val nowMs = System.currentTimeMillis()
        val evaluation = evaluator.evaluate(question, rawInput)

        val attempt = QuestionAttempt(
            id = UUID.randomUUID().toString(),
            questionId = question.id,
            category = question.category,
            difficulty = question.difficulty,
            prompt = question.prompt,
            expectedAnswer = question.answer,
            submittedAnswer = evaluation.normalizedSubmitted.ifEmpty { rawInput.trim() },
            isCorrect = evaluation.isCorrect,
            responseTimeMs = responseTimeMs.coerceAtLeast(300L),
            attemptedAtEpochMs = nowMs
        )

        val currentProfile = repository.getSkillProfile(question.category)
        val recentForCategory = repository.getRecentAttemptsForCategory(question.category, limit = 10)

        val adaptiveResult = adaptiveController.updateProfile(
            currentProfile = currentProfile,
            newAttempt = attempt,
            recentAttemptsForCategory = recentForCategory,
            expectedTimeSeconds = question.expectedTimeSeconds,
            preferences = preferences
        )

        val xpEarned = if (evaluation.isCorrect) {
            val streakBonus = (adaptiveResult.updatedProfile.correctStreak * 2).coerceAtMost(12)
            question.difficulty.baseXp + streakBonus
        } else {
            2 // Effort XP for reviewing mistakes without punishing thoughtful attempts
        }

        val reviewItemToUpsert = if (!evaluation.isCorrect) {
            val conceptKey = "${question.category.name}:${question.difficulty.name}"
            // Schedule review in 15 minutes (or immediately if repeated failure)
            ReviewItem(
                id = UUID.randomUUID().toString(),
                category = question.category,
                difficulty = question.difficulty,
                conceptKey = conceptKey,
                promptSnapshot = question.prompt,
                expectedAnswer = question.answer,
                explanationSnapshot = question.explanation,
                reviewAtEpochMs = nowMs + 15 * 60 * 1000L,
                priority = 2 + adaptiveResult.updatedProfile.wrongStreak,
                failureCount = 1
            )
        } else {
            null
        }

        val resolvedCategory = if (evaluation.isCorrect) question.category else null
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(nowMs))

        repository.recordAttemptTransaction(
            attempt = attempt,
            updatedProfile = adaptiveResult.updatedProfile,
            dateKey = dateKey,
            xpEarned = xpEarned,
            reviewItemToUpsert = reviewItemToUpsert,
            resolvedReviewCategory = resolvedCategory
        )

        return SubmissionOutcome(
            evaluation = evaluation,
            adaptiveResult = adaptiveResult,
            xpEarned = xpEarned
        )
    }
}

class ExportLearningDataUseCase(
    private val repository: MathLearningRepository
) {
    suspend operator fun invoke(): String = repository.exportAllDataAsJson()
}

class ResetLearningProfileUseCase(
    private val repository: MathLearningRepository
) {
    suspend operator fun invoke() = repository.resetAllLearningData()
}
