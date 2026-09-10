package com.kisanprocure.app.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kisanprocure.app.ui.components.KisanPrimaryButton
import com.kisanprocure.app.ui.theme.*
import com.kisanprocure.app.ui.viewmodel.AuthUiState
import com.kisanprocure.app.ui.viewmodel.AuthViewModel
import com.kisanprocure.app.utils.AppConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: (role: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }
    var serverUrlInput by remember { mutableStateOf(AppConfig.apiBaseUrl) }
    var selectedLanguage by remember { mutableStateOf("English") }
    var showLanguageMenu by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Authenticated) {
            onLoginSuccess((uiState as AuthUiState.Authenticated).user.role)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KisanSurfaceLight)
    ) {
        // Decorative top green area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(KisanGreenDark, KisanGreenPrimary)
                    )
                )
        ) {
            // Decorative subtle circles
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = (-40).dp, y = (-40).dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(KisanWhite.copy(alpha = 0.06f))
            )
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = 260.dp, y = 20.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(KisanWhite.copy(alpha = 0.06f))
            )

            // Logo + App Name (centered in top area)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(KisanWhite.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌾", fontSize = 40.sp)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "KisanProcure",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KisanWhite
                )
                Text(
                    text = "Smart Farmer Procurement System",
                    fontSize = 13.sp,
                    color = KisanWhite.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Main scroll content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Spacer to clear the decorative header
            Spacer(Modifier.height(200.dp))

            // Main Login Card — overlaps the green header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = KisanWhite)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Role Selector Tabs
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = KisanGreenDark
                    )
                    Text(
                        text = "Select your role to continue",
                        style = MaterialTheme.typography.bodySmall,
                        color = KisanTextMuted
                    )

                    Spacer(Modifier.height(16.dp))

                    // Demo Quick-Fill Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("🌾 Farmer", "ramesh@kisan.in", Icons.Default.Agriculture),
                            Triple("⚙️ Operator", "operator1@kisan.gov.in", Icons.Default.ManageAccounts),
                            Triple("🛡 Admin", "superadmin@kisan.gov.in", Icons.Default.AdminPanelSettings)
                        ).forEach { (label, demoEmail, _) ->
                            val isSelected = email == demoEmail
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        email = demoEmail
                                        password = "password123"
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) KisanGreenPrimary else KisanMintContainer,
                                border = if (isSelected) null else CardDefaults.outlinedCardBorder().copy(
                                    brush = SolidColor(KisanBorder)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) KisanWhite else KisanGreenDark,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Email / Mobile field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = KisanGreenPrimary, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KisanGreenPrimary,
                            unfocusedBorderColor = KisanBorder,
                            focusedLabelColor = KisanGreenPrimary,
                            cursorColor = KisanGreenPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = KisanGreenPrimary, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password",
                                    tint = KisanTextMuted
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            if (email.isNotBlank() && password.isNotBlank()) {
                                authViewModel.login(email.trim(), password)
                            }
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KisanGreenPrimary,
                            unfocusedBorderColor = KisanBorder,
                            focusedLabelColor = KisanGreenPrimary,
                            cursorColor = KisanGreenPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Error message
                    AnimatedVisibility(visible = uiState is AuthUiState.Error) {
                        if (uiState is AuthUiState.Error) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = KisanErrorBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = KisanError, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = (uiState as AuthUiState.Error).message,
                                        color = KisanError,
                                        style = MaterialTheme.typography.bodySmall,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Sign In Button
                    KisanPrimaryButton(
                        text = "Sign In  →",
                        onClick = { authViewModel.login(email.trim(), password) },
                        enabled = email.isNotBlank() && password.isNotBlank(),
                        isLoading = uiState is AuthUiState.Loading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Bottom configuration row: Language + Server
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Language selector
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLanguageMenu = true },
                        shape = RoundedCornerShape(12.dp),
                        color = KisanWhite,
                        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(KisanBorder))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🗣️", fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = selectedLanguage,
                                style = MaterialTheme.typography.labelMedium,
                                color = KisanGreenDark,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = KisanTextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                    DropdownMenu(expanded = showLanguageMenu, onDismissRequest = { showLanguageMenu = false }) {
                        listOf("ಕನ್ನಡ (Kannada)", "हिन्दी (Hindi)", "English").forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang) },
                                onClick = { selectedLanguage = lang.substringAfterLast(" ").removeSuffix(")").let { if (it.isEmpty()) lang else it }; showLanguageMenu = false }
                            )
                        }
                    }
                }

                // Server config
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { serverUrlInput = AppConfig.apiBaseUrl; showServerDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = KisanWhite,
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(KisanBorder))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌐", fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Server",
                            style = MaterialTheme.typography.labelMedium,
                            color = KisanGreenDark,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.Edit, contentDescription = null, tint = KisanTextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Ministry of Agriculture & Farmers Welfare • Digital India",
                style = MaterialTheme.typography.labelSmall,
                color = KisanTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    // Server Config Dialog
    if (showServerDialog) {
        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            title = { Text("Backend Server URL", fontWeight = FontWeight.Bold, color = KisanGreenDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Set the backend API endpoint:", style = MaterialTheme.typography.bodySmall, color = KisanTextMuted)
                    OutlinedTextField(
                        value = serverUrlInput,
                        onValueChange = { serverUrlInput = it },
                        label = { Text("Base URL") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KisanGreenPrimary, unfocusedBorderColor = KisanBorder),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = { serverUrlInput = AppConfig.LIVE_CLOUDFLARE_URL }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) { Text("☁️ Cloudflare", fontSize = 12.sp) }
                        FilledTonalButton(onClick = { serverUrlInput = AppConfig.USB_LOCAL_URL }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) { Text("🔌 USB Local", fontSize = 12.sp) }
                    }
                }
            },
            confirmButton = {
                KisanPrimaryButton(text = "Save", onClick = { AppConfig.setCustomUrl(serverUrlInput); showServerDialog = false }, modifier = Modifier.wrapContentWidth())
            },
            dismissButton = {
                TextButton(onClick = { showServerDialog = false }) { Text("Cancel", color = KisanTextMuted) }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
