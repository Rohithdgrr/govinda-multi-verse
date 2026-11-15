package com.example.feature.notes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.FolderEntity
import com.example.core.database.NoteAttachmentEntity
import com.example.core.database.NoteBlockEntity
import com.example.core.database.NoteEntity
import com.example.core.database.NoteLinkEntity
import com.example.core.database.NoteRevisionEntity
import com.example.core.database.NoteTagCrossRef
import com.example.core.database.NoteTemplateEntity
import com.example.core.database.TagEntity
import com.example.core.document.BlockType
import com.example.core.document.TableData
import com.example.core.export.NotesExportImportManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class NotesHomeFilter(val displayName: String) {
    ALL("All Notes"),
    RECENT("Recent"),
    FAVOURITES("Favourites"),
    PINNED("Pinned"),
    ARCHIVED("Archived"),
    TRASH("Trash"),
    TEMPLATES("Templates")
}

data class NotesStudioUiState(
    val allNotes: List<NoteEntity> = emptyList(),
    val filteredNotes: List<NoteEntity> = emptyList(),
    val folders: List<FolderEntity> = emptyList(),
    val tags: List<TagEntity> = emptyList(),
    val noteTagRefs: List<NoteTagCrossRef> = emptyList(),
    val templates: List<NoteTemplateEntity> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: NotesHomeFilter = NotesHomeFilter.ALL,
    val selectedFolderId: String? = null,
    val selectedSubjectFilter: String? = null,
    val selectedTagId: String? = null,
    val activeNote: NoteEntity? = null,
    val activeBlocks: List<NoteBlockEntity> = emptyList(),
    val activeBacklinks: List<NoteLinkEntity> = emptyList(),
    val activeOutgoingLinks: List<NoteLinkEntity> = emptyList(),
    val activeRevisions: List<NoteRevisionEntity> = emptyList(),
    val activeAttachments: List<NoteAttachmentEntity> = emptyList(),
    val isReadingMode: Boolean = false,
    val isMarkdownLiveMode: Boolean = false,
    val showEquationNumbers: Boolean = true,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val exportedPreviewTitle: String? = null,
    val exportedPreviewContent: String? = null,
    val statusBanner: String? = null,
    val commentsByNoteId: Map<String, List<com.example.feature.notes.features.NoteCommentItem>> = mapOf(
        "note_001" to listOf(
            com.example.feature.notes.features.NoteCommentItem(
                id = "cmt_seed_1",
                author = "You",
                mention = "@Prof_Sharma",
                text = "Verified De Morgan's NAND realization truth table for GATE ECE.",
                timestampLabel = "Today, 09:30 AM"
            )
        )
    )
)

