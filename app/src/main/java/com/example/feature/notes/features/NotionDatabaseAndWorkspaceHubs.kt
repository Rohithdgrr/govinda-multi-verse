package com.example.feature.notes.features

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.NoteBlockEntity
import com.example.core.database.NoteEntity
import com.example.core.document.BlockType
import com.example.feature.notes.NotesHomeFilter
import com.example.feature.notes.NotesStudioUiState
import com.example.feature.notes.NotesStudioViewModel

private val NotionPrimary = Color(0xFF003DA5)
private val NotionSecondary = Color(0xFF1647BB)
private val NotionSurface = Color(0xFFEAF1FF)
private val NotionSubtleBg = Color(0xFFF8FAFC)
private val NotionBorder = Color(0xFFDCE6F7)

enum class NotesStudioHub(val label: String, val badge: String) {
    DOCUMENTS("Documents", "📄"),
    DATABASES("Databases", "⊞"),
    WORKSPACE("Workspace", "🗂"),
    SEARCH("Search & TOC", "🔍"),
    EQUATION_STUDIO("Equation Studio", "∑"),
    CODE_STUDIO("Code & Eng Studio", "</>")
}

enum class NotionDatabaseViewMode(val label: String) {
    TABLE("Table View"),
    KANBAN("Kanban Board"),
    CALENDAR("Calendar & Revision")
}

data class NoteDatabaseMetadata(
    val status: String = "IN_PROGRESS", // TO_DO, IN_PROGRESS, REVIEW, MASTERED
    val priority: String = "HIGH",      // HIGH, MEDIUM, LOW
    val parentNoteId: String? = null,
    val revisionIntervalDays: Int = 3,
    val nextRevisionLabel: String = "Tomorrow",
    val accessLevel: String = "WORKSPACE_EDIT" // PRIVATE, WORKSPACE_VIEW, WORKSPACE_EDIT
) {
    fun encode(): String =
        "STATUS=$status|PRIORITY=$priority|PARENT=${parentNoteId.orEmpty()}|REV_DAYS=$revisionIntervalDays|NEXT_REV=$nextRevisionLabel|ACCESS=$accessLevel"

    companion object {
        fun decode(rawCollectionName: String, noteIndex: Int = 0): NoteDatabaseMetadata {
            if (!rawCollectionName.contains("STATUS=")) {
                val defaultStatuses = listOf("IN_PROGRESS", "TO_DO", "REVIEW", "MASTERED")
                val defaultPriorities = listOf("HIGH", "MEDIUM", "HIGH", "LOW")
                val defaultRev = listOf("Today", "Tomorrow", "In 3 days", "In 7 days")
                return NoteDatabaseMetadata(
                    status = defaultStatuses[noteIndex % defaultStatuses.size],
                    priority = defaultPriorities[noteIndex % defaultPriorities.size],
                    parentNoteId = null,
                    revisionIntervalDays = listOf(1, 3, 7, 14)[noteIndex % 4],
                    nextRevisionLabel = defaultRev[noteIndex % defaultRev.size],
                    accessLevel = "WORKSPACE_EDIT"
                )
            }
            val map = rawCollectionName.split("|").associate { part ->
                val kv = part.split("=", limit = 2)
                kv.getOrElse(0) { "" } to kv.getOrElse(1) { "" }
            }
            return NoteDatabaseMetadata(
                status = map["STATUS"]?.ifBlank { "IN_PROGRESS" } ?: "IN_PROGRESS",
                priority = map["PRIORITY"]?.ifBlank { "MEDIUM" } ?: "MEDIUM",
                parentNoteId = map["PARENT"]?.takeIf { it.isNotBlank() },
                revisionIntervalDays = map["REV_DAYS"]?.toIntOrNull() ?: 3,
                nextRevisionLabel = map["NEXT_REV"]?.ifBlank { "Tomorrow" } ?: "Tomorrow",
                accessLevel = map["ACCESS"]?.ifBlank { "WORKSPACE_EDIT" } ?: "WORKSPACE_EDIT"
            )
        }
    }
}

data class NoteCommentItem(
    val id: String,
    val author: String,
    val mention: String,
    val text: String,
    val timestampLabel: String
)

/**
 * Hub 2: Notion Databases (Table View, Kanban Board, Calendar & Spaced-Repetition Scheduler, Filters & Sorting).
 */
