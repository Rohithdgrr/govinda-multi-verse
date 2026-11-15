package com.example.feature.chat

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudChatRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun authenticatedUserCanCreateAndStreamMessages_Media_Location_Files() = runBlocking {
        val uid = signInTestUser("student1@example.com")
        val repository = CloudChatRepository(firestore)
        val convId = "conv_${UUID.randomUUID().toString().take(8)}"

        // 1. Create conversation
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.upsertCloudConversation(
                userId = uid,
                conversationId = convId,
                title = "ECE Gate Study Room",
                type = "STUDY_GROUP",
                peerCode = "482910",
                lastMessagePreview = "Initial room setup"
            )
        }

        val convs = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeCloudConversations(uid).first { list -> list.any { it.id == convId } }
        }
        assertTrue(convs.any { it.id == convId && it.title == "ECE Gate Study Room" })

        // 2. Send text, image (Base64), and GPS location messages
        val textId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.sendCloudMessage(
                userId = uid,
                conversationId = convId,
                senderId = uid,
                senderName = "Student One",
                content = "Hello from real Firestore stream!",
                messageType = "PLAIN_TEXT"
            )
        }

        val imageId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.sendCloudMessage(
                userId = uid,
                conversationId = convId,
                senderId = uid,
                senderName = "Student One",
                content = "📷 Circuit diagram photo",
                messageType = "IMAGE",
                attachmentName = "op_amp_schematic.jpg",
                attachmentMimeType = "image/jpeg",
                attachmentSizeBytes = 18420L,
                attachmentBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
            )
        }

        val locId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.sendCloudMessage(
                userId = uid,
                conversationId = convId,
                senderId = uid,
                senderName = "Student One",
                content = "📍 Live Location (60 min): Campus Library",
                messageType = "LOCATION",
                latitude = 28.5450,
                longitude = 77.1926,
                locationLabel = "Campus Library",
                liveLocationDurationMinutes = 60
            )
        }

        assertNotNull(textId)
        assertNotNull(imageId)
        assertNotNull(locId)

        // 3. Stream messages via real-time snapshots()
        val messages = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeCloudMessages(uid, convId).first { it.size >= 3 }
        }
        assertEquals(3, messages.size)
        assertTrue(messages.any { it.messageType == "IMAGE" && !it.attachmentBase64.isNullOrBlank() })
        assertTrue(messages.any { it.messageType == "LOCATION" && it.latitude == 28.5450 })

        // 4. Update message content & reaction
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateCloudMessageContent(uid, textId, "Edited real-time text")
            repository.updateCloudMessageReaction(uid, textId, "🔥")
        }

        val updatedMessages = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeCloudMessages(uid, convId).first { list ->
                list.any { it.id == textId && it.isEdited && it.reactionEmoji == "🔥" }
            }
        }
        val updatedText = updatedMessages.first { it.id == textId }
        assertEquals("Edited real-time text", updatedText.content)
        assertEquals("🔥", updatedText.reactionEmoji)
    }

    @Test
    fun crossUserReadAndWriteAreDeniedWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser("alice@example.com")
        val repository = CloudChatRepository(firestore)
        val convId = "conv_priv_${UUID.randomUUID().toString().take(8)}"
        val msgId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.sendCloudMessage(
                userId = aliceUid,
                conversationId = convId,
                senderId = aliceUid,
                senderName = "Alice",
                content = "Alice private message"
            )
        }

        // Switch to Bob
        signInTestUser("bob@example.com")

        // Bob cannot update or delete Alice's message
        var deleteException: Exception? = null
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.deleteCloudMessage(aliceUid, msgId)
            }
        } catch (e: Exception) {
            deleteException = e
        }
        val fsDeleteEx = checkNotNull(deleteException?.unwrapFirestoreException()) {
            "Expected nested FirebaseFirestoreException on cross-user delete, got $deleteException"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsDeleteEx.code)

        // Bob cannot create a message inside Alice's user path
        var createException: Exception? = null
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.sendCloudMessage(
                    userId = aliceUid,
                    conversationId = convId,
                    senderId = aliceUid,
                    senderName = "Bob Spoofing Alice",
                    content = "Spoofed message"
                )
            }
        } catch (e: Exception) {
            createException = e
        }
        val fsCreateEx = checkNotNull(createException?.unwrapFirestoreException()) {
            "Expected nested FirebaseFirestoreException on cross-user create, got $createException"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsCreateEx.code)
    }

    @Test
    fun unauthenticatedAccessIsDeniedWithPermissionDenied() = runBlocking {
        val uid = signInTestUser("temp@example.com")
        auth.signOut()
        val repository = CloudChatRepository(firestore)

        var exception: Exception? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeCloudMessages(uid, "conv_unauth_${UUID.randomUUID().toString().take(6)}").first()
            }
        } catch (e: Exception) {
            exception = e
        }
        val firestoreException = checkNotNull(exception?.unwrapFirestoreException()) {
            "Expected nested FirebaseFirestoreException when unauthenticated, got $exception"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
