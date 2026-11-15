package com.example.personalization.model

import androidx.compose.runtime.Immutable

enum class FontCategory(val displayName: String) {
    CONDENSED("Condensed"),
    SANS_SERIF("Sans-Serif"),
    MONOSPACE("Monospace"),
    SERIF("Serif")
}

@Immutable
data class FontOption(
    val id: String,
    val displayName: String,
    val category: FontCategory
)

enum class FontWeightLevel(val label: String, val weightValue: Int) {
    REGULAR("Regular", 400),
    MEDIUM("Medium", 500),
    SEMI_BOLD("Semi-Bold", 600),
    BOLD("Bold", 700)
}

@Immutable
data class TypographyProfile(
    val headingFontId: String = "barlow_condensed",
    val bodyFontId: String = "roboto_condensed",
    val equationFontId: String = "ibm_plex_mono",
    val technicalFontId: String = "ibm_plex_mono",
    val fontSizeScale: Float = 1.0f,
    val fontWeightLevel: FontWeightLevel = FontWeightLevel.BOLD,
    val letterSpacingScale: Float = 1.0f,
    val lineHeightScale: Float = 1.0f,
    val tabularNumerals: Boolean = true
)
