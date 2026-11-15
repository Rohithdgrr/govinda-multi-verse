package com.example.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ConversationType(val label: String) {
    PERSONAL("Personal"),
    GROUP("Study Group"),
    SELF("Saved Messages"),
    BROADCAST("Broadcast")
}

enum class ChatMessageType(val label: String) {
    PLAIN_TEXT("Text"),
    RICH_TEXT("Rich Text"),
    IMAGE("Image"),
    VIDEO("Video"),
    DOCUMENT("Document"),
    PDF("PDF"),
    AUDIO_FILE("Audio"),
    VOICE_MESSAGE("Voice Note"),
    SHARED_LINK("Link"),
    CONTACT_CARD("Contact"),
    LOCATION("Location"),
    LIVE_LOCATION("Live Location"),
    GROUP_EVENT("Group Event"),
    LATEX_EQUATION("LaTeX Equation"),
    CODE_SNIPPET("Code Snippet"),
    NOTE_CARD("Notes Studio Doc"),
    QUESTION_CARD("MATHGATE Question"),
    POLL("Group Poll"),
    SYSTEM_NOTIFICATION("System"),
    FILE_ATTACHMENT("Attachment")
}

enum class DeliveryState(val symbol: String, val label: String) {
    LOCAL_SAVED("✓", "Saved on device (Local-Only)"),
    QUEUED_OFFLINE("🕒", "Queued offline"),
    SENT("✓", "Sent"),
    DELIVERED("✓✓", "Delivered"),
    READ("✓✓", "Read")
}

@Entity(
    tableName = "chat_users",
    indices = [Index(value = ["handle"])]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val handle: String,
    val avatarInitials: String,
    val roleOrBranch: String,
    val pairingCodeOtp: String,
    val linkedVia: String, // "SELF", "QR_CODE", "OTP_CODE", "STUDY_GROUP", "LOCAL_BOT"
    val isBlocked: Boolean = false,
    val isReported: Boolean = false,
    val statusText: String = "Available for GATE study discussions",
    val statusUpdatedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_conversations",
    indices = [
        Index(value = ["conversation_type"]),
        Index(value = ["last_message_at"]),
        Index(value = ["is_pinned"]),
        Index(value = ["is_archived"])
    ]
)
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val avatarInitials: String,
    @ColumnInfo(name = "conversation_type") val conversationType: String = ConversationType.PERSONAL.name,
    val linkedVia: String = "OTP_CODE",
    val pairingCodeOtp: String = "",
    @ColumnInfo(name = "is_pinned") val isPinned: Boolean = false,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean = false,
    val isMuted: Boolean = false,
    val isLocked: Boolean = false,
    val disappearingHours: Int = 0, // 0 = Off, 24 = 24h, 168 = 7d, 2160 = 90d
    val isBlocked: Boolean = false,
    val isReported: Boolean = false,
    val onlyAdminsCanSend: Boolean = false,
    val groupRules: String = "1. Keep discussions focused on technical problem solving.\n2. Share step-by-step derivations when posting solutions.",
    val inviteLink: String = "mathgate://join/study-cohort",
    val lastSeenText: String = "last seen today at 12:15 PM",
    val isTyping: Boolean = false,
    val unreadCount: Int = 0,
    val lastMessagePreview: String = "",
    @ColumnInfo(name = "last_message_at") val lastMessageAt: Long = System.currentTimeMillis(),
    val wallpaperId: String = "WHITE_BLUEPRINT",
    val announcementText: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_conversation_members",
    primaryKeys = ["conversation_id", "user_id"],
    indices = [
        Index(value = ["conversation_id"]),
        Index(value = ["user_id"])
    ]
)
data class ConversationMemberEntity(
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val displayName: String,
    val role: String = "MEMBER", // "ADMIN", "MEMBER"
    val canSendMessages: Boolean = true,
    val joinedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_messages",
    indices = [
        Index(value = ["conversation_id"]),
        Index(value = ["sender_id"]),
        Index(value = ["created_at"]),
        Index(value = ["message_type"]),
        Index(value = ["delivery_state"])
    ]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "sender_id") val senderId: String,
    val senderName: String,
    val isOutgoing: Boolean,
    @ColumnInfo(name = "message_type") val messageType: String = ChatMessageType.PLAIN_TEXT.name,
    val content: String,
    val latexFormula: String? = null,
    val codeLanguage: String? = null,
    val codeContent: String? = null,
    val sharedNoteId: String? = null,
    val sharedNoteTitle: String? = null,
    val questionId: String? = null,
    val questionJson: String? = null,
    val pollId: String? = null,
    val attachmentId: String? = null,
    val replyToMessageId: String? = null,
    val replyToSnippet: String? = null,
    val forwardedFrom: String? = null,
    val isViewOnce: Boolean = false,
    val isViewOnceOpened: Boolean = false,
    val linkPreviewUrl: String? = null,
    val linkPreviewTitle: String? = null,
    val linkPreviewHost: String? = null,
    val eventPayloadJson: String? = null,
    val liveLocationExpiresAt: Long? = null,
    @ColumnInfo(name = "delivery_state") val deliveryState: String = DeliveryState.LOCAL_SAVED.name,
    val isDeleted: Boolean = false,
    val deletedForEveryone: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    val editedAt: Long? = null
)

