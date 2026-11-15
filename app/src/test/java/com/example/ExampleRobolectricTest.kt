package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetFirstLaunchState() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<GovindaApplication>()
        app.container.repository.updatePreferences {
            it.copy(
                hasAcceptedPermissions = false,
                hasCompletedSurvey = false,
                learnerTrackId = "DEFAULT",
                learnerTrackLabel = "By Default (Adaptive)",
                surveyFocusTag = "",
                enabledCategories = com.example.domain.model.MathCategory.defaultBalancedSet,
                quizLockEnabled = false,
                quizLockedPackages = emptySet(),
                developerModeEnabled = false
            )
        }
    }

    @Test
    fun appIdentityAndOfflineManifestCheck() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("GOVINDA", context.getString(R.string.app_name))

        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        val permissions = packageInfo.requestedPermissions?.toList().orEmpty()
        assertTrue(
            "GOVINDA Cloud Chat backend should declare INTERNET permission",
            Manifest.permission.INTERNET in permissions
        )
        assertFalse(
            "GOVINDA must not declare broad QUERY_ALL_PACKAGES permission",
            Manifest.permission.QUERY_ALL_PACKAGES in permissions
        )
        assertTrue(
            "GOVINDA should declare SYSTEM_ALERT_WINDOW for App Quiz Lock overlay readiness",
            Manifest.permission.SYSTEM_ALERT_WINDOW in permissions
        )
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun launchesCleanHomeAndConfiguresQuizLockInSettings() {
        // First open shows mandatory single-tap permission dialog with NO cancel/close/hide button
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("mandatory_permission_dialog"), timeoutMillis = 10_000L)
        composeTestRule.onNodeWithTag("mandatory_permission_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("permission_dialog_close").assertDoesNotExist()
        composeTestRule.onNodeWithTag("accept_all_permissions_button").assertIsDisplayed().performClick()

        // Survey appears immediately after permissions with Skip + By Default options
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("learner_survey_dialog"), timeoutMillis = 10_000L)
        composeTestRule.onNodeWithTag("survey_option_DEFAULT").assertIsDisplayed()
        composeTestRule.onNodeWithTag("survey_skip_button").assertIsDisplayed().performClick()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("learner_survey_dialog"), timeoutMillis = 10_000L)

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("question_card"), timeoutMillis = 10_000L)

        // Clean Home UI: equation card, answer display, and docked keypad without mode/XP/bookmark clutter
        composeTestRule.onNodeWithTag("question_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("question_prompt").assertIsDisplayed()
        composeTestRule.onNodeWithTag("answer_display").assertIsDisplayed()
        composeTestRule.onNodeWithTag("challenge_mode_left").assertDoesNotExist()
        composeTestRule.onNodeWithTag("input_mode_left").assertDoesNotExist()
        composeTestRule.onNodeWithTag("bookmark_button").assertDoesNotExist()
        composeTestRule.onNodeWithTag("nav_explore").assertDoesNotExist()
        composeTestRule.onNodeWithTag("nav_categories").assertDoesNotExist()
        composeTestRule.onNodeWithTag("nav_saved").assertDoesNotExist()

        // Docked keypad input and guaranteed-wrong submission -> triggers interactive 20s cooldown timer
        composeTestRule.onNodeWithTag("keypad_key_9").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("keypad_key_9").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("keypad_key_9").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("keypad_key_9").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("keypad_backspace").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("submit_button").assertIsDisplayed().performClick()

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("feedback_card"), timeoutMillis = 10_000L)
        composeTestRule.onNodeWithTag("question_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("question_prompt").assertIsDisplayed()
        composeTestRule.onNodeWithTag("feedback_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("wrong_cooldown_timer").assertIsDisplayed()
        composeTestRule.onNodeWithTag("submit_button").assertIsDisplayed()

        // Wrong answers must NEVER reveal the solution or allow skipping to a new question
        composeTestRule.onNodeWithTag("toggle_explanation_button").assertDoesNotExist()
        composeTestRule.onNodeWithTag("explanation_steps").assertDoesNotExist()
        composeTestRule.onNodeWithTag("next_question_button").assertDoesNotExist()

        // Tapping cooldown timer shows a neutral hint only, still hiding the solution
        composeTestRule.onNodeWithTag("wrong_cooldown_timer").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("explanation_steps").assertDoesNotExist()

        // Open Settings from Top Nav Bar
        composeTestRule.onNodeWithTag("top_settings_button").assertIsDisplayed().performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("settings_list"), timeoutMillis = 5_000L)

        // Confirm automated sections are NOT shown in Settings UI
        composeTestRule.onNodeWithTag("adaptive_mechanism_card").assertDoesNotExist()
        composeTestRule.onNodeWithTag("difficulty_override_auto").assertDoesNotExist()
        composeTestRule.onNodeWithTag("export_data_button").assertDoesNotExist()
        composeTestRule.onNodeWithTag("permissions_card").assertDoesNotExist()

        // App Quiz Lock permission dialog opens without a cancel/close button
        composeTestRule.onNodeWithTag("toggle_quiz_lock").assertIsDisplayed()
        composeTestRule.onNodeWithTag("open_accessibility_settings_button").assertIsDisplayed().performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("permission_dialog"), timeoutMillis = 5_000L)
        composeTestRule.onNodeWithTag("permission_dialog_close").assertDoesNotExist()
        composeTestRule.onNodeWithTag("permission_dialog_open_settings").assertIsDisplayed().performClick()

        // App selector dialog works cleanly
        composeTestRule.onNodeWithTag("select_locked_apps_button").assertIsDisplayed().performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("app_search_input"), timeoutMillis = 5_000L)
        composeTestRule.onNodeWithTag("app_picker_done_button").assertIsDisplayed().performClick()

        // Verify Help, FAQ, About, Privacy Policy, Terms & Conditions, and Open-Source Licenses rows
        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("help_support_card"))
        composeTestRule.onNodeWithTag("help_guide_row").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("faq_row").performScrollTo().assertIsDisplayed().performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("settings_info_dialog"), timeoutMillis = 5_000L)
        composeTestRule.onNodeWithTag("settings_info_dialog_close").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("about_row").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("legal_licenses_card"))
        composeTestRule.onNodeWithTag("privacy_policy_row").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("terms_conditions_row").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("open_source_licenses_row").performScrollTo().assertIsDisplayed().performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("settings_info_dialog"), timeoutMillis = 5_000L)
        composeTestRule.onNodeWithTag("settings_info_dialog_close").assertIsDisplayed().performClick()

        // Verify Developer Mode is initially off (no bottom nav), then enable it -> reveals bottom nav + Notes Studio
        composeTestRule.onNodeWithTag("developer_bottom_nav").assertDoesNotExist()
        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("developer_mode_card"))
        composeTestRule.onNodeWithTag("toggle_developer_mode").performScrollTo().assertIsDisplayed().performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("developer_bottom_nav"), timeoutMillis = 5_000L)
        composeTestRule.onNodeWithTag("nav_notes_studio").assertIsDisplayed().performClick()

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("notes_studio_screen"), timeoutMillis = 5_000L)
        composeTestRule.onNodeWithTag("notes_search_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("create_note_button").assertIsDisplayed()

        // Navigate back to Settings and disable Developer Mode -> hides bottom nav again
        composeTestRule.onNodeWithTag("nav_dev_settings").assertIsDisplayed().performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("settings_list"), timeoutMillis = 5_000L)
        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("developer_mode_card"))
        composeTestRule.onNodeWithTag("toggle_developer_mode").performScrollTo().assertIsDisplayed().performClick()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("developer_bottom_nav"), timeoutMillis = 5_000L)

        // Navigate back to Home
        composeTestRule.onNodeWithTag("settings_back_button").assertIsDisplayed().performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("question_card"), timeoutMillis = 5_000L)
        composeTestRule.onNodeWithTag("question_card").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun personalizationStudio_switchesThemesFontsIconsLayoutsAndAppliesToHome() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("mandatory_permission_dialog"), timeoutMillis = 10_000L)
        composeTestRule.onNodeWithTag("accept_all_permissions_button").assertIsDisplayed().performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("learner_survey_dialog"), timeoutMillis = 10_000L)
        composeTestRule.onNodeWithTag("survey_use_default_button").assertIsDisplayed().performClick()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("learner_survey_dialog"), timeoutMillis = 10_000L)

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("question_card"), timeoutMillis = 10_000L)

        // Verify keypad-only: no multiple-choice buttons exist
        composeTestRule.onNodeWithTag("mcq_option_0").assertDoesNotExist()
        composeTestRule.onNodeWithTag("input_mode_right").assertDoesNotExist()

        // Navigate Home -> Settings -> Personalization Studio
        composeTestRule.onNodeWithTag("top_settings_button").assertIsDisplayed().performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("open_personalization_button"), timeoutMillis = 5_000L)
        composeTestRule.onNodeWithTag("open_personalization_button").assertIsDisplayed().performClick()

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("live_theme_preview"), timeoutMillis = 5_000L)
        composeTestRule.onNodeWithTag("live_theme_preview").assertIsDisplayed()

        // Switch theme to Dracula in Themes tab
        composeTestRule.onNodeWithTag("theme_card_dracula").performScrollTo().assertIsDisplayed().performClick()

        // Switch to Typography tab and pick JetBrains Mono
        composeTestRule.onNodeWithTag("studio_tab_typography").performScrollTo().performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("font_option_jetbrains_mono"), timeoutMillis = 5_000L)
        composeTestRule.onNodeWithTag("font_option_jetbrains_mono").performScrollTo().performClick()

        // Switch to Icons tab and pick Duotone
        composeTestRule.onNodeWithTag("studio_tab_icons").performScrollTo().performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("icon_family_duotone"), timeoutMillis = 5_000L)
        composeTestRule.onNodeWithTag("icon_family_duotone").performScrollTo().performClick()

        // Switch to Layouts tab and pick Stats-Assisted
        composeTestRule.onNodeWithTag("studio_tab_layouts").performScrollTo().performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("challenge_layout_stats_assisted"), timeoutMillis = 5_000L)
        composeTestRule.onNodeWithTag("challenge_layout_stats_assisted").performScrollTo().performClick()

        // Apply changes and navigate back to Home
        composeTestRule.onNodeWithTag("studio_apply_button").assertIsDisplayed().performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("settings_back_button"), timeoutMillis = 5_000L)
        composeTestRule.onNodeWithTag("settings_back_button").assertIsDisplayed().performClick()

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("question_card"), timeoutMillis = 5_000L)
        composeTestRule.onNodeWithTag("question_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("submit_button").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun hierarchicalSurvey_branchesAndFiltersQuestionsOnlyToSelectedTrack() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("mandatory_permission_dialog"), timeoutMillis = 10_000L)
        composeTestRule.onNodeWithTag("accept_all_permissions_button").assertIsDisplayed().performClick()

        // Step 1 -> Pick Coding & CS Logic
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("learner_survey_dialog"), timeoutMillis = 10_000L)
        composeTestRule.onNodeWithTag("survey_option_CODING").performScrollTo().assertIsDisplayed().performClick()

        // Step 2 -> Pick Binary, Bits & Bitwise
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("survey_sub_CODING_BINARY"), timeoutMillis = 5_000L)
        composeTestRule.onNodeWithTag("survey_sub_CODING_BINARY").performScrollTo().assertIsDisplayed().performClick()

        // Step 3 -> Pick Balanced Growth
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("survey_pace_BALANCED"), timeoutMillis = 5_000L)
        composeTestRule.onNodeWithTag("survey_pace_BALANCED").performScrollTo().assertIsDisplayed().performClick()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("learner_survey_dialog"), timeoutMillis = 10_000L)

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("question_card"), timeoutMillis = 10_000L)

        // Verify persisted preferences strictly limit questions to Coding & Logic track
        val app = ApplicationProvider.getApplicationContext<GovindaApplication>()
        val prefs = runBlocking {
            app.container.repository.observePreferences().first()
        }
        assertTrue(prefs.hasCompletedSurvey)
        assertEquals("CODING_BINARY", prefs.learnerTrackId)
        assertEquals("CODING_BINARY", prefs.surveyFocusTag)
        assertEquals(setOf(com.example.domain.model.MathCategory.CODING_LOGIC), prefs.enabledCategories)

        // Verify user can reopen survey from Settings -> Change Learner Profile
        composeTestRule.onNodeWithTag("top_settings_button").assertIsDisplayed().performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("open_learner_survey_button"), timeoutMillis = 5_000L)
        composeTestRule.onNodeWithTag("open_learner_survey_button").assertIsDisplayed().performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("learner_survey_dialog"), timeoutMillis = 5_000L)
        composeTestRule.onNodeWithTag("survey_skip_button").performClick()
    }

    @Test
    fun appearanceDataStore_jsonRoundTripPreservesAllPersonalizationSettings() {
        val customTheme = com.example.personalization.theme.ThemeRegistry.darkThemes
            .first { it.id == "dracula" }
            .copy(id = "custom_dracula", name = "Custom Dracula", isCustom = true)
        val stateToSave = com.example.personalization.model.PersonalizationState(
            activeProfile = com.example.personalization.model.AppearanceProfile(
                id = "custom_01",
                name = "Night Study",
                isBuiltIn = false,
                themeId = customTheme.id,
                darkThemeId = "amoled_black",
                themeSwitchMode = com.example.personalization.model.ThemeSwitchMode.SCHEDULED,
                scheduledDarkStartHour = 21,
                scheduledDarkEndHour = 7,
                background = com.example.personalization.model.BackgroundPattern.DOTTED_PATTERN,
                components = com.example.personalization.model.ComponentStyle(
                    cardStyle = com.example.personalization.model.CardStylePreset.SOFT_ELEVATION,
                    keypadStyle = com.example.personalization.model.KeypadStylePreset.COMPACT_CALCULATOR
                )
            ),
            savedProfiles = com.example.personalization.theme.ThemeRegistry.builtInProfiles,
            customThemes = listOf(customTheme)
        )
        val encoded = com.example.personalization.storage.AppearanceDataStore.encodeState(stateToSave)
        val decoded = com.example.personalization.storage.AppearanceDataStore.decodeState(encoded)
        assertEquals(stateToSave.activeProfile, decoded.activeProfile)
        assertEquals(stateToSave.customThemes, decoded.customThemes)
        assertEquals(6, decoded.savedProfiles.size)
    }

    @Test
    fun challengeViewModel_wrongAnswerRepeatsSameQuestionAfterCooldownUntilCorrect() {
        val fakeRepo = FakeMathLearningRepository()
        val adaptive = com.example.domain.adaptive.AdaptiveController()
        val generateUseCase = com.example.domain.usecase.GenerateNextQuestionUseCase(
            repository = fakeRepo,
            registry = com.example.domain.generator.GeneratorRegistry.default(),
            topicSelector = com.example.domain.selector.TopicSelector(),
            adaptiveController = adaptive,
            random = kotlin.random.Random(101)
        )
        val submitUseCase = com.example.domain.usecase.SubmitAnswerUseCase(
            repository = fakeRepo,
            evaluator = com.example.domain.evaluator.AnswerEvaluator(),
            adaptiveController = adaptive
        )
        val vm = com.example.feature.challenge.ChallengeViewModel(
            repository = fakeRepo,
            generateNextQuestion = generateUseCase,
            submitAnswer = submitUseCase,
            cooldownDurationSeconds = 2,
            cooldownTickMs = 200L
        )

        fun pumpMain(waitMs: Long = 20L) {
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper())
                .idleFor(java.time.Duration.ofMillis(waitMs))
        }

        fun waitUntil(maxSteps: Int = 50, stepMs: Long = 20L, condition: () -> Boolean) {
            for (i in 0 until maxSteps) {
                if (condition()) return
                pumpMain(stepMs)
            }
            assertTrue("Timed out waiting for condition", condition())
        }

        // Wait for initial question load
        waitUntil { !vm.uiState.value.isLoading && vm.uiState.value.currentQuestion != null }
        val initialQuestion = requireNotNull(vm.uiState.value.currentQuestion)

        // 1. Submit guaranteed-wrong answer ("999999")
        repeat(6) { vm.onKeypadSymbol("9") }
        vm.onSubmitAnswer()
        waitUntil { vm.uiState.value.submissionOutcome != null }

        assertEquals(initialQuestion.id, vm.uiState.value.currentQuestion?.id)
        assertEquals(false, vm.uiState.value.submissionOutcome?.evaluation?.isCorrect)
        assertEquals(1, vm.uiState.value.wrongAttemptCount)
        assertTrue(vm.uiState.value.wrongCooldownSecondsRemaining > 0)

        // Cannot toggle solution steps or skip to next question on wrong answer
        vm.onToggleExplanation()
        assertFalse(vm.uiState.value.isExplanationExpanded)
        vm.onNextQuestion()
        pumpMain(20L)
        assertEquals(initialQuestion.id, vm.uiState.value.currentQuestion?.id)

        // Wait for cooldown to finish -> auto-unlocks SAME question for retry #2
        waitUntil(maxSteps = 40, stepMs = 50L) {
            !vm.uiState.value.isCooldownActive && vm.uiState.value.submissionOutcome == null
        }
        assertEquals(initialQuestion.id, vm.uiState.value.currentQuestion?.id)
        assertEquals(null, vm.uiState.value.submissionOutcome)
        assertEquals("", vm.uiState.value.userInput)
        assertEquals(1, vm.uiState.value.wrongAttemptCount)

        // 2. Submit another wrong answer -> still same question, attempt #2
        repeat(3) { vm.onKeypadSymbol("8") }
        vm.onSubmitAnswer()
        waitUntil { vm.uiState.value.submissionOutcome != null }
        assertEquals(initialQuestion.id, vm.uiState.value.currentQuestion?.id)
        assertEquals(2, vm.uiState.value.wrongAttemptCount)

        // Wait for second cooldown to finish
        waitUntil(maxSteps = 40, stepMs = 50L) {
            !vm.uiState.value.isCooldownActive && vm.uiState.value.submissionOutcome == null
        }
        assertEquals(initialQuestion.id, vm.uiState.value.currentQuestion?.id)

        // 3. Submit exact correct answer -> unlocks solution toggle, resets wrongAttemptCount, and allows Next Question
        initialQuestion.answer.forEach { ch -> vm.onKeypadSymbol(ch.toString()) }
        vm.onSubmitAnswer()
        waitUntil { vm.uiState.value.submissionOutcome?.evaluation?.isCorrect == true }
        assertEquals(true, vm.uiState.value.submissionOutcome?.evaluation?.isCorrect)
        assertEquals(0, vm.uiState.value.wrongAttemptCount)

        vm.onToggleExplanation()
        assertTrue(vm.uiState.value.isExplanationExpanded)

        vm.onNextQuestion()
        waitUntil { vm.uiState.value.currentQuestion?.id != initialQuestion.id }
        assertTrue(vm.uiState.value.currentQuestion?.id != initialQuestion.id)
    }
}

