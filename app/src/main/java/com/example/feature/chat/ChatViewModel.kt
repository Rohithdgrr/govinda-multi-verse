package com.example.feature.chat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.AttachmentEntity
import com.example.core.database.ChatDraftEntity
import com.example.core.database.ChatMessageType
import com.example.core.database.ChatSettingsEntity
import com.example.core.database.ConversationEntity
import com.example.core.database.ConversationMemberEntity
import com.example.core.database.ConversationType
import com.example.core.database.DeliveryState
import com.example.core.database.MessageEntity
import com.example.core.database.MessageReactionEntity
import com.example.core.database.NoteBlockEntity
import com.example.core.database.NoteEntity
import com.example.core.database.PollEntity
import com.example.core.database.PollOptionEntity
import com.example.core.database.PollVoteEntity
import com.example.core.database.UserEntity
import com.example.domain.model.MathQuestion
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val conversations: List<ConversationEntity> = emptyList(),
    val users: List<UserEntity> = emptyList(),
    val allMessages: List<MessageEntity> = emptyList(),
    val members: List<ConversationMemberEntity> = emptyList(),
    val attachments: Map<String, AttachmentEntity> = emptyMap(),
    val reactionsByMessageId: Map<String, List<MessageReactionEntity>> = emptyMap(),
    val starredIds: Set<String> = emptySet(),
    val pinnedIds: Set<String> = emptySet(),
    val draftsByConversationId: Map<String, ChatDraftEntity> = emptyMap(),
    val pollsById: Map<String, PollEntity> = emptyMap(),
    val pollOptionsByPollId: Map<String, List<PollOptionEntity>> = emptyMap(),
    val pollVotesByPollId: Map<String, List<PollVoteEntity>> = emptyMap(),
    val settings: ChatSettingsEntity = ChatSettingsEntity(),
    val activeConversationId: String = "conv_group_ece",
    val selectedFilterTab: String = "ALL", // "ALL", "UNREAD", "GROUPS", "PERSONAL", "SAVED", "ARCHIVED", "LOCKED"
    val isLockedFolderUnlocked: Boolean = false,
    val conversationSearchQuery: String = "",
    val messageSearchQuery: String = "",
    val selectedMessageTypeFilter: String? = null,
    val showStarredOnly: Boolean = false,
    val showPinnedOnly: Boolean = false,
    val myDeviceOtp: String = PeerPairingEngine.generateSixDigitOtp(20261114L),
    val myDeviceId: String = "MG-DEV-094",
    val cloudUser: CloudAuthUser? = null,
    val isCloudStreamingActive: Boolean = false,
    val isSigningInWithGoogle: Boolean = false,
    val lastLocationSummary: String? = null,
    val statusBanner: String? = null
) {
    val activeConversation: ConversationEntity?
        get() = conversations.firstOrNull { it.id == activeConversationId } ?: conversations.firstOrNull()

    val activeConversationMembers: List<ConversationMemberEntity>
        get() {
            val convId = activeConversation?.id ?: activeConversationId
            return members.filter { it.conversationId == convId }
        }

    val activeMessages: List<MessageEntity>
        get() {
            val convId = activeConversation?.id ?: activeConversationId
            val base = allMessages.filter { it.conversationId == convId && !it.isDeleted }
            return base.filter { msg ->
                val matchesStar = !showStarredOnly || msg.id in starredIds
                val matchesPin = !showPinnedOnly || msg.id in pinnedIds
                val matchesType = selectedMessageTypeFilter == null || msg.messageType == selectedMessageTypeFilter
                val matchesSearch = messageSearchQuery.isBlank() ||
                    msg.content.contains(messageSearchQuery, ignoreCase = true) ||
                    msg.senderName.contains(messageSearchQuery, ignoreCase = true) ||
                    (msg.latexFormula?.contains(messageSearchQuery, ignoreCase = true) == true) ||
                    (msg.sharedNoteTitle?.contains(messageSearchQuery, ignoreCase = true) == true)
                matchesStar && matchesPin && matchesType && matchesSearch
            }
        }
}

