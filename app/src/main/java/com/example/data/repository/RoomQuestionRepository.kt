package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.database.GovindaDatabase
import com.example.data.local.entity.DatasetQuestionEntity
import com.example.data.local.entity.QuestionAttemptEntity
import com.example.data.local.entity.QuestionOptionEntity
import com.example.data.local.entity.QuestionPackEntity
import com.example.data.local.entity.QuestionPackVersionEntity
import com.example.data.local.entity.QuestionSourceEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TopicEntity
import com.example.domain.model.QuestionAttempt
import com.example.domain.repository.DatasetOption
import com.example.domain.repository.Question
import com.example.domain.repository.QuestionRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RoomQuestionRepository(
    private val database: GovindaDatabase
) : QuestionRepository {

    private val dao = database.questionBankDao()
    private val attemptDao = database.questionAttemptDao()
    private val seedMutex = Mutex()
    @Volatile
    private var seeded = false

    private suspend fun ensureSeeded() {
        if (seeded) return
        seedMutex.withLock {
            if (seeded) return
            if (dao.countQuestions() == 0) {
                seedPermissiveStarterPack()
            }
            seeded = true
        }
    }

    override suspend fun getQuestions(
        subjectId: String,
        topicId: String?,
        difficulty: Int?,
        limit: Int
    ): List<Question> {
        ensureSeeded()
        val entities = dao.queryQuestions(
            subjectId = subjectId,
            topicId = topicId,
            difficulty = difficulty,
            limit = limit.coerceIn(1, 200)
        )
        return entities.map { hydrateQuestion(it) }
    }

    override suspend fun getRandomQuestion(
        subjectId: String,
        topicId: String?,
        difficulty: Int?
    ): Question? {
        ensureSeeded()
        val entity = dao.queryRandomQuestion(
            subjectId = subjectId,
            topicId = topicId,
            difficulty = difficulty
        ) ?: return null
        return hydrateQuestion(entity)
    }

    override suspend fun saveAttempt(attempt: QuestionAttempt) {
        ensureSeeded()
        attemptDao.insert(
            QuestionAttemptEntity(
                id = attempt.id,
                questionId = attempt.questionId,
                category = attempt.category,
                difficulty = attempt.difficulty,
                prompt = attempt.prompt,
                expectedAnswer = attempt.expectedAnswer,
                submittedAnswer = attempt.submittedAnswer,
                isCorrect = attempt.isCorrect,
                responseTimeMs = attempt.responseTimeMs,
                attemptedAt = attempt.attemptedAtEpochMs
            )
        )
    }

    override suspend fun getQuestionById(questionId: String): Question? {
        ensureSeeded()
        val entity = dao.getQuestionById(questionId) ?: return null
        return hydrateQuestion(entity)
    }

    private suspend fun hydrateQuestion(entity: DatasetQuestionEntity): Question {
        val options = dao.getOptionsForQuestion(entity.id).map {
            DatasetOption(id = it.optionId, text = it.optionText)
        }
        val source = dao.getSourceForQuestion(entity.id)
        return Question(
            id = entity.id,
            subjectId = entity.subjectId,
            topicId = entity.topicId,
            subtopic = entity.subtopic,
            educationLevel = entity.educationLevel,
            classLevel = entity.classLevel,
            examId = entity.examId,
            difficulty = entity.difficulty,
            questionType = entity.questionType,
            language = entity.language,
            questionText = entity.questionText,
            options = options,
            correctAnswerType = entity.correctAnswerType,
            correctAnswerValue = entity.correctAnswerValue,
            explanation = entity.explanation,
            sourceDataset = entity.sourceDataset,
            license = source?.license ?: "MIT/Apache-2.0/CC-BY-4.0",
            fingerprint = entity.fingerprint
        )
    }

    private suspend fun seedPermissiveStarterPack() {
        val now = System.currentTimeMillis()
        val packId = "PACK_CORE_PERMISSIVE_V1"
        database.withTransaction {
            dao.insertPacks(
                listOf(
                    QuestionPackEntity(
                        id = packId,
                        name = "MATHGATE Permissive Starter Pack",
                        license = "MIT / Apache-2.0 / CC-BY-4.0",
                        isInstalled = true,
                        createdAt = now
                    )
                )
            )
            dao.insertPackVersions(
                listOf(
                    QuestionPackVersionEntity(
                        id = "${packId}_V1",
                        packId = packId,
                        versionCode = 1,
                        recordCount = 6,
                        sha256 = "verified-pack-v1",
                        releasedAt = now
                    )
                )
            )
            dao.insertSubjects(
                listOf(
                    SubjectEntity("PHYSICS", "Physics", "SCIENCE", "Mechanics, Electricity & Waves"),
                    SubjectEntity("BIOLOGY", "Biology", "SCIENCE", "Cell & Human Biology"),
                    SubjectEntity("CHEMISTRY", "Chemistry", "SCIENCE", "Matter, Acids & Atomic Structure"),
                    SubjectEntity("MATHEMATICS", "Mathematics", "MATHEMATICS", "Arithmetic, Algebra, Geometry & Calculus"),
                    SubjectEntity("GENERAL_KNOWLEDGE", "General Knowledge", "GENERAL", "Science, Geography & Polity")
                )
            )
            dao.insertTopics(
                listOf(
                    TopicEntity("BIOLOGY_CELL_AND_HUMAN_BIOLOGY", "BIOLOGY", "Cell And Human Biology", "PHYSIOLOGY"),
                    TopicEntity("PHYSICS_MECHANICS_AND_ELECTRICITY", "PHYSICS", "Mechanics And Electricity", "FUNDAMENTAL_LAWS"),
                    TopicEntity("CHEMISTRY_MATTER_AND_REACTIONS", "CHEMISTRY", "Matter And Reactions", "ATOMIC_AND_ACIDS"),
                    TopicEntity("MATHEMATICS_ARITHMETIC", "MATHEMATICS", "Arithmetic", "WORD_PROBLEMS"),
                    TopicEntity("MATHEMATICS_QUANTITATIVE_APTITUDE", "MATHEMATICS", "Quantitative Aptitude", "SIMPLE_INTEREST"),
                    TopicEntity("GENERAL_KNOWLEDGE_GEOGRAPHY", "GENERAL_KNOWLEDGE", "Geography", "FACTUAL_TRIVIA")
                )
            )

            val seedQuestions = listOf(
                DatasetQuestionEntity(
                    id = "QASC_QASC_TRAIN_0001",
                    packId = packId,
                    subjectId = "BIOLOGY",
                    topicId = "BIOLOGY_CELL_AND_HUMAN_BIOLOGY",
                    subtopic = "PHYSIOLOGY",
                    educationLevel = "SCHOOL_FOUNDATION",
                    classLevel = "CLASS_5_TO_8",
                    examId = null,
                    difficulty = 2,
                    questionType = "MCQ_SINGLE",
                    language = "en",
                    questionText = "What cellular process converts light energy into chemical energy in green plants?",
                    correctAnswerType = "OPTION",
                    correctAnswerValue = "B",
                    explanation = "Green plants convert light energy into chemical energy via photosynthesis.",
                    solutionStepsJson = "[\"Chloroplasts absorb sunlight.\",\"Photosynthesis synthesizes glucose.\"]",
                    supportingFactsJson = "[\"Chloroplasts absorb sunlight.\"]",
                    sourceDataset = "allenai/qasc",
                    fingerprint = "qasc_photosynthesis_0001",
                    verified = true,
                    contentVersion = 1
                ),
                DatasetQuestionEntity(
                    id = "QASC_QASC_VAL_0001",
                    packId = packId,
                    subjectId = "PHYSICS",
                    topicId = "PHYSICS_MECHANICS_AND_ELECTRICITY",
                    subtopic = "FUNDAMENTAL_LAWS",
                    educationLevel = "SCHOOL_FOUNDATION",
                    classLevel = "CLASS_5_TO_8",
                    examId = null,
                    difficulty = 2,
                    questionType = "MCQ_SINGLE",
                    language = "en",
                    questionText = "What is the SI unit of electrical resistance?",
                    correctAnswerType = "OPTION",
                    correctAnswerValue = "C",
                    explanation = "By Ohm's law R = V / I, electrical resistance is measured in Ohms.",
                    solutionStepsJson = "[\"Electrical resistance opposes current flow.\",\"SI unit is Ohm.\"]",
                    supportingFactsJson = "[\"SI unit is Ohm.\"]",
                    sourceDataset = "allenai/qasc",
                    fingerprint = "qasc_resistance_0001",
                    verified = true,
                    contentVersion = 1
                ),
                DatasetQuestionEntity(
                    id = "QASC_QASC_TRAIN_0003",
                    packId = packId,
                    subjectId = "CHEMISTRY",
                    topicId = "CHEMISTRY_MATTER_AND_REACTIONS",
                    subtopic = "ATOMIC_AND_ACIDS",
                    educationLevel = "SCHOOL_FOUNDATION",
                    classLevel = "CLASS_5_TO_8",
                    examId = null,
                    difficulty = 2,
                    questionType = "MCQ_SINGLE",
                    language = "en",
                    questionText = "What gas is released when an acid reacts with an active metal such as zinc?",
                    correctAnswerType = "OPTION",
                    correctAnswerValue = "D",
                    explanation = "When an acid reacts with zinc, hydrogen gas is released (Zn + 2HCl -> ZnCl2 + H2).",
                    solutionStepsJson = "[\"Active metals displace hydrogen ions from dilute acids.\"]",
                    supportingFactsJson = "[\"Zn + 2HCl -> ZnCl2 + H2\"]",
                    sourceDataset = "allenai/qasc",
                    fingerprint = "qasc_hydrogen_0003",
                    verified = true,
                    contentVersion = 1
                ),
                DatasetQuestionEntity(
                    id = "GSM8K_GSM8K_TRAIN_0001",
                    packId = packId,
                    subjectId = "MATHEMATICS",
                    topicId = "MATHEMATICS_ARITHMETIC",
                    subtopic = "WORD_PROBLEMS",
                    educationLevel = "SCHOOL",
                    classLevel = "CLASS_5_TO_8",
                    examId = null,
                    difficulty = 2,
                    questionType = "NUMERICAL",
                    language = "en",
                    questionText = "A notebook costs 45 rupees and a geometry box costs 120 rupees. If Aarav buys 4 notebooks and 1 geometry box, how much does he pay in total?",
                    correctAnswerType = "NUMERICAL",
                    correctAnswerValue = "300",
                    explanation = "4 notebooks cost 4 * 45 = 180 rupees. Adding 120 gives 300 rupees.",
                    solutionStepsJson = "[\"4 * 45 = 180\",\"180 + 120 = 300\"]",
                    supportingFactsJson = "[]",
                    sourceDataset = "openai/gsm8k",
                    fingerprint = "gsm8k_notebooks_0001",
                    verified = true,
                    contentVersion = 1
                ),
                DatasetQuestionEntity(
                    id = "MATHQA_MATHQA_TRAIN_0001",
                    packId = packId,
                    subjectId = "MATHEMATICS",
                    topicId = "MATHEMATICS_QUANTITATIVE_APTITUDE",
                    subtopic = "SIMPLE_INTEREST",
                    educationLevel = "COMPETITIVE_EXAMS",
                    classLevel = null,
                    examId = "SSC",
                    difficulty = 3,
                    questionType = "MCQ_SINGLE",
                    language = "en",
                    questionText = "If a sum of Rs. 2500 is invested at 8% simple interest per annum for 3 years, what is the simple interest earned?",
                    correctAnswerType = "OPTION",
                    correctAnswerValue = "B",
                    explanation = "SI = (2500 * 8 * 3) / 100 = 600.",
                    solutionStepsJson = "[\"SI = P * R * T / 100 = 600\"]",
                    supportingFactsJson = "[]",
                    sourceDataset = "allenai/math_qa",
                    fingerprint = "mathqa_si_0001",
                    verified = true,
                    contentVersion = 1
                ),
                DatasetQuestionEntity(
                    id = "LCT_LCT_0002",
                    packId = packId,
                    subjectId = "GENERAL_KNOWLEDGE",
                    topicId = "GENERAL_KNOWLEDGE_GEOGRAPHY",
                    subtopic = "FACTUAL_TRIVIA",
                    educationLevel = "GENERAL",
                    classLevel = null,
                    examId = "UPSC",
                    difficulty = 2,
                    questionType = "MCQ_SINGLE",
                    language = "en",
                    questionText = "Which river is the longest river flowing entirely within India?",
                    correctAnswerType = "OPTION",
                    correctAnswerValue = "A",
                    explanation = "The Godavari flows for ~1,465 km entirely within India.",
                    solutionStepsJson = "[\"Godavari is the longest peninsular river flowing wholly in India.\"]",
                    supportingFactsJson = "[]",
                    sourceDataset = "https://github.com/Pluxia-GmbH/learnclash-open-trivia",
                    fingerprint = "lct_godavari_0002",
                    verified = true,
                    contentVersion = 1
                )
            )
            dao.insertQuestions(seedQuestions)

            dao.insertOptions(
                listOf(
                    QuestionOptionEntity("QASC_QASC_TRAIN_0001_A", "QASC_QASC_TRAIN_0001", "A", "Respiration", false),
                    QuestionOptionEntity("QASC_QASC_TRAIN_0001_B", "QASC_QASC_TRAIN_0001", "B", "Photosynthesis", true),
                    QuestionOptionEntity("QASC_QASC_TRAIN_0001_C", "QASC_QASC_TRAIN_0001", "C", "Transpiration", false),
                    QuestionOptionEntity("QASC_QASC_TRAIN_0001_D", "QASC_QASC_TRAIN_0001", "D", "Fermentation", false),
                    QuestionOptionEntity("QASC_QASC_VAL_0001_A", "QASC_QASC_VAL_0001", "A", "Volt", false),
                    QuestionOptionEntity("QASC_QASC_VAL_0001_B", "QASC_QASC_VAL_0001", "B", "Ampere", false),
                    QuestionOptionEntity("QASC_QASC_VAL_0001_C", "QASC_QASC_VAL_0001", "C", "Ohm", true),
                    QuestionOptionEntity("QASC_QASC_VAL_0001_D", "QASC_QASC_VAL_0001", "D", "Watt", false),
                    QuestionOptionEntity("QASC_QASC_TRAIN_0003_A", "QASC_QASC_TRAIN_0003", "A", "Oxygen", false),
                    QuestionOptionEntity("QASC_QASC_TRAIN_0003_B", "QASC_QASC_TRAIN_0003", "B", "Nitrogen", false),
                    QuestionOptionEntity("QASC_QASC_TRAIN_0003_C", "QASC_QASC_TRAIN_0003", "C", "Carbon dioxide", false),
                    QuestionOptionEntity("QASC_QASC_TRAIN_0003_D", "QASC_QASC_TRAIN_0003", "D", "Hydrogen", true),
                    QuestionOptionEntity("MATHQA_MATHQA_TRAIN_0001_A", "MATHQA_MATHQA_TRAIN_0001", "A", "500", false),
                    QuestionOptionEntity("MATHQA_MATHQA_TRAIN_0001_B", "MATHQA_MATHQA_TRAIN_0001", "B", "600", true),
                    QuestionOptionEntity("MATHQA_MATHQA_TRAIN_0001_C", "MATHQA_MATHQA_TRAIN_0001", "C", "650", false),
                    QuestionOptionEntity("MATHQA_MATHQA_TRAIN_0001_D", "MATHQA_MATHQA_TRAIN_0001", "D", "720", false),
                    QuestionOptionEntity("LCT_LCT_0002_A", "LCT_LCT_0002", "A", "Godavari", true),
                    QuestionOptionEntity("LCT_LCT_0002_B", "LCT_LCT_0002", "B", "Yamuna", false),
                    QuestionOptionEntity("LCT_LCT_0002_C", "LCT_LCT_0002", "C", "Narmada", false),
                    QuestionOptionEntity("LCT_LCT_0002_D", "LCT_LCT_0002", "D", "Kaveri", false)
                )
            )

            dao.insertSources(
                listOf(
                    QuestionSourceEntity(
                        questionId = "QASC_QASC_TRAIN_0001",
                        sourceDataset = "allenai/qasc",
                        sourceRecordId = "QASC_TRAIN_0001",
                        datasetUrl = "https://huggingface.co/datasets/allenai/qasc",
                        license = "CC-BY-4.0",
                        sourceSplit = "train",
                        sourceRevision = "main@qasc-v1"
                    ),
                    QuestionSourceEntity(
                        questionId = "QASC_QASC_VAL_0001",
                        sourceDataset = "allenai/qasc",
                        sourceRecordId = "QASC_VAL_0001",
                        datasetUrl = "https://huggingface.co/datasets/allenai/qasc",
                        license = "CC-BY-4.0",
                        sourceSplit = "validation",
                        sourceRevision = "main@qasc-v1"
                    ),
                    QuestionSourceEntity(
                        questionId = "QASC_QASC_TRAIN_0003",
                        sourceDataset = "allenai/qasc",
                        sourceRecordId = "QASC_TRAIN_0003",
                        datasetUrl = "https://huggingface.co/datasets/allenai/qasc",
                        license = "CC-BY-4.0",
                        sourceSplit = "train",
                        sourceRevision = "main@qasc-v1"
                    ),
                    QuestionSourceEntity(
                        questionId = "GSM8K_GSM8K_TRAIN_0001",
                        sourceDataset = "openai/gsm8k",
                        sourceRecordId = "GSM8K_TRAIN_0001",
                        datasetUrl = "https://huggingface.co/datasets/openai/gsm8k",
                        license = "MIT",
                        sourceSplit = "train",
                        sourceRevision = "main@gsm8k-v1"
                    ),
                    QuestionSourceEntity(
                        questionId = "MATHQA_MATHQA_TRAIN_0001",
                        sourceDataset = "allenai/math_qa",
                        sourceRecordId = "MATHQA_TRAIN_0001",
                        datasetUrl = "https://huggingface.co/datasets/allenai/math_qa",
                        license = "Apache-2.0",
                        sourceSplit = "train",
                        sourceRevision = "static-export@mathqa-v1"
                    ),
                    QuestionSourceEntity(
                        questionId = "LCT_LCT_0002",
                        sourceDataset = "https://github.com/Pluxia-GmbH/learnclash-open-trivia",
                        sourceRecordId = "LCT_0002",
                        datasetUrl = "https://github.com/Pluxia-GmbH/learnclash-open-trivia",
                        license = "CC-BY-4.0",
                        sourceSplit = "train",
                        sourceRevision = "main@learnclash-v1"
                    )
                )
            )
        }
    }
}
