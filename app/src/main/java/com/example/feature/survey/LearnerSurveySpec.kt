package com.example.feature.survey

import com.example.domain.model.AdaptivePace
import com.example.domain.model.Difficulty
import com.example.domain.model.MathCategory
import com.example.domain.model.TopicMixMode
import com.example.domain.model.UserPreferences

enum class SurveyRootTrack(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String
) {
    DEFAULT(
        id = "DEFAULT",
        title = "By Default (Recommended)",
        subtitle = "Balanced adaptive math starting easy and growing with you",
        badge = "AUTO"
    ),
    EVERYDAY(
        id = "EVERYDAY",
        title = "Everyday / Basic Numeracy",
        subtitle = "No prior schooling needed · counting, rupee change & sharing",
        badge = "BASIC"
    ),
    SCHOOL(
        id = "SCHOOL",
        title = "School Student (Class 1st – 12th)",
        subtitle = "Questions tailored to your exact class standard",
        badge = "1–12"
    ),
    ENGINEERING(
        id = "ENGINEERING",
        title = "Engineering",
        subtitle = "Calculus slopes, determinants, logs, Ohm's law & numericals",
        badge = "ENG"
    ),
    CODING(
        id = "CODING",
        title = "Coding & CS Logic",
        subtitle = "Binary, bits & bytes, XOR/AND, loops & modular math",
        badge = "CODE"
    ),
    GENERAL_KNOWLEDGE(
        id = "GENERAL_KNOWLEDGE",
        title = "General Knowledge",
        subtitle = "Science, space, India, constitution, history & world facts",
        badge = "GK"
    ),
    COMPETITIVE(
        id = "COMPETITIVE",
        title = "Competitive & Govt Exams",
        subtitle = "SSC, Banking, Railways, UPSC, State PSC, CAT & GATE",
        badge = "EXAM"
    )
}

data class SurveySubTrackOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val categories: Set<MathCategory>,
    val focusTag: String = "",
    val baseMinDifficulty: Difficulty = Difficulty.BEGINNER,
    val baseMaxDifficulty: Difficulty = Difficulty.EXPERT
)

enum class SurveyPaceLevel(
    val id: String,
    val title: String,
    val subtitle: String
) {
    STARTER(
        id = "STARTER",
        title = "Gentle Starter",
        subtitle = "Start at easy questions and build confidence step-by-step"
    ),
    BALANCED(
        id = "BALANCED",
        title = "Balanced Growth",
        subtitle = "Adapts steadily based on your speed and accuracy"
    ),
    CHALLENGE(
        id = "CHALLENGE",
        title = "Exam / High Challenge",
        subtitle = "Jump straight into faster, multi-step & harder problems"
    )
}

object LearnerSurveyEngine {

