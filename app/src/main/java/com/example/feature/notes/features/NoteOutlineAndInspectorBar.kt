package com.example.feature.notes.features

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.NoteBlockEntity
import com.example.core.document.BlockType
import kotlin.math.max

private val BlueprintPrimary = Color(0xFF003DA5)
private val BlueprintSecondary = Color(0xFF1647BB)
private val BlueprintSurface = Color(0xFFEAF1FF)

data class NoteDocumentMetrics(
    val wordCount: Int,
    val charCount: Int,
    val readingTimeMinutes: Int,
    val equationCount: Int,
    val codeBlockCount: Int,
    val tableCount: Int,
    val checklistCompleted: Int,
    val checklistTotal: Int
)

object NoteAnalyticsCalculator {
    fun compute(blocks: List<NoteBlockEntity>): NoteDocumentMetrics {
        val fullText = blocks.joinToString(" ") { it.content }
        val words = fullText.trim().split(Regex("\\s+")).count { it.isNotEmpty() }
        val chars = fullText.length
        val readMin = max(1, (words + 179) / 180)
        val eqs = blocks.count { it.type == BlockType.LATEX_BLOCK.name || it.type == BlockType.INLINE_MATH.name }
        val codes = blocks.count { it.type == BlockType.CODE_BLOCK.name }
        val tables = blocks.count { it.type == BlockType.TABLE.name }

        var done = 0
        var total = 0
        blocks.filter { it.type == BlockType.CHECKLIST.name }.forEach { blk ->
            blk.content.lines().filter { it.isNotBlank() }.forEach { line ->
                total++
                if (line.trim().startsWith("[x]", ignoreCase = true)) {
                    done++
                }
            }
        }
        return NoteDocumentMetrics(
            wordCount = words,
            charCount = chars,
            readingTimeMinutes = readMin,
            equationCount = eqs,
            codeBlockCount = codes,
            tableCount = tables,
            checklistCompleted = done,
            checklistTotal = total
        )
    }
}

@Composable
fun NoteTelemetryAndPowerBar(
    blocks: List<NoteBlockEntity>,
    onOpenSlashPalette: () -> Unit,
    onOpenOutline: () -> Unit,
    onOpenFindReplace: () -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenLatexPad: () -> Unit,
    onOpenGraphView: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics = remember(blocks) { NoteAnalyticsCalculator.compute(blocks) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("notes_telemetry_power_bar"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BlueprintSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Live Document Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${metrics.wordCount} words • ${metrics.charCount} chars • ~${metrics.readingTimeMinutes} min read",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintPrimary,
                    modifier = Modifier.testTag("notes_word_count_badge")
                )
                Text(
                    text = "∑ ${metrics.equationCount} Equations",
                    style = MaterialTheme.typography.labelSmall,
                    color = BlueprintSecondary
                )
                Text(
                    text = "</> ${metrics.codeBlockCount} Code",
                    style = MaterialTheme.typography.labelSmall,
                    color = BlueprintSecondary
                )
                Text(
                    text = "⊞ ${metrics.tableCount} Tables",
                    style = MaterialTheme.typography.labelSmall,
                    color = BlueprintSecondary
                )
                if (metrics.checklistTotal > 0) {
                    Text(
                        text = "☑ ${metrics.checklistCompleted}/${metrics.checklistTotal} Tasks",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintPrimary
                    )
                }
            }

            if (metrics.checklistTotal > 0) {
                LinearProgressIndicator(
                    progress = { metrics.checklistCompleted.toFloat() / metrics.checklistTotal.toFloat() },
                    color = BlueprintPrimary,
                    trackColor = BlueprintSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                )
            }

            // Power Tools Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpenSlashPalette,
                    colors = ButtonDefaults.buttonColors(containerColor = BlueprintPrimary),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("slash_command_button")
                ) {
                    Text("/ COMMANDS", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = onOpenLatexPad,
                    border = BorderStroke(1.dp, BlueprintPrimary),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("notes_latex_pad_button")
                ) {
                    Text("∑ LATEX PAD", fontSize = 11.sp, color = BlueprintPrimary)
                }
                OutlinedButton(
                    onClick = onOpenOutline,
                    border = BorderStroke(1.dp, BlueprintPrimary),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("notes_outline_button")
                ) {
                    Text("≡ OUTLINE (TOC)", fontSize = 11.sp, color = BlueprintPrimary)
                }
                OutlinedButton(
                    onClick = onOpenFlashcards,
                    border = BorderStroke(1.dp, BlueprintPrimary),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("notes_flashcards_button")
                ) {
                    Text("⚡ FLASHCARDS", fontSize = 11.sp, color = BlueprintPrimary)
                }
                OutlinedButton(
                    onClick = onOpenFindReplace,
                    border = BorderStroke(1.dp, BlueprintPrimary),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("notes_find_replace_button")
                ) {
                    Text("🔍 FIND & REPLACE", fontSize = 11.sp, color = BlueprintPrimary)
                }
                OutlinedButton(
                    onClick = onOpenGraphView,
                    border = BorderStroke(1.dp, BlueprintPrimary),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("notes_graph_view_button")
                ) {
                    Text("◉ KNOWLEDGE GRAPH", fontSize = 11.sp, color = BlueprintPrimary)
                }
            }
        }
    }
}

