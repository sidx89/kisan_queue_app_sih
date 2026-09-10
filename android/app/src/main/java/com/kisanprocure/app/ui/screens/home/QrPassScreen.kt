package com.kisanprocure.app.ui.screens.home

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode
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
import com.kisanprocure.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrPassScreen(
    token: String,
    onBack: () -> Unit
) {
    val qrBitmap = remember(token) { generateQrBitmap(token, 512) }

    Scaffold(
        containerColor = KisanSurfaceLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Procurement Entry Pass",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = KisanGreenDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = KisanGreenDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KisanWhite)
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
            Spacer(modifier = Modifier.height(8.dp))

            // Pass Container Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = KisanWhite)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Emblem Header
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(KisanMintContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🇮🇳", fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "GOVERNMENT OF KARNATAKA",
                        style = MaterialTheme.typography.labelMedium,
                        color = KisanGreenDark,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Smart APMC Procurement Entry Pass",
                        fontSize = 14.sp,
                        color = KisanTextMuted,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // QR Frame
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(KisanWhite)
                            .border(2.dp, KisanBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        qrBitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Booking QR Code",
                                modifier = Modifier.size(220.dp)
                            )
                        } ?: Box(
                            modifier = Modifier.size(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = KisanGreenPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Token Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = KisanMintContainer
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TOKEN NUMBER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = KisanTextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = token,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = KisanGreenDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = KisanBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = KisanGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Show this QR code at the security gate or weighing bridge. Once scanned, your turn will be called on the live announcement board.",
                            style = MaterialTheme.typography.bodySmall,
                            color = KisanTextMuted,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

private fun generateQrBitmap(content: String, size: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) 0xFF1B5E20.toInt() else android.graphics.Color.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}
