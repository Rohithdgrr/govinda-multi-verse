package com.example.personalization.model

import androidx.compose.runtime.Immutable

enum class ThemeSwitchMode(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    FOLLOW_SYSTEM("Follow System"),
    AUTO_DAY_NIGHT("Auto Day/Night"),
    SCHEDULED("Scheduled"),
    CUSTOM("Custom")
}

@Immutable
data class ThemeDefinition(
    val id: String,
    val name: String,
    val isDark: Boolean,
    val isCustom: Boolean = false,
    val backgroundArgb: Long,
    val surfaceArgb: Long,
    val elevatedSurfaceArgb: Long,
    val primaryArgb: Long,
    val secondaryArgb: Long,
    val accentArgb: Long,
    val onPrimaryArgb: Long,
    val primaryTextArgb: Long,
    val secondaryTextArgb: Long,
    val mutedTextArgb: Long,
    val borderArgb: Long,
    val dividerArgb: Long,
    val successArgb: Long,
    val warningArgb: Long,
    val errorArgb: Long,
    val selectedStateArgb: Long,
    val disabledStateArgb: Long
)

@Immutable
data class CustomColorOverrides(
    val enabled: Boolean = false,
    val primaryArgb: Long? = null,
    val accentArgb: Long? = null,
    val backgroundArgb: Long? = null,
    val surfaceArgb: Long? = null,
    val primaryTextArgb: Long? = null,
    val borderArgb: Long? = null
)