class NotesStudioViewModel(
    private val repository: NotesStudioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesStudioUiState())
    val uiState: StateFlow<NotesStudioUiState> = _uiState.asStateFlow()

    private val undoStack = ArrayDeque<List<NoteBlockEntity>>()
    private val redoStack = ArrayDeque<List<NoteBlockEntity>>()
    private var activeNoteObserverJob: Job? = null
    private var autoSaveJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            combine(
                repository.notesFlow,
                repository.foldersFlow,
                repository.tagsFlow,
                repository.noteTagRefsFlow,
                repository.templatesFlow
            ) { notes, folders, tags, refs, templates ->
                 Quintuple(notes, folders, tags, refs, templates)
            }.collect { (notes, folders, tags, refs, templates) ->
                _uiState.update { state ->
                    val currentActive = state.activeNote?.let { act ->
                        notes.firstOrNull { it.id == act.id }
                    } ?: notes.firstOrNull { !it.isTrashed && !it.isArchived }
                    state.copy(
                        allNotes = notes,
                        folders = folders,
                        tags = tags,
                        noteTagRefs = refs,
                        templates = templates,
                        activeNote = currentActive
                    )
                }
                val targetId = _uiState.value.activeNote?.id
                if (targetId != null && activeNoteObserverJob == null) {
                    openNote(targetId)
                }
                recomputeFilteredNotes()
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

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        recomputeFilteredNotes()
    }

    fun setFilter(filter: NotesHomeFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
        recomputeFilteredNotes()
    }

    fun selectFolderFilter(folderId: String?) {
        _uiState.update {
            it.copy(selectedFolderId = if (it.selectedFolderId == folderId) null else folderId)
        }
        recomputeFilteredNotes()
    }

    fun selectTagFilter(tagId: String?) {
        _uiState.update {
            it.copy(selectedTagId = if (it.selectedTagId == tagId) null else tagId)
        }
        recomputeFilteredNotes()
    }

    fun selectSubjectFilter(subject: String?) {
        _uiState.update {
            it.copy(selectedSubjectFilter = if (it.selectedSubjectFilter == subject) null else subject)
        }
        recomputeFilteredNotes()
    }

    private fun recomputeFilteredNotes() {
        viewModelScope.launch {
            val state = _uiState.value
            val baseList = if (state.searchQuery.isNotBlank()) {
                repository.searchNotes(state.searchQuery)
            } else {
                state.allNotes
            }
            val tagFilteredIds = state.selectedTagId?.let { tid ->
                state.noteTagRefs.filter { it.tagId == tid }.map { it.noteId }.toSet()
            }

            val filtered = baseList.filter { n ->
                val matchesTab = when (state.activeFilter) {
                    NotesHomeFilter.ALL, NotesHomeFilter.TEMPLATES -> !n.isTrashed && !n.isArchived
                    NotesHomeFilter.RECENT -> !n.isTrashed && !n.isArchived
                    NotesHomeFilter.FAVOURITES -> !n.isTrashed && n.isFavourite
                    NotesHomeFilter.PINNED -> !n.isTrashed && n.isPinned
                    NotesHomeFilter.ARCHIVED -> !n.isTrashed && n.isArchived
                    NotesHomeFilter.TRASH -> n.isTrashed
                }
                val matchesFolder = state.selectedFolderId == null || n.folderId == state.selectedFolderId
                val matchesSubject = state.selectedSubjectFilter == null || n.subjectTag.equals(state.selectedSubjectFilter, ignoreCase = true)
                val matchesTag = tagFilteredIds == null || n.id in tagFilteredIds
                matchesTab && matchesFolder && matchesSubject && matchesTag
            }
            _uiState.update { it.copy(filteredNotes = filtered) }
        }
    }

    fun openNote(noteId: String) {
        activeNoteObserverJob?.cancel()
        undoStack.clear()
        redoStack.clear()
        activeNoteObserverJob = viewModelScope.launch {
            val pair = repository.getNoteWithBlocks(noteId) ?: return@launch
            _uiState.update {
                it.copy(
                    activeNote = pair.first,
                    activeBlocks = pair.second,
                    canUndo = false,
                    canRedo = false
                )
            }
            combine(
                repository.observeBlocks(noteId),
                repository.observeBacklinks(noteId),
                repository.observeOutgoingLinks(noteId),
                repository.observeRevisions(noteId),
                repository.observeAttachments(noteId)
            ) { blocks, backlinks, outgoing, revisions, attachments ->
                Quintuple(blocks, backlinks, outgoing, revisions, attachments)
            }.collectLatest { (blocks, backlinks, outgoing, revisions, attachments) ->
                val refreshedNote = _uiState.value.allNotes.firstOrNull { it.id == noteId } ?: _uiState.value.activeNote
                _uiState.update {
                    it.copy(
                        activeNote = refreshedNote,
                        activeBlocks = blocks,
                        activeBacklinks = backlinks,
                        activeOutgoingLinks = outgoing,
                        activeRevisions = revisions,
                        activeAttachments = attachments
                    )
                }
            }
        }
    }

    fun closeActiveNoteOnCompact() {
        activeNoteObserverJob?.cancel()
        activeNoteObserverJob = null
        _uiState.update { it.copy(activeNote = null) }
    }

    fun createNewNote(title: String = "Untitled Engineering Note") {
        viewModelScope.launch {
            val created = repository.createNote(
                title = title,
                folderId = _uiState.value.selectedFolderId ?: "ece"
            )
            openNote(created.id)
            _uiState.update { it.copy(statusBanner = "Created note '${created.title}'") }
        }
    }

    fun createFromTemplate(templateId: String) {
        viewModelScope.launch {
            val created = repository.createNoteFromTemplate(
                templateId = templateId,
                folderId = _uiState.value.selectedFolderId ?: "ece"
            )
            _uiState.update { it.copy(activeFilter = NotesHomeFilter.ALL, statusBanner = "Created from template: ${created.title}") }
            openNote(created.id)
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            repository.createFolder(name)
            _uiState.update { it.copy(statusBanner = "Folder '$name' created") }
        }
    }

    fun createTag(name: String) {
        viewModelScope.launch {
            repository.createTag(name)
            _uiState.update { it.copy(statusBanner = "Tag '#$name' created") }
        }
    }

    fun toggleTagOnActiveNote(tagId: String) {
        val note = _uiState.value.activeNote ?: return
        val attached = _uiState.value.noteTagRefs.any { it.noteId == note.id && it.tagId == tagId }
        viewModelScope.launch {
            repository.toggleTagOnNote(note.id, tagId, attached)
        }
    }

    fun updateActiveNoteTitle(newTitle: String) {
        val note = _uiState.value.activeNote ?: return
        val updated = note.copy(title = newTitle)
        _uiState.update { it.copy(activeNote = updated) }
        scheduleDebouncedAutoSave(recordRevision = false)
    }

    fun toggleFavouriteActiveNote() {
        val note = _uiState.value.activeNote ?: return
        viewModelScope.launch {
            repository.updateNoteMetadata({ it.copy(isFavourite = !it.isFavourite) }, note.id)
        }
    }

    fun togglePinActiveNote() {
        val note = _uiState.value.activeNote ?: return
        viewModelScope.launch {
            repository.updateNoteMetadata({ it.copy(isPinned = !it.isPinned) }, note.id)
        }
    }

    fun toggleArchiveActiveNote() {
        val note = _uiState.value.activeNote ?: return
        viewModelScope.launch {
            repository.updateNoteMetadata({ it.copy(isArchived = !it.isArchived) }, note.id)
        }
    }

    fun toggleTrashActiveNote() {
        val note = _uiState.value.activeNote ?: return
        viewModelScope.launch {
            repository.updateNoteMetadata({ it.copy(isTrashed = !it.isTrashed) }, note.id)
        }
    }

    fun toggleReadingMode() {
        _uiState.update { it.copy(isReadingMode = !it.isReadingMode, isMarkdownLiveMode = false) }
    }

    fun toggleMarkdownLiveMode() {
        _uiState.update { it.copy(isMarkdownLiveMode = !it.isMarkdownLiveMode, isReadingMode = false) }
    }

    fun setEditorDisplayMode(mode: String) {
        when (mode) {
            "READING" -> _uiState.update { it.copy(isReadingMode = true, isMarkdownLiveMode = false) }
            "MARKDOWN_LIVE" -> _uiState.update { it.copy(isReadingMode = false, isMarkdownLiveMode = true) }
            else -> _uiState.update { it.copy(isReadingMode = false, isMarkdownLiveMode = false) }
        }
    }

    fun applyLiveMarkdownToActiveNote(markdownText: String) {
        val note = _uiState.value.activeNote ?: return
        pushUndoSnapshot()
        viewModelScope.launch {
            runCatching {
                val (parsedNote, parsedBlocks) = NotesExportImportManager.importFromMarkdown(markdownText)
                val updatedNote = note.copy(
                    title = parsedNote.title.ifBlank { note.title },
                    updatedAt = System.currentTimeMillis()
                )
                val reindexedBlocks = parsedBlocks.mapIndexed { idx, blk ->
                    blk.copy(noteId = note.id, position = idx)
                }
                repository.saveNoteAndBlocks(updatedNote, reindexedBlocks, recordRevision = false)
                _uiState.update {
                    it.copy(
                        activeNote = updatedNote,
                        activeBlocks = reindexedBlocks,
                        statusBanner = "Synced live Markdown source (${reindexedBlocks.size} blocks)"
                    )
                }
            }
        }
    }

    fun updateNoteCollectionMetadata(noteId: String, encodedMetadata: String) {
        viewModelScope.launch {
            repository.updateNoteMetadata(
                transform = { it.copy(collectionName = encodedMetadata) },
                noteId = noteId
            )
            recomputeFilteredNotes()
        }
    }

    fun createNestedSubPage(parentNote: NoteEntity) {
        viewModelScope.launch {
            val childMeta = com.example.feature.notes.features.NoteDatabaseMetadata(
                status = "IN_PROGRESS",
                priority = "HIGH",
                parentNoteId = parentNote.id,
                revisionIntervalDays = 3,
                nextRevisionLabel = "Tomorrow"
            ).encode()
            val subPage = repository.createNote(
                title = "${parentNote.title} — Sub-Page",
                folderId = parentNote.folderId,
                subjectTag = parentNote.subjectTag,
                initialBlocks = listOf(
                    BlockType.HEADING_1 to "${parentNote.title} — Sub-Page Notes",
                    BlockType.BOOKMARK to "[[${parentNote.id}|Parent: ${parentNote.title}]]",
                    BlockType.PARAGRAPH to "Detailed derivation and sub-topic notes linked to ${parentNote.title}."
                )
            )
            repository.updateNoteMetadata(
                transform = { it.copy(collectionName = childMeta) },
                noteId = subPage.id
            )
            openNote(subPage.id)
            _uiState.update { it.copy(statusBanner = "Created nested sub-page under '${parentNote.title}'") }
        }
    }

    fun addCommentToActiveNote(mention: String, text: String) {
        val note = _uiState.value.activeNote ?: return
        val newItem = com.example.feature.notes.features.NoteCommentItem(
            id = "cmt_${UUID.randomUUID().toString().take(6)}",
            author = "You",
            mention = mention,
            text = text,
            timestampLabel = "Just now"
        )
        _uiState.update { state ->
            val existing = state.commentsByNoteId[note.id].orEmpty()
            state.copy(
                commentsByNoteId = state.commentsByNoteId + (note.id to (listOf(newItem) + existing)),
                statusBanner = "Posted comment mentioning $mention"
            )
        }
    }

    fun postStatusBanner(message: String) {
        _uiState.update { it.copy(statusBanner = message) }
    }

    fun toggleEquationNumbers() {
        _uiState.update { it.copy(showEquationNumbers = !it.showEquationNumbers) }
    }

    private fun pushUndoSnapshot() {
        val current = _uiState.value.activeBlocks
        if (undoStack.size >= 30) undoStack.removeFirst()
        undoStack.addLast(current)
        redoStack.clear()
        _uiState.update { it.copy(canUndo = undoStack.isNotEmpty(), canRedo = false) }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = _uiState.value.activeBlocks
        redoStack.addLast(current)
        val previous = undoStack.removeLast()
        _uiState.update {
            it.copy(
                activeBlocks = previous,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        scheduleDebouncedAutoSave(recordRevision = false)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = _uiState.value.activeBlocks
        undoStack.addLast(current)
        val next = redoStack.removeLast()
        _uiState.update {
            it.copy(
                activeBlocks = next,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        scheduleDebouncedAutoSave(recordRevision = false)
    }

    fun addBlock(type: BlockType, initialContent: String? = null, metadata: String? = null) {
        val note = _uiState.value.activeNote ?: return
        pushUndoSnapshot()
        val now = System.currentTimeMillis()
        val currentBlocks = _uiState.value.activeBlocks.toMutableList()
        val defaultPayload = initialContent ?: when (type) {
            BlockType.TABLE -> TableData.defaultComponentTable().toJson()
            else -> type.defaultContent
        }
        val newBlock = NoteBlockEntity(
            id = "blk_${UUID.randomUUID().toString().take(8)}",
            noteId = note.id,
            type = type.name,
            position = currentBlocks.size,
            content = defaultPayload,
            metadata = metadata ?: if (type == BlockType.CODE_BLOCK) "KOTLIN" else null,
            createdAt = now,
            updatedAt = now
        )
        currentBlocks.add(newBlock)
        _uiState.update { it.copy(activeBlocks = currentBlocks) }
        scheduleDebouncedAutoSave(recordRevision = false)
    }

    fun updateBlockContent(blockId: String, newContent: String, newMetadata: String? = null) {
        pushUndoSnapshot()
        val updated = _uiState.value.activeBlocks.map { blk ->
            if (blk.id == blockId) {
                blk.copy(
                    content = newContent,
                    metadata = newMetadata ?: blk.metadata,
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                blk
            }
        }
        _uiState.update { it.copy(activeBlocks = updated) }
        scheduleDebouncedAutoSave(recordRevision = false)
    }

    fun convertBlockType(blockId: String, targetType: BlockType) {
        pushUndoSnapshot()
        val updated = _uiState.value.activeBlocks.map { blk ->
            if (blk.id == blockId) {
                val content = if (targetType == BlockType.TABLE && blk.type != BlockType.TABLE.name) {
                    TableData.defaultComponentTable().toJson()
                } else {
                    blk.content
                }
                blk.copy(type = targetType.name, content = content, updatedAt = System.currentTimeMillis())
            } else {
                blk
            }
        }
        _uiState.update { it.copy(activeBlocks = updated) }
        scheduleDebouncedAutoSave(recordRevision = false)
    }

    fun duplicateBlock(blockId: String) {
        pushUndoSnapshot()
        val current = _uiState.value.activeBlocks.toMutableList()
        val idx = current.indexOfFirst { it.id == blockId }
        if (idx == -1) return
        val orig = current[idx]
        val copy = orig.copy(
            id = "blk_${UUID.randomUUID().toString().take(8)}",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        current.add(idx + 1, copy)
        val reindexed = current.mapIndexed { i, b -> b.copy(position = i) }
        _uiState.update { it.copy(activeBlocks = reindexed) }
        scheduleDebouncedAutoSave(recordRevision = false)
    }

    fun moveBlock(blockId: String, delta: Int) {
        pushUndoSnapshot()
        val current = _uiState.value.activeBlocks.toMutableList()
        val idx = current.indexOfFirst { it.id == blockId }
        if (idx == -1) return
        val targetIdx = (idx + delta).coerceIn(0, current.lastIndex)
        if (idx == targetIdx) return
        val item = current.removeAt(idx)
        current.add(targetIdx, item)
        val reindexed = current.mapIndexed { i, b -> b.copy(position = i) }
        _uiState.update { it.copy(activeBlocks = reindexed) }
        scheduleDebouncedAutoSave(recordRevision = false)
    }

    fun deleteBlock(blockId: String) {
        pushUndoSnapshot()
        val remaining = _uiState.value.activeBlocks
            .filterNot { it.id == blockId }
            .mapIndexed { i, b -> b.copy(position = i) }
        _uiState.update { it.copy(activeBlocks = remaining) }
        scheduleDebouncedAutoSave(recordRevision = false)
    }

    fun attachImageUriToBlock(blockId: String, uriString: String, caption: String) {
        val note = _uiState.value.activeNote ?: return
        updateBlockContent(blockId, uriString, caption)
        viewModelScope.launch {
            repository.addAttachment(
                NoteAttachmentEntity(
                    id = "att_${UUID.randomUUID().toString().take(8)}",
                    noteId = note.id,
                    blockId = blockId,
                    uriString = uriString,
                    mimeType = "image/*",
                    caption = caption,
                    altText = caption,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun saveNowWithRevision() {
        autoSaveJob?.cancel()
        val note = _uiState.value.activeNote ?: return
        val blocks = _uiState.value.activeBlocks
        viewModelScope.launch {
            repository.saveNoteAndBlocks(note, blocks, recordRevision = true)
            _uiState.update { it.copy(statusBanner = "Saved revision snapshot for '${note.title}'") }
        }
    }

    fun restoreRevision(revision: NoteRevisionEntity) {
        viewModelScope.launch {
            val (note, blocks) = NotesExportImportManager.importFromJson(revision.blocksJsonSnapshot)
            repository.saveNoteAndBlocks(note, blocks, recordRevision = true)
            openNote(note.id)
            _uiState.update { it.copy(statusBanner = "Restored revision from history") }
        }
    }

    fun exportActiveNote(format: String, context: Context? = null) {
        val note = _uiState.value.activeNote ?: return
        val blocks = _uiState.value.activeBlocks
        viewModelScope.launch {
            when (format.uppercase()) {
                "MARKDOWN" -> {
                    val md = NotesExportImportManager.exportToMarkdown(note, blocks)
                    _uiState.update {
                        it.copy(
                            exportedPreviewTitle = "${note.title}.md",
                            exportedPreviewContent = md
                        )
                    }
                }
                "JSON" -> {
                    val json = NotesExportImportManager.exportToJson(note, blocks)
                    _uiState.update {
                        it.copy(
                            exportedPreviewTitle = "${note.title}.json",
                            exportedPreviewContent = json
                        )
                    }
                }
                "HTML" -> {
                    val html = NotesExportImportManager.exportToHtml(note, blocks)
                    _uiState.update {
                        it.copy(
                            exportedPreviewTitle = "${note.title}.html",
                            exportedPreviewContent = html
                        )
                    }
                }
                "TXT" -> {
                    val txt = NotesExportImportManager.exportToPlainText(note, blocks)
                    _uiState.update {
                        it.copy(
                            exportedPreviewTitle = "${note.title}.txt",
                            exportedPreviewContent = txt
                        )
                    }
                }
                "CSV" -> {
                    val tableBlock = blocks.firstOrNull { it.type == BlockType.TABLE.name }
                    val csv = TableData.fromJsonOrDefault(tableBlock?.content.orEmpty()).toCsv()
                    _uiState.update {
                        it.copy(
                            exportedPreviewTitle = "${note.title}_table.csv",
                            exportedPreviewContent = csv
                        )
                    }
                }
                "PDF" -> {
                    val pdfBytes = NotesExportImportManager.exportToPdfBytes(note, blocks)
                    if (context != null) {
                        val f = NotesExportImportManager.saveExportToCacheFile(context, "${note.id}.pdf", pdfBytes)
                        _uiState.update {
                            it.copy(
                                exportedPreviewTitle = "${note.title}.pdf (${pdfBytes.size} bytes)",
                                exportedPreviewContent = "Generated offline PDF at:\n${f.absolutePath}\n\nSize: ${pdfBytes.size} bytes\nBlocks rendered: ${blocks.size}"
                            )
                        }
                    }
                }
                "ZIP" -> {
                    val all = repository.getAllNotesWithBlocks()
                    val zipBytes = NotesExportImportManager.exportAllToZipBytes(all)
                    if (context != null) {
                        val f = NotesExportImportManager.saveExportToCacheFile(context, "mathgate_notes_backup.zip", zipBytes)
                        _uiState.update {
                            it.copy(
                                exportedPreviewTitle = "mathgate_notes_backup.zip (${zipBytes.size} bytes)",
                                exportedPreviewContent = "ZIP archive created at:\n${f.absolutePath}\n\nNotes bundled: ${all.size}\nArchive size: ${zipBytes.size} bytes"
                            )
                        }
                    }
                }
            }
        }
    }

    fun importFromRawText(rawText: String, isJson: Boolean) {
        viewModelScope.launch {
            runCatching {
                val (note, blocks) = if (isJson) {
                    NotesExportImportManager.importFromJson(rawText)
                } else {
                    NotesExportImportManager.importFromMarkdown(rawText)
                }
                repository.importNote(note, blocks)
                openNote(note.id)
                _uiState.update { it.copy(statusBanner = "Imported '${note.title}' (${blocks.size} blocks)") }
            }.onFailure { err ->
                _uiState.update { it.copy(statusBanner = "Import error: ${err.message}") }
            }
        }
    }

    fun dismissExportPreview() {
        _uiState.update { it.copy(exportedPreviewTitle = null, exportedPreviewContent = null) }
    }

    fun clearStatusBanner() {
        _uiState.update { it.copy(statusBanner = null) }
    }

    fun duplicateActiveNote() {
        val note = _uiState.value.activeNote ?: return
        val blocks = _uiState.value.activeBlocks
        viewModelScope.launch {
            val pairs = blocks.map { BlockType.fromName(it.type) to it.content }
            val created = repository.createNote(
                title = "${note.title} (Copy)",
                folderId = note.folderId,
                subjectTag = note.subjectTag,
                initialBlocks = pairs
            )
            openNote(created.id)
            _uiState.update { it.copy(statusBanner = "Duplicated note as '${created.title}'") }
        }
    }

    fun autoNumberHeadings() {
        pushUndoSnapshot()
        var h1 = 0
        var h2 = 0
        var h3 = 0
        val numberPrefixRegex = Regex("^\\d+(?:\\.\\d+)*\\s+")
        val updated = _uiState.value.activeBlocks.map { blk ->
            val clean = blk.content.replace(numberPrefixRegex, "")
            when (BlockType.fromName(blk.type)) {
                BlockType.HEADING_1 -> {
                    h1++
                    h2 = 0
                    h3 = 0
                    blk.copy(content = "$h1. $clean", updatedAt = System.currentTimeMillis())
                }
                BlockType.HEADING_2 -> {
                    if (h1 == 0) h1 = 1
                    h2++
                    h3 = 0
                    blk.copy(content = "$h1.$h2 $clean", updatedAt = System.currentTimeMillis())
                }
                BlockType.HEADING_3 -> {
                    if (h1 == 0) h1 = 1
                    if (h2 == 0) h2 = 1
                    h3++
                    blk.copy(content = "$h1.$h2.$h3 $clean", updatedAt = System.currentTimeMillis())
                }
                else -> blk
            }
        }
        _uiState.update {
            it.copy(
                activeBlocks = updated,
                statusBanner = "Auto-numbered section headings"
            )
        }
        scheduleDebouncedAutoSave(recordRevision = false)
    }

    fun findAndReplaceInActiveNote(findText: String, replaceText: String, ignoreCase: Boolean): Int {
        if (findText.isEmpty()) return 0
        pushUndoSnapshot()
        var modifiedCount = 0
        val regex = Regex(Regex.escape(findText), if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet())
        val updated = _uiState.value.activeBlocks.map { blk ->
            if (regex.containsMatchIn(blk.content)) {
                modifiedCount++
                blk.copy(
                    content = regex.replace(blk.content, Regex.escapeReplacement(replaceText)),
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                blk
            }
        }
        if (modifiedCount > 0) {
            _uiState.update {
                it.copy(
                    activeBlocks = updated,
                    statusBanner = "Replaced '$findText' in $modifiedCount block(s)"
                )
            }
            scheduleDebouncedAutoSave(recordRevision = false)
        }
        return modifiedCount
    }

    fun insertBlockAfter(
        afterBlockId: String,
        type: BlockType,
        initialContent: String? = null,
        metadata: String? = null
    ) {
        val note = _uiState.value.activeNote ?: return
        pushUndoSnapshot()
        val now = System.currentTimeMillis()
        val currentBlocks = _uiState.value.activeBlocks.toMutableList()
        val defaultPayload = initialContent ?: when (type) {
            BlockType.TABLE -> TableData.defaultComponentTable().toJson()
            else -> type.defaultContent
        }
        val newBlock = NoteBlockEntity(
            id = "blk_${UUID.randomUUID().toString().take(8)}",
            noteId = note.id,
            type = type.name,
            position = currentBlocks.size,
            content = defaultPayload,
            metadata = metadata ?: if (type == BlockType.CODE_BLOCK) "KOTLIN" else null,
            createdAt = now,
            updatedAt = now
        )
        val idx = currentBlocks.indexOfFirst { it.id == afterBlockId }
        if (idx >= 0) {
            currentBlocks.add(idx + 1, newBlock)
        } else {
            currentBlocks.add(newBlock)
        }
        val reindexed = currentBlocks.mapIndexed { i, b -> b.copy(position = i) }
        _uiState.update { it.copy(activeBlocks = reindexed) }
        scheduleDebouncedAutoSave(recordRevision = false)
    }

    fun updateActiveNoteSubject(newSubject: String) {
        val note = _uiState.value.activeNote ?: return
        viewModelScope.launch {
            repository.updateNoteMetadata(
                transform = { it.copy(subjectTag = newSubject, updatedAt = System.currentTimeMillis()) },
                noteId = note.id
            )
            _uiState.update {
                it.copy(
                    activeNote = it.activeNote?.copy(subjectTag = newSubject),
                    statusBanner = "Updated subject to $newSubject"
                )
            }
            recomputeFilteredNotes()
        }
    }

    fun updateActiveNoteFolder(newFolderId: String) {
        val note = _uiState.value.activeNote ?: return
        viewModelScope.launch {
            repository.updateNoteMetadata(
                transform = { it.copy(folderId = newFolderId, updatedAt = System.currentTimeMillis()) },
                noteId = note.id
            )
            _uiState.update {
                it.copy(
                    activeNote = it.activeNote?.copy(folderId = newFolderId),
                    statusBanner = "Moved page to folder '$newFolderId'"
                )
            }
            recomputeFilteredNotes()
        }
    }

    fun sortChecklistsInActiveNote() {
        pushUndoSnapshot()
        val updated = _uiState.value.activeBlocks.map { blk ->
            if (blk.type == BlockType.CHECKLIST.name) {
                val lines = blk.content.lines().filter { it.isNotBlank() }
                val sorted = lines.sortedBy { it.trim().startsWith("[x]", ignoreCase = true) }
                blk.copy(content = sorted.joinToString("\n"), updatedAt = System.currentTimeMillis())
            } else {
                blk
            }
        }
        _uiState.update {
            it.copy(
                activeBlocks = updated,
                statusBanner = "Sorted checklist items (pending tasks first)"
            )
        }
        scheduleDebouncedAutoSave(recordRevision = false)
    }

    fun generateExecutiveTakeawayBlock() {
        val note = _uiState.value.activeNote ?: return
        val blocks = _uiState.value.activeBlocks
        val headingCount = blocks.count {
            it.type == BlockType.HEADING_1.name ||
                it.type == BlockType.HEADING_2.name ||
                it.type == BlockType.HEADING_3.name
        }
        val eqCount = blocks.count { it.type == BlockType.LATEX_BLOCK.name }
        val firstFormula = blocks.firstOrNull { it.type == BlockType.LATEX_BLOCK.name }?.content?.take(45)
        val summary = buildString {
            append("📌 Executive Key Takeaway (${note.title}): ")
            append("Covers $headingCount structured modules and $eqCount mathematical derivations. ")
            if (!firstFormula.isNullOrBlank()) {
                append("Core formula: $$firstFormula$.")
            }
        }
        addBlock(BlockType.CALLOUT, initialContent = summary, metadata = "BLUE")
        _uiState.update { it.copy(statusBanner = "Inserted Executive Key Takeaway block") }
    }

    private fun scheduleDebouncedAutoSave(recordRevision: Boolean) {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(350L)
            val note = _uiState.value.activeNote ?: return@launch
            val blocks = _uiState.value.activeBlocks
            repository.saveNoteAndBlocks(note, blocks, recordRevision = recordRevision)
        }
    }

    class Factory(
        private val repository: NotesStudioRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotesStudioViewModel(repository) as T
        }
    }
}
