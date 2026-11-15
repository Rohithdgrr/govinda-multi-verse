package com.example.domain.generator

import com.example.domain.model.AnswerFormat
import com.example.domain.model.Difficulty
import com.example.domain.model.MathCategory
import com.example.domain.model.MathQuestion
import com.example.domain.model.QuestionMetadata
import com.example.domain.model.QuestionStyle
import java.util.UUID
import kotlin.math.pow
import kotlin.random.Random

private fun buildTrackIntQuestion(
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

/** Practical numeracy: counting items, rupee notes/change, equal sharing & simple bills. */
class EverydayBasicsGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.EVERYDAY_BASICS
    override val version: Int = 1

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)
        val maxNote = when (difficulty) {
            Difficulty.BEGINNER -> 20
            Difficulty.BASIC -> 50
            Difficulty.INTERMEDIATE -> 100
            Difficulty.ADVANCED -> 200
            Difficulty.EXPERT -> 500
        }

        val mode = when (config.focusTag) {
            "EVERYDAY_MONEY" -> 1
            "EVERYDAY_SHARING" -> 2
            "EVERYDAY_COUNTING" -> 0
            else -> random.nextInt(3)
        }

        return when (mode) {
            0 -> {
                val a = random.nextInt(2, (maxNote / 4).coerceAtLeast(6))
                val b = random.nextInt(1, (maxNote / 4).coerceAtLeast(5))
                val items = listOf("apples", "books", "mangoes", "cups", "pens")[random.nextInt(5)]
                if (style == QuestionStyle.MISSING_OPERAND) {
                    val total = a + b
                    val prompt = "You need $total $items. You have $a. How many more?"
                    buildTrackIntQuestion(
                        category, difficulty, prompt, b.toLong(),
                        listOf(prompt, "$total − $a = $b"),
                        10, "EVD:NEED:$a:$total", tier, style,
                        QuestionMetadata.MissingOperandMeta(prompt, b.toString()), random = random
                    )
                } else {
                    val ans = (a + b).toLong()
                    val prompt = "You have $a $items and get $b more. Total $items?"
                    buildTrackIntQuestion(
                        category, difficulty, prompt, ans,
                        listOf(prompt, "$a + $b = $ans"),
                        8, "EVD:CNT:$a:$b", tier, style,
                        QuestionMetadata.MultiStepMeta(prompt, ans.toString()), random = random
                    )
                }
            }
            1 -> {
                val paid = listOf(10, 20, 50, 100, 200, 500).filter { it <= maxNote }.random(random)
                val cost = random.nextInt(2, (paid - 1).coerceAtLeast(3))
                val change = (paid - cost).toLong()
                val prompt = "Item costs ₹$cost. You pay ₹$paid. Change left (₹)?"
                buildTrackIntQuestion(
                    category, difficulty, prompt, change,
                    listOf(prompt, "₹$paid − ₹$cost = ₹$change"),
                    10, "EVD:PAY:$paid:$cost", tier, style,
                    QuestionMetadata.MultiStepMeta(prompt, change.toString()), random = random
                )
            }
            else -> {
                val people = random.nextInt(2, 7)
                val each = random.nextInt(2, (maxNote / 10).coerceAtLeast(4))
                val total = people * each
                val prompt = "$total sweets shared equally among $people people. Each gets?"
                buildTrackIntQuestion(
                    category, difficulty, prompt, each.toLong(),
                    listOf(prompt, "$total ÷ $people = $each"),
                    10, "EVD:SHR:$total:$people", tier, style,
                    QuestionMetadata.DivisionMeta(total.toLong(), people.toLong(), each.toLong()), random = random
                )
            }
        }
    }
}

