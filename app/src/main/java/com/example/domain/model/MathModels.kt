package com.example.domain.model

import java.math.BigDecimal
import java.math.BigInteger

enum class MathCategory(
    val displayName: String,
    val symbol: String,
    val shortCode: String,
    val description: String,
    val isCoreBaseline: Boolean
) {
    ADDITION(
        displayName = "Addition",
        symbol = "+",
        shortCode = "ADD",
        description = "Multi-digit sums, place-value decomposition & carrying",
        isCoreBaseline = true
    ),
    SUBTRACTION(
        displayName = "Subtraction",
        symbol = "−",
        shortCode = "SUB",
        description = "Regrouping, borrowing & optional signed arithmetic",
        isCoreBaseline = true
    ),
    MULTIPLICATION(
        displayName = "Multiplication",
        symbol = "×",
        shortCode = "MUL",
        description = "Mental factors, distributive splitting & multi-digit products",
        isCoreBaseline = true
    ),
    DIVISION(
        displayName = "Division",
        symbol = "÷",
        shortCode = "DIV",
        description = "Exact integer quotients generated from divisor-quotient pairs",
        isCoreBaseline = true
    ),
    PERCENTAGE(
        displayName = "Percentages",
        symbol = "%",
        shortCode = "PCT",
        description = "Proportional scaling with exact integer or clean decimal results",
        isCoreBaseline = true
    ),
    FRACTION(
        displayName = "Fractions",
        symbol = "a/b",
        shortCode = "FRC",
        description = "GCD simplification, common denominators & exact fraction arithmetic",
        isCoreBaseline = true
    ),
    DECIMAL(
        displayName = "Decimals",
        symbol = ".01",
        shortCode = "DEC",
        description = "Exact BigDecimal addition, subtraction & scaling operations",
        isCoreBaseline = true
    ),
    POWERS_ROOTS(
        displayName = "Powers & Roots",
        symbol = "xⁿ",
        shortCode = "PWR",
        description = "Integer exponents, perfect squares, cubes & exact integer roots",
        isCoreBaseline = false
    ),
    MIXED_OPERATIONS(
        displayName = "Mixed Ops",
        symbol = "( )",
        shortCode = "MIX",
        description = "Order of operations combining multiplication, addition & brackets",
        isCoreBaseline = false
    ),
    SQUARES(
        displayName = "Squares",
        symbol = "n²",
        shortCode = "SQR",
        description = "Perfect squares, square sums/differences & geometric side area",
        isCoreBaseline = false
    ),
    CUBES(
        displayName = "Cubes",
        symbol = "n³",
        shortCode = "CUB",
        description = "Integer cubes, cube combinations & cubic volume calculations",
        isCoreBaseline = false
    ),
    ROOTS(
        displayName = "Roots",
        symbol = "√",
        shortCode = "ROT",
        description = "Exact square roots, cube roots & combined radical expressions",
        isCoreBaseline = false
    ),
    BODMAS(
        displayName = "BODMAS",
        symbol = "[ ]",
        shortCode = "BDM",
        description = "Full Brackets, Orders, Division, Multiplication, Addition & Subtraction",
        isCoreBaseline = false
    ),
    SIMPLE_INTEREST(
        displayName = "Simple Interest",
        symbol = "SI",
        shortCode = "INT",
        description = "Principal, rate, time & amount calculations using SI = PRT / 100",
        isCoreBaseline = false
    ),
    SPEED_DISTANCE(
        displayName = "Speed & Distance",
        symbol = "v=d/t",
        shortCode = "SPD",
        description = "Speed, distance, time, average speed & km/h ↔ m/s conversions",
        isCoreBaseline = false
    ),
    AVERAGE(
        displayName = "Average",
        symbol = "x̄",
        shortCode = "AVG",
        description = "Arithmetic mean, weighted totals & finding missing observations",
        isCoreBaseline = false
    ),
    STATISTICS(
        displayName = "Statistics",
        symbol = "σ",
        shortCode = "STT",
        description = "Median, mode, range & central tendency of numerical sets",
        isCoreBaseline = false
    ),
    NUMBER_SYSTEM(
        displayName = "Number System",
        symbol = "ℕ",
        shortCode = "NUM",
        description = "HCF, LCM, remainders, primes, series sums & place value",
        isCoreBaseline = false
    ),
    RATIO_PROPORTION(
        displayName = "Ratio & Work",
        symbol = "a:b",
        shortCode = "RAT",
        description = "Ratios, proportions, profit/loss & inverse time-work rates",
        isCoreBaseline = false
    ),
    EVERYDAY_BASICS(
        displayName = "Everyday Basics",
        symbol = "₹+",
        shortCode = "EVD",
        description = "Practical counting, Indian rupee change, sharing & daily time",
        isCoreBaseline = false
    ),
    CODING_LOGIC(
        displayName = "Coding & Logic",
        symbol = "0x",
        shortCode = "COD",
        description = "Binary, bits & bytes, bitwise XOR/AND, loops & modular math",
        isCoreBaseline = false
    ),
    ENGINEERING_MATH(
        displayName = "Engineering",
        symbol = "∂",
        shortCode = "ENG",
        description = "Calculus slopes, determinants, logs, Ohm's law & kinematics",
        isCoreBaseline = false
    ),
    GENERAL_KNOWLEDGE(
        displayName = "General Knowledge",
        symbol = "GK",
        shortCode = "GK",
        description = "Science, space, India, polity, history, world geography & facts",
        isCoreBaseline = false
    ),
    COMPETITIVE_QUANT(
        displayName = "Competitive Quant",
        symbol = "Σ%",
        shortCode = "CMP",
        description = "Trains, pipes, compound interest, clock angles & exam aptitude",
        isCoreBaseline = false
    ),
    SCHOOL_SCIENCE(
        displayName = "School Science",
        symbol = "⚛",
        shortCode = "SCI",
        description = "Grade-school Physics, Chemistry & Biology reasoning (QASC)",
        isCoreBaseline = false
    ),
    ENG_ECE(
        displayName = "ECE Engineering",
        symbol = "∿",
        shortCode = "ECE",
        description = "Networks, Analog/Digital, Signals, Control, Comm, EM & VLSI",
        isCoreBaseline = false
    ),
    ENG_EEE(
        displayName = "EEE Engineering",
        symbol = "⚡",
        shortCode = "EEE",
        description = "Electrical machines, Power systems, Power electronics & Circuits",
        isCoreBaseline = false
    ),
    ENG_CSE(
        displayName = "CSE Engineering",
        symbol = "</>",
        shortCode = "CSE",
        description = "DSA, Algorithms, OS paging, DBMS, Computer Networks & Architecture",
        isCoreBaseline = false
    ),
    ENG_MECHANICAL(
        displayName = "Mechanical Eng",
        symbol = "⚙",
        shortCode = "MEC",
        description = "Mechanics, Thermodynamics, Fluid mechanics, Heat transfer & Design",
        isCoreBaseline = false
    ),
    ENG_CIVIL(
        displayName = "Civil Engineering",
        symbol = "🌉",
        shortCode = "CIV",
        description = "Structural beams, Surveying, Geotechnical, Transportation & Hydraulics",
        isCoreBaseline = false
    );

    val usesTappableMcq: Boolean
        get() = this == SCHOOL_SCIENCE || this == GENERAL_KNOWLEDGE

    companion object {
        private val specializedTrackCategories = setOf(
            EVERYDAY_BASICS,
            CODING_LOGIC,
            ENGINEERING_MATH,
            GENERAL_KNOWLEDGE,
            COMPETITIVE_QUANT,
            SCHOOL_SCIENCE,
            ENG_ECE,
            ENG_EEE,
            ENG_CSE,
            ENG_MECHANICAL,
            ENG_CIVIL
        )

        val defaultBalancedSet: Set<MathCategory> by lazy {
            entries.filterNot { it in specializedTrackCategories }.toSet()
        }
    }
}

