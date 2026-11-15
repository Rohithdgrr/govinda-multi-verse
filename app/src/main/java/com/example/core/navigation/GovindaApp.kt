package com.example.core.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.document.BlockType
import com.example.di.AppContainer
import com.example.feature.challenge.ChallengeScreen
import com.example.feature.challenge.ChallengeViewModel
import com.example.feature.chat.ChatScreen
import com.example.feature.chat.ChatViewModel
import androidx.compose.material.icons.filled.PhotoLibrary
import com.example.feature.imageeditor.ImageEditorScreen
import com.example.feature.imageeditor.ImageEditorViewModel
import com.example.feature.notes.NotesStudioScreen
import com.example.feature.notes.NotesStudioViewModel
import com.example.feature.settings.MandatoryPermissionsDialog
import com.example.feature.settings.SettingsScreen
import com.example.feature.settings.SettingsViewModel
import com.example.feature.survey.LearnerSurveyDialog
import com.example.feature.survey.LearnerSurveyEngine
import com.example.lock.GovindaQuizLockService
import com.example.personalization.theme.GovindaDynamicTheme
import com.example.personalization.theme.LocalGovindaTokens
import com.example.personalization.ui.PersonalizationScreen
import com.example.personalization.ui.PersonalizationViewModel
import kotlinx.coroutines.launch

const val ROUTE_HOME = "home"
const val ROUTE_SETTINGS = "settings"
const val ROUTE_PERSONALIZATION = "personalization"
const val ROUTE_NOTES_STUDIO = "notes_studio"
const val ROUTE_DEVELOPER = "developer"
const val ROUTE_IMAGE_EDITOR = "image_editor"

data class LockLaunchRequest(
    val packageName: String,
    val appName: String,
    val requestToken: Long
)

