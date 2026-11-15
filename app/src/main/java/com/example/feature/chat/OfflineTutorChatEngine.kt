package com.example.feature.chat

import com.example.core.database.NoteBlockEntity
import com.example.core.database.NoteEntity
import com.example.core.latex.LatexRendererEngine
import com.example.domain.model.MathQuestion
import java.util.Locale
import java.util.UUID
import kotlin.math.PI
import kotlin.math.sqrt

data class ChatMessage(
    val id: String = "msg_${UUID.randomUUID().toString().take(8)}",
    val isUser: Boolean,
    val text: String,
    val latexAttachment: String? = null,
    val codeAttachment: String? = null,
    val codeLanguage: String? = null,
    val sourceBadge: String = "MATHGATE TUTOR",
    val timestampMs: Long = System.currentTimeMillis()
)

object OfflineTutorChatEngine {

    fun initialWelcomeMessage(activeNoteTitle: String?): ChatMessage = ChatMessage(
        isUser = false,
        text = "Welcome to MATHGATE Chat! Ask me any Math, Science, ECE/EEE/CSE/Mechanical/Civil Engineering question, type an arithmetic or algebraic expression to solve step-by-step, or search across your Notes Studio workspace" +
            (if (!activeNoteTitle.isNullOrBlank()) " (Active note: '$activeNoteTitle')." else "."),
        latexAttachment = "H(s) = \\frac{Y(s)}{X(s)} \\quad , \\quad E = mc^2",
        sourceBadge = "OFFLINE TUTOR READY"
    )

