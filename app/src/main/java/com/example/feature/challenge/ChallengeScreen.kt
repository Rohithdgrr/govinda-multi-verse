package com.example.feature.challenge

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.designsystem.BlueprintMetricTile
import com.example.core.designsystem.GovindaLogoMark
import com.example.core.designsystem.dynamicPatternBackground
import com.example.personalization.avatars.PersonalizationAvatar
import com.example.personalization.icons.PersonalizationIcon
import com.example.personalization.layouts.LayoutManager
import com.example.personalization.model.ChallengeLayoutStyle
import com.example.personalization.model.DashboardWidgetId
import com.example.personalization.model.IconGlyph
import com.example.personalization.model.NavigationLayoutStyle
import com.example.personalization.theme.LocalGovindaTokens

@Composable
fun ChallengeScreen(
    uiState: ChallengeUiState,
    onKeypadSymbol: (String) -> Unit,
    onBackspace: () -> Unit,
    onClearInput: () -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onToggleExplanation: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    onSelectMcqOption: (String) -> Unit = onKeypadSymbol,
    onOpenChats: () -> Unit = {}
) {
    val tokens = LocalGovindaTokens.current
    val density = tokens.layoutProfile.density
    val pagePad = LayoutManager.scaleDp(16.dp, density)
    val cardGap = LayoutManager.scaleDp(12.dp, density)
    val motionScale = tokens.effects.motionLevel.durationScale

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.background)
            .dynamicPatternBackground(
                pattern = tokens.backgroundPattern,
                borderColor = tokens.divider,
                primaryColor = tokens.primary,
                selectedColor = tokens.selectedState
            )
    ) {
        ChallengeTopNavBar(
            navStyle = tokens.layoutProfile.navigationLayout,
            onOpenSettings = onOpenSettings
        )

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            val isWide = maxWidth >= 700.dp ||
                (tokens.layoutProfile.challengeLayout == ChallengeLayoutStyle.SPLIT_PANEL && maxWidth >= 560.dp)
            val isCompactShort = maxHeight < 500.dp

            if (isWide || isCompactShort) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = pagePad, vertical = cardGap),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier.widthIn(max = if (isWide) 920.dp else 600.dp),
                        verticalArrangement = Arrangement.spacedBy(cardGap)
                    ) {
                        uiState.lockedAppName?.let { appName ->
                            QuizLockBanner(appName = appName)
                        }

                        if (tokens.layoutProfile.challengeLayout == ChallengeLayoutStyle.STATS_ASSISTED) {
                            StatsAssistedStrip(uiState = uiState)
                        }

                        if (uiState.isLoading || uiState.currentQuestion == null) {
                            LoadingBlueprintBox()
                        } else {
                            val question = uiState.currentQuestion
                            val isWrongOutcome = uiState.submissionOutcome?.evaluation?.isCorrect == false
                            if (isWide) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(cardGap)
                                    ) {
                                        QuestionEquationCard(
                                            question = question,
                                            submissionOutcome = uiState.submissionOutcome
                                        )
                                        if (uiState.submissionOutcome != null) {
                                            FeedbackExplanationCard(
                                                question = question,
                                                outcome = uiState.submissionOutcome,
                                                isQuizLockGate = uiState.lockedPackage != null,
                                                isExpanded = uiState.isExplanationExpanded,
                                                wrongCooldownSecondsRemaining = uiState.wrongCooldownSecondsRemaining,
                                                wrongAttemptCount = uiState.wrongAttemptCount,
                                                onToggleExplanation = onToggleExplanation,
                                                onNextQuestion = onNextQuestion
                                            )
                                        }
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        if (isWrongOutcome && uiState.wrongCooldownSecondsRemaining > 0) {
                                            WrongCooldownTimerBar(
                                                secondsRemaining = uiState.wrongCooldownSecondsRemaining,
                                                totalSeconds = uiState.wrongCooldownTotalSeconds,
                                                wrongAttemptCount = uiState.wrongAttemptCount
                                            )
                                        }
                                        KeypadInputDeck(
                                            uiState = uiState,
                                            question = question,
                                            onKeypadSymbol = onKeypadSymbol,
                                            onSelectMcqOption = onSelectMcqOption,
                                            onBackspace = onBackspace,
                                            onClearInput = onClearInput,
                                            onSubmitAnswer = onSubmitAnswer,
                                            onNextQuestion = onNextQuestion
                                        )
                                    }
                                }
                            } else {
                                QuestionEquationCard(
                                    question = question,
                                    submissionOutcome = uiState.submissionOutcome
                                )
                                if (uiState.submissionOutcome != null) {
                                    FeedbackExplanationCard(
                                        question = question,
                                        outcome = uiState.submissionOutcome,
                                        isQuizLockGate = uiState.lockedPackage != null,
                                        isExpanded = uiState.isExplanationExpanded,
                                        wrongCooldownSecondsRemaining = uiState.wrongCooldownSecondsRemaining,
                                        wrongAttemptCount = uiState.wrongAttemptCount,
                                        onToggleExplanation = onToggleExplanation,
                                        onNextQuestion = onNextQuestion
                                    )
                                }
                                if (isWrongOutcome && uiState.wrongCooldownSecondsRemaining > 0) {
                                    WrongCooldownTimerBar(
                                        secondsRemaining = uiState.wrongCooldownSecondsRemaining,
                                        totalSeconds = uiState.wrongCooldownTotalSeconds,
                                        wrongAttemptCount = uiState.wrongAttemptCount
                                    )
                                }
                                if (uiState.submissionOutcome == null || isWrongOutcome) {
                                    KeypadInputDeck(
                                        uiState = uiState,
                                        question = question,
                                        onKeypadSymbol = onKeypadSymbol,
                                        onSelectMcqOption = onSelectMcqOption,
                                        onBackspace = onBackspace,
                                        onClearInput = onClearInput,
                                        onSubmitAnswer = onSubmitAnswer,
                                        onNextQuestion = onNextQuestion
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                    }
                }
            } else {
                // Standard Portrait Phone: Clean centered equation above, interactive cooldown between, docked keypad at bottom
                val isWrongOutcome = uiState.submissionOutcome?.evaluation?.isCorrect == false
                val showKeypadDock = !uiState.isLoading &&
                    uiState.currentQuestion != null &&
                    (uiState.submissionOutcome == null || isWrongOutcome)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = pagePad, vertical = cardGap),
                        verticalArrangement = Arrangement.spacedBy(cardGap)
                    ) {
                        uiState.lockedAppName?.let { appName ->
                            QuizLockBanner(appName = appName)
                        }

                        if (tokens.layoutProfile.challengeLayout == ChallengeLayoutStyle.STATS_ASSISTED) {
                            StatsAssistedStrip(uiState = uiState)
                        }

                        if (uiState.isLoading || uiState.currentQuestion == null) {
                            LoadingBlueprintBox()
                        } else {
                            val enterMs = (180 * motionScale).toInt()
                            val exitMs = (120 * motionScale).toInt()
                            AnimatedContent(
                                targetState = uiState.currentQuestion,
                                transitionSpec = {
                                    if (enterMs == 0) {
                                        fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                                    } else {
                                        (fadeIn(tween(enterMs)) + slideInVertically(tween(enterMs)) { it / 16 }) togetherWith
                                            fadeOut(tween(exitMs))
                                    }
                                },
                                label = "questionTransition"
                            ) { question ->
                                Column(verticalArrangement = Arrangement.spacedBy(cardGap)) {
                                    QuestionEquationCard(
                                        question = question,
                                        submissionOutcome = uiState.submissionOutcome
                                    )

                                    if (uiState.submissionOutcome != null) {
                                        FeedbackExplanationCard(
                                            question = question,
                                            outcome = uiState.submissionOutcome,
                                            isQuizLockGate = uiState.lockedPackage != null,
                                            isExpanded = uiState.isExplanationExpanded,
                                            wrongCooldownSecondsRemaining = uiState.wrongCooldownSecondsRemaining,
                                            wrongAttemptCount = uiState.wrongAttemptCount,
                                            onToggleExplanation = onToggleExplanation,
                                            onNextQuestion = onNextQuestion
                                        )
                                    }
                                }
                            }
                        }

                        if (!showKeypadDock && uiState.submissionOutcome != null) {
                            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                        }
                    }

                    val currentQ = uiState.currentQuestion
                    if (showKeypadDock && currentQ != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(tokens.surface)
                                .windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            AnimatedVisibility(
                                visible = isWrongOutcome && uiState.wrongCooldownSecondsRemaining > 0,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(tokens.background)
                                        .padding(horizontal = pagePad, vertical = 8.dp)
                                ) {
                                    WrongCooldownTimerBar(
                                        secondsRemaining = uiState.wrongCooldownSecondsRemaining,
                                        totalSeconds = uiState.wrongCooldownTotalSeconds,
                                        wrongAttemptCount = uiState.wrongAttemptCount
                                    )
                                }
                            }
                            HorizontalDivider(thickness = 2.dp, color = tokens.border)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = pagePad, vertical = LayoutManager.scaleDp(12.dp, density)),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                KeypadInputDeck(
                                    uiState = uiState,
                                    question = currentQ,
                                    onKeypadSymbol = onKeypadSymbol,
                                    onSelectMcqOption = onSelectMcqOption,
                                    onBackspace = onBackspace,
                                    onClearInput = onClearInput,
                                    onSubmitAnswer = onSubmitAnswer,
                                    onNextQuestion = onNextQuestion
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsAssistedStrip(uiState: ChallengeUiState) {
    val tokens = LocalGovindaTokens.current
    val visibleWidgets = tokens.layoutProfile.widgets.filter { it.visible }.take(3)
    if (visibleWidgets.isEmpty()) return

    val updatedProfile = uiState.submissionOutcome?.adaptiveResult?.updatedProfile
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        visibleWidgets.forEach { widget ->
            val value = when (widget.id) {
                DashboardWidgetId.DAILY_STREAK -> "${uiState.preferences.quizLockedPackages.size} Apps"
                DashboardWidgetId.CURRENT_DIFFICULTY -> uiState.currentQuestion?.difficulty?.displayName ?: "Auto"
                DashboardWidgetId.XP -> uiState.currentQuestion?.category?.displayName ?: "Math"
                DashboardWidgetId.DAILY_TARGET -> "${uiState.currentQuestion?.expectedTimeSeconds ?: 10}s"
                DashboardWidgetId.ACCURACY_CHART -> if (updatedProfile != null) {
                    "${(updatedProfile.accuracy * 100).toInt()}%"
                } else {
                    "Ready"
                }
                else -> uiState.currentQuestion?.answerFormat?.name ?: "Keypad"
            }
            BlueprintMetricTile(
                label = widget.id.displayName,
                value = value,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ChallengeTopNavBar(
    navStyle: NavigationLayoutStyle,
    onOpenSettings: () -> Unit
) {
    val tokens = LocalGovindaTokens.current
    val isCompact = navStyle == NavigationLayoutStyle.COMPACT_TOP_BAR
    val isPill = navStyle == NavigationLayoutStyle.PILL_HEADER
    val buttonSize = if (tokens.effects.extraLargeTargets) 54.dp else 48.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(tokens.surface)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = if (isCompact) 6.dp else 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (isPill) {
                            Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(tokens.selectedState)
                                .border(1.4.dp, tokens.border, RoundedCornerShape(24.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        } else {
                            Modifier
                        }
                    )
            ) {
                GovindaLogoMark(size = if (isCompact) 32.dp else 36.dp)
                if (navStyle != NavigationLayoutStyle.ICON_ONLY) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        color = tokens.primary,
                        maxLines = 1
                    )
                }
                PersonalizationAvatar(
                    avatar = tokens.avatarProfile,
                    size = if (isCompact) 28.dp else 32.dp
                )
            }

            Surface(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(if (isPill) 24.dp else 6.dp),
                color = tokens.background,
                border = BorderStroke(1.5.dp, tokens.border),
                modifier = Modifier
                    .size(buttonSize)
                    .testTag("top_settings_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    PersonalizationIcon(
                        glyph = IconGlyph.SETTINGS,
                        iconProfile = tokens.iconProfile,
                        tint = tokens.primary,
                        contentDescription = stringResource(R.string.nav_settings)
                    )
                }
            }
        }
        HorizontalDivider(thickness = 2.dp, color = tokens.border)
    }
}

