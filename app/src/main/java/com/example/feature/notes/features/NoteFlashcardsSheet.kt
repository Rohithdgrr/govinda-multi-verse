package com.example.feature.notes.features

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.database.NoteBlockEntity
import com.example.core.document.BlockType
import com.example.core.latex.LatexRendererEngine

private val BlueprintPrimary = Color(0xFF003DA5)
private val BlueprintSecondary = Color(0xFF1647BB)
private val BlueprintSurface = Color(0xFFEAF1FF)

data class NoteFlashcard(
    val id: String,
    val prompt: String,
    val answer: String,
    val badge: String
)

object NoteFlashcardGenerator {
    fun generateFromBlocks(noteTitle: String, blocks: List<NoteBlockEntity>): List<NoteFlashcard> {
        val cards = mutableListOf<NoteFlashcard>()
        val sorted = blocks.sortedBy { it.position }

        for (i in sorted.indices) {
            val blk = sorted[i]
            val type = BlockType.fromName(blk.type)
            when (type) {
                BlockType.TOGGLE_SECTION -> {
                    val parts = blk.content.split("::", limit = 2)
                    cards += NoteFlashcard(
                        id = "fc_${blk.id}",
                        prompt = parts.getOrElse(0) { blk.content },
                        answer = parts.getOrElse(1) { blk.content },
                        badge = "TOGGLE QA"
                    )
                }
                BlockType.LATEX_BLOCK -> {
                    val label = blk.metadata?.takeIf { it.isNotBlank() } ?: "Key Formula in $noteTitle"
                    cards += NoteFlashcard(
                        id = "fc_${blk.id}",
                        prompt = "State the equation for: $label",
                        answer = LatexRendererEngine.formatInlineSymbols(blk.content) + "\n\nLaTeX: ${blk.content}",
                        badge = "FORMULA RECALL"
                    )
                }
                BlockType.HEADING_1, BlockType.HEADING_2, BlockType.HEADING_3 -> {
                    val nextBlk = sorted.getOrNull(i + 1)
                    if (nextBlk != null && nextBlk.content.isNotBlank()) {
                        cards += NoteFlashcard(
                            id = "fc_${blk.id}",
                            prompt = "Explain: ${blk.content}",
                            answer = LatexRendererEngine.formatInlineSymbols(nextBlk.content),
                            badge = "CONCEPT CARD"
                        )
                    }
                }
                BlockType.CALLOUT -> {
                    cards += NoteFlashcard(
                        id = "fc_${blk.id}",
                        prompt = "What is the key engineering takeaway / callout in $noteTitle?",
                        answer = blk.content,
                        badge = "KEY CALLOUT"
                    )
                }
                else -> Unit
            }
        }

        if (cards.isEmpty()) {
            cards += NoteFlashcard(
                id = "fc_default",
                prompt = "Summarize core concepts of $noteTitle",
                answer = sorted.firstOrNull()?.content ?: "Add headings, toggle sections, or LaTeX equations to generate flashcards.",
                badge = "SUMMARY"
            )
        }
        return cards
    }
}

@Composable
fun NoteFlashcardsDialog(
    noteTitle: String,
    blocks: List<NoteBlockEntity>,
    onDismiss: () -> Unit
) {
    val cards = remember(noteTitle, blocks) {
        NoteFlashcardGenerator.generateFromBlocks(noteTitle, blocks)
    }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var masteredCount by remember { mutableIntStateOf(0) }
    val activeCard = cards[currentIndex.coerceIn(0, cards.lastIndex)]

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ACTIVE RECALL FLASHCARDS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = BlueprintPrimary
                    )
                    Text(
                        text = "Card ${currentIndex + 1} of ${cards.size} • Mastered: $masteredCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = BlueprintSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp)
                        .clickable { isFlipped = !isFlipped }
                        .testTag("flashcard_flip_surface"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFlipped) BlueprintSurface else Color.White
                    ),
                    border = BorderStroke(2.dp, BlueprintPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (isFlipped) "ANSWER • ${activeCard.badge}" else "PROMPT • ${activeCard.badge} (TAP TO FLIP)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = BlueprintSecondary,
                                modifier = Modifier
                                    .background(BlueprintSurface, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            Text(
                                text = if (isFlipped) activeCard.answer else activeCard.prompt,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = if (isFlipped) FontFamily.Serif else FontFamily.Default,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isFlipped = false
                            currentIndex = (currentIndex - 1 + cards.size) % cards.size
                        },
                        border = BorderStroke(1.dp, BlueprintPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("flashcard_prev_button")
                    ) {
                        Text("← PREV", color = BlueprintPrimary)
                    }
                    Button(
                        onClick = {
                            masteredCount++
                            isFlipped = false
                            currentIndex = (currentIndex + 1) % cards.size
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueprintPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("flashcard_got_it_button")
                    ) {
                        Text("✓ GOT IT NEXT →")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("DONE", color = BlueprintPrimary)
            }
        }
    )
}
