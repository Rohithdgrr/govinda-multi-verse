package com.example.lock

sealed interface LockDecision {
    data object NoAction : LockDecision
    data class LaunchQuizGate(val packageName: String) : LockDecision
}

/**
 * Pure state machine enforcing "1 quiz question once per fresh open" for locked apps.
 *
 * - While the user stays inside an unlocked target app (even across internal activity transitions),
 *   no additional quiz is triggered.
 * - Switching away to the launcher or another app, or turning the screen off, resets the active
 *   session so the next fresh open of any locked app prompts 1 question again.
 */
class QuizLockSessionTracker(
    private val relaunchDebounceMs: Long = 1500L
) {
    private var activeUnlockedPackage: String? = null
    private var pendingLockPackage: String? = null
    private var pendingLockTimestampMs: Long = 0L

    @Synchronized
    fun onPackageForegrounded(
        packageName: String,
        lockedPackages: Set<String>,
        quizLockEnabled: Boolean,
        ownPackageName: String,
        nowMs: Long = System.currentTimeMillis()
    ): LockDecision {
        if (!quizLockEnabled || lockedPackages.isEmpty()) {
            activeUnlockedPackage = null
            pendingLockPackage = null
            return LockDecision.NoAction
        }

        if (packageName.isBlank() || packageName == ownPackageName) {
            return LockDecision.NoAction
        }

        if (isTransientOverlayPackage(packageName)) {
            return LockDecision.NoAction
        }

        // Stay inside the unlocked app without prompting a second question
        if (packageName == activeUnlockedPackage) {
            return LockDecision.NoAction
        }

        // User moved to another app or launcher: end any previously unlocked session
        activeUnlockedPackage = null

        if (packageName in lockedPackages) {
            if (packageName == pendingLockPackage &&
                (nowMs - pendingLockTimestampMs) in 0L until relaunchDebounceMs
            ) {
                return LockDecision.NoAction
            }
            pendingLockPackage = packageName
            pendingLockTimestampMs = nowMs
            return LockDecision.LaunchQuizGate(packageName)
        }

        pendingLockPackage = null
        return LockDecision.NoAction
    }

    @Synchronized
    fun markUnlocked(packageName: String) {
        activeUnlockedPackage = packageName
        if (pendingLockPackage == packageName) {
            pendingLockPackage = null
        }
    }

    @Synchronized
    fun onBackCancelled() {
        pendingLockPackage = null
        pendingLockTimestampMs = 0L
    }

    @Synchronized
    fun onScreenOff() {
        activeUnlockedPackage = null
        pendingLockPackage = null
    }

    @Synchronized
    fun resetAll() {
        activeUnlockedPackage = null
        pendingLockPackage = null
        pendingLockTimestampMs = 0L
    }

    private fun isTransientOverlayPackage(pkg: String): Boolean {
        val lower = pkg.lowercase()
        return lower == "com.android.systemui" ||
            lower == "com.android.intentresolver" ||
            lower.contains("inputmethod") ||
            lower.contains("keyboard") ||
            lower.contains("gboard") ||
            lower.contains("swiftkey") ||
            lower == "com.android.permissioncontroller" ||
            lower == "com.google.android.permissioncontroller" ||
            lower == "com.android.packageinstaller"
    }
}

object QuizLockSessionHolder {
    val tracker = QuizLockSessionTracker()
}
