package com.kisanprocure.app.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var selectedCentreId by remember { mutableStateOf<Int?>(null) }
    var selectedCropId by remember { mutableStateOf<Int?>(null) }
    var selectedSlotId by remember { mutableStateOf<Int?>(null) }
    var quantityKg by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var centreExpanded by remember { mutableStateOf(false) }
    var cropExpanded by remember { mutableStateOf(false) }
    var slotExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        bookingViewModel.loadInitialData()
    }

    LaunchedEffect(selectedCentreId) {
        selectedCentreId?.let { bookingViewModel.loadSlots(it) }
    }

    LaunchedEffect(bookingSuccess) {
        bookingSuccess?.let {
            onBookingSuccess(it.bookingToken)
            bookingViewModel.clearBookingSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Procurement Slot") },
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Schedule Your Visit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Select centre, crop type, and preferred time slot to receive a guaranteed queue token.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Centre Dropdown
            ExposedDropdownMenuBox(
                expanded = centreExpanded,
                onExpandedChange = { centreExpanded = !centreExpanded }
            ) {
                OutlinedTextField(
                    value = centres.firstOrNull { it.id == selectedCentreId }?.name ?: "Select Centre",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Procurement Centre") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = centreExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = centreExpanded,
                    onDismissRequest = { centreExpanded = false }
                ) {
                    centres.forEach { centre ->
                        DropdownMenuItem(
                            text = { Text("${centre.name} (${centre.district})") },
                            onClick = {
                                selectedCentreId = centre.id
                                selectedSlotId = null
                                centreExpanded = false
                            }
                        )
                    }
                }
            }

            // Crop Dropdown
            ExposedDropdownMenuBox(
                expanded = cropExpanded,
                onExpandedChange = { cropExpanded = !cropExpanded }
            ) {
                OutlinedTextField(
                    value = crops.firstOrNull { it.id == selectedCropId }?.let { "${it.name} (MSP: ₹${it.mspPerQuintal}/q)" } ?: "Select Crop",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Crop Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = cropExpanded,
                    onDismissRequest = { cropExpanded = false }
                ) {
                    crops.forEach { crop ->
                        DropdownMenuItem(
                            text = { Text("${crop.name} — MSP ₹${crop.mspPerQuintal}/qtl") },
                            onClick = {
                                selectedCropId = crop.id
                                cropExpanded = false
                            }
                        )
                    }
                }
            }

            // Slot Dropdown
            ExposedDropdownMenuBox(
                expanded = slotExpanded,
                onExpandedChange = { slotExpanded = !slotExpanded }
            ) {
                OutlinedTextField(
                    value = slots.firstOrNull { it.id == selectedSlotId }?.let { "${it.slotDate} ${it.slotTime} (${it.available ?: (it.capacity - it.booked)} left)" } ?: "Select Slot Time",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Time Slot") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = slotExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = slotExpanded,
                    onDismissRequest = { slotExpanded = false }
                ) {
                    if (slots.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text(if (selectedCentreId == null) "Choose centre first" else "No available slots") },
                            onClick = { slotExpanded = false }
                        )
                    } else {
                        slots.forEach { slot ->
                            val remaining = slot.available ?: (slot.capacity - slot.booked)
                            DropdownMenuItem(
                                text = { Text("${slot.slotDate} at ${slot.slotTime} — $remaining spots") },
                                onClick = {
                                    selectedSlotId = slot.id
                                    slotExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Quantity in Kg
            OutlinedTextField(
                value = quantityKg,
                onValueChange = { quantityKg = it },
                label = { Text("Estimated Quantity (Kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Vehicle No / Remarks (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (selectedCentreId != null && selectedSlotId != null && selectedCropId != null && quantityKg.isNotBlank()) {
                        bookingViewModel.createBooking(
                            centreId = selectedCentreId!,
                            slotId = selectedSlotId!,
                            cropId = selectedCropId!,
                            qty = quantityKg.toDoubleOrNull() ?: 100.0,
                            notes = notes.ifBlank { null }
                        )
                    }
                },
                enabled = selectedCentreId != null && selectedSlotId != null && selectedCropId != null && quantityKg.isNotBlank() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Confirm Booking & Generate QR Pass", fontSize = 16.sp)
                }
            }
        }
    }
}
