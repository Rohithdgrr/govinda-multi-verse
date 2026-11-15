package com.example.feature.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.sin

private val PrimaryBlue = Color(0xFF003DA5)
private val LightBlueSurface = Color(0xFFEAF1FF)
private val BorderBlue = Color(0xFFBFD4FF)
private val RecordRed = Color(0xFFD93025)

@Composable
fun VoiceNoteRecorderBar(
    onCancel: () -> Unit,
    onSendVoiceNote: (durationMs: Long, waveformCsv: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var elapsedDeciseconds by remember { mutableIntStateOf(10) }
    var isPaused by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(true) }
    val amplitudes = remember {
        mutableStateListOf(0.35f, 0.65f, 0.8f, 0.45f, 0.9f, 0.55f, 0.7f, 0.4f, 0.85f, 0.6f, 0.5f, 0.75f)
    }

    LaunchedEffect(isPaused) {
        while (!isPaused) {
            delay(250L)
            elapsedDeciseconds += 2
            val nextAmp = (0.25f + 0.65f * kotlin.math.abs(sin(elapsedDeciseconds * 0.45))).toFloat()
            if (amplitudes.size >= 24) amplitudes.removeAt(0)
            amplitudes.add(nextAmp)
        }
    }

    val totalSeconds = (elapsedDeciseconds / 10).coerceAtLeast(1)
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    val formattedDuration = "%d:%02d".format(mins, secs)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, PrimaryBlue),
        modifier = modifier
            .fillMaxWidth()
            .testTag("voice_recorder_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("voice_cancel_button")
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete recording", tint = RecordRed)
            }

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isPaused) Color(0xFF94A3B8) else RecordRed)
            )

            Text(
                text = formattedDuration,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF0F172A)
            )

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(LightBlueSurface)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                val barCount = amplitudes.size.coerceAtLeast(1)
                val slotWidth = size.width / barCount
                val barWidth = (slotWidth * 0.55f).coerceAtLeast(2f)
                amplitudes.forEachIndexed { idx, amp ->
                    val h = (size.height * amp.coerceIn(0.15f, 1f))
                    val top = (size.height - h) / 2f
                    drawRoundRect(
                        color = PrimaryBlue,
                        topLeft = Offset(idx * slotWidth, top),
                        size = Size(barWidth, h),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                }
            }

            Surface(
                onClick = { isLocked = !isLocked },
                shape = RoundedCornerShape(6.dp),
                color = if (isLocked) LightBlueSurface else Color.White,
                border = BorderStroke(1.dp, BorderBlue)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = "Lock recording", tint = PrimaryBlue, modifier = Modifier.size(13.dp))
                    Text(if (isLocked) "LOCKED" else "HOLD", style = MaterialTheme.typography.labelSmall, color = PrimaryBlue)
                }
            }

            IconButton(
                onClick = { isPaused = !isPaused },
                modifier = Modifier
                    .size(38.dp)
                    .testTag("voice_pause_resume_button")
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                    contentDescription = if (isPaused) "Resume recording" else "Pause recording",
                    tint = PrimaryBlue
                )
            }

            Surface(
                onClick = {
                    val csv = amplitudes.joinToString(",") { "%.2f".format(it) }
                    onSendVoiceNote(totalSeconds * 1000L, csv)
                },
                shape = RoundedCornerShape(8.dp),
                color = PrimaryBlue,
                modifier = Modifier
                    .size(42.dp)
                    .testTag("voice_send_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send voice message", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun VoiceMessagePlaybackBubble(
    durationMs: Long,
    waveformCsv: String,
    fileName: String,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var speedMultiplier by remember { mutableFloatStateOf(1.0f) }

    val bars = remember(waveformCsv) {
        waveformCsv.split(",")
            .mapNotNull { it.toFloatOrNull() }
            .ifEmpty { listOf(0.3f, 0.7f, 0.5f, 0.9f, 0.6f, 0.4f, 0.8f, 0.5f, 0.75f, 0.45f, 0.65f, 0.55f) }
    }

    LaunchedEffect(isPlaying, speedMultiplier) {
        while (isPlaying) {
            delay((120L / speedMultiplier).toLong().coerceAtLeast(40L))
            progress += 0.05f
            if (progress >= 1f) {
                progress = 0f
                isPlaying = false
            }
        }
    }

    val secs = (durationMs / 1000L).coerceAtLeast(1L)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, BorderBlue, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = { isPlaying = !isPlaying },
                shape = CircleShape,
                color = PrimaryBlue,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("voice_playback_toggle")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause voice message" else "Play voice message",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
            ) {
                val count = bars.size.coerceAtLeast(1)
                val slot = size.width / count
                val barW = (slot * 0.58f).coerceAtLeast(2f)
                bars.forEachIndexed { idx, amp ->
                    val frac = idx.toFloat() / count.toFloat()
                    val active = frac <= progress
                    val h = size.height * amp.coerceIn(0.18f, 1f)
                    val top = (size.height - h) / 2f
                    drawRoundRect(
                        color = if (active) PrimaryBlue else BorderBlue,
                        topLeft = Offset(idx * slot, top),
                        size = Size(barW, h),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                }
            }

            Surface(
                onClick = {
                    speedMultiplier = when (speedMultiplier) {
                        1.0f -> 1.5f
                        1.5f -> 2.0f
                        else -> 1.0f
                    }
                },
                shape = RoundedCornerShape(6.dp),
                color = LightBlueSurface,
                border = BorderStroke(1.dp, BorderBlue),
                modifier = Modifier.testTag("voice_speed_button")
            ) {
                Text(
                    text = "${speedMultiplier}x",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Filled.Mic, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(12.dp))
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF475569)
                )
            }
            Text(
                text = "0:%02d · Local Audio".format(secs),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = PrimaryBlue
            )
        }
    }
}