    fun generateReply(
        rawInput: String,
        currentQuestion: MathQuestion?,
        allNotes: List<NoteEntity>,
        activeNote: NoteEntity?,
        activeBlocks: List<NoteBlockEntity>
    ): ChatMessage {
        val query = rawInput.trim()
        val lower = query.lowercase(Locale.ROOT)

        // 1. Help with current active practice question
        if (lower.contains("active question") || lower.contains("current question") || lower.contains("hint")) {
            if (currentQuestion != null) {
                val firstStep = currentQuestion.explanationSteps.firstOrNull()
                    ?: "Identify the operator and simplify step-by-step."
                return ChatMessage(
                    isUser = false,
                    text = "Active Challenge (${currentQuestion.category.displayName} • ${currentQuestion.difficulty.displayName}):\n" +
                        "Prompt: ${currentQuestion.prompt}\n\n" +
                        "Approach & First Step:\n• $firstStep",
                    sourceBadge = "CHALLENGE COACH"
                )
            }
        }

        // 2. Summarize or query user's Notes Studio workspace
        if (lower.contains("summarize") || lower.contains("my note") || lower.contains("active note")) {
            if (activeNote != null) {
                val previewLines = activeBlocks.take(4).joinToString("\n") { "• [${it.type}] ${it.content.take(70)}" }
                val eqBlock = activeBlocks.firstOrNull { it.type == "LATEX_BLOCK" }?.content
                return ChatMessage(
                    isUser = false,
                    text = "Summary of '${activeNote.title}' (${activeBlocks.size} blocks in folder '${activeNote.folderId ?: "root"}'):\n$previewLines",
                    latexAttachment = eqBlock,
                    sourceBadge = "NOTES STUDIO INDEX"
                )
            }
        }

        // 3. Direct search hit inside user's Notes Studio titles or active blocks
        val matchingNote = allNotes.firstOrNull { note ->
            val words = lower.split(Regex("\\s+")).filter { it.length >= 4 }
            words.any { w -> note.title.lowercase(Locale.ROOT).contains(w) }
        }
        val matchingBlock = activeBlocks.firstOrNull { blk ->
            val words = lower.split(Regex("\\s+")).filter { it.length >= 4 }
            words.any { w -> blk.content.lowercase(Locale.ROOT).contains(w) }
        }

        // 4. RC Cutoff / Filter calculation
        if (lower.contains("cutoff") || lower.contains("low pass") || lower.contains("low-pass") || lower.contains("rc filter")) {
            val fCutoff = 1.0 / (2.0 * PI * 10_000.0 * 100e-9)
            return ChatMessage(
                isUser = false,
                text = "For a first-order RC Low-Pass Filter:\n" +
                    "• Transfer Function: H(jω) = 1 / (1 + jωRC)\n" +
                    "• -3 dB Cutoff Frequency: f_c = 1 / (2πRC)\n" +
                    "• Example (R = 10 kΩ, C = 100 nF): f_c ≈ ${String.format(Locale.US, "%.2f", fCutoff)} Hz",
                latexAttachment = "f_c = \\frac{1}{2\\pi R C} \\quad , \\quad H(j\\omega) = \\frac{1}{1 + j\\omega R C}",
                sourceBadge = "ECE CIRCUIT SOLVER"
            )
        }

        // 5. Boolean Algebra / De Morgan / Logic Gates
        if (lower.contains("boolean") || lower.contains("morgan") || lower.contains("nand") || lower.contains("k-map")) {
            return ChatMessage(
                isUser = false,
                text = "Boolean Algebra & Digital Logic Reference:\n" +
                    "• De Morgan's Laws: ~(A · B) = ~A + ~B  and  ~(A + B) = ~A · ~B\n" +
                    "• Consensus Theorem: AB + ~AC + BC = AB + ~AC\n" +
                    "• Universal Gates: NAND and NOR can synthesize any combinational function.",
                latexAttachment = "\\overline{A \\cdot B} = \\overline{A} + \\overline{B} \\quad , \\quad \\overline{A + B} = \\overline{A} \\cdot \\overline{B}",
                codeAttachment = "module universal_nand(input wire a, input wire b, output wire y);\n    assign y = ~(a & b);\nendmodule",
                codeLanguage = "VERILOG",
                sourceBadge = "DIGITAL LOGIC ENGINE"
            )
        }

        // 6. Three-Phase Power / EEE / Ohm's Law
        if (lower.contains("three phase") || lower.contains("3 phase") || lower.contains("power factor") || lower.contains("ohm")) {
            return ChatMessage(
                isUser = false,
                text = "Electrical & Power Engineering Reference:\n" +
                    "• Ohm's & Watt's Law: V = I·R , P = V·I = I²R = V²/R\n" +
                    "• Balanced 3-Phase Active Power: P = √3 · V_L · I_L · cos(φ)\n" +
                    "• Reactive Power: Q = √3 · V_L · I_L · sin(φ)",
                latexAttachment = "P = \\sqrt{3} V_L I_L \\cos\\phi \\quad , \\quad S = \\sqrt{P^2 + Q^2}",
                sourceBadge = "EEE POWER ENGINE"
            )
        }

        // 7. Calculus / Laplace / Fourier / Eigenvalues / Matrices
        if (lower.contains("laplace") || lower.contains("fourier") || lower.contains("matrix") || lower.contains("eigen") || lower.contains("integral") || lower.contains("derivative")) {
            return ChatMessage(
                isUser = false,
                text = "Engineering Mathematics & Calculus Reference:\n" +
                    "• Unilateral Laplace Transform: L{f(t)} = ∫₀^∞ f(t)e^(-st) dt\n" +
                    "• Characteristic Eigenvalue Equation: det(A - λI) = 0\n" +
                    "• For a 2×2 matrix [a b; c d]: λ² - (a+d)λ + (ad - bc) = 0",
                latexAttachment = "\\det(A - \\lambda I) = \\begin{vmatrix} a - \\lambda & b \\\\ c & d - \\lambda \\end{vmatrix} = 0",
                sourceBadge = "MATH & CALCULUS ENGINE"
            )
        }

        // 8. Algorithms / Big-O / CSE / Coding
        if (lower.contains("dijkstra") || lower.contains("binary search") || lower.contains("big-o") || lower.contains("sorting") || lower.contains("algorithm")) {
            return ChatMessage(
                isUser = false,
                text = "Computer Science & Algorithm Complexity:\n" +
                    "• Binary Search: O(log n) time, O(1) space\n" +
                    "• Merge Sort / Heap Sort: O(n log n) worst-case time\n" +
                    "• Dijkstra (Binary Heap): O((V + E) log V)",
                codeAttachment = "fun binarySearch(a: IntArray, key: Int): Int {\n    var l = 0; var r = a.lastIndex\n    while (l <= r) {\n        val m = (l + r) ushr 1\n        if (a[m] == key) return m\n        if (a[m] < key) l = m + 1 else r = m - 1\n    }\n    return -1\n}",
                codeLanguage = "KOTLIN",
                sourceBadge = "CSE ALGORITHMS"
            )
        }

        // 9. Try evaluating arithmetic / algebraic numeric expression (e.g., "25 * 16", "sqrt(144)", "15% of 240")
        val mathEval = tryEvaluateMathQuery(query)
        if (mathEval != null) {
            return mathEval
        }

        // 10. If matched a note or block in Notes Studio, return that context
        if (matchingBlock != null || matchingNote != null) {
            val noteTitle = matchingNote?.title ?: activeNote?.title ?: "Workspace Note"
            val snippet = matchingBlock?.content ?: "Found matching note '$noteTitle' in your offline workspace."
            return ChatMessage(
                isUser = false,
                text = "From your Notes Studio ('$noteTitle'):\n• ${LatexRendererEngine.formatInlineSymbols(snippet)}",
                latexAttachment = if (matchingBlock?.type == "LATEX_BLOCK") matchingBlock.content else null,
                sourceBadge = "WORKSPACE SEARCH"
            )
        }

        // Default structured tutor response
        return ChatMessage(
            isUser = false,
            text = "Here is how I can help with \"$query\":\n" +
                "1. Solve expressions: type `24 * 15`, `25% of 360`, or `sqrt(256)`.\n" +
                "2. Engineering & Math formulas: ask about `RC filter`, `Boolean algebra`, `3 phase power`, `Laplace transform`, `Eigenvalues`, or `Binary search`.\n" +
                "3. Practice & Notes: tap `Explain Active Question` or `Summarize My Notes` below, or tap `INSERT TO NOTE` on any reply to save it into Notes Studio.",
            latexAttachment = "\\nabla \\cdot \\vec{E} = \\frac{\\rho}{\\varepsilon_0}",
            sourceBadge = "MATHGATE TUTOR"
        )
    }

