package com.example.personalization.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.personalization.theme.GovindaDynamicTheme
import com.example.personalization.theme.LiveThemePreviewCard
import com.example.personalization.theme.LocalGovindaTokens

@Composable
fun PersonalizationScreen(
    viewModel: PersonalizationViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val draft = uiState.draftProfile
    val customThemes = uiState.savedState.customThemes
    var showSaveProfileDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (uiState.isFullPreviewMode) {
            viewModel.setFullPreviewMode(false)
        } else {
            onNavigateBack()
        }
    }

    // Studio renders live inside the draft profile so users immediately feel every change!
    GovindaDynamicTheme(profile = draft, customThemes = customThemes) {
        val tokens = LocalGovindaTokens.current

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(tokens.background)
        ) {
            // Top Studio Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tokens.surface)
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        onClick = onNavigateBack,
                        shape = RoundedCornerShape(6.dp),
                        color = tokens.background,
                        border = BorderStroke(1.5.dp, tokens.border),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("personalization_back_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Settings",
                                tint = tokens.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PERSONALIZATION STUDIO",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = tokens.primaryText,
                            maxLines = 1
                        )
                        Text(
                            text = "Configure the GOVINDA experience.",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.secondaryText,
                            maxLines = 1
                        )
                    }

                    Surface(
                        onClick = { viewModel.toggleLivePreview() },
                        shape = RoundedCornerShape(6.dp),
                        color = if (uiState.showLivePreview) tokens.selectedState else tokens.background,
                        border = BorderStroke(1.5.dp, tokens.border),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("toggle_preview_mode_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (uiState.showLivePreview) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle Live Preview",
                                tint = tokens.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // 11-Section Studio Navigation Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PersonalizationSection.entries.forEach { section ->
                        StudioOptionChip(
                            label = section.title,
                            selected = uiState.selectedSection == section,
                            onClick = { viewModel.selectSection(section) },
                            modifier = Modifier.testTag("studio_tab_${section.name.lowercase()}")
                        )
                    }
                }
                HorizontalDivider(thickness = 2.dp, color = tokens.border)
            }

            // Scrollable Preview + Active Studio Section
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 760.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.statusBanner?.let { banner ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = tokens.success.copy(alpha = 0.14f),
                            border = BorderStroke(1.4.dp, tokens.success),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = banner,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = tokens.primaryText,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = viewModel::clearStatusBanner) {
                                    Text("OK", color = tokens.primary)
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = uiState.showLivePreview) {
                        LiveThemePreviewCard(
                            profile = draft,
                            customThemes = customThemes
                        )
                    }

                    when (uiState.selectedSection) {
                        PersonalizationSection.THEMES -> ThemeGallerySection(
                            draft = draft,
                            customThemes = customThemes,
                            onUpdateDraft = viewModel::updateDraft
                        )
                        PersonalizationSection.COLORS -> ColorStudioSection(
                            draft = draft,
                            customThemes = customThemes,
                            onUpdateDraft = viewModel::updateDraft,
                            onSaveCustomTheme = viewModel::saveCurrentAsCustomTheme,
                            onRenameCustomTheme = viewModel::renameCustomTheme,
                            onDeleteCustomTheme = viewModel::deleteCustomTheme
                        )
                        PersonalizationSection.TYPOGRAPHY -> TypographyStudioSection(
                            draft = draft,
                            onUpdateDraft = viewModel::updateDraft
                        )
                        PersonalizationSection.ICONS -> IconGallerySection(
                            draft = draft,
                            onUpdateDraft = viewModel::updateDraft
                        )
                        PersonalizationSection.AVATARS -> AvatarStudioSection(
                            draft = draft,
                            onUpdateDraft = viewModel::updateDraft
                        )
                        PersonalizationSection.LAYOUTS -> LayoutStudioSection(
                            draft = draft,
                            onUpdateDraft = viewModel::updateDraft
                        )
                        PersonalizationSection.COMPONENTS -> ComponentsStudioSection(
                            draft = draft,
                            onUpdateDraft = viewModel::updateDraft
                        )
                        PersonalizationSection.BACKGROUNDS -> BackgroundsStudioSection(
                            draft = draft,
                            onUpdateDraft = viewModel::updateDraft
                        )
                        PersonalizationSection.ANIMATIONS -> AnimationsAccessibilitySection(
                            draft = draft,
                            onUpdateDraft = viewModel::updateDraft
                        )
                        PersonalizationSection.SOUND_HAPTICS -> SoundHapticsStudioSection(
                            draft = draft,
                            onUpdateDraft = viewModel::updateDraft
                        )
                        PersonalizationSection.PROFILES -> ProfileManagerSection(
                            activeProfileId = uiState.savedState.activeProfile.id,
                            profiles = uiState.savedState.savedProfiles,
                            onSwitchProfile = viewModel::switchProfile,
                            onCreateProfile = viewModel::saveDraftAsNewProfile,
                            onRenameProfile = viewModel::renameProfile,
                            onDuplicateProfile = viewModel::duplicateProfile,
                            onDeleteProfile = viewModel::deleteProfile,
                            onResetToDefaults = viewModel::resetAllToDefaults
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Bottom Action Controls: Reset | Save Profile | Cancel | Apply
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tokens.surface)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                HorizontalDivider(thickness = 2.dp, color = tokens.border)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = viewModel::resetAllToDefaults,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.4.dp, tokens.border),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("studio_reset_button")
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reset", tint = tokens.primary, modifier = Modifier.size(18.dp))
                    }

                    OutlinedButton(
                        onClick = { showSaveProfileDialog = true },
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.4.dp, tokens.border),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("studio_save_profile_button")
                    ) {
                        Icon(Icons.Filled.BookmarkAdd, contentDescription = "Save as Profile", tint = tokens.primary, modifier = Modifier.size(18.dp))
                    }

                    if (uiState.hasUnsavedChanges) {
                        OutlinedButton(
                            onClick = viewModel::cancelChanges,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.4.dp, tokens.border),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("studio_cancel_button")
                        ) {
                            Text("CANCEL", color = tokens.secondaryText, maxLines = 1)
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.applyChanges()
                            onNavigateBack()
                        },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.primary,
                            contentColor = tokens.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("studio_apply_button")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.hasUnsavedChanges) "APPLY" else "APPLIED",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        if (showSaveProfileDialog) {
            var nameInput by remember { mutableStateOf("${draft.name} Custom") }
            AlertDialog(
                onDismissRequest = { showSaveProfileDialog = false },
                containerColor = tokens.background,
                title = { Text("Save as Profile", color = tokens.primaryText) },
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
                            showSaveProfileDialog = false
                            viewModel.saveDraftAsNewProfile(nameInput)
                        }
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveProfileDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
