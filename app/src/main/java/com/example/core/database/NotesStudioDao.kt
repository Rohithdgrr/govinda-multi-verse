package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesStudioDao {

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun countNotes(): Int

    @Query("SELECT COUNT(*) FROM note_blocks")
    suspend fun countBlocks(): Int

    @Query("SELECT COUNT(*) FROM note_revisions")
    suspend fun countRevisions(): Int

    @Query("SELECT * FROM notes ORDER BY is_pinned DESC, updated_at DESC")
    fun observeAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY is_pinned DESC, updated_at DESC")
    suspend fun getAllNotes(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    suspend fun getNoteById(noteId: String): NoteEntity?

    @Query("SELECT * FROM note_blocks WHERE note_id = :noteId ORDER BY position ASC")
    fun observeBlocksForNote(noteId: String): Flow<List<NoteBlockEntity>>

    @Query("SELECT * FROM note_blocks WHERE note_id = :noteId ORDER BY position ASC")
    suspend fun getBlocksForNote(noteId: String): List<NoteBlockEntity>

    @Query("SELECT * FROM note_blocks ORDER BY note_id ASC, position ASC")
    suspend fun getAllBlocks(): List<NoteBlockEntity>

    @Query("SELECT * FROM note_folders ORDER BY name ASC")
    fun observeFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM note_folders ORDER BY name ASC")
    suspend fun getFolders(): List<FolderEntity>

    @Query("SELECT * FROM note_tags ORDER BY name ASC")
    fun observeTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM note_tag_cross_ref")
    fun observeNoteTagCrossRefs(): Flow<List<NoteTagCrossRef>>

    @Query("SELECT * FROM note_templates ORDER BY category ASC, title ASC")
    fun observeTemplates(): Flow<List<NoteTemplateEntity>>

    @Query("SELECT * FROM note_links WHERE target_note_id = :targetNoteId ORDER BY created_at DESC")
    fun observeBacklinksForNote(targetNoteId: String): Flow<List<NoteLinkEntity>>

    @Query("SELECT * FROM note_links WHERE source_note_id = :sourceNoteId ORDER BY created_at DESC")
    fun observeOutgoingLinksForNote(sourceNoteId: String): Flow<List<NoteLinkEntity>>

    @Query("SELECT * FROM note_revisions WHERE note_id = :noteId ORDER BY created_at DESC LIMIT 25")
    fun observeRevisionsForNote(noteId: String): Flow<List<NoteRevisionEntity>>

    @Query("SELECT * FROM note_attachments WHERE note_id = :noteId ORDER BY created_at ASC")
    fun observeAttachmentsForNote(noteId: String): Flow<List<NoteAttachmentEntity>>

    @Query(
        """
        SELECT DISTINCT n.* FROM notes n
        LEFT JOIN note_blocks b ON b.note_id = n.id
        LEFT JOIN note_folders f ON f.id = n.folder_id
        WHERE n.is_trashed = 0
          AND (
            LOWER(n.title) LIKE '%' || LOWER(:query) || '%'
            OR LOWER(b.content) LIKE '%' || LOWER(:query) || '%'
            OR LOWER(COALESCE(f.name, '')) LIKE '%' || LOWER(:query) || '%'
            OR LOWER(n.subject_tag) LIKE '%' || LOWER(:query) || '%'
          )
        ORDER BY n.is_pinned DESC, n.updated_at DESC
        """
    )
    suspend fun searchNotesFullText(query: String): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotes(notes: List<NoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBlock(block: NoteBlockEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBlocks(blocks: List<NoteBlockEntity>)

    @Query("DELETE FROM note_blocks WHERE note_id = :noteId")
    suspend fun deleteBlocksForNote(noteId: String)

    @Query("DELETE FROM note_blocks WHERE id = :blockId")
    suspend fun deleteBlockById(blockId: String)

    @Transaction
    suspend fun replaceBlocksForNote(noteId: String, blocks: List<NoteBlockEntity>, updatedAt: Long) {
        deleteBlocksForNote(noteId)
        upsertBlocks(blocks)
        updateNoteTimestamp(noteId, updatedAt)
    }

    @Query("UPDATE notes SET updated_at = :updatedAt WHERE id = :noteId")
    suspend fun updateNoteTimestamp(noteId: String, updatedAt: Long)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNotePermanently(noteId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFolder(folder: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFolders(folders: List<FolderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTags(tags: List<TagEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNoteTagCrossRef(ref: NoteTagCrossRef)

    @Query("DELETE FROM note_tag_cross_ref WHERE note_id = :noteId AND tag_id = :tagId")
    suspend fun removeNoteTagCrossRef(noteId: String, tagId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAttachment(attachment: NoteAttachmentEntity)

    @Query("DELETE FROM note_attachments WHERE id = :attachmentId")
    suspend fun deleteAttachmentById(attachmentId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLinks(links: List<NoteLinkEntity>)

    @Query("DELETE FROM note_links WHERE source_note_id = :sourceNoteId")
    suspend fun deleteOutgoingLinksForNote(sourceNoteId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevision(revision: NoteRevisionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTemplates(templates: List<NoteTemplateEntity>)
}