@Composable
fun NoteOutlineDialog(
    noteTitle: String,
    blocks: List<NoteBlockEntity>,
    onDismiss: () -> Unit
) {
    val structureBlocks = remember(blocks) {
        blocks.filter {
            it.type == BlockType.HEADING_1.name ||
                it.type == BlockType.HEADING_2.name ||
                it.type == BlockType.HEADING_3.name ||
                it.type == BlockType.LATEX_BLOCK.name ||
                it.type == BlockType.TABLE.name
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Column {
                Text(
                    text = "DOCUMENT TABLE OF CONTENTS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = BlueprintPrimary
                )
                Text(
                    text = "$noteTitle • ${structureBlocks.size} structural nodes",
                    style = MaterialTheme.typography.labelSmall,
                    color = BlueprintSecondary
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .testTag("notes_outline_list"),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(structureBlocks, key = { _, b -> b.id }) { idx, blk ->
                    val type = BlockType.fromName(blk.type)
                    val indent = when (type) {
                        BlockType.HEADING_1 -> 0.dp
                        BlockType.HEADING_2 -> 12.dp
                        BlockType.HEADING_3 -> 24.dp
                        else -> 16.dp
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = indent)
                            .background(BlueprintSurface, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${idx + 1}. [${type.badge}] ${blk.content.take(48)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (type == BlockType.HEADING_1) FontWeight.ExtraBold else FontWeight.Medium,
                            color = BlueprintPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "#${blk.position + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = BlueprintSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = BlueprintPrimary)
            }
        }
    )
}

@Composable
fun NoteFindAndReplaceDialog(
    blocks: List<NoteBlockEntity>,
    onReplaceAll: (String, String, Boolean) -> Int,
    onDismiss: () -> Unit
) {
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    var ignoreCase by remember { mutableStateOf(true) }
    var feedback by remember { mutableStateOf<String?>(null) }

    val matchCount = remember(blocks, findText, ignoreCase) {
        if (findText.isEmpty()) 0
        else blocks.sumOf { blk ->
            Regex(Regex.escape(findText), if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet())
                .findAll(blk.content).count()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "IN-NOTE FIND & REPLACE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = BlueprintPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = findText,
                    onValueChange = { findText = it },
                    label = { Text("Find text or LaTeX token") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("find_text_input")
                )
                OutlinedTextField(
                    value = replaceText,
                    onValueChange = { replaceText = it },
                    label = { Text("Replace with") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("replace_text_input")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Matches across active blocks: $matchCount",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintPrimary
                    )
                    TextButton(onClick = { ignoreCase = !ignoreCase }) {
                        Text(if (ignoreCase) "Case-Insensitive" else "Case-Sensitive", fontSize = 11.sp)
                    }
                }
                feedback?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintSecondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (findText.isNotEmpty()) {
                        val updatedBlocks = onReplaceAll(findText, replaceText, ignoreCase)
                        feedback = "Updated $updatedBlocks block(s)."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlueprintPrimary),
                modifier = Modifier.testTag("confirm_replace_all_button")
            ) {
                Text("REPLACE ALL ($matchCount)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = BlueprintPrimary)
            }
        }
    )
}