@Composable
fun NotionDatabasesHubSection(
    uiState: NotesStudioUiState,
    viewModel: NotesStudioViewModel,
    onOpenNoteInEditor: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(NotionDatabaseViewMode.KANBAN) }
    var selectedSubject by remember { mutableStateOf("ALL") }
    var selectedPriority by remember { mutableStateOf("ALL") }
    var sortBy by remember { mutableStateOf("UPDATED") } // UPDATED, TITLE, PRIORITY

    val activeNotes = remember(uiState.allNotes, selectedSubject, selectedPriority, sortBy) {
        val base = uiState.allNotes.filter { !it.isTrashed }
        val filtered = base.filterIndexed { idx, note ->
            val meta = NoteDatabaseMetadata.decode(note.collectionName, idx)
            val matchesSubject = selectedSubject == "ALL" || note.subjectTag.equals(selectedSubject, ignoreCase = true)
            val matchesPriority = selectedPriority == "ALL" || meta.priority == selectedPriority
            matchesSubject && matchesPriority
        }
        when (sortBy) {
            "TITLE" -> filtered.sortedBy { it.title.lowercase() }
            "PRIORITY" -> filtered.sortedBy { note ->
                when (NoteDatabaseMetadata.decode(note.collectionName).priority) {
                    "HIGH" -> 0
                    "MEDIUM" -> 1
                    else -> 2
                }
            }
            else -> filtered.sortedByDescending { it.updatedAt }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("notion_databases_hub"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, NotionBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header & View Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "NOTION ENGINEERING DATABASES",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = NotionPrimary
                    )
                    Text(
                        text = "Switch between Table, Kanban Board & Spaced-Repetition Calendar views",
                        style = MaterialTheme.typography.bodySmall,
                        color = NotionSecondary
                    )
                }
                Button(
                    onClick = { viewModel.createNewNote("New Database Record") },
                    colors = ButtonDefaults.buttonColors(containerColor = NotionPrimary),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("NEW RECORD", fontSize = 11.sp)
                }
            }

            // Database View Switcher (Table | Kanban | Calendar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotionDatabaseViewMode.entries.forEach { mode ->
                    val selected = viewMode == mode
                    FilterChip(
                        selected = selected,
                        onClick = { viewMode = mode },
                        leadingIcon = {
                            val icon = when (mode) {
                                NotionDatabaseViewMode.TABLE -> Icons.Filled.TableChart
                                NotionDatabaseViewMode.KANBAN -> Icons.Filled.ViewKanban
                                NotionDatabaseViewMode.CALENDAR -> Icons.Filled.CalendarMonth
                            }
                            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text(mode.label, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NotionSurface,
                            selectedLabelColor = NotionPrimary
                        ),
                        modifier = Modifier.testTag("db_view_${mode.name}")
                    )
                }
            }

            // Filters & Sorting Bar (#35)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NotionSubtleBg, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Subject:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NotionPrimary
                )
                listOf("ALL", "MATHEMATICS", "ELECTRONICS", "COMPUTER_SCIENCE").forEach { subj ->
                    FilterChip(
                        selected = selectedSubject == subj,
                        onClick = { selectedSubject = subj },
                        label = { Text(subj.take(10), fontSize = 10.sp) }
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Priority:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NotionPrimary
                )
                listOf("ALL", "HIGH", "MEDIUM", "LOW").forEach { prio ->
                    FilterChip(
                        selected = selectedPriority == prio,
                        onClick = { selectedPriority = prio },
                        label = { Text(prio, fontSize = 10.sp) }
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sort:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NotionPrimary
                )
                listOf("UPDATED" to "Recent", "TITLE" to "A–Z", "PRIORITY" to "Priority").forEach { (key, label) ->
                    FilterChip(
                        selected = sortBy == key,
                        onClick = { sortBy = key },
                        label = { Text(label, fontSize = 10.sp) }
                    )
                }
            }

            when (viewMode) {
                NotionDatabaseViewMode.KANBAN -> {
                    val columns = listOf(
                        "TO_DO" to "📋 To Do",
                        "IN_PROGRESS" to "⚡ In Progress",
                        "REVIEW" to "🔍 In Review",
                        "MASTERED" to "✅ Mastered"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        columns.forEach { (statusKey, colTitle) ->
                            val cardsInCol = activeNotes.filterIndexed { idx, n ->
                                NoteDatabaseMetadata.decode(n.collectionName, idx).status == statusKey
                            }
                            Card(
                                modifier = Modifier.width(245.dp),
                                colors = CardDefaults.cardColors(containerColor = NotionSubtleBg),
                                border = BorderStroke(1.dp, NotionBorder),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = colTitle,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = NotionPrimary
                                        )
                                        Surface(
                                            color = NotionSurface,
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = "${cardsInCol.size}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = NotionPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    cardsInCol.forEachIndexed { idx, note ->
                                        val meta = NoteDatabaseMetadata.decode(note.collectionName, idx)
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onOpenNoteInEditor(note.id) },
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            border = BorderStroke(1.dp, NotionBorder),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = note.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NotionPrimary,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = note.subjectTag,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = NotionSecondary,
                                                        modifier = Modifier
                                                            .background(NotionSurface, RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                    Text(
                                                        text = meta.priority,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (meta.priority == "HIGH") Color(0xFFB91C1C) else NotionPrimary
                                                    )
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    val statusOrder = listOf("TO_DO", "IN_PROGRESS", "REVIEW", "MASTERED")
                                                    val curIdx = statusOrder.indexOf(statusKey).coerceAtLeast(0)
                                                    if (curIdx > 0) {
                                                        TextButton(
                                                            onClick = {
                                                                val updatedMeta = meta.copy(status = statusOrder[curIdx - 1])
                                                                viewModel.updateNoteCollectionMetadata(note.id, updatedMeta.encode())
                                                            },
                                                            modifier = Modifier.height(28.dp)
                                                        ) {
                                                            Text("← Move", fontSize = 10.sp)
                                                        }
                                                    } else {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                    }
                                                    if (curIdx < statusOrder.lastIndex) {
                                                        TextButton(
                                                            onClick = {
                                                                val updatedMeta = meta.copy(status = statusOrder[curIdx + 1])
                                                                viewModel.updateNoteCollectionMetadata(note.id, updatedMeta.encode())
                                                            },
                                                            modifier = Modifier.height(28.dp)
                                                        ) {
                                                            Text("Advance →", fontSize = 10.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                NotionDatabaseViewMode.TABLE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .border(1.dp, NotionBorder, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .background(NotionSurface)
                                .padding(vertical = 8.dp, horizontal = 10.dp)
                        ) {
                            Text("PAGE TITLE", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = NotionPrimary, modifier = Modifier.width(190.dp))
                            Text("STATUS", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = NotionPrimary, modifier = Modifier.width(120.dp))
                            Text("PRIORITY", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = NotionPrimary, modifier = Modifier.width(90.dp))
                            Text("SUBJECT", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = NotionPrimary, modifier = Modifier.width(120.dp))
                            Text("NEXT REVISION", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = NotionPrimary, modifier = Modifier.width(130.dp))
                        }
                        HorizontalDivider(color = NotionBorder)
                        activeNotes.forEachIndexed { idx, note ->
                            val meta = NoteDatabaseMetadata.decode(note.collectionName, idx)
                            Row(
                                modifier = Modifier
                                    .clickable { onOpenNoteInEditor(note.id) }
                                    .background(if (idx % 2 == 0) Color.White else NotionSubtleBg)
                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📄 ${note.title}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NotionPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.width(190.dp)
                                )
                                Box(modifier = Modifier.width(120.dp)) {
                                    AssistChip(
                                        onClick = {
                                            val order = listOf("TO_DO", "IN_PROGRESS", "REVIEW", "MASTERED")
                                            val next = order[(order.indexOf(meta.status) + 1) % order.size]
                                            viewModel.updateNoteCollectionMetadata(note.id, meta.copy(status = next).encode())
                                        },
                                        label = { Text(meta.status.replace("_", " "), fontSize = 10.sp) }
                                    )
                                }
                                Text(
                                    text = meta.priority,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NotionSecondary,
                                    modifier = Modifier.width(90.dp)
                                )
                                Text(
                                    text = note.subjectTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.DarkGray,
                                    modifier = Modifier.width(120.dp)
                                )
                                Text(
                                    text = "🗓 ${meta.nextRevisionLabel} (${meta.revisionIntervalDays}d)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NotionPrimary,
                                    modifier = Modifier.width(130.dp)
                                )
                            }
                            HorizontalDivider(color = NotionBorder)
                        }
                    }
                }

                NotionDatabaseViewMode.CALENDAR -> {
                    // Calendar & Spaced-Repetition Revision Scheduler (#37 + Extra #10)
                    val scheduleBuckets = listOf(
                        "Today" to "🔥 Due Today (Immediate Active Recall)",
                        "Tomorrow" to "📅 Scheduled Tomorrow (+1d / +3d)",
                        "In 3 days" to "🗓 Upcoming This Week (+3d / +7d)",
                        "In 7 days" to "✅ Long-Term Retention (+7d / +14d / +30d)"
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        scheduleBuckets.forEach { (bucketKey, bucketTitle) ->
                            val notesInBucket = activeNotes.filterIndexed { idx, n ->
                                NoteDatabaseMetadata.decode(n.collectionName, idx).nextRevisionLabel == bucketKey
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NotionSubtleBg),
                                border = BorderStroke(1.dp, NotionBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Filled.Schedule, contentDescription = null, tint = NotionPrimary, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = bucketTitle,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = NotionPrimary
                                            )
                                        }
                                        Text(
                                            text = "${notesInBucket.size} page(s)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NotionSecondary
                                        )
                                    }

                                    notesInBucket.forEachIndexed { idx, note ->
                                        val meta = NoteDatabaseMetadata.decode(note.collectionName, idx)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.White, RoundedCornerShape(6.dp))
                                                .border(1.dp, NotionBorder, RoundedCornerShape(6.dp))
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { onOpenNoteInEditor(note.id) }
                                            ) {
                                                Text(
                                                    text = note.title,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NotionPrimary
                                                )
                                                Text(
                                                    text = "Interval: ${meta.revisionIntervalDays}d • Subject: ${note.subjectTag}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = NotionSecondary
                                                )
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    val nextDays = when (meta.revisionIntervalDays) {
                                                        1 -> 3
                                                        3 -> 7
                                                        7 -> 14
                                                        else -> 30
                                                    }
                                                    val nextBucket = when (bucketKey) {
                                                        "Today" -> "Tomorrow"
                                                        "Tomorrow" -> "In 3 days"
                                                        else -> "In 7 days"
                                                    }
                                                    viewModel.updateNoteCollectionMetadata(
                                                        note.id,
                                                        meta.copy(
                                                            revisionIntervalDays = nextDays,
                                                            nextRevisionLabel = nextBucket,
                                                            status = "MASTERED"
                                                        ).encode()
                                                    )
                                                },
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = NotionPrimary)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("MARK REVISED", fontSize = 10.sp, color = NotionPrimary)
                                            }
                                        }
                                    }
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
 * Hub 3: Workspace Organization (Folders, Nested Sub-Pages Tree, Tags, Favourites, Templates, Import/Export).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotionWorkspaceHubSection(
    uiState: NotesStudioUiState,
    viewModel: NotesStudioViewModel,
    onOpenNoteInEditor: (String) -> Unit,
    onOpenFolderModal: () -> Unit,
    onOpenTagModal: () -> Unit,
    onOpenExportImportModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("notion_workspace_hub"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, NotionBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "WORKSPACE HIERARCHY & TEMPLATES",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = NotionPrimary
                    )
                    Text(
                        text = "Nested Sub-Pages • Folders • Tags • Favourites • 11 Templates • Offline Backup",
                        style = MaterialTheme.typography.bodySmall,
                        color = NotionSecondary
                    )
                }
                OutlinedButton(
                    onClick = onOpenExportImportModal,
                    border = BorderStroke(1.dp, NotionPrimary),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Filled.IosShare, contentDescription = null, modifier = Modifier.size(15.dp), tint = NotionPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("IMPORT / EXPORT", fontSize = 11.sp, color = NotionPrimary)
                }
            }

            // Favourites & Pinned Quick Strip (#32)
            val favOrPinned = uiState.allNotes.filter { !it.isTrashed && (it.isFavourite || it.isPinned) }
            if (favOrPinned.isNotEmpty()) {
                Text(
                    text = "★ FAVOURITES & PINNED PAGES (${favOrPinned.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = NotionPrimary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    favOrPinned.forEach { note ->
                        AssistChip(
                            onClick = { onOpenNoteInEditor(note.id) },
                            leadingIcon = {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFE6A100), modifier = Modifier.size(15.dp))
                            },
                            label = { Text(note.title, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Folders & Nested Pages Tree (#26 Nested Pages + #27 Folders)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📁 FOLDERS & NESTED SUB-PAGES TREE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = NotionPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = onOpenFolderModal) {
                        Text("+ New Folder", fontSize = 11.sp, color = NotionPrimary)
                    }
                    TextButton(onClick = onOpenTagModal) {
                        Text("+ New Tag", fontSize = 11.sp, color = NotionPrimary)
                    }
                }
            }

            uiState.folders.forEach { folder ->
                val folderNotes = uiState.allNotes.filter { !it.isTrashed && it.folderId == folder.id }
                val rootNotes = folderNotes.filter { n ->
                    NoteDatabaseMetadata.decode(n.collectionName).parentNoteId == null
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = NotionSubtleBg),
                    border = BorderStroke(1.dp, NotionBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Filled.Folder, contentDescription = null, tint = NotionPrimary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = folder.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NotionPrimary
                                )
                            }
                            Text(
                                text = "${folderNotes.size} pages",
                                style = MaterialTheme.typography.labelSmall,
                                color = NotionSecondary
                            )
                        }

                        rootNotes.forEach { parentNote ->
                            val childPages = uiState.allNotes.filter { child ->
                                !child.isTrashed && NoteDatabaseMetadata.decode(child.collectionName).parentNoteId == parentNote.id
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(6.dp))
                                        .clickable { onOpenNoteInEditor(parentNote.id) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📄 ${parentNote.title}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = NotionPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(
                                        onClick = {
                                            viewModel.createNestedSubPage(parentNote)
                                        },
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("+ Sub-page", fontSize = 10.sp, color = NotionSecondary)
                                    }
                                }

                                // Indented Nested Sub-Pages (#26)
                                childPages.forEach { subPage ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 22.dp)
                                            .background(NotionSurface.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                                            .clickable { onOpenNoteInEditor(subPage.id) }
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.SubdirectoryArrowRight,
                                            contentDescription = null,
                                            tint = NotionSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "📑 ${subPage.title}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = NotionPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Reusable Templates Gallery (#33)
            Text(
                text = "✨ REUSABLE ENGINEERING & ACADEMIC TEMPLATES (${uiState.templates.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = NotionPrimary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.templates.forEach { tpl ->
                    OutlinedButton(
                        onClick = { viewModel.createFromTemplate(tpl.id) },
                        border = BorderStroke(1.dp, NotionPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("[${tpl.category}] ${tpl.title}", fontSize = 11.sp, color = NotionPrimary)
                    }
                }
            }
        }
    }
}

