package com.example.domain.generator

import com.example.domain.model.AnswerFormat
import com.example.domain.model.Difficulty
import com.example.domain.model.MathCategory
import com.example.domain.model.MathQuestion
import com.example.domain.model.QuestionMetadata
import com.example.domain.model.QuestionStyle
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class AdditionGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.ADDITION
    override val version: Int = 2

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val range = MathDifficultyRanges.additionConfig(difficulty)
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)

        val a = random.nextInt(range.operandMin, range.operandMax + 1).toLong()
        val b = random.nextInt(range.operandMin, range.operandMax + 1).toLong()
        val sum = Math.addExact(a, b)

        return when (style) {
            QuestionStyle.MISSING_OPERAND -> {
                val hideLeft = random.nextBoolean()
                val answerVal = if (hideLeft) a else b
                val known = if (hideLeft) b else a
                val prompt = if (hideLeft) "□ + $b = $sum" else "$a + □ = $sum"
                val steps = listOf(
                    prompt,
                    "Isolate □ by subtracting $known from $sum",
                    "□ = $sum − $known",
                    "= $answerVal"
                )
                MathQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    difficulty = difficulty,
                    answerFormat = AnswerFormat.INTEGER,
                    prompt = prompt,
                    answer = answerVal.toString(),
                    explanationSteps = steps,
                    explanation = steps.joinToString("\n"),
                    options = DistractorBuilder.integerOptions(answerVal, allowNegative = false, random = random),
                    expectedTimeSeconds = range.expectedTimeSeconds + 2,
                    fingerprint = "ADDITION:MISS:${if (hideLeft) "L" else "R"}:$known:$sum",
                    generatorVersion = version,
                    metadata = QuestionMetadata.MissingOperandMeta(prompt, answerVal.toString()),
                    complexityTier = tier,
                    questionStyle = QuestionStyle.MISSING_OPERAND
                )
            }
            QuestionStyle.MULTI_STEP -> {
                val cMax = (range.operandMax / 2).coerceAtLeast(range.operandMin + 1)
                val c = random.nextInt(range.operandMin, cMax + 1).toLong()
                val total = Math.addExact(sum, c)
                val steps = listOf(
                    "$a + $b + $c",
                    "Add left to right: $a + $b = $sum",
                    "= $sum + $c",
                    "= $total"
                )
                MathQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    difficulty = difficulty,
                    answerFormat = AnswerFormat.INTEGER,
                    prompt = "$a + $b + $c",
                    answer = total.toString(),
                    explanationSteps = steps,
                    explanation = steps.joinToString("\n"),
                    options = DistractorBuilder.integerOptions(total, allowNegative = false, random = random),
                    expectedTimeSeconds = range.expectedTimeSeconds + 4,
                    fingerprint = "ADDITION:MULTI:${listOf(a, b, c).sorted().joinToString(":")}",
                    generatorVersion = version,
                    metadata = QuestionMetadata.MultiStepMeta("$a + $b + $c", total.toString()),
                    complexityTier = tier,
                    questionStyle = QuestionStyle.MULTI_STEP
                )
            }
            QuestionStyle.APPLIED_CHALLENGE -> {
                val cMax = (range.operandMax / 2).coerceAtLeast(range.operandMin + 1)
                val c = random.nextInt(range.operandMin, cMax + 1).toLong()
                val total = Math.addExact(sum, c)
                val prompt = "$a + $b + □ = $total"
                val steps = listOf(
                    prompt,
                    "Combine known terms: $a + $b = $sum",
                    "$sum + □ = $total",
                    "□ = $total − $sum",
                    "= $c"
                )
                MathQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    difficulty = difficulty,
                    answerFormat = AnswerFormat.INTEGER,
                    prompt = prompt,
                    answer = c.toString(),
                    explanationSteps = steps,
                    explanation = steps.joinToString("\n"),
                    options = DistractorBuilder.integerOptions(c, allowNegative = false, random = random),
                    expectedTimeSeconds = range.expectedTimeSeconds + 6,
                    fingerprint = "ADDITION:APP:$a:$b:$total",
                    generatorVersion = version,
                    metadata = QuestionMetadata.MissingOperandMeta(prompt, c.toString()),
                    complexityTier = tier,
                    questionStyle = QuestionStyle.APPLIED_CHALLENGE
                )
            }
            QuestionStyle.DIRECT -> {
                val steps = buildDirectExplanation(a, b, sum)
                val low = min(a, b)
                val high = max(a, b)
                MathQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    difficulty = difficulty,
                    answerFormat = AnswerFormat.INTEGER,
                    prompt = "$a + $b",
                    answer = sum.toString(),
                    explanationSteps = steps,
                    explanation = steps.joinToString("\n"),
                    options = DistractorBuilder.integerOptions(sum, allowNegative = false, random = random),
                    expectedTimeSeconds = range.expectedTimeSeconds,
                    fingerprint = "ADDITION:$low:$high",
                    generatorVersion = version,
                    metadata = QuestionMetadata.BinaryIntMeta(a, b, "+", sum),
                    complexityTier = tier,
                    questionStyle = QuestionStyle.DIRECT
                )
            }
        }
    }

    private fun buildDirectExplanation(a: Long, b: Long, sum: Long): List<String> {
        if (b < 10L) return listOf("$a + $b", "= $sum")
        val tensPart = (b / 10L) * 10L
        val onesPart = b % 10L
        return if (onesPart == 0L) {
            listOf("$a + $b", "= $sum")
        } else {
            val intermediate = a + tensPart
            listOf(
                "$a + $b",
                "= $a + ($tensPart + $onesPart)",
                "= ($a + $tensPart) + $onesPart",
                "= $intermediate + $onesPart",
                "= $sum"
            )
        }
    }
}

class SubtractionGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.SUBTRACTION
    override val version: Int = 2

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val range = MathDifficultyRanges.subtractionConfig(difficulty, config.allowNegativeSubtraction)
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)

        var a = random.nextInt(range.operandMin, range.operandMax + 1).toLong()
        var b = random.nextInt(range.operandMin, range.operandMax + 1).toLong()
        if ((!range.allowNegativeResult || style != QuestionStyle.DIRECT) && a < b) {
            val temp = a
            a = b
            b = temp
        }
        val difference = Math.subtractExact(a, b)

        return when (style) {
            QuestionStyle.MISSING_OPERAND -> {
                val hideSubtrahend = random.nextBoolean()
                if (hideSubtrahend) {
                    val prompt = "$a − □ = $difference"
                    val steps = listOf(
                        prompt,
                        "Subtract the result from $a: □ = $a − $difference",
                        "= $b"
                    )
                    MathQuestion(
                        id = UUID.randomUUID().toString(),
                        category = category,
                        difficulty = difficulty,
                        answerFormat = AnswerFormat.INTEGER,
                        prompt = prompt,
                        answer = b.toString(),
                        explanationSteps = steps,
                        explanation = steps.joinToString("\n"),
                        options = DistractorBuilder.integerOptions(b, allowNegative = false, random = random),
                        expectedTimeSeconds = range.expectedTimeSeconds + 2,
                        fingerprint = "SUBTRACTION:MISSR:$a:$difference",
                        generatorVersion = version,
                        metadata = QuestionMetadata.MissingOperandMeta(prompt, b.toString()),
                        complexityTier = tier,
                        questionStyle = QuestionStyle.MISSING_OPERAND
                    )
                } else {
                    val prompt = "□ − $b = $difference"
                    val steps = listOf(
                        prompt,
                        "Add $b to both sides: □ = $difference + $b",
                        "= $a"
                    )
                    MathQuestion(
                        id = UUID.randomUUID().toString(),
                        category = category,
                        difficulty = difficulty,
                        answerFormat = AnswerFormat.INTEGER,
                        prompt = prompt,
                        answer = a.toString(),
                        explanationSteps = steps,
                        explanation = steps.joinToString("\n"),
                        options = DistractorBuilder.integerOptions(a, allowNegative = false, random = random),
                        expectedTimeSeconds = range.expectedTimeSeconds + 2,
                        fingerprint = "SUBTRACTION:MISSL:$b:$difference",
                        generatorVersion = version,
                        metadata = QuestionMetadata.MissingOperandMeta(prompt, a.toString()),
                        complexityTier = tier,
                        questionStyle = QuestionStyle.MISSING_OPERAND
                    )
                }
            }
            QuestionStyle.MULTI_STEP -> {
                val cMax = (range.operandMax / 2).coerceAtLeast(range.operandMin + 1)
                val c = random.nextInt(range.operandMin, cMax + 1).toLong()
                val rawStart = a + b + c
                val step1 = rawStart - b
                val res = step1 - c
                val prompt = "$rawStart − $b − $c"
                val steps = listOf(
                    prompt,
                    "Subtract left to right: $rawStart − $b = $step1",
                    "= $step1 − $c",
                    "= $res"
                )
                MathQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    difficulty = difficulty,
                    answerFormat = AnswerFormat.INTEGER,
                    prompt = prompt,
                    answer = res.toString(),
                    explanationSteps = steps,
                    explanation = steps.joinToString("\n"),
                    options = DistractorBuilder.integerOptions(res, allowNegative = false, random = random),
                    expectedTimeSeconds = range.expectedTimeSeconds + 4,
                    fingerprint = "SUBTRACTION:MULTI:$rawStart:$b:$c",
                    generatorVersion = version,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, res.toString()),
                    complexityTier = tier,
                    questionStyle = QuestionStyle.MULTI_STEP
                )
            }
            QuestionStyle.APPLIED_CHALLENGE -> {
                val cMax = (range.operandMax / 2).coerceAtLeast(range.operandMin + 1)
                val c = random.nextInt(range.operandMin, cMax + 1).toLong()
                val totalLeft = a + b + c
                val prompt = "$totalLeft − ($a + □) = $c"
                val steps = listOf(
                    prompt,
                    "$a + □ = $totalLeft − $c = ${totalLeft - c}",
                    "□ = ${totalLeft - c} − $a",
                    "= $b"
                )
                MathQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    difficulty = difficulty,
                    answerFormat = AnswerFormat.INTEGER,
                    prompt = prompt,
                    answer = b.toString(),
                    explanationSteps = steps,
                    explanation = steps.joinToString("\n"),
                    options = DistractorBuilder.integerOptions(b, allowNegative = false, random = random),
                    expectedTimeSeconds = range.expectedTimeSeconds + 6,
                    fingerprint = "SUBTRACTION:APP:$totalLeft:$a:$c",
                    generatorVersion = version,
                    metadata = QuestionMetadata.MissingOperandMeta(prompt, b.toString()),
                    complexityTier = tier,
                    questionStyle = QuestionStyle.APPLIED_CHALLENGE
                )
            }
            QuestionStyle.DIRECT -> {
                val steps = buildDirectExplanation(a, b, difference)
                MathQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    difficulty = difficulty,
                    answerFormat = AnswerFormat.INTEGER,
                    prompt = "$a − $b",
                    answer = difference.toString(),
                    explanationSteps = steps,
                    explanation = steps.joinToString("\n"),
                    options = DistractorBuilder.integerOptions(
                        correct = difference,
                        allowNegative = range.allowNegativeResult,
                        random = random
                    ),
                    expectedTimeSeconds = range.expectedTimeSeconds,
                    fingerprint = "SUBTRACTION:$a:$b",
                    generatorVersion = version,
                    metadata = QuestionMetadata.BinaryIntMeta(a, b, "-", difference),
                    complexityTier = tier,
                    questionStyle = QuestionStyle.DIRECT
                )
            }
        }
    }

    private fun buildDirectExplanation(a: Long, b: Long, diff: Long): List<String> {
        if (b < 10L) return listOf("$a − $b", "= $diff")
        val tensPart = (b / 10L) * 10L
        val onesPart = b % 10L
        return if (onesPart == 0L) {
            listOf("$a − $b", "= $diff")
        } else {
            val intermediate = a - tensPart
            listOf(
                "$a − $b",
                "= $a − ($tensPart + $onesPart)",
                "= ($a − $tensPart) − $onesPart",
                "= $intermediate − $onesPart",
                "= $diff"
            )
        }
    }
}

class MultiplicationGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.MULTIPLICATION
    override val version: Int = 2

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val range = MathDifficultyRanges.multiplicationConfig(difficulty)
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)

        val a = random.nextInt(range.leftMin, range.leftMax + 1).toLong()
        val b = random.nextInt(range.rightMin, range.rightMax + 1).toLong()
        val product = Math.multiplyExact(a, b)

        return when (style) {
            QuestionStyle.MISSING_OPERAND -> {
                val hideLeft = random.nextBoolean()
                val answerVal = if (hideLeft) a else b
                val known = if (hideLeft) b else a
                val prompt = if (hideLeft) "□ × $b = $product" else "$a × □ = $product"
                val steps = listOf(
                    prompt,
                    "Divide $product by $known",
                    "□ = $product ÷ $known",
                    "= $answerVal"
                )
                MathQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    difficulty = difficulty,
                    answerFormat = AnswerFormat.INTEGER,
                    prompt = prompt,
                    answer = answerVal.toString(),
                    explanationSteps = steps,
                    explanation = steps.joinToString("\n"),
                    options = DistractorBuilder.integerOptions(answerVal, allowNegative = false, random = random),
                    expectedTimeSeconds = range.expectedTimeSeconds + 2,
                    fingerprint = "MULTIPLICATION:MISS:$known:$product",
                    generatorVersion = version,
                    metadata = QuestionMetadata.MissingOperandMeta(prompt, answerVal.toString()),
                    complexityTier = tier,
                    questionStyle = QuestionStyle.MISSING_OPERAND
                )
            }
            QuestionStyle.MULTI_STEP -> {
                val c = random.nextInt(2, 19).toLong()
                val total = Math.addExact(product, c)
                val prompt = "$a × $b + $c"
                val steps = listOf(
                    prompt,
                    "Multiply first: $a × $b = $product",
                    "= $product + $c",
                    "= $total"
                )
                MathQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    difficulty = difficulty,
                    answerFormat = AnswerFormat.INTEGER,
                    prompt = prompt,
                    answer = total.toString(),
                    explanationSteps = steps,
                    explanation = steps.joinToString("\n"),
                    options = DistractorBuilder.integerOptions(total, allowNegative = false, random = random),
                    expectedTimeSeconds = range.expectedTimeSeconds + 4,
                    fingerprint = "MULTIPLICATION:MULTI:$a:$b:$c",
                    generatorVersion = version,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, total.toString()),
                    complexityTier = tier,
                    questionStyle = QuestionStyle.MULTI_STEP
                )
            }
            QuestionStyle.APPLIED_CHALLENGE -> {
                val c = random.nextInt(2, 15).toLong()
                val total = product + c
                val prompt = "$a × □ + $c = $total"
                val steps = listOf(
                    prompt,
                    "Subtract $c: $a × □ = $total − $c = $product",
                    "Divide by $a: □ = $product ÷ $a",
                    "= $b"
                )
                MathQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    difficulty = difficulty,
                    answerFormat = AnswerFormat.INTEGER,
                    prompt = prompt,
                    answer = b.toString(),
                    explanationSteps = steps,
                    explanation = steps.joinToString("\n"),
                    options = DistractorBuilder.integerOptions(b, allowNegative = false, random = random),
                    expectedTimeSeconds = range.expectedTimeSeconds + 6,
                    fingerprint = "MULTIPLICATION:APP:$a:$c:$total",
                    generatorVersion = version,
                    metadata = QuestionMetadata.MissingOperandMeta(prompt, b.toString()),
                    complexityTier = tier,
                    questionStyle = QuestionStyle.APPLIED_CHALLENGE
                )
            }
            QuestionStyle.DIRECT -> {
                val steps = buildDirectExplanation(a, b, product)
                val low = min(a, b)
                val high = max(a, b)
                MathQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    difficulty = difficulty,
                    answerFormat = AnswerFormat.INTEGER,
                    prompt = "$a × $b",
                    answer = product.toString(),
                    explanationSteps = steps,
                    explanation = steps.joinToString("\n"),
                    options = DistractorBuilder.integerOptions(product, allowNegative = false, random = random),
                    expectedTimeSeconds = range.expectedTimeSeconds,
                    fingerprint = "MULTIPLICATION:$low:$high",
                    generatorVersion = version,
                    metadata = QuestionMetadata.BinaryIntMeta(a, b, "×", product),
                    complexityTier = tier,
                    questionStyle = QuestionStyle.DIRECT
                )
            }
        }
    }

    private fun buildDirectExplanation(a: Long, b: Long, product: Long): List<String> {
        if (b < 10L) return listOf("$a × $b", "= $product")
        val tens = (b / 10L) * 10L
        val ones = b % 10L
        return if (ones == 0L) {
            listOf("$a × $b", "= $product")
        } else {
            val partA = a * tens
            val partB = a * ones
            listOf(
                "$a × $b",
                "= $a × ($tens + $ones)",
                "= ($a × $tens) + ($a × $ones)",
                "= $partA + $partB",
                "= $product"
            )
        }
    }
}

class DivisionGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.DIVISION
    override val version: Int = 2

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val range = MathDifficultyRanges.divisionConfig(difficulty)
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)

        val divisor = random.nextInt(range.divisorMin, range.divisorMax + 1).toLong()
        val quotient = random.nextInt(range.quotientMin, range.quotientMax + 1).toLong()
        val dividend = Math.multiplyExact(divisor, quotient)

        return when (style) {
            QuestionStyle.MISSING_OPERAND -> {
                val hideDivisor = random.nextBoolean()
                if (hideDivisor) {
                    val prompt = "$dividend ÷ □ = $quotient"
                    val steps = listOf(
                        prompt,
                        "□ = $dividend ÷ $quotient",
                        "= $divisor"
                    )
                    MathQuestion(
                        id = UUID.randomUUID().toString(),
                        category = category,
                        difficulty = difficulty,
                        answerFormat = AnswerFormat.INTEGER,
                        prompt = prompt,
                        answer = divisor.toString(),
                        explanationSteps = steps,
                        explanation = steps.joinToString("\n"),
                        options = DistractorBuilder.integerOptions(divisor, allowNegative = false, random = random),
                        expectedTimeSeconds = range.expectedTimeSeconds + 2,
                        fingerprint = "DIVISION:MISSD:$dividend:$quotient",
                        generatorVersion = version,
                        metadata = QuestionMetadata.MissingOperandMeta(prompt, divisor.toString()),
                        complexityTier = tier,
                        questionStyle = QuestionStyle.MISSING_OPERAND
                    )
                } else {
                    val prompt = "□ ÷ $divisor = $quotient"
                    val steps = listOf(
                        prompt,
                        "Multiply $quotient × $divisor",
                        "= $dividend"
                    )
                    MathQuestion(
                        id = UUID.randomUUID().toString(),
                        category = category,
                        difficulty = difficulty,
                        answerFormat = AnswerFormat.INTEGER,
                        prompt = prompt,
                        answer = dividend.toString(),
                        explanationSteps = steps,
                        explanation = steps.joinToString("\n"),
                        options = DistractorBuilder.integerOptions(dividend, allowNegative = false, random = random),
                        expectedTimeSeconds = range.expectedTimeSeconds + 2,
                        fingerprint = "DIVISION:MISSN:$divisor:$quotient",
                        generatorVersion = version,
                        metadata = QuestionMetadata.MissingOperandMeta(prompt, dividend.toString()),
                        complexityTier = tier,
                        questionStyle = QuestionStyle.MISSING_OPERAND
                    )
                }
            }
            QuestionStyle.MULTI_STEP -> {
                val extra = random.nextInt(2, 16).toLong()
                val res = quotient + extra
                val prompt = "($dividend ÷ $divisor) + $extra"
                val steps = listOf(
                    prompt,
                    "Divide first: $dividend ÷ $divisor = $quotient",
                    "= $quotient + $extra",
                    "= $res"
                )
                MathQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    difficulty = difficulty,
                    answerFormat = AnswerFormat.INTEGER,
                    prompt = prompt,
                    answer = res.toString(),
                    explanationSteps = steps,
                    explanation = steps.joinToString("\n"),
                    options = DistractorBuilder.integerOptions(res, allowNegative = false, random = random),
                    expectedTimeSeconds = range.expectedTimeSeconds + 4,
                    fingerprint = "DIVISION:MULTI:$dividend:$divisor:$extra",
                    generatorVersion = version,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, res.toString()),
                    complexityTier = tier,
                    questionStyle = QuestionStyle.MULTI_STEP
                )
            }
            QuestionStyle.APPLIED_CHALLENGE -> {
                val k = random.nextInt(2, 8).toLong()
                val addend = divisor * k
                val sum = dividend + addend
                val res = quotient + k
                val prompt = "($dividend + $addend) ÷ $divisor"
                val steps = listOf(
                    prompt,
                    "Add inside parentheses: $dividend + $addend = $sum",
                    "= $sum ÷ $divisor",
                    "= $res"
                )
                MathQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    difficulty = difficulty,
                    answerFormat = AnswerFormat.INTEGER,
                    prompt = prompt,
                    answer = res.toString(),
                    explanationSteps = steps,
                    explanation = steps.joinToString("\n"),
                    options = DistractorBuilder.integerOptions(res, allowNegative = false, random = random),
                    expectedTimeSeconds = range.expectedTimeSeconds + 6,
                    fingerprint = "DIVISION:APP:$dividend:$addend:$divisor",
                    generatorVersion = version,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, res.toString()),
                    complexityTier = tier,
                    questionStyle = QuestionStyle.APPLIED_CHALLENGE
                )
            }
            QuestionStyle.DIRECT -> {
                val steps = buildDirectExplanation(dividend, divisor, quotient)
                MathQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    difficulty = difficulty,
                    answerFormat = AnswerFormat.INTEGER,
                    prompt = "$dividend ÷ $divisor",
                    answer = quotient.toString(),
                    explanationSteps = steps,
                    explanation = steps.joinToString("\n"),
                    options = DistractorBuilder.integerOptions(quotient, allowNegative = false, random = random),
                    expectedTimeSeconds = range.expectedTimeSeconds,
                    fingerprint = "DIVISION:$dividend:$divisor",
                    generatorVersion = version,
                    metadata = QuestionMetadata.DivisionMeta(dividend, divisor, quotient),
                    complexityTier = tier,
                    questionStyle = QuestionStyle.DIRECT
                )
            }
        }
    }

    private fun buildDirectExplanation(dividend: Long, divisor: Long, quotient: Long): List<String> {
        if (quotient < 10L) {
            return listOf(
                "$dividend ÷ $divisor",
                "Verify: $divisor × $quotient = $dividend",
                "= $quotient"
            )
        }
        val tens = (quotient / 10L) * 10L
        val ones = quotient % 10L
        return if (ones == 0L) {
            listOf("$dividend ÷ $divisor", "Verify: $divisor × $quotient = $dividend", "= $quotient")
        } else {
            val firstChunk = divisor * tens
            val remainder = dividend - firstChunk
            listOf(
                "$dividend ÷ $divisor",
                "= ($firstChunk + $remainder) ÷ $divisor",
                "= ($firstChunk ÷ $divisor) + ($remainder ÷ $divisor)",
                "= $tens + $ones",
                "= $quotient"
            )
        }
    }
}

internal fun effectiveStyle(config: GeneratorConfig): QuestionStyle {
    return when {
        config.complexityTier <= 1 -> QuestionStyle.DIRECT
        config.complexityTier == 2 && config.questionStyle == QuestionStyle.DIRECT -> QuestionStyle.DIRECT
        config.complexityTier == 2 && config.questionStyle != QuestionStyle.DIRECT -> QuestionStyle.MISSING_OPERAND
        else -> config.questionStyle
    }
}
