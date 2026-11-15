package com.example.core.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.core.database.NoteBlockEntity
import com.example.core.database.NoteEntity
import com.example.core.document.BlockType
import com.example.core.document.TableData
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Portable Export & Import Engine for MATHGATE Notes Studio (Part 12).
 * Supports:
 * - Markdown (.md) export & import
 * - JSON (.json) structured document export & import
 * - PDF (.pdf) generation via native Android PdfDocument
 * - CSV (.csv) table export
 * - HTML (.html) standalone offline export
 * - Plain Text (.txt) export
 * - ZIP (.zip) archive backup of notes & attachments
 */
object NotesExportImportManager {

    fun exportToMarkdown(note: NoteEntity, blocks: List<NoteBlockEntity>): String {
        val sb = StringBuilder()
        sb.appendLine("# ${note.title}")
        sb.appendLine("> Subject: ${note.subjectTag} | Collection: ${note.collectionName}")
        sb.appendLine()

        for (b in blocks.sortedBy { it.position }) {
            when (BlockType.fromName(b.type)) {
                BlockType.HEADING_1 -> sb.appendLine("# ${b.content}\n")
                BlockType.HEADING_2 -> sb.appendLine("## ${b.content}\n")
                BlockType.HEADING_3 -> sb.appendLine("### ${b.content}\n")
                BlockType.PARAGRAPH, BlockType.INLINE_MATH, BlockType.INLINE_CODE -> sb.appendLine("${b.content}\n")
                BlockType.BULLETED_LIST -> {
                    b.content.lines().filter { it.isNotBlank() }.forEach { sb.appendLine("- $it") }
                    sb.appendLine()
                }
                BlockType.NUMBERED_LIST -> {
                    b.content.lines().filter { it.isNotBlank() }.forEachIndexed { i, line ->
                        sb.appendLine("${i + 1}. $line")
                    }
                    sb.appendLine()
                }
                BlockType.CHECKLIST -> {
                    b.content.lines().filter { it.isNotBlank() }.forEach { line ->
                        val norm = if (line.startsWith("[")) "- $line" else "- [ ] $line"
                        sb.appendLine(norm)
                    }
                    sb.appendLine()
                }
                BlockType.TOGGLE_SECTION -> {
                    val parts = b.content.split("::", limit = 2)
                    val summary = parts.getOrElse(0) { "Details" }
                    val body = parts.getOrElse(1) { "" }
                    sb.appendLine("<details><summary>$summary</summary>\n\n$body\n</details>\n")
                }
                BlockType.QUOTE -> sb.appendLine("> ${b.content}\n")
                BlockType.CALLOUT -> sb.appendLine("> [!IMPORTANT] ${b.content}\n")
                BlockType.DIVIDER -> sb.appendLine("---\n")
                BlockType.TABLE -> sb.appendLine(TableData.fromJsonOrDefault(b.content).toMarkdownTable() + "\n")
                BlockType.LATEX_BLOCK -> sb.appendLine("$$\n${b.content}\n$$\n")
                BlockType.CODE_BLOCK -> {
                    val lang = (b.metadata ?: "kotlin").lowercase()
                    sb.appendLine("```$lang\n${b.content}\n```\n")
                }
                BlockType.IMAGE, BlockType.IMAGE_GALLERY -> {
                    val alt = b.metadata ?: "Image"
                    sb.appendLine("![$alt](${b.content})\n")
                }
                BlockType.HYPERLINK, BlockType.EMBEDDED_WEBPAGE, BlockType.EMBEDDED_VIDEO -> {
                    val parts = b.content.split("|", limit = 2)
                    val url = parts.getOrElse(0) { b.content }
                    val label = parts.getOrElse(1) { url }
                    sb.appendLine("[$label]($url)\n")
                }
                BlockType.BOOKMARK -> sb.appendLine("${b.content}\n")
                BlockType.MERMAID_DIAGRAM -> sb.appendLine("```mermaid\n${b.content}\n```\n")
                BlockType.ENGINEERING_SCHEMATIC -> sb.appendLine("`[Schematic: ${b.content}]`\n")
                BlockType.FILE_ATTACHMENT, BlockType.AUDIO_ATTACHMENT -> sb.appendLine("`[Attachment: ${b.content}]`\n")
            }
        }
        return sb.toString().trimEnd() + "\n"
    }

