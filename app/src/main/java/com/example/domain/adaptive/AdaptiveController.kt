package com.example.domain.adaptive

import com.example.domain.model.AdaptivePace
import com.example.domain.model.AdaptiveQuestionPlan
import com.example.domain.model.AdaptiveStage
import com.example.domain.model.Difficulty
import com.example.domain.model.DifficultyChange
import com.example.domain.model.MathCategory
import com.example.domain.model.QuestionAttempt
import com.example.domain.model.QuestionStyle
import com.example.domain.model.SkillProfile
import com.example.domain.model.UserPreferences
import kotlin.random.Random

data class AdaptiveUpdateResult(
    val updatedProfile: SkillProfile,
    val difficultyChange: DifficultyChange,
    val rationale: String
)

/**
 * Multi-parameter adaptive controller.
 * 1) Always starts with simple direct questions during initial warm-up / cold start.
 * 2) Evaluates rolling accuracy, response speed vs target, recent trend, consistency, and streaks
 *    under configurable AdaptivePace, speed sensitivity, and style progression parameters.
 * 3) Steps both Difficulty (BEGINNER..EXPERT) and structural Complexity Tier (1..4) / QuestionStyle
 *    up smoothly when parameters are strong, and scaffolds down when the user struggles.
 */
class AdaptiveController(
    private val windowSize: Int = 10
) {

    fun computeGlobalStage(
        allProfiles: List<SkillProfile>,
        preferences: UserPreferences
    ): AdaptiveStage {
        val totalAttempts = allProfiles.sumOf { it.attemptsCount }
        if (preferences.startSimpleEnabled && totalAttempts < preferences.warmUpQuestionCount) {
            return AdaptiveStage.WARM_UP
        }
        val practiced = allProfiles.filter { it.attemptsCount > 0 }
        if (practiced.isEmpty()) return AdaptiveStage.FOUNDATION

        val avgScore = practiced.map { it.skillScore }.average().toFloat()
        val avgDiff = practiced.map { it.currentDifficulty.level }.average()
        return when {
            totalAttempts < preferences.warmUpQuestionCount + 5 || avgScore < 55f -> AdaptiveStage.FOUNDATION
            avgScore < 72f || avgDiff < 2.2 -> AdaptiveStage.BUILDING
            avgScore < 85f || avgDiff < 3.2 -> AdaptiveStage.ADVANCED
            else -> AdaptiveStage.MASTERY
        }
    }

    fun resolveQuestionPlan(
        category: MathCategory,
        categoryProfile: SkillProfile,
        allProfiles: List<SkillProfile>,
        recentAttemptsForCategory: List<QuestionAttempt>,
        preferences: UserPreferences,
        random: Random = Random.Default
    ): AdaptiveQuestionPlan {
        val minBound = minOf(preferences.minDifficulty, preferences.maxDifficulty, compareBy { it.level })
        val maxBound = maxOf(preferences.minDifficulty, preferences.maxDifficulty, compareBy { it.level })
        val globalStage = computeGlobalStage(allProfiles, preferences)
        val totalGlobalAttempts = allProfiles.sumOf { it.attemptsCount }

        // Phase 1: Simple Start Warm-Up (unless manual difficulty override forces a fixed level)
        if (preferences.startSimpleEnabled &&
            preferences.manualDifficultyOverride == null &&
            totalGlobalAttempts < preferences.warmUpQuestionCount
        ) {
            return AdaptiveQuestionPlan(
                stage = AdaptiveStage.WARM_UP,
                difficulty = minBound,
                complexityTier = 1,
                questionStyle = QuestionStyle.DIRECT,
                rationale = "Warm-up ${totalGlobalAttempts + 1}/${preferences.warmUpQuestionCount}: simple ${category.displayName.lowercase()}"
            )
        }

        val clampedDiff = (preferences.manualDifficultyOverride ?: categoryProfile.currentDifficulty)
            .coerceInRange(minBound, maxBound)

        // New category cold-start protection: first 2 questions in any topic stay direct & accessible
        if (preferences.startSimpleEnabled &&
            preferences.manualDifficultyOverride == null &&
            categoryProfile.attemptsCount < 2
        ) {
            val introDiff = if (clampedDiff.level > Difficulty.BASIC.level) {
                Difficulty.BASIC.coerceInRange(minBound, maxBound)
            } else {
                clampedDiff
            }
            return AdaptiveQuestionPlan(
                stage = globalStage,
                difficulty = introDiff,
                complexityTier = 1,
                questionStyle = QuestionStyle.DIRECT,
                rationale = "Topic warm-up: direct ${introDiff.displayName.lowercase()} baseline"
            )
        }

        if (!preferences.progressiveStyleEnabled) {
            return AdaptiveQuestionPlan(
                stage = globalStage,
                difficulty = clampedDiff,
                complexityTier = 1,
                questionStyle = QuestionStyle.DIRECT,
                rationale = "Direct style mode at ${clampedDiff.displayName}"
            )
        }

        val recentWindow = recentAttemptsForCategory.takeLast(6)
        val recentAccuracy = if (recentWindow.isNotEmpty()) {
            recentWindow.count { it.isCorrect }.toFloat() / recentWindow.size.toFloat()
        } else {
            categoryProfile.accuracy
        }

        // Supportive scaffolding: drop complexity immediately on active wrong streak or low recent accuracy
        if (categoryProfile.wrongStreak >= 1 || (recentWindow.size >= 3 && recentAccuracy < 0.55f)) {
            return AdaptiveQuestionPlan(
                stage = globalStage,
                difficulty = clampedDiff,
                complexityTier = 1,
                questionStyle = QuestionStyle.DIRECT,
                rationale = "Reinforcing fundamentals with clean direct format"
            )
        }

        val paceOffset = preferences.adaptivePace.tierOffset
        val tier2Threshold = 52f + paceOffset
        val tier3Threshold = 68f + paceOffset
        val tier4Threshold = 80f + paceOffset

        val effectiveScore = categoryProfile.skillScore +
            (categoryProfile.correctStreak * 2.5f).coerceAtMost(10f) +
            if (preferences.speedWeightEnabled && categoryProfile.averageResponseTimeMs in 1L..9000L) 3f else 0f

        val complexityTier = when {
            categoryProfile.attemptsCount >= 6 &&
                effectiveScore >= tier4Threshold &&
                recentAccuracy >= 0.80f &&
                clampedDiff.level >= Difficulty.INTERMEDIATE.level -> 4

            categoryProfile.attemptsCount >= 4 &&
                effectiveScore >= tier3Threshold &&
                recentAccuracy >= 0.72f &&
                clampedDiff.level >= Difficulty.BASIC.level -> 3

            categoryProfile.attemptsCount >= 2 &&
                (effectiveScore >= tier2Threshold || categoryProfile.correctStreak >= 2) -> 2

            else -> 1
        }

        val style = chooseStyleForTier(complexityTier, random)
        return AdaptiveQuestionPlan(
            stage = globalStage,
            difficulty = clampedDiff,
            complexityTier = complexityTier,
            questionStyle = style,
            rationale = "Tier $complexityTier (${style.displayName}) · ${clampedDiff.displayName}"
        )
    }

    private fun chooseStyleForTier(tier: Int, random: Random): QuestionStyle {
        val roll = random.nextFloat()
        return when (tier) {
            1 -> QuestionStyle.DIRECT
            2 -> if (roll < 0.65f) QuestionStyle.MISSING_OPERAND else QuestionStyle.DIRECT
            3 -> when {
                roll < 0.55f -> QuestionStyle.MULTI_STEP
                roll < 0.85f -> QuestionStyle.MISSING_OPERAND
                else -> QuestionStyle.DIRECT
            }
            else -> when {
                roll < 0.60f -> QuestionStyle.APPLIED_CHALLENGE
                roll < 0.88f -> QuestionStyle.MULTI_STEP
                else -> QuestionStyle.MISSING_OPERAND
            }
        }
    }

    fun updateProfile(
        currentProfile: SkillProfile,
        newAttempt: QuestionAttempt,
        recentAttemptsForCategory: List<QuestionAttempt>,
        expectedTimeSeconds: Int,
        preferences: UserPreferences
    ): AdaptiveUpdateResult {
        val window = (recentAttemptsForCategory + newAttempt)
            .distinctBy { it.id }
            .sortedBy { it.attemptedAtEpochMs }
            .takeLast(windowSize)

        val totalAttempts = currentProfile.attemptsCount + 1
        val newCorrectStreak = if (newAttempt.isCorrect) currentProfile.correctStreak + 1 else 0
        val newWrongStreak = if (!newAttempt.isCorrect) currentProfile.wrongStreak + 1 else 0

        val windowAccuracy = window.count { it.isCorrect }.toFloat() / window.size.toFloat()
        val overallAccuracy = ((currentProfile.accuracy * currentProfile.attemptsCount) +
            (if (newAttempt.isCorrect) 1f else 0f)) / totalAttempts.toFloat()

        val avgResponseMs = if (currentProfile.attemptsCount == 0) {
            newAttempt.responseTimeMs
        } else {
            ((currentProfile.averageResponseTimeMs * currentProfile.attemptsCount) + newAttempt.responseTimeMs) /
                totalAttempts
        }

        val expectedMs = (expectedTimeSeconds.coerceAtLeast(1) * 1000L).toFloat()
        val speedScores = window.map { attempt ->
            if (!attempt.isCorrect) {
                0f
            } else {
                val ratio = expectedMs / attempt.responseTimeMs.coerceAtLeast(800L).toFloat()
                ratio.coerceIn(0.45f, 1.0f)
            }
        }
        val normalizedSpeed = speedScores.average().toFloat()

        val recentTrend = if (window.size >= 4) {
            val mid = window.size / 2
            val firstHalfAcc = window.take(mid).count { it.isCorrect }.toFloat() / mid
            val secondHalfAcc = window.drop(mid).count { it.isCorrect }.toFloat() / (window.size - mid)
            ((secondHalfAcc - firstHalfAcc) * 0.5f + 0.5f).coerceIn(0f, 1f)
        } else {
            windowAccuracy
        }

        val consistency = if (window.size >= 3) {
            var transitions = 0
            for (i in 1 until window.size) {
                if (window[i].isCorrect != window[i - 1].isCorrect) transitions++
            }
            val swingRate = transitions.toFloat() / (window.size - 1).toFloat()
            (1f - swingRate * 0.6f).coerceIn(0.2f, 1f)
        } else {
            0.5f
        }

        val speedWeight = if (preferences.speedWeightEnabled) 0.20f else 0.05f
        val accuracyWeight = if (preferences.speedWeightEnabled) 0.50f else 0.65f
        val computedScore = (
            (accuracyWeight * windowAccuracy) +
                (speedWeight * normalizedSpeed) +
                (0.20f * recentTrend) +
                (0.10f * consistency)
            ) * 100f
        val clampedScore = computedScore.coerceIn(5f, 100f)

        val (nextDifficulty, change, rationale) = resolveDifficultyTransition(
            currentDifficulty = currentProfile.currentDifficulty,
            newCorrectStreak = newCorrectStreak,
            newWrongStreak = newWrongStreak,
            windowSize = window.size,
            windowAccuracy = windowAccuracy,
            avgWindowTimeMs = window.map { it.responseTimeMs }.average().toLong(),
            expectedMs = expectedMs.toLong(),
            isLastCorrect = newAttempt.isCorrect,
            preferences = preferences
        )

        val finalCorrectStreak = if (change == DifficultyChange.INCREASED) 0 else newCorrectStreak
        val finalWrongStreak = if (change == DifficultyChange.DECREASED) 0 else newWrongStreak

        val updated = currentProfile.copy(
            skillScore = clampedScore,
            accuracy = overallAccuracy,
            averageResponseTimeMs = avgResponseMs,
            currentDifficulty = nextDifficulty,
            correctStreak = finalCorrectStreak,
            wrongStreak = finalWrongStreak,
            attemptsCount = totalAttempts,
            lastPracticedAtEpochMs = newAttempt.attemptedAtEpochMs
        )

        return AdaptiveUpdateResult(
            updatedProfile = updated,
            difficultyChange = change,
            rationale = rationale
        )
    }

    private fun resolveDifficultyTransition(
        currentDifficulty: Difficulty,
        newCorrectStreak: Int,
        newWrongStreak: Int,
        windowSize: Int,
        windowAccuracy: Float,
        avgWindowTimeMs: Long,
        expectedMs: Long,
        isLastCorrect: Boolean,
        preferences: UserPreferences
    ): Triple<Difficulty, DifficultyChange, String> {
        val minBound = minOf(preferences.minDifficulty, preferences.maxDifficulty, compareBy { it.level })
        val maxBound = maxOf(preferences.minDifficulty, preferences.maxDifficulty, compareBy { it.level })

        if (preferences.manualDifficultyOverride != null) {
            return Triple(
                currentDifficulty,
                DifficultyChange.UNCHANGED,
                "Manual difficulty override active (${preferences.manualDifficultyOverride.displayName})"
            )
        }

        if (windowSize < 3) {
            return Triple(
                currentDifficulty.coerceInRange(minBound, maxBound),
                DifficultyChange.UNCHANGED,
                "Calibrating baseline ($windowSize/3 initial attempts)"
            )
        }

        val pace: AdaptivePace = preferences.adaptivePace
        val isReasonablyFast = !preferences.speedWeightEnabled ||
            avgWindowTimeMs <= (expectedMs * pace.speedToleranceMultiplier).toLong()

        if (newCorrectStreak >= pace.streakToAdvance &&
            windowAccuracy >= pace.minAccuracyToAdvance &&
            isReasonablyFast
        ) {
            val stepped = currentDifficulty.stepUp(maxBound)
            if (stepped != currentDifficulty) {
                return Triple(
                    stepped,
                    DifficultyChange.INCREASED,
                    "${pace.streakToAdvance} correct in a row (${(windowAccuracy * 100).toInt()}% accuracy)"
                )
            }
        }

        if (newCorrectStreak >= pace.quickStreakToAdvance &&
            windowSize >= 5 &&
            windowAccuracy >= pace.quickAccuracyToAdvance &&
            isReasonablyFast
        ) {
            val stepped = currentDifficulty.stepUp(maxBound)
            if (stepped != currentDifficulty) {
                return Triple(
                    stepped,
                    DifficultyChange.INCREASED,
                    "Sustained ${(windowAccuracy * 100).toInt()}% mastery over recent window"
                )
            }
        }

        if (isLastCorrect && !isReasonablyFast) {
            return Triple(
                currentDifficulty.coerceInRange(minBound, maxBound),
                DifficultyChange.UNCHANGED,
                "Accurate solve — building speed fluency before advancing"
            )
        }

        if (newWrongStreak >= pace.missesToStepDown) {
            val steppedDown = currentDifficulty.stepDown(minBound)
            if (steppedDown != currentDifficulty) {
                return Triple(
                    steppedDown,
                    DifficultyChange.DECREASED,
                    "${pace.missesToStepDown} consecutive misses — stepping down for reinforcement"
                )
            }
        }

        if (newWrongStreak >= 2 && windowSize >= 5 && windowAccuracy < 0.45f) {
            val steppedDown = currentDifficulty.stepDown(minBound)
            if (steppedDown != currentDifficulty) {
                return Triple(
                    steppedDown,
                    DifficultyChange.DECREASED,
                    "Low rolling accuracy (${(windowAccuracy * 100).toInt()}%) — adjusting level"
                )
            }
        }

        val clamped = currentDifficulty.coerceInRange(minBound, maxBound)
        return Triple(
            clamped,
            DifficultyChange.UNCHANGED,
            "Difficulty steady at ${clamped.displayName}"
        )
    }

    private fun Difficulty.coerceInRange(minBound: Difficulty, maxBound: Difficulty): Difficulty {
        val boundedLevel = level.coerceIn(minBound.level, maxBound.level)
        return Difficulty.entries.first { it.level == boundedLevel }
    }
}
