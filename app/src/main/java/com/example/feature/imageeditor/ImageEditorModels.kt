package com.example.feature.imageeditor

data class ImageItem(
    val id: String,
    val title: String,
    val fileName: String,
    val uriOrDrawableRes: String,
    val fileSizeBytes: Long,
    val width: Int,
    val height: Int,
    val format: String,
    val dpi: Int = 300,
    val dateModified: Long = System.currentTimeMillis(),
    val extractedText: String = "",
    val cameraMake: String = "MATHGATE Studio Cam",
    val colorSpace: String = "sRGB"
)

enum class ImageEditTab(val title: String, val badge: String) {
    GALLERY("Gallery", "🖼"),
    VIEWER("Viewer & Info", "👁"),
    ADJUST("Adjust & Enhance", "✨"),
    RESIZE("Resize & DPI", "📐"),
    TRANSFORM("Crop & Rotate", "✂️"),
    CONVERT("Convert & Compress", "⚡"),
    OCR("Text Extract", "🔤"),
    COLLAGE("Collage Maker", "⊞")
}

data class ImageAdjustments(
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val filterType: ImageFilterType = ImageFilterType.NORMAL,
    val rotationDegrees: Int = 0,
    val isFlippedHorizontal: Boolean = false,
    val isFlippedVertical: Boolean = false,
    val cropAspect: String = "Free"
)

enum class ImageFilterType(val displayName: String) {
    NORMAL("Normal"),
    GRAYSCALE("Grayscale"),
    SEPIA("Sepia"),
    INVERT("Inverted"),
    HIGH_CONTRAST("High Contrast"),
    WARM("Warm Tone"),
    COOL("Cool Tone")
}

data class ResizeParams(
    val widthPx: Int = 1920,
    val heightPx: Int = 1080,
    val unit: ResizeUnit = ResizeUnit.PIXEL,
    val dpi: Int = 300,
    val maintainAspectRatio: BoolLock = BoolLock.LOCKED
)

enum class ResizeUnit(val label: String) {
    PIXEL("Pixels (px)"),
    CM("Centimeters (cm)"),
    MM("Millimeters (mm)"),
    INCH("Inches (in)")
}

enum class BoolLock { LOCKED, UNLOCKED }

enum class ExportFormat(val extension: String, val mimeType: String) {
    JPEG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
    WEBP("webp", "image/webp"),
    PDF("pdf", "application/pdf"),
    BMP("bmp", "image/bmp")
}
