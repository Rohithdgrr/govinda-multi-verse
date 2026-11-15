package com.example.personalization.fonts

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.personalization.model.FontWeightLevel
import com.example.personalization.model.TypographyProfile

object TypographyManager {

    fun buildTypography(profile: TypographyProfile): Typography {
        val headingFamily = FontRegistry.fontFamilyFor(profile.headingFontId)
        val bodyFamily = FontRegistry.fontFamilyFor(profile.bodyFontId)
        val equationFamily = FontRegistry.fontFamilyFor(profile.equationFontId)
        val techFamily = FontRegistry.fontFamilyFor(profile.technicalFontId)

        val sizeScale = profile.fontSizeScale.coerceIn(0.85f, 1.35f)
        val lineScale = profile.lineHeightScale.coerceIn(0.85f, 1.4f)
        val letterScale = profile.letterSpacingScale.coerceIn(0.5f, 1.8f)

        val baseWeight = when (profile.fontWeightLevel) {
            FontWeightLevel.REGULAR -> FontWeight.Normal
            FontWeightLevel.MEDIUM -> FontWeight.Medium
            FontWeightLevel.SEMI_BOLD -> FontWeight.SemiBold
            FontWeightLevel.BOLD -> FontWeight.Bold
        }
        val strongWeight = when (profile.fontWeightLevel) {
            FontWeightLevel.REGULAR -> FontWeight.SemiBold
            FontWeightLevel.MEDIUM, FontWeightLevel.SEMI_BOLD, FontWeightLevel.BOLD -> FontWeight.Bold
        }

        val numFeatures = if (profile.tabularNumerals) "tnum" else null

        return Typography(
            displayLarge = TextStyle(
                fontFamily = equationFamily,
                fontWeight = strongWeight,
                fontSize = (38f * sizeScale).sp,
                lineHeight = (44f * sizeScale * lineScale).sp,
                letterSpacing = (-0.5f * letterScale).sp,
                fontFeatureSettings = numFeatures
            ),
            displayMedium = TextStyle(
                fontFamily = equationFamily,
                fontWeight = strongWeight,
                fontSize = (30f * sizeScale).sp,
                lineHeight = (36f * sizeScale * lineScale).sp,
                letterSpacing = (-0.25f * letterScale).sp,
                fontFeatureSettings = numFeatures
            ),
            headlineLarge = TextStyle(
                fontFamily = headingFamily,
                fontWeight = strongWeight,
                fontSize = (30f * sizeScale).sp,
                lineHeight = (34f * sizeScale * lineScale).sp,
                letterSpacing = (0.25f * letterScale).sp
            ),
            headlineMedium = TextStyle(
                fontFamily = headingFamily,
                fontWeight = strongWeight,
                fontSize = (24f * sizeScale).sp,
                lineHeight = (28f * sizeScale * lineScale).sp,
                letterSpacing = (0.2f * letterScale).sp
            ),
            titleLarge = TextStyle(
                fontFamily = headingFamily,
                fontWeight = strongWeight,
                fontSize = (21f * sizeScale).sp,
                lineHeight = (25f * sizeScale * lineScale).sp,
                letterSpacing = (0.15f * letterScale).sp
            ),
            titleMedium = TextStyle(
                fontFamily = bodyFamily,
                fontWeight = strongWeight,
                fontSize = (17f * sizeScale).sp,
                lineHeight = (22f * sizeScale * lineScale).sp,
                letterSpacing = (0.1f * letterScale).sp
            ),
            titleSmall = TextStyle(
                fontFamily = bodyFamily,
                fontWeight = baseWeight,
                fontSize = (15f * sizeScale).sp,
                lineHeight = (20f * sizeScale * lineScale).sp,
                letterSpacing = (0.1f * letterScale).sp
            ),
            bodyLarge = TextStyle(
                fontFamily = bodyFamily,
                fontWeight = baseWeight,
                fontSize = (16f * sizeScale).sp,
                lineHeight = (22f * sizeScale * lineScale).sp,
                letterSpacing = (0.15f * letterScale).sp
            ),
            bodyMedium = TextStyle(
                fontFamily = bodyFamily,
                fontWeight = FontWeight.Normal,
                fontSize = (15f * sizeScale).sp,
                lineHeight = (20f * sizeScale * lineScale).sp,
                letterSpacing = (0.15f * letterScale).sp
            ),
            bodySmall = TextStyle(
                fontFamily = bodyFamily,
                fontWeight = baseWeight,
                fontSize = (13f * sizeScale).sp,
                lineHeight = (17f * sizeScale * lineScale).sp,
                letterSpacing = (0.15f * letterScale).sp
            ),
            labelLarge = TextStyle(
                fontFamily = techFamily,
                fontWeight = strongWeight,
                fontSize = (14f * sizeScale).sp,
                lineHeight = (18f * sizeScale * lineScale).sp,
                letterSpacing = (0.25f * letterScale).sp,
                fontFeatureSettings = numFeatures
            ),
            labelMedium = TextStyle(
                fontFamily = techFamily,
                fontWeight = baseWeight,
                fontSize = (12f * sizeScale).sp,
                lineHeight = (16f * sizeScale * lineScale).sp,
                letterSpacing = (0.3f * letterScale).sp,
                fontFeatureSettings = numFeatures
            ),
            labelSmall = TextStyle(
                fontFamily = techFamily,
                fontWeight = baseWeight,
                fontSize = (11f * sizeScale).sp,
                lineHeight = (14f * sizeScale * lineScale).sp,
                letterSpacing = (0.3f * letterScale).sp,
                fontFeatureSettings = numFeatures
            )
        )
    }
}
