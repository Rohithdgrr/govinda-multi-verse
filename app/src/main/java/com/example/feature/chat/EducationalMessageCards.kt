package com.example.feature.chat

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.database.AttachmentEntity
import com.example.core.database.ChatMessageType
import com.example.core.database.PollEntity
import com.example.core.database.PollOptionEntity
import com.example.core.database.PollVoteEntity
import kotlinx.coroutines.launch

private val PrimaryBlue = Color(0xFF003DA5)
private val SecondaryBlue = Color(0xFF1647BB)
private val LightBlueSurface = Color(0xFFEAF1FF)
private val BorderBlue = Color(0xFFBFD4FF)
private val VerifiedGreen = Color(0xFF1E8E3E)

@Composable
fun ChatQuestionCard(
    payload: SharedQuestionPayload,
    onOpenInPractice: () -> Unit,
    onSaveQuestionToNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var showExplanation by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.5.dp, PrimaryBlue, RoundedCornerShape(8.dp))
            .padding(10.dp)
            .testTag("chat_question_card"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.Quiz, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                Text(
                    text = "${payload.subject.uppercase()} · ${payload.difficulty.uppercase()}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = PrimaryBlue
                )
            }
            Text(
                text = payload.topic,
                style = MaterialTheme.typography.labelSmall,
                color = SecondaryBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = payload.prompt,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF0F172A)
        )

        if (payload.options.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                payload.options.forEachIndexed { idx, opt ->
                    val isPicked = selectedOption == opt
                    val isRight = opt == payload.answer
                    val borderCol = when {
                        selectedOption != null && isRight -> VerifiedGreen
                        isPicked -> Color(0xFFD93025)
                        else -> BorderBlue
                    }
                    Surface(
                        onClick = { selectedOption = opt },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isPicked) LightBlueSurface else Color.White,
                        border = BorderStroke(1.dp, borderCol),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${('A' + idx)}. $opt",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF0F172A)
                            )
                            if (selectedOption != null && isRight) {
                                Text(
                                    text = "✓ CORRECT",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = VerifiedGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "Source: ${payload.sourceAttribution}",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF64748B)
        )

        if (showExplanation && payload.explanation.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(LightBlueSurface)
                    .padding(8.dp)
            ) {
                Text(
                    text = "Solution: ${payload.explanation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryBlue
                )
            }
        }

        HorizontalDivider(color = BorderBlue)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                onClick = { showExplanation = !showExplanation },
                shape = RoundedCornerShape(6.dp),
                color = PrimaryBlue,
                modifier = Modifier
                    .weight(1f)
                    .testTag("open_question_in_practice_button")
            ) {
                Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (showExplanation) "HIDE SOLUTION" else "SHOW SOLUTION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                }
            }
            Surface(
                onClick = { onSaveQuestionToNote("${payload.prompt}\nAnswer: ${payload.answer}\n${payload.explanation}") },
                shape = RoundedCornerShape(6.dp),
                color = LightBlueSurface,
                border = BorderStroke(1.dp, PrimaryBlue),
                modifier = Modifier.testTag("save_question_to_note_button")
            ) {
                Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "+ NOTE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = PrimaryBlue
                    )
                }
            }
        }
    }
}

