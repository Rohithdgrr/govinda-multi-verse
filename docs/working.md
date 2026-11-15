# How GOVINDA Works Internally

This document explains the mathematical, adaptive, evaluation, and session-locking algorithms inside **GOVINDA**.

---

## 1. Multi-Parameter Adaptive Pipeline

Every question request passes through three deterministic stages:

### Stage A — Topic Selection (`TopicSelector`)
1. Filters `SkillProfile` records to `preferences.enabledCategories`.
2. In **Progressive Unlock** mode (`TopicMixMode.PROGRESSIVE_UNLOCK`):
   - **Warm-Up (`totalAttempts < warmUpCount`)**: Restricts selection to `ADDITION` and `SUBTRACTION` (if enabled).
   - **Foundational (`totalAttempts < warmUpCount + 5` or `avgScore < 55`)**: Opens `MULTIPLICATION` and `DIVISION`.
   - **Applied (`totalAttempts < warmUpCount + 12` or `avgScore < 70`)**: Opens `PERCENTAGE`, `DECIMAL`, and `FRACTION`.
   - **Full Spectrum**: Unlocks `POWERS_ROOTS` and `MIXED_OPERATIONS`.
3. Groups eligible categories into `WEAK` (`attempts < 3` or `score < 50`), `DEVELOPING` (`50 <= score < 80`), and `STRONG` (`score >= 80`), samples a tier bucket (50/30/20 Balanced or 70/20/10 Focus Weak), and weights categories by weakness, overdue reviews, recency, and anti-repeat variety.

### Stage B — Question Plan Resolution (`AdaptiveController.resolveQuestionPlan`)
Given the selected category's profile, global attempts, and `UserPreferences`:
1. **Simple Warm-Up Rule**: If `startSimpleEnabled == true`, `manualDifficultyOverride == null`, and `totalGlobalAttempts < warmUpQuestionCount`, forces `AdaptiveStage.WARM_UP`, `minDifficulty`, `complexityTier = 1`, `QuestionStyle.DIRECT`.
2. **Cold-Start Protection**: If a specific category has `< 2` attempts, caps complexity at Tier 1 `DIRECT` and difficulty at `BASIC`.
3. **Supportive Scaffolding**: If `wrongStreak >= 1` or recent 6-attempt accuracy `< 55%`, immediately resets `complexityTier = 1` (`DIRECT`) so the learner stabilizes before climbing again.
4. **Tier Progression**: Combines `skillScore`, streak bonus (`+2.5` per consecutive correct, max `+10`), speed bonus (`+3` when avg solve time `<= 9s` and `speedWeightEnabled`), and `AdaptivePace.tierOffset` (`+6` Gentle, `0` Balanced, `-6` Rapid) to select Complexity Tier `1..4` and corresponding `QuestionStyle`.

### Stage C — Profile Update & Hysteresis (`AdaptiveController.updateProfile`)
Using a rolling 10-attempt window per category:

$$\text{SkillScore} = 100 \times (W_{acc} \cdot \text{Accuracy} + W_{spd} \cdot \text{Speed} + 0.20 \cdot \text{Trend} + 0.10 \cdot \text{Consistency})$$

- When `speedWeightEnabled = true`: $W_{acc} = 0.50$, $W_{spd} = 0.20$.
- When `speedWeightEnabled = false`: $W_{acc} = 0.65$, $W_{spd} = 0.05$.
- **Difficulty Step-Up**: Requires hitting `AdaptivePace.streakToAdvance` (`6` Gentle / `5` Balanced / `3` Rapid) with `minAccuracyToAdvance` (`85%` / `80%` / `75%`) and acceptable solve time (`<= expectedMs * speedToleranceMultiplier`).
- **Difficulty Step-Down**: Triggers after `missesToStepDown` consecutive wrong answers (or 2 misses with rolling accuracy `< 45%`), resetting streak counters on transition to prevent oscillation.

---

## 2. Exact Evaluation (`AnswerEvaluator`)

- **Integers**: Parsed with `BigInteger` (`BigDecimal.compareTo` fallback so `"12.0"` matches `"12"`).
- **Decimals**: Normalized with `BigDecimal.compareTo` after `stripTrailingZeros()` (`"0.500"` == `"0.5"`).
- **Fractions**: Parsed via `Fraction.parse`, reduced using `BigInteger.gcd`, and compared canonically (`"2/4"` == `"1/2"` == `"0.5"`).

---

## 3. Quiz Lock Session State Machine (`QuizLockSessionTracker`)

- **Foreground Locked App (no active unlocked session)** → Debounced check → Emit `LaunchQuizGate(pkg)` → Open `MainActivity` challenge screen.
- **User Solves Question Correctly** → `markUnlocked(pkg)` sets `activeUnlockedPackage = pkg` → Relaunches `pkg` via `ACTION_MAIN` launcher intent.
- **Same App / System Overlay / Soft Keyboard** → `NoAction` (does NOT re-prompt while inside the app).
- **Launcher / Different App / Screen Off** → Clears `activeUnlockedPackage` so the next launch of `pkg` counts as a fresh open and prompts 1 question.
