package com.example.feature.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.ChatDraftEntity
import com.example.core.database.ConversationEntity
import com.example.core.database.ConversationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PrimaryBlue = Color(0xFF003DA5)
private val SecondaryBlue = Color(0xFF1647BB)
private val LightBlueSurface = Color(0xFFEAF1FF)
private val SoftSearchSurface = Color(0xFFF1F5FB)
private val BorderBlue = Color(0xFFBFD4FF)
private val OnlineDotBlue = Color(0xFF0052CC)
private val MutedTextGray = Color(0xFF64748B)
private val DarkSlateText = Color(0xFF0F172A)

@Composable
fun ChatContactListPane(
    conversations: List<ConversationEntity>,
    draftsByConversationId: Map<String, ChatDraftEntity>,
    selectedConversationId: String,
    searchQuery: String,
    selectedFilter: String, // "ALL", "UNREAD", "GROUPS", "PERSONAL", "SAVED", "ARCHIVED", "LOCKED"
    myDeviceOtp: String,
    isLockedFolderUnlocked: Boolean = false,
    onSearchQueryChange: (String) -> Unit,
    onSelectFilter: (String) -> Unit,
    onSelectConversation: (String) -> Unit,
    onOpenQrAndOtpDialog: () -> Unit,
    onOpenNewGroupDialog: () -> Unit,
    onOpenCameraPicker: () -> Unit = {},
    onOpenPrivacyDialog: () -> Unit = {},
    onToggleStarredFilter: () -> Unit = {},
    onOpenStatusUpdatesDrawer: () -> Unit = {},
    onOpenCallsDrawer: () -> Unit = {},
    onRequestUnlockLockedChats: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showHeaderMenu by remember { mutableStateOf(false) }

    val lockedCount = conversations.count { it.isLocked }

    val filteredConversations = conversations.filter { conv ->
        if (selectedFilter == "LOCKED") {
            return@filter conv.isLocked && isLockedFolderUnlocked
        }
        if (conv.isLocked) {
            return@filter false
        }
        val matchesArchive = if (selectedFilter == "ARCHIVED") conv.isArchived else !conv.isArchived
        val matchesQuery = searchQuery.isBlank() ||
            conv.title.contains(searchQuery, ignoreCase = true) ||
            conv.subtitle.contains(searchQuery, ignoreCase = true) ||
            conv.pairingCodeOtp.contains(searchQuery, ignoreCase = true) ||
            conv.lastMessagePreview.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "UNREAD" -> conv.unreadCount > 0
            "GROUPS" -> conv.conversationType == ConversationType.GROUP.name || conv.conversationType == ConversationType.BROADCAST.name
            "PERSONAL" -> conv.conversationType == ConversationType.PERSONAL.name
            "SAVED" -> conv.conversationType == ConversationType.SELF.name
            "ARCHIVED" -> conv.isArchived
            else -> true
        }
        matchesArchive && matchesQuery && matchesFilter
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(Color.White)
            .testTag("whatsapp_contacts_list")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // WhatsApp-Style Top Header ("Chats" on left, Updates + Calls + QR/OTP + Camera + Plus + More on right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 12.dp, top = 12.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Chats",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp
                        ),
                        color = PrimaryBlue
                    )
                    Text(
                        text = "My OTP: ${PeerPairingEngine.formatOtpDisplay(myDeviceOtp)} · Tap QR to link peer",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = SecondaryBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Status Updates Drawer Button (24h Status Ring)
                    IconButton(
                        onClick = onOpenStatusUpdatesDrawer,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("open_status_updates_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.HistoryToggleOff,
                            contentDescription = "Status Updates",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Call Logs Drawer Button
                    IconButton(
                        onClick = onOpenCallsDrawer,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("open_calls_drawer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Voice & Video Calls",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenQrAndOtpDialog,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("open_qr_otp_modal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCode2,
                            contentDescription = "QR & OTP Pairing",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenCameraPicker,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("chats_camera_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Camera / Media",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    // WhatsApp-style circular filled "+" button in our platform PrimaryBlue
                    Surface(
                        onClick = onOpenQrAndOtpDialog,
                        shape = CircleShape,
                        color = PrimaryBlue,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("header_qr_otp_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "New Chat / Link Peer",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { showHeaderMenu = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("chats_list_more_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More options",
                                tint = PrimaryBlue
                            )
                        }

                        DropdownMenu(
                            expanded = showHeaderMenu,
                            onDismissRequest = { showHeaderMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Status Updates (24h)") },
                                leadingIcon = { Icon(Icons.Filled.HistoryToggleOff, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showHeaderMenu = false
                                    onOpenStatusUpdatesDrawer()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Voice & Video Calls") },
                                leadingIcon = { Icon(Icons.Filled.Call, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showHeaderMenu = false
                                    onOpenCallsDrawer()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("New Study Group / Broadcast") },
                                leadingIcon = { Icon(Icons.Filled.GroupAdd, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showHeaderMenu = false
                                    onOpenNewGroupDialog()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Link Device via QR / 6-Digit OTP") },
                                leadingIcon = { Icon(Icons.Filled.QrCode2, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showHeaderMenu = false
                                    onOpenQrAndOtpDialog()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Starred Messages") },
                                leadingIcon = { Icon(Icons.Filled.Star, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showHeaderMenu = false
                                    onToggleStarredFilter()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Locked Chats (PIN)") },
                                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showHeaderMenu = false
                                    onRequestUnlockLockedChats()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Privacy & Theme Settings") },
                                leadingIcon = { Icon(Icons.Filled.Security, contentDescription = null, tint = PrimaryBlue) },
                                onClick = {
                                    showHeaderMenu = false
                                    onOpenPrivacyDialog()
                                }
                            )
                        }
                    }
                }
            }

            // WhatsApp-Style Rounded Pill Search Bar
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SoftSearchSurface,
                border = BorderStroke(1.dp, BorderBlue.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .height(44.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(2.5.dp, PrimaryBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search chats, contacts, or OTP...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedTextGray
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = DarkSlateText),
                            cursorBrush = SolidColor(PrimaryBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("whatsapp_search_input")
                        )
                    }

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear search",
                                tint = MutedTextGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // WhatsApp-Style Filter Pills: All, Unread, Groups, Personal, Saved, Archived, Locked
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val unreadTotal = conversations.count { !it.isArchived && !it.isLocked && it.unreadCount > 0 }
                listOf(
                    "ALL" to "All",
                    "UNREAD" to if (unreadTotal > 0) "Unread $unreadTotal" else "Unread",
                    "GROUPS" to "Groups",
                    "PERSONAL" to "Personal",
                    "SAVED" to "Saved",
                    "ARCHIVED" to "Archived",
                    "LOCKED" to if (lockedCount > 0) "🔒 Locked ($lockedCount)" else "🔒 Locked"
                ).forEach { (id, label) ->
                    val active = selectedFilter == id
                    Surface(
                        onClick = {
                            if (id == "LOCKED" && !isLockedFolderUnlocked) {
                                onRequestUnlockLockedChats()
                            } else {
                                onSelectFilter(id)
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        color = if (active) LightBlueSurface else SoftSearchSurface,
                        border = BorderStroke(
                            width = if (active) 1.5.dp else 1.dp,
                            color = if (active) PrimaryBlue else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.testTag("chat_filter_$id")
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold
                            ),
                            color = if (active) PrimaryBlue else MutedTextGray,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // WhatsApp "Locked Chats" Folder Row at top of list if locked chats exist
            if (lockedCount > 0 && selectedFilter != "LOCKED") {
                Surface(
                    onClick = {
                        if (isLockedFolderUnlocked) {
                            onSelectFilter("LOCKED")
                        } else {
                            onRequestUnlockLockedChats()
                        }
                    },
                    color = LightBlueSurface.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("locked_chats_folder_row")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(LightBlueSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Locked Chats",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Locked Chats",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryBlue
                                )
                                Text(
                                    text = if (isLockedFolderUnlocked) "Unlocked · Tap to view $lockedCount chat(s)" else "PIN protected · Tap to unlock",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SecondaryBlue
                                )
                            }
                        }
                        Text(
                            text = "$lockedCount",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = PrimaryBlue
                        )
                    }
                }
                HorizontalDivider(color = BorderBlue.copy(alpha = 0.5f))
            }

            // Conversation List (Exact WhatsApp Row Layout)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 84.dp)
            ) {
                items(filteredConversations, key = { it.id }) { conv ->
                    val isSelected = conv.id == selectedConversationId
                    val draft = draftsByConversationId[conv.id]
                    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.US) }
                    val timeLabel = timeFormatter.format(Date(conv.lastMessageAt))
                    val hasUnread = conv.unreadCount > 0

                    Surface(
                        onClick = { onSelectConversation(conv.id) },
                        color = if (isSelected) LightBlueSurface.copy(alpha = 0.55f) else Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("whatsapp_contact_${conv.id}")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left: 52.dp Circular Avatar with Status Ring & Online/Disappearing Indicator
                                Box(modifier = Modifier.size(54.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(LightBlueSurface)
                                            .border(
                                                width = if (hasUnread) 2.dp else 1.2.dp,
                                                color = if (hasUnread) PrimaryBlue else BorderBlue,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when (conv.conversationType) {
                                            ConversationType.SELF.name -> Icon(
                                                Icons.Filled.Bookmark,
                                                contentDescription = "Saved Messages",
                                                tint = PrimaryBlue,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            ConversationType.GROUP.name, ConversationType.BROADCAST.name -> Icon(
                                                Icons.Filled.Groups,
                                                contentDescription = "Study Group",
                                                tint = PrimaryBlue,
                                                modifier = Modifier.size(26.dp)
                                            )
                                            else -> Text(
                                                text = conv.avatarInitials,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.ExtraBold
                                                ),
                                                color = PrimaryBlue
                                            )
                                        }
                                    }

                                    // Online Dot or Disappearing Timer Badge at bottom-right of avatar
                                    if (conv.disappearingHours > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .align(Alignment.BottomEnd)
                                                .clip(CircleShape)
                                                .background(PrimaryBlue),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Timer,
                                                contentDescription = "Disappearing Messages",
                                                tint = Color.White,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    } else if (conv.conversationType == ConversationType.PERSONAL.name && !conv.isBlocked) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .align(Alignment.BottomEnd)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                                .padding(2.dp)
                                                .clip(CircleShape)
                                                .background(OnlineDotBlue)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                // Middle + Right: Name & Timestamp on top row, Preview & Pin/Unread Badge on bottom row
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Top Row: Contact/Group Name + Timestamp
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = conv.title,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            ),
                                            color = DarkSlateText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = timeLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp
                                            ),
                                            color = if (hasUnread) PrimaryBlue else MutedTextGray
                                        )
                                    }

                                    // Bottom Row: Typing / Draft / Double-Tick + Message Preview on left, Mute / Pin / Unread Count Badge on right
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            when {
                                                conv.isTyping -> {
                                                    Text(
                                                        text = "typing...",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontStyle = FontStyle.Italic,
                                                            fontSize = 14.sp
                                                        ),
                                                        color = PrimaryBlue
                                                    )
                                                }
                                                draft != null && draft.draftText.isNotBlank() -> {
                                                    Text(
                                                        text = "Draft: ",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp
                                                        ),
                                                        color = Color(0xFFD93025)
                                                    )
                                                    Text(
                                                        text = draft.draftText,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                                        color = MutedTextGray,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                else -> {
                                                    if (!hasUnread) {
                                                        Icon(
                                                            imageVector = Icons.Filled.DoneAll,
                                                            contentDescription = "Read",
                                                            tint = PrimaryBlue,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = conv.lastMessagePreview,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                                                            fontSize = 14.sp
                                                        ),
                                                        color = if (hasUnread) DarkSlateText else MutedTextGray,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (conv.isLocked) {
                                                Icon(
                                                    imageVector = Icons.Filled.Lock,
                                                    contentDescription = "Locked",
                                                    tint = PrimaryBlue,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            if (conv.isMuted) {
                                                Icon(
                                                    imageVector = Icons.Filled.NotificationsOff,
                                                    contentDescription = "Muted",
                                                    tint = MutedTextGray,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                            if (conv.isPinned) {
                                                Icon(
                                                    imageVector = Icons.Filled.PushPin,
                                                    contentDescription = "Pinned",
                                                    tint = MutedTextGray,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                            if (conv.isArchived) {
                                                Icon(
                                                    imageVector = Icons.Filled.Archive,
                                                    contentDescription = "Archived",
                                                    tint = SecondaryBlue,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                            if (hasUnread) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .clip(CircleShape)
                                                        .background(PrimaryBlue),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = conv.unreadCount.toString(),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp
                                                        ),
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                color = Color(0xFFF1F5F9),
                                thickness = 1.dp,
                                modifier = Modifier.padding(start = 82.dp, end = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        // WhatsApp-Style Bottom-Right Floating Action Button (New Chat / Group)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                onClick = onOpenNewGroupDialog,
                shape = RoundedCornerShape(14.dp),
                color = LightBlueSurface,
                border = BorderStroke(1.2.dp, PrimaryBlue),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("open_new_group_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.GroupAdd,
                        contentDescription = "New Group",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            FloatingActionButton(
                onClick = onOpenQrAndOtpDialog,
                shape = RoundedCornerShape(16.dp),
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("whatsapp_new_chat_fab")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "New Chat / Pair QR or OTP",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
