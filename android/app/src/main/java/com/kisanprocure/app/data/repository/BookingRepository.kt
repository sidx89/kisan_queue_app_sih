package com.kisanprocure.app.data.repository

import android.content.Context
import com.kisanprocure.app.data.local.KisanDatabase
import com.kisanprocure.app.data.model.*
import com.kisanprocure.app.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookingRepository(private val context: Context) {

    private val db by lazy { KisanDatabase.getInstance(context) }

    suspend fun getCentres(): Result<List<ProcurementCentre>> = withContext(Dispatchers.IO) {
        try {
            val res = ApiClient.apiService.getCentres()
            if (res.isSuccessful && res.body()?.data != null) {
                Result.success(res.body()!!.data!!)
            } else {
                Result.failure(Exception(res.body()?.message ?: "Failed to load centres"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCrops(): Result<List<Crop>> = withContext(Dispatchers.IO) {
        try {
            val res = ApiClient.apiService.getCrops()
            if (res.isSuccessful && res.body()?.data != null) {
                Result.success(res.body()!!.data!!)
            } else {
                Result.failure(Exception(res.body()?.message ?: "Failed to load crops"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSlots(centreId: Int, date: String? = null): Result<List<Slot>> =
        withContext(Dispatchers.IO) {
            try {
                val res = ApiClient.apiService.getSlots(centreId, date)
                if (res.isSuccessful && res.body()?.data != null) {
                    Result.success(res.body()!!.data!!)
                } else {
                    Result.failure(Exception(res.body()?.message ?: "Failed to load slots"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun createBooking(request: CreateBookingRequest): Result<Booking> =
        withContext(Dispatchers.IO) {
            try {
                val res = ApiClient.apiService.createBooking(request)
                if (res.isSuccessful && res.body()?.data != null) {
                    Result.success(res.body()!!.data!!)
                } else {
                    Result.failure(Exception(res.body()?.message ?: "Failed to create booking"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getMyBookings(): Result<List<Booking>> = withContext(Dispatchers.IO) {
        try {
            val res = ApiClient.apiService.getMyBookings()
            if (res.isSuccessful && res.body()?.data != null) {
                val bookings = res.body()!!.data!!
                db.saveBookings(bookings)
                Result.success(bookings)
            } else {
                // Offline fallback
                val cached = db.getCachedBookings()
                if (cached.isNotEmpty()) Result.success(cached)
                else Result.failure(Exception(res.body()?.message ?: "Failed to load bookings"))
            }
        } catch (e: Exception) {
            val cached = db.getCachedBookings()
            if (cached.isNotEmpty()) Result.success(cached)
            else Result.failure(e)
        }
    }
}
