package com.example.feature.challenge

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.designsystem.BlueprintCard
import com.example.core.designsystem.BlueprintTimelineSteps
import com.example.core.designsystem.dynamicPatternBackground
import com.example.domain.model.AnswerFormat
import com.example.domain.model.MathQuestion
import com.example.domain.usecase.SubmissionOutcome
import com.example.personalization.model.ButtonStylePreset
import com.example.personalization.model.ChallengeLayoutStyle
import com.example.personalization.model.InputStylePreset
import com.example.personalization.model.KeypadStylePreset
import com.example.personalization.theme.GovindaDesignTokens
import com.example.personalization.theme.LocalGovindaTokens
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun QuestionEquationCard(
    question: MathQuestion,
    submissionOutcome: SubmissionOutcome? = null,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGovindaTokens.current
    val motionScale = tokens.effects.motionLevel.durationScale
    val layout = tokens.layoutProfile.challengeLayout
    val isLongPrompt = question.prompt.length > 26
    val verticalPad = when {
        isLongPrompt -> 20.dp
        layout == ChallengeLayoutStyle.LARGE_EQUATION || layout == ChallengeLayoutStyle.FULLSCREEN_FOCUS -> 42.dp
        layout == ChallengeLayoutStyle.COMPACT_EQUATION -> 18.dp
        else -> 32.dp
    }

    val shakeX = remember { Animatable(0f) }
    val popScale = remember { Animatable(1f) }
    val isCorrect = submissionOutcome?.evaluation?.isCorrect

    LaunchedEffect(question.id, submissionOutcome != null) {
        if (motionScale <= 0f) {
            shakeX.snapTo(0f)
            popScale.snapTo(1f)
            return@LaunchedEffect
        }
        when (isCorrect) {
            true -> {
                popScale.snapTo(0.97f)
                popScale.animateTo(
                    targetValue = 1.028f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                popScale.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = (180 * motionScale).toInt().coerceAtLeast(1))
                )
            }
            false -> {
                val amp = 16f
                val stepMs = (48 * motionScale).toInt().coerceAtLeast(1)
                listOf(-amp, amp * 0.9f, -amp * 0.7f, amp * 0.45f, -amp * 0.2f, 0f).forEach { target ->
                    shakeX.animateTo(target, tween(durationMillis = stepMs))
                }
            }
            null -> {
                shakeX.snapTo(0f)
                popScale.snapTo(0.97f)
                popScale.animateTo(
                    targetValue = 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }
    }

    val targetBorder = when (isCorrect) {
        true -> tokens.success
        false -> tokens.error
        null -> tokens.primary
    }
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorder,
        animationSpec = tween(durationMillis = (220 * motionScale).toInt().coerceAtLeast(1)),
        label = "questionCardBorder"
    )

    BlueprintCard(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = shakeX.value
                scaleX = popScale.value
                scaleY = popScale.value
            }
            .testTag("question_card"),
        containerColor = tokens.background,
        borderColor = animatedBorderColor,
        borderWidth = 1.8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .dynamicPatternBackground(
                    pattern = tokens.backgroundPattern,
                    borderColor = tokens.border,
                    primaryColor = tokens.primary,
                    selectedColor = tokens.selectedState
                )
                .padding(horizontal = 18.dp, vertical = verticalPad),
            contentAlignment = Alignment.Center
        ) {
            val baseSize = when {
                question.prompt.length > 52 -> 18f
                question.prompt.length > 34 -> 21f
                question.prompt.length > 22 -> 25f
                question.prompt.length > 16 -> 28f
                question.prompt.length > 11 -> 34f
                else -> 42f
            }
            val layoutMultiplier = when (layout) {
                ChallengeLayoutStyle.LARGE_EQUATION, ChallengeLayoutStyle.FULLSCREEN_FOCUS -> 1.12f
                ChallengeLayoutStyle.COMPACT_EQUATION -> 0.88f
                else -> 1.0f
            }
            val fontSize = (baseSize * layoutMultiplier * tokens.typographyProfile.fontSizeScale).sp

            val highlightedPrompt = remember(question.id, tokens.primary, tokens.primaryText) {
                highlightMathOperators(
                    prompt = question.prompt,
                    operatorColor = tokens.primary,
                    digitColor = tokens.primaryText
                )
            }

            Text(
                text = highlightedPrompt,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = fontSize,
                    lineHeight = fontSize * 1.24f
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("question_prompt")
            )
        }
    }
}