    private fun tryEvaluateMathQuery(query: String): ChatMessage? {
        val clean = query.lowercase(Locale.ROOT).removePrefix("solve").removePrefix("calculate").trim()

        // Percentage pattern: "X% of Y"
        val pctMatch = Regex("^(\\d+(?:\\.\\d+)?)\\s*%\\s*of\\s*(\\d+(?:\\.\\d+)?)$").find(clean)
        if (pctMatch != null) {
            val p = pctMatch.groupValues[1].toDoubleOrNull() ?: return null
            val base = pctMatch.groupValues[2].toDoubleOrNull() ?: return null
            val res = (p / 100.0) * base
            val formatted = if (res % 1.0 == 0.0) res.toLong().toString() else String.format(Locale.US, "%.4f", res).trimEnd('0').trimEnd('.')
            return ChatMessage(
                isUser = false,
                text = "Step-by-Step Percentage Calculation:\n" +
                    "1. Convert $p% to decimal/fraction: $p / 100\n" +
                    "2. Multiply by $base: ($p / 100) × $base = $formatted",
                latexAttachment = "\\frac{$p}{100} \\times $base = $formatted",
                sourceBadge = "STEP-BY-STEP SOLVER"
            )
        }

        // Square root pattern: "sqrt(X)" or "√X"
        val sqrtMatch = Regex("^(?:sqrt\\(|√)\\s*(\\d+(?:\\.\\d+)?)\\)?$").find(clean)
        if (sqrtMatch != null) {
            val v = sqrtMatch.groupValues[1].toDoubleOrNull() ?: return null
            val r = sqrt(v)
            val formatted = if (r % 1.0 == 0.0) r.toLong().toString() else String.format(Locale.US, "%.4f", r)
            return ChatMessage(
                isUser = false,
                text = "Principal Square Root:\n• √$v = $formatted",
                latexAttachment = "\\sqrt{$v} = $formatted",
                sourceBadge = "STEP-BY-STEP SOLVER"
            )
        }

        // Binary arithmetic: "A + B", "A - B", "A * B", "A / B", "A ^ B"
        val binMatch = Regex("^(-?\\d+(?:\\.\\d+)?)\\s*([+\\-*/×÷^])\\s*(-?\\d+(?:\\.\\d+)?)$").find(clean)
        if (binMatch != null) {
            val a = binMatch.groupValues[1].toDoubleOrNull() ?: return null
            val op = binMatch.groupValues[2]
            val b = binMatch.groupValues[3].toDoubleOrNull() ?: return null
            val res = when (op) {
                "+" -> a + b
                "-" -> a - b
                "*", "×" -> a * b
                "/", "÷" -> if (b != 0.0) a / b else return ChatMessage(
                    isUser = false,
                    text = "Division by zero is undefined.",
                    sourceBadge = "MATH VALIDATOR"
                )
                "^" -> Math.pow(a, b)
                else -> return null
            }
            val formatted = if (res % 1.0 == 0.0) res.toLong().toString() else String.format(Locale.US, "%.4f", res).trimEnd('0').trimEnd('.')
            return ChatMessage(
                isUser = false,
                text = "Evaluated Expression:\n• $a $op $b = $formatted",
                latexAttachment = "$a $op $b = $formatted",
                sourceBadge = "STEP-BY-STEP SOLVER"
            )
        }
        return null
    }
}
