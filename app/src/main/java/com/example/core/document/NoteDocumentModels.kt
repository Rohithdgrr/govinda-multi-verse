package com.example.core.document

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * All 26 required block types for the MATHGATE Block-Based Document Editor.
 */
enum class BlockType(
    val displayName: String,
    val badge: String,
    val defaultContent: String
) {
    HEADING_1("Heading 1", "H1", "Section Heading"),
    HEADING_2("Heading 2", "H2", "Sub-section Heading"),
    HEADING_3("Heading 3", "H3", "Topic Header"),
    PARAGRAPH("Paragraph", "¶", "Write technical notes, definitions, or derivations here."),
    BULLETED_LIST("Bulleted List", "•", "First key point\nSecond key point"),
    NUMBERED_LIST("Numbered List", "1.", "Step one\nStep two"),
    CHECKLIST("Checklist", "☑", "[x] Verify boundary conditions\n[ ] Solve characteristic equation"),
    TOGGLE_SECTION("Toggle Section", "▸", "Derivation Details::Expand to view step-by-step algebraic simplification."),
    QUOTE("Quote", "❝", "An equation means nothing to me unless it expresses a thought of God. — S. Ramanujan"),
    CALLOUT("Callout", "💡", "IMPORTANT: Ensure all units are converted to standard SI before substituting."),
    DIVIDER("Divider", "—", "---"),
    TABLE("Table", "⊞", ""),
    IMAGE("Image", "🖼", ""),
    IMAGE_GALLERY("Image Gallery", "⊞🖼", ""),
    LATEX_BLOCK("LaTeX Equation", "∑", "H(s) = \\frac{Y(s)}{X(s)} = \\frac{\\omega_n^2}{s^2 + 2\\zeta\\omega_n s + \\omega_n^2}"),
    INLINE_MATH("Inline Math", "$", "${'$'}E = mc^2${'$'} and ${'$'}V = IR${'$'}"),
    CODE_BLOCK("Code Block", "</>", "fun transferGain(rf: Double, r1: Double): Double = 1.0 + (rf / r1)"),
    INLINE_CODE("Inline Code", "`c`", "Use `O(n log n)` merge sort for stable ordering."),
    HYPERLINK("Hyperlink", "🔗", "https://ncert.nic.in/textbook.php|NCERT Official Textbooks"),
    BOOKMARK("Bookmark", "🔖", "[[note_boolean_algebra|Boolean Algebra & De Morgan's Laws]]"),
    EMBEDDED_WEBPAGE("Embedded Webpage", "🌐", "https://phet.colorado.edu|PhET Interactive Physics Simulations"),
    EMBEDDED_VIDEO("Embedded Video", "▶", "https://www.youtube.com/watch?v=aircAruvnKk|3Blue1Brown Neural Networks Lesson"),
    FILE_ATTACHMENT("File Attachment", "📎", "lab_oscilloscope_trace.csv|24 KB CSV Dataset"),
    MERMAID_DIAGRAM("Mermaid Diagram", "◇", "graph LR\n  Input[x(t)] --> System[H(s)]\n  System --> Output[y(t)]"),
    ENGINEERING_SCHEMATIC("Engineering Schematic", "⚡", "RC_LOW_PASS|R = 10 kΩ, C = 100 nF, f_c = 159.15 Hz"),
    AUDIO_ATTACHMENT("Audio Attachment", "🎙", "lecture_control_systems_01.m4a|03:45 Voice Memo");

    companion object {
        fun fromName(raw: String): BlockType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: PARAGRAPH
    }
}