@Composable
fun AnswerDisplayBox(
    userInput: String,
    answerFormat: AnswerFormat,
    isSubmitted: Boolean,
    isCorrect: Boolean?,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGovindaTokens.current
    val inputStyle = tokens.componentStyle.inputStyle
    val motionScale = tokens.effects.motionLevel.durationScale

    val textBounce = remember { Animatable(1f) }
    val boxShakeX = remember { Animatable(0f) }

    LaunchedEffect(userInput) {
        if (motionScale > 0f && userInput.isNotEmpty() && !isSubmitted) {
            textBounce.snapTo(1.09f)
            textBounce.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            )
        } else {
            textBounce.snapTo(1f)
        }
    }

    LaunchedEffect(isSubmitted, isCorrect) {
        if (motionScale > 0f && isSubmitted && isCorrect == false) {
            val stepMs = (42 * motionScale).toInt().coerceAtLeast(1)
            listOf(-12f, 11f, -7f, 4f, 0f).forEach { target ->
                boxShakeX.animateTo(target, tween(stepMs))
            }
        } else {
            boxShakeX.snapTo(0f)
        }
    }

    val borderColor by animateColorAsState(
        targetValue = when (isCorrect) {
            true -> tokens.success
            false -> tokens.error
            null -> if (userInput.isNotEmpty()) tokens.primary else tokens.border
        },
        animationSpec = tween(durationMillis = (180 * motionScale).toInt().coerceAtLeast(1)),
        label = "answerBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = when (isCorrect) {
            true -> tokens.success.copy(alpha = 0.14f)
            false -> tokens.error.copy(alpha = 0.14f)
            null -> when (inputStyle) {
                InputStylePreset.FILLED -> tokens.selectedState.copy(alpha = 0.7f)
                InputStylePreset.MINIMAL, InputStylePreset.UNDERLINED -> tokens.background
                else -> tokens.surface
            }
        },
        animationSpec = tween(durationMillis = (180 * motionScale).toInt().coerceAtLeast(1)),
        label = "answerBg"
    )
    val shape = when (inputStyle) {
        InputStylePreset.TECHNICAL_MONO -> RoundedCornerShape(2.dp)
        InputStylePreset.UNDERLINED, InputStylePreset.MINIMAL -> RoundedCornerShape(0.dp)
        else -> RoundedCornerShape(6.dp)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { translationX = boxShakeX.value }
            .testTag("answer_display")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (tokens.effects.extraLargeTargets) 64.dp else 56.dp)
                .clip(shape)
                .background(bgColor)
                .then(
                    if (inputStyle == InputStylePreset.OUTLINED ||
                        inputStyle == InputStylePreset.FILLED ||
                        inputStyle == InputStylePreset.TECHNICAL_MONO
                    ) {
                        Modifier.border(2.dp, borderColor, shape)
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (answerFormat == AnswerFormat.FRACTION) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(tokens.background)
                        .border(1.5.dp, tokens.border, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "a/b",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = tokens.primaryText
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (userInput.isEmpty() && !isSubmitted) {
                    Text(
                        text = stringResource(R.string.hint_enter_answer),
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.mutedText
                    )
                } else {
                    Text(
                        text = userInput.ifEmpty { "—" },
                        style = MaterialTheme.typography.displayMedium,
                        color = when (isCorrect) {
                            true -> tokens.success
                            false -> tokens.error
                            null -> tokens.primaryText
                        },
                        modifier = Modifier.graphicsLayer {
                            scaleX = textBounce.value
                            scaleY = textBounce.value
                        }
                    )
                }
                if (!isSubmitted) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(26.dp)
                            .alpha(cursorAlpha)
                            .background(tokens.primary)
                    )
                }
            }
        }
        if (inputStyle == InputStylePreset.UNDERLINED) {
            HorizontalDivider(thickness = 2.dp, color = borderColor)
        }
    }
}