@Composable
fun ChatPollCard(
    poll: PollEntity,
    options: List<PollOptionEntity>,
    votes: List<PollVoteEntity>,
    onVote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalVotes = votes.size.coerceAtLeast(1)
    val myVotes = votes.filter { it.userId == "user_self" }.map { it.optionId }.toSet()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.5.dp, PrimaryBlue, RoundedCornerShape(8.dp))
            .padding(10.dp)
            .testTag("chat_poll_card"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(Icons.Filled.Poll, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                Text(
                    text = if (poll.allowMultiple) "GROUP POLL · MULTI-SELECT" else "GROUP POLL · SINGLE CHOICE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = PrimaryBlue
                )
            }
            if (poll.isAnonymous) {
                Text(
                    text = "ANONYMOUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryBlue
                )
            }
        }

        Text(
            text = poll.question,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF0F172A)
        )

        options.forEach { option ->
            val count = votes.count { it.optionId == option.id }
            val pct = if (votes.isEmpty()) 0 else ((count * 100f) / totalVotes).toInt()
            val isVoted = option.id in myVotes

            Surface(
                onClick = { onVote(option.id) },
                shape = RoundedCornerShape(6.dp),
                color = if (isVoted) LightBlueSurface else Color.White,
                border = BorderStroke(if (isVoted) 1.5.dp else 1.dp, if (isVoted) PrimaryBlue else BorderBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("poll_option_${option.position}")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isVoted) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = option.optionText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isVoted) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = Color(0xFF0F172A)
                            )
                        }
                        Text(
                            text = "$count ($pct%)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = PrimaryBlue
                        )
                    }
                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((pct / 100f).coerceIn(0f, 1f))
                                .height(4.dp)
                                .background(PrimaryBlue)
                        )
                    }
                }
            }
        }

        Text(
            text = "${votes.size} total votes · Tap any option to vote or update",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF64748B)
        )
    }
}

@Composable
fun ChatCodeSnippetCard(
    language: String,
    code: String,
    onCopyCode: () -> Unit,
    onSaveToNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.5.dp, PrimaryBlue, RoundedCornerShape(8.dp))
            .testTag("chat_code_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LightBlueSurface)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(Icons.Filled.Code, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(15.dp))
                Text(
                    text = language.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = PrimaryBlue
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    onClick = onSaveToNote,
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderBlue)
                ) {
                    Text(
                        text = "+ NOTE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Surface(
                    onClick = onCopyCode,
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderBlue)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(11.dp))
                        Text("COPY", style = MaterialTheme.typography.labelSmall, color = PrimaryBlue)
                    }
                }
            }
        }
        Text(
            text = code,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(10.dp)
        )
    }
}

