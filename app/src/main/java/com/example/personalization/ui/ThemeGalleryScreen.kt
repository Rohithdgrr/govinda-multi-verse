package com.example.personalization.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.personalization.model.AppearanceProfile
import com.example.personalization.model.ThemeDefinition
import com.example.personalization.model.ThemeSwitchMode
import com.example.personalization.theme.LocalGovindaTokens
import com.example.personalization.theme.ThemeRegistry

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeGallerySection(
    draft: AppearanceProfile,
    customThemes: List<ThemeDefinition>,
    onUpdateDraft: ((AppearanceProfile) -> AppearanceProfile) -> Unit
) {
    val tokens = LocalGovindaTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StudioSectionCard(
            title = "Theme Switching Mode",
            subtitle = "Choose fixed, system-linked, day/night, or scheduled switching."
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ThemeSwitchMode.entries.forEach { mode ->
                    StudioOptionChip(
                        label = mode.label,
                        selected = draft.themeSwitchMode == mode,
                        onClick = {
                            onUpdateDraft {
                                val autoThemeId = when (mode) {
                                    ThemeSwitchMode.DARK -> {
                                        if (ThemeRegistry.findById(it.themeId, customThemes)?.isDark == true) it.themeId else it.darkThemeId
                                    }
                                    ThemeSwitchMode.LIGHT -> {
                                        if (ThemeRegistry.findById(it.themeId, customThemes)?.isDark == false) it.themeId else ThemeRegistry.blueprintBlue.id
                                    }
                                    else -> it.themeId
                                }
                                it.copy(themeSwitchMode = mode, themeId = autoThemeId)
                            }
                        },
                        modifier = Modifier.testTag("switch_mode_${mode.name.lowercase()}")
                    )
                }
            }

            if (draft.themeSwitchMode == ThemeSwitchMode.SCHEDULED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HourStepperBox(
                        label = "Dark Starts",
                        hour = draft.scheduledDarkStartHour,
                        onChange = { next ->
                            onUpdateDraft { it.copy(scheduledDarkStartHour = next) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    HourStepperBox(
                        label = "Light Starts",
                        hour = draft.scheduledDarkEndHour,
                        onChange = { next ->
                            onUpdateDraft { it.copy(scheduledDarkEndHour = next) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (draft.themeSwitchMode in listOf(
                    ThemeSwitchMode.FOLLOW_SYSTEM,
                    ThemeSwitchMode.AUTO_DAY_NIGHT,
                    ThemeSwitchMode.SCHEDULED
                )
            ) {
                Text(
                    text = "PREFERRED NIGHT THEME",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.secondaryText
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ThemeRegistry.darkThemes.forEach { darkTheme ->
                        StudioOptionChip(
                            label = darkTheme.name,
                            selected = draft.darkThemeId == darkTheme.id,
                            onClick = {
                                onUpdateDraft { it.copy(darkThemeId = darkTheme.id) }
                            }
                        )
                    }
                }
            }
        }

        if (customThemes.isNotEmpty()) {
            StudioSectionCard(
                title = "Custom Saved Themes (${customThemes.size})",
                subtitle = "Created in your Custom Color Studio."
            ) {
                ThemeGridRows(
                    themes = customThemes,
                    selectedThemeId = draft.themeId,
                    onSelectTheme = { selected ->
                        onUpdateDraft {
                            it.copy(
                                themeId = selected.id,
                                themeSwitchMode = ThemeSwitchMode.CUSTOM
                            )
                        }
                    }
                )
            }
        }

        StudioSectionCard(
            title = "Light Themes (${ThemeRegistry.lightThemes.size})",
            subtitle = "Crisp daylight & technical paper themes."
        ) {
            ThemeGridRows(
                themes = ThemeRegistry.lightThemes,
                selectedThemeId = draft.themeId,
                onSelectTheme = { selected ->
                    onUpdateDraft {
                        it.copy(
                            themeId = selected.id,
                            themeSwitchMode = if (it.themeSwitchMode == ThemeSwitchMode.DARK) {
                                ThemeSwitchMode.LIGHT
                            } else {
                                it.themeSwitchMode
                            }
                        )
                    }
                }
            )
        }

        StudioSectionCard(
            title = "Dark Themes (${ThemeRegistry.darkThemes.size})",
            subtitle = "Developer IDE & low-glare AMOLED dark themes."
        ) {
            ThemeGridRows(
                themes = ThemeRegistry.darkThemes,
                selectedThemeId = draft.themeId,
                onSelectTheme = { selected ->
                    onUpdateDraft {
                        it.copy(
                            themeId = selected.id,
                            darkThemeId = selected.id,
                            themeSwitchMode = ThemeSwitchMode.DARK
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun ThemeGridRows(
    themes: List<ThemeDefinition>,
    selectedThemeId: String,
    onSelectTheme: (ThemeDefinition) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        themes.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pair.forEach { theme ->
                    ThemePreviewTile(
                        theme = theme,
                        selected = theme.id == selectedThemeId,
                        onClick = { onSelectTheme(theme) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ThemePreviewTile(
    theme: ThemeDefinition,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGovindaTokens.current
    Surface(
        onClick = onClick,
        modifier = modifier.testTag("theme_card_${theme.id}"),
        shape = RoundedCornerShape(8.dp),
        color = Color(theme.backgroundArgb),
        border = BorderStroke(
            width = if (selected) 2.5.dp else 1.4.dp,
            color = if (selected) tokens.primary else Color(theme.borderArgb)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(theme.primaryTextArgb),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(theme.primaryArgb)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = Color(theme.onPrimaryArgb),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(theme.surfaceArgb))
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    theme.primaryArgb,
                    theme.secondaryArgb,
                    theme.accentArgb,
                    theme.successArgb
                ).forEach { swatch ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(swatch))
                            .border(0.8.dp, Color(theme.borderArgb), CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun HourStepperBox(
    label: String,
    hour: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGovindaTokens.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(tokens.background)
            .border(1.4.dp, tokens.border, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = tokens.secondaryText
            )
            Text(
                text = "%02d:00".format(hour),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = tokens.primaryText
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Surface(
                onClick = { onChange((hour + 23) % 24) },
                shape = RoundedCornerShape(4.dp),
                color = tokens.selectedState,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("-", color = tokens.primary, fontWeight = FontWeight.Bold)
                }
            }
            Surface(
                onClick = { onChange((hour + 1) % 24) },
                shape = RoundedCornerShape(4.dp),
                color = tokens.selectedState,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("+", color = tokens.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