/** Computer science & coding logic: binary, powers of 2, bitwise XOR/AND/OR, loops & modulo. */
class CodingLogicGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.CODING_LOGIC
    override val version: Int = 1

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)

        val mode = when (config.focusTag) {
            "CODING_BINARY" -> random.nextInt(3)
            "CODING_ALGO" -> 3 + random.nextInt(2)
            else -> random.nextInt(5)
        }

        return when (mode) {
            0 -> { // Binary -> Decimal
                val bits = when (difficulty) {
                    Difficulty.BEGINNER -> 4
                    Difficulty.BASIC -> 5
                    Difficulty.INTERMEDIATE -> 6
                    Difficulty.ADVANCED -> 7
                    Difficulty.EXPERT -> 8
                }
                val value = random.nextInt(3, 1 shl bits)
                val bin = value.toString(2)
                val prompt = "Binary $bin₂ in decimal = ?"
                buildTrackIntQuestion(
                    category, difficulty, prompt, value.toLong(),
                    listOf(prompt, "Sum powers of 2 where bit = 1", "= $value"),
                    12, "COD:BIN:$bin", tier, style,
                    QuestionMetadata.MultiStepMeta(prompt, value.toString()), random = random
                )
            }
            1 -> { // Bitwise AND / OR / XOR
                val maxVal = when (difficulty) {
                    Difficulty.BEGINNER -> 15
                    Difficulty.BASIC -> 31
                    Difficulty.INTERMEDIATE -> 63
                    else -> 127
                }
                val a = random.nextInt(2, maxVal)
                val b = random.nextInt(2, maxVal)
                val (opName, res) = when (random.nextInt(3)) {
                    0 -> "AND (&)" to (a and b)
                    1 -> "OR (|)" to (a or b)
                    else -> "XOR (^)" to (a xor b)
                }
                val prompt = "Bitwise: $a $opName $b = ?"
                buildTrackIntQuestion(
                    category, difficulty, prompt, res.toLong(),
                    listOf("$a = ${a.toString(2)}₂, $b = ${b.toString(2)}₂", "Apply bit-by-bit $opName", "= $res"),
                    15, "COD:BIT:$opName:$a:$b", tier, style,
                    QuestionMetadata.MultiStepMeta(prompt, res.toString()), random = random
                )
            }
            2 -> { // Bits / Bytes / Powers of 2
                if (random.nextBoolean()) {
                    val exp = random.nextInt(3, (difficulty.level + 6).coerceAtMost(12))
                    val ans = 1L shl exp
                    val prompt = "2^$exp (number of states in $exp bits) = ?"
                    buildTrackIntQuestion(
                        category, difficulty, prompt, ans,
                        listOf(prompt, "Multiply 2 by itself $exp times", "= $ans"),
                        10, "COD:POW2:$exp", tier, style,
                        QuestionMetadata.PowerRootMeta(2L, exp, isRoot = false, result = ans), random = random
                    )
                } else {
                    val bytes = random.nextInt(2, difficulty.level * 8 + 4)
                    val bits = (bytes * 8).toLong()
                    val prompt = "How many bits are in $bytes bytes?"
                    buildTrackIntQuestion(
                        category, difficulty, prompt, bits,
                        listOf("1 byte = 8 bits", "$bytes × 8 = $bits bits"),
                        8, "COD:BYTE:$bytes", tier, style,
                        QuestionMetadata.BinaryIntMeta(bytes.toLong(), 8L, "×", bits), random = random
                    )
                }
            }
            3 -> { // Modulo arithmetic
                val mod = random.nextInt(3, 13 + difficulty.level * 2)
                val base = random.nextInt(mod + 2, mod * 6 + 15)
                val ans = (base % mod).toLong()
                val prompt = "Output of ($base % $mod) = ?"
                buildTrackIntQuestion(
                    category, difficulty, prompt, ans,
                    listOf("$base = (${base / mod} × $mod) + $ans", "Remainder = $ans"),
                    10, "COD:MOD:$base:$mod", tier, style,
                    QuestionMetadata.MultiStepMeta(prompt, ans.toString()), random = random
                )
            }
            else -> { // Loop iteration count
                val start = random.nextInt(0, 5)
                val step = random.nextInt(1, 5)
                val count = random.nextInt(3, 8 + difficulty.level * 2)
                val end = start + step * count
                val prompt = "for (i = $start; i < $end; i += $step) runs how many times?"
                buildTrackIntQuestion(
                    category, difficulty, prompt, count.toLong(),
                    listOf("Iterations = ⌈($end − $start) / $step⌉", "= ${end - start} ÷ $step = $count"),
                    12, "COD:LOOP:$start:$end:$step", tier, style,
                    QuestionMetadata.MultiStepMeta(prompt, count.toString()), random = random
                )
            }
        }
    }
}

