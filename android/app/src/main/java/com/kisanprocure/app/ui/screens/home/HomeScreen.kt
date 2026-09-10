package com.kisanprocure.app.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import com.kisanprocure.app.ui.components.*
import com.kisanprocure.app.ui.theme.*
import com.kisanprocure.app.ui.viewmodel.AuthViewModel
import com.kisanprocure.app.ui.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    bookingViewModel: BookingViewModel,
    authViewModel: AuthViewModel,
    onNavigateToBookSlot: () -> Unit,
    onNavigateToLiveQueue: (centreId: Int) -> Unit,
    onNavigateToQrPass: (token: String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val myBookings by bookingViewModel.myBookings.collectAsState()
    val centres by bookingViewModel.centres.collectAsState()
    val isLoading by bookingViewModel.isLoading.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) { bookingViewModel.loadInitialData() }

    var selectedLanguage by remember { mutableStateOf("English") }
    var showLanguageMenu by remember { mutableStateOf(false) }

    val firstName = currentUser?.name?.substringBefore(" ") ?: "Kisan"
    val activeBooking = myBookings.firstOrNull { it.displayStatus in listOf("CONFIRMED", "CHECKED_IN", "WAITING") }

    Scaffold(
        containerColor = KisanSurfaceLight,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(KisanMintContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👨‍🌾", fontSize = 20.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Good Day, $firstName! 👋",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = KisanGreenDark
                            )
                            Text(
                                text = "Karnataka APMC Network",
                                style = MaterialTheme.typography.labelSmall,
                                color = KisanTextMuted
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KisanWhite),
                actions = {
                    Box {
                        IconButton(onClick = { showLanguageMenu = true }) {
                            Text("🗣️", fontSize = 16.sp)
                        }
                        DropdownMenu(expanded = showLanguageMenu, onDismissRequest = { showLanguageMenu = false }) {
                            listOf("ಕನ್ನಡ", "हिन्दी", "English").forEach { lang ->
                                DropdownMenuItem(text = { Text(lang) }, onClick = { selectedLanguage = lang; showLanguageMenu = false })
                            }
                        }
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = KisanGreenDark, modifier = Modifier.size(22.dp))
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
                shape = RoundedCornerShape(18.dp)
            )
        }
    ) { padding ->
        if (isLoading && myBookings.isEmpty() && centres.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) { KisanCardSkeleton() }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Active Booking Hero ──────────────────────────────────────
                if (activeBooking != null) {
                    item {
                        ActiveBookingHeroCard(
                            booking = activeBooking,
                            onViewQr = { onNavigateToQrPass(activeBooking.displayToken) },
                            onViewQueue = { centres.firstOrNull()?.id?.let { onNavigateToLiveQueue(it) } }
                        )
                    }
                }

                // ── Quick Actions ────────────────────────────────────────────
                item {
                    KisanSectionHeader(title = "Quick Actions")
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            Triple("📅", "Book Slot", onNavigateToBookSlot),
                            Triple("👥", "Live Queue") { centres.firstOrNull()?.id?.let { onNavigateToLiveQueue(it) }; Unit },
                            Triple("🏪", "Centres") { centres.firstOrNull()?.id?.let { onNavigateToLiveQueue(it) }; Unit },
                            Triple("📄", "History", onNavigateToProfile)
                        ).forEach { (emoji, label, action) ->
                            QuickActionTile(
                                emoji = emoji,
                                label = label,
                                modifier = Modifier.weight(1f),
                                onClick = action
                            )
                        }
                    }
                }

                // ── My Bookings ──────────────────────────────────────────────
                item {
                    KisanSectionHeader(title = "My Bookings")
                }

                if (myBookings.isEmpty()) {
                    item {
                        KisanCard(modifier = Modifier.fillMaxWidth()) {
                            KisanEmptyState(
                                emoji = "📋",
                                title = "No Bookings Yet",
                                description = "Book a procurement slot to receive your guaranteed entry token.",
                                actionLabel = "Book Slot",
                                onAction = onNavigateToBookSlot
                            )
                        }
                    }
                } else {
                    items(myBookings) { booking ->
                        if (booking != activeBooking) { // active booking already shown in hero
                            BookingListCard(
                                booking = booking,
                                onViewQr = { onNavigateToQrPass(booking.displayToken) },
                                onViewQueue = { centres.firstOrNull()?.id?.let { onNavigateToLiveQueue(it) } }
                            )
                        }
                    }
                }

                // ── Procurement Centres ──────────────────────────────────────
                item {
                    KisanSectionHeader(title = "Procurement Centres")
                    Spacer(Modifier.height(4.dp))
                    Text("Karnataka APMC Network", style = MaterialTheme.typography.bodySmall, color = KisanTextMuted)
                }

                items(centres) { centre ->
                    ProcurementCentreCard(
                        centre = centre,
                        onClick = { onNavigateToLiveQueue(centre.id) }
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ACTIVE BOOKING HERO CARD
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ActiveBookingHeroCard(
    booking: Booking,
    onViewQr: () -> Unit,
    onViewQueue: () -> Unit
) {
    KisanCard(elevation = 4.dp) {
        // Green gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(KisanGreenDark, KisanGreenPrimary))
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "YOUR NEXT VISIT",
                        style = MaterialTheme.typography.labelSmall,
                        color = KisanWhite.copy(alpha = 0.75f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = booking.displayToken,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = KisanWhite
                    )
                }
                KisanStatusBadge(status = booking.displayStatus)
            }
        }

        // Body
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Store, contentDescription = null, tint = KisanGreenPrimary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = booking.centreName ?: "Procurement Centre",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = KisanTextDark
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = KisanTextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "${booking.displayDate} • ${booking.displayTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = KisanTextMuted
                )
            }
            Spacer(Modifier.height(12.dp))

            // Progress timeline
            BookingTimeline(status = booking.displayStatus)

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KisanOutlineButton(
                    text = "View QR",
                    onClick = onViewQr,
                    icon = Icons.Default.QrCode,
                    modifier = Modifier.weight(1f)
                )
                KisanPrimaryButton(
                    text = "Live Queue",
                    onClick = onViewQueue,
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// BOOKING TIMELINE PROGRESS
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun BookingTimeline(status: String) {
    val steps = listOf("Booked", "Arrived", "Processing", "Done")
    val currentStep = when (status) {
        "CONFIRMED"  -> 0
        "CHECKED_IN" -> 1
        "PROCESSING" -> 2
        "COMPLETED"  -> 3
        else         -> 0
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        steps.forEachIndexed { index, step ->
            val done = index <= currentStep
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (done) KisanGreenPrimary else KisanBorder),
                    contentAlignment = Alignment.Center
                ) {
                    if (done) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = KisanWhite, modifier = Modifier.size(12.dp))
                    }
                }
                Text(step, style = MaterialTheme.typography.labelSmall, color = if (done) KisanGreenPrimary else KisanTextMuted)
            }
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (index < currentStep) KisanGreenPrimary else KisanBorder)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// QUICK ACTION TILE
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun QuickActionTile(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    KisanCard(modifier = modifier, onClick = onClick, elevation = 1.dp) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = KisanGreenDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// BOOKING LIST CARD (non-active bookings)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun BookingListCard(
    booking: Booking,
    onViewQr: () -> Unit,
    onViewQueue: () -> Unit
) {
    KisanCard(modifier = Modifier.fillMaxWidth(), elevation = 1.dp) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(KisanMintContainer),
                contentAlignment = Alignment.Center
            ) {
                Text("🎫", fontSize = 22.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.displayToken, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = KisanGreenDark)
                Text(
                    text = "${booking.centreName ?: "Centre"} • ${booking.displayDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = KisanTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${booking.displayCrop} • ${booking.displayQuantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = KisanTextMuted
                )
            }
            KisanStatusBadge(status = booking.displayStatus)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// PROCUREMENT CENTRE CARD
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ProcurementCentreCard(
    centre: ProcurementCentre,
    onClick: () -> Unit
) {
    val statusLabel = centre.displayStatus

    KisanCard(modifier = Modifier.fillMaxWidth(), onClick = onClick, elevation = 2.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KisanMintContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏪", fontSize = 22.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = centre.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = KisanTextDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = buildString {
                            val parts = listOfNotNull(
                                centre.displayLocation.takeIf { it.isNotBlank() },
                                centre.district?.takeIf { it.isNotBlank() && it != centre.displayLocation }
                            )
                            append(if (parts.isNotEmpty()) parts.joinToString(", ") else "Karnataka APMC")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = KisanTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(8.dp))
                KisanStatusBadge(status = statusLabel)
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = KisanDivider, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            // Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CentreMetric(
                    value = "${centre.waitingCount ?: centre.currentQueue ?: 0}",
                    label = "Waiting",
                    valueColor = KisanAmber
                )
                VerticalDivider(modifier = Modifier.height(32.dp), color = KisanDivider)
                CentreMetric(
                    value = "${centre.processingCount ?: 0}",
                    label = "Processing",
                    valueColor = KisanStatusProcessing
                )
                VerticalDivider(modifier = Modifier.height(32.dp), color = KisanDivider)
                CentreMetric(
                    value = "${centre.displayCapacity}",
                    label = "Capacity",
                    valueColor = KisanGreenPrimary
                )
                VerticalDivider(modifier = Modifier.height(32.dp), color = KisanDivider)
                CentreMetric(
                    value = centre.displayOperatingHours.substringBefore(" - ").substringBefore(":").trim().let { if (it.isNotBlank()) "$it:00" else "08:30" },
                    label = "Opens",
                    valueColor = KisanTextSecondary
                )
            }

            Spacer(Modifier.height(12.dp))

            // View Centre button-like row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View Queue  →",
                    style = MaterialTheme.typography.labelMedium,
                    color = KisanGreenPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CentreMetric(value: String, label: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = KisanTextMuted
        )
    }
}
