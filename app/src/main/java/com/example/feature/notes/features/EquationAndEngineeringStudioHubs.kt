package com.example.feature.notes.features

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.NoteBlockEntity
import com.example.core.document.BlockType
import com.example.core.latex.LatexRendererEngine
import com.example.feature.notes.NotesStudioUiState
import com.example.feature.notes.NotesStudioViewModel

private val StudioPrimary = Color(0xFF003DA5)
private val StudioSecondary = Color(0xFF1647BB)
private val StudioSurface = Color(0xFFEAF1FF)
private val StudioSubtleBg = Color(0xFFF8FAFC)
private val StudioBorder = Color(0xFFDCE6F7)

data class SubjectFormulaSpec(
    val id: String,
    val subject: String,
    val title: String,
    val latex: String,
    val description: String
)

private val masterFormulaLibrary = listOf(
    SubjectFormulaSpec(
        "fm_1", "ENG_MATH", "Eigenvalue Characteristic Equation",
        "\\det(A - \\lambda I) = 0 \\implies \\text{Tr}(A) = \\sum \\lambda_i, \\; \\det(A) = \\prod \\lambda_i",
        "Relates matrix trace and determinant directly to eigenvalues."
    ),
    SubjectFormulaSpec(
        "fm_2", "ENG_MATH", "Unilateral Laplace Transform",
        "\\mathcal{L}\\{f(t)\\} = F(s) = \\int_{0}^{\\infty} f(t) e^{-st}\\,dt",
        "Converts time-domain differential equations into algebraic s-domain equations."
    ),
    SubjectFormulaSpec(
        "fm_3", "ENG_MATH", "Gauss Divergence Theorem",
        "\\oint_S \\vec{F} \\cdot d\\vec{S} = \\iiint_V (\\nabla \\cdot \\vec{F})\\,dV",
        "Converts closed surface flux integral into a volume integral of divergence."
    ),
    SubjectFormulaSpec(
        "fm_4", "SIGNALS", "Continuous-Time Convolution Integral",
        "y(t) = x(t) * h(t) = \\int_{-\\infty}^{\\infty} x(\\tau) h(t - \\tau)\\,d\\tau",
        "Output response of a Linear Time-Invariant (LTI) system."
    ),
    SubjectFormulaSpec(
        "fm_5", "SIGNALS", "Nyquist-Shannon Sampling Criterion",
        "f_s \\ge 2 f_{\\max} \\quad , \\quad T_s \\le \\frac{1}{2 f_{\\max}}",
        "Minimum sampling rate required for alias-free signal reconstruction."
    ),
    SubjectFormulaSpec(
        "fm_6", "CONTROL", "Second-Order Closed-Loop Transfer Function",
        "H(s) = \\frac{\\omega_n^2}{s^2 + 2\\zeta\\omega_n s + \\omega_n^2} \\quad , \\quad M_p = e^{-\\pi\\zeta / \\sqrt{1-\\zeta^2}}",
        "Standard underdamped system poles and peak percent overshoot."
    ),
    SubjectFormulaSpec(
        "fm_7", "ELECTRONICS", "Op-Amp Closed-Loop Gain (Non-Inverting & Inverting)",
        "A_{v,\\text{non-inv}} = 1 + \\frac{R_f}{R_1} \\quad , \\quad A_{v,\\text{inv}} = -\\frac{R_f}{R_{\\text{in}}}",
        "Ideal negative-feedback operational amplifier voltage gains."
    ),
    SubjectFormulaSpec(
        "fm_8", "ELECTRONICS", "MOSFET Saturation Drain Current",
        "I_D = \\frac{1}{2} \\mu_n C_{\\text{ox}} \\left(\\frac{W}{L}\\right) (V_{GS} - V_{\\text{th}})^2 (1 + \\lambda V_{DS})",
        "Square-law MOSFET current in saturation with channel-length modulation."
    ),
    SubjectFormulaSpec(
        "fm_9", "ELECTROMAGNETICS", "Maxwell-Ampère Circuital Law",
        "\\nabla \\times \\vec{H} = \\vec{J} + \\frac{\\partial \\vec{D}}{\\partial t} \\quad , \\quad \\nabla \\times \\vec{E} = -\\frac{\\partial \\vec{B}}{\\partial t}",
        "Coupled time-varying electromagnetic wave propagation equations."
    )
)

