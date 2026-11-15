package com.example.feature.chat

import com.example.core.database.AttachmentEntity
import com.example.core.database.ChatDao
import com.example.core.database.ChatDraftEntity
import com.example.core.database.ChatMessageType
import com.example.core.database.ChatSettingsEntity
import com.example.core.database.ConversationEntity
import com.example.core.database.ConversationMemberEntity
import com.example.core.database.ConversationType
import com.example.core.database.DeliveryState
import com.example.core.database.MessageEntity
import com.example.core.database.MessageReactionEntity
import com.example.core.database.MessageReceiptEntity
import com.example.core.database.PinnedMessageEntity
import com.example.core.database.PollEntity
import com.example.core.database.PollOptionEntity
import com.example.core.database.PollVoteEntity
import com.example.core.database.StarredMessageEntity
import com.example.core.database.UserEntity
import com.example.data.local.database.GovindaDatabase
import com.example.domain.model.MathQuestion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

data class GroupEventPayload(
    val eventId: String,
    val title: String,
    val dateTimeText: String,
    val location: String,
    val description: String,
    val rsvpByUser: Map<String, String> // userId -> "GOING", "MAYBE", "NOT_GOING"
) {
    fun encode(): String {
        val rsvpRaw = rsvpByUser.entries.joinToString(";;") { "${it.key}:${it.value}" }
        return listOf(
            eventId.replace("|", "/"),
            title.replace("|", "/"),
            dateTimeText.replace("|", "/"),
            location.replace("|", "/"),
            description.replace("|", "/"),
            rsvpRaw
        ).joinToString("|")
    }

    companion object {
        fun decode(raw: String?): GroupEventPayload? {
            if (raw.isNullOrBlank()) return null
            val parts = raw.split("|")
            if (parts.size < 6) return null
            val map = parts[5].split(";;")
                .mapNotNull { entry ->
                    val kv = entry.split(":")
                    if (kv.size == 2 && kv[0].isNotBlank()) kv[0] to kv[1] else null
                }
                .toMap()
            return GroupEventPayload(
                eventId = parts[0],
                title = parts[1],
                dateTimeText = parts[2],
                location = parts[3],
                description = parts[4],
                rsvpByUser = map
            )
        }
    }
}

data class SharedQuestionPayload(
    val questionId: String,
    val prompt: String,
    val subject: String,
    val topic: String,
    val difficulty: String,
    val options: List<String>,
    val answer: String,
    val sourceAttribution: String,
    val explanation: String
) {
    fun encodeToPipeString(): String {
        val safeOpts = options.joinToString(";;") { it.replace("|", "/") }
        return listOf(
            questionId,
            prompt.replace("|", "/"),
            subject.replace("|", "/"),
            topic.replace("|", "/"),
            difficulty.replace("|", "/"),
            safeOpts,
            answer.replace("|", "/"),
            sourceAttribution.replace("|", "/"),
            explanation.replace("|", "/")
        ).joinToString("|")
    }

    companion object {
        fun decodeFromPipeString(raw: String?): SharedQuestionPayload? {
            if (raw.isNullOrBlank()) return null
            val parts = raw.split("|")
            if (parts.size < 9) return null
            return SharedQuestionPayload(
                questionId = parts[0],
                prompt = parts[1],
                subject = parts[2],
                topic = parts[3],
                difficulty = parts[4],
                options = parts[5].split(";;").filter { it.isNotBlank() },
                answer = parts[6],
                sourceAttribution = parts[7],
                explanation = parts[8]
            )
        }

        fun fromMathQuestion(question: MathQuestion): SharedQuestionPayload {
            return SharedQuestionPayload(
                questionId = question.id,
                prompt = question.prompt,
                subject = question.category.displayName,
                topic = question.category.displayName,
                difficulty = question.difficulty.displayName,
                options = question.options,
                answer = question.answer,
                sourceAttribution = "GOVINDA Adaptive Engine · ${question.category.displayName}",
                explanation = question.explanationSteps.joinToString(" → ")
            )
        }
    }
}

