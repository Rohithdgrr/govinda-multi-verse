package com.example.feature.chat

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    GET("get"),
    LIST("list"),
    WRITE("write")
}

fun handleFirestoreError(
    exception: Exception,
    operationType: OperationType,
    path: String?
): Nothing {
    val currentUser = try {
        Firebase.auth.currentUser
    } catch (_: Exception) {
        null
    }

    val providerArray = JSONArray()
    currentUser?.providerData?.forEach { profile ->
        providerArray.put(
            JSONObject().apply {
                put("providerId", profile.providerId)
                put("displayName", profile.displayName)
                put("email", profile.email)
                put(" federatedId", profile.uid)
            }
        )
    }

    val authInfo = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("isAnonymous", currentUser?.isAnonymous)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", providerArray)
    }

    val errorInfo = JSONObject().apply {
        put("error", exception.message ?: exception.javaClass.simpleName)
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfo)
    }

    Log.e("FirestoreError", errorInfo.toString(), exception)
    throw RuntimeException(errorInfo.toString(), exception)
}
