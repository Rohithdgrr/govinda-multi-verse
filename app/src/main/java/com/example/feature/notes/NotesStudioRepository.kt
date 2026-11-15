package com.example.feature.notes

import androidx.room.withTransaction
import com.example.core.database.FolderEntity
import com.example.core.database.NoteAttachmentEntity
import com.example.core.database.NoteBlockEntity
import com.example.core.database.NoteEntity
import com.example.core.database.NoteLinkEntity
import com.example.core.database.NoteRevisionEntity
import com.example.core.database.NoteTagCrossRef
import com.example.core.database.NoteTemplateEntity
import com.example.core.database.NotesStudioDao
import com.example.core.database.TagEntity
import com.example.core.document.BlockType
import com.example.core.document.NoteTemplateCatalog
import com.example.core.document.TableData
import com.example.core.export.NotesExportImportManager
import com.example.data.local.database.GovindaDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class NotesStudioRepository(
    private val database: GovindaDatabase
) {
    private val dao: NotesStudioDao = database.notesStudioDao()
    private val seedMutex = Mutex()
    @Volatile
    private var isSeeded = false

    val notesFlow: Flow<List<NoteEntity>> = dao.observeAllNotes()
    val foldersFlow: Flow<List<FolderEntity>> = dao.observeFolders()
    val tagsFlow: Flow<List<TagEntity>> = dao.observeTags()
    val noteTagRefsFlow: Flow<List<NoteTagCrossRef>> = dao.observeNoteTagCrossRefs()
    val templatesFlow: Flow<List<NoteTemplateEntity>> = dao.observeTemplates()

    fun observeBlocks(noteId: String): Flow<List<NoteBlockEntity>> = dao.observeBlocksForNote(noteId)
    fun observeBacklinks(noteId: String): Flow<List<NoteLinkEntity>> = dao.observeBacklinksForNote(noteId)
    fun observeOutgoingLinks(noteId: String): Flow<List<NoteLinkEntity>> = dao.observeOutgoingLinksForNote(noteId)
    fun observeRevisions(noteId: String): Flow<List<NoteRevisionEntity>> = dao.observeRevisionsForNote(noteId)
    fun observeAttachments(noteId: String): Flow<List<NoteAttachmentEntity>> = dao.observeAttachmentsForNote(noteId)

    suspend fun ensureSeeded() {
        if (isSeeded) return
        seedMutex.withLock {
            if (isSeeded) return
            if (dao.countNotes() == 0) {
                seedInitialWorkspace()
            }
            isSeeded = true
        }
    }

    suspend fun getCounts(): Triple<Int, Int, Int> {
        ensureSeeded()
        return Triple(dao.countNotes(), dao.countBlocks(), dao.countRevisions())
    }

    suspend fun getNoteWithBlocks(noteId: String): Pair<NoteEntity, List<NoteBlockEntity>>? {
        ensureSeeded()
        val note = dao.getNoteById(noteId) ?: return null
        val blocks = dao.getBlocksForNote(noteId)
        return note to blocks
    }

    suspend fun getAllNotesWithBlocks(): List<Pair<NoteEntity, List<NoteBlockEntity>>> {
        ensureSeeded()
        val notes = dao.getAllNotes()
        val allBlocks = dao.getAllBlocks().groupBy { it.noteId }
        return notes.map { n -> n to (allBlocks[n.id] ?: emptyList()) }
    }

    suspend fun searchNotes(query: String): List<NoteEntity> {
        ensureSeeded()
        if (query.isBlank()) return dao.getAllNotes()
        return dao.searchNotesFullText(query.trim())
    }

    suspend fun createNote(
        title: String = "Untitled Engineering Note",
        folderId: String? = "ece",
        subjectTag: String = "ENGINEERING",
        initialBlocks: List<Pair<BlockType, String>> = listOf(
            BlockType.HEADING_1 to title,
            BlockType.PARAGRAPH to "Start writing structured blocks, LaTeX equations, tables, or code.",
            BlockType.LATEX_BLOCK to "H(s) = \\frac{Y(s)}{X(s)}"
        )
    ): NoteEntity {
        ensureSeeded()
        val now = System.currentTimeMillis()
        val noteId = "note_${UUID.randomUUID().toString().take(8)}"
        val note = NoteEntity(
            id = noteId,
            title = title,
            folderId = folderId,
            collectionName = "Engineering Workspace",
            subjectTag = subjectTag,
            createdAt = now,
            updatedAt = now
        )
        val blocks = initialBlocks.mapIndexed { idx, (type, content) ->
            NoteBlockEntity(
                id = "blk_${UUID.randomUUID().toString().take(8)}",
                noteId = noteId,
                type = type.name,
                position = idx,
                content = content,
                createdAt = now,
                updatedAt = now
            )
        }
        database.withTransaction {
            dao.upsertNote(note)
            dao.upsertBlocks(blocks)
            refreshLinksForNote(noteId, blocks)
            saveRevisionSnapshot(note, blocks, now)
        }
        return note
    }

    suspend fun createNoteFromTemplate(templateId: String, folderId: String?): NoteEntity {
        ensureSeeded()
        val spec = NoteTemplateCatalog.allTemplates.firstOrNull { it.id == templateId }
            ?: NoteTemplateCatalog.allTemplates.first()
        return createNote(
            title = spec.title,
            folderId = folderId ?: "ece",
            subjectTag = spec.category,
            initialBlocks = spec.blocks
        )
    }

    suspend fun createFolder(name: String, parentFolderId: String? = null) {
        ensureSeeded()
        val clean = name.trim()
        if (clean.isEmpty()) return
        val id = clean.lowercase().replace(Regex("[^a-z0-9]+"), "_").take(24) +
            "_${UUID.randomUUID().toString().take(4)}"
        dao.upsertFolder(
            FolderEntity(
                id = id,
                name = clean,
                parentFolderId = parentFolderId,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun createTag(name: String, colorHex: String = "#003DA5") {
        ensureSeeded()
        val clean = name.trim().removePrefix("#")
        if (clean.isEmpty()) return
        val id = "tag_${clean.lowercase()}"
        dao.upsertTags(listOf(TagEntity(id = id, name = clean, colorHex = colorHex)))
    }

    suspend fun toggleTagOnNote(noteId: String, tagId: String, currentlyAttached: Boolean) {
        ensureSeeded()
        if (currentlyAttached) {
            dao.removeNoteTagCrossRef(noteId, tagId)
        } else {
            dao.upsertNoteTagCrossRef(NoteTagCrossRef(noteId = noteId, tagId = tagId))
        }
    }

    suspend fun updateNoteMetadata(transform: (NoteEntity) -> NoteEntity, noteId: String) {
        ensureSeeded()
        val current = dao.getNoteById(noteId) ?: return
        val updated = transform(current).copy(updatedAt = System.currentTimeMillis())
        dao.upsertNote(updated)
    }

    suspend fun deleteNotePermanently(noteId: String) {
        ensureSeeded()
        dao.deleteNotePermanently(noteId)
    }

    suspend fun saveNoteAndBlocks(
        note: NoteEntity,
        blocks: List<NoteBlockEntity>,
        recordRevision: Boolean = false
    ) {
        ensureSeeded()
        val now = System.currentTimeMillis()
        val normalizedBlocks = blocks.mapIndexed { idx, blk ->
            blk.copy(position = idx, updatedAt = now)
        }
        val updatedNote = note.copy(updatedAt = now)
        database.withTransaction {
            dao.upsertNote(updatedNote)
            dao.replaceBlocksForNote(updatedNote.id, normalizedBlocks, now)
            refreshLinksForNote(updatedNote.id, normalizedBlocks)
            if (recordRevision) {
                saveRevisionSnapshot(updatedNote, normalizedBlocks, now)
            }
        }
    }

    suspend fun addAttachment(attachment: NoteAttachmentEntity) {
        ensureSeeded()
        dao.upsertAttachment(attachment)
    }

    suspend fun importNote(note: NoteEntity, blocks: List<NoteBlockEntity>): NoteEntity {
        ensureSeeded()
        val now = System.currentTimeMillis()
        database.withTransaction {
            dao.upsertNote(note.copy(updatedAt = now))
            dao.replaceBlocksForNote(note.id, blocks, now)
            refreshLinksForNote(note.id, blocks)
            saveRevisionSnapshot(note, blocks, now)
        }
        return note
    }

    private suspend fun refreshLinksForNote(sourceNoteId: String, blocks: List<NoteBlockEntity>) {
        dao.deleteOutgoingLinksForNote(sourceNoteId)
        val now = System.currentTimeMillis()
        val linkRegex = Regex("\\[\\[([^|\\]]+)(?:\\|([^\\]]+))?\\]\\]")
        val links = mutableListOf<NoteLinkEntity>()
        for (b in blocks) {
            for (match in linkRegex.findAll(b.content)) {
                val targetId = match.groupValues[1].trim()
                val anchor = match.groupValues[2].trim().ifEmpty { targetId }
                links += NoteLinkEntity(
                    id = "lnk_${UUID.randomUUID().toString().take(8)}",
                    sourceNoteId = sourceNoteId,
                    targetNoteId = targetId,
                    anchorText = anchor,
                    createdAt = now
                )
            }
        }
        if (links.isNotEmpty()) {
            dao.upsertLinks(links)
        }
    }

    private suspend fun saveRevisionSnapshot(
        note: NoteEntity,
        blocks: List<NoteBlockEntity>,
        now: Long
    ) {
        val jsonSnapshot = NotesExportImportManager.exportToJson(note, blocks)
        dao.insertRevision(
            NoteRevisionEntity(
                id = "rev_${UUID.randomUUID().toString().take(8)}",
                noteId = note.id,
                titleSnapshot = note.title,
                blocksJsonSnapshot = jsonSnapshot,
                createdAt = now
            )
        )
    }

    private suspend fun seedInitialWorkspace() {
        val now = System.currentTimeMillis()
        database.withTransaction {
            dao.upsertFolders(
                listOf(
                    FolderEntity("ece", "ECE & Digital Systems", null, "memory", now),
                    FolderEntity("eee", "EEE & Power Circuits", null, "bolt", now),
                    FolderEntity("cse", "CSE & Algorithms", null, "code", now),
                    FolderEntity("math", "Mathematics & Proofs", null, "functions", now),
                    FolderEntity("exam_prep", "GATE & Competitive Prep", null, "school", now)
                )
            )

            dao.upsertTags(
                listOf(
                    TagEntity("tag_digital_logic", "digital-logic", "#003DA5"),
                    TagEntity("tag_latex", "latex-formulas", "#1647BB"),
                    TagEntity("tag_gate", "gate-revision", "#0D7A3E"),
                    TagEntity("tag_circuits", "circuits", "#B45309")
                )
            )

            val templates = NoteTemplateCatalog.allTemplates.map { tpl ->
                val arr = JSONArray()
                tpl.blocks.forEachIndexed { idx, (type, content) ->
                    val o = JSONObject()
                    o.put("type", type.name)
                    o.put("position", idx)
                    o.put("content", content)
                    arr.put(o)
                }
                NoteTemplateEntity(
                    id = tpl.id,
                    category = tpl.category,
                    title = tpl.title,
                    subtitle = tpl.subtitle,
                    defaultBlocksJson = arr.toString()
                )
            }
            dao.upsertTemplates(templates)

            val noteDigital = NoteEntity(
                id = "note_001",
                title = "Digital Electronics & Logic Design",
                folderId = "ece",
                collectionName = "Core ECE",
                subjectTag = "ECE",
                isFavourite = true,
                isPinned = true,
                createdAt = now - 60_000L,
                updatedAt = now
            )
            val noteBoolean = NoteEntity(
                id = "note_boolean_algebra",
                title = "Boolean Algebra & K-Map Simplification",
                folderId = "ece",
                collectionName = "Core ECE",
                subjectTag = "ECE",
                isFavourite = true,
                isPinned = false,
                createdAt = now - 120_000L,
                updatedAt = now - 30_000L
            )
            val noteControl = NoteEntity(
                id = "note_control_systems",
                title = "Control Systems & Transfer Functions",
                folderId = "eee",
                collectionName = "Core EEE",
                subjectTag = "EEE",
                isFavourite = false,
                isPinned = false,
                createdAt = now - 180_000L,
                updatedAt = now - 60_000L
            )

            dao.upsertNotes(listOf(noteDigital, noteBoolean, noteControl))
            dao.upsertNoteTagCrossRef(NoteTagCrossRef("note_001", "tag_digital_logic"))
            dao.upsertNoteTagCrossRef(NoteTagCrossRef("note_001", "tag_gate"))
            dao.upsertNoteTagCrossRef(NoteTagCrossRef("note_boolean_algebra", "tag_digital_logic"))
            dao.upsertNoteTagCrossRef(NoteTagCrossRef("note_control_systems", "tag_latex"))

            val digitalBlocks = listOf(
                NoteBlockEntity("block_001", "note_001", BlockType.HEADING_1.name, 0, "Boolean Algebra & Logic Gates", null, now, now),
                NoteBlockEntity("block_002", "note_001", BlockType.PARAGRAPH.name, 1, "Boolean algebra is used in digital logic design, combinational circuits, and VLSI synthesis.", null, now, now),
                NoteBlockEntity("block_003", "note_001", BlockType.LATEX_BLOCK.name, 2, "F = \\overline{A \\cdot B} = \\overline{A} + \\overline{B}", "De Morgan's Law", now, now),
                NoteBlockEntity("block_004", "note_001", BlockType.TABLE.name, 3, TableData.defaultComponentTable().toJson(), null, now, now),
                NoteBlockEntity("block_005", "note_001", BlockType.CODE_BLOCK.name, 4, "module nand_gate(input a, input b, output y);\n  assign y = ~(a & b);\nendmodule", "VERILOG", now, now),
                NoteBlockEntity("block_006", "note_001", BlockType.BOOKMARK.name, 5, "[[note_boolean_algebra|Boolean Algebra & K-Map Simplification]]", null, now, now),
                NoteBlockEntity("block_007", "note_001", BlockType.MERMAID_DIAGRAM.name, 6, "graph LR\n  Inputs[A, B] --> NAND[NAND Gate]\n  NAND --> Output[Y = ~(A.B)]", null, now, now)
            )
            val booleanBlocks = listOf(
                NoteBlockEntity("block_101", "note_boolean_algebra", BlockType.HEADING_1.name, 0, "De Morgan's Theorems & Consensus Law", null, now, now),
                NoteBlockEntity("block_102", "note_boolean_algebra", BlockType.LATEX_BLOCK.name, 1, "A \\cdot B + \\overline{A} \\cdot C + B \\cdot C = A \\cdot B + \\overline{A} \\cdot C", null, now, now),
                NoteBlockEntity("block_103", "note_boolean_algebra", BlockType.BOOKMARK.name, 2, "[[note_001|Digital Electronics & Logic Design]]", null, now, now)
            )
            val controlBlocks = listOf(
                NoteBlockEntity("block_201", "note_control_systems", BlockType.HEADING_1.name, 0, "Closed-Loop Transfer Function", null, now, now),
                NoteBlockEntity("block_202", "note_control_systems", BlockType.LATEX_BLOCK.name, 1, "H(s) = \\frac{Y(s)}{X(s)} = \\frac{G(s)}{1 + G(s)H(s)}", null, now, now),
                NoteBlockEntity("block_203", "note_control_systems", BlockType.ENGINEERING_SCHEMATIC.name, 2, "RC_LOW_PASS|R = 10 kΩ, C = 100 nF, f_c = 159.15 Hz", null, now, now)
            )

            dao.upsertBlocks(digitalBlocks + booleanBlocks + controlBlocks)
            refreshLinksForNote("note_001", digitalBlocks)
            refreshLinksForNote("note_boolean_algebra", booleanBlocks)
            refreshLinksForNote("note_control_systems", controlBlocks)
            saveRevisionSnapshot(noteDigital, digitalBlocks, now)
        }
    }
}
