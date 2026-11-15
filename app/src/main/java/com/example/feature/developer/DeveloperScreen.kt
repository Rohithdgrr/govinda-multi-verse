package com.example.feature.developer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.document.BlockType
import com.example.core.document.CodeLanguage
import com.example.core.document.NoteTemplateCatalog
import com.example.core.latex.LatexRendererEngine
import com.example.feature.notes.NotesStudioUiState

private val BlueprintPrimary = Color(0xFF003DA5)
private val BlueprintSecondary = Color(0xFF1647BB)
private val BlueprintLight = Color(0xFFEAF1FF)

data class FormulaReferenceItem(
    val id: String,
    val domain: String,
    val title: String,
    val latex: String,
    val summary: String
)

data class CodeSnippetReferenceItem(
    val id: String,
    val language: CodeLanguage,
    val title: String,
    val code: String
)

private val developerFormulas = listOf(
    FormulaReferenceItem(
        id = "fmt_euler",
        domain = "Mathematics",
        title = "Euler's Identity & Complex Exponential",
        latex = "e^{i\\pi} + 1 = 0",
        summary = "Links exponential function and trigonometric functions in complex analysis."
    ),
    FormulaReferenceItem(
        id = "fmt_fourier",
        domain = "ECE / Signals",
        title = "Continuous-Time Fourier Transform",
        latex = "\\int_{-\\infty}^{\\infty} x(t) e^{-j\\omega t} dt",
        summary = "Transforms time-domain signal x(t) to frequency-domain spectrum X(jω)."
    ),
    FormulaReferenceItem(
        id = "fmt_transfer",
        domain = "Control Systems",
        title = "Closed-Loop Transfer Function",
        latex = "\\frac{G(s)}{1 + G(s)H(s)}",
        summary = "Standard negative-feedback loop gain for LTI systems."
    ),
    FormulaReferenceItem(
        id = "fmt_threephase",
        domain = "EEE / Power",
        title = "Three-Phase Active Power",
        latex = "P = \\sqrt{3} V_L I_L \\cos\\phi",
        summary = "Balanced 3-phase real power in terms of line voltage and line current."
    ),
    FormulaReferenceItem(
        id = "fmt_state_matrix",
        domain = "Linear Algebra",
        title = "2×2 State Transition Matrix",
        latex = "\\begin{bmatrix} a & b \\\\ c & d \\end{bmatrix}",
        summary = "State-space matrix block representation."
    )
)

