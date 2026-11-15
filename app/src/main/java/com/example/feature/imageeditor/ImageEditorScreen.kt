package com.example.feature.imageeditor

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val StudioBg = Color(0xFFF7F9FC)
private val StudioCardBg = Color(0xFFFFFFFF)
private val StudioInk = Color(0xFF1E293B)
private val StudioMuted = Color(0xFF64748B)
private val StudioPrimary = Color(0xFF0F172A)
private val StudioAccent = Color(0xFF2563EB)
private val StudioBorder = Color(0xFFE2E8F0)

fun getTabIcon(tab: ImageEditTab): ImageVector {
    return when (tab) {
        ImageEditTab.GALLERY -> Icons.Filled.PhotoLibrary
        ImageEditTab.VIEWER -> Icons.Filled.Visibility
        ImageEditTab.ADJUST -> Icons.Filled.Tune
        ImageEditTab.RESIZE -> Icons.Filled.AspectRatio
        ImageEditTab.TRANSFORM -> Icons.Filled.Crop
        ImageEditTab.CONVERT -> Icons.Filled.Compress
        ImageEditTab.OCR -> Icons.Filled.TextFields
        ImageEditTab.COLLAGE -> Icons.Filled.GridView
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEditorScreen(
    uiState: ImageEditorUiState,
    viewModel: ImageEditorViewModel,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val activeImage = uiState.activeImage

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment ?: "imported_image_${System.currentTimeMillis()}.jpg"
            viewModel.importCustomImage(uri.toString(), fileName, context)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("image_editor_screen"),
        containerColor = StudioBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StudioPrimary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text(
                                text = "Image Studio & Editor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = StudioInk
                            )
                            activeImage?.let {
                                Text(
                                    text = "${it.fileName} • ${it.width}×${it.height} px",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StudioMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            imagePickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.testTag("import_image_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Import Image", tint = StudioInk)
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("image_studio_settings_button")
                    ) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = StudioInk)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Status Banner if present
            uiState.statusBanner?.let { msg ->
                Surface(
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = msg, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = Color(0xFF1E40AF))
                        TextButton(onClick = { viewModel.clearStatusBanner() }) {
                            Text("Dismiss", fontSize = 11.sp, color = Color(0xFF1E40AF))
                        }
                    }
                }
            }

            // Professional Horizontal Tab Bar with Icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ImageEditTab.entries.forEach { tab ->
                    val isSelected = uiState.activeTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(tab) },
                        leadingIcon = {
                            Icon(
                                imageVector = getTabIcon(tab),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) Color.White else StudioAccent
                            )
                        },
                        label = { Text(tab.title, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("tab_${tab.name}")
                    )
                }
            }

            HorizontalDivider(color = StudioBorder)

            // Main Content Area based on Active Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (uiState.activeTab) {
                    ImageEditTab.GALLERY -> GalleryTabContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        onImport = {
                            imagePickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    )
                    ImageEditTab.VIEWER -> ViewerAndInfoTabContent(activeImage = activeImage)
                    ImageEditTab.ADJUST -> AdjustmentsTabContent(uiState = uiState, viewModel = viewModel)
                    ImageEditTab.RESIZE -> ResizeTabContent(uiState = uiState, viewModel = viewModel)
                    ImageEditTab.TRANSFORM -> TransformTabContent(uiState = uiState, viewModel = viewModel)
                    ImageEditTab.CONVERT -> ConvertAndCompressTabContent(uiState = uiState, viewModel = viewModel)
                    ImageEditTab.OCR -> TextExtractTabContent(activeImage = activeImage, clipboard = clipboard)
                    ImageEditTab.COLLAGE -> CollageTabContent(uiState = uiState, viewModel = viewModel)
                }
            }
        }
    }

    // Output Preview Dialog (Shows real-time output preview when applying tools)
    if (uiState.showOutputPreviewDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissOutputPreview() },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
                    Text(text = uiState.outputPreviewTitle ?: "Tool Output Preview", fontWeight = FontWeight.Bold, color = StudioInk)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Mini Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (activeImage != null) {
                            AsyncImage(
                                model = activeImage.uriOrDrawableRes,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(uiState.adjustments.rotationDegrees.toFloat())
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, StudioBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.outputPreviewDetails ?: "Processing complete successfully.",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, lineHeight = 18.sp),
                            color = StudioInk,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissOutputPreview() },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
                ) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun GalleryTabContent(
    uiState: ImageEditorUiState,
    viewModel: ImageEditorViewModel,
    onImport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = StudioAccent, modifier = Modifier.size(22.dp))
                Text(
                    text = "Gallery Library (${uiState.images.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = StudioInk
                )
            }
            Button(
                onClick = onImport,
                colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Image", fontSize = 12.sp)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(uiState.images, key = { it.id }) { img ->
                val isSelected = uiState.activeImageId == img.id
                Card(
                    onClick = {
                        viewModel.setActiveImage(img.id)
                        viewModel.selectTab(ImageEditTab.VIEWER)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) StudioAccent else StudioBorder),
                    modifier = Modifier.testTag("gallery_item_${img.id}")
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = img.uriOrDrawableRes,
                                contentDescription = img.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = img.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = StudioInk,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${img.width}×${img.height}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StudioMuted
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFEFF6FF)
                                ) {
                                    Text(
                                        text = img.format,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StudioAccent,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewerAndInfoTabContent(activeImage: ImageItem?) {
    if (activeImage == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No image selected", color = StudioMuted)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Image Preview Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = BorderStroke(1.dp, StudioBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0B0F19)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = activeImage.uriOrDrawableRes,
                        contentDescription = activeImage.title,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Action Toolbar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { /* Download action */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { /* Share action */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open With", fontSize = 12.sp)
                }
            }
        }

        // EXIF & File Info Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = BorderStroke(1.dp, StudioBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = StudioAccent, modifier = Modifier.size(20.dp))
                        Text(
                            text = "EXIF & Image Properties",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = StudioInk
                        )
                    }
                    HorizontalDivider(color = StudioBorder)

                    val infoRows = listOf(
                        "File Name" to activeImage.fileName,
                        "Resolution" to "${activeImage.width} × ${activeImage.height} pixels",
                        "File Size" to "${activeImage.fileSizeBytes / 1024} KB",
                        "Format" to activeImage.format,
                        "Resolution (DPI)" to "${activeImage.dpi} DPI",
                        "Color Space" to activeImage.colorSpace,
                        "Camera Make" to activeImage.cameraMake,
                        "Modified" to java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(activeImage.dateModified)
                    )

                    infoRows.forEach { (label, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = label, style = MaterialTheme.typography.bodySmall, color = StudioMuted)
                            Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = StudioInk)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdjustmentsTabContent(
    uiState: ImageEditorUiState,
    viewModel: ImageEditorViewModel
) {
    val adj = uiState.adjustments
    val activeImage = uiState.activeImage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Preview Box
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCardBg),
            border = BorderStroke(1.dp, StudioBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                if (activeImage != null) {
                    AsyncImage(
                        model = activeImage.uriOrDrawableRes,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(adj.rotationDegrees.toFloat())
                    )
                }
            }
        }

        // Sliders & Filter Chips Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCardBg),
            border = BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Tune, contentDescription = null, tint = StudioAccent, modifier = Modifier.size(20.dp))
                        Text(text = "Adjustments & Filters", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StudioInk)
                    }
                    TextButton(onClick = { viewModel.resetAdjustments() }) {
                        Text("Reset All", fontSize = 12.sp, color = StudioAccent)
                    }
                }

                // Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ImageFilterType.entries.forEach { fType ->
                        FilterChip(
                            selected = adj.filterType == fType,
                            onClick = { viewModel.updateAdjustments { it.copy(filterType = fType) } },
                            label = { Text(fType.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                // Brightness Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Brightness", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = StudioInk)
                        Text("${(adj.brightness * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = StudioMuted)
                    }
                    Slider(
                        value = adj.brightness,
                        onValueChange = { v -> viewModel.updateAdjustments { it.copy(brightness = v) } },
                        valueRange = -1f..1f
                    )
                }

                // Contrast Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Contrast", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = StudioInk)
                        Text("${String.format("%.1f", adj.contrast)}x", style = MaterialTheme.typography.labelSmall, color = StudioMuted)
                    }
                    Slider(
                        value = adj.contrast,
                        onValueChange = { v -> viewModel.updateAdjustments { it.copy(contrast = v) } },
                        valueRange = 0.5f..2f
                    )
                }
            }
        }
    }
}

