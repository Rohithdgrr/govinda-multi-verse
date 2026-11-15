# Features List

## 1. 30-Category Math, Science, Engineering & Exam Engine
- **Foundational & Intermediate Math (16 Categories)**:
  - Addition, Subtraction, Multiplication, Division, Percentages, Fractions, Decimals, Powers & Roots, Mixed Operations, Squares, Cubes, Roots (`√`/`∛`), BODMAS, Simple Interest, Speed & Distance, Averages, Statistics, Number Systems, Ratio & Proportion.
- **Higher-Order & Competitive Tracks (8 Categories)**:
  - Linear & Quadratic Equations, Geometry & Angles, Trigonometry, Mensuration (2D/3D), Permutations & Probability, Progression & Series (AP/GP), Matrices & Determinants, Vectors & 3D, Calculus (Derivatives & Integrals), Complex Numbers, Coding & CS Logic, General Knowledge, Competitive & Gov Exam Quant.
- **School Science & 5 Engineering Branches (6 Categories)**:
  - School Science (`SCHOOL_SCIENCE`), Electronics & Communication (`ECE_ENGINEERING`), Electrical & Electronics (`EEE_ENGINEERING`), Computer Science (`CSE_ENGINEERING`), Mechanical (`MECHANICAL_ENGINEERING`), and Civil (`CIVIL_ENGINEERING`).
- **20-Second Wrong-Answer Cooldown**:
  - Wrong submissions never reveal the solution or skip the question; an interactive 20-second retry countdown keeps the question visible until the learner solves it accurately.
- **Hybrid Answer Entry**:
  - Tactile 4×4 numeric keypad for all mathematical and engineering tracks, plus tappable 4-option MCQ cards specifically for `SCHOOL_SCIENCE` and `GENERAL_KNOWLEDGE`.

---

## 2. Hierarchical Learner Profile Survey
- Triggered automatically after single-tap permission onboarding (and accessible anytime via `Settings → Learner Profile`).
- Supports 1-tap **By Default (Adaptive)** or **Skip**, or a 3-step hierarchical flow (**Root Track → Specialization Sub-Track → Pacing**) that tailors `enabledCategories` and difficulty bounds.

---

## 3. MATHGATE Educational Dataset Pipeline (`mathgate-data/`)
- Python data engineering pipeline (`download_huggingface.py`, `normalize_and_classify.py`, `validate_dataset.py`, `build_sqlite.py`, `run_pipeline.py`) with strict permissive commercial-safe license filtering (`MIT`, `Apache-2.0`, `CC-BY-4.0`).
- Generates audit reports in `mathgate-data/reports/`, normalized JSONL files, and `mathgate_question_bank.db`, integrated on-device via `RoomQuestionRepository`.

---

## 4. Developer Mode & Notion-Inspired Notes Studio
- **Developer Mode Switch (`Settings → Appearance / Application Modes`)**:
  - Enables/disables Developer Mode instantaneously without app restart, revealing the animated bottom navigation bar (**Home • Notes • Developer • Modes**).
  - Turning Developer Mode off hides developer destinations without deleting any notes, blocks, or revisions.
- **26-Block Document Editor**:
  - Supports `HEADING_1..3`, `PARAGRAPH`, `BULLETED_LIST`, `NUMBERED_LIST`, `CHECKLIST`, `TOGGLE_SECTION`, `QUOTE`, `CALLOUT`, `DIVIDER`, `TABLE`, `IMAGE`, `IMAGE_GALLERY`, `LATEX_BLOCK`, `INLINE_MATH`, `CODE_BLOCK` (16 languages), `INLINE_CODE`, `HYPERLINK`, `BOOKMARK`, `EMBEDDED_WEBPAGE`, `EMBEDDED_VIDEO`, `FILE_ATTACHMENT`, `MERMAID_DIAGRAM`, `ENGINEERING_SCHEMATIC`, and `AUDIO_ATTACHMENT`.
- **Power Tools**:
  - **`/ COMMANDS` Slash Palette**: Instant insertion of LaTeX macros, digital logic truth tables, RTL Verilog modules, schematics, heading auto-numbering (`1.1`), and full note duplication.
  - **`∑ LATEX PAD` & Offline LaTeX Engine**: Fractions (`\frac`), 2×2 matrices (`bmatrix`), determinants (`vmatrix`), integrals, summations, Greek symbols, equation numbering, and syntax validation.
  - **`≡ OUTLINE (TOC)` & `🔍 FIND & REPLACE`**: Hierarchical Table of Contents and batch Find & Replace across all blocks in the active note.
  - **`⚡ FLASHCARDS` & `◉ KNOWLEDGE GRAPH`**: Auto-extracted active recall flashcards from note headings/toggles/equations and a radial Knowledge Graph canvas for `[[linked_notes]]` and backlinks.
  - **Portable Export & Import**: Offline export/import for **Markdown (`.md`), JSON (`.json`), PDF (`.pdf`), CSV (`.csv`), HTML (`.html`), Plain Text (`.txt`), and ZIP (`.zip`)**.
- **Developer Workbench**:
  - Engineering & Math Formula Library, Code Snippets Library, Technical Templates launcher, and Local Room & Content Pack Inspector.
