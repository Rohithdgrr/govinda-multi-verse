# GOVINDA (MATHGATE) — Software Architecture

GOVINDA follows **Clean Architecture + MVVM** organized around strict unidirectional data flow, zero network dependencies, and constructor-based dependency injection via `AppContainer`.

---

## 1. Module & Package Layers

```
com.example
├── GovindaApplication.kt          # Initializes singleton AppContainer
├── MainActivity.kt                # Single-activity edge-to-edge host + quiz lock intent handler
├── core
│   ├── database/                  # NotesStudioEntities (9 tables) & NotesStudioDao
│   ├── designsystem/              # BlueprintCard, BlueprintBadge, BlueprintMetricTile, patterns
│   ├── document/                  # NoteDocumentModels (26 BlockTypes, 16 CodeLanguages, TableData, Templates)
│   ├── export/                    # NotesExportImportManager (Markdown, JSON, PDF, CSV, HTML, TXT, ZIP)
│   ├── latex/                     # LatexRendererEngine (offline LaTeX validator, fraction/matrix AST & Unicode formatter)
│   └── navigation/                # GovindaApp NavHost (home, settings, personalization, notes_studio, developer)
├── domain                         # Pure Kotlin business logic
│   ├── adaptive/                  # AdaptiveController (scoring, pacing, complexity tiers, warm-up)
│   ├── evaluator/                 # AnswerEvaluator (BigInteger, BigDecimal, Fraction normalization)
│   ├── generator/                 # QuestionGenerator + 30 generators (Arithmetic, Higher-Order, TrackSpecific, EngineeringBranches)
│   ├── model/                     # MathModels (MathCategory, Difficulty, QuestionStyle, UserPreferences)
│   ├── repository/                # MathLearningRepository & QuestionRepository interfaces
│   ├── selector/                  # TopicSelector (progressive unlock + weighted tier bucketing)
│   └── usecase/                   # GenerateNextQuestionUseCase, SubmitAnswerUseCase, Export/Reset
├── data                           # Offline persistence layer
│   ├── local/                     # GovindaDatabase (Room v3, 24 entities), DAOs, DatasetBankEntities
│   ├── preferences/               # UserPreferencesDataStore (Preferences DataStore)
│   └── repository/                # RoomMathLearningRepository & RoomQuestionRepository
├── di/                            # AppContainer manual dependency graph
├── feature
│   ├── challenge/                 # ChallengeScreen, ScienceGkMcqDeck, ChallengeComponents, ChallengeViewModel
│   ├── developer/                 # DeveloperSettings (DeveloperModeSettingsCard) & DeveloperScreen workbench
│   ├── notes/                     # NotesStudioScreen, NotesStudioViewModel, NotesStudioRepository
│   │   ├── blocks/                # NoteBlockComponents (26 interactive block views)
│   │   └── features/              # SlashCommandPalette, NoteOutlineAndInspectorBar, NoteFlashcardsSheet, LatexAndGraphTools
│   ├── settings/                  # SettingsScreen, SettingsPermissionsAndInfo, SettingsViewModel
│   └── survey/                    # LearnerSurveyDialog, LearnerSurveyEngine, LearnerSurveySpec
├── lock/                          # GovindaQuizLockService, QuizLockSessionTracker, InstalledAppsProvider
└── personalization/               # ThemeRegistry, FontRegistry, IconRegistry, Avatar, Layout, DataStore
```

---

## 2. Clean Architecture Data Flow

```
  [ Jetpack Compose UI ]
  (ChallengeScreen / SettingsScreen / PersonalizationScreen / NotesStudioScreen / DeveloperScreen)
            │
            ▼  User Events
  [ ViewModel + StateFlow ]
  (ChallengeViewModel / SettingsViewModel / PersonalizationViewModel / NotesStudioViewModel)
            │
            ▼  Coroutine UseCases & Engines
  [ Domain & Document Engines ]
  (TopicSelector / AdaptiveController / QuestionGenerator / LatexRendererEngine / NotesExportImportManager)
            │
            ▼  Repository Interfaces
  [ Room v3 + Preferences DataStore ]
  (GovindaDatabase @Transaction + govinda_user_preferences + govinda_appearance)
```

---

## 3. Local Database Schema (Room v3 — `GovindaDatabase`)

### Adaptive Practice & Dataset Bank Tables (15 Entities)
| Table | Purpose |
| :--- | :--- |
| `skill_profiles` | Per-category `skillScore`, `accuracy`, `averageResponseTimeMs`, `currentDifficulty`, streaks |
| `question_attempts` | Timestamped attempt log with prompt, expected vs submitted answers, correctness, and duration |
| `generation_history` | Recent prompt fingerprints (last 100 trimmed) to prevent repetitive questions |
| `review_queue` | Spaced-repetition recovery items queued when a question is missed |
| `daily_progress` | Aggregated daily solved count, correct count, XP, and active practice duration |
| `bookmarked_questions` | Local question bookmark storage |
| `subjects`, `topics` | Hierarchical curriculum taxonomy for the offline question bank |
| `dataset_questions`, `question_options` | Normalized offline dataset questions and 4-option MCQ choices (`PACK_CORE_PERMISSIVE_V1`) |
| `question_sources`, `question_packs`, `question_pack_versions` | Provenance, license (`MIT`, `Apache-2.0`, `CC-BY-4.0`), and content pack versioning |
| `user_skill_profiles`, `saved_questions` | Extended per-topic mastery and saved dataset items |

### Notes Studio Tables (9 Entities)
| Table | Purpose |
| :--- | :--- |
| `notes` | Document metadata (`id`, `title`, `folder_id`, `collection_name`, `subject_tag`, pin/favourite/archive/trash flags) |
| `note_blocks` | Ordered block records (`type`, `position`, `content`, `metadata`) across all 26 block types |
| `note_folders` | Hierarchical folders (`parent_folder_id`, `icon_name`) |
| `note_tags`, `note_tag_cross_ref` | Many-to-many `#tag` organization |
| `note_attachments` | Local image/media attachment references (`uri_string`, `caption`, `alt_text`) |
| `note_links` | Bidirectional `[[note_id|Label]]` graph edges for incoming backlinks and outgoing links |
| `note_revisions` | Point-in-time JSON snapshots for full document version history & restore |
| `note_templates` | Built-in and custom structured templates for Academic, Engineering, Math, and Personal notes |
