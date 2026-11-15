package com.example.personalization.ui

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.personalization.model.AppearanceProfile
import com.example.personalization.model.BackgroundPattern
import com.example.personalization.model.ButtonStylePreset
import com.example.personalization.model.CardStylePreset
import com.example.personalization.model.ElevationLevel
import com.example.personalization.model.InputStylePreset
import com.example.personalization.model.KeypadStylePreset
import com.example.personalization.model.LauncherIconStyle
import com.example.personalization.model.MotionLevel
import com.example.personalization.theme.LocalGovindaTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComponentsStudioSection(
    draft: AppearanceProfile,
    onUpdateDraft: ((AppearanceProfile) -> AppearanceProfile) -> Unit
) {
    val tokens = LocalGovindaTokens.current
    val comp = draft.components

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StudioSectionCard(
            title = "Card Styles (6)",
            subtitle = "Customize panel geometry, outline weight & corners."
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CardStylePreset.entries.forEach { preset ->
                    StudioOptionChip(
                        label = preset.displayName,
                        selected = comp.cardStyle == preset,
                        onClick = {
                            onUpdateDraft { it.copy(components = it.components.copy(cardStyle = preset)) }
                        },
                        modifier = Modifier.testTag("card_style_${preset.name.lowercase()}")
                    )
                }
            }
        }

        StudioSectionCard(
            title = "Button Styles (6)",
            subtitle = "Customize action buttons like Submit and Settings controls."
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ButtonStylePreset.entries.forEach { preset ->
                    StudioOptionChip(
                        label = preset.displayName,
                        selected = comp.buttonStyle == preset,
                        onClick = {
                            onUpdateDraft { it.copy(components = it.components.copy(buttonStyle = preset)) }
                        }
                    )
                }
            }
        }

        StudioSectionCard(
            title = "Answer Input Styles (5)",
            subtitle = "Configure the keypad answer readout frame."
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InputStylePreset.entries.forEach { preset ->
                    StudioOptionChip(
                        label = preset.displayName,
                        selected = comp.inputStyle == preset,
                        onClick = {
                            onUpdateDraft { it.copy(components = it.components.copy(inputStyle = preset)) }
                        }
                    )
                }
            }
        }

        StudioSectionCard(
            title = "Mathematical Keypad Style (5)",
            subtitle = "Choose key proportions, bevels, and borders (Keypad-only input)."
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                KeypadStylePreset.entries.forEach { preset ->
                    StudioOptionChip(
                        label = preset.displayName,
                        selected = comp.keypadStyle == preset,
                        onClick = {
                            onUpdateDraft { it.copy(components = it.components.copy(keypadStyle = preset)) }
                        },
                        modifier = Modifier.testTag("keypad_style_${preset.name.lowercase()}")
                    )
                }
            }
            Text(
                text = "Current: ${comp.keypadStyle.displayName} · Corners ${comp.cardStyle.cornerDp}dp",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.secondaryText
            )
        }
    }
}