enum class Difficulty(
    val level: Int,
    val displayName: String,
    val baseXp: Int
) {
    BEGINNER(level = 1, displayName = "Beginner", baseXp = 10),
    BASIC(level = 2, displayName = "Basic", baseXp = 15),
    INTERMEDIATE(level = 3, displayName = "Intermediate", baseXp = 22),
    ADVANCED(level = 4, displayName = "Advanced", baseXp = 32),
    EXPERT(level = 5, displayName = "Expert", baseXp = 45);

    fun stepUp(maxBound: Difficulty = EXPERT): Difficulty {
        val nextLevel = (level + 1).coerceAtMost(maxBound.level)
        return entries.first { it.level == nextLevel }
    }

    fun stepDown(minBound: Difficulty = BEGINNER): Difficulty {
        val prevLevel = (level - 1).coerceAtLeast(minBound.level)
        return entries.first { it.level == prevLevel }
    }
}

enum class AnswerFormat {
    INTEGER,
    DECIMAL,
    FRACTION,
    MULTIPLE_CHOICE
}

enum class InputMode {
    KEYPAD,
    MULTIPLE_CHOICE
}

enum class ChallengeMode {
    CONTINUOUS,
    FIXED_SESSION
}

enum class AdaptivePace(
    val displayName: String,
    val streakToAdvance: Int,
    val quickStreakToAdvance: Int,
    val minAccuracyToAdvance: Float,
    val quickAccuracyToAdvance: Float,
    val speedToleranceMultiplier: Float,
    val missesToStepDown: Int,
    val tierOffset: Float
) {
    GENTLE(
        displayName = "Gentle",
        streakToAdvance = 6,
        quickStreakToAdvance = 4,
        minAccuracyToAdvance = 0.85f,
        quickAccuracyToAdvance = 0.90f,
        speedToleranceMultiplier = 1.70f,
        missesToStepDown = 2,
        tierOffset = 6f
    ),
    BALANCED(
        displayName = "Balanced",
        streakToAdvance = 5,
        quickStreakToAdvance = 3,
        minAccuracyToAdvance = 0.80f,
        quickAccuracyToAdvance = 0.85f,
        speedToleranceMultiplier = 1.45f,
        missesToStepDown = 3,
        tierOffset = 0f
    ),
    RAPID(
        displayName = "Rapid",
        streakToAdvance = 3,
        quickStreakToAdvance = 2,
        minAccuracyToAdvance = 0.75f,
        quickAccuracyToAdvance = 0.80f,
        speedToleranceMultiplier = 1.25f,
        missesToStepDown = 2,
        tierOffset = -6f
    )
}

