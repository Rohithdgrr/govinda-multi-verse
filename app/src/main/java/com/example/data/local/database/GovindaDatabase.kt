package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.core.database.AttachmentEntity
import com.example.core.database.ChatDao
import com.example.core.database.ChatDraftEntity
import com.example.core.database.ChatSettingsEntity
import com.example.core.database.ConversationEntity
import com.example.core.database.ConversationMemberEntity
import com.example.core.database.FolderEntity
import com.example.core.database.MessageEntity
import com.example.core.database.MessageReactionEntity
import com.example.core.database.MessageReceiptEntity
import com.example.core.database.NoteAttachmentEntity
import com.example.core.database.NoteBlockEntity
import com.example.core.database.NoteEntity
import com.example.core.database.NoteLinkEntity
import com.example.core.database.NoteRevisionEntity
import com.example.core.database.NoteTagCrossRef
import com.example.core.database.NoteTemplateEntity
import com.example.core.database.NotesStudioDao
import com.example.core.database.PinnedMessageEntity
import com.example.core.database.PollEntity
import com.example.core.database.PollOptionEntity
import com.example.core.database.PollVoteEntity
import com.example.core.database.StarredMessageEntity
import com.example.core.database.TagEntity
import com.example.core.database.UserEntity
import com.example.data.local.dao.BookmarkedQuestionDao
import com.example.data.local.dao.DailyProgressDao
import com.example.data.local.dao.GenerationHistoryDao
import com.example.data.local.dao.QuestionAttemptDao
import com.example.data.local.dao.QuestionBankDao
import com.example.data.local.dao.ReviewQueueDao
import com.example.data.local.dao.SkillProfileDao
import com.example.data.local.entity.BookmarkedQuestionEntity
import com.example.data.local.entity.DailyProgressEntity
import com.example.data.local.entity.DatasetQuestionEntity
import com.example.data.local.entity.GenerationHistoryEntity
import com.example.data.local.entity.QuestionAttemptEntity
import com.example.data.local.entity.QuestionOptionEntity
import com.example.data.local.entity.QuestionPackEntity
import com.example.data.local.entity.QuestionPackVersionEntity
import com.example.data.local.entity.QuestionSourceEntity
import com.example.data.local.entity.ReviewQueueEntity
import com.example.data.local.entity.SavedQuestionEntity
import com.example.data.local.entity.SkillProfileEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TopicEntity
import com.example.data.local.entity.UserSkillProfileEntity
import com.example.domain.model.Difficulty
import com.example.domain.model.MathCategory

class GovindaTypeConverters {
    @TypeConverter
    fun fromCategory(value: MathCategory): String = value.name

    @TypeConverter
    fun toCategory(value: String): MathCategory =
        runCatching { MathCategory.valueOf(value) }.getOrDefault(MathCategory.ADDITION)

    @TypeConverter
    fun fromDifficulty(value: Difficulty): String = value.name

    @TypeConverter
    fun toDifficulty(value: String): Difficulty =
        runCatching { Difficulty.valueOf(value) }.getOrDefault(Difficulty.BEGINNER)
}

@Database(
    entities = [
        SkillProfileEntity::class,
        QuestionAttemptEntity::class,
        GenerationHistoryEntity::class,
        ReviewQueueEntity::class,
        DailyProgressEntity::class,
        BookmarkedQuestionEntity::class,
        SubjectEntity::class,
        TopicEntity::class,
        DatasetQuestionEntity::class,
        QuestionOptionEntity::class,
        QuestionSourceEntity::class,
        QuestionPackEntity::class,
        QuestionPackVersionEntity::class,
        UserSkillProfileEntity::class,
        SavedQuestionEntity::class,
        NoteEntity::class,
        NoteBlockEntity::class,
        FolderEntity::class,
        TagEntity::class,
        NoteTagCrossRef::class,
        NoteAttachmentEntity::class,
        NoteLinkEntity::class,
        NoteRevisionEntity::class,
        NoteTemplateEntity::class,
        UserEntity::class,
        ConversationEntity::class,
        ConversationMemberEntity::class,
        MessageEntity::class,
        AttachmentEntity::class,
        MessageReactionEntity::class,
        MessageReceiptEntity::class,
        StarredMessageEntity::class,
        PinnedMessageEntity::class,
        ChatDraftEntity::class,
        ChatSettingsEntity::class,
        PollEntity::class,
        PollOptionEntity::class,
        PollVoteEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(GovindaTypeConverters::class)
abstract class GovindaDatabase : RoomDatabase() {
    abstract fun skillProfileDao(): SkillProfileDao
    abstract fun questionAttemptDao(): QuestionAttemptDao
    abstract fun generationHistoryDao(): GenerationHistoryDao
    abstract fun reviewQueueDao(): ReviewQueueDao
    abstract fun dailyProgressDao(): DailyProgressDao
    abstract fun bookmarkedQuestionDao(): BookmarkedQuestionDao
    abstract fun questionBankDao(): QuestionBankDao
    abstract fun notesStudioDao(): NotesStudioDao
    abstract fun chatDao(): ChatDao

    companion object {
        fun build(context: Context): GovindaDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                GovindaDatabase::class.java,
                "govinda_math_engine.db"
            ).fallbackToDestructiveMigration().build()
    }
}
