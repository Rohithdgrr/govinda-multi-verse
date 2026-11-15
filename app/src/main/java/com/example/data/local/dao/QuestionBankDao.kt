package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.DatasetQuestionEntity
import com.example.data.local.entity.QuestionOptionEntity
import com.example.data.local.entity.QuestionPackEntity
import com.example.data.local.entity.QuestionPackVersionEntity
import com.example.data.local.entity.QuestionSourceEntity
import com.example.data.local.entity.SavedQuestionEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TopicEntity
import com.example.data.local.entity.UserSkillProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionBankDao {

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun countQuestions(): Int

    @Query("SELECT * FROM subjects ORDER BY id ASC")
    fun observeSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM question_packs ORDER BY created_at DESC")
    fun observeQuestionPacks(): Flow<List<QuestionPackEntity>>

    @Query(
        """
        SELECT * FROM questions
        WHERE subject_id = :subjectId
          AND (:topicId IS NULL OR topic_id = :topicId)
          AND (:difficulty IS NULL OR difficulty = :difficulty)
        ORDER BY id ASC
        LIMIT :limit
        """
    )
    suspend fun queryQuestions(
        subjectId: String,
        topicId: String?,
        difficulty: Int?,
        limit: Int
    ): List<DatasetQuestionEntity>

    @Query(
        """
        SELECT * FROM questions
        WHERE subject_id = :subjectId
          AND (:topicId IS NULL OR topic_id = :topicId)
          AND (:difficulty IS NULL OR difficulty = :difficulty)
        ORDER BY RANDOM()
        LIMIT 1
        """
    )
    suspend fun queryRandomQuestion(
        subjectId: String,
        topicId: String?,
        difficulty: Int?
    ): DatasetQuestionEntity?

    @Query("SELECT * FROM questions WHERE id = :questionId LIMIT 1")
    suspend fun getQuestionById(questionId: String): DatasetQuestionEntity?

    @Query("SELECT * FROM question_options WHERE question_id = :questionId ORDER BY option_id ASC")
    suspend fun getOptionsForQuestion(questionId: String): List<QuestionOptionEntity>

    @Query("SELECT * FROM question_sources WHERE question_id = :questionId LIMIT 1")
    suspend fun getSourceForQuestion(questionId: String): QuestionSourceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPacks(packs: List<QuestionPackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackVersions(versions: List<QuestionPackVersionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<DatasetQuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOptions(options: List<QuestionOptionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSources(sources: List<QuestionSourceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserSkillProfile(profile: UserSkillProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSavedQuestion(saved: SavedQuestionEntity)
}
