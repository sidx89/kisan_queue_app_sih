package com.kisanprocure.app.ui.screens.operator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kisanprocure.app.data.model.QueueEntry
import com.kisanprocure.app.ui.theme.*
import com.kisanprocure.app.ui.viewmodel.QueueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorDashboardScreen(
    centreId: Int,
    queueViewModel: QueueViewModel,
    onBack: () -> Unit
) {
    var checkInToken by remember { mutableStateOf("") }
    val liveQueue by queueViewModel.liveQueue.collectAsState()
    val isLoading by queueViewModel.isLoading.collectAsState()
    val checkInSuccess by queueViewModel.checkInSuccess.collectAsState()
    val errorMessage by queueViewModel.errorMessage.collectAsState()

    LaunchedEffect(centreId) {
        queueViewModel.loadLiveQueue(centreId)
    }

    Scaffold(
        containerColor = KisanSurfaceLight,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Operator Control Station", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = KisanGreenDark)
                        Text("Centre ID #$centreId • Counter 1", fontSize = 11.sp, color = KisanTextMuted)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = KisanGreenDark)
                    }
                },
                actions = {
                    IconButton(onClick = { queueViewModel.loadLiveQueue(centreId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = KisanGreenDark)
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Check-in card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KisanWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(KisanMintContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = KisanGreenPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Farmer Token Check-In", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KisanGreenDark)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = checkInToken,
                            onValueChange = { checkInToken = it },
                            placeholder = { Text("e.g. TOK-APMC-001") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KisanGreenPrimary,
                                unfocusedBorderColor = KisanBorder,
                                focusedLabelColor = KisanGreenPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (checkInToken.isNotBlank()) {
                                    queueViewModel.checkIn(checkInToken.trim())
                                    checkInToken = ""
                                }
                            },
                            enabled = checkInToken.isNotBlank() && !isLoading,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary)
                        ) {
                            Text("Check-In", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (checkInSuccess != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = KisanMintContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KisanGreenPrimary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Checked in: ${checkInSuccess!!.tokenNumber}",
                                    color = KisanGreenDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFEBEE),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = KisanError, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = errorMessage!!,
                                    color = KisanError,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Quick Call Next Action Button
            Button(
                onClick = { queueViewModel.callNext(centreId, counterId = 1) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = KisanWhite)
                Spacer(Modifier.width(8.dp))
                Text("📢 Call Next Token (Counter 1)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Current Station Queue", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KisanTextDark)
                liveQueue?.queueEntries?.size?.let {
                    Text("$it tokens", fontSize = 12.sp, color = KisanTextMuted)
                }
            }

            if (isLoading && liveQueue == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = KisanGreenPrimary)
                }
            } else if (liveQueue?.queueEntries.isNullOrEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = KisanWhite)
                ) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No tokens waiting at this station", color = KisanTextMuted)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(liveQueue?.queueEntries ?: emptyList()) { entry ->
                        OperatorQueueCard(
                            entry = entry,
                            onStatusChange = { newStatus ->
                                queueViewModel.updateStatus(entry.id, newStatus)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OperatorQueueCard(
    entry: QueueEntry,
    onStatusChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = KisanWhite),
        elevation = CardDefaults.cardElevation(1.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(KisanMintContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("#${entry.queuePosition}", fontWeight = FontWeight.ExtraBold, color = KisanGreenDark, fontSize = 14.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(entry.tokenNumber, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KisanTextDark)
                    Text("${entry.farmerName ?: "Farmer"} • ${entry.cropName ?: ""}", style = MaterialTheme.typography.bodySmall, color = KisanTextMuted)
                    Text(
                        "Status: ${entry.status}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (entry.status) {
                            "CALLED" -> KisanError
                            "PROCESSING" -> KisanGreenDark
                            else -> KisanAmber
                        }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (entry.status == "CALLED") {
                    Button(
                        onClick = { onStatusChange("PROCESSING") },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Process", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (entry.status == "PROCESSING") {
                    Button(
                        onClick = { onStatusChange("COMPLETED") },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Complete", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
