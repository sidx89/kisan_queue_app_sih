package com.kisanprocure.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kisanprocure.app.data.model.Booking
import com.kisanprocure.app.data.model.ProcurementCentre
import com.kisanprocure.app.ui.theme.*
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

    var selectedLanguage by remember { mutableStateOf("English") }
    var showLanguageMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = KisanSurfaceLight,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(KisanMintContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌾", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "KisanProcure",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = KisanGreenDark
                            )
                            Text(
                                "Karnataka APMC",
                                fontSize = 10.sp,
                                color = KisanTextMuted
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KisanWhite,
                    titleContentColor = KisanGreenDark
                ),
                actions = {
                    // Language Selector
                    Box {
                        Surface(
                            modifier = Modifier.clickable { showLanguageMenu = true },
                            shape = RoundedCornerShape(20.dp),
                            color = KisanMintContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🗣️", fontSize = 13.sp)
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    selectedLanguage,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KisanGreenDark
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = KisanGreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        DropdownMenu(expanded = showLanguageMenu, onDismissRequest = { showLanguageMenu = false }) {
                            DropdownMenuItem(text = { Text("ಕನ್ನಡ (Kannada)") }, onClick = { selectedLanguage = "ಕನ್ನಡ"; showLanguageMenu = false })
                            DropdownMenuItem(text = { Text("हिन्दी (Hindi)") }, onClick = { selectedLanguage = "हिन्दी"; showLanguageMenu = false })
                            DropdownMenuItem(text = { Text("English") }, onClick = { selectedLanguage = "English"; showLanguageMenu = false })
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = KisanGreenDark)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToBookSlot,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Book Slot", fontWeight = FontWeight.Bold) },
                containerColor = KisanGreenPrimary,
                contentColor = KisanWhite,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { padding ->
        if (isLoading && myBookings.isEmpty() && centres.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = KisanGreenPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Welcome banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(KisanGreenPrimary, KisanGreenMedium)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Good Day, Kisan! 🙏", color = KisanWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("Karnataka APMC Portal", color = KisanWhite.copy(alpha = 0.8f), fontSize = 13.sp)
                            }
                            Text("🌾", fontSize = 40.sp)
                        }
                    }
                }

                // Quick Actions
                item {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = KisanTextDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionCard(
                            title = "Book Slot",
                            subtitle = "Schedule visit",
                            icon = Icons.Default.CalendarToday,
                            iconBg = KisanGreenPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToBookSlot
                        )
                        QuickActionCard(
                            title = "Live Queue",
                            subtitle = "Check status",
                            icon = Icons.Default.People,
                            iconBg = KisanAmber,
                            modifier = Modifier.weight(1f),
                            onClick = { centres.firstOrNull()?.id?.let { onNavigateToLiveQueue(it) } }
                        )
                    }
                }

                // My Bookings
                item {
                    Text(
                        text = "My Active Bookings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = KisanTextDark
                    )
                }

                if (myBookings.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = KisanMintContainer),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📋", fontSize = 28.sp)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "No active bookings yet",
                                        fontWeight = FontWeight.SemiBold,
                                        color = KisanGreenDark
                                    )
                                    Text(
                                        "Tap 'Book Slot' to schedule a procurement visit.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KisanTextMuted
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(myBookings) { booking ->
                        BookingCard(
                            booking = booking,
                            onViewQr = { onNavigateToQrPass(booking.bookingToken) },
                            onViewQueue = { centres.firstOrNull()?.id?.let { onNavigateToLiveQueue(it) } }
                        )
                    }
                }

                // Nearby Centres Header
                item {
                    Text(
                        text = "Procurement Centres",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = KisanTextDark
                    )
                    Text(
                        text = "Karnataka APMC Network",
                        style = MaterialTheme.typography.bodySmall,
                        color = KisanTextMuted
                    )
                    Spacer(Modifier.height(4.dp))
                }

                items(centres) { centre ->
                    CentreCard(centre = centre, onClick = { onNavigateToLiveQueue(centre.id) })
                }

                item { Spacer(Modifier.height(72.dp)) } // FAB clearance
            }
        }
    }
}

@Composable
fun CentreCard(centre: ProcurementCentre, onClick: () -> Unit) {
    val isOpen = centre.isActive
    val statusColor = if (isOpen) Color(0xFF2E7D32) else Color(0xFFB71C1C)
    val statusBg = if (isOpen) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val statusLabel = if (isOpen) "OPEN" else "CLOSED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = KisanWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: name + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Left: name + location
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = centre.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = KisanTextDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = buildString {
                            val parts = listOfNotNull(
                                centre.location?.takeIf { it.isNotBlank() },
                                centre.district?.takeIf { it.isNotBlank() },
                                centre.state?.takeIf { it.isNotBlank() }
                            )
                            append(parts.joinToString(", "))
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = KisanTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(8.dp))
                // Status badge (fixed width, no wrap)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = KisanBorder, thickness = 0.5.dp)
            Spacer(Modifier.height(10.dp))

            // Queue counts row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QueueCountChip(
                    label = "Waiting",
                    count = centre.waitingCount ?: 0,
                    color = KisanAmber,
                    modifier = Modifier.weight(1f)
                )
                QueueCountChip(
                    label = "Processing",
                    count = centre.processingCount ?: 0,
                    color = KisanGreenPrimary,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = KisanMintContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = KisanGreenPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            "View Queue",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KisanGreenDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QueueCountChip(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = color
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = KisanWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = KisanWhite, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KisanTextDark)
            Text(subtitle, fontSize = 11.sp, color = KisanTextMuted)
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    onViewQr: () -> Unit,
    onViewQueue: () -> Unit
) {
    val (statusColor, statusBg) = when (booking.status) {
        "CONFIRMED"  -> Pair(Color(0xFF1565C0), Color(0xFFE3F2FD))
        "CHECKED_IN" -> Pair(Color(0xFF6A1B9A), Color(0xFFF3E5F5))
        "COMPLETED"  -> Pair(Color(0xFF2E7D32), Color(0xFFE8F5E9))
        "CANCELLED"  -> Pair(Color(0xFFB71C1C), Color(0xFFFFEBEE))
        else         -> Pair(KisanTextMuted, KisanMintContainer)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = KisanWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(KisanMintContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎫", fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = booking.bookingToken,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = KisanGreenDark
                        )
                        Text(
                            text = booking.centreName ?: "N/A",
                            style = MaterialTheme.typography.bodySmall,
                            color = KisanTextMuted
                        )
                    }
                }
                Surface(shape = RoundedCornerShape(20.dp), color = statusBg) {
                    Text(
                        text = booking.status,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = KisanBorder, thickness = 0.5.dp)
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Crop", fontSize = 10.sp, color = KisanTextMuted)
                    Text(
                        "${booking.cropName ?: "N/A"} (${booking.estimatedQuantityKg ?: 0.0} kg)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KisanTextDark
                    )
                }
                Column {
                    Text("Slot", fontSize = 10.sp, color = KisanTextMuted)
                    Text(
                        "${booking.slotDate ?: ""} ${booking.slotTime ?: ""}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KisanTextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onViewQueue,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = KisanGreenPrimary)
                ) {
                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Live Queue", fontSize = 13.sp)
                }
                Button(
                    onClick = onViewQr,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("QR Pass", fontSize = 13.sp)
                }
            }
        }
    }
}
