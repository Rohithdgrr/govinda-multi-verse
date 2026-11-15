package com.example.personalization.avatars

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalization.model.AvatarBadge
import com.example.personalization.model.AvatarBorderStyle
import com.example.personalization.model.AvatarProfile
import com.example.personalization.model.AvatarStyle
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun PersonalizationAvatar(
    avatar: AvatarProfile,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val bgColor = Color(avatar.backgroundColorArgb)
    val accentColor = Color(avatar.accentColorArgb)
    val shape = when (avatar.shapeVariant % 3) {
        0 -> RoundedCornerShape(8.dp)
        1 -> CircleShape
        else -> RoundedCornerShape(4.dp)
    }

    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "User avatar (${avatar.style.displayName})" },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(shape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                when (avatar.style) {
                    AvatarStyle.GEOMETRIC -> drawGeometricPattern(avatar.seed, accentColor)
                    AvatarStyle.ABSTRACT_TECH -> drawAbstractTechPattern(avatar.seed, accentColor)
                    AvatarStyle.MINIMAL_ILLUSTRATED -> drawMinimalFace(avatar.accessoryIndex, accentColor)
                    AvatarStyle.PIXEL -> drawPixelGrid(avatar.seed, accentColor)
                    AvatarStyle.MATH_CHARACTER -> drawMathEmblem(accentColor)
                    AvatarStyle.DEVELOPER -> drawDevTerminal(accentColor)
                    AvatarStyle.MONOGRAM -> drawMonogramFrame(accentColor)
                    AvatarStyle.GRADIENT_FREE -> drawBauhaus(avatar.seed, accentColor)
                }

                // Border rendering
                when (avatar.borderStyle) {
                    AvatarBorderStyle.NONE -> Unit
                    AvatarBorderStyle.SOLID -> {
                        drawRoundRect(
                            color = accentColor,
                            size = this.size,
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                    AvatarBorderStyle.DOUBLE_TECH -> {
                        drawRoundRect(
                            color = accentColor,
                            size = this.size,
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                            style = Stroke(width = 1.6.dp.toPx())
                        )
                        val inset = 3.dp.toPx()
                        drawRoundRect(
                            color = accentColor.copy(alpha = 0.55f),
                            topLeft = Offset(inset, inset),
                            size = Size(this.size.width - inset * 2, this.size.height - inset * 2),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                    AvatarBorderStyle.DASHED -> {
                        drawRoundRect(
                            color = accentColor,
                            size = this.size,
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                            style = Stroke(
                                width = 1.8.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                            )
                        )
                    }
                }
            }

            if (avatar.style == AvatarStyle.MONOGRAM || avatar.style == AvatarStyle.MATH_CHARACTER) {
                val glyphText = if (avatar.style == AvatarStyle.MATH_CHARACTER) {
                    listOf("π", "Σ", "√", "∞")[avatar.shapeVariant % 4]
                } else {
                    avatar.monogram.ifBlank { "G" }.take(2).uppercase()
                }
                Text(
                    text = glyphText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = (size.value * 0.42f).sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = accentColor
                )
            }
        }

        if (avatar.badge != AvatarBadge.NONE && size >= 32.dp) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .clip(CircleShape)
                    .background(accentColor)
                    .border(1.dp, Color.White, CircleShape)
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatar.badge.symbol,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = (size.value * 0.20f).coerceAtLeast(8f).sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }
}

private fun DrawScope.drawGeometricPattern(seed: Int, accent: Color) {
    val rng = Random(seed)
    val cx = size.width / 2f
    val cy = size.height / 2f
    val sides = 3 + (seed % 4)
    val radius = size.minDimension * 0.34f
    val path = Path()
    for (i in 0 until sides) {
        val angle = (-90.0 + i * (360.0 / sides)) * (Math.PI / 180.0)
        val px = cx + (radius * cos(angle)).toFloat()
        val py = cy + (radius * sin(angle)).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, accent.copy(alpha = 0.25f))
    drawPath(path, accent, style = Stroke(width = 2.dp.toPx()))
    if (rng.nextBoolean()) {
        drawCircle(accent, radius = size.minDimension * 0.08f, center = Offset(cx, cy))
    }
}

private fun DrawScope.drawAbstractTechPattern(seed: Int, accent: Color) {
    val w = size.width
    val h = size.height
    val step = w / 4f
    for (i in 1..3) {
        drawLine(accent.copy(alpha = 0.25f), Offset(i * step, 0f), Offset(i * step, h), 1.dp.toPx())
        drawLine(accent.copy(alpha = 0.25f), Offset(0f, i * step), Offset(w, i * step), 1.dp.toPx())
    }
    val offset = ((seed % 3) + 1) * 0.12f
    drawRect(
        color = accent,
        topLeft = Offset(w * offset, h * 0.28f),
        size = Size(w * 0.45f, h * 0.44f),
        style = Stroke(width = 2.dp.toPx())
    )
    drawCircle(accent, radius = w * 0.1f, center = Offset(w * 0.65f, h * 0.5f))
}

private fun DrawScope.drawMinimalFace(accessoryIndex: Int, accent: Color) {
    val w = size.width
    val h = size.height
    drawCircle(accent.copy(alpha = 0.16f), radius = w * 0.36f, center = Offset(w * 0.5f, h * 0.5f))
    drawCircle(accent, radius = w * 0.055f, center = Offset(w * 0.37f, h * 0.44f))
    drawCircle(accent, radius = w * 0.055f, center = Offset(w * 0.63f, h * 0.44f))
    drawArc(
        color = accent,
        startAngle = 15f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = Offset(w * 0.36f, h * 0.46f),
        size = Size(w * 0.28f, h * 0.2f),
        style = Stroke(width = 1.8.dp.toPx())
    )
    if (accessoryIndex % 2 == 1) {
        // Minimal glasses frame
        drawRect(accent, Offset(w * 0.28f, h * 0.37f), Size(w * 0.18f, h * 0.14f), style = Stroke(1.3.dp.toPx()))
        drawRect(accent, Offset(w * 0.54f, h * 0.37f), Size(w * 0.18f, h * 0.14f), style = Stroke(1.3.dp.toPx()))
        drawLine(accent, Offset(w * 0.46f, h * 0.44f), Offset(w * 0.54f, h * 0.44f), 1.3.dp.toPx())
    }
}

private fun DrawScope.drawPixelGrid(seed: Int, accent: Color) {
    val rng = Random(seed)
    val cols = 5
    val cell = size.width / (cols + 2)
    val startX = cell
    val startY = cell
    for (r in 0 until cols) {
        for (c in 0..cols / 2) {
            if (rng.nextBoolean()) {
                drawRect(
                    color = accent,
                    topLeft = Offset(startX + c * cell, startY + r * cell),
                    size = Size(cell * 0.92f, cell * 0.92f)
                )
                val mirrorCol = cols - 1 - c
                if (mirrorCol != c) {
                    drawRect(
                        color = accent,
                        topLeft = Offset(startX + mirrorCol * cell, startY + r * cell),
                        size = Size(cell * 0.92f, cell * 0.92f)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawMathEmblem(accent: Color) {
    drawCircle(
        color = accent.copy(alpha = 0.14f),
        radius = size.minDimension * 0.38f,
        center = Offset(size.width / 2f, size.height / 2f)
    )
}

private fun DrawScope.drawDevTerminal(accent: Color) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.24f, h * 0.34f)
        lineTo(w * 0.46f, h * 0.50f)
        lineTo(w * 0.24f, h * 0.66f)
    }
    drawPath(path, accent, style = Stroke(width = 2.2.dp.toPx()))
    drawLine(accent, Offset(w * 0.52f, h * 0.66f), Offset(w * 0.76f, h * 0.66f), 2.2.dp.toPx())
}

private fun DrawScope.drawMonogramFrame(accent: Color) {
    drawCircle(
        color = accent.copy(alpha = 0.12f),
        radius = size.minDimension * 0.4f,
        center = Offset(size.width / 2f, size.height / 2f)
    )
}

private fun DrawScope.drawBauhaus(seed: Int, accent: Color) {
    val w = size.width
    val h = size.height
    drawArc(
        color = accent.copy(alpha = 0.85f),
        startAngle = (seed % 4) * 90f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(w * 0.18f, h * 0.18f),
        size = Size(w * 0.64f, h * 0.64f)
    )
    drawRect(
        color = accent.copy(alpha = 0.35f),
        topLeft = Offset(w * 0.50f, h * 0.50f),
        size = Size(w * 0.30f, h * 0.30f)
    )
}
