package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat_users ORDER BY displayName ASC")
    fun observeUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM chat_users")
    suspend fun getAllUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUsers(users: List<UserEntity>)

    @Query("SELECT * FROM chat_conversations ORDER BY is_pinned DESC, last_message_at DESC")
    fun observeConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM chat_conversations ORDER BY is_pinned DESC, last_message_at DESC")
    suspend fun getAllConversations(): List<ConversationEntity>

    @Query("SELECT * FROM chat_conversations WHERE id = :conversationId LIMIT 1")
    suspend fun getConversationById(conversationId: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversations(conversations: List<ConversationEntity>)

    @Query("SELECT * FROM chat_conversation_members WHERE conversation_id = :conversationId ORDER BY role ASC, displayName ASC")
    fun observeMembersForConversation(conversationId: String): Flow<List<ConversationMemberEntity>>

    @Query("SELECT * FROM chat_conversation_members")
    fun observeAllMembers(): Flow<List<ConversationMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMember(member: ConversationMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMembers(members: List<ConversationMemberEntity>)

    @Query("DELETE FROM chat_conversation_members WHERE conversation_id = :conversationId AND user_id = :userId")
    suspend fun removeMember(conversationId: String, userId: String)

    @Query("SELECT * FROM chat_messages WHERE conversation_id = :conversationId AND isDeleted = 0 ORDER BY created_at ASC")
    fun observeMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE isDeleted = 0 ORDER BY created_at ASC")
    fun observeAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE conversation_id = :conversationId AND isDeleted = 0 ORDER BY created_at ASC")
    suspend fun getMessagesForConversation(conversationId: String): List<MessageEntity>

    @Query("SELECT * FROM chat_messages WHERE id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): MessageEntity?

    @Query(
        """
        SELECT * FROM chat_messages
        WHERE isDeleted = 0
          AND (:conversationId IS NULL OR conversation_id = :conversationId)
          AND (:messageType IS NULL OR message_type = :messageType)
          AND (content LIKE '%' || :query || '%'
               OR senderName LIKE '%' || :query || '%'
               OR COALESCE(sharedNoteTitle, '') LIKE '%' || :query || '%'
               OR COALESCE(latexFormula, '') LIKE '%' || :query || '%')
        ORDER BY created_at DESC
        """
    )
    suspend fun searchMessages(
        query: String,
        conversationId: String? = null,
        messageType: String? = null
    ): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessages(messages: List<MessageEntity>)

    @Query("DELETE FROM chat_messages WHERE conversation_id = :conversationId")
    suspend fun clearConversationMessages(conversationId: String)

    @Query("SELECT * FROM chat_attachments")
    fun observeAllAttachments(): Flow<List<AttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAttachment(attachment: AttachmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAttachments(attachments: List<AttachmentEntity>)

    @Query("SELECT * FROM chat_message_reactions")
    fun observeAllReactions(): Flow<List<MessageReactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReaction(reaction: MessageReactionEntity)

    @Query("DELETE FROM chat_message_reactions WHERE message_id = :messageId AND user_id = :userId")
    suspend fun deleteReaction(messageId: String, userId: String)

    @Query("SELECT * FROM chat_message_receipts")
    fun observeAllReceipts(): Flow<List<MessageReceiptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReceipt(receipt: MessageReceiptEntity)

    @Query("SELECT * FROM chat_starred_messages")
    fun observeStarredMessages(): Flow<List<StarredMessageEntity>>

    @Query("SELECT * FROM chat_starred_messages WHERE message_id = :messageId LIMIT 1")
    suspend fun getStarredMessage(messageId: String): StarredMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStarredMessage(starred: StarredMessageEntity)

    @Query("DELETE FROM chat_starred_messages WHERE message_id = :messageId")
    suspend fun deleteStarredMessage(messageId: String)

    @Query("SELECT * FROM chat_pinned_messages")
    fun observePinnedMessages(): Flow<List<PinnedMessageEntity>>

    @Query("SELECT * FROM chat_pinned_messages WHERE message_id = :messageId LIMIT 1")
    suspend fun getPinnedMessage(messageId: String): PinnedMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPinnedMessage(pinned: PinnedMessageEntity)

    @Query("DELETE FROM chat_pinned_messages WHERE message_id = :messageId")
    suspend fun deletePinnedMessage(messageId: String)

    @Query("SELECT * FROM chat_drafts")
    fun observeAllDrafts(): Flow<List<ChatDraftEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDraft(draft: ChatDraftEntity)

    @Query("DELETE FROM chat_drafts WHERE conversation_id = :conversationId")
    suspend fun deleteDraft(conversationId: String)

    @Query("SELECT * FROM chat_settings WHERE id = 'default_chat_settings' LIMIT 1")
    fun observeChatSettings(): Flow<ChatSettingsEntity?>

    @Query("SELECT * FROM chat_settings WHERE id = 'default_chat_settings' LIMIT 1")
    suspend fun getChatSettings(): ChatSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChatSettings(settings: ChatSettingsEntity)

    @Query("SELECT * FROM chat_polls")
    fun observeAllPolls(): Flow<List<PollEntity>>

    @Query("SELECT * FROM chat_poll_options ORDER BY position ASC")
    fun observeAllPollOptions(): Flow<List<PollOptionEntity>>

    @Query("SELECT * FROM chat_poll_votes")
    fun observeAllPollVotes(): Flow<List<PollVoteEntity>>

    @Query("SELECT * FROM chat_poll_votes WHERE poll_id = :pollId AND user_id = :userId")
    suspend fun getUserVotesForPoll(pollId: String, userId: String): List<PollVoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPoll(poll: PollEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPollOptions(options: List<PollOptionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPollVote(vote: PollVoteEntity)

    @Query("DELETE FROM chat_poll_votes WHERE poll_id = :pollId AND user_id = :userId")
    suspend fun clearUserVotesForPoll(pollId: String, userId: String)

    @Query("DELETE FROM chat_poll_votes WHERE poll_id = :pollId AND option_id = :optionId AND user_id = :userId")
    suspend fun deletePollVote(pollId: String, optionId: String, userId: String)
}