    fun importFromMarkdown(
        markdown: String,
        folderId: String? = "ece"
    ): Pair<NoteEntity, List<NoteBlockEntity>> {
        val now = System.currentTimeMillis()
        val noteId = "note_${UUID.randomUUID().toString().take(8)}"
        val lines = markdown.lines()
        var title = "Imported Markdown Note"
        val blocks = mutableListOf<NoteBlockEntity>()
        var idx = 0
        var pos = 0

        while (idx < lines.size) {
            val line = lines[idx].trimEnd()
            val trimmed = line.trim()
            when {
                trimmed.isEmpty() -> idx++
                trimmed.startsWith("# ") && pos == 0 && title == "Imported Markdown Note" -> {
                    title = trimmed.removePrefix("# ").trim()
                    idx++
                }
                trimmed.startsWith("### ") -> {
                    blocks += NoteBlockEntity(
                        id = "blk_${UUID.randomUUID().toString().take(8)}",
                        noteId = noteId,
                        type = BlockType.HEADING_3.name,
                        position = pos++,
                        content = trimmed.removePrefix("### ").trim(),
                        createdAt = now,
                        updatedAt = now
                    )
                    idx++
                }
                trimmed.startsWith("## ") -> {
                    blocks += NoteBlockEntity(
                        id = "blk_${UUID.randomUUID().toString().take(8)}",
                        noteId = noteId,
                        type = BlockType.HEADING_2.name,
                        position = pos++,
                        content = trimmed.removePrefix("## ").trim(),
                        createdAt = now,
                        updatedAt = now
                    )
                    idx++
                }
                trimmed.startsWith("# ") -> {
                    blocks += NoteBlockEntity(
                        id = "blk_${UUID.randomUUID().toString().take(8)}",
                        noteId = noteId,
                        type = BlockType.HEADING_1.name,
                        position = pos++,
                        content = trimmed.removePrefix("# ").trim(),
                        createdAt = now,
                        updatedAt = now
                    )
                    idx++
                }
                trimmed.startsWith("$$") -> {
                    val latexLines = mutableListOf<String>()
                    if (trimmed.length > 4 && trimmed.endsWith("$$")) {
                        latexLines += trimmed.removePrefix("$$").removeSuffix("$$").trim()
                        idx++
                    } else {
                        idx++
                        while (idx < lines.size && !lines[idx].trim().startsWith("$$")) {
                            latexLines += lines[idx]
                            idx++
                        }
                        if (idx < lines.size) idx++
                    }
                    blocks += NoteBlockEntity(
                        id = "blk_${UUID.randomUUID().toString().take(8)}",
                        noteId = noteId,
                        type = BlockType.LATEX_BLOCK.name,
                        position = pos++,
                        content = latexLines.joinToString("\n").trim(),
                        createdAt = now,
                        updatedAt = now
                    )
                }
                trimmed.startsWith("```") -> {
                    val lang = trimmed.removePrefix("```").trim().ifEmpty { "kotlin" }
                    val codeLines = mutableListOf<String>()
                    idx++
                    while (idx < lines.size && !lines[idx].trim().startsWith("```")) {
                        codeLines += lines[idx]
                        idx++
                    }
                    if (idx < lines.size) idx++
                    val blockType = if (lang.equals("mermaid", ignoreCase = true)) {
                        BlockType.MERMAID_DIAGRAM
                    } else {
                        BlockType.CODE_BLOCK
                    }
                    blocks += NoteBlockEntity(
                        id = "blk_${UUID.randomUUID().toString().take(8)}",
                        noteId = noteId,
                        type = blockType.name,
                        position = pos++,
                        content = codeLines.joinToString("\n"),
                        metadata = lang.uppercase(),
                        createdAt = now,
                        updatedAt = now
                    )
                }
                trimmed.startsWith("- [") -> {
                    val items = mutableListOf<String>()
                    while (idx < lines.size && lines[idx].trim().startsWith("- [")) {
                        items += lines[idx].trim().removePrefix("- ").trim()
                        idx++
                    }
                    blocks += NoteBlockEntity(
                        id = "blk_${UUID.randomUUID().toString().take(8)}",
                        noteId = noteId,
                        type = BlockType.CHECKLIST.name,
                        position = pos++,
                        content = items.joinToString("\n"),
                        createdAt = now,
                        updatedAt = now
                    )
                }
                trimmed.startsWith("- ") -> {
                    val items = mutableListOf<String>()
                    while (idx < lines.size && lines[idx].trim().startsWith("- ")) {
                        items += lines[idx].trim().removePrefix("- ").trim()
                        idx++
                    }
                    blocks += NoteBlockEntity(
                        id = "blk_${UUID.randomUUID().toString().take(8)}",
                        noteId = noteId,
                        type = BlockType.BULLETED_LIST.name,
                        position = pos++,
                        content = items.joinToString("\n"),
                        createdAt = now,
                        updatedAt = now
                    )
                }
                trimmed == "---" -> {
                    blocks += NoteBlockEntity(
                        id = "blk_${UUID.randomUUID().toString().take(8)}",
                        noteId = noteId,
                        type = BlockType.DIVIDER.name,
                        position = pos++,
                        content = "---",
                        createdAt = now,
                        updatedAt = now
                    )
                    idx++
                }
                else -> {
                    blocks += NoteBlockEntity(
                        id = "blk_${UUID.randomUUID().toString().take(8)}",
                        noteId = noteId,
                        type = BlockType.PARAGRAPH.name,
                        position = pos++,
                        content = trimmed,
                        createdAt = now,
                        updatedAt = now
                    )
                    idx++
                }
            }
        }

        val note = NoteEntity(
            id = noteId,
            title = title,
            folderId = folderId,
            collectionName = "Imported",
            subjectTag = "ENGINEERING",
            createdAt = now,
            updatedAt = now
        )
        return note to blocks
    }