@Composable
fun WrongCooldownTimerBar(
    secondsRemaining: Int,
    totalSeconds: Int,
    wrongAttemptCount: Int = 1,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGovindaTokens.current
    val haptics = LocalHapticFeedback.current
    val motionScale = tokens.effects.motionLevel.durationScale
    val safeTotal = totalSeconds.coerceAtLeast(1)
    val rawFraction = (secondsRemaining.toFloat() / safeTotal.toFloat()).coerceIn(0f, 1f)

    val smoothFraction by animateFloatAsState(
        targetValue = rawFraction,
        animationSpec = tween(
            durationMillis = if (motionScale <= 0f) 1 else 900,
            easing = FastOutSlowInEasing
        ),
        label = "cooldownSweep"
    )

    val tickPulse = remember { Animatable(1f) }
    var tapPulseCount by remember { mutableIntStateOf(0) }
    val tapScale = remember { Animatable(1f) }

    LaunchedEffect(secondsRemaining) {
        if (motionScale > 0f && secondsRemaining > 0) {
            tickPulse.snapTo(1.14f)
            tickPulse.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    LaunchedEffect(tapPulseCount) {
        if (tapPulseCount > 0 && motionScale > 0f) {
            tapScale.snapTo(0.96f)
            tapScale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            )
        }
    }

    val accentColor by animateColorAsState(
        targetValue = if (secondsRemaining <= 5) tokens.warning else tokens.error,
        animationSpec = tween(250),
        label = "cooldownAccent"
    )

    val reflectionTip = remember(tapPulseCount, secondsRemaining) {
        val tips = listOf(
            "Re-check the question above & try again in ${secondsRemaining}s",
            "Check signs, brackets, or unit conversion before retrying",
            "Question stays until answered correctly"
        )
        if (tapPulseCount == 0) tips[0] else tips[tapPulseCount % tips.size]
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = tapScale.value
                scaleY = tapScale.value
            }
            .clip(RoundedCornerShape(8.dp))
            .background(tokens.surface)
            .border(1.8.dp, accentColor, RoundedCornerShape(8.dp))
            .clickable {
                tapPulseCount += 1
                if (tokens.effects.hapticsEnabled) {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
            .testTag("wrong_cooldown_timer")
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    val barWidth = size.width * smoothFraction
                    drawRect(
                        color = accentColor.copy(alpha = 0.12f),
                        size = Size(barWidth, size.height)
                    )
                }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .graphicsLayer {
                        scaleX = tickPulse.value
                        scaleY = tickPulse.value
                    },
                contentAlignment = Alignment.Center
            ) {
                val trackColor = tokens.divider
                Canvas(modifier = Modifier.size(38.dp)) {
                    val stroke = 3.5.dp.toPx()
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = stroke)
                    )
                    drawArc(
                        color = accentColor,
                        startAngle = -90f,
                        sweepAngle = 360f * smoothFraction,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${secondsRemaining}s",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    ),
                    color = tokens.primaryText,
                    maxLines = 1
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "WRONG · TRY AGAIN IN ${secondsRemaining}s",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    ),
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = reflectionTip,
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(tokens.background)
                    .border(1.4.dp, accentColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "TRY #${wrongAttemptCount}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = accentColor
                )
            }
        }
    }
}

