package com.kisanprocure.app.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Scaffold(
        containerColor = KisanSurfaceLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile & System Settings",
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KisanWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(KisanMintContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👨‍🌾", fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = currentUser?.name ?: "Farmer Account",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = KisanTextDark
                            )
                            Text(
                                text = currentUser?.email ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = KisanTextMuted
                            )
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = KisanMintContainer
                            ) {
                                Text(
                                    text = "ROLE: ${currentUser?.role ?: "FARMER"}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KisanGreenDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = KisanBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileInfoRow(label = "Mobile", value = currentUser?.phone ?: "Not registered")
                    currentUser?.farmLocation?.let {
                        ProfileInfoRow(label = "Farm District", value = it)
                    }
                }
            }

            // Server & Network Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KisanWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Network & Server Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = KisanGreenDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AppConfig.Environment.values().forEach { env ->
                            val isSelected = selectedEnv == env
                            Surface(
                                modifier = Modifier
                                    .weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) KisanGreenPrimary else KisanMintContainer,
                                onClick = {
                                    selectedEnv = env
                                    AppConfig.setEnvironment(context, env)
                                    currentUrl = AppConfig.apiBaseUrl
                                }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = env.name.replace("_", " "),
                                        fontSize = 11.sp,
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

                    Button(
                        onClick = onNavigateToDiagnostics,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KisanMintContainer, contentColor = KisanGreenDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = KisanGreenDark, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("📡 Test Cloudflare & Real-Time Sync", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Logout Button
            Button(
                onClick = {
                    authViewModel.logout()
                    onLogout()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = KisanError),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = KisanError, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Log Out of Session", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
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
