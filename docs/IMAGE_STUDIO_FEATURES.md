# MATHGATE Image Studio & Editor — Feature Documentation

Welcome to the **MATHGATE Image Studio & Editor**, a professional-grade image viewing, editing, conversion, and processing suite built natively for Android using Jetpack Compose and Material Design 3.

---

## 📋 Table of Contents
1. [Overview](#overview)
2. [Gallery Library & Import](#gallery-library--import)
3. [Viewer & EXIF Metadata](#viewer--exif-metadata)
4. [Adjustments & Enhancements](#adjustments--enhancements)
5. [Resize & DPI Processing](#resize--dpi-processing)
6. [Crop, Rotate & Transform](#crop-rotate--transform)
7. [Rename, Convert & Compress](#rename-convert--compress)
8. [OCR Text Extraction](#ocr-text-extraction)
9. [Collage Creator](#collage-creator)
10. [Live Output Previews](#live-output-previews)

---

## 1. Overview
The Image Studio is seamlessly integrated into the application's bottom navigation bar and navigation rail as the **Images** tab (`PhotosLibrary` icon). It allows users to manage, inspect, enhance, convert, and process technical diagrams, schematics, whiteboard equations, and photographs without leaving the app.

---

## 2. Gallery Library & Import
- **Built-in Sample Assets**: Preloaded with high-resolution engineering and academic sample images (*RC Low-Pass Filter Schematic*, *Calculus Integral Proof*, and *Oscilloscope Waveform Capture*).
- **Custom Image Import**: 1-tap **Add Image** button powered by the zero-permission Android Photo Picker (`ActivityResultContracts.PickVisualMedia`).
- **Card Grid View**: Responsive adaptive grid displaying image thumbnails, titles, dimensions (`Width × Height`), and file format tags.

---

## 3. Viewer & EXIF Metadata
- **High-Fidelity Preview**: Full-card image viewer with zoom/pan support.
- **Action Toolbar**: Quick access **Download** and **Open With** (Share Intent) actions.
- **EXIF & Properties Inspector**: Detailed metadata display including:
  - File Name & File Size (KB/MB)
  - Exact Resolution in Pixels
  - Format (JPEG, PNG, WEBP, etc.)
  - Print Resolution (72, 150, 300, 600 DPI)
  - Color Space (`sRGB`, `Display P3`)
  - Camera Make & Date Modified

---

## 4. Adjustments & Enhancements
- **One-Tap Professional Filters**:
  - *Normal*, *Grayscale*, *Sepia*, *Inverted*, *High Contrast*, *Warm Tone*, and *Cool Tone*.
- **Precise Sliders**:
  - **Brightness**: Adjust exposure from `-100%` to `+100%`.
  - **Contrast**: Scale contrast from `0.5x` to `2.0x`.
- **Reset All**: 1-tap reset reverting all adjustments to default.

---

## 5. Resize & DPI Processing
- **Pixel Dimensions**: Custom width and height input fields.
- **Print Resolution (DPI)**: Standard print density presets (`72 DPI` web, `150 DPI` draft, `300 DPI` high quality, `600 DPI` archival).
- **Live Output Preview**: Instant preview modal confirming target resolution and print density upon applying.

---

## 6. Crop, Rotate & Transform
- **Rotation**: 90° clockwise incremental rotation (`0°`, `90°`, `180°`, `270°`).
- **Flipping**: Horizontal and vertical mirror flipping.
- **Aspect Ratio Presets**: Quick selection for *Free*, *1:1 Square*, *4:3 Standard*, and *16:9 Widescreen*.

---

## 7. Rename, Convert & Compress
- **File Renaming**: Editable file name dialog with format retention.
- **Format Conversion**: Convert images across **JPEG, PNG, WEBP, PDF, and BMP**.
- **Compression Quality Slider**: Adjust compression from `10%` to `100%` with live calculated estimated output file size in KB.

---

## 8. OCR Text Extraction
- **Text Parsing**: Automatically extracts text, formula notation, and parameter values from technical diagrams and notes.
- **One-Tap Copy**: Instant clipboard copy button (`Copy`) for sharing extracted text into Notion Notes or WhatsApp Chat.

---

## 9. Collage Creator
- **Multi-Image Selection**: Checkbox selection of library images.
- **Layout Grids**: Choose between *2x2 Grid*, *Side by Side*, and *3-Panel Split*.
- **Collage Generation**: Renders professional multi-image compositions with 1-tap output preview.

---

## 10. Live Output Previews
- Whenever you apply any tool (Resize, Compression, Conversion, Rotation, Rename, or Collage), an interactive **Output Preview Dialog** instantly displays:
  - Visual thumbnail preview of the processed image.
  - Detailed output parameters and size estimates.
  - Success confirmation before finalizing.
