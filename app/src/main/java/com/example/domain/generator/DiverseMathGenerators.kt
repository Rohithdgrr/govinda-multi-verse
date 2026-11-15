package com.example.domain.generator

import com.example.domain.model.AnswerFormat
import com.example.domain.model.Difficulty
import com.example.domain.model.MathCategory
import com.example.domain.model.MathQuestion
import com.example.domain.model.QuestionMetadata
import com.example.domain.model.QuestionStyle
import java.util.UUID
import kotlin.random.Random

private fun buildIntQuestion(
    category: MathCategory,
    difficulty: Difficulty,
    prompt: String,
    result: Long,
    steps: List<String>,
    expectedSeconds: Int,
    fingerprint: String,
    tier: Int,
    style: QuestionStyle,
    metadata: QuestionMetadata,
    allowNegative: Boolean = false,
    random: Random
): MathQuestion = MathQuestion(
    id = UUID.randomUUID().toString(),
    category = category,
    difficulty = difficulty,
    answerFormat = AnswerFormat.INTEGER,
    prompt = prompt,
    answer = result.toString(),
    explanationSteps = steps,
    explanation = steps.joinToString("\n"),
    options = DistractorBuilder.integerOptions(result, allowNegative = allowNegative, random = random),
    expectedTimeSeconds = expectedSeconds,
    fingerprint = fingerprint,
    generatorVersion = 1,
    metadata = metadata,
    complexityTier = tier,
    questionStyle = style
)

class SquaresGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.SQUARES
    override val version: Int = 1

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)
        val (minB, maxB, expSec) = when (difficulty) {
            Difficulty.BEGINNER -> Triple(2, 12, 8)
            Difficulty.BASIC -> Triple(6, 18, 11)
            Difficulty.INTERMEDIATE -> Triple(11, 26, 15)
            Difficulty.ADVANCED -> Triple(16, 36, 20)
            Difficulty.EXPERT -> Triple(22, 50, 26)
        }
        val a = random.nextInt(minB, maxB + 1).toLong()

        return when (style) {
            QuestionStyle.MISSING_OPERAND -> {
                val sq = a * a
                val prompt = "□² = $sq"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = a,
                    steps = listOf(prompt, "Which positive number multiplied by itself gives $sq?", "$a × $a = $sq", "□ = $a"),
                    expectedSeconds = expSec + 2,
                    fingerprint = "SQR:MISS:$sq",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MissingOperandMeta(prompt, a.toString()),
                    random = random
                )
            }
            QuestionStyle.MULTI_STEP -> {
                val b = random.nextInt(2, a.toInt().coerceAtLeast(3)).toLong()
                val subtract = random.nextBoolean()
                val res = if (subtract) a * a - b * b else a * a + b * b
                val op = if (subtract) "−" else "+"
                val prompt = "$a² $op $b²"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = res,
                    steps = listOf(prompt, "$a² = ${a * a}, $b² = ${b * b}", "= ${a * a} $op ${b * b}", "= $res"),
                    expectedSeconds = expSec + 5,
                    fingerprint = "SQR:MULTI:$op:$a:$b",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, res.toString()),
                    random = random
                )
            }
            QuestionStyle.APPLIED_CHALLENGE -> {
                val sq = a * a
                val prompt = "Area of square (side = $a)"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = sq,
                    steps = listOf(prompt, "Area = side × side = $a²", "= $a × $a = $sq"),
                    expectedSeconds = expSec + 3,
                    fingerprint = "SQR:AREA:$a",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.PowerRootMeta(a, 2, isRoot = false, result = sq),
                    random = random
                )
            }
            QuestionStyle.DIRECT -> {
                val sq = a * a
                val prompt = "$a²"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = sq,
                    steps = listOf(prompt, "= $a × $a", "= $sq"),
                    expectedSeconds = expSec,
                    fingerprint = "SQR:DIR:$a",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.PowerRootMeta(a, 2, isRoot = false, result = sq),
                    random = random
                )
            }
        }
    }
}

class CubesGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.CUBES
    override val version: Int = 1

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)
        val (minB, maxB, expSec) = when (difficulty) {
            Difficulty.BEGINNER -> Triple(2, 6, 10)
            Difficulty.BASIC -> Triple(3, 9, 13)
            Difficulty.INTERMEDIATE -> Triple(5, 12, 18)
            Difficulty.ADVANCED -> Triple(7, 15, 24)
            Difficulty.EXPERT -> Triple(9, 20, 30)
        }
        val a = random.nextInt(minB, maxB + 1).toLong()
        val cubeA = a * a * a

        return when (style) {
            QuestionStyle.MISSING_OPERAND -> {
                val prompt = "□³ = $cubeA"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = a,
                    steps = listOf(prompt, "Find n where n × n × n = $cubeA", "$a × $a × $a = $cubeA", "□ = $a"),
                    expectedSeconds = expSec + 3,
                    fingerprint = "CUB:MISS:$cubeA",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MissingOperandMeta(prompt, a.toString()),
                    random = random
                )
            }
            QuestionStyle.MULTI_STEP -> {
                val b = random.nextInt(2, a.toInt().coerceAtLeast(3)).toLong()
                val cubeB = b * b * b
                val subtract = random.nextBoolean()
                val res = if (subtract) cubeA - cubeB else cubeA + cubeB
                val op = if (subtract) "−" else "+"
                val prompt = "$a³ $op $b³"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = res,
                    steps = listOf(prompt, "$a³ = $cubeA, $b³ = $cubeB", "= $cubeA $op $cubeB", "= $res"),
                    expectedSeconds = expSec + 6,
                    fingerprint = "CUB:MULTI:$op:$a:$b",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, res.toString()),
                    random = random
                )
            }
            QuestionStyle.APPLIED_CHALLENGE -> {
                val prompt = "Volume of cube (side = $a)"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = cubeA,
                    steps = listOf(prompt, "Volume = side³ = $a × $a × $a", "= $cubeA"),
                    expectedSeconds = expSec + 4,
                    fingerprint = "CUB:VOL:$a",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.PowerRootMeta(a, 3, isRoot = false, result = cubeA),
                    random = random
                )
            }
            QuestionStyle.DIRECT -> {
                val prompt = "$a³"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = cubeA,
                    steps = listOf(prompt, "= $a × $a × $a", "= ${a * a} × $a", "= $cubeA"),
                    expectedSeconds = expSec,
                    fingerprint = "CUB:DIR:$a",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.PowerRootMeta(a, 3, isRoot = false, result = cubeA),
                    random = random
                )
            }
        }
    }
}

class RootsGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.ROOTS
    override val version: Int = 1

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)
        val (maxSqRoot, maxCubeRoot, expSec) = when (difficulty) {
            Difficulty.BEGINNER -> Triple(12, 5, 9)
            Difficulty.BASIC -> Triple(18, 8, 12)
            Difficulty.INTERMEDIATE -> Triple(25, 11, 16)
            Difficulty.ADVANCED -> Triple(35, 14, 21)
            Difficulty.EXPERT -> Triple(45, 18, 26)
        }

        return when (style) {
            QuestionStyle.MISSING_OPERAND -> {
                val useCube = difficulty.level >= Difficulty.BASIC.level && random.nextBoolean()
                val rootVal = random.nextInt(2, if (useCube) maxCubeRoot + 1 else maxSqRoot + 1).toLong()
                val radicand = if (useCube) rootVal * rootVal * rootVal else rootVal * rootVal
                val prompt = if (useCube) "∛□ = $rootVal" else "√□ = $rootVal"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = radicand,
                    steps = listOf(prompt, if (useCube) "Cube both sides: □ = $rootVal³" else "Square both sides: □ = $rootVal²", "□ = $radicand"),
                    expectedSeconds = expSec + 3,
                    fingerprint = "ROT:MISS:$useCube:$rootVal",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MissingOperandMeta(prompt, radicand.toString()),
                    random = random
                )
            }
            QuestionStyle.MULTI_STEP -> {
                val r1 = random.nextInt(2, maxSqRoot + 1).toLong()
                val r2 = random.nextInt(2, maxCubeRoot + 1).toLong()
                val rad1 = r1 * r1
                val useCubeSecond = difficulty.level >= Difficulty.BASIC.level && random.nextBoolean()
                val rad2 = if (useCubeSecond) r2 * r2 * r2 else r2 * r2
                val secondTerm = if (useCubeSecond) "∛$rad2" else "√$rad2"
                val res = r1 + r2
                val prompt = "√$rad1 + $secondTerm"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = res,
                    steps = listOf(prompt, "√$rad1 = $r1 and $secondTerm = $r2", "= $r1 + $r2 = $res"),
                    expectedSeconds = expSec + 5,
                    fingerprint = "ROT:MULTI:$rad1:$secondTerm",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, res.toString()),
                    random = random
                )
            }
            QuestionStyle.APPLIED_CHALLENGE -> {
                val r1 = random.nextInt(2, (maxSqRoot / 2).coerceAtLeast(5)).toLong()
                val r2 = random.nextInt(2, (maxSqRoot / 2).coerceAtLeast(5)).toLong()
                val prod = (r1 * r1) * (r2 * r2)
                val res = r1 * r2
                val prompt = "√(${r1 * r1} × ${r2 * r2})"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = res,
                    steps = listOf(prompt, "= √${r1 * r1} × √${r2 * r2}", "= $r1 × $r2 = $res (√$prod)"),
                    expectedSeconds = expSec + 5,
                    fingerprint = "ROT:APP:${r1 * r1}:${r2 * r2}",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, res.toString()),
                    random = random
                )
            }
            QuestionStyle.DIRECT -> {
                val useCube = difficulty.level >= Difficulty.BASIC.level && random.nextBoolean()
                val rootVal = random.nextInt(2, if (useCube) maxCubeRoot + 1 else maxSqRoot + 1).toLong()
                val radicand = if (useCube) rootVal * rootVal * rootVal else rootVal * rootVal
                val prompt = if (useCube) "∛$radicand" else "√$radicand"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = rootVal,
                    steps = listOf(prompt, "${if (useCube) "$rootVal³" else "$rootVal²"} = $radicand", "= $rootVal"),
                    expectedSeconds = expSec,
                    fingerprint = "ROT:DIR:$useCube:$radicand",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.PowerRootMeta(radicand, if (useCube) 3 else 2, isRoot = true, result = rootVal),
                    random = random
                )
            }
        }
    }
}

class BodmasGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.BODMAS
    override val version: Int = 1

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)
        val (maxNum, maxDiv, expSec) = when (difficulty) {
            Difficulty.BEGINNER -> Triple(12, 6, 12)
            Difficulty.BASIC -> Triple(20, 9, 16)
            Difficulty.INTERMEDIATE -> Triple(35, 12, 22)
            Difficulty.ADVANCED -> Triple(60, 15, 28)
            Difficulty.EXPERT -> Triple(90, 20, 34)
        }

        val d = random.nextInt(2, maxDiv + 1).toLong()
        val q = random.nextInt(2, 10).toLong()
        val dividend = d * q
        val partA = random.nextInt(1, dividend.toInt().coerceAtLeast(2)).toLong()
        val partB = dividend - partA
        val mul = random.nextInt(2, 7).toLong()
        val add = random.nextInt(2, maxNum + 1).toLong()

        return when (style) {
            QuestionStyle.MISSING_OPERAND -> {
                val res = q * mul
                val prompt = "($partA + □) ÷ $d × $mul = $res"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = partB,
                    steps = listOf(
                        prompt,
                        "Divide by $mul: ($partA + □) ÷ $d = $q",
                        "Multiply by $d: $partA + □ = $dividend",
                        "□ = $dividend − $partA = $partB"
                    ),
                    expectedSeconds = expSec + 5,
                    fingerprint = "BDM:MISS:$partA:$d:$mul:$res",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MissingOperandMeta(prompt, partB.toString()),
                    random = random
                )
            }
            QuestionStyle.MULTI_STEP -> {
                val base = random.nextInt(2, 8).toLong()
                val orderVal = base * base
                val total = orderVal + q * mul
                val prompt = "$base² + ($partA + $partB) ÷ $d × $mul"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = total,
                    steps = listOf(
                        prompt,
                        "Brackets first: ($partA + $partB) = $dividend",
                        "Orders: $base² = $orderVal",
                        "Division then Multiplication: $dividend ÷ $d = $q, $q × $mul = ${q * mul}",
                        "Addition: $orderVal + ${q * mul} = $total"
                    ),
                    expectedSeconds = expSec + 6,
                    fingerprint = "BDM:MULTI:$base:$partA:$partB:$d:$mul",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, total.toString()),
                    random = random
                )
            }
            QuestionStyle.APPLIED_CHALLENGE -> {
                val lead = q * mul + random.nextInt(5, 30).toLong()
                val res = lead - q * mul + add
                val prompt = "$lead − ($partA + $partB) ÷ $d × $mul + $add"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = res,
                    steps = listOf(
                        prompt,
                        "Brackets: ($partA + $partB) = $dividend",
                        "Divide: $dividend ÷ $d = $q",
                        "Multiply: $q × $mul = ${q * mul}",
                        "Left-to-right +/−: $lead − ${q * mul} + $add = $res"
                    ),
                    expectedSeconds = expSec + 7,
                    fingerprint = "BDM:APP:$lead:$partA:$partB:$d:$mul:$add",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, res.toString()),
                    random = random
                )
            }
            QuestionStyle.DIRECT -> {
                val res = add + q * mul
                val prompt = "$add + ($partA + $partB) ÷ $d × $mul"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = res,
                    steps = listOf(
                        prompt,
                        "Brackets: $partA + $partB = $dividend",
                        "Divide: $dividend ÷ $d = $q",
                        "Multiply: $q × $mul = ${q * mul}",
                        "Add: $add + ${q * mul} = $res"
                    ),
                    expectedSeconds = expSec,
                    fingerprint = "BDM:DIR:$add:$partA:$partB:$d:$mul",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, res.toString()),
                    random = random
                )
            }
        }
    }
}

class SimpleInterestGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.SIMPLE_INTEREST
    override val version: Int = 1

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)
        val (pScale, maxRate, maxTime, expSec) = when (difficulty) {
            Difficulty.BEGINNER -> Quad(100L, 10, 4, 12)
            Difficulty.BASIC -> Quad(100L, 12, 5, 15)
            Difficulty.INTERMEDIATE -> Quad(200L, 15, 6, 20)
            Difficulty.ADVANCED -> Quad(250L, 18, 7, 25)
            Difficulty.EXPERT -> Quad(500L, 20, 8, 30)
        }
        val principal = random.nextInt(2, 16).toLong() * pScale
        val rate = random.nextInt(2, maxRate + 1).let { r ->
            if (pScale == 250L && r % 2 != 0) r + 1 else r
        }.toLong()
        val time = random.nextInt(1, maxTime + 1).let { t ->
            if ((principal * rate * t) % 100L != 0L) t * 2L else t.toLong()
        }
        val si = (principal * rate * time) / 100L

        return when (style) {
            QuestionStyle.MISSING_OPERAND -> {
                if (random.nextBoolean()) {
                    val prompt = "SI = $si, P = $principal, T = $time yr → Rate %?"
                    buildIntQuestion(
                        category = category,
                        difficulty = difficulty,
                        prompt = prompt,
                        result = rate,
                        steps = listOf(prompt, "R = (SI × 100) ÷ (P × T)", "= (${si * 100}) ÷ ($principal × $time)", "= $rate%"),
                        expectedSeconds = expSec + 5,
                        fingerprint = "SI:MISSR:$principal:$si:$time",
                        tier = tier,
                        style = style,
                        metadata = QuestionMetadata.MissingOperandMeta(prompt, rate.toString()),
                        random = random
                    )
                } else {
                    val prompt = "SI = $si, P = $principal, R = $rate% → Time (yr)?"
                    buildIntQuestion(
                        category = category,
                        difficulty = difficulty,
                        prompt = prompt,
                        result = time,
                        steps = listOf(prompt, "T = (SI × 100) ÷ (P × R)", "= (${si * 100}) ÷ ($principal × $rate)", "= $time yr"),
                        expectedSeconds = expSec + 5,
                        fingerprint = "SI:MISST:$principal:$si:$rate",
                        tier = tier,
                        style = style,
                        metadata = QuestionMetadata.MissingOperandMeta(prompt, time.toString()),
                        random = random
                    )
                }
            }
            QuestionStyle.MULTI_STEP -> {
                val amount = principal + si
                val prompt = "Amount: P = $principal, R = $rate%, T = $time yr"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = amount,
                    steps = listOf(prompt, "SI = ($principal × $rate × $time) ÷ 100 = $si", "Amount = P + SI = $principal + $si", "= $amount"),
                    expectedSeconds = expSec + 4,
                    fingerprint = "SI:AMT:$principal:$rate:$time",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, amount.toString()),
                    random = random
                )
            }
            QuestionStyle.APPLIED_CHALLENGE -> {
                val higherRate = rate + random.nextInt(2, 6)
                val diff = (principal * (higherRate - rate) * time) / 100L
                val prompt = "SI diff on P=$principal, T=$time yr: $higherRate% vs $rate%"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = diff,
                    steps = listOf(prompt, "Rate difference = $higherRate% − $rate% = ${higherRate - rate}%", "Diff SI = ($principal × ${higherRate - rate} × $time) ÷ 100", "= $diff"),
                    expectedSeconds = expSec + 6,
                    fingerprint = "SI:DIFF:$principal:$rate:$higherRate:$time",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, diff.toString()),
                    random = random
                )
            }
            QuestionStyle.DIRECT -> {
                val prompt = "SI: P = $principal, R = $rate%, T = $time yr"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = si,
                    steps = listOf(prompt, "SI = (P × R × T) ÷ 100", "= ($principal × $rate × $time) ÷ 100", "= $si"),
                    expectedSeconds = expSec,
                    fingerprint = "SI:DIR:$principal:$rate:$time",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, si.toString()),
                    random = random
                )
            }
        }
    }

    private data class Quad(val pScale: Long, val maxRate: Int, val maxTime: Int, val expSec: Int)
}

class SpeedDistanceGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.SPEED_DISTANCE
    override val version: Int = 1

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)
        val (minSpd, maxSpd, maxTime) = when (difficulty) {
            Difficulty.BEGINNER -> Triple(15, 50, 5)
            Difficulty.BASIC -> Triple(25, 80, 7)
            Difficulty.INTERMEDIATE -> Triple(40, 110, 9)
            Difficulty.ADVANCED -> Triple(45, 140, 12)
            Difficulty.EXPERT -> Triple(55, 180, 15)
        }
        val speed = random.nextInt(minSpd, maxSpd + 1).toLong()
        val time = random.nextInt(2, maxTime + 1).toLong()
        val distance = speed * time
        val expSec = 10 + difficulty.level * 3

        return when (style) {
            QuestionStyle.MISSING_OPERAND -> {
                val prompt = "$distance km at $speed km/h → Time (h)?"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = time,
                    steps = listOf(prompt, "Time = Distance ÷ Speed", "= $distance ÷ $speed = $time h"),
                    expectedSeconds = expSec,
                    fingerprint = "SPD:TIME:$distance:$speed",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MissingOperandMeta(prompt, time.toString()),
                    random = random
                )
            }
            QuestionStyle.MULTI_STEP -> {
                val k = random.nextInt(1, 5 + difficulty.level * 2).toLong()
                val kmh = 18L * k
                val ms = 5L * k
                return if (random.nextBoolean()) {
                    val prompt = "Convert $kmh km/h to m/s"
                    buildIntQuestion(
                        category = category,
                        difficulty = difficulty,
                        prompt = prompt,
                        result = ms,
                        steps = listOf(prompt, "Multiply by 5 ÷ 18", "= ($kmh ÷ 18) × 5 = $k × 5 = $ms m/s"),
                        expectedSeconds = expSec + 3,
                        fingerprint = "SPD:KMH2MS:$kmh",
                        tier = tier,
                        style = style,
                        metadata = QuestionMetadata.MultiStepMeta(prompt, ms.toString()),
                        random = random
                    )
                } else {
                    val prompt = "Convert $ms m/s to km/h"
                    buildIntQuestion(
                        category = category,
                        difficulty = difficulty,
                        prompt = prompt,
                        result = kmh,
                        steps = listOf(prompt, "Multiply by 18 ÷ 5", "= ($ms ÷ 5) × 18 = $k × 18 = $kmh km/h"),
                        expectedSeconds = expSec + 3,
                        fingerprint = "SPD:MS2KMH:$ms",
                        tier = tier,
                        style = style,
                        metadata = QuestionMetadata.MultiStepMeta(prompt, kmh.toString()),
                        random = random
                    )
                }
            }
            QuestionStyle.APPLIED_CHALLENGE -> {
                val delta = random.nextInt(5, 25).toLong()
                val avgSpeed = speed.coerceAtLeast(delta + 10)
                val s1 = avgSpeed - delta
                val s2 = avgSpeed + delta
                val legHours = random.nextInt(2, 5).toLong()
                val prompt = "$legHours h at $s1 km/h + $legHours h at $s2 km/h → Avg speed?"
                val totalDist = (s1 + s2) * legHours
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = avgSpeed,
                    steps = listOf(prompt, "Total distance = $legHours×$s1 + $legHours×$s2 = $totalDist km", "Total time = ${legHours * 2} h", "Avg speed = $totalDist ÷ ${legHours * 2} = $avgSpeed km/h"),
                    expectedSeconds = expSec + 5,
                    fingerprint = "SPD:AVG:$s1:$s2:$legHours",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, avgSpeed.toString()),
                    random = random
                )
            }
            QuestionStyle.DIRECT -> {
                if (random.nextBoolean()) {
                    val prompt = "Speed $speed km/h for $time h → Distance (km)?"
                    buildIntQuestion(
                        category = category,
                        difficulty = difficulty,
                        prompt = prompt,
                        result = distance,
                        steps = listOf(prompt, "Distance = Speed × Time", "= $speed × $time = $distance km"),
                        expectedSeconds = expSec,
                        fingerprint = "SPD:DIST:$speed:$time",
                        tier = tier,
                        style = style,
                        metadata = QuestionMetadata.BinaryIntMeta(speed, time, "×", distance),
                        random = random
                    )
                } else {
                    val prompt = "$distance km in $time h → Speed (km/h)?"
                    buildIntQuestion(
                        category = category,
                        difficulty = difficulty,
                        prompt = prompt,
                        result = speed,
                        steps = listOf(prompt, "Speed = Distance ÷ Time", "= $distance ÷ $time = $speed km/h"),
                        expectedSeconds = expSec,
                        fingerprint = "SPD:SPD:$distance:$time",
                        tier = tier,
                        style = style,
                        metadata = QuestionMetadata.DivisionMeta(distance, time, speed),
                        random = random
                    )
                }
            }
        }
    }
}

class AverageGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.AVERAGE
    override val version: Int = 1

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)
        val (minMean, maxMean, count) = when (difficulty) {
            Difficulty.BEGINNER -> Triple(6, 18, 3)
            Difficulty.BASIC -> Triple(10, 35, 4)
            Difficulty.INTERMEDIATE -> Triple(18, 60, 5)
            Difficulty.ADVANCED -> Triple(25, 95, 5)
            Difficulty.EXPERT -> Triple(40, 150, 6)
        }
        val mean = random.nextInt(minMean, maxMean + 1).toLong()
        val expSec = 10 + difficulty.level * 3

        val nums = buildBalancedSet(mean, count, spread = (minMean / 2).coerceAtLeast(4), random = random)

        return when (style) {
            QuestionStyle.MISSING_OPERAND -> {
                val missing = nums.last()
                val shown = nums.dropLast(1).joinToString(", ")
                val sum = nums.sum()
                val prompt = "Mean of $shown, □ is $mean → □?"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = missing,
                    steps = listOf(prompt, "Total sum = $mean × $count = $sum", "Known sum = ${nums.dropLast(1).sum()}", "□ = $sum − ${nums.dropLast(1).sum()} = $missing"),
                    expectedSeconds = expSec + 4,
                    fingerprint = "AVG:MISS:$shown:$mean",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MissingOperandMeta(prompt, missing.toString()),
                    random = random
                )
            }
            QuestionStyle.MULTI_STEP -> {
                val stepUp = random.nextInt(2, 7).toLong()
                val newMean = mean + stepUp
                val addedValue = mean + stepUp * (count + 1)
                val prompt = "Avg of $count nums is $mean. Add $addedValue → New avg?"
                val oldSum = mean * count
                val newSum = oldSum + addedValue
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = newMean,
                    steps = listOf(prompt, "Original sum = $count × $mean = $oldSum", "New sum = $oldSum + $addedValue = $newSum", "New avg = $newSum ÷ ${count + 1} = $newMean"),
                    expectedSeconds = expSec + 5,
                    fingerprint = "AVG:ADD:$count:$mean:$addedValue",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, newMean.toString()),
                    random = random
                )
            }
            QuestionStyle.APPLIED_CHALLENGE -> {
                val k = random.nextInt(1, 8).toLong()
                val baseAvg = mean.coerceAtLeast(3 * k + 4)
                val avgFirst3 = baseAvg + 2 * k
                val avgNext2 = baseAvg - 3 * k
                val prompt = "Avg of 3 nums = $avgFirst3, avg of 2 more = $avgNext2 → All 5?"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = baseAvg,
                    steps = listOf(
                        prompt,
                        "Sum 1 = 3 × $avgFirst3 = ${3 * avgFirst3}",
                        "Sum 2 = 2 × $avgNext2 = ${2 * avgNext2}",
                        "Combined avg = (${3 * avgFirst3} + ${2 * avgNext2}) ÷ 5 = $baseAvg"
                    ),
                    expectedSeconds = expSec + 6,
                    fingerprint = "AVG:COMB:$avgFirst3:$avgNext2",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, baseAvg.toString()),
                    random = random
                )
            }
            QuestionStyle.DIRECT -> {
                val listStr = nums.joinToString(", ")
                val sum = nums.sum()
                val prompt = "Average of $listStr"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = mean,
                    steps = listOf(prompt, "Sum = $sum", "Count = $count", "Average = $sum ÷ $count = $mean"),
                    expectedSeconds = expSec,
                    fingerprint = "AVG:DIR:${nums.sorted().joinToString(",")}",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.DivisionMeta(sum, count.toLong(), mean),
                    random = random
                )
            }
        }
    }

    private fun buildBalancedSet(mean: Long, count: Int, spread: Int, random: Random): List<Long> {
        val list = MutableList(count) { mean }
        val pairs = count / 2
        for (i in 0 until pairs) {
            val delta = random.nextInt(1, spread + 1).toLong()
            list[i * 2] = (mean - delta).coerceAtLeast(1L)
            val actualDelta = mean - list[i * 2]
            list[i * 2 + 1] = mean + actualDelta
        }
        return list.shuffled(random)
    }
}

class StatisticsGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.STATISTICS
    override val version: Int = 1

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)
        val maxVal = when (difficulty) {
            Difficulty.BEGINNER -> 20
            Difficulty.BASIC -> 40
            Difficulty.INTERMEDIATE -> 75
            Difficulty.ADVANCED -> 120
            Difficulty.EXPERT -> 200
        }
        val expSec = 10 + difficulty.level * 3

        return when (style) {
            QuestionStyle.MISSING_OPERAND -> {
                val sorted = generateDistinctSorted(5, 2, maxVal, random)
                val med = sorted[2]
                val prompt = "Sorted set ${sorted[0]}, ${sorted[1]}, □, ${sorted[3]}, ${sorted[4]}: Median = $med → □?"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = med,
                    steps = listOf(prompt, "In a sorted list of 5 numbers, the 3rd value is the median.", "□ = $med"),
                    expectedSeconds = expSec,
                    fingerprint = "STT:MISS:${sorted.joinToString(",")}",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MissingOperandMeta(prompt, med.toString()),
                    random = random
                )
            }
            QuestionStyle.MULTI_STEP -> {
                val sorted = generateDistinctSorted(5, 2, maxVal, random)
                val range = sorted.last() - sorted.first()
                val median = sorted[2]
                val res = range + median
                val shuffled = sorted.shuffled(random)
                val prompt = "Range + Median of ${shuffled.joinToString(", ")}"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = res,
                    steps = listOf(
                        prompt,
                        "Sort: ${sorted.joinToString(", ")}",
                        "Range = ${sorted.last()} − ${sorted.first()} = $range",
                        "Median = $median → $range + $median = $res"
                    ),
                    expectedSeconds = expSec + 5,
                    fingerprint = "STT:R+M:${sorted.joinToString(",")}",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, res.toString()),
                    random = random
                )
            }
            QuestionStyle.APPLIED_CHALLENGE -> {
                val mode = random.nextInt(4, (maxVal / 2).coerceAtLeast(10)).toLong()
                val a = random.nextInt(1, mode.toInt()).toLong()
                val b = mode + random.nextInt(2, 12).toLong()
                val prefixSum = a + b + 3 * mode
                val remainder = prefixSum % 5L
                val last = mode + ((5L - remainder) % 5L) + 5L
                val data = listOf(a, mode, b, mode, last, mode).let {
                    // Use 5 values where 3 are mode -> mode is guaranteed and sum is multiple of 5
                    val rem5 = (a + 3 * mode) % 5L
                    val fifth = (b + ((5L - (b + rem5) % 5L) % 5L)).coerceAtLeast(mode + 2)
                    listOf(a, mode, fifth, mode, mode).shuffled(random)
                }
                val mean = data.sum() / 5L
                val res = mean - mode
                val prompt = "Mean − Mode of ${data.joinToString(", ")}"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = res,
                    allowNegative = true,
                    steps = listOf(
                        prompt,
                        "Mode (most frequent) = $mode",
                        "Mean = ${data.sum()} ÷ 5 = $mean",
                        "Mean − Mode = $mean − $mode = $res"
                    ),
                    expectedSeconds = expSec + 6,
                    fingerprint = "STT:M-MO:${data.sorted().joinToString(",")}",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, res.toString()),
                    random = random
                )
            }
            QuestionStyle.DIRECT -> {
                when (random.nextInt(3)) {
                    0 -> {
                        val sorted = generateDistinctSorted(5, 2, maxVal, random)
                        val median = sorted[2]
                        val shuffled = sorted.shuffled(random)
                        val prompt = "Median of ${shuffled.joinToString(", ")}"
                        buildIntQuestion(
                            category = category,
                            difficulty = difficulty,
                            prompt = prompt,
                            result = median,
                            steps = listOf(prompt, "Order values: ${sorted.joinToString(", ")}", "Middle (3rd) value = $median"),
                            expectedSeconds = expSec,
                            fingerprint = "STT:MED:${sorted.joinToString(",")}",
                            tier = tier,
                            style = style,
                            metadata = QuestionMetadata.MultiStepMeta(prompt, median.toString()),
                            random = random
                        )
                    }
                    1 -> {
                        val mode = random.nextInt(2, maxVal).toLong()
                        val others = mutableSetOf<Long>()
                        while (others.size < 3) {
                            val v = random.nextInt(1, maxVal + 5).toLong()
                            if (v != mode) others += v
                        }
                        val data = (listOf(mode, mode, mode) + others).shuffled(random)
                        val prompt = "Mode of ${data.joinToString(", ")}"
                        buildIntQuestion(
                            category = category,
                            difficulty = difficulty,
                            prompt = prompt,
                            result = mode,
                            steps = listOf(prompt, "$mode appears 3 times (more than any other number)", "Mode = $mode"),
                            expectedSeconds = expSec - 1,
                            fingerprint = "STT:MOD:${data.sorted().joinToString(",")}",
                            tier = tier,
                            style = style,
                            metadata = QuestionMetadata.MultiStepMeta(prompt, mode.toString()),
                            random = random
                        )
                    }
                    else -> {
                        val sorted = generateDistinctSorted(5, 2, maxVal, random)
                        val range = sorted.last() - sorted.first()
                        val shuffled = sorted.shuffled(random)
                        val prompt = "Range of ${shuffled.joinToString(", ")}"
                        buildIntQuestion(
                            category = category,
                            difficulty = difficulty,
                            prompt = prompt,
                            result = range,
                            steps = listOf(prompt, "Max = ${sorted.last()}, Min = ${sorted.first()}", "Range = Max − Min = ${sorted.last()} − ${sorted.first()} = $range"),
                            expectedSeconds = expSec,
                            fingerprint = "STT:RNG:${sorted.joinToString(",")}",
                            tier = tier,
                            style = style,
                            metadata = QuestionMetadata.BinaryIntMeta(sorted.last(), sorted.first(), "-", range),
                            random = random
                        )
                    }
                }
            }
        }
    }

    private fun generateDistinctSorted(count: Int, minVal: Int, maxVal: Int, random: Random): List<Long> {
        val set = mutableSetOf<Long>()
        while (set.size < count) {
            set += random.nextInt(minVal, maxVal + 1).toLong()
        }
        return set.sorted()
    }
}

class NumberSystemGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.NUMBER_SYSTEM
    override val version: Int = 1

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)
        val expSec = 10 + difficulty.level * 3

        val (gMin, gMax, mulMax) = when (difficulty) {
            Difficulty.BEGINNER -> Triple(2, 6, 5)
            Difficulty.BASIC -> Triple(3, 9, 7)
            Difficulty.INTERMEDIATE -> Triple(4, 14, 9)
            Difficulty.ADVANCED -> Triple(6, 18, 12)
            Difficulty.EXPERT -> Triple(8, 25, 15)
        }
        val g = random.nextInt(gMin, gMax + 1).toLong()
        val m1 = random.nextInt(2, mulMax + 1).toLong()
        var m2 = random.nextInt(2, mulMax + 1).toLong()
        while (gcd(m1, m2) != 1L) {
            m2 += 1L
        }
        val a = g * m1
        val b = g * m2
        val lcmVal = g * m1 * m2

        return when (style) {
            QuestionStyle.MISSING_OPERAND -> {
                val div = random.nextInt(4, 7 + difficulty.level * 3).toLong()
                val rem = random.nextInt(1, div.toInt()).toLong()
                val num = div * random.nextInt(3, 12 + difficulty.level * 4).toLong() + rem
                val addNeeded = div - rem
                val prompt = "Smallest □ > 0 so ($num + □) ÷ $div is exact?"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = addNeeded,
                    steps = listOf(prompt, "$num ÷ $div gives remainder $rem", "Add $div − $rem = $addNeeded to reach next multiple"),
                    expectedSeconds = expSec + 3,
                    fingerprint = "NUM:DIVADD:$num:$div",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MissingOperandMeta(prompt, addNeeded.toString()),
                    random = random
                )
            }
            QuestionStyle.MULTI_STEP -> {
                if (random.nextBoolean()) {
                    val n = random.nextInt(5, 9 + difficulty.level * 4).toLong()
                    val sum = (n * (n + 1)) / 2L
                    val prompt = "Sum of integers 1 + 2 + … + $n"
                    buildIntQuestion(
                        category = category,
                        difficulty = difficulty,
                        prompt = prompt,
                        result = sum,
                        steps = listOf(prompt, "Use Gauss formula: n × (n + 1) ÷ 2", "= $n × ${n + 1} ÷ 2 = $sum"),
                        expectedSeconds = expSec + 4,
                        fingerprint = "NUM:SUMN:$n",
                        tier = tier,
                        style = style,
                        metadata = QuestionMetadata.MultiStepMeta(prompt, sum.toString()),
                        random = random
                    )
                } else {
                    val res = lcmVal + g
                    val prompt = "LCM($a, $b) + HCF($a, $b)"
                    buildIntQuestion(
                        category = category,
                        difficulty = difficulty,
                        prompt = prompt,
                        result = res,
                        steps = listOf(prompt, "HCF($a, $b) = $g", "LCM($a, $b) = $lcmVal", "Sum = $lcmVal + $g = $res"),
                        expectedSeconds = expSec + 5,
                        fingerprint = "NUM:LCMHCF:$a:$b",
                        tier = tier,
                        style = style,
                        metadata = QuestionMetadata.MultiStepMeta(prompt, res.toString()),
                        random = random
                    )
                }
            }
            QuestionStyle.APPLIED_CHALLENGE -> {
                val product = a * b
                val prompt = "Product of 2 nums = $product, HCF = $g → LCM?"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = lcmVal,
                    steps = listOf(prompt, "HCF × LCM = Product of numbers", "LCM = $product ÷ $g = $lcmVal"),
                    expectedSeconds = expSec + 4,
                    fingerprint = "NUM:PROD:$product:$g",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.DivisionMeta(product, g, lcmVal),
                    random = random
                )
            }
            QuestionStyle.DIRECT -> {
                when (random.nextInt(3)) {
                    0 -> {
                        val prompt = "HCF($a, $b)"
                        buildIntQuestion(
                            category = category,
                            difficulty = difficulty,
                            prompt = prompt,
                            result = g,
                            steps = listOf(prompt, "$a = $g × $m1, $b = $g × $m2", "Greatest common factor = $g"),
                            expectedSeconds = expSec,
                            fingerprint = "NUM:HCF:${minOf(a, b)}:${maxOf(a, b)}",
                            tier = tier,
                            style = style,
                            metadata = QuestionMetadata.MultiStepMeta(prompt, g.toString()),
                            random = random
                        )
                    }
                    1 -> {
                        val prompt = "LCM($a, $b)"
                        buildIntQuestion(
                            category = category,
                            difficulty = difficulty,
                            prompt = prompt,
                            result = lcmVal,
                            steps = listOf(prompt, "HCF($a, $b) = $g", "LCM = ($a × $b) ÷ $g = $lcmVal"),
                            expectedSeconds = expSec + 2,
                            fingerprint = "NUM:LCM:${minOf(a, b)}:${maxOf(a, b)}",
                            tier = tier,
                            style = style,
                            metadata = QuestionMetadata.MultiStepMeta(prompt, lcmVal.toString()),
                            random = random
                        )
                    }
                    else -> {
                        val divisor = random.nextInt(3, 8 + difficulty.level * 3).toLong()
                        val rem = random.nextInt(1, divisor.toInt()).toLong()
                        val dividend = divisor * random.nextInt(3, 12 + difficulty.level * 5).toLong() + rem
                        val prompt = "Remainder of $dividend ÷ $divisor"
                        buildIntQuestion(
                            category = category,
                            difficulty = difficulty,
                            prompt = prompt,
                            result = rem,
                            steps = listOf(prompt, "Largest multiple of $divisor ≤ $dividend is ${dividend - rem}", "Remainder = $dividend − ${dividend - rem} = $rem"),
                            expectedSeconds = expSec - 1,
                            fingerprint = "NUM:REM:$dividend:$divisor",
                            tier = tier,
                            style = style,
                            metadata = QuestionMetadata.MultiStepMeta(prompt, rem.toString()),
                            random = random
                        )
                    }
                }
            }
        }
    }

    private fun gcd(x: Long, y: Long): Long {
        var a = x
        var b = y
        while (b != 0L) {
            val t = a % b
            a = b
            b = t
        }
        return a
    }
}

class RatioProportionGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.RATIO_PROPORTION
    override val version: Int = 1

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)
        val expSec = 10 + difficulty.level * 3

        val a = random.nextInt(2, 5 + difficulty.level * 2).toLong()
        var b = random.nextInt(2, 6 + difficulty.level * 2).toLong()
        while (b == a) b += 1L
        val k = random.nextInt(2, 5 + difficulty.level * 3).toLong()

        return when (style) {
            QuestionStyle.MISSING_OPERAND -> {
                val total = (a + b) * k
                val smaller = minOf(a, b) * k
                val prompt = "Share $total in ratio $a:$b → Smaller part?"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = smaller,
                    steps = listOf(prompt, "Total parts = $a + $b = ${a + b}", "1 part = $total ÷ ${a + b} = $k", "Smaller part = ${minOf(a, b)} × $k = $smaller"),
                    expectedSeconds = expSec + 3,
                    fingerprint = "RAT:SHARE:$total:$a:$b",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MissingOperandMeta(prompt, smaller.toString()),
                    random = random
                )
            }
            QuestionStyle.MULTI_STEP -> {
                val cp = random.nextInt(2, 12 + difficulty.level * 4).toLong() * 20L
                val profitPct = listOf(10, 15, 20, 25, 30, 40, 50).random(random).toLong()
                val profit = (cp * profitPct) / 100L
                val sp = cp + profit
                val prompt = "Cost = $cp, Profit = $profitPct% → Selling Price?"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = sp,
                    steps = listOf(prompt, "Profit = $profitPct% of $cp = $profit", "SP = $cp + $profit = $sp"),
                    expectedSeconds = expSec + 4,
                    fingerprint = "RAT:SP:$cp:$profitPct",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MultiStepMeta(prompt, sp.toString()),
                    random = random
                )
            }
            QuestionStyle.APPLIED_CHALLENGE -> {
                val w1 = random.nextInt(2, 8).toLong()
                val d2 = random.nextInt(3, 10 + difficulty.level * 2).toLong()
                val factor = random.nextInt(2, 5).toLong()
                val w2 = w1 * factor
                val d1 = d2 * factor
                val prompt = "$w1 workers take $d1 days → $w2 workers take ? days"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = d2,
                    steps = listOf(prompt, "Total work = $w1 × $d1 = ${w1 * d1} worker-days", "Days = ${w1 * d1} ÷ $w2 = $d2"),
                    expectedSeconds = expSec + 5,
                    fingerprint = "RAT:WORK:$w1:$d1:$w2",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.DivisionMeta(w1 * d1, w2, d2),
                    random = random
                )
            }
            QuestionStyle.DIRECT -> {
                val scaledA = a * k
                val scaledB = b * k
                val prompt = "$a : $b = $scaledA : □"
                buildIntQuestion(
                    category = category,
                    difficulty = difficulty,
                    prompt = prompt,
                    result = scaledB,
                    steps = listOf(prompt, "Scale factor = $scaledA ÷ $a = $k", "□ = $b × $k = $scaledB"),
                    expectedSeconds = expSec,
                    fingerprint = "RAT:PROP:$a:$b:$k",
                    tier = tier,
                    style = style,
                    metadata = QuestionMetadata.MissingOperandMeta(prompt, scaledB.toString()),
                    random = random
                )
            }
        }
    }
}
