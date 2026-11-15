package com.example.base

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import java.lang.reflect.Modifier
import java.util.IdentityHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@LooperMode(LooperMode.Mode.INSTRUMENTATION_TEST)
abstract class FirestoreEmulatorTestBase {

    protected lateinit var firestore: FirebaseFirestore
    protected lateinit var auth: FirebaseAuth
    protected lateinit var databaseId: String

    @Before
    open fun setUpFirebase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val resId = context.resources.getIdentifier("firestore_database_id", "string", context.packageName)
        databaseId = if (resId != 0) context.getString(resId) else "ai-studio-android-govinda-f241fb37-0ac1-437a-af1b-cfa55a012a2a"

        val app = if (FirebaseApp.getApps(context).isEmpty()) {
            val newApp = FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("com.aistudio.govinda.mathkx")
                    .setProjectId(PROJECT_ID)
                    .setApiKey("fake-api-key-for-emulator")
                    .build()
            )
            val newFirestore = FirebaseFirestore.getInstance(newApp, databaseId)
            newFirestore.useEmulator(EMULATOR_HOST, FIRESTORE_PORT)
            newFirestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                .build()

            val newAuth = FirebaseAuth.getInstance(newApp)
            newAuth.useEmulator(EMULATOR_HOST, AUTH_PORT)
            patchTenZeroTwoTwo(newAuth)
            newApp
        } else {
            FirebaseApp.getInstance()
        }

        firestore = FirebaseFirestore.getInstance(app, databaseId)
        auth = FirebaseAuth.getInstance(app)
        patchTenZeroTwoTwo(auth)
    }

    @After
    open fun tearDownFirebase() {
        auth.signOut()
    }

    protected suspend fun signInTestUser(email: String): String = withContext(Dispatchers.IO) {
        withTimeout(AUTH_TIMEOUT_MS) {
            val result = try {
                val task = auth.signInWithEmailAndPassword(email, DEFAULT_PASSWORD)
                patchTenZeroTwoTwo(auth)
                task.await()
            } catch (_: FirebaseAuthInvalidUserException) {
                val createTask = auth.createUserWithEmailAndPassword(email, DEFAULT_PASSWORD)
                patchTenZeroTwoTwo(auth)
                createTask.await()
            } catch (_: Exception) {
                val createTask = auth.createUserWithEmailAndPassword(email, DEFAULT_PASSWORD)
                patchTenZeroTwoTwo(auth)
                createTask.await()
            }
            checkNotNull(result.user?.uid) { "User auth failed" }
        }
    }

    protected fun Throwable.unwrapFirestoreException(): FirebaseFirestoreException? {
        var current: Throwable? = this
        while (current != null) {
            if (current is FirebaseFirestoreException) return current
            current = current.cause
        }
        return null
    }

    private fun patchTenZeroTwoTwo(root: Any?) {
        val visited = IdentityHashMap<Any, Boolean>()
        fun walk(obj: Any?, depth: Int) {
            if (obj == null || depth > 4 || visited.put(obj, true) != null) return
            var clazz: Class<*>? = obj.javaClass
            while (clazz != null && clazz != Any::class.java) {
                val pkg = clazz.name
                if (pkg.startsWith("java.") || pkg.startsWith("android.") || pkg.startsWith("androidx.")) {
                    break
                }
                for (field in clazz.declaredFields) {
                    try {
                        field.isAccessible = true
                        val isStatic = Modifier.isStatic(field.modifiers)
                        val target = if (isStatic) null else obj
                        val value = field.get(target)
                        if (value is String && value.contains("10.0.2.2")) {
                            field.set(target, value.replace("10.0.2.2", "127.0.0.1"))
                        } else if (value is Map<*, *>) {
                            for ((_, mapVal) in value) {
                                walk(mapVal, depth + 1)
                            }
                        } else if (value != null && !field.type.isPrimitive && !field.type.isEnum) {
                            walk(value, depth + 1)
                        }
                    } catch (_: Throwable) {
                    }
                }
                clazz = clazz.superclass
            }
        }
        walk(root, 0)
    }

    private companion object {
        const val EMULATOR_HOST = "127.0.0.1"
        const val FIRESTORE_PORT = 8085
        const val AUTH_PORT = 9099
        const val PROJECT_ID = "gen-lang-client-0571452922"
        const val DEFAULT_PASSWORD = "password123"
        const val AUTH_TIMEOUT_MS = 5000L
    }
}
