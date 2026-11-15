package com.example.domain.repository

import com.example.domain.model.QuestionAttempt

data class DatasetOption(
    val id: String,
    val text: String
)

data class Question(
    val id: String,
    val subjectId: String,
    val topicId: String,
    val subtopic: String?,
    val educationLevel: String,
    val classLevel: String?,
    val examId: String?,
    val difficulty: Int,
    val questionType: String,
    val language: String,
    val questionText: String,
    val options: List<DatasetOption>,
    val correctAnswerType: String,
    val correctAnswerValue: String,
    val explanation: String?,
    val sourceDataset: String,
    val license: String,
    val fingerprint: String
)

/**
 * Part 8 — Repository interface for local Room/SQLite dataset question retrieval
 * and offline content pack querying without runtime JSON parsing.
 */
interface QuestionRepository {
    suspend fun getQuestions(
        subjectId: String,
        topicId: String?,
        difficulty: Int?,
        limit: Int
    ): List<Question>

    suspend fun getRandomQuestion(
        subjectId: String,
        topicId: String?,
        difficulty: Int?
    ): Question?

    suspend fun saveAttempt(
        attempt: QuestionAttempt
    )

    suspend fun getQuestionById(
        questionId: String
    ): Question?
}