@Composable
fun BackgroundsStudioSection(
    draft: AppearanceProfile,
    onUpdateDraft: ((AppearanceProfile) -> AppearanceProfile) -> Unit
) {
    val tokens = LocalGovindaTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StudioSectionCard(
            title = "Background Canvas Patterns (8)",
            subtitle = "Rendered locally on Canvas with zero image assets."
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BackgroundPattern.entries.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { pattern ->
                            val selected = draft.background == pattern
                            Surface(
                                onClick = {
                                    onUpdateDraft { it.copy(background = pattern) }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = tokens.background,
                                border = BorderStroke(
                                    width = if (selected) 2.2.dp else 1.3.dp,
                                    color = if (selected) tokens.primary else tokens.border
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("bg_pattern_${pattern.name.lowercase()}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .border(1.dp, tokens.divider, RoundedCornerShape(4.dp))
                                    ) {
                                        PatternPreviewCanvas(
                                            pattern = pattern,
                                            lineColor = tokens.border,
                                            tintColor = tokens.selectedState
                                        )
                                    }
                                    Text(
                                        text = pattern.displayName,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (selected) tokens.primary else tokens.primaryText,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        if (pair.size == 1) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PatternPreviewCanvas(
    pattern: BackgroundPattern,
    lineColor: Color,
    tintColor: Color
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (pattern == BackgroundPattern.LIGHT_BLUE || pattern == BackgroundPattern.WARM_PAPER) {
            drawRect(tintColor.copy(alpha = 0.55f))
        }
        val step = 12.dp.toPx()
        when (pattern) {
            BackgroundPattern.BLUEPRINT_GRID, BackgroundPattern.SUBTLE_TECH_GRID -> {
                val alpha = if (pattern == BackgroundPattern.BLUEPRINT_GRID) 0.7f else 0.35f
                var x = step
                while (x < size.width) {
                    drawLine(lineColor.copy(alpha = alpha), Offset(x, 0f), Offset(x, size.height), 1f)
                    x += step
                }
                var y = step
                while (y < size.height) {
                    drawLine(lineColor.copy(alpha = alpha), Offset(0f, y), Offset(size.width, y), 1f)
                    y += step
                }
            }
            BackgroundPattern.DOTTED_PATTERN -> {
                var x = step
                while (x < size.width) {
                    var y = step
                    while (y < size.height) {
                        drawCircle(lineColor.copy(alpha = 0.75f), 1.6.dp.toPx(), Offset(x, y))
                        y += step
                    }
                    x += step
                }
            }
            BackgroundPattern.GEOMETRIC_PATTERN -> {
                var x = 0f
                while (x < size.width + step) {
                    drawLine(lineColor.copy(alpha = 0.45f), Offset(x, 0f), Offset(x - size.height * 0.5f, size.height), 1.2f)
                    x += step * 1.4f
                }
            }
            else -> Unit
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimationsAccessibilitySection(
    draft: AppearanceProfile,
    onUpdateDraft: ((AppearanceProfile) -> AppearanceProfile) -> Unit
) {
    val tokens = LocalGovindaTokens.current
    val effects = draft.effects

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StudioSectionCard(
            title = "Elevation & Motion Effects",
            subtitle = "Control shadows, transitions, and accessibility ergonomics."
        ) {
            Text(
                text = "ELEVATION LEVEL",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.secondaryText
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ElevationLevel.entries.forEach { lvl ->
                    StudioOptionChip(
                        label = lvl.displayName,
                        selected = effects.elevationLevel == lvl,
                        onClick = {
                            onUpdateDraft { it.copy(effects = it.effects.copy(elevationLevel = lvl)) }
                        }
                    )
                }
            }

            Text(
                text = "ANIMATION MOTION LEVEL",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.secondaryText
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MotionLevel.entries.forEach { motion ->
                    StudioOptionChip(
                        label = motion.displayName,
                        selected = effects.motionLevel == motion,
                        onClick = {
                            onUpdateDraft { it.copy(effects = it.effects.copy(motionLevel = motion)) }
                        }
                    )
                }
            }

            StudioToggleRow(
                title = "High-Contrast Mode",
                subtitle = "Boost text, surface & border contrast above AAA thresholds",
                checked = effects.highContrastMode,
                onCheckedChange = { checked ->
                    onUpdateDraft { it.copy(effects = it.effects.copy(highContrastMode = checked)) }
                }
            )

            StudioToggleRow(
                title = "Extra-Large Touch Targets",
                subtitle = "Increase keypad key & navigation button height for accessibility",
                checked = effects.extraLargeTargets,
                onCheckedChange = { checked ->
                    onUpdateDraft { it.copy(effects = it.effects.copy(extraLargeTargets = checked)) }
                }
            )
        }

        StudioSectionCard(
            title = "GOVINDA App Emblem Style (4)",
            subtitle = "Customize the geometric G-emblem across the top bar and headers."
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LauncherIconStyle.entries.forEach { style ->
                    StudioOptionChip(
                        label = style.displayName,
                        selected = draft.launcherIconStyle == style,
                        onClick = {
                            onUpdateDraft { it.copy(launcherIconStyle = style) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SoundHapticsStudioSection(
    draft: AppearanceProfile,
    onUpdateDraft: ((AppearanceProfile) -> AppearanceProfile) -> Unit
) {
    val tokens = LocalGovindaTokens.current
    val haptics = LocalHapticFeedback.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StudioSectionCard(
            title = "Tactile & Audio Feedback",
            subtitle = "Configure keypad click haptics and subtle offline tone feedback."
        ) {
            StudioToggleRow(
                title = "Haptic Keypad Feedback",
                subtitle = "Subtle tactile click on digit entry and submit",
                checked = draft.effects.hapticsEnabled,
                onCheckedChange = { enabled ->
                    onUpdateDraft { it.copy(effects = it.effects.copy(hapticsEnabled = enabled)) }
                }
            )

            StudioToggleRow(
                title = "Sound Click & Verification Tones",
                subtitle = "Lightweight local tone cues on keypad tap and answer check",
                checked = draft.effects.soundEnabled,
                onCheckedChange = { enabled ->
                    onUpdateDraft { it.copy(effects = it.effects.copy(soundEnabled = enabled)) }
                }
            )

            Button(
                onClick = {
                    if (draft.effects.hapticsEnabled) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    if (draft.effects.soundEnabled) {
                        runCatching {
                            val tg = ToneGenerator(AudioManager.STREAM_SYSTEM, 35)
                            tg.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
                        }
                    }
                },
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.primary,
                    contentColor = tokens.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Filled.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("TEST FEEDBACK NOW")
            }
        }
    }
}