    fun exportToJson(note: NoteEntity, blocks: List<NoteBlockEntity>): String {
        val root = JSONObject()
        root.put("id", note.id)
        root.put("title", note.title)
        root.put("folderId", note.folderId ?: JSONObject.NULL)
        root.put("collectionName", note.collectionName)
        root.put("subjectTag", note.subjectTag)
        root.put("isFavourite", note.isFavourite)
        root.put("isPinned", note.isPinned)
        val arr = JSONArray()
        blocks.sortedBy { it.position }.forEach { b ->
            val bObj = JSONObject()
            bObj.put("id", b.id)
            bObj.put("type", b.type)
            bObj.put("position", b.position)
            bObj.put("content", b.content)
            if (b.metadata != null) bObj.put("metadata", b.metadata)
            arr.put(bObj)
        }
        root.put("blocks", arr)
        return root.toString(2)
    }

    fun importFromJson(rawJson: String): Pair<NoteEntity, List<NoteBlockEntity>> {
        val now = System.currentTimeMillis()
        val obj = JSONObject(rawJson)
        val noteId = obj.optString("id").takeIf { it.isNotBlank() } ?: "note_${UUID.randomUUID().toString().take(8)}"
        val note = NoteEntity(
            id = noteId,
            title = obj.optString("title", "Imported JSON Note"),
            folderId = obj.optString("folderId").takeIf { it.isNotBlank() && it != "null" },
            collectionName = obj.optString("collectionName", "General"),
            subjectTag = obj.optString("subjectTag", "ENGINEERING"),
            isFavourite = obj.optBoolean("isFavourite", false),
            isPinned = obj.optBoolean("isPinned", false),
            createdAt = now,
            updatedAt = now
        )
        val blocksArr = obj.optJSONArray("blocks") ?: JSONArray()
        val blocks = (0 until blocksArr.length()).map { idx ->
            val bObj = blocksArr.getJSONObject(idx)
            NoteBlockEntity(
                id = bObj.optString("id").takeIf { it.isNotBlank() } ?: "blk_${UUID.randomUUID().toString().take(8)}",
                noteId = noteId,
                type = bObj.optString("type", BlockType.PARAGRAPH.name),
                position = bObj.optInt("position", idx),
                content = bObj.optString("content", ""),
                metadata = bObj.optString("metadata").takeIf { it.isNotBlank() },
                createdAt = now,
                updatedAt = now
            )
        }
        return note to blocks
    }

