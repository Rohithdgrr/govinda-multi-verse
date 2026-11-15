package com.example.feature.notes.blocks

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.NoteBlockEntity
import com.example.core.designsystem.BlueprintBadge
import com.example.core.designsystem.BlueprintCard
import com.example.core.document.BlockType
import com.example.core.document.CodeLanguage
import com.example.core.document.TableData
import com.example.core.latex.LatexRendererEngine
import com.example.personalization.theme.LocalGovindaTokens

@Composable
fun NoteBlockItemCard(
    block: NoteBlockEntity,
    equationIndex: Int?,
    isReadingMode: Boolean,
    onUpdateContent: (String, String?) -> Unit,
    onConvertType: (BlockType) -> Unit,
    onDuplicate: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    onAttachImage: (String, String) -> Unit,
    onNavigateToLinkedNote: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSectionCollapsed: Boolean = false,
    hiddenChildCount: Int = 0,
    onToggleSectionCollapse: () -> Unit = {},
    onInsertBlockBelow: (BlockType) -> Unit = {}
) {
    val tokens = LocalGovindaTokens.current
    val clipboard = LocalClipboardManager.current
    val blockType = BlockType.fromName(block.type)
    var isEditingBlock by remember(block.id, isReadingMode) { mutableStateOf(false) }
    var showTypeConverter by remember(block.id) { mutableStateOf(false) }
    var showInsertBelowBar by remember(block.id) { mutableStateOf(false) }
    var localHeadingCollapsed by remember(block.id) { mutableStateOf(false) }
    val effectiveCollapsed = isSectionCollapsed || localHeadingCollapsed

    val notionInk = Color(0xFF191919)
    val notionBodyInk = Color(0xFF2F2F2F)
    val notionMuted = Color(0xFF787774)

    // Optional Notion Block Accent Tint (stored in metadata for Callout/Paragraph/Quote/Heading)
    val accentBgColor = when (block.metadata?.uppercase()) {
        "BLUE" -> Color(0xFFEAF2FF)
        "AMBER" -> Color(0xFFFFF8E6)
        "EMERALD" -> Color(0xFFEAFBF0)
        "ROSE" -> Color(0xFFFFEFEF)
        "PURPLE" -> Color(0xFFF3EEFF)
        else -> if (isEditingBlock && !isReadingMode) Color(0xFFF7F7F5) else Color.Transparent
    }
    val accentBorderColor = when (block.metadata?.uppercase()) {
        "BLUE" -> Color(0xFF93C5FD)
        "AMBER" -> Color(0xFFFCD34D)
        "EMERALD" -> Color(0xFF86EFAC)
        "ROSE" -> Color(0xFFFCA5A5)
        "PURPLE" -> Color(0xFFC4B5FD)
        else -> Color(0xFFEBEBEA)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(accentBgColor)
            .clickable(enabled = !isReadingMode) { isEditingBlock = !isEditingBlock }
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .testTag("note_block_${block.id}"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        when (blockType) {
            BlockType.HEADING_1 -> {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 2.dp)
                ) {
                    Text(
                        text = if (effectiveCollapsed) "▸" else "▾",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = notionInk,
                        modifier = Modifier
                            .clickable {
                                localHeadingCollapsed = !localHeadingCollapsed
                                onToggleSectionCollapse()
                            }
                            .padding(top = 3.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = block.content,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 23.sp,
                                lineHeight = 30.sp
                            ),
                            color = notionInk
                        )
                        if (effectiveCollapsed && hiddenChildCount > 0) {
                            Surface(
                                onClick = {
                                    localHeadingCollapsed = false
                                    onToggleSectionCollapse()
                                },
                                color = Color(0xFFEFEFEF),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "$hiddenChildCount block(s) folded · Tap to expand section",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = notionMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
            BlockType.HEADING_2 -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Text(
                        text = if (effectiveCollapsed) "▸" else "▾",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = notionInk,
                        modifier = Modifier.clickable {
                            localHeadingCollapsed = !localHeadingCollapsed
                            onToggleSectionCollapse()
                        }
                    )
                    Text(
                        text = block.content,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = notionInk,
                        modifier = Modifier.weight(1f)
                    )
                    if (effectiveCollapsed && hiddenChildCount > 0) {
                        Text(
                            text = "+$hiddenChildCount hidden",
                            style = MaterialTheme.typography.labelSmall,
                            color = notionMuted
                        )
                    }
                }
            }
            BlockType.HEADING_3 -> Text(
                text = block.content,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = notionInk,
                modifier = Modifier.padding(top = 4.dp)
            )
            BlockType.PARAGRAPH, BlockType.INLINE_MATH, BlockType.INLINE_CODE -> {
                val formatted = LatexRendererEngine.formatInlineSymbols(block.content)
                val colonIndex = formatted.indexOf(':')
                if (colonIndex in 1..32) {
                    val prefix = formatted.substring(0, colonIndex + 1)
                    val rest = formatted.substring(colonIndex + 1)
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = notionInk)) {
                                append(prefix)
                            }
                            withStyle(SpanStyle(color = notionBodyInk)) {
                                append(rest)
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.5.sp,
                            lineHeight = 23.sp
                        )
                    )
                } else {
                    Text(
                        text = formatted,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.5.sp,
                            lineHeight = 23.sp
                        ),
                        color = notionBodyInk
                    )
                }
            }
            BlockType.BULLETED_LIST -> Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
            ) {
                block.content.lines().filter { it.isNotBlank() }.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "•",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = notionInk
                        )
                        Text(
                            text = item.removePrefix("•").removePrefix("-").trim(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 15.5.sp,
                                lineHeight = 22.sp
                            ),
                            color = notionBodyInk
                        )
                    }
                }
            }
            BlockType.NUMBERED_LIST -> Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
            ) {
                block.content.lines().filter { it.isNotBlank() }.forEachIndexed { idx, item ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "${idx + 1}.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = notionInk
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 15.5.sp,
                                lineHeight = 22.sp
                            ),
                            color = notionBodyInk
                        )
                    }
                }
            }
            BlockType.CHECKLIST -> ChecklistBlockView(
                content = block.content,
                isReadingMode = isReadingMode,
                onToggleLine = { updated -> onUpdateContent(updated, block.metadata) }
            )
            BlockType.TOGGLE_SECTION -> ToggleSectionBlockView(content = block.content)
            BlockType.QUOTE -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(42.dp)
                        .background(notionInk, RoundedCornerShape(2.dp))
                )
                Text(
                    text = "“${block.content}”",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontStyle = FontStyle.Italic,
                        fontSize = 15.5.sp,
                        lineHeight = 22.sp
                    ),
                    color = notionBodyInk
                )
            }
            BlockType.CALLOUT -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (accentBgColor == Color.Transparent) Color(0xFFF7F7F5) else accentBgColor)
                    .border(1.dp, accentBorderColor, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text("💡", fontSize = 18.sp)
                Text(
                    text = block.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    ),
                    color = notionInk
                )
            }
            BlockType.DIVIDER -> HorizontalDivider(
                thickness = 1.dp,
                color = Color(0xFFEBEBEA),
                modifier = Modifier.padding(vertical = 6.dp)
            )
            BlockType.LATEX_BLOCK -> LatexEquationBlockView(
                blockId = block.id,
                latexSource = block.content,
                equationNumber = equationIndex,
                onUpdateLatex = { onUpdateContent(it, block.metadata) }
            )
            BlockType.TABLE -> EditableTableBlockView(
                blockId = block.id,
                rawTableJson = block.content,
                isReadingMode = isReadingMode,
                onUpdateTableJson = { onUpdateContent(it, block.metadata) }
            )
            BlockType.CODE_BLOCK -> CodeEditorBlockView(
                code = block.content,
                languageName = block.metadata ?: "KOTLIN",
                isReadingMode = isReadingMode,
                onUpdateCode = { newCode, newLang -> onUpdateContent(newCode, newLang) }
            )
            BlockType.IMAGE, BlockType.IMAGE_GALLERY -> ImageAttachmentBlockView(
                uriString = block.content,
                caption = block.metadata.orEmpty(),
                isReadingMode = isReadingMode,
                onPickUri = { uri, cap -> onAttachImage(uri, cap) }
            )
            BlockType.BOOKMARK -> LinkedNoteBookmarkView(
                rawBookmark = block.content,
                onNavigateToLinkedNote = onNavigateToLinkedNote
            )
            BlockType.HYPERLINK, BlockType.EMBEDDED_WEBPAGE, BlockType.EMBEDDED_VIDEO -> SecureEmbedBlockView(
                rawPayload = block.content,
                blockType = blockType
            )
            BlockType.MERMAID_DIAGRAM, BlockType.ENGINEERING_SCHEMATIC -> DiagramSchematicBlockView(
                rawPayload = block.content,
                blockType = blockType
            )
            BlockType.FILE_ATTACHMENT, BlockType.AUDIO_ATTACHMENT -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF7F7F5))
                    .border(1.dp, Color(0xFFEBEBEA), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${blockType.badge} ${block.content}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = notionInk,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "FILE",
                    style = MaterialTheme.typography.labelSmall,
                    color = notionMuted
                )
            }
        }

        // Structured Notion Block Action & Formatting Deck (appears when user taps the block)
        if (!isReadingMode && isEditingBlock) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFEBEBEA)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Block Type Selector + "+ Below" + Color Accents + Reorder / Copy / Delete
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = { showTypeConverter = !showTypeConverter },
                                color = Color(0xFFEFEFEF),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${blockType.badge} ${blockType.displayName} ▾",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = notionInk,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }

                            Surface(
                                onClick = { showInsertBelowBar = !showInsertBelowBar },
                                color = Color(0xFFEAF2FF),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "+ Insert Below",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF003DA5),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }

                            // Block Accent Color Dots
                            if (blockType != BlockType.CODE_BLOCK && blockType != BlockType.ENGINEERING_SCHEMATIC) {
                                listOf(
                                    "" to Color(0xFFE5E5E5),
                                    "BLUE" to Color(0xFF60A5FA),
                                    "AMBER" to Color(0xFFFBBF24),
                                    "EMERALD" to Color(0xFF34D399),
                                    "ROSE" to Color(0xFFF87171),
                                    "PURPLE" to Color(0xFFA78BFA)
                                ).forEach { (colorKey, dotColor) ->
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(RoundedCornerShape(9.dp))
                                            .background(dotColor)
                                            .border(
                                                width = if ((block.metadata ?: "") == colorKey) 2.dp else 0.5.dp,
                                                color = if ((block.metadata ?: "") == colorKey) notionInk else Color.Gray,
                                                shape = RoundedCornerShape(9.dp)
                                            )
                                            .clickable {
                                                onUpdateContent(block.content, colorKey.ifEmpty { null })
                                            }
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            IconButton(
                                onClick = { isEditingBlock = !isEditingBlock },
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("edit_block_${block.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Close block editor",
                                    tint = notionInk,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            IconButton(onClick = onMoveUp, modifier = Modifier.size(30.dp)) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowUpward,
                                    contentDescription = "Move block up",
                                    tint = notionMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            IconButton(onClick = onMoveDown, modifier = Modifier.size(30.dp)) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDownward,
                                    contentDescription = "Move block down",
                                    tint = notionMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            IconButton(
                                onClick = { clipboard.setText(AnnotatedString(block.content)) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "Copy block text",
                                    tint = notionMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            IconButton(onClick = onDuplicate, modifier = Modifier.size(30.dp)) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Duplicate block",
                                    tint = notionMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("delete_block_${block.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete block",
                                    tint = Color(0xFFD93025),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    // Expandable "+ Insert Block Below" quick selector
                    if (showInsertBelowBar) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                BlockType.PARAGRAPH,
                                BlockType.HEADING_1,
                                BlockType.HEADING_2,
                                BlockType.BULLETED_LIST,
                                BlockType.CHECKLIST,
                                BlockType.CALLOUT,
                                BlockType.LATEX_BLOCK,
                                BlockType.TABLE,
                                BlockType.CODE_BLOCK,
                                BlockType.DIVIDER
                            ).forEach { nextType ->
                                Surface(
                                    onClick = {
                                        onInsertBlockBelow(nextType)
                                        showInsertBelowBar = false
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF7F7F5),
                                    border = BorderStroke(1.dp, Color(0xFFEBEBEA))
                                ) {
                                    Text(
                                        text = "+ ${nextType.badge} ${nextType.displayName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = notionInk,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Expandable "Turn Into" Block Type Converter
                    if (showTypeConverter) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                BlockType.HEADING_1,
                                BlockType.HEADING_2,
                                BlockType.HEADING_3,
                                BlockType.PARAGRAPH,
                                BlockType.BULLETED_LIST,
                                BlockType.NUMBERED_LIST,
                                BlockType.CHECKLIST,
                                BlockType.TOGGLE_SECTION,
                                BlockType.CALLOUT,
                                BlockType.QUOTE,
                                BlockType.LATEX_BLOCK,
                                BlockType.TABLE,
                                BlockType.CODE_BLOCK,
                                BlockType.MERMAID_DIAGRAM,
                                BlockType.ENGINEERING_SCHEMATIC
                            ).forEach { target ->
                                OutlinedButton(
                                    onClick = {
                                        onConvertType(target)
                                        showTypeConverter = false
                                    },
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("${target.badge} ${target.displayName}", fontSize = 10.5.sp)
                                }
                            }
                        }
                    }

                    if (blockType != BlockType.TABLE) {
                        var draftText by remember(block.content) { mutableStateOf(block.content) }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Rich Text Formatting Strip
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    "B Bold" to "**bold**",
                                    "I Italic" to "*italic*",
                                    "U Underline" to "<u>underline</u>",
                                    "S Strike" to "~~strike~~",
                                    "`Code`" to "`code`",
                                    "$ Math $" to "\$E = mc^2\$",
                                    "@Mention" to "@Prof_Sharma",
                                    "[[Link]]" to "[[note_001|Linked Note]]"
                                ).forEach { (label, snippet) ->
                                    Surface(
                                        onClick = {
                                            val updated = if (draftText.isBlank()) snippet else "$draftText $snippet"
                                            draftText = updated
                                            onUpdateContent(updated, block.metadata)
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFF7F7F5),
                                        border = BorderStroke(1.dp, Color(0xFFEBEBEA))
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = notionInk,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Quick Engineering & Math Symbols Strip
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("∫", "∑", "∏", "√", "∂", "∇", "∞", "≈", "≠", "≤", "≥", "⇒", "⇔", "α", "β", "θ", "λ", "π", "ω", "Ω").forEach { sym ->
                                    Surface(
                                        onClick = {
                                            val updated = "$draftText$sym"
                                            draftText = updated
                                            onUpdateContent(updated, block.metadata)
                                        },
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFEFEFEF)
                                    ) {
                                        Text(
                                            text = sym,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            color = notionInk,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = draftText,
                                onValueChange = {
                                    draftText = it
                                    onUpdateContent(it, block.metadata)
                                },
                                label = { Text("Edit ${blockType.displayName}") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("block_input_${block.id}")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistBlockView(
    content: String,
    isReadingMode: Boolean = false,
    onToggleLine: (String) -> Unit
) {
    val tokens = LocalGovindaTokens.current
    val lines = content.lines().filter { it.isNotBlank() }
    val doneCount = lines.count { it.trim().startsWith("[x]", ignoreCase = true) }
    val totalCount = lines.size.coerceAtLeast(1)
    val pct = (doneCount * 100) / totalCount

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Checklist Header with Progress Pill & Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFFEFEFEF),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "✓ $doneCount/${lines.size} Done ($pct%)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191919),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            if (!isReadingMode) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "+ Task",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF003DA5),
                        modifier = Modifier.clickable {
                            val updated = (lines + "[ ] New action item").joinToString("\n")
                            onToggleLine(updated)
                        }
                    )
                    Text(
                        text = "Check All",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF787774),
                        modifier = Modifier.clickable {
                            val updated = lines.map { line ->
                                val clean = line.replace(Regex("^\\[[xX ]\\]\\s*"), "")
                                "[x] $clean"
                            }.joinToString("\n")
                            onToggleLine(updated)
                        }
                    )
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF787774),
                        modifier = Modifier.clickable {
                            val updated = lines.map { line ->
                                val clean = line.replace(Regex("^\\[[xX ]\\]\\s*"), "")
                                "[ ] $clean"
                            }.joinToString("\n")
                            onToggleLine(updated)
                        }
                    )
                }
            }
        }

        lines.forEachIndexed { idx, line ->
            val checked = line.trim().startsWith("[x]", ignoreCase = true)
            val cleanText = line.replace(Regex("^\\[[xX ]\\]\\s*"), "")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val updatedLines = lines.toMutableList()
                        updatedLines[idx] = if (checked) "[ ] $cleanText" else "[x] $cleanText"
                        onToggleLine(updatedLines.joinToString("\n"))
                    }
                    .padding(vertical = 2.dp)
            ) {
                Icon(
                    imageVector = if (checked) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                    contentDescription = null,
                    tint = if (checked) tokens.success else tokens.secondaryText,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = cleanText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (checked) tokens.secondaryText else tokens.primaryText
                )
            }
        }
    }
}

