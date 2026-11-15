package com.example.domain.selector

import com.example.domain.model.MathCategory
import com.example.domain.model.ReviewItem
import com.example.domain.model.SkillProfile
import com.example.domain.model.SkillTier
import com.example.domain.model.TopicMixMode
import com.example.domain.model.UserPreferences
import kotlin.random.Random

/**
 * Parameter-driven topic selector:
 * - In PROGRESSIVE_UNLOCK mode (default), begins with simple core addition/subtraction during initial
 *   warm-up, then unlocks multiplication/division, applied decimals/percentages/fractions, and finally
 *   powers/mixed operations as global attempts and mastery grow.
 * - Balances weakness recovery, developing skills, mastered retention, overdue review urgency, and variety.
 */
class TopicSelector {

    fun selectNextCategory(
        profiles: List<SkillProfile>,
        reviewQueue: List<ReviewItem>,
        preferences: UserPreferences,
        lastCategory: MathCategory?,
        nowEpochMs: Long,
        random: Random = Random.Default
    ): MathCategory {
        val enabled = preferences.enabledCategories.ifEmpty {
            MathCategory.entries.toSet()
        }

        val allEnabledProfiles = profiles.filter { it.category in enabled }
        if (allEnabledProfiles.isEmpty()) {
            return enabled.first()
        }
        if (allEnabledProfiles.size == 1) {
            return allEnabledProfiles.first().category
        }

        val eligibleProfiles = resolveProgressivePool(
            enabledProfiles = allEnabledProfiles,
            allProfiles = profiles,
            preferences = preferences
        )
        if (eligibleProfiles.size == 1) {
            return eligibleProfiles.first().category
        }

        val grouped = eligibleProfiles.groupBy { it.tier }
        val nonEmptyTiers = grouped.keys
        val pickedTier = pickTierBucket(preferences, nonEmptyTiers, random)
        val candidates = grouped[pickedTier]?.takeIf { it.isNotEmpty() } ?: eligibleProfiles

        val filteredCandidates = if (lastCategory != null && candidates.size > 1) {
            candidates.filter { it.category != lastCategory }
        } else {
            candidates
        }

        val reviewByCategory = reviewQueue.groupBy { it.category }

        val weighted = filteredCandidates.map { profile ->
            val weakness = ((100f - profile.skillScore) / 100f).coerceIn(0.05f, 1.0f)

            val overdueItems = reviewByCategory[profile.category].orEmpty()
                .filter { it.reviewAtEpochMs <= nowEpochMs }
            val reviewUrgency = when {
                overdueItems.isNotEmpty() -> (0.5f + 0.15f * overdueItems.size).coerceAtMost(1.0f)
                reviewByCategory[profile.category].orEmpty().isNotEmpty() -> 0.35f
                else -> 0.0f
            }

            val hoursSincePractice = if (profile.lastPracticedAtEpochMs <= 0L) {
                24f
            } else {
                ((nowEpochMs - profile.lastPracticedAtEpochMs).coerceAtLeast(0L) / 3_600_000f)
            }
            val timeFactor = (hoursSincePractice / 24f).coerceIn(0.05f, 1.0f)

            val varietyBonus = if (profile.category != lastCategory) 1.0f else 0.1f
            val unpracticedBoost = if (profile.attemptsCount == 0) 0.25f else 0f

            val score = (
                0.40f * weakness +
                    0.25f * reviewUrgency +
                    0.15f * timeFactor +
                    0.10f * varietyBonus +
                    unpracticedBoost
                ).coerceAtLeast(0.05f)

            profile.category to score
        }

        val totalWeight = weighted.sumOf { it.second.toDouble() }
        if (totalWeight <= 0.0) {
            return filteredCandidates.first().category
        }

        var cursor = random.nextDouble() * totalWeight
        for ((category, weight) in weighted) {
            cursor -= weight
            if (cursor <= 0.0) {
                return category
            }
        }
        return weighted.last().first
    }

    private fun resolveProgressivePool(
        enabledProfiles: List<SkillProfile>,
        allProfiles: List<SkillProfile>,
        preferences: UserPreferences
    ): List<SkillProfile> {
        if (preferences.topicMixMode != TopicMixMode.PROGRESSIVE_UNLOCK) {
            return enabledProfiles
        }

        val totalAttempts = allProfiles.sumOf { it.attemptsCount }
        val warmUp = if (preferences.startSimpleEnabled) preferences.warmUpQuestionCount else 0
        val practiced = allProfiles.filter { it.attemptsCount > 0 }
        val avgScore = if (practiced.isEmpty()) 40f else practiced.map { it.skillScore }.average().toFloat()

        val tier0 = setOf(MathCategory.ADDITION, MathCategory.SUBTRACTION)
        val tier1 = tier0 + setOf(
            MathCategory.MULTIPLICATION,
            MathCategory.DIVISION,
            MathCategory.SQUARES,
            MathCategory.CUBES,
            MathCategory.ROOTS
        )
        val tier2 = tier1 + setOf(
            MathCategory.PERCENTAGE,
            MathCategory.DECIMAL,
            MathCategory.FRACTION,
            MathCategory.BODMAS,
            MathCategory.AVERAGE,
            MathCategory.NUMBER_SYSTEM
        )

        val candidatePool = when {
            preferences.startSimpleEnabled && totalAttempts < warmUp -> {
                enabledProfiles.filter { it.category in tier0 }
                    .ifEmpty { enabledProfiles.filter { it.category in tier1 } }
            }
            totalAttempts < warmUp + 3 || avgScore < 48f -> {
                enabledProfiles.filter { it.category in tier1 }
                    .ifEmpty { enabledProfiles.filter { it.category in tier2 } }
            }
            totalAttempts < warmUp + 7 || avgScore < 58f -> {
                enabledProfiles.filter { it.category in tier2 }
            }
            else -> enabledProfiles
        }

        return candidatePool.ifEmpty { enabledProfiles }
    }

    private fun pickTierBucket(
        preferences: UserPreferences,
        availableTiers: Set<SkillTier>,
        random: Random
    ): SkillTier {
        val (weakW, devW, strongW) = when (preferences.topicMixMode) {
            TopicMixMode.FOCUS_WEAK -> Triple(0.70f, 0.20f, 0.10f)
            else -> Triple(
                preferences.weakShare.coerceAtLeast(0.05f),
                preferences.developingShare.coerceAtLeast(0.05f),
                preferences.strongShare.coerceAtLeast(0.05f)
            )
        }
        val rawShares = buildList {
            if (SkillTier.WEAK in availableTiers) add(SkillTier.WEAK to weakW)
            if (SkillTier.DEVELOPING in availableTiers) add(SkillTier.DEVELOPING to devW)
            if (SkillTier.STRONG in availableTiers) add(SkillTier.STRONG to strongW)
        }
        val total = rawShares.sumOf { it.second.toDouble() }
        var roll = random.nextDouble() * total
        for ((tier, weight) in rawShares) {
            roll -= weight
            if (roll <= 0.0) return tier
        }
        return rawShares.last().first
    }
}
