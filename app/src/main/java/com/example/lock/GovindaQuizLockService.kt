package com.example.lock

import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.os.Build
import android.net.Uri
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import com.example.GovindaApplication
import com.example.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GovindaQuizLockService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Volatile
    private var quizLockEnabled: Boolean = false

    @Volatile
    private var lockedPackages: Set<String> = emptySet()

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                QuizLockSessionHolder.tracker.onScreenOff()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val app = application as? GovindaApplication ?: return
        serviceScope.launch {
            runCatching {
                val initial = app.container.repository.observePreferences().first()
                quizLockEnabled = initial.quizLockEnabled
                lockedPackages = initial.quizLockedPackages
            }
            app.container.repository.observePreferences().collect { prefs ->
                quizLockEnabled = prefs.quizLockEnabled
                lockedPackages = prefs.quizLockedPackages
                if (!prefs.quizLockEnabled) {
                    QuizLockSessionHolder.tracker.resetAll()
                }
            }
        }
        ContextCompat.registerReceiver(
            this,
            screenOffReceiver,
            IntentFilter(Intent.ACTION_SCREEN_OFF),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }
        val pkg = event.packageName?.toString() ?: return
        val decision = QuizLockSessionHolder.tracker.onPackageForegrounded(
            packageName = pkg,
            lockedPackages = lockedPackages,
            quizLockEnabled = quizLockEnabled,
            ownPackageName = packageName
        )

        if (decision is LockDecision.LaunchQuizGate) {
            val appName = InstalledAppsProvider.resolveAppName(this, decision.packageName)
            val gateIntent = Intent(this, MainActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_NO_USER_ACTION
                )
                putExtra(EXTRA_LOCKED_PACKAGE, decision.packageName)
                putExtra(EXTRA_LOCKED_APP_NAME, appName)
            }
            runCatching { startActivity(gateIntent) }
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        super.onDestroy()
        runCatching { unregisterReceiver(screenOffReceiver) }
        serviceScope.cancel()
    }

    companion object {
        const val EXTRA_LOCKED_PACKAGE = "extra_locked_package"
        const val EXTRA_LOCKED_APP_NAME = "extra_locked_app_name"

        fun isServiceEnabled(context: Context): Boolean {
            val expectedComponent = ComponentName(context, GovindaQuizLockService::class.java)
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val splitter = TextUtils.SimpleStringSplitter(':')
            splitter.setString(enabledServices)
            while (splitter.hasNext()) {
                val componentName = ComponentName.unflattenFromString(splitter.next())
                if (componentName == expectedComponent) {
                    return true
                }
            }
            return false
        }

        fun isOverlayEnabled(context: Context): Boolean {
            return runCatching { Settings.canDrawOverlays(context) }.getOrDefault(false)
        }

        fun areAllLockPermissionsGranted(context: Context): Boolean {
            return isServiceEnabled(context) && isOverlayEnabled(context)
        }

        /**
         * Android 13+ blocks Accessibility for apps not installed by a store
         * ("Restricted setting" dialog). True when that block is likely.
         */
        fun isLikelyRestrictedSideload(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
            return runCatching {
                val src = context.packageManager
                    .getInstallSourceInfo(context.packageName).packageSource
                src != PackageInstaller.PACKAGE_SOURCE_STORE
            }.getOrDefault(false)
        }

        /** Opens App info, where the 3-dot menu offers "Allow restricted settings". */
        fun openAppInfoForRestrictedSettings(context: Context) {
            runCatching {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }

        fun launchAllMissingPermissions(context: Context) {
            val intents = mutableListOf<Intent>()
            if (!isServiceEnabled(context)) {
                // Sideloaded installs must unlock restricted settings first, otherwise
                // the Accessibility toggle just shows "Restricted setting".
                if (isLikelyRestrictedSideload(context)) {
                    openAppInfoForRestrictedSettings(context)
                    return
                }
                intents += Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            }
            if (!isOverlayEnabled(context)) {
                intents += Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.fromParts("package", context.packageName, null)
                )
            }
            if (intents.isEmpty()) return

            runCatching {
                if (context is Activity && intents.size > 1) {
                    context.startActivities(intents.toTypedArray())
                } else {
                    val target = intents.last().apply {
                        if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(target)
                }
            }
        }
    }
}