data class NoteBlock(
    val id: String = UUID.randomUUID().toString(),
    val noteId: String,
    val type: BlockType,
    val position: Int,
    val content: String,
    val metadata: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class CodeLanguage(val displayName: String, val keywords: Set<String>) {
    KOTLIN("Kotlin", setOf("fun", "val", "var", "class", "object", "return", "if", "else", "when", "for", "suspend")),
    PYTHON("Python", setOf("def", "return", "import", "from", "class", "if", "elif", "else", "for", "in", "while")),
    CPP("C++", setOf("int", "double", "void", "return", "include", "std", "vector", "for", "while", "const")),
    C("C", setOf("int", "float", "char", "void", "return", "struct", "for", "while", "if", "else")),
    JAVA("Java", setOf("public", "private", "class", "static", "void", "int", "double", "return", "new")),
    JAVASCRIPT("JavaScript", setOf("function", "const", "let", "return", "async", "await", "export", "import")),
    TYPESCRIPT("TypeScript", setOf("interface", "type", "function", "const", "let", "return", "number", "string")),
    HTML("HTML", setOf("html", "head", "body", "div", "span", "table", "section", "script")),
    CSS("CSS", setOf("display", "flex", "grid", "color", "background", "padding", "margin", "border")),
    SQL("SQL", setOf("SELECT", "FROM", "WHERE", "JOIN", "GROUP", "BY", "ORDER", "INSERT", "CREATE", "INDEX")),
    JSON("JSON", emptySet()),
    XML("XML", emptySet()),
    BASH("Bash", setOf("echo", "export", "if", "then", "fi", "for", "do", "done", "grep", "awk")),
    MATLAB("MATLAB", setOf("function", "end", "for", "plot", "linspace", "zeros", "ones", "tf", "bode")),
    VERILOG("Verilog", setOf("module", "endmodule", "input", "output", "wire", "reg", "always", "posedge", "assign")),
    VHDL("VHDL", setOf("entity", "architecture", "port", "signal", "begin", "end", "process", "std_logic"))
}

data class TableData(
    val columns: List<String>,
    val rows: List<List<String>>,
    val alignments: List<String> = columns.map { "LEFT" }
) {
    fun toJson(): String {
        val root = JSONObject()
        root.put("columns", JSONArray(columns))
        val rowsArr = JSONArray()
        rows.forEach { r -> rowsArr.put(JSONArray(r)) }
        root.put("rows", rowsArr)
        root.put("alignments", JSONArray(alignments))
        return root.toString()
    }

    fun toCsv(): String {
        val all = listOf(columns) + rows
        return all.joinToString("\n") { row ->
            row.joinToString(",") { cell ->
                if (cell.contains(",") || cell.contains("\"")) "\"${cell.replace("\"", "\"\"")}\"" else cell
            }
        }
    }

    fun toMarkdownTable(): String {
        val header = "| " + columns.joinToString(" | ") + " |"
        val sep = "| " + columns.mapIndexed { idx, _ ->
            when (alignments.getOrNull(idx)) {
                "CENTER" -> ":---:"
                "RIGHT" -> "---:"
                else -> "---"
            }
        }.joinToString(" | ") + " |"
        val body = rows.joinToString("\n") { r ->
            "| " + columns.indices.map { i -> r.getOrNull(i).orEmpty() }.joinToString(" | ") + " |"
        }
        return listOf(header, sep, body).filter { it.isNotBlank() }.joinToString("\n")
    }

    companion object {
        fun defaultComponentTable(): TableData = TableData(
            columns = listOf("Component", "Function", "Important parameter"),
            rows = listOf(
                listOf("Resistor", "Limits current", "Resistance (Ω)"),
                listOf("Capacitor", "Stores electric charge", "Capacitance (F)"),
                listOf("Inductor", "Stores magnetic energy", "Inductance (H)")
            )
        )

        fun fromJsonOrDefault(raw: String): TableData = runCatching {
            if (raw.isBlank()) return defaultComponentTable()
            val obj = JSONObject(raw)
            val colsArr = obj.getJSONArray("columns")
            val cols = (0 until colsArr.length()).map { colsArr.getString(it) }
            val rowsArr = obj.getJSONArray("rows")
            val rows = (0 until rowsArr.length()).map { rIdx ->
                val rArr = rowsArr.getJSONArray(rIdx)
                (0 until cols.size).map { cIdx -> rArr.optString(cIdx, "") }
            }
            val alignArr = obj.optJSONArray("alignments")
            val aligns = if (alignArr != null) {
                (0 until cols.size).map { alignArr.optString(it, "LEFT") }
            } else {
                cols.map { "LEFT" }
            }
            TableData(cols, rows, aligns)
        }.getOrElse { defaultComponentTable() }

        fun fromCsv(csvText: String): TableData {
            val lines = csvText.lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) return defaultComponentTable()
            val parsed = lines.map { line -> line.split(",").map { it.trim().removeSurrounding("\"") } }
            val cols = parsed.first()
            val rows = parsed.drop(1).map { r -> cols.indices.map { idx -> r.getOrNull(idx).orEmpty() } }
            return TableData(cols, if (rows.isEmpty()) listOf(cols.map { "" }) else rows)
        }
    }
}

