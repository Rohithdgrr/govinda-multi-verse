package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.core.navigation.GovindaApp
import com.example.core.navigation.LockLaunchRequest
import com.example.lock.GovindaQuizLockService
import com.example.lock.InstalledAppsProvider
import com.example.lock.QuizLockSessionHolder

class MainActivity : ComponentActivity() {

    private var lockLaunchRequest by mutableStateOf<LockLaunchRequest?>(null)
    private var freshOpenSignal by mutableIntStateOf(0)
    private var wentToBackground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        handleLockIntent(intent)

        val appContainer = (application as GovindaApplication).container
        setContent {
            GovindaApp(
                container = appContainer,
                lockLaunchRequest = lockLaunchRequest,
                freshOpenSignal = freshOpenSignal,
                onUnlockAndOpenPackage = { packageName ->
                    launchUnlockedApp(packageName)
                },
                onCancelLockToHome = {
                    cancelLockToHome()
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLockIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        if (wentToBackground) {
            wentToBackground = false
            freshOpenSignal += 1
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            wentToBackground = true
        }
    }

    private fun handleLockIntent(intent: Intent?) {
        val pkg = intent?.getStringExtra(GovindaQuizLockService.EXTRA_LOCKED_PACKAGE) ?: return
        val appName = intent.getStringExtra(GovindaQuizLockService.EXTRA_LOCKED_APP_NAME)
            ?: InstalledAppsProvider.resolveAppName(this, pkg)
        intent.removeExtra(GovindaQuizLockService.EXTRA_LOCKED_PACKAGE)
        intent.removeExtra(GovindaQuizLockService.EXTRA_LOCKED_APP_NAME)
        lockLaunchRequest = LockLaunchRequest(
            packageName = pkg,
            appName = appName,
            requestToken = System.nanoTime()
        )
    }

    private fun launchUnlockedApp(packageName: String) {
        lockLaunchRequest = null
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            )
        }
        if (launchIntent != null) {
            runCatching { startActivity(launchIntent) }
        } else {
            moveTaskToBack(true)
        }
    }

    private fun cancelLockToHome() {
        lockLaunchRequest = null
        QuizLockSessionHolder.tracker.onBackCancelled()
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { startActivity(homeIntent) }
    }
}
