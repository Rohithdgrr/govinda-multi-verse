package com.example.domain.evaluator

import com.example.domain.model.AnswerFormat
import com.example.domain.model.Fraction
import com.example.domain.model.MathQuestion
import java.math.BigDecimal

data class EvaluationResult(
    val isCorrect: Boolean,
    val normalizedSubmitted: String,
    val canonicalExpected: String,
    val note: String? = null
)

class AnswerEvaluator {

    fun evaluate(question: MathQuestion, rawInput: String): EvaluationResult {
        val rawTrimmed = rawInput.trim()
        if (rawTrimmed.isEmpty()) {
            return EvaluationResult(
                isCorrect = false,
                normalizedSubmitted = "",
                canonicalExpected = question.answer,
                note = "No answer entered"
            )
        }
        if (question.answerFormat == AnswerFormat.MULTIPLE_CHOICE) {
            val cleanInput = rawTrimmed.take(MAX_MCQ_LENGTH)
            val expected = question.answer.trim()
            val matchedByText = cleanInput.equals(expected, ignoreCase = true)
            val matchedByIndex = cleanInput.toIntOrNull()?.let { idx ->
                question.options.getOrNull(idx - 1)?.trim()?.equals(expected, ignoreCase = true) == true
            } ?: false
            return EvaluationResult(
                isCorrect = matchedByText || matchedByIndex,
                normalizedSubmitted = cleanInput,
                canonicalExpected = expected
            )
        }

        val trimmed = rawTrimmed.replace(" ", "")
        if (trimmed.length > MAX_ANSWER_LENGTH || trimmed.any { it == 'e' || it == 'E' }) {
            return EvaluationResult(
                isCorrect = false,
                normalizedSubmitted = trimmed.take(MAX_ANSWER_LENGTH),
                canonicalExpected = question.answer,
                note = "Invalid numeric format"
            )
        }

        return when (question.answerFormat) {
            AnswerFormat.INTEGER -> evaluateInteger(question.answer, trimmed)
            AnswerFormat.DECIMAL -> evaluateDecimal(question.answer, trimmed)
            AnswerFormat.FRACTION -> evaluateFraction(question.answer, trimmed)
            AnswerFormat.MULTIPLE_CHOICE -> EvaluationResult(
                isCorrect = rawTrimmed.equals(question.answer.trim(), ignoreCase = true),
                normalizedSubmitted = rawTrimmed,
                canonicalExpected = question.answer
            )
        }
    }

    private fun evaluateInteger(expectedStr: String, input: String): EvaluationResult {
        val expected = expectedStr.toBigIntegerOrNull()
        val parsedInput = input.toBigIntegerOrNull()
        if (expected != null && parsedInput != null) {
            return EvaluationResult(
                isCorrect = parsedInput == expected,
                normalizedSubmitted = parsedInput.toString(),
                canonicalExpected = expected.toString()
            )
        }
        // Fallback: accept "12.0" when expected is "12"
        val expectedBd = expectedStr.toBigDecimalOrNull()
        val inputBd = input.toBigDecimalOrNull()
        if (expectedBd != null && inputBd != null && expectedBd.compareTo(inputBd) == 0) {
            return EvaluationResult(
                isCorrect = true,
                normalizedSubmitted = inputBd.stripTrailingZerosSafe(),
                canonicalExpected = expectedStr
            )
        }
        return EvaluationResult(
            isCorrect = false,
            normalizedSubmitted = input,
            canonicalExpected = expectedStr
        )
    }

    private fun evaluateDecimal(expectedStr: String, input: String): EvaluationResult {
        val expectedBd = expectedStr.toBigDecimalOrNull()
        val inputBd = input.toBigDecimalOrNull()
        if (expectedBd != null && inputBd != null) {
            return EvaluationResult(
                isCorrect = expectedBd.compareTo(inputBd) == 0,
                normalizedSubmitted = inputBd.stripTrailingZerosSafe(),
                canonicalExpected = expectedBd.stripTrailingZerosSafe()
            )
        }
        return EvaluationResult(
            isCorrect = false,
            normalizedSubmitted = input,
            canonicalExpected = expectedStr
        )
    }

    private fun BigDecimal.stripTrailingZerosSafe(): String =
        if (compareTo(BigDecimal.ZERO) == 0) "0" else stripTrailingZeros().toPlainString()

    companion object {
        private const val MAX_ANSWER_LENGTH = 32
        private const val MAX_MCQ_LENGTH = 160
    }

    private fun evaluateFraction(expectedStr: String, input: String): EvaluationResult {
        val expectedFrac = Fraction.parse(expectedStr)
        val inputFrac = Fraction.parse(input)
        if (expectedFrac != null && inputFrac != null) {
            val isExact = expectedFrac.normalized() == inputFrac.normalized()
            val note = if (isExact && input != expectedFrac.toCanonicalString()) {
                "Equivalent to simplified ${expectedFrac.toCanonicalString()}"
            } else {
                null
            }
            return EvaluationResult(
                isCorrect = isExact,
                normalizedSubmitted = input,
                canonicalExpected = expectedFrac.toCanonicalString(),
                note = note
            )
        }
        return EvaluationResult(
            isCorrect = false,
            normalizedSubmitted = input,
            canonicalExpected = expectedStr
        )
    }
}