class ChatRepository(
    private val database: GovindaDatabase
) {
    private val dao: ChatDao = database.chatDao()
    private val seedMutex = Mutex()
    private var seeded = false

    val conversationsFlow: Flow<List<ConversationEntity>> = dao.observeConversations()
    val allMessagesFlow: Flow<List<MessageEntity>> = dao.observeAllMessages()
    val allMembersFlow: Flow<List<ConversationMemberEntity>> = dao.observeAllMembers()
    val allAttachmentsFlow: Flow<List<AttachmentEntity>> = dao.observeAllAttachments()
    val allReactionsFlow: Flow<List<MessageReactionEntity>> = dao.observeAllReactions()
    val starredFlow: Flow<List<StarredMessageEntity>> = dao.observeStarredMessages()
    val pinnedFlow: Flow<List<PinnedMessageEntity>> = dao.observePinnedMessages()
    val draftsFlow: Flow<List<ChatDraftEntity>> = dao.observeAllDrafts()
    val settingsFlow: Flow<ChatSettingsEntity?> = dao.observeChatSettings()
    val pollsFlow: Flow<List<PollEntity>> = dao.observeAllPolls()
    val pollOptionsFlow: Flow<List<PollOptionEntity>> = dao.observeAllPollOptions()
    val pollVotesFlow: Flow<List<PollVoteEntity>> = dao.observeAllPollVotes()
    val usersFlow: Flow<List<UserEntity>> = dao.observeUsers()

    suspend fun ensureSeeded() {
        seedMutex.withLock {
            if (seeded) return
            val existing = dao.getAllConversations()
            if (existing.isNotEmpty()) {
                seeded = true
                return
            }

            val now = System.currentTimeMillis()
            val users = listOf(
                UserEntity(
                    id = "user_self",
                    displayName = "Saved Messages (You)",
                    handle = "@self_vault",
                    avatarInitials = "ME",
                    roleOrBranch = "Personal Formula, Question & Note Vault",
                    pairingCodeOtp = "000000",
                    linkedVia = "SELF",
                    statusText = "Solving Laplace & Bode Plot problem sets 📐"
                ),
                UserEntity(
                    id = "user_arjun",
                    displayName = "Arjun Sharma",
                    handle = "@arjun_eee",
                    avatarInitials = "AS",
                    roleOrBranch = "EEE · Control & Power Systems",
                    pairingCodeOtp = "731904",
                    linkedVia = "OTP_CODE",
                    statusText = "Reviewing State-Space & Nyquist criteria ⚡"
                ),
                UserEntity(
                    id = "user_priya",
                    displayName = "Priya Nair",
                    handle = "@priya_cse",
                    avatarInitials = "PN",
                    roleOrBranch = "CSE · Algorithms & Graph Theory",
                    pairingCodeOtp = "519842",
                    linkedVia = "QR_CODE",
                    statusText = "Dynamic Programming & Dijkstra marathon 💻"
                ),
                UserEntity(
                    id = "user_bot",
                    displayName = "MATHGATE Formula & Note Bot",
                    handle = "@mathgate_bot",
                    avatarInitials = "MG",
                    roleOrBranch = "Offline Symbolic Solver & Note Assistant",
                    pairingCodeOtp = "111222",
                    linkedVia = "LOCAL_BOT",
                    statusText = "Always online · Send any equation or formula"
                )
            )
            dao.upsertUsers(users)

            val conversations = listOf(
                ConversationEntity(
                    id = "conv_self",
                    title = "Saved Messages (Self)",
                    subtitle = "Personal vault for formulas, questions, links & notes",
                    avatarInitials = "ME",
                    conversationType = ConversationType.SELF.name,
                    linkedVia = "LOCAL_SAVED",
                    pairingCodeOtp = "000000",
                    isPinned = true,
                    unreadCount = 0,
                    lastSeenText = "message yourself · personal vault",
                    lastMessagePreview = "Saved Question: GSM8K Multi-step Ratio & Laplace Transform",
                    lastMessageAt = now - 60_000L
                ),
                ConversationEntity(
                    id = "conv_group_ece",
                    title = "ECE & VLSI Study Group",
                    subtitle = "GATE / IES Signals, CMOS & Op-Amp Cohort · 4 Members",
                    avatarInitials = "EC",
                    conversationType = ConversationType.GROUP.name,
                    linkedVia = "QR_CODE",
                    pairingCodeOtp = "482910",
                    isPinned = true,
                    unreadCount = 2,
                    inviteLink = "mathgate://join/ece-vlsi-482910",
                    lastSeenText = "You, Arjun Sharma, Priya Nair, MATHGATE Bot",
                    lastMessagePreview = "📅 Event: Sunday Full-Syllabus Mock Test",
                    lastMessageAt = now - 90_000L,
                    announcementText = "Pinned Announcement: Complete the Op-Amp & Bode Plot problem set before Sunday."
                ),
                ConversationEntity(
                    id = "conv_peer_arjun",
                    title = "Arjun Sharma",
                    subtitle = "Linked via 6-Digit OTP #731-904 · Local-Only Mode",
                    avatarInitials = "AS",
                    conversationType = ConversationType.PERSONAL.name,
                    linkedVia = "OTP_CODE",
                    pairingCodeOtp = "731904",
                    isPinned = false,
                    unreadCount = 1,
                    lastSeenText = "online",
                    lastMessagePreview = "Here is the *RC low-pass* transfer function derivation.",
                    lastMessageAt = now - 240_000L
                ),
                ConversationEntity(
                    id = "conv_peer_priya",
                    title = "Priya Nair",
                    subtitle = "Linked via QR Matrix #519-842 · Local-Only Mode",
                    avatarInitials = "PN",
                    conversationType = ConversationType.PERSONAL.name,
                    linkedVia = "QR_CODE",
                    pairingCodeOtp = "519842",
                    isPinned = false,
                    unreadCount = 0,
                    lastSeenText = "last seen today at 11:45 AM",
                    lastMessagePreview = "Master Theorem recurrence snippet attached.",
                    lastMessageAt = now - 360_000L
                ),
                ConversationEntity(
                    id = "conv_bot_tutor",
                    title = "MATHGATE Bot",
                    subtitle = "Offline Symbolic Solver · Always Available",
                    avatarInitials = "MG",
                    conversationType = ConversationType.PERSONAL.name,
                    linkedVia = "LOCAL_SAVED",
                    pairingCodeOtp = "111222",
                    isPinned = false,
                    unreadCount = 0,
                    lastSeenText = "online · automated solver",
                    lastMessagePreview = "Send any expression, *bold* text, or LaTeX equation.",
                    lastMessageAt = now - 480_000L
                )
            )
            dao.upsertConversations(conversations)

            val members = listOf(
                ConversationMemberEntity("conv_group_ece", "user_self", "You", role = "ADMIN"),
                ConversationMemberEntity("conv_group_ece", "user_arjun", "Arjun Sharma", role = "ADMIN"),
                ConversationMemberEntity("conv_group_ece", "user_priya", "Priya Nair", role = "MEMBER"),
                ConversationMemberEntity("conv_group_ece", "user_bot", "MATHGATE Bot", role = "MEMBER")
            )
            dao.upsertMembers(members)

            val sampleQuestionPayload = SharedQuestionPayload(
                questionId = "GSM8K_GSM8K_TRAIN_0001",
                prompt = "A 10 kΩ resistor is in series with a 10 nF capacitor. What is the RC time constant τ in microseconds?",
                subject = "Electronics & Communication (ECE)",
                topic = "RC Time Constant & Filters",
                difficulty = "Intermediate",
                options = listOf("100 μs", "10 μs", "1000 μs", "1 μs"),
                answer = "100 μs",
                sourceAttribution = "MATHGATE ECE Generator · CC-BY-4.0",
                explanation = "τ = R × C = 10,000 Ω × 10 × 10⁻⁹ F = 10⁻⁴ s = 100 μs"
            )

            val sampleEvent = GroupEventPayload(
                eventId = "evt_gate_mock_01",
                title = "Sunday Full-Syllabus Mock Test & Discussion",
                dateTimeText = "Sun, 6:00 PM · 90 Mins",
                location = "Room 304 · Engineering Library",
                description = "Live problem-solving session covering Laplace, Bode Plots, and CMOS Delay.",
                rsvpByUser = mapOf("user_self" to "GOING", "user_arjun" to "GOING", "user_priya" to "MAYBE")
            )

            val pollId = "poll_gate_mock_01"
            val pollMsgId = "msg_seed_ece_poll"
            val messages = listOf(
                // Saved Messages (Self)
                MessageEntity(
                    id = "msg_seed_self_1",
                    conversationId = "conv_self",
                    senderId = "user_self",
                    senderName = "You",
                    isOutgoing = true,
                    messageType = ChatMessageType.LATEX_EQUATION.name,
                    content = "Saved *Laplace Transfer Function* & _Stability Criterion_:",
                    latexFormula = "H(s) = \\frac{Y(s)}{X(s)} = \\frac{\\omega_n^2}{s^2 + 2\\zeta\\omega_n s + \\omega_n^2}",
                    deliveryState = DeliveryState.READ.name,
                    createdAt = now - 180_000L
                ),
                MessageEntity(
                    id = "msg_seed_self_2",
                    conversationId = "conv_self",
                    senderId = "user_self",
                    senderName = "You",
                    isOutgoing = true,
                    messageType = ChatMessageType.QUESTION_CARD.name,
                    content = "Saved Question for Sunday Revision:",
                    questionId = sampleQuestionPayload.questionId,
                    questionJson = sampleQuestionPayload.encodeToPipeString(),
                    deliveryState = DeliveryState.READ.name,
                    createdAt = now - 60_000L
                ),
                // ECE & VLSI Study Group
                MessageEntity(
                    id = "msg_seed_ece_sys",
                    conversationId = "conv_group_ece",
                    senderId = "system",
                    senderName = "System",
                    isOutgoing = false,
                    messageType = ChatMessageType.SYSTEM_NOTIFICATION.name,
                    content = "Arjun Sharma created group \"ECE & VLSI Study Group\" · Local-Only Mode",
                    deliveryState = DeliveryState.READ.name,
                    createdAt = now - 500_000L
                ),
                MessageEntity(
                    id = "msg_seed_ece_1",
                    conversationId = "conv_group_ece",
                    senderId = "user_arjun",
                    senderName = "Arjun Sharma",
                    isOutgoing = false,
                    messageType = ChatMessageType.PLAIN_TEXT.name,
                    content = "Hey @Priya Nair and @You! Check out the reference sheet at https://nptel.ac.in/courses/108106188 for *CMOS propagation delay* formulas.",
                    linkPreviewUrl = "https://nptel.ac.in/courses/108106188",
                    linkPreviewHost = "nptel.ac.in",
                    linkPreviewTitle = "NPTEL · Digital IC Design & CMOS Timing Lecture Notes",
                    latexFormula = "f_{max} \\leq \\frac{1}{t_{pcq} + t_{pd} + t_{setup} - t_{skew}}",
                    deliveryState = DeliveryState.READ.name,
                    createdAt = now - 320_000L
                ),
                MessageEntity(
                    id = pollMsgId,
                    conversationId = "conv_group_ece",
                    senderId = "user_arjun",
                    senderName = "Arjun Sharma",
                    isOutgoing = false,
                    messageType = ChatMessageType.POLL.name,
                    content = "Which domain should our next weekend GATE mock cover?",
                    pollId = pollId,
                    deliveryState = DeliveryState.READ.name,
                    createdAt = now - 120_000L
                ),
                MessageEntity(
                    id = "msg_seed_ece_evt",
                    conversationId = "conv_group_ece",
                    senderId = "user_priya",
                    senderName = "Priya Nair",
                    isOutgoing = false,
                    messageType = ChatMessageType.GROUP_EVENT.name,
                    content = "Scheduled Group Event: ${sampleEvent.title}",
                    eventPayloadJson = sampleEvent.encode(),
                    deliveryState = DeliveryState.READ.name,
                    createdAt = now - 90_000L
                ),
                // Arjun Sharma personal chat
                MessageEntity(
                    id = "msg_seed_arjun_1",
                    conversationId = "conv_peer_arjun",
                    senderId = "user_arjun",
                    senderName = "Arjun Sharma",
                    isOutgoing = false,
                    messageType = ChatMessageType.LATEX_EQUATION.name,
                    content = "Here is the *RC low-pass* transfer function derivation (`f_c = 1 / 2πRC`):",
                    latexFormula = "H(j\\omega) = \\frac{1}{1 + j\\omega RC}, \\quad f_c = \\frac{1}{2\\pi RC}",
                    deliveryState = DeliveryState.READ.name,
                    createdAt = now - 240_000L
                ),
                // Priya Nair personal chat
                MessageEntity(
                    id = "msg_seed_priya_1",
                    conversationId = "conv_peer_priya",
                    senderId = "user_priya",
                    senderName = "Priya Nair",
                    isOutgoing = false,
                    messageType = ChatMessageType.CODE_SNIPPET.name,
                    content = "Binary search & _Master Theorem_ recurrence (`O(n log n)`):",
                    codeLanguage = "Kotlin",
                    codeContent = "fun binarySearch(a: IntArray, target: Int): Int {\n    var lo = 0; var hi = a.lastIndex\n    while (lo <= hi) {\n        val mid = (lo + hi) ushr 1\n        if (a[mid] == target) return mid\n        if (a[mid] < target) lo = mid + 1 else hi = mid - 1\n    }\n    return -1\n}",
                    latexFormula = "T(n) = 2T(n/2) + O(n) \\implies O(n \\log n)",
                    deliveryState = DeliveryState.READ.name,
                    createdAt = now - 360_000L
                ),
                // MATHGATE Formula & Note Bot
                MessageEntity(
                    id = "msg_seed_bot_1",
                    conversationId = "conv_bot_tutor",
                    senderId = "user_bot",
                    senderName = "MATHGATE Bot",
                    isOutgoing = false,
                    messageType = ChatMessageType.LATEX_EQUATION.name,
                    content = "Welcome to *MATHGATE Chats*! Format text with `*bold*`, `_italic_`, `~strike~`, or `` `code` ``, or send any math query.",
                    latexFormula = "A_v = -\\frac{R_f}{R_{in}}, \\quad P = V \\cdot I",
                    deliveryState = DeliveryState.READ.name,
                    createdAt = now - 480_000L
                )
            )
            dao.upsertMessages(messages)

            val poll = PollEntity(
                id = pollId,
                conversationId = "conv_group_ece",
                messageId = pollMsgId,
                question = "Which domain should our next weekend GATE mock cover?",
                allowMultiple = false,
                isAnonymous = false,
                createdAt = now - 120_000L
            )
            val pollOptions = listOf(
                PollOptionEntity("opt_ece_signals", pollId, "Signals & Laplace / Z-Transform", 0),
                PollOptionEntity("opt_ece_cmos", pollId, "CMOS VLSI & Digital Timing", 1),
                PollOptionEntity("opt_ece_control", pollId, "Control Systems & Bode Plots", 2),
                PollOptionEntity("opt_ece_math", pollId, "Engineering Mathematics & Linear Algebra", 3)
            )
            dao.upsertPoll(poll)
            dao.upsertPollOptions(pollOptions)
            dao.upsertPollVote(PollVoteEntity(pollId, "opt_ece_signals", "user_arjun"))
            dao.upsertPollVote(PollVoteEntity(pollId, "opt_ece_cmos", "user_priya"))

            dao.upsertPinnedMessage(PinnedMessageEntity("msg_seed_ece_1", "conv_group_ece"))
            dao.upsertStarredMessage(StarredMessageEntity("msg_seed_self_1", "conv_self"))
            dao.upsertChatSettings(ChatSettingsEntity())

            seeded = true
        }
    }

    suspend fun getAllConversations(): List<ConversationEntity> = dao.getAllConversations()

    suspend fun getMessagesForConversation(conversationId: String): List<MessageEntity> =
        dao.getMessagesForConversation(conversationId)

    private fun extractUrlMetadata(text: String): Triple<String, String, String>? {
        val regex = Regex("""(https?://[^\s]+|www\.[^\s]+)""", RegexOption.IGNORE_CASE)
        val match = regex.find(text)?.value ?: return null
        val fullUrl = if (match.startsWith("http", ignoreCase = true)) match else "https://$match"
        val host = fullUrl.substringAfter("://").substringBefore("/").lowercase()
        val title = when {
            host.contains("nptel") -> "NPTEL Engineering Lecture & Course Portal"
            host.contains("github") -> "GitHub · Source Code & Technical Repository"
            host.contains("arxiv") -> "arXiv · Open-Access Research Preprint"
            host.contains("wikipedia") -> "Wikipedia · Technical Reference Encyclopedia"
            else -> "Web Reference · ${host.uppercase()}"
        }
        return Triple(fullUrl, host, title)
    }

    suspend fun sendMessage(
        conversationId: String,
        content: String,
        messageType: ChatMessageType = ChatMessageType.PLAIN_TEXT,
        latexFormula: String? = null,
        codeLanguage: String? = null,
        codeContent: String? = null,
        sharedNoteId: String? = null,
        sharedNoteTitle: String? = null,
        questionPayload: SharedQuestionPayload? = null,
        replyToMessageId: String? = null,
        replyToSnippet: String? = null,
        forwardedFrom: String? = null,
        attachment: AttachmentEntity? = null,
        isViewOnce: Boolean = false,
        eventPayload: GroupEventPayload? = null,
        liveLocationExpiresAt: Long? = null
    ): MessageEntity {
        val now = System.currentTimeMillis()
        val msgId = "msg_${UUID.randomUUID().toString().take(10)}"
        val settings = dao.getChatSettings() ?: ChatSettingsEntity()
        val delivery = if (settings.readReceiptsEnabled) DeliveryState.READ.name else DeliveryState.DELIVERED.name

        if (attachment != null) {
            dao.upsertAttachment(attachment.copy(messageId = msgId, conversationId = conversationId))
        }

        val urlMeta = extractUrlMetadata(content)
        val resolvedType = if (urlMeta != null && messageType == ChatMessageType.PLAIN_TEXT) {
            ChatMessageType.SHARED_LINK
        } else {
            messageType
        }

        val message = MessageEntity(
            id = msgId,
            conversationId = conversationId,
            senderId = "user_self",
            senderName = "You",
            isOutgoing = true,
            messageType = resolvedType.name,
            content = content,
            latexFormula = latexFormula,
            codeLanguage = codeLanguage,
            codeContent = codeContent,
            sharedNoteId = sharedNoteId,
            sharedNoteTitle = sharedNoteTitle,
            questionId = questionPayload?.questionId,
            questionJson = questionPayload?.encodeToPipeString(),
            attachmentId = attachment?.id,
            replyToMessageId = replyToMessageId,
            replyToSnippet = replyToSnippet,
            forwardedFrom = forwardedFrom,
            isViewOnce = isViewOnce,
            isViewOnceOpened = false,
            linkPreviewUrl = urlMeta?.first,
            linkPreviewHost = urlMeta?.second,
            linkPreviewTitle = urlMeta?.third,
            eventPayloadJson = eventPayload?.encode(),
            liveLocationExpiresAt = liveLocationExpiresAt,
            deliveryState = delivery,
            createdAt = now
        )
        dao.upsertMessage(message)
        dao.upsertReceipt(MessageReceiptEntity(msgId, "user_self", delivery, now))
        dao.deleteDraft(conversationId)

        val conv = dao.getConversationById(conversationId)
        if (conv != null) {
            val preview = when {
                isViewOnce -> "1️⃣ View Once ${resolvedType.label}"
                resolvedType == ChatMessageType.GROUP_EVENT -> "📅 Event: ${eventPayload?.title ?: content}"
                resolvedType == ChatMessageType.LIVE_LOCATION -> "📍 Live Location (${content})"
                resolvedType == ChatMessageType.QUESTION_CARD -> "📐 Question: ${questionPayload?.topic ?: content}"
                resolvedType == ChatMessageType.LATEX_EQUATION -> "∑ Equation: ${latexFormula ?: content}"
                resolvedType == ChatMessageType.CODE_SNIPPET -> "💻 Code (${codeLanguage ?: "Kotlin"}): $content"
                resolvedType == ChatMessageType.NOTE_CARD -> "📓 Note: ${sharedNoteTitle ?: content}"
                resolvedType == ChatMessageType.VOICE_MESSAGE -> "🎙️ Voice message (${(attachment?.durationMs ?: 0L) / 1000}s)"
                resolvedType == ChatMessageType.POLL -> "📊 Poll: $content"
                else -> content
            }
            dao.upsertConversation(
                conv.copy(
                    lastMessagePreview = preview.take(90),
                    lastMessageAt = now,
                    unreadCount = 0
                )
            )
        }
        return message
    }

    suspend fun openViewOnceMessage(messageId: String) {
        val existing = dao.getMessageById(messageId) ?: return
        if (existing.isViewOnce && !existing.isViewOnceOpened) {
            dao.upsertMessage(existing.copy(isViewOnceOpened = true))
        }
    }

    suspend fun stopLiveLocation(messageId: String) {
        val existing = dao.getMessageById(messageId) ?: return
        dao.upsertMessage(
            existing.copy(
                liveLocationExpiresAt = System.currentTimeMillis() - 1000L,
                content = "📍 Live location ended"
            )
        )
    }

    suspend fun createGroupEvent(
        conversationId: String,
        title: String,
        dateTimeText: String,
        location: String,
        description: String
    ): MessageEntity {
        val payload = GroupEventPayload(
            eventId = "evt_${UUID.randomUUID().toString().take(8)}",
            title = title.trim(),
            dateTimeText = dateTimeText.trim(),
            location = location.trim(),
            description = description.trim(),
            rsvpByUser = mapOf("user_self" to "GOING")
        )
        return sendMessage(
            conversationId = conversationId,
            content = title.trim(),
            messageType = ChatMessageType.GROUP_EVENT,
            eventPayload = payload
        )
    }

    suspend fun rsvpToGroupEvent(messageId: String, rsvpStatus: String, userId: String = "user_self") {
        val existing = dao.getMessageById(messageId) ?: return
        val current = GroupEventPayload.decode(existing.eventPayloadJson) ?: return
        val updatedMap = current.rsvpByUser.toMutableMap().apply {
            put(userId, rsvpStatus)
        }
        val updatedPayload = current.copy(rsvpByUser = updatedMap)
        dao.upsertMessage(existing.copy(eventPayloadJson = updatedPayload.encode()))
    }

    suspend fun addGroupMember(conversationId: String, displayName: String, isAdmin: Boolean) {
        val trimmed = displayName.trim()
        if (trimmed.isEmpty()) return
        val userId = "member_${UUID.randomUUID().toString().take(6)}"
        dao.upsertMember(
            ConversationMemberEntity(
                conversationId = conversationId,
                userId = userId,
                displayName = trimmed,
                role = if (isAdmin) "ADMIN" else "MEMBER"
            )
        )
        insertSystemNotification(conversationId, "You added $trimmed to the group")
    }

    suspend fun toggleGroupMemberRole(conversationId: String, userId: String, currentRole: String, displayName: String) {
        val nextRole = if (currentRole == "ADMIN") "MEMBER" else "ADMIN"
        dao.upsertMember(
            ConversationMemberEntity(
                conversationId = conversationId,
                userId = userId,
                displayName = displayName,
                role = nextRole
            )
        )
        insertSystemNotification(
            conversationId,
            if (nextRole == "ADMIN") "$displayName is now a group admin" else "$displayName is no longer a group admin"
        )
    }

    suspend fun removeGroupMember(conversationId: String, userId: String, displayName: String) {
        dao.removeMember(conversationId, userId)
        insertSystemNotification(conversationId, "You removed $displayName from the group")
    }

    suspend fun toggleOnlyAdminsCanSend(conversationId: String) {
        val conv = dao.getConversationById(conversationId) ?: return
        val next = !conv.onlyAdminsCanSend
        dao.upsertConversation(conv.copy(onlyAdminsCanSend = next))
        insertSystemNotification(
            conversationId,
            if (next) "Changed group settings to allow only admins to send messages"
            else "Changed group settings to allow all participants to send messages"
        )
    }

    suspend fun resetGroupInviteLink(conversationId: String): String {
        val conv = dao.getConversationById(conversationId) ?: return ""
        val newCode = PeerPairingEngine.generateSixDigitOtp(System.nanoTime())
        val newLink = "mathgate://join/${conv.id.takeLast(4)}-$newCode"
        dao.upsertConversation(conv.copy(inviteLink = newLink, pairingCodeOtp = newCode))
        insertSystemNotification(conversationId, "You reset the group invite link")
        return newLink
    }

    suspend fun toggleLockConversation(conversationId: String): Boolean {
        val conv = dao.getConversationById(conversationId) ?: return false
        val next = !conv.isLocked
        dao.upsertConversation(conv.copy(isLocked = next))
        return next
    }

    suspend fun setDisappearingMessages(conversationId: String, hours: Int) {
        val conv = dao.getConversationById(conversationId) ?: return
        dao.upsertConversation(conv.copy(disappearingHours = hours))
        val label = when (hours) {
            24 -> "24 hours"
            168 -> "7 days"
            2160 -> "90 days"
            else -> "Off"
        }
        insertSystemNotification(
            conversationId,
            if (hours > 0) "⏱️ You turned on disappearing messages. New messages will disappear after $label."
            else "⏱️ You turned off disappearing messages."
        )
    }

    suspend fun toggleBlockConversation(conversationId: String): Boolean {
        val conv = dao.getConversationById(conversationId) ?: return false
        val next = !conv.isBlocked
        dao.upsertConversation(conv.copy(isBlocked = next))
        insertSystemNotification(
            conversationId,
            if (next) "🚫 You blocked this contact." else "You unblocked this contact."
        )
        return next
    }

    suspend fun reportConversation(conversationId: String) {
        val conv = dao.getConversationById(conversationId) ?: return
        dao.upsertConversation(conv.copy(isReported = true, isBlocked = true))
        insertSystemNotification(conversationId, "⚠️ Reported conversation & blocked sender locally.")
    }

    suspend fun insertSystemNotification(conversationId: String, text: String) {
        val now = System.currentTimeMillis()
        dao.upsertMessage(
            MessageEntity(
                id = "msg_sys_${UUID.randomUUID().toString().take(8)}",
                conversationId = conversationId,
                senderId = "system",
                senderName = "System",
                isOutgoing = false,
                messageType = ChatMessageType.SYSTEM_NOTIFICATION.name,
                content = text,
                deliveryState = DeliveryState.READ.name,
                createdAt = now
            )
        )
    }

    suspend fun insertIncomingReply(
        conversationId: String,
        senderId: String,
        senderName: String,
        content: String,
        messageType: ChatMessageType = ChatMessageType.PLAIN_TEXT,
        latexFormula: String? = null,
        sharedNoteTitle: String? = null,
        replyToMessageId: String? = null,
        replyToSnippet: String? = null
    ): MessageEntity {
        val now = System.currentTimeMillis() + 50L
        val msgId = "msg_${UUID.randomUUID().toString().take(10)}"
        val msg = MessageEntity(
            id = msgId,
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            isOutgoing = false,
            messageType = messageType.name,
            content = content,
            latexFormula = latexFormula,
            sharedNoteTitle = sharedNoteTitle,
            replyToMessageId = replyToMessageId,
            replyToSnippet = replyToSnippet,
            deliveryState = DeliveryState.READ.name,
            createdAt = now
        )
        dao.upsertMessage(msg)
        val conv = dao.getConversationById(conversationId)
        if (conv != null) {
            dao.upsertConversation(
                conv.copy(
                    lastMessagePreview = content.take(90),
                    lastMessageAt = now
                )
            )
        }
        return msg
    }

    suspend fun editMessage(messageId: String, newContent: String, newLatex: String? = null) {
        val existing = dao.getMessageById(messageId) ?: return
        if (!existing.isOutgoing) return
        val now = System.currentTimeMillis()
        dao.upsertMessage(
            existing.copy(
                content = newContent,
                latexFormula = newLatex ?: existing.latexFormula,
                editedAt = now
            )
        )
    }

    suspend fun deleteMessage(messageId: String, deleteForEveryone: Boolean) {
        val existing = dao.getMessageById(messageId) ?: return
        if (deleteForEveryone) {
            dao.upsertMessage(
                existing.copy(
                    content = "🚫 This message was deleted",
                    latexFormula = null,
                    codeContent = null,
                    questionJson = null,
                    deletedForEveryone = true,
                    editedAt = System.currentTimeMillis()
                )
            )
        } else {
            dao.upsertMessage(existing.copy(isDeleted = true))
        }
    }

    suspend fun toggleReaction(messageId: String, conversationId: String, emoji: String, userId: String = "user_self") {
        dao.upsertReaction(
            MessageReactionEntity(
                messageId = messageId,
                conversationId = conversationId,
                userId = userId,
                emoji = emoji
            )
        )
    }

    suspend fun toggleStarred(messageId: String, conversationId: String): Boolean {
        val existing = dao.getStarredMessage(messageId)
        return if (existing != null) {
            dao.deleteStarredMessage(messageId)
            false
        } else {
            dao.upsertStarredMessage(StarredMessageEntity(messageId, conversationId))
            true
        }
    }

    suspend fun togglePinned(messageId: String, conversationId: String): Boolean {
        val existing = dao.getPinnedMessage(messageId)
        return if (existing != null) {
            dao.deletePinnedMessage(messageId)
            false
        } else {
            dao.upsertPinnedMessage(PinnedMessageEntity(messageId, conversationId))
            true
        }
    }

    suspend fun saveDraft(conversationId: String, draftText: String, replyToMessageId: String? = null, replyToSnippet: String? = null) {
        if (draftText.isBlank() && replyToMessageId == null) {
            dao.deleteDraft(conversationId)
        } else {
            dao.upsertDraft(
                ChatDraftEntity(
                    conversationId = conversationId,
                    draftText = draftText,
                    replyToMessageId = replyToMessageId,
                    replyToSnippet = replyToSnippet
                )
            )
        }
    }

    suspend fun createPollMessage(
        conversationId: String,
        question: String,
        options: List<String>,
        allowMultiple: Boolean = false,
        isAnonymous: Boolean = false
    ): PollEntity {
        val now = System.currentTimeMillis()
        val pollId = "poll_${UUID.randomUUID().toString().take(8)}"
        val msgId = "msg_${UUID.randomUUID().toString().take(10)}"

        val poll = PollEntity(
            id = pollId,
            conversationId = conversationId,
            messageId = msgId,
            question = question,
            allowMultiple = allowMultiple,
            isAnonymous = isAnonymous,
            createdAt = now
        )
        val optionEntities = options.filter { it.isNotBlank() }.mapIndexed { index, text ->
            PollOptionEntity(
                id = "opt_${pollId}_$index",
                pollId = pollId,
                optionText = text.trim(),
                position = index
            )
        }
        dao.upsertPoll(poll)
        dao.upsertPollOptions(optionEntities)

        val pollMessage = MessageEntity(
            id = msgId,
            conversationId = conversationId,
            senderId = "user_self",
            senderName = "You",
            isOutgoing = true,
            messageType = ChatMessageType.POLL.name,
            content = question,
            pollId = pollId,
            deliveryState = DeliveryState.READ.name,
            createdAt = now
        )
        dao.upsertMessage(pollMessage)

        val conv = dao.getConversationById(conversationId)
        if (conv != null) {
            dao.upsertConversation(
                conv.copy(
                    lastMessagePreview = "📊 Poll: $question",
                    lastMessageAt = now + 1L
                )
            )
        }
        return poll
    }

    suspend fun voteOnPoll(pollId: String, optionId: String, allowMultiple: Boolean, userId: String = "user_self") {
        val currentVotes = dao.getUserVotesForPoll(pollId, userId)
        val alreadyVotedThis = currentVotes.any { it.optionId == optionId }
        if (!allowMultiple) {
            dao.clearUserVotesForPoll(pollId, userId)
            if (!alreadyVotedThis) {
                dao.upsertPollVote(PollVoteEntity(pollId, optionId, userId))
            }
        } else {
            if (alreadyVotedThis) {
                dao.deletePollVote(pollId, optionId, userId)
            } else {
                dao.upsertPollVote(PollVoteEntity(pollId, optionId, userId))
            }
        }
    }

    suspend fun createGroupOrBroadcast(
        title: String,
        description: String,
        isBroadcast: Boolean,
        memberNames: List<String>
    ): ConversationEntity {
        val now = System.currentTimeMillis()
        val id = if (isBroadcast) "conv_bcast_${UUID.randomUUID().toString().take(6)}"
        else "conv_grp_${UUID.randomUUID().toString().take(6)}"
        val initials = title.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("").ifEmpty { "GR" }
        val otp = PeerPairingEngine.generateSixDigitOtp(now)
        val type = if (isBroadcast) ConversationType.BROADCAST else ConversationType.GROUP

        val conv = ConversationEntity(
            id = id,
            title = title.trim(),
            subtitle = description.ifBlank { "${type.label} · ${memberNames.size + 1} members · Local-Only" },
            avatarInitials = initials,
            conversationType = type.name,
            linkedVia = "STUDY_GROUP",
            pairingCodeOtp = otp,
            inviteLink = "mathgate://join/${id.takeLast(4)}-$otp",
            lastSeenText = "You, ${memberNames.joinToString(", ")}",
            isPinned = true,
            lastMessagePreview = "Created ${type.label}: $title",
            lastMessageAt = now,
            announcementText = description.ifBlank { null }
        )
        dao.upsertConversation(conv)
        dao.upsertMember(ConversationMemberEntity(id, "user_self", "You", role = "ADMIN"))
        memberNames.forEachIndexed { idx, name ->
            dao.upsertMember(
                ConversationMemberEntity(
                    conversationId = id,
                    userId = "member_${id}_$idx",
                    displayName = name.trim(),
                    role = "MEMBER"
                )
            )
        }
        dao.upsertMessage(
            MessageEntity(
                id = "msg_sys_${UUID.randomUUID().toString().take(8)}",
                conversationId = id,
                senderId = "system",
                senderName = "System",
                isOutgoing = false,
                messageType = ChatMessageType.SYSTEM_NOTIFICATION.name,
                content = "You created ${type.label.lowercase()} \"${title.trim()}\" with ${memberNames.size + 1} participants.",
                deliveryState = DeliveryState.READ.name,
                createdAt = now
            )
        )
        return conv
    }

    suspend fun pairContactViaQrOrOtp(contact: ChatPeerContact): ConversationEntity {
        val now = System.currentTimeMillis()
        val convId = "conv_${contact.id}"
        val user = UserEntity(
            id = contact.id,
            displayName = contact.name,
            handle = contact.handle,
            avatarInitials = contact.avatarInitials,
            roleOrBranch = contact.roleOrBranch,
            pairingCodeOtp = contact.pairingCodeOtp,
            linkedVia = contact.linkedVia
        )
        dao.upsertUser(user)

        val conv = ConversationEntity(
            id = convId,
            title = contact.name,
            subtitle = "${contact.roleOrBranch} · OTP #${PeerPairingEngine.formatOtpDisplay(contact.pairingCodeOtp)}",
            avatarInitials = contact.avatarInitials,
            conversationType = if (contact.isGroup) ConversationType.GROUP.name else ConversationType.PERSONAL.name,
            linkedVia = contact.linkedVia,
            pairingCodeOtp = contact.pairingCodeOtp,
            lastSeenText = "online · paired via ${contact.linkedVia}",
            isPinned = true,
            unreadCount = 0,
            lastMessagePreview = "Verified ${contact.linkedVia} handshake (#${PeerPairingEngine.formatOtpDisplay(contact.pairingCodeOtp)})",
            lastMessageAt = now
        )
        dao.upsertConversation(conv)
        dao.upsertMessage(
            MessageEntity(
                id = "msg_pair_${UUID.randomUUID().toString().take(8)}",
                conversationId = convId,
                senderId = contact.id,
                senderName = contact.name,
                isOutgoing = false,
                messageType = ChatMessageType.SYSTEM_NOTIFICATION.name,
                content = "Connected with ${contact.name} via ${contact.linkedVia.replace("_", " ")} (OTP ${PeerPairingEngine.formatOtpDisplay(contact.pairingCodeOtp)}) · Local-Only Mode",
                deliveryState = DeliveryState.READ.name,
                createdAt = now
            )
        )
        return conv
    }

    suspend fun togglePinConversation(conversationId: String) {
        val conv = dao.getConversationById(conversationId) ?: return
        dao.upsertConversation(conv.copy(isPinned = !conv.isPinned))
    }

    suspend fun toggleArchiveConversation(conversationId: String) {
        val conv = dao.getConversationById(conversationId) ?: return
        dao.upsertConversation(conv.copy(isArchived = !conv.isArchived))
    }

    suspend fun toggleMuteConversation(conversationId: String) {
        val conv = dao.getConversationById(conversationId) ?: return
        dao.upsertConversation(conv.copy(isMuted = !conv.isMuted))
    }

    suspend fun markConversationRead(conversationId: String) {
        val conv = dao.getConversationById(conversationId) ?: return
        if (conv.unreadCount > 0) {
            dao.upsertConversation(conv.copy(unreadCount = 0))
        }
    }

    suspend fun clearConversationHistory(conversationId: String) {
        dao.clearConversationMessages(conversationId)
        val conv = dao.getConversationById(conversationId) ?: return
        dao.upsertConversation(
            conv.copy(
                lastMessagePreview = "Chat history cleared",
                unreadCount = 0
            )
        )
    }

    suspend fun updateChatSettings(transform: (ChatSettingsEntity) -> ChatSettingsEntity) {
        val current = dao.getChatSettings() ?: ChatSettingsEntity()
        dao.upsertChatSettings(transform(current).copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun searchMessages(
        query: String,
        conversationId: String? = null,
        messageType: String? = null
    ): List<MessageEntity> = dao.searchMessages(query.trim(), conversationId, messageType)

    suspend fun exportConversationAsMarkdown(conversationId: String): String {
        val conv = dao.getConversationById(conversationId)
        val messages = dao.getMessagesForConversation(conversationId)
        val sb = StringBuilder()
        sb.appendLine("# MATHGATE Chat Export — ${conv?.title ?: conversationId}")
        sb.appendLine("- Mode: Local-Only Offline Storage")
        sb.appendLine("- Total Messages: ${messages.size}")
        sb.appendLine()
        messages.forEach { msg ->
            val editedTag = if (msg.editedAt != null) " *(edited)*" else ""
            sb.appendLine("### ${msg.senderName} [${msg.messageType}]$editedTag")
            sb.appendLine(msg.content)
            if (!msg.latexFormula.isNullOrBlank()) {
                val d = '$'
                sb.appendLine("$d$d${msg.latexFormula}$d$d")
            }
            if (!msg.codeContent.isNullOrBlank()) {
                sb.appendLine("```${msg.codeLanguage ?: "kotlin"}")
                sb.appendLine(msg.codeContent)
                sb.appendLine("```")
            }
            sb.appendLine()
        }
        return sb.toString()
    }

    suspend fun syncCloudConversationsToLocal(cloudConvs: List<CloudConversationDto>) {
        if (cloudConvs.isEmpty()) return
        val existingById = dao.getAllConversations().associateBy { it.id }
        val toUpsert = cloudConvs.map { dto ->
            val prev = existingById[dto.id]
            val initials = dto.title.split(" ")
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .take(2)
                .joinToString("")
                .ifBlank { "CL" }
            val mappedType = when (dto.type) {
                "STUDY_GROUP" -> ConversationType.GROUP.name
                "PERSONAL_NOTES" -> ConversationType.SELF.name
                else -> prev?.conversationType ?: ConversationType.PERSONAL.name
            }
            ConversationEntity(
                id = dto.id,
                title = dto.title,
                subtitle = dto.subtitle.ifBlank { "Cloud Firestore · OTP #${dto.peerCode}" },
                avatarInitials = prev?.avatarInitials ?: initials,
                conversationType = mappedType,
                linkedVia = prev?.linkedVia ?: "CLOUD_FIRESTORE",
                pairingCodeOtp = dto.peerCode.ifBlank { prev?.pairingCodeOtp ?: "000000" },
                isPinned = dto.isPinned || (prev?.isPinned == true),
                isArchived = dto.isArchived,
                isMuted = dto.isMuted,
                isLocked = prev?.isLocked ?: false,
                disappearingHours = prev?.disappearingHours ?: 0,
                isBlocked = dto.isBlocked,
                isReported = prev?.isReported ?: false,
                onlyAdminsCanSend = prev?.onlyAdminsCanSend ?: false,
                groupRules = prev?.groupRules ?: "1. Keep discussions focused on technical problem solving.",
                inviteLink = prev?.inviteLink ?: "mathgate://join/${dto.peerCode}",
                lastSeenText = "☁️ Live Firestore Stream · OTP #${dto.peerCode}",
                isTyping = false,
                unreadCount = prev?.unreadCount ?: 0,
                lastMessagePreview = dto.lastMessagePreview.ifBlank { prev?.lastMessagePreview ?: "Cloud connected" },
                lastMessageAt = maxOf(dto.updatedAtMillis, prev?.lastMessageAt ?: 0L),
                wallpaperId = prev?.wallpaperId ?: "WHITE_BLUEPRINT",
                announcementText = prev?.announcementText,
                createdAt = prev?.createdAt ?: dto.createdAtMillis
            )
        }
        dao.upsertConversations(toUpsert)
    }

    suspend fun syncCloudMessagesToLocal(currentUid: String, cloudMessages: List<CloudMessageDto>) {
        if (cloudMessages.isEmpty()) return
        for (dto in cloudMessages) {
            val hasMediaOrCoords = !dto.attachmentBase64.isNullOrBlank() ||
                !dto.attachmentName.isNullOrBlank() ||
                !dto.attachmentUri.isNullOrBlank() ||
                (dto.latitude != null && dto.longitude != null)

            val attachmentId = if (hasMediaOrCoords) "att_${dto.id}" else null
            if (attachmentId != null) {
                val resolvedUri = when {
                    !dto.attachmentBase64.isNullOrBlank() ->
                        "data:${dto.attachmentMimeType ?: "image/jpeg"};base64,${dto.attachmentBase64}"
                    !dto.attachmentUri.isNullOrBlank() -> dto.attachmentUri
                    dto.latitude != null && dto.longitude != null ->
                        "geo:${dto.latitude},${dto.longitude}?q=${dto.latitude},${dto.longitude}(${dto.locationLabel ?: "Shared+Location"})"
                    else -> "cloud://attachment/${dto.id}"
                }
                val resolvedFileName = when {
                    !dto.attachmentName.isNullOrBlank() -> dto.attachmentName
                    dto.latitude != null && dto.longitude != null ->
                        "${dto.locationLabel ?: "GPS Location"} (${String.format(java.util.Locale.US, "%.5f, %.5f", dto.latitude, dto.longitude)})"
                    else -> "Attachment_${dto.id.takeLast(6)}"
                }
                dao.upsertAttachment(
                    AttachmentEntity(
                        id = attachmentId,
                        conversationId = dto.conversationId,
                        messageId = dto.id,
                        uriString = resolvedUri,
                        fileName = resolvedFileName,
                        mimeType = dto.attachmentMimeType ?: "application/octet-stream",
                        fileSizeBytes = dto.attachmentSizeBytes.coerceAtLeast(1024L),
                        transferState = "CLOUD_SYNCED ✓",
                        createdAt = dto.createdAtMillis
                    )
                )
            }

            val urlMeta = extractUrlMetadata(dto.content)
            val liveExpires = if (dto.liveLocationDurationMinutes > 0) {
                dto.createdAtMillis + dto.liveLocationDurationMinutes * 60_000L
            } else {
                null
            }

            val existing = dao.getMessageById(dto.id)
            val entity = MessageEntity(
                id = dto.id,
                conversationId = dto.conversationId,
                senderId = dto.senderId,
                senderName = dto.senderName,
                isOutgoing = dto.senderId == currentUid || dto.senderId == "user_self" || (existing?.isOutgoing == true),
                messageType = dto.messageType,
                content = if (dto.isDeletedForEveryone) "🚫 This message was deleted" else dto.content,
                latexFormula = dto.latexFormula ?: existing?.latexFormula,
                codeLanguage = existing?.codeLanguage,
                codeContent = existing?.codeContent,
                sharedNoteId = existing?.sharedNoteId,
                sharedNoteTitle = existing?.sharedNoteTitle,
                questionId = existing?.questionId,
                questionJson = existing?.questionJson,
                pollId = existing?.pollId,
                attachmentId = attachmentId ?: existing?.attachmentId,
                replyToMessageId = dto.replyToMessageId ?: existing?.replyToMessageId,
                replyToSnippet = dto.replyToPreview ?: existing?.replyToSnippet,
                forwardedFrom = existing?.forwardedFrom,
                isViewOnce = dto.isViewOnce,
                isViewOnceOpened = dto.isViewOnceOpened || (existing?.isViewOnceOpened == true),
                linkPreviewUrl = urlMeta?.first ?: existing?.linkPreviewUrl,
                linkPreviewHost = urlMeta?.second ?: existing?.linkPreviewHost,
                linkPreviewTitle = urlMeta?.third ?: existing?.linkPreviewTitle,
                eventPayloadJson = existing?.eventPayloadJson,
                liveLocationExpiresAt = liveExpires ?: existing?.liveLocationExpiresAt,
                deliveryState = dto.deliveryStatus,
                isDeleted = existing?.isDeleted ?: false,
                deletedForEveryone = dto.isDeletedForEveryone,
                createdAt = dto.createdAtMillis,
                editedAt = if (dto.isEdited) dto.updatedAtMillis else existing?.editedAt
            )
            dao.upsertMessage(entity)

            if (!dto.reactionEmoji.isNullOrBlank()) {
                dao.upsertReaction(
                    MessageReactionEntity(
                        messageId = dto.id,
                        conversationId = dto.conversationId,
                        userId = dto.senderId,
                        emoji = dto.reactionEmoji,
                        reactedAt = dto.updatedAtMillis
                    )
                )
            }
        }
    }

    suspend fun syncSharedRoomMessagesToLocal(
        conversationId: String,
        currentUid: String,
        roomMessages: List<SharedRoomMessageDto>
    ) {
        if (roomMessages.isEmpty()) return
        for (dto in roomMessages) {
            val hasMediaOrCoords = !dto.attachmentBase64.isNullOrBlank() ||
                !dto.attachmentName.isNullOrBlank() ||
                (dto.latitude != null && dto.longitude != null)
            val attachmentId = if (hasMediaOrCoords) "att_${dto.id}" else null
            if (attachmentId != null) {
                val resolvedUri = when {
                    !dto.attachmentBase64.isNullOrBlank() ->
                        "data:${dto.attachmentMimeType ?: "image/jpeg"};base64,${dto.attachmentBase64}"
                    dto.latitude != null && dto.longitude != null ->
                        "geo:${dto.latitude},${dto.longitude}?q=${dto.latitude},${dto.longitude}(${dto.locationLabel ?: "Shared+Location"})"
                    else -> "cloud://room_attachment/${dto.id}"
                }
                val resolvedFileName = when {
                    !dto.attachmentName.isNullOrBlank() -> dto.attachmentName
                    dto.latitude != null && dto.longitude != null ->
                        "${dto.locationLabel ?: "GPS Location"} (${String.format(java.util.Locale.US, "%.5f, %.5f", dto.latitude, dto.longitude)})"
                    else -> "RoomAttachment_${dto.id.takeLast(6)}"
                }
                dao.upsertAttachment(
                    AttachmentEntity(
                        id = attachmentId,
                        conversationId = conversationId,
                        messageId = dto.id,
                        uriString = resolvedUri,
                        fileName = resolvedFileName,
                        mimeType = dto.attachmentMimeType ?: "application/octet-stream",
                        fileSizeBytes = dto.attachmentSizeBytes.coerceAtLeast(1024L),
                        transferState = "CLOUD_SYNCED ✓",
                        createdAt = dto.createdAtMillis
                    )
                )
            }

            val existing = dao.getMessageById(dto.id)
            val liveExpires = if (dto.liveLocationDurationMinutes > 0) {
                dto.createdAtMillis + dto.liveLocationDurationMinutes * 60_000L
            } else {
                null
            }
            val entity = MessageEntity(
                id = dto.id,
                conversationId = conversationId,
                senderId = dto.senderUid,
                senderName = if (dto.senderUid == currentUid) "You" else dto.senderName,
                isOutgoing = dto.senderUid == currentUid,
                messageType = dto.messageType,
                content = dto.content,
                latexFormula = dto.latexFormula ?: existing?.latexFormula,
                attachmentId = attachmentId ?: existing?.attachmentId,
                replyToMessageId = dto.replyToMessageId ?: existing?.replyToMessageId,
                replyToSnippet = dto.replyToPreview ?: existing?.replyToSnippet,
                liveLocationExpiresAt = liveExpires ?: existing?.liveLocationExpiresAt,
                deliveryState = dto.deliveryStatus,
                createdAt = dto.createdAtMillis
            )
            dao.upsertMessage(entity)
        }
    }
}
