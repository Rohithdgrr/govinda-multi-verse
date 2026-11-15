# Setup & Development Guide

This guide explains how to configure, build, test, and run **GOVINDA** locally.

---

## 1. Prerequisites

- **JDK**: Java 17 (Temurin or OpenJDK 17; `compileOptions` targets Java 11 bytecode)
- **Android SDK**:
  - `compileSdk`: 36 (minor API level 1)
  - `targetSdk`: 36
  - `minSdk`: 24 (Android 7.0 Nougat)
- **Gradle**: Pre-configured system `gradle` (AGP `9.1.1`, Kotlin `2.2.10`, KSP `2.3.5`)
- **Android Studio**: Ladybug / Meerkat or newer (optional if building via CLI)

> **Important**: GOVINDA operates **100% offline** with no external API keys required. Do not commit `local.properties`, `.env`, or release keystores.

---

## 2. Environment Configuration

1. Verify `ANDROID_SDK_ROOT` (or `ANDROID_HOME`) points to your Android SDK installation.
2. In containerized CI/cloud environments, use the `gradle` command directly (not `./gradlew` if the wrapper is absent).
3. Ensure `.env` stays untracked (`.env.example` exists only as the template placeholder for the Secrets plugin).

---

## 3. Common Build & Test Commands

```bash
# 1. Assemble Debug APK
gradle :app:assembleDebug

# 2. Run all JVM unit tests + Robolectric Compose UI tests (no emulator needed)
gradle :app:testDebugUnitTest

# 3. Run Android Lint verification
gradle :app:lintDebug

# 4. Run Release APK build (requires keystore env vars if signing release)
gradle :app:assembleRelease
```

---

## 4. Enabling App Quiz Lock on a Physical Device / Emulator

1. Install and open **GOVINDA**.
2. Tap the **Settings** icon in the top navigation bar.
3. Under **App Quiz Lock**, toggle **Lock Selected Applications** ON and tap **Select Apps** to pick target apps.
4. Tap **Open Accessibility Settings** (or inspect **Permissions & Access -> App Quiz Lock Accessibility**) and review the disclosure dialog.
5. Enable **GOVINDA App Quiz Lock** in Android OS Accessibility settings.
6. Launch any selected app fresh from the home screen launcher to verify the 1-question math gate appears and unlocks cleanly upon solving.
