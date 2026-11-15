# Engineering Problems & Solutions

This log documents key technical challenges solved during GOVINDA's development.

---

### 1. Floating-Point Rounding Errors in Decimal & Fraction Grading
- **Problem**: `Double` arithmetic causes representation bugs (`0.1 + 0.2 != 0.3`), and fractions like `2/4` or `3/6` need canonical equivalence checking without rejecting valid unsimplified student inputs.
- **Solution**: All decimal generation and grading use `BigDecimal` (`stripTrailingZeros()` + `compareTo() == 0`), and all fractions use exact `BigInteger` numerator/denominator arithmetic reduced via `BigInteger.gcd` in `Fraction.normalized()`.

---

### 2. Erratic Difficulty Jumps & Hard Cold-Start Problems
- **Problem**: Jumping into fractions, powers, or multi-step expressions immediately—or oscillating difficulty after a single accidental miss—frustrates new users.
- **Solution**: Designed `AdaptiveController` + `TopicSelector` with:
  1. Initial **Warm-Up Stage** locking early questions to `Beginner` `Direct` arithmetic (`+` / `−`).
  2. **Rolling 10-Attempt Window + Hysteresis** requiring sustained streaks & accuracy (`AdaptivePace`) to advance.
  3. **Multi-Tier Complexity (`1..4`)** that evolves style (`Direct` → `Missing Value` → `Multi-Step` → `Analytic`) only when category mastery is proven, and immediately scaffolds back to `Direct` on active wrong streaks.

---

### 3. App Quiz Lock Prompting Repeatedly Inside an Unlocked App
- **Problem**: Android emits multiple `TYPE_WINDOW_STATE_CHANGED` events when switching activities inside an app or opening the soft keyboard (`IME`), which would repeatedly lock the user out after solving once.
- **Solution**: Built `QuizLockSessionTracker` with:
  - Package-level active unlock state (`activeUnlockedPackage`).
  - Transient system/keyboard window ignoring (`inputmethod`, `systemui`, etc.).
  - Relaunch debounce (`1500ms`) and clean reset only on launcher return, switching to a different app, or `ACTION_SCREEN_OFF`.

---

### 4. Android Play Policy Compliance for App Discovery
- **Problem**: Using `QUERY_ALL_PACKAGES` triggers strict Google Play policy rejection unless the app is a launcher/antivirus/financial app.
- **Solution**: Declared an explicit `<queries>` intent filter for `ACTION_MAIN` + `CATEGORY_LAUNCHER` in `AndroidManifest.xml`, letting `PackageManager` list user launchable apps with zero broad package permissions.

---

### 5. Plain-JVM Unit Test Stub Failure on `org.json.JSONObject`
- **Problem**: Android's `testOptions` provides unimplemented `org.json.JSONObject` stubs in plain JVM tests, throwing `RuntimeException: Method ... not mocked` during JSON serialization tests.
- **Solution**: Moved `AppearanceDataStore` JSON round-trip serialization tests to `ExampleRobolectricTest` where Robolectric provides real `org.json` implementations alongside Jetpack Compose UI testing.

---

### 6. Custom Font Resources & Offline Personalization Persistence
- **Problem**: Downloadable Google Fonts fail completely in an offline app with no `INTERNET` permission.
- **Solution**: Bundled 20 verified `.ttf` font families locally under `res/font/` and persisted active `PersonalizationState` (26 themes, custom palettes, typography, icons, avatars, layouts, patterns) locally in Preferences DataStore.