enum class QuestionStyle(val displayName: String) {
    DIRECT("Direct"),
    MISSING_OPERAND("Missing Value"),
    MULTI_STEP("Multi-Step"),
    APPLIED_CHALLENGE("Analytic")
}

enum class AdaptiveStage(val displayName: String) {
    WARM_UP("Warm-Up"),
    FOUNDATION("Foundation"),
    BUILDING("Building"),
    ADVANCED("Advanced"),
    MASTERY("Mastery")
}

enum class TopicMixMode(val displayName: String) {
    PROGRESSIVE_UNLOCK("Progressive"),
    FOCUS_WEAK("Focus Weak"),
    BALANCED("Balanced")
}

data class AdaptiveQuestionPlan(
    val stage: AdaptiveStage,
    val difficulty: Difficulty,
    val complexityTier: Int,
    val questionStyle: QuestionStyle,
    val rationale: String
)

data class Fraction(
    val numerator: BigInteger,
    val denominator: BigInteger
) {
    init {
        require(denominator != BigInteger.ZERO) { "Fraction denominator cannot be zero." }
    }

    constructor(numerator: Long, denominator: Long) : this(
        BigInteger.valueOf(numerator),
        BigInteger.valueOf(denominator)
    )

    fun normalized(): Fraction {
        if (numerator == BigInteger.ZERO) {
            return Fraction(BigInteger.ZERO, BigInteger.ONE)
        }
        val signAdjustedNum = if (denominator < BigInteger.ZERO) numerator.negate() else numerator
        val signAdjustedDen = denominator.abs()
        val gcd = signAdjustedNum.abs().gcd(signAdjustedDen)
        return Fraction(signAdjustedNum / gcd, signAdjustedDen / gcd)
    }

    operator fun plus(other: Fraction): Fraction =
        Fraction(
            numerator * other.denominator + other.numerator * denominator,
            denominator * other.denominator
        ).normalized()

    operator fun minus(other: Fraction): Fraction =
        Fraction(
            numerator * other.denominator - other.numerator * denominator,
            denominator * other.denominator
        ).normalized()

    operator fun times(other: Fraction): Fraction =
        Fraction(
            numerator * other.numerator,
            denominator * other.denominator
        ).normalized()

    operator fun div(other: Fraction): Fraction {
        require(other.numerator != BigInteger.ZERO) { "Cannot divide by zero fraction." }
        return Fraction(
            numerator * other.denominator,
            denominator * other.numerator
        ).normalized()
    }

    fun toCanonicalString(): String {
        val norm = normalized()
        return if (norm.denominator == BigInteger.ONE) {
            norm.numerator.toString()
        } else {
            "${norm.numerator}/${norm.denominator}"
        }
    }

    fun toRawString(): String = "$numerator/$denominator"

    companion object {
        private const val MAX_INPUT_LENGTH = 32
        private const val MAX_DECIMAL_SCALE = 18

        fun parse(rawInput: String): Fraction? {
            val cleaned = rawInput.trim().replace(" ", "")
            if (cleaned.isEmpty() || cleaned.length > MAX_INPUT_LENGTH) return null
            if (cleaned.any { it == 'e' || it == 'E' }) return null

            if (cleaned.contains("/")) {
                val parts = cleaned.split("/")
                if (parts.size != 2) return null
                val num = parts[0].toBigIntegerOrNull() ?: return null
                val den = parts[1].toBigIntegerOrNull() ?: return null
                if (den == BigInteger.ZERO) return null
                return Fraction(num, den).normalized()
            }

            // Support whole numbers or finite decimal inputs (e.g. "0.5" -> "1/2")
            val decimal = cleaned.toBigDecimalOrNull() ?: return null
            if (decimal.compareTo(java.math.BigDecimal.ZERO) == 0) {
                return Fraction(BigInteger.ZERO, BigInteger.ONE)
            }
            val stripped = decimal.stripTrailingZeros()
            val scale = stripped.scale()
            if (scale < -MAX_DECIMAL_SCALE || scale > MAX_DECIMAL_SCALE) return null
            return if (scale <= 0) {
                Fraction(stripped.toBigIntegerExact(), BigInteger.ONE)
            } else {
                val den = BigInteger.TEN.pow(scale)
                val num = stripped.movePointRight(scale).toBigIntegerExact()
                Fraction(num, den).normalized()
            }
        }
    }
}