@Composable
private fun ResizeTabContent(
    uiState: ImageEditorUiState,
    viewModel: ImageEditorViewModel
) {
    val activeImage = uiState.activeImage
    val rp = uiState.resizeParams

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCardBg),
            border = BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.AspectRatio, contentDescription = null, tint = StudioAccent, modifier = Modifier.size(20.dp))
                    Text(text = "Resize & DPI Settings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StudioInk)
                }
                Text(text = "Original Dimensions: ${activeImage?.width} × ${activeImage?.height} px @ ${activeImage?.dpi} DPI", style = MaterialTheme.typography.bodySmall, color = StudioMuted)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = rp.widthPx.toString(),
                        onValueChange = { v -> v.toIntOrNull()?.let { w -> viewModel.updateResizeParams { it.copy(widthPx = w) } } },
                        label = { Text("Width (px)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = rp.heightPx.toString(),
                        onValueChange = { v -> v.toIntOrNull()?.let { h -> viewModel.updateResizeParams { it.copy(heightPx = h) } } },
                        label = { Text("Height (px)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Text(text = "Print Resolution (DPI):", style = MaterialTheme.typography.bodySmall, color = StudioMuted)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(72, 150, 300, 600).forEach { standardDpi ->
                        FilterChip(
                            selected = rp.dpi == standardDpi,
                            onClick = { viewModel.updateResizeParams { it.copy(dpi = standardDpi) } },
                            label = { Text("$standardDpi DPI", fontSize = 11.sp) }
                        )
                    }
                }

                Button(
                    onClick = { viewModel.applyResizeAndSave() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply Resize & Preview Output")
                }
            }
        }
    }
}

