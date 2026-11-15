package com.example.personalization.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.personalization.layouts.LayoutManager
import com.example.personalization.layouts.LayoutRegistry
import com.example.personalization.model.AppearanceProfile
import com.example.personalization.model.LayoutProfile
import com.example.personalization.theme.LocalGovindaTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LayoutStudioSection(
    draft: AppearanceProfile,
    onUpdateDraft: ((AppearanceProfile) -> AppearanceProfile) -> Unit
) {
    val tokens = LocalGovindaTokens.current
    val layout = draft.layout

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StudioSectionCard(
            title = "Challenge Screen Layout (6)",
            subtitle = "Tailor equation prominence, chrome, and telemetry visibility."
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LayoutRegistry.challengeLayouts.forEach { preset ->
                    val selected = layout.challengeLayout == preset
                    Surface(
                        onClick = {
                            onUpdateDraft { it.copy(layout = it.layout.copy(challengeLayout = preset)) }
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = if (selected) tokens.selectedState else tokens.background,
                        border = BorderStroke(
                            width = if (selected) 2.dp else 1.2.dp,
                            color = if (selected) tokens.primary else tokens.border
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("challenge_layout_${preset.name.lowercase()}")
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(
                                text = preset.displayName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (selected) tokens.primary else tokens.primaryText
                            )
                            Text(
                                text = preset.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.secondaryText
                            )
                        }
                    }
                }
            }
        }

        StudioSectionCard(
            title = "Top Navigation & Density",
            subtitle = "Adjust header bar framing and overall UI spacing rhythm."
        ) {
            Text(
                text = "NAVIGATION BAR STYLE (4)",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.secondaryText
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LayoutRegistry.navigationLayouts.forEach { nav ->
                    StudioOptionChip(
                        label = nav.displayName,
                        selected = layout.navigationLayout == nav,
                        onClick = {
                            onUpdateDraft { it.copy(layout = it.layout.copy(navigationLayout = nav)) }
                        }
                    )
                }
            }

            Text(
                text = "APPLICATION DENSITY (3)",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.secondaryText
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LayoutRegistry.densityModes.forEach { density ->
                    StudioOptionChip(
                        label = density.displayName,
                        selected = layout.density == density,
                        onClick = {
                            onUpdateDraft { it.copy(layout = it.layout.copy(density = density)) }
                        }
                    )
                }
            }

            Text(
                text = "TELEMETRY PANEL PRESET (8)",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.secondaryText
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LayoutRegistry.dashboardLayouts.forEach { dash ->
                    StudioOptionChip(
                        label = dash.displayName,
                        selected = layout.dashboardLayout == dash,
                        onClick = {
                            onUpdateDraft { it.copy(layout = it.layout.copy(dashboardLayout = dash)) }
                        }
                    )
                }
            }
        }

        StudioSectionCard(
            title = "Widget Customization",
            subtitle = "Reorder, show/hide, or resize telemetry widgets used in Stats-Assisted layout."
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                layout.widgets.forEachIndexed { idx, widget ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(tokens.background)
                            .border(1.2.dp, tokens.border, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = widget.id.displayName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = tokens.primaryText
                            )
                            Text(
                                text = "Size: ${widget.size.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.secondaryText
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                enabled = idx > 0,
                                onClick = {
                                    onUpdateDraft {
                                        it.copy(
                                            layout = it.layout.copy(
                                                widgets = LayoutManager.moveWidget(it.layout.widgets, idx, idx - 1)
                                            )
                                        )
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.ArrowUpward, contentDescription = "Move up", tint = tokens.primary)
                            }
                            IconButton(
                                enabled = idx < layout.widgets.lastIndex,
                                onClick = {
                                    onUpdateDraft {
                                        it.copy(
                                            layout = it.layout.copy(
                                                widgets = LayoutManager.moveWidget(it.layout.widgets, idx, idx + 1)
                                            )
                                        )
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.ArrowDownward, contentDescription = "Move down", tint = tokens.primary)
                            }
                            StudioOptionChip(
                                label = widget.size.displayName,
                                selected = widget.size.name == "WIDE",
                                onClick = {
                                    onUpdateDraft {
                                        it.copy(
                                            layout = it.layout.copy(
                                                widgets = LayoutManager.toggleWidgetSize(it.layout.widgets, widget.id)
                                            )
                                        )
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = widget.visible,
                                onCheckedChange = {
                                    onUpdateDraft {
                                        it.copy(
                                            layout = it.layout.copy(
                                                widgets = LayoutManager.toggleWidgetVisibility(it.layout.widgets, widget.id)
                                            )
                                        )
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = tokens.onPrimary,
                                    checkedTrackColor = tokens.primary
                                )
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    onUpdateDraft { it.copy(layout = LayoutProfile()) }
                },
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.4.dp, tokens.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, tint = tokens.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("RESTORE DEFAULT LAYOUT", color = tokens.primary)
            }
        }
    }
}
