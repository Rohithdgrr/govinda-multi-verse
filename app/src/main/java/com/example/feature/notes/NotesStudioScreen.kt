package com.example.feature.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.NoteEntity
import com.example.core.document.BlockType
import com.example.core.export.NotesExportImportManager
import com.example.feature.notes.blocks.NoteBlockItemCard
import com.example.feature.notes.features.LatexSymbolPadDialog
import com.example.feature.notes.features.NoteCommentsAndSharingDialog
import com.example.feature.notes.features.NoteDatabaseMetadata
import com.example.feature.notes.features.NoteFindAndReplaceDialog
import com.example.feature.notes.features.NoteFlashcardsDialog
import com.example.feature.notes.features.NoteKnowledgeGraphDialog
import com.example.feature.notes.features.NoteOutlineDialog
import com.example.feature.notes.features.NotesStudioHub
import com.example.feature.notes.features.NotionCodeAndEngineeringStudioHubSection
import com.example.feature.notes.features.NotionDatabasesHubSection
import com.example.feature.notes.features.NotionEquationStudioHubSection
import com.example.feature.notes.features.NotionSearchAndBacklinksHubSection
import com.example.feature.notes.features.NotionWorkspaceHubSection
import com.example.feature.notes.features.SlashCommandPaletteDialog

// Minimal Notion Palette (matching uploaded screenshots)
private val NotionHomeBg = Color(0xFFF7F7F5)
private val NotionPageBg = Color(0xFFFFFFFF)
private val NotionInk = Color(0xFF191919)
private val NotionBodyInk = Color(0xFF2F2F2F)
private val NotionMutedInk = Color(0xFF787774)
private val NotionHairline = Color(0xFFEBEBEA)
private val NotionCardCoverGray = Color(0xFFEFEFEF)
private val BlueprintPrimary = Color(0xFF003DA5)
private val BlueprintSecondary = Color(0xFF1647BB)
private val BlueprintSurface = Color(0xFFEAF1FF)