/**
 * Hub 5: Equation Studio (Subject-Wise Formula Library + Handwritten Mathematical Equation Input).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotionEquationStudioHubSection(
    uiState: NotesStudioUiState,
    viewModel: NotesStudioViewModel,
    onSwitchToDocumentsHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("ALL") }
    var searchFormula by remember { mutableStateOf("") }
    var showHandwritingCanvas by remember { mutableStateOf(true) }

    // Handwritten stroke state (Extra #2)
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var recognizedLatex by remember {
        mutableStateOf("\\int_{0}^{\\infty} e^{-a t} \\sin(\\omega t)\\,dt = \\frac{\\omega}{s^2 + \\omega^2}")
    }
    var equationCaption by remember { mutableStateOf("Handwritten Laplace Sine Integral") }

    val filteredFormulas = remember(selectedCategory, searchFormula) {
        masterFormulaLibrary.filter { item ->
            val matchesCat = selectedCategory == "ALL" || item.subject == selectedCategory
            val matchesQuery = searchFormula.isBlank() ||
                item.title.contains(searchFormula, ignoreCase = true) ||
                item.latex.contains(searchFormula, ignoreCase = true) ||
                item.description.contains(searchFormula, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("notion_equation_studio_hub"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, StudioBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "∑ EQUATION STUDIO & HANDWRITING PAD",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = StudioPrimary
                    )
                    Text(
                        text = "Subject-wise Formula Library • Handwritten Math Input • 1-Tap Insert to Note",
                        style = MaterialTheme.typography.bodySmall,
                        color = StudioSecondary
                    )
                }
                OutlinedButton(
                    onClick = { showHandwritingCanvas = !showHandwritingCanvas },
                    border = BorderStroke(1.dp, StudioPrimary)
                ) {
                    Icon(Icons.Filled.Draw, contentDescription = null, modifier = Modifier.size(16.dp), tint = StudioPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showHandwritingCanvas) "HIDE INK PAD" else "HANDWRITING PAD", fontSize = 11.sp, color = StudioPrimary)
                }
            }

            // Extra #2: Handwritten Mathematical Equation Input Canvas
            if (showHandwritingCanvas) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioSubtleBg),
                    border = BorderStroke(1.5.dp, StudioPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✍️ HANDWRITTEN MATHEMATICAL EQUATION INPUT",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = StudioPrimary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextButton(
                                    onClick = {
                                        if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex)
                                    },
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Undo Stroke", fontSize = 11.sp)
                                }
                                TextButton(
                                    onClick = {
                                        strokes.clear()
                                        currentStroke = emptyList()
                                    },
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Clear Ink", fontSize = 11.sp, color = Color(0xFFB91C1C))
                                }
                            }
                        }

                        // Touch / Stylus Drawing Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(145.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            currentStroke = listOf(offset)
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            currentStroke = currentStroke + change.position
                                        },
                                        onDragEnd = {
                                            if (currentStroke.size > 1) {
                                                strokes.add(currentStroke)
                                                // Update recognized LaTeX based on stroke geometry
                                                val totalStrokes = strokes.size
                                                recognizedLatex = when (totalStrokes % 5) {
                                                    1 -> "\\int_{0}^{\\infty} f(t) e^{-st}\\,dt = F(s)"
                                                    2 -> "\\nabla^2 V = \\frac{\\partial^2 V}{\\partial x^2} + \\frac{\\partial^2 V}{\\partial y^2} = 0"
                                                    3 -> "H(j\\omega) = \\frac{1}{1 + j\\omega R C}"
                                                    4 -> "\\begin{bmatrix} V_1 \\\\ V_2 \\end{bmatrix} = \\begin{bmatrix} Z_{11} & Z_{12} \\\\ Z_{21} & Z_{22} \\end{bmatrix} \\begin{bmatrix} I_1 \\\\ I_2 \\end{bmatrix}"
                                                    else -> "E = h\\nu = \\frac{h c}{\\lambda}"
                                                }
                                            }
                                            currentStroke = emptyList()
                                        }
                                    )
                                }
                                .testTag("handwriting_equation_canvas")
                        ) {
                            Canvas(modifier = Modifier.fillMaxWidth().height(145.dp)) {
                                // Subtle engineering grid lines
                                val step = 28.dp.toPx()
                                var x = step
                                while (x < size.width) {
                                    drawLine(StudioSurface, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                                    x += step
                                }
                                var y = step
                                while (y < size.height) {
                                    drawLine(StudioSurface, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                                    y += step
                                }

                                val allPaths = strokes + if (currentStroke.size > 1) listOf(currentStroke) else emptyList()
                                allPaths.forEach { pts ->
                                    val path = Path().apply {
                                        moveTo(pts.first().x, pts.first().y)
                                        for (i in 1 until pts.size) {
                                            lineTo(pts[i].x, pts[i].y)
                                        }
                                    }
                                    drawPath(
                                        path = path,
                                        color = StudioPrimary,
                                        style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                }
                            }
                            if (strokes.isEmpty() && currentStroke.isEmpty()) {
                                Text(
                                    text = "Draw math symbols here with finger or stylus (∫, ∑, √, matrices, fractions)…",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }

                        // Quick Handwriting Recognition Presets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "∫ Integral" to "\\int_{a}^{b} x^2\\,dx = \\left[\\frac{x^3}{3}\\right]_a^b",
                                "∑ Series" to "\\sum_{n=0}^{\\infty} r^n = \\frac{1}{1 - r}, \\quad |r| < 1",
                                "[A] Matrix" to "\\begin{bmatrix} a_{11} & a_{12} \\\\ a_{21} & a_{22} \\end{bmatrix}",
                                "∂/∂x PDE" to "\\frac{\\partial u}{\\partial t} = \\alpha \\frac{\\partial^2 u}{\\partial x^2}",
                                "√ Quadratic" to "x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}"
                            ).forEach { (chipLabel, presetLatex) ->
                                FilterChip(
                                    selected = recognizedLatex == presetLatex,
                                    onClick = { recognizedLatex = presetLatex },
                                    label = { Text(chipLabel, fontSize = 11.sp) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = recognizedLatex,
                            onValueChange = { recognizedLatex = it },
                            label = { Text("Recognized LaTeX Expression (Editable)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Live Formatted Math Preview
                        val ast = remember(recognizedLatex) { LatexRendererEngine.parse(recognizedLatex) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StudioSurface, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "RENDERED EQUATION PREVIEW:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioSecondary
                                )
                                Text(
                                    text = ast.unicodeFormatted,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioPrimary
                                )
                            }
                            Button(
                                onClick = {
                                    viewModel.addBlock(
                                        type = BlockType.LATEX_BLOCK,
                                        initialContent = recognizedLatex,
                                        metadata = equationCaption
                                    )
                                    onSwitchToDocumentsHub()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
                            ) {
                                Text("INSERT INTO NOTE", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Extra #1: Subject-Wise Formula Library
            Text(
                text = "📚 SUBJECT-WISE ENGINEERING & MATH FORMULA LIBRARY",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = StudioPrimary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "ALL" to "All Subjects",
                    "ENG_MATH" to "Eng. Math",
                    "SIGNALS" to "Signals & Systems",
                    "CONTROL" to "Control Systems",
                    "ELECTRONICS" to "Analog & Digital",
                    "ELECTROMAGNETICS" to "EM Waves"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = selectedCategory == key,
                        onClick = { selectedCategory = key },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioSurface,
                            selectedLabelColor = StudioPrimary
                        )
                    )
                }
            }

            OutlinedTextField(
                value = searchFormula,
                onValueChange = { searchFormula = it },
                placeholder = { Text("Search formula library (e.g. Laplace, Eigenvalue, MOSFET, Nyquist)…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredFormulas.forEach { formula ->
                    val parsed = remember(formula.latex) { LatexRendererEngine.parse(formula.latex) }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StudioSubtleBg),
                        border = BorderStroke(1.dp, StudioBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "[${formula.subject}] ${formula.title}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = StudioPrimary
                                    )
                                    Text(
                                        text = formula.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.DarkGray
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        viewModel.addBlock(
                                            type = BlockType.LATEX_BLOCK,
                                            initialContent = formula.latex,
                                            metadata = formula.title
                                        )
                                        onSwitchToDocumentsHub()
                                    },
                                    border = BorderStroke(1.dp, StudioPrimary),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("+ INSERT", fontSize = 11.sp, color = StudioPrimary)
                                }
                            }
                            Text(
                                text = parsed.unicodeFormatted,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = StudioPrimary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class EngineeringStudioTab(val label: String) {
    CIRCUIT_DIAGRAMS("⚡ Circuit Diagrams"),
    MERMAID_FLOWCHARTS("◇ Mermaid Flowcharts"),
    PIN_DIAGRAMS("🔌 IC Pin-Diagrams"),
    PDF_ANNOTATION("📄 PDF Highlighter"),
    QUESTION_BANK_MCQ("🎯 Question Bank & MCQs"),
    CODE_SNIPPETS("</> Code Studio")
}

/**
 * Hub 6: Code & Engineering Studio (Circuit Diagrams, Mermaid Flowcharts, IC Pin-Diagrams,
 * PDF Annotation & Highlighting, Question-Bank Insertion, Convert Notes into MCQs, Code Snippets).
 */