@Composable
private fun ToggleSectionBlockView(content: String) {
    val tokens = LocalGovindaTokens.current
    var expanded by remember { mutableStateOf(false) }
    val parts = content.split("::", limit = 2)
    val header = parts.getOrElse(0) { content }
    val details = parts.getOrElse(1) { "Expandable section details." }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(tokens.surface)
            .border(1.dp, tokens.border, RoundedCornerShape(6.dp))
            .clickable { expanded = !expanded }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = header,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = tokens.primary
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = tokens.primary
            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = details,
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.primaryText
            )
        }
    }
}

@Composable
fun LatexEquationBlockView(
    blockId: String,
    latexSource: String,
    equationNumber: Int?,
    onUpdateLatex: (String) -> Unit
) {
    val tokens = LocalGovindaTokens.current
    val clipboard = LocalClipboardManager.current
    val ast = remember(latexSource) { LatexRendererEngine.parse(latexSource) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(tokens.selectedState)
            .border(
                width = 1.6.dp,
                color = if (ast.isValid) tokens.primary else tokens.error,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
            .testTag("latex_rendered_$blockId"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!ast.isValid) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = tokens.error)
                Text(
                    text = ast.errorMessage ?: "Invalid LaTeX expression",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = tokens.error
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (ast.matrixRows != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (ast.prefixExpression.isNotEmpty()) {
                            Text(
                                text = ast.prefixExpression,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = tokens.primaryText
                            )
                        }
                        Column(
                            modifier = Modifier
                                .border(1.5.dp, tokens.primary, RoundedCornerShape(4.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ast.matrixRows.forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    row.forEach { cell ->
                                        Text(
                                            text = cell,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = tokens.primaryText
                                        )
                                    }
                                }
                            }
                        }
                        if (ast.suffixExpression.isNotEmpty()) {
                            Text(
                                text = ast.suffixExpression,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = tokens.primaryText
                            )
                        }
                    }
                } else if (ast.topFraction != null && ast.bottomFraction != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (ast.prefixExpression.isNotEmpty()) {
                            Text(
                                text = ast.prefixExpression,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = tokens.primaryText
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = ast.topFraction,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = tokens.primaryText
                            )
                            HorizontalDivider(
                                modifier = Modifier.width(120.dp),
                                thickness = 1.8.dp,
                                color = tokens.primary
                            )
                            Text(
                                text = ast.bottomFraction,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = tokens.primaryText
                            )
                        }
                        if (ast.suffixExpression.isNotEmpty()) {
                            Text(
                                text = ast.suffixExpression,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = tokens.primaryText
                            )
                        }
                    }
                } else {
                    Text(
                        text = ast.unicodeFormatted,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = tokens.primaryText
                    )
                }

                if (equationNumber != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "($equationNumber)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = tokens.secondary
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LaTeX: $latexSource",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = tokens.secondaryText,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = { clipboard.setText(AnnotatedString(latexSource)) },
                modifier = Modifier.height(32.dp)
            ) {
                Text("COPY LATEX", fontSize = 11.sp, color = tokens.primary)
            }
        }
    }
}

