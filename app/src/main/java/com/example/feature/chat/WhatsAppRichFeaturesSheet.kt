package com.example.feature.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.database.AttachmentEntity
import com.example.core.database.ChatMessageType
import com.example.core.database.ConversationEntity
import com.example.core.database.ConversationMemberEntity
import com.example.core.database.ConversationType
import com.example.core.database.MessageEntity
import com.example.core.database.UserEntity

private val PrimaryBlue = Color(0xFF003DA5)
private val SecondaryBlue = Color(0xFF1647BB)
private val LightBlueSurface = Color(0xFFEAF1FF)
private val BorderBlue = Color(0xFFBFD4FF)
private val MutedGray = Color(0xFF64748B)
private val DarkSlate = Color(0xFF0F172A)
private val AlertRed = Color(0xFFD93025)

/**
 * Parses WhatsApp-style inline rich text formatting:
 * *bold*, _italic_, ~strikethrough~, `monospace`, and @mentions
 */
fun parseWhatsAppRichText(raw: String): AnnotatedString {
    return buildAnnotatedString {
        var index = 0
        val len = raw.length
        while (index < len) {
            val ch = raw[index]
            when {
                ch == '*' -> {
                    val closeIdx = raw.indexOf('*', index + 1)
                    if (closeIdx > index + 1) {
                        pushStyle(SpanStyle(fontWeight = FontWeight.ExtraBold))
                        append(raw.substring(index + 1, closeIdx))
                        pop()
                        index = closeIdx + 1
                        continue
                    }
                }
                ch == '_' -> {
                    val closeIdx = raw.indexOf('_', index + 1)
                    if (closeIdx > index + 1) {
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        append(raw.substring(index + 1, closeIdx))
                        pop()
                        index = closeIdx + 1
                        continue
                    }
                }
                ch == '~' -> {
                    val closeIdx = raw.indexOf('~', index + 1)
                    if (closeIdx > index + 1) {
                        pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                        append(raw.substring(index + 1, closeIdx))
                        pop()
                        index = closeIdx + 1
                        continue
                    }
                }
                ch == '`' -> {
                    val closeIdx = raw.indexOf('`', index + 1)
                    if (closeIdx > index + 1) {
                        pushStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = LightBlueSurface,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        append(" ${raw.substring(index + 1, closeIdx)} ")
                        pop()
                        index = closeIdx + 1
                        continue
                    }
                }
                ch == '@' -> {
                    var endMention = index + 1
                    while (endMention < len && (raw[endMention].isLetterOrDigit() || raw[endMention] == '_' || raw[endMention] == '.')) {
                        endMention++
                    }
                    if (endMention > index + 1) {
                        pushStyle(
                            SpanStyle(
                                color = PrimaryBlue,
                                fontWeight = FontWeight.ExtraBold,
                                background = LightBlueSurface
                            )
                        )
                        append(raw.substring(index, endMention))
                        pop()
                        index = endMention
                        continue
                    }
                }
            }
            append(ch)
            index++
        }
    }
}

