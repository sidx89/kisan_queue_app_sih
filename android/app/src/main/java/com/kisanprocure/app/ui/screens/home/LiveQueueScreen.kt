package com.kisanprocure.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kisanprocure.app.data.model.QueueEntry
import com.kisanprocure.app.ui.theme.*
import com.kisanprocure.app.ui.viewmodel.QueueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveQueueScreen(
    centreId: Int,
    queueViewModel: QueueViewModel,
    onBack: () -> Unit
) {
    val liveQueue by queueViewModel.liveQueue.collectAsState()
    val isLoading by queueViewModel.isLoading.collectAsState()

    LaunchedEffect(centreId) {
        queueViewModel.loadLiveQueue(centreId)
    }

    Scaffold(
        containerColor = KisanSurfaceLight,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            liveQueue?.centreName ?: "Live Queue Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = KisanGreenDark
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(KisanGreenPrimary)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Real-Time Connected", fontSize = 11.sp, color = KisanGreenDark)
                        }
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Live Status Banner with gradient
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(3.dp),
                colors = CardDefaults.cardColors(containerColor = KisanWhite)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(KisanGreenPrimary, KisanGreenMedium)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = KisanWhite.copy(alpha = 0.9f), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Estimated Wait Time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KisanWhite.copy(alpha = 0.9f)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${liveQueue?.estimatedWaitMinutes ?: 15} mins",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = KisanWhite
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Tag, contentDescription = null, tint = KisanWhite.copy(alpha = 0.9f), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Your Queue Position",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KisanWhite.copy(alpha = 0.9f)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if ((liveQueue?.yourPosition ?: 0) > 0) "#${liveQueue?.yourPosition}" else "Not Queued",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = KisanWhite
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Token Roster",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KisanTextDark
                )
                liveQueue?.queueEntries?.size?.let { count ->
                    Surface(shape = RoundedCornerShape(12.dp), color = KisanMintContainer) {
                        Text(
                            text = "$count in queue",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = KisanGreenDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isLoading && liveQueue == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = KisanGreenPrimary)
                }
            } else if (liveQueue?.queueEntries.isNullOrEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KisanWhite)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🚜", fontSize = 40.sp)
                        Spacer(Modifier.height(10.dp))
                        Text("Queue is Clear", fontWeight = FontWeight.Bold, color = KisanTextDark)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "No farmers currently in queue for this centre.",
                            style = MaterialTheme.typography.bodySmall,
                            color = KisanTextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(liveQueue!!.queueEntries) { entry ->
                        QueueItemCard(entry)
                    }
                }
            }
        }
    }
}

@Composable
fun QueueItemCard(entry: QueueEntry) {
    val (statusColor, statusBg) = when (entry.status) {
        "CALLED"     -> Pair(KisanError, Color(0xFFFFEBEE))
        "PROCESSING" -> Pair(KisanGreenDark, KisanMintContainer)
        "WAITING"    -> Pair(KisanAmber, Color(0xFFFFF3E0))
        "COMPLETED"  -> Pair(Color(0xFF1565C0), Color(0xFFE3F2FD))
        else         -> Pair(KisanTextMuted, Color(0xFFF5F5F5))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = KisanWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KisanMintContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${entry.queuePosition}",
                        color = KisanGreenDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = entry.tokenNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = KisanTextDark
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${entry.farmerName ?: "Farmer"} • ${entry.cropName ?: "Produce"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = KisanTextMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = entry.status,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor
                    )
                }
                entry.counterName?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "📍 $it",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = KisanGreenPrimary
                    )
                }
            }
        }
    }
}