@Composable
fun MathNumericKeypad(
    enabled: Boolean,
    canSubmit: Boolean,
    cooldownSecondsRemaining: Int = 0,
    isWrongCompleted: Boolean = false,
    onSymbol: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    onNextQuestion: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tokens = LocalGovindaTokens.current
    val haptics = LocalHapticFeedback.current
    val keypadStyle = tokens.componentStyle.keypadStyle

    val keyHeight = when {
        tokens.effects.extraLargeTargets -> 62.dp
        keypadStyle == KeypadStylePreset.LARGE_ACCESSIBILITY -> 62.dp
        keypadStyle == KeypadStylePreset.COMPACT_CALCULATOR -> 48.dp
        else -> 54.dp
    }
    val gridSpacing = if (keypadStyle == KeypadStylePreset.COMPACT_CALCULATOR) 5.dp else 8.dp
    val corner = when (keypadStyle) {
        KeypadStylePreset.COMPACT_CALCULATOR -> 3.dp
        KeypadStylePreset.LARGE_ACCESSIBILITY -> 10.dp
        else -> 6.dp
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(gridSpacing)
    ) {
        val rows = listOf(
            listOf("7", "8", "9", "⌫"),
            listOf("4", "5", "6", "/"),
            listOf("1", "2", "3", "-"),
            listOf("CLR", "0", ".", "SUBMIT")
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gridSpacing)
            ) {
                row.forEach { key ->
                    when (key) {
                        "SUBMIT" -> {
                            val isReadyNext = isWrongCompleted && cooldownSecondsRemaining == 0
                            val isActive = (enabled && canSubmit) || isReadyNext
                            val actionLabel = when {
                                cooldownSecondsRemaining > 0 -> "${cooldownSecondsRemaining}s"
                                isReadyNext -> "NEXT"
                                else -> stringResource(R.string.action_submit).uppercase()
                            }
                            DynamicActionButton(
                                text = actionLabel,
                                enabled = isActive,
                                isCooldownWaiting = cooldownSecondsRemaining > 0,
                                style = tokens.componentStyle.buttonStyle,
                                tokens = tokens,
                                onClick = {
                                    triggerKeyFeedback(tokens, haptics, isSubmit = true)
                                    if (isReadyNext) onNextQuestion() else onSubmit()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(keyHeight)
                                    .testTag("submit_button")
                            )
                        }
                        "⌫" -> {
                            KeypadKeyTile(
                                label = null,
                                enabled = enabled,
                                keypadStyle = keypadStyle,
                                tokens = tokens,
                                keyHeight = keyHeight,
                                cornerDp = corner,
                                containerColor = tokens.selectedState,
                                textColor = tokens.primary,
                                borderColor = tokens.primary,
                                bottomBevelColor = tokens.primary,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("keypad_backspace"),
                                onClick = {
                                    triggerKeyFeedback(tokens, haptics, isSubmit = false)
                                    onBackspace()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = if (enabled) tokens.primary else tokens.mutedText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        "CLR" -> {
                            KeypadKeyTile(
                                label = stringResource(R.string.action_clear),
                                enabled = enabled,
                                keypadStyle = keypadStyle,
                                tokens = tokens,
                                keyHeight = keyHeight,
                                cornerDp = corner,
                                containerColor = tokens.surface,
                                textColor = tokens.primaryText,
                                borderColor = tokens.border,
                                bottomBevelColor = tokens.border,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("keypad_clear"),
                                onClick = {
                                    triggerKeyFeedback(tokens, haptics, isSubmit = false)
                                    onClear()
                                }
                            )
                        }
                        else -> {
                            val isOperator = key == "/" || key == "-" || key == "."
                            KeypadKeyTile(
                                label = if (key == "-") "±" else key,
                                enabled = enabled,
                                keypadStyle = keypadStyle,
                                tokens = tokens,
                                keyHeight = keyHeight,
                                cornerDp = corner,
                                containerColor = if (isOperator) tokens.surface else tokens.background,
                                textColor = if (isOperator) tokens.primary else tokens.primaryText,
                                borderColor = tokens.border,
                                bottomBevelColor = tokens.border,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("keypad_key_$key"),
                                onClick = {
                                    triggerKeyFeedback(tokens, haptics, isSubmit = false)
                                    onSymbol(key)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DynamicActionButton(
    text: String,
    enabled: Boolean,
    isCooldownWaiting: Boolean,
    style: ButtonStylePreset,
    tokens: GovindaDesignTokens,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val corner = if (style == ButtonStylePreset.COMPACT_RECTANGULAR) 2.dp else 6.dp
    val motionScale = tokens.effects.motionLevel.durationScale
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var submitPulse by remember { mutableIntStateOf(0) }
    val readyScale = remember { Animatable(1f) }

    LaunchedEffect(enabled) {
        if (enabled && motionScale > 0f) {
            readyScale.snapTo(0.95f)
            readyScale.animateTo(
                targetValue = 1.035f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            readyScale.animateTo(1.0f, tween((130 * motionScale).toInt().coerceAtLeast(1)))
        }
    }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && motionScale > 0f) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "submitPressScale"
    )
    val iconSlideDp by animateDpAsState(
        targetValue = if ((isPressed || submitPulse > 0) && motionScale > 0f) 3.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "submitIconSlide"
    )

    val combinedModifier = modifier.graphicsLayer {
        val s = pressScale * readyScale.value
        scaleX = s
        scaleY = s
    }

    val wrappedOnClick = {
        submitPulse += 1
        onClick()
    }

    when (style) {
        ButtonStylePreset.OUTLINED -> {
            OutlinedButton(
                onClick = wrappedOnClick,
                enabled = enabled,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(corner),
                contentPadding = PaddingValues(horizontal = 4.dp),
                border = BorderStroke(2.dp, if (enabled) tokens.primary else tokens.border),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = tokens.background,
                    contentColor = tokens.primary
                ),
                modifier = combinedModifier
            ) {
                SubmitButtonContent(
                    text = text,
                    showLabel = true,
                    isCooldownWaiting = isCooldownWaiting,
                    iconOffsetDp = iconSlideDp
                )
            }
        }
        ButtonStylePreset.TEXT -> {
            TextButton(
                onClick = wrappedOnClick,
                enabled = enabled,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(corner),
                contentPadding = PaddingValues(horizontal = 4.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = tokens.primary
                ),
                modifier = combinedModifier
            ) {
                SubmitButtonContent(
                    text = text,
                    showLabel = true,
                    isCooldownWaiting = isCooldownWaiting,
                    iconOffsetDp = iconSlideDp
                )
            }
        }
        ButtonStylePreset.ICON_ONLY -> {
            Button(
                onClick = wrappedOnClick,
                enabled = enabled,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(corner),
                contentPadding = PaddingValues(horizontal = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.primary,
                    contentColor = tokens.onPrimary,
                    disabledContainerColor = tokens.disabledState,
                    disabledContentColor = tokens.secondaryText
                ),
                modifier = combinedModifier
            ) {
                SubmitButtonContent(
                    text = text,
                    showLabel = isCooldownWaiting,
                    isCooldownWaiting = isCooldownWaiting,
                    iconOffsetDp = iconSlideDp
                )
            }
        }
        else -> {
            val bg = if (style == ButtonStylePreset.HIGH_CONTRAST) tokens.primaryText else tokens.primary
            val fg = if (style == ButtonStylePreset.HIGH_CONTRAST) tokens.background else tokens.onPrimary
            Button(
                onClick = wrappedOnClick,
                enabled = enabled,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(corner),
                contentPadding = PaddingValues(horizontal = 4.dp),
                border = BorderStroke(
                    width = 2.dp,
                    color = if (enabled) bg else tokens.border
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = bg,
                    contentColor = fg,
                    disabledContainerColor = tokens.selectedState,
                    disabledContentColor = tokens.secondaryText
                ),
                modifier = combinedModifier
            ) {
                SubmitButtonContent(
                    text = text,
                    showLabel = true,
                    isCooldownWaiting = isCooldownWaiting,
                    iconOffsetDp = iconSlideDp
                )
            }
        }
    }
}

@Composable
private fun SubmitButtonContent(
    text: String,
    showLabel: Boolean,
    isCooldownWaiting: Boolean = false,
    iconOffsetDp: androidx.compose.ui.unit.Dp = 0.dp
) {
    if (showLabel) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 12.sp,
                letterSpacing = 0.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            softWrap = false
        )
        Spacer(modifier = Modifier.width(2.dp))
    }
    Icon(
        imageVector = if (isCooldownWaiting) Icons.Filled.HourglassBottom else Icons.AutoMirrored.Filled.Send,
        contentDescription = if (showLabel) null else text,
        modifier = Modifier
            .offset { IntOffset(iconOffsetDp.roundToPx(), 0) }
            .size(if (showLabel) 13.dp else 20.dp)
    )
}

@Composable
private fun KeypadKeyTile(
    label: String?,
    enabled: Boolean,
    keypadStyle: KeypadStylePreset,
    tokens: GovindaDesignTokens,
    keyHeight: androidx.compose.ui.unit.Dp,
    cornerDp: androidx.compose.ui.unit.Dp,
    containerColor: Color,
    textColor: Color,
    borderColor: Color,
    bottomBevelColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    customContent: (@Composable () -> Unit)? = null
) {
    val motionScale = tokens.effects.motionLevel.durationScale
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var tapCount by remember { mutableIntStateOf(0) }
    val tapFlash = remember { Animatable(0f) }

    LaunchedEffect(tapCount) {
        if (tapCount > 0 && motionScale > 0f) {
            tapFlash.snapTo(1f)
            tapFlash.animateTo(0f, tween(durationMillis = (180 * motionScale).toInt().coerceAtLeast(1)))
        }
    }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled && motionScale > 0f) 0.91f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "keyPressScale"
    )
    val pressOffsetY by animateDpAsState(
        targetValue = if (isPressed && enabled && motionScale > 0f) 2.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "keyPressOffset"
    )

    val drawBevel = enabled && (keypadStyle == KeypadStylePreset.BLUEPRINT || keypadStyle == KeypadStylePreset.LARGE_ACCESSIBILITY)
    val resolvedBorder = when {
        !enabled -> tokens.divider
        keypadStyle == KeypadStylePreset.FLAT_WHITE -> Color.Transparent
        isPressed || tapFlash.value > 0.3f -> tokens.primary
        else -> borderColor
    }
    val baseContainer = when {
        !enabled -> tokens.disabledState
        keypadStyle == KeypadStylePreset.OUTLINED -> tokens.background
        else -> containerColor
    }
    val resolvedContainer by animateColorAsState(
        targetValue = if ((isPressed || tapFlash.value > 0.35f) && enabled) tokens.selectedState else baseContainer,
        animationSpec = tween(durationMillis = (90 * motionScale).toInt().coerceAtLeast(1)),
        label = "keyContainerFlash"
    )

    Surface(
        onClick = {
            tapCount += 1
            onClick()
        },
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .height(keyHeight)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
                translationY = pressOffsetY.toPx()
            }
            .drawBehind {
                if (drawBevel) {
                    drawRoundRect(
                        color = bottomBevelColor,
                        topLeft = Offset(0f, 3.dp.toPx()),
                        size = Size(size.width, size.height - 1.dp.toPx()),
                        cornerRadius = CornerRadius(cornerDp.toPx(), cornerDp.toPx())
                    )
                }
            }
            .padding(bottom = if (drawBevel) 3.dp else 0.dp),
        shape = RoundedCornerShape(cornerDp),
        color = resolvedContainer,
        border = if (resolvedBorder != Color.Transparent) BorderStroke(1.8.dp, resolvedBorder) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (customContent != null) {
                customContent()
            } else if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (keypadStyle == KeypadStylePreset.LARGE_ACCESSIBILITY) 24.sp else 20.sp
                    ),
                    color = if (enabled) textColor else tokens.mutedText
                )
            }
        }
    }
}

