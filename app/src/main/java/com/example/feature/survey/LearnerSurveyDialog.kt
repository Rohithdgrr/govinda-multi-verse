package com.example.feature.survey

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.designsystem.BlueprintBadge
import com.example.core.designsystem.BlueprintCard
import com.example.personalization.theme.LocalGovindaTokens

/**
 * Compact hierarchical onboarding survey shown immediately after accepting permissions:
 * - Step 1: Root learner class, with 1-tap "By Default" and "Skip" actions.
 * - Step 2: Sub-track specialization tailored to the selected root class.
 * - Step 3: Starting pace & challenge calibration -> saves exact track categories immediately.
 */
@Composable
fun LearnerSurveyDialog(
    onSkip: () -> Unit,
    onSelectDefault: () -> Unit,
    onCompleteCustom: (root: SurveyRootTrack, subTrack: SurveySubTrackOption, pace: SurveyPaceLevel) -> Unit
) {
    val tokens = LocalGovindaTokens.current
    var step by remember { mutableIntStateOf(1) }
    var selectedRoot by remember { mutableStateOf<SurveyRootTrack?>(null) }
    var selectedSubTrack by remember { mutableStateOf<SurveySubTrackOption?>(null) }

    BackHandler(enabled = step > 1) {
        if (step == 3) {
            step = 2
            selectedSubTrack = null
        } else if (step == 2) {
            step = 1
            selectedRoot = null
        }
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        BlueprintCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .heightIn(max = 650.dp)
                .testTag("learner_survey_dialog"),
            containerColor = tokens.background,
            borderColor = tokens.primary,
            borderWidth = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        BlueprintBadge(
                            text = if (step == 1) "STEP 1 · QUICK SETUP" else "STEP $step OF 3"
                        )
                    }

                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("survey_skip_button")
                    ) {
                        Text(
                            text = "SKIP",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = tokens.secondaryText
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = {
                        when (step) {
                            1 -> 0.34f
                            2 -> 0.67f
                            else -> 1.0f
                        }
                    },
                    color = tokens.primary,
                    trackColor = tokens.selectedState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                )

                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        (slideInHorizontally { it / 5 } + fadeIn())
                            .togetherWith(slideOutHorizontally { -it / 5 } + fadeOut())
                    },
                    label = "surveyStepTransition",
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                ) { activeStep ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (activeStep) {
                            1 -> {
                                Text(
                                    text = "WHO IS THIS FOR?",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = tokens.primaryText
                                )
                                Text(
                                    text = "Pick your goal so GOVINDA suggests only relevant questions, or continue with By Default.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.secondaryText
                                )

                                // Highlighted Default Option (1-tap completion)
                                SurveyChoiceRow(
                                    title = SurveyRootTrack.DEFAULT.title,
                                    subtitle = SurveyRootTrack.DEFAULT.subtitle,
                                    badge = SurveyRootTrack.DEFAULT.badge,
                                    highlighted = true,
                                    tag = "survey_option_DEFAULT",
                                    onClick = onSelectDefault
                                )

                                // Custom hierarchical branches
                                SurveyRootTrack.entries
                                    .filter { it != SurveyRootTrack.DEFAULT }
                                    .forEach { root ->
                                        SurveyChoiceRow(
                                            title = root.title,
                                            subtitle = root.subtitle,
                                            badge = root.badge,
                                            highlighted = false,
                                            tag = "survey_option_${root.id}",
                                            onClick = {
                                                selectedRoot = root
                                                selectedSubTrack = null
                                                step = 2
                                            }
                                        )
                                    }
                            }

                            2 -> {
                                val root = selectedRoot ?: SurveyRootTrack.SCHOOL
                                val options = remember(root) { LearnerSurveyEngine.subTracksFor(root) }
                                Text(
                                    text = "CHOOSE YOUR EXACT FOCUS",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = tokens.primaryText
                                )
                                Text(
                                    text = "${root.title} — pick your level or topic area:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.secondaryText
                                )

                                options.forEach { sub ->
                                    SurveyChoiceRow(
                                        title = sub.title,
                                        subtitle = sub.subtitle,
                                        badge = "${sub.categories.size} TOPIC${if (sub.categories.size > 1) "S" else ""}",
                                        highlighted = false,
                                        tag = "survey_sub_${sub.id}",
                                        onClick = {
                                            selectedSubTrack = sub
                                            step = 3
                                        }
                                    )
                                }
                            }

                            else -> {
                                val root = selectedRoot ?: SurveyRootTrack.SCHOOL
                                val sub = selectedSubTrack ?: LearnerSurveyEngine.subTracksFor(root).first()
                                Text(
                                    text = "PICK YOUR STARTING PACE",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = tokens.primaryText
                                )
                                Text(
                                    text = "Selected: ${sub.title}. How should difficulty begin?",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.secondaryText
                                )

                                SurveyPaceLevel.entries.forEach { pace ->
                                    SurveyChoiceRow(
                                        title = pace.title,
                                        subtitle = pace.subtitle,
                                        badge = if (pace == SurveyPaceLevel.BALANCED) "REC" else null,
                                        highlighted = pace == SurveyPaceLevel.BALANCED,
                                        tag = "survey_pace_${pace.id}",
                                        onClick = {
                                            onCompleteCustom(root, sub, pace)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Footer Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 1) {
                        OutlinedButton(
                            onClick = {
                                if (step == 3) {
                                    step = 2
                                    selectedSubTrack = null
                                } else {
                                    step = 1
                                    selectedRoot = null
                                }
                            },
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.5.dp, tokens.border),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("survey_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = tokens.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BACK",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = tokens.primary
                            )
                        }
                    }

                    Button(
                        onClick = onSelectDefault,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.primary,
                            contentColor = tokens.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("survey_use_default_button")
                    ) {
                        Text(
                            text = "USE BY DEFAULT",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SurveyChoiceRow(
    title: String,
    subtitle: String,
    badge: String?,
    highlighted: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    val tokens = LocalGovindaTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (highlighted) tokens.selectedState else tokens.surface)
            .border(
                width = if (highlighted) 1.8.dp else 1.2.dp,
                color = if (highlighted) tokens.primary else tokens.border,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = tokens.primaryText
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.secondaryText
            )
        }

        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (highlighted) tokens.primary else tokens.background)
                    .border(1.dp, tokens.primary.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (highlighted) tokens.onPrimary else tokens.primary
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = tokens.primary,
            modifier = Modifier.size(18.dp)
        )
    }
}
