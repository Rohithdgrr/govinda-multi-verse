package com.example.personalization.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalization.model.AppearanceProfile
import com.example.personalization.model.IconGlyph
import com.example.personalization.model.ThemeDefinition

@Composable
fun LiveThemePreviewCard(
    profile: AppearanceProfile,
    customThemes: List<ThemeDefinition> = emptyList(),
    modifier: Modifier = Modifier
) {
    GovindaDynamicTheme(profile = profile, customThemes = customThemes) {
        val tokens = LocalGovindaTokens.current
        val corner = tokens.componentStyle.cardStyle.cornerDp.dp

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .testTag("live_theme_preview"),
            shape = RoundedCornerShape(corner.coerceAtLeast(4.dp)),
            color = tokens.background,
            border = BorderStroke(1.8.dp, tokens.border)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Mini Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tokens.surface)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.example.personalization.avatars.PersonalizationAvatar(
                            avatar = tokens.avatarProfile,
                            size = 24.dp
                        )
                        Text(
                            text = "GOVINDA · ${tokens.theme.name.uppercase()}",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                            color = tokens.primaryText
                        )
                    }
                    com.example.personalization.icons.PersonalizationIcon(
                        glyph = IconGlyph.SETTINGS,
                        iconProfile = tokens.iconProfile.copy(sizeDp = 18f),
                        tint = tokens.primary
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(tokens.divider)
                )

                // Mini Question Card + Keypad + Stats + Button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .clip(RoundedCornerShape(corner))
                                .background(tokens.surface)
                                .border(1.5.dp, tokens.primary, RoundedCornerShape(corner))
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "18 × 7",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontSize = 22.sp,
                                    lineHeight = 26.sp
                                ),
                                color = tokens.primaryText
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(tokens.elevatedSurface)
                                    .border(1.dp, tokens.border, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ACCURACY 92%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = tokens.success
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(tokens.selectedState)
                                    .border(1.dp, tokens.border, RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.75f)
                                        .height(8.dp)
                                        .background(tokens.primary)
                                )
                            }
                        }
                    }

                    // Mini Keypad Row + Submit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("7", "8", "9").forEach { digit ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(tokens.surface)
                                    .border(1.2.dp, tokens.border, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = digit,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = tokens.primaryText
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1.4f)
                                .height(30.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(tokens.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SUBMIT",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = tokens.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
