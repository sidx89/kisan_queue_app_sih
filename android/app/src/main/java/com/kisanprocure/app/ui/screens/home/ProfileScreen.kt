package com.kisanprocure.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import com.kisanprocure.app.ui.components.*
import com.kisanprocure.app.ui.theme.*
import com.kisanprocure.app.ui.viewmodel.AuthViewModel
import com.kisanprocure.app.utils.AppConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onNavigateToDiagnostics: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    var currentUrl by remember { mutableStateOf(AppConfig.apiBaseUrl) }
    var selectedEnv by remember { mutableStateOf(AppConfig.environment) }

    var selectedLanguage by remember { mutableStateOf("English") }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var darkModeEnabled by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = KisanSurfaceLight,
        topBar = {
            KisanTopBar(
                title = "Profile & Settings",
                subtitle = "Manage Account & Network",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header Card
            KisanCard(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(KisanMintContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👨‍🌾", fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.name ?: "Ramesh Kumar",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = KisanTextDark
                            )
                            Text(
                                text = currentUser?.email ?: "ramesh@kisan.in",
                                style = MaterialTheme.typography.bodySmall,
                                color = KisanTextMuted
                            )
                            Spacer(Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = KisanMintContainer
                            ) {
                                Text(
                                    text = "ROLE: ${currentUser?.role ?: "FARMER"}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = KisanGreenDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = KisanDivider, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileInfoItem(label = "Mobile", value = currentUser?.phone?.ifBlank { null } ?: "+91 98765 43210")
                    currentUser?.farmLocation?.let {
                        ProfileInfoItem(label = "Farm District", value = it)
                    }
                }
            }

            // Quick Services Section
            KisanCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingsMenuRow(
                        icon = Icons.Default.Grass,
                        title = "My Crops",
                        subtitle = "Manage crops & MSP rates",
                        onClick = {}
                    )
                    HorizontalDivider(color = KisanDivider, thickness = 0.5.dp)
                    SettingsMenuRow(
                        icon = Icons.Default.ReceiptLong,
                        title = "My Bookings",
                        subtitle = "View pass & visit history",
                        onClick = onBack
                    )
                    HorizontalDivider(color = KisanDivider, thickness = 0.5.dp)
                    SettingsMenuRow(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = "SMS & in-app alerts",
                        badge = "3",
                        onClick = {}
                    )
                }
            }

            // Network & Server Card
            KisanCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Network & Server",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = KisanGreenDark
                        )
                        KisanConnectionChip(isConnected = true)
                    }

                    // Environment Picker Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AppConfig.Environment.values().forEach { env ->
                            val isSelected = selectedEnv == env
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedEnv = env
                                        AppConfig.setEnvironment(context, env)
                                        currentUrl = AppConfig.apiBaseUrl
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) KisanGreenPrimary else KisanMintContainer
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = env.name.replace("_", " "),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) KisanWhite else KisanGreenDark
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = currentUrl,
                        onValueChange = {
                            currentUrl = it
                            AppConfig.setCustomUrl(it)
                        },
                        label = { Text("Active Backend URL") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KisanGreenPrimary,
                            unfocusedBorderColor = KisanBorder,
                            focusedLabelColor = KisanGreenPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    KisanOutlineButton(
                        text = "📡 Test Cloudflare & Real-Time Sync",
                        onClick = onNavigateToDiagnostics,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // App Settings Card
            KisanCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        "App Settings",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = KisanGreenDark,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Language Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLanguageDialog = true }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(KisanMintContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🗣️", fontSize = 16.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Text("Language", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedLanguage, style = MaterialTheme.typography.bodySmall, color = KisanTextMuted)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = KisanTextMuted)
                        }
                    }

                    HorizontalDivider(color = KisanDivider, thickness = 0.5.dp)

                    // Dark Mode Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(KisanMintContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DarkMode, contentDescription = null, tint = KisanGreenPrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text("Dark Mode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        Switch(
                            checked = darkModeEnabled,
                            onCheckedChange = { darkModeEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = KisanWhite,
                                checkedTrackColor = KisanGreenPrimary
                            )
                        )
                    }

                    HorizontalDivider(color = KisanDivider, thickness = 0.5.dp)

                    // Share APK Row (WhatsApp / Direct file)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { shareApk(context) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(KisanMintContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = KisanGreenPrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Share App (WhatsApp / APK)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = KisanGreenDark)
                                Text("Send APK directly to other farmers", style = MaterialTheme.typography.labelSmall, color = KisanTextMuted)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = KisanGreenPrimary)
                    }

                    HorizontalDivider(color = KisanDivider, thickness = 0.5.dp)

                    // About App
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(KisanMintContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = KisanGreenPrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text("About App", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        Text("v1.0.0 SIH", style = MaterialTheme.typography.labelSmall, color = KisanTextMuted)
                    }
                }
            }

            // Log Out Button
            Button(
                onClick = {
                    authViewModel.logout()
                    onLogout()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KisanErrorBg, contentColor = KisanError),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = KisanError, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Log Out of Session", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language", fontWeight = FontWeight.Bold, color = KisanGreenDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ಕನ್ನಡ (Kannada)", "हिन्दी (Hindi)", "English").forEach { lang ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLanguage = lang.substringBefore(" ")
                                    showLanguageDialog = false
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedLanguage in lang) KisanMintContainer else KisanWhite
                        ) {
                            Text(
                                text = lang,
                                modifier = Modifier.padding(14.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedLanguage in lang) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedLanguage in lang) KisanGreenDark else KisanTextDark
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close", color = KisanGreenPrimary)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun ProfileInfoItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = KisanTextMuted)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = KisanTextDark)
    }
}

@Composable
fun SettingsMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(KisanMintContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = KisanGreenPrimary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = KisanTextDark)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = KisanTextMuted)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (badge != null) {
                Surface(
                    shape = CircleShape,
                    color = KisanError
                ) {
                    Text(
                        text = badge,
                        color = KisanWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = KisanTextMuted)
        }
    }
}

fun shareApk(context: Context) {
    try {
        val originalApk = File(context.applicationInfo.sourceDir)
        if (!originalApk.exists()) {
            Toast.makeText(context, "APK not found on device", Toast.LENGTH_SHORT).show()
            return
        }

        val cacheApk = File(context.cacheDir, "KisanProcure.apk")
        originalApk.inputStream().use { input ->
            cacheApk.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheApk
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "KisanProcure App")
            putExtra(
                Intent.EXTRA_TEXT,
                "Download & install KisanProcure - Smart Farmer Procurement App to book APMC slots & track queues live."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share KisanProcure APK via"))
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to share APK: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
