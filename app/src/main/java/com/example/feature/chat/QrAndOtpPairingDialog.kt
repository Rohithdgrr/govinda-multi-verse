package com.example.feature.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BlueprintPrimary = Color(0xFF003DA5)
private val BlueprintSecondary = Color(0xFF1647BB)
private val BlueprintSurface = Color(0xFFEAF1FF)

@Composable
fun QrAndOtpPairingDialog(
    myDeviceId: String,
    myOtpCode: String,
    onRegenerateMyOtp: () -> Unit,
    onPeerConnected: (ChatPeerContact) -> Unit,
    onDismiss: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = MY QR & OTP, 1 = ENTER OTP / QR LINK
    var remoteOtpInput by remember { mutableStateOf("") }
    var remotePeerNameInput by remember { mutableStateOf("") }
    var errorBanner by remember { mutableStateOf<String?>(null) }

    val qrPayload = remember(myDeviceId, myOtpCode) {
        PeerPairingEngine.buildQrPayload(myDeviceId, myOtpCode, "GOVINDA_USER")
    }
    val qrMatrix = remember(qrPayload) {
        PeerPairingEngine.generateQrMatrix(qrPayload, size = 21)
    }
    val digits = myOtpCode.filter { it.isDigit() }.padEnd(6, '0').take(6)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        modifier = Modifier.testTag("qr_otp_pairing_dialog"),
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCode2,
                        contentDescription = "QR & OTP Link",
                        tint = BlueprintPrimary
                    )
                    Text(
                        text = "LINK DEVICE / PEER (QR & OTP)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = BlueprintPrimary
                    )
                }
                Text(
                    text = "WhatsApp-style instant pairing via scannable QR matrix or 6-digit OTP code",
                    style = MaterialTheme.typography.labelSmall,
                    color = BlueprintSecondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            errorBanner = null
                        },
                        label = { Text("MY QR & OTP CODE", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BlueprintSurface,
                            selectedLabelColor = BlueprintPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pairing_tab_my_qr")
                    )
                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            errorBanner = null
                        },
                        label = { Text("CONNECT BY OTP / QR", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BlueprintSurface,
                            selectedLabelColor = BlueprintPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pairing_tab_enter_otp")
                    )
                }

                if (selectedTab == 0) {
                    // Tab 0: Show My Scannable QR Matrix + 6-Digit OTP Boxes
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(176.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(2.dp, BlueprintPrimary, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                                .testTag("pairing_qr_canvas"),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(148.dp)) {
                                val n = qrMatrix.size
                                val cellSize = size.width / n
                                for (r in 0 until n) {
                                    for (c in 0 until n) {
                                        if (qrMatrix[r][c]) {
                                            drawRect(
                                                color = BlueprintPrimary,
                                                topLeft = Offset(c * cellSize, r * cellSize),
                                                size = Size(cellSize + 0.5f, cellSize + 0.5f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = "ONE-TIME 6-DIGIT PAIRING CODE (OTP)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = BlueprintSecondary
                        )

                        // 6-Digit OTP Display Boxes
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.testTag("my_otp_boxes_row")
                        ) {
                            digits.forEachIndexed { index, ch ->
                                Box(
                                    modifier = Modifier
                                        .size(width = 36.dp, height = 44.dp)
                                        .background(BlueprintSurface, RoundedCornerShape(8.dp))
                                        .border(1.5.dp, BlueprintPrimary, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ch.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BlueprintPrimary
                                    )
                                }
                                if (index == 2) {
                                    Text(
                                        text = "-",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BlueprintPrimary
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onRegenerateMyOtp,
                                border = BorderStroke(1.dp, BlueprintPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("regenerate_otp_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null,
                                    tint = BlueprintPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("NEW OTP", fontSize = 11.sp, color = BlueprintPrimary)
                            }
                            Button(
                                onClick = {
                                    clipboard.setText(AnnotatedString(myOtpCode))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BlueprintPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("copy_my_otp_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("COPY OTP", fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    // Tab 1: Enter 6-Digit OTP Code or Paste QR Payload to Link Peer
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Enter a peer's 6-digit OTP code (e.g. 654321) or paste a mathgate:// QR link:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )

                        OutlinedTextField(
                            value = remotePeerNameInput,
                            onValueChange = { remotePeerNameInput = it },
                            label = { Text("Peer or Device Name (e.g. Vikram ECE Lab)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_peer_name_input")
                        )

                        OutlinedTextField(
                            value = remoteOtpInput,
                            onValueChange = {
                                remoteOtpInput = it
                                errorBanner = null
                            },
                            label = { Text("6-Digit OTP Code or QR Payload") },
                            placeholder = { Text("654321 or mathgate://chat-link?...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_code_input")
                        )

                        // Quick Demo OTP Presets for 1-tap testing
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    remotePeerNameInput = "Vikram (GATE ECE Peer)"
                                    remoteOtpInput = "849201"
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Fill OTP 849-201", fontSize = 10.sp, color = BlueprintPrimary)
                            }
                            OutlinedButton(
                                onClick = {
                                    remotePeerNameInput = "Study Tablet QR"
                                    remoteOtpInput = "mathgate://chat-link?id=tab_09&otp=918273&name=Study_Tablet_QR"
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Simulate QR Scan", fontSize = 10.sp, color = BlueprintPrimary)
                            }
                        }

                        errorBanner?.let { err ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                border = BorderStroke(1.dp, Color.Red)
                            ) {
                                Text(
                                    text = err,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Red,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val linked = PeerPairingEngine.parseQrOrOtpPayload(
                                    rawInput = remoteOtpInput,
                                    fallbackName = remotePeerNameInput
                                )
                                if (linked != null) {
                                    onPeerConnected(linked)
                                    onDismiss()
                                } else {
                                    errorBanner = "Please enter a valid 6-digit OTP code (e.g. 849201) or mathgate:// QR URI."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BlueprintPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("connect_otp_button")
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("VERIFY OTP / QR & CONNECT CHAT", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = BlueprintPrimary)
            }
        }
    )
}
