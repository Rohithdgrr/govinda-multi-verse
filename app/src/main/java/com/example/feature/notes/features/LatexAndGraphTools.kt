package com.example.feature.notes.features

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.NoteEntity
import com.example.core.database.NoteLinkEntity
import com.example.core.latex.LatexRendererEngine
import kotlin.math.cos
import kotlin.math.sin

private val BlueprintPrimary = Color(0xFF003DA5)
private val BlueprintSecondary = Color(0xFF1647BB)
private val BlueprintSurface = Color(0xFFEAF1FF)

private val latexQuickTokens = listOf(
    "\\frac{a}{b}" to "Fraction",
    "\\sqrt{x}" to "Sqrt",
    "\\int_{0}^{\\infty}" to "Integral",
    "\\sum_{k=0}^{N}" to "Sum",
    "\\begin{bmatrix} a & b \\\\ c & d \\end{bmatrix}" to "2×2 Matrix",
    "\\begin{vmatrix} a & b \\\\ c & d \\end{vmatrix}" to "Determinant",
    "\\alpha" to "α",
    "\\beta" to "β",
    "\\omega" to "ω",
    "\\Omega" to "Ω",
    "\\lambda" to "λ",
    "\\theta" to "θ",
    "\\pi" to "π",
    "\\partial" to "∂",
    "\\nabla" to "∇",
    "\\infty" to "∞",
    "\\implies" to "⟹",
    "\\approx" to "≈"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LatexSymbolPadDialog(
    onInsertLatexBlock: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var latexDraft by remember {
        mutableStateOf("H(s) = \\frac{\\omega_n^2}{s^2 + 2\\zeta\\omega_n s + \\omega_n^2}")
    }
    var equationLabel by remember { mutableStateOf("Second-Order Transfer Function") }
    val ast = remember(latexDraft) { LatexRendererEngine.parse(latexDraft) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Column {
                Text(
                    text = "∑ INTERACTIVE LATEX SYMBOL PAD",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = BlueprintPrimary
                )
                Text(
                    text = "Tap symbols to compose verified equations with live AST preview",
                    style = MaterialTheme.typography.labelSmall,
                    color = BlueprintSecondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    latexQuickTokens.forEach { (token, label) ->
                        OutlinedButton(
                            onClick = { latexDraft = "$latexDraft $token".trim() },
                            border = BorderStroke(1.dp, BlueprintPrimary),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(label, fontSize = 11.sp, color = BlueprintPrimary)
                        }
                    }
                }

                OutlinedTextField(
                    value = equationLabel,
                    onValueChange = { equationLabel = it },
                    label = { Text("Equation Title / Caption") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = latexDraft,
                    onValueChange = { latexDraft = it },
                    label = { Text("LaTeX Source") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("latex_pad_source_input")
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (ast.isValid) BlueprintSurface else Color(0xFFFFEBEE)
                    ),
                    border = BorderStroke(1.dp, if (ast.isValid) BlueprintPrimary else Color.Red)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (ast.isValid) "LIVE PREVIEW:" else (ast.errorMessage ?: "Invalid Syntax"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (ast.isValid) BlueprintPrimary else Color.Red
                        )
                        Text(
                            text = ast.unicodeFormatted,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (latexDraft.isNotBlank()) {
                        onInsertLatexBlock(latexDraft.trim(), equationLabel.trim())
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlueprintPrimary),
                modifier = Modifier.testTag("latex_pad_insert_button")
            ) {
                Text("INSERT EQUATION BLOCK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = BlueprintPrimary)
            }
        }
    )
}

@Composable
fun NoteKnowledgeGraphDialog(
    allNotes: List<NoteEntity>,
    activeNote: NoteEntity?,
    backlinks: List<NoteLinkEntity>,
    outgoingLinks: List<NoteLinkEntity>,
    onSelectNote: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Column {
                Text(
                    text = "◉ KNOWLEDGE GRAPH & NOTE TOPOLOGY",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = BlueprintPrimary
                )
                Text(
                    text = "${allNotes.size} Workspace Nodes • ${backlinks.size + outgoingLinks.size} Active Links",
                    style = MaterialTheme.typography.labelSmall,
                    color = BlueprintSecondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Visual Radial Topology Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(BlueprintSurface, RoundedCornerShape(10.dp))
                        .testTag("knowledge_graph_canvas")
                ) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val radius = (size.minDimension / 2f) * 0.68f
                    val nodes = allNotes.take(8)
                    val positions = nodes.mapIndexed { idx, _ ->
                        if (idx == 0) {
                            Offset(cx, cy)
                        } else {
                            val angle = (2.0 * Math.PI * (idx - 1)) / maxOf(1, nodes.size - 1)
                            Offset(
                                x = cx + (radius * cos(angle)).toFloat(),
                                y = cy + (radius * sin(angle)).toFloat()
                            )
                        }
                    }
                    // Draw edges from center active node to linked/neighbor nodes
                    for (i in 1 until positions.size) {
                        drawLine(
                            color = BlueprintSecondary.copy(alpha = 0.45f),
                            start = positions[0],
                            end = positions[i],
                            strokeWidth = 3.5f
                        )
                    }
                    // Draw node circles
                    positions.forEachIndexed { idx, pos ->
                        drawCircle(
                            color = if (idx == 0) BlueprintPrimary else BlueprintSecondary,
                            radius = if (idx == 0) 18f else 12f,
                            center = pos
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allNotes, key = { it.id }) { note ->
                        val isCurrent = note.id == activeNote?.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isCurrent) BlueprintSurface else Color(0xFFF8FAFF),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    onSelectNote(note.id)
                                    onDismiss()
                                }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = (if (isCurrent) "◉ " else "○ ") + note.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = BlueprintPrimary
                                )
                                Text(
                                    text = "ID: [[${note.id}]] • Folder: ${note.folderId ?: "root"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = BlueprintSecondary
                                )
                            }
                            AssistChip(
                                onClick = {
                                    onSelectNote(note.id)
                                    onDismiss()
                                },
                                label = { Text("OPEN", fontSize = 10.sp) }
                            )
                        }
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
