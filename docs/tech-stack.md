# Tech Stack & Dependencies

GOVINDA is engineered strictly with modern, offline-first Android libraries managed via Gradle Version Catalog (`gradle/libs.versions.toml`).

---

## 1. Core Language & Build Toolchain

| Component | Version / Tool | Purpose |
| :--- | :--- | :--- |
| **Language** | Kotlin `2.2.10` | Null-safe domain logic, sealed interfaces, coroutines, Compose UI |
| **Android Gradle Plugin** | `9.1.1` | Build pipeline, R8/ProGuard config, resource shrinking |
| **KSP** | `2.3.5` | Fast compile-time Room annotation processing (replaces KAPT) |
| **Build Configuration** | Kotlin DSL (`.gradle.kts`) | Type-safe Gradle scripts with Version Catalog (`libs.versions.toml`) |

---

## 2. UI & Design System

| Library | Usage |
| :--- | :--- |
| **Jetpack Compose BOM** (`2024.09.00`) | Declarative UI toolkit across Challenge, Settings, and Personalization Studio |
| **Material 3** (`androidx-compose-material3`) | Dynamic color roles, accessible touch targets (`48.dp`), dialogs, switches, chips |
| **Material Icons Core & Extended** | Standard navigation, lock, tuning, help, legal, and studio icons |
| **Navigation Compose** (`2.8.9`) | Route management (`home`, `settings`, `personalization`) with motion scaling |
| **Activity Compose** (`1.10.1`) | `enableEdgeToEdge()`, system bar icon contrast adaptation |
| **Bundled Local Typography (`.ttf`)** | 20 offline Open-Source font families loaded from `res/font/` |

---

## 3. State Management, Storage & Concurrency

| Library | Usage |
| :--- | :--- |
| **Kotlin Coroutines & Flow** (`1.10.2`) | Asynchronous Room transactions, DataStore streaming, non-blocking generation |
| **Lifecycle ViewModel & Runtime Compose** (`2.8.7`) | `collectAsStateWithLifecycle()` reactive UI state binding |
| **Room Database** (`2.7.0`) | Local SQLite persistence for profiles, attempts, review queue, daily progress |
| **Preferences DataStore** (`1.1.7`) | Atomic key-value storage for adaptive parameters, quiz lock config, and JSON personalization state |
| **Constructor DI (`AppContainer`)** | Lightweight compile-time-safe dependency wiring without reflection overhead |

---

## 4. Testing & Quality Assurance

| Tool | Purpose |
| :--- | :--- |
| **JUnit 4** (`4.13.2`) | Deterministic generator property tests, `AnswerEvaluator`, `AdaptiveController`, `QuizLockSessionTracker` |
| **Robolectric** (`4.16.1`) | Headless JVM Android framework + Compose UI testing (`ExampleRobolectricTest`) without an emulator |
| **Compose UI Test (`ui-test-junit4`)** | Semantic node assertions (`testTag`), keypad interactions, Settings & Personalization navigation verification |
| **Kotlinx Coroutines Test** (`1.10.2`) | Deterministic coroutine test execution |
