package com.example.core.latex

/**
 * Offline-First Mathematical & LaTeX Rendering / Validation Engine for MATHGATE.
 * Supports:
 * - Inline ($...$) and block (\[ ... \], $$...$$) LaTeX
 * - Fractions (\frac{a}{b}), integrals (\int_a^b), summations (\sum_{i=1}^n), limits (\lim_{x \to 0})
 * - Matrices & determinants (\begin{bmatrix} ... \end{bmatrix}, \begin{vmatrix} ... \end{vmatrix})
 * - Derivatives, partial derivatives (\partial), vectors (\vec{x}), Greek symbols, Physics/ECE/Chemistry notation
 * - Syntax error reporting for unbalanced braces or environments
 */
data class ParsedLatexAst(
    val rawSource: String,
    val isValid: Boolean,
    val errorMessage: String? = null,
    val topFraction: String? = null,
    val bottomFraction: String? = null,
    val prefixExpression: String = "",
    val suffixExpression: String = "",
    val matrixRows: List<List<String>>? = null,
    val isDeterminant: Boolean = false,
    val unicodeFormatted: String
)

object LatexRendererEngine {

    private val symbolReplacements = listOf(
        "\\alpha" to "α",
        "\\beta" to "β",
        "\\gamma" to "γ",
        "\\delta" to "δ",
        "\\Delta" to "Δ",
        "\\varepsilon" to "ε",
        "\\epsilon" to "ϵ",
        "\\zeta" to "ζ",
        "\\eta" to "η",
        "\\theta" to "θ",
        "\\Theta" to "Θ",
        "\\lambda" to "λ",
        "\\Lambda" to "Λ",
        "\\mu" to "μ",
        "\\nu" to "ν",
        "\\xi" to "ξ",
        "\\pi" to "π",
        "\\Pi" to "∏",
        "\\rho" to "ρ",
        "\\sigma" to "σ",
        "\\Sigma" to "Σ",
        "\\tau" to "τ",
        "\\phi" to "φ",
        "\\Phi" to "Φ",
        "\\chi" to "χ",
        "\\psi" to "ψ",
        "\\Psi" to "Ψ",
        "\\omega" to "ω",
        "\\Omega" to "Ω",
        "\\partial" to "∂",
        "\\nabla" to "∇",
        "\\infty" to "∞",
        "\\int" to "∫",
        "\\oint" to "∮",
        "\\sum" to "∑",
        "\\prod" to "∏",
        "\\sqrt" to "√",
        "\\pm" to "±",
        "\\mp" to "∓",
        "\\times" to "×",
        "\\cdot" to "·",
        "\\div" to "÷",
        "\\neq" to "≠",
        "\\leq" to "≤",
        "\\le" to "≤",
        "\\geq" to "≥",
        "\\ge" to "≥",
        "\\approx" to "≈",
        "\\equiv" to "≡",
        "\\propto" to "∝",
        "\\implies" to "⟹",
        "\\rightarrow" to "→",
        "\\to" to "→",
        "\\leftarrow" to "←",
        "\\forall" to "∀",
        "\\exists" to "∃",
        "\\in" to "∈",
        "\\quad" to "   ",
        "\\," to " "
    )