private val developerSnippets = listOf(
    CodeSnippetReferenceItem(
        id = "snip_dijkstra",
        language = CodeLanguage.KOTLIN,
        title = "Dijkstra Shortest Path (O((V+E) log V))",
        code = """
            fun dijkstra(n: Int, adj: Array<List<Pair<Int, Int>>>, src: Int): IntArray {
                val dist = IntArray(n) { Int.MAX_VALUE }.apply { this[src] = 0 }
                val pq = java.util.PriorityQueue<Pair<Int, Int>>(compareBy { it.first })
                pq.add(0 to src)
                while (pq.isNotEmpty()) {
                    val (d, u) = pq.poll()
                    if (d > dist[u]) continue
                    for ((v, w) in adj[u]) {
                        if (dist[u] + w < dist[v]) {
                            dist[v] = dist[u] + w
                            pq.add(dist[v] to v)
                        }
                    }
                }
                return dist
            }
        """.trimIndent()
    ),
    CodeSnippetReferenceItem(
        id = "snip_verilog_dff",
        language = CodeLanguage.VERILOG,
        title = "Synchronous Reset D Flip-Flop (Verilog)",
        code = """
            module d_ff_sync (
                input wire clk,
                input wire rst,
                input wire d,
                output reg q
            );
                always @(posedge clk) begin
                    if (rst) q <= 1'b0;
                    else     q <= d;
                end
            endmodule
        """.trimIndent()
    ),
    CodeSnippetReferenceItem(
        id = "snip_python_fft",
        language = CodeLanguage.PYTHON,
        title = "Discrete Fourier Transform Magnitude Check",
        code = """
            import math
            def dft_dc_component(samples):
                return sum(samples) / max(1, len(samples))
        """.trimIndent()
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DeveloperScreen(
    notesState: NotesStudioUiState,
    questionBankCount: Int,
    onBack: () -> Unit,
    onOpenNotesStudio: () -> Unit,
    onInsertFormulaToActiveNote: (String, String) -> Unit,
    onInsertSnippetToActiveNote: (CodeLanguage, String) -> Unit,
    onCreateNoteFromTemplate: (String) -> Unit,
    onExportAllZip: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var statusBanner by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("developer_dashboard_screen"),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "DEVELOPER WORKBENCH",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = BlueprintPrimary
                        )
                        Text(
                            text = "Notes Studio • Formula & Code Library • Storage Inspector",
                            style = MaterialTheme.typography.labelSmall,
                            color = BlueprintSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("developer_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BlueprintPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            statusBanner?.let { msg ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BlueprintLight),
                        border = BorderStroke(1.dp, BlueprintPrimary)
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = BlueprintPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // 1. Notes Studio Summary & Quick Launch
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dev_notes_summary_card"),
                    colors = CardDefaults.cardColors(containerColor = BlueprintLight),
                    border = BorderStroke(1.dp, BlueprintPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = "Notes Studio",
                                    tint = BlueprintPrimary
                                )
                                Text(
                                    text = "NOTES STUDIO & DOCUMENT ENGINE",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BlueprintPrimary
                                )
                            }
                            Text(
                                text = "${notesState.allNotes.size} Notes • ${notesState.activeBlocks.size} Active Blocks",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintSecondary
                            )
                        }

                        Text(
                            text = "Active Document: ${notesState.activeNote?.title ?: "None selected"} " +
                                "(${notesState.activeRevisions.size} saved revisions, ${notesState.activeBacklinks.size} backlinks)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onOpenNotesStudio,
                                colors = ButtonDefaults.buttonColors(containerColor = BlueprintPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dev_launch_notes_studio_button")
                            ) {
                                Text("OPEN NOTES STUDIO")
                            }
                            OutlinedButton(
                                onClick = {
                                    onExportAllZip()
                                    statusBanner = "Full ZIP backup generated in Export Studio."
                                },
                                border = BorderStroke(1.dp, BlueprintPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dev_backup_zip_button")
                            ) {
                                Text("EXPORT ZIP BACKUP", color = BlueprintPrimary)
                            }
                        }
                    }
                }
            }

            // 2. Quick Templates Launcher
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "TECHNICAL DOCUMENT TEMPLATES (${NoteTemplateCatalog.allTemplates.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintPrimary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            NoteTemplateCatalog.allTemplates.take(8).forEach { tpl ->
                                AssistChip(
                                    onClick = {
                                        onCreateNoteFromTemplate(tpl.id)
                                        onOpenNotesStudio()
                                    },
                                    label = { Text(tpl.title, style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.testTag("dev_template_${tpl.id}")
                                )
                            }
                        }
                    }
                }
            }

            // 3. Formula Library Section Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Functions,
                        contentDescription = "Formula Library",
                        tint = BlueprintPrimary
                    )
                    Text(
                        text = "ENGINEERING & MATH FORMULA LIBRARY",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = BlueprintPrimary
                    )
                }
            }

            items(developerFormulas, key = { it.id }) { formula ->
                val renderedPreview = remember(formula.latex) {
                    LatexRendererEngine.formatInlineSymbols(formula.latex)
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("formula_card_${formula.id}"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BlueprintLight)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${formula.domain} • ${formula.title}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintPrimary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(formula.latex))
                                        statusBanner = "Copied LaTeX: ${formula.latex}"
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = "Copy LaTeX",
                                        tint = BlueprintSecondary
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        onInsertFormulaToActiveNote(formula.latex, formula.title)
                                        statusBanner = "Inserted '${formula.title}' into active note."
                                    },
                                    border = BorderStroke(1.dp, BlueprintPrimary),
                                    modifier = Modifier.testTag("insert_formula_${formula.id}")
                                ) {
                                    Text("INSERT", color = BlueprintPrimary, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        Text(
                            text = renderedPreview,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BlueprintLight, RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        )
                        Text(
                            text = "LaTeX: ${formula.latex} — ${formula.summary}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 4. Code Snippets Library Section Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Code,
                        contentDescription = "Code Snippets",
                        tint = BlueprintPrimary
                    )
                    Text(
                        text = "TECHNICAL CODE SNIPPETS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = BlueprintPrimary
                    )
                }
            }

            items(developerSnippets, key = { it.id }) { snippet ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("snippet_card_${snippet.id}"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "[${snippet.language.displayName}] ${snippet.title}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintPrimary
                            )
                            OutlinedButton(
                                onClick = {
                                    onInsertSnippetToActiveNote(snippet.language, snippet.code)
                                    statusBanner = "Inserted ${snippet.language.displayName} snippet into active note."
                                },
                                border = BorderStroke(1.dp, BlueprintPrimary),
                                modifier = Modifier.testTag("insert_snippet_${snippet.id}")
                            ) {
                                Text("INSERT CODE BLOCK", color = BlueprintPrimary, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Text(
                            text = snippet.code,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF6F8FC), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        )
                    }
                }
            }

            // 5. Storage & Room Database Inspector
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dev_storage_inspector_card"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BlueprintPrimary)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Storage,
                                contentDescription = "Database Inspector",
                                tint = BlueprintPrimary
                            )
                            Text(
                                text = "LOCAL ROOM DATABASE & CONTENT PACK INSPECTOR",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = BlueprintPrimary
                            )
                        }
                        Text(
                            text = "• Offline Notes: ${notesState.allNotes.size} | Folders: ${notesState.folders.size} | Tags: ${notesState.tags.size}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Supported Block Types: ${BlockType.entries.size} | Supported Languages: ${CodeLanguage.entries.size}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Question Bank: $questionBankCount verified records (Pack: PACK_CORE_PERMISSIVE_V1 • 100% Offline)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