/** Engineering mathematics & applied numericals: calculus slopes, determinants, logs & circuits. */
class EngineeringMathGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.ENGINEERING_MATH
    override val version: Int = 1

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)

        val mode = when (config.focusTag) {
            "ENG_CALCULUS" -> random.nextInt(3)
            "ENG_PHYSICS" -> 3 + random.nextInt(2)
            else -> random.nextInt(5)
        }

        return when (mode) {
            0 -> { // Derivative slope at x = x0: d/dx(a x^2 + b x) = 2ax + b
                val a = random.nextInt(1, 3 + difficulty.level)
                val b = random.nextInt(1, 6 + difficulty.level)
                val x0 = random.nextInt(1, 4 + difficulty.level)
                val ans = (2L * a * x0 + b)
                val prompt = "Find d/dx (${a}x² + ${b}x) at x = $x0"
                buildTrackIntQuestion(
                    category, difficulty, prompt, ans,
                    listOf("f'(x) = ${2 * a}x + $b", "At x = $x0: ${2 * a}($x0) + $b = $ans"),
                    14, "ENG:DRV:$a:$b:$x0", tier, style,
                    QuestionMetadata.MultiStepMeta(prompt, ans.toString()), random = random
                )
            }
            1 -> { // 2x2 Matrix determinant: ad - bc
                val maxEntry = 4 + difficulty.level * 2
                val a = random.nextInt(2, maxEntry)
                val d = random.nextInt(2, maxEntry)
                val b = random.nextInt(1, maxEntry - 1)
                val c = random.nextInt(1, ((a * d) / b).coerceIn(2, maxEntry))
                val det = (a * d - b * c).toLong()
                val prompt = "Determinant of 2×2 matrix [$a, $b; $c, $d] = ?"
                buildTrackIntQuestion(
                    category, difficulty, prompt, det,
                    listOf("det = a·d − b·c", "= ($a × $d) − ($b × $c) = ${a * d} − ${b * c} = $det"),
                    14, "ENG:DET:$a:$b:$c:$d", tier, style,
                    QuestionMetadata.MultiStepMeta(prompt, det.toString()), allowNegative = det < 0, random = random
                )
            }
            2 -> { // Exact integer logarithm
                val base = listOf(2, 3, 5, 10).random(random)
                val maxExp = if (base == 2) 8 else if (base == 10) 5 else 4
                val exp = random.nextInt(1, maxExp + 1)
                val value = base.toDouble().pow(exp).toLong()
                val prompt = "log base $base of $value = ?"
                buildTrackIntQuestion(
                    category, difficulty, prompt, exp.toLong(),
                    listOf("$base^$exp = $value", "Therefore log_$base($value) = $exp"),
                    10, "ENG:LOG:$base:$exp", tier, style,
                    QuestionMetadata.PowerRootMeta(base.toLong(), exp, isRoot = false, result = value), random = random
                )
            }
            3 -> { // Ohm's law & power: V = I*R or P = V*I
                val current = random.nextInt(2, 8 + difficulty.level * 2)
                val resistance = random.nextInt(2, 12 + difficulty.level * 3)
                val voltage = (current * resistance).toLong()
                val prompt = "Circuit: I = ${current}A, R = ${resistance}Ω. Voltage V = ?"
                buildTrackIntQuestion(
                    category, difficulty, prompt, voltage,
                    listOf("Ohm's Law: V = I × R", "V = $current × $resistance = ${voltage}V"),
                    10, "ENG:OHM:$current:$resistance", tier, style,
                    QuestionMetadata.BinaryIntMeta(current.toLong(), resistance.toLong(), "×", voltage), random = random
                )
            }
            else -> { // Kinematics v = u + a*t
                val u = random.nextInt(0, 15)
                val a = random.nextInt(2, 8)
                val t = random.nextInt(2, 8)
                val v = (u + a * t).toLong()
                val prompt = "Kinematics: u=${u}m/s, a=${a}m/s², t=${t}s → v = ?"
                buildTrackIntQuestion(
                    category, difficulty, prompt, v,
                    listOf("v = u + a·t", "v = $u + ($a × $t) = $v m/s"),
                    12, "ENG:KIN:$u:$a:$t", tier, style,
                    QuestionMetadata.MultiStepMeta(prompt, v.toString()), random = random
                )
            }
        }
    }
}

