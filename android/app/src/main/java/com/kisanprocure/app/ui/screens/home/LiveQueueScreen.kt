package com.kisanprocure.app.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kisanprocure.app.data.model.QueueEntry
import com.kisanprocure.app.ui.components.*
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

    LaunchedEffect(centreId) { queueViewModel.loadLiveQueue(centreId) }

    // Animated queue position
    val yourPosition = liveQueue?.yourPosition ?: 0
    val animatedPosition by animateIntAsState(
        targetValue = yourPosition,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "queue_pos"
    )

    Scaffold(
        containerColor = KisanSurfaceLight,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            KisanConnectionChip(isConnected = true)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                liveQueue?.centreName ?: "Live Queue",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = KisanGreenDark,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            "Updated just now",
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Hero status card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(3.dp),
                colors = CardDefaults.cardColors(containerColor = KisanGreenDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(KisanGreenDark, KisanGreenPrimary))
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Your position (animating)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Your Position",
                                style = MaterialTheme.typography.bodySmall,
                                color = KisanWhite.copy(alpha = 0.75f)
                            )
                            Text(
                                text = if (animatedPosition > 0) "#$animatedPosition" else "N/A",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = KisanWhite
                            )
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(60.dp)
                                .background(KisanWhite.copy(alpha = 0.2f))
                        )

                        // Estimated wait
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Estimated Wait",
                                style = MaterialTheme.typography.bodySmall,
                                color = KisanWhite.copy(alpha = 0.75f)
                            )
                            Text(
                                text = "~${liveQueue?.estimatedWaitMinutes ?: 15} min",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = KisanWhite
                            )
                        }
                    }
                }

                // "You'll be notified" footer
                if (yourPosition > 0) {
                    Surface(color = KisanGreenPrimary.copy(alpha = 0.6f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = KisanWhite, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "You'll be notified when it's your turn.",
                                style = MaterialTheme.typography.labelSmall,
                                color = KisanWhite
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Queue count pills
            liveQueue?.let { queue ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Live Token Roster",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = KisanTextDark
                    )
                    Surface(shape = RoundedCornerShape(12.dp), color = KisanMintContainer) {
                        Text(
                            text = "${queue.queueEntries.size} tokens",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = KisanGreenDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            if (isLoading && liveQueue == null) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(4) { KisanCardSkeleton() }
                }
            } else if (liveQueue?.queueEntries.isNullOrEmpty()) {
                KisanCard(modifier = Modifier.fillMaxWidth()) {
                    KisanEmptyState(
                        emoji = "🚜",
                        title = "Queue is Clear",
                        description = "No farmers currently in queue for this centre."
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(liveQueue!!.queueEntries) { _, entry ->
                        QueueEntryCard(entry = entry, isCurrentUser = liveQueue?.yourPosition == entry.queuePosition)
                    }
                }
            }
        }
    }
}

@Composable
fun QueueEntryCard(entry: QueueEntry, isCurrentUser: Boolean = false) {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = if (isCurrentUser && entry.status == "CALLED") 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val borderColor = when {
        isCurrentUser && entry.status == "CALLED" -> KisanStatusCalled
        isCurrentUser -> KisanGreenPrimary
        else -> android.graphics.Color.TRANSPARENT.let { androidx.compose.ui.graphics.Color.Transparent }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isCurrentUser) Modifier
                    .wrapContentWidth()
                else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(if (isCurrentUser) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCurrentUser && entry.status == "CALLED" -> KisanStatusCalledBg
                isCurrentUser -> KisanYourTurnBg
                else -> KisanWhite
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position number circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCurrentUser -> KisanGreenPrimary
                            entry.status == "PROCESSING" -> KisanStatusProcessing
                            entry.status == "CALLED" -> KisanStatusCalled
                            entry.status == "COMPLETED" -> KisanBorder
                            else -> KisanSurfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${entry.queuePosition}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = when {
                        isCurrentUser || entry.status in listOf("PROCESSING", "CALLED") -> KisanWhite
                        else -> KisanTextMuted
                    }
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.tokenNumber,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrentUser) KisanGreenDark else KisanTextDark
                    )
                    if (isCurrentUser) {
                        Spacer(Modifier.width(6.dp))
                        Surface(shape = RoundedCornerShape(6.dp), color = KisanGreenPrimary) {
                            Text(
                                text = "YOU",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = KisanWhite,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
                Text(
                    text = "${entry.farmerName ?: "Farmer"} • ${entry.cropName ?: "Produce"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = KisanTextMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                KisanStatusBadge(status = entry.status)
                entry.counterName?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = KisanGreenPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
