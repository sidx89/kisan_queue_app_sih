package com.kisanprocure.app.data.model

import com.google.gson.annotations.SerializedName

// ─── Generic Wrapper ───────────────────────────────────────────────────────────

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val error: String? = null,
    val requestId: String? = null
)

// ─── Auth ──────────────────────────────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String,
    @SerializedName("fcm_token") val fcmToken: String? = null
)

data class LoginResponse(
    val token: String,
    val user: UserProfile
)

data class UserProfile(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    @SerializedName("aadhaar_masked") val aadhaarMasked: String? = null,
    @SerializedName("farm_location") val farmLocation: String? = null,
    @SerializedName("total_land_acres") val totalLandAcres: Double? = null,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("created_at") val createdAt: String? = null
)

// ─── Procurement Centres ───────────────────────────────────────────────────────

data class ProcurementCentre(
    val id: Int,
    val name: String = "",
    val address: String? = null,
    val village: String? = null,
    val location: String? = null,
    val district: String? = null,
    val state: String? = null,
    @SerializedName("daily_capacity") val dailyCapacity: Int? = null,
    val capacity: Int? = null,
    @SerializedName("open_time") val openTime: String? = null,
    @SerializedName("close_time") val closeTime: String? = null,
    @SerializedName("operating_hours") val operatingHours: String? = null,
    val status: String? = "OPEN",
    @SerializedName("is_active") val isActive: Boolean? = null,
    @SerializedName("waiting_count") val waitingCount: Int? = null,
    @SerializedName("processing_count") val processingCount: Int? = null,
    @SerializedName("current_queue") val currentQueue: Int? = null,
    @SerializedName("total_queue") val totalQueue: Int? = null
) {
    val displayCapacity: Int
        get() = dailyCapacity ?: capacity ?: 150

    val displayStatus: String
        get() = status ?: if (isActive == false) "CLOSED" else "OPEN"

    val displayLocation: String
        get() = village?.takeIf { it.isNotBlank() } ?: location?.takeIf { it.isNotBlank() } ?: address?.takeIf { it.isNotBlank() } ?: district ?: ""

    val displayOperatingHours: String
        get() = operatingHours?.takeIf { it.isNotBlank() } ?: if (!openTime.isNullOrBlank() && !closeTime.isNullOrBlank()) "$openTime - $closeTime" else "08:30 - 17:30"
}

// ─── Slots ─────────────────────────────────────────────────────────────────────

data class Slot(
    val id: Int,
    @SerializedName("centre_id") val centreId: Int,
    @SerializedName("slot_date") val slotDate: String,
    @SerializedName("slot_time") val slotTime: String,
    val capacity: Int,
    val booked: Int,
    @SerializedName("is_available") val isAvailable: Boolean,
    val available: Int? = null
)

// ─── Bookings ──────────────────────────────────────────────────────────────────

data class CreateBookingRequest(
    @SerializedName("centre_id") val centreId: Int,
    @SerializedName("slot_id") val slotId: Int,
    @SerializedName("crop_id") val cropId: Int,
    @SerializedName("estimated_quantity_kg") val estimatedQuantityKg: Double,
    val notes: String? = null
)

data class Booking(
    val id: Int = 0,
    @SerializedName("booking_token") val bookingToken: String? = null,
    @SerializedName("booking_ref") val bookingRef: String? = null,
    @SerializedName("qr_code") val qrCode: String? = null,
    val status: String? = "WAITING",
    @SerializedName("centre_name") val centreName: String? = null,
    @SerializedName("slot_date") val slotDate: String? = null,
    @SerializedName("booking_date") val bookingDate: String? = null,
    @SerializedName("slot_time") val slotTime: String? = null,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("crop_name") val cropName: String? = null,
    @SerializedName("estimated_quantity_kg") val estimatedQuantityKg: Double? = null,
    @SerializedName("estimated_quantity") val estimatedQuantity: Double? = null,
    @SerializedName("created_at") val createdAt: String? = null
) {
    val displayToken: String
        get() = bookingToken?.takeIf { it.isNotBlank() } ?: bookingRef?.takeIf { it.isNotBlank() } ?: "TOK-$id"

    val displayDate: String
        get() = slotDate?.takeIf { it.isNotBlank() } ?: bookingDate ?: "Today"

    val displayTime: String
        get() = slotTime?.takeIf { it.isNotBlank() } ?: startTime ?: "09:00 - 11:00"

    val displayStatus: String
        get() = status ?: "WAITING"

    val displayCrop: String
        get() = cropName ?: "Produce"

    val displayQuantity: String
        get() = "${(estimatedQuantityKg ?: estimatedQuantity ?: 100.0).toInt()} kg"
}

// ─── Queue ─────────────────────────────────────────────────────────────────────

data class QueueEntry(
    val id: Int,
    @SerializedName("queue_position") val queuePosition: Int,
    @SerializedName("token_number") val tokenNumber: String,
    val status: String,
    @SerializedName("farmer_name") val farmerName: String? = null,
    @SerializedName("counter_name") val counterName: String? = null,
    @SerializedName("crop_name") val cropName: String? = null,
    @SerializedName("eta_minutes") val etaMinutes: Int? = null,
    @SerializedName("check_in_time") val checkInTime: String? = null,
    @SerializedName("called_time") val calledTime: String? = null
)

data class LiveQueue(
    val centreId: Int,
    val centreName: String,
    @SerializedName("queue_entries") val queueEntries: List<QueueEntry>,
    @SerializedName("your_position") val yourPosition: Int? = null,
    @SerializedName("estimated_wait_minutes") val estimatedWaitMinutes: Int? = null
)

// ─── Crops ─────────────────────────────────────────────────────────────────────

data class Crop(
    val id: Int,
    val name: String,
    @SerializedName("msp_per_quintal") val mspPerQuintal: Double,
    val season: String,
    val category: String
)

// ─── Errors (outgoing to backend) ─────────────────────────────────────────────

data class ErrorReportRequest(
    @SerializedName("error_type") val errorType: String,
    val severity: String,
    val message: String,
    @SerializedName("stack_trace") val stackTrace: String?,
    @SerializedName("device_info") val deviceInfo: String,
    @SerializedName("app_version") val appVersion: String
)
