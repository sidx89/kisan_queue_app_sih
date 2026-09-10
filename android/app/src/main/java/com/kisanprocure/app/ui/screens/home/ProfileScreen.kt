package com.kisanprocure.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = currentUser?.name ?: "Farmer Account",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentUser?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Role: ${currentUser?.role ?: "FARMER"}", style = MaterialTheme.typography.bodySmall)
                    Text("Phone: ${currentUser?.phone ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                    currentUser?.farmLocation?.let {
                        Text("Location: $it", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Text(
                text = "Backend Server Environment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AppConfig.Environment.values().forEach { env ->
                    FilterChip(
                        selected = selectedEnv == env,
                        onClick = {
                            selectedEnv = env
                            AppConfig.setEnvironment(context, env)
                            currentUrl = AppConfig.apiBaseUrl
                        },
                        label = { Text(env.name) }
                    )
                }
            }

            OutlinedTextField(
                value = currentUrl,
                onValueChange = {
                    currentUrl = it
                    AppConfig.setCustomUrl(it)
                },
                label = { Text("API Base URL") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = onNavigateToDiagnostics,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("📡 Connection Diagnostics")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    authViewModel.logout()
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Log Out")
            }
        }
    }
}
