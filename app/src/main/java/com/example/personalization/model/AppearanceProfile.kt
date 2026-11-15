package com.example.personalization.model

import androidx.compose.runtime.Immutable

@Immutable
data class AppearanceProfile(
    val id: String = "default_blueprint",
    val name: String = "Blueprint Default",
    val isBuiltIn: Boolean = true,
    val themeId: String = "blueprint_blue",
    val darkThemeId: String = "vscode_dark_plus",
    val themeSwitchMode: ThemeSwitchMode = ThemeSwitchMode.LIGHT,
    val scheduledDarkStartHour: Int = 20,
    val scheduledDarkEndHour: Int = 6,
    val customColors: CustomColorOverrides = CustomColorOverrides(),
    val typography: TypographyProfile = TypographyProfile(),
    val iconProfile: IconProfile = IconProfile(),
    val avatar: AvatarProfile = AvatarProfile(),
    val layout: LayoutProfile = LayoutProfile(),
    val components: ComponentStyle = ComponentStyle(),
    val background: BackgroundPattern = BackgroundPattern.BLUEPRINT_GRID,
    val effects: EffectSettings = EffectSettings(),
    val launcherIconStyle: LauncherIconStyle = LauncherIconStyle.CLASSIC_BLUEPRINT
)

@Immutable
data class PersonalizationState(
    val activeProfile: AppearanceProfile = AppearanceProfile(),
    val savedProfiles: List<AppearanceProfile> = emptyList(),
    val customThemes: List<ThemeDefinition> = emptyList()
)
