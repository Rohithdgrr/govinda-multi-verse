package com.example.feature.notes.features

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.core.document.BlockType

private val BlueprintPrimary = Color(0xFF003DA5)
private val BlueprintSecondary = Color(0xFF1647BB)
private val BlueprintSurface = Color(0xFFEAF1FF)

data class SlashCommandItem(
    val id: String,
    val command: String,
    val title: String,
    val category: String,
    val blockType: BlockType? = null,
    val presetContent: String? = null,
    val metadata: String? = null
)

object SlashCommandCatalog {
    val commands: List<SlashCommandItem> = listOf(
        SlashCommandItem("cmd_h1", "/h1", "Heading 1 — Major Section", "Structure", BlockType.HEADING_1),
        SlashCommandItem("cmd_h2", "/h2", "Heading 2 — Sub-Section", "Structure", BlockType.HEADING_2),
        SlashCommandItem("cmd_h3", "/h3", "Heading 3 — Topic Header", "Structure", BlockType.HEADING_3),
        SlashCommandItem("cmd_todo", "/todo", "Interactive Checklist", "Structure", BlockType.CHECKLIST),
        SlashCommandItem("cmd_toggle", "/toggle", "Collapsible Derivation Toggle", "Structure", BlockType.TOGGLE_SECTION),
        SlashCommandItem("cmd_table", "/table", "3×3 Engineering Parameter Table", "Data", BlockType.TABLE),
        SlashCommandItem(
            "cmd_truth_table",
            "/truthtable",
            "Digital Logic Gate Truth Table (A, B, NAND, NOR, XOR)",
            "Data",
            BlockType.TABLE,
            presetContent = """{"columns":["A","B","NAND","NOR","XOR"],"rows":[["0","0","1","1","0"],["0","1","1","0","1"],["1","0","1","0","1"],["1","1","0","0","0"]],"alignments":["CENTER","CENTER","CENTER","CENTER","CENTER"]}"""
        ),
        SlashCommandItem(
            "cmd_latex_frac",
            "/frac",
            "LaTeX Transfer Function Fraction",
            "Math & LaTeX",
            BlockType.LATEX_BLOCK,
            presetContent = "H(s) = \\frac{Y(s)}{X(s)} = \\frac{K \\cdot \\omega_n^2}{s^2 + 2\\zeta\\omega_n s + \\omega_n^2}"
        ),
        SlashCommandItem(
            "cmd_latex_matrix",
            "/matrix",
            "LaTeX 2×2 State Matrix [A]",
            "Math & LaTeX",
            BlockType.LATEX_BLOCK,
            presetContent = "A = \\begin{bmatrix} 0 & 1 \\\\ -\\omega_0^2 & -2\\zeta\\omega_0 \\end{bmatrix}"
        ),
        SlashCommandItem(
            "cmd_latex_integral",
            "/integral",
            "LaTeX Laplace & Fourier Integral",
            "Math & LaTeX",
            BlockType.LATEX_BLOCK,
            presetContent = "\\mathcal{L}\\{f(t)\\} = \\int_{0}^{\\infty} f(t) e^{-st} dt"
        ),
        SlashCommandItem(
            "cmd_code_kotlin",
            "/kotlin",
            "Kotlin Algorithm Block",
            "Code",
            BlockType.CODE_BLOCK,
            presetContent = "fun binarySearch(arr: IntArray, target: Int): Int {\n    var lo = 0; var hi = arr.lastIndex\n    while (lo <= hi) {\n        val mid = (lo + hi) ushr 1\n        if (arr[mid] == target) return mid\n        if (arr[mid] < target) lo = mid + 1 else hi = mid - 1\n    }\n    return -1\n}",
            metadata = "KOTLIN"
        ),
        SlashCommandItem(
            "cmd_code_verilog",
            "/verilog",
            "Verilog RTL Module Block",
            "Code",
            BlockType.CODE_BLOCK,
            presetContent = "module counter_4bit(\n    input wire clk,\n    input wire rst,\n    output reg [3:0] count\n);\n    always @(posedge clk) begin\n        if (rst) count <= 4'b0000;\n        else count <= count + 1'b1;\n    end\nendmodule",
            metadata = "VERILOG"
        ),
        SlashCommandItem(
            "cmd_schematic_rc",
            "/rc",
            "RC Low-Pass Filter Schematic",
            "Diagrams",
            BlockType.ENGINEERING_SCHEMATIC,
            presetContent = "RC_LOW_PASS|R = 10 kΩ, C = 100 nF, f_c = 159.15 Hz"
        ),
        SlashCommandItem(
            "cmd_mermaid",
            "/flowchart",
            "Closed-Loop Control Mermaid Diagram",
            "Diagrams",
            BlockType.MERMAID_DIAGRAM,
            presetContent = "graph LR\n  Ref[R(s)] --> Sum((+ / -))\n  Sum --> Controller[G_c(s)]\n  Controller --> Plant[G_p(s)]\n  Plant --> Output[Y(s)]"
        ),
        SlashCommandItem(
            "cmd_callout_exam",
            "/tip",
            "GATE / Exam High-Yield Callout",
            "Structure",
            BlockType.CALLOUT,
            presetContent = "EXAM TIP: Check causal stability (ROC includes jω-axis and all poles lie in the Left-Half Plane)."
        )
    )
}

@Composable
fun SlashCommandPaletteDialog(
    onDismiss: () -> Unit,
    onExecuteCommand: (SlashCommandItem) -> Unit,
    onDuplicateActiveNote: () -> Unit,
    onAutoNumberHeadings: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        val q = query.trim().removePrefix("/")
        if (q.isEmpty()) SlashCommandCatalog.commands
        else SlashCommandCatalog.commands.filter {
            it.command.contains(q, ignoreCase = true) ||
                it.title.contains(q, ignoreCase = true) ||
                it.category.contains(q, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Column {
                Text(
                    text = "/ SLASH COMMAND PALETTE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = BlueprintPrimary
                )
                Text(
                    text = "Quickly insert LaTeX macros, truth tables, RTL code, or run document actions",
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
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Type /frac, /matrix, /truthtable, /verilog…") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = BlueprintPrimary)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slash_command_search_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onAutoNumberHeadings()
                                onDismiss()
                            }
                            .testTag("slash_action_autonumber"),
                        colors = CardDefaults.cardColors(containerColor = BlueprintSurface),
                        border = BorderStroke(1.dp, BlueprintPrimary)
                    ) {
                        Text(
                            text = "1.1 Auto-Number Headings",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintPrimary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onDuplicateActiveNote()
                                onDismiss()
                            }
                            .testTag("slash_action_duplicate_note"),
                        colors = CardDefaults.cardColors(containerColor = BlueprintSurface),
                        border = BorderStroke(1.dp, BlueprintPrimary)
                    ) {
                        Text(
                            text = "⧉ Duplicate Whole Note",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintPrimary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onExecuteCommand(item)
                                    onDismiss()
                                }
                                .testTag("slash_item_${item.id}"),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, BlueprintSurface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${item.command}  •  ${item.title}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = BlueprintPrimary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = item.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BlueprintSecondary
                                    )
                                }
                                Text(
                                    text = "INSERT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BlueprintPrimary
                                )
                            }
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
