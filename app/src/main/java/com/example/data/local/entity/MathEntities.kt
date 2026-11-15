package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Difficulty
import com.example.domain.model.MathCategory

@Entity(tableName = "skill_profiles")
data class SkillProfileEntity(
    @PrimaryKey val category: MathCategory,
    val skillScore: Float,
    val accuracy: Float,
    val averageResponseTimeMs: Long,
    val currentDifficulty: Difficulty,
    val correctStreak: Int,
    val wrongStreak: Int,
    val attemptsCount: Int,
    val lastPracticedAt: Long
)

@Entity(tableName = "question_attempts")
data class QuestionAttemptEntity(
    @PrimaryKey val id: String,
    val questionId: String,
    val category: MathCategory,
    val difficulty: Difficulty,
    val prompt: String,
    val expectedAnswer: String,
    val submittedAnswer: String,
    val isCorrect: Boolean,
    val responseTimeMs: Long,
    val attemptedAt: Long
)

@Entity(tableName = "generation_history")
data class GenerationHistoryEntity(
    @PrimaryKey val fingerprint: String,
    val category: MathCategory,
    val generatedAt: Long,
    val generatorVersion: Int
)

@Entity(tableName = "review_queue")
data class ReviewQueueEntity(
    @PrimaryKey val id: String,
    val category: MathCategory,
    val difficulty: Difficulty,
    val conceptKey: String,
    val promptSnapshot: String,
    val expectedAnswer: String,
    val explanationSnapshot: String,
    val reviewAt: Long,
    val priority: Int,
    val failureCount: Int
)

@Entity(tableName = "daily_progress")
data class DailyProgressEntity(
    @PrimaryKey val date: String,
    val attemptedCount: Int,
    val correctCount: Int,
    val totalXp: Int,
    val practiceDurationMs: Long
)

@Entity(tableName = "bookmarked_questions")
data class BookmarkedQuestionEntity(
    @PrimaryKey val id: String,
    val category: MathCategory,
    val difficulty: Difficulty,
    val prompt: String,
    val answer: String,
    val explanation: String,
    val savedAt: Long
)
