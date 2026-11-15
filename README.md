# GOVINDA (MATHGATE) — Offline Adaptive Math, Engineering & Notes Studio Engine

**GOVINDA** is a 100% offline Android application built with Kotlin and Jetpack Compose that combines a multi-parameter adaptive math & engineering practice engine, an App Quiz Lock (`GovindaQuizLockService`), a curated offline dataset pipeline (`mathgate-data/`), and a Notion/Obsidian-inspired **Developer Mode & Notes Studio**.

- **Application ID**: `com.aistudio.govinda.mathkx`
- **Minimum SDK**: Android 7.0 (API 24) · **Target / Compile SDK**: API 36
- **Privacy Guarantee**: Zero `INTERNET` permission, zero `QUERY_ALL_PACKAGES` permission, zero analytics, zero cloud APIs, and zero accounts. All problem generation, grading, dataset banks, notes, LaTeX rendering, and customizations stay 100% on-device.

---

## Key Capabilities

1. **30 Adaptive Math, Science, Engineering & Exam Categories**:
   - Covers Foundational Arithmetic, Squares/Cubes/Roots, BODMAS, Simple Interest, Speed & Distance, Averages, Statistics, Number Systems, Ratio & Proportion, Linear/Quadratic Equations, Geometry, Trigonometry, Mensuration, Permutations & Probability, Progression & Series, Matrices & Determinants, Vectors & 3D, Calculus, Complex Numbers, Coding & CS Logic, General Knowledge, Competitive/Gov Exam Quant, School Science, and 5 Engineering Branches (**ECE, EEE, CSE, Mechanical, Civil**).
2. **Hierarchical Learner Profile Survey & Retry Cooldown**:
   - Post-permission learner survey adapts topic pools to the learner's background (Daily Life, Class 1–12, Engineering, Coding, GK, Competitive/Gov Exams) or 1-tap `By Default / Skip`.
   - Wrong answers trigger a **20-second retry cooldown** while keeping the current question visible and hiding the solution until solved accurately.
   - Keypad-first answer entry for math/engineering, with tappable 4-option MCQ decks enabled specifically for `SCHOOL_SCIENCE` and `GENERAL_KNOWLEDGE`.
3. **MATHGATE Educational Dataset Pipeline & Room Question Bank (`mathgate-data/`)**:
   - End-to-end Python acquisition, normalization, taxonomy classification, and validation pipeline (`PACK_CORE_PERMISSIVE_V1` with MIT/Apache-2.0/CC-BY-4.0 licensing) integrated with Room (`DatasetQuestionEntity`, `QuestionOptionEntity`, `QuestionBankDao`, `RoomQuestionRepository`).
4. **Developer Mode & Notion-Inspired Notes Studio**:
   - Toggle **Developer Mode** in `Settings → Appearance / Application Modes` to dynamically reveal the **Notes Studio** and **Developer Workbench** navigation bar without restarting.
   - **26-Block Rich Document Editor**: Headings 1–3, Paragraph, Bulleted/Numbered Lists, Checklists, Toggle Sections, Quotes, Callouts, Dividers, Editable Tables (row/col add/remove, sorting, CSV/Markdown copy), LaTeX Equations, Inline Math, Code Blocks (16 languages with keyword highlighting & line numbers), Images (Android Photo Picker), Bookmarks (`[[note_id|Label]]` with bidirectional backlinks), Mermaid Diagrams, and Canvas Engineering Schematics.
   - **Power Tools**: `/ COMMANDS` Slash Palette, `∑ LATEX PAD` symbol & formula builder, `≡ OUTLINE (TOC)` drawer, `🔍 FIND & REPLACE`, `⚡ FLASHCARDS` active recall study deck, `◉ KNOWLEDGE GRAPH` topology canvas, 30-step Undo/Redo, Revision History snapshots, and portable **Markdown, JSON, PDF, CSV, HTML, TXT, and ZIP** export/import.
5. **App Quiz Lock & Offline Personalization Studio**:
   - Single-tap mandatory permission onboarding and fresh-launch App Quiz Lock using scoped `<queries>` discovery.
   - 26 built-in themes (12 Light / 14 Dark), custom color studio, 20 bundled offline fonts, 8 icon families, procedural avatars, and layout presets.

---

## Documentation Index

| Document | Description |
| :--- | :--- |
| [`docs/architecture.md`](docs/architecture.md) | Clean Architecture + MVVM layers, Room v3 schema (24 entities), DataStore, dataset pipeline, and DI |
| [`docs/setup.md`](docs/setup.md) | Prerequisites, environment setup, Gradle build, dataset pipeline & Robolectric test commands |
| [`docs/tech-stack.md`](docs/tech-stack.md) | Kotlin, Jetpack Compose, Material 3, Room, DataStore, and testing dependencies |
| [`docs/features-list.md`](docs/features-list.md) | Complete breakdown of math/engineering tracks, dataset bank, Notes Studio, and Developer Mode |
| [`docs/working.md`](docs/working.md) | Adaptive formulas, 20s cooldown state machine, LaTeX AST parser, and evaluation rules |
| [`docs/workflow.md`](docs/workflow.md) | End-to-end user journeys: onboarding survey, practice, quiz lock, and Notes Studio workflows |
| [`docs/ui/ux.md`](docs/ui/ux.md) | Blueprint-inspired design tokens, white surface hierarchy, responsive layouts, and accessibility |
| [`docs/problems-solutions.md`](docs/problems-solutions.md) | Engineering challenges resolved (precision grading, fresh-launch tracking, offline LaTeX, Room v3) |
| [`CHANGELOG.md`](CHANGELOG.md) | Version history and release notes |
| [`LICENSE`](LICENSE) | MIT Open-Source License |

---

## Quick Verification Commands

```bash
# Run the MATHGATE dataset acquisition, validation & SQLite build pipeline
python3 mathgate-data/scripts/run_pipeline.py

# Run unit & Robolectric UI tests (no emulator required)
gradle :app:testDebugUnitTest

# Build debug APK
gradle :app:assembleDebug
```