private class FakeMathLearningRepository : com.example.domain.repository.MathLearningRepository {
    private val prefsFlow = kotlinx.coroutines.flow.MutableStateFlow(com.example.domain.model.UserPreferences())
    private val profilesMap = com.example.domain.model.MathCategory.entries
        .associateWith { com.example.domain.model.SkillProfile.initial(it) }
        .toMutableMap()
    private val attempts = mutableListOf<com.example.domain.model.QuestionAttempt>()
    private val fingerprints = linkedSetOf<String>()

    override fun observeSkillProfiles() = kotlinx.coroutines.flow.flowOf(profilesMap.values.toList())
    override fun observeRecentAttempts(limit: Int) = kotlinx.coroutines.flow.flowOf(attempts.takeLast(limit))
    override fun observeReviewQueue() = kotlinx.coroutines.flow.flowOf(emptyList<com.example.domain.model.ReviewItem>())
    override fun observeBookmarks() = kotlinx.coroutines.flow.flowOf(emptyList<com.example.domain.model.BookmarkedQuestion>())
    override fun observeDailyProgress(limit: Int) = kotlinx.coroutines.flow.flowOf(emptyList<com.example.domain.model.DailyProgress>())
    override fun observePreferences() = prefsFlow

    override suspend fun getSkillProfiles() = profilesMap.values.toList()
    override suspend fun getSkillProfile(category: com.example.domain.model.MathCategory) =
        profilesMap[category] ?: com.example.domain.model.SkillProfile.initial(category)
    override suspend fun getRecentAttemptsForCategory(category: com.example.domain.model.MathCategory, limit: Int) =
        attempts.filter { it.category == category }.takeLast(limit)
    override suspend fun getReviewQueue() = emptyList<com.example.domain.model.ReviewItem>()
    override suspend fun getRecentFingerprints(limit: Int) = fingerprints.toSet()
    override suspend fun saveFingerprint(fingerprint: String, category: com.example.domain.model.MathCategory, generatorVersion: Int) {
        fingerprints += fingerprint
    }

