package com.example.personalization.model

import androidx.compose.runtime.Immutable

enum class IconStyleFamily(val displayName: String, val description: String) {
    LUCIDE_OUTLINE("Lucide Outline", "Crisp 2dp open geometric strokes"),
    MATERIAL_ROUNDED("Material Rounded", "Friendly rounded corners & terminals"),
    MATERIAL_OUTLINED("Material Outlined", "Clean uniform outline geometry"),
    MATERIAL_SHARP("Material Sharp", "Crisp 90-degree technical corners"),
    MINIMAL_LINE("Minimal Line", "Ultra-clean hairline stroke paths"),
    TECHNICAL_BLUEPRINT("Technical Blueprint", "Precision CAD crosshair & ticks"),
    FILLED("Filled", "High-contrast solid filled glyphs"),
    DUOTONE("Duotone", "Two-tone layered tint & fill")
}

enum class IconGlyph(
    val id: String,
    val displayName: String,
    val categoryGroup: String
) {
    ADDITION("addition", "Addition", "Arithmetic"),
    SUBTRACTION("subtraction", "Subtraction", "Arithmetic"),
    MULTIPLICATION("multiplication", "Multiplication", "Arithmetic"),
    DIVISION("division", "Division", "Arithmetic"),
    FRACTIONS("fractions", "Fractions", "Numbers"),
    PERCENTAGES("percentages", "Percentages", "Numbers"),
    DECIMALS("decimals", "Decimals", "Numbers"),
    POWERS("powers", "Powers", "Advanced"),
    ROOTS("roots", "Roots", "Advanced"),
    ALGEBRA("algebra", "Algebra", "Advanced"),
    PROGRESS("progress", "Progress", "Telemetry"),
    STATISTICS("statistics", "Statistics", "Telemetry"),
    SETTINGS("settings", "Settings", "System"),
    PRACTICE("practice", "Practice", "System"),
    ACHIEVEMENTS("achievements", "Achievements", "Telemetry")
}

@Immutable
data class IconProfile(
    val family: IconStyleFamily = IconStyleFamily.TECHNICAL_BLUEPRINT,
    val sizeDp: Float = 22f,
    val strokeWidthDp: Float = 2.0f,
    val customTintArgb: Long? = null,
    val navIconFamily: IconStyleFamily = IconStyleFamily.FILLED,
    val categoryIconFamily: IconStyleFamily = IconStyleFamily.TECHNICAL_BLUEPRINT
)