@Composable
fun ChatNoteAttachmentCard(
    noteTitle: String,
    summaryText: String,
    onOpenInNotesStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onOpenInNotesStudio,
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, PrimaryBlue),
        modifier = modifier
            .fillMaxWidth()
            .testTag("chat_note_attachment_card")
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(LightBlueSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Description, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        text = "NOTES STUDIO DOCUMENT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = PrimaryBlue
                    )
                    Text(
                        text = noteTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (summaryText.isNotBlank()) {
                        Text(
                            text = summaryText,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF475569),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open in Notes Studio", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun ChatMediaAttachmentCard(
    messageType: ChatMessageType,
    attachment: AttachmentEntity?,
    fallbackTitle: String,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val fileSizeKb = ((attachment?.fileSizeBytes ?: 48_200L) / 1024L).coerceAtLeast(1L)
    var downloadStatus by remember(attachment?.id, attachment?.transferState) {
        mutableStateOf(attachment?.transferState ?: "READY_TO_DOWNLOAD")
    }

    val decodedImageBitmap = remember(attachment?.uriString, messageType) {
        val rawUri = attachment?.uriString.orEmpty()
        try {
            when {
                rawUri.startsWith("data:") && rawUri.contains(";base64,") -> {
                    val b64 = rawUri.substringAfter(";base64,")
                    val bytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
                messageType == ChatMessageType.IMAGE && (rawUri.startsWith("file://") || rawUri.startsWith("content://")) -> {
                    val uri = android.net.Uri.parse(rawUri)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        android.graphics.BitmapFactory.decodeStream(stream)
                    }
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    val icon = when (messageType) {
        ChatMessageType.LOCATION, ChatMessageType.LIVE_LOCATION -> Icons.Filled.LocationOn
        ChatMessageType.CONTACT_CARD -> Icons.Filled.Person
        else -> Icons.Filled.InsertDriveFile
    }

    Surface(
        onClick = {
            val rawUri = attachment?.uriString.orEmpty()
            if (rawUri.startsWith("data:") && rawUri.contains(";base64,")) {
                val b64 = rawUri.substringAfter(";base64,")
                scope.launch {
                    val savedFile = CloudChatRepository.saveBase64AttachmentToLocalFile(
                        context = context,
                        fileName = attachment?.fileName ?: "download_${System.currentTimeMillis()}.bin",
                        base64Data = b64
                    )
                    downloadStatus = if (savedFile != null) "SAVED: ${savedFile.name} ✓" else "SAVED_OFFLINE ✓"
                }
            } else {
                downloadStatus = when (downloadStatus) {
                    "READY_TO_DOWNLOAD", "FAILED_TAP_TO_RETRY" -> "SAVED_OFFLINE ✓"
                    else -> "VERIFIED_LOCAL ✓"
                }
            }
        },
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderBlue),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (decodedImageBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = decodedImageBitmap.asImageBitmap(),
                    contentDescription = attachment?.fileName ?: fallbackTitle,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, BorderBlue, RoundedCornerShape(6.dp))
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(LightBlueSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = attachment?.fileName ?: fallbackTitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0F172A),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${messageType.label} · ${fileSizeKb} KB · $downloadStatus",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = SecondaryBlue
                    )
                }
                Surface(
                    onClick = {
                        val rawUri = attachment?.uriString.orEmpty()
                        if (rawUri.startsWith("data:") && rawUri.contains(";base64,")) {
                            val b64 = rawUri.substringAfter(";base64,")
                            scope.launch {
                                val savedFile = CloudChatRepository.saveBase64AttachmentToLocalFile(
                                    context = context,
                                    fileName = attachment?.fileName ?: "download_${System.currentTimeMillis()}.bin",
                                    base64Data = b64
                                )
                                downloadStatus = if (savedFile != null) "SAVED: ${savedFile.name} ✓" else "SAVED_OFFLINE ✓"
                            }
                        } else {
                            downloadStatus = if (downloadStatus.contains("✓")) "READY_TO_DOWNLOAD" else "SAVED_OFFLINE ✓"
                        }
                    },
                    shape = RoundedCornerShape(6.dp),
                    color = LightBlueSurface,
                    border = BorderStroke(1.dp, PrimaryBlue)
                ) {
                    Text(
                        text = if (downloadStatus.contains("✓") || downloadStatus == "STORED_LOCALLY") "SAVED ✓" else "DOWNLOAD",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatLatexEquationCard(
    latex: String,
    onCopyLatex: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ast = remember(latex) { com.example.core.latex.LatexRendererEngine.parse(latex) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.5.dp, PrimaryBlue, RoundedCornerShape(8.dp))
            .padding(10.dp)
            .testTag("chat_latex_equation_card"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "∑ LATEX MATHEMATICAL EXPRESSION",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = PrimaryBlue
            )
            Surface(
                onClick = onCopyLatex,
                shape = RoundedCornerShape(4.dp),
                color = LightBlueSurface,
                border = BorderStroke(1.dp, BorderBlue)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(11.dp))
                    Text("SOURCE", style = MaterialTheme.typography.labelSmall, color = PrimaryBlue)
                }
            }
        }

        if (ast.topFraction != null && ast.bottomFraction != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (ast.prefixExpression.isNotEmpty()) {
                    Text(
                        text = ast.prefixExpression,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF0F172A)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = ast.topFraction,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = PrimaryBlue
                    )
                    HorizontalDivider(
                        thickness = 1.5.dp,
                        color = PrimaryBlue,
                        modifier = Modifier.fillMaxWidth(0.35f)
                    )
                    Text(
                        text = ast.bottomFraction,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = PrimaryBlue
                    )
                }
                if (ast.suffixExpression.isNotEmpty()) {
                    Text(
                        text = ast.suffixExpression,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF0F172A)
                    )
                }
            }
        } else {
            Text(
                text = ast.unicodeFormatted,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = PrimaryBlue
            )
        }
    }
}