@Composable
fun ChatLinkPreviewCard(
    url: String,
    host: String,
    title: String,
    onCopyUrl: () -> Unit
) {
    Surface(
        onClick = onCopyUrl,
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderBlue),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightBlueSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Link,
                    contentDescription = "Link Preview",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = host,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = SecondaryBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = url,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MutedGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ChatViewOnceMediaBubble(
    message: MessageEntity,
    onOpenViewOnce: () -> Unit
) {
    val opened = message.isViewOnceOpened
    Surface(
        onClick = { if (!opened) onOpenViewOnce() },
        shape = RoundedCornerShape(12.dp),
        color = if (opened) Color(0xFFF8FAFC) else LightBlueSurface,
        border = BorderStroke(1.dp, if (opened) Color(0xFFCBD5E1) else PrimaryBlue),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .testTag("view_once_bubble_${message.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.8.dp,
                        color = if (opened) MutedGray else PrimaryBlue,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (opened) {
                    Icon(
                        imageVector = Icons.Filled.VisibilityOff,
                        contentDescription = "Opened",
                        tint = MutedGray,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Text(
                        text = "1",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = PrimaryBlue
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (opened) "Opened" else "Photo · View Once",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontStyle = if (opened) FontStyle.Italic else FontStyle.Normal
                    ),
                    color = if (opened) MutedGray else PrimaryBlue
                )
                if (!opened) {
                    Text(
                        text = "Tap to view once (self-destructs after opening)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryBlue
                    )
                }
            }
        }
    }
}

@Composable
fun PreSendMediaCaptionDialog(
    fileName: String,
    messageType: ChatMessageType,
    onDismiss: () -> Unit,
    onConfirmSend: (caption: String, isViewOnce: Boolean) -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var isViewOnce by remember { mutableStateOf(false) }
    var previewKind by remember { mutableStateOf(if (messageType == ChatMessageType.VIDEO) "VIDEO" else "IMAGE") }
    var selectedBatchCount by remember { mutableStateOf(1) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.5.dp, PrimaryBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Media Preview & Caption",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = PrimaryBlue
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = PrimaryBlue)
                    }
                }

                // Image / Video / Multi-Media Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("IMAGE" to "📷 Photo Preview", "VIDEO" to "🎬 Video Preview").forEach { (kind, label) ->
                        val active = previewKind == kind
                        Surface(
                            onClick = { previewKind = kind },
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) PrimaryBlue else LightBlueSurface,
                            border = BorderStroke(1.dp, PrimaryBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (active) Color.White else PrimaryBlue
                                )
                            }
                        }
                    }
                    Surface(
                        onClick = { selectedBatchCount = if (selectedBatchCount >= 4) 1 else selectedBatchCount + 1 },
                        shape = RoundedCornerShape(10.dp),
                        color = LightBlueSurface,
                        border = BorderStroke(1.dp, PrimaryBlue)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "+ Multi ($selectedBatchCount)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryBlue
                            )
                        }
                    }
                }

                // Media Preview Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LightBlueSurface,
                    border = BorderStroke(1.dp, BorderBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (previewKind == "VIDEO") {
                                Icons.Filled.Videocam
                            } else {
                                Icons.Filled.Image
                            },
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedBatchCount > 1) "$fileName (+${selectedBatchCount - 1} more selected)" else fileName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryBlue
                        )
                    }
                }

                // Caption Input + WhatsApp "1️⃣ View Once" toggle pill inside right side
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Add a caption...") },
                    trailingIcon = {
                        Surface(
                            onClick = { isViewOnce = !isViewOnce },
                            shape = CircleShape,
                            color = if (isViewOnce) PrimaryBlue else LightBlueSurface,
                            border = BorderStroke(1.5.dp, PrimaryBlue),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .size(30.dp)
                                .testTag("toggle_view_once_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "1",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = if (isViewOnce) Color.White else PrimaryBlue
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("media_caption_input")
                )

                if (isViewOnce) {
                    Text(
                        text = "1️⃣ View Once enabled: Media will disappear from chat after recipient opens it.",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = PrimaryBlue
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel", color = PrimaryBlue)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirmSend(caption, isViewOnce) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.testTag("confirm_send_media_button")
                    ) {
                        Text(
                            text = if (selectedBatchCount > 1) "Send ($selectedBatchCount)" else "Send",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatLiveLocationCard(
    message: MessageEntity,
    onStopSharing: () -> Unit
) {
    val expiresAt = message.liveLocationExpiresAt
    val isActive = expiresAt != null && expiresAt > System.currentTimeMillis()
    val remainingMin = if (expiresAt != null && isActive) {
        ((expiresAt - System.currentTimeMillis()) / 60_000L).coerceAtLeast(1L)
    } else {
        0L
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (isActive) PrimaryBlue else BorderBlue),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(LightBlueSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Live Location",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isActive) "Live Location · Active ($remainingMin min left)" else "Live Location · Ended",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (isActive) PrimaryBlue else MutedGray
                    )
                    Text(
                        text = "Real-time study session telemetry",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryBlue
                    )
                }
            }

            if (isActive && message.isOutgoing) {
                Surface(
                    onClick = onStopSharing,
                    shape = RoundedCornerShape(8.dp),
                    color = LightBlueSurface,
                    border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Stop Sharing",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = AlertRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiveLocationDurationDialog(
    onDismiss: () -> Unit,
    onSelectDuration: (durationMinutes: Int, placeLabel: String) -> Unit
) {
    var placeLabel by remember { mutableStateOf("Campus Math Library · Floor 2") }
    var selectedMinutes by remember { mutableStateOf(60) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.5.dp, PrimaryBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Share Location",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = PrimaryBlue
                )
                OutlinedTextField(
                    value = placeLabel,
                    onValueChange = { placeLabel = it },
                    label = { Text("Location Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Live Location Duration:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = DarkSlate
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        15 to "15 min",
                        60 to "1 Hour",
                        480 to "8 Hours"
                    ).forEach { (mins, label) ->
                        val active = selectedMinutes == mins
                        Surface(
                            onClick = { selectedMinutes = mins },
                            shape = RoundedCornerShape(12.dp),
                            color = if (active) PrimaryBlue else LightBlueSurface,
                            border = BorderStroke(1.dp, PrimaryBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (active) Color.White else PrimaryBlue
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel", color = PrimaryBlue)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSelectDuration(selectedMinutes, placeLabel.ifBlank { "Campus Study Lounge" })
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.testTag("confirm_live_location_button")
                    ) {
                        Text("Share Live", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatGroupEventCard(
    payload: GroupEventPayload,
    onRsvp: (String) -> Unit
) {
    val myStatus = payload.rsvpByUser["user_self"] ?: ""
    val goingCount = payload.rsvpByUser.values.count { it == "GOING" }
    val maybeCount = payload.rsvpByUser.values.count { it == "MAYBE" }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.2.dp, PrimaryBlue),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("group_event_card_${payload.eventId}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = LightBlueSurface,
                    border = BorderStroke(1.dp, BorderBlue),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = "Group Event",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = payload.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = PrimaryBlue
                    )
                    Text(
                        text = "🗓 ${payload.dateTimeText}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = SecondaryBlue
                    )
                    if (payload.location.isNotBlank()) {
                        Text(
                            text = "📍 ${payload.location}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedGray
                        )
                    }
                }
            }

            if (payload.description.isNotBlank()) {
                Text(
                    text = payload.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkSlate
                )
            }

            Text(
                text = "$goingCount Going · $maybeCount Maybe",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = PrimaryBlue
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "GOING" to "Going",
                    "MAYBE" to "Maybe",
                    "NOT_GOING" to "Can't Go"
                ).forEach { (key, label) ->
                    val active = myStatus == key
                    Surface(
                        onClick = { onRsvp(key) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (active) PrimaryBlue else LightBlueSurface,
                        border = BorderStroke(1.dp, PrimaryBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (active) Color.White else PrimaryBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateGroupEventDialog(
    onDismiss: () -> Unit,
    onCreateEvent: (title: String, dateTime: String, location: String, description: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("Saturday · 4:00 PM") }
    var location by remember { mutableStateOf("Online / Study Room 204") }
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.5.dp, PrimaryBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Study Event",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = PrimaryBlue
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = PrimaryBlue)
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Name (e.g., Linear Algebra Mock Exam)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_title_input")
                )
                OutlinedTextField(
                    value = dateTime,
                    onValueChange = { dateTime = it },
                    label = { Text("Date & Time") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location or Meeting Link") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Topics") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel", color = PrimaryBlue)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onCreateEvent(title.trim(), dateTime.trim(), location.trim(), description.trim())
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.testTag("create_event_confirm_button")
                    ) {
                        Text("Create Event", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ContactAndGroupInfoSheet(
    conversation: ConversationEntity,
    members: List<ConversationMemberEntity>,
    messages: List<MessageEntity>,
    attachments: Map<String, AttachmentEntity>,
    myDeviceId: String,
    onDismiss: () -> Unit,
    onSetDisappearingHours: (Int) -> Unit,
    onToggleChatLock: () -> Unit,
    onToggleOnlyAdminsCanSend: () -> Unit,
    onResetInviteLink: () -> Unit,
    onCopyInviteLink: (String) -> Unit,
    onAddGroupMember: (String) -> Unit,
    onToggleMemberRole: (ConversationMemberEntity) -> Unit,
    onRemoveMember: (ConversationMemberEntity) -> Unit,
    onToggleBlock: () -> Unit,
    onReportContact: () -> Unit
) {
    var selectedGalleryTab by remember { mutableStateOf("MEDIA") } // "MEDIA", "DOCS", "LINKS"
    var newMemberName by remember { mutableStateOf("") }
    var showSecurityCode by remember { mutableStateOf(false) }

    val isGroup = conversation.conversationType == ConversationType.GROUP.name ||
        conversation.conversationType == ConversationType.BROADCAST.name

    // Generate deterministic 60-digit End-to-End Verification Code for this peer
    val verificationDigits = remember(conversation.id, myDeviceId) {
        val seed = (conversation.id + myDeviceId).hashCode().toLong()
        buildString {
            var cur = kotlin.math.abs(seed) + 135792468L
            repeat(12) { idx ->
                val block = ((cur * (idx + 31)) % 100000L).toString().padStart(5, '0')
                append(block)
                if (idx % 4 == 3 && idx < 11) append("\n") else if (idx < 11) append("  ")
                cur = (cur * 6364136223846793005L + 1442695040888963407L) and Long.MAX_VALUE
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.5.dp, PrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .testTag("contact_group_info_sheet")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(LightBlueSurface)
                                .border(1.5.dp, PrimaryBlue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = conversation.avatarInitials,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = PrimaryBlue
                            )
                        }
                        Column {
                            Text(
                                text = conversation.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = DarkSlate
                            )
                            Text(
                                text = if (isGroup) "${members.size} participants · ${conversation.subtitle}" else conversation.lastSeenText,
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryBlue
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = PrimaryBlue)
                    }
                }

                HorizontalDivider(color = BorderBlue)

                // 1. Media, Docs & Links Gallery
                Text(
                    text = "Media, Links, and Docs",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = PrimaryBlue
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "MEDIA" to "Media",
                        "DOCS" to "Docs",
                        "LINKS" to "Links"
                    ).forEach { (tabKey, label) ->
                        val active = selectedGalleryTab == tabKey
                        Surface(
                            onClick = { selectedGalleryTab = tabKey },
                            shape = RoundedCornerShape(14.dp),
                            color = if (active) PrimaryBlue else LightBlueSurface
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (active) Color.White else PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                val filteredGalleryMessages = remember(messages, selectedGalleryTab) {
                    messages.filter { msg ->
                        when (selectedGalleryTab) {
                            "MEDIA" -> msg.messageType == ChatMessageType.IMAGE.name || msg.messageType == ChatMessageType.VIDEO.name
                            "DOCS" -> msg.messageType == ChatMessageType.PDF.name || msg.messageType == ChatMessageType.DOCUMENT.name || msg.messageType == ChatMessageType.FILE_ATTACHMENT.name
                            "LINKS" -> !msg.linkPreviewUrl.isNullOrBlank() || msg.content.contains("http", ignoreCase = true)
                            else -> false
                        }
                    }
                }

                if (filteredGalleryMessages.isEmpty()) {
                    Text(
                        text = "No ${selectedGalleryTab.lowercase()} shared in this chat yet.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedGray
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        filteredGalleryMessages.take(4).forEach { item ->
                            val att = item.attachmentId?.let { attachments[it] }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = LightBlueSurface.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = att?.fileName ?: item.linkPreviewUrl ?: item.content.take(48),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = PrimaryBlue,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = BorderBlue)

                // 2. Disappearing Messages Timer (Off, 24h, 7d, 90d)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.Timer, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Disappearing Messages",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = PrimaryBlue
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        0 to "Off",
                        24 to "24h",
                        168 to "7d",
                        2160 to "90d"
                    ).forEach { (hours, label) ->
                        val active = conversation.disappearingHours == hours
                        Surface(
                            onClick = { onSetDisappearingHours(hours) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) PrimaryBlue else LightBlueSurface,
                            border = BorderStroke(1.dp, PrimaryBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (active) Color.White else PrimaryBlue
                                )
                            }
                        }
                    }
                }

                // 3. Chat Lock (PIN Protection) & 60-Digit Encryption Verification
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        Column {
                            Text(
                                text = "Chat Lock (PIN Protected)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = DarkSlate
                            )
                            Text(
                                text = "Keep this chat locked in Locked Chats folder",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedGray
                            )
                        }
                    }
                    Switch(
                        checked = conversation.isLocked,
                        onCheckedChange = { onToggleChatLock() },
                        modifier = Modifier.testTag("toggle_chat_lock_switch")
                    )
                }

                Surface(
                    onClick = { showSecurityCode = !showSecurityCode },
                    shape = RoundedCornerShape(10.dp),
                    color = LightBlueSurface,
                    border = BorderStroke(1.dp, BorderBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Security, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Encryption · Tap to verify 60-digit security code",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryBlue
                            )
                        }
                        if (showSecurityCode) {
                            Text(
                                text = verificationDigits,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = DarkSlate
                            )
                        }
                    }
                }

                // 4. Group Admin & Member Controls (for Study Groups)
                if (isGroup) {
                    HorizontalDivider(color = BorderBlue)
                    Text(
                        text = "Group Admin & Permissions",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = PrimaryBlue
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Only admins can send messages",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = DarkSlate
                            )
                            Text(
                                text = "Announcement mode for class updates",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedGray
                            )
                        }
                        Switch(
                            checked = conversation.onlyAdminsCanSend,
                            onCheckedChange = { onToggleOnlyAdminsCanSend() },
                            modifier = Modifier.testTag("toggle_only_admins_switch")
                        )
                    }

                    // Group Invite Link
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = LightBlueSurface,
                        border = BorderStroke(1.dp, BorderBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Invite via Group Link",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryBlue
                                )
                                Text(
                                    text = conversation.inviteLink ?: "mathgate://join/${conversation.id}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = SecondaryBlue,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = { onCopyInviteLink(conversation.inviteLink ?: "mathgate://join/${conversation.id}") },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Link", tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = onResetInviteLink,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Reset Link", tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Add participant
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newMemberName,
                            onValueChange = { newMemberName = it },
                            placeholder = { Text("Add member name...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (newMemberName.isNotBlank()) {
                                    onAddGroupMember(newMemberName.trim())
                                    newMemberName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Filled.PersonAdd, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Member List with Promote/Demote Admin & Remove
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        members.forEach { member ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, BorderBlue.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = member.displayName,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = DarkSlate
                                        )
                                        Text(
                                            text = member.role,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (member.role == "ADMIN") PrimaryBlue else MutedGray
                                        )
                                    }
                                    if (member.userId != "user_self") {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = { onToggleMemberRole(member) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.AdminPanelSettings,
                                                    contentDescription = "Toggle Admin",
                                                    tint = if (member.role == "ADMIN") PrimaryBlue else MutedGray,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { onRemoveMember(member) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.PersonRemove,
                                                    contentDescription = "Remove Member",
                                                    tint = AlertRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = BorderBlue)

                // 5. Block & Report Contact Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onToggleBlock,
                        border = BorderStroke(1.dp, AlertRed),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("block_contact_button")
                    ) {
                        Icon(Icons.Filled.Block, contentDescription = null, tint = AlertRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (conversation.isBlocked) "Unblock" else "Block",
                            color = AlertRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedButton(
                        onClick = onReportContact,
                        border = BorderStroke(1.dp, AlertRed),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("report_contact_button")
                    ) {
                        Icon(Icons.Filled.ReportProblem, contentDescription = null, tint = AlertRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Report",
                            color = AlertRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusUpdatesHeaderDrawerDialog(
    users: List<UserEntity>,
    myStatusText: String,
    onUpdateMyStatus: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var draftStatus by remember { mutableStateOf(myStatusText) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.5.dp, PrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .testTag("status_updates_drawer_dialog")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.HistoryToggleOff, contentDescription = null, tint = PrimaryBlue)
                        Text(
                            text = "Status Updates (24h)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = PrimaryBlue
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = PrimaryBlue)
                    }
                }

                // My Status Composer
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LightBlueSurface,
                    border = BorderStroke(1.dp, BorderBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "My Status",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = PrimaryBlue
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = draftStatus,
                                onValueChange = { draftStatus = it },
                                placeholder = { Text("Share a formula or study update...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (draftStatus.isNotBlank()) {
                                        onUpdateMyStatus(draftStatus.trim())
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Text("Post", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Text(
                    text = "Recent Updates from Contacts",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = SecondaryBlue
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(users.filter { it.id != "user_self" }, key = { it.id }) { user ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, BorderBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(LightBlueSurface)
                                        .border(2.5.dp, PrimaryBlue, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.avatarInitials,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = PrimaryBlue
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.displayName,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = DarkSlate
                                    )
                                    Text(
                                        text = user.statusText.ifBlank { user.roleOrBranch },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PrimaryBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CallLogsHeaderDrawerDialog(
    conversations: List<ConversationEntity>,
    onStartCallWith: (ConversationEntity, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.5.dp, PrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp)
                .testTag("calls_drawer_dialog")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = null, tint = PrimaryBlue)
                        Text(
                            text = "Voice & Video Study Calls",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = PrimaryBlue
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = PrimaryBlue)
                    }
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(conversations.filter { it.conversationType != ConversationType.SELF.name }, key = { it.id }) { conv ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, BorderBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = conv.title,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = DarkSlate
                                    )
                                    Text(
                                        text = "Encrypted Peer-to-Peer Study Call · ${conv.linkedVia}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SecondaryBlue
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = { onStartCallWith(conv, false) }) {
                                        Icon(Icons.Filled.Call, contentDescription = "Audio Call", tint = PrimaryBlue)
                                    }
                                    IconButton(onClick = { onStartCallWith(conv, true) }) {
                                        Icon(Icons.Filled.Videocam, contentDescription = "Video Call", tint = PrimaryBlue)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatLockPinDialog(
    expectedPin: String,
    onUnlocked: () -> Unit,
    onDismiss: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.5.dp, PrimaryBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = PrimaryBlue)
                    Text(
                        text = "Unlock Locked Chats",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = PrimaryBlue
                    )
                }
                Text(
                    text = "Enter your 4-digit Chat Lock PIN (default: $expectedPin)",
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryBlue
                )
                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = {
                        enteredPin = it.take(6)
                        showError = false
                    },
                    label = { Text("4-Digit PIN") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_lock_pin_input")
                )
                if (showError) {
                    Text(
                        text = "Incorrect PIN. Try $expectedPin",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AlertRed
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel", color = PrimaryBlue)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (enteredPin.trim() == expectedPin) {
                                onUnlocked()
                                onDismiss()
                            } else {
                                showError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.testTag("unlock_chats_confirm_button")
                    ) {
                        Text("Unlock", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
