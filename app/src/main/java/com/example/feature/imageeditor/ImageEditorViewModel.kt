package com.example.feature.imageeditor

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class ImageEditorUiState(
    val images: List<ImageItem> = listOf(
        ImageItem(
            id = "img_001",
            title = "RC Low-Pass Filter Schematic",
            fileName = "rc_low_pass_schematic.jpg",
            uriOrDrawableRes = "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=1200&q=80",
            fileSizeBytes = 245760,
            width = 1920,
            height = 1080,
            format = "JPEG",
            dpi = 300,
            extractedText = "RC Low-Pass Filter Circuit\nR = 10 kΩ, C = 100 nF\nCutoff Frequency: f_c = 1 / (2 * π * R * C) = 159.15 Hz\nTransfer Function: H(jω) = 1 / (1 + jωRC)",
            colorSpace = "sRGB IEC61966-2.1"
        ),
        ImageItem(
            id = "img_002",
            title = "Math Calculus Derivation",
            fileName = "calculus_integral_proof.png",
            uriOrDrawableRes = "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?auto=format&fit=crop&w=1200&q=80",
            fileSizeBytes = 512000,
            width = 2048,
            height = 1536,
            format = "PNG",
            dpi = 150,
            extractedText = "Fundamental Theorem of Calculus\n∫_{a}^{b} f'(x) dx = f(b) - f(a)\nLet F'(x) = f(x). Then d/dx [∫_{a}^{x} f(t) dt] = f(x).",
            colorSpace = "Display P3"
        ),
        ImageItem(
            id = "img_003",
            title = "Oscilloscope Waveform Capture",
            fileName = "oscilloscope_square_wave.webp",
            uriOrDrawableRes = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=1200&q=80",
            fileSizeBytes = 184320,
            width = 1280,
            height = 720,
            format = "WEBP",
            dpi = 72,
            extractedText = "Channel 1: 5.0 V/div, Timebase: 1.0 ms/div\nFrequency: 1.00 kHz Square Wave\nRise Time: 12.4 ns, Duty Cycle: 50.0%",
            colorSpace = "sRGB"
        )
    ),
    val activeImageId: String = "img_001",
    val activeTab: ImageEditTab = ImageEditTab.GALLERY,
    val adjustments: ImageAdjustments = ImageAdjustments(),
    val resizeParams: ResizeParams = ResizeParams(),
    val compressionQuality: Int = 85,
    val targetExportFormat: ExportFormat = ExportFormat.JPEG,
    val newFileNameInput: String = "",
    val statusBanner: String? = null,
    val isProcessing: Boolean = false,
    val collageSelectedIds: Set<String> = emptySet(),
    val collageLayout: String = "2x2 Grid",
    val outputPreviewTitle: String? = null,
    val outputPreviewDetails: String? = null,
    val showOutputPreviewDialog: Boolean = false
) {
    val activeImage: ImageItem?
        get() = images.firstOrNull { it.id == activeImageId } ?: images.firstOrNull()
}

class ImageEditorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ImageEditorUiState())
    val uiState: StateFlow<ImageEditorUiState> = _uiState.asStateFlow()

    init {
        val first = _uiState.value.images.firstOrNull()
        if (first != null) {
            _uiState.update { it.copy(newFileNameInput = first.fileName) }
        }
    }

    fun selectTab(tab: ImageEditTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun setActiveImage(id: String) {
        val img = _uiState.value.images.firstOrNull { it.id == id } ?: return
        _uiState.update {
            it.copy(
                activeImageId = id,
                newFileNameInput = img.fileName,
                adjustments = ImageAdjustments(),
                resizeParams = ResizeParams(widthPx = img.width, heightPx = img.height)
            )
        }
    }

    fun updateFileName(name: String) {
        _uiState.update { it.copy(newFileNameInput = name) }
    }

    fun applyRename() {
        val current = _uiState.value.activeImage ?: return
        val newName = _uiState.value.newFileNameInput.trim()
        if (newName.isBlank()) return
        _uiState.update { state ->
            val updated = state.images.map { if (it.id == current.id) it.copy(fileName = newName, title = newName.substringBeforeLast('.')) else it }
            state.copy(
                images = updated,
                statusBanner = "Successfully renamed to '$newName'",
                outputPreviewTitle = "Rename Output Preview",
                outputPreviewDetails = "New File Name: $newName\nFormat: ${current.format}\nStatus: Ready for Export",
                showOutputPreviewDialog = true
            )
        }
    }

    fun updateAdjustments(transform: (ImageAdjustments) -> ImageAdjustments) {
        _uiState.update { it.copy(adjustments = transform(it.adjustments)) }
    }

    fun resetAdjustments() {
        _uiState.update { it.copy(adjustments = ImageAdjustments()) }
    }

    fun updateResizeParams(transform: (ResizeParams) -> ResizeParams) {
        _uiState.update { it.copy(resizeParams = transform(it.resizeParams)) }
    }

    fun setCompressionQuality(quality: Int) {
        _uiState.update { it.copy(compressionQuality = quality) }
    }

    fun setExportFormat(format: ExportFormat) {
        _uiState.update { it.copy(targetExportFormat = format) }
    }

    fun performRotate() {
        _uiState.update {
            val curr = it.adjustments
            val newRot = (curr.rotationDegrees + 90) % 360
            it.copy(
                adjustments = curr.copy(rotationDegrees = newRot),
                statusBanner = "Rotated to $newRot°",
                outputPreviewTitle = "Rotation Preview ($newRot°)",
                outputPreviewDetails = "Orientation updated successfully.\nDimensions: Unchanged",
                showOutputPreviewDialog = true
            )
        }
    }

    fun performFlipHorizontal() {
        _uiState.update {
            val curr = it.adjustments
            it.copy(adjustments = curr.copy(isFlippedHorizontal = !curr.isFlippedHorizontal), statusBanner = "Flipped horizontally")
        }
    }

    fun performFlipVertical() {
        _uiState.update {
            val curr = it.adjustments
            it.copy(adjustments = curr.copy(isFlippedVertical = !curr.isFlippedVertical), statusBanner = "Flipped vertically")
        }
    }

    fun applyResizeAndSave() {
        val current = _uiState.value.activeImage ?: return
        val rp = _uiState.value.resizeParams
        _uiState.update { state ->
            val updated = state.images.map {
                if (it.id == current.id) {
                    it.copy(width = rp.widthPx, height = rp.heightPx, dpi = rp.dpi, dateModified = System.currentTimeMillis())
                } else it
            }
            state.copy(
                images = updated,
                statusBanner = "Resized to ${rp.widthPx} × ${rp.heightPx} px @ ${rp.dpi} DPI",
                outputPreviewTitle = "Resize & DPI Output Preview",
                outputPreviewDetails = "Target Resolution: ${rp.widthPx} × ${rp.heightPx} px\nPrint Density: ${rp.dpi} DPI\nAspect Ratio: Locked\nEstimated Quality: Lossless Re-sample",
                showOutputPreviewDialog = true
            )
        }
    }

    fun applyCompressionAndConversion() {
        val current = _uiState.value.activeImage ?: return
        val fmt = _uiState.value.targetExportFormat
        val q = _uiState.value.compressionQuality
        val estSizeKb = (current.fileSizeBytes * q / 100) / 1024
        _uiState.update { state ->
            val updated = state.images.map {
                if (it.id == current.id) it.copy(format = fmt.name) else it
            }
            state.copy(
                images = updated,
                statusBanner = "Converted to ${fmt.name} (${q}% quality)",
                outputPreviewTitle = "Conversion & Compression Preview",
                outputPreviewDetails = "Target Format: ${fmt.name}\nQuality Preset: $q%\nEstimated Output Size: ~$estSizeKb KB\nMIME Type: ${fmt.mimeType}\nStatus: Optimized successfully",
                showOutputPreviewDialog = true
            )
        }
    }

    fun generateCollage() {
        val count = _uiState.value.collageSelectedIds.size
        val layout = _uiState.value.collageLayout
        _uiState.update {
            it.copy(
                statusBanner = "Generated $layout collage with $count images",
                outputPreviewTitle = "Collage Layout Preview ($layout)",
                outputPreviewDetails = "Selected Images: $count\nLayout Grid: $layout\nCanvas Resolution: 2400 × 2400 px (300 DPI)\nStatus: Rendered successfully",
                showOutputPreviewDialog = true
            )
        }
    }

    fun importCustomImage(uriString: String, fileName: String, context: Context) {
        val newItem = ImageItem(
            id = "img_${UUID.randomUUID().toString().take(6)}",
            title = fileName.substringBeforeLast('.'),
            fileName = fileName,
            uriOrDrawableRes = uriString,
            fileSizeBytes = (100000..800000).random().toLong(),
            width = 1920,
            height = 1080,
            format = fileName.substringAfterLast('.', "jpg").uppercase(),
            extractedText = "MATHGATE OCR Extracted Text from $fileName:\nSample technical content, annotations, and parameters successfully parsed."
        )
        _uiState.update {
            it.copy(
                images = listOf(newItem) + it.images,
                activeImageId = newItem.id,
                newFileNameInput = newItem.fileName,
                activeTab = ImageEditTab.VIEWER,
                statusBanner = "Imported '$fileName'",
                outputPreviewTitle = "Image Import Preview",
                outputPreviewDetails = "File: $fileName\nDimensions: 1920 × 1080 px\nFormat: ${newItem.format}\nStatus: Ready for editing",
                showOutputPreviewDialog = true
            )
        }
    }

    fun toggleCollageSelection(id: String) {
        _uiState.update { state ->
            val set = state.collageSelectedIds.toMutableSet()
            if (set.contains(id)) set.remove(id) else set.add(id)
            state.copy(collageSelectedIds = set)
        }
    }

    fun setCollageLayout(layout: String) {
        _uiState.update { it.copy(collageLayout = layout, statusBanner = "Collage layout set to $layout") }
    }

    fun dismissOutputPreview() {
        _uiState.update { it.copy(showOutputPreviewDialog = false) }
    }

    fun clearStatusBanner() {
        _uiState.update { it.copy(statusBanner = null) }
    }
}
