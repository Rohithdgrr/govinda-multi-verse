package com.example.personalization.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.sharp.Add
import androidx.compose.material.icons.sharp.BarChart
import androidx.compose.material.icons.sharp.Calculate
import androidx.compose.material.icons.sharp.EmojiEvents
import androidx.compose.material.icons.sharp.Functions
import androidx.compose.material.icons.sharp.Percent
import androidx.compose.material.icons.sharp.PlayArrow
import androidx.compose.material.icons.sharp.Remove
import androidx.compose.material.icons.sharp.Settings
import androidx.compose.material.icons.sharp.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.personalization.model.IconGlyph
import com.example.personalization.model.IconProfile
import com.example.personalization.model.IconStyleFamily
import com.example.personalization.theme.LocalGovindaTokens
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PersonalizationIcon(
    glyph: IconGlyph,
    modifier: Modifier = Modifier,
    iconProfile: IconProfile = LocalGovindaTokens.current.iconProfile,
    tint: Color? = null,
    selected: Boolean = false,
    contentDescription: String? = glyph.displayName
) {
    val tokens = LocalGovindaTokens.current
    val resolvedTint = tint
        ?: iconProfile.customTintArgb?.let { Color(it) }
        ?: if (selected) tokens.primary else tokens.primaryText
    val iconSize: Dp = iconProfile.sizeDp.dp

    when (iconProfile.family) {
        IconStyleFamily.MATERIAL_ROUNDED,
        IconStyleFamily.MATERIAL_OUTLINED,
        IconStyleFamily.MATERIAL_SHARP,
        IconStyleFamily.FILLED -> {
            val vector = resolveMaterialVector(glyph, iconProfile.family)
            if (vector != null) {
                Icon(
                    imageVector = vector,
                    contentDescription = contentDescription,
                    tint = resolvedTint,
                    modifier = modifier.size(iconSize)
                )
            } else {
                CustomGeometricGlyphCanvas(
                    glyph = glyph,
                    family = iconProfile.family,
                    tint = resolvedTint,
                    strokeWidthDp = iconProfile.strokeWidthDp,
                    size = iconSize,
                    contentDesc = contentDescription,
                    modifier = modifier
                )
            }
        }
        IconStyleFamily.LUCIDE_OUTLINE,
        IconStyleFamily.MINIMAL_LINE,
        IconStyleFamily.TECHNICAL_BLUEPRINT,
        IconStyleFamily.DUOTONE -> {
            CustomGeometricGlyphCanvas(
                glyph = glyph,
                family = iconProfile.family,
                tint = resolvedTint,
                strokeWidthDp = if (iconProfile.family == IconStyleFamily.MINIMAL_LINE) {
                    (iconProfile.strokeWidthDp * 0.72f).coerceAtLeast(1.2f)
                } else {
                    iconProfile.strokeWidthDp
                },
                size = iconSize,
                contentDesc = contentDescription,
                modifier = modifier
            )
        }
    }
}