    override suspend fun recordAttemptTransaction(
        attempt: com.example.domain.model.QuestionAttempt,
        updatedProfile: com.example.domain.model.SkillProfile,
        dateKey: String,
        xpEarned: Int,
        reviewItemToUpsert: com.example.domain.model.ReviewItem?,
        resolvedReviewCategory: com.example.domain.model.MathCategory?
    ) {
        attempts += attempt
        profilesMap[updatedProfile.category] = updatedProfile
    }

    override suspend fun toggleBookmark(question: com.example.domain.model.BookmarkedQuestion) = false
    override suspend fun removeBookmark(questionId: String) = Unit
    override suspend fun isBookmarked(questionId: String) = false
    override suspend fun dismissReviewItem(id: String) = Unit
    override suspend fun updatePreferences(transform: (com.example.domain.model.UserPreferences) -> com.example.domain.model.UserPreferences) {
        prefsFlow.value = transform(prefsFlow.value)
    }
    override suspend fun exportAllDataAsJson() = "{}"
    override suspend fun resetAllLearningData() = Unit
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ResilienceAndCacheRobolectricTest {

    @Test
    fun appearanceDataStore_partialCorruptionSkipsBadItemsWithoutLosingValidData() {
        val validTheme = com.example.personalization.theme.ThemeRegistry.darkThemes
            .first { it.id == "dracula" }
            .copy(id = "custom_valid", name = "Valid Custom", isCustom = true)
        val baseline = com.example.personalization.storage.AppearanceDataStore.encodeState(
            com.example.personalization.storage.AppearanceDataStore.defaultState()
                .copy(customThemes = listOf(validTheme))
        )
        // Inject one corrupt theme object alongside the valid custom theme
        val root = org.json.JSONObject(baseline)
        root.getJSONArray("customThemes").put(org.json.JSONObject().put("id", "broken_missing_colors"))

        val decoded = com.example.personalization.storage.AppearanceDataStore.decodeState(root.toString())
        assertEquals(1, decoded.customThemes.size)
        assertEquals("custom_valid", decoded.customThemes.first().id)

        // Empty JSON recovers cleanly to default profiles
        val fromEmpty = com.example.personalization.storage.AppearanceDataStore.decodeState("{}")
        assertTrue(fromEmpty.savedProfiles.isNotEmpty())
    }

    @Test
    fun challengeViewModel_repositoryFailureFallsBackGracefullyWithoutStalling() {
        val failingRepo = FailingMathLearningRepository()
        val adaptive = com.example.domain.adaptive.AdaptiveController()
        val vm = com.example.feature.challenge.ChallengeViewModel(
            repository = failingRepo,
            generateNextQuestion = com.example.domain.usecase.GenerateNextQuestionUseCase(
                repository = failingRepo,
                registry = com.example.domain.generator.GeneratorRegistry.default(),
                topicSelector = com.example.domain.selector.TopicSelector(),
                adaptiveController = adaptive
            ),
            submitAnswer = com.example.domain.usecase.SubmitAnswerUseCase(
                repository = failingRepo,
                evaluator = com.example.domain.evaluator.AnswerEvaluator(),
                adaptiveController = adaptive
            ),
            cooldownDurationSeconds = 2,
            cooldownTickMs = 200L
        )

        val looper = org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper())
        repeat(20) {
            looper.idleFor(java.time.Duration.ofMillis(20L))
        }

        // Fallback question loads cleanly despite failing DB
        val fallbackQ = requireNotNull(vm.uiState.value.currentQuestion)
        assertFalse(vm.uiState.value.isLoading)

        // Submit correct answer -> local fallback evaluation succeeds despite DB failure
        fallbackQ.answer.forEach { ch -> vm.onKeypadSymbol(ch.toString()) }
        vm.onSubmitAnswer()
        repeat(15) {
            looper.idleFor(java.time.Duration.ofMillis(20L))
        }
        assertEquals(true, vm.uiState.value.submissionOutcome?.evaluation?.isCorrect)
    }

