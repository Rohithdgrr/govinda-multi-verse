package com.example.personalization.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.personalization.model.AppearanceProfile
import com.example.personalization.model.CustomColorOverrides
import com.example.personalization.model.ThemeDefinition
import com.example.personalization.model.ThemeSwitchMode
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min

object ThemeManager {

    fun resolveActiveTheme(
        profile: AppearanceProfile,
        customThemes: List<ThemeDefinition>,
        isSystemDark: Boolean,
        currentHourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    ): ThemeDefinition {
        val selectedTheme = ThemeRegistry.findById(profile.themeId, customThemes)
            ?: ThemeRegistry.blueprintBlue

        val baseTheme = when (profile.themeSwitchMode) {
            ThemeSwitchMode.LIGHT -> {
                if (!selectedTheme.isDark) selectedTheme else ThemeRegistry.blueprintBlue
            }
            ThemeSwitchMode.DARK -> {
                if (selectedTheme.isDark) selectedTheme
                else ThemeRegistry.findById(profile.darkThemeId, customThemes) ?: ThemeRegistry.darkThemes.first()
            }
            ThemeSwitchMode.FOLLOW_SYSTEM -> {
                if (isSystemDark) {
                    if (selectedTheme.isDark) selectedTheme
                    else ThemeRegistry.findById(profile.darkThemeId, customThemes) ?: ThemeRegistry.darkThemes.first()
                } else {
                    if (!selectedTheme.isDark) selectedTheme else ThemeRegistry.blueprintBlue
                }
            }
            ThemeSwitchMode.AUTO_DAY_NIGHT -> {
                val isNight = currentHourOfDay >= 19 || currentHourOfDay < 6
                if (isNight) {
                    if (selectedTheme.isDark) selectedTheme
                    else ThemeRegistry.findById(profile.darkThemeId, customThemes) ?: ThemeRegistry.darkThemes.first()
                } else {
                    if (!selectedTheme.isDark) selectedTheme else ThemeRegistry.blueprintBlue
                }
            }
            ThemeSwitchMode.SCHEDULED -> {
                val isScheduledDark = isHourInWindow(
                    hour = currentHourOfDay,
                    startHour = profile.scheduledDarkStartHour,
                    endHour = profile.scheduledDarkEndHour
                )
                if (isScheduledDark) {
                    ThemeRegistry.findById(profile.darkThemeId, customThemes) ?: ThemeRegistry.darkThemes.first()
                } else {
                    if (!selectedTheme.isDark) selectedTheme else ThemeRegistry.blueprintBlue
                }
            }
            ThemeSwitchMode.CUSTOM -> {
                selectedTheme
            }
        }

        val withCustom = applyCustomOverrides(baseTheme, profile.customColors)
        return if (profile.effects.highContrastMode) applyHighContrast(withCustom) else withCustom
    }

    fun applyCustomOverrides(
        base: ThemeDefinition,
        overrides: CustomColorOverrides
    ): ThemeDefinition {
        if (!overrides.enabled) return base
        val resolvedBg = overrides.backgroundArgb ?: base.backgroundArgb
        val resolvedPrimary = overrides.primaryArgb ?: base.primaryArgb
        val bgIsDark = Color(resolvedBg).luminance() < 0.35f
        val onPrimary = if (Color(resolvedPrimary).luminance() > 0.5f) 0xFF0D1117 else 0xFFFFFFFF

        return base.copy(
            isDark = bgIsDark,
            backgroundArgb = resolvedBg,
            surfaceArgb = overrides.surfaceArgb ?: base.surfaceArgb,
            primaryArgb = resolvedPrimary,
            accentArgb = overrides.accentArgb ?: base.accentArgb,
            onPrimaryArgb = onPrimary,
            primaryTextArgb = overrides.primaryTextArgb ?: base.primaryTextArgb,
            borderArgb = overrides.borderArgb ?: base.borderArgb
        )
    }

    private fun applyHighContrast(theme: ThemeDefinition): ThemeDefinition {
        return if (theme.isDark) {
            theme.copy(
                backgroundArgb = 0xFF000000,
                surfaceArgb = 0xFF101012,
                primaryTextArgb = 0xFFFFFFFF,
                secondaryTextArgb = 0xFFE6ECF5,
                borderArgb = 0xFF9EC5FF
            )
        } else {
            theme.copy(
                backgroundArgb = 0xFFFFFFFF,
                surfaceArgb = 0xFFF2F5FA,
                primaryTextArgb = 0xFF000000,
                secondaryTextArgb = 0xFF1A2B4C,
                borderArgb = 0xFF003DA5
            )
        }
    }

    private fun isHourInWindow(hour: Int, startHour: Int, endHour: Int): Boolean {
        val h = hour.coerceIn(0, 23)
        val s = startHour.coerceIn(0, 23)
        val e = endHour.coerceIn(0, 23)
        return if (s <= e) h in s until e else h >= s || h < e
    }

    fun toColorScheme(theme: ThemeDefinition): ColorScheme {
        val bg = Color(theme.backgroundArgb)
        val surface = Color(theme.surfaceArgb)
        val elevated = Color(theme.elevatedSurfaceArgb)
        val primary = Color(theme.primaryArgb)
        val secondary = Color(theme.secondaryArgb)
        val accent = Color(theme.accentArgb)
        val onPrimary = Color(theme.onPrimaryArgb)
        val primaryText = Color(theme.primaryTextArgb)
        val secondaryText = Color(theme.secondaryTextArgb)
        val border = Color(theme.borderArgb)
        val divider = Color(theme.dividerArgb)
        val error = Color(theme.errorArgb)

        return if (theme.isDark) {
            darkColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                primaryContainer = elevated,
                onPrimaryContainer = primaryText,
                secondary = secondary,
                onSecondary = onPrimary,
                secondaryContainer = surface,
                onSecondaryContainer = primaryText,
                tertiary = accent,
                onTertiary = onPrimary,
                background = bg,
                onBackground = primaryText,
                surface = surface,
                onSurface = primaryText,
                surfaceVariant = elevated,
                onSurfaceVariant = secondaryText,
                outline = border,
                outlineVariant = divider,
                error = error,
                onError = onPrimary,
                errorContainer = error.copy(alpha = 0.22f),
                onErrorContainer = error
            )
        } else {
            lightColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                primaryContainer = Color(theme.selectedStateArgb),
                onPrimaryContainer = primaryText,
                secondary = secondary,
                onSecondary = onPrimary,
                secondaryContainer = surface,
                onSecondaryContainer = primaryText,
                tertiary = accent,
                onTertiary = onPrimary,
                background = bg,
                onBackground = primaryText,
                surface = surface,
                onSurface = primaryText,
                surfaceVariant = elevated,
                onSurfaceVariant = secondaryText,
                outline = border,
                outlineVariant = divider,
                error = error,
                onError = Color.White,
                errorContainer = error.copy(alpha = 0.14f),
                onErrorContainer = error
            )
        }
    }

    fun contrastRatio(foregroundArgb: Long, backgroundArgb: Long): Float {
        val l1 = Color(foregroundArgb).luminance()
        val l2 = Color(backgroundArgb).luminance()
        val lighter = max(l1, l2) + 0.05f
        val darker = min(l1, l2) + 0.05f
        return lighter / darker
    }
}
