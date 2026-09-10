package com.kisanprocure.app.ui.screens.operator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kisanprocure.app.data.model.QueueEntry
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
        topBar = {
            TopAppBar(
                title = { Text("Operator Station — Centre #$centreId") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Check-in card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Farmer Token Check-In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
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
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (checkInToken.isNotBlank()) {
                                    queueViewModel.checkIn(checkInToken.trim())
                                    checkInToken = ""
                                }
                            },
                            enabled = checkInToken.isNotBlank() && !isLoading
                        ) {
                            Text("Check In")
                        }
                    }

                    if (checkInSuccess != null) {
                        Text(
                            text = "✓ Checked in token: ${checkInSuccess!!.tokenNumber}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Quick Call Next Action
            Button(
                onClick = { queueViewModel.callNext(centreId, counterId = 1) },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("📢 Call Next Token (Counter 1)")
            }

            Text("Current Queue Roster", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            if (isLoading && liveQueue == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("#${entry.queuePosition} — ${entry.tokenNumber}", fontWeight = FontWeight.Bold)
                Text("${entry.farmerName ?: "Farmer"} (${entry.cropName ?: ""})", style = MaterialTheme.typography.bodySmall)
                Text("Status: ${entry.status}", style = MaterialTheme.typography.labelSmall)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (entry.status == "CALLED") {
                    Button(onClick = { onStatusChange("PROCESSING") }, contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Text("Process", fontSize = 12.sp)
                    }
                }
                if (entry.status == "PROCESSING") {
                    Button(onClick = { onStatusChange("COMPLETED") }, contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Text("Done", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