    fun subTracksFor(root: SurveyRootTrack): List<SurveySubTrackOption> = when (root) {
        SurveyRootTrack.DEFAULT -> emptyList()
        SurveyRootTrack.EVERYDAY -> listOf(
            SurveySubTrackOption(
                id = "EVERYDAY_COUNTING",
                title = "Daily Counting & Numbers",
                subtitle = "Simple counting items, adding & taking away",
                categories = setOf(MathCategory.EVERYDAY_BASICS),
                focusTag = "EVERYDAY_COUNTING",
                baseMinDifficulty = Difficulty.BEGINNER,
                baseMaxDifficulty = Difficulty.BASIC
            ),
            SurveySubTrackOption(
                id = "EVERYDAY_MONEY",
                title = "Money & Shopping Change",
                subtitle = "₹10, ₹20, ₹50, ₹100 notes & everyday bills",
                categories = setOf(MathCategory.EVERYDAY_BASICS),
                focusTag = "EVERYDAY_MONEY",
                baseMinDifficulty = Difficulty.BEGINNER,
                baseMaxDifficulty = Difficulty.INTERMEDIATE
            ),
            SurveySubTrackOption(
                id = "EVERYDAY_SHARING",
                title = "Simple Sharing & + / −",
                subtitle = "Equal sharing, daily addition & subtraction",
                categories = setOf(MathCategory.EVERYDAY_BASICS, MathCategory.ADDITION, MathCategory.SUBTRACTION),
                focusTag = "EVERYDAY_SHARING",
                baseMinDifficulty = Difficulty.BEGINNER,
                baseMaxDifficulty = Difficulty.BASIC
            )
        )
        SurveyRootTrack.SCHOOL -> listOf(
            SurveySubTrackOption(
                id = "SCHOOL_1_2",
                title = "Class 1st – 2nd Standard",
                subtitle = "Everyday counting, simple + and −",
                categories = setOf(MathCategory.EVERYDAY_BASICS, MathCategory.ADDITION, MathCategory.SUBTRACTION),
                baseMinDifficulty = Difficulty.BEGINNER,
                baseMaxDifficulty = Difficulty.BASIC
            ),
            SurveySubTrackOption(
                id = "SCHOOL_3_5",
                title = "Class 3rd – 5th Standard",
                subtitle = "Multiplication, division, simple fractions & squares",
                categories = setOf(
                    MathCategory.ADDITION,
                    MathCategory.SUBTRACTION,
                    MathCategory.MULTIPLICATION,
                    MathCategory.DIVISION,
                    MathCategory.FRACTION,
                    MathCategory.SQUARES
                ),
                baseMinDifficulty = Difficulty.BEGINNER,
                baseMaxDifficulty = Difficulty.INTERMEDIATE
            ),
            SurveySubTrackOption(
                id = "SCHOOL_6_8",
                title = "Class 6th – 8th Standard",
                subtitle = "BODMAS, decimals, percentage, ratio, roots & averages",
                categories = setOf(
                    MathCategory.BODMAS,
                    MathCategory.FRACTION,
                    MathCategory.DECIMAL,
                    MathCategory.PERCENTAGE,
                    MathCategory.RATIO_PROPORTION,
                    MathCategory.SQUARES,
                    MathCategory.CUBES,
                    MathCategory.ROOTS,
                    MathCategory.AVERAGE,
                    MathCategory.NUMBER_SYSTEM
                ),
                baseMinDifficulty = Difficulty.BASIC,
                baseMaxDifficulty = Difficulty.ADVANCED
            ),
            SurveySubTrackOption(
                id = "SCHOOL_9_10",
                title = "Class 9th – 10th Standard",
                subtitle = "Board quant: powers, SI, speed-distance, stats & number system",
                categories = setOf(
                    MathCategory.BODMAS,
                    MathCategory.POWERS_ROOTS,
                    MathCategory.RATIO_PROPORTION,
                    MathCategory.SIMPLE_INTEREST,
                    MathCategory.SPEED_DISTANCE,
                    MathCategory.STATISTICS,
                    MathCategory.NUMBER_SYSTEM,
                    MathCategory.MIXED_OPERATIONS
                ),
                baseMinDifficulty = Difficulty.INTERMEDIATE,
                baseMaxDifficulty = Difficulty.EXPERT
            ),
            SurveySubTrackOption(
                id = "SCHOOL_11_12",
                title = "Class 11th – 12th Standard",
                subtitle = "Calculus slopes, determinants, logs, stats & higher algebra",
                categories = setOf(
                    MathCategory.ENGINEERING_MATH,
                    MathCategory.POWERS_ROOTS,
                    MathCategory.STATISTICS,
                    MathCategory.NUMBER_SYSTEM
                ),
                baseMinDifficulty = Difficulty.INTERMEDIATE,
                baseMaxDifficulty = Difficulty.EXPERT
            ),
            SurveySubTrackOption(
                id = "SCHOOL_SCIENCE",
                title = "School Science (Physics, Chemistry & Biology)",
                subtitle = "Grade-school scientific concepts & QASC multiple-choice reasoning",
                categories = setOf(MathCategory.SCHOOL_SCIENCE),
                baseMinDifficulty = Difficulty.BEGINNER,
                baseMaxDifficulty = Difficulty.EXPERT
            )
        )
        SurveyRootTrack.ENGINEERING -> listOf(
            SurveySubTrackOption(
                id = "ENG_ECE",
                title = "ECE — Electronics & Communication",
                subtitle = "Networks, Op-Amps, ADC/VLSI, Nyquist, Control & Antennas",
                categories = setOf(MathCategory.ENG_ECE),
                baseMinDifficulty = Difficulty.BASIC,
                baseMaxDifficulty = Difficulty.EXPERT
            ),
            SurveySubTrackOption(
                id = "ENG_EEE",
                title = "EEE — Electrical & Electronics",
                subtitle = "AC Machines, Power Systems, Buck Converters & Transformers",
                categories = setOf(MathCategory.ENG_EEE),
                baseMinDifficulty = Difficulty.BASIC,
                baseMaxDifficulty = Difficulty.EXPERT
            ),
            SurveySubTrackOption(
                id = "ENG_CSE",
                title = "CSE — Computer Science & Engineering",
                subtitle = "Binary Trees, CIDR Subnets, OS Paging, DBMS & Cache AMAT",
                categories = setOf(MathCategory.ENG_CSE),
                baseMinDifficulty = Difficulty.BASIC,
                baseMaxDifficulty = Difficulty.EXPERT
            ),
            SurveySubTrackOption(
                id = "ENG_MECHANICAL",
                title = "Mechanical Engineering",
                subtitle = "Carnot Thermodynamics, Continuity, Gears, Heat & Stress",
                categories = setOf(MathCategory.ENG_MECHANICAL),
                baseMinDifficulty = Difficulty.BASIC,
                baseMaxDifficulty = Difficulty.EXPERT
            ),
            SurveySubTrackOption(
                id = "ENG_CIVIL",
                title = "Civil Engineering",
                subtitle = "Beam Bending, Surveying RL, Soil Mechanics & Hydraulics",
                categories = setOf(MathCategory.ENG_CIVIL),
                baseMinDifficulty = Difficulty.BASIC,
                baseMaxDifficulty = Difficulty.EXPERT
            ),
            SurveySubTrackOption(
                id = "ENG_CALCULUS",
                title = "Engineering Math (Calculus & Matrices)",
                subtitle = "Derivatives at a point, 2×2 determinants & logarithms",
                categories = setOf(MathCategory.ENGINEERING_MATH),
                focusTag = "ENG_CALCULUS",
                baseMinDifficulty = Difficulty.INTERMEDIATE,
                baseMaxDifficulty = Difficulty.EXPERT
            )
        )
        SurveyRootTrack.CODING -> listOf(
            SurveySubTrackOption(
                id = "CODING_BINARY",
                title = "Binary, Bits & Bitwise",
                subtitle = "Binary→Decimal, powers of 2, bytes & XOR/AND/OR",
                categories = setOf(MathCategory.CODING_LOGIC),
                focusTag = "CODING_BINARY",
                baseMinDifficulty = Difficulty.BEGINNER,
                baseMaxDifficulty = Difficulty.EXPERT
            ),
            SurveySubTrackOption(
                id = "CODING_ALGO",
                title = "Loops, Modulo & Complexity",
                subtitle = "Loop iteration counts & modular arithmetic",
                categories = setOf(MathCategory.CODING_LOGIC),
                focusTag = "CODING_ALGO",
                baseMinDifficulty = Difficulty.BASIC,
                baseMaxDifficulty = Difficulty.EXPERT
            ),
            SurveySubTrackOption(
                id = "CODING_FULL",
                title = "Full CS & Number Theory",
                subtitle = "All coding logic plus HCF, LCM & remainders",
                categories = setOf(MathCategory.CODING_LOGIC, MathCategory.NUMBER_SYSTEM),
                baseMinDifficulty = Difficulty.BASIC,
                baseMaxDifficulty = Difficulty.EXPERT
            )
        )
        SurveyRootTrack.GENERAL_KNOWLEDGE -> listOf(
            SurveySubTrackOption(
                id = "GK_SCIENCE",
                title = "Science, Space & Human Body",
                subtitle = "Physics, chemistry, biology & astronomy facts",
                categories = setOf(MathCategory.GENERAL_KNOWLEDGE),
                focusTag = "SCIENCE_SPACE"
            ),
            SurveySubTrackOption(
                id = "GK_INDIA",
                title = "India, Constitution & History",
                subtitle = "Articles, states, national symbols & milestones",
                categories = setOf(MathCategory.GENERAL_KNOWLEDGE),
                focusTag = "INDIA_POLITY"
            ),
            SurveySubTrackOption(
                id = "GK_MIXED",
                title = "World Geography & Mixed GK",
                subtitle = "All general knowledge topics combined",
                categories = setOf(MathCategory.GENERAL_KNOWLEDGE),
                focusTag = ""
            )
        )
        SurveyRootTrack.COMPETITIVE -> listOf(
            SurveySubTrackOption(
                id = "GOVT_SSC_BANK",
                title = "SSC / Railways / Banking",
                subtitle = "Trains, CI, profit-loss %, ratios, speed & quant",
                categories = setOf(
                    MathCategory.COMPETITIVE_QUANT,
                    MathCategory.PERCENTAGE,
                    MathCategory.RATIO_PROPORTION,
                    MathCategory.SIMPLE_INTEREST,
                    MathCategory.SPEED_DISTANCE,
                    MathCategory.AVERAGE,
                    MathCategory.NUMBER_SYSTEM,
                    MathCategory.BODMAS
                ),
                baseMinDifficulty = Difficulty.INTERMEDIATE,
                baseMaxDifficulty = Difficulty.EXPERT
            ),
            SurveySubTrackOption(
                id = "GOVT_UPSC_GS",
                title = "UPSC / State PSC (GS + CSAT)",
                subtitle = "Polity, science, world GK & quantitative reasoning",
                categories = setOf(
                    MathCategory.GENERAL_KNOWLEDGE,
                    MathCategory.COMPETITIVE_QUANT,
                    MathCategory.STATISTICS,
                    MathCategory.RATIO_PROPORTION
                ),
                baseMinDifficulty = Difficulty.INTERMEDIATE,
                baseMaxDifficulty = Difficulty.EXPERT
            ),
            SurveySubTrackOption(
                id = "COMPETITIVE_GATE_CAT",
                title = "GATE / CAT Aptitude",
                subtitle = "Advanced quant, engineering maths, coding & statistics",
                categories = setOf(
                    MathCategory.COMPETITIVE_QUANT,
                    MathCategory.ENGINEERING_MATH,
                    MathCategory.CODING_LOGIC,
                    MathCategory.STATISTICS,
                    MathCategory.NUMBER_SYSTEM
                ),
                baseMinDifficulty = Difficulty.INTERMEDIATE,
                baseMaxDifficulty = Difficulty.EXPERT
            )
        )
    }

