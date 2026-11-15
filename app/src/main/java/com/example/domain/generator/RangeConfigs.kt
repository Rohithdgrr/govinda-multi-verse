package com.example.domain.generator

import com.example.domain.model.Difficulty

data class IntegerRangeConfig(
    val operandMin: Int,
    val operandMax: Int,
    val allowNegativeResult: Boolean = false,
    val expectedTimeSeconds: Int
)

data class MultiplicationRangeConfig(
    val leftMin: Int,
    val leftMax: Int,
    val rightMin: Int,
    val rightMax: Int,
    val expectedTimeSeconds: Int
)

data class DivisionRangeConfig(
    val divisorMin: Int,
    val divisorMax: Int,
    val quotientMin: Int,
    val quotientMax: Int,
    val expectedTimeSeconds: Int
)

data class PercentageRangeConfig(
    val percentages: List<Int>,
    val baseMin: Int,
    val baseMax: Int,
    val baseStep: Int,
    val expectedTimeSeconds: Int
)

data class FractionRangeConfig(
    val denominatorMin: Int,
    val denominatorMax: Int,
    val allowOperations: Boolean,
    val allowDivision: Boolean,
    val expectedTimeSeconds: Int
)

data class DecimalRangeConfig(
    val scale: Int,
    val wholeMin: Int,
    val wholeMax: Int,
    val allowMultiplication: Boolean,
    val expectedTimeSeconds: Int
)

object MathDifficultyRanges {

    fun additionConfig(difficulty: Difficulty): IntegerRangeConfig = when (difficulty) {
        Difficulty.BEGINNER -> IntegerRangeConfig(1, 9, expectedTimeSeconds = 6)
        Difficulty.BASIC -> IntegerRangeConfig(10, 99, expectedTimeSeconds = 10)
        Difficulty.INTERMEDIATE -> IntegerRangeConfig(100, 999, expectedTimeSeconds = 15)
        Difficulty.ADVANCED -> IntegerRangeConfig(1000, 9999, expectedTimeSeconds = 22)
        Difficulty.EXPERT -> IntegerRangeConfig(10000, 99999, expectedTimeSeconds = 30)
    }

    fun subtractionConfig(
        difficulty: Difficulty,
        allowNegative: Boolean
    ): IntegerRangeConfig = when (difficulty) {
        Difficulty.BEGINNER -> IntegerRangeConfig(1, 9, allowNegative, expectedTimeSeconds = 7)
        Difficulty.BASIC -> IntegerRangeConfig(10, 99, allowNegative, expectedTimeSeconds = 11)
        Difficulty.INTERMEDIATE -> IntegerRangeConfig(100, 999, allowNegative, expectedTimeSeconds = 16)
        Difficulty.ADVANCED -> IntegerRangeConfig(1000, 9999, allowNegative, expectedTimeSeconds = 24)
        Difficulty.EXPERT -> IntegerRangeConfig(10000, 99999, allowNegative, expectedTimeSeconds = 32)
    }

    fun multiplicationConfig(difficulty: Difficulty): MultiplicationRangeConfig = when (difficulty) {
        Difficulty.BEGINNER -> MultiplicationRangeConfig(2, 9, 2, 9, expectedTimeSeconds = 7)
        Difficulty.BASIC -> MultiplicationRangeConfig(2, 12, 2, 12, expectedTimeSeconds = 10)
        Difficulty.INTERMEDIATE -> MultiplicationRangeConfig(10, 30, 4, 19, expectedTimeSeconds = 18)
        Difficulty.ADVANCED -> MultiplicationRangeConfig(20, 99, 12, 49, expectedTimeSeconds = 26)
        Difficulty.EXPERT -> MultiplicationRangeConfig(50, 499, 15, 99, expectedTimeSeconds = 36)
    }