/**
 * Hub 4: Global Full-Text Search, Backlinks Inspector, and Automatic Table of Contents (#29, #30, #31).
 */
@Composable
fun NotionSearchAndBacklinksHubSection(
    uiState: NotesStudioUiState,
    viewModel: NotesStudioViewModel,
    onOpenNoteInEditor: (String) -> Unit,
    onOpenGraphDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val headings = remember(uiState.activeBlocks) {
        uiState.activeBlocks.filter {
            it.type == BlockType.HEADING_1.name ||
                it.type == BlockType.HEADING_2.name ||
                it.type == BlockType.HEADING_3.name ||
                it.type == BlockType.LATEX_BLOCK.name
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("notion_search_hub"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, NotionBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "GLOBAL FULL-TEXT SEARCH, BACKLINKS & TOC",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = NotionPrimary
                    )
                    Text(
                        text = "Search across all pages, equations & code • Navigate bi-directional [[Backlinks]]",
                        style = MaterialTheme.typography.bodySmall,
                        color = NotionSecondary
                    )
                }
                OutlinedButton(
                    onClick = onOpenGraphDialog,
                    border = BorderStroke(1.dp, NotionPrimary)
                ) {
                    Text("◉ GRAPH TOPOLOGY", fontSize = 11.sp, color = NotionPrimary)
                }
            }

            // Matching Notes Results (#29)
            Text(
                text = "SEARCH RESULTS (${uiState.filteredNotes.size} matching pages)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = NotionPrimary
            )
            uiState.filteredNotes.take(6).forEach { note ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NotionSubtleBg, RoundedCornerShape(8.dp))
                        .border(1.dp, NotionBorder, RoundedCornerShape(8.dp))
                        .clickable { onOpenNoteInEditor(note.id) }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📄 ${note.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = NotionPrimary
                        )
                        Text(
                            text = "Subject: ${note.subjectTag} • ID: [[${note.id}]]",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = NotionSecondary
                        )
                    }
                    Text("OPEN →", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = NotionPrimary)
                }
            }

            HorizontalDivider(color = NotionBorder)

            // Automatic Table of Contents (#31)
            Text(
                text = "≡ AUTOMATIC TABLE OF CONTENTS (${uiState.activeNote?.title ?: "Active Page"})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = NotionPrimary
            )
            if (headings.isEmpty()) {
                Text(
                    text = "Add H1, H2, H3 or Equation blocks to generate an automatic Table of Contents.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            } else {
                headings.forEachIndexed { idx, blk ->
                    val type = BlockType.fromName(blk.type)
                    val indent = when (type) {
                        BlockType.HEADING_1 -> 0.dp
                        BlockType.HEADING_2 -> 14.dp
                        BlockType.HEADING_3 -> 28.dp
                        else -> 18.dp
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = indent)
                            .background(NotionSurface.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${idx + 1}. [${type.badge}] ${blk.content.take(65)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (type == BlockType.HEADING_1) FontWeight.Bold else FontWeight.Medium,
                            color = NotionPrimary
                        )
                        Text(
                            text = "Block #${blk.position + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = NotionSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog for Comments & @Mentions (#38) and Sharing & Controlled Access Permissions (#40).
 */
@Composable
fun NoteCommentsAndSharingDialog(
    note: NoteEntity,
    blocks: List<NoteBlockEntity>,
    comments: List<NoteCommentItem>,
    accessLevel: String,
    onAddComment: (String, String) -> Unit,
    onUpdateAccessLevel: (String) -> Unit,
    onCopyShareLink: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var commentDraft by remember { mutableStateOf("") }
    var selectedMention by remember { mutableStateOf("@Prof_Sharma") }
    val mentionOptions = listOf("@Prof_Sharma", "@GATE_Study_Group", "@Peer_Arjun", "@Lab_Partner")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Column {
                Text(
                    text = "SHARE, PERMISSIONS & PAGE COMMENTS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = NotionPrimary
                )
                Text(
                    text = "Page: ${note.title} • ${blocks.size} blocks",
                    style = MaterialTheme.typography.labelSmall,
                    color = NotionSecondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Sharing & Access Control (#40)
                Text(
                    text = "1. Access Control & Permissions",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = NotionPrimary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "PRIVATE" to "🔒 Private Only",
                        "WORKSPACE_VIEW" to "👁 Can View",
                        "WORKSPACE_EDIT" to "✏️ Can Edit & Comment"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = accessLevel == key,
                            onClick = { onUpdateAccessLevel(key) },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        onCopyShareLink("https://mathgate.studio/notes/${note.id}?access=$accessLevel")
                    },
                    border = BorderStroke(1.dp, NotionPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Public, contentDescription = null, modifier = Modifier.size(16.dp), tint = NotionPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("COPY CONTROLLED SHARE LINK", fontSize = 11.sp, color = NotionPrimary)
                }

                HorizontalDivider(color = NotionBorder)

                // Comments & @Mentions (#38)
                Text(
                    text = "2. Page Discussion & @Mentions (${comments.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = NotionPrimary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    mentionOptions.forEach { mention ->
                        FilterChip(
                            selected = selectedMention == mention,
                            onClick = { selectedMention = mention },
                            label = { Text(mention, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = commentDraft,
                    onValueChange = { commentDraft = it },
                    placeholder = { Text("Add a review note or annotate with $selectedMention…") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_comment_input")
                )

                Button(
                    onClick = {
                        if (commentDraft.isNotBlank()) {
                            onAddComment(selectedMention, commentDraft.trim())
                            commentDraft = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NotionPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("POST COMMENT WITH $selectedMention", fontSize = 11.sp)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(comments, key = { it.id }) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NotionSubtleBg),
                            border = BorderStroke(1.dp, NotionBorder)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${item.author} → ${item.mention}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NotionPrimary
                                    )
                                    Text(
                                        text = item.timestampLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NotionSecondary
                                    )
                                }
                                Text(
                                    text = item.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("DONE", color = NotionPrimary)
            }
        }
    )
}
