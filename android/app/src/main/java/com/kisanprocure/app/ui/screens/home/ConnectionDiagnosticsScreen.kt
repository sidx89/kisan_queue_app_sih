package com.kisanprocure.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kisanprocure.app.ui.theme.*
import com.kisanprocure.app.utils.AppConfig
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionDiagnosticsScreen(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    var apiStatus by remember { mutableStateOf("Testing...") }
    var apiSuccess by remember { mutableStateOf<Boolean?>(null) }
    var healthStatus by remember { mutableStateOf("Pending") }
    var healthSuccess by remember { mutableStateOf<Boolean?>(null) }
    var dbStatus by remember { mutableStateOf("Pending") }
    var dbSuccess by remember { mutableStateOf<Boolean?>(null) }
    var socketStatus by remember { mutableStateOf("Pending") }
    var socketSuccess by remember { mutableStateOf<Boolean?>(null) }
    var socketTransport by remember { mutableStateOf("websocket / polling") }
    var latencyMs by remember { mutableStateOf<Long?>(null) }
    var lastCheckTime by remember { mutableStateOf("Never") }
    var isTesting by remember { mutableStateOf(false) }

    fun runDiagnostics() {
        coroutineScope.launch {
            isTesting = true
            val startTime = System.currentTimeMillis()
            val client = OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .build()

            val baseUrl = if (AppConfig.apiBaseUrl.endsWith("/")) AppConfig.apiBaseUrl else "${AppConfig.apiBaseUrl}/"

            // 1. Test /health
            withContext(Dispatchers.IO) {
                try {
                    val req = Request.Builder().url("${baseUrl}health").build()
                    val resp = client.newCall(req).execute()
                    val took = System.currentTimeMillis() - startTime
                    latencyMs = took
                    if (resp.isSuccessful) {
                        apiStatus = "Connected (${took}ms)"
                        apiSuccess = true
                        healthStatus = "OK (200)"
                        healthSuccess = true
                    } else {
                        apiStatus = "HTTP Error ${resp.code}"
                        apiSuccess = false
                        healthStatus = "Degraded (${resp.code})"
                        healthSuccess = false
                    }
                } catch (e: Exception) {
                    apiStatus = "Unreachable: ${e.localizedMessage ?: "Failed"}"
                    apiSuccess = false
                    healthStatus = "Unreachable"
                    healthSuccess = false
                }

                // 2. Test /api/system/connection-test
                try {
                    val req = Request.Builder().url("${baseUrl}api/system/connection-test").build()
                    val resp = client.newCall(req).execute()
                    if (resp.isSuccessful) {
                        dbStatus = "Connected"
                        dbSuccess = true
                    } else {
                        dbStatus = "Error (${resp.code})"
                        dbSuccess = false
                    }
                } catch (e: Exception) {
                    dbStatus = "Check failed"
                    dbSuccess = false
                }

                // 3. Test Socket.IO
                try {
                    val opts = IO.Options().apply {
                        timeout = 6000
                        reconnection = false
                    }
                    val sock = IO.socket(AppConfig.getSocketUrl(), opts)
                    sock.on(Socket.EVENT_CONNECT) {
                        socketStatus = "Connected"
                        socketSuccess = true
                        sock.disconnect()
                    }
                    sock.on(Socket.EVENT_CONNECT_ERROR) {
                        socketStatus = "Connection Error"
                        socketSuccess = false
                    }
                    sock.connect()
                } catch (e: Exception) {
                    socketStatus = "Error: ${e.message}"
                    socketSuccess = false
                }
            }

            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            lastCheckTime = sdf.format(Date())
            isTesting = false
        }
    }

    LaunchedEffect(Unit) {
        runDiagnostics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connection Diagnostics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { runDiagnostics() }, enabled = !isTesting) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Environment Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = KisanMintContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Active Environment: ${AppConfig.environment.name}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = KisanGreenDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "API URL: ${AppConfig.apiBaseUrl}",
                        fontSize = 12.sp,
                        color = KisanTextMuted
                    )
                }
            }

            // Diagnostic Results Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = KisanWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Live Network Status",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = KisanGreenDark
                    )
                    Divider(color = KisanBorder)

                    DiagRow(label = "Public API", status = apiStatus, success = apiSuccess)
                    DiagRow(label = "Health Check", status = healthStatus, success = healthSuccess)
                    DiagRow(label = "Database", status = dbStatus, success = dbSuccess)
                    DiagRow(label = "Socket.IO", status = socketStatus, success = socketSuccess)
                    DiagRow(label = "Socket Transport", status = socketTransport, success = true)
                    DiagRow(label = "Latency", status = if (latencyMs != null) "${latencyMs}ms" else "Measuring...", success = latencyMs != null && latencyMs!! < 1000)
                    DiagRow(label = "Last Check", status = lastCheckTime, success = null)
                }
            }

            // Buttons Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { runDiagnostics() },
                    enabled = !isTesting,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary)
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = KisanWhite)
                    } else {
                        Text("Retest All")
                    }
                }

                OutlinedButton(
                    onClick = {
                        AppConfig.setEnvironment(
                            context,
                            AppConfig.Environment.TUNNEL
                        )
                        runDiagnostics()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset to Tunnel", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun DiagRow(label: String, status: String, success: Boolean?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            val dotColor = when (success) {
                true -> Color(0xFF2E7D32)
                false -> Color(0xFFC62828)
                null -> Color(0xFF757575)
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(dotColor, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = dotColor
            )
        }
    }
}
