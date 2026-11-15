package com.example.domain.generator

import com.example.domain.model.Difficulty
import com.example.domain.model.MathCategory
import com.example.domain.model.MathQuestion
import com.example.domain.model.QuestionStyle
import kotlin.random.Random

data class GeneratorConfig(
    val allowNegativeSubtraction: Boolean = false,
    val complexityTier: Int = 1,
    val questionStyle: QuestionStyle = QuestionStyle.DIRECT,
    val focusTag: String = ""
)

interface QuestionGenerator {
    val category: MathCategory
    val version: Int

    fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig = GeneratorConfig(),
        random: Random = Random.Default
    ): MathQuestion
}

class GeneratorRegistry(
    private val generators: Map<MathCategory, QuestionGenerator>
) {
    fun generatorFor(category: MathCategory): QuestionGenerator =
        requireNotNull(generators[category]) {
            "No generator registered for $category"
        }

    fun supportedCategories(): Set<MathCategory> = generators.keys

    companion object {
        fun default(): GeneratorRegistry = GeneratorRegistry(
            mapOf(
                MathCategory.ADDITION to AdditionGenerator(),
                MathCategory.SUBTRACTION to SubtractionGenerator(),
                MathCategory.MULTIPLICATION to MultiplicationGenerator(),
                MathCategory.DIVISION to DivisionGenerator(),
                MathCategory.PERCENTAGE to PercentageGenerator(),
                MathCategory.FRACTION to FractionGenerator(),
                MathCategory.DECIMAL to DecimalGenerator(),
                MathCategory.POWERS_ROOTS to PowerRootGenerator(),
                MathCategory.MIXED_OPERATIONS to MixedOperationsGenerator(),
                MathCategory.SQUARES to SquaresGenerator(),
                MathCategory.CUBES to CubesGenerator(),
                MathCategory.ROOTS to RootsGenerator(),
                MathCategory.BODMAS to BodmasGenerator(),
                MathCategory.SIMPLE_INTEREST to SimpleInterestGenerator(),
                MathCategory.SPEED_DISTANCE to SpeedDistanceGenerator(),
                MathCategory.AVERAGE to AverageGenerator(),
                MathCategory.STATISTICS to StatisticsGenerator(),
                MathCategory.NUMBER_SYSTEM to NumberSystemGenerator(),
                MathCategory.RATIO_PROPORTION to RatioProportionGenerator(),
                MathCategory.EVERYDAY_BASICS to EverydayBasicsGenerator(),
                MathCategory.CODING_LOGIC to CodingLogicGenerator(),
                MathCategory.ENGINEERING_MATH to EngineeringMathGenerator(),
                MathCategory.GENERAL_KNOWLEDGE to GeneralKnowledgeGenerator(),
                MathCategory.COMPETITIVE_QUANT to CompetitiveQuantGenerator(),
                MathCategory.SCHOOL_SCIENCE to SchoolScienceGenerator(),
                MathCategory.ENG_ECE to EceEngineeringGenerator(),
                MathCategory.ENG_EEE to EeeEngineeringGenerator(),
                MathCategory.ENG_CSE to CseEngineeringGenerator(),
                MathCategory.ENG_MECHANICAL to MechanicalEngineeringGenerator(),
                MathCategory.ENG_CIVIL to CivilEngineeringGenerator()
            )
        )
    }
}