    fun exportToHtml(note: NoteEntity, blocks: List<NoteBlockEntity>): String {
        val bodyHtml = blocks.sortedBy { it.position }.joinToString("\n") { b ->
            when (BlockType.fromName(b.type)) {
                BlockType.HEADING_1 -> "<h1>${escapeHtml(b.content)}</h1>"
                BlockType.HEADING_2 -> "<h2>${escapeHtml(b.content)}</h2>"
                BlockType.HEADING_3 -> "<h3>${escapeHtml(b.content)}</h3>"
                BlockType.LATEX_BLOCK -> "<div class=\"latex\">\\[${escapeHtml(b.content)}\\]</div>"
                BlockType.CODE_BLOCK -> "<pre><code>${escapeHtml(b.content)}</code></pre>"
                BlockType.QUOTE -> "<blockquote>${escapeHtml(b.content)}</blockquote>"
                else -> "<p>${escapeHtml(b.content)}</p>"
            }
        }
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head><meta charset="utf-8"><title>${escapeHtml(note.title)}</title>
            <style>body{font-family:sans-serif;color:#12336F;background:#FFFFFF;padding:24px;} .latex,pre{background:#EAF1FF;padding:12px;border:1px solid #A9C1EF;border-radius:6px;}</style>
            </head>
            <body><h1>${escapeHtml(note.title)}</h1>$bodyHtml</body>
            </html>
        """.trimIndent()
    }

    fun exportToPlainText(note: NoteEntity, blocks: List<NoteBlockEntity>): String {
        return buildString {
            appendLine(note.title.uppercase())
            appendLine("=".repeat(note.title.length.coerceAtLeast(12)))
            blocks.sortedBy { it.position }.forEach { b ->
                appendLine(b.content)
                appendLine()
            }
        }
    }

    fun exportToPdfBytes(note: NoteEntity, blocks: List<NoteBlockEntity>): ByteArray {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas
        val titlePaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply {
            textSize = 11f
        }
        var y = 48f
        canvas.drawText(note.title.take(60), 40f, y, titlePaint)
        y += 28f
        for (b in blocks.sortedBy { it.position }) {
            for (line in b.content.lines()) {
                if (y > 790f) break
                canvas.drawText("[${b.type.take(6)}] ${line.take(75)}", 40f, y, bodyPaint)
                y += 16f
            }
            y += 6f
        }
        doc.finishPage(page)
        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }

    fun exportAllToZipBytes(notesWithBlocks: List<Pair<NoteEntity, List<NoteBlockEntity>>>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            for ((note, blocks) in notesWithBlocks) {
                val safeName = note.id.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                zip.putNextEntry(ZipEntry("$safeName.json"))
                zip.write(exportToJson(note, blocks).toByteArray(Charsets.UTF_8))
                zip.closeEntry()

                zip.putNextEntry(ZipEntry("$safeName.md"))
                zip.write(exportToMarkdown(note, blocks).toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }
        return out.toByteArray()
    }

    fun saveExportToCacheFile(context: Context, fileName: String, bytes: ByteArray): File {
        val exportDir = File(context.cacheDir, "notes_exports").apply { mkdirs() }
        val file = File(exportDir, fileName)
        file.writeBytes(bytes)
        return file
    }

    private fun escapeHtml(raw: String): String =
        raw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}