private val notionPageEmojis = listOf("👨‍💻", "🏋️", "🥦", "💰", "🥬", "🧠", "📐", "⚡", "🧮", "📘")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesStudioScreen(
    uiState: NotesStudioUiState,
    viewModel: NotesStudioViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    // Controls whether compact screens show the Notion Home screen (Screenshot 1) or the clean Page Document screen (Screenshot 2)
    var isViewingPageEditorOnCompact by remember { mutableStateOf(false) }
    var activeHub by remember { mutableStateOf(NotesStudioHub.DOCUMENTS) }

    var showPageMoreActionsSheet by remember { mutableStateOf(false) }
    var showStudioHubsPickerDialog by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var showNewTagDialog by remember { mutableStateOf(false) }
    var showExportImportDialog by remember { mutableStateOf(false) }
    var showRevisionHistoryDialog by remember { mutableStateOf(false) }
    var showAllBlockTypesPicker by remember { mutableStateOf(false) }
    var showSlashPaletteDialog by remember { mutableStateOf(false) }
    var showOutlineDialog by remember { mutableStateOf(false) }
    var showFindReplaceDialog by remember { mutableStateOf(false) }
    var showFlashcardsDialog by remember { mutableStateOf(false) }
    var showLatexPadDialog by remember { mutableStateOf(false) }
    var showGraphViewDialog by remember { mutableStateOf(false) }
    var showCommentsAndShareDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = isViewingPageEditorOnCompact || activeHub != NotesStudioHub.DOCUMENTS) {
        if (activeHub != NotesStudioHub.DOCUMENTS) {
            activeHub = NotesStudioHub.DOCUMENTS
        } else {
            isViewingPageEditorOnCompact = false
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("notes_studio_screen"),
        containerColor = if (isViewingPageEditorOnCompact && activeHub == NotesStudioHub.DOCUMENTS) NotionPageBg else NotionHomeBg
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            val isWideScreen = maxWidth >= 640.dp
            val isExpandedTablet = maxWidth >= 840.dp
            val sidebarWidth = if (isExpandedTablet) 330.dp else 270.dp

            if (isWideScreen) {
                // Wide screen / Foldable / Tablet: Notion Home Sidebar on Left + Centered Notion Document Canvas on Right
                Row(modifier = Modifier.fillMaxSize()) {
                    NotionHomeDashboardView(
                        uiState = uiState,
                        viewModel = viewModel,
                        activeHub = activeHub,
                        onOpenPage = { noteId ->
                            viewModel.openNote(noteId)
                            activeHub = NotesStudioHub.DOCUMENTS
                        },
                        onSelectHub = { hub -> activeHub = hub },
                        onNavigateBack = onNavigateBack,
                        onOpenMoreMenu = { showPageMoreActionsSheet = true },
                        onOpenFolderModal = { showNewFolderDialog = true },
                        onOpenTagModal = { showNewTagDialog = true },
                        modifier = Modifier
                            .width(sidebarWidth)
                            .fillMaxHeight()
                            .background(NotionHomeBg)
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(NotionHairline)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(NotionPageBg),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        NotionDocumentOrHubCanvas(
                            uiState = uiState,
                            viewModel = viewModel,
                            activeHub = activeHub,
                            onSelectHub = { activeHub = it },
                            onBackToHome = { activeHub = NotesStudioHub.DOCUMENTS },
                            onOpenShareSheet = { showCommentsAndShareDialog = true },
                            onOpenMoreMenu = { showPageMoreActionsSheet = true },
                            onOpenSlashPalette = { showSlashPaletteDialog = true },
                            onOpenAllBlocksPicker = { showAllBlockTypesPicker = true },
                            onOpenFolderModal = { showNewFolderDialog = true },
                            onOpenTagModal = { showNewTagDialog = true },
                            onOpenExportImportModal = { showExportImportDialog = true },
                            onOpenGraphDialog = { showGraphViewDialog = true },
                            onOpenFlashcardsModal = { showFlashcardsDialog = true },
                            onOpenOutlineDialog = { showOutlineDialog = true },
                            onOpenLatexPadDialog = { showLatexPadDialog = true },
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(max = if (activeHub == NotesStudioHub.DOCUMENTS) 780.dp else 1080.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            } else {
                // Compact screen (Phone): Two-screen Notion flow matching Screenshot 1 (Home) & Screenshot 2 (Page Editor)
                Box(modifier = Modifier.fillMaxSize()) {
                    if (!isViewingPageEditorOnCompact && activeHub == NotesStudioHub.DOCUMENTS) {
                        NotionHomeDashboardView(
                            uiState = uiState,
                            viewModel = viewModel,
                            activeHub = activeHub,
                            onOpenPage = { noteId ->
                                viewModel.openNote(noteId)
                                isViewingPageEditorOnCompact = true
                            },
                            onSelectHub = { hub ->
                                activeHub = hub
                                isViewingPageEditorOnCompact = true
                            },
                            onNavigateBack = onNavigateBack,
                            onOpenMoreMenu = { showPageMoreActionsSheet = true },
                            onOpenFolderModal = { showNewFolderDialog = true },
                            onOpenTagModal = { showNewTagDialog = true },
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NotionHomeBg)
                        )
                    } else {
                        NotionDocumentOrHubCanvas(
                            uiState = uiState,
                            viewModel = viewModel,
                            activeHub = activeHub,
                            onSelectHub = { activeHub = it },
                            onBackToHome = {
                                activeHub = NotesStudioHub.DOCUMENTS
                                isViewingPageEditorOnCompact = false
                            },
                            onOpenShareSheet = { showCommentsAndShareDialog = true },
                            onOpenMoreMenu = { showPageMoreActionsSheet = true },
                            onOpenSlashPalette = { showSlashPaletteDialog = true },
                            onOpenAllBlocksPicker = { showAllBlockTypesPicker = true },
                            onOpenFolderModal = { showNewFolderDialog = true },
                            onOpenTagModal = { showNewTagDialog = true },
                            onOpenExportImportModal = { showExportImportDialog = true },
                            onOpenGraphDialog = { showGraphViewDialog = true },
                            onOpenFlashcardsModal = { showFlashcardsDialog = true },
                            onOpenOutlineDialog = { showOutlineDialog = true },
                            onOpenLatexPadDialog = { showLatexPadDialog = true },
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NotionPageBg)
                        )
                    }

                    // Floating Notion Bottom Pill Bar (from Screenshot 2)
                    NotionFloatingBottomPill(
                        isHomeActive = !isViewingPageEditorOnCompact && activeHub == NotesStudioHub.DOCUMENTS,
                        activeHub = activeHub,
                        onTapHome = {
                            activeHub = NotesStudioHub.DOCUMENTS
                            isViewingPageEditorOnCompact = false
                        },
                        onTapSearch = {
                            activeHub = NotesStudioHub.SEARCH
                            isViewingPageEditorOnCompact = true
                        },
                        onTapHubsInbox = {
                            showStudioHubsPickerDialog = true
                        },
                        onTapComposeOrSlash = {
                            if (isViewingPageEditorOnCompact && activeHub == NotesStudioHub.DOCUMENTS) {
                                showSlashPaletteDialog = true
                            } else {
                                viewModel.createNewNote("Untitled Page")
                                activeHub = NotesStudioHub.DOCUMENTS
                                isViewingPageEditorOnCompact = true
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = 14.dp)
                    )
                }
            }
        }
    }

    // Studio Hubs Switcher Modal (Databases, Workspace, Equation Studio, Code & Eng Studio, Flashcards, Outline)
    if (showStudioHubsPickerDialog) {
        AlertDialog(
            onDismissRequest = { showStudioHubsPickerDialog = false },
            containerColor = Color.White,
            title = {
                Column {
                    Text(
                        text = "MATHGATE Notion Studio Hubs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NotionInk
                    )
                    Text(
                        text = "Switch workspace view or launch engineering & math tools",
                        style = MaterialTheme.typography.bodySmall,
                        color = NotionMutedInk
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    NotesStudioHub.entries.forEach { hub ->
                        Surface(
                            onClick = {
                                activeHub = hub
                                isViewingPageEditorOnCompact = true
                                showStudioHubsPickerDialog = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (activeHub == hub) NotionHomeBg else Color.White,
                            border = BorderStroke(1.dp, NotionHairline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("notes_hub_${hub.name}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${hub.badge}  ${hub.label}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NotionInk
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = NotionMutedInk,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStudioHubsPickerDialog = false }) {
                    Text("Close", color = NotionInk)
                }
            }
        )
    }

    // Page "•••" More Options Dialog (Undo/Redo, Reading Mode, Markdown Live, History, Export/Import, Outline, Flashcards, LaTeX Pad, Find/Replace)
    if (showPageMoreActionsSheet) {
        AlertDialog(
            onDismissRequest = { showPageMoreActionsSheet = false },
            containerColor = Color.White,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Page & Studio Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NotionInk
                    )
                    Row {
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = uiState.canUndo,
                            modifier = Modifier.testTag("notes_undo_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = if (uiState.canUndo) NotionInk else Color.LightGray)
                        }
                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = uiState.canRedo,
                            modifier = Modifier.testTag("notes_redo_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = if (uiState.canRedo) NotionInk else Color.LightGray)
                        }
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Page quick actions (Favorite, Pin, Archive, Trash)
                    val activeNote = uiState.activeNote
                    if (activeNote != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleFavouriteActiveNote() },
                                modifier = Modifier.testTag("note_favourite_button")
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = "Favorite",
                                    tint = if (activeNote.isFavourite) Color(0xFFE6A100) else NotionMutedInk
                                )
                            }
                            IconButton(
                                onClick = { viewModel.togglePinActiveNote() },
                                modifier = Modifier.testTag("note_pin_button")
                            ) {
                                Icon(
                                    Icons.Filled.PushPin,
                                    contentDescription = "Pin",
                                    tint = if (activeNote.isPinned) NotionInk else NotionMutedInk
                                )
                            }
                            IconButton(
                                onClick = { viewModel.toggleArchiveActiveNote() },
                                modifier = Modifier.testTag("note_archive_button")
                            ) {
                                Icon(
                                    Icons.Filled.Archive,
                                    contentDescription = "Archive",
                                    tint = if (activeNote.isArchived) NotionInk else NotionMutedInk
                                )
                            }
                            IconButton(
                                onClick = { viewModel.toggleTrashActiveNote() },
                                modifier = Modifier.testTag("note_trash_button")
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Trash",
                                    tint = if (activeNote.isTrashed) Color(0xFFD93025) else NotionMutedInk
                                )
                            }
                        }
                        HorizontalDivider(color = NotionHairline)
                    }

                    // Editor Modes (Blocks | Markdown Live | Reading)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "BLOCKS" to "✏️ Blocks",
                            "MARKDOWN_LIVE" to "📝 Markdown",
                            "READING" to "📖 Reading"
                        ).forEach { (key, label) ->
                            val selected = when (key) {
                                "READING" -> uiState.isReadingMode
                                "MARKDOWN_LIVE" -> uiState.isMarkdownLiveMode
                                else -> !uiState.isReadingMode && !uiState.isMarkdownLiveMode
                            }
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    viewModel.setEditorDisplayMode(key)
                                    showPageMoreActionsSheet = false
                                },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Tool items
                    listOf(
                        "/ Slash Command Palette" to { showPageMoreActionsSheet = false; showSlashPaletteDialog = true },
                        "∑ Interactive LaTeX Symbol Pad" to { showPageMoreActionsSheet = false; showLatexPadDialog = true },
                        "⚡ Active Recall Flashcards" to { showPageMoreActionsSheet = false; showFlashcardsDialog = true },
                        "≡ Document Table of Contents" to { showPageMoreActionsSheet = false; showOutlineDialog = true },
                        "🔍 Find & Replace in Page" to { showPageMoreActionsSheet = false; showFindReplaceDialog = true },
                        "◉ Knowledge Graph & Backlinks" to { showPageMoreActionsSheet = false; showGraphViewDialog = true },
                        "🕒 Version History & Snapshots" to { showPageMoreActionsSheet = false; showRevisionHistoryDialog = true },
                        "📤 Export & Import (MD, PDF, JSON, CSV)" to { showPageMoreActionsSheet = false; showExportImportDialog = true }
                    ).forEach { (label, action) ->
                        Surface(
                            onClick = action,
                            color = NotionHomeBg,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = NotionInk,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPageMoreActionsSheet = false }) {
                    Text("Done", color = NotionInk)
                }
            }
        )
    }

    if (showNewFolderDialog) {
        var folderName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("New Folder") },
            text = {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder name") },
                    singleLine = true,
                    modifier = Modifier.testTag("new_folder_name_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (folderName.isNotBlank()) {
                            viewModel.createFolder(folderName.trim())
                            showNewFolderDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_folder_button")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showNewTagDialog) {
        var tagName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewTagDialog = false },
            title = { Text("New Tag") },
            text = {
                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = { Text("Tag name") },
                    singleLine = true,
                    modifier = Modifier.testTag("new_tag_name_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tagName.isNotBlank()) {
                            viewModel.createTag(tagName.trim())
                            showNewTagDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_tag_button")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTagDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAllBlockTypesPicker) {
        AlertDialog(
            onDismissRequest = { showAllBlockTypesPicker = false },
            containerColor = Color.White,
            title = { Text("Add a block (${BlockType.entries.size} types)", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(BlockType.entries) { type ->
                        Surface(
                            onClick = {
                                viewModel.addBlock(type)
                                showAllBlockTypesPicker = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = NotionHomeBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("insert_block_type_${type.name}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${type.badge}   ${type.displayName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = NotionInk
                                )
                                Icon(Icons.Filled.Add, contentDescription = null, tint = NotionMutedInk, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAllBlockTypesPicker = false }) {
                    Text("Close", color = NotionInk)
                }
            }
        )
    }

    if (showRevisionHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showRevisionHistoryDialog = false },
            containerColor = Color.White,
            title = { Text("Page History (${uiState.activeRevisions.size})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.saveNowWithRevision() },
                        colors = ButtonDefaults.buttonColors(containerColor = NotionInk),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notes_save_revision_button")
                    ) {
                        Text("Save Snapshot")
                    }
                    if (uiState.activeRevisions.isEmpty()) {
                        Text(
                            text = "No snapshots yet. Tap above to save a point-in-time version.",
                            style = MaterialTheme.typography.bodySmall,
                            color = NotionMutedInk
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(220.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(uiState.activeRevisions, key = { it.id }) { rev ->
                                Surface(
                                    color = NotionHomeBg,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = rev.titleSnapshot,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = NotionInk
                                            )
                                            Text(
                                                text = "Snapshot #${rev.id.takeLast(6)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = NotionMutedInk
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.restoreRevision(rev)
                                                showRevisionHistoryDialog = false
                                            }
                                        ) {
                                            Text("Restore", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRevisionHistoryDialog = false }) {
                    Text("Close", color = NotionInk)
                }
            }
        )
    }

    if (showExportImportDialog) {
        var importBuffer by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = {
                viewModel.dismissExportPreview()
                showExportImportDialog = false
            },
            containerColor = Color.White,
            title = { Text("Export & Import") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Export format:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NotionInk
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("MARKDOWN", "JSON", "PDF", "CSV", "HTML", "TXT", "ZIP").forEach { fmt ->
                            OutlinedButton(
                                onClick = { viewModel.exportActiveNote(fmt, context) },
                                modifier = Modifier.testTag("export_format_$fmt")
                            ) {
                                Text(fmt, fontSize = 11.sp)
                            }
                        }
                    }

                    if (!uiState.exportedPreviewContent.isNullOrBlank()) {
                        Surface(
                            color = NotionHomeBg,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, NotionHairline)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = uiState.exportedPreviewTitle ?: "Export Output",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = NotionInk
                                    )
                                    TextButton(
                                        onClick = {
                                            clipboard.setText(AnnotatedString(uiState.exportedPreviewContent))
                                        }
                                    ) {
                                        Text("Copy", fontSize = 11.sp)
                                    }
                                }
                                Text(
                                    text = uiState.exportedPreviewContent.take(600),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 8,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.testTag("export_preview_text")
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = NotionHairline)
                    Text(
                        text = "Import Markdown or JSON:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NotionInk
                    )
                    OutlinedTextField(
                        value = importBuffer,
                        onValueChange = { importBuffer = it },
                        placeholder = { Text("# Page Title\nParagraph or \$\$E = mc^2\$\$") },
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_raw_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (importBuffer.isNotBlank()) {
                                    viewModel.importFromRawText(importBuffer, isJson = false)
                                    showExportImportDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NotionInk),
                            modifier = Modifier.testTag("import_markdown_button")
                        ) {
                            Text("Import MD", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                if (importBuffer.isNotBlank()) {
                                    viewModel.importFromRawText(importBuffer, isJson = true)
                                    showExportImportDialog = false
                                }
                            },
                            modifier = Modifier.testTag("import_json_button")
                        ) {
                            Text("Import JSON", fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissExportPreview()
                        showExportImportDialog = false
                    }
                ) {
                    Text("Done", color = NotionInk)
                }
            }
        )
    }

    if (showSlashPaletteDialog) {
        SlashCommandPaletteDialog(
            onDismiss = { showSlashPaletteDialog = false },
            onExecuteCommand = { cmd ->
                if (cmd.blockType != null) {
                    viewModel.addBlock(
                        type = cmd.blockType,
                        initialContent = cmd.presetContent,
                        metadata = cmd.metadata
                    )
                }
            },
            onDuplicateActiveNote = { viewModel.duplicateActiveNote() },
            onAutoNumberHeadings = { viewModel.autoNumberHeadings() }
        )
    }

    if (showOutlineDialog) {
        NoteOutlineDialog(
            noteTitle = uiState.activeNote?.title ?: "Document",
            blocks = uiState.activeBlocks,
            onDismiss = { showOutlineDialog = false }
        )
    }

    if (showFindReplaceDialog) {
        NoteFindAndReplaceDialog(
            blocks = uiState.activeBlocks,
            onReplaceAll = { find, rep, ignoreCase ->
                viewModel.findAndReplaceInActiveNote(find, rep, ignoreCase)
            },
            onDismiss = { showFindReplaceDialog = false }
        )
    }

    if (showFlashcardsDialog) {
        NoteFlashcardsDialog(
            noteTitle = uiState.activeNote?.title ?: "Study Deck",
            blocks = uiState.activeBlocks,
            onDismiss = { showFlashcardsDialog = false }
        )
    }

    if (showLatexPadDialog) {
        LatexSymbolPadDialog(
            onInsertLatexBlock = { latex, caption ->
                viewModel.addBlock(BlockType.LATEX_BLOCK, initialContent = latex, metadata = caption)
            },
            onDismiss = { showLatexPadDialog = false }
        )
    }

    if (showGraphViewDialog) {
        NoteKnowledgeGraphDialog(
            allNotes = uiState.allNotes,
            activeNote = uiState.activeNote,
            backlinks = uiState.activeBacklinks,
            outgoingLinks = uiState.activeOutgoingLinks,
            onSelectNote = { viewModel.openNote(it) },
            onDismiss = { showGraphViewDialog = false }
        )
    }

    if (showCommentsAndShareDialog && uiState.activeNote != null) {
        val activeNote = uiState.activeNote
        val dbMeta = NoteDatabaseMetadata.decode(activeNote.collectionName)
        NoteCommentsAndSharingDialog(
            note = activeNote,
            blocks = uiState.activeBlocks,
            comments = uiState.commentsByNoteId[activeNote.id].orEmpty(),
            accessLevel = dbMeta.accessLevel,
            onAddComment = { mention, commentText ->
                viewModel.addCommentToActiveNote(mention, commentText)
            },
            onUpdateAccessLevel = { newAccess ->
                viewModel.updateNoteCollectionMetadata(
                    activeNote.id,
                    dbMeta.copy(accessLevel = newAccess).encode()
                )
            },
            onCopyShareLink = { link ->
                clipboard.setText(AnnotatedString(link))
                viewModel.postStatusBanner("Copied page share link")
                showCommentsAndShareDialog = false
            },
            onDismiss = { showCommentsAndShareDialog = false }
        )
    }
}

/**
 * Screenshot 1: Notion Home Dashboard ("Create your perfect home.")
 * - Workspace Identity Header (Monogram + "MATHGATE's Notion ⌄" + email subtitle + "•••")
 * - Clean Search Bar & "+" New Note button
 * - Structured Filter & Category Control Bar + Quick Page Template Creator Strip
 * - "Jump back in" horizontal square cards with cover art & overlapping emoji icon
 * - "Favorites" white rounded card with expandable nested sub-pages (`>` / `⌄` + emoji + title + `+`)
 * - "Workspace Pages" & "Studio Hubs" sections
 */
@Composable
private fun NotionHomeDashboardView(
    uiState: NotesStudioUiState,
    viewModel: NotesStudioViewModel,
    activeHub: NotesStudioHub,
    onOpenPage: (String) -> Unit,
    onSelectHub: (NotesStudioHub) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenMoreMenu: () -> Unit,
    onOpenFolderModal: () -> Unit,
    onOpenTagModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val expandedParentIds = remember { mutableStateMapOf<String, Boolean>("note_001" to true) }
    val activeNotes = remember(uiState.filteredNotes, uiState.allNotes) {
        uiState.filteredNotes.ifEmpty { uiState.allNotes.filter { !it.isTrashed } }
    }
    val rootPages = remember(activeNotes) {
        activeNotes.filter { n ->
            NoteDatabaseMetadata.decode(n.collectionName).parentNoteId == null
        }
    }
    val favoritePages = remember(rootPages) {
        val favs = rootPages.filter { it.isFavourite || it.isPinned }
        favs.ifEmpty { rootPages.take(3) }
    }

    BoxWithConstraints(modifier = modifier) {
        val horizontalPad = if (maxWidth < 340.dp) 12.dp else 18.dp
        val cardWidth = if (maxWidth < 340.dp) 130.dp else 144.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("notes_studio_compact_scroll"),
            contentPadding = PaddingValues(start = horizontalPad, end = horizontalPad, top = 14.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Notion Workspace Header (Matches Screenshot 1: "M | MATHGATE's Notion ⌄ / email | •••")
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE6E6E4))
                                .clickable { onNavigateBack() }
                                .testTag("notes_back_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "M",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = Color(0xFF5A5A57)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "MATHGATE's Notion",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.5.sp
                                    ),
                                    color = NotionInk,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Filled.UnfoldMore,
                                    contentDescription = null,
                                    tint = NotionMutedInk,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "${uiState.allNotes.count { !it.isTrashed }} pages • ${uiState.folders.size} folders • ${uiState.tags.size} tags",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = NotionMutedInk,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenMoreMenu,
                        modifier = Modifier.testTag("notes_export_import_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreHoriz,
                            contentDescription = "Workspace Options",
                            tint = NotionMutedInk
                        )
                    }
                }
            }

            // 2. Minimal Search & New Page Row (Preserves testTags: notes_search_input & create_note_button)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = {
                            Text(
                                "Search pages, equations, or blocks…",
                                fontSize = 13.5.sp,
                                color = NotionMutedInk
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                                tint = NotionMutedInk,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .testTag("notes_search_input")
                    )
                    Surface(
                        onClick = {
                            viewModel.createNewNote("Untitled Page")
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = NotionInk,
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("create_note_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "New Page", tint = Color.White, modifier = Modifier.size(18.dp))
                            Text(
                                text = "New",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 2B. Structured Filter & Quick Page Template Creator Strip
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Filter Chips Row (All, Favorites, Pinned, Templates, Archived, Trash + Folder/Subject filters)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val filters = listOf(
                            NotesHomeFilter.ALL to "All Pages",
                            NotesHomeFilter.FAVOURITES to "★ Favorites",
                            NotesHomeFilter.PINNED to "📌 Pinned",
                            NotesHomeFilter.TEMPLATES to "✨ Templates",
                            NotesHomeFilter.ARCHIVED to "📦 Archive",
                            NotesHomeFilter.TRASH to "🗑 Trash"
                        )
                        filters.forEach { (mode, label) ->
                            val isSelected = uiState.activeFilter == mode && uiState.selectedFolderId == null
                            Surface(
                                onClick = {
                                    viewModel.selectFolderFilter(null)
                                    viewModel.selectTagFilter(null)
                                    viewModel.setFilter(mode)
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) NotionInk else Color.White,
                                border = BorderStroke(1.dp, if (isSelected) NotionInk else NotionHairline),
                                modifier = Modifier.testTag("home_filter_${mode.name}")
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else NotionBodyInk,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                )
                            }
                        }

                        uiState.folders.forEach { folder ->
                            val isFolderSelected = uiState.selectedFolderId == folder.id
                            Surface(
                                onClick = {
                                    viewModel.selectFolderFilter(if (isFolderSelected) null else folder.id)
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isFolderSelected) BlueprintPrimary else Color.White,
                                border = BorderStroke(1.dp, if (isFolderSelected) BlueprintPrimary else NotionHairline)
                            ) {
                                Text(
                                    text = "📁 ${folder.name}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isFolderSelected) Color.White else NotionMutedInk,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // 1-Tap Structured Page Templates Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick Start:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = NotionMutedInk,
                            modifier = Modifier.padding(end = 2.dp)
                        )
                        val quickTemplates = listOf(
                            "📐 Math Proof" to "tpl_math_derivation",
                            "⚡ Circuit Lab" to "tpl_circuit_design",
                            "🧪 Lab Report" to "tpl_lab_observation",
                            "🎯 Exam Cram" to "tpl_exam_prep"
                        )
                        quickTemplates.forEach { (badgeLabel, templateId) ->
                            Surface(
                                onClick = {
                                    viewModel.createFromTemplate(templateId)
                                    uiState.activeNote?.id?.let { onOpenPage(it) }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, NotionHairline)
                            ) {
                                Text(
                                    text = "+ $badgeLabel",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = NotionInk,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. "Jump back in" Horizontal Cards Section (Matches Screenshot 1)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Jump back in",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp
                        ),
                        color = NotionMutedInk
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        activeNotes.take(6).forEachIndexed { idx, note ->
                            val emoji = notionPageEmojis[idx % notionPageEmojis.size]
                            NotionJumpBackInCard(
                                note = note,
                                emoji = emoji,
                                hasDarkWaveCover = idx == 0,
                                isSelected = uiState.activeNote?.id == note.id,
                                cardWidth = cardWidth,
                                onClick = { onOpenPage(note.id) }
                            )
                        }
                    }
                }
            }

            // 4. "Favorites" Expandable Tree Card (Matches Screenshot 1: > emoji Title + | v emoji Title -> sub-pages)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Favorites",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp
                        ),
                        color = NotionMutedInk
                    )
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, NotionHairline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            favoritePages.forEachIndexed { idx, page ->
                                val emoji = notionPageEmojis[idx % notionPageEmojis.size]
                                val childSubPages = uiState.allNotes.filter { child ->
                                    !child.isTrashed && NoteDatabaseMetadata.decode(child.collectionName).parentNoteId == page.id
                                }
                                val isExpanded = expandedParentIds[page.id] == true

                                NotionTreePageRow(
                                    title = page.title.ifBlank { "Untitled" },
                                    emoji = emoji,
                                    isExpanded = isExpanded,
                                    indentLevel = 0,
                                    onToggleExpand = {
                                        expandedParentIds[page.id] = !isExpanded
                                    },
                                    onClickRow = { onOpenPage(page.id) },
                                    onAddSubPage = {
                                        expandedParentIds[page.id] = true
                                        viewModel.createNestedSubPage(page)
                                    }
                                )

                                if (isExpanded) {
                                    // Show nested sub-pages or active H1 modules inside this page
                                    if (childSubPages.isNotEmpty()) {
                                        childSubPages.forEach { child ->
                                            HorizontalDivider(color = NotionHairline, modifier = Modifier.padding(start = 42.dp))
                                            NotionTreePageRow(
                                                title = child.title.removePrefix("${page.title} — "),
                                                emoji = null,
                                                isExpanded = false,
                                                indentLevel = 1,
                                                onToggleExpand = { onOpenPage(child.id) },
                                                onClickRow = { onOpenPage(child.id) },
                                                onAddSubPage = { viewModel.createNestedSubPage(child) }
                                            )
                                        }
                                    } else {
                                        // Default nested sub-sections preview (like "Workouts", "Body Weight" in Screenshot 1)
                                        listOf("Core Derivations & Formulas", "Solved Practice Set").forEach { subLabel ->
                                            HorizontalDivider(color = NotionHairline, modifier = Modifier.padding(start = 42.dp))
                                            NotionTreePageRow(
                                                title = subLabel,
                                                emoji = null,
                                                isExpanded = false,
                                                indentLevel = 1,
                                                onToggleExpand = { onOpenPage(page.id) },
                                                onClickRow = { onOpenPage(page.id) },
                                                onAddSubPage = {
                                                    viewModel.createNestedSubPage(page)
                                                }
                                            )
                                        }
                                    }
                                }

                                if (idx < favoritePages.lastIndex) {
                                    HorizontalDivider(color = NotionHairline)
                                }
                            }
                        }
                    }
                }
            }

            // 5. "Private & Workspace Pages" Expandable Tree Card
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Workspace Pages (${rootPages.size})",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp
                            ),
                            color = NotionMutedInk
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "+ Folder",
                                style = MaterialTheme.typography.labelSmall,
                                color = NotionMutedInk,
                                modifier = Modifier
                                    .clickable { onOpenFolderModal() }
                                    .testTag("add_folder_chip")
                            )
                            Text(
                                text = "+ Tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = NotionMutedInk,
                                modifier = Modifier
                                    .clickable { onOpenTagModal() }
                                    .testTag("add_tag_chip")
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, NotionHairline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            rootPages.forEachIndexed { idx, page ->
                                val emoji = notionPageEmojis[(idx + 3) % notionPageEmojis.size]
                                val childSubPages = uiState.allNotes.filter { child ->
                                    !child.isTrashed && NoteDatabaseMetadata.decode(child.collectionName).parentNoteId == page.id
                                }
                                val isExpanded = expandedParentIds["ws_${page.id}"] == true

                                NotionTreePageRow(
                                    title = page.title.ifBlank { "Untitled" },
                                    emoji = emoji,
                                    isExpanded = isExpanded,
                                    indentLevel = 0,
                                    onToggleExpand = {
                                        expandedParentIds["ws_${page.id}"] = !isExpanded
                                    },
                                    onClickRow = { onOpenPage(page.id) },
                                    onAddSubPage = {
                                        expandedParentIds["ws_${page.id}"] = true
                                        viewModel.createNestedSubPage(page)
                                    },
                                    modifier = Modifier.testTag("note_card_${page.id}")
                                )

                                if (isExpanded && childSubPages.isNotEmpty()) {
                                    childSubPages.forEach { child ->
                                        HorizontalDivider(color = NotionHairline, modifier = Modifier.padding(start = 42.dp))
                                        NotionTreePageRow(
                                            title = child.title,
                                            emoji = null,
                                            isExpanded = false,
                                            indentLevel = 1,
                                            onToggleExpand = { onOpenPage(child.id) },
                                            onClickRow = { onOpenPage(child.id) },
                                            onAddSubPage = { viewModel.createNestedSubPage(child) }
                                        )
                                    }
                                }

                                if (idx < rootPages.lastIndex) {
                                    HorizontalDivider(color = NotionHairline)
                                }
                            }
                        }
                    }
                }
            }

            // 6. Notion Databases, Equation Studio & Engineering Tools Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Databases & Engineering Studios",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp
                        ),
                        color = NotionMutedInk
                    )
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, NotionHairline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val studioEntries = listOf(
                                Triple(NotesStudioHub.DATABASES, "📊", "Databases (Kanban, Table & Revision Calendar)"),
                                Triple(NotesStudioHub.EQUATION_STUDIO, "∑", "Equation Studio & Handwritten Math Pad"),
                                Triple(NotesStudioHub.CODE_STUDIO, "⚡", "Code, Circuits, Mermaid, IC Pinouts & PDF Lab"),
                                Triple(NotesStudioHub.WORKSPACE, "🗂", "Folders, Tags, Templates & Backup"),
                                Triple(NotesStudioHub.SEARCH, "🔍", "Global Search, Backlinks & TOC")
                            )
                            studioEntries.forEachIndexed { idx, (hub, icon, title) ->
                                NotionTreePageRow(
                                    title = title,
                                    emoji = icon,
                                    isExpanded = false,
                                    indentLevel = 0,
                                    onToggleExpand = { onSelectHub(hub) },
                                    onClickRow = { onSelectHub(hub) },
                                    onAddSubPage = { onSelectHub(hub) }
                                )
                                if (idx < studioEntries.lastIndex) {
                                    HorizontalDivider(color = NotionHairline)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Screenshot 1 Card: "Jump back in" square card with cover band + overlapping emoji + page title.
 */
@Composable
private fun NotionJumpBackInCard(
    note: NoteEntity,
    emoji: String,
    hasDarkWaveCover: Boolean,
    isSelected: Boolean,
    cardWidth: androidx.compose.ui.unit.Dp = 142.dp,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) NotionInk else NotionHairline
        ),
        modifier = Modifier
            .width(cardWidth)
            .height(128.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Cover Half
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(if (hasDarkWaveCover) Color(0xFF0E0E10) else NotionCardCoverGray)
            ) {
                if (hasDarkWaveCover) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path().apply {
                            moveTo(0f, size.height * 0.75f)
                            quadraticTo(
                                size.width * 0.45f,
                                size.height * 0.1f,
                                size.width,
                                size.height * 0.35f
                            )
                        }
                        drawPath(
                            path = path,
                            color = Color.White.copy(alpha = 0.25f),
                            style = Stroke(width = 3f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
                            ),
                            radius = size.minDimension * 0.7f,
                            center = Offset(size.width * 0.8f, size.height * 0.5f)
                        )
                    }
                }
            }

            // Overlapping Emoji + Bottom Page Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .offset(y = (-16).dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = emoji,
                    fontSize = 24.sp
                )
                Text(
                    text = note.title.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    ),
                    color = NotionInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Screenshot 1 Row: Expandable Notion Tree Row (`>` or `⌄` chevron, emoji, title, and `+` button).
 */
