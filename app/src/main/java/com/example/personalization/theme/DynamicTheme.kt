package com.example.personalization.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.personalization.fonts.TypographyManager
import com.example.personalization.model.AppearanceProfile
import com.example.personalization.model.AvatarProfile
import com.example.personalization.model.BackgroundPattern
import com.example.personalization.model.ComponentStyle
import com.example.personalization.model.EffectSettings
import com.example.personalization.model.IconProfile
import com.example.personalization.model.LauncherIconStyle
import com.example.personalization.model.LayoutProfile
import com.example.personalization.model.ThemeDefinition
import com.example.personalization.model.TypographyProfile

@Immutable
data class GovindaDesignTokens(
    val theme: ThemeDefinition = ThemeRegistry.blueprintBlue,
    val profile: AppearanceProfile = AppearanceProfile(),
    val background: Color = Color(ThemeRegistry.blueprintBlue.backgroundArgb),
    val surface: Color = Color(ThemeRegistry.blueprintBlue.surfaceArgb),
    val elevatedSurface: Color = Color(ThemeRegistry.blueprintBlue.elevatedSurfaceArgb),
    val primary: Color = Color(ThemeRegistry.blueprintBlue.primaryArgb),
    val secondary: Color = Color(ThemeRegistry.blueprintBlue.secondaryArgb),
    val accent: Color = Color(ThemeRegistry.blueprintBlue.accentArgb),
    val onPrimary: Color = Color(ThemeRegistry.blueprintBlue.onPrimaryArgb),
    val primaryText: Color = Color(ThemeRegistry.blueprintBlue.primaryTextArgb),
    val secondaryText: Color = Color(ThemeRegistry.blueprintBlue.secondaryTextArgb),
    val mutedText: Color = Color(ThemeRegistry.blueprintBlue.mutedTextArgb),
    val border: Color = Color(ThemeRegistry.blueprintBlue.borderArgb),
    val divider: Color = Color(ThemeRegistry.blueprintBlue.dividerArgb),
    val success: Color = Color(ThemeRegistry.blueprintBlue.successArgb),
    val warning: Color = Color(ThemeRegistry.blueprintBlue.warningArgb),
    val error: Color = Color(ThemeRegistry.blueprintBlue.errorArgb),
    val selectedState: Color = Color(ThemeRegistry.blueprintBlue.selectedStateArgb),
    val disabledState: Color = Color(ThemeRegistry.blueprintBlue.disabledStateArgb),
    val typographyProfile: TypographyProfile = TypographyProfile(),
    val iconProfile: IconProfile = IconProfile(),
    val avatarProfile: AvatarProfile = AvatarProfile(),
    val layoutProfile: LayoutProfile = LayoutProfile(),
    val componentStyle: ComponentStyle = ComponentStyle(),
    val backgroundPattern: BackgroundPattern = BackgroundPattern.BLUEPRINT_GRID,
    val effects: EffectSettings = EffectSettings(),
    val launcherIconStyle: LauncherIconStyle = LauncherIconStyle.CLASSIC_BLUEPRINT
)

val LocalGovindaTokens = staticCompositionLocalOf { GovindaDesignTokens() }

@Composable
fun GovindaDynamicTheme(
    profile: AppearanceProfile,
    customThemes: List<ThemeDefinition> = emptyList(),
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val resolvedTheme = remember(profile, customThemes, systemDark) {
        ThemeManager.resolveActiveTheme(
            profile = profile,
            customThemes = customThemes,
            isSystemDark = systemDark
        )
    }

    val colorScheme = remember(resolvedTheme) {
        ThemeManager.toColorScheme(resolvedTheme)
    }

    val typography = remember(profile.typography) {
        TypographyManager.buildTypography(profile.typography)
    }

    val corner = profile.components.cardStyle.cornerDp
    val shapes = remember(corner) {
        Shapes(
            extraSmall = RoundedCornerShape((corner / 2).coerceAtLeast(0).dp),
            small = RoundedCornerShape(corner.coerceAtLeast(0).dp),
            medium = RoundedCornerShape(corner.coerceAtLeast(0).dp),
            large = RoundedCornerShape((corner + 2).dp),
            extraLarge = RoundedCornerShape((corner + 4).dp)
        )
    }

    val tokens = remember(resolvedTheme, profile) {
        GovindaDesignTokens(
            theme = resolvedTheme,
            profile = profile,
            background = Color(resolvedTheme.backgroundArgb),
            surface = Color(resolvedTheme.surfaceArgb),
            elevatedSurface = Color(resolvedTheme.elevatedSurfaceArgb),
            primary = Color(resolvedTheme.primaryArgb),
            secondary = Color(resolvedTheme.secondaryArgb),
            accent = Color(resolvedTheme.accentArgb),
            onPrimary = Color(resolvedTheme.onPrimaryArgb),
            primaryText = Color(resolvedTheme.primaryTextArgb),
            secondaryText = Color(resolvedTheme.secondaryTextArgb),
            mutedText = Color(resolvedTheme.mutedTextArgb),
            border = Color(resolvedTheme.borderArgb),
            divider = Color(resolvedTheme.dividerArgb),
            success = Color(resolvedTheme.successArgb),
            warning = Color(resolvedTheme.warningArgb),
            error = Color(resolvedTheme.errorArgb),
            selectedState = Color(resolvedTheme.selectedStateArgb),
            disabledState = Color(resolvedTheme.disabledStateArgb),
            typographyProfile = profile.typography,
            iconProfile = profile.iconProfile,
            avatarProfile = profile.avatar,
            layoutProfile = profile.layout,
            componentStyle = profile.components,
            backgroundPattern = profile.background,
            effects = profile.effects,
            launcherIconStyle = profile.launcherIconStyle
        )
    }

    CompositionLocalProvider(LocalGovindaTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}
