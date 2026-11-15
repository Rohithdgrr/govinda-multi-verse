package com.example.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["folder_id"]),
        Index(value = ["subject_tag"]),
        Index(value = ["is_favourite"]),
        Index(value = ["is_pinned"]),
        Index(value = ["is_archived"]),
        Index(value = ["is_trashed"]),
        Index(value = ["updated_at"])
    ]
)
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "folder_id") val folderId: String?,
    @ColumnInfo(name = "collection_name") val collectionName: String = "General",
    @ColumnInfo(name = "subject_tag") val subjectTag: String = "MATHEMATICS",
    @ColumnInfo(name = "is_favourite") val isFavourite: Boolean = false,
    @ColumnInfo(name = "is_pinned") val isPinned: Boolean = false,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean = false,
    @ColumnInfo(name = "is_trashed") val isTrashed: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)

@Entity(
    tableName = "note_blocks",
    indices = [
        Index(value = ["note_id", "position"]),
        Index(value = ["type"])
    ]
)
data class NoteBlockEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "note_id") val noteId: String,
    val type: String,
    val position: Int,
    val content: String,
    val metadata: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)

@Entity(
    tableName = "note_folders",
    indices = [
        Index(value = ["parent_folder_id"])
    ]
)
data class FolderEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "parent_folder_id") val parentFolderId: String? = null,
    @ColumnInfo(name = "icon_name") val iconName: String = "folder",
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(tableName = "note_tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "color_hex") val colorHex: String = "#003DA5"
)

@Entity(
    tableName = "note_tag_cross_ref",
    primaryKeys = ["note_id", "tag_id"],
    indices = [
        Index(value = ["note_id"]),
        Index(value = ["tag_id"])
    ]
)
data class NoteTagCrossRef(
    @ColumnInfo(name = "note_id") val noteId: String,
    @ColumnInfo(name = "tag_id") val tagId: String
)

@Entity(
    tableName = "note_attachments",
    indices = [
        Index(value = ["note_id"]),
        Index(value = ["block_id"])
    ]
)
data class NoteAttachmentEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "note_id") val noteId: String,
    @ColumnInfo(name = "block_id") val blockId: String,
    @ColumnInfo(name = "uri_string") val uriString: String,
    @ColumnInfo(name = "mime_type") val mimeType: String,
    val caption: String = "",
    @ColumnInfo(name = "alt_text") val altText: String = "",
    val alignment: String = "CENTER",
    @ColumnInfo(name = "width_fraction") val widthFraction: Float = 1.0f,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(
    tableName = "note_links",
    indices = [
        Index(value = ["source_note_id"]),
        Index(value = ["target_note_id"])
    ]
)
data class NoteLinkEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "source_note_id") val sourceNoteId: String,
    @ColumnInfo(name = "target_note_id") val targetNoteId: String,
    @ColumnInfo(name = "anchor_text") val anchorText: String,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(
    tableName = "note_revisions",
    indices = [
        Index(value = ["note_id", "created_at"])
    ]
)
data class NoteRevisionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "note_id") val noteId: String,
    @ColumnInfo(name = "title_snapshot") val titleSnapshot: String,
    @ColumnInfo(name = "blocks_json_snapshot") val blocksJsonSnapshot: String,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(tableName = "note_templates")
data class NoteTemplateEntity(
    @PrimaryKey val id: String,
    val category: String,
    val title: String,
    val subtitle: String,
    @ColumnInfo(name = "default_blocks_json") val defaultBlocksJson: String
)