object NoteTemplateCatalog {
    data class TemplateSpec(
        val id: String,
        val category: String,
        val title: String,
        val subtitle: String,
        val blocks: List<Pair<BlockType, String>>
    )

    val allTemplates: List<TemplateSpec> = listOf(
        // Academic templates
        TemplateSpec(
            "tpl_class_notes", "ACADEMIC", "Class Notes", "Structured lecture headings, definitions, equations & summary",
            listOf(
                BlockType.HEADING_1 to "Lecture Topic & Objectives",
                BlockType.PARAGRAPH to "Core concept definitions and physical significance.",
                BlockType.LATEX_BLOCK to "F = m \\cdot a \\quad , \\quad W = \\int_{x_1}^{x_2} F(x)\\,dx",
                BlockType.CHECKLIST to "[x] Review textbook derivation\n[ ] Solve 5 numerical problems"
            )
        ),
        TemplateSpec(
            "tpl_chapter_summary", "ACADEMIC", "Chapter Summary", "Condensed chapter key takeaways, table & formula block",
            listOf(
                BlockType.HEADING_1 to "Chapter Key Takeaways",
                BlockType.BULLETED_LIST to "Fundamental conservation laws\nBoundary conditions and assumptions",
                BlockType.TABLE to TableData.defaultComponentTable().toJson()
            )
        ),
        TemplateSpec(
            "tpl_formula_sheet", "ACADEMIC", "Formula Sheet", "High-density LaTeX formula reference sheet",
            listOf(
                BlockType.HEADING_1 to "Master Formula Sheet",
                BlockType.LATEX_BLOCK to "\\nabla \\cdot \\vec{E} = \\frac{\\rho}{\\varepsilon_0} \\quad , \\quad \\nabla \\times \\vec{B} = \\mu_0 \\vec{J} + \\mu_0 \\varepsilon_0 \\frac{\\partial \\vec{E}}{\\partial t}",
                BlockType.CALLOUT to "Verify dimensional homogeneity before final substitution."
            )
        ),
        TemplateSpec(
            "tpl_lab_observation", "ACADEMIC", "Laboratory Observation", "Aim, apparatus, observation table & error calculation",
            listOf(
                BlockType.HEADING_1 to "Experiment & Apparatus",
                BlockType.PARAGRAPH to "Objective: Verify Ohm's law and measure unknown resistance.",
                BlockType.TABLE to TableData(
                    columns = listOf("Trial", "Voltage V (V)", "Current I (A)", "R = V/I (Ω)"),
                    rows = listOf(listOf("1", "2.0", "0.20", "10.0"), listOf("2", "4.0", "0.40", "10.0"))
                ).toJson()
            )
        ),
        TemplateSpec(
            "tpl_exam_prep", "ACADEMIC", "Exam Preparation & Revision", "GATE / JEE / Semester high-yield revision checklist",
            listOf(
                BlockType.HEADING_1 to "High-Yield Exam Checklist",
                BlockType.CHECKLIST to "[x] Revise Laplace & Fourier transforms\n[ ] Practice 20 timed numericals\n[ ] Review previous year mistakes",
                BlockType.LATEX_BLOCK to "\\mathcal{L}\\{f(t)\\} = \\int_0^{\\infty} f(t) e^{-st}\\,dt"
            )
        ),
        // Engineering templates
        TemplateSpec(
            "tpl_circuit_design", "ENGINEERING", "Circuit Design Documentation", "Schematic, transfer function, BOM table & test bench",
            listOf(
                BlockType.HEADING_1 to "RC Low-Pass Filter Design",
                BlockType.ENGINEERING_SCHEMATIC to "RC_LOW_PASS|R = 10 kΩ, C = 100 nF, f_c = 159.15 Hz",
                BlockType.LATEX_BLOCK to "H(j\\omega) = \\frac{1}{1 + j\\omega R C} \\quad , \\quad f_c = \\frac{1}{2\\pi R C}",
                BlockType.TABLE to TableData.defaultComponentTable().toJson()
            )
        ),
        TemplateSpec(
            "tpl_embedded_mcu", "ENGINEERING", "Microcontroller & Embedded Notes", "Pinout, register map, ISR code & timing diagram",
            listOf(
                BlockType.HEADING_1 to "Timer PWM & Interrupt Configuration",
                BlockType.CODE_BLOCK to "void TIM2_IRQHandler(void) {\n    if (TIM2->SR & TIM_SR_UIF) {\n        TIM2->SR &= ~TIM_SR_UIF;\n        GPIOA->ODR ^= (1U << 5);\n    }\n}",
                BlockType.MERMAID_DIAGRAM to "graph LR\n  Clock[SYSCLK 72MHz] --> Prescaler[PSC /72]\n  Prescaler --> Counter[CNT 1MHz]\n  Counter --> Compare[CCR1 PWM]"
            )
        ),
        TemplateSpec(
            "tpl_component_datasheet", "ENGINEERING", "Component Datasheet Notes", "Absolute maximum ratings, pin specs & application circuit",
            listOf(
                BlockType.HEADING_1 to "Op-Amp LM741 / TL082 Quick Spec",
                BlockType.TABLE to TableData(
                    columns = listOf("Parameter", "Symbol", "Typical Value"),
                    rows = listOf(
                        listOf("Open-Loop Gain", "A_OL", "100 dB"),
                        listOf("Slew Rate", "SR", "13 V/μs"),
                        listOf("CMRR", "CMRR", "86 dB")
                    )
                ).toJson(),
                BlockType.LATEX_BLOCK to "A_v = 1 + \\frac{R_f}{R_1}"
            )
        ),
        // Mathematics templates
        TemplateSpec(
            "tpl_math_derivation", "MATHEMATICS", "Theorem, Proof & Derivation", "Statement, assumptions, step-by-step LaTeX proof & Q.E.D.",
            listOf(
                BlockType.HEADING_1 to "Fundamental Theorem & Eigenvalue Identity",
                BlockType.LATEX_BLOCK to "\\det(A - \\lambda I) = 0 \\implies \\begin{vmatrix} a - \\lambda & b \\\\ c & d - \\lambda \\end{vmatrix} = 0",
                BlockType.TOGGLE_SECTION to "Characteristic Polynomial Proof::Expanding the 2×2 determinant gives λ² − tr(A)λ + det(A) = 0."
            )
        ),
        TemplateSpec(
            "tpl_solved_problem", "MATHEMATICS", "Solved Numerical Problem", "Given data, formula, step-by-step substitution & verification",
            listOf(
                BlockType.HEADING_1 to "Solved Problem — Second-Order Step Response",
                BlockType.CALLOUT to "Given: ω_n = 10 rad/s, ζ = 0.5. Find damped natural frequency ω_d.",
                BlockType.LATEX_BLOCK to "\\omega_d = \\omega_n \\sqrt{1 - \\zeta^2} = 10 \\sqrt{1 - 0.25} = 8.66\\text{ rad/s}"
            )
        ),
        // Personal templates
        TemplateSpec(
            "tpl_study_planner", "PERSONAL", "Daily Study & Sprint Planner", "Time-blocked goals, active recall checklist & reflection",
            listOf(
                BlockType.HEADING_1 to "Daily Engineering & Math Sprint",
                BlockType.CHECKLIST to "[x] 08:00 — 15 Adaptive Calculus & Matrix Questions\n[ ] 10:00 — Digital Electronics K-Map Notes\n[ ] 16:00 — Mock GATE Timed Set",
                BlockType.QUOTE to "Consistency compounds faster than intensity."
            )
        )
    )
}