@Composable
private fun NotionTreePageRow(
    title: String,
    emoji: String?,
    isExpanded: Boolean,
    indentLevel: Int,
    onToggleExpand: () -> Unit,
    onClickRow: () -> Unit,
    onAddSubPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClickRow() }
            .padding(
                start = (12 + indentLevel * 28).dp,
                end = 12.dp,
                top = 12.dp,
                bottom = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Expand or Collapse",
                tint = NotionMutedInk,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onToggleExpand() }
            )
            if (emoji != null) {
                Text(
                    text = emoji,
                    fontSize = 18.sp
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.5.sp
                ),
                color = NotionInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add sub-page",
            tint = NotionMutedInk,
            modifier = Modifier
                .size(20.dp)
                .clickable { onAddSubPage() }
        )
    }
}

/**
 * Screenshot 2: Clean Notion Page Document Canvas ("Launch Program") with Structured Controls & Abilities:
 * - Top Header (`<` back + Breadcrumb Path + 1-tap Undo/Redo/Favorite/Pin/Outline/LaTeX/Share/More)
 * - 6-Hub Segmented Pill Switcher (`Page`, `Databases`, `Workspace`, `Search & TOC`, `Equation Studio`, `Code & Eng Lab`)
 * - Huge Bold Borderless Page Title
 * - Collapsible Notion Page Properties & Live Telemetry Panel (Status, Priority, Subject, Folder, Mode, Word/Eq Stats)
 * - "Quick Links" with Expand/Collapse All Sections control and underlined section/module links
 * - Interactive Section Folding for Heading 1 & Heading 2 blocks + inline `+ Insert Below` block controls
 * - Structured Bottom Block Insertion & Smart Page Actions Dock
 */
