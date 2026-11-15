package com.example.personalization.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.personalization.avatars.PersonalizationAvatar
import com.example.personalization.model.AppearanceProfile
import com.example.personalization.theme.LocalGovindaTokens
import com.example.personalization.theme.ThemeRegistry

@Composable
fun ProfileManagerSection(
    activeProfileId: String,
    profiles: List<AppearanceProfile>,
    onSwitchProfile: (String) -> Unit,
    onCreateProfile: (String) -> Unit,
    onRenameProfile: (String, String) -> Unit,
    onDuplicateProfile: (String) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onResetToDefaults: () -> Unit
) {
    val tokens = LocalGovindaTokens.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<AppearanceProfile?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StudioSectionCard(
            title = "Appearance Profiles (${profiles.size})",
            subtitle = "Switch complete theme, font, icon, avatar & layout setups instantly."
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.primary,
                        contentColor = tokens.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("create_profile_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("NEW PROFILE")
                }

                OutlinedButton(
                    onClick = onResetToDefaults,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.4.dp, tokens.border),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Filled.Restore, contentDescription = "Restore Default", tint = tokens.primary)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                profiles.forEach { prof ->
                    val isActive = prof.id == activeProfileId
                    val themeName = ThemeRegistry.findById(prof.themeId)?.name ?: prof.themeId
                    Surface(
                        onClick = { onSwitchProfile(prof.id) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isActive) tokens.selectedState else tokens.background,
                        border = BorderStroke(
                            width = if (isActive) 2.2.dp else 1.3.dp,
                            color = if (isActive) tokens.primary else tokens.border
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_card_${prof.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PersonalizationAvatar(avatar = prof.avatar, size = 40.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = prof.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isActive) tokens.primary else tokens.primaryText
                                    )
                                    if (isActive) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Active",
                                            tint = tokens.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "$themeName · ${prof.layout.challengeLayout.displayName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.secondaryText
                                )
                            }

                            Row {
                                IconButton(onClick = { onDuplicateProfile(prof.id) }) {
                                    Icon(
                                        Icons.Filled.ContentCopy,
                                        contentDescription = "Duplicate ${prof.name}",
                                        tint = tokens.primary
                                    )
                                }
                                if (!prof.isBuiltIn) {
                                    IconButton(onClick = { renameTarget = prof }) {
                                        Icon(
                                            Icons.Filled.Edit,
                                            contentDescription = "Rename ${prof.name}",
                                            tint = tokens.primary
                                        )
                                    }
                                    IconButton(onClick = { onDeleteProfile(prof.id) }) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Delete ${prof.name}",
                                            tint = tokens.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var nameInput by remember { mutableStateOf("My Setup") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = tokens.background,
            title = { Text("Save Current Setup as Profile", color = tokens.primaryText) },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Profile Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCreateDialog = false
                        onCreateProfile(nameInput)
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }

    renameTarget?.let { target ->
        var newName by remember(target.id) { mutableStateOf(target.name) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            containerColor = tokens.background,
            title = { Text("Rename Profile", color = tokens.primaryText) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        renameTarget = null
                        onRenameProfile(target.id, newName)
                    }
                ) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel") }
            }
        )
    }
}