sealed interface QuestionMetadata {
    data class BinaryIntMeta(
        val left: Long,
        val right: Long,
        val operator: String,
        val result: Long
    ) : QuestionMetadata

    data class DivisionMeta(
        val dividend: Long,
        val divisor: Long,
        val quotient: Long
    ) : QuestionMetadata

    data class PercentageMeta(
        val percent: BigDecimal,
        val baseValue: BigDecimal,
        val result: BigDecimal
    ) : QuestionMetadata

    data class FractionMeta(
        val left: Fraction,
        val right: Fraction?,
        val operator: String,
        val result: Fraction,
        val isSimplification: Boolean
    ) : QuestionMetadata

    data class DecimalMeta(
        val left: BigDecimal,
        val right: BigDecimal,
        val operator: String,
        val result: BigDecimal
    ) : QuestionMetadata

    data class PowerRootMeta(
        val base: Long,
        val exponent: Int,
        val isRoot: Boolean,
        val result: Long
    ) : QuestionMetadata

    data class MixedOpMeta(
        val a: Long,
        val b: Long,
        val c: Long,
        val op1: String,
        val op2: String,
        val groupFirst: Boolean,
        val result: Long
    ) : QuestionMetadata

    data class MissingOperandMeta(
        val equationDescription: String,
        val missingValue: String
    ) : QuestionMetadata

    data class MultiStepMeta(
        val expression: String,
        val result: String
    ) : QuestionMetadata
}