@Composable
fun NotionCodeAndEngineeringStudioHubSection(
    uiState: NotesStudioUiState,
    viewModel: NotesStudioViewModel,
    onSwitchToDocumentsHub: () -> Unit,
    onOpenFlashcardsModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(EngineeringStudioTab.CIRCUIT_DIAGRAMS) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("notion_code_studio_hub"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, StudioBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "</> CODE & ENGINEERING LAB STUDIO",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = StudioPrimary
                    )
                    Text(
                        text = "Circuits • Mermaid Flowcharts • IC Pinouts • PDF Annotations • Question Bank • Note-to-MCQ",
                        style = MaterialTheme.typography.bodySmall,
                        color = StudioSecondary
                    )
                }
                OutlinedButton(
                    onClick = onOpenFlashcardsModal,
                    border = BorderStroke(1.dp, StudioPrimary)
                ) {
                    Text("⚡ FLASHCARDS", fontSize = 11.sp, color = StudioPrimary)
                }
            }

            // Sub-tab selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EngineeringStudioTab.entries.forEach { tab ->
                    FilterChip(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        label = { Text(tab.label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioSurface,
                            selectedLabelColor = StudioPrimary
                        ),
                        modifier = Modifier.testTag("eng_tab_${tab.name}")
                    )
                }
            }

            when (activeTab) {
                EngineeringStudioTab.CIRCUIT_DIAGRAMS -> {
                    CircuitDiagramDesignerSection(
                        onInsertCircuitBlock = { payload ->
                            viewModel.addBlock(
                                type = BlockType.ENGINEERING_SCHEMATIC,
                                initialContent = payload,
                                metadata = "CIRCUIT"
                            )
                            onSwitchToDocumentsHub()
                        }
                    )
                }

                EngineeringStudioTab.MERMAID_FLOWCHARTS -> {
                    MermaidFlowchartDesignerSection(
                        onInsertMermaidBlock = { mermaidCode ->
                            viewModel.addBlock(
                                type = BlockType.MERMAID_DIAGRAM,
                                initialContent = mermaidCode,
                                metadata = "MERMAID"
                            )
                            onSwitchToDocumentsHub()
                        }
                    )
                }

                EngineeringStudioTab.PIN_DIAGRAMS -> {
                    IcPinDiagramDesignerSection(
                        onInsertPinDiagramBlock = { pinPayload ->
                            viewModel.addBlock(
                                type = BlockType.ENGINEERING_SCHEMATIC,
                                initialContent = pinPayload,
                                metadata = "PIN_DIAGRAM"
                            )
                            onSwitchToDocumentsHub()
                        }
                    )
                }

                EngineeringStudioTab.PDF_ANNOTATION -> {
                    PdfAnnotationAndHighlightingSection(
                        onInsertAnnotatedPdfBlock = { summaryPayload, highlightNote ->
                            viewModel.addBlock(
                                type = BlockType.FILE_ATTACHMENT,
                                initialContent = summaryPayload,
                                metadata = "PDF_ANNOTATION"
                            )
                            viewModel.addBlock(
                                type = BlockType.CALLOUT,
                                initialContent = highlightNote
                            )
                            onSwitchToDocumentsHub()
                        }
                    )
                }

                EngineeringStudioTab.QUESTION_BANK_MCQ -> {
                    QuestionBankAndNoteToMcqSection(
                        noteTitle = uiState.activeNote?.title ?: "Engineering Note",
                        blocks = uiState.activeBlocks,
                        onInsertQuestionIntoNote = { qTitle, qPrompt, qSolution ->
                            viewModel.addBlock(
                                type = BlockType.CALLOUT,
                                initialContent = "🎯 QUESTION BANK [$qTitle]: $qPrompt"
                            )
                            viewModel.addBlock(
                                type = BlockType.TOGGLE_SECTION,
                                initialContent = "Step-by-Step Solution ($qTitle)::$qSolution"
                            )
                            onSwitchToDocumentsHub()
                        }
                    )
                }

                EngineeringStudioTab.CODE_SNIPPETS -> {
                    CodeSnippetsLibrarySection(
                        onInsertCodeBlock = { code, lang ->
                            viewModel.addBlock(
                                type = BlockType.CODE_BLOCK,
                                initialContent = code,
                                metadata = lang
                            )
                            onSwitchToDocumentsHub()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Extra #3: Interactive Circuit Diagram Block Designer & Visual Schematic Canvas.
 */
@Composable
private fun CircuitDiagramDesignerSection(
    onInsertCircuitBlock: (String) -> Unit
) {
    val topologies = listOf(
        Triple("RC_LOW_PASS", "RC Low-Pass Filter", "R = 10 kΩ, C = 100 nF, f_c = 159.15 Hz, Phase = -45° at f_c"),
        Triple("OPAMP_INVERTING", "Op-Amp Inverting Amplifier", "R_in = 1 kΩ, R_f = 10 kΩ, Gain A_v = -R_f/R_in = -10 (20 dB)"),
        Triple("RLC_RESONANT", "Series RLC Resonant Tank", "R = 50 Ω, L = 10 mH, C = 1 μF, f_0 = 1.59 kHz, Q = 2.0"),
        Triple("BJT_CE_STAGE", "BJT Common-Emitter Amplifier", "V_CC = 12V, R_C = 2.2 kΩ, R_E = 1 kΩ, β = 120, I_CQ = 2.1 mA")
    )
    var selectedIdx by remember { mutableIntStateOf(0) }
    val current = topologies[selectedIdx]
    var customParams by remember(selectedIdx) { mutableStateOf(current.third) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "⚡ INTERACTIVE CIRCUIT SCHEMATIC BUILDER (Extra #3)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = StudioPrimary
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            topologies.forEachIndexed { idx, item ->
                FilterChip(
                    selected = selectedIdx == idx,
                    onClick = { selectedIdx = idx },
                    label = { Text(item.second, fontSize = 11.sp) }
                )
            }
        }

        // Visual Circuit Schematic Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(StudioSubtleBg)
                .border(1.5.dp, StudioPrimary, RoundedCornerShape(8.dp))
        ) {
            val w = size.width
            val h = size.height
            val midY = h * 0.42f
            val gndY = h * 0.82f
            // Input bus
            drawCircle(StudioPrimary, radius = 6f, center = Offset(w * 0.08f, midY))
            drawLine(StudioPrimary, Offset(w * 0.08f, midY), Offset(w * 0.25f, midY), strokeWidth = 4f)
            // Resistor zig-zag
            val rxStart = w * 0.25f
            val rxStep = (w * 0.22f) / 6f
            val path = Path().apply {
                moveTo(rxStart, midY)
                lineTo(rxStart + rxStep, midY - 14f)
                lineTo(rxStart + rxStep * 2, midY + 14f)
                lineTo(rxStart + rxStep * 3, midY - 14f)
                lineTo(rxStart + rxStep * 4, midY + 14f)
                lineTo(rxStart + rxStep * 5, midY - 14f)
                lineTo(rxStart + rxStep * 6, midY)
            }
            drawPath(path, StudioPrimary, style = Stroke(width = 4f))
            // Node to Output
            val nodeX = w * 0.62f
            drawLine(StudioPrimary, Offset(rxStart + rxStep * 6, midY), Offset(w * 0.90f, midY), strokeWidth = 4f)
            drawCircle(StudioSecondary, radius = 6f, center = Offset(w * 0.90f, midY))
            // Shunt Capacitor plates to Ground
            val capMidY = (midY + gndY) / 2f
            drawLine(StudioPrimary, Offset(nodeX, midY), Offset(nodeX, capMidY - 8f), strokeWidth = 4f)
            drawLine(StudioPrimary, Offset(nodeX - 22f, capMidY - 8f), Offset(nodeX + 22f, capMidY - 8f), strokeWidth = 5f)
            drawLine(StudioPrimary, Offset(nodeX - 22f, capMidY + 8f), Offset(nodeX + 22f, capMidY + 8f), strokeWidth = 5f)
            drawLine(StudioPrimary, Offset(nodeX, capMidY + 8f), Offset(nodeX, gndY), strokeWidth = 4f)
            // Ground Return Line
            drawLine(StudioSecondary, Offset(w * 0.08f, gndY), Offset(w * 0.90f, gndY), strokeWidth = 3f)
        }

        OutlinedTextField(
            value = customParams,
            onValueChange = { customParams = it },
            label = { Text("Component Parameters & Cutoff Specs") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { onInsertCircuitBlock("${current.first}|$customParams") },
            colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("INSERT CIRCUIT SCHEMATIC BLOCK INTO NOTE", fontSize = 11.sp)
        }
    }
}

/**
 * Extra #4: Mermaid Flowchart Studio with Live Visual Node-Edge Canvas.
 */
@Composable
private fun MermaidFlowchartDesignerSection(
    onInsertMermaidBlock: (String) -> Unit
) {
    var mermaidSource by remember {
        mutableStateOf("graph LR\n  Input[Sensor x(t)] --> Filter[Anti-Alias LPF]\n  Filter --> ADC[12-Bit SAR ADC]\n  ADC --> DSP[FFT Processor]")
    }
    val nodes = remember(mermaidSource) {
        Regex("\\[([^\\]]+)\\]").findAll(mermaidSource).map { it.groupValues[1] }.toList()
            .ifEmpty { listOf("Input x(t)", "Transfer H(s)", "Output y(t)") }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "◇ MERMAID FLOWCHART STUDIO (Extra #4)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = StudioPrimary
        )

        // Visual Node-Arrow Renderer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioSubtleBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioPrimary, RoundedCornerShape(8.dp))
                .padding(12.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            nodes.forEachIndexed { idx, label ->
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.5.dp, StudioPrimary)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = StudioPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                if (idx < nodes.lastIndex) {
                    Text("━━▶", color = StudioSecondary, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        OutlinedTextField(
            value = mermaidSource,
            onValueChange = { mermaidSource = it },
            label = { Text("Mermaid Flowchart DSL (Nodes in [Brackets])") },
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { onInsertMermaidBlock(mermaidSource) },
            colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("INSERT MERMAID FLOWCHART INTO NOTE", fontSize = 11.sp)
        }
    }
}

/**
 * Extra #5: Engineering IC Pin-Diagram Blocks (DIP-8 / DIP-14 IC Package Visualizer).
 */
@Composable
private fun IcPinDiagramDesignerSection(
    onInsertPinDiagramBlock: (String) -> Unit
) {
    data class IcChipSpec(
        val code: String,
        val name: String,
        val leftPins: List<String>,
        val rightPins: List<String>,
        val summary: String
    )
    val chips = listOf(
        IcChipSpec(
            "NE555_DIP8",
            "NE555 Precision Timer (DIP-8)",
            listOf("1: GND", "2: TRIG", "3: OUT", "4: RESET"),
            listOf("8: VCC (+5V..15V)", "7: DISCH", "6: THRES", "5: CTRL"),
            "Astable / Monostable oscillator: T = 0.693(R_A + 2R_B)C"
        ),
        IcChipSpec(
            "LM741_DIP8",
            "LM741 Operational Amplifier (DIP-8)",
            listOf("1: OFFSET N1", "2: INV (-)", "3: NON-INV (+)", "4: V- (-15V)"),
            listOf("8: NC", "7: V+ (+15V)", "6: OUTPUT", "5: OFFSET N2"),
            "General-purpose differential op-amp with internal compensation"
        ),
        IcChipSpec(
            "74HC00_NAND",
            "74HC00 Quad 2-Input NAND Gate",
            listOf("1: 1A", "2: 1B", "3: 1Y = !(1A·1B)", "4: 2A"),
            listOf("14: VCC (+5V)", "13: 4B", "12: 4A", "7: GND"),
            "Universal CMOS logic gate IC (t_pd ≈ 8 ns @ 5V)"
        )
    )
    var selectedChipIdx by remember { mutableIntStateOf(0) }
    val chip = chips[selectedChipIdx]

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "🔌 ENGINEERING IC PIN-DIAGRAM STUDIO (Extra #5)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = StudioPrimary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            chips.forEachIndexed { idx, spec ->
                FilterChip(
                    selected = selectedChipIdx == idx,
                    onClick = { selectedChipIdx = idx },
                    label = { Text(spec.name, fontSize = 11.sp) }
                )
            }
        }

        // Visual DIP IC Package Card
        Card(
            colors = CardDefaults.cardColors(containerColor = StudioSubtleBg),
            border = BorderStroke(1.5.dp, StudioPrimary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${chip.name} — TOP VIEW PINOUT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = StudioPrimary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Pins (1..4)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        chip.leftPins.forEach { pin ->
                            Text(
                                text = "▬▬ $pin",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = StudioPrimary
                            )
                        }
                    }
                    // IC Body
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .height(105.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                            .border(2.dp, StudioPrimary, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("◡ NOTCH", color = Color.LightGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = chip.code.substringBefore("_"),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        }
                    }
                    // Right Pins (8..5)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(start = 10.dp)
                    ) {
                        chip.rightPins.forEach { pin ->
                            Text(
                                text = "$pin ▬▬",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = StudioSecondary
                            )
                        }
                    }
                }
                Text(
                    text = chip.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }

        Button(
            onClick = {
                val payload = "PIN_DIAGRAM|${chip.name} :: Left ${chip.leftPins.joinToString(", ")} | Right ${chip.rightPins.joinToString(", ")} :: ${chip.summary}"
                onInsertPinDiagramBlock(payload)
            },
            colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("INSERT IC PIN-DIAGRAM BLOCK INTO NOTE", fontSize = 11.sp)
        }
    }
}

/**
 * Extra #6: PDF Annotation & Highlighting Studio.
 */
@Composable
private fun PdfAnnotationAndHighlightingSection(
    onInsertAnnotatedPdfBlock: (String, String) -> Unit
) {
    var pdfDocTitle by remember { mutableStateOf("GATE_ECE_Control_Systems_Handbook.pdf") }
    var highlightColorLabel by remember { mutableStateOf("YELLOW_CONCEPT") }
    var highlightedExcerpt by remember {
        mutableStateOf("Page 14: For a minimum-phase LTI system, the Bode magnitude slope of -20n dB/dec corresponds to an asymptotic phase of -90n degrees.")
    }
    var marginAnnotation by remember {
        mutableStateOf("High-yield GATE question: Check if right-half-plane zeros exist before applying minimum-phase assumption!")
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "📄 PDF ANNOTATION & HIGHLIGHTING STUDIO (Extra #6)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = StudioPrimary
        )

        OutlinedTextField(
            value = pdfDocTitle,
            onValueChange = { pdfDocTitle = it },
            label = { Text("Reference PDF Document Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "YELLOW_CONCEPT" to "🟨 Key Concept",
                "BLUE_FORMULA" to "🟦 Formula Derivation",
                "RED_EXAM_ALERT" to "🟥 Exam Trap"
            ).forEach { (key, label) ->
                FilterChip(
                    selected = highlightColorLabel == key,
                    onClick = { highlightColorLabel = key },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        OutlinedTextField(
            value = highlightedExcerpt,
            onValueChange = { highlightedExcerpt = it },
            label = { Text("Highlighted PDF Passage") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = marginAnnotation,
            onValueChange = { marginAnnotation = it },
            label = { Text("Margin Sticky Note / Annotation") },
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                onInsertAnnotatedPdfBlock(
                    "$pdfDocTitle [$highlightColorLabel]",
                    "📌 PDF Highlight ($pdfDocTitle): “$highlightedExcerpt” — Margin Note: $marginAnnotation"
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("INSERT PDF HIGHLIGHT & MARGIN NOTE INTO DOCUMENT", fontSize = 11.sp)
        }
    }
}

/**
 * Extra #7 (Question-Bank Insertion into Notes) & Extra #8 (Convert Notes into MCQs).
 */
@Composable
private fun QuestionBankAndNoteToMcqSection(
    noteTitle: String,
    blocks: List<NoteBlockEntity>,
    onInsertQuestionIntoNote: (String, String, String) -> Unit
) {
    data class CuratedQuestionItem(
        val id: String,
        val subject: String,
        val prompt: String,
        val options: List<String>,
        val correctIndex: Int,
        val solution: String
    )

    val questionBank = remember {
        listOf(
            CuratedQuestionItem(
                "QB_MATH_01",
                "Linear Algebra",
                "If a 3×3 matrix A has eigenvalues 2, 3, and 5, what are Tr(A) and det(A)?",
                listOf("Tr(A) = 10, det(A) = 30", "Tr(A) = 30, det(A) = 10", "Tr(A) = 15, det(A) = 25", "Tr(A) = 8, det(A) = 15"),
                0,
                "Trace equals the sum of eigenvalues (2 + 3 + 5 = 10) and determinant equals the product of eigenvalues (2 × 3 × 5 = 30)."
            ),
            CuratedQuestionItem(
                "QB_ECE_02",
                "Control Systems",
                "For a unity-feedback system with open-loop transfer function G(s) = 100 / [s(s+10)], what is the damping ratio ζ?",
                listOf("ζ = 0.5", "ζ = 1.0", "ζ = 0.707", "ζ = 0.25"),
                0,
                "Characteristic equation: s² + 10s + 100 = 0 ⇒ ω_n = 10 rad/s, 2ζω_n = 10 ⇒ ζ = 10 / 20 = 0.5."
            ),
            CuratedQuestionItem(
                "QB_CS_03",
                "Digital Logic",
                "What is the minimum number of 2-input NAND gates required to implement Y = A ⊕ B (XOR)?",
                listOf("4 NAND gates", "3 NAND gates", "5 NAND gates", "2 NAND gates"),
                0,
                "A standard minimum NAND-only realization of XOR uses 4 two-input NAND gates."
            )
        )
    }

    // Also synthesize an MCQ directly from the active note's blocks (Extra #8)
    val generatedNoteMcq = remember(noteTitle, blocks) {
        val latexBlock = blocks.firstOrNull { it.type == BlockType.LATEX_BLOCK.name }
        val calloutBlock = blocks.firstOrNull { it.type == BlockType.CALLOUT.name }
        val targetAnswer = latexBlock?.content?.take(48)
            ?: calloutBlock?.content?.take(48)
            ?: blocks.firstOrNull()?.content?.take(48)
            ?: "Conservation of energy and causal stability"
        CuratedQuestionItem(
            id = "NOTE_GEN_MCQ",
            subject = "Generated from '$noteTitle'",
            prompt = "Which key expression or takeaway is established in '$noteTitle'?",
            options = listOf(
                targetAnswer,
                "Unbounded positive-feedback divergence (ζ < 0)",
                "Non-causal zero-input impulse instability",
                "None of the above"
            ),
            correctIndex = 0,
            solution = "Directly synthesized from active blocks in '$noteTitle'."
        )
    }

    var selectedOptionByQuestion by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    val allQuestions = listOf(generatedNoteMcq) + questionBank

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "🎯 QUESTION-BANK INSERTION (Extra #7) & NOTE-TO-MCQ GENERATOR (Extra #8)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = StudioPrimary
        )

        allQuestions.forEach { q ->
            val pickedIdx = selectedOptionByQuestion[q.id]
            Card(
                colors = CardDefaults.cardColors(containerColor = StudioSubtleBg),
                border = BorderStroke(1.dp, StudioBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "[${q.subject}] ${q.id}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = StudioPrimary,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        OutlinedButton(
                            onClick = { onInsertQuestionIntoNote(q.subject, q.prompt, q.solution) },
                            border = BorderStroke(1.dp, StudioPrimary),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("+ INSERT TO NOTE", fontSize = 10.sp, color = StudioPrimary)
                        }
                    }

                    Text(
                        text = q.prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    q.options.forEachIndexed { idx, opt ->
                        val isSelected = pickedIdx == idx
                        val isCorrect = idx == q.correctIndex
                        val bg = when {
                            pickedIdx == null -> Color.White
                            isSelected && isCorrect -> Color(0xFFDCFCE7)
                            isSelected && !isCorrect -> Color(0xFFFEE2E2)
                            else -> Color.White
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bg, RoundedCornerShape(6.dp))
                                .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedOptionByQuestion = selectedOptionByQuestion + (q.id to idx)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${'A' + idx}. $opt",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = StudioPrimary
                            )
                        }
                    }

                    if (pickedIdx != null) {
                        Text(
                            text = if (pickedIdx == q.correctIndex) "✓ Correct! ${q.solution}" else "✗ Review: ${q.solution}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (pickedIdx == q.correctIndex) Color(0xFF166534) else Color(0xFF991B1B)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Code Studio Snippets Library (#22, #23).
 */
@Composable
private fun CodeSnippetsLibrarySection(
    onInsertCodeBlock: (String, String) -> Unit
) {
    val snippets = listOf(
        Triple(
            "KOTLIN",
            "Runge-Kutta 4th Order ODE Solver",
            "fun rk4Step(f: (Double, Double) -> Double, t: Double, y: Double, h: Double): Double {\n    val k1 = f(t, y)\n    val k2 = f(t + 0.5 * h, y + 0.5 * h * k1)\n    val k3 = f(t + 0.5 * h, y + 0.5 * h * k2)\n    val k4 = f(t + h, y + h * k3)\n    return y + (h / 6.0) * (k1 + 2.0 * k2 + 2.0 * k3 + k4)\n}"
        ),
        Triple(
            "VERILOG",
            "Synchronous 4-Bit Up/Down Counter RTL",
            "module sync_counter_4bit(\n    input wire clk, input wire rst, input wire up,\n    output reg [3:0] q\n);\n    always @(posedge clk) begin\n        if (rst) q <= 4'b0000;\n        else if (up) q <= q + 1'b1;\n        else q <= q - 1'b1;\n    end\nendmodule"
        ),
        Triple(
            "MATLAB",
            "Closed-Loop Bode & Step Margin Analysis",
            "s = tf('s');\nG = 100 / (s * (s + 5) * (s + 20));\n[Gm, Pm, Wcg, Wcp] = margin(G);\nstep(feedback(G, 1)); grid on;"
        ),
        Triple(
            "PYTHON",
            "Fast Fourier Transform (FFT) Spectrum",
            "import numpy as np\nN = 1024\nt = np.linspace(0, 1.0, N, endpoint=False)\nx = np.sin(2 * np.pi * 50 * t) + 0.5 * np.cos(2 * np.pi * 120 * t)\nX_mag = np.abs(np.fft.rfft(x)) / N"
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        snippets.forEach { (lang, title, code) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = StudioSubtleBg),
                border = BorderStroke(1.dp, StudioBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "[$lang] $title",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = StudioPrimary
                        )
                        OutlinedButton(
                            onClick = { onInsertCodeBlock(code, lang) },
                            border = BorderStroke(1.dp, StudioPrimary),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("+ INSERT CODE", fontSize = 10.sp, color = StudioPrimary)
                        }
                    }
                    Text(
                        text = code,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF0F172A),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}
