package com.kisanprocure.app.ui.screens.home

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.kisanprocure.app.ui.components.KisanTopBar
import com.kisanprocure.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrPassScreen(
    token: String,
    onBack: () -> Unit
) {
    val qrBitmap = remember(token) { generateQrBitmapGreen(token, 600) }

    // Entry animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        containerColor = KisanSurfaceLight,
        topBar = {
            KisanTopBar(
                title = "Procurement Pass",
                subtitle = "Digital Entry Token",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400, easing = EaseOutCubic)) { it / 3 }
            ) {
                // Ticket card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = KisanWhite)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header band — green
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(KisanGreenDark)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "GOVERNMENT OF KARNATAKA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KisanWhite.copy(alpha = 0.75f),
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = "KisanProcure Digital Pass",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = KisanWhite
                                )
                            }
                        }

                        // Ticket perforation effect
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .offset(x = (-8).dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(KisanSurfaceLight)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(KisanDivider)
                                    .align(Alignment.CenterVertically)
                            )
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .offset(x = 8.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(KisanSurfaceLight)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Token number — prominent
                        Text(
                            text = "TOKEN",
                            style = MaterialTheme.typography.labelSmall,
                            color = KisanTextMuted,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = token,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = KisanGreenDark
                        )

                        Spacer(Modifier.height(20.dp))

                        // QR Code
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, KisanBorder, RoundedCornerShape(16.dp))
                                .background(KisanWhite)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            qrBitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "QR Code for $token",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } ?: CircularProgressIndicator(color = KisanGreenPrimary)
                        }

                        Spacer(Modifier.height(16.dp))

                        // Status chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = KisanStatusOpenBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(KisanStatusOpen)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "VALID",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = KisanStatusOpen,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                        HorizontalDivider(color = KisanDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(Modifier.height(16.dp))

                        // Instructions
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = KisanTextMuted, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Show this QR code at the security gate or weighing bridge. Your token will be announced at the operator counter.",
                                style = MaterialTheme.typography.bodySmall,
                                color = KisanTextMuted,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

private fun generateQrBitmapGreen(content: String, size: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF1B5E20.toInt() else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) { null }
}
