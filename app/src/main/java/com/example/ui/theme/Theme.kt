package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val GovindaLightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = BlueprintWhite,
    primaryContainer = LightBlue,
    onPrimaryContainer = PrimaryText,
    secondary = SecondaryBlue,
    onSecondary = BlueprintWhite,
    secondaryContainer = BlueprintSurface,
    onSecondaryContainer = PrimaryText,
    tertiary = AccentPurple,
    onTertiary = BlueprintWhite,
    tertiaryContainer = AccentPurpleContainer,
    onTertiaryContainer = PrimaryText,
    background = BlueprintWhite,
    onBackground = PrimaryText,
    surface = BlueprintWhite,
    onSurface = PrimaryText,
    surfaceVariant = BlueprintSurface,
    onSurfaceVariant = SecondaryText,
    outline = BorderBlue,
    outlineVariant = LightBorder,
    error = ErrorRed,
    onError = BlueprintWhite,
    errorContainer = ErrorContainer,
    onErrorContainer = ErrorRed
)

val GovindaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(6.dp),
    extraLarge = RoundedCornerShape(8.dp)
)

@Composable
fun GovindaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GovindaLightColorScheme,
        typography = GovindaTypography,
        shapes = GovindaShapes,
        content = content
    )
}
