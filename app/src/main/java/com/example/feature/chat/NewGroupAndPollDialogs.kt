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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.core.database.ChatMessageType
import com.example.core.database.ChatSettingsEntity
import com.example.core.database.NoteEntity
import com.example.domain.model.MathQuestion

private val PrimaryBlue = Color(0xFF003DA5)
private val SecondaryBlue = Color(0xFF1647BB)
private val LightBlueSurface = Color(0xFFEAF1FF)
private val BorderBlue = Color(0xFFBFD4FF)

@Composable
fun CreateStudyGroupOrBroadcastDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, description: String, isBroadcast: Boolean, membersCsv: String) -> Unit
) {
    var title by remember { mutableStateOf("GATE 2027 Power & Control Cohort") }
    var description by remember { mutableStateOf("Daily Laplace, Bode & State-Space problem solving") }
    var isBroadcast by remember { mutableStateOf(false) }
    var membersCsv by remember { mutableStateOf("Arjun Sharma, Priya Nair, Vikram Rao") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = BorderStroke(2.dp, PrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_group_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        Icon(Icons.Filled.Groups, contentDescription = null, tint = PrimaryBlue)
                        Text(
                            text = if (isBroadcast) "NEW BROADCAST LIST" else "NEW STUDY GROUP",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = PrimaryBlue
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = PrimaryBlue)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        onClick = { isBroadcast = false },
                        shape = RoundedCornerShape(8.dp),
                        color = if (!isBroadcast) PrimaryBlue else LightBlueSurface,
                        border = BorderStroke(1.dp, PrimaryBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "STUDY GROUP",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (!isBroadcast) Color.White else PrimaryBlue
                            )
                        }
                    }
                    Surface(
                        onClick = { isBroadcast = true },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isBroadcast) PrimaryBlue else LightBlueSurface,
                        border = BorderStroke(1.dp, PrimaryBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "BROADCAST",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isBroadcast) Color.White else PrimaryBlue
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Group / Broadcast Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("group_title_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Pinned Announcement") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = membersCsv,
                    onValueChange = { membersCsv = it },
                    label = { Text("Members (comma-separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    onClick = {
                        if (title.isNotBlank()) {
                            onCreate(title, description, isBroadcast, membersCsv)
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryBlue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_create_group_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isBroadcast) "CREATE BROADCAST LIST" else "CREATE STUDY GROUP",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreatePollDialog(
    onDismiss: () -> Unit,
    onCreatePoll: (question: String, options: List<String>, allowMultiple: Boolean, isAnonymous: Boolean) -> Unit
) {
    var question by remember { mutableStateOf("Which topic should we practice next?") }
    var opt1 by remember { mutableStateOf("Laplace & Transfer Functions") }
    var opt2 by remember { mutableStateOf("Digital Logic & K-Maps") }
    var opt3 by remember { mutableStateOf("Graph Algorithms & DP") }
    var opt4 by remember { mutableStateOf("Linear Algebra & Eigenvalues") }
    var allowMultiple by remember { mutableStateOf(false) }
    var isAnonymous by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = BorderStroke(2.dp, PrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_poll_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        Icon(Icons.Filled.Poll, contentDescription = null, tint = PrimaryBlue)
                        Text(
                            text = "CREATE STUDY GROUP POLL",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = PrimaryBlue
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = PrimaryBlue)
                    }
                }

                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Poll Question") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("poll_question_input")
                )

                OutlinedTextField(value = opt1, onValueChange = { opt1 = it }, label = { Text("Option 1") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = opt2, onValueChange = { opt2 = it }, label = { Text("Option 2") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = opt3, onValueChange = { opt3 = it }, label = { Text("Option 3") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = opt4, onValueChange = { opt4 = it }, label = { Text("Option 4 (Optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = allowMultiple, onCheckedChange = { allowMultiple = it })
                    Text("Allow multiple selections", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isAnonymous, onCheckedChange = { isAnonymous = it })
                    Text("Anonymous voting", style = MaterialTheme.typography.bodySmall)
                }

                Surface(
                    onClick = {
                        val list = listOf(opt1, opt2, opt3, opt4).filter { it.isNotBlank() }
                        if (question.isNotBlank() && list.size >= 2) {
                            onCreatePoll(question, list, allowMultiple, isAnonymous)
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryBlue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_create_poll_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "POST POLL TO CONVERSATION",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EducationalAttachmentDialog(
    currentQuestion: MathQuestion?,
    notes: List<NoteEntity>,
    onDismiss: () -> Unit,
    onShareCurrentQuestion: () -> Unit,
    onSendLatexEquation: (caption: String, latex: String) -> Unit,
    onSendCodeSnippet: (caption: String, language: String, code: String) -> Unit,
    onShareNote: (NoteEntity) -> Unit,
    onOpenPollCreator: () -> Unit,
    onPickPhotoOrVideo: () -> Unit,
    onPickDocumentOrPdf: () -> Unit,
    onSendLocationWithConsent: () -> Unit,
    onSendContactCard: (name: String, role: String, otp: String) -> Unit = { _, _, _ -> },
    onSendAudioFile: () -> Unit = {}
) {
    var mode by remember { mutableStateOf("MENU") } // "MENU", "LATEX", "CODE", "NOTES", "CONTACT"
    var latexInput by remember { mutableStateOf("H(s) = \\frac{Y(s)}{X(s)} = \\frac{1}{s^2 + 2\\zeta\\omega_n s + \\omega_n^2}") }
    var codeLang by remember { mutableStateOf("Kotlin") }
    var codeInput by remember { mutableStateOf("fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)") }
    var contactName by remember { mutableStateOf("Dr. Ananya Verma") }
    var contactBranch by remember { mutableStateOf("ECE · Signal Processing Mentor") }
    var contactOtp by remember { mutableStateOf("849-204") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = BorderStroke(2.dp, PrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("educational_attachment_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WHATSAPP-STYLE CHAT ATTACHMENTS",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = PrimaryBlue
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = PrimaryBlue)
                    }
                }

                when (mode) {
                    "LATEX" -> {
                        OutlinedTextField(
                            value = latexInput,
                            onValueChange = { latexInput = it },
                            label = { Text("LaTeX Mathematical Expression") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("composer_latex_input")
                        )
                        Surface(
                            onClick = {
                                if (latexInput.isNotBlank()) {
                                    onSendLatexEquation("Shared mathematical equation:", latexInput)
                                    onDismiss()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryBlue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("confirm_send_latex_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("SEND LATEX EQUATION", color = Color.White, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }

                    "CODE" -> {
                        OutlinedTextField(
                            value = codeLang,
                            onValueChange = { codeLang = it },
                            label = { Text("Language (Kotlin, Python, C, Verilog, MATLAB)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = codeInput,
                            onValueChange = { codeInput = it },
                            label = { Text("Source Code") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Surface(
                            onClick = {
                                if (codeInput.isNotBlank()) {
                                    onSendCodeSnippet("Shared $codeLang code snippet:", codeLang, codeInput)
                                    onDismiss()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryBlue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("SEND CODE SNIPPET", color = Color.White, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }

                    "NOTES" -> {
                        Text(
                            text = "Select a Note from Notes Studio to share:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryBlue
                        )
                        if (notes.isEmpty()) {
                            Text(
                                text = "No notes found. Tap below to share a template study note.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            notes.take(6).forEach { note ->
                                Surface(
                                    onClick = {
                                        onShareNote(note)
                                        onDismiss()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = LightBlueSurface,
                                    border = BorderStroke(1.dp, BorderBlue),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(note.title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
                                        Text(note.subjectTag, style = MaterialTheme.typography.labelSmall, color = SecondaryBlue)
                                    }
                                }
                            }
                        }
                    }

                    "CONTACT" -> {
                        OutlinedTextField(
                            value = contactName,
                            onValueChange = { contactName = it },
                            label = { Text("Contact Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = contactBranch,
                            onValueChange = { contactBranch = it },
                            label = { Text("Branch / Role") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = contactOtp,
                            onValueChange = { contactOtp = it },
                            label = { Text("Peer Pairing OTP Code") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Surface(
                            onClick = {
                                if (contactName.isNotBlank()) {
                                    onSendContactCard(contactName.trim(), contactBranch.trim(), contactOtp.trim())
                                    onDismiss()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryBlue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("SHARE CONTACT CARD", color = Color.White, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }

                    else -> {
                        val actions = listOf(
                            Triple("📄 Document / PDF / Office", "Share PDFs, DOCX, XLSX, PPTX, or ZIP archives", "DOC"),
                            Triple("🖼️ Photos & Videos (Multi-Select)", "Pick single or multiple photos/videos with preview & caption", "PHOTO"),
                            Triple("🎧 Audio File", "Share lecture recording or audio track (.mp3 / .m4a)", "AUDIO"),
                            Triple("👤 Contact Card", "Share a study peer contact card with OTP code", "CONTACT"),
                            Triple("📝 Notes Studio Document", "Share a Notion-style structured note into chat", "NOTES"),
                            Triple("❓ Practice Question Card", "Share an interactive GATE question with options & solution", "QUESTION"),
                            Triple("📊 Group Poll", "Create single or multi-choice poll in chat", "POLL"),
                            Triple("∑ Mathematical Formula", "Send formatted LaTeX equation bubble", "LATEX"),
                            Triple("💻 Code Block", "Send syntax-formatted code snippet", "CODE"),
                            Triple("📍 Location & Live Location", "Share current or timed live location (15m / 1h / 8h)", "LOC")
                        )
                        actions.forEach { (title, subtitle, key) ->
                            Surface(
                                onClick = {
                                    when (key) {
                                        "LATEX" -> mode = "LATEX"
                                        "CODE" -> mode = "CODE"
                                        "NOTES" -> mode = "NOTES"
                                        "CONTACT" -> mode = "CONTACT"
                                        "QUESTION" -> {
                                            onShareCurrentQuestion()
                                            onDismiss()
                                        }
                                        "AUDIO" -> {
                                            onSendAudioFile()
                                            onDismiss()
                                        }
                                        "POLL" -> {
                                            onDismiss()
                                            onOpenPollCreator()
                                        }
                                        "PHOTO" -> {
                                            onDismiss()
                                            onPickPhotoOrVideo()
                                        }
                                        "DOC" -> {
                                            onDismiss()
                                            onPickDocumentOrPdf()
                                        }
                                        "LOC" -> {
                                            onSendLocationWithConsent()
                                            onDismiss()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = LightBlueSurface,
                                border = BorderStroke(1.dp, BorderBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("attach_option_$key")
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Text(title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
                                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color(0xFF475569))
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
fun ChatPrivacyAndCustomizationDialog(
    settings: ChatSettingsEntity,
    onDismiss: () -> Unit,
    onUpdateSettings: ((ChatSettingsEntity) -> ChatSettingsEntity) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = BorderStroke(2.dp, PrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("chat_privacy_settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        Icon(Icons.Filled.Security, contentDescription = null, tint = PrimaryBlue)
                        Text(
                            text = "CHAT PRIVACY & APPEARANCE",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = PrimaryBlue
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = PrimaryBlue)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(LightBlueSurface)
                        .border(1.dp, BorderBlue, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Architecture Mode: Local-Only SQLite / Room Storage. Messages are stored on-device and do not claim end-to-end remote encryption or fake live server delivery.",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryBlue
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Read Receipts", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        Text("Record read state when opening conversations", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                    }
                    Switch(
                        checked = settings.readReceiptsEnabled,
                        onCheckedChange = { enabled -> onUpdateSettings { it.copy(readReceiptsEnabled = enabled) } }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Share Online Visibility", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        Text("Disabled by default in Local-Only mode", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                    }
                    Switch(
                        checked = settings.onlineVisibilityEnabled,
                        onCheckedChange = { enabled -> onUpdateSettings { it.copy(onlineVisibilityEnabled = enabled) } }
                    )
                }

                HorizontalDivider(color = BorderBlue)

                Text("Chat Surface Pattern", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("WHITE_BLUEPRINT" to "Pure White", "LIGHT_GRID" to "Tech Grid", "SOFT_BLUE" to "Blueprint Tint").forEach { (id, label) ->
                        val active = settings.wallpaperId == id
                        Surface(
                            onClick = { onUpdateSettings { it.copy(wallpaperId = id) } },
                            shape = RoundedCornerShape(6.dp),
                            color = if (active) PrimaryBlue else LightBlueSurface,
                            border = BorderStroke(1.dp, PrimaryBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (active) Color.White else PrimaryBlue
                                )
                            }
                        }
                    }
                }

                Text("Message Bubble Style", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("BLUEPRINT_SHARP" to "Technical Blueprint", "ROUNDED_MODERN" to "Modern Rounded").forEach { (id, label) ->
                        val active = settings.bubbleStyle == id
                        Surface(
                            onClick = { onUpdateSettings { it.copy(bubbleStyle = id) } },
                            shape = RoundedCornerShape(6.dp),
                            color = if (active) PrimaryBlue else LightBlueSurface,
                            border = BorderStroke(1.dp, PrimaryBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    label,
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
}
