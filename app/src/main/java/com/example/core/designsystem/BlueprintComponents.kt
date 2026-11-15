package com.example.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.Difficulty
import com.example.domain.model.SkillTier
import com.example.personalization.model.BackgroundPattern
import com.example.personalization.model.CardStylePreset
import com.example.personalization.model.LauncherIconStyle
import com.example.personalization.theme.LocalGovindaTokens

fun Modifier.dynamicPatternBackground(
    pattern: BackgroundPattern,
    borderColor: Color,
    primaryColor: Color,
    selectedColor: Color,
    minorStepDp: Dp = 18.dp
): Modifier = drawBehind {
    val stepPx = minorStepDp.toPx()
    if (stepPx <= 0f) return@drawBehind

    when (pattern) {
        BackgroundPattern.PURE_WHITE, BackgroundPattern.MINIMAL_PLAIN -> Unit
        BackgroundPattern.LIGHT_BLUE -> {
            drawRect(selectedColor.copy(alpha = 0.25f))
        }
        BackgroundPattern.WARM_PAPER -> {
            drawRect(Color(0xFFF6EFE0).copy(alpha = 0.28f))
            var y = stepPx * 2f
            while (y < size.height) {
                drawLine(
                    color = borderColor.copy(alpha = 0.20f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += stepPx * 2f
            }
        }
        BackgroundPattern.SUBTLE_TECH_GRID -> {
            val minor = borderColor.copy(alpha = 0.25f)
            var x = stepPx
            while (x < size.width) {
                drawLine(minor, Offset(x, 0f), Offset(x, size.height), 0.9f)
                x += stepPx
            }
            var y = stepPx
            while (y < size.height) {
                drawLine(minor, Offset(0f, y), Offset(size.width, y), 0.9f)
                y += stepPx
            }
        }
        BackgroundPattern.BLUEPRINT_GRID -> {
            val minorLineColor = borderColor.copy(alpha = 0.34f)
            val majorLineColor = borderColor.copy(alpha = 0.56f)
            val registrationColor = primaryColor.copy(alpha = 0.70f)

            var col = 1
            var x = stepPx
            while (x < size.width) {
                val isMajor = col % 4 == 0
                drawLine(
                    color = if (isMajor) majorLineColor else minorLineColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = if (isMajor) 1.5f else 1.0f
                )
                x += stepPx
                col++
            }

            var row = 1
            var y = stepPx
            while (y < size.height) {
                val isMajor = row % 4 == 0
                drawLine(
                    color = if (isMajor) majorLineColor else minorLineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = if (isMajor) 1.5f else 1.0f
                )
                y += stepPx
                row++
            }

            val tick = 9.dp.toPx()
            val inset = 6.dp.toPx()
            val stroke = 2.dp.toPx()
            drawLine(registrationColor, Offset(inset, inset), Offset(inset + tick, inset), stroke)
            drawLine(registrationColor, Offset(inset, inset), Offset(inset, inset + tick), stroke)
            drawLine(registrationColor, Offset(size.width - inset, inset), Offset(size.width - inset - tick, inset), stroke)
            drawLine(registrationColor, Offset(size.width - inset, inset), Offset(size.width - inset, inset + tick), stroke)
            drawLine(registrationColor, Offset(inset, size.height - inset), Offset(inset + tick, size.height - inset), stroke)
            drawLine(registrationColor, Offset(inset, size.height - inset), Offset(inset, size.height - inset - tick), stroke)
            drawLine(registrationColor, Offset(size.width - inset, size.height - inset), Offset(size.width - inset - tick, size.height - inset), stroke)
            drawLine(registrationColor, Offset(size.width - inset, size.height - inset), Offset(size.width - inset, size.height - inset - tick), stroke)
        }
        BackgroundPattern.DOTTED_PATTERN -> {
            val dotColor = borderColor.copy(alpha = 0.55f)
            val radius = 1.6.dp.toPx()
            var x = stepPx
            while (x < size.width) {
                var y = stepPx
                while (y < size.height) {
                    drawCircle(dotColor, radius = radius, center = Offset(x, y))
                    y += stepPx
                }
                x += stepPx
            }
        }
        BackgroundPattern.GEOMETRIC_PATTERN -> {
            val lineColor = borderColor.copy(alpha = 0.30f)
            val diag = stepPx * 2.2f
            var x = -size.height
            while (x < size.width + size.height) {
                drawLine(lineColor, Offset(x, 0f), Offset(x + size.height * 0.5f, size.height), 1.2f)
                x += diag
            }
        }
    }
}

@Composable
fun GovindaLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val tokens = LocalGovindaTokens.current
    val style = tokens.launcherIconStyle
    val bgColor = when (style) {
        LauncherIconStyle.CLASSIC_BLUEPRINT -> tokens.primary
        LauncherIconStyle.MONO_TERMINAL -> tokens.elevatedSurface
        LauncherIconStyle.GEOMETRIC_PRISM -> tokens.accent
        LauncherIconStyle.HIGH_CONTRAST -> tokens.primaryText
    }
    val fgColor = when (style) {
        LauncherIconStyle.CLASSIC_BLUEPRINT -> tokens.onPrimary
        LauncherIconStyle.MONO_TERMINAL -> tokens.primary
        LauncherIconStyle.GEOMETRIC_PRISM -> tokens.onPrimary
        LauncherIconStyle.HIGH_CONTRAST -> tokens.background
    }
    val corner = when (style) {
        LauncherIconStyle.CLASSIC_BLUEPRINT -> 6.dp
        LauncherIconStyle.MONO_TERMINAL -> 4.dp
        LauncherIconStyle.GEOMETRIC_PRISM -> 10.dp
        LauncherIconStyle.HIGH_CONTRAST -> 2.dp
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(bgColor)
            .border(
                width = if (style == LauncherIconStyle.MONO_TERMINAL) 1.5.dp else 0.dp,
                color = if (style == LauncherIconStyle.MONO_TERMINAL) tokens.primary else Color.Transparent,
                shape = RoundedCornerShape(corner)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.62f)) {
            val w = this.size.width
            val h = this.size.height
            when (style) {
                LauncherIconStyle.GEOMETRIC_PRISM -> {
                    val diamond = Path().apply {
                        moveTo(w / 2f, 0f)
                        lineTo(w, h / 2f)
                        lineTo(w / 2f, h)
                        lineTo(0f, h / 2f)
                        close()
                    }
                    drawPath(diamond, fgColor, style = Stroke(width = w * 0.16f))
                    drawCircle(fgColor, radius = w * 0.16f, center = Offset(w / 2f, h / 2f))
                }
                LauncherIconStyle.MONO_TERMINAL -> {
                    val prompt = Path().apply {
                        moveTo(w * 0.12f, h * 0.22f)
                        lineTo(w * 0.50f, h * 0.50f)
                        lineTo(w * 0.12f, h * 0.78f)
                    }
                    drawPath(prompt, fgColor, style = Stroke(width = w * 0.18f))
                    drawLine(
                        fgColor,
                        Offset(w * 0.54f, h * 0.78f),
                        Offset(w * 0.90f, h * 0.78f),
                        strokeWidth = w * 0.18f
                    )
                }
                else -> {
                    val stroke = w * 0.22f
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, stroke)
                        lineTo(stroke, stroke)
                        lineTo(stroke, h - stroke)
                        lineTo(w - stroke, h - stroke)
                        lineTo(w - stroke, h * 0.56f)
                        lineTo(w * 0.50f, h * 0.56f)
                        lineTo(w * 0.50f, h * 0.38f)
                        lineTo(w, h * 0.38f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(path = path, color = fgColor)
                    drawRect(
                        color = tokens.border,
                        topLeft = Offset(w * 0.55f, h * 0.20f),
                        size = Size(w * 0.16f, h * 0.16f)
                    )
                }
            }
        }
    }
}