    private val superscripts = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        '+' to '⁺', '-' to '⁻', 'n' to 'ⁿ', 'i' to 'ⁱ', 'T' to 'ᵀ'
    )

    private val subscripts = mapOf(
        '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
        '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
        '+' to '₊', '-' to '₋', 'n' to 'ₙ', 'i' to 'ᵢ', 'j' to 'ⱼ',
        'a' to 'ₐ', 'e' to 'ₑ', 'o' to 'ₒ', 'x' to 'ₓ'
    )

    fun validateLatex(raw: String): String? {
        val stripped = raw.trim()
        if (stripped.isEmpty()) return "LaTeX expression is empty."
        var braceDepth = 0
        for (ch in stripped) {
            if (ch == '{') braceDepth++
            else if (ch == '}') braceDepth--
            if (braceDepth < 0) return "Syntax Error: Unexpected closing brace '}'."
        }
        if (braceDepth != 0) return "Syntax Error: Unbalanced '{' braces ($braceDepth unclosed)."

        val beginCount = Regex("\\\\begin\\{([^}]+)\\}").findAll(stripped).map { it.groupValues[1] }.toList()
        val endCount = Regex("\\\\end\\{([^}]+)\\}").findAll(stripped).map { it.groupValues[1] }.toList()
        if (beginCount != endCount) {
            return "Syntax Error: Mismatched \\begin${beginCount} and \\end${endCount} environments."
        }
        if (Regex("\\\\frac\\s*(?!\\{)").containsMatchIn(stripped)) {
            return "Syntax Error: \\frac requires two brace arguments \\frac{num}{den}."
        }
        return null
    }

    fun parse(raw: String): ParsedLatexAst {
        val cleaned = raw.trim()
            .removePrefix("\\[").removeSuffix("\\]")
            .removePrefix("$$").removeSuffix("$$")
            .removePrefix("$").removeSuffix("$")
            .trim()

        val error = validateLatex(cleaned)
        if (error != null) {
            return ParsedLatexAst(
                rawSource = raw,
                isValid = false,
                errorMessage = error,
                unicodeFormatted = cleaned
            )
        }

        // Check for matrix or determinant environment
        val matrixMatch = Regex("\\\\begin\\{(bmatrix|pmatrix|vmatrix|matrix)\\}(.+?)\\\\end\\{\\1\\}", RegexOption.DOT_MATCHES_ALL)
            .find(cleaned)
        if (matrixMatch != null) {
            val envType = matrixMatch.groupValues[1]
            val body = matrixMatch.groupValues[2].trim()
            val rows = body.split("\\\\").map { it.trim() }.filter { it.isNotEmpty() }.map { rowStr ->
                rowStr.split("&").map { cell -> formatInlineSymbols(cell.trim()) }
            }
            val prefix = formatInlineSymbols(cleaned.substring(0, matrixMatch.range.first).trim())
            val suffix = formatInlineSymbols(cleaned.substring(matrixMatch.range.last + 1).trim())
            return ParsedLatexAst(
                rawSource = raw,
                isValid = true,
                prefixExpression = prefix,
                suffixExpression = suffix,
                matrixRows = rows,
                isDeterminant = envType == "vmatrix",
                unicodeFormatted = formatInlineSymbols(cleaned)
            )
        }

        // Check for primary top-level \frac{num}{den}
        val fracMatch = Regex("^(.*?)\\\\frac\\{([^{}]+)\\}\\{([^{}]+)\\}(.*)$").find(cleaned)
        if (fracMatch != null) {
            val prefix = formatInlineSymbols(fracMatch.groupValues[1].trim())
            val num = formatInlineSymbols(fracMatch.groupValues[2].trim())
            val den = formatInlineSymbols(fracMatch.groupValues[3].trim())
            val suffix = formatInlineSymbols(fracMatch.groupValues[4].trim())
            return ParsedLatexAst(
                rawSource = raw,
                isValid = true,
                topFraction = num,
                bottomFraction = den,
                prefixExpression = prefix,
                suffixExpression = suffix,
                unicodeFormatted = if (prefix.isNotEmpty()) "$prefix ($num) / ($den) $suffix".trim() else "($num) / ($den) $suffix".trim()
            )
        }

        return ParsedLatexAst(
            rawSource = raw,
            isValid = true,
            unicodeFormatted = formatInlineSymbols(cleaned)
        )
    }

    fun formatInlineSymbols(input: String): String {
        var result = input
            .replace("\\left(", "(")
            .replace("\\right)", ")")
            .replace("\\left[", "[")
            .replace("\\right]", "]")

        // Vector \vec{F} -> F⃗
        result = Regex("\\\\vec\\{([^{}]+)\\}").replace(result) { "${it.groupValues[1]}\u20D7" }
        // Text \text{...} -> ...
        result = Regex("\\\\text\\{([^{}]+)\\}").replace(result) { it.groupValues[1] }
        // Calligraphic \mathcal{L} -> ℒ
        result = result.replace("\\mathcal{L}", "ℒ").replace("\\mathcal{F}", "ℱ")
        // Inline \frac{a}{b} -> (a)/(b)
        result = Regex("\\\\frac\\{([^{}]+)\\}\\{([^{}]+)\\}").replace(result) {
            "(${it.groupValues[1]})/(${it.groupValues[2]})"
        }

        for ((cmd, sym) in symbolReplacements) {
            result = result.replace(cmd, sym)
        }

        // Convert ^{...} and ^x
        result = Regex("\\^\\{([^{}]+)\\}").replace(result) { m ->
            val inner = m.groupValues[1]
            if (inner.all { it in superscripts }) inner.map { superscripts[it]!! }.joinToString("")
            else "^($inner)"
        }
        result = Regex("\\^([0-9niT+-])").replace(result) { m ->
            superscripts[m.groupValues[1].first()]?.toString() ?: m.value
        }

        // Convert _{...} and _x
        result = Regex("_\\{([^{}]+)\\}").replace(result) { m ->
            val inner = m.groupValues[1]
            if (inner.all { it in subscripts }) inner.map { subscripts[it]!! }.joinToString("")
            else "_($inner)"
        }
        result = Regex("_([0-9nijaeox+-])").replace(result) { m ->
            subscripts[m.groupValues[1].first()]?.toString() ?: m.value
        }

        return result.replace(Regex("\\s+"), " ").trim()
    }
}