class ChatViewModel(
    private val repository: ChatRepository,
    private val cloudRepository: CloudChatRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var cloudConversationsJob: Job? = null
    private var cloudMessagesJob: Job? = null
    private var sharedRoomStreamJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            refreshCloudAuthState()
        }

        viewModelScope.launch {
            combine(
                repository.conversationsFlow,
                repository.allMessagesFlow,
                repository.allMembersFlow,
                repository.allAttachmentsFlow,
                repository.allReactionsFlow
            ) { convs, msgs, members, attachments, reactions ->
                Quintuple(convs, msgs, members, attachments, reactions)
            }.collect { (convs, msgs, members, attachments, reactions) ->
                _uiState.update { current ->
                    val resolvedActive = if (convs.any { it.id == current.activeConversationId }) {
                        current.activeConversationId
                    } else {
                        convs.firstOrNull()?.id ?: "conv_group_ece"
                    }
                    current.copy(
                        conversations = convs,
                        allMessages = msgs,
                        members = members,
                        attachments = attachments.associateBy { it.id },
                        reactionsByMessageId = reactions.groupBy { it.messageId },
                        activeConversationId = resolvedActive
                    )
                }
            }
        }

        viewModelScope.launch {
            combine(
                repository.starredFlow,
                repository.pinnedFlow,
                repository.draftsFlow,
                repository.settingsFlow,
                repository.usersFlow
            ) { starred, pinned, drafts, settings, users ->
                Quintuple(starred, pinned, drafts, settings, users)
            }.collect { (starred, pinned, drafts, settings, users) ->
                _uiState.update { current ->
                    current.copy(
                        starredIds = starred.map { it.messageId }.toSet(),
                        pinnedIds = pinned.map { it.messageId }.toSet(),
                        draftsByConversationId = drafts.associateBy { it.conversationId },
                        settings = settings ?: ChatSettingsEntity(),
                        users = users
                    )
                }
            }
        }

        viewModelScope.launch {
            combine(
                repository.pollsFlow,
                repository.pollOptionsFlow,
                repository.pollVotesFlow
            ) { polls, options, votes ->
                Triple(polls, options, votes)
            }.collect { (polls, options, votes) ->
                _uiState.update { current ->
                    current.copy(
                        pollsById = polls.associateBy { it.id },
                        pollOptionsByPollId = options.groupBy { it.pollId },
                        pollVotesByPollId = votes.groupBy { it.pollId }
                    )
                }
            }
        }
    }

    // =========================================================================
    // GOOGLE SIGN-IN & REAL-TIME FIRESTORE STREAM LIFECYCLE
    // =========================================================================

    fun refreshCloudAuthState() {
        val currentUser = GoogleAuthManager.currentCloudUser()
        if (currentUser != null) {
            _uiState.update {
                it.copy(
                    cloudUser = currentUser,
                    isCloudStreamingActive = cloudRepository != null
                )
            }
            startCloudStreaming(currentUser)
        } else {
            cancelAllCloudListeners()
            _uiState.update {
                it.copy(
                    cloudUser = null,
                    isCloudStreamingActive = false
                )
            }
        }
    }

    fun attemptAutoGoogleSignIn(activityContext: Context) {
        if (_uiState.value.cloudUser != null || _uiState.value.isSigningInWithGoogle) return
        viewModelScope.launch {
            val result = GoogleAuthManager.attemptAutoSignIn(activityContext)
            val user = result.getOrNull()
            if (user != null) {
                _uiState.update {
                    it.copy(
                        cloudUser = user,
                        isCloudStreamingActive = cloudRepository != null,
                        statusBanner = "☁️ Connected to Firestore as ${user.displayName}"
                    )
                }
                startCloudStreaming(user)
            }
        }
    }

    fun signInWithGoogle(activityContext: Context) {
        if (_uiState.value.isSigningInWithGoogle) return
        _uiState.update { it.copy(isSigningInWithGoogle = true) }
        viewModelScope.launch {
            val result = GoogleAuthManager.onGoogleSignInClicked(activityContext)
            _uiState.update { it.copy(isSigningInWithGoogle = false) }
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        cloudUser = user,
                        isCloudStreamingActive = cloudRepository != null,
                        statusBanner = "☁️ Live Firestore Chat enabled for ${user.displayName}"
                    )
                }
                startCloudStreaming(user)
            }.onFailure { err ->
                postStatusBanner("Google Sign-In not completed: ${err.localizedMessage ?: "Check Google account on device"}")
            }
        }
    }

    fun signOutFromCloud(context: Context) {
        viewModelScope.launch {
            // Cancel all Firestore snapshot listeners BEFORE signing out so no listener throws PERMISSION_DENIED
            cancelAllCloudListeners()
            GoogleAuthManager.signOut(context)
            _uiState.update {
                it.copy(
                    cloudUser = null,
                    isCloudStreamingActive = false,
                    statusBanner = "Signed out of Cloud Firestore · Local Room mode active"
                )
            }
        }
    }

    private fun cancelAllCloudListeners() {
        cloudConversationsJob?.cancel()
        cloudConversationsJob = null
        cloudMessagesJob?.cancel()
        cloudMessagesJob = null
        sharedRoomStreamJob?.cancel()
        sharedRoomStreamJob = null
    }

    private fun startCloudStreaming(user: CloudAuthUser) {
        val cloudRepo = cloudRepository ?: return
        cancelAllCloudListeners()

        // 1. Seed current conversations to user's Firestore space & listen for real-time updates
        cloudConversationsJob = viewModelScope.launch {
            runCatching {
                val localConvs = repository.getAllConversations()
                for (conv in localConvs.take(5)) {
                    val mappedType = when (conv.conversationType) {
                        ConversationType.GROUP.name -> "STUDY_GROUP"
                        ConversationType.SELF.name -> "PERSONAL_NOTES"
                        else -> "DIRECT_PEER"
                    }
                    cloudRepo.upsertCloudConversation(
                        userId = user.uid,
                        conversationId = conv.id,
                        title = conv.title,
                        type = mappedType,
                        peerCode = conv.pairingCodeOtp.ifBlank { "000000" },
                        lastMessagePreview = conv.lastMessagePreview.ifBlank { "Cloud Connected" },
                        subtitle = conv.subtitle,
                        isPinned = conv.isPinned
                    )
                }
            }
            runCatching {
                cloudRepo.observeCloudConversations(user.uid).collect { cloudConvs ->
                    repository.syncCloudConversationsToLocal(cloudConvs)
                }
            }
        }

        // 2. Listen to all user-scoped real-time messages
        cloudMessagesJob = viewModelScope.launch {
            runCatching {
                cloudRepo.observeAllUserMessages(user.uid).collect { cloudMsgs ->
                    repository.syncCloudMessagesToLocal(user.uid, cloudMsgs)
                }
            }
        }

        // 3. Listen to active conversation's shared OTP/QR room stream
        observeActiveSharedRoom(user)
    }

    private fun observeActiveSharedRoom(user: CloudAuthUser) {
        val cloudRepo = cloudRepository ?: return
        sharedRoomStreamJob?.cancel()
        val activeConv = _uiState.value.activeConversation ?: return
        val otp = activeConv.pairingCodeOtp.filter { it.isDigit() }
        if (otp.isBlank() || otp == "000000") return
        val roomId = "room_$otp"
        val convId = activeConv.id

        sharedRoomStreamJob = viewModelScope.launch {
            runCatching {
                cloudRepo.observeSharedRoomMessages(roomId = roomId, currentUid = user.uid).collect { roomMsgs ->
                    repository.syncSharedRoomMessagesToLocal(
                        conversationId = convId,
                        currentUid = user.uid,
                        roomMessages = roomMsgs
                    )
                }
            }
        }
    }

    fun selectConversation(conversationId: String) {
        _uiState.update {
            it.copy(
                activeConversationId = conversationId,
                messageSearchQuery = "",
                selectedMessageTypeFilter = null,
                showStarredOnly = false,
                showPinnedOnly = false
            )
        }
        viewModelScope.launch {
            repository.markConversationRead(conversationId)
            _uiState.value.cloudUser?.let { observeActiveSharedRoom(it) }
        }
    }

    fun setFilterTab(tab: String) {
        _uiState.update { it.copy(selectedFilterTab = tab) }
    }

    fun unlockLockedFolderWithPin(enteredPin: String): Boolean {
        val expected = _uiState.value.settings.chatLockPin.ifBlank { "1234" }
        return if (enteredPin.trim() == expected) {
            _uiState.update {
                it.copy(
                    isLockedFolderUnlocked = true,
                    selectedFilterTab = "LOCKED",
                    statusBanner = "Unlocked Locked Chats folder"
                )
            }
            true
        } else {
            false
        }
    }

    fun setConversationSearchQuery(query: String) {
        _uiState.update { it.copy(conversationSearchQuery = query) }
    }

    fun setMessageSearchQuery(query: String) {
        _uiState.update { it.copy(messageSearchQuery = query) }
    }

    fun setMessageTypeFilter(type: String?) {
        _uiState.update { it.copy(selectedMessageTypeFilter = type) }
    }

    fun toggleShowStarredOnly() {
        _uiState.update { it.copy(showStarredOnly = !it.showStarredOnly) }
    }

    fun toggleShowPinnedOnly() {
        _uiState.update { it.copy(showPinnedOnly = !it.showPinnedOnly) }
    }

    fun regenerateDeviceOtp() {
        _uiState.update {
            val next = PeerPairingEngine.generateSixDigitOtp(System.nanoTime())
            it.copy(
                myDeviceOtp = next,
                statusBanner = "Generated new 6-Digit Pairing OTP: ${PeerPairingEngine.formatOtpDisplay(next)}"
            )
        }
    }

    fun clearStatusBanner() {
        _uiState.update { it.copy(statusBanner = null) }
    }

    fun postStatusBanner(msg: String) {
        _uiState.update { it.copy(statusBanner = msg) }
    }

    fun updateDraft(conversationId: String, text: String, replyId: String? = null, replySnippet: String? = null) {
        viewModelScope.launch {
            repository.saveDraft(conversationId, text, replyId, replySnippet)
        }
    }

    fun sendMessage(
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
        liveLocationExpiresAt: Long? = null,
        currentQuestion: MathQuestion? = null,
        allNotes: List<NoteEntity> = emptyList(),
        activeNote: NoteEntity? = null,
        activeBlocks: List<NoteBlockEntity> = emptyList(),
        targetConversationId: String? = null,
        cloudAttachmentBase64: String? = null,
        cloudLatitude: Double? = null,
        cloudLongitude: Double? = null,
        cloudLocationLabel: String? = null,
        cloudLiveDurationMinutes: Int? = null
    ) {
        val conv = _uiState.value.activeConversation
        if (targetConversationId == null && conv?.isBlocked == true) {
            postStatusBanner("Unblock ${conv.title} to send messages.")
            return
        }
        val convId = targetConversationId ?: conv?.id ?: return
        viewModelScope.launch {
            val sent = repository.sendMessage(
                conversationId = convId,
                content = content,
                messageType = messageType,
                latexFormula = latexFormula,
                codeLanguage = codeLanguage,
                codeContent = codeContent,
                sharedNoteId = sharedNoteId,
                sharedNoteTitle = sharedNoteTitle,
                questionPayload = questionPayload,
                replyToMessageId = replyToMessageId,
                replyToSnippet = replyToSnippet,
                forwardedFrom = forwardedFrom,
                attachment = attachment,
                isViewOnce = isViewOnce,
                liveLocationExpiresAt = liveLocationExpiresAt
            )

            // Push to Cloud Firestore when authenticated
            val user = _uiState.value.cloudUser
            val cloudRepo = cloudRepository
            if (user != null && cloudRepo != null) {
                runCatching {
                    cloudRepo.sendCloudMessage(
                        userId = user.uid,
                        conversationId = convId,
                        senderId = user.uid,
                        senderName = user.displayName,
                        content = content,
                        messageType = sent.messageType,
                        deliveryStatus = DeliveryState.DELIVERED.name,
                        messageId = sent.id,
                        attachmentUri = attachment?.uriString?.takeIf { !it.startsWith("data:") },
                        attachmentName = attachment?.fileName,
                        attachmentMimeType = attachment?.mimeType,
                        attachmentSizeBytes = attachment?.fileSizeBytes,
                        attachmentBase64 = cloudAttachmentBase64,
                        latitude = cloudLatitude,
                        longitude = cloudLongitude,
                        locationLabel = cloudLocationLabel,
                        liveLocationDurationMinutes = cloudLiveDurationMinutes,
                        latexFormula = latexFormula,
                        replyToMessageId = replyToMessageId,
                        replyToPreview = replyToSnippet,
                        isViewOnce = isViewOnce
                    )

                    val targetConv = _uiState.value.conversations.firstOrNull { it.id == convId }
                    val otp = targetConv?.pairingCodeOtp?.filter { it.isDigit() }.orEmpty()
                    if (otp.isNotBlank() && otp != "000000") {
                        cloudRepo.sendSharedRoomMessage(
                            roomId = "room_$otp",
                            senderUid = user.uid,
                            senderName = user.displayName,
                            participantUids = listOf(user.uid),
                            content = content,
                            messageType = sent.messageType,
                            messageId = "r_${sent.id}",
                            attachmentName = attachment?.fileName,
                            attachmentMimeType = attachment?.mimeType,
                            attachmentSizeBytes = attachment?.fileSizeBytes,
                            attachmentBase64 = cloudAttachmentBase64,
                            latitude = cloudLatitude,
                            longitude = cloudLongitude,
                            locationLabel = cloudLocationLabel,
                            liveLocationDurationMinutes = cloudLiveDurationMinutes,
                            latexFormula = latexFormula
                        )
                    }
                }
            }

            // If sending a text query to the offline MATHGATE Formula & Note Bot or Study Group, generate a response
            if ((convId == "conv_bot_tutor" || convId == "conv_group_ece") &&
                messageType == ChatMessageType.PLAIN_TEXT &&
                !isViewOnce
            ) {
                val reply = OfflineTutorChatEngine.generateReply(
                    rawInput = content,
                    currentQuestion = currentQuestion,
                    allNotes = allNotes,
                    activeNote = activeNote,
                    activeBlocks = activeBlocks
                )
                val responderName = if (convId == "conv_bot_tutor") "MATHGATE Bot" else "Arjun Sharma"
                val responderId = if (convId == "conv_bot_tutor") "user_bot" else "user_arjun"
                repository.insertIncomingReply(
                    conversationId = convId,
                    senderId = responderId,
                    senderName = responderName,
                    content = reply.text,
                    messageType = if (reply.latexAttachment != null) ChatMessageType.LATEX_EQUATION else ChatMessageType.PLAIN_TEXT,
                    latexFormula = reply.latexAttachment,
                    sharedNoteTitle = reply.sourceBadge,
                    replyToMessageId = sent.id,
                    replyToSnippet = content.take(55)
                )
            }
        }
    }

    /**
     * Reads a real image/photo from Android's ContentResolver, downscales & compresses it to
     * JPEG Base64, renders the real Bitmap in the chat thread, and streams it over Firestore.
     */
    fun sendImageFromUri(
        context: Context,
        uriString: String,
        fallbackFileName: String,
        caption: String = "",
        isViewOnce: Boolean = false
    ) {
        val convId = _uiState.value.activeConversation?.id ?: return
        viewModelScope.launch {
            val parsedUri = runCatching { Uri.parse(uriString) }.getOrNull()
            val encoded = if (parsedUri != null && (uriString.startsWith("content://") || uriString.startsWith("file://"))) {
                CloudChatRepository.encodeImageUriToBase64(context, parsedUri)
            } else {
                null
            }

            val resolvedName = encoded?.displayName?.ifBlank { fallbackFileName } ?: fallbackFileName
            val resolvedMime = encoded?.mimeType ?: "image/jpeg"
            val resolvedSize = encoded?.sizeBytes ?: 184_320L
            val base64 = encoded?.base64Data
            val resolvedUri = if (!base64.isNullOrBlank()) {
                "data:$resolvedMime;base64,$base64"
            } else {
                encoded?.localCachedUri ?: uriString
            }

            val attId = "att_img_${UUID.randomUUID().toString().take(8)}"
            val attachment = AttachmentEntity(
                id = attId,
                conversationId = convId,
                messageId = "",
                uriString = resolvedUri,
                fileName = resolvedName,
                mimeType = resolvedMime,
                fileSizeBytes = resolvedSize,
                transferState = if (_uiState.value.cloudUser != null) "CLOUD_STREAMED ✓" else "STORED_LOCALLY"
            )
            val body = if (caption.isNotBlank()) caption.trim() else "📷 Photo: $resolvedName"
            sendMessage(
                content = body,
                messageType = ChatMessageType.IMAGE,
                attachment = attachment,
                isViewOnce = isViewOnce,
                cloudAttachmentBase64 = base64
            )
            postStatusBanner(
                if (_uiState.value.cloudUser != null) "☁️ Streamed photo $resolvedName to Firestore"
                else "Sent photo $resolvedName"
            )
        }
    }

    /**
     * Reads a real document/PDF/audio/file from Android's ContentResolver, extracts real
     * file name, byte size, MIME type, and Base64 preview bytes, and streams over Firestore.
     */
    fun sendDocumentFromUri(
        context: Context,
        uriString: String,
        fallbackFileName: String,
        fallbackMimeType: String = "application/pdf",
        messageType: ChatMessageType = ChatMessageType.PDF,
        caption: String = ""
    ) {
        val convId = _uiState.value.activeConversation?.id ?: return
        viewModelScope.launch {
            val parsedUri = runCatching { Uri.parse(uriString) }.getOrNull()
            val encoded = if (parsedUri != null && (uriString.startsWith("content://") || uriString.startsWith("file://"))) {
                CloudChatRepository.encodeFileUriPayload(context, parsedUri, fallbackMimeType)
            } else {
                null
            }

            val resolvedName = encoded?.displayName?.ifBlank { fallbackFileName } ?: fallbackFileName
            val resolvedMime = encoded?.mimeType ?: fallbackMimeType
            val resolvedSize = encoded?.sizeBytes ?: 320_000L
            val base64 = encoded?.base64Data
            val resolvedUri = if (!base64.isNullOrBlank()) {
                "data:$resolvedMime;base64,$base64"
            } else {
                encoded?.localCachedUri ?: uriString
            }

            val attId = "att_doc_${UUID.randomUUID().toString().take(8)}"
            val attachment = AttachmentEntity(
                id = attId,
                conversationId = convId,
                messageId = "",
                uriString = resolvedUri,
                fileName = resolvedName,
                mimeType = resolvedMime,
                fileSizeBytes = resolvedSize,
                transferState = if (_uiState.value.cloudUser != null) "CLOUD_STREAMED ✓" else "STORED_LOCALLY"
            )
            val body = if (caption.isNotBlank()) caption.trim() else "📄 ${messageType.label}: $resolvedName"
            sendMessage(
                content = body,
                messageType = messageType,
                attachment = attachment,
                cloudAttachmentBase64 = base64
            )
            postStatusBanner(
                if (_uiState.value.cloudUser != null) "☁️ Streamed file $resolvedName (${resolvedSize / 1024} KB) to Firestore"
                else "Attached file $resolvedName (${resolvedSize / 1024} KB)"
            )
        }
    }

    /**
     * Resolves real GPS / Network location coordinates from Android's LocationManager
     * and streams the live/static coordinates to Room and Firestore.
     */
    fun sendRealDeviceLocation(
        context: Context,
        durationMinutes: Int,
        customPlaceLabel: String = "Campus Engineering Library · Wing B"
    ) {
        val convId = _uiState.value.activeConversation?.id ?: return
        val fix = CloudChatRepository.resolveRealDeviceLocation(context)
        val coordsFormatted = String.format(Locale.US, "%.5f, %.5f", fix.latitude, fix.longitude)
        val fullLabel = if (customPlaceLabel.isNotBlank()) {
            "$customPlaceLabel · GPS ($coordsFormatted)"
        } else {
            "GPS Coordinates ($coordsFormatted) · ${fix.providerLabel}"
        }
        val expiresAt = if (durationMinutes > 0) {
            System.currentTimeMillis() + durationMinutes * 60_000L
        } else {
            null
        }
        val attId = "att_loc_${UUID.randomUUID().toString().take(8)}"
        val attachment = AttachmentEntity(
            id = attId,
            conversationId = convId,
            messageId = "",
            uriString = "geo:${fix.latitude},${fix.longitude}?q=${fix.latitude},${fix.longitude}",
            fileName = fullLabel,
            mimeType = "application/vnd.geo+json",
            fileSizeBytes = 2048L,
            transferState = if (_uiState.value.cloudUser != null) "CLOUD_GPS_LIVE ✓" else "GPS_LOCKED ✓"
        )
        _uiState.update { it.copy(lastLocationSummary = "$coordsFormatted (${fix.providerLabel})") }

        val msgType = if (durationMinutes > 0) ChatMessageType.LIVE_LOCATION else ChatMessageType.LOCATION
        val body = if (durationMinutes > 0) {
            "📍 $fullLabel (Live for $durationMinutes min)"
        } else {
            "📍 Shared Location: $fullLabel"
        }

        sendMessage(
            content = body,
            messageType = msgType,
            attachment = attachment,
            liveLocationExpiresAt = expiresAt,
            cloudLatitude = fix.latitude,
            cloudLongitude = fix.longitude,
            cloudLocationLabel = fullLabel,
            cloudLiveDurationMinutes = durationMinutes
        )
        postStatusBanner("📍 Shared real GPS coordinates ($coordsFormatted)")
    }

    fun sendVoiceNote(durationMs: Long, waveformCsv: String) {
        val convId = _uiState.value.activeConversation?.id ?: return
        val attId = "att_voice_${UUID.randomUUID().toString().take(8)}"
        val secs = (durationMs / 1000L).coerceAtLeast(1L)
        val attachment = AttachmentEntity(
            id = attId,
            conversationId = convId,
            messageId = "",
            uriString = "local://voice/$attId.m4a",
            fileName = "VoiceNote_${secs}s.m4a",
            mimeType = "audio/mp4",
            fileSizeBytes = secs * 16_400L,
            durationMs = durationMs.coerceAtLeast(1000L),
            waveformCsv = waveformCsv,
            transferState = if (_uiState.value.cloudUser != null) "CLOUD_STREAMED ✓" else "STORED_LOCALLY"
        )
        sendMessage(
            content = "🎙️ Voice message (${secs}s)",
            messageType = ChatMessageType.VOICE_MESSAGE,
            attachment = attachment
        )
        postStatusBanner("Recorded & sent ${secs}s voice message.")
    }

    fun sendMediaOrDocumentAttachment(
        uriString: String,
        fileName: String,
        mimeType: String,
        fileSizeBytes: Long,
        messageType: ChatMessageType,
        caption: String = "",
        isViewOnce: Boolean = false
    ) {
        val convId = _uiState.value.activeConversation?.id ?: return
        val attId = "att_doc_${UUID.randomUUID().toString().take(8)}"
        val attachment = AttachmentEntity(
            id = attId,
            conversationId = convId,
            messageId = "",
            uriString = uriString,
            fileName = fileName,
            mimeType = mimeType,
            fileSizeBytes = fileSizeBytes,
            transferState = if (_uiState.value.cloudUser != null) "CLOUD_STREAMED ✓" else "STORED_LOCALLY"
        )
        val body = if (caption.isNotBlank()) caption.trim() else "${messageType.label}: $fileName"
        sendMessage(
            content = body,
            messageType = messageType,
            attachment = attachment,
            isViewOnce = isViewOnce
        )
        postStatusBanner(if (isViewOnce) "Sent View Once $fileName" else "Sent $fileName")
    }

    fun openViewOnceMessage(messageId: String) {
        viewModelScope.launch {
            repository.openViewOnceMessage(messageId)
            val user = _uiState.value.cloudUser
            val cloudRepo = cloudRepository
            if (user != null && cloudRepo != null) {
                runCatching {
                    cloudRepo.updateCloudMessageFlags(user.uid, messageId, isViewOnceOpened = true)
                }
            }
        }
    }

    fun sendLiveLocation(durationMinutes: Int, label: String = "Campus Engineering Library · Wing B") {
        val expiresAt = System.currentTimeMillis() + durationMinutes * 60_000L
        sendMessage(
            content = "$label (Live for $durationMinutes min)",
            messageType = ChatMessageType.LIVE_LOCATION,
            liveLocationExpiresAt = expiresAt,
            cloudLatitude = 37.421998,
            cloudLongitude = -122.084000,
            cloudLocationLabel = label,
            cloudLiveDurationMinutes = durationMinutes
        )
        postStatusBanner("Sharing live location for $durationMinutes minutes")
    }

    fun stopLiveLocation(messageId: String) {
        viewModelScope.launch {
            repository.stopLiveLocation(messageId)
            postStatusBanner("Stopped sharing live location")
        }
    }

    fun createGroupEvent(title: String, dateTimeText: String, location: String, description: String) {
        val convId = _uiState.value.activeConversation?.id ?: return
        viewModelScope.launch {
            val created = repository.createGroupEvent(convId, title, dateTimeText, location, description)
            val user = _uiState.value.cloudUser
            val cloudRepo = cloudRepository
            if (user != null && cloudRepo != null) {
                runCatching {
                    cloudRepo.sendCloudMessage(
                        userId = user.uid,
                        conversationId = convId,
                        senderId = user.uid,
                        senderName = user.displayName,
                        content = "📅 Group Event: $title ($dateTimeText @ $location)",
                        messageType = ChatMessageType.GROUP_EVENT.name,
                        messageId = created.id
                    )
                }
            }
            postStatusBanner("Created event: $title")
        }
    }

    fun rsvpToGroupEvent(messageId: String, rsvpStatus: String) {
        viewModelScope.launch {
            repository.rsvpToGroupEvent(messageId, rsvpStatus)
            postStatusBanner("Updated RSVP to $rsvpStatus")
        }
    }

    fun addMemberToActiveGroup(displayName: String, isAdmin: Boolean) {
        val convId = _uiState.value.activeConversation?.id ?: return
        viewModelScope.launch {
            repository.addGroupMember(convId, displayName, isAdmin)
            postStatusBanner("Added $displayName to group")
        }
    }

    fun toggleMemberAdminRole(member: ConversationMemberEntity) {
        viewModelScope.launch {
            repository.toggleGroupMemberRole(member.conversationId, member.userId, member.role, member.displayName)
        }
    }

    fun removeMemberFromGroup(member: ConversationMemberEntity) {
        viewModelScope.launch {
            repository.removeGroupMember(member.conversationId, member.userId, member.displayName)
            postStatusBanner("Removed ${member.displayName}")
        }
    }

    fun toggleOnlyAdminsCanSend() {
        val convId = _uiState.value.activeConversation?.id ?: return
        viewModelScope.launch {
            repository.toggleOnlyAdminsCanSend(convId)
        }
    }

    fun resetGroupInviteLink() {
        val convId = _uiState.value.activeConversation?.id ?: return
        viewModelScope.launch {
            val newLink = repository.resetGroupInviteLink(convId)
            postStatusBanner("Reset invite link: $newLink")
        }
    }

    fun toggleLockActiveConversation() {
        val convId = _uiState.value.activeConversation?.id ?: return
        viewModelScope.launch {
            val locked = repository.toggleLockConversation(convId)
            postStatusBanner(if (locked) "Chat locked behind PIN 🔒" else "Removed Chat Lock")
        }
    }

    fun setDisappearingMessagesForActive(hours: Int) {
        val convId = _uiState.value.activeConversation?.id ?: return
        viewModelScope.launch {
            repository.setDisappearingMessages(convId, hours)
        }
    }

    fun toggleBlockActiveConversation() {
        val convId = _uiState.value.activeConversation?.id ?: return
        viewModelScope.launch {
            val blocked = repository.toggleBlockConversation(convId)
            postStatusBanner(if (blocked) "Contact blocked" else "Contact unblocked")
        }
    }

    fun reportActiveConversation() {
        val convId = _uiState.value.activeConversation?.id ?: return
        viewModelScope.launch {
            repository.reportConversation(convId)
            postStatusBanner("Reported & blocked conversation")
        }
    }

    fun updateMyStatus(newStatus: String) {
        val trimmed = newStatus.trim()
        if (trimmed.isEmpty()) return
        updateSettings {
            it.copy(
                myStatusText = trimmed,
                myStatusUpdatedAt = System.currentTimeMillis()
            )
        }
        postStatusBanner("Updated your 24h status")
    }

    fun shareCurrentQuestionToConversation(question: MathQuestion, targetConversationId: String) {
        val payload = SharedQuestionPayload.fromMathQuestion(question)
        sendMessage(
            content = "Shared MATHGATE Question: ${question.prompt}",
            messageType = ChatMessageType.QUESTION_CARD,
            questionPayload = payload,
            targetConversationId = targetConversationId
        )
        selectConversation(targetConversationId)
        val targetTitle = _uiState.value.conversations.firstOrNull { it.id == targetConversationId }?.title ?: "Chat"
        postStatusBanner("Shared question card to $targetTitle")
    }

    fun shareNoteToConversation(note: NoteEntity, targetConversationId: String, firstLatex: String? = null) {
        sendMessage(
            content = "Shared Note from Notes Studio: ${note.title}",
            messageType = ChatMessageType.NOTE_CARD,
            sharedNoteId = note.id,
            sharedNoteTitle = note.title,
            latexFormula = firstLatex,
            targetConversationId = targetConversationId
        )
        selectConversation(targetConversationId)
        postStatusBanner("Shared note \"${note.title}\" to conversation")
    }

    fun editMessage(messageId: String, newText: String, newLatex: String? = null) {
        viewModelScope.launch {
            repository.editMessage(messageId, newText, newLatex)
            val user = _uiState.value.cloudUser
            val cloudRepo = cloudRepository
            if (user != null && cloudRepo != null) {
                runCatching {
                    cloudRepo.updateCloudMessageContent(user.uid, messageId, newText)
                }
            }
            postStatusBanner("Message edited")
        }
    }

    fun deleteMessage(messageId: String, deleteForEveryone: Boolean) {
        viewModelScope.launch {
            repository.deleteMessage(messageId, deleteForEveryone)
            val user = _uiState.value.cloudUser
            val cloudRepo = cloudRepository
            if (user != null && cloudRepo != null) {
                runCatching {
                    if (deleteForEveryone) {
                        cloudRepo.updateCloudMessageFlags(user.uid, messageId, isDeletedForEveryone = true)
                    } else {
                        cloudRepo.deleteCloudMessage(user.uid, messageId)
                    }
                }
            }
            postStatusBanner(if (deleteForEveryone) "Deleted message for everyone" else "Deleted message for you")
        }
    }

    fun forwardMessage(message: MessageEntity, targetConversationId: String) {
        viewModelScope.launch {
            val type = runCatching { ChatMessageType.valueOf(message.messageType) }.getOrDefault(ChatMessageType.PLAIN_TEXT)
            val qPayload = SharedQuestionPayload.decodeFromPipeString(message.questionJson)
            sendMessage(
                content = message.content,
                messageType = type,
                latexFormula = message.latexFormula,
                codeLanguage = message.codeLanguage,
                codeContent = message.codeContent,
                sharedNoteId = message.sharedNoteId,
                sharedNoteTitle = message.sharedNoteTitle,
                questionPayload = qPayload,
                forwardedFrom = message.senderName,
                targetConversationId = targetConversationId
            )
            selectConversation(targetConversationId)
            postStatusBanner("Forwarded message")
        }
    }

    fun toggleReaction(messageId: String, emoji: String) {
        val convId = _uiState.value.activeConversation?.id ?: return
        viewModelScope.launch {
            repository.toggleReaction(messageId, convId, emoji)
            val user = _uiState.value.cloudUser
            val cloudRepo = cloudRepository
            if (user != null && cloudRepo != null) {
                runCatching {
                    cloudRepo.updateCloudMessageReaction(user.uid, messageId, emoji)
                }
            }
        }
    }

    fun toggleStarred(messageId: String) {
        val convId = _uiState.value.activeConversation?.id ?: return
        viewModelScope.launch {
            val added = repository.toggleStarred(messageId, convId)
            val user = _uiState.value.cloudUser
            val cloudRepo = cloudRepository
            if (user != null && cloudRepo != null) {
                runCatching {
                    cloudRepo.updateCloudMessageFlags(user.uid, messageId, isStarred = added)
                }
            }
            postStatusBanner(if (added) "Message starred ★" else "Removed star")
        }
    }

    fun togglePinned(messageId: String) {
        val convId = _uiState.value.activeConversation?.id ?: return
        viewModelScope.launch {
            val added = repository.togglePinned(messageId, convId)
            val user = _uiState.value.cloudUser
            val cloudRepo = cloudRepository
            if (user != null && cloudRepo != null) {
                runCatching {
                    cloudRepo.updateCloudMessageFlags(user.uid, messageId, isPinned = added)
                }
            }
            postStatusBanner(if (added) "Message pinned to top 📌" else "Message unpinned")
        }
    }

    fun createPoll(question: String, options: List<String>, allowMultiple: Boolean, isAnonymous: Boolean) {
        val convId = _uiState.value.activeConversation?.id ?: return
        viewModelScope.launch {
            val poll = repository.createPollMessage(convId, question, options, allowMultiple, isAnonymous)
            val user = _uiState.value.cloudUser
            val cloudRepo = cloudRepository
            if (user != null && cloudRepo != null) {
                runCatching {
                    cloudRepo.sendCloudMessage(
                        userId = user.uid,
                        conversationId = convId,
                        senderId = user.uid,
                        senderName = user.displayName,
                        content = "📊 Poll: $question (${options.joinToString(" / ")})",
                        messageType = ChatMessageType.POLL.name,
                        messageId = poll.messageId
                    )
                }
            }
            postStatusBanner("Created group poll: $question")
        }
    }

    fun voteOnPoll(pollId: String, optionId: String, allowMultiple: Boolean) {
        viewModelScope.launch {
            repository.voteOnPoll(pollId, optionId, allowMultiple)
        }
    }

    fun createGroupOrBroadcast(title: String, description: String, isBroadcast: Boolean, membersCsv: String) {
        val memberNames = membersCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
        viewModelScope.launch {
            val created = repository.createGroupOrBroadcast(title, description, isBroadcast, memberNames)
            val user = _uiState.value.cloudUser
            val cloudRepo = cloudRepository
            if (user != null && cloudRepo != null) {
                runCatching {
                    cloudRepo.upsertCloudConversation(
                        userId = user.uid,
                        conversationId = created.id,
                        title = created.title,
                        type = "STUDY_GROUP",
                        peerCode = created.pairingCodeOtp,
                        lastMessagePreview = created.lastMessagePreview,
                        subtitle = created.subtitle,
                        isPinned = true
                    )
                }
            }
            selectConversation(created.id)
            postStatusBanner("Created ${if (isBroadcast) "Broadcast list" else "Study Group"}: ${created.title}")
        }
    }

    fun pairContact(contact: ChatPeerContact) {
        viewModelScope.launch {
            val conv = repository.pairContactViaQrOrOtp(contact)
            val user = _uiState.value.cloudUser
            val cloudRepo = cloudRepository
            if (user != null && cloudRepo != null) {
                runCatching {
                    cloudRepo.upsertCloudConversation(
                        userId = user.uid,
                        conversationId = conv.id,
                        title = conv.title,
                        type = if (contact.isGroup) "STUDY_GROUP" else "DIRECT_PEER",
                        peerCode = contact.pairingCodeOtp,
                        lastMessagePreview = conv.lastMessagePreview,
                        subtitle = "☁️ Cloud Paired · OTP #${PeerPairingEngine.formatOtpDisplay(contact.pairingCodeOtp)}",
                        isPinned = true
                    )
                    val cleanOtp = contact.pairingCodeOtp.filter { it.isDigit() }
                    if (cleanOtp.isNotBlank()) {
                        cloudRepo.sendSharedRoomMessage(
                            roomId = "room_$cleanOtp",
                            senderUid = user.uid,
                            senderName = user.displayName,
                            participantUids = listOf(user.uid),
                            content = "🤝 ${user.displayName} joined room #${PeerPairingEngine.formatOtpDisplay(cleanOtp)}",
                            messageType = ChatMessageType.SYSTEM_NOTIFICATION.name
                        )
                    }
                }
            }
            selectConversation(conv.id)
            postStatusBanner("Connected with ${contact.name} (OTP #${PeerPairingEngine.formatOtpDisplay(contact.pairingCodeOtp)})")
        }
    }

    fun togglePinConversation(conversationId: String) {
        viewModelScope.launch {
            repository.togglePinConversation(conversationId)
        }
    }

    fun toggleArchiveConversation(conversationId: String) {
        viewModelScope.launch {
            repository.toggleArchiveConversation(conversationId)
            postStatusBanner("Updated conversation archive state")
        }
    }

    fun toggleMuteConversation(conversationId: String) {
        viewModelScope.launch {
            repository.toggleMuteConversation(conversationId)
            postStatusBanner("Updated conversation mute notification state")
        }
    }

    fun clearActiveConversation() {
        val convId = _uiState.value.activeConversation?.id ?: return
        viewModelScope.launch {
            repository.clearConversationHistory(convId)
            postStatusBanner("Cleared local conversation history")
        }
    }

    fun updateSettings(transform: (ChatSettingsEntity) -> ChatSettingsEntity) {
        viewModelScope.launch {
            repository.updateChatSettings(transform)
            postStatusBanner("Updated Chat privacy & appearance settings")
        }
    }

    suspend fun exportActiveConversation(): String {
        val convId = _uiState.value.activeConversation?.id ?: return ""
        return repository.exportConversationAsMarkdown(convId)
    }

    class Factory(
        private val repository: ChatRepository,
        private val cloudRepository: CloudChatRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(repository, cloudRepository) as T
        }
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
