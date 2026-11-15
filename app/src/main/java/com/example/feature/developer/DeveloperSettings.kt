package com.example.feature.developer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val BlueprintPrimary = Color(0xFF003DA5)
private val BlueprintSecondary = Color(0xFF1647BB)
private val BlueprintSurface = Color(0xFFEAF1FF)

@Composable
fun DeveloperModeSettingsCard(
    developerModeEnabled: Boolean,
    onToggleDeveloperMode: (Boolean) -> Unit,
    onOpenNotesStudio: () -> Unit,
    onOpenDeveloperDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("developer_mode_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (developerModeEnabled) BlueprintSurface else Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (developerModeEnabled) BlueprintPrimary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Code,
                        contentDescription = "Developer Mode",
                        tint = BlueprintPrimary
                    )
                    Column {
                        Text(
                            text = "SEPARATE WORKSPACES · ACTIVATED IN SETTINGS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintSecondary
                        )
                        Text(
                            text = "Enable Notes (Notion) & Chat (WhatsApp)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Switch(
                    checked = developerModeEnabled,
                    onCheckedChange = onToggleDeveloperMode,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BlueprintPrimary
                    ),
                    modifier = Modifier.testTag("toggle_developer_mode")
                )
            }

            Text(
                text = if (developerModeEnabled) {
                    "ACTIVE: Quiz, Notion-style Notes, and WhatsApp-style Chat are enabled as three separate workspaces in the bottom navigation bar."
                } else {
                    "QUIZ-ONLY MODE: Notes and Chat are hidden. Turn on this switch to activate the separate Notion-inspired Notes workspace and WhatsApp-inspired Chat messenger."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Workspaces:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (developerModeEnabled) "QUIZ  •  NOTES (NOTION)  •  CHAT (WHATSAPP)" else "QUIZ ONLY (Standard)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintPrimary,
                    modifier = Modifier.testTag("developer_mode_status_badge")
                )
            }

            if (developerModeEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenNotesStudio,
                        colors = ButtonDefaults.buttonColors(containerColor = BlueprintPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_notes_studio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = "Open Notes",
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text("OPEN NOTES (NOTION)", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = onOpenDeveloperDashboard,
                        border = BorderStroke(1.dp, BlueprintPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_developer_dashboard_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = "Open Chat",
                            tint = BlueprintPrimary,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text("OPEN CHAT (WHATSAPP)", color = BlueprintPrimary, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
