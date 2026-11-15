package com.example.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.core.designsystem.BlueprintBadge
import com.example.core.designsystem.BlueprintCard
import com.example.lock.InstalledAppItem
import com.example.personalization.avatars.PersonalizationAvatar
import com.example.personalization.model.PersonalizationState
import com.example.personalization.theme.LocalGovindaTokens

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    personalizationState: PersonalizationState,
    onNavigateBack: () -> Unit,
    onOpenPersonalization: () -> Unit,
    onOpenLearnerSurvey: () -> Unit = {},
    onSetDeveloperModeEnabled: (Boolean) -> Unit = {},
    onOpenNotesStudio: () -> Unit = {},
    onOpenDeveloperDashboard: () -> Unit = {},
    onRefreshAppLockEnvironment: () -> Unit,
    onSetQuizLockEnabled: (Boolean) -> Unit,
    onToggleLockedPackage: (String) -> Unit,
    onClearLockedPackages: () -> Unit,
    onDismissStatusMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGovindaTokens.current
    var showAppPickerDialog by remember { mutableStateOf(false) }
    var activePermissionDialog by remember { mutableStateOf<PermissionDialogType?>(null) }
    var activeInfoDialog by remember { mutableStateOf<SettingsInfoDialogType?>(null) }
    val prefs = uiState.preferences
    val lifecycleOwner = LocalLifecycleOwner.current
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(Unit) {
        onRefreshAppLockEnvironment()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRefreshAppLockEnvironment()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.background)
    ) {
        SettingsTopNavBar(onNavigateBack = onNavigateBack)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
                    .testTag("settings_list"),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp + navBarBottom
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!uiState.statusMessage.isNullOrBlank()) {
                    item {
                        BlueprintCard(
                            containerColor = tokens.success.copy(alpha = 0.14f),
                            borderColor = tokens.success
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = uiState.statusMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = tokens.primaryText,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = onDismissStatusMessage) {
                                    Text("OK", color = tokens.primary)
                                }
                            }
                        }
                    }
                }

                // Learner Profile & Question Track Card
                item {
                    BlueprintCard(
                        borderColor = tokens.primary,
                        borderWidth = 1.8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SettingsCardTitle(
                                    icon = Icons.Filled.AutoAwesome,
                                    title = "LEARNER PROFILE"
                                )
                                BlueprintBadge(text = "${prefs.enabledCategories.size} TOPICS")
                            }
                            Text(
                                text = "Active Track: ${prefs.learnerTrackLabel.removeSuffix(" (Adaptive)")}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = tokens.primaryText
                            )
                            OutlinedButton(
                                onClick = onOpenLearnerSurvey,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.5.dp, tokens.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("open_learner_survey_button")
                            ) {
                                Text(
                                    text = "CHANGE LEARNER PROFILE",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = tokens.primary
                                )
                            }
                        }
                    }
                }

                // Personalization Studio Launch Card
                item {
                    BlueprintCard(
                        borderColor = tokens.primary,
                        borderWidth = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SettingsCardTitle(
                                    icon = Icons.Filled.Palette,
                                    title = "PERSONALIZATION STUDIO"
                                )
                                BlueprintBadge(text = tokens.theme.name.uppercase())
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(tokens.surface)
                                    .border(1.4.dp, tokens.border, RoundedCornerShape(6.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PersonalizationAvatar(
                                    avatar = personalizationState.activeProfile.avatar,
                                    size = 42.dp
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = personalizationState.activeProfile.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = tokens.primaryText
                                    )
                                    Text(
                                        text = "${tokens.theme.name} · ${tokens.iconProfile.family.displayName} · ${tokens.componentStyle.keypadStyle.displayName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.secondaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Button(
                                onClick = onOpenPersonalization,
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = tokens.primary,
                                    contentColor = tokens.onPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("open_personalization_button")
                            ) {
                                Text(
                                    text = "OPEN PERSONALIZATION STUDIO",
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

                // App Quiz Lock Selection Card
                item {
                    BlueprintCard(borderColor = tokens.primary, borderWidth = 1.8.dp) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SettingsCardTitle(
                                    icon = Icons.Filled.Lock,
                                    title = "APP QUIZ LOCK"
                                )
                                BlueprintBadge(text = "${prefs.quizLockedPackages.size} APPS")
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Lock Selected Applications",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = tokens.primaryText
                                    )
                                    Text(
                                        text = "Asks 1 question once when a selected app is opened fresh.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.secondaryText
                                    )
                                }
                                Switch(
                                    checked = prefs.quizLockEnabled,
                                    onCheckedChange = { enabled ->
                                        onSetQuizLockEnabled(enabled)
                                        if (enabled && !uiState.isAllLockPermissionsGranted) {
                                            activePermissionDialog = PermissionDialogType.ACCESSIBILITY_SERVICE
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = tokens.onPrimary,
                                        checkedTrackColor = tokens.primary
                                    ),
                                    modifier = Modifier.testTag("toggle_quiz_lock")
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (uiState.isAllLockPermissionsGranted) tokens.success.copy(alpha = 0.14f)
                                        else tokens.warning.copy(alpha = 0.16f)
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        color = if (uiState.isAllLockPermissionsGranted) tokens.success else tokens.warning,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.isAllLockPermissionsGranted) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = if (uiState.isAllLockPermissionsGranted) tokens.success else tokens.warning,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (uiState.isAllLockPermissionsGranted) {
                                            "Quiz Lock Permissions Active"
                                        } else {
                                            "Enable App Lock Permissions"
                                        },
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = tokens.primaryText
                                    )
                                    Text(
                                        text = if (uiState.isAllLockPermissionsGranted) {
                                            "Accessibility + Overlay ready to prompt 1 question on fresh launch."
                                        } else {
                                            val missing = buildList {
                                                if (!uiState.isAccessibilityEnabled) add("Accessibility Service")
                                                if (!uiState.isOverlayEnabled) add("Display Over Other Apps")
                                            }.joinToString(" & ")
                                            "Needed: $missing to intercept locked apps reliably."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.secondaryText
                                    )
                                }
                            }

                            if (!uiState.isAllLockPermissionsGranted) {
                                OutlinedButton(
                                    onClick = {
                                        activePermissionDialog = PermissionDialogType.ACCESSIBILITY_SERVICE
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.5.dp, tokens.border),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = tokens.background,
                                        contentColor = tokens.primary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("open_accessibility_settings_button")
                                ) {
                                    Text(
                                        text = "ALLOW APP LOCK PERMISSIONS",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            val selectedNames = uiState.installedApps
                                .filter { it.packageName in prefs.quizLockedPackages }
                                .map { it.appName }
                            val summaryText = when {
                                prefs.quizLockedPackages.isEmpty() -> "No applications selected yet."
                                selectedNames.isNotEmpty() -> "Locked: ${selectedNames.joinToString(", ")}"
                                else -> "${prefs.quizLockedPackages.size} application(s) locked"
                            }
                            Text(
                                text = summaryText,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = tokens.primaryText,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onRefreshAppLockEnvironment()
                                        showAppPickerDialog = true
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = tokens.primary,
                                        contentColor = tokens.onPrimary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("select_locked_apps_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Apps,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "SELECT APPS (${prefs.quizLockedPackages.size})",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                if (prefs.quizLockedPackages.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = onClearLockedPackages,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.5.dp, tokens.border),
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("clear_locked_apps_button")
                                    ) {
                                        Text(
                                            text = "CLEAR",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = tokens.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Help, FAQ, About & Legal Licenses Cards
                item {
                    HelpAndLegalSettingsCard(
                        onOpenInfoDialog = { activeInfoDialog = it }
                    )
                }
            }
        }
    }

    if (showAppPickerDialog) {
        AppPickerDialog(
            apps = uiState.installedApps,
            isLoading = uiState.isLoadingApps,
            selectedPackages = prefs.quizLockedPackages,
            onTogglePackage = onToggleLockedPackage,
            onDismiss = {
                showAppPickerDialog = false
                if (prefs.quizLockedPackages.isNotEmpty() && !uiState.isAllLockPermissionsGranted) {
                    activePermissionDialog = PermissionDialogType.ACCESSIBILITY_SERVICE
                }
            }
        )
    }

    activePermissionDialog?.let { permissionType ->
        PermissionDisclosureDialog(
            type = permissionType,
            isAccessibilityEnabled = uiState.isAccessibilityEnabled,
            isOverlayEnabled = uiState.isOverlayEnabled,
            onDismiss = { activePermissionDialog = null }
        )
    }

    activeInfoDialog?.let { infoType ->
        SettingsInfoModalDialog(
            type = infoType,
            onDismiss = { activeInfoDialog = null }
        )
    }
}

@Composable
private fun AppPickerDialog(
    apps: List<InstalledAppItem>,
    isLoading: Boolean,
    selectedPackages: Set<String>,
    onTogglePackage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = LocalGovindaTokens.current
    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = remember(apps, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) apps
        else apps.filter {
            it.appName.contains(q, ignoreCase = true) || it.packageName.contains(q, ignoreCase = true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BlueprintCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 620.dp),
            containerColor = tokens.background,
            borderColor = tokens.primary,
            borderWidth = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT LOCKED APPS",
                        style = MaterialTheme.typography.titleLarge,
                        color = tokens.primaryText
                    )
                    BlueprintBadge(text = "${selectedPackages.size} LOCKED")
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search installed apps…") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = tokens.primary
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tokens.primary,
                        unfocusedBorderColor = tokens.border,
                        focusedTextColor = tokens.primaryText,
                        unfocusedTextColor = tokens.primaryText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("app_search_input")
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(min = 180.dp, max = 380.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(tokens.surface)
                        .border(1.5.dp, tokens.border, RoundedCornerShape(6.dp))
                ) {
                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = tokens.primary)
                            }
                        }
                        filteredApps.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No matching launchable apps found on this device.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = tokens.secondaryText
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("installed_apps_list"),
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(
                                    items = filteredApps,
                                    key = { it.packageName },
                                    contentType = { "app_row" }
                                ) { app ->
                                    val isSelected = app.packageName in selectedPackages
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) tokens.selectedState else tokens.background)
                                            .border(
                                                width = 1.5.dp,
                                                color = if (isSelected) tokens.primary else tokens.border,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable { onTogglePackage(app.packageName) }
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                            .testTag("app_row_${app.packageName}"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        if (app.iconBitmap != null) {
                                            Image(
                                                bitmap = app.iconBitmap,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(tokens.selectedState),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Apps,
                                                    contentDescription = null,
                                                    tint = tokens.primary
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = app.appName,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = tokens.primaryText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = app.packageName,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = tokens.secondaryText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { onTogglePackage(app.packageName) },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = tokens.primary,
                                                uncheckedColor = tokens.border
                                            )
                                        )
                                    }
                                }
                            }
                        }
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
                        .testTag("app_picker_done_button")
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
private fun SettingsTopNavBar(
    onNavigateBack: () -> Unit
) {
    val tokens = LocalGovindaTokens.current
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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                onClick = onNavigateBack,
                shape = RoundedCornerShape(6.dp),
                color = tokens.background,
                border = BorderStroke(1.5.dp, tokens.border),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("settings_back_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back_home),
                        tint = tokens.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_title).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = tokens.primaryText,
                    maxLines = 1
                )
                Text(
                    text = stringResource(R.string.settings_subtitle),
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.secondaryText,
                    maxLines = 1
                )
            }
        }
        HorizontalDivider(thickness = 2.dp, color = tokens.border)
    }
}

@Composable
private fun SettingsCardTitle(
    icon: ImageVector,
    title: String
) {
    val tokens = LocalGovindaTokens.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tokens.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = tokens.primaryText
        )
    }
}