    @Test
    fun installedAppsProvider_cachesResultsAndLabelsSafely() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shadowPm = org.robolectric.Shadows.shadowOf(context.packageManager)
        val launcherIntent = android.content.Intent(android.content.Intent.ACTION_MAIN)
            .addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        val resolveInfo = android.content.pm.ResolveInfo().apply {
            activityInfo = android.content.pm.ActivityInfo().apply {
                packageName = "com.example.dummyapp"
                name = "com.example.dummyapp.MainActivity"
            }
            nonLocalizedLabel = "Dummy App"
        }
        shadowPm.addResolveInfoForIntent(launcherIntent, resolveInfo)

        com.example.lock.InstalledAppsProvider.invalidateCache()

        val first = com.example.lock.InstalledAppsProvider.loadLaunchableApps(context)
        val second = com.example.lock.InstalledAppsProvider.loadLaunchableApps(context)
        assertEquals(1, first.size)
        // Cached hit returns exact same list instance
        assertTrue(first === second)

        val label = com.example.lock.InstalledAppsProvider.resolveAppName(context, context.packageName)
        assertEquals("GOVINDA", label)

        com.example.lock.InstalledAppsProvider.invalidateCache()
        val refreshed = com.example.lock.InstalledAppsProvider.loadLaunchableApps(context, forceRefresh = true)
        assertEquals(1, refreshed.size)
        assertFalse(first === refreshed)
    }

    @Test
    fun roomQuestionRepository_seedsAndQueriesValidatedDatasetQuestions() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<GovindaApplication>()
        val repo = app.container.questionRepository

        val bioQuestions = repo.getQuestions(
            subjectId = "BIOLOGY",
            topicId = null,
            difficulty = 2,
            limit = 10
        )
        assertTrue(bioQuestions.isNotEmpty())
        assertEquals("allenai/qasc", bioQuestions.first().sourceDataset)
        assertEquals("CC-BY-4.0", bioQuestions.first().license)
        assertEquals(4, bioQuestions.first().options.size)

        val randomMath = repo.getRandomQuestion(
            subjectId = "MATHEMATICS",
            topicId = null,
            difficulty = null
        )
        assertTrue(randomMath != null)

        val byId = repo.getQuestionById("GSM8K_GSM8K_TRAIN_0001")
        assertEquals("300", byId?.correctAnswerValue)
        assertEquals("MIT", byId?.license)
    }

    @Test
    fun notesStudioRepository_andDeveloperModePersistenceWork() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<GovindaApplication>()
        val notesRepo = app.container.notesStudioRepository
        notesRepo.ensureSeeded()

        // Verify starter Digital Electronics note, blocks, and bidirectional backlinks are seeded
        val starter = notesRepo.getNoteWithBlocks("note_001")
        assertTrue(starter != null)
        assertEquals("Digital Electronics & Logic Design", starter!!.first.title)
        assertTrue(starter.second.size >= 7)

        // Create a new note from the Circuit Design template
        val createdFromTpl = notesRepo.createNoteFromTemplate("tpl_circuit_design", folderId = "ece")
        val loadedTpl = notesRepo.getNoteWithBlocks(createdFromTpl.id)
        assertTrue(loadedTpl != null)
        assertTrue(loadedTpl!!.second.any { it.type == com.example.core.document.BlockType.ENGINEERING_SCHEMATIC.name })
        assertTrue(loadedTpl.second.any { it.type == com.example.core.document.BlockType.LATEX_BLOCK.name })

        // Verify TableData JSON round-trip and Markdown/JSON/HTML/ZIP export/import under Robolectric
        val table = com.example.core.document.TableData.defaultComponentTable()
        val restoredTable = com.example.core.document.TableData.fromJsonOrDefault(table.toJson())
        assertEquals(table.columns, restoredTable.columns)

        val d = '$'
        val markdownInput = "# Op-Amp Transfer Notes\n## Inverting Configuration\n$d${d}A_v = -\\frac{R_f}{R_{in}}$d$d"
        val (mdNote, mdBlocks) = com.example.core.export.NotesExportImportManager.importFromMarkdown(markdownInput)
        val exportedJson = com.example.core.export.NotesExportImportManager.exportToJson(mdNote, mdBlocks)
        val (jsonNote, jsonBlocks) = com.example.core.export.NotesExportImportManager.importFromJson(exportedJson)
        assertEquals("Op-Amp Transfer Notes", jsonNote.title)
        assertEquals(mdBlocks.size, jsonBlocks.size)

        // Enable Developer Mode in preferences, then disable it and verify notes remain intact
        app.container.repository.updatePreferences { it.copy(developerModeEnabled = true) }
        assertTrue(app.container.repository.observePreferences().first().developerModeEnabled)

        app.container.repository.updatePreferences { it.copy(developerModeEnabled = false) }
        assertFalse(app.container.repository.observePreferences().first().developerModeEnabled)

        // Disabling Developer Mode must never delete user notes
        val allAfterDisable = notesRepo.getAllNotesWithBlocks()
        assertTrue(allAfterDisable.size >= 4)
        assertTrue(allAfterDisable.any { it.first.id == createdFromTpl.id })

        // Verify Room-backed ChatRepository seeds Personal, Study Group, and Self-Chat conversations and persists messages, polls, and search
        val chatRepo = app.container.chatRepository
        chatRepo.ensureSeeded()
        val seededConvs = chatRepo.getAllConversations()
        assertTrue(seededConvs.any { it.id == "conv_self" })
        assertTrue(seededConvs.any { it.id == "conv_group_ece" })
        assertTrue(seededConvs.any { it.id == "conv_peer_arjun" })

        val sentMsg = chatRepo.sendMessage(
            conversationId = "conv_self",
            content = "Eigenvalue trace identity for 3x3 matrix",
            messageType = com.example.core.database.ChatMessageType.LATEX_EQUATION,
            latexFormula = "\\text{Tr}(A) = \\lambda_1 + \\lambda_2 + \\lambda_3"
        )
        assertTrue(chatRepo.toggleStarred(sentMsg.id, "conv_self"))
        assertTrue(chatRepo.togglePinned(sentMsg.id, "conv_self"))

        val searchHits = chatRepo.searchMessages("Eigenvalue", conversationId = "conv_self")
        assertEquals(1, searchHits.size)
        assertEquals(sentMsg.id, searchHits.first().id)

        val poll = chatRepo.createPollMessage(
            conversationId = "conv_group_ece",
            question = "Next revision topic?",
            options = listOf("Bode Plots", "State Space")
        )
        assertEquals("Next revision topic?", poll.question)
    }
}

