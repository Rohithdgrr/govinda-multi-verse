package com.example.personalization.model

import androidx.compose.runtime.Immutable

enum class CardStylePreset(val displayName: String, val cornerDp: Int, val borderDp: Float) {
    TECHNICAL_RECTANGULAR("Technical Rectangular", 2, 1.8f),
    COMPACT_OUTLINED("Compact Outlined", 6, 1.5f),
    FLAT("Flat", 8, 0f),
    SOFT_ELEVATION("Soft Elevation", 12, 1.0f),
    BLUEPRINT_PANEL("Blueprint Panel", 6, 2.0f),
    MINIMAL_DIVIDER("Minimal Divider", 0, 1.5f)
}

enum class ButtonStylePreset(val displayName: String) {
    SOLID("Solid"),
    OUTLINED("Outlined"),
    TEXT("Text"),
    COMPACT_RECTANGULAR("Compact Rectangular"),
    ICON_ONLY("Icon Accent"),
    HIGH_CONTRAST("High Contrast")
}

enum class InputStylePreset(val displayName: String) {
    OUTLINED("Outlined"),
    UNDERLINED("Underlined"),
    FILLED("Filled"),
    MINIMAL("Minimal"),
    TECHNICAL_MONO("Technical Mono")
}

enum class KeypadStylePreset(val displayName: String) {
    BLUEPRINT("Blueprint Keypad"),
    COMPACT_CALCULATOR("Compact Calculator"),
    FLAT_WHITE("Flat Clean"),
    OUTLINED("Outlined"),
    LARGE_ACCESSIBILITY("Large Accessibility")
}

enum class BackgroundPattern(val displayName: String) {
    PURE_WHITE("Pure Clean"),
    LIGHT_BLUE("Tinted Surface"),
    WARM_PAPER("Warm Paper"),
    SUBTLE_TECH_GRID("Subtle Tech Grid"),
    BLUEPRINT_GRID("Blueprint Grid"),
    DOTTED_PATTERN("Dotted Matrix"),
    GEOMETRIC_PATTERN("Geometric"),
    MINIMAL_PLAIN("Minimal Plain")
}

enum class ElevationLevel(val displayName: String, val elevationDp: Float) {
    NO_SHADOWS("No Shadows", 0f),
    SUBTLE_SHADOWS("Subtle Shadows", 2f),
    SOFT_ELEVATION("Soft Elevation", 6f)
}

enum class MotionLevel(val displayName: String, val durationScale: Float) {
    REDUCED("Reduced Motion", 0f),
    STANDARD("Standard", 1f),
    ENHANCED("Enhanced", 1.35f)
}

enum class LauncherIconStyle(val displayName: String) {
    CLASSIC_BLUEPRINT("Classic Blueprint"),
    MONO_TERMINAL("Mono Terminal"),
    GEOMETRIC_PRISM("Geometric Prism"),
    HIGH_CONTRAST("High Contrast")
}

@Immutable
data class ComponentStyle(
    val cardStyle: CardStylePreset = CardStylePreset.BLUEPRINT_PANEL,
    val buttonStyle: ButtonStylePreset = ButtonStylePreset.SOLID,
    val inputStyle: InputStylePreset = InputStylePreset.OUTLINED,
    val keypadStyle: KeypadStylePreset = KeypadStylePreset.BLUEPRINT
)

@Immutable
data class EffectSettings(
    val elevationLevel: ElevationLevel = ElevationLevel.NO_SHADOWS,
    val motionLevel: MotionLevel = MotionLevel.STANDARD,
    val soundEnabled: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val highContrastMode: Boolean = false,
    val extraLargeTargets: Boolean = false
)