@Composable
fun GovindaApp(
    container: AppContainer,
    lockLaunchRequest: LockLaunchRequest?,
    freshOpenSignal: Int,
    onUnlockAndOpenPackage: (String) -> Unit,
    onCancelLockToHome: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val personalizationState by container.personalizationRepository.state.collectAsStateWithLifecycle()
    val loadedPreferences by container.repository.observePreferences()
        .collectAsStateWithLifecycle(initialValue = null)
    var isAccessibilityGranted by remember {
        mutableStateOf(GovindaQuizLockService.isServiceEnabled(context))
    }
    var isOverlayGranted by remember {
        mutableStateOf(GovindaQuizLockService.isOverlayEnabled(context))
    }
    var showManualSurvey by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAccessibilityGranted = GovindaQuizLockService.isServiceEnabled(context)
                isOverlayGranted = GovindaQuizLockService.isOverlayEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val challengeViewModel: ChallengeViewModel = viewModel(
        factory = ChallengeViewModel.Factory(
            repository = container.repository,
            generateNextQuestion = container.generateNextQuestionUseCase,
            submitAnswer = container.submitAnswerUseCase
        )
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            repository = container.repository,
            exportLearningData = container.exportLearningDataUseCase,
            resetLearningProfile = container.resetLearningProfileUseCase
        )
    )
    val personalizationViewModel: PersonalizationViewModel = viewModel(
        factory = PersonalizationViewModel.Factory(
            repository = container.personalizationRepository
        )
    )
    val notesStudioViewModel: NotesStudioViewModel = viewModel(
        factory = NotesStudioViewModel.Factory(
            repository = container.notesStudioRepository
        )
    )
    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.Factory(
            repository = container.chatRepository,
            cloudRepository = container.cloudChatRepository
        )
    )
    val imageEditorViewModel: ImageEditorViewModel = viewModel()

    val challengeState by challengeViewModel.uiState.collectAsStateWithLifecycle()
    val notesStudioState by notesStudioViewModel.uiState.collectAsStateWithLifecycle()
    val chatState by chatViewModel.uiState.collectAsStateWithLifecycle()
    val imageEditorState by imageEditorViewModel.uiState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(lockLaunchRequest?.requestToken) {
        if (lockLaunchRequest != null) {
            challengeViewModel.onFreshAppOpen(
                lockedPackage = lockLaunchRequest.packageName,
                lockedAppName = lockLaunchRequest.appName
            )
            navController.popBackStack(ROUTE_HOME, inclusive = false)
        }
    }

    LaunchedEffect(freshOpenSignal) {
        if (freshOpenSignal > 0) {
            challengeViewModel.onReturnedFromBackground()
        }
    }

    LaunchedEffect(challengeState.unlockedPackageToLaunch) {
        val pkg = challengeState.unlockedPackageToLaunch ?: return@LaunchedEffect
        challengeViewModel.consumeUnlockedPackageLaunch()
        onUnlockAndOpenPackage(pkg)
    }

    BackHandler(enabled = challengeState.lockedPackage != null) {
        challengeViewModel.clearLockGate()
        onCancelLockToHome()
    }

    GovindaDynamicTheme(
        profile = personalizationState.activeProfile,
        customThemes = personalizationState.customThemes
    ) {
        val tokens = LocalGovindaTokens.current
        val view = LocalView.current
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as? Activity)?.window
                if (window != null) {
                    val controller = WindowCompat.getInsetsController(window, view)
                    controller.isAppearanceLightStatusBars = !tokens.theme.isDark
                    controller.isAppearanceLightNavigationBars = !tokens.theme.isDark
                }
            }
        }

        val motionScale = tokens.effects.motionLevel.durationScale
        val enterDuration = (200 * motionScale).toInt()
        val exitDuration = (150 * motionScale).toInt()

        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(tokens.background)
        ) {
            val isDeveloperMode = loadedPreferences?.developerModeEnabled == true
            val useNavigationRail = maxWidth >= 600.dp || (maxWidth >= 480.dp && maxHeight < 480.dp)
            val showDevNav = isDeveloperMode && challengeState.lockedPackage == null

            LaunchedEffect(isDeveloperMode, currentRoute) {
                if (loadedPreferences != null && !isDeveloperMode &&
                    currentRoute in setOf(ROUTE_NOTES_STUDIO, ROUTE_DEVELOPER, ROUTE_IMAGE_EDITOR)
                ) {
                    navController.popBackStack(ROUTE_HOME, inclusive = false)
                }
            }

            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(tokens.background),
                containerColor = tokens.background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    AnimatedVisibility(visible = showDevNav && !useNavigationRail) {
                        NavigationBar(
                            containerColor = Color.White,
                            modifier = Modifier.testTag("developer_bottom_nav")
                        ) {
                            NavigationBarItem(
                                selected = currentRoute == ROUTE_HOME,
                                onClick = {
                                    navController.navigate(ROUTE_HOME) {
                                        popUpTo(ROUTE_HOME) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                },
                                icon = { Icon(Icons.Filled.Home, contentDescription = "Quiz") },
                                label = { Text("Quiz") },
                                modifier = Modifier.testTag("nav_dev_home")
                            )
                            NavigationBarItem(
                                selected = currentRoute == ROUTE_NOTES_STUDIO,
                                onClick = {
                                    navController.navigate(ROUTE_NOTES_STUDIO) {
                                        launchSingleTop = true
                                    }
                                },
                                icon = { Icon(Icons.Filled.Description, contentDescription = "Notes (Notion)") },
                                label = { Text("Notes") },
                                modifier = Modifier.testTag("nav_notes_studio")
                            )
                            NavigationBarItem(
                                selected = currentRoute == ROUTE_DEVELOPER,
                                onClick = {
                                    navController.navigate(ROUTE_DEVELOPER) {
                                        launchSingleTop = true
                                    }
                                },
                                icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat (WhatsApp)") },
                                label = { Text("Chat") },
                                modifier = Modifier.testTag("nav_developer_dashboard")
                            )
                            NavigationBarItem(
                                selected = currentRoute == ROUTE_IMAGE_EDITOR,
                                onClick = {
                                    navController.navigate(ROUTE_IMAGE_EDITOR) {
                                        launchSingleTop = true
                                    }
                                },
                                icon = { Icon(Icons.Filled.PhotoLibrary, contentDescription = "Image Studio") },
                                label = { Text("Images") },
                                modifier = Modifier.testTag("nav_image_editor")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (showDevNav && useNavigationRail) {
                        NavigationRail(
                            containerColor = Color.White,
                            modifier = Modifier
                                .fillMaxHeight()
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .testTag("developer_bottom_nav")
                        ) {
                            NavigationRailItem(
                                selected = currentRoute == ROUTE_HOME,
                                onClick = {
                                    navController.navigate(ROUTE_HOME) {
                                        popUpTo(ROUTE_HOME) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                },
                                icon = { Icon(Icons.Filled.Home, contentDescription = "Quiz") },
                                label = { Text("Quiz") },
                                modifier = Modifier.testTag("nav_dev_home")
                            )
                            NavigationRailItem(
                                selected = currentRoute == ROUTE_NOTES_STUDIO,
                                onClick = {
                                    navController.navigate(ROUTE_NOTES_STUDIO) {
                                        launchSingleTop = true
                                    }
                                },
                                icon = { Icon(Icons.Filled.Description, contentDescription = "Notes (Notion)") },
                                label = { Text("Notes") },
                                modifier = Modifier.testTag("nav_notes_studio")
                            )
                            NavigationRailItem(
                                selected = currentRoute == ROUTE_DEVELOPER,
                                onClick = {
                                    navController.navigate(ROUTE_DEVELOPER) {
                                        launchSingleTop = true
                                    }
                                },
                                icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat (WhatsApp)") },
                                label = { Text("Chat") },
                                modifier = Modifier.testTag("nav_developer_dashboard")
                            )
                            NavigationRailItem(
                                selected = currentRoute == ROUTE_IMAGE_EDITOR,
                                onClick = {
                                    navController.navigate(ROUTE_IMAGE_EDITOR) {
                                        launchSingleTop = true
                                    }
                                },
                                icon = { Icon(Icons.Filled.PhotoLibrary, contentDescription = "Image Studio") },
                                label = { Text("Images") },
                                modifier = Modifier.testTag("nav_image_editor")
                            )
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_HOME,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                composable(
                    route = ROUTE_HOME,
                    enterTransition = { fadeIn(tween(enterDuration)) },
                    exitTransition = { fadeOut(tween(exitDuration)) }
                ) {
                    ChallengeScreen(
                        uiState = challengeState,
                        onKeypadSymbol = challengeViewModel::onKeypadSymbol,
                        onSelectMcqOption = challengeViewModel::onSelectMcqOption,
                        onBackspace = challengeViewModel::onBackspace,
                        onClearInput = challengeViewModel::onClearInput,
                        onSubmitAnswer = challengeViewModel::onSubmitAnswer,
                        onNextQuestion = challengeViewModel::onNextQuestion,
                        onToggleExplanation = challengeViewModel::onToggleExplanation,
                        onOpenChats = {
                            navController.navigate(ROUTE_DEVELOPER) {
                                launchSingleTop = true
                            }
                        },
                        onOpenSettings = {
                            navController.navigate(ROUTE_SETTINGS) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(
                    route = ROUTE_SETTINGS,
                    enterTransition = {
                        if (enterDuration == 0) fadeIn(tween(0))
                        else slideInHorizontally(tween(enterDuration)) { it / 4 } + fadeIn(tween(enterDuration))
                    },
                    exitTransition = {
                        if (exitDuration == 0) fadeOut(tween(0))
                        else slideOutHorizontally(tween(exitDuration)) { it / 4 } + fadeOut(tween(exitDuration))
                    }
                ) {
                    val state by settingsViewModel.uiState.collectAsStateWithLifecycle()
                    SettingsScreen(
                        uiState = state,
                        personalizationState = personalizationState,
                        onNavigateBack = { navController.popBackStack() },
                        onOpenPersonalization = {
                            navController.navigate(ROUTE_PERSONALIZATION) {
                                launchSingleTop = true
                            }
                        },
                        onOpenLearnerSurvey = { showManualSurvey = true },
                        onSetDeveloperModeEnabled = settingsViewModel::setDeveloperModeEnabled,
                        onOpenNotesStudio = {
                            navController.navigate(ROUTE_NOTES_STUDIO) {
                                launchSingleTop = true
                            }
                        },
                        onOpenDeveloperDashboard = {
                            navController.navigate(ROUTE_DEVELOPER) {
                                launchSingleTop = true
                            }
                        },
                        onRefreshAppLockEnvironment = {
                            settingsViewModel.refreshAppLockEnvironment(context)
                        },
                        onSetQuizLockEnabled = settingsViewModel::setQuizLockEnabled,
                        onToggleLockedPackage = settingsViewModel::toggleLockedPackage,
                        onClearLockedPackages = settingsViewModel::clearLockedPackages,
                        onDismissStatusMessage = settingsViewModel::clearStatusMessage
                    )
                }

                composable(
                    route = ROUTE_PERSONALIZATION,
                    enterTransition = {
                        if (enterDuration == 0) fadeIn(tween(0))
                        else slideInHorizontally(tween(enterDuration)) { it / 4 } + fadeIn(tween(enterDuration))
                    },
                    exitTransition = {
                        if (exitDuration == 0) fadeOut(tween(0))
                        else slideOutHorizontally(tween(exitDuration)) { it / 4 } + fadeOut(tween(exitDuration))
                    }
                ) {
                    PersonalizationScreen(
                        viewModel = personalizationViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = ROUTE_NOTES_STUDIO,
                    enterTransition = { fadeIn(tween(enterDuration)) },
                    exitTransition = { fadeOut(tween(exitDuration)) }
                ) {
                    NotesStudioScreen(
                        uiState = notesStudioState,
                        viewModel = notesStudioViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = ROUTE_IMAGE_EDITOR,
                    enterTransition = { fadeIn(tween(enterDuration)) },
                    exitTransition = { fadeOut(tween(exitDuration)) }
                ) {
                    ImageEditorScreen(
                        uiState = imageEditorState,
                        viewModel = imageEditorViewModel,
                        onOpenSettings = {
                            navController.navigate(ROUTE_SETTINGS) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(
                    route = ROUTE_DEVELOPER,
                    enterTransition = { fadeIn(tween(enterDuration)) },
                    exitTransition = { fadeOut(tween(exitDuration)) }
                ) {
                    ChatScreen(
                        chatState = chatState,
                        chatViewModel = chatViewModel,
                        notesState = notesStudioState,
                        currentQuestion = challengeState.currentQuestion,
                        onBack = { navController.popBackStack() },
                        onOpenNotesStudio = {
                            navController.navigate(ROUTE_NOTES_STUDIO) {
                                launchSingleTop = true
                            }
                        },
                        onOpenPracticeMode = {
                            navController.navigate(ROUTE_HOME) {
                                popUpTo(ROUTE_HOME) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onInsertParagraphToNote = { text ->
                            notesStudioViewModel.addBlock(BlockType.PARAGRAPH, initialContent = text)
                        },
                        onInsertLatexToNote = { latex ->
                            notesStudioViewModel.addBlock(BlockType.LATEX_BLOCK, initialContent = latex)
                        },
                        onInsertCodeToNote = { lang, code ->
                            notesStudioViewModel.addBlock(
                                type = BlockType.CODE_BLOCK,
                                initialContent = code,
                                metadata = lang
                            )
                        }
                    )
                }
            }
        }

            if (loadedPreferences != null && !loadedPreferences!!.hasAcceptedPermissions) {
                MandatoryPermissionsDialog(
                    isAccessibilityEnabled = isAccessibilityGranted,
                    isOverlayEnabled = isOverlayGranted,
                    onAllowAll = {
                        coroutineScope.launch {
                            container.repository.updatePreferences { prefs ->
                                prefs.copy(
                                    hasAcceptedPermissions = true,
                                    quizLockEnabled = true
                                )
                            }
                        }
                    }
                )
            } else if (loadedPreferences != null &&
                loadedPreferences!!.hasAcceptedPermissions &&
                (!loadedPreferences!!.hasCompletedSurvey || showManualSurvey)
            ) {
                LearnerSurveyDialog(
                    onSkip = {
                        val wasCustomTrack = loadedPreferences?.learnerTrackId != "DEFAULT"
                        showManualSurvey = false
                        coroutineScope.launch {
                            var updatedSnapshot = LearnerSurveyEngine.applyDefault(loadedPreferences!!)
                            container.repository.updatePreferences { prefs ->
                                LearnerSurveyEngine.applyDefault(prefs).also { updatedSnapshot = it }
                            }
                            if (wasCustomTrack) {
                                challengeViewModel.reloadQuestionForUpdatedProfile(updatedSnapshot)
                            }
                        }
                    },
                    onSelectDefault = {
                        val wasCustomTrack = loadedPreferences?.learnerTrackId != "DEFAULT"
                        showManualSurvey = false
                        coroutineScope.launch {
                            var updatedSnapshot = LearnerSurveyEngine.applyDefault(loadedPreferences!!)
                            container.repository.updatePreferences { prefs ->
                                LearnerSurveyEngine.applyDefault(prefs).also { updatedSnapshot = it }
                            }
                            if (wasCustomTrack) {
                                challengeViewModel.reloadQuestionForUpdatedProfile(updatedSnapshot)
                            }
                        }
                    },
                    onCompleteCustom = { root, subTrack, pace ->
                        showManualSurvey = false
                        coroutineScope.launch {
                            var updatedSnapshot = LearnerSurveyEngine.applySelection(
                                loadedPreferences!!, root, subTrack, pace
                            )
                            container.repository.updatePreferences { prefs ->
                                LearnerSurveyEngine.applySelection(prefs, root, subTrack, pace)
                                    .also { updatedSnapshot = it }
                            }
                            challengeViewModel.reloadQuestionForUpdatedProfile(updatedSnapshot)
                        }
                    }
                )
            }
            }
        }
    }
}
