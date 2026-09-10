package com.kisanprocure.app.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kisanprocure.app.data.model.Booking
import com.kisanprocure.app.ui.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    bookingViewModel: BookingViewModel,
    onNavigateToBookSlot: () -> Unit,
    onNavigateToLiveQueue: (centreId: Int) -> Unit,
    onNavigateToQrPass: (token: String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val myBookings by bookingViewModel.myBookings.collectAsState()
    val centres by bookingViewModel.centres.collectAsState()
    val isLoading by bookingViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        bookingViewModel.loadInitialData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🌾 KisanProcure Home") },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToBookSlot,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Book Slot") }
            )
        }
    ) { padding ->
        if (isLoading && myBookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionCard(
                            title = "Book Slot",
                            icon = Icons.Default.CalendarToday,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToBookSlot
                        )
                        QuickActionCard(
                            title = "Live Queue",
                            icon = Icons.Default.People,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                centres.firstOrNull()?.id?.let { onNavigateToLiveQueue(it) }
                            }
                        )
                    }
                }

                item {
                    Text(
                        text = "My Active Bookings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (myBookings.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = "No active bookings found. Tap 'Book Slot' to schedule a procurement visit.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(myBookings) { booking ->
                        BookingCard(
                            booking = booking,
                            onViewQr = { onNavigateToQrPass(booking.bookingToken) },
                            onViewQueue = {
                                centres.firstOrNull()?.id?.let { onNavigateToLiveQueue(it) }
                            }
                        )
                    }
                }

                item {
                    Text(
                        text = "Nearby Procurement Centres",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(centres) { centre ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToLiveQueue(centre.id) },
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = centre.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Surface(
                                    color = if (centre.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = if (centre.isActive) "OPEN" else "CLOSED",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${centre.location}, ${centre.district}, ${centre.state}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Waiting: ${centre.waitingCount ?: 0}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Processing: ${centre.processingCount ?: 0}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    onViewQr: () -> Unit,
    onViewQueue: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Token: ${booking.bookingToken}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Surface(
                    color = when (booking.status) {
                        "CONFIRMED" -> MaterialTheme.colorScheme.primaryContainer
                        "CHECKED_IN" -> MaterialTheme.colorScheme.tertiaryContainer
                        "COMPLETED" -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = booking.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Centre: ${booking.centreName ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
            Text("Crop: ${booking.cropName ?: "N/A"} (${booking.estimatedQuantityKg ?: 0.0} kg)", style = MaterialTheme.typography.bodySmall)
            Text("Slot: ${booking.slotDate ?: ""} ${booking.slotTime ?: ""}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onViewQueue) {
                    Text("Live Queue")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onViewQr) {
                    Text("View QR Pass")
                }
            }
        }
    }
}
