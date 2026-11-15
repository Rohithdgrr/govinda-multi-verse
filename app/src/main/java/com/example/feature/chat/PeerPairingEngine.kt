package com.example.feature.chat

import java.util.UUID
import kotlin.math.abs

data class ChatPeerContact(
    val id: String,
    val name: String,
    val handle: String,
    val avatarInitials: String,
    val roleOrBranch: String,
    val linkedVia: String, // "QR_CODE", "OTP_CODE", "STUDY_GROUP"
    val pairingCodeOtp: String,
    val isOnline: Boolean = true,
    val isPinned: Boolean = false,
    val isGroup: Boolean = false,
    val unreadCount: Int = 0
)

data class WhatsAppChatMessage(
    val id: String = "wmsg_${UUID.randomUUID().toString().take(8)}",
    val peerId: String,
    val senderName: String,
    val isOutgoing: Boolean,
    val text: String,
    val latexFormula: String? = null,
    val sharedNoteTitle: String? = null,
    val replyToSnippet: String? = null,
    val timeLabel: String = "10:42",
    val deliveryStatus: String = "READ", // SENT (✓), DELIVERED (✓✓), READ (✓✓ blue)
    val isStarred: Boolean = false,
    val reaction: String? = null,
    val timestampMs: Long = System.currentTimeMillis()
)

object PeerPairingEngine {

    fun generateSixDigitOtp(seed: Long = System.currentTimeMillis()): String {
        val code = (abs(seed * 6364136223846793005L + 1442695040888963407L) % 900_000L) + 100_000L
        return code.toString()
    }

    fun formatOtpDisplay(otp: String): String {
        val digits = otp.filter { it.isDigit() }.padEnd(6, '0').take(6)
        return "${digits.substring(0, 3)}-${digits.substring(3, 6)}"
    }

    fun validateOtpInput(rawOtp: String): Boolean {
        val digits = rawOtp.filter { it.isDigit() }
        return digits.length == 6
    }

    fun buildQrPayload(deviceId: String, otp: String, displayName: String): String {
        val cleanOtp = otp.filter { it.isDigit() }.take(6)
        return "mathgate://chat-link?id=$deviceId&otp=$cleanOtp&name=${displayName.replace(" ", "_")}"
    }

    fun parseQrOrOtpPayload(rawInput: String, fallbackName: String): ChatPeerContact? {
        val trimmed = rawInput.trim()
        if (trimmed.startsWith("mathgate://", ignoreCase = true)) {
            val query = trimmed.substringAfter("?", "")
            val params = query.split("&").mapNotNull { part ->
                val kv = part.split("=", limit = 2)
                if (kv.size == 2) kv[0] to kv[1] else null
            }.toMap()
            val otp = params["otp"]?.filter { it.isDigit() }?.take(6) ?: generateSixDigitOtp()
            val name = params["name"]?.replace("_", " ")?.ifBlank { fallbackName } ?: fallbackName
            val id = params["id"] ?: "peer_${otp}"
            val initials = name.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("").ifEmpty { "QR" }
            return ChatPeerContact(
                id = id,
                name = name,
                handle = "@${id.lowercase()}",
                avatarInitials = initials,
                roleOrBranch = "Linked via QR Matrix",
                linkedVia = "QR_CODE",
                pairingCodeOtp = otp,
                isOnline = true,
                isPinned = true,
                unreadCount = 0
            )
        }

        val digits = trimmed.filter { it.isDigit() }
        if (digits.length == 6) {
            val cleanName = fallbackName.trim().ifEmpty { "Peer #$digits" }
            val initials = cleanName.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("").ifEmpty { "OT" }
            return ChatPeerContact(
                id = "peer_$digits",
                name = cleanName,
                handle = "@otp_$digits",
                avatarInitials = initials,
                roleOrBranch = "Verified 6-Digit OTP Link",
                linkedVia = "OTP_CODE",
                pairingCodeOtp = digits,
                isOnline = true,
                isPinned = true,
                unreadCount = 0
            )
        }
        return null
    }

    /**
     * Generates a deterministic 21x21 QR Code module matrix (with standard 7x7 finder patterns
     * in top-left, top-right, and bottom-left corners, timing patterns, and payload bits)
     * for rendering on a Jetpack Compose Canvas with zero external network dependencies.
     */
    fun generateQrMatrix(payload: String, size: Int = 21): Array<BooleanArray> {
        val grid = Array(size) { BooleanArray(size) }
        val reserved = Array(size) { BooleanArray(size) }

        fun placeFinderPattern(rowOffset: Int, colOffset: Int) {
            for (r in 0..6) {
                for (c in 0..6) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isInner = r in 2..4 && c in 2..4
                    grid[rowOffset + r][colOffset + c] = isBorder || isInner
                    reserved[rowOffset + r][colOffset + c] = true
                }
            }
        }

        placeFinderPattern(0, 0)
        placeFinderPattern(0, size - 7)
        placeFinderPattern(size - 7, 0)