private data class GkMcqFact(
    val topic: String,
    val minDifficulty: Int,
    val prompt: String,
    val answer: String,
    val options: List<String>,
    val step: String
)

/** Curated offline General Knowledge bank (CC-BY-4.0 LearnClash + verified civic/science facts) with 4 tappable MCQ options. */
class GeneralKnowledgeGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.GENERAL_KNOWLEDGE
    override val version: Int = 2

    private val facts = listOf(
        // Science & Space
        GkMcqFact(
            "SCIENCE_SPACE", 1,
            "Which planet in our Solar System has the highest average surface temperature due to a runaway greenhouse effect?",
            "Venus", listOf("Mercury", "Venus", "Mars", "Jupiter"),
            "Venus has a dense carbon dioxide atmosphere that traps heat."
        ),
        GkMcqFact(
            "SCIENCE_SPACE", 1,
            "Which layer of Earth's atmosphere contains the ozone layer that absorbs ultraviolet radiation?",
            "Stratosphere", listOf("Troposphere", "Stratosphere", "Mesosphere", "Thermosphere"),
            "The ozone layer is concentrated in the lower stratosphere (~15–35 km)."
        ),
        GkMcqFact(
            "SCIENCE_SPACE", 2,
            "Which chemical element has the atomic symbol 'W' and the highest melting point of all pure metals?",
            "Tungsten", listOf("Titanium", "Platinum", "Tungsten", "Osmium"),
            "Tungsten (Wolfram, symbol W) melts at 3,422 °C."
        ),
        GkMcqFact(
            "SCIENCE_SPACE", 2,
            "How many bones are present in a normal adult human skeleton?",
            "206", listOf("196", "206", "216", "232"),
            "An adult human skeleton consists of 206 bones."
        ),
        GkMcqFact(
            "SCIENCE_SPACE", 3,
            "Which gas is the most abundant in Earth's dry atmosphere (~78%)?",
            "Nitrogen (N₂)", listOf("Oxygen (O₂)", "Carbon Dioxide (CO₂)", "Nitrogen (N₂)", "Argon (Ar)"),
            "Dry air contains ~78.08% Nitrogen and ~20.95% Oxygen."
        ),
        GkMcqFact(
            "SCIENCE_SPACE", 4,
            "What is the chemical symbol for Gold on the periodic table?",
            "Au", listOf("Ag", "Fe", "Au", "Cu"),
            "Au derives from the Latin 'aurum' meaning shining dawn (Gold)."
        ),
        // India, Polity & History
        GkMcqFact(
            "INDIA_POLITY", 1,
            "How many spokes are depicted in the Ashoka Chakra on India's national flag?",
            "24", listOf("16", "20", "24", "32"),
            "The Ashoka Chakra has 24 spokes representing the 24 hours of the day."
        ),
        GkMcqFact(
            "INDIA_POLITY", 1,
            "Which river is the longest river flowing entirely within India?",
            "Godavari", listOf("Godavari", "Yamuna", "Narmada", "Kaveri"),
            "The Godavari (Dakshin Ganga) flows for ~1,465 km entirely within India."
        ),
        GkMcqFact(
            "INDIA_POLITY", 2,
            "In which year did the Constituent Assembly adopt the Constitution of India (26 November)?",
            "1949", listOf("1947", "1949", "1950", "1952"),
            "Adopted on 26 November 1949 and enforced on 26 January 1950."
        ),
        GkMcqFact(
            "INDIA_POLITY", 3,
            "How many Fundamental Duties are listed under Article 51A of the Indian Constitution?",
            "11", listOf("9", "10", "11", "12"),
            "Originally 10 duties (42nd Amendment); the 86th Amendment added the 11th."
        ),
        GkMcqFact(
            "INDIA_POLITY", 4,
            "Which Article of the Indian Constitution guarantees Equality before Law?",
            "Article 14", listOf("Article 12", "Article 14", "Article 19", "Article 21"),
            "Article 14 ensures equality before law and equal protection of laws."
        ),
        GkMcqFact(
            "INDIA_POLITY", 5,
            "Which Constitutional Amendment lowered the voting age in India from 21 to 18 years?",
            "61st Amendment", listOf("42nd Amendment", "44th Amendment", "61st Amendment", "73rd Amendment"),
            "The 61st Amendment Act, 1988 reduced the voting age under Article 326 to 18."
        ),
        // World Geography & General Facts
        GkMcqFact(
            "WORLD_MIXED", 1,
            "Which is the largest and deepest ocean on Earth?",
            "Pacific Ocean", listOf("Atlantic Ocean", "Indian Ocean", "Pacific Ocean", "Arctic Ocean"),
            "The Pacific Ocean covers more than 30% of Earth's surface."
        ),
        GkMcqFact(
            "WORLD_MIXED", 2,
            "How many permanent members hold veto power on the UN Security Council?",
            "5", listOf("4", "5", "7", "10"),
            "The 5 permanent members are China, France, Russia, the UK, and the USA."
        ),
        GkMcqFact(
            "WORLD_MIXED", 3,
            "What is the official currency of Japan?",
            "Yen", listOf("Won", "Yuan", "Yen", "Baht"),
            "The Japanese Yen (¥, JPY) is the official currency of Japan."
        ),
        GkMcqFact(
            "WORLD_MIXED", 4,
            "How many days are there in a standard leap year?",
            "366", listOf("364", "365", "366", "367"),
            "A leap year includes February 29, totaling 366 days."
        )
    )

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)
        val poolByFocus = when (config.focusTag) {
            "SCIENCE_SPACE", "INDIA_POLITY", "WORLD_MIXED" ->
                facts.filter { it.topic == config.focusTag }
            else -> facts
        }
        val levelPool = poolByFocus.filter { it.minDifficulty <= difficulty.level }.ifEmpty { poolByFocus }
        val picked = levelPool[random.nextInt(levelPool.size)]
        val shuffledOptions = picked.options.shuffled(random)

        return MathQuestion(
            id = UUID.randomUUID().toString(),
            category = category,
            difficulty = difficulty,
            answerFormat = AnswerFormat.MULTIPLE_CHOICE,
            prompt = picked.prompt,
            answer = picked.answer,
            explanationSteps = listOf(picked.prompt, picked.step),
            explanation = "${picked.prompt}\n${picked.step}",
            options = shuffledOptions,
            expectedTimeSeconds = 12,
            fingerprint = "GK:${picked.topic}:${picked.prompt.hashCode()}",
            generatorVersion = version,
            metadata = QuestionMetadata.MultiStepMeta(picked.prompt, picked.answer),
            complexityTier = tier,
            questionStyle = style
        )
    }
}