@Entity(
    tableName = "chat_attachments",
    indices = [Index(value = ["conversation_id"]), Index(value = ["message_id"])]
)
data class AttachmentEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "message_id") val messageId: String,
    val uriString: String,
    val fileName: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val durationMs: Long = 0L,
    val waveformCsv: String = "",
    val transferState: String = "STORED_LOCALLY", // "STORED_LOCALLY", "UPLOADING", "FAILED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_message_reactions",
    primaryKeys = ["message_id", "user_id"],
    indices = [Index(value = ["message_id"]), Index(value = ["conversation_id"])]
)
data class MessageReactionEntity(
    @ColumnInfo(name = "message_id") val messageId: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val emoji: String,
    val reactedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_message_receipts",
    primaryKeys = ["message_id", "user_id"],
    indices = [Index(value = ["message_id"])]
)
data class MessageReceiptEntity(
    @ColumnInfo(name = "message_id") val messageId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val state: String, // "LOCAL_SAVED", "DELIVERED", "READ"
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_starred_messages",
    indices = [Index(value = ["conversation_id"])]
)
data class StarredMessageEntity(
    @PrimaryKey @ColumnInfo(name = "message_id") val messageId: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    val starredAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_pinned_messages",
    indices = [Index(value = ["conversation_id"])]
)
data class PinnedMessageEntity(
    @PrimaryKey @ColumnInfo(name = "message_id") val messageId: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    val pinnedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_drafts"
)
data class ChatDraftEntity(
    @PrimaryKey @ColumnInfo(name = "conversation_id") val conversationId: String,
    val draftText: String,
    val replyToMessageId: String? = null,
    val replyToSnippet: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_settings"
)
data class ChatSettingsEntity(
    @PrimaryKey val id: String = "default_chat_settings",
    val readReceiptsEnabled: Boolean = true,
    val onlineVisibilityEnabled: Boolean = true,
    val profileVisibility: String = "CONTACTS_ONLY",
    val wallpaperId: String = "WHITE_BLUEPRINT", // "WHITE_BLUEPRINT", "LIGHT_GRID", "SOFT_BLUE"
    val bubbleStyle: String = "BLUEPRINT_SHARP", // "BLUEPRINT_SHARP", "ROUNDED_MODERN"
    val fontSizeScale: Float = 1.0f,
    val messageDensity: String = "COMFORTABLE", // "COMPACT", "COMFORTABLE"
    val remoteSyncEnabled: Boolean = false,
    val chatLockPin: String = "1234",
    val twoStepPin: String = "",
    val myStatusText: String = "Solving Laplace & Bode Plot problem sets 📐",
    val myStatusUpdatedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_polls",
    indices = [Index(value = ["conversation_id"]), Index(value = ["message_id"])]
)
data class PollEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "message_id") val messageId: String,
    val question: String,
    val allowMultiple: Boolean = false,
    val isAnonymous: Boolean = false,
    val expiresAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_poll_options",
    indices = [Index(value = ["poll_id"])]
)
data class PollOptionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "poll_id") val pollId: String,
    val optionText: String,
    val position: Int
)

@Entity(
    tableName = "chat_poll_votes",
    primaryKeys = ["poll_id", "option_id", "user_id"],
    indices = [Index(value = ["poll_id"]), Index(value = ["option_id"])]
)
data class PollVoteEntity(
    @ColumnInfo(name = "poll_id") val pollId: String,
    @ColumnInfo(name = "option_id") val optionId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val votedAt: Long = System.currentTimeMillis()
)