@Composable
private fun TransformTabContent(
    uiState: ImageEditorUiState,
    viewModel: ImageEditorViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCardBg),
            border = BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Crop, contentDescription = null, tint = StudioAccent, modifier = Modifier.size(20.dp))
                    Text(text = "Crop & Rotate Studio", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StudioInk)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.performRotate() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rotate 90°")
                    }
                    OutlinedButton(
                        onClick = { viewModel.performFlipHorizontal() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Flip H")
                    }
                }

                Text(text = "Aspect Ratio Presets:", style = MaterialTheme.typography.bodySmall, color = StudioMuted)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Free", "1:1 Square", "4:3 Standard", "16:9 Widescreen").forEach { ratio ->
                        FilterChip(
                            selected = uiState.adjustments.cropAspect == ratio,
                            onClick = { viewModel.updateAdjustments { it.copy(cropAspect = ratio) } },
                            label = { Text(ratio, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConvertAndCompressTabContent(
    uiState: ImageEditorUiState,
    viewModel: ImageEditorViewModel
) {
    var showRenameDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCardBg),
            border = BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Compress, contentDescription = null, tint = StudioAccent, modifier = Modifier.size(20.dp))
                    Text(text = "Convert, Compress & Rename", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StudioInk)
                }

                OutlinedButton(
                    onClick = { showRenameDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rename File: ${uiState.activeImage?.fileName}")
                }

                HorizontalDivider(color = StudioBorder)

                Text(text = "Export Format:", style = MaterialTheme.typography.bodySmall, color = StudioMuted)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExportFormat.entries.forEach { fmt ->
                        FilterChip(
                            selected = uiState.targetExportFormat == fmt,
                            onClick = { viewModel.setExportFormat(fmt) },
                            label = { Text(fmt.name, fontSize = 11.sp) }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Compression Quality", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = StudioInk)
                    Text("${uiState.compressionQuality}%", style = MaterialTheme.typography.labelSmall, color = StudioMuted)
                }
                Slider(
                    value = uiState.compressionQuality.toFloat(),
                    onValueChange = { v -> viewModel.setCompressionQuality(v.toInt()) },
                    valueRange = 10f..100f
                )
                Text(
                    text = "Estimated File Size: ~${(uiState.activeImage?.fileSizeBytes ?: 200000) * uiState.compressionQuality / 100 / 1024} KB",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = StudioAccent
                )

                Button(
                    onClick = { viewModel.applyCompressionAndConversion() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply & Preview Output")
                }
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Image File") },
            text = {
                OutlinedTextField(
                    value = uiState.newFileNameInput,
                    onValueChange = { viewModel.updateFileName(it) },
                    singleLine = true,
                    label = { Text("File Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.applyRename()
                    showRenameDialog = false
                }) {
                    Text("Save & Preview")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TextExtractTabContent(
    activeImage: ImageItem?,
    clipboard: androidx.compose.ui.platform.ClipboardManager
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCardBg),
            border = BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.TextFields, contentDescription = null, tint = StudioAccent, modifier = Modifier.size(20.dp))
                        Text(text = "OCR Text Extraction", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StudioInk)
                    }
                    Button(
                        onClick = {
                            activeImage?.extractedText?.let { clipboard.setText(AnnotatedString(it)) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy", fontSize = 12.sp)
                    }
                }

                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, StudioBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = activeImage?.extractedText ?: "No text detected.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, lineHeight = 20.sp),
                        color = StudioInk,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CollageTabContent(
    uiState: ImageEditorUiState,
    viewModel: ImageEditorViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCardBg),
            border = BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.GridView, contentDescription = null, tint = StudioAccent, modifier = Modifier.size(20.dp))
                    Text(text = "Collage Creator", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StudioInk)
                }
                Text(text = "Select images for collage layout grid:", style = MaterialTheme.typography.bodySmall, color = StudioMuted)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("2x2 Grid", "Side by Side", "3-Panel Split").forEach { layout ->
                        FilterChip(
                            selected = uiState.collageLayout == layout,
                            onClick = { viewModel.setCollageLayout(layout) },
                            label = { Text(layout, fontSize = 11.sp) }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.images) { img ->
                        val isChecked = uiState.collageSelectedIds.contains(img.id)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isChecked) Color(0xFFEFF6FF) else Color.White,
                            border = BorderStroke(1.dp, if (isChecked) StudioAccent else StudioBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleCollageSelection(img.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = img.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = StudioInk)
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { viewModel.toggleCollageSelection(img.id) }
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = { viewModel.generateCollage() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generate & Preview Collage")
                }
            }
        }
    }
}
