package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.BookmarkedQuestionEntity
import com.example.data.local.entity.DailyProgressEntity
import com.example.data.local.entity.GenerationHistoryEntity
import com.example.data.local.entity.QuestionAttemptEntity
import com.example.data.local.entity.ReviewQueueEntity
import com.example.data.local.entity.SkillProfileEntity
import com.example.domain.model.MathCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillProfileDao {
    @Query("SELECT * FROM skill_profiles")
    fun observeAll(): Flow<List<SkillProfileEntity>>

    @Query("SELECT * FROM skill_profiles")
    suspend fun getAll(): List<SkillProfileEntity>

    @Query("SELECT * FROM skill_profiles WHERE category = :category LIMIT 1")
    suspend fun getByCategory(category: MathCategory): SkillProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: SkillProfileEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(profiles: List<SkillProfileEntity>)

    @Query("DELETE FROM skill_profiles")
    suspend fun clearAll()
}

@Dao
interface QuestionAttemptDao {
    @Query("SELECT * FROM question_attempts ORDER BY attemptedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<QuestionAttemptEntity>>

    @Query("SELECT * FROM question_attempts ORDER BY attemptedAt DESC")
    suspend fun getAll(): List<QuestionAttemptEntity>

    @Query("SELECT * FROM question_attempts WHERE category = :category ORDER BY attemptedAt DESC LIMIT :limit")
    suspend fun getRecentForCategory(category: MathCategory, limit: Int): List<QuestionAttemptEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attempt: QuestionAttemptEntity)

    @Query(
        "DELETE FROM question_attempts WHERE id NOT IN " +
            "(SELECT id FROM question_attempts ORDER BY attemptedAt DESC LIMIT 500)"
    )
    suspend fun trimOldAttempts()

    @Query("DELETE FROM question_attempts")
    suspend fun clearAll()
}

@Dao
interface GenerationHistoryDao {
    @Query("SELECT fingerprint FROM generation_history ORDER BY generatedAt DESC LIMIT :limit")
    suspend fun getRecentFingerprints(limit: Int): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: GenerationHistoryEntity)

    @Query(
        "DELETE FROM generation_history WHERE fingerprint NOT IN " +
            "(SELECT fingerprint FROM generation_history ORDER BY generatedAt DESC LIMIT 200)"
    )
    suspend fun trimOldHistory()

    @Query("DELETE FROM generation_history")
    suspend fun clearAll()
}

@Dao
interface ReviewQueueDao {
    @Query("SELECT * FROM review_queue ORDER BY reviewAt ASC, priority DESC")
    fun observeAll(): Flow<List<ReviewQueueEntity>>

    @Query("SELECT * FROM review_queue ORDER BY reviewAt ASC, priority DESC")
    suspend fun getAll(): List<ReviewQueueEntity>

    @Query("SELECT * FROM review_queue WHERE conceptKey = :conceptKey LIMIT 1")
    suspend fun getByConceptKey(conceptKey: String): ReviewQueueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ReviewQueueEntity)

    @Query("DELETE FROM review_queue WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query(
        "DELETE FROM review_queue WHERE id IN " +
            "(SELECT id FROM review_queue WHERE category = :category ORDER BY reviewAt ASC LIMIT 1)"
    )
    suspend fun resolveOldestForCategory(category: MathCategory)

    @Query("DELETE FROM review_queue")
    suspend fun clearAll()
}

@Dao
interface DailyProgressDao {
    @Query("SELECT * FROM daily_progress ORDER BY date DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<DailyProgressEntity>>

    @Query("SELECT * FROM daily_progress ORDER BY date DESC")
    suspend fun getAll(): List<DailyProgressEntity>

    @Query("SELECT * FROM daily_progress WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DailyProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: DailyProgressEntity)

    @Query("DELETE FROM daily_progress")
    suspend fun clearAll()
}

@Dao
interface BookmarkedQuestionDao {
    @Query("SELECT * FROM bookmarked_questions ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<BookmarkedQuestionEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_questions WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: BookmarkedQuestionEntity)

    @Query("DELETE FROM bookmarked_questions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM bookmarked_questions")
    suspend fun clearAll()
}