/** Competitive & Government exam aptitude: trains, clock angles, pipes, CI & discounts. */
class CompetitiveQuantGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.COMPETITIVE_QUANT
    override val version: Int = 1

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val style = effectiveStyle(config)
        val tier = config.complexityTier.coerceIn(1, 4)

        return when (random.nextInt(5)) {
            0 -> { // Train crossing a pole: speed in multiples of 18 km/h
                val speedMs = random.nextInt(10, 20 + difficulty.level * 5) // m/s
                val alignedMs = (speedMs / 5).coerceAtLeast(2) * 5
                val kmh = alignedMs * 18 / 5
                val timeSec = random.nextInt(5, 14 + difficulty.level * 3)
                val length = (alignedMs * timeSec).toLong()
                val prompt = "Train (${length}m) runs at $kmh km/h. Time (sec) to pass a pole?"
                buildTrackIntQuestion(
                    category, difficulty, prompt, timeSec.toLong(),
                    listOf("$kmh km/h × 5/18 = $alignedMs m/s", "Time = $length ÷ $alignedMs = $timeSec sec"),
                    18, "CMP:TRAIN:$length:$kmh", tier, style,
                    QuestionMetadata.MultiStepMeta(prompt, timeSec.toString()), random = random
                )
            }
            1 -> { // Clock hands acute angle at exact hour H:00
                val hour = random.nextInt(1, 12)
                val raw = hour * 30
                val angle = minOf(raw, 360 - raw).toLong()
                val prompt = "Smaller angle (degrees) between clock hands at $hour:00?"
                buildTrackIntQuestion(
                    category, difficulty, prompt, angle,
                    listOf("Each hour gap = 30°", "$hour × 30° = ${raw}° -> smaller angle = $angle°"),
                    10, "CMP:CLK:$hour", tier, style,
                    QuestionMetadata.MultiStepMeta(prompt, angle.toString()), random = random
                )
            }
            2 -> { // Successive discounts d1% then d2% -> net % = d1 + d2 - d1*d2/100
                val d1 = listOf(10, 20, 30, 40, 50).random(random)
                val d2 = listOf(10, 20, 30, 50).random(random)
                val net = (d1 + d2 - (d1 * d2) / 100).toLong()
                val prompt = "Successive discounts of $d1% and $d2% equal a single discount of (%)?"
                buildTrackIntQuestion(
                    category, difficulty, prompt, net,
                    listOf("Net = d₁ + d₂ − (d₁×d₂)/100", "= $d1 + $d2 − ${(d1 * d2) / 100} = $net%"),
                    16, "CMP:DISC:$d1:$d2", tier, style,
                    QuestionMetadata.MultiStepMeta(prompt, net.toString()), random = random
                )
            }
            3 -> { // 2-year Compound Interest on multiple of 1000 at 10% or 20%
                val rate = if (difficulty.level <= 2) 10 else listOf(10, 20).random(random)
                val p = random.nextInt(1, 8 + difficulty.level * 2) * 1000L
                val ci = p * rate / 100L + (p + p * rate / 100L) * rate / 100L
                val prompt = "Compound Interest on ₹$p at $rate% p.a. for 2 years = ₹?"
                buildTrackIntQuestion(
                    category, difficulty, prompt, ci,
                    listOf("Year 1 interest = ₹${p * rate / 100}", "Year 2 interest on ₹${p + p * rate / 100} = ₹${ci - p * rate / 100}", "Total CI = ₹$ci"),
                    20, "CMP:CI:$p:$rate", tier, style,
                    QuestionMetadata.MultiStepMeta(prompt, ci.toString()), random = random
                )
            }
            else -> { // Handshakes / combinations nC2
                val n = random.nextInt(4, 9 + difficulty.level * 2)
                val ans = (n * (n - 1) / 2).toLong()
                val prompt = "In a room of $n people, everyone shakes hands once. Total handshakes?"
                buildTrackIntQuestion(
                    category, difficulty, prompt, ans,
                    listOf("Formula: n(n−1) ÷ 2", "= $n × ${n - 1} ÷ 2 = $ans"),
                    14, "CMP:NC2:$n", tier, style,
                    QuestionMetadata.MultiStepMeta(prompt, ans.toString()), random = random
                )
            }
        }
    }
}