@Composable
private fun QuizLockBanner(appName: String) {
    val tokens = LocalGovindaTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(tokens.selectedState)
            .border(1.5.dp, tokens.border, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("quiz_lock_banner"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = tokens.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "SOLVE TO OPEN ${appName.uppercase()}",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = tokens.primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LoadingBlueprintBox() {
    val tokens = LocalGovindaTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = tokens.primary, strokeWidth = 3.dp)
    }
}

@Composable
private fun KeypadInputDeck(
    uiState: ChallengeUiState,
    question: com.example.domain.model.MathQuestion,
    onKeypadSymbol: (String) -> Unit,
    onSelectMcqOption: (String) -> Unit,
    onBackspace: () -> Unit,
    onClearInput: () -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit
) {
    val tokens = LocalGovindaTokens.current
    val isSubmitted = uiState.submissionOutcome != null
    val isWrongCompleted = uiState.submissionOutcome?.evaluation?.isCorrect == false
    if (uiState.wrongAttemptCount > 0 && !isSubmitted) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(tokens.warning.copy(alpha = 0.14f))
                .border(1.4.dp, tokens.warning, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("retry_attempt_banner"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TRY AGAIN · ATTEMPT #${uiState.wrongAttemptCount + 1}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = tokens.primaryText
            )
            Text(
                text = "SOLVE TO CONTINUE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = tokens.warning
            )
        }
    }
    AnswerDisplayBox(
        userInput = uiState.userInput,
        answerFormat = question.answerFormat,
        isSubmitted = isSubmitted,
        isCorrect = uiState.submissionOutcome?.evaluation?.isCorrect
    )
    if (question.usesTappableMcq) {
        ScienceGkMcqDeck(
            options = question.options,
            selectedOption = uiState.userInput,
            enabled = !isSubmitted && !uiState.isCooldownActive,
            canSubmit = uiState.canSubmit,
            cooldownSecondsRemaining = uiState.wrongCooldownSecondsRemaining,
            onSelectOption = onSelectMcqOption,
            onSubmit = onSubmitAnswer
        )
    } else {
        MathNumericKeypad(
            enabled = !isSubmitted && !uiState.isCooldownActive,
            canSubmit = uiState.canSubmit,
            cooldownSecondsRemaining = uiState.wrongCooldownSecondsRemaining,
            isWrongCompleted = isWrongCompleted,
            onSymbol = onKeypadSymbol,
            onBackspace = onBackspace,
            onClear = onClearInput,
            onSubmit = onSubmitAnswer,
            onNextQuestion = onNextQuestion
        )
    }
}

