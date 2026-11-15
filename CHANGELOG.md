# Changelog

All notable changes to **GOVINDA (MATHGATE)** are documented in this file.

The format loosely follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.3.0] - 2026-11-13

### Added
- **Developer Mode & Dynamic Navigation (`Settings → Appearance / Application Modes`)**:
  - Persistent `developerModeEnabled` DataStore flag with instant UI updates and zero restart required.
  - Animated bottom navigation bar (**Home • Notes • Developer • Modes**) that appears when Developer Mode is enabled and hides non-destructively when disabled.
- **Notion-Inspired Notes Studio & 26-Block Document Editor**:
  - 9 Room entities (`NoteEntity`, `NoteBlockEntity`, `FolderEntity`, `TagEntity`, `NoteTagCrossRef`, `NoteAttachmentEntity`, `NoteLinkEntity`, `NoteRevisionEntity`, `NoteTemplateEntity`) and reactive `NotesStudioDao`.
  - Support for all 26 block types including Headings 1–3, Paragraph, Lists, Interactive Checklists, Collapsible Toggles, Quotes, Callouts, Dividers, Editable Tables, Images (zero-permission Android Photo Picker), LaTeX Equations, Inline Math, Code Blocks (16 languages), Bookmarks (`[[note_id|Label]]` with bidirectional backlinks), Mermaid Diagrams, and Engineering Schematics.
  - Power-user tools: `/ COMMANDS` Slash Palette, `∑ LATEX PAD` symbol builder, `≡ OUTLINE (TOC)`, `🔍 FIND & REPLACE`, `⚡ FLASHCARDS` active recall deck, `◉ KNOWLEDGE GRAPH` topology canvas, 1.1 Heading Auto-Numbering, Note Duplication, 30-step Undo/Redo, Revision History snapshots, and portable **Markdown, JSON, PDF, CSV, HTML, TXT, and ZIP** export/import.
- **Developer Workbench**:
  - Interactive Engineering & Math Formula Library, Code Snippets Library (Kotlin, Verilog, Python), Technical Document Templates launcher, and Local Room & Content Pack Storage Inspector.

## [1.2.0] - 2026-11-13

### Added
- **MATHGATE Educational Dataset Pipeline & Room Integration (`mathgate-data/`)**:
  - Automated Python pipeline (`download_huggingface.py`, `normalize_and_classify.py`, `validate_dataset.py`, `build_sqlite.py`, `run_pipeline.py`) producing 148 verified commercial-safe records across QASC, GSM8K, SciBench, OpenBookQA, and JEE/GATE engineering branches (`PACK_CORE_PERMISSIVE_V1`).
  - Room integration (`DatasetQuestionEntity`, `QuestionOptionEntity`, `QuestionSourceEntity`, `QuestionPackEntity`, `QuestionBankDao`, `RoomQuestionRepository`) and 6 new generators (`SchoolScienceGenerator`, `EceEngineeringGenerator`, `EeeEngineeringGenerator`, `CseEngineeringGenerator`, `MechanicalEngineeringGenerator`, `CivilEngineeringGenerator`).
  - Tappable 4-option MCQ deck (`ScienceGkMcqDeck`) specifically for `SCHOOL_SCIENCE` and `GENERAL_KNOWLEDGE` while preserving keypad-only input for all math and engineering tracks.

## [1.1.0] - 2026-11-13

### Added
- **Mandatory Single-Tap Permission Onboarding & Hierarchical Learner Profile Survey**:
  - Non-dismissible first-launch permission onboarding (`MandatoryPermissionsDialog`) and hierarchical 3-step `LearnerSurveyDialog` with `By Default (Adaptive)` and `Skip` options.
  - 20-second wrong-answer retry cooldown timer that keeps the same question visible and hides the solution until answered accurately.

## [1.0.0] - 2026-11-13

### Added
- **Multi-Parameter Adaptive Math Engine, App Quiz Lock & Offline Personalization Studio**:
  - Initial offline math engine, `GovindaQuizLockService`, 26 themes, 20 bundled fonts, and Room/DataStore persistence.