@Composable
fun EditableTableBlockView(
    blockId: String,
    rawTableJson: String,
    isReadingMode: Boolean,
    onUpdateTableJson: (String) -> Unit
) {
    val tokens = LocalGovindaTokens.current
    val clipboard = LocalClipboardManager.current
    val table = remember(rawTableJson) { TableData.fromJsonOrDefault(rawTableJson) }
    var sortAsc by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("table_block_$blockId"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .border(1.5.dp, tokens.border, RoundedCornerShape(6.dp))
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .background(tokens.selectedState)
                    .padding(vertical = 6.dp)
            ) {
                table.columns.forEachIndexed { colIdx, colName ->
                    Box(
                        modifier = Modifier
                            .width(148.dp)
                            .padding(horizontal = 8.dp)
                            .clickable(enabled = !isReadingMode) {
                                val sortedRows = if (sortAsc) {
                                    table.rows.sortedBy { it.getOrNull(colIdx).orEmpty() }
                                } else {
                                    table.rows.sortedByDescending { it.getOrNull(colIdx).orEmpty() }
                                }
                                sortAsc = !sortAsc
                                onUpdateTableJson(table.copy(rows = sortedRows).toJson())
                            }
                    ) {
                        Text(
                            text = "$colName ⇅",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = tokens.primary
                        )
                    }
                }
            }
            HorizontalDivider(thickness = 1.5.dp, color = tokens.border)

            // Data Rows
            table.rows.forEachIndexed { rIdx, row ->
                Row(
                    modifier = Modifier
                        .background(if (rIdx % 2 == 0) tokens.background else tokens.surface)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    table.columns.indices.forEach { cIdx ->
                        val cellVal = row.getOrNull(cIdx).orEmpty()
                        if (isReadingMode) {
                            Text(
                                text = cellVal,
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.primaryText,
                                modifier = Modifier
                                    .width(148.dp)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        } else {
                            OutlinedTextField(
                                value = cellVal,
                                onValueChange = { newText ->
                                    val newRows = table.rows.mapIndexed { ri, r ->
                                        if (ri == rIdx) {
                                            table.columns.indices.map { ci ->
                                                if (ci == cIdx) newText else r.getOrNull(ci).orEmpty()
                                            }
                                        } else {
                                            r
                                        }
                                    }
                                    onUpdateTableJson(table.copy(rows = newRows).toJson())
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .width(148.dp)
                                    .padding(horizontal = 4.dp)
                                    .testTag("table_cell_${blockId}_${rIdx}_${cIdx}")
                            )
                        }
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = tokens.divider)
            }
        }

        if (!isReadingMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val emptyRow = table.columns.map { "New" }
                        onUpdateTableJson(table.copy(rows = table.rows + listOf(emptyRow)).toJson())
                    },
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("table_add_row_$blockId")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ROW", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = {
                        val nextCol = "Col ${table.columns.size + 1}"
                        val nextRows = table.rows.map { it + "-" }
                        onUpdateTableJson(
                            table.copy(columns = table.columns + nextCol, rows = nextRows).toJson()
                        )
                    },
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("table_add_col_$blockId")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("COL", fontSize = 11.sp)
                }
                if (table.rows.size > 1) {
                    OutlinedButton(
                        onClick = {
                            onUpdateTableJson(table.copy(rows = table.rows.dropLast(1)).toJson())
                        },
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("− ROW", fontSize = 11.sp)
                    }
                }
                OutlinedButton(
                    onClick = { clipboard.setText(AnnotatedString(table.toCsv())) },
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("COPY CSV", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { clipboard.setText(AnnotatedString(table.toMarkdownTable())) },
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("COPY MD", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun CodeEditorBlockView(
    code: String,
    languageName: String,
    isReadingMode: Boolean,
    onUpdateCode: (String, String) -> Unit
) {
    val tokens = LocalGovindaTokens.current
    val clipboard = LocalClipboardManager.current
    val language = remember(languageName) {
        CodeLanguage.entries.firstOrNull { it.name.equals(languageName, ignoreCase = true) }
            ?: CodeLanguage.KOTLIN
    }
    var isFolded by remember { mutableStateOf(false) }

    val highlighted = remember(code, language, tokens.primary, tokens.primaryText) {
        buildAnnotatedString {
            val words = Regex("([A-Za-z_][A-Za-z0-9_]*|[^A-Za-z_]+)").findAll(code)
            for (m in words) {
                val token = m.value
                if (token in language.keywords) {
                    withStyle(SpanStyle(color = tokens.primary, fontWeight = FontWeight.Bold)) {
                        append(token)
                    }
                } else {
                    withStyle(SpanStyle(color = tokens.primaryText)) {
                        append(token)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(tokens.surface)
            .border(1.4.dp, tokens.border, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BlueprintBadge(text = language.displayName.uppercase())
                if (!isReadingMode) {
                    listOf(
                        CodeLanguage.KOTLIN,
                        CodeLanguage.PYTHON,
                        CodeLanguage.CPP,
                        CodeLanguage.VERILOG,
                        CodeLanguage.MATLAB,
                        CodeLanguage.SQL
                    ).forEach { lang ->
                        if (lang != language) {
                            Text(
                                text = lang.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.secondaryText,
                                modifier = Modifier
                                    .clickable { onUpdateCode(code, lang.name) }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = { isFolded = !isFolded },
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(if (isFolded) "UNFOLD" else "FOLD", fontSize = 11.sp)
                }
                TextButton(
                    onClick = { clipboard.setText(AnnotatedString(code)) },
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("COPY", fontSize = 11.sp, color = tokens.primary)
                }
            }
        }

        if (!isFolded) {
            val lines = code.lines()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .padding(end = 10.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    lines.indices.forEach { idx ->
                        Text(
                            text = "${idx + 1}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = tokens.mutedText
                        )
                    }
                }
                Text(
                    text = highlighted,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                )
            }
        }
    }
}

@Composable
private fun ImageAttachmentBlockView(
    uriString: String,
    caption: String,
    isReadingMode: Boolean,
    onPickUri: (String, String) -> Unit
) {
    val tokens = LocalGovindaTokens.current
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPickUri(uri.toString(), caption.ifEmpty { "Attached Engineering Figure" })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(tokens.surface)
            .border(1.4.dp, tokens.border, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Image,
            contentDescription = caption.ifEmpty { "Attached figure" },
            tint = tokens.primary,
            modifier = Modifier.size(36.dp)
        )
        Text(
            text = if (uriString.isBlank()) "No local image attached yet" else "Local Media Reference: $uriString",
            style = MaterialTheme.typography.bodySmall,
            color = tokens.primaryText,
            textAlign = TextAlign.Center
        )
        if (caption.isNotBlank()) {
            Text(
                text = "Caption: $caption",
                style = MaterialTheme.typography.labelMedium,
                color = tokens.secondaryText
            )
        }
        if (!isReadingMode) {
            OutlinedButton(
                onClick = {
                    pickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("SELECT IMAGE (PHOTO PICKER)", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun LinkedNoteBookmarkView(
    rawBookmark: String,
    onNavigateToLinkedNote: (String) -> Unit
) {
    val tokens = LocalGovindaTokens.current
    val cleaned = rawBookmark.removePrefix("[[").removeSuffix("]]")
    val parts = cleaned.split("|", limit = 2)
    val targetNoteId = parts.getOrElse(0) { "note_001" }.trim()
    val label = parts.getOrElse(1) { targetNoteId }.trim()

    Surface(
        onClick = { onNavigateToLinkedNote(targetNoteId) },
        shape = RoundedCornerShape(8.dp),
        color = tokens.selectedState,
        border = BorderStroke(1.5.dp, tokens.primary),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("linked_note_chip_$targetNoteId")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Link, contentDescription = null, tint = tokens.primary)
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = tokens.primary
                    )
                    Text(
                        text = "Internal Note Link → $targetNoteId",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.secondaryText
                    )
                }
            }
            BlueprintBadge(text = "OPEN NOTE")
        }
    }
}

@Composable
private fun SecureEmbedBlockView(
    rawPayload: String,
    blockType: BlockType
) {
    val tokens = LocalGovindaTokens.current
    val context = LocalContext.current
    var userApproved by remember(rawPayload) { mutableStateOf(false) }
    val parts = rawPayload.split("|", limit = 2)
    val url = parts.getOrElse(0) { rawPayload }.trim()
    val title = parts.getOrElse(1) { url }.trim()
    val isHttps = url.startsWith("https://", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(tokens.surface)
            .border(1.4.dp, tokens.border, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                Icon(Icons.Filled.Lock, contentDescription = null, tint = tokens.primary, modifier = Modifier.size(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = tokens.primaryText
                )
            }
            BlueprintBadge(text = if (isHttps) "HTTPS VERIFIED" else "UNVERIFIED URL")
        }

        Text(
            text = "Offline Sandbox Active: External embeds are never auto-loaded without explicit approval.",
            style = MaterialTheme.typography.bodySmall,
            color = tokens.secondaryText
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { userApproved = !userApproved },
                modifier = Modifier.height(36.dp)
            ) {
                Text(if (userApproved) "HIDE PREVIEW" else "APPROVE EMBED PREVIEW", fontSize = 11.sp)
            }
            if (isHttps) {
                OutlinedButton(
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            )
                        }
                    },
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("OPEN IN BROWSER", fontSize = 11.sp)
                }
            }
        }

        if (userApproved) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(tokens.selectedState)
                    .padding(10.dp)
            ) {
                Text(
                    text = "Approved Offline Reference Card (${blockType.displayName}):\nSource: $url\nStatus: Cached metadata available offline.",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = tokens.primaryText
                )
            }
        }
    }
}

@Composable
private fun DiagramSchematicBlockView(
    rawPayload: String,
    blockType: BlockType
) {
    val tokens = LocalGovindaTokens.current
    val isPinDiagram = rawPayload.startsWith("PIN_DIAGRAM|")
    val headerTitle = when {
        isPinDiagram -> "🔌 ENGINEERING IC PIN-DIAGRAM BLOCK"
        blockType == BlockType.ENGINEERING_SCHEMATIC -> "⚡ CIRCUIT SCHEMATIC DIAGRAM BLOCK"
        else -> "◇ MERMAID FLOWCHART DIAGRAM BLOCK"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(tokens.surface)
            .border(1.4.dp, tokens.primary, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = headerTitle,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = tokens.primary
            )
            BlueprintBadge(text = if (isPinDiagram) "DIP PACKAGE" else "OFFLINE VECTOR")
        }

        if (blockType == BlockType.MERMAID_DIAGRAM) {
            val nodes = remember(rawPayload) {
                Regex("\\[([^\\]]+)\\]").findAll(rawPayload).map { it.groupValues[1] }.toList()
                    .ifEmpty { listOf("x(t)", "H(s)", "y(t)") }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tokens.background, RoundedCornerShape(6.dp))
                    .padding(10.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                nodes.forEachIndexed { idx, label ->
                    Surface(
                        color = tokens.selectedState,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.2.dp, tokens.primary)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = tokens.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    if (idx < nodes.lastIndex) {
                        Text("━━▶", color = tokens.secondary, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        } else {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .background(tokens.background, RoundedCornerShape(6.dp))
            ) {
                val strokeColor: Color = tokens.primary
                val yMid = size.height / 2f
                drawLine(strokeColor, Offset(24f, yMid), Offset(size.width - 24f, yMid), strokeWidth = 4f)
                drawCircle(strokeColor, radius = 14f, center = Offset(size.width * 0.25f, yMid), style = Stroke(width = 4f))
                drawCircle(strokeColor, radius = 14f, center = Offset(size.width * 0.50f, yMid), style = Stroke(width = 4f))
                drawCircle(strokeColor, radius = 14f, center = Offset(size.width * 0.75f, yMid), style = Stroke(width = 4f))
            }
        }

        Text(
            text = rawPayload.removePrefix("PIN_DIAGRAM|"),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = tokens.primaryText
        )
    }
}