@Composable
fun BlueprintCard(
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalGovindaTokens.current
    val preset = tokens.componentStyle.cardStyle
    val resolvedContainer = containerColor ?: when (preset) {
        CardStylePreset.SOFT_ELEVATION -> tokens.elevatedSurface
        CardStylePreset.FLAT -> tokens.surface
        else -> tokens.background
    }
    val resolvedBorder = borderColor ?: tokens.border
    val resolvedWidth = borderWidth ?: preset.borderDp.dp
    val shape = RoundedCornerShape(preset.cornerDp.dp)
    val elevation = when {
        preset == CardStylePreset.SOFT_ELEVATION -> tokens.effects.elevationLevel.elevationDp.coerceAtLeast(3f).dp
        else -> tokens.effects.elevationLevel.elevationDp.dp
    }
    val borderStroke = if (resolvedWidth > 0.dp && preset != CardStylePreset.MINIMAL_DIVIDER) {
        BorderStroke(resolvedWidth, resolvedBorder)
    } else {
        null
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = resolvedContainer,
            border = borderStroke,
            tonalElevation = 0.dp,
            shadowElevation = elevation
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
                if (preset == CardStylePreset.MINIMAL_DIVIDER) {
                    HorizontalDivider(thickness = 1.5.dp, color = resolvedBorder)
                }
            }
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = resolvedContainer,
            border = borderStroke,
            tonalElevation = 0.dp,
            shadowElevation = elevation
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
                if (preset == CardStylePreset.MINIMAL_DIVIDER) {
                    HorizontalDivider(thickness = 1.5.dp, color = resolvedBorder)
                }
            }
        }
    }
}

