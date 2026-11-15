# User & System Workflows

## 1. Daily Math Practice Workflow (Keypad-Only Home)

1. **Launch App** → `MainActivity` opens directly onto `ChallengeScreen` (`ROUTE_HOME`).
2. **Generate Question**:
   - `ChallengeViewModel` invokes `GenerateNextQuestionUseCase`.
   - `TopicSelector` selects an eligible topic based on progressive stage & tier weights.
   - `AdaptiveController.resolveQuestionPlan` picks `Difficulty`, `complexityTier (1..4)`, and `QuestionStyle`.
   - Generator emits a non-repeating `MathQuestion` with step-by-step explanations.
3. **Enter Answer**:
   - User taps numeric keys (`0–9`), fraction slash (`/`), negative toggle (`±`), or decimal (`.`) on the docked keypad.
   - Optional haptic/sound feedback triggers per `EffectSettings`.
4. **Submit & Grade**:
   - `SubmitAnswerUseCase` evaluates the answer via `AnswerEvaluator`, updates `SkillProfile` via `AdaptiveController.updateProfile`, records the Room transaction, and queues missed concepts into `review_queue`.
   - **Correct**: Green success feedback banner + optional **Show Steps** + **New Question** button.
   - **Incorrect**: Error feedback banner showing expected canonical answer, auto-expanded step-by-step explanation timeline, and **New Question**.

---

## 2. App Quiz Lock Unlock Workflow

1. User enables **App Quiz Lock** in **Settings**, selects target applications, and grants **GOVINDA App Quiz Lock** via the Accessibility permission disclosure dialog.
2. User launches a locked application fresh from the device launcher.
3. `GovindaQuizLockService` receives `TYPE_WINDOW_STATE_CHANGED` for the locked package and consults `QuizLockSessionTracker`.
4. Tracker returns `LaunchQuizGate(packageName)` → `MainActivity` opens with a `"SOLVE TO OPEN <APP>"` lock banner and 1 adaptive question.
5. **If incorrect**: Banner displays explanation + **Try Another Question** until solved.
6. **If correct**: `QuizLockSessionTracker.markUnlocked(packageName)` unlocks that app session and `MainActivity` immediately launches the unlocked app. Switching screens inside that app does **not** re-prompt; returning to the launcher or turning the screen off resets the session.

---

## 3. Adaptive Configuration & Personalization Workflow

1. User taps the **Settings** gear icon in the top bar (`ChallengeTopNavBar`).
2. Adjusts **Adaptive Mechanism** parameters (Start Simple Warm-Up, Adaptation Pace, Topic Progression, Style Evolution, Speed Weight) or **Question Difficulty** overrides.
3. Opens **Personalization Studio** (`ROUTE_PERSONALIZATION`) to preview and apply themes, custom colors, bundled fonts, icon packs, layouts, avatars, or background patterns; all UI surfaces re-render reactively via `GovindaDynamicTheme`.
4. Inspects **Permissions & Access**, **Help Guide**, **FAQ**, **About**, **Privacy Policy**, **Terms & Conditions**, or **Open-Source Licenses**, or exports/resets local data.