    fun applyDefault(current: UserPreferences): UserPreferences = current.copy(
        hasCompletedSurvey = true,
        learnerTrackId = SurveyRootTrack.DEFAULT.id,
        learnerTrackLabel = "By Default (Adaptive)",
        surveyFocusTag = "",
        enabledCategories = MathCategory.defaultBalancedSet,
        minDifficulty = Difficulty.BEGINNER,
        maxDifficulty = Difficulty.EXPERT,
        manualDifficultyOverride = null,
        startSimpleEnabled = true,
        adaptivePace = AdaptivePace.BALANCED,
        progressiveStyleEnabled = true,
        topicMixMode = TopicMixMode.PROGRESSIVE_UNLOCK
    )

    fun applySelection(
        current: UserPreferences,
        root: SurveyRootTrack,
        subTrack: SurveySubTrackOption?,
        pace: SurveyPaceLevel
    ): UserPreferences {
        if (root == SurveyRootTrack.DEFAULT || subTrack == null) {
            return applyDefault(current)
        }
        val (minD, maxD, adaptPace, startSimple) = when (pace) {
            SurveyPaceLevel.STARTER -> QuadValues(
                min = Difficulty.BEGINNER,
                max = if (subTrack.baseMaxDifficulty.level <= Difficulty.INTERMEDIATE.level) subTrack.baseMaxDifficulty else Difficulty.ADVANCED,
                pace = AdaptivePace.GENTLE,
                simple = true
            )
            SurveyPaceLevel.BALANCED -> QuadValues(
                min = subTrack.baseMinDifficulty,
                max = subTrack.baseMaxDifficulty,
                pace = AdaptivePace.BALANCED,
                simple = subTrack.baseMinDifficulty == Difficulty.BEGINNER
            )
            SurveyPaceLevel.CHALLENGE -> QuadValues(
                min = if (subTrack.baseMaxDifficulty == Difficulty.BASIC) Difficulty.BASIC else Difficulty.INTERMEDIATE,
                max = Difficulty.EXPERT,
                pace = AdaptivePace.RAPID,
                simple = false
            )
        }
        val coercedMax = if (maxD.level < minD.level) minD else maxD
        return current.copy(
            hasCompletedSurvey = true,
            learnerTrackId = subTrack.id,
            learnerTrackLabel = subTrack.title,
            surveyFocusTag = subTrack.focusTag,
            enabledCategories = subTrack.categories,
            minDifficulty = minD,
            maxDifficulty = coercedMax,
            manualDifficultyOverride = null,
            startSimpleEnabled = startSimple,
            adaptivePace = adaptPace,
            progressiveStyleEnabled = true,
            topicMixMode = TopicMixMode.BALANCED
        )
    }

    private data class QuadValues(
        val min: Difficulty,
        val max: Difficulty,
        val pace: AdaptivePace,
        val simple: Boolean
    )
}
