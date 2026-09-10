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
    val name: String,
    val location: String,
    val district: String,
    val state: String,
    val capacity: Int,
    @SerializedName("operating_hours") val operatingHours: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("waiting_count") val waitingCount: Int? = null,
    @SerializedName("processing_count") val processingCount: Int? = null,
    @SerializedName("total_queue") val totalQueue: Int? = null
)

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
    val id: Int,
    @SerializedName("booking_token") val bookingToken: String,
    @SerializedName("qr_code") val qrCode: String? = null,
    val status: String,
    @SerializedName("centre_name") val centreName: String? = null,
    @SerializedName("slot_date") val slotDate: String? = null,
    @SerializedName("slot_time") val slotTime: String? = null,
    @SerializedName("crop_name") val cropName: String? = null,
    @SerializedName("estimated_quantity_kg") val estimatedQuantityKg: Double? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

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
