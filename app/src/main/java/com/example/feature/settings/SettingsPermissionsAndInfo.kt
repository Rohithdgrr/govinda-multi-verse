package com.example.feature.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.core.designsystem.BlueprintBadge
import com.example.core.designsystem.BlueprintCard
import com.example.lock.GovindaQuizLockService
import com.example.personalization.theme.LocalGovindaTokens

enum class PermissionDialogType {
    ACCESSIBILITY_SERVICE,
    SCOPED_APP_QUERY,
    RUNTIME_PERMISSIONS
}

enum class SettingsInfoDialogType {
    HELP,
    FAQ,
    ABOUT,
    PRIVACY_POLICY,
    TERMS_AND_CONDITIONS,
    OPEN_SOURCE_LICENSES
}

@Composable
fun MandatoryPermissionsDialog(
    isAccessibilityEnabled: Boolean,
    isOverlayEnabled: Boolean,
    onAllowAll: () -> Unit
) {
    val tokens = LocalGovindaTokens.current
    val context = LocalContext.current

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
                .padding(16.dp)
                .testTag("mandatory_permission_dialog"),
            containerColor = tokens.background,
            borderColor = tokens.primary,
            borderWidth = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "ENABLE PERMISSIONS",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = tokens.primaryText
                        )
                    }
                    BlueprintBadge(text = "REQUIRED")
                }

                Text(
                    text = "GOVINDA needs these permissions so App Quiz Lock can detect fresh opens of locked apps and launch 1 math challenge immediately. Everything runs 100% offline.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.primaryText
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(tokens.surface)
                        .border(1.4.dp, tokens.border, RoundedCornerShape(6.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MandatoryPermissionItemRow(
                        icon = Icons.Filled.Security,
                        title = "App Launch Monitor (Accessibility)",
                        subtitle = "Detects when a locked app opens fresh (never reads screen text).",
                        badge = if (isAccessibilityEnabled) "GRANTED" else "ALLOW"
                    )
                    HorizontalDivider(color = tokens.divider)
                    MandatoryPermissionItemRow(
                        icon = Icons.Filled.AdminPanelSettings,
                        title = "Display Over Other Apps",
                        subtitle = "Allows the math quiz lock screen to appear over locked apps immediately.",
                        badge = if (isOverlayEnabled) "GRANTED" else "ALLOW"
                    )
                    HorizontalDivider(color = tokens.divider)
                    MandatoryPermissionItemRow(
                        icon = Icons.Filled.Apps,
                        title = "Scoped App Visibility",
                        subtitle = "Lists launchable apps locally so you can pick which apps to lock.",
                        badge = "LOCAL"
                    )
                }

                Button(
                    onClick = {
                        onAllowAll()
                        GovindaQuizLockService.launchAllMissingPermissions(context)
                    },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.primary,
                        contentColor = tokens.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("accept_all_permissions_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ALLOW ALL PERMISSIONS",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun MandatoryPermissionItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String
) {
    val tokens = LocalGovindaTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tokens.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
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
        BlueprintBadge(text = badge)
    }
}

@Composable
fun HelpAndLegalSettingsCard(
    onOpenInfoDialog: (SettingsInfoDialogType) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGovindaTokens.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BlueprintCard(
            modifier = Modifier.testTag("help_support_card"),
            borderColor = tokens.border
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = null,
                        tint = tokens.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "HELP & ABOUT",
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.primaryText
                    )
                }

                SettingsActionRow(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = "Help Guide",
                    subtitle = "How quiz practice, keypad input & quiz lock work",
                    onClick = { onOpenInfoDialog(SettingsInfoDialogType.HELP) },
                    modifier = Modifier.testTag("help_guide_row")
                )

                SettingsActionRow(
                    icon = Icons.Filled.QuestionAnswer,
                    title = "Frequently Asked Questions",
                    subtitle = "Common questions on scoring, styles & app lock",
                    onClick = { onOpenInfoDialog(SettingsInfoDialogType.FAQ) },
                    modifier = Modifier.testTag("faq_row")
                )

                SettingsActionRow(
                    icon = Icons.Filled.Info,
                    title = "About GOVINDA",
                    subtitle = "v${BuildConfig.VERSION_NAME} · Offline Math & Study Studio",
                    onClick = { onOpenInfoDialog(SettingsInfoDialogType.ABOUT) },
                    modifier = Modifier.testTag("about_row")
                )
            }
        }

        BlueprintCard(
            modifier = Modifier.testTag("legal_licenses_card"),
            borderColor = tokens.border
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Gavel,
                        contentDescription = null,
                        tint = tokens.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "LEGAL & LICENSES",
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.primaryText
                    )
                }

                SettingsActionRow(
                    icon = Icons.Filled.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "100% offline processing · Zero data collection",
                    onClick = { onOpenInfoDialog(SettingsInfoDialogType.PRIVACY_POLICY) },
                    modifier = Modifier.testTag("privacy_policy_row")
                )

                SettingsActionRow(
                    icon = Icons.Filled.Description,
                    title = "Terms & Conditions",
                    subtitle = "Educational offline usage & MIT license terms",
                    onClick = { onOpenInfoDialog(SettingsInfoDialogType.TERMS_AND_CONDITIONS) },
                    modifier = Modifier.testTag("terms_conditions_row")
                )

                SettingsActionRow(
                    icon = Icons.Filled.Gavel,
                    title = "Open-Source Licenses",
                    subtitle = "AndroidX, Compose, Room, DataStore & bundled fonts",
                    onClick = { onOpenInfoDialog(SettingsInfoDialogType.OPEN_SOURCE_LICENSES) },
                    modifier = Modifier.testTag("open_source_licenses_row")
                )
            }
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null
) {
    val tokens = LocalGovindaTokens.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(tokens.surface)
            .border(1.4.dp, tokens.border, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tokens.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
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
            BlueprintBadge(text = badge)
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = tokens.secondaryText,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun PermissionDisclosureDialog(
    type: PermissionDialogType,
    isAccessibilityEnabled: Boolean,
    isOverlayEnabled: Boolean = false,
    onDismiss: () -> Unit
) {
    val tokens = LocalGovindaTokens.current
    val context = LocalContext.current

    val title: String
    val statusBadge: String
    val summary: String
    val guarantees: List<String>
    val primaryActionLabel: String
    val systemIntent: Intent?

    when (type) {
        PermissionDialogType.ACCESSIBILITY_SERVICE -> {
            val allGranted = isAccessibilityEnabled && isOverlayEnabled
            title = "App Lock Permissions Required"
            statusBadge = if (allGranted) "ALL GRANTED" else "REQUIRED"
            summary = "GOVINDA App Quiz Lock needs Accessibility & Display Over Other Apps permissions to detect fresh opens of locked apps and prompt 1 math question before opening them."
            guarantees = listOf(
                "Accessibility Monitor (${if (isAccessibilityEnabled) "Granted" else "Needed"}): detects package transitions only; canRetrieveWindowContent = false.",
                "Display Over Other Apps (${if (isOverlayEnabled) "Granted" else "Needed"}): lets the quiz lock appear reliably above locked apps.",
                "Never reads screen text, passwords, notifications, or keystrokes."
            )
            primaryActionLabel = "ALLOW ALL PERMISSIONS"
            systemIntent = null
        }
        PermissionDialogType.SCOPED_APP_QUERY -> {
            title = "Scoped App Discovery"
            statusBadge = "MANIFEST <QUERIES>"
            summary = "To let you pick which applications are protected by Quiz Lock, GOVINDA queries Android's launcher intent filter (ACTION_MAIN + CATEGORY_LAUNCHER)."
            guarantees = listOf(
                "Does NOT request the restricted QUERY_ALL_PACKAGES permission.",
                "Only inspects launchable app names and icons locally on your device.",
                "No app list is ever stored remotely or transmitted anywhere."
            )
            primaryActionLabel = "ALLOW APP SETTINGS"
            systemIntent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        PermissionDialogType.RUNTIME_PERMISSIONS -> {
            title = "Runtime & Network Permissions"
            statusBadge = "0 DANGEROUS"
            summary = "GOVINDA is engineered as a 100% offline application and requests zero dangerous runtime permissions."
            guarantees = listOf(
                "No INTERNET permission: offline by manifest design.",
                "No Camera, Microphone, Location, Contacts, or Storage permissions.",
                "All profiles, attempts, and personalization data stay in local Room & DataStore."
            )
            primaryActionLabel = "CONTINUE"
            systemIntent = null
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
                .padding(16.dp)
                .testTag("permission_dialog"),
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
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = tokens.primaryText,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BlueprintBadge(text = statusBadge)
                }

                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.primaryText
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(tokens.surface)
                        .border(1.4.dp, tokens.border, RoundedCornerShape(6.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    guarantees.forEach { bullet ->
                        Text(
                            text = "• $bullet",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.secondaryText
                        )
                    }
                }

                Button(
                    onClick = {
                        if (systemIntent == null) {
                            GovindaQuizLockService.launchAllMissingPermissions(context)
                        } else {
                            runCatching { context.startActivity(systemIntent) }
                        }
                        onDismiss()
                    },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.primary,
                        contentColor = tokens.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("permission_dialog_open_settings")
                ) {
                    Icon(
                        imageVector = Icons.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = primaryActionLabel,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsInfoModalDialog(
    type: SettingsInfoDialogType,
    onDismiss: () -> Unit
) {
    val tokens = LocalGovindaTokens.current
    val title = when (type) {
        SettingsInfoDialogType.HELP -> "HELP GUIDE"
        SettingsInfoDialogType.FAQ -> "FREQUENTLY ASKED QUESTIONS"
        SettingsInfoDialogType.ABOUT -> "ABOUT GOVINDA"
        SettingsInfoDialogType.PRIVACY_POLICY -> "PRIVACY POLICY"
        SettingsInfoDialogType.TERMS_AND_CONDITIONS -> "TERMS & CONDITIONS"
        SettingsInfoDialogType.OPEN_SOURCE_LICENSES -> "OPEN-SOURCE LICENSES"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BlueprintCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 620.dp)
                .testTag("settings_info_dialog"),
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = tokens.primaryText
                )
                HorizontalDivider(thickness = 1.5.dp, color = tokens.border)

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (type) {
                        SettingsInfoDialogType.HELP -> HelpContent()
                        SettingsInfoDialogType.FAQ -> FaqContent()
                        SettingsInfoDialogType.ABOUT -> AboutContent()
                        SettingsInfoDialogType.PRIVACY_POLICY -> PrivacyPolicyContent()
                        SettingsInfoDialogType.TERMS_AND_CONDITIONS -> TermsContent()
                        SettingsInfoDialogType.OPEN_SOURCE_LICENSES -> LicensesContent()
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.primary,
                        contentColor = tokens.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("settings_info_dialog_close")
                ) {
                    Text(
                        text = "DONE",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun HelpContent() {
    InfoSectionCard(
        heading = "1. Answering with Keypad & Science/GK MCQ",
        body = "Use the tactile keypad (0–9, '/', '.', '±') for math and engineering calculations. For School Science and General Knowledge tracks, tap one of the 4 multiple-choice options directly."
    )
    InfoSectionCard(
        heading = "2. 20-Second Retry Cooldown & Learner Profile",
        body = "Wrong answers trigger a 20-second retry cooldown while keeping the same question visible and hiding the solution until you solve it accurately. You can switch learner tracks anytime in Settings → Learner Profile."
    )
    InfoSectionCard(
        heading = "3. Developer Mode & Notes Studio",
        body = "Enable Developer Mode in Settings → Appearance / Application Modes to reveal Notes Studio and the Developer Workbench. Create 26-block technical notes with offline LaTeX equations, editable tables, 16-language code blocks, / Slash Commands, Flashcards, Knowledge Graph, and PDF/Markdown/JSON/ZIP export."
    )
    InfoSectionCard(
        heading = "4. App Quiz Lock & Personalization Studio",
        body = "Toggle App Quiz Lock ON to require 1 question on fresh launch of selected apps. Open Personalization Studio to customize 26 themes, 20 bundled fonts, icon styles, and layouts."
    )
}

@Composable
private fun FaqContent() {
    val items = listOf(
        "Does GOVINDA require an internet connection?" to
            "No. GOVINDA has zero INTERNET permission. Every problem, dataset question, LaTeX equation, note, and theme runs 100% locally on your device.",
        "How do I open Notes Studio and Developer Tools?" to
            "In Settings, toggle ON 'Developer Mode & Notes Studio'. The bottom navigation bar (Home • Notes • Developer • Modes) appears immediately without restarting. Turning Developer Mode off hides those tabs without deleting your saved notes.",
        "What happens when I submit a wrong answer?" to
            "GOVINDA hides the solution and starts a 20-second retry cooldown while keeping the question visible so you can work through the problem and try again until solved.",
        "Why does Quiz Lock not ask again while I am inside an app?" to
            "Quiz Lock prompts 1 question only when a locked app is opened fresh. It resets after you return to the home launcher or turn off the screen.",
        "How can I export my notes or back up my workspace?" to
            "Inside Notes Studio, tap the Export & Import icon in the top bar to export to Markdown, JSON, PDF, CSV, HTML, TXT, or a full workspace ZIP archive."
    )
    items.forEachIndexed { idx, (q, a) ->
        var expanded by remember { mutableStateOf(idx == 0) }
        val tokens = LocalGovindaTokens.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(tokens.surface)
                .border(1.4.dp, tokens.border, RoundedCornerShape(6.dp))
                .clickable { expanded = !expanded }
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = q,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = tokens.primaryText,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = tokens.primary
                )
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = a,
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.secondaryText
                )
            }
        }
    }
}

@Composable
private fun AboutContent() {
    InfoSectionCard(
        heading = "GOVINDA (MATHGATE) Offline Engine",
        body = "Version ${BuildConfig.VERSION_NAME} (Code ${BuildConfig.VERSION_CODE})\nPackage: ${BuildConfig.APPLICATION_ID}\nLicense: MIT Open Source License"
    )
    InfoSectionCard(
        heading = "Engine & Studio Scope",
        body = "• 30 Math, Science, GK & Engineering Categories (ECE, EEE, CSE, Mech, Civil)\n• 148 Verified Dataset Questions (PACK_CORE_PERMISSIVE_V1)\n• Notion-Inspired Notes Studio (26 Block Types, Offline LaTeX, Tables, Code, Flashcards, Knowledge Graph)\n• 26 Offline Themes & 20 Bundled Font Families"
    )
}

@Composable
private fun PrivacyPolicyContent() {
    InfoSectionCard(
        heading = "Zero Collection & Zero Telemetry",
        body = "GOVINDA does not collect, store remotely, share, or transmit any personal data, usage analytics, crash reports, or device identifiers. The app declares no INTERNET permission."
    )
    InfoSectionCard(
        heading = "On-Device Storage Only",
        body = "Skill profiles, attempt history, quiz preferences, locked package preferences, and appearance themes are stored strictly in your device's private Room database and DataStore files."
    )
    InfoSectionCard(
        heading = "Accessibility Service Scope",
        body = "When voluntarily enabled for App Quiz Lock, GovindaQuizLockService only observes foreground window package transitions (canRetrieveWindowContent = false). It never inspects text, passwords, or inputs inside other apps."
    )
}

@Composable
private fun TermsContent() {
    InfoSectionCard(
        heading = "1. Educational & Personal Use",
        body = "GOVINDA is provided as an offline mathematical practice and focus utility under the MIT License without warranty of any kind."
    )
    InfoSectionCard(
        heading = "2. App Quiz Lock Responsibility",
        body = "You control which applications are gated by Quiz Lock and may disable or clear locked apps at any time in Settings or via Android Accessibility Settings."
    )
    InfoSectionCard(
        heading = "3. Local Data Custody",
        body = "Because GOVINDA stores no data in the cloud, uninstalling the app or tapping Reset Learning Profile permanently deletes local practice records unless exported to JSON first."
    )
}

@Composable
private fun LicensesContent() {
    val libs = listOf(
        "Kotlin & Kotlin Coroutines" to "Apache License 2.0 — JetBrains",
        "AndroidX Core, Lifecycle, Activity & Navigation Compose" to "Apache License 2.0 — The Android Open Source Project",
        "Jetpack Compose UI & Material 3" to "Apache License 2.0 — The Android Open Source Project",
        "AndroidX Room & Preferences DataStore" to "Apache License 2.0 — The Android Open Source Project",
        "Bundled Offline Typography (Inter, Roboto, Fira Code, JetBrains Mono, Oswald, Space Grotesk, etc.)" to "SIL Open Font License 1.1 & Apache License 2.0",
        "JUnit 4" to "Eclipse Public License 1.0",
        "Robolectric" to "MIT License"
    )
    libs.forEach { (name, license) ->
        InfoSectionCard(heading = name, body = license)
    }
}

@Composable
private fun InfoSectionCard(heading: String, body: String) {
    val tokens = LocalGovindaTokens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(tokens.surface)
            .border(1.4.dp, tokens.border, RoundedCornerShape(6.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = heading,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = tokens.primaryText
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = tokens.secondaryText
        )
    }
}
