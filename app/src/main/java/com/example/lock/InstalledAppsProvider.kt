package com.example.lock

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val iconBitmap: ImageBitmap?
)

object InstalledAppsProvider {

    private const val APPS_CACHE_TTL_MS = 60_000L
    private const val ICON_SIZE_PX = 96
    private val appNameCache = LruCache<String, String>(128)

    @Volatile
    private var cachedApps: List<InstalledAppItem>? = null

    @Volatile
    private var cachedAtMs: Long = 0L

    fun invalidateCache() {
        cachedApps = null
        cachedAtMs = 0L
        appNameCache.evictAll()
    }

    suspend fun loadLaunchableApps(
        context: Context,
        forceRefresh: Boolean = false
    ): List<InstalledAppItem> {
        val now = SystemClock.elapsedRealtime()
        val snapshot = cachedApps
        if (!forceRefresh && snapshot != null && (now - cachedAtMs) < APPS_CACHE_TTL_MS) {
            return snapshot
        }

        return withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val ownPackage = context.packageName
            val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

            val resolved = runCatching {
                pm.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
            }.getOrElse {
                runCatching { pm.queryIntentActivities(launcherIntent, 0) }.getOrDefault(emptyList())
            }

            val seenPackages = HashSet<String>(resolved.size)
            val result = resolved
                .asSequence()
                .mapNotNull { info ->
                    val activityInfo = info.activityInfo ?: return@mapNotNull null
                    val pkg = activityInfo.packageName ?: return@mapNotNull null
                    if (pkg == ownPackage || !seenPackages.add(pkg)) return@mapNotNull null
                    val label = runCatching { info.loadLabel(pm).toString().trim() }
                        .getOrDefault(pkg)
                        .ifBlank { pkg }
                    appNameCache.put(pkg, label)
                    val iconBitmap = runCatching {
                        info.loadIcon(pm).toImageBitmapSafely()
                    }.getOrNull()
                    InstalledAppItem(
                        packageName = pkg,
                        appName = label,
                        iconBitmap = iconBitmap
                    )
                }
                .sortedBy { it.appName.lowercase() }
                .toList()

            cachedApps = result
            cachedAtMs = SystemClock.elapsedRealtime()
            result
        }
    }

    fun resolveAppName(context: Context, packageName: String): String {
        appNameCache.get(packageName)?.let { return it }
        val pm = context.packageManager
        val resolved = runCatching {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString().trim().ifBlank { packageName }
        }.getOrDefault(packageName)
        appNameCache.put(packageName, resolved)
        return resolved
    }

    private fun Drawable.toImageBitmapSafely(sizePx: Int = ICON_SIZE_PX): ImageBitmap? {
        if (this is BitmapDrawable && bitmap != null && !bitmap.isRecycled) {
            val src = bitmap
            if (src.width <= sizePx && src.height <= sizePx) {
                return src.asImageBitmap()
            }
            val scaled = Bitmap.createScaledBitmap(src, sizePx, sizePx, true)
            return scaled.asImageBitmap()
        }
        val width = intrinsicWidth.takeIf { it > 0 }?.coerceAtMost(sizePx) ?: sizePx
        val height = intrinsicHeight.takeIf { it > 0 }?.coerceAtMost(sizePx) ?: sizePx
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bmp.asImageBitmap()
    }
}
