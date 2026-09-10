package com.kisanprocure.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        containerColor = KisanSurfaceLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Book Procurement Slot",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = KisanGreenDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = KisanGreenDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KisanWhite
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header instructions card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KisanMintContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(KisanGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = KisanWhite, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Guaranteed Entry Token",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = KisanGreenDark
                        )
                        Text(
                            text = "Book a verified APMC time slot to skip physical queues.",
                            style = MaterialTheme.typography.bodySmall,
                            color = KisanTextMuted
                        )
                    }
                }
            }

            // Main Booking Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KisanWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Booking Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = KisanGreenDark
                    )

                    // Centre Dropdown
                    ExposedDropdownMenuBox(
                        expanded = centreExpanded,
                        onExpandedChange = { centreExpanded = !centreExpanded }
                    ) {
                        OutlinedTextField(
                            value = centres.firstOrNull { it.id == selectedCentreId }?.let { "${it.name} (${it.district})" } ?: "Select Procurement Centre",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Procurement Centre") },
                            leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = KisanGreenPrimary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = centreExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KisanGreenPrimary,
                                unfocusedBorderColor = KisanBorder,
                                focusedLabelColor = KisanGreenPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = centreExpanded,
                            onDismissRequest = { centreExpanded = false }
                        ) {
                            centres.forEach { centre ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(centre.name, fontWeight = FontWeight.SemiBold, color = KisanTextDark)
                                            Text("${centre.location ?: ""}, ${centre.district}", style = MaterialTheme.typography.bodySmall, color = KisanTextMuted)
                                        }
                                    },
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
                            value = crops.firstOrNull { it.id == selectedCropId }?.let { "${it.name} (MSP: ₹${it.mspPerQuintal}/q)" } ?: "Select Crop Type",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Crop Type") },
                            leadingIcon = { Icon(Icons.Default.Grass, contentDescription = null, tint = KisanGreenPrimary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KisanGreenPrimary,
                                unfocusedBorderColor = KisanBorder,
                                focusedLabelColor = KisanGreenPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = cropExpanded,
                            onDismissRequest = { cropExpanded = false }
                        ) {
                            crops.forEach { crop ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(crop.name, fontWeight = FontWeight.SemiBold, color = KisanTextDark)
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = KisanMintContainer
                                            ) {
                                                Text(
                                                    "₹${crop.mspPerQuintal}/q",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = KisanGreenDark
                                                )
                                            }
                                        }
                                    },
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
                            value = slots.firstOrNull { it.id == selectedSlotId }?.let { "${it.slotDate} • ${it.slotTime} (${it.available ?: (it.capacity - it.booked)} spots left)" } ?: "Select Available Slot",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Time Slot") },
                            leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = KisanGreenPrimary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = slotExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KisanGreenPrimary,
                                unfocusedBorderColor = KisanBorder,
                                focusedLabelColor = KisanGreenPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = slotExpanded,
                            onDismissRequest = { slotExpanded = false }
                        ) {
                            if (slots.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text(if (selectedCentreId == null) "Choose centre first" else "No available slots for this centre") },
                                    onClick = { slotExpanded = false }
                                )
                            } else {
                                slots.forEach { slot ->
                                    val remaining = slot.available ?: (slot.capacity - slot.booked)
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("${slot.slotDate} • ${slot.slotTime}", fontWeight = FontWeight.Medium, color = KisanTextDark)
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (remaining > 5) KisanMintContainer else Color(0xFFFFEBEE)
                                                ) {
                                                    Text(
                                                        "$remaining left",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (remaining > 5) KisanGreenDark else KisanError
                                                    )
                                                }
                                            }
                                        },
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
                        label = { Text("Estimated Quantity (in Kg)") },
                        leadingIcon = { Icon(Icons.Default.Scale, contentDescription = null, tint = KisanGreenPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KisanGreenPrimary,
                            unfocusedBorderColor = KisanBorder,
                            focusedLabelColor = KisanGreenPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Notes / Vehicle No
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Vehicle No / Remarks (Optional)") },
                        leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = null, tint = KisanGreenPrimary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KisanGreenPrimary,
                            unfocusedBorderColor = KisanBorder,
                            focusedLabelColor = KisanGreenPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFEBEE),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = KisanError, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage!!,
                                    color = KisanError,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            val cId = selectedCentreId
                            val sId = selectedSlotId
                            val crId = selectedCropId
                            if (cId != null && sId != null && crId != null && quantityKg.isNotBlank()) {
                                bookingViewModel.createBooking(
                                    centreId = cId,
                                    slotId = sId,
                                    cropId = crId,
                                    qty = quantityKg.toDoubleOrNull() ?: 100.0,
                                    notes = notes.ifBlank { null }
                                )
                            }
                        },
                        enabled = selectedCentreId != null && selectedSlotId != null && selectedCropId != null && quantityKg.isNotBlank() && !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KisanGreenPrimary,
                            contentColor = KisanWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = KisanWhite, strokeWidth = 2.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Confirm Booking & Generate Pass", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