private fun triggerKeyFeedback(
    tokens: GovindaDesignTokens,
    haptics: HapticFeedback,
    isSubmit: Boolean
) {
    if (tokens.effects.hapticsEnabled) {
        haptics.performHapticFeedback(
            if (isSubmit) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
        )
    }
    if (tokens.effects.soundEnabled) {
        runCatching {
            val tg = ToneGenerator(AudioManager.STREAM_SYSTEM, 30)
            tg.startTone(
                if (isSubmit) ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_BEEP,
                if (isSubmit) 65 else 35
            )
        }
    }
}

@Composable
fun FeedbackExplanationCard(
    question: MathQuestion,
    outcome: SubmissionOutcome,
    isQuizLockGate: Boolean,
    isExpanded: Boolean,
    wrongCooldownSecondsRemaining: Int = 0,
    wrongAttemptCount: Int = 0,
    onToggleExplanation: () -> Unit,
    onNextQuestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGovindaTokens.current
    val motionScale = tokens.effects.motionLevel.durationScale
    val isCorrect = outcome.evaluation.isCorrect
    val borderColor = if (isCorrect) tokens.success else tokens.error
    val headerBg = if (isCorrect) tokens.success.copy(alpha = 0.14f) else tokens.error.copy(alpha = 0.14f)
    val statusColor = if (isCorrect) tokens.success else tokens.error

    val cardScale = remember { Animatable(0.94f) }
    val badgeScale = remember { Animatable(0.55f) }
    val burstProgress = remember { Animatable(0f) }

    LaunchedEffect(question.id, outcome) {
        if (motionScale <= 0f) {
            cardScale.snapTo(1f)
            badgeScale.snapTo(1f)
            burstProgress.snapTo(1f)
            return@LaunchedEffect
        }
        cardScale.snapTo(0.94f)
        badgeScale.snapTo(0.45f)
        burstProgress.snapTo(0f)
        cardScale.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    LaunchedEffect(question.id, outcome) {
        if (motionScale <= 0f) return@LaunchedEffect
        badgeScale.animateTo(
            targetValue = 1.18f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
        badgeScale.animateTo(1.0f, tween((140 * motionScale).toInt().coerceAtLeast(1)))
    }

    LaunchedEffect(question.id, outcome, isCorrect) {
        if (isCorrect && motionScale > 0f) {
            burstProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = (560 * motionScale).toInt().coerceAtLeast(1))
            )
        }
    }

    val confidencePraise = remember(question.id) {
        val phrases = listOf("Exact!", "Locked in!", "Clean solve!", "Sharp focus!")
        phrases[(question.id.hashCode().and(0x7fffffff)) % phrases.size]
    }

    BlueprintCard(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale.value
                scaleY = cardScale.value
            }
            .testTag("feedback_card"),
        containerColor = tokens.background,
        borderColor = borderColor,
        borderWidth = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCorrect && burstProgress.value < 0.99f) {
                        val p = burstProgress.value
                        val sparkleColor = tokens.success
                        val accentSparkle = tokens.primary
                        Canvas(modifier = Modifier.size(34.dp)) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val radius = (8.dp.toPx() + 14.dp.toPx() * p)
                            val dotAlpha = (1f - p).coerceIn(0f, 1f)
                            for (i in 0 until 8) {
                                val angle = (i * 45.0) * PI / 180.0
                                val dx = cx + (cos(angle) * radius).toFloat()
                                val dy = cy + (sin(angle) * radius).toFloat()
                                drawCircle(
                                    color = (if (i % 2 == 0) sparkleColor else accentSparkle).copy(alpha = dotAlpha),
                                    radius = 2.5.dp.toPx() * (1f - p * 0.4f),
                                    center = Offset(dx, dy)
                                )
                            }
                        }
                    }
                    Icon(
                        imageVector = if (isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Close,
                        contentDescription = stringResource(
                            if (isCorrect) R.string.feedback_correct else R.string.feedback_incorrect
                        ),
                        tint = statusColor,
                        modifier = Modifier
                            .size(26.dp)
                            .graphicsLayer {
                                scaleX = badgeScale.value
                                scaleY = badgeScale.value
                            }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isCorrect) {
                            "${stringResource(R.string.feedback_correct).uppercase()} · $confidencePraise"
                        } else {
                            "WRONG ANSWER"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                    if (!isCorrect) {
                        val waitText = if (wrongCooldownSecondsRemaining > 0) {
                            "Try again in ${wrongCooldownSecondsRemaining}s (Attempt #$wrongAttemptCount)"
                        } else {
                            "Try again on this question (Attempt #$wrongAttemptCount)"
                        }
                        Text(
                            text = waitText,
                            style = MaterialTheme.typography.labelLarge,
                            color = tokens.primaryText
                        )
                    }
                }
            }

            // Show step-by-step explanation & next question button ONLY when solved correctly
            if (isCorrect) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggleExplanation)
                        .heightIn(min = 48.dp)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("toggle_explanation_button"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            if (isExpanded) R.string.action_hide_explanation else R.string.action_show_explanation
                        ),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = tokens.primary
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = tokens.primary
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(tokens.surface)
                            .border(1.5.dp, tokens.divider)
                            .padding(14.dp)
                            .testTag("explanation_steps")
                    ) {
                        BlueprintTimelineSteps(steps = question.explanationSteps)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Button(
                        onClick = onNextQuestion,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(2.dp, tokens.primary),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.primary,
                            contentColor = tokens.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("next_question_button")
                    ) {
                        Text(
                            text = stringResource(R.string.action_new_question).uppercase(),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun highlightMathOperators(
    prompt: String,
    operatorColor: Color,
    digitColor: Color
): AnnotatedString {
    val operators = setOf('+', '−', '×', '÷', '%', '√', '∛', '^', '²', '³', '(', ')', '=', '□')
    return buildAnnotatedString {
        prompt.forEach { ch ->
            if (ch in operators) {
                withStyle(SpanStyle(color = operatorColor, fontWeight = FontWeight.Bold)) {
                    append(ch)
                }
            } else {
                withStyle(SpanStyle(color = digitColor)) {
                    append(ch)
                }
            }
        }
    }
}
