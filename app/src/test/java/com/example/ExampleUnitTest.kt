package com.example

import com.example.domain.adaptive.AdaptiveController
import com.example.domain.evaluator.AnswerEvaluator
import com.example.domain.generator.GeneratorConfig
import com.example.domain.generator.GeneratorRegistry
import com.example.domain.model.Difficulty
import com.example.domain.model.DifficultyChange
import com.example.domain.model.Fraction
import com.example.domain.model.MathCategory
import com.example.domain.model.QuestionAttempt
import com.example.domain.model.QuestionMetadata
import com.example.domain.model.SkillProfile
import com.example.domain.model.UserPreferences
import com.example.domain.selector.TopicSelector
import com.example.lock.LockDecision
import com.example.lock.QuizLockSessionTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.util.UUID
import kotlin.random.Random

class MathEngineUnitTest {

    private val registry = GeneratorRegistry.default()
    private val evaluator = AnswerEvaluator()

    @Test
    fun propertyTest_allGeneratorsProduceValidAnswersAndMcqOptions() {
        val seeded = Random(42_026)
        for (category in MathCategory.entries) {
            val generator = registry.generatorFor(category)
            for (difficulty in Difficulty.entries) {
                repeat(60) {
                    val q = generator.generate(
                        difficulty = difficulty,
                        config = GeneratorConfig(allowNegativeSubtraction = false),
                        random = seeded
                    )

                    assertTrue("Prompt should not be blank", q.prompt.isNotBlank())
                    assertTrue("Explanation steps should not be empty", q.explanationSteps.isNotEmpty())
                    assertEquals("MCQ must have 4 options", 4, q.options.size)
                    assertEquals("MCQ options must be unique", 4, q.options.distinct().size)
                    assertTrue("MCQ options must contain exact answer", q.answer in q.options)

                    // Independently verify answer via metadata & evaluator
                    val eval = evaluator.evaluate(q, q.answer)
                    assertTrue("Evaluator must accept canonical answer for $category/$difficulty", eval.isCorrect)

                    when (val meta = q.metadata) {
                        is QuestionMetadata.BinaryIntMeta -> {
                            val expected = when (meta.operator) {
                                "+" -> meta.left + meta.right
                                "-" -> meta.left - meta.right
                                "×" -> meta.left * meta.right
                                else -> error("Unexpected op ${meta.operator}")
                            }
                            assertEquals(expected, meta.result)
                            assertEquals(expected.toString(), q.answer)
                        }
                        is QuestionMetadata.DivisionMeta -> {
                            assertEquals(0L, meta.dividend % meta.divisor)
                            assertEquals(meta.quotient, meta.dividend / meta.divisor)
                            assertEquals(meta.quotient.toString(), q.answer)
                        }
                        is QuestionMetadata.FractionMeta -> {
                            val norm = meta.result.normalized()
                            assertEquals(norm.toCanonicalString(), q.answer)
                        }
                        is QuestionMetadata.DecimalMeta -> {
                            val expected = when (meta.operator) {
                                "+" -> meta.left.add(meta.right)
                                "−" -> meta.left.subtract(meta.right)
                                "×" -> meta.left.multiply(meta.right)
                                else -> error("Unexpected decimal op ${meta.operator}")
                            }
                            assertEquals(0, expected.compareTo(BigDecimal(q.answer)))
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    @Test
    fun evaluator_handlesEquivalentFractionsAndDecimalsExactly() {
        val fracQuestion = registry.generatorFor(MathCategory.FRACTION)
            .generate(Difficulty.BEGINNER, random = Random(7))
        // Regardless of generated fraction, test Fraction equivalence parsing directly
        val half = Fraction.parse("2/4")!!
        assertEquals("1/2", half.toCanonicalString())

        val decQuestion = registry.generatorFor(MathCategory.DECIMAL)
            .generate(Difficulty.BASIC, random = Random(9))
        val trailingZeroInput = if (decQuestion.answer.contains(".")) {
            "${decQuestion.answer}00"
        } else {
            "${decQuestion.answer}.00"
        }
        assertTrue(evaluator.evaluate(decQuestion, trailingZeroInput).isCorrect)
        assertFalse(evaluator.evaluate(decQuestion, "99999.123").isCorrect)
        assertEquals(fracQuestion.answer, evaluator.evaluate(fracQuestion, fracQuestion.answer).canonicalExpected)
    }

    @Test
    fun adaptiveController_respectsHysteresisAndManualOverride() {
        val controller = AdaptiveController(windowSize = 10)
        var profile = SkillProfile.initial(MathCategory.ADDITION)
        val history = mutableListOf<QuestionAttempt>()
        val prefs = UserPreferences()

        // Single wrong answer must not drop below BEGINNER or trigger erratic drop
        val firstMiss = attempt(MathCategory.ADDITION, Difficulty.BASIC, isCorrect = false, timeMs = 4000L, ts = 1000L)
        val basicProfile = profile.copy(currentDifficulty = Difficulty.BASIC, attemptsCount = 3)
        val afterOneMiss = controller.updateProfile(basicProfile, firstMiss, history, 10, prefs)
        assertEquals(DifficultyChange.UNCHANGED, afterOneMiss.difficultyChange)
        assertEquals(Difficulty.BASIC, afterOneMiss.updatedProfile.currentDifficulty)

        // 5 consecutive fast accurate answers step up difficulty by 1 level
        profile = SkillProfile.initial(MathCategory.MULTIPLICATION)
        history.clear()
        var lastChange = DifficultyChange.UNCHANGED
        repeat(5) { idx ->
            val att = attempt(
                category = MathCategory.MULTIPLICATION,
                difficulty = profile.currentDifficulty,
                isCorrect = true,
                timeMs = 4500L,
                ts = (idx + 1) * 2000L
            )
            val res = controller.updateProfile(profile, att, history, expectedTimeSeconds = 8, preferences = prefs)
            history.add(att)
            profile = res.updatedProfile
            lastChange = res.difficultyChange
        }
        assertEquals(DifficultyChange.INCREASED, lastChange)
        assertEquals(Difficulty.BASIC, profile.currentDifficulty)

        // Manual override prevents automatic difficulty change
        val lockedPrefs = prefs.copy(manualDifficultyOverride = Difficulty.EXPERT)
        repeat(3) { idx ->
            val att = attempt(
                category = MathCategory.MULTIPLICATION,
                difficulty = Difficulty.EXPERT,
                isCorrect = false,
                timeMs = 15000L,
                ts = 50_000L + idx * 1000L
            )
            val res = controller.updateProfile(profile, att, history, 10, lockedPrefs)
            assertEquals(DifficultyChange.UNCHANGED, res.difficultyChange)
        }
    }

    @Test
    fun topicSelector_onlySelectsEnabledCategories() {
        val selector = TopicSelector()
        val enabled = setOf(MathCategory.PERCENTAGE, MathCategory.FRACTION)
        val prefs = UserPreferences(enabledCategories = enabled)
        val profiles = MathCategory.entries.map { SkillProfile.initial(it) }
        val random = Random(1234)

        repeat(40) {
            val selected = selector.selectNextCategory(
                profiles = profiles,
                reviewQueue = emptyList(),
                preferences = prefs,
                lastCategory = null,
                nowEpochMs = System.currentTimeMillis(),
                random = random
            )
            assertTrue("Selected $selected must be in enabled set", selected in enabled)
        }
    }

    @Test
    fun topicSelector_unlocksDiverseNewCategoriesAfterWarmUp() {
        val selector = TopicSelector()
        val prefs = UserPreferences(enabledCategories = MathCategory.entries.toSet())
        val practicedProfiles = MathCategory.entries.map { category ->
            if (category == MathCategory.ADDITION || category == MathCategory.SUBTRACTION) {
                SkillProfile.initial(category).copy(
                    attemptsCount = 12,
                    skillScore = 75f,
                    accuracy = 0.9f
                )
            } else {
                SkillProfile.initial(category)
            }
        }
        val selectedSet = mutableSetOf<MathCategory>()
        val random = Random(2026)
        repeat(160) {
            selectedSet += selector.selectNextCategory(
                profiles = practicedProfiles,
                reviewQueue = emptyList(),
                preferences = prefs,
                lastCategory = null,
                nowEpochMs = 5_000_000L,
                random = random
            )
        }

        val expectedNewCategories = listOf(
            MathCategory.SQUARES,
            MathCategory.CUBES,
            MathCategory.ROOTS,
            MathCategory.BODMAS,
            MathCategory.SIMPLE_INTEREST,
            MathCategory.SPEED_DISTANCE,
            MathCategory.AVERAGE,
            MathCategory.STATISTICS,
            MathCategory.NUMBER_SYSTEM,
            MathCategory.RATIO_PROPORTION
        )
        expectedNewCategories.forEach { cat ->
            assertTrue("Expected $cat to be unlocked and selectable after warm-up; selected=$selectedSet", cat in selectedSet)
        }
    }

    @Test
    fun quizLockTracker_promptsOncePerFreshOpenOnly() {
        val tracker = QuizLockSessionTracker(relaunchDebounceMs = 1500L)
        val locked = setOf("com.example.video", "com.example.social")
        val own = "com.aistudio.govinda.mathkx"

        // 1. Fresh open of locked app triggers quiz gate once
        val first = tracker.onPackageForegrounded("com.example.video", locked, true, own, nowMs = 1000L)
        assertEquals(LockDecision.LaunchQuizGate("com.example.video"), first)

        // 2. Rapid duplicate window transition does not re-trigger within debounce
        val dup = tracker.onPackageForegrounded("com.example.video", locked, true, own, nowMs = 1500L)
        assertEquals(LockDecision.NoAction, dup)

        // 3. Solving quiz unlocks the session; switching activities inside the same app does NOT ask again
        tracker.markUnlocked("com.example.video")
        assertEquals(
            LockDecision.NoAction,
            tracker.onPackageForegrounded("com.example.video", locked, true, own, nowMs = 2000L)
        )
        // Transient keyboard / systemui overlay does NOT break active unlocked session
        assertEquals(
            LockDecision.NoAction,
            tracker.onPackageForegrounded("com.google.android.inputmethod.latin", locked, true, own, nowMs = 2500L)
        )
        assertEquals(
            LockDecision.NoAction,
            tracker.onPackageForegrounded("com.example.video", locked, true, own, nowMs = 3000L)
        )

        // 4. Leaving to launcher ends the session; opening fresh again prompts 1 question
        assertEquals(
            LockDecision.NoAction,
            tracker.onPackageForegrounded("com.google.android.apps.nexuslauncher", locked, true, own, nowMs = 4000L)
        )
        assertEquals(
            LockDecision.LaunchQuizGate("com.example.video"),
            tracker.onPackageForegrounded("com.example.video", locked, true, own, nowMs = 5000L)
        )

        // 5. Screen off resets unlocked session so next open counts as fresh
        tracker.markUnlocked("com.example.video")
        tracker.onScreenOff()
        assertEquals(
            LockDecision.LaunchQuizGate("com.example.video"),
            tracker.onPackageForegrounded("com.example.video", locked, true, own, nowMs = 9000L)
        )

        // 6. Backing out of quiz gate resets pending debounce so next attempt still locks
        tracker.onBackCancelled()
        assertEquals(
            LockDecision.LaunchQuizGate("com.example.video"),
            tracker.onPackageForegrounded("com.example.video", locked, true, own, nowMs = 9200L)
        )
    }

    @Test
    fun personalizationEngine_allThemesContrastFontsLayoutsAndRoundTripWork() {
        // 12 Light + 14 Dark themes
        assertEquals(12, com.example.personalization.theme.ThemeRegistry.lightThemes.size)
        assertEquals(14, com.example.personalization.theme.ThemeRegistry.darkThemes.size)
        assertEquals(26, com.example.personalization.theme.ThemeRegistry.allBuiltInThemes.size)

        com.example.personalization.theme.ThemeRegistry.allBuiltInThemes.forEach { theme ->
            val ratio = com.example.personalization.theme.ThemeManager.contrastRatio(
                theme.primaryTextArgb,
                theme.backgroundArgb
            )
            assertTrue("Theme ${theme.name} contrast $ratio must be >= 4.5", ratio >= 4.5f)
        }

        // Theme switch modes: Light, Dark, FollowSystem, AutoDayNight, Scheduled, CustomOverrides, HighContrast
        val base = com.example.personalization.model.AppearanceProfile(
            themeId = "blueprint_blue",
            darkThemeId = "dracula",
            themeSwitchMode = com.example.personalization.model.ThemeSwitchMode.AUTO_DAY_NIGHT
        )
        val dayResolved = com.example.personalization.theme.ThemeManager.resolveActiveTheme(
            profile = base,
            customThemes = emptyList(),
            isSystemDark = false,
            currentHourOfDay = 12
        )
        assertEquals("blueprint_blue", dayResolved.id)

        val nightResolved = com.example.personalization.theme.ThemeManager.resolveActiveTheme(
            profile = base,
            customThemes = emptyList(),
            isSystemDark = false,
            currentHourOfDay = 22
        )
        assertEquals("dracula", nightResolved.id)

        val highContrast = com.example.personalization.theme.ThemeManager.resolveActiveTheme(
            profile = base.copy(
                effects = com.example.personalization.model.EffectSettings(highContrastMode = true)
            ),
            customThemes = emptyList(),
            isSystemDark = false,
            currentHourOfDay = 22
        )
        assertEquals(0xFF000000L, highContrast.backgroundArgb)
        assertEquals(0xFFFFFFFFL, highContrast.primaryTextArgb)

        // All 20 bundled fonts & typography building
        assertEquals(20, com.example.personalization.fonts.FontRegistry.allFonts.size)
        val typo = com.example.personalization.fonts.TypographyManager.buildTypography(
            com.example.personalization.model.TypographyProfile(
                headingFontId = "oswald",
                bodyFontId = "inter",
                equationFontId = "fira_code",
                technicalFontId = "jetbrains_mono",
                fontSizeScale = 1.1f,
                tabularNumerals = true
            )
        )
        assertEquals("tnum", typo.displayLarge.fontFeatureSettings)

        // Avatar deterministic seed generation
        val av1 = com.example.personalization.avatars.AvatarRegistry.generateFromSeed(
            999,
            com.example.personalization.model.AvatarProfile()
        )
        val av2 = com.example.personalization.avatars.AvatarRegistry.generateFromSeed(
            999,
            com.example.personalization.model.AvatarProfile()
        )
        assertEquals(av1, av2)

        // Layout widget move, toggle, resize
        val widgets = com.example.personalization.model.LayoutProfile().widgets
        val moved = com.example.personalization.layouts.LayoutManager.moveWidget(widgets, 0, 1)
        assertEquals(widgets[0].id, moved[1].id)
        val toggled = com.example.personalization.layouts.LayoutManager.toggleWidgetVisibility(moved, moved[0].id)
        assertEquals(!moved[0].visible, toggled[0].visible)
    }

    @Test
    fun propertyTest_allGeneratorsAdaptAcrossComplexityTiersAndStyles() {
        val seeded = Random(99_123)
        for (category in MathCategory.entries) {
            val generator = registry.generatorFor(category)
            for (difficulty in Difficulty.entries) {
                for (tier in 1..4) {
                    for (style in com.example.domain.model.QuestionStyle.entries) {
                        val q = generator.generate(
                            difficulty = difficulty,
                            config = GeneratorConfig(
                                allowNegativeSubtraction = false,
                                complexityTier = tier,
                                questionStyle = style
                            ),
                            random = seeded
                        )
                        assertTrue("Prompt must not be blank for $category/$difficulty/T$tier/$style", q.prompt.isNotBlank())
                        assertTrue("Explanation steps required", q.explanationSteps.isNotEmpty())
                        assertEquals("4 MCQ options required", 4, q.options.size)
                        assertEquals("4 unique MCQ options required", 4, q.options.distinct().size)
                        assertTrue("Correct answer must be in options", q.answer in q.options)
                        assertTrue(
                            "Evaluator must accept canonical answer '${q.answer}' for '${q.prompt}'",
                            evaluator.evaluate(q, q.answer).isCorrect
                        )
                    }
                }
            }
        }
    }

    @Test
    fun adaptiveMechanism_startsSimpleAndEvolvesDifficultyStyleComplexityByParameters() {
        val controller = AdaptiveController(windowSize = 10)
        val selector = TopicSelector()
        val prefs = UserPreferences(
            enabledCategories = MathCategory.entries.toSet(),
            startSimpleEnabled = true,
            warmUpQuestionCount = 5,
            adaptivePace = com.example.domain.model.AdaptivePace.BALANCED,
            progressiveStyleEnabled = true,
            speedWeightEnabled = true,
            topicMixMode = com.example.domain.model.TopicMixMode.PROGRESSIVE_UNLOCK
        )
        val initialProfiles = MathCategory.entries.map { SkillProfile.initial(it) }

        // 1. Fresh start: always chooses foundational arithmetic and produces Tier 1 DIRECT BEGINNER
        val seeded = Random(77)
        repeat(20) {
            val picked = selector.selectNextCategory(
                profiles = initialProfiles,
                reviewQueue = emptyList(),
                preferences = prefs,
                lastCategory = null,
                nowEpochMs = 1_000_000L,
                random = seeded
            )
            assertTrue(
                "Warm-up should start with simple addition or subtraction, got $picked",
                picked == MathCategory.ADDITION || picked == MathCategory.SUBTRACTION
            )
        }

        val warmPlan = controller.resolveQuestionPlan(
            category = MathCategory.ADDITION,
            categoryProfile = SkillProfile.initial(MathCategory.ADDITION),
            allProfiles = initialProfiles,
            recentAttemptsForCategory = emptyList(),
            preferences = prefs,
            random = seeded
        )
        assertEquals(com.example.domain.model.AdaptiveStage.WARM_UP, warmPlan.stage)
        assertEquals(Difficulty.BEGINNER, warmPlan.difficulty)
        assertEquals(1, warmPlan.complexityTier)
        assertEquals(com.example.domain.model.QuestionStyle.DIRECT, warmPlan.questionStyle)

        // 2. Simulate sustained fast accurate solves -> difficulty, complexity tier, and style improve
        val practicedProfiles = initialProfiles.toMutableList()
        var addProfile = SkillProfile.initial(MathCategory.ADDITION)
        val addHistory = mutableListOf<QuestionAttempt>()
        repeat(12) { idx ->
            val att = attempt(
                category = MathCategory.ADDITION,
                difficulty = addProfile.currentDifficulty,
                isCorrect = true,
                timeMs = 3500L,
                ts = (idx + 1) * 4000L
            )
            val res = controller.updateProfile(addProfile, att, addHistory, expectedTimeSeconds = 10, preferences = prefs)
            addHistory.add(att)
            addProfile = res.updatedProfile
        }
        practicedProfiles[0] = addProfile

        val progressedPlan = controller.resolveQuestionPlan(
            category = MathCategory.ADDITION,
            categoryProfile = addProfile,
            allProfiles = practicedProfiles,
            recentAttemptsForCategory = addHistory,
            preferences = prefs,
            random = Random(5)
        )
        assertTrue(
            "Difficulty should advance beyond BEGINNER after 12 fast solves, was ${progressedPlan.difficulty}",
            progressedPlan.difficulty.level >= Difficulty.INTERMEDIATE.level
        )
        assertTrue(
            "Complexity tier should evolve to >= 3, was ${progressedPlan.complexityTier}",
            progressedPlan.complexityTier >= 3
        )

        // 3. Struggling (wrong streak) scaffolds complexity back down to Tier 1 DIRECT
        val missAttempt = attempt(
            category = MathCategory.ADDITION,
            difficulty = addProfile.currentDifficulty,
            isCorrect = false,
            timeMs = 15000L,
            ts = 99_000L
        )
        val missedResult = controller.updateProfile(addProfile, missAttempt, addHistory, 10, prefs)
        val scaffoldedPlan = controller.resolveQuestionPlan(
            category = MathCategory.ADDITION,
            categoryProfile = missedResult.updatedProfile,
            allProfiles = practicedProfiles,
            recentAttemptsForCategory = addHistory + missAttempt,
            preferences = prefs,
            random = Random(5)
        )
        assertEquals(1, scaffoldedPlan.complexityTier)
        assertEquals(com.example.domain.model.QuestionStyle.DIRECT, scaffoldedPlan.questionStyle)

        // 4. Disabling progressive styles locks style to DIRECT regardless of high mastery
        val directOnlyPrefs = prefs.copy(progressiveStyleEnabled = false)
        val directPlan = controller.resolveQuestionPlan(
            category = MathCategory.ADDITION,
            categoryProfile = addProfile,
            allProfiles = practicedProfiles,
            recentAttemptsForCategory = addHistory,
            preferences = directOnlyPrefs,
            random = Random(5)
        )
        assertEquals(1, directPlan.complexityTier)
        assertEquals(com.example.domain.model.QuestionStyle.DIRECT, directPlan.questionStyle)

        // 5. RAPID pace advances faster than GENTLE pace under identical 3-attempt streak
        val rapidPrefs = prefs.copy(adaptivePace = com.example.domain.model.AdaptivePace.RAPID)
        val gentlePrefs = prefs.copy(adaptivePace = com.example.domain.model.AdaptivePace.GENTLE)
        var rapidProf = SkillProfile.initial(MathCategory.SUBTRACTION)
        var gentleProf = SkillProfile.initial(MathCategory.SUBTRACTION)
        val rapidHist = mutableListOf<QuestionAttempt>()
        val gentleHist = mutableListOf<QuestionAttempt>()
        repeat(3) { idx ->
            val att = attempt(MathCategory.SUBTRACTION, Difficulty.BEGINNER, true, 3000L, (idx + 1) * 2000L)
            rapidProf = controller.updateProfile(rapidProf, att, rapidHist, 8, rapidPrefs).updatedProfile
            rapidHist.add(att)
            gentleProf = controller.updateProfile(gentleProf, att, gentleHist, 8, gentlePrefs).updatedProfile
            gentleHist.add(att)
        }
        assertTrue(
            "RAPID pace should advance difficulty before GENTLE pace",
            rapidProf.currentDifficulty.level > gentleProf.currentDifficulty.level
        )
    }

    @Test
    fun fractionAndEvaluator_rejectsMalformedOversizedAndScientificInputsSafely() {
        // Division by zero, empty, malformed, and DoS exponent/length inputs must return null cleanly
        assertEquals(null, Fraction.parse(""))
        assertEquals(null, Fraction.parse("   "))
        assertEquals(null, Fraction.parse("/"))
        assertEquals(null, Fraction.parse("0/0"))
        assertEquals(null, Fraction.parse("5/0"))
        assertEquals(null, Fraction.parse("--1"))
        assertEquals(null, Fraction.parse("1e9999999"))
        assertEquals(null, Fraction.parse("1E-9999999"))
        assertEquals(null, Fraction.parse("1".repeat(40)))
        assertEquals(null, Fraction.parse("0." + "0".repeat(25) + "1"))

        // Valid rare fraction & decimal edge cases
        assertEquals("1/2", Fraction.parse("-3/-6")?.toCanonicalString())
        assertEquals("0", Fraction.parse("0.00")?.toCanonicalString())
        assertEquals("1/2", Fraction.parse(".5")?.toCanonicalString())
        assertEquals("-1/4", Fraction.parse("-.25")?.toCanonicalString())

        val intQ = registry.generatorFor(MathCategory.ADDITION).generate(Difficulty.BEGINNER, random = Random(1))
        val zeroIntQ = intQ.copy(answer = "0", answerFormat = com.example.domain.model.AnswerFormat.INTEGER)
        assertTrue(evaluator.evaluate(zeroIntQ, "-0").isCorrect)
        assertTrue(evaluator.evaluate(zeroIntQ, "0.00").isCorrect)
        assertFalse(evaluator.evaluate(zeroIntQ, "1e9999999").isCorrect)
        assertFalse(evaluator.evaluate(zeroIntQ, "9".repeat(40)).isCorrect)

        val zeroDecQ = intQ.copy(answer = "0", answerFormat = com.example.domain.model.AnswerFormat.DECIMAL)
        assertTrue(evaluator.evaluate(zeroDecQ, "0.000").isCorrect)
        assertEquals("0", evaluator.evaluate(zeroDecQ, "0.000").canonicalExpected)
        assertFalse(evaluator.evaluate(zeroDecQ, "1E10000000").isCorrect)

        val halfFracQ = intQ.copy(answer = "1/2", answerFormat = com.example.domain.model.AnswerFormat.FRACTION)
        assertTrue(evaluator.evaluate(halfFracQ, " 2 / 4 ").isCorrect)
        assertTrue(evaluator.evaluate(halfFracQ, "0.5").isCorrect)
        assertFalse(evaluator.evaluate(halfFracQ, "1/0").isCorrect)
    }

    @Test
    fun quizLockTracker_handlesClockSkewBlankAndDisabledEdgeCases() {
        val tracker = QuizLockSessionTracker(relaunchDebounceMs = 1500L)
        val locked = setOf("com.example.video")
        val own = "com.aistudio.govinda.mathkx"

        // Blank package or own package never locks
        assertEquals(LockDecision.NoAction, tracker.onPackageForegrounded("", locked, true, own, nowMs = 1000L))
        assertEquals(LockDecision.NoAction, tracker.onPackageForegrounded("  ", locked, true, own, nowMs = 1000L))
        assertEquals(LockDecision.NoAction, tracker.onPackageForegrounded(own, locked, true, own, nowMs = 1000L))

        // First trigger at t=5000, clock jumps backwards to t=4000 -> negative delta does NOT suppress lock
        assertEquals(
            LockDecision.LaunchQuizGate("com.example.video"),
            tracker.onPackageForegrounded("com.example.video", locked, true, own, nowMs = 5000L)
        )
        assertEquals(
            LockDecision.LaunchQuizGate("com.example.video"),
            tracker.onPackageForegrounded("com.example.video", locked, true, own, nowMs = 4000L)
        )

        // Disabling quizLock clears unlocked session so re-enabling requires a fresh question
        tracker.markUnlocked("com.example.video")
        assertEquals(LockDecision.NoAction, tracker.onPackageForegrounded("com.example.video", locked, false, own, nowMs = 6000L))
        assertEquals(
            LockDecision.LaunchQuizGate("com.example.video"),
            tracker.onPackageForegrounded("com.example.video", locked, true, own, nowMs = 6100L)
        )
    }

    @Test
    fun learnerSurveyEngine_mapsAllHierarchicalTracksToExclusiveCategories() {
        val base = UserPreferences()
        val selector = TopicSelector()
        val profiles = MathCategory.entries.map { SkillProfile.initial(it) }

        // Default / Skip preserves balanced adaptive set
        val defPrefs = com.example.feature.survey.LearnerSurveyEngine.applyDefault(base)
        assertTrue(defPrefs.hasCompletedSurvey)
        assertEquals("DEFAULT", defPrefs.learnerTrackId)
        assertEquals(MathCategory.defaultBalancedSet, defPrefs.enabledCategories)

        // Every custom root track -> sub-track -> pace enforces exclusive categories and valid difficulty bounds
        com.example.feature.survey.SurveyRootTrack.entries
            .filter { it != com.example.feature.survey.SurveyRootTrack.DEFAULT }
            .forEach { root ->
                val subTracks = com.example.feature.survey.LearnerSurveyEngine.subTracksFor(root)
                assertTrue("Root $root must have sub-tracks", subTracks.isNotEmpty())
                subTracks.forEach { sub ->
                    com.example.feature.survey.SurveyPaceLevel.entries.forEach { pace ->
                        val mapped = com.example.feature.survey.LearnerSurveyEngine.applySelection(
                            current = base,
                            root = root,
                            subTrack = sub,
                            pace = pace
                        )
                        assertTrue(mapped.hasCompletedSurvey)
                        assertEquals(sub.id, mapped.learnerTrackId)
                        assertEquals(sub.categories, mapped.enabledCategories)
                        assertTrue(
                            "minDifficulty must be <= maxDifficulty for ${sub.id}/$pace",
                            mapped.minDifficulty.level <= mapped.maxDifficulty.level
                        )

                        // Verify TopicSelector strictly suggests ONLY categories within this track
                        repeat(15) { idx ->
                            val picked = selector.selectNextCategory(
                                profiles = profiles,
                                reviewQueue = emptyList(),
                                preferences = mapped,
                                lastCategory = null,
                                nowEpochMs = 1_000_000L + idx * 1000L,
                                random = Random(idx + sub.id.hashCode())
                            )
                            assertTrue(
                                "Picked $picked must belong to ${sub.id} categories ${sub.categories}",
                                picked in sub.categories
                            )
                        }
                    }
                }
            }
    }

    @Test
    fun notesStudio_latexTableBlockTypesAndExportRoundTripsWork() {
        // 1. All 26 block types and 16 programming languages exist
        assertEquals(26, com.example.core.document.BlockType.entries.size)
        assertEquals(16, com.example.core.document.CodeLanguage.entries.size)

        // 2. LaTeX parser handles fractions, matrices, determinants, Greek symbols, and syntax errors
        val fracAst = com.example.core.latex.LatexRendererEngine.parse(
            "H(s) = \\frac{Y(s)}{X(s)}"
        )
        assertTrue(fracAst.isValid)
        assertEquals("Y(s)", fracAst.topFraction)
        assertEquals("X(s)", fracAst.bottomFraction)

        val matrixAst = com.example.core.latex.LatexRendererEngine.parse(
            "A = \\begin{bmatrix} 1 & 2 \\\\ 3 & 4 \\end{bmatrix}"
        )
        assertTrue(matrixAst.isValid)
        assertEquals(2, matrixAst.matrixRows?.size)
        assertEquals(listOf("1", "2"), matrixAst.matrixRows?.first())

        val invalidAst = com.example.core.latex.LatexRendererEngine.parse("\\frac{1}{2")
        assertFalse(invalidAst.isValid)
        assertTrue(invalidAst.errorMessage?.contains("Unbalanced") == true)

        // 3. TableData CSV and Markdown conversion
        val table = com.example.core.document.TableData.defaultComponentTable()
        val csv = table.toCsv()
        val fromCsv = com.example.core.document.TableData.fromCsv(csv)
        assertEquals(table.columns, fromCsv.columns)
        assertEquals(3, fromCsv.rows.size)
        assertTrue(table.toMarkdownTable().contains("| Component | Function | Important parameter |"))

        // 4. Slash Command Catalog, Document Telemetry & Active Recall Flashcard extraction
        assertTrue(com.example.feature.notes.features.SlashCommandCatalog.commands.size >= 12)
        val sampleBlocks = listOf(
            com.example.core.database.NoteBlockEntity(
                id = "b1",
                noteId = "n1",
                type = com.example.core.document.BlockType.HEADING_1.name,
                position = 0,
                content = "Laplace Stability Criteria",
                createdAt = 1L,
                updatedAt = 1L
            ),
            com.example.core.database.NoteBlockEntity(
                id = "b2",
                noteId = "n1",
                type = com.example.core.document.BlockType.PARAGRAPH.name,
                position = 1,
                content = "All poles of H(s) must have negative real parts.",
                createdAt = 1L,
                updatedAt = 1L
            ),
            com.example.core.database.NoteBlockEntity(
                id = "b3",
                noteId = "n1",
                type = com.example.core.document.BlockType.CHECKLIST.name,
                position = 2,
                content = "[x] Check Routh array\n[ ] Plot Bode phase margin",
                createdAt = 1L,
                updatedAt = 1L
            )
        )
        val metrics = com.example.feature.notes.features.NoteAnalyticsCalculator.compute(sampleBlocks)
        assertTrue(metrics.wordCount >= 10)
        assertEquals(1, metrics.checklistCompleted)
        assertEquals(2, metrics.checklistTotal)

        val flashcards = com.example.feature.notes.features.NoteFlashcardGenerator.generateFromBlocks(
            "Control Systems",
            sampleBlocks
        )
        assertTrue(flashcards.isNotEmpty())
        assertTrue(flashcards.first().prompt.contains("Laplace Stability Criteria"))

        // 5. OfflineTutorChatEngine evaluates expressions, engineering formulas, and note summaries
        val pctReply = com.example.feature.chat.OfflineTutorChatEngine.generateReply(
            rawInput = "25% of 360",
            currentQuestion = null,
            allNotes = emptyList(),
            activeNote = null,
            activeBlocks = sampleBlocks
        )
        assertTrue(pctReply.text.contains("90"))

        val rcReply = com.example.feature.chat.OfflineTutorChatEngine.generateReply(
            rawInput = "RC Low-Pass Filter Formula",
            currentQuestion = null,
            allNotes = emptyList(),
            activeNote = null,
            activeBlocks = sampleBlocks
        )
        assertTrue(rcReply.text.contains("159.15"))

        // 6. PeerPairingEngine generates 6-digit OTP, QR matrix, and parses OTP/QR pairing payloads
        val otp = com.example.feature.chat.PeerPairingEngine.generateSixDigitOtp(42L)
        assertEquals(6, otp.length)
        assertEquals("849-201", com.example.feature.chat.PeerPairingEngine.formatOtpDisplay("849-201"))
        assertTrue(com.example.feature.chat.PeerPairingEngine.validateOtpInput("849-201"))

        val qrPayload = com.example.feature.chat.PeerPairingEngine.buildQrPayload("MG-DEV-01", "849201", "Rohan")
        val parsedQr = com.example.feature.chat.PeerPairingEngine.parseQrOrOtpPayload(qrPayload, "Fallback")
        assertEquals("849201", parsedQr?.pairingCodeOtp)
        assertTrue(parsedQr?.name?.contains("Rohan") == true)

        val parsedOtpOnly = com.example.feature.chat.PeerPairingEngine.parseQrOrOtpPayload("482-910", "Vikram")
        assertEquals("482910", parsedOtpOnly?.pairingCodeOtp)
        assertEquals("Vikram", parsedOtpOnly?.name)

        val qrMatrix = com.example.feature.chat.PeerPairingEngine.generateQrMatrix(qrPayload, size = 21)
        assertEquals(21, qrMatrix.size)
        assertTrue("Top-left finder corner must be dark", qrMatrix[0][0])
        assertTrue("Top-left finder center must be dark", qrMatrix[3][3])

        // 7. All 20 ChatMessageType entries and SharedQuestionPayload pipe round-trip work
        assertEquals(20, com.example.core.database.ChatMessageType.entries.size)
        val sampleQ = registry.generatorFor(MathCategory.ADDITION).generate(Difficulty.BEGINNER, random = Random(12))
        val qPayload = com.example.feature.chat.SharedQuestionPayload.fromMathQuestion(sampleQ)
        val decodedQ = com.example.feature.chat.SharedQuestionPayload.decodeFromPipeString(qPayload.encodeToPipeString())
        assertEquals(qPayload.questionId, decodedQ?.questionId)
        assertEquals(qPayload.prompt, decodedQ?.prompt)
        assertEquals(qPayload.answer, decodedQ?.answer)
    }

    private fun attempt(
        category: MathCategory,
        difficulty: Difficulty,
        isCorrect: Boolean,
        timeMs: Long,
        ts: Long
    ) = QuestionAttempt(
        id = UUID.randomUUID().toString(),
        questionId = UUID.randomUUID().toString(),
        category = category,
        difficulty = difficulty,
        prompt = "2 + 2",
        expectedAnswer = "4",
        submittedAnswer = if (isCorrect) "4" else "5",
        isCorrect = isCorrect,
        responseTimeMs = timeMs,
        attemptedAtEpochMs = ts
    )
}
