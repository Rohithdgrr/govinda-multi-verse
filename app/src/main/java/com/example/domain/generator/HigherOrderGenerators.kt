package com.example.domain.generator

import com.example.domain.model.AnswerFormat
import com.example.domain.model.Difficulty
import com.example.domain.model.MathCategory
import com.example.domain.model.MathQuestion
import com.example.domain.model.QuestionMetadata
import com.example.domain.model.QuestionStyle
import java.util.UUID
import kotlin.random.Random

class PowerRootGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.POWERS_ROOTS
    override val version: Int = 2

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)

        if (style == QuestionStyle.MISSING_OPERAND) {
            val base = when (difficulty) {
                Difficulty.BEGINNER -> random.nextInt(2, 10).toLong()
                Difficulty.BASIC -> random.nextInt(2, 14).toLong()
                Difficulty.INTERMEDIATE -> random.nextInt(3, 18).toLong()
                else -> random.nextInt(4, 22).toLong()
            }
            val exp = if (difficulty.level >= Difficulty.INTERMEDIATE.level && random.nextBoolean()) 3 else 2
            var power = 1L
            repeat(exp) { power *= base }
            val prompt = "□^$exp = $power"
            val steps = listOf(
                prompt,
                "Find the ${if (exp == 2) "square" else "cube"} root of $power",
                "$base^$exp = $power",
                "□ = $base"
            )
            return MathQuestion(
                id = UUID.randomUUID().toString(),
                category = category,
                difficulty = difficulty,
                answerFormat = AnswerFormat.INTEGER,
                prompt = prompt,
                answer = base.toString(),
                explanationSteps = steps,
                explanation = steps.joinToString("\n"),
                options = DistractorBuilder.integerOptions(base, allowNegative = false, random = random),
                expectedTimeSeconds = 12 + difficulty.level * 2,
                fingerprint = "POWER:MISS:$exp:$power",
                generatorVersion = version,
                metadata = QuestionMetadata.MissingOperandMeta(prompt, base.toString()),
                complexityTier = tier,
                questionStyle = QuestionStyle.MISSING_OPERAND
            )
        }

        if (style == QuestionStyle.MULTI_STEP || style == QuestionStyle.APPLIED_CHALLENGE) {
            val maxBase = when (difficulty) {
                Difficulty.BEGINNER -> 6
                Difficulty.BASIC -> 9
                Difficulty.INTERMEDIATE -> 12
                Difficulty.ADVANCED -> 16
                Difficulty.EXPERT -> 20
            }
            val b1 = random.nextInt(2, maxBase + 1).toLong()
            val b2 = random.nextInt(2, maxBase + 1).toLong()
            val sq1 = b1 * b1
            val sq2 = b2 * b2

            return if (style == QuestionStyle.APPLIED_CHALLENGE) {
                val rootAns = b1
                val radicand = sq1
                val res = rootAns + sq2
                val prompt = "√$radicand + $b2^2"
                val steps = listOf(
                    prompt,
                    "√$radicand = $rootAns and $b2^2 = $sq2",
                    "= $rootAns + $sq2",
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
                    expectedTimeSeconds = 16 + difficulty.level * 3,
                    fingerprint = "POWER:APP:$radicand:$b2",
                    generatorVersion = version,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, res.toString()),
                    complexityTier = tier,
                    questionStyle = QuestionStyle.APPLIED_CHALLENGE
                )
            } else {
                val res = sq1 + sq2
                val prompt = "$b1^2 + $b2^2"
                val steps = listOf(
                    prompt,
                    "$b1^2 = $sq1 and $b2^2 = $sq2",
                    "= $sq1 + $sq2",
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
                    expectedTimeSeconds = 14 + difficulty.level * 3,
                    fingerprint = "POWER:MULTI:${minOf(b1, b2)}:${maxOf(b1, b2)}",
                    generatorVersion = version,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, res.toString()),
                    complexityTier = tier,
                    questionStyle = QuestionStyle.MULTI_STEP
                )
            }
        }

        val isRoot = difficulty.level >= Difficulty.BASIC.level && random.nextBoolean()
        return if (!isRoot) {
            val (base, exp, expectedSec) = when (difficulty) {
                Difficulty.BEGINNER -> Triple(random.nextInt(2, 10).toLong(), 2, 8)
                Difficulty.BASIC -> Triple(random.nextInt(2, 16).toLong(), 2, 11)
                Difficulty.INTERMEDIATE -> Triple(random.nextInt(2, 9).toLong(), 3, 15)
                Difficulty.ADVANCED -> Triple(random.nextInt(11, 26).toLong(), 2, 20)
                Difficulty.EXPERT -> Triple(random.nextInt(2, 7).toLong(), random.nextInt(3, 6), 24)
            }
            var result = 1L
            repeat(exp) { result *= base }
            val expansion = List(exp) { base.toString() }.joinToString(" × ")
            val steps = listOf("$base^$exp", "= $expansion", "= $result")
            MathQuestion(
                id = UUID.randomUUID().toString(),
                category = category,
                difficulty = difficulty,
                answerFormat = AnswerFormat.INTEGER,
                prompt = "$base^$exp",
                answer = result.toString(),
                explanationSteps = steps,
                explanation = steps.joinToString("\n"),
                options = DistractorBuilder.integerOptions(result, allowNegative = false, random = random),
                expectedTimeSeconds = expectedSec,
                fingerprint = "POWER:$base:$exp",
                generatorVersion = version,
                metadata = QuestionMetadata.PowerRootMeta(base, exp, isRoot = false, result = result),
                complexityTier = tier,
                questionStyle = QuestionStyle.DIRECT
            )
        } else {
            val (rootVal, exp, expectedSec) = when (difficulty) {
                Difficulty.BEGINNER -> Triple(random.nextInt(2, 10).toLong(), 2, 9)
                Difficulty.BASIC -> Triple(random.nextInt(4, 15).toLong(), 2, 12)
                Difficulty.INTERMEDIATE -> Triple(random.nextInt(10, 25).toLong(), 2, 17)
                Difficulty.ADVANCED -> Triple(random.nextInt(3, 12).toLong(), 3, 22)
                Difficulty.EXPERT -> Triple(random.nextInt(18, 45).toLong(), 2, 28)
            }
            var radicand = 1L
            repeat(exp) { radicand *= rootVal }
            val prompt = if (exp == 2) "√$radicand" else "∛$radicand"
            val steps = listOf(
                prompt,
                "Find n such that ${if (exp == 2) "n × n" else "n × n × n"} = $radicand",
                "$rootVal^$exp = $radicand",
                "= $rootVal"
            )
            MathQuestion(
                id = UUID.randomUUID().toString(),
                category = category,
                difficulty = difficulty,
                answerFormat = AnswerFormat.INTEGER,
                prompt = prompt,
                answer = rootVal.toString(),
                explanationSteps = steps,
                explanation = steps.joinToString("\n"),
                options = DistractorBuilder.integerOptions(rootVal, allowNegative = false, random = random),
                expectedTimeSeconds = expectedSec,
                fingerprint = "ROOT:$radicand:$exp",
                generatorVersion = version,
                metadata = QuestionMetadata.PowerRootMeta(radicand, exp, isRoot = true, result = rootVal),
                complexityTier = tier,
                questionStyle = QuestionStyle.DIRECT
            )
        }
    }
}

class MixedOperationsGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.MIXED_OPERATIONS
    override val version: Int = 2

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)

        val (maxAdd, maxMul, expectedSec) = when (difficulty) {
            Difficulty.BEGINNER -> Triple(12, 6, 11)
            Difficulty.BASIC -> Triple(25, 10, 15)
            Difficulty.INTERMEDIATE -> Triple(60, 15, 20)
            Difficulty.ADVANCED -> Triple(150, 25, 26)
            Difficulty.EXPERT -> Triple(400, 40, 34)
        }

        val a = random.nextInt(2, maxAdd + 1).toLong()
        val b = random.nextInt(2, maxMul + 1).toLong()
        val c = random.nextInt(2, maxMul + 1).toLong()

        if (style == QuestionStyle.MISSING_OPERAND) {
            val total = a + b * c
            val prompt = "$a + $b × □ = $total"
            val steps = listOf(
                prompt,
                "Subtract $a: $b × □ = $total − $a = ${b * c}",
                "Divide by $b: □ = ${b * c} ÷ $b",
                "= $c"
            )
            return MathQuestion(
                id = UUID.randomUUID().toString(),
                category = category,
                difficulty = difficulty,
                answerFormat = AnswerFormat.INTEGER,
                prompt = prompt,
                answer = c.toString(),
                explanationSteps = steps,
                explanation = steps.joinToString("\n"),
                options = DistractorBuilder.integerOptions(c, allowNegative = false, random = random),
                expectedTimeSeconds = expectedSec + 4,
                fingerprint = "MIXED:MISS:$a:$b:$total",
                generatorVersion = version,
                metadata = QuestionMetadata.MissingOperandMeta(prompt, c.toString()),
                complexityTier = tier,
                questionStyle = QuestionStyle.MISSING_OPERAND
            )
        }

        if (style == QuestionStyle.APPLIED_CHALLENGE) {
            val d = random.nextInt(2, maxMul + 1).toLong()
            val prod1 = a * b
            val prod2 = c * d
            val res = prod1 + prod2
            val prompt = "$a × $b + $c × $d"
            val steps = listOf(
                prompt,
                "Evaluate both products: $a × $b = $prod1, $c × $d = $prod2",
                "= $prod1 + $prod2",
                "= $res"
            )
            return MathQuestion(
                id = UUID.randomUUID().toString(),
                category = category,
                difficulty = difficulty,
                answerFormat = AnswerFormat.INTEGER,
                prompt = prompt,
                answer = res.toString(),
                explanationSteps = steps,
                explanation = steps.joinToString("\n"),
                options = DistractorBuilder.integerOptions(res, allowNegative = false, random = random),
                expectedTimeSeconds = expectedSec + 6,
                fingerprint = "MIXED:APP:$a:$b:$c:$d",
                generatorVersion = version,
                metadata = QuestionMetadata.MultiStepMeta(prompt, res.toString()),
                complexityTier = tier,
                questionStyle = QuestionStyle.APPLIED_CHALLENGE
            )
        }

        val groupFirst = difficulty.level >= Difficulty.BASIC.level && random.nextBoolean()
        val (prompt, result, steps, fp) = if (groupFirst) {
            val sumAB = a + b
            val prod = sumAB * c
            val stepList = listOf(
                "($a + $b) × $c",
                "Evaluate parentheses first: $a + $b = $sumAB",
                "= $sumAB × $c",
                "= $prod"
            )
            MixedTuple("($a + $b) × $c", prod, stepList, "MIXED:GRP:$a:$b:$c")
        } else {
            val useSubtraction = difficulty.level >= Difficulty.INTERMEDIATE.level && random.nextBoolean()
            if (useSubtraction) {
                val prod = b * c
                val safeA = prod + random.nextInt(2, maxAdd + 1).toLong()
                val res = safeA - prod
                val stepList = listOf(
                    "$safeA − $b × $c",
                    "Multiply first: $b × $c = $prod",
                    "= $safeA − $prod",
                    "= $res"
                )
                MixedTuple("$safeA − $b × $c", res, stepList, "MIXED:SUB:$safeA:$b:$c")
            } else {
                val prod = b * c
                val res = a + prod
                val stepList = listOf(
                    "$a + $b × $c",
                    "Multiply before adding: $b × $c = $prod",
                    "= $a + $prod",
                    "= $res"
                )
                MixedTuple("$a + $b × $c", res, stepList, "MIXED:ADD:$a:$b:$c")
            }
        }

        return MathQuestion(
            id = UUID.randomUUID().toString(),
            category = category,
            difficulty = difficulty,
            answerFormat = AnswerFormat.INTEGER,
            prompt = prompt,
            answer = result.toString(),
            explanationSteps = steps,
            explanation = steps.joinToString("\n"),
            options = DistractorBuilder.integerOptions(result, allowNegative = false, random = random),
            expectedTimeSeconds = expectedSec,
            fingerprint = fp,
            generatorVersion = version,
            metadata = QuestionMetadata.MixedOpMeta(a, b, c, "+", "×", groupFirst, result),
            complexityTier = tier,
            questionStyle = style
        )
    }

    private data class MixedTuple(
        val prompt: String,
        val result: Long,
        val steps: List<String>,
        val fingerprint: String
    )
}
