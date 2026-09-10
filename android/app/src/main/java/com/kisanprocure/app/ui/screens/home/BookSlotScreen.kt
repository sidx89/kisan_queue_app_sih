package com.kisanprocure.app.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kisanprocure.app.data.model.Slot
import com.kisanprocure.app.ui.components.*
import com.kisanprocure.app.ui.theme.*
import com.kisanprocure.app.ui.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSlotScreen(
    bookingViewModel: BookingViewModel,
    onBack: () -> Unit,
    onBookingSuccess: (token: String) -> Unit
) {
    val centres by bookingViewModel.centres.collectAsState()
    val crops by bookingViewModel.crops.collectAsState()
    val slots by bookingViewModel.slots.collectAsState()
    val isLoading by bookingViewModel.isLoading.collectAsState()
    val bookingSuccess by bookingViewModel.bookingSuccess.collectAsState()
    val errorMessage by bookingViewModel.errorMessage.collectAsState()

    // Step state
    var currentStep by remember { mutableStateOf(1) }
    var selectedCentreId by remember { mutableStateOf<Int?>(null) }
    var selectedCropId by remember { mutableStateOf<Int?>(null) }
    var selectedSlotId by remember { mutableStateOf<Int?>(null) }
    var quantityKg by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { bookingViewModel.loadInitialData() }

    LaunchedEffect(selectedCentreId) {
        selectedCentreId?.let { bookingViewModel.loadSlots(it) }
    }

    LaunchedEffect(bookingSuccess) {
        bookingSuccess?.let {
            onBookingSuccess(it.displayToken)
            bookingViewModel.clearBookingSuccess()
        }
    }

    val selectedCentre = centres.firstOrNull { it.id == selectedCentreId }
    val selectedCrop = crops.firstOrNull { it.id == selectedCropId }
    val selectedSlot = slots.firstOrNull { it.id == selectedSlotId }

    Scaffold(
        containerColor = KisanSurfaceLight,
        topBar = {
            KisanTopBar(
                title = "Book Slot",
                subtitle = "Step $currentStep of 4",
                onBack = {
                    if (currentStep > 1) currentStep-- else onBack()
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Step indicator
            KisanStepIndicator(
                totalSteps = 4,
                currentStep = currentStep,
                stepLabels = listOf("Centre", "Crop", "Slot", "Confirm")
            )

            Spacer(Modifier.height(20.dp))

            // Step content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut())
                    } else {
                        (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { it / 3 } + fadeOut())
                    }
                },
                label = "step_content"
            ) { step ->
                when (step) {
                    1 -> StepCentre(
                        centres = centres,
                        selectedCentreId = selectedCentreId,
                        onSelect = { id ->
                            selectedCentreId = id
                            selectedSlotId = null
                        }
                    )
                    2 -> StepCrop(
                        crops = crops,
                        selectedCropId = selectedCropId,
                        onSelect = { selectedCropId = it }
                    )
                    3 -> StepSlot(
                        slots = slots,
                        selectedSlotId = selectedSlotId,
                        selectedCentreId = selectedCentreId,
                        isLoading = isLoading,
                        onSelect = { selectedSlotId = it }
                    )
                    4 -> StepConfirm(
                        centre = selectedCentre,
                        crop = selectedCrop,
                        slot = selectedSlot,
                        quantityKg = quantityKg,
                        notes = notes,
                        onQuantityChange = { quantityKg = it },
                        onNotesChange = { notes = it },
                        errorMessage = errorMessage
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Navigation buttons
            val canProceed = when (currentStep) {
                1 -> selectedCentreId != null
                2 -> selectedCropId != null
                3 -> selectedSlotId != null
                4 -> quantityKg.isNotBlank()
                else -> false
            }

            KisanPrimaryButton(
                text = if (currentStep < 4) "Continue  →" else "Confirm Booking & Generate Pass",
                onClick = {
                    if (currentStep < 4) {
                        currentStep++
                    } else {
                        val cId = selectedCentreId
                        val sId = selectedSlotId
                        val crId = selectedCropId
                        if (cId != null && sId != null && crId != null) {
                            bookingViewModel.createBooking(
                                centreId = cId,
                                slotId = sId,
                                cropId = crId,
                                qty = quantityKg.toDoubleOrNull() ?: 100.0,
                                notes = notes.ifBlank { null }
                            )
                        }
                    }
                },
                enabled = canProceed,
                isLoading = currentStep == 4 && isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── STEP 1: Centre ─────────────────────────────────────────────────────────

@Composable
private fun StepCentre(
    centres: List<com.kisanprocure.app.data.model.ProcurementCentre>,
    selectedCentreId: Int?,
    onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Where are you selling?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = KisanTextDark)
        Text("Select the nearest APMC procurement centre", style = MaterialTheme.typography.bodyMedium, color = KisanTextMuted)

        if (centres.isEmpty()) {
            KisanEmptyState(emoji = "🏪", title = "Loading Centres...", description = "Please wait while we fetch available centres.")
        } else {
            centres.forEach { centre ->
                val isSelected = centre.id == selectedCentreId
                KisanCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isSelected) Modifier.border(2.dp, KisanGreenPrimary, MaterialTheme.shapes.large)
                            else Modifier
                        ),
                    onClick = { onSelect(centre.id) },
                    elevation = if (isSelected) 3.dp else 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) KisanGreenPrimary else KisanMintContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏪", fontSize = 22.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(centre.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (isSelected) KisanGreenDark else KisanTextDark)
                            val locStr = listOfNotNull(centre.displayLocation.takeIf { it.isNotBlank() }, centre.district?.takeIf { it.isNotBlank() && it != centre.displayLocation }).joinToString(", ")
                            if (locStr.isNotBlank()) {
                                Text(locStr, style = MaterialTheme.typography.bodySmall, color = KisanTextMuted)
                            }
                            Text(centre.displayOperatingHours, style = MaterialTheme.typography.labelSmall, color = KisanTextHint)
                        }
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KisanGreenPrimary, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }
}

// ─── STEP 2: Crop ─────────────────────────────────────────────────────────

@Composable
private fun StepCrop(
    crops: List<com.kisanprocure.app.data.model.Crop>,
    selectedCropId: Int?,
    onSelect: (Int) -> Unit
) {
    val cropEmojis = mapOf("Ragi" to "🌾", "Paddy" to "🌾", "Cotton" to "🌿", "Tur" to "🫘", "Maize" to "🌽", "Groundnut" to "🥜")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("What are you bringing?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = KisanTextDark)
        Text("Select the crop you want to sell at this centre", style = MaterialTheme.typography.bodyMedium, color = KisanTextMuted)

        crops.forEach { crop ->
            val isSelected = crop.id == selectedCropId
            val emoji = cropEmojis.entries.firstOrNull { crop.name.contains(it.key, ignoreCase = true) }?.value ?: "🌱"
            KisanCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isSelected) Modifier.border(2.dp, KisanGreenPrimary, MaterialTheme.shapes.large) else Modifier),
                onClick = { onSelect(crop.id) },
                elevation = if (isSelected) 3.dp else 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) KisanGreenPrimary else KisanMintContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(crop.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (isSelected) KisanGreenDark else KisanTextDark)
                        Text("${crop.category} • ${crop.season}", style = MaterialTheme.typography.bodySmall, color = KisanTextMuted)
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = if (isSelected) KisanStatusOpenBg else KisanSurfaceVariant) {
                        Text(
                            text = "₹${crop.mspPerQuintal.toInt()}/q",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) KisanStatusOpen else KisanTextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ─── STEP 3: Slot ─────────────────────────────────────────────────────────

@Composable
private fun StepSlot(
    slots: List<Slot>,
    selectedSlotId: Int?,
    selectedCentreId: Int?,
    isLoading: Boolean,
    onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Choose your time", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = KisanTextDark)
        Text("Select an available time slot for your visit", style = MaterialTheme.typography.bodyMedium, color = KisanTextMuted)

        if (isLoading) {
            repeat(4) { KisanCardSkeleton() }
        } else if (slots.isEmpty()) {
            KisanEmptyState(
                emoji = "📅",
                title = if (selectedCentreId == null) "Select a centre first" else "No Slots Available",
                description = "There are no available slots for this centre today."
            )
        } else {
            // Group by date
            val groupedSlots = slots.groupBy { it.slotDate }
            groupedSlots.forEach { (date, daySlots) ->
                Text(date, style = MaterialTheme.typography.labelMedium, color = KisanTextMuted, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    daySlots.forEach { slot ->
                        val remaining = slot.available ?: (slot.capacity - slot.booked)
                        val isFull = remaining <= 0
                        val isSelected = slot.id == selectedSlotId
                        SlotChip(
                            time = slot.slotTime,
                            remaining = remaining,
                            isFull = isFull,
                            isSelected = isSelected,
                            modifier = Modifier.weight(1f),
                            onClick = { if (!isFull) onSelect(slot.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotChip(
    time: String,
    remaining: Int,
    isFull: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> KisanGreenPrimary
        isFull     -> KisanSurfaceVariant
        else       -> KisanWhite
    }
    val borderColor = when {
        isSelected -> KisanGreenPrimary
        isFull     -> KisanBorder
        else       -> KisanBorder
    }

    Surface(
        modifier = modifier
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = !isFull, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.labelLarge,
                color = when { isSelected -> KisanWhite; isFull -> KisanTextHint; else -> KisanTextDark },
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (isFull) "FULL" else "$remaining left",
                style = MaterialTheme.typography.labelSmall,
                color = when { isSelected -> KisanWhite.copy(0.8f); isFull -> KisanTextHint; else -> KisanGreenPrimary }
            )
        }
    }
}

// ─── STEP 4: Confirm ─────────────────────────────────────────────────────

@Composable
private fun StepConfirm(
    centre: com.kisanprocure.app.data.model.ProcurementCentre?,
    crop: com.kisanprocure.app.data.model.Crop?,
    slot: Slot?,
    quantityKg: String,
    notes: String,
    onQuantityChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    errorMessage: String?
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Booking Summary", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = KisanTextDark)

        KisanCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ConfirmRow(label = "Centre", value = centre?.name ?: "-")
                HorizontalDivider(color = KisanDivider, thickness = 0.5.dp)
                ConfirmRow(label = "Crop", value = crop?.name ?: "-")
                HorizontalDivider(color = KisanDivider, thickness = 0.5.dp)
                ConfirmRow(label = "Date", value = slot?.slotDate ?: "-")
                HorizontalDivider(color = KisanDivider, thickness = 0.5.dp)
                ConfirmRow(label = "Time", value = slot?.slotTime ?: "-")
                HorizontalDivider(color = KisanDivider, thickness = 0.5.dp)
                ConfirmRow(label = "MSP Rate", value = crop?.let { "₹${it.mspPerQuintal.toInt()}/qtl" } ?: "-")
            }
        }

        OutlinedTextField(
            value = quantityKg,
            onValueChange = onQuantityChange,
            label = { Text("Estimated Quantity (Kg)") },
            leadingIcon = { Icon(Icons.Default.Scale, contentDescription = null, tint = KisanGreenPrimary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KisanGreenPrimary, unfocusedBorderColor = KisanBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Vehicle No / Remarks (Optional)") },
            leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = null, tint = KisanGreenPrimary) },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KisanGreenPrimary, unfocusedBorderColor = KisanBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            KisanErrorState(message = errorMessage, onRetry = {})
        }
    }
}

@Composable
private fun ConfirmRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = KisanTextMuted)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = KisanTextDark)
    }
}