    fun divisionConfig(difficulty: Difficulty): DivisionRangeConfig = when (difficulty) {
        Difficulty.BEGINNER -> DivisionRangeConfig(2, 9, 2, 9, expectedTimeSeconds = 8)
        Difficulty.BASIC -> DivisionRangeConfig(2, 12, 2, 15, expectedTimeSeconds = 11)
        Difficulty.INTERMEDIATE -> DivisionRangeConfig(4, 19, 10, 45, expectedTimeSeconds = 18)
        Difficulty.ADVANCED -> DivisionRangeConfig(11, 29, 15, 99, expectedTimeSeconds = 26)
        Difficulty.EXPERT -> DivisionRangeConfig(15, 49, 25, 199, expectedTimeSeconds = 35)
    }

    fun percentageConfig(difficulty: Difficulty): PercentageRangeConfig = when (difficulty) {
        Difficulty.BEGINNER -> PercentageRangeConfig(
            percentages = listOf(10, 20, 25, 50),
            baseMin = 20,
            baseMax = 100,
            baseStep = 10,
            expectedTimeSeconds = 9
        )
        Difficulty.BASIC -> PercentageRangeConfig(
            percentages = listOf(5, 10, 15, 20, 25, 30, 40, 50, 75),
            baseMin = 20,
            baseMax = 200,
            baseStep = 10,
            expectedTimeSeconds = 13
        )
        Difficulty.INTERMEDIATE -> PercentageRangeConfig(
            percentages = listOf(5, 12, 15, 20, 25, 35, 45, 60, 75, 80),
            baseMin = 40,
            baseMax = 400,
            baseStep = 5,
            expectedTimeSeconds = 18
        )
        Difficulty.ADVANCED -> PercentageRangeConfig(
            percentages = listOf(8, 12, 15, 18, 22, 35, 45, 65, 85),
            baseMin = 50,
            baseMax = 800,
            baseStep = 5,
            expectedTimeSeconds = 24
        )
        Difficulty.EXPERT -> PercentageRangeConfig(
            percentages = listOf(7, 13, 17, 23, 28, 37, 42, 64, 78, 86),
            baseMin = 120,
            baseMax = 1200,
            baseStep = 5,
            expectedTimeSeconds = 30
        )
    }

    fun fractionConfig(difficulty: Difficulty): FractionRangeConfig = when (difficulty) {
        Difficulty.BEGINNER -> FractionRangeConfig(2, 6, allowOperations = false, allowDivision = false, expectedTimeSeconds = 12)
        Difficulty.BASIC -> FractionRangeConfig(2, 9, allowOperations = true, allowDivision = false, expectedTimeSeconds = 16)
        Difficulty.INTERMEDIATE -> FractionRangeConfig(3, 12, allowOperations = true, allowDivision = false, expectedTimeSeconds = 22)
        Difficulty.ADVANCED -> FractionRangeConfig(4, 15, allowOperations = true, allowDivision = false, expectedTimeSeconds = 28)
        Difficulty.EXPERT -> FractionRangeConfig(4, 18, allowOperations = true, allowDivision = true, expectedTimeSeconds = 36)
    }

    fun decimalConfig(difficulty: Difficulty): DecimalRangeConfig = when (difficulty) {
        Difficulty.BEGINNER -> DecimalRangeConfig(scale = 1, wholeMin = 1, wholeMax = 9, allowMultiplication = false, expectedTimeSeconds = 10)
        Difficulty.BASIC -> DecimalRangeConfig(scale = 1, wholeMin = 5, wholeMax = 40, allowMultiplication = false, expectedTimeSeconds = 14)
        Difficulty.INTERMEDIATE -> DecimalRangeConfig(scale = 2, wholeMin = 1, wholeMax = 50, allowMultiplication = false, expectedTimeSeconds = 20)
        Difficulty.ADVANCED -> DecimalRangeConfig(scale = 2, wholeMin = 2, wholeMax = 95, allowMultiplication = true, expectedTimeSeconds = 26)
        Difficulty.EXPERT -> DecimalRangeConfig(scale = 2, wholeMin = 10, wholeMax = 250, allowMultiplication = true, expectedTimeSeconds = 34)
    }
}