@Composable
fun BlueprintBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    borderColor: Color? = null
) {
    val tokens = LocalGovindaTokens.current
    val bg = backgroundColor ?: tokens.selectedState
    val fg = textColor ?: tokens.primary
    val border = borderColor ?: tokens.border
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = fg,
            maxLines = 1
        )
    }
}

@Composable
fun DifficultyStepMeter(
    difficulty: Difficulty,
    modifier: Modifier = Modifier,
    activeColor: Color? = null
) {
    val tokens = LocalGovindaTokens.current
    val activeFill = activeColor ?: tokens.primary
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (lvl in 1..5) {
            val active = lvl <= difficulty.level
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (active) activeFill else tokens.background)
                    .border(
                        width = 1.5.dp,
                        color = if (active) activeFill else tokens.border,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
fun SkillTierBadge(
    tier: SkillTier,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGovindaTokens.current
    val (bg, fg, border) = when (tier) {
        SkillTier.WEAK -> Triple(tokens.error.copy(alpha = 0.14f), tokens.error, tokens.error)
        SkillTier.DEVELOPING -> Triple(tokens.warning.copy(alpha = 0.16f), tokens.primaryText, tokens.warning)
        SkillTier.STRONG -> Triple(tokens.success.copy(alpha = 0.14f), tokens.success, tokens.success)
    }
    BlueprintBadge(
        text = tier.label.uppercase(),
        modifier = modifier,
        backgroundColor = bg,
        textColor = fg,
        borderColor = border
    )
}

@Composable
fun BlueprintProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color? = null,
    fillColor: Color? = null,
    height: Dp = 10.dp
) {
    val tokens = LocalGovindaTokens.current
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .background(trackColor ?: tokens.selectedState)
            .border(1.5.dp, tokens.border, RoundedCornerShape(4.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .height(height)
                .background(fillColor ?: tokens.primary)
        )
    }
}

@Composable
fun BlueprintSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    val tokens = LocalGovindaTokens.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = tokens.primaryText
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.secondaryText
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
fun BlueprintMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    icon: ImageVector? = null
) {
    val tokens = LocalGovindaTokens.current
    val accent = accentColor ?: tokens.primary
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(tokens.surface)
            .border(1.5.dp, tokens.border, RoundedCornerShape(6.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(accent)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.secondaryText,
                    maxLines = 1
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = tokens.primaryText,
                maxLines = 1
            )
        }
    }
}

@Composable
fun BlueprintTimelineSteps(
    steps: List<String>,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGovindaTokens.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        steps.forEachIndexed { idx, step ->
            val isLast = idx == steps.lastIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(22.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (isLast) tokens.primary else tokens.selectedState)
                            .border(1.5.dp, if (isLast) tokens.primary else tokens.border, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isLast) "=" else "${idx + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isLast) tokens.onPrimary else tokens.primary
                        )
                    }
                    if (!isLast) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .fillMaxHeight()
                                .background(tokens.border)
                        )
                    }
                }
                Text(
                    text = step,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isLast) tokens.primary else tokens.primaryText,
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = if (isLast) 0.dp else 10.dp)
                )
            }
        }
    }
}