@Composable
private fun NotionDocumentOrHubCanvas(
    uiState: NotesStudioUiState,
    viewModel: NotesStudioViewModel,
    activeHub: NotesStudioHub,
    onSelectHub: (NotesStudioHub) -> Unit,
    onBackToHome: () -> Unit,
    onOpenShareSheet: () -> Unit,
    onOpenMoreMenu: () -> Unit,
    onOpenSlashPalette: () -> Unit,
    onOpenAllBlocksPicker: () -> Unit,
    onOpenFolderModal: () -> Unit,
    onOpenTagModal: () -> Unit,
    onOpenExportImportModal: () -> Unit,
    onOpenGraphDialog: () -> Unit,
    onOpenFlashcardsModal: () -> Unit,
    onOpenOutlineDialog: () -> Unit = {},
    onOpenLatexPadDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeNote = uiState.activeNote
    var showPropertiesPanel by remember { mutableStateOf(false) }
    val collapsedSectionHeadingIds = remember { mutableStateMapOf<String, Boolean>() }

    val sectionHeadings = remember(uiState.activeBlocks) {
        uiState.activeBlocks.filter {
            it.type == BlockType.HEADING_1.name ||
                it.type == BlockType.HEADING_2.name ||
                it.type == BlockType.HEADING_3.name
        }
    }
    val childSubPages = remember(activeNote?.id, uiState.allNotes) {
        if (activeNote == null) emptyList()
        else uiState.allNotes.filter { n ->
            !n.isTrashed && NoteDatabaseMetadata.decode(n.collectionName).parentNoteId == activeNote.id
        }
    }

    // Compute visible blocks and hidden child counts when H1/H2 sections are folded
    val visibleBlocksWithFoldMeta = remember(uiState.activeBlocks, collapsedSectionHeadingIds.toMap()) {
        val list = mutableListOf<Triple<com.example.core.database.NoteBlockEntity, Boolean, Int>>()
        val blocks = uiState.activeBlocks
        var i = 0
        while (i < blocks.size) {
            val blk = blocks[i]
            val isCollapsibleHeading = blk.type == BlockType.HEADING_1.name || blk.type == BlockType.HEADING_2.name
            val isCollapsed = isCollapsibleHeading && (collapsedSectionHeadingIds[blk.id] == true)
            if (isCollapsed) {
                var hiddenCount = 0
                var j = i + 1
                while (j < blocks.size) {
                    val nextType = blocks[j].type
                    val stopsFold = if (blk.type == BlockType.HEADING_1.name) {
                        nextType == BlockType.HEADING_1.name
                    } else {
                        nextType == BlockType.HEADING_1.name || nextType == BlockType.HEADING_2.name
                    }
                    if (stopsFold) break
                    hiddenCount++
                    j++
                }
                list.add(Triple(blk, true, hiddenCount))
                i = j
            } else {
                list.add(Triple(blk, false, 0))
                i++
            }
        }
        list
    }

    // Live document telemetry
    val totalWords = remember(uiState.activeBlocks) {
        uiState.activeBlocks.sumOf { blk ->
            blk.content.trim().split(Regex("\\s+")).count { it.isNotEmpty() }
        }
    }
    val equationCount = remember(uiState.activeBlocks) {
        uiState.activeBlocks.count { it.type == BlockType.LATEX_BLOCK.name || it.type == BlockType.INLINE_MATH.name }
    }

    BoxWithConstraints(modifier = modifier) {
        val docHorizontalPad = when {
            maxWidth < 360.dp -> 14.dp
            maxWidth >= 600.dp -> 32.dp
            else -> 22.dp
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = docHorizontalPad, end = docHorizontalPad, top = 10.dp, bottom = 108.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Minimal Notion Page Top Bar (`<` + Breadcrumb on left, Undo/Redo/Star/Pin/Outline/Share/`•••` on right)
            item {
                val folderName = remember(activeNote?.folderId, uiState.folders) {
                    uiState.folders.firstOrNull { it.id == activeNote?.folderId }?.name ?: "Workspace"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBackToHome,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Notion Home",
                                tint = NotionInk,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "$folderName / ${activeNote?.title?.ifBlank { "Untitled" } ?: activeHub.label}",
                            style = MaterialTheme.typography.labelMedium,
                            color = NotionMutedInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable { onBackToHome() }
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = uiState.canUndo,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo",
                                tint = if (uiState.canUndo) NotionInk else NotionHairline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = uiState.canRedo,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Redo",
                                tint = if (uiState.canRedo) NotionInk else NotionHairline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (activeNote != null) {
                            IconButton(
                                onClick = { viewModel.toggleFavouriteActiveNote() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Favorite Page",
                                    tint = if (activeNote.isFavourite) Color(0xFFE6A100) else NotionMutedInk,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = onOpenShareSheet,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("notes_comments_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.IosShare,
                                contentDescription = "Share & Comments",
                                tint = NotionInk,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        IconButton(
                            onClick = onOpenMoreMenu,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("notes_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreHoriz,
                                contentDescription = "Page Options",
                                tint = NotionInk,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }
                }
            }

            // 1B. Segmented 6-Hub Switcher Bar (1-Tap switching between Page, Databases, Workspace, Search, Equation Studio, Code Lab)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NotesStudioHub.entries.forEach { hub ->
                        val isSelected = activeHub == hub
                        Surface(
                            onClick = { onSelectHub(hub) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) NotionInk else NotionHomeBg,
                            border = BorderStroke(1.dp, if (isSelected) NotionInk else NotionHairline)
                        ) {
                            Text(
                                text = "${hub.badge} ${hub.label}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else NotionBodyInk,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Status banner if present
            uiState.statusBanner?.let { msg ->
                item {
                    Surface(
                        color = NotionHomeBg,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, NotionHairline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = NotionInk,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.clearStatusBanner() }) {
                                Text("Dismiss", fontSize = 11.sp, color = NotionMutedInk)
                            }
                        }
                    }
                }
            }

            when (activeHub) {
                NotesStudioHub.DATABASES -> {
                    item {
                        NotionDatabasesHubSection(
                            uiState = uiState,
                            viewModel = viewModel,
                            onOpenNoteInEditor = { noteId ->
                                viewModel.openNote(noteId)
                                onSelectHub(NotesStudioHub.DOCUMENTS)
                            }
                        )
                    }
                }

                NotesStudioHub.WORKSPACE -> {
                    item {
                        NotionWorkspaceHubSection(
                            uiState = uiState,
                            viewModel = viewModel,
                            onOpenNoteInEditor = { noteId ->
                                viewModel.openNote(noteId)
                                onSelectHub(NotesStudioHub.DOCUMENTS)
                            },
                            onOpenFolderModal = onOpenFolderModal,
                            onOpenTagModal = onOpenTagModal,
                            onOpenExportImportModal = onOpenExportImportModal
                        )
                    }
                }

                NotesStudioHub.SEARCH -> {
                    item {
                        NotionSearchAndBacklinksHubSection(
                            uiState = uiState,
                            viewModel = viewModel,
                            onOpenNoteInEditor = { noteId ->
                                viewModel.openNote(noteId)
                                onSelectHub(NotesStudioHub.DOCUMENTS)
                            },
                            onOpenGraphDialog = onOpenGraphDialog
                        )
                    }
                }

                NotesStudioHub.EQUATION_STUDIO -> {
                    item {
                        NotionEquationStudioHubSection(
                            uiState = uiState,
                            viewModel = viewModel,
                            onSwitchToDocumentsHub = { onSelectHub(NotesStudioHub.DOCUMENTS) }
                        )
                    }
                }

                NotesStudioHub.CODE_STUDIO -> {
                    item {
                        NotionCodeAndEngineeringStudioHubSection(
                            uiState = uiState,
                            viewModel = viewModel,
                            onSwitchToDocumentsHub = { onSelectHub(NotesStudioHub.DOCUMENTS) },
                            onOpenFlashcardsModal = onOpenFlashcardsModal
                        )
                    }
                }

                NotesStudioHub.DOCUMENTS -> {
                    if (activeNote != null) {
                        val dbMeta = NoteDatabaseMetadata.decode(activeNote.collectionName)

                        // 2. Large Borderless Notion Page Title (Matches Screenshot 2: "Launch Program")
                        item {
                            BasicTextField(
                                value = activeNote.title,
                                onValueChange = { viewModel.updateActiveNoteTitle(it) },
                                textStyle = TextStyle(
                                    color = NotionInk,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 36.sp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .testTag("note_title_input")
                            )
                        }

                        // 2B. Compact Notion Page Telemetry & Properties Strip (Collapsible)
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Toggle Properties Pill
                                    Surface(
                                        onClick = { showPropertiesPanel = !showPropertiesPanel },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (showPropertiesPanel) NotionInk else NotionHomeBg,
                                        border = BorderStroke(1.dp, NotionHairline)
                                    ) {
                                        Text(
                                            text = if (showPropertiesPanel) "▾ Properties" else "▸ Properties",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (showPropertiesPanel) Color.White else NotionMutedInk,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    // Status Pill (1-tap cycle)
                                    val statuses = listOf("TO_STUDY", "IN_PROGRESS", "COMPLETED")
                                    Surface(
                                        onClick = {
                                            val nextStatus = statuses[(statuses.indexOf(dbMeta.status) + 1) % statuses.size]
                                            viewModel.updateNoteCollectionMetadata(
                                                activeNote.id,
                                                dbMeta.copy(status = nextStatus).encode()
                                            )
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        color = NotionHomeBg,
                                        border = BorderStroke(1.dp, NotionHairline)
                                    ) {
                                        Text(
                                            text = "● ${dbMeta.status.replace('_', ' ')}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = NotionInk,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    // Editor Mode Pill (Blocks / Markdown / Reading)
                                    val modeLabel = when {
                                        uiState.isReadingMode -> "📖 Reading"
                                        uiState.isMarkdownLiveMode -> "📝 Markdown"
                                        else -> "✏️ Blocks"
                                    }
                                    Surface(
                                        onClick = {
                                            val nextMode = when {
                                                uiState.isReadingMode -> "BLOCKS"
                                                uiState.isMarkdownLiveMode -> "READING"
                                                else -> "MARKDOWN_LIVE"
                                            }
                                            viewModel.setEditorDisplayMode(nextMode)
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        color = NotionHomeBg,
                                        border = BorderStroke(1.dp, NotionHairline)
                                    ) {
                                        Text(
                                            text = modeLabel,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = NotionInk,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    // Equation Pad & Outline Quick Pills
                                    Surface(
                                        onClick = onOpenLatexPadDialog,
                                        shape = RoundedCornerShape(6.dp),
                                        color = NotionHomeBg,
                                        border = BorderStroke(1.dp, NotionHairline)
                                    ) {
                                        Text(
                                            text = "∑ LaTeX Pad",
                                            fontSize = 11.sp,
                                            color = NotionMutedInk,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        onClick = onOpenOutlineDialog,
                                        shape = RoundedCornerShape(6.dp),
                                        color = NotionHomeBg,
                                        border = BorderStroke(1.dp, NotionHairline)
                                    ) {
                                        Text(
                                            text = "≡ Outline",
                                            fontSize = 11.sp,
                                            color = NotionMutedInk,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    // Live Telemetry Summary
                                    Text(
                                        text = "${uiState.activeBlocks.size} blocks • $totalWords words • $equationCount eqs",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NotionMutedInk,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }

                                // Expanded Structured Notion Page Properties Card
                                if (showPropertiesPanel) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = NotionHomeBg,
                                        border = BorderStroke(1.dp, NotionHairline),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Priority Selector
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text("Priority:", style = MaterialTheme.typography.labelSmall, color = NotionMutedInk, modifier = Modifier.width(64.dp))
                                                listOf("HIGH", "MEDIUM", "LOW").forEach { prio ->
                                                    val selected = dbMeta.priority == prio
                                                    Surface(
                                                        onClick = {
                                                            viewModel.updateNoteCollectionMetadata(
                                                                activeNote.id,
                                                                dbMeta.copy(priority = prio).encode()
                                                            )
                                                        },
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (selected) NotionInk else Color.White,
                                                        border = BorderStroke(1.dp, NotionHairline)
                                                    ) {
                                                        Text(
                                                            text = prio,
                                                            fontSize = 10.5.sp,
                                                            color = if (selected) Color.White else NotionInk,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            // Subject Selector
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.horizontalScroll(rememberScrollState())
                                            ) {
                                                Text("Subject:", style = MaterialTheme.typography.labelSmall, color = NotionMutedInk, modifier = Modifier.width(64.dp))
                                                listOf(
                                                    "Engineering Mathematics",
                                                    "Signals & Systems",
                                                    "Control Systems",
                                                    "Analog & Digital Electronics",
                                                    "Algorithms & Data Structures"
                                                ).forEach { subj ->
                                                    val selected = activeNote.subjectTag == subj
                                                    Surface(
                                                        onClick = { viewModel.updateActiveNoteSubject(subj) },
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (selected) NotionInk else Color.White,
                                                        border = BorderStroke(1.dp, NotionHairline)
                                                    ) {
                                                        Text(
                                                            text = subj,
                                                            fontSize = 10.5.sp,
                                                            color = if (selected) Color.White else NotionInk,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            // Folder Selector
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.horizontalScroll(rememberScrollState())
                                            ) {
                                                Text("Folder:", style = MaterialTheme.typography.labelSmall, color = NotionMutedInk, modifier = Modifier.width(64.dp))
                                                uiState.folders.forEach { folder ->
                                                    val selected = activeNote.folderId == folder.id
                                                    Surface(
                                                        onClick = { viewModel.updateActiveNoteFolder(folder.id) },
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (selected) NotionInk else Color.White,
                                                        border = BorderStroke(1.dp, NotionHairline)
                                                    ) {
                                                        Text(
                                                            text = "📁 ${folder.name}",
                                                            fontSize = 10.5.sp,
                                                            color = if (selected) Color.White else NotionInk,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 3. "Quick Links" Section (Matches Screenshot 2: Bold "Quick Links", hairline divider, underlined links + Expand/Collapse All)
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp, bottom = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Quick Links",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        ),
                                        color = NotionInk
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        if (sectionHeadings.isNotEmpty()) {
                                            val anyCollapsed = collapsedSectionHeadingIds.values.any { it }
                                            Text(
                                                text = if (anyCollapsed) "Expand All Sections" else "Collapse All Sections",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = NotionMutedInk,
                                                modifier = Modifier.clickable {
                                                    if (anyCollapsed) {
                                                        collapsedSectionHeadingIds.clear()
                                                    } else {
                                                        sectionHeadings.forEach { h ->
                                                            if (h.type == BlockType.HEADING_1.name || h.type == BlockType.HEADING_2.name) {
                                                                collapsedSectionHeadingIds[h.id] = true
                                                            }
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                        Text(
                                            text = "+ Sub-page",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NotionMutedInk,
                                            modifier = Modifier.clickable {
                                                viewModel.createNestedSubPage(activeNote)
                                            }
                                        )
                                    }
                                }
                                HorizontalDivider(thickness = 1.dp, color = NotionHairline)

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Show child sub-pages as Quick Links if present
                                    childSubPages.forEach { subPage ->
                                        Text(
                                            text = "📄 ${subPage.title}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 14.5.sp,
                                                textDecoration = TextDecoration.Underline
                                            ),
                                            color = NotionMutedInk,
                                            modifier = Modifier.clickable { viewModel.openNote(subPage.id) }
                                        )
                                    }

                                    // Show document headings as interactive Quick Links (tapping toggles fold/unfold)
                                    if (sectionHeadings.isNotEmpty()) {
                                        sectionHeadings.forEachIndexed { idx, headingBlk ->
                                            val cleanHeading = headingBlk.content.trim()
                                            val displayLink = if (cleanHeading.startsWith("Module", ignoreCase = true) ||
                                                cleanHeading.firstOrNull()?.isDigit() == true
                                            ) {
                                                cleanHeading
                                            } else {
                                                "Module ${idx + 1}: $cleanHeading"
                                            }
                                            val isFolded = collapsedSectionHeadingIds[headingBlk.id] == true
                                            Text(
                                                text = if (isFolded) "▸ $displayLink (folded)" else displayLink,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 14.5.sp,
                                                    textDecoration = TextDecoration.Underline
                                                ),
                                                color = NotionMutedInk,
                                                modifier = Modifier.clickable {
                                                    collapsedSectionHeadingIds[headingBlk.id] = !isFolded
                                                }
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "+ Add sub-page or section heading",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 14.sp,
                                                textDecoration = TextDecoration.Underline
                                            ),
                                            color = NotionMutedInk,
                                            modifier = Modifier.clickable {
                                                viewModel.createNestedSubPage(activeNote)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Live Markdown Source Editor if toggled on (Extra #12)
                        if (uiState.isMarkdownLiveMode) {
                            item {
                                MarkdownLiveSourceEditorCard(
                                    note = activeNote,
                                    blocks = uiState.activeBlocks,
                                    onApplyMarkdown = { md ->
                                        viewModel.applyLiveMarkdownToActiveNote(md)
                                    }
                                )
                            }
                        }

                        // 4. Clean Borderless Document Blocks with Interactive Section Folding & Inline Insertion
                        var eqCounter = 0
                        itemsIndexed(
                            items = visibleBlocksWithFoldMeta,
                            key = { _, (blk, _, _) -> blk.id }
                        ) { _, (block, isCollapsed, hiddenChildCount) ->
                            val eqNum = if (uiState.showEquationNumbers && block.type == BlockType.LATEX_BLOCK.name) {
                                ++eqCounter
                            } else {
                                null
                            }
                            NoteBlockItemCard(
                                block = block,
                                equationIndex = eqNum,
                                isReadingMode = uiState.isReadingMode,
                                isSectionCollapsed = isCollapsed,
                                hiddenChildCount = hiddenChildCount,
                                onToggleSectionCollapse = {
                                    collapsedSectionHeadingIds[block.id] = !(collapsedSectionHeadingIds[block.id] ?: false)
                                },
                                onInsertBlockBelow = { nextType ->
                                    viewModel.insertBlockAfter(block.id, nextType)
                                },
                                onUpdateContent = { content, meta ->
                                    viewModel.updateBlockContent(block.id, content, meta)
                                },
                                onConvertType = { target ->
                                    viewModel.convertBlockType(block.id, target)
                                },
                                onDuplicate = { viewModel.duplicateBlock(block.id) },
                                onMoveUp = { viewModel.moveBlock(block.id, -1) },
                                onMoveDown = { viewModel.moveBlock(block.id, 1) },
                                onDelete = { viewModel.deleteBlock(block.id) },
                                onAttachImage = { uri, cap ->
                                    viewModel.attachImageUriToBlock(block.id, uri, cap)
                                },
                                onNavigateToLinkedNote = { targetNoteId ->
                                    viewModel.openNote(targetNoteId)
                                }
                            )
                        }

                        // 5. Structured Notion Block Insertion & Smart Page Actions Dock
                        if (!uiState.isReadingMode) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Primary Slash & All Block Picker Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            onClick = onOpenSlashPalette,
                                            shape = RoundedCornerShape(8.dp),
                                            color = NotionHomeBg,
                                            border = BorderStroke(1.dp, NotionHairline),
                                            modifier = Modifier.testTag("slash_command_button")
                                        ) {
                                            Text(
                                                text = "/ Type '/' for commands",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = NotionMutedInk,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                            )
                                        }
                                        Surface(
                                            onClick = onOpenAllBlocksPicker,
                                            shape = RoundedCornerShape(8.dp),
                                            color = NotionHomeBg,
                                            border = BorderStroke(1.dp, NotionHairline),
                                            modifier = Modifier.testTag("all_block_types_button")
                                        ) {
                                            Text(
                                                text = "+ Add block (${BlockType.entries.size})",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = NotionMutedInk,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                            )
                                        }
                                    }

                                    // 1-Tap Quick Block Strip (Headings, Lists, Math, Table, Code, Callout, Circuit)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(
                                            BlockType.HEADING_1 to "+ H1 Section",
                                            BlockType.PARAGRAPH to "+ Text",
                                            BlockType.BULLETED_LIST to "+ Bullet",
                                            BlockType.CHECKLIST to "+ Checklist",
                                            BlockType.LATEX_BLOCK to "+ ∑ Equation",
                                            BlockType.TABLE to "+ ⊞ Table",
                                            BlockType.CODE_BLOCK to "+ </> Code",
                                            BlockType.CALLOUT to "+ 💡 Callout",
                                            BlockType.TOGGLE_SECTION to "+ ▸ Toggle",
                                            BlockType.ENGINEERING_SCHEMATIC to "+ ⚡ Circuit"
                                        ).forEach { (type, label) ->
                                            Surface(
                                                onClick = { viewModel.addBlock(type) },
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color.White,
                                                border = BorderStroke(1.dp, NotionHairline)
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontSize = 11.sp,
                                                    color = NotionInk,
                                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Smart Page Actions Row (Auto-Number Headings, Sort Checklists, Executive Summary, Flashcards)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            onClick = { viewModel.autoNumberHeadings() },
                                            shape = RoundedCornerShape(6.dp),
                                            color = NotionHomeBg,
                                            border = BorderStroke(1.dp, NotionHairline)
                                        ) {
                                            Text(
                                                text = "1.2 Auto-Number Headings",
                                                fontSize = 10.5.sp,
                                                color = NotionMutedInk,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Surface(
                                            onClick = { viewModel.sortChecklistsInActiveNote() },
                                            shape = RoundedCornerShape(6.dp),
                                            color = NotionHomeBg,
                                            border = BorderStroke(1.dp, NotionHairline)
                                        ) {
                                            Text(
                                                text = "☑ Sort Checklists",
                                                fontSize = 10.5.sp,
                                                color = NotionMutedInk,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Surface(
                                            onClick = { viewModel.generateExecutiveTakeawayBlock() },
                                            shape = RoundedCornerShape(6.dp),
                                            color = NotionHomeBg,
                                            border = BorderStroke(1.dp, NotionHairline)
                                        ) {
                                            Text(
                                                text = "✨ Generate Page Takeaway",
                                                fontSize = 10.5.sp,
                                                color = NotionMutedInk,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Surface(
                                            onClick = onOpenFlashcardsModal,
                                            shape = RoundedCornerShape(6.dp),
                                            color = NotionHomeBg,
                                            border = BorderStroke(1.dp, NotionHairline)
                                        ) {
                                            Text(
                                                text = "⚡ Study Flashcards",
                                                fontSize = 10.5.sp,
                                                color = NotionMutedInk,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Subtle Backlinks footer if any links exist
                        if (uiState.activeBacklinks.isNotEmpty() || uiState.activeOutgoingLinks.isNotEmpty()) {
                            item {
                                BacklinksAndConnectionsCard(
                                    uiState = uiState,
                                    onOpenNote = { viewModel.openNote(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Screenshot 2 Floating Bottom Pill Bar:
 * Centered rounded white pill with Home, Search, Inbox/Hubs (with red badge), and Compose/Slash icons.
 */
@Composable
private fun NotionFloatingBottomPill(
    isHomeActive: Boolean,
    activeHub: NotesStudioHub,
    onTapHome: () -> Unit,
    onTapSearch: () -> Unit,
    onTapHubsInbox: () -> Unit,
    onTapComposeOrSlash: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, NotionHairline),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onTapHome,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Notion Home",
                    tint = if (isHomeActive) NotionInk else NotionMutedInk,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = onTapSearch,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search & TOC",
                    tint = if (activeHub == NotesStudioHub.SEARCH) NotionInk else NotionMutedInk,
                    modifier = Modifier.size(22.dp)
                )
            }

            Box {
                IconButton(
                    onClick = onTapHubsInbox,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Inbox,
                        contentDescription = "Databases & Studio Hubs",
                        tint = if (activeHub != NotesStudioHub.DOCUMENTS && activeHub != NotesStudioHub.SEARCH) NotionInk else NotionMutedInk,
                        modifier = Modifier.size(22.dp)
                    )
                }
                // Red notification badge matching Screenshot 2
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp)
                        .size(15.dp)
                        .background(Color(0xFFE03E3E), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "6",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(
                onClick = onTapComposeOrSlash,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "New Page or Insert Block",
                    tint = NotionInk,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Extra #12: Markdown Source Editing with Live Preview Card.
 */
@Composable
private fun MarkdownLiveSourceEditorCard(
    note: NoteEntity,
    blocks: List<com.example.core.database.NoteBlockEntity>,
    onApplyMarkdown: (String) -> Unit
) {
    val initialMarkdown = remember(note.id, blocks.size) {
        NotesExportImportManager.exportToMarkdown(note, blocks)
    }
    var markdownDraft by remember(note.id) { mutableStateOf(initialMarkdown) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("markdown_live_editor_card"),
        color = NotionHomeBg,
        border = BorderStroke(1.dp, NotionHairline),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Markdown Live Source",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = NotionInk
                    )
                    Text(
                        text = "Edit raw Markdown and sync to document blocks",
                        style = MaterialTheme.typography.labelSmall,
                        color = NotionMutedInk
                    )
                }
                Button(
                    onClick = { onApplyMarkdown(markdownDraft) },
                    colors = ButtonDefaults.buttonColors(containerColor = NotionInk),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Sync", fontSize = 11.sp)
                }
            }

            OutlinedTextField(
                value = markdownDraft,
                onValueChange = { markdownDraft = it },
                label = { Text("Markdown (.md)") },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                maxLines = 12,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("markdown_live_source_input")
            )
        }
    }
}

@Composable
private fun BacklinksAndConnectionsCard(
    uiState: NotesStudioUiState,
    onOpenNote: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("backlinks_card"),
        color = NotionHomeBg,
        border = BorderStroke(1.dp, NotionHairline),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Bookmark,
                    contentDescription = null,
                    tint = NotionMutedInk,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Backlinks (${uiState.activeBacklinks.size} incoming • ${uiState.activeOutgoingLinks.size} outgoing)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NotionMutedInk
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.activeBacklinks.forEach { link ->
                    AssistChip(
                        onClick = { onOpenNote(link.sourceNoteId) },
                        label = { Text("← ${link.sourceNoteId}", fontSize = 11.sp) },
                        modifier = Modifier.testTag("backlink_${link.sourceNoteId}")
                    )
                }
                uiState.activeOutgoingLinks.forEach { link ->
                    AssistChip(
                        onClick = { onOpenNote(link.targetNoteId) },
                        label = { Text("→ ${link.anchorText}", fontSize = 11.sp) },
                        modifier = Modifier.testTag("outgoing_link_${link.targetNoteId}")
                    )
                }
            }
        }
    }
}