        // Quiet separators around finder patterns
        for (i in 0..7) {
            if (i < size) {
                reserved[7][i] = true
                reserved[i][7] = true
                reserved[7][size - 1 - i] = true
                reserved[i][size - 8] = true
                reserved[size - 8][i] = true
                reserved[size - 1 - i][7] = true
            }
        }

        // Timing patterns on row 6 and col 6
        for (i in 8 until size - 8) {
            grid[6][i] = (i % 2 == 0)
            reserved[6][i] = true
            grid[i][6] = (i % 2 == 0)
            reserved[i][6] = true
        }

        // Deterministic payload encoding across remaining modules
        val bytes = payload.encodeToByteArray()
        var bitIndex = 0
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (!reserved[r][c]) {
                    val byteVal = bytes[bitIndex % bytes.size].toInt() and 0xFF
                    val shift = (bitIndex / bytes.size) % 8
                    val hashMix = ((r * 31 + c * 17 + bitIndex * 13) xor byteVal)
                    grid[r][c] = ((hashMix shr shift) and 1) == 1
                    bitIndex++
                }
            }
        }
        return grid
    }

    fun defaultContacts(): List<ChatPeerContact> = listOf(
        ChatPeerContact(
            id = "peer_ece_group",
            name = "ECE & VLSI Study Group",
            handle = "@ece_gate_group",
            avatarInitials = "EC",
            roleOrBranch = "ECE • 14 Peers Linked",
            linkedVia = "QR_CODE",
            pairingCodeOtp = "482910",
            isOnline = true,
            isPinned = true,
            isGroup = true,
            unreadCount = 2
        ),
        ChatPeerContact(
            id = "peer_arjun_eee",
            name = "Arjun Sharma (EEE / Control)",
            handle = "@arjun_control",
            avatarInitials = "AS",
            roleOrBranch = "EEE • Verified via OTP",
            linkedVia = "OTP_CODE",
            pairingCodeOtp = "739204",
            isOnline = true,
            isPinned = true,
            isGroup = false,
            unreadCount = 1
        ),
        ChatPeerContact(
            id = "peer_priya_cse",
            name = "Priya Nair (CSE & Algorithms)",
            handle = "@priya_algo",
            avatarInitials = "PN",
            roleOrBranch = "CSE • Verified via QR",
            linkedVia = "QR_CODE",
            pairingCodeOtp = "518392",
            isOnline = true,
            isPinned = false,
            isGroup = false,
            unreadCount = 0
        ),
        ChatPeerContact(
            id = "peer_tutor_bot",
            name = "MATHGATE Formula & Note Bot",
            handle = "@mathgate_bot",
            avatarInitials = "MG",
            roleOrBranch = "Instant Solver & Note Sync",
            linkedVia = "OTP_CODE",
            pairingCodeOtp = "100200",
            isOnline = true,
            isPinned = false,
            isGroup = false,
            unreadCount = 0
        )
    )

    fun defaultMessages(): List<WhatsAppChatMessage> = listOf(
        WhatsAppChatMessage(
            id = "m_101",
            peerId = "peer_ece_group",
            senderName = "Rohan (ECE)",
            isOutgoing = false,
            text = "Shared the De Morgan & NAND synthesis truth table from our Digital Electronics note.",
            latexFormula = "F = \\overline{A \\cdot B} = \\overline{A} + \\overline{B}",
            sharedNoteTitle = "Digital Electronics & Logic Design",
            timeLabel = "09:15",
            deliveryStatus = "READ",
            isStarred = true
        ),
        WhatsAppChatMessage(
            id = "m_102",
            peerId = "peer_ece_group",
            senderName = "You",
            isOutgoing = true,
            text = "Awesome! I also verified the RC Low-Pass cutoff f_c = 159.15 Hz for R=10kΩ, C=100nF.",
            latexFormula = "f_c = \\frac{1}{2\\pi R C} = 159.15\\text{ Hz}",
            timeLabel = "09:18",
            deliveryStatus = "READ"
        ),
        WhatsAppChatMessage(
            id = "m_201",
            peerId = "peer_arjun_eee",
            senderName = "Arjun Sharma",
            isOutgoing = false,
            text = "Hey! Connected via OTP 739-204. Can you share the closed-loop transfer function formula?",
            latexFormula = "T(s) = \\frac{G(s)}{1 + G(s)H(s)}",
            timeLabel = "10:05",
            deliveryStatus = "READ"
        ),
        WhatsAppChatMessage(
            id = "m_301",
            peerId = "peer_priya_cse",
            senderName = "Priya Nair",
            isOutgoing = false,
            text = "Linked via QR! Let's review Dijkstra's O((V+E) log V) priority queue implementation.",
            timeLabel = "Yesterday",
            deliveryStatus = "READ"
        ),
        WhatsAppChatMessage(
            id = "m_401",
            peerId = "peer_tutor_bot",
            senderName = "MATHGATE Bot",
            isOutgoing = false,
            text = "Send any math expression (e.g. 25% of 360), engineering topic (RC filter, Boolean, 3-phase power), or share a note!",
            latexFormula = "e^{i\\pi} + 1 = 0",
            timeLabel = "10:30",
            deliveryStatus = "READ"
        )
    )
}