private fun resolveMaterialVector(glyph: IconGlyph, family: IconStyleFamily): ImageVector? {
    return when (glyph) {
        IconGlyph.ADDITION -> when (family) {
            IconStyleFamily.MATERIAL_ROUNDED -> Icons.Rounded.Add
            IconStyleFamily.MATERIAL_OUTLINED -> Icons.Outlined.Add
            IconStyleFamily.MATERIAL_SHARP -> Icons.Sharp.Add
            else -> Icons.Filled.Add
        }
        IconGlyph.SUBTRACTION -> when (family) {
            IconStyleFamily.MATERIAL_ROUNDED -> Icons.Rounded.Remove
            IconStyleFamily.MATERIAL_OUTLINED -> Icons.Outlined.Remove
            IconStyleFamily.MATERIAL_SHARP -> Icons.Sharp.Remove
            else -> Icons.Filled.Remove
        }
        IconGlyph.PERCENTAGES -> when (family) {
            IconStyleFamily.MATERIAL_ROUNDED -> Icons.Rounded.Percent
            IconStyleFamily.MATERIAL_OUTLINED -> Icons.Outlined.Percent
            IconStyleFamily.MATERIAL_SHARP -> Icons.Sharp.Percent
            else -> Icons.Filled.Percent
        }
        IconGlyph.ALGEBRA -> when (family) {
            IconStyleFamily.MATERIAL_ROUNDED -> Icons.Rounded.Functions
            IconStyleFamily.MATERIAL_OUTLINED -> Icons.Outlined.Functions
            IconStyleFamily.MATERIAL_SHARP -> Icons.Sharp.Functions
            else -> Icons.Filled.Functions
        }
        IconGlyph.PROGRESS -> when (family) {
            IconStyleFamily.MATERIAL_ROUNDED -> Icons.Rounded.ShowChart
            IconStyleFamily.MATERIAL_OUTLINED -> Icons.Outlined.ShowChart
            IconStyleFamily.MATERIAL_SHARP -> Icons.Sharp.ShowChart
            else -> Icons.Filled.ShowChart
        }
        IconGlyph.STATISTICS -> when (family) {
            IconStyleFamily.MATERIAL_ROUNDED -> Icons.Rounded.BarChart
            IconStyleFamily.MATERIAL_OUTLINED -> Icons.Outlined.BarChart
            IconStyleFamily.MATERIAL_SHARP -> Icons.Sharp.BarChart
            else -> Icons.Filled.BarChart
        }
        IconGlyph.SETTINGS -> when (family) {
            IconStyleFamily.MATERIAL_ROUNDED -> Icons.Rounded.Settings
            IconStyleFamily.MATERIAL_OUTLINED -> Icons.Outlined.Settings
            IconStyleFamily.MATERIAL_SHARP -> Icons.Sharp.Settings
            else -> Icons.Filled.Settings
        }
        IconGlyph.PRACTICE -> when (family) {
            IconStyleFamily.MATERIAL_ROUNDED -> Icons.Rounded.PlayArrow
            IconStyleFamily.MATERIAL_OUTLINED -> Icons.Outlined.PlayArrow
            IconStyleFamily.MATERIAL_SHARP -> Icons.Sharp.PlayArrow
            else -> Icons.Filled.PlayArrow
        }
        IconGlyph.ACHIEVEMENTS -> when (family) {
            IconStyleFamily.MATERIAL_ROUNDED -> Icons.Rounded.EmojiEvents
            IconStyleFamily.MATERIAL_OUTLINED -> Icons.Outlined.EmojiEvents
            IconStyleFamily.MATERIAL_SHARP -> Icons.Sharp.EmojiEvents
            else -> Icons.Filled.EmojiEvents
        }
        IconGlyph.DECIMALS -> when (family) {
            IconStyleFamily.MATERIAL_ROUNDED -> Icons.Rounded.Calculate
            IconStyleFamily.MATERIAL_OUTLINED -> Icons.Outlined.Calculate
            IconStyleFamily.MATERIAL_SHARP -> Icons.Sharp.Calculate
            else -> Icons.Filled.Calculate
        }
        else -> null
    }
}

@Composable
private fun CustomGeometricGlyphCanvas(
    glyph: IconGlyph,
    family: IconStyleFamily,
    tint: Color,
    strokeWidthDp: Float,
    size: Dp,
    contentDesc: String?,
    modifier: Modifier = Modifier
) {
    val cap = when (family) {
        IconStyleFamily.MATERIAL_SHARP, IconStyleFamily.TECHNICAL_BLUEPRINT -> StrokeCap.Square
        else -> StrokeCap.Round
    }
    val join = when (family) {
        IconStyleFamily.MATERIAL_SHARP, IconStyleFamily.TECHNICAL_BLUEPRINT -> StrokeJoin.Miter
        else -> StrokeJoin.Round
    }
    val isDuotone = family == IconStyleFamily.DUOTONE
    val isBlueprint = family == IconStyleFamily.TECHNICAL_BLUEPRINT
    val isFilled = family == IconStyleFamily.FILLED

    Canvas(
        modifier = modifier
            .size(size)
            .semantics {
                if (contentDesc != null) {
                    contentDescription = contentDesc
                }
            }
    ) {
        val w = this.size.width
        val h = this.size.height
        val strokePx = strokeWidthDp.dp.toPx().coerceIn(1.5f, w * 0.18f)
        val stroke = Stroke(width = strokePx, cap = cap, join = join)

        if (isDuotone) {
            drawRoundRect(
                color = tint.copy(alpha = 0.16f),
                topLeft = Offset(w * 0.08f, h * 0.08f),
                size = Size(w * 0.84f, h * 0.84f),
                cornerRadius = CornerRadius(w * 0.18f, w * 0.18f)
            )
        } else if (isBlueprint) {
            val tick = w * 0.14f
            val tickStroke = (strokePx * 0.65f).coerceAtLeast(1.2f)
            drawLine(tint.copy(alpha = 0.45f), Offset(0f, 0f), Offset(tick, 0f), tickStroke)
            drawLine(tint.copy(alpha = 0.45f), Offset(0f, 0f), Offset(0f, tick), tickStroke)
            drawLine(tint.copy(alpha = 0.45f), Offset(w, h), Offset(w - tick, h), tickStroke)
            drawLine(tint.copy(alpha = 0.45f), Offset(w, h), Offset(w, h - tick), tickStroke)
        }

        drawGlyphGeometry(
            glyph = glyph,
            tint = tint,
            stroke = stroke,
            strokePx = strokePx,
            isFilled = isFilled
        )
    }
}

