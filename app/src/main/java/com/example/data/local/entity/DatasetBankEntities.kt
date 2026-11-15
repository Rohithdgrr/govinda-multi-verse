package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "parent_group") val parentGroup: String,
    val description: String
)

@Entity(
    tableName = "topics",
    indices = [
        Index(value = ["subject_id"])
    ]
)
data class TopicEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "subject_id") val subjectId: String,
    val name: String,
    val subtopic: String?
)

@Entity(
    tableName = "questions",
    indices = [
        Index(value = ["subject_id"]),
        Index(value = ["topic_id"]),
        Index(value = ["education_level"]),
        Index(value = ["exam_id"]),
        Index(value = ["difficulty"]),
        Index(value = ["question_type"]),
        Index(value = ["source_dataset"]),
        Index(value = ["fingerprint"])
    ]
)
data class DatasetQuestionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "pack_id") val packId: String,
    @ColumnInfo(name = "subject_id") val subjectId: String,
    @ColumnInfo(name = "topic_id") val topicId: String,
    val subtopic: String?,
    @ColumnInfo(name = "education_level") val educationLevel: String,
    @ColumnInfo(name = "class_level") val classLevel: String?,
    @ColumnInfo(name = "exam_id") val examId: String?,
    val difficulty: Int,
    @ColumnInfo(name = "question_type") val questionType: String,
    val language: String,
    @ColumnInfo(name = "question_text") val questionText: String,
    @ColumnInfo(name = "correct_answer_type") val correctAnswerType: String,
    @ColumnInfo(name = "correct_answer_value") val correctAnswerValue: String,
    val explanation: String?,
    @ColumnInfo(name = "solution_steps_json") val solutionStepsJson: String,
    @ColumnInfo(name = "supporting_facts_json") val supportingFactsJson: String,
    @ColumnInfo(name = "source_dataset") val sourceDataset: String,
    val fingerprint: String,
    val verified: Boolean = false,
    @ColumnInfo(name = "content_version") val contentVersion: Int = 1
)

@Entity(
    tableName = "question_options",
    indices = [
        Index(value = ["question_id"])
    ]
)
data class QuestionOptionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "question_id") val questionId: String,
    @ColumnInfo(name = "option_id") val optionId: String,
    @ColumnInfo(name = "option_text") val optionText: String,
    @ColumnInfo(name = "is_correct") val isCorrect: Boolean
)

@Entity(
    tableName = "question_sources",
    indices = [
        Index(value = ["source_dataset"])
    ]
)
data class QuestionSourceEntity(
    @PrimaryKey @ColumnInfo(name = "question_id") val questionId: String,
    @ColumnInfo(name = "source_dataset") val sourceDataset: String,
    @ColumnInfo(name = "source_record_id") val sourceRecordId: String,
    @ColumnInfo(name = "dataset_url") val datasetUrl: String,
    val license: String,
    @ColumnInfo(name = "source_split") val sourceSplit: String,
    @ColumnInfo(name = "source_revision") val sourceRevision: String
)

@Entity(tableName = "question_packs")
data class QuestionPackEntity(
    @PrimaryKey val id: String,
    val name: String,
    val license: String,
    @ColumnInfo(name = "is_installed") val isInstalled: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(
    tableName = "question_pack_versions",
    indices = [
        Index(value = ["pack_id"])
    ]
)
data class QuestionPackVersionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "pack_id") val packId: String,
    @ColumnInfo(name = "version_code") val versionCode: Int,
    @ColumnInfo(name = "record_count") val recordCount: Int,
    val sha256: String,
    @ColumnInfo(name = "released_at") val releasedAt: Long
)

@Entity(
    tableName = "user_skill_profiles",
    primaryKeys = ["subject_id", "topic_id"],
    indices = [
        Index(value = ["subject_id"]),
        Index(value = ["topic_id"])
    ]
)
data class UserSkillProfileEntity(
    @ColumnInfo(name = "subject_id") val subjectId: String,
    @ColumnInfo(name = "topic_id") val topicId: String,
    @ColumnInfo(name = "skill_score") val skillScore: Float,
    val accuracy: Float,
    @ColumnInfo(name = "current_difficulty") val currentDifficulty: Int,
    @ColumnInfo(name = "attempts_count") val attemptsCount: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)

@Entity(tableName = "saved_questions")
data class SavedQuestionEntity(
    @PrimaryKey @ColumnInfo(name = "question_id") val questionId: String,
    @ColumnInfo(name = "saved_at") val savedAt: Long,
    val notes: String? = null
)
