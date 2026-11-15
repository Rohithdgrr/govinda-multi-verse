package com.example.feature.chat

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.ChatMessageType
import com.example.core.database.ConversationEntity
import com.example.core.database.ConversationMemberEntity
import com.example.core.database.ConversationType
import com.example.core.database.MessageEntity
import com.example.domain.model.MathQuestion
import com.example.feature.notes.NotesStudioUiState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PrimaryBlue = Color(0xFF003DA5)
private val SecondaryBlue = Color(0xFF1647BB)
private val LightBlueSurface = Color(0xFFEAF1FF)
private val WallpaperTint = Color(0xFFF3F7FE)
private val BorderBlue = Color(0xFFBFD4FF)
private val OutgoingBubbleBg = Color(0xFFDCE8FF)
private val IncomingBubbleBg = Color.White
private val MutedGray = Color(0xFF64748B)
private val DarkSlate = Color(0xFF0F172A)

@Composable
fun ChatScreen(
    chatState: ChatUiState,
    chatViewModel: ChatViewModel,
    notesState: NotesStudioUiState,
    currentQuestion: MathQuestion?,
    onBack: () -> Unit,
    onOpenNotesStudio: () -> Unit,
    onOpenPracticeMode: () -> Unit,
    onInsertParagraphToNote: (String) -> Unit,
    onInsertLatexToNote: (String) -> Unit,
    onInsertCodeToNote: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboard = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        chatViewModel.refreshCloudAuthState()
        chatViewModel.attemptAutoGoogleSignIn(context)
    }

    var showQrOtpModal by remember { mutableStateOf(false) }
    var showNewGroupModal by remember { mutableStateOf(false) }
    var showPollCreatorModal by remember { mutableStateOf(false) }
    var showGroupEventModal by remember { mutableStateOf(false) }
    var showLiveLocationModal by remember { mutableStateOf(false) }
    var showAttachmentModal by remember { mutableStateOf(false) }
    var showPrivacyModal by remember { mutableStateOf(false) }
    var showContactGroupInfoSheet by remember { mutableStateOf(false) }
    var showStatusUpdatesDrawer by remember { mutableStateOf(false) }
    var showCallsDrawer by remember { mutableStateOf(false) }
    var showUnlockPinDialog by remember { mutableStateOf(false) }

    // Pending media for PreSendMediaCaptionDialog (with 1️⃣ View Once support)
    var pendingMediaUri by remember { mutableStateOf<String?>(null) }
    var pendingMediaFileName by remember { mutableStateOf("") }
    var pendingMediaMime by remember { mutableStateOf("image/jpeg") }
    var pendingMediaType by remember { mutableStateOf(ChatMessageType.IMAGE) }

    // On mobile phones, start on the WhatsApp "Chats" list screen (like normal WhatsApp),
    // and tapping any chat row opens the full-screen conversation thread!
    var showCompactContactsList by remember { mutableStateOf(true) }
    var showAttachmentTray by remember { mutableStateOf(false) }
    var showSearchBarInThread by remember { mutableStateOf(false) }

    var inputMessage by remember(chatState.activeConversationId) {
        val savedDraft = chatState.draftsByConversationId[chatState.activeConversationId]?.draftText ?: ""
        mutableStateOf(savedDraft)
    }
    var replyTarget by remember { mutableStateOf<MessageEntity?>(null) }
    var editingMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var forwardingMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var isRecordingVoiceNote by remember { mutableStateOf(false) }

    val activeConversation = chatState.activeConversation
    val activeMessages = chatState.activeMessages

    BackHandler(enabled = !showCompactContactsList) {
        if (showAttachmentTray) {
            showAttachmentTray = false
        } else {
            showCompactContactsList = true
        }
    }

    // Zero-permission Android Photo Picker -> opens PreSendMediaCaptionDialog
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pendingMediaUri = uri.toString()
            pendingMediaFileName = "Photo_${System.currentTimeMillis() % 10000}.jpg"
            pendingMediaMime = "image/jpeg"
            pendingMediaType = ChatMessageType.IMAGE
            showAttachmentTray = false
        }
    }

    // Zero-permission Storage Access Framework Document Picker -> reads real file metadata & Base64 preview
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            chatViewModel.sendDocumentFromUri(
                context = context,
                uriString = uri.toString(),
                fallbackFileName = "Document_${System.currentTimeMillis() % 10000}.pdf",
                fallbackMimeType = "application/pdf",
                messageType = ChatMessageType.PDF
            )
            showCompactContactsList = false
            showAttachmentTray = false
        }
    }

    // Runtime Location Permission Launcher -> resolves actual Android LocationManager GPS coordinates
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        showLiveLocationModal = true
    }

    fun triggerLocationSharingWithPermission() {
        runCatching {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }.onFailure {
            showLiveLocationModal = true
        }
    }

    fun triggerPhotoPicker() {
        runCatching {
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }.onFailure {
            pendingMediaUri = "local://gallery/photo_attachment.png"
            pendingMediaFileName = "Shared_Photo_${System.currentTimeMillis() % 1000}.png"
            pendingMediaMime = "image/png"
            pendingMediaType = ChatMessageType.IMAGE
            showAttachmentTray = false
        }
    }

    fun triggerDocumentPicker() {
        runCatching {
            documentPickerLauncher.launch(arrayOf("application/pdf", "text/*", "application/zip", "*/*"))
        }.onFailure {
            chatViewModel.sendDocumentFromUri(
                context = context,
                uriString = "local://docs/Reference_Notes.pdf",
                fallbackFileName = "Reference_Handout.pdf",
                fallbackMimeType = "application/pdf",
                messageType = ChatMessageType.PDF
            )
            showCompactContactsList = false
            showAttachmentTray = false
        }
    }

    fun submitCurrentMessage() {
        val trimmed = inputMessage.trim()
        if (trimmed.isEmpty()) return

        val editTarget = editingMessage
        if (editTarget != null) {
            chatViewModel.editMessage(editTarget.id, trimmed)
            editingMessage = null
            inputMessage = ""
            return
        }

        chatViewModel.sendMessage(
            content = trimmed,
            messageType = ChatMessageType.PLAIN_TEXT,
            replyToMessageId = replyTarget?.id,
            replyToSnippet = replyTarget?.content?.take(60),
            currentQuestion = currentQuestion,
            allNotes = notesState.allNotes,
            activeNote = notesState.activeNote,
            activeBlocks = notesState.activeBlocks
        )
        inputMessage = ""
        replyTarget = null
        showAttachmentTray = false
    }

    if (pendingMediaUri != null) {
        PreSendMediaCaptionDialog(
            fileName = pendingMediaFileName,
            messageType = pendingMediaType,
            onDismiss = { pendingMediaUri = null },
            onConfirmSend = { caption, isViewOnce ->
                val uri = pendingMediaUri ?: return@PreSendMediaCaptionDialog
                chatViewModel.sendImageFromUri(
                    context = context,
                    uriString = uri,
                    fallbackFileName = pendingMediaFileName,
                    caption = caption,
                    isViewOnce = isViewOnce
                )
                pendingMediaUri = null
                showCompactContactsList = false
            }
        )
    }

    if (showQrOtpModal) {
        QrAndOtpPairingDialog(
            myDeviceId = chatState.myDeviceId,
            myOtpCode = chatState.myDeviceOtp,
            onRegenerateMyOtp = chatViewModel::regenerateDeviceOtp,
            onPeerConnected = { newContact ->
                chatViewModel.pairContact(newContact)
                showCompactContactsList = false
            },
            onDismiss = { showQrOtpModal = false }
        )
    }

    if (showNewGroupModal) {
        CreateStudyGroupOrBroadcastDialog(
            onDismiss = { showNewGroupModal = false },
            onCreate = { title, desc, isBroadcast, membersCsv ->
                chatViewModel.createGroupOrBroadcast(title, desc, isBroadcast, membersCsv)
                showCompactContactsList = false
            }
        )
    }

    if (showPollCreatorModal) {
        CreatePollDialog(
            onDismiss = { showPollCreatorModal = false },
            onCreatePoll = { q, opts, multi, anon ->
                chatViewModel.createPoll(q, opts, multi, anon)
                showAttachmentTray = false
            }
        )
    }

    if (showGroupEventModal) {
        CreateGroupEventDialog(
            onDismiss = { showGroupEventModal = false },
            onCreateEvent = { title, dateTime, location, description ->
                chatViewModel.createGroupEvent(title, dateTime, location, description)
                showAttachmentTray = false
            }
        )
    }

    if (showLiveLocationModal) {
        LiveLocationDurationDialog(
            onDismiss = { showLiveLocationModal = false },
            onSelectDuration = { mins, placeLabel ->
                chatViewModel.sendRealDeviceLocation(context, mins, placeLabel)
                showCompactContactsList = false
                showAttachmentTray = false
            }
        )
    }

    if (showAttachmentModal) {
        EducationalAttachmentDialog(
            currentQuestion = currentQuestion,
            notes = notesState.allNotes,
            onDismiss = { showAttachmentModal = false },
            onShareCurrentQuestion = {
                val q = currentQuestion
                val payload = if (q != null) {
                    SharedQuestionPayload.fromMathQuestion(q)
                } else {
                    SharedQuestionPayload(
                        questionId = "q_gate_${System.currentTimeMillis() % 1000}",
                        subject = "Engineering Mathematics",
                        topic = "Eigenvalues & Cayley-Hamilton",
                        difficulty = "Medium",
                        prompt = "If A is a 3×3 matrix with eigenvalues 1, 2, and 3, what is the trace of A² + 2A?",
                        options = listOf("26", "18", "14", "32"),
                        answer = "26",
                        explanation = "Eigenvalues of A² + 2A are λ² + 2λ: (1+2) + (4+4) + (9+6) = 3 + 8 + 15 = 26.",
                        sourceAttribution = "MATHGATE Practice Engine"
                    )
                }
                chatViewModel.sendMessage(
                    content = "Shared a practice question: ${payload.topic}",
                    messageType = ChatMessageType.QUESTION_CARD,
                    questionPayload = payload
                )
                showAttachmentTray = false
            },
            onSendLatexEquation = { caption, latex ->
                chatViewModel.sendMessage(
                    content = caption,
                    messageType = ChatMessageType.LATEX_EQUATION,
                    latexFormula = latex
                )
                showAttachmentTray = false
            },
            onSendCodeSnippet = { caption, lang, code ->
                chatViewModel.sendMessage(
                    content = caption,
                    messageType = ChatMessageType.CODE_SNIPPET,
                    codeLanguage = lang,
                    codeContent = code
                )
                showAttachmentTray = false
            },
            onShareNote = { note ->
                chatViewModel.sendMessage(
                    content = "Shared note from Notes Studio · ${note.subjectTag}",
                    messageType = ChatMessageType.NOTE_CARD,
                    sharedNoteId = note.id,
                    sharedNoteTitle = note.title
                )
                showAttachmentTray = false
            },
            onOpenPollCreator = { showPollCreatorModal = true },
            onPickPhotoOrVideo = ::triggerPhotoPicker,
            onPickDocumentOrPdf = ::triggerDocumentPicker,
            onSendLocationWithConsent = ::triggerLocationSharingWithPermission,
            onSendContactCard = { name, role, otp ->
                chatViewModel.sendMediaOrDocumentAttachment(
                    uriString = "local://contacts/$otp",
                    fileName = "$name ($role) · OTP: $otp",
                    mimeType = "text/x-vcard",
                    fileSizeBytes = 4_096L,
                    messageType = ChatMessageType.CONTACT_CARD,
                    caption = "👤 Shared Contact Card: *$name* · $role (OTP: `$otp`)"
                )
                showAttachmentTray = false
            },
            onSendAudioFile = {
                chatViewModel.sendMediaOrDocumentAttachment(
                    uriString = "local://audio/gate_revision_lecture.m4a",
                    fileName = "GATE_Formula_Revision_Audio.m4a",
                    mimeType = "audio/mp4",
                    fileSizeBytes = 1_420_000L,
                    messageType = ChatMessageType.AUDIO_FILE,
                    caption = "🎧 Shared Audio Lecture: GATE Formula Revision"
                )
                showAttachmentTray = false
            }
        )
    }

    if (showPrivacyModal) {
        ChatPrivacyAndCustomizationDialog(
            settings = chatState.settings,
            onDismiss = { showPrivacyModal = false },
            onUpdateSettings = chatViewModel::updateSettings
        )
    }

    if (showContactGroupInfoSheet && activeConversation != null) {
        ContactAndGroupInfoSheet(
            conversation = activeConversation,
            members = chatState.activeConversationMembers,
            messages = activeMessages,
            attachments = chatState.attachments,
            myDeviceId = chatState.myDeviceId,
            onDismiss = { showContactGroupInfoSheet = false },
            onSetDisappearingHours = chatViewModel::setDisappearingMessagesForActive,
            onToggleChatLock = chatViewModel::toggleLockActiveConversation,
            onToggleOnlyAdminsCanSend = chatViewModel::toggleOnlyAdminsCanSend,
            onResetInviteLink = chatViewModel::resetGroupInviteLink,
            onCopyInviteLink = { link ->
                clipboard.setText(AnnotatedString(link))
                chatViewModel.postStatusBanner("Invite link copied to clipboard")
            },
            onAddGroupMember = { name -> chatViewModel.addMemberToActiveGroup(name, false) },
            onToggleMemberRole = chatViewModel::toggleMemberAdminRole,
            onRemoveMember = chatViewModel::removeMemberFromGroup,
            onToggleBlock = chatViewModel::toggleBlockActiveConversation,
            onReportContact = {
                chatViewModel.reportActiveConversation()
                showContactGroupInfoSheet = false
            }
        )
    }

    if (showStatusUpdatesDrawer) {
        StatusUpdatesHeaderDrawerDialog(
            users = chatState.users,
            myStatusText = chatState.settings.myStatusText,
            onUpdateMyStatus = chatViewModel::updateMyStatus,
            onDismiss = { showStatusUpdatesDrawer = false }
        )
    }

    if (showCallsDrawer) {
        CallLogsHeaderDrawerDialog(
            conversations = chatState.conversations,
            onStartCallWith = { conv, isVideo ->
                chatViewModel.selectConversation(conv.id)
                showCallsDrawer = false
                showCompactContactsList = false
                chatViewModel.postStatusBanner(
                    if (isVideo) "Started encrypted video study session with ${conv.title}"
                    else "Started encrypted voice study call with ${conv.title}"
                )
            },
            onDismiss = { showCallsDrawer = false }
        )
    }

    if (showUnlockPinDialog) {
        ChatLockPinDialog(
            expectedPin = chatState.settings.chatLockPin,
            onUnlocked = {
                chatViewModel.unlockLockedFolderWithPin(chatState.settings.chatLockPin)
                chatViewModel.setFilterTab("LOCKED")
            },
            onDismiss = { showUnlockPinDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("developer_dashboard_screen")
    ) {
        // Status Feedback Banner (if active)
        chatState.statusBanner?.let { banner ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightBlueSurface)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = banner,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    onClick = chatViewModel::clearStatusBanner,
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderBlue)
                ) {
                    Text(
                        text = "OK",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            HorizontalDivider(color = BorderBlue)
        }

        // Cloud Firestore Realtime Backend & Google Sign-In Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (chatState.cloudUser != null) Color(0xFFEAF6EE) else LightBlueSurface)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val cloudUser = chatState.cloudUser
            if (cloudUser == null) {
                Text(
                    text = "☁️ Cloud Chat Ready · Sign in with Google to stream live messages, photos, files & GPS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Surface(
                    onClick = { chatViewModel.signInWithGoogle(context) },
                    shape = RoundedCornerShape(4.dp),
                    color = PrimaryBlue,
                    modifier = Modifier.testTag("google_sign_in_button")
                ) {
                    Text(
                        text = if (chatState.isSigningInWithGoogle) "SIGNING IN..." else "SIGN IN WITH GOOGLE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            } else {
                Text(
                    text = "☁️ Live Firestore Connected · ${cloudUser.displayName} (OTP #${chatState.myDeviceOtp})",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF146C2E),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Surface(
                    onClick = { chatViewModel.signOutFromCloud(context) },
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFF146C2E)),
                    modifier = Modifier.testTag("google_sign_out_button")
                ) {
                    Text(
                        text = "SIGN OUT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF146C2E),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
        HorizontalDivider(color = BorderBlue)

        // Responsive Phone (WhatsApp 2-screen Chats -> Conversation flow) & Tablet (WhatsApp Web Split-Pane)
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val isWideScreen = maxWidth >= 600.dp
            val contactPaneWidth = if (maxWidth >= 840.dp) 350.dp else 280.dp

            if (isWideScreen) {
                Row(modifier = Modifier.fillMaxSize()) {
                    ChatContactListPane(
                        conversations = chatState.conversations,
                        draftsByConversationId = chatState.draftsByConversationId,
                        selectedConversationId = chatState.activeConversationId,
                        searchQuery = chatState.conversationSearchQuery,
                        selectedFilter = chatState.selectedFilterTab,
                        myDeviceOtp = chatState.myDeviceOtp,
                        isLockedFolderUnlocked = chatState.isLockedFolderUnlocked,
                        onSearchQueryChange = chatViewModel::setConversationSearchQuery,
                        onSelectFilter = chatViewModel::setFilterTab,
                        onSelectConversation = chatViewModel::selectConversation,
                        onOpenQrAndOtpDialog = { showQrOtpModal = true },
                        onOpenNewGroupDialog = { showNewGroupModal = true },
                        onOpenCameraPicker = ::triggerPhotoPicker,
                        onOpenPrivacyDialog = { showPrivacyModal = true },
                        onToggleStarredFilter = chatViewModel::toggleShowStarredOnly,
                        onOpenStatusUpdatesDrawer = { showStatusUpdatesDrawer = true },
                        onOpenCallsDrawer = { showCallsDrawer = true },
                        onRequestUnlockLockedChats = { showUnlockPinDialog = true },
                        modifier = Modifier.width(contactPaneWidth)
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(BorderBlue)
                    )
                    WhatsAppConversationPane(
                        activeConversation = activeConversation,
                        conversations = chatState.conversations,
                        members = chatState.activeConversationMembers,
                        messages = activeMessages,
                        attachments = chatState.attachments,
                        reactionsByMessageId = chatState.reactionsByMessageId,
                        starredIds = chatState.starredIds,
                        pinnedIds = chatState.pinnedIds,
                        pollsById = chatState.pollsById,
                        pollOptionsByPollId = chatState.pollOptionsByPollId,
                        pollVotesByPollId = chatState.pollVotesByPollId,
                        inputMessage = inputMessage,
                        replyTarget = replyTarget,
                        editingMessage = editingMessage,
                        forwardingMessage = forwardingMessage,
                        isRecordingVoiceNote = isRecordingVoiceNote,
                        showAttachmentTray = showAttachmentTray,
                        showSearchBarInThread = showSearchBarInThread,
                        searchQuery = chatState.messageSearchQuery,
                        selectedMessageTypeFilter = chatState.selectedMessageTypeFilter,
                        showStarredOnly = chatState.showStarredOnly,
                        showPinnedOnly = chatState.showPinnedOnly,
                        onBackToChatsList = onBack,
                        onOpenContactOrGroupInfo = { showContactGroupInfoSheet = true },
                        onSelectConversation = chatViewModel::selectConversation,
                        onOpenQrOtpDialog = { showQrOtpModal = true },
                        onOpenNewGroupDialog = { showNewGroupModal = true },
                        onOpenPrivacyDialog = { showPrivacyModal = true },
                        onToggleSearchBar = { showSearchBarInThread = !showSearchBarInThread },
                        onSearchQueryChange = chatViewModel::setMessageSearchQuery,
                        onSelectMessageTypeFilter = chatViewModel::setMessageTypeFilter,
                        onToggleStarredOnly = chatViewModel::toggleShowStarredOnly,
                        onTogglePinnedOnly = chatViewModel::toggleShowPinnedOnly,
                        onToggleMuteConversation = {
                            activeConversation?.let { chatViewModel.toggleMuteConversation(it.id) }
                        },
                        onToggleArchiveConversation = {
                            activeConversation?.let { chatViewModel.toggleArchiveConversation(it.id) }
                        },
                        onToggleBlockConversation = chatViewModel::toggleBlockActiveConversation,
                        onExportConversation = {
                            coroutineScope.launch {
                                val md = chatViewModel.exportActiveConversation()
                                clipboard.setText(AnnotatedString(md))
                                chatViewModel.postStatusBanner("Exported chat transcript to clipboard")
                            }
                        },
                        onClearConversation = chatViewModel::clearActiveConversation,
                        onToggleAttachmentTray = { showAttachmentTray = !showAttachmentTray },
                        onOpenFormulaCodeModal = { showAttachmentModal = true },
                        onOpenPollDialog = { showPollCreatorModal = true },
                        onOpenGroupEventDialog = { showGroupEventModal = true },
                        onPickPhoto = ::triggerPhotoPicker,
                        onPickDocument = ::triggerDocumentPicker,
                        onSendLocation = ::triggerLocationSharingWithPermission,
                        onInputMessageChange = { text ->
                            inputMessage = text
                            activeConversation?.let {
                                chatViewModel.updateDraft(it.id, text, replyTarget?.id, replyTarget?.content?.take(50))
                            }
                        },
                        onClearReplyTarget = {
                            replyTarget = null
                            editingMessage = null
                        },
                        onSendMessage = ::submitCurrentMessage,
                        onStartVoiceRecording = { isRecordingVoiceNote = true },
                        onCancelVoiceRecording = { isRecordingVoiceNote = false },
                        onSendVoiceNote = { dur, wave ->
                            isRecordingVoiceNote = false
                            chatViewModel.sendVoiceNote(dur, wave)
                        },
                        onReplyToMessage = { msg -> replyTarget = msg },
                        onEditMessage = { msg ->
                            editingMessage = msg
                            inputMessage = msg.content
                        },
                        onForwardMessage = { msg -> forwardingMessage = msg },
                        onConfirmForwardTo = { targetConvId ->
                            forwardingMessage?.let { chatViewModel.forwardMessage(it, targetConvId) }
                            forwardingMessage = null
                        },
                        onCancelForward = { forwardingMessage = null },
                        onDeleteMessage = { msg, forAll -> chatViewModel.deleteMessage(msg.id, forAll) },
                        onToggleStar = { msg -> chatViewModel.toggleStarred(msg.id) },
                        onTogglePin = { msg -> chatViewModel.togglePinned(msg.id) },
                        onReactToMessage = { msg, emoji -> chatViewModel.toggleReaction(msg.id, emoji) },
                        onVotePoll = { pollId, optionId, allowMulti ->
                            chatViewModel.voteOnPoll(pollId, optionId, allowMulti)
                        },
                        onOpenViewOnceMessage = { msg -> chatViewModel.openViewOnceMessage(msg.id) },
                        onStopLiveLocation = { msg -> chatViewModel.stopLiveLocation(msg.id) },
                        onRsvpGroupEvent = { msg, status -> chatViewModel.rsvpToGroupEvent(msg.id, status) },
                        onCopyMessage = { text ->
                            clipboard.setText(AnnotatedString(text))
                            chatViewModel.postStatusBanner("Copied to clipboard")
                        },
                        onOpenNotesStudio = onOpenNotesStudio,
                        onOpenPracticeMode = onOpenPracticeMode,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                if (showCompactContactsList) {
                    ChatContactListPane(
                        conversations = chatState.conversations,
                        draftsByConversationId = chatState.draftsByConversationId,
                        selectedConversationId = chatState.activeConversationId,
                        searchQuery = chatState.conversationSearchQuery,
                        selectedFilter = chatState.selectedFilterTab,
                        myDeviceOtp = chatState.myDeviceOtp,
                        isLockedFolderUnlocked = chatState.isLockedFolderUnlocked,
                        onSearchQueryChange = chatViewModel::setConversationSearchQuery,
                        onSelectFilter = chatViewModel::setFilterTab,
                        onSelectConversation = { id ->
                            chatViewModel.selectConversation(id)
                            showCompactContactsList = false
                        },
                        onOpenQrAndOtpDialog = { showQrOtpModal = true },
                        onOpenNewGroupDialog = { showNewGroupModal = true },
                        onOpenCameraPicker = ::triggerPhotoPicker,
                        onOpenPrivacyDialog = { showPrivacyModal = true },
                        onToggleStarredFilter = chatViewModel::toggleShowStarredOnly,
                        onOpenStatusUpdatesDrawer = { showStatusUpdatesDrawer = true },
                        onOpenCallsDrawer = { showCallsDrawer = true },
                        onRequestUnlockLockedChats = { showUnlockPinDialog = true },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    WhatsAppConversationPane(
                        activeConversation = activeConversation,
                        conversations = chatState.conversations,
                        members = chatState.activeConversationMembers,
                        messages = activeMessages,
                        attachments = chatState.attachments,
                        reactionsByMessageId = chatState.reactionsByMessageId,
                        starredIds = chatState.starredIds,
                        pinnedIds = chatState.pinnedIds,
                        pollsById = chatState.pollsById,
                        pollOptionsByPollId = chatState.pollOptionsByPollId,
                        pollVotesByPollId = chatState.pollVotesByPollId,
                        inputMessage = inputMessage,
                        replyTarget = replyTarget,
                        editingMessage = editingMessage,
                        forwardingMessage = forwardingMessage,
                        isRecordingVoiceNote = isRecordingVoiceNote,
                        showAttachmentTray = showAttachmentTray,
                        showSearchBarInThread = showSearchBarInThread,
                        searchQuery = chatState.messageSearchQuery,
                        selectedMessageTypeFilter = chatState.selectedMessageTypeFilter,
                        showStarredOnly = chatState.showStarredOnly,
                        showPinnedOnly = chatState.showPinnedOnly,
                        onBackToChatsList = { showCompactContactsList = true },
                        onOpenContactOrGroupInfo = { showContactGroupInfoSheet = true },
                        onSelectConversation = chatViewModel::selectConversation,
                        onOpenQrOtpDialog = { showQrOtpModal = true },
                        onOpenNewGroupDialog = { showNewGroupModal = true },
                        onOpenPrivacyDialog = { showPrivacyModal = true },
                        onToggleSearchBar = { showSearchBarInThread = !showSearchBarInThread },
                        onSearchQueryChange = chatViewModel::setMessageSearchQuery,
                        onSelectMessageTypeFilter = chatViewModel::setMessageTypeFilter,
                        onToggleStarredOnly = chatViewModel::toggleShowStarredOnly,
                        onTogglePinnedOnly = chatViewModel::toggleShowPinnedOnly,
                        onToggleMuteConversation = {
                            activeConversation?.let { chatViewModel.toggleMuteConversation(it.id) }
                        },
                        onToggleArchiveConversation = {
                            activeConversation?.let { chatViewModel.toggleArchiveConversation(it.id) }
                        },
                        onToggleBlockConversation = chatViewModel::toggleBlockActiveConversation,
                        onExportConversation = {
                            coroutineScope.launch {
                                val md = chatViewModel.exportActiveConversation()
                                clipboard.setText(AnnotatedString(md))
                                chatViewModel.postStatusBanner("Exported chat transcript to clipboard")
                            }
                        },
                        onClearConversation = chatViewModel::clearActiveConversation,
                        onToggleAttachmentTray = { showAttachmentTray = !showAttachmentTray },
                        onOpenFormulaCodeModal = { showAttachmentModal = true },
                        onOpenPollDialog = { showPollCreatorModal = true },
                        onOpenGroupEventDialog = { showGroupEventModal = true },
                        onPickPhoto = ::triggerPhotoPicker,
                        onPickDocument = ::triggerDocumentPicker,
                        onSendLocation = ::triggerLocationSharingWithPermission,
                        onInputMessageChange = { text ->
                            inputMessage = text
                            activeConversation?.let {
                                chatViewModel.updateDraft(it.id, text, replyTarget?.id, replyTarget?.content?.take(50))
                            }
                        },
                        onClearReplyTarget = {
                            replyTarget = null
                            editingMessage = null
                        },
                        onSendMessage = ::submitCurrentMessage,
                        onStartVoiceRecording = { isRecordingVoiceNote = true },
                        onCancelVoiceRecording = { isRecordingVoiceNote = false },
                        onSendVoiceNote = { dur, wave ->
                            isRecordingVoiceNote = false
                            chatViewModel.sendVoiceNote(dur, wave)
                        },
                        onReplyToMessage = { msg -> replyTarget = msg },
                        onEditMessage = { msg ->
                            editingMessage = msg
                            inputMessage = msg.content
                        },
                        onForwardMessage = { msg -> forwardingMessage = msg },
                        onConfirmForwardTo = { targetConvId ->
                            forwardingMessage?.let { chatViewModel.forwardMessage(it, targetConvId) }
                            forwardingMessage = null
                        },
                        onCancelForward = { forwardingMessage = null },
                        onDeleteMessage = { msg, forAll -> chatViewModel.deleteMessage(msg.id, forAll) },
                        onToggleStar = { msg -> chatViewModel.toggleStarred(msg.id) },
                        onTogglePin = { msg -> chatViewModel.togglePinned(msg.id) },
                        onReactToMessage = { msg, emoji -> chatViewModel.toggleReaction(msg.id, emoji) },
                        onVotePoll = { pollId, optionId, allowMulti ->
                            chatViewModel.voteOnPoll(pollId, optionId, allowMulti)
                        },
                        onOpenViewOnceMessage = { msg -> chatViewModel.openViewOnceMessage(msg.id) },
                        onStopLiveLocation = { msg -> chatViewModel.stopLiveLocation(msg.id) },
                        onRsvpGroupEvent = { msg, status -> chatViewModel.rsvpToGroupEvent(msg.id, status) },
                        onCopyMessage = { text ->
                            clipboard.setText(AnnotatedString(text))
                            chatViewModel.postStatusBanner("Copied to clipboard")
                        },
                        onOpenNotesStudio = onOpenNotesStudio,
                        onOpenPracticeMode = onOpenPracticeMode,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun WhatsAppConversationPane(
    activeConversation: ConversationEntity?,
    conversations: List<ConversationEntity>,
    members: List<ConversationMemberEntity>,
    messages: List<MessageEntity>,
    attachments: Map<String, com.example.core.database.AttachmentEntity>,
    reactionsByMessageId: Map<String, List<com.example.core.database.MessageReactionEntity>>,
    starredIds: Set<String>,
    pinnedIds: Set<String>,
    pollsById: Map<String, com.example.core.database.PollEntity>,
    pollOptionsByPollId: Map<String, List<com.example.core.database.PollOptionEntity>>,
    pollVotesByPollId: Map<String, List<com.example.core.database.PollVoteEntity>>,
    inputMessage: String,
    replyTarget: MessageEntity?,
    editingMessage: MessageEntity?,
    forwardingMessage: MessageEntity?,
    isRecordingVoiceNote: Boolean,
    showAttachmentTray: Boolean,
    showSearchBarInThread: Boolean,
    searchQuery: String,
    selectedMessageTypeFilter: String?,
    showStarredOnly: Boolean,
    showPinnedOnly: Boolean,
    onBackToChatsList: () -> Unit,
    onOpenContactOrGroupInfo: () -> Unit,
    onSelectConversation: (String) -> Unit,
    onOpenQrOtpDialog: () -> Unit,
    onOpenNewGroupDialog: () -> Unit,
    onOpenPrivacyDialog: () -> Unit,
    onToggleSearchBar: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectMessageTypeFilter: (String?) -> Unit,
    onToggleStarredOnly: () -> Unit,
    onTogglePinnedOnly: () -> Unit,
    onToggleMuteConversation: () -> Unit,
    onToggleArchiveConversation: () -> Unit,
    onToggleBlockConversation: () -> Unit,
    onExportConversation: () -> Unit,
    onClearConversation: () -> Unit,
    onToggleAttachmentTray: () -> Unit,
    onOpenFormulaCodeModal: () -> Unit,
    onOpenPollDialog: () -> Unit,
    onOpenGroupEventDialog: () -> Unit,
    onPickPhoto: () -> Unit,
    onPickDocument: () -> Unit,
    onSendLocation: () -> Unit,
    onInputMessageChange: (String) -> Unit,
    onClearReplyTarget: () -> Unit,
    onSendMessage: () -> Unit,
    onStartVoiceRecording: () -> Unit,
    onCancelVoiceRecording: () -> Unit,
    onSendVoiceNote: (Long, String) -> Unit,
    onReplyToMessage: (MessageEntity) -> Unit,
    onEditMessage: (MessageEntity) -> Unit,
    onForwardMessage: (MessageEntity) -> Unit,
    onConfirmForwardTo: (String) -> Unit,
    onCancelForward: () -> Unit,
    onDeleteMessage: (MessageEntity, Boolean) -> Unit,
    onToggleStar: (MessageEntity) -> Unit,
    onTogglePin: (MessageEntity) -> Unit,
    onReactToMessage: (MessageEntity, String) -> Unit,
    onVotePoll: (String, String, Boolean) -> Unit,
    onOpenViewOnceMessage: (MessageEntity) -> Unit,
    onStopLiveLocation: (MessageEntity) -> Unit,
    onRsvpGroupEvent: (MessageEntity, String) -> Unit,
    onCopyMessage: (String) -> Unit,
    onOpenNotesStudio: () -> Unit,
    onOpenPracticeMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.US) }
    var showTopMoreMenu by remember { mutableStateOf(false) }

    val isGroupChat = activeConversation?.conversationType == ConversationType.GROUP.name ||
        activeConversation?.conversationType == ConversationType.BROADCAST.name

    // Detect if the user is currently typing an @mention in a group chat
    val mentionQuery = remember(inputMessage, isGroupChat) {
        if (!isGroupChat) null
        else {
            val lastAt = inputMessage.lastIndexOf('@')
            if (lastAt >= 0 && (lastAt == 0 || inputMessage[lastAt - 1].isWhitespace())) {
                val tail = inputMessage.substring(lastAt + 1)
                if (!tail.contains(' ')) tail else null
            } else null
        }
    }

    val matchingMembers = remember(mentionQuery, members) {
        if (mentionQuery == null) emptyList()
        else members.filter { it.displayName.contains(mentionQuery, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(Color.White)
    ) {
        // 1. WhatsApp Conversation Top App Bar (Back Arrow + Clickable Avatar + Contact Name & Status + Actions)
        Surface(
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBackToChatsList,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("developer_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Chats",
                            tint = PrimaryBlue
                        )
                    }

                    // Clickable Contact/Group Header Area -> opens ContactAndGroupInfoSheet
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onOpenContactOrGroupInfo)
                            .padding(vertical = 2.dp, horizontal = 4.dp)
                            .testTag("open_contact_group_info_header")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(LightBlueSurface)
                                .border(1.5.dp, PrimaryBlue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            when (activeConversation?.conversationType) {
                                ConversationType.SELF.name -> Icon(
                                    Icons.Filled.Bookmark,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                ConversationType.GROUP.name, ConversationType.BROADCAST.name -> Icon(
                                    Icons.Filled.Groups,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                                else -> Text(
                                    text = activeConversation?.avatarInitials ?: "MG",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = PrimaryBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = activeConversation?.title ?: "Chats",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = DarkSlate,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if ((activeConversation?.disappearingHours ?: 0) > 0) {
                                    Icon(
                                        imageVector = Icons.Filled.Timer,
                                        contentDescription = "Disappearing Messages Active",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                if (activeConversation?.isLocked == true) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = "Chat Locked",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            Text(
                                text = when {
                                    activeConversation?.isTyping == true -> "typing..."
                                    activeConversation?.conversationType == ConversationType.GROUP.name ->
                                        "${members.size} members · tap for group info & admin"
                                    activeConversation?.conversationType == ConversationType.SELF.name ->
                                        "message yourself · personal vault"
                                    else -> "${activeConversation?.lastSeenText ?: "online"} · tap for info"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontStyle = if (activeConversation?.isTyping == true) FontStyle.Italic else FontStyle.Normal
                                ),
                                color = if (activeConversation?.isTyping == true) PrimaryBlue else SecondaryBlue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Right Header Actions (QR/OTP Link, Search, Info, More Menu)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onOpenQrOtpDialog,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("thread_qr_otp_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCode2,
                            contentDescription = "Pair QR or OTP",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleSearchBar,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("chat_search_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search in chat",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showTopMoreMenu = true },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("chat_more_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More options",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(21.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showTopMoreMenu,
                            onDismissRequest = { showTopMoreMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isGroupChat) "Group Info & Admin" else "Contact Info & Media") },
                                leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showTopMoreMenu = false
                                    onOpenContactOrGroupInfo()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("All Chats") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showTopMoreMenu = false
                                    onBackToChatsList()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("New Study Group / Broadcast") },
                                leadingIcon = { Icon(Icons.Filled.GroupAdd, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showTopMoreMenu = false
                                    onOpenNewGroupDialog()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Create Group Event (RSVP)") },
                                leadingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showTopMoreMenu = false
                                    onOpenGroupEventDialog()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Create Group Poll") },
                                leadingIcon = { Icon(Icons.Filled.Poll, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showTopMoreMenu = false
                                    onOpenPollDialog()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (showStarredOnly) "Show All Messages" else "Starred Messages") },
                                leadingIcon = {
                                    Icon(
                                        if (showStarredOnly) Icons.Filled.Star else Icons.Filled.StarBorder,
                                        contentDescription = null,
                                        tint = PrimaryBlue
                                    )
                                },
                                onClick = {
                                    showTopMoreMenu = false
                                    onToggleStarredOnly()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (showPinnedOnly) "Show All Messages" else "Pinned Messages") },
                                leadingIcon = { Icon(Icons.Filled.PushPin, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showTopMoreMenu = false
                                    onTogglePinnedOnly()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (activeConversation?.isMuted == true) "Unmute Notifications" else "Mute Notifications") },
                                leadingIcon = { Icon(Icons.Filled.NotificationsOff, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showTopMoreMenu = false
                                    onToggleMuteConversation()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (activeConversation?.isArchived == true) "Unarchive Chat" else "Archive Chat") },
                                leadingIcon = { Icon(Icons.Filled.Archive, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showTopMoreMenu = false
                                    onToggleArchiveConversation()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export Chat (Markdown)") },
                                leadingIcon = { Icon(Icons.Filled.IosShare, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showTopMoreMenu = false
                                    onExportConversation()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Wallpaper & Privacy") },
                                leadingIcon = { Icon(Icons.Filled.Security, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showTopMoreMenu = false
                                    onOpenPrivacyDialog()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear Chat", color = Color(0xFFD93025)) },
                                leadingIcon = { Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = Color(0xFFD93025)) },
                                onClick = {
                                    showTopMoreMenu = false
                                    onClearConversation()
                                }
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = BorderBlue.copy(alpha = 0.7f))

        // Optional Search Bar inside Thread
        if (showSearchBarInThread) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search in conversation...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = PrimaryBlue) },
                    trailingIcon = {
                        IconButton(onClick = {
                            onSearchQueryChange("")
                            onSelectMessageTypeFilter(null)
                            onToggleSearchBar()
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close search", tint = PrimaryBlue)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("thread_message_search_input")
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        null to "All",
                        ChatMessageType.LATEX_EQUATION.name to "Equations",
                        ChatMessageType.CODE_SNIPPET.name to "Code",
                        ChatMessageType.POLL.name to "Polls",
                        ChatMessageType.GROUP_EVENT.name to "Events",
                        ChatMessageType.VOICE_MESSAGE.name to "Voice"
                    ).forEach { (typeKey, label) ->
                        val active = selectedMessageTypeFilter == typeKey
                        Surface(
                            onClick = { onSelectMessageTypeFilter(typeKey) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (active) PrimaryBlue else LightBlueSurface
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (active) Color.White else PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = BorderBlue)
        }

        // Forward Message Target Strip (when forwarding)
        if (forwardingMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightBlueSurface)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Forward to:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    conversations.forEach { targetConv ->
                        Surface(
                            onClick = { onConfirmForwardTo(targetConv.id) },
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryBlue
                        ) {
                            Text(
                                text = targetConv.title.take(18),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                IconButton(onClick = onCancelForward, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Cancel forward", tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                }
            }
            HorizontalDivider(color = BorderBlue)
        }

        // Pinned Group Announcement Banner (if present)
        activeConversation?.announcementText?.let { announcement ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightBlueSurface.copy(alpha = 0.7f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.PushPin, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                Text(
                    text = announcement,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            HorizontalDivider(color = BorderBlue.copy(alpha = 0.5f))
        }

        // 2. WhatsApp Message Stream with Subtle Blueprint Doodle Pattern Wallpaper
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(WallpaperTint)
                .drawBehind {
                    val step = 44.dp.toPx()
                    val dotColor = PrimaryBlue.copy(alpha = 0.045f)
                    var y = step / 2f
                    var rowIdx = 0
                    while (y < size.height) {
                        var x = if (rowIdx % 2 == 0) step / 2f else step
                        while (x < size.width) {
                            drawCircle(
                                color = dotColor,
                                radius = 2.2.dp.toPx(),
                                center = Offset(x, y)
                            )
                            x += step
                        }
                        y += step
                        rowIdx++
                    }
                }
                .testTag("chat_messages_list"),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // WhatsApp Date Pill
            item(key = "date_separator_today") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        shadowElevation = 0.5.dp,
                        border = BorderStroke(0.8.dp, BorderBlue)
                    ) {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = SecondaryBlue,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                val isStarred = msg.id in starredIds
                val isPinned = msg.id in pinnedIds
                val reactions = reactionsByMessageId[msg.id].orEmpty()
                val attachment = msg.attachmentId?.let { attachments[it] }
                val poll = msg.pollId?.let { pollsById[it] }
                val pollOptions = msg.pollId?.let { pollOptionsByPollId[it] }.orEmpty()
                val pollVotes = msg.pollId?.let { pollVotesByPollId[it] }.orEmpty()
                val timeStr = timeFormat.format(Date(msg.createdAt))

                if (msg.messageType == ChatMessageType.SYSTEM_NOTIFICATION.name) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LightBlueSurface,
                            border = BorderStroke(0.8.dp, BorderBlue)
                        ) {
                            Text(
                                text = msg.content,
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    WhatsAppMessageBubble(
                        message = msg,
                        showSenderName = isGroupChat && !msg.isOutgoing,
                        timeLabel = timeStr,
                        isStarred = isStarred,
                        isPinned = isPinned,
                        reactions = reactions,
                        attachment = attachment,
                        poll = poll,
                        pollOptions = pollOptions,
                        pollVotes = pollVotes,
                        onReply = { onReplyToMessage(msg) },
                        onEdit = { onEditMessage(msg) },
                        onForward = { onForwardMessage(msg) },
                        onDeleteForMe = { onDeleteMessage(msg, false) },
                        onDeleteForEveryone = { onDeleteMessage(msg, true) },
                        onToggleStar = { onToggleStar(msg) },
                        onTogglePin = { onTogglePin(msg) },
                        onReact = { emoji -> onReactToMessage(msg, emoji) },
                        onVotePoll = { optId ->
                            if (poll != null) onVotePoll(poll.id, optId, poll.allowMultiple)
                        },
                        onOpenViewOnce = { onOpenViewOnceMessage(msg) },
                        onStopLiveLocation = { onStopLiveLocation(msg) },
                        onRsvpGroupEvent = { status -> onRsvpGroupEvent(msg, status) },
                        onCopy = { onCopyMessage(msg.latexFormula ?: msg.codeContent ?: msg.content) },
                        onOpenNotesStudio = onOpenNotesStudio,
                        onOpenPracticeMode = onOpenPracticeMode
                    )
                }
            }
        }

        // @Mention Autocomplete Strip (when typing @ in a study group)
        if (matchingMembers.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightBlueSurface)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mention:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
                matchingMembers.forEach { member ->
                    val handle = member.displayName.replace(" ", "_")
                    Surface(
                        onClick = {
                            val lastAt = inputMessage.lastIndexOf('@')
                            if (lastAt >= 0) {
                                onInputMessageChange(inputMessage.substring(0, lastAt) + "@$handle ")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, PrimaryBlue)
                    ) {
                        Text(
                            text = "@${member.displayName}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Reply or Edit Preview Strip (above Composer)
        if (replyTarget != null || editingMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightBlueSurface)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(32.dp)
                            .background(PrimaryBlue, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (editingMessage != null) {
                                "Editing Message"
                            } else {
                                replyTarget?.senderName ?: "Reply"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = PrimaryBlue
                        )
                        Text(
                            text = (editingMessage?.content ?: replyTarget?.content).orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkSlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(onClick = onClearReplyTarget, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                }
            }
        }

        // 3. WhatsApp Bottom Composer Bar (or Blocked / Admin-only Banner)
        if (activeConversation?.isBlocked == true) {
            Surface(
                onClick = onToggleBlockConversation,
                color = LightBlueSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("unblock_contact_banner")
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🚫 You blocked this contact. Tap here to unblock.",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryBlue
                    )
                }
            }
        } else if (isRecordingVoiceNote) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WallpaperTint)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                VoiceNoteRecorderBar(
                    onCancel = onCancelVoiceRecording,
                    onSendVoiceNote = onSendVoiceNote
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WallpaperTint)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rounded Pill Input Container (matching WhatsApp's composer bar)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderBlue),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left '+' / Attachment Tray Toggle Button inside pill
                        IconButton(
                            onClick = onToggleAttachmentTray,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("chat_plus_tray_button")
                        ) {
                            Icon(
                                imageVector = if (showAttachmentTray) Icons.Filled.Close else Icons.Filled.Add,
                                contentDescription = "Toggle Attachment Tray",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Message Text Input (supports *bold*, _italic_, ~strike~, `code`, @mention)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (inputMessage.isEmpty()) {
                                Text(
                                    text = "Message (*bold*, _italic_, @mention)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            BasicTextField(
                                value = inputMessage,
                                onValueChange = onInputMessageChange,
                                singleLine = false,
                                maxLines = 4,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = DarkSlate),
                                cursorBrush = SolidColor(PrimaryBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("chat_input_field")
                            )
                        }

                        // Paperclip Attachment Icon inside pill
                        IconButton(
                            onClick = onToggleAttachmentTray,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("chat_open_attachment_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AttachFile,
                                contentDescription = "Attach files, photos, polls, or equations",
                                tint = MutedGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Camera Icon inside pill (when text is empty)
                        if (inputMessage.isBlank()) {
                            IconButton(
                                onClick = onPickPhoto,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("chat_camera_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = "Camera / Photo",
                                    tint = MutedGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Voice Note Mic Icon button when empty, or Circular Send Button when typing
                if (inputMessage.isBlank()) {
                    Surface(
                        onClick = onStartVoiceRecording,
                        shape = CircleShape,
                        color = PrimaryBlue,
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("chat_voice_record_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = "Record Voice Message",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                } else {
                    Surface(
                        onClick = onSendMessage,
                        shape = CircleShape,
                        color = PrimaryBlue,
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("chat_send_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Message",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. WhatsApp Expandable Bottom Attachment Grid Tray (Matches the 2x3 circular icon grid in the screenshot!)
        AnimatedVisibility(
            visible = showAttachmentTray,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            WhatsAppAttachmentGridTray(
                onPickPhotos = onPickPhoto,
                onOpenCamera = onPickPhoto,
                onShareLocation = onSendLocation,
                onPickDocument = onPickDocument,
                onOpenPoll = onOpenPollDialog,
                onOpenGroupEvent = onOpenGroupEventDialog,
                onOpenFormulaOrCode = onOpenFormulaCodeModal
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WhatsAppAttachmentGridTray(
    onPickPhotos: () -> Unit,
    onOpenCamera: () -> Unit,
    onShareLocation: () -> Unit,
    onPickDocument: () -> Unit,
    onOpenPoll: () -> Unit,
    onOpenGroupEvent: () -> Unit,
    onOpenFormulaOrCode: () -> Unit
) {
    val items = listOf(
        AttachmentTrayItem("Photos/Video", Icons.Filled.PhotoLibrary, Color(0xFF0052CC), onPickPhotos),
        AttachmentTrayItem("Camera", Icons.Filled.CameraAlt, PrimaryBlue, onOpenCamera),
        AttachmentTrayItem("Location", Icons.Filled.LocationOn, Color(0xFF0284C7), onShareLocation),
        AttachmentTrayItem("Document", Icons.Filled.Description, SecondaryBlue, onPickDocument),
        AttachmentTrayItem("Contact", Icons.Filled.Person, Color(0xFF0369A1), onOpenFormulaOrCode),
        AttachmentTrayItem("Audio", Icons.Filled.Headphones, Color(0xFF1D4ED8), onOpenFormulaOrCode),
        AttachmentTrayItem("Poll", Icons.Filled.Poll, Color(0xFF2563EB), onOpenPoll),
        AttachmentTrayItem("Event", Icons.Filled.CalendarMonth, Color(0xFF1D4ED8), onOpenGroupEvent),
        AttachmentTrayItem("Notes/LaTeX", Icons.Filled.Functions, Color(0xFF1E40AF), onOpenFormulaOrCode)
    )

    Surface(
        color = Color.White,
        border = BorderStroke(1.dp, BorderBlue),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("whatsapp_attachment_tray")
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cols = if (maxWidth >= 500.dp) 5 else 3
            val itemWidth = ((maxWidth - 40.dp) / cols).coerceIn(72.dp, 108.dp)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                maxItemsInEachRow = cols
            ) {
                items.forEach { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .width(itemWidth)
                            .clickable { item.onClick() }
                    ) {
                        Surface(
                            onClick = item.onClick,
                            shape = RoundedCornerShape(18.dp),
                            color = LightBlueSurface,
                            border = BorderStroke(1.dp, BorderBlue),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = item.tint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            ),
                            color = DarkSlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private data class AttachmentTrayItem(
    val label: String,
    val icon: ImageVector,
    val tint: Color,
    val onClick: () -> Unit
)

@Composable
private fun WhatsAppMessageBubble(
    message: MessageEntity,
    showSenderName: Boolean,
    timeLabel: String,
    isStarred: Boolean,
    isPinned: Boolean,
    reactions: List<com.example.core.database.MessageReactionEntity>,
    attachment: com.example.core.database.AttachmentEntity?,
    poll: com.example.core.database.PollEntity?,
    pollOptions: List<com.example.core.database.PollOptionEntity>,
    pollVotes: List<com.example.core.database.PollVoteEntity>,
    onReply: () -> Unit,
    onEdit: () -> Unit,
    onForward: () -> Unit,
    onDeleteForMe: () -> Unit,
    onDeleteForEveryone: () -> Unit,
    onToggleStar: () -> Unit,
    onTogglePin: () -> Unit,
    onReact: (String) -> Unit,
    onVotePoll: (String) -> Unit,
    onOpenViewOnce: () -> Unit,
    onStopLiveLocation: () -> Unit,
    onRsvpGroupEvent: (String) -> Unit,
    onCopy: () -> Unit,
    onOpenNotesStudio: () -> Unit,
    onOpenPracticeMode: () -> Unit
) {
    val isUser = message.isOutgoing
    var showBubbleInteractionBar by remember { mutableStateOf(false) }
    var showMoreDropdown by remember { mutableStateOf(false) }

    val msgType = remember(message.messageType) {
        runCatching { ChatMessageType.valueOf(message.messageType) }.getOrDefault(ChatMessageType.PLAIN_TEXT)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val maxBubbleWidth = (maxWidth * 0.85f).coerceIn(180.dp, 460.dp)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
        // Floating WhatsApp Emoji Reaction Pill + Quick Actions (shown when user taps the bubble)
        AnimatedVisibility(visible = showBubbleInteractionBar && !message.deletedForEveryone) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                shadowElevation = 3.dp,
                border = BorderStroke(1.dp, BorderBlue),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("👍", "❤️", "😂", "😮", "🙏", "∑").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 16.sp,
                            modifier = Modifier.clickable {
                                onReact(emoji)
                                showBubbleInteractionBar = false
                            }
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(18.dp)
                            .background(BorderBlue)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = "Reply",
                        tint = PrimaryBlue,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                onReply()
                                showBubbleInteractionBar = false
                            }
                    )
                    Icon(
                        imageVector = if (isStarred) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "Star",
                        tint = PrimaryBlue,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                onToggleStar()
                                showBubbleInteractionBar = false
                            }
                    )
                    Box {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "More message actions",
                            tint = PrimaryBlue,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { showMoreDropdown = true }
                                .testTag("msg_more_actions_${message.id}")
                        )
                        DropdownMenu(
                            expanded = showMoreDropdown,
                            onDismissRequest = { showMoreDropdown = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Reply") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showMoreDropdown = false
                                    showBubbleInteractionBar = false
                                    onReply()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Copy") },
                                leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showMoreDropdown = false
                                    showBubbleInteractionBar = false
                                    onCopy()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Forward") },
                                leadingIcon = { Icon(Icons.Filled.IosShare, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showMoreDropdown = false
                                    showBubbleInteractionBar = false
                                    onForward()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isStarred) "Unstar" else "Star") },
                                leadingIcon = { Icon(Icons.Filled.Star, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showMoreDropdown = false
                                    showBubbleInteractionBar = false
                                    onToggleStar()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isPinned) "Unpin" else "Pin") },
                                leadingIcon = { Icon(Icons.Filled.PushPin, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showMoreDropdown = false
                                    showBubbleInteractionBar = false
                                    onTogglePin()
                                }
                            )
                            if (isUser) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null, tint = PrimaryBlue) },
                                    onClick = {
                                        showMoreDropdown = false
                                        showBubbleInteractionBar = false
                                        onEdit()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Delete for me") },
                                leadingIcon = { Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = Color(0xFFD93025)) },
                                onClick = {
                                    showMoreDropdown = false
                                    showBubbleInteractionBar = false
                                    onDeleteForMe()
                                }
                            )
                            if (isUser) {
                                DropdownMenuItem(
                                    text = { Text("Delete for everyone", color = Color(0xFFD93025)) },
                                    leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFD93025)) },
                                    onClick = {
                                        showMoreDropdown = false
                                        showBubbleInteractionBar = false
                                        onDeleteForEveryone()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Main WhatsApp Message Bubble
        Surface(
            onClick = { showBubbleInteractionBar = !showBubbleInteractionBar },
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) OutgoingBubbleBg else IncomingBubbleBg,
            shadowElevation = 0.5.dp,
            border = BorderStroke(
                width = 0.8.dp,
                color = if (isUser) PrimaryBlue.copy(alpha = 0.35f) else BorderBlue
            ),
            modifier = Modifier
                .widthIn(min = 90.dp, max = maxBubbleWidth)
                .testTag("chat_bubble_${message.id}")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Sender name only in Group incoming messages (just like WhatsApp)
                if (showSenderName) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = PrimaryBlue
                    )
                }

                // Forwarded tag
                if (!message.forwardedFrom.isNullOrBlank()) {
                    Text(
                        text = "↪ Forwarded",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = SecondaryBlue
                    )
                }

                // Quoted Reply Preview inside bubble
                if (!message.replyToSnippet.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isUser) Color.White.copy(alpha = 0.7f) else LightBlueSurface)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(22.dp)
                                .background(PrimaryBlue, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = message.replyToSnippet,
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkSlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // View Once Media Self-Destructing Bubble (if isViewOnce == true)
                if (message.isViewOnce) {
                    ChatViewOnceMediaBubble(
                        message = message,
                        onOpenViewOnce = onOpenViewOnce
                    )
                } else {
                    // Rich-Formatted Message Body Text (*bold*, _italic_, ~strike~, `code`, @mention)
                    if (message.content.isNotBlank() && msgType != ChatMessageType.POLL && msgType != ChatMessageType.GROUP_EVENT) {
                        val styledText = remember(message.content) {
                            parseWhatsAppRichText(message.content)
                        }
                        Text(
                            text = styledText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.5.sp,
                                lineHeight = 19.sp
                            ),
                            color = if (message.deletedForEveryone) MutedGray else DarkSlate
                        )
                    }

                    // Automatic Link Preview Card (when URL metadata is present)
                    if (!message.linkPreviewUrl.isNullOrBlank()) {
                        ChatLinkPreviewCard(
                            url = message.linkPreviewUrl,
                            host = message.linkPreviewHost ?: "web",
                            title = message.linkPreviewTitle ?: message.linkPreviewUrl,
                            onCopyUrl = onCopy
                        )
                    }

                    // Specialized Media / Poll / Event / Live Location / Voice / Equation / Code Cards
                    when (msgType) {
                        ChatMessageType.QUESTION_CARD -> {
                            val payload = SharedQuestionPayload.decodeFromPipeString(message.questionJson)
                            if (payload != null) {
                                ChatQuestionCard(
                                    payload = payload,
                                    onOpenInPractice = onOpenPracticeMode,
                                    onSaveQuestionToNote = {}
                                )
                            }
                        }
                        ChatMessageType.POLL -> {
                            if (poll != null) {
                                ChatPollCard(
                                    poll = poll,
                                    options = pollOptions,
                                    votes = pollVotes,
                                    onVote = onVotePoll
                                )
                            }
                        }
                        ChatMessageType.GROUP_EVENT -> {
                            val eventPayload = GroupEventPayload.decode(message.eventPayloadJson)
                            if (eventPayload != null) {
                                ChatGroupEventCard(
                                    payload = eventPayload,
                                    onRsvp = onRsvpGroupEvent
                                )
                            }
                        }
                        ChatMessageType.LIVE_LOCATION -> {
                            ChatLiveLocationCard(
                                message = message,
                                onStopSharing = onStopLiveLocation
                            )
                        }
                        ChatMessageType.CODE_SNIPPET -> {
                            if (!message.codeContent.isNullOrBlank()) {
                                ChatCodeSnippetCard(
                                    language = message.codeLanguage ?: "Kotlin",
                                    code = message.codeContent,
                                    onCopyCode = onCopy,
                                    onSaveToNote = {}
                                )
                            }
                        }
                        ChatMessageType.NOTE_CARD -> {
                            if (!message.sharedNoteTitle.isNullOrBlank()) {
                                ChatNoteAttachmentCard(
                                    noteTitle = message.sharedNoteTitle,
                                    summaryText = message.content,
                                    onOpenInNotesStudio = onOpenNotesStudio
                                )
                            }
                        }
                        ChatMessageType.VOICE_MESSAGE -> {
                            VoiceMessagePlaybackBubble(
                                durationMs = attachment?.durationMs ?: 4000L,
                                waveformCsv = attachment?.waveformCsv ?: "",
                                fileName = attachment?.fileName ?: "VoiceNote.m4a"
                            )
                        }
                        ChatMessageType.IMAGE,
                        ChatMessageType.VIDEO,
                        ChatMessageType.PDF,
                        ChatMessageType.DOCUMENT,
                        ChatMessageType.AUDIO_FILE,
                        ChatMessageType.FILE_ATTACHMENT,
                        ChatMessageType.LOCATION,
                        ChatMessageType.CONTACT_CARD -> {
                            ChatMediaAttachmentCard(
                                messageType = msgType,
                                attachment = attachment,
                                fallbackTitle = message.content
                            )
                        }
                        else -> Unit
                    }
                }

                // LaTeX formula card
                if (!message.latexFormula.isNullOrBlank()) {
                    ChatLatexEquationCard(
                        latex = message.latexFormula,
                        onCopyLatex = onCopy
                    )
                }

                // Bottom-Right Inline Timestamp + Star/Pin + Blue Double Checkmark (Just like WhatsApp!)
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isPinned) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "Pinned",
                            tint = SecondaryBlue,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    if (isStarred) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Starred",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    if (message.editedAt != null) {
                        Text(
                            text = "edited",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MutedGray
                        )
                    }
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MutedGray
                    )
                    if (isUser) {
                        Icon(
                            imageVector = Icons.Filled.DoneAll,
                            contentDescription = "Delivered & Read",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        // Compact Emoji Reaction Pills overlapping bottom edge of bubble
        if (reactions.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                reactions.groupBy { it.emoji }.forEach { (emoji, list) ->
                    Surface(
                        onClick = { onReact(emoji) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        border = BorderStroke(1.dp, BorderBlue)
                    ) {
                        Text(
                            text = if (list.size > 1) "$emoji ${list.size}" else emoji,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
        }
    }
}