private fun DrawScope.drawGlyphGeometry(
    glyph: IconGlyph,
    tint: Color,
    stroke: Stroke,
    strokePx: Float,
    isFilled: Boolean
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val pad = w * 0.2f

    when (glyph) {
        IconGlyph.ADDITION -> {
            drawLine(tint, Offset(cx, pad), Offset(cx, h - pad), strokePx, stroke.cap)
            drawLine(tint, Offset(pad, cy), Offset(w - pad, cy), strokePx, stroke.cap)
        }
        IconGlyph.SUBTRACTION -> {
            drawLine(tint, Offset(pad, cy), Offset(w - pad, cy), strokePx, stroke.cap)
        }
        IconGlyph.MULTIPLICATION -> {
            val p = w * 0.24f
            drawLine(tint, Offset(p, p), Offset(w - p, h - p), strokePx, stroke.cap)
            drawLine(tint, Offset(w - p, p), Offset(p, h - p), strokePx, stroke.cap)
        }
        IconGlyph.DIVISION -> {
            drawLine(tint, Offset(pad, cy), Offset(w - pad, cy), strokePx, stroke.cap)
            drawCircle(tint, radius = (strokePx * 0.9f).coerceAtLeast(w * 0.07f), center = Offset(cx, h * 0.27f))
            drawCircle(tint, radius = (strokePx * 0.9f).coerceAtLeast(w * 0.07f), center = Offset(cx, h * 0.73f))
        }
        IconGlyph.FRACTIONS -> {
            drawLine(tint, Offset(pad, cy), Offset(w - pad, cy), strokePx, stroke.cap)
            val boxW = w * 0.28f
            val boxH = h * 0.22f
            drawRoundRect(
                color = tint,
                topLeft = Offset(cx - boxW / 2f, h * 0.16f),
                size = Size(boxW, boxH),
                cornerRadius = CornerRadius(2f, 2f),
                style = if (isFilled) androidx.compose.ui.graphics.drawscope.Fill else stroke
            )
            drawRoundRect(
                color = tint,
                topLeft = Offset(cx - boxW / 2f, h * 0.62f),
                size = Size(boxW, boxH),
                cornerRadius = CornerRadius(2f, 2f),
                style = if (isFilled) androidx.compose.ui.graphics.drawscope.Fill else stroke
            )
        }
        IconGlyph.PERCENTAGES -> {
            drawLine(tint, Offset(w * 0.75f, h * 0.22f), Offset(w * 0.25f, h * 0.78f), strokePx, stroke.cap)
            drawCircle(tint, radius = w * 0.12f, center = Offset(w * 0.3f, h * 0.3f), style = stroke)
            drawCircle(tint, radius = w * 0.12f, center = Offset(w * 0.7f, h * 0.7f), style = stroke)
        }
        IconGlyph.DECIMALS -> {
            drawCircle(tint, radius = (strokePx * 1.1f).coerceAtLeast(w * 0.08f), center = Offset(w * 0.24f, h * 0.72f))
            drawOval(
                color = tint,
                topLeft = Offset(w * 0.42f, h * 0.24f),
                size = Size(w * 0.24f, h * 0.52f),
                style = stroke
            )
            drawLine(tint, Offset(w * 0.80f, h * 0.24f), Offset(w * 0.80f, h * 0.76f), strokePx, stroke.cap)
        }
        IconGlyph.POWERS -> {
            // x^2 styled geometric mark
            drawLine(tint, Offset(w * 0.2f, h * 0.42f), Offset(w * 0.48f, h * 0.76f), strokePx, stroke.cap)
            drawLine(tint, Offset(w * 0.48f, h * 0.42f), Offset(w * 0.2f, h * 0.76f), strokePx, stroke.cap)
            val expPath = Path().apply {
                moveTo(w * 0.58f, h * 0.24f)
                lineTo(w * 0.78f, h * 0.24f)
                lineTo(w * 0.58f, h * 0.44f)
                lineTo(w * 0.8f, h * 0.44f)
            }
            drawPath(expPath, tint, style = stroke)
        }
        IconGlyph.ROOTS -> {
            val rootPath = Path().apply {
                moveTo(w * 0.14f, h * 0.56f)
                lineTo(w * 0.30f, h * 0.56f)
                lineTo(w * 0.42f, h * 0.80f)
                lineTo(w * 0.56f, h * 0.24f)
                lineTo(w * 0.86f, h * 0.24f)
            }
            drawPath(rootPath, tint, style = stroke)
        }
        IconGlyph.ALGEBRA -> {
            val path = Path().apply {
                moveTo(w * 0.76f, h * 0.24f)
                lineTo(w * 0.28f, h * 0.24f)
                lineTo(w * 0.56f, cy)
                lineTo(w * 0.28f, h * 0.76f)
                lineTo(w * 0.76f, h * 0.76f)
            }
            drawPath(path, tint, style = stroke)
        }
        IconGlyph.PROGRESS -> {
            val path = Path().apply {
                moveTo(w * 0.16f, h * 0.72f)
                lineTo(w * 0.40f, h * 0.48f)
                lineTo(w * 0.58f, h * 0.60f)
                lineTo(w * 0.84f, h * 0.28f)
            }
            drawPath(path, tint, style = stroke)
            drawCircle(tint, radius = strokePx, center = Offset(w * 0.84f, h * 0.28f))
        }
        IconGlyph.STATISTICS -> {
            val barW = w * 0.16f
            drawRect(tint, Offset(w * 0.18f, h * 0.52f), Size(barW, h * 0.28f), style = stroke)
            drawRect(tint, Offset(w * 0.42f, h * 0.30f), Size(barW, h * 0.50f), style = stroke)
            drawRect(tint, Offset(w * 0.66f, h * 0.42f), Size(barW, h * 0.38f), style = stroke)
        }
        IconGlyph.SETTINGS -> {
            drawCircle(tint, radius = w * 0.18f, center = Offset(cx, cy), style = stroke)
            val outerR = w * 0.37f
            val innerR = w * 0.27f
            for (i in 0 until 8) {
                val angle = (i * 45.0) * (Math.PI / 180.0)
                val sx = cx + (innerR * cos(angle)).toFloat()
                val sy = cy + (innerR * sin(angle)).toFloat()
                val ex = cx + (outerR * cos(angle)).toFloat()
                val ey = cy + (outerR * sin(angle)).toFloat()
                drawLine(tint, Offset(sx, sy), Offset(ex, ey), strokePx, stroke.cap)
            }
        }
        IconGlyph.PRACTICE -> {
            val playPath = Path().apply {
                moveTo(w * 0.30f, h * 0.22f)
                lineTo(w * 0.78f, cy)
                lineTo(w * 0.30f, h * 0.78f)
                close()
            }
            drawPath(
                path = playPath,
                color = tint,
                style = if (isFilled) androidx.compose.ui.graphics.drawscope.Fill else stroke
            )
        }
        IconGlyph.ACHIEVEMENTS -> {
            val starPath = Path().apply {
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) w * 0.36f else w * 0.16f
                    val angle = (-90.0 + i * 36.0) * (Math.PI / 180.0)
                    val px = cx + (r * cos(angle)).toFloat()
                    val py = cy + (r * sin(angle)).toFloat()
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(
                path = starPath,
                color = tint,
                style = if (isFilled) androidx.compose.ui.graphics.drawscope.Fill else stroke
            )
        }
    }
}
