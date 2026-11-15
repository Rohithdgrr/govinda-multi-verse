package com.example.domain.generator

import com.example.domain.model.AnswerFormat
import com.example.domain.model.Difficulty
import com.example.domain.model.Fraction
import com.example.domain.model.MathCategory
import com.example.domain.model.MathQuestion
import com.example.domain.model.QuestionMetadata
import com.example.domain.model.QuestionStyle
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.UUID
import kotlin.math.pow
import kotlin.random.Random

class PercentageGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.PERCENTAGE
    override val version: Int = 2

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val range = MathDifficultyRanges.percentageConfig(difficulty)
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)

        val percentInt = range.percentages[random.nextInt(range.percentages.size)]
        val stepsCount = ((range.baseMax - range.baseMin) / range.baseStep).coerceAtLeast(1)
        val baseInt = range.baseMin + random.nextInt(stepsCount + 1) * range.baseStep

        val percentBd = BigDecimal(percentInt)
        val baseBd = BigDecimal(baseInt)
        val part = percentBd
            .multiply(baseBd)
            .divide(BigDecimal(100), 4, RoundingMode.HALF_UP)
            .stripTrailingZeros()
        val partStr = DistractorBuilder.formatDecimal(part)

        return when (style) {
            QuestionStyle.MISSING_OPERAND -> {
                val findPercent = random.nextBoolean()
                if (findPercent) {
                    val prompt = "□% of $baseInt = $partStr"
                    val steps = listOf(
                        prompt,
                        "Divide part by base: $partStr ÷ $baseInt = ${DistractorBuilder.formatDecimal(part.divide(baseBd, 4, RoundingMode.HALF_UP))}",
                        "Multiply by 100 to get percentage",
                        "□ = $percentInt"
                    )
                    MathQuestion(
                        id = UUID.randomUUID().toString(),
                        category = category,
                        difficulty = difficulty,
                        answerFormat = AnswerFormat.INTEGER,
                        prompt = prompt,
                        answer = percentInt.toString(),
                        explanationSteps = steps,
                        explanation = steps.joinToString("\n"),
                        options = DistractorBuilder.integerOptions(percentInt.toLong(), allowNegative = false, random = random),
                        expectedTimeSeconds = range.expectedTimeSeconds + 3,
                        fingerprint = "PERCENTAGE:MISSP:$baseInt:$partStr",
                        generatorVersion = version,
                        metadata = QuestionMetadata.MissingOperandMeta(prompt, percentInt.toString()),
                        complexityTier = tier,
                        questionStyle = QuestionStyle.MISSING_OPERAND
                    )
                } else {
                    val prompt = "$percentInt% of □ = $partStr"
                    val steps = listOf(
                        prompt,
                        "Divide part by percentage: $partStr ÷ ${DistractorBuilder.formatDecimal(percentBd.divide(BigDecimal(100)))}",
                        "□ = $baseInt"
                    )
                    MathQuestion(
                        id = UUID.randomUUID().toString(),
                        category = category,
                        difficulty = difficulty,
                        answerFormat = AnswerFormat.INTEGER,
                        prompt = prompt,
                        answer = baseInt.toString(),
                        explanationSteps = steps,
                        explanation = steps.joinToString("\n"),
                        options = DistractorBuilder.integerOptions(baseInt.toLong(), allowNegative = false, random = random),
                        expectedTimeSeconds = range.expectedTimeSeconds + 4,
                        fingerprint = "PERCENTAGE:MISSB:$percentInt:$partStr",
                        generatorVersion = version,
                        metadata = QuestionMetadata.MissingOperandMeta(prompt, baseInt.toString()),
                        complexityTier = tier,
                        questionStyle = QuestionStyle.MISSING_OPERAND
                    )
                }
            }
            QuestionStyle.MULTI_STEP -> {
                val total = baseBd.add(part).stripTrailingZeros()
                val canonical = DistractorBuilder.formatDecimal(total)
                val isInt = !canonical.contains(".")
                val prompt = "$baseInt + $percentInt% of $baseInt"
                val steps = listOf(
                    prompt,
                    "Find $percentInt% of $baseInt = $partStr",
                    "Add to original: $baseInt + $partStr",
                    "= $canonical"
                )
                val options = if (isInt) {
                    DistractorBuilder.integerOptions(total.toLong(), allowNegative = false, random = random)
                } else {
                    DistractorBuilder.decimalOptions(total, allowNegative = false, random = random)
                }
                MathQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    difficulty = difficulty,
                    answerFormat = if (isInt) AnswerFormat.INTEGER else AnswerFormat.DECIMAL,
                    prompt = prompt,
                    answer = canonical,
                    explanationSteps = steps,
                    explanation = steps.joinToString("\n"),
                    options = options,
                    expectedTimeSeconds = range.expectedTimeSeconds + 5,
                    fingerprint = "PERCENTAGE:INC:$percentInt:$baseInt",
                    generatorVersion = version,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, canonical),
                    complexityTier = tier,
                    questionStyle = QuestionStyle.MULTI_STEP
                )
            }
            QuestionStyle.APPLIED_CHALLENGE -> {
                val net = baseBd.subtract(part).stripTrailingZeros()
                val canonical = DistractorBuilder.formatDecimal(net)
                val isInt = !canonical.contains(".")
                val prompt = "$baseInt − $percentInt% of $baseInt"
                val steps = listOf(
                    prompt,
                    "Find $percentInt% of $baseInt = $partStr",
                    "Subtract from original: $baseInt − $partStr",
                    "= $canonical"
                )
                val options = if (isInt) {
                    DistractorBuilder.integerOptions(net.toLong(), allowNegative = false, random = random)
                } else {
                    DistractorBuilder.decimalOptions(net, allowNegative = false, random = random)
                }
                MathQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    difficulty = difficulty,
                    answerFormat = if (isInt) AnswerFormat.INTEGER else AnswerFormat.DECIMAL,
                    prompt = prompt,
                    answer = canonical,
                    explanationSteps = steps,
                    explanation = steps.joinToString("\n"),
                    options = options,
                    expectedTimeSeconds = range.expectedTimeSeconds + 6,
                    fingerprint = "PERCENTAGE:DEC:$percentInt:$baseInt",
                    generatorVersion = version,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, canonical),
                    complexityTier = tier,
                    questionStyle = QuestionStyle.APPLIED_CHALLENGE
                )
            }
            QuestionStyle.DIRECT -> {
                val isInteger = !partStr.contains(".")
                val format = if (isInteger) AnswerFormat.INTEGER else AnswerFormat.DECIMAL
                val tenPercent = baseBd.divide(BigDecimal(10), 4, RoundingMode.HALF_UP)
                val explanationSteps = listOf(
                    "$percentInt% of $baseInt",
                    "= ($percentInt ÷ 100) × $baseInt",
                    "10% of $baseInt = ${DistractorBuilder.formatDecimal(tenPercent)}",
                    "= (${DistractorBuilder.formatDecimal(percentBd.divide(BigDecimal(100)))}) × $baseInt",
                    "= $partStr"
                )
                val options = if (isInteger) {
                    DistractorBuilder.integerOptions(part.toLong(), allowNegative = false, random = random)
                } else {
                    DistractorBuilder.decimalOptions(part, allowNegative = false, random = random)
                }
                MathQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    difficulty = difficulty,
                    answerFormat = format,
                    prompt = "$percentInt% of $baseInt",
                    answer = partStr,
                    explanationSteps = explanationSteps,
                    explanation = explanationSteps.joinToString("\n"),
                    options = options,
                    expectedTimeSeconds = range.expectedTimeSeconds,
                    fingerprint = "PERCENTAGE:$percentInt:$baseInt",
                    generatorVersion = version,
                    metadata = QuestionMetadata.PercentageMeta(percentBd, baseBd, part),
                    complexityTier = tier,
                    questionStyle = QuestionStyle.DIRECT
                )
            }
        }
    }
}

class FractionGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.FRACTION
    override val version: Int = 2

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val range = MathDifficultyRanges.fractionConfig(difficulty)
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)

        if (!range.allowOperations && style == QuestionStyle.DIRECT) {
            return generateSimplification(difficulty, range, tier, random)
        }

        val d1 = random.nextInt(range.denominatorMin, range.denominatorMax + 1).toLong()
        val d2 = random.nextInt(range.denominatorMin, range.denominatorMax + 1).toLong()
        val n1 = random.nextInt(1, d1.toInt()).toLong()
        val n2 = random.nextInt(1, d2.toInt()).toLong()

        var f1 = Fraction(n1, d1).normalized()
        var f2 = Fraction(n2, d2).normalized()

        if (style == QuestionStyle.MISSING_OPERAND) {
            val sum = f1 + f2
            val prompt = "${f1.toCanonicalString()} + □ = ${sum.toCanonicalString()}"
            val steps = listOf(
                prompt,
                "Subtract ${f1.toCanonicalString()} from ${sum.toCanonicalString()}",
                "□ = ${sum.toCanonicalString()} − ${f1.toCanonicalString()}",
                "= ${f2.toCanonicalString()}"
            )
            val format = if (f2.denominator == BigInteger.ONE) AnswerFormat.INTEGER else AnswerFormat.FRACTION
            return MathQuestion(
                id = UUID.randomUUID().toString(),
                category = category,
                difficulty = difficulty,
                answerFormat = format,
                prompt = prompt,
                answer = f2.toCanonicalString(),
                explanationSteps = steps,
                explanation = steps.joinToString("\n"),
                options = DistractorBuilder.fractionOptions(f2, random),
                expectedTimeSeconds = range.expectedTimeSeconds + 4,
                fingerprint = "FRACTION:MISS:${f1.toCanonicalString()}:${sum.toCanonicalString()}",
                generatorVersion = version,
                metadata = QuestionMetadata.MissingOperandMeta(prompt, f2.toCanonicalString()),
                complexityTier = tier,
                questionStyle = QuestionStyle.MISSING_OPERAND
            )
        }

        if (style == QuestionStyle.MULTI_STEP || style == QuestionStyle.APPLIED_CHALLENGE) {
            val d3 = random.nextInt(range.denominatorMin, range.denominatorMax + 1).toLong()
            val n3 = random.nextInt(1, d3.toInt()).toLong()
            val f3 = Fraction(n3, d3).normalized()

            val (prompt, result, steps, fp) = if (style == QuestionStyle.APPLIED_CHALLENGE) {
                val prod = f2 * f3
                val total = f1 + prod
                val p = "${f1.toCanonicalString()} + ${f2.toCanonicalString()} × ${f3.toCanonicalString()}"
                val s = listOf(
                    p,
                    "Multiply first: ${f2.toCanonicalString()} × ${f3.toCanonicalString()} = ${prod.toCanonicalString()}",
                    "= ${f1.toCanonicalString()} + ${prod.toCanonicalString()}",
                    "= ${total.toCanonicalString()}"
                )
                Quad(p, total, s, "FRACTION:APP:${f1.toCanonicalString()}:${f2.toCanonicalString()}:${f3.toCanonicalString()}")
            } else {
                val partial = f1 + f2
                val total = partial + f3
                val p = "${f1.toCanonicalString()} + ${f2.toCanonicalString()} + ${f3.toCanonicalString()}"
                val s = listOf(
                    p,
                    "Add first pair: ${f1.toCanonicalString()} + ${f2.toCanonicalString()} = ${partial.toCanonicalString()}",
                    "= ${partial.toCanonicalString()} + ${f3.toCanonicalString()}",
                    "= ${total.toCanonicalString()}"
                )
                Quad(p, total, s, "FRACTION:MULTI:${f1.toCanonicalString()}:${f2.toCanonicalString()}:${f3.toCanonicalString()}")
            }
            val format = if (result.denominator == BigInteger.ONE) AnswerFormat.INTEGER else AnswerFormat.FRACTION
            return MathQuestion(
                id = UUID.randomUUID().toString(),
                category = category,
                difficulty = difficulty,
                answerFormat = format,
                prompt = prompt,
                answer = result.toCanonicalString(),
                explanationSteps = steps,
                explanation = steps.joinToString("\n"),
                options = DistractorBuilder.fractionOptions(result, random),
                expectedTimeSeconds = range.expectedTimeSeconds + 7,
                fingerprint = fp,
                generatorVersion = version,
                metadata = QuestionMetadata.MultiStepMeta(prompt, result.toCanonicalString()),
                complexityTier = tier,
                questionStyle = style
            )
        }

        val opChoice = when {
            range.allowDivision -> random.nextInt(4)
            difficulty.level >= Difficulty.INTERMEDIATE.level -> random.nextInt(3)
            else -> random.nextInt(2)
        }

        val (opSymbol, result, steps, fingerprint) = when (opChoice) {
            0 -> {
                val res = f1 + f2
                val lcmDen = lcm(f1.denominator, f2.denominator)
                val s1 = f1.numerator * (lcmDen / f1.denominator)
                val s2 = f2.numerator * (lcmDen / f2.denominator)
                val stepList = listOf(
                    "${f1.toCanonicalString()} + ${f2.toCanonicalString()}",
                    "Common denominator = $lcmDen",
                    "= $s1/$lcmDen + $s2/$lcmDen",
                    "= ${s1 + s2}/$lcmDen",
                    "= ${res.toCanonicalString()}"
                )
                val fpList = listOf(f1.toCanonicalString(), f2.toCanonicalString()).sorted()
                Quad("+", res, stepList, "FRACTION:+:${fpList[0]}:${fpList[1]}")
            }
            1 -> {
                if (f1.numerator * f2.denominator < f2.numerator * f1.denominator) {
                    val tmp = f1
                    f1 = f2
                    f2 = tmp
                }
                val res = f1 - f2
                val lcmDen = lcm(f1.denominator, f2.denominator)
                val s1 = f1.numerator * (lcmDen / f1.denominator)
                val s2 = f2.numerator * (lcmDen / f2.denominator)
                val stepList = listOf(
                    "${f1.toCanonicalString()} − ${f2.toCanonicalString()}",
                    "Common denominator = $lcmDen",
                    "= $s1/$lcmDen − $s2/$lcmDen",
                    "= ${s1 - s2}/$lcmDen",
                    "= ${res.toCanonicalString()}"
                )
                Quad("−", res, stepList, "FRACTION:-:${f1.toCanonicalString()}:${f2.toCanonicalString()}")
            }
            2 -> {
                val res = f1 * f2
                val rawNum = f1.numerator * f2.numerator
                val rawDen = f1.denominator * f2.denominator
                val stepList = listOf(
                    "${f1.toCanonicalString()} × ${f2.toCanonicalString()}",
                    "= ($rawNum) / ($rawDen)",
                    "Simplify by GCD(${rawNum.abs()}, $rawDen) = ${rawNum.abs().gcd(rawDen)}",
                    "= ${res.toCanonicalString()}"
                )
                val fpList = listOf(f1.toCanonicalString(), f2.toCanonicalString()).sorted()
                Quad("×", res, stepList, "FRACTION:*:${fpList[0]}:${fpList[1]}")
            }
            else -> {
                val res = f1 / f2
                val stepList = listOf(
                    "${f1.toCanonicalString()} ÷ ${f2.toCanonicalString()}",
                    "= ${f1.toCanonicalString()} × ${f2.denominator}/${f2.numerator}",
                    "= ${(f1.numerator * f2.denominator)}/${(f1.denominator * f2.numerator)}",
                    "= ${res.toCanonicalString()}"
                )
                Quad("÷", res, stepList, "FRACTION:/:${f1.toCanonicalString()}:${f2.toCanonicalString()}")
            }
        }

        val canonical = result.toCanonicalString()
        val format = if (result.denominator == BigInteger.ONE) AnswerFormat.INTEGER else AnswerFormat.FRACTION

        return MathQuestion(
            id = UUID.randomUUID().toString(),
            category = category,
            difficulty = difficulty,
            answerFormat = format,
            prompt = "${f1.toCanonicalString()} $opSymbol ${f2.toCanonicalString()}",
            answer = canonical,
            explanationSteps = steps,
            explanation = steps.joinToString("\n"),
            options = DistractorBuilder.fractionOptions(result, random),
            expectedTimeSeconds = range.expectedTimeSeconds,
            fingerprint = fingerprint,
            generatorVersion = version,
            metadata = QuestionMetadata.FractionMeta(
                left = f1,
                right = f2,
                operator = opSymbol,
                result = result,
                isSimplification = false
            ),
            complexityTier = tier,
            questionStyle = QuestionStyle.DIRECT
        )
    }

    private fun generateSimplification(
        difficulty: Difficulty,
        range: FractionRangeConfig,
        tier: Int,
        random: Random
    ): MathQuestion {
        val baseDen = random.nextInt(range.denominatorMin, range.denominatorMax + 1).toLong()
        val baseNum = random.nextInt(1, baseDen.toInt()).toLong()
        val simplified = Fraction(baseNum, baseDen).normalized()
        val multiplier = random.nextInt(2, 6).toLong()
        val rawNum = simplified.numerator * BigInteger.valueOf(multiplier)
        val rawDen = simplified.denominator * BigInteger.valueOf(multiplier)
        val rawFraction = Fraction(rawNum, rawDen)
        val gcd = rawNum.abs().gcd(rawDen)

        val steps = listOf(
            "Simplify $rawNum/$rawDen",
            "Find GCD($rawNum, $rawDen) = $gcd",
            "= ($rawNum ÷ $gcd) / ($rawDen ÷ $gcd)",
            "= ${simplified.toCanonicalString()}"
        )

        return MathQuestion(
            id = UUID.randomUUID().toString(),
            category = category,
            difficulty = difficulty,
            answerFormat = AnswerFormat.FRACTION,
            prompt = "Simplify $rawNum/$rawDen",
            answer = simplified.toCanonicalString(),
            explanationSteps = steps,
            explanation = steps.joinToString("\n"),
            options = DistractorBuilder.fractionOptions(simplified, random),
            expectedTimeSeconds = range.expectedTimeSeconds,
            fingerprint = "FRACTION:SIMPLIFY:$rawNum:$rawDen",
            generatorVersion = version,
            metadata = QuestionMetadata.FractionMeta(
                left = rawFraction,
                right = null,
                operator = "simplify",
                result = simplified,
                isSimplification = true
            ),
            complexityTier = tier,
            questionStyle = QuestionStyle.DIRECT
        )
    }

    private fun lcm(a: BigInteger, b: BigInteger): BigInteger = (a * b).abs() / a.gcd(b)

    private data class Quad(
        val op: String,
        val result: Fraction,
        val steps: List<String>,
        val fingerprint: String
    )
}

class DecimalGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.DECIMAL
    override val version: Int = 2

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val range = MathDifficultyRanges.decimalConfig(difficulty)
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)

        val factor = 10.0.pow(range.scale).toInt()
        val minScaled = range.wholeMin * factor
        val maxScaled = range.wholeMax * factor

        var rawA = random.nextInt(minScaled, maxScaled + 1)
        var rawB = random.nextInt(minScaled, maxScaled + 1)
        if (rawA % factor == 0) rawA += random.nextInt(1, factor)
        if (rawB % factor == 0) rawB += random.nextInt(1, factor)

        var a = BigDecimal(rawA).movePointLeft(range.scale)
        var b = BigDecimal(rawB).movePointLeft(range.scale)

        if (style == QuestionStyle.MISSING_OPERAND) {
            val sum = a.add(b)
            val aStr = DistractorBuilder.formatDecimal(a)
            val bStr = DistractorBuilder.formatDecimal(b)
            val sumStr = DistractorBuilder.formatDecimal(sum)
            val prompt = "$aStr + □ = $sumStr"
            val steps = listOf(
                prompt,
                "Subtract $aStr from $sumStr",
                "□ = $sumStr − $aStr",
                "= $bStr"
            )
            return MathQuestion(
                id = UUID.randomUUID().toString(),
                category = category,
                difficulty = difficulty,
                answerFormat = if (bStr.contains(".")) AnswerFormat.DECIMAL else AnswerFormat.INTEGER,
                prompt = prompt,
                answer = bStr,
                explanationSteps = steps,
                explanation = steps.joinToString("\n"),
                options = DistractorBuilder.decimalOptions(b, allowNegative = false, random = random),
                expectedTimeSeconds = range.expectedTimeSeconds + 3,
                fingerprint = "DECIMAL:MISS:$aStr:$sumStr",
                generatorVersion = version,
                metadata = QuestionMetadata.MissingOperandMeta(prompt, bStr),
                complexityTier = tier,
                questionStyle = QuestionStyle.MISSING_OPERAND
            )
        }

        if (style == QuestionStyle.MULTI_STEP || style == QuestionStyle.APPLIED_CHALLENGE) {
            var rawC = random.nextInt(minScaled, maxScaled + 1)
            if (rawC % factor == 0) rawC += random.nextInt(1, factor)
            val c = BigDecimal(rawC).movePointLeft(range.scale)
            val top = a.max(c).add(b)
            val small = a.min(c)
            val res = top.subtract(small)
            val topStr = DistractorBuilder.formatDecimal(top)
            val bStr = DistractorBuilder.formatDecimal(b)
            val smallStr = DistractorBuilder.formatDecimal(small)
            val resStr = DistractorBuilder.formatDecimal(res)
            val prompt = "$topStr − $bStr − $smallStr"
            val intermediate = top.subtract(b)
            val steps = listOf(
                prompt,
                "First: $topStr − $bStr = ${DistractorBuilder.formatDecimal(intermediate)}",
                "= ${DistractorBuilder.formatDecimal(intermediate)} − $smallStr",
                "= $resStr"
            )
            return MathQuestion(
                id = UUID.randomUUID().toString(),
                category = category,
                difficulty = difficulty,
                answerFormat = if (resStr.contains(".")) AnswerFormat.DECIMAL else AnswerFormat.INTEGER,
                prompt = prompt,
                answer = resStr,
                explanationSteps = steps,
                explanation = steps.joinToString("\n"),
                options = DistractorBuilder.decimalOptions(res, allowNegative = false, random = random),
                expectedTimeSeconds = range.expectedTimeSeconds + 6,
                fingerprint = "DECIMAL:MULTI:$topStr:$bStr:$smallStr",
                generatorVersion = version,
                metadata = QuestionMetadata.MultiStepMeta(prompt, resStr),
                complexityTier = tier,
                questionStyle = style
            )
        }

        val opMode = when {
            range.allowMultiplication -> random.nextInt(3)
            else -> random.nextInt(2)
        }

        val (opSymbol, result, steps, fingerprint) = when (opMode) {
            0 -> {
                val res = a.add(b)
                val stepList = listOf(
                    "${a.toPlainString()} + ${b.toPlainString()}",
                    "Align decimal points to ${range.scale} decimal place(s)",
                    "= ${DistractorBuilder.formatDecimal(res)}"
                )
                val fp = listOf(a.toPlainString(), b.toPlainString()).sorted()
                DecQuad("+", res, stepList, "DECIMAL:+:${fp[0]}:${fp[1]}")
            }
            1 -> {
                if (a < b) {
                    val tmp = a
                    a = b
                    b = tmp
                }
                val res = a.subtract(b)
                val stepList = listOf(
                    "${a.toPlainString()} − ${b.toPlainString()}",
                    "Align decimal points and subtract column-by-column",
                    "= ${DistractorBuilder.formatDecimal(res)}"
                )
                DecQuad("−", res, stepList, "DECIMAL:-:${a.toPlainString()}:${b.toPlainString()}")
            }
            else -> {
                val compactA = BigDecimal(random.nextInt(11, 95)).movePointLeft(1)
                val compactB = BigDecimal(random.nextInt(11, 45)).movePointLeft(1)
                a = compactA
                b = compactB
                val res = a.multiply(b)
                val intA = a.movePointRight(1).toBigIntegerExact()
                val intB = b.movePointRight(1).toBigIntegerExact()
                val stepList = listOf(
                    "${a.toPlainString()} × ${b.toPlainString()}",
                    "Multiply integers: $intA × $intB = ${intA * intB}",
                    "Shift decimal left by 2 places",
                    "= ${DistractorBuilder.formatDecimal(res)}"
                )
                val fp = listOf(a.toPlainString(), b.toPlainString()).sorted()
                DecQuad("×", res, stepList, "DECIMAL:*:${fp[0]}:${fp[1]}")
            }
        }

        val canonical = DistractorBuilder.formatDecimal(result)
        val format = if (canonical.contains(".")) AnswerFormat.DECIMAL else AnswerFormat.INTEGER

        return MathQuestion(
            id = UUID.randomUUID().toString(),
            category = category,
            difficulty = difficulty,
            answerFormat = format,
            prompt = "${DistractorBuilder.formatDecimal(a)} $opSymbol ${DistractorBuilder.formatDecimal(b)}",
            answer = canonical,
            explanationSteps = steps,
            explanation = steps.joinToString("\n"),
            options = DistractorBuilder.decimalOptions(result, allowNegative = false, random = random),
            expectedTimeSeconds = range.expectedTimeSeconds,
            fingerprint = fingerprint,
            generatorVersion = version,
            metadata = QuestionMetadata.DecimalMeta(a, b, opSymbol, result),
            complexityTier = tier,
            questionStyle = QuestionStyle.DIRECT
        )
    }

    private data class DecQuad(
        val op: String,
        val result: BigDecimal,
        val steps: List<String>,
        val fingerprint: String
    )
}