@Composable
private fun ScienceGkMcqDeck(
    options: List<String>,
    selectedOption: String,
    enabled: Boolean,
    canSubmit: Boolean,
    cooldownSecondsRemaining: Int,
    onSelectOption: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val tokens = LocalGovindaTokens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("science_gk_mcq_deck"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { idx, optText ->
            val label = ('A' + idx).toString()
            val isSelected = selectedOption == optText
            Surface(
                onClick = { if (enabled) onSelectOption(optText) },
                enabled = enabled,
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) tokens.selectedState else tokens.background,
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.4.dp,
                    color = if (isSelected) tokens.primary else tokens.border
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("mcq_option_$idx")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) tokens.primary else tokens.surface)
                            .border(1.dp, if (isSelected) tokens.primary else tokens.border, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) tokens.background else tokens.primaryText
                        )
                    }
                    Text(
                        text = optText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = tokens.primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Surface(
            onClick = { if (canSubmit) onSubmit() },
            enabled = canSubmit,
            shape = RoundedCornerShape(8.dp),
            color = if (canSubmit) tokens.primary else tokens.disabledState,
            border = BorderStroke(1.5.dp, if (canSubmit) tokens.primary else tokens.border),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (cooldownSecondsRemaining > 0) {
                        "RETRY IN ${cooldownSecondsRemaining}S"
                    } else {
                        "SUBMIT ANSWER"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (canSubmit) tokens.background else tokens.mutedText
                )
            }
        }
    }
}
