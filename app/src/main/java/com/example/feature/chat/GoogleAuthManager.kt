package com.example.feature.chat

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class CloudAuthUser(
    val uid: String,
    val displayName: String,
    val email: String
)

object GoogleAuthManager {

    fun currentCloudUser(): CloudAuthUser? {
        return try {
            val user = Firebase.auth.currentUser ?: return null
            CloudAuthUser(
                uid = user.uid,
                displayName = user.displayName?.takeIf { it.isNotBlank() }
                    ?: user.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
                    ?: "Student (${user.uid.take(6)})",
                email = user.email.orEmpty()
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Step 1: Attempt returning-user sign-in automatically on startup using GetGoogleIdOption
     * in its own separate GetCredentialRequest.
     */
    suspend fun attemptAutoSignIn(activityContext: Context): Result<CloudAuthUser?> {
        val existing = currentCloudUser()
        if (existing != null) return Result.success(existing)

        return try {
            val credentialManager = CredentialManager.create(activityContext)
            val webClientId = activityContext.getString(R.string.default_web_client_id)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(webClientId)
                .setFilterByAuthorizedAccounts(true)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )
            val user = handleSignInResult(result.credential)
            Result.success(user)
        } catch (_: NoCredentialException) {
            // Normal on first launch or emulator without pre-authorized Google session
            Result.success(null)
        } catch (e: GetCredentialException) {
            Log.w("GoogleAuthManager", "Auto sign-in unavailable: ${e.message}")
            Result.success(null)
        } catch (e: Exception) {
            Log.w("GoogleAuthManager", "Auto sign-in skipped: ${e.message}")
            Result.success(null)
        }
    }

    /**
     * Step 2: Explicit "Sign in with Google" button click using GetSignInWithGoogleOption
     * in its own separate GetCredentialRequest.
     */
    suspend fun onGoogleSignInClicked(activityContext: Context): Result<CloudAuthUser> {
        return try {
            val credentialManager = CredentialManager.create(activityContext)
            val webClientId = activityContext.getString(R.string.default_web_client_id)

            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
                serverClientId = webClientId
            ).build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )
            val user = handleSignInResult(result.credential)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Unsupported credential type returned from Google Sign-In"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.i("GoogleAuthManager", "User cancelled the Google Sign-In dialog", e)
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.e("GoogleAuthManager", "Google Sign-In failed: ${e.type} - ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("GoogleAuthManager", "Unexpected error during Google Sign-In", e)
            Result.failure(e)
        }
    }

    private suspend fun handleSignInResult(credential: androidx.credentials.Credential): CloudAuthUser? {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            val authResult = Firebase.auth.signInWithCredential(firebaseCredential).await()
            val fbUser: FirebaseUser = authResult.user ?: return null
            return CloudAuthUser(
                uid = fbUser.uid,
                displayName = fbUser.displayName?.takeIf { it.isNotBlank() }
                    ?: fbUser.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
                    ?: "Student (${fbUser.uid.take(6)})",
                email = fbUser.email.orEmpty()
            )
        }
        Log.w("GoogleAuthManager", "Unexpected credential type: ${credential.type}")
        return null
    }

    /**
     * Step 3: Sign out of both CredentialManager and FirebaseAuth inside NonCancellable.
     */
    suspend fun signOut(context: Context) {
        withContext(NonCancellable) {
            try {
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: ClearCredentialException) {
                Log.w("GoogleAuthManager", "Failed to clear CredentialManager state", e)
            } catch (e: Exception) {
                Log.w("GoogleAuthManager", "CredentialManager clear skipped", e)
            } finally {
                try {
                    Firebase.auth.signOut()
                } catch (e: Exception) {
                    Log.w("GoogleAuthManager", "Firebase.auth.signOut failed", e)
                }
            }
        }
    }
}