data class MathQuestion(
    val id: String,
    val category: MathCategory,
    val difficulty: Difficulty,
    val answerFormat: AnswerFormat,
    val prompt: String,
    val answer: String,
    val explanationSteps: List<String>,
    val explanation: String,
    val options: List<String>,
    val expectedTimeSeconds: Int,
    val fingerprint: String,
    val generatorVersion: Int,
    val metadata: QuestionMetadata,
    val complexityTier: Int = 1,
    val questionStyle: QuestionStyle = QuestionStyle.DIRECT
) {
    val usesTappableMcq: Boolean
        get() = answerFormat == AnswerFormat.MULTIPLE_CHOICE && category.usesTappableMcq
}

data class QuestionAttempt(
    val id: String,
    val questionId: String,
    val category: MathCategory,
    val difficulty: Difficulty,
    val prompt: String,
    val expectedAnswer: String,
    val submittedAnswer: String,
    val isCorrect: Boolean,
    val responseTimeMs: Long,
    val attemptedAtEpochMs: Long
)

enum class SkillTier(val label: String) {
    WEAK("Weak"),
    DEVELOPING("Developing"),
    STRONG("Strong")
}

data class SkillProfile(
    val category: MathCategory,
    val skillScore: Float,
    val accuracy: Float,
    val averageResponseTimeMs: Long,
    val currentDifficulty: Difficulty,
    val correctStreak: Int,
    val wrongStreak: Int,
    val attemptsCount: Int,
    val lastPracticedAtEpochMs: Long
) {
    val tier: SkillTier
        get() = when {
            attemptsCount < 3 || skillScore < 50f -> SkillTier.WEAK
            skillScore < 80f -> SkillTier.DEVELOPING
            else -> SkillTier.STRONG
        }

    companion object {
        fun initial(category: MathCategory): SkillProfile = SkillProfile(
            category = category,
            skillScore = 40f,
            accuracy = 0f,
            averageResponseTimeMs = 0L,
            currentDifficulty = Difficulty.BEGINNER,
            correctStreak = 0,
            wrongStreak = 0,
            attemptsCount = 0,
            lastPracticedAtEpochMs = 0L
        )
    }
}

data class ReviewItem(
    val id: String,
    val category: MathCategory,
    val difficulty: Difficulty,
    val conceptKey: String,
    val promptSnapshot: String,
    val expectedAnswer: String,
    val explanationSnapshot: String,
    val reviewAtEpochMs: Long,
    val priority: Int,
    val failureCount: Int
)

data class DailyProgress(
    val dateKey: String,
    val attemptedCount: Int,
    val correctCount: Int,
    val totalXp: Int,
    val practiceDurationMs: Long
)

data class BookmarkedQuestion(
    val id: String,
    val category: MathCategory,
    val difficulty: Difficulty,
    val prompt: String,
    val answer: String,
    val explanation: String,
    val savedAtEpochMs: Long
)

data class UserPreferences(
    val defaultInputMode: InputMode = InputMode.KEYPAD,
    val defaultChallengeMode: ChallengeMode = ChallengeMode.CONTINUOUS,
    val sessionLength: Int = 10,
    val allowNegativeSubtraction: Boolean = false,
    val manualDifficultyOverride: Difficulty? = null,
    val minDifficulty: Difficulty = Difficulty.BEGINNER,
    val maxDifficulty: Difficulty = Difficulty.EXPERT,
    val enabledCategories: Set<MathCategory> = MathCategory.defaultBalancedSet,
    val weakShare: Float = 0.50f,
    val developingShare: Float = 0.30f,
    val strongShare: Float = 0.20f,
    val quizLockEnabled: Boolean = false,
    val quizLockedPackages: Set<String> = emptySet(),
    val hasAcceptedPermissions: Boolean = false,
    val hasCompletedSurvey: Boolean = false,
    val learnerTrackId: String = "DEFAULT",
    val learnerTrackLabel: String = "By Default (Adaptive)",
    val surveyFocusTag: String = "",
    val startSimpleEnabled: Boolean = true,
    val warmUpQuestionCount: Int = 5,
    val adaptivePace: AdaptivePace = AdaptivePace.BALANCED,
    val progressiveStyleEnabled: Boolean = true,
    val speedWeightEnabled: Boolean = true,
    val topicMixMode: TopicMixMode = TopicMixMode.PROGRESSIVE_UNLOCK,
    val developerModeEnabled: Boolean = false
)

enum class DifficultyChange {
    INCREASED,
    DECREASED,
    UNCHANGED
}
