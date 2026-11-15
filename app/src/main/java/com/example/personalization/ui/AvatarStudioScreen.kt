package com.example.personalization.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.personalization.avatars.AvatarRegistry
import com.example.personalization.avatars.PersonalizationAvatar
import com.example.personalization.model.AppearanceProfile
import com.example.personalization.model.AvatarBadge
import com.example.personalization.model.AvatarBorderStyle
import com.example.personalization.model.AvatarStyle
import com.example.personalization.theme.LocalGovindaTokens
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AvatarStudioSection(
    draft: AppearanceProfile,
    onUpdateDraft: ((AppearanceProfile) -> AppearanceProfile) -> Unit
) {
    val tokens = LocalGovindaTokens.current
    val avatar = draft.avatar

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Profile Header Preview Card
        StudioSectionCard(
            title = "Avatar & Profile Appearance",
            subtitle = "Procedural offline vector avatars stored locally."
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(tokens.background)
                    .border(1.4.dp, tokens.border, RoundedCornerShape(8.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PersonalizationAvatar(avatar = avatar, size = 68.dp)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = draft.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = tokens.primaryText
                    )
                    Text(
                        text = "${avatar.style.displayName} · Seed #${avatar.seed}",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.secondaryText
                    )
                    Text(
                        text = "Badge: ${avatar.badge.displayName} · Border: ${avatar.borderStyle.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.mutedText
                    )
                }
            }

            Button(
                onClick = {
                    val nextSeed = Random.nextInt(100, 9999)
                    onUpdateDraft {
                        it.copy(avatar = AvatarRegistry.generateFromSeed(nextSeed, it.avatar))
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
                    .testTag("avatar_random_seed_button")
            ) {
                Icon(Icons.Filled.Casino, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("RANDOMIZE PROCEDURAL SEED")
            }

            Text(
                text = "AVATAR PRESETS",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.secondaryText
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AvatarRegistry.presets.forEach { preset ->
                    val selected = preset.style == avatar.style && preset.seed == avatar.seed
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) tokens.selectedState else tokens.background,
                        border = BorderStroke(
                            width = if (selected) 2.dp else 1.2.dp,
                            color = if (selected) tokens.primary else tokens.border
                        ),
                        modifier = Modifier
                            .clickable {
                                onUpdateDraft { it.copy(avatar = preset) }
                            }
                            .padding(2.dp)
                    ) {
                        PersonalizationAvatar(
                            avatar = preset,
                            size = 48.dp,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }
        }

        StudioSectionCard(
            title = "Customize Avatar Traits",
            subtitle = "Style, frame shape, accessory, border, badge & colors."
        ) {
            Text(
                text = "AVATAR STYLE (8)",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.secondaryText
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AvatarStyle.entries.forEach { style ->
                    StudioOptionChip(
                        label = style.displayName,
                        selected = avatar.style == style,
                        onClick = {
                            onUpdateDraft { it.copy(avatar = it.avatar.copy(style = style)) }
                        },
                        modifier = Modifier.testTag("avatar_style_${style.name.lowercase()}")
                    )
                }
            }

            Text(
                text = "FRAME SHAPE",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.secondaryText
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(0 to "Rounded", 1 to "Circle", 2 to "Blueprint Square").forEach { (variant, name) ->
                    StudioOptionChip(
                        label = name,
                        selected = avatar.shapeVariant % 3 == variant,
                        onClick = {
                            onUpdateDraft { it.copy(avatar = it.avatar.copy(shapeVariant = variant)) }
                        }
                    )
                }
            }

            Text(
                text = "BORDER & ACCESSORY",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.secondaryText
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AvatarBorderStyle.entries.forEach { b ->
                    StudioOptionChip(
                        label = b.displayName,
                        selected = avatar.borderStyle == b,
                        onClick = {
                            onUpdateDraft { it.copy(avatar = it.avatar.copy(borderStyle = b)) }
                        }
                    )
                }
                StudioOptionChip(
                    label = if (avatar.accessoryIndex % 2 == 1) "Glasses: On" else "Glasses: Off",
                    selected = avatar.accessoryIndex % 2 == 1,
                    onClick = {
                        onUpdateDraft {
                            it.copy(avatar = it.avatar.copy(accessoryIndex = it.avatar.accessoryIndex + 1))
                        }
                    }
                )
            }

            Text(
                text = "CORNER BADGE",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.secondaryText
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AvatarBadge.entries.forEach { badge ->
                    val label = if (badge == AvatarBadge.NONE) "None" else "${badge.symbol} ${badge.displayName}"
                    StudioOptionChip(
                        label = label,
                        selected = avatar.badge == badge,
                        onClick = {
                            onUpdateDraft { it.copy(avatar = it.avatar.copy(badge = badge)) }
                        }
                    )
                }
            }

            OutlinedTextField(
                value = avatar.monogram,
                onValueChange = { txt ->
                    onUpdateDraft { it.copy(avatar = it.avatar.copy(monogram = txt.take(2))) }
                },
                label = { Text("Monogram Letters (max 2)") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = tokens.primary,
                    unfocusedBorderColor = tokens.border,
                    focusedTextColor = tokens.primaryText,
                    unfocusedTextColor = tokens.primaryText
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "BACKGROUND COLOR",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.secondaryText
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AvatarRegistry.avatarColorSwatches.forEach { argb ->
                    ColorSwatchCircle(
                        colorArgb = argb,
                        selected = avatar.backgroundColorArgb == argb,
                        onClick = {
                            onUpdateDraft { it.copy(avatar = it.avatar.copy(backgroundColorArgb = argb)) }
                        }
                    )
                }
            }

            Text(
                text = "ACCENT COLOR",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.secondaryText
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AvatarRegistry.accentSwatches.forEach { argb ->
                    ColorSwatchCircle(
                        colorArgb = argb,
                        selected = avatar.accentColorArgb == argb,
                        onClick = {
                            onUpdateDraft { it.copy(avatar = it.avatar.copy(accentColorArgb = argb)) }
                        }
                    )
                }
            }
        }
    }
}