private class FailingMathLearningRepository : com.example.domain.repository.MathLearningRepository {
    override fun observeSkillProfiles() = kotlinx.coroutines.flow.flowOf(emptyList<com.example.domain.model.SkillProfile>())
    override fun observeRecentAttempts(limit: Int) = kotlinx.coroutines.flow.flowOf(emptyList<com.example.domain.model.QuestionAttempt>())
    override fun observeReviewQueue() = kotlinx.coroutines.flow.flowOf(emptyList<com.example.domain.model.ReviewItem>())
    override fun observeBookmarks() = kotlinx.coroutines.flow.flowOf(emptyList<com.example.domain.model.BookmarkedQuestion>())
    override fun observeDailyProgress(limit: Int) = kotlinx.coroutines.flow.flowOf(emptyList<com.example.domain.model.DailyProgress>())
    override fun observePreferences() = kotlinx.coroutines.flow.flowOf(com.example.domain.model.UserPreferences())

    override suspend fun getSkillProfiles(): List<com.example.domain.model.SkillProfile> =
        throw IllegalStateException("Simulated SQLite read failure")
    override suspend fun getSkillProfile(category: com.example.domain.model.MathCategory): com.example.domain.model.SkillProfile =
        throw IllegalStateException("Simulated SQLite read failure")
    override suspend fun getRecentAttemptsForCategory(category: com.example.domain.model.MathCategory, limit: Int): List<com.example.domain.model.QuestionAttempt> =
        emptyList()
    override suspend fun getReviewQueue() = emptyList<com.example.domain.model.ReviewItem>()
    override suspend fun getRecentFingerprints(limit: Int) = emptySet<String>()
    override suspend fun saveFingerprint(fingerprint: String, category: com.example.domain.model.MathCategory, generatorVersion: Int) =
        throw IllegalStateException("Simulated SQLite write failure")

    override suspend fun recordAttemptTransaction(
        attempt: com.example.domain.model.QuestionAttempt,
        updatedProfile: com.example.domain.model.SkillProfile,
        dateKey: String,
        xpEarned: Int,
        reviewItemToUpsert: com.example.domain.model.ReviewItem?,
        resolvedReviewCategory: com.example.domain.model.MathCategory?
    ) = throw IllegalStateException("Simulated SQLite write failure")

    override suspend fun toggleBookmark(question: com.example.domain.model.BookmarkedQuestion) = false
    override suspend fun removeBookmark(questionId: String) = Unit
    override suspend fun isBookmarked(questionId: String) = false
    override suspend fun dismissReviewItem(id: String) = Unit
    override suspend fun updatePreferences(transform: (com.example.domain.model.UserPreferences) -> com.example.domain.model.UserPreferences) = Unit
    override suspend fun exportAllDataAsJson() = "{}"
    override suspend fun resetAllLearningData() = Unit
}
