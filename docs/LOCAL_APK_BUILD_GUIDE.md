# Local APK Build, Debugging & Troubleshooting Guide

Complete reference for building, signing, installing, debugging, and troubleshooting **GOVINDA** (`com.aistudio.govinda.mathkx`) APKs on your local machine.

---

## Table of Contents

1. [Project Build Specifications](#1-project-build-specifications)
2. [Prerequisites & Environment Setup](#2-prerequisites--environment-setup)
3. [Pre-Build Configuration (Keystores, SDK, Secrets)](#3-pre-build-configuration-keystores-sdk-secrets)
4. [Method 1: Build APK via Command Line (Gradle CLI)](#4-method-1-build-apk-via-command-line-gradle-cli)
5. [Method 2: Build APK via Android Studio (GUI)](#5-method-2-build-apk-via-android-studio-gui)
6. [Method 3: Generate & Sign a Release APK](#6-method-3-generate--sign-a-release-apk)
7. [Installing & Running on Device / Emulator (`adb`)](#7-installing--running-on-device--emulator-adb)
8. [Debugging & Logcat Diagnostics](#8-debugging--logcat-diagnostics)
9. [Running Local Tests & Lint Checks](#9-running-local-tests--lint-checks)
10. [Troubleshooting Common Issues](#10-troubleshooting-common-issues)

---

## 1. Project Build Specifications

Values defined in `app/build.gradle.kts`, `gradle/libs.versions.toml`, and `gradle.properties`:

| Setting | Value |
| :--- | :--- |
| **Application ID** | `com.aistudio.govinda.mathkx` |
| **Namespace** | `com.example` |
| **Compile SDK** | `36` (`minorApiLevel = 1`) |
| **Target SDK** | `36` |
| **Min SDK** | `24` (Android 7.0 Nougat) |
| **AGP Version** | `9.1.1` |
| **Kotlin Version** | `2.2.10` |
| **KSP Version** | `2.3.5` |
| **JDK Compatibility** | Source & Target `Java 11` bytecode (JDK 17+ required to run Gradle/AGP 9) |
| **Gradle JVM Args** | `-Xmx4g -Dfile.encoding=UTF-8` |
| **Gradle Caching** | Build cache + Configuration cache enabled |

---

## 2. Prerequisites & Environment Setup

### Required Tools

1. **Java Development Kit (JDK 17 or JDK 21)**
   - AGP `9.x` requires **JDK 17+** to execute Gradle.
   - Verify:
     ```bash
     java -version
     javac -version
     ```
2. **Android SDK** (via Android Studio or Android Command-Line Tools)
   - Platform: `platforms;android-36` (and minor API 36.1 if prompted by SDK Manager)
   - Build-Tools: `build-tools;36.0.0` (or latest matching 36.x)
   - Platform-Tools (`adb`, `fastboot`)
3. **Gradle**
   - This repository uses the system `gradle` binary directly (note: `gradlew` wrapper scripts are not included in this workspace).
   - If you prefer using the wrapper locally outside this environment, you can run `gradle wrapper` once on your local machine—or simply invoke `gradle` directly.

### Environment Variables

#### macOS / Linux (`~/.zshrc` or `~/.bashrc`)
```bash
export JAVA_HOME="/path/to/jdk-17"
export ANDROID_SDK_ROOT="$HOME/Android/Sdk"        # macOS default: $HOME/Library/Android/sdk
export ANDROID_HOME="$ANDROID_SDK_ROOT"
export PATH="$JAVA_HOME/bin:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"
```

#### Windows (PowerShell)
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17"
$env:ANDROID_SDK_ROOT = "$env:LOCALAPPDATA\Android\Sdk"
$env:ANDROID_HOME = $env:ANDROID_SDK_ROOT
$env:Path += ";$env:JAVA_HOME\bin;$env:ANDROID_SDK_ROOT\platform-tools"
```

### Install Missing SDK Packages via `sdkmanager`
```bash
sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0"
```

---

## 3. Pre-Build Configuration (Keystores, SDK, Secrets)

### A. Android SDK Location (`local.properties` vs Environment Variable)
- **Recommended**: Set `ANDROID_SDK_ROOT` / `ANDROID_HOME` in your shell environment.
- **Alternative (local machine only)**: If opening the project in Android Studio on your personal computer without global env vars, Android Studio auto-generates `local.properties` with:
  ```properties
  sdk.dir=/Users/yourname/Library/Android/sdk
  ```
  > Never commit `local.properties` to version control (it is listed in `.gitignore`).

### B. Restore `debug.keystore` (If Missing)
`app/build.gradle.kts` signs `debug` builds using `${rootDir}/debug.keystore` (`storePassword = "android"`, `keyAlias = "androiddebugkey"`, `keyPassword = "android"`).

If `debug.keystore` is not present in the root directory, decode the repository's `debug.keystore.base64` file:

```bash
# Linux / macOS
base64 -d debug.keystore.base64 > debug.keystore

# macOS older base64 syntax (if -d fails)
base64 -D -i debug.keystore.base64 -o debug.keystore

# Windows (PowerShell)
[IO.File]::WriteAllBytes("debug.keystore", [Convert]::FromBase64String((Get-Content debug.keystore.base64 -Raw)))
```

### C. Secrets & Google Services
- `.env.example` provides placeholders for the Secrets Gradle Plugin (`secrets { ... }`). You do **not** need an `.env` file unless adding custom third-party keys.
- `googleServices.missing.passthrough=true` and `MissingGoogleServicesStrategy.WARN` allow building cleanly even if `app/google-services.json` is not configured.

---

## 4. Method 1: Build APK via Command Line (Gradle CLI)

Run all commands from the project root directory (where `settings.gradle.kts` lives).

### Step 1: Verify Gradle Sees the Project
```bash
gradle tasks --group="build"
```

### Step 2: Build the Debug APK
```bash
gradle :app:assembleDebug
```

**Output APK Path:**
```text
app/build/outputs/apk/debug/app-debug.apk
```

### Step 3: Build with Clean Cache (When Needed)
Use only when troubleshooting stale artifacts (this clears cached intermediates and takes longer):
```bash
gradle clean :app:assembleDebug
```

### Step 4: Verify the Generated APK
Inspect package ID, version, permissions, and signature:
```bash
# Check APK signing certificate & signature scheme (v1/v2/v3/v4)
$ANDROID_SDK_ROOT/build-tools/36.0.0/apksigner verify --verbose --print-certs app/build/outputs/apk/debug/app-debug.apk

# Inspect manifest metadata (package, minSdk, targetSdk, permissions)
$ANDROID_SDK_ROOT/build-tools/36.0.0/aapt2 dump badging app/build/outputs/apk/debug/app-debug.apk
```

### Useful Gradle CLI Flags

| Flag | Purpose |
| :--- | :--- |
| `--info` | Detailed build step logs (helpful to see which task failed) |
| `--stacktrace` | Print full Java/Kotlin stack trace on build failure |
| `--debug` | Verbose internal Gradle diagnostics |
| `--offline` | Build using locally cached dependencies without network access |
| `--no-configuration-cache` | Bypass Gradle configuration cache if a plugin misbehaves |
| `--rerun-tasks` | Force all tasks to re-execute without skipping up-to-date outputs |
| `--scan` | Generate a shareable Gradle Build Scan report |

Example diagnostic build:
```bash
gradle :app:assembleDebug --stacktrace --info
```

---

## 5. Method 2: Build APK via Android Studio (GUI)

1. Launch **Android Studio** (Ladybug / Meerkat or newer with AGP 9 support).
2. Click **Open** and select the project root folder (containing `settings.gradle.kts`).
3. Wait for **Gradle Sync** to finish.
   - If prompted for Gradle JDK: **Settings / Preferences → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK → select JDK 17+**.
4. Open the **Build Variants** tool window (`View → Tool Windows → Build Variants`) and confirm `:app` is set to `debug` (or `release`).
5. Generate the APK:
   - **Quick Build**: `Build → Build Bundle(s) / APK(s) → Build APK(s)`.
   - When finished, click **locate** in the notification toast to open `app/build/outputs/apk/debug/`.
6. Direct Run on Device/Emulator:
   - Select your target device/emulator in the top toolbar and click **Run** (`Shift + F10` / `Ctrl + R`).

---

## 6. Method 3: Generate & Sign a Release APK

In `app/build.gradle.kts`, the `release` signing config expects:
- `KEYSTORE_PATH` (defaults to `${rootDir}/my-upload-key.jks`)
- `STORE_PASSWORD`
- `KEY_PASSWORD`
- Fixed `keyAlias = "upload"`

### Step 1: Create an Upload Keystore (First Time Only)
Use `-alias upload` to match `app/build.gradle.kts`:
```bash
keytool -genkeypair -v \
  -keystore my-upload-key.jks \
  -alias upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```
> Keep `my-upload-key.jks` outside version control and back it up securely.

### Step 2: Export Signing Environment Variables & Build
```bash
export KEYSTORE_PATH="$(pwd)/my-upload-key.jks"
export STORE_PASSWORD="your_store_password"
export KEY_PASSWORD="your_key_password"

gradle :app:assembleRelease
```

**Windows (PowerShell):**
```powershell
$env:KEYSTORE_PATH = "$PWD\my-upload-key.jks"
$env:STORE_PASSWORD = "your_store_password"
$env:KEY_PASSWORD = "your_key_password"

gradle :app:assembleRelease
```

**Release APK Output Path:**
```text
app/build/outputs/apk/release/app-release.apk
```

### Building an Android App Bundle (`.aab`) for Play Store
```bash
gradle :app:bundleRelease
```
**Output Path:**
```text
app/build/outputs/bundle/release/app-release.aab
```

### Inspecting APK Size Breakdown
```bash
# Check file size
ls -lh app/build/outputs/apk/debug/app-debug.apk
ls -lh app/build/outputs/apk/release/app-release.apk
```
Or drag-and-drop the `.apk` into **Android Studio → Build → Analyze APK...** to inspect `classes.dex`, `res/`, `lib/`, and `AndroidManifest.xml`.

---

## 7. Installing & Running on Device / Emulator (`adb`)

### Step 1: Enable USB Debugging on Your Android Phone
1. Go to **Settings → About phone** and tap **Build number** 7 times to unlock Developer Options.
2. Go to **Settings → System → Developer options** and enable **USB debugging**.
3. Connect via USB and accept the **Allow USB debugging** RSA prompt on the phone.

### Step 2: Verify Device Connection
```bash
adb devices -l
```
Expected output shows `<device-id>    device`.

### Step 3: Install & Launch the APK
```bash
# Fresh install or replace keeping app data (-r = replace, -t = allow test packages)
adb install -r -t app/build/outputs/apk/debug/app-debug.apk

# Or build and install directly in one Gradle command:
gradle :app:installDebug

# Launch MainActivity from CLI
adb shell am start -n com.aistudio.govinda.mathkx/com.example.MainActivity
```

### Step 4: Uninstall / Clear App Data
```bash
# Uninstall completely
adb uninstall com.aistudio.govinda.mathkx

# Clear local Room DB & DataStore without uninstalling
adb shell pm clear com.aistudio.govinda.mathkx
```

### Wireless Debugging (Android 11+)
```bash
adb pair <IP>:<PAIRING_PORT>
adb connect <IP>:<CONNECT_PORT>
```

---

## 8. Debugging & Logcat Diagnostics

### Live Logcat Filtering
```bash
# Clear old logs and stream logs only for this app's PID
adb logcat -c
adb logcat --pid=$(adb shell pidof -s com.aistudio.govinda.mathkx)

# Filter only Errors and Fatal crashes (AndroidRuntime / System.err)
adb logcat -v time *:E

# Filter crashes & uncaught exceptions cleanly
adb logcat -v time AndroidRuntime:E *:S
```

### Save Logs to a File for Bug Reports
```bash
adb logcat -d -v threadtime > govinda_logcat.txt
```

### Debugging App Quiz Lock (Accessibility + Overlay)
If testing `GovindaQuizLockService` via ADB:
```bash
# Check if Accessibility service is active
adb shell settings get secure enabled_accessibility_services

# Check overlay permission ("Display over other apps")
adb shell appops get com.aistudio.govinda.mathkx SYSTEM_ALERT_WINDOW

# Simulate screen off / on to test lock session reset behavior
adb shell input keyevent 26
```

### Attach Android Studio Debugger Without Rebuilding
1. Open the project in Android Studio.
2. Click **Run → Attach Debugger to Android Process** (`Ctrl + Alt + F5`).
3. Choose `com.aistudio.govinda.mathkx` from the device process list and set breakpoints in Kotlin files.

---

## 9. Running Local Tests & Lint Checks

```bash
# Run JVM Unit Tests + Robolectric Compose UI Tests (no device/emulator needed)
gradle :app:testDebugUnitTest

# Run a single test class or method
gradle :app:testDebugUnitTest --tests "com.example.ExampleUnitTest"
gradle :app:testDebugUnitTest --tests "com.example.ExampleRobolectricTest"

# Run Android Lint checks
gradle :app:lintDebug
```

**Report Locations:**
- **Test HTML Report**: `app/build/reports/tests/testDebugUnitTest/index.html`
- **Lint HTML Report**: `app/build/reports/lint-results-debug.html`

---

## 10. Troubleshooting Common Issues

### 1. `SDK location not found`
- **Cause**: Neither `ANDROID_SDK_ROOT` / `ANDROID_HOME` nor `local.properties` is configured.
- **Fix**:
  ```bash
  export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
  export ANDROID_HOME="$ANDROID_SDK_ROOT"
  ```
  Or create `local.properties` locally with `sdk.dir=...` (do not commit it).

---

### 2. `debug.keystore` Not Found / `Keystore file '.../debug.keystore' not found for signing config 'debugConfig'`
- **Cause**: `debug.keystore` has not been decoded yet from `debug.keystore.base64`.
- **Fix**:
  ```bash
  base64 -d debug.keystore.base64 > debug.keystore
  ```

---

### 3. Release Build Fails: `Keystore was tampered with, or password was incorrect` / `my-upload-key.jks not found`
- **Cause**: Release `signingConfig` requires `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`, and alias `upload`.
- **Fix**: Verify all 3 environment variables are exported in the same terminal session before running `gradle :app:assembleRelease`. For unsigned local testing, always use `assembleDebug`.

---

### 4. `Unsupported class file major version` / Java Version Mismatch
- **Cause**: Gradle is running under JDK 8 or JDK 11 instead of JDK 17+.
- **Fix**: Set `JAVA_HOME` to JDK 17 (or 21), or pass it inline:
  ```bash
  gradle :app:assembleDebug -Dorg.gradle.java.home=/path/to/jdk-17
  ```

---

### 5. `Could not resolve dependencies` / Network Timeout
- **Cause**: Offline environment or corporate proxy / interrupted Gradle download.
- **Fix**:
  - If dependencies were already downloaded once, build offline:
    ```bash
    gradle :app:assembleDebug --offline
    ```
  - Otherwise check internet/proxy settings and re-run with `--refresh-dependencies`.

---

### 6. KSP / Room Compiler Errors or Unresolved Generated Classes
- **Cause**: Incremental KSP/Kotlin compilation cache became out of sync after schema/model edits.
- **Fix**:
  ```bash
  gradle :app:kspDebugKotlin --rerun-tasks
  # Or full clean rebuild:
  gradle clean :app:assembleDebug
  ```

---

### 7. Configuration Cache Serialization Failure
- **Cause**: `org.gradle.configuration-cache=true` in `gradle.properties` conflicts with a local plugin or custom task invocation.
- **Fix**:
  ```bash
  gradle :app:assembleDebug --no-configuration-cache
  ```

---

### 8. `OutOfMemoryError: Metaspace` or `Java heap space`
- **Cause**: Large Compose + Kotlin build running out of heap on lower-RAM machines.
- **Fix**: Adjust `-Xmx` temporarily on CLI or stop stale Gradle daemons:
  ```bash
  gradle --stop
  gradle :app:assembleDebug -Dorg.gradle.jvmargs="-Xmx4g -XX:MaxMetaspaceSize=1g -Dfile.encoding=UTF-8"
  ```

---

### 9. `adb install` Fails with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`
- **Cause**: Device already has `com.aistudio.govinda.mathkx` installed signed with a different key (e.g., Release vs Debug or another machine's keystore).
- **Fix**: Uninstall the existing app first, then reinstall:
  ```bash
  adb uninstall com.aistudio.govinda.mathkx
  adb install app/build/outputs/apk/debug/app-debug.apk
  ```

---

### 10. `adb devices` Shows `unauthorized` or `offline`
- **Cause**: USB RSA key was not accepted on the device or ADB daemon is stuck.
- **Fix**:
  ```bash
  adb kill-server
  adb start-server
  adb devices
  ```
  Unlock your phone screen and tap **Always allow from this computer → Allow**.

---

### 11. `INSTALL_FAILED_OLDER_SDK`
- **Cause**: Test device is running Android 6.0 (API 23) or lower, whereas `minSdk` is `24` (Android 7.0).
- **Fix**: Install on an Android 7.0+ (API 24–36) device or emulator.

---

### 12. Stop Stuck Gradle Daemons
If a previous build hung or locked files on Windows/macOS/Linux:
```bash
gradle --stop
```
