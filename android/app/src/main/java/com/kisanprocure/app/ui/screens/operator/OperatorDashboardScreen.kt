package com.kisanprocure.app.ui.screens.operator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kisanprocure.app.data.model.QueueEntry
import com.kisanprocure.app.ui.components.*
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
    var showManualInput by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Queue") }

    val liveQueue by queueViewModel.liveQueue.collectAsState()
    val isLoading by queueViewModel.isLoading.collectAsState()
    val checkInSuccess by queueViewModel.checkInSuccess.collectAsState()
    val errorMessage by queueViewModel.errorMessage.collectAsState()

    LaunchedEffect(centreId) {
        queueViewModel.loadLiveQueue(centreId)
    }

    // Determine the next token to call
    val nextWaitingEntry = liveQueue?.queueEntries?.firstOrNull { it.status == "WAITING" }
    val currentlyCalledEntry = liveQueue?.queueEntries?.firstOrNull { it.status == "CALLED" }
    val displayNextToken = currentlyCalledEntry?.tokenNumber ?: nextWaitingEntry?.tokenNumber ?: "#104"

    Scaffold(
        containerColor = KisanSurfaceLight,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Operator Control Station",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = KisanGreenDark
                            )
                            Spacer(Modifier.width(8.dp))
                            KisanConnectionChip(isConnected = true)
                        }
                        Text(
                            "Centre #$centreId • Counter 1 • Ravi Kumar (Operator)",
                            style = MaterialTheme.typography.labelSmall,
                            color = KisanTextMuted
                        )
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
        },
        bottomBar = {
            NavigationBar(
                containerColor = KisanWhite,
                contentColor = KisanGreenPrimary,
                tonalElevation = 0.dp
            ) {
                listOf(
                    Triple("Queue", Icons.Default.People, "Queue"),
                    Triple("Weighment", Icons.Default.Scale, "Weighment"),
                    Triple("Quality", Icons.Default.Verified, "Quality"),
                    Triple("Payments", Icons.Default.CurrencyRupee, "Payments")
                ).forEach { (label, icon, route) ->
                    val isSelected = selectedTab == route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = route },
                        icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp)) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KisanGreenPrimary,
                            selectedTextColor = KisanGreenPrimary,
                            indicatorColor = KisanMintContainer,
                            unselectedIconColor = KisanTextMuted,
                            unselectedTextColor = KisanTextMuted
                        )
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Next Token Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(3.dp),
                colors = CardDefaults.cardColors(containerColor = KisanWhite)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(KisanGreenDark, KisanGreenPrimary))
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "NEXT TOKEN",
                                style = MaterialTheme.typography.labelSmall,
                                color = KisanWhite.copy(alpha = 0.75f),
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = displayNextToken,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = KisanWhite
                            )
                        }

                        Button(
                            onClick = { queueViewModel.callNext(centreId, counterId = 1) },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KisanWhite,
                                contentColor = KisanGreenDark
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = KisanGreenDark, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Call Token", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Scan QR / Manual Token Check-In Section
            KisanCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Scan QR Code",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = KisanGreenDark
                        )
                        TextButton(
                            onClick = { showManualInput = !showManualInput },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                if (showManualInput) "Cancel" else "Enter Manually",
                                style = MaterialTheme.typography.labelSmall,
                                color = KisanGreenPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (!showManualInput) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, KisanBorder, RoundedCornerShape(12.dp))
                                .background(KisanSurfaceVariant)
                                .clickable { showManualInput = true }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
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
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Tap to scan or enter token manually",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = KisanTextDark
                                    )
                                    Text(
                                        "Verify farmer digital pass on entry",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = KisanTextMuted
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(Modifier.height(8.dp))
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
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            KisanPrimaryButton(
                                text = "Check-In",
                                onClick = {
                                    if (checkInToken.isNotBlank()) {
                                        queueViewModel.checkIn(checkInToken.trim())
                                        checkInToken = ""
                                        showManualInput = false
                                    }
                                },
                                enabled = checkInToken.isNotBlank() && !isLoading,
                                modifier = Modifier.height(52.dp)
                            )
                        }
                    }

                    if (checkInSuccess != null) {
                        Spacer(Modifier.height(8.dp))
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
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = KisanErrorBg,
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
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            // Current Queue Roster Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Current Queue",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = KisanTextDark
                )
                liveQueue?.queueEntries?.size?.let { count ->
                    Text(
                        "$count tokens",
                        style = MaterialTheme.typography.labelSmall,
                        color = KisanTextMuted
                    )
                }
            }

            // Queue list
            if (isLoading && liveQueue == null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { KisanCardSkeleton() }
                }
            } else if (liveQueue?.queueEntries.isNullOrEmpty()) {
                KisanCard(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(28.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No tokens in queue at this station", style = MaterialTheme.typography.bodyMedium, color = KisanTextMuted)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(liveQueue?.queueEntries ?: emptyList()) { entry ->
                        OperatorQueueRow(
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
fun OperatorQueueRow(
    entry: QueueEntry,
    onStatusChange: (String) -> Unit
) {
    KisanCard(modifier = Modifier.fillMaxWidth(), elevation = 1.dp) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when (entry.status) {
                                "CALLED" -> KisanStatusCalledBg
                                "PROCESSING" -> KisanStatusProcessingBg
                                "COMPLETED" -> KisanMintContainer
                                else -> KisanSurfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "#${entry.queuePosition}",
                        fontWeight = FontWeight.ExtraBold,
                        color = when (entry.status) {
                            "CALLED" -> KisanStatusCalled
                            "PROCESSING" -> KisanStatusProcessing
                            "COMPLETED" -> KisanGreenDark
                            else -> KisanTextMuted
                        },
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        entry.tokenNumber,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = KisanTextDark
                    )
                    Text(
                        "${entry.farmerName ?: "Farmer"} • ${entry.cropName ?: "Produce"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = KisanTextMuted
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KisanStatusBadge(status = entry.status)

                if (entry.status == "CALLED") {
                    Button(
                        onClick = { onStatusChange("PROCESSING") },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Process", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (entry.status == "PROCESSING") {
                    Button(
                        onClick = { onStatusChange("COMPLETED") },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KisanBlue),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Complete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
