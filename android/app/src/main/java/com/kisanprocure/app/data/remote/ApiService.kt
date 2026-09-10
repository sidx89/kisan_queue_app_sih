package com.kisanprocure.app.data.remote

import com.kisanprocure.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @GET("api/auth/profile")
    suspend fun getProfile(): Response<ApiResponse<UserProfile>>

    @PUT("api/auth/fcm")
    suspend fun updateFcmToken(@Body body: Map<String, String>): Response<ApiResponse<Map<String, String>>>

    // Centres & Crops
    @GET("api/centres")
    suspend fun getCentres(): Response<ApiResponse<List<ProcurementCentre>>>

    @GET("api/crops")
    suspend fun getCrops(): Response<ApiResponse<List<Crop>>>

    // Slots
    @GET("api/centres/{id}/slots")
    suspend fun getSlots(
        @Path("id") centreId: Int,
        @Query("date") date: String? = null
    ): Response<ApiResponse<List<Slot>>>

    // Bookings
    @POST("api/bookings")
    suspend fun createBooking(@Body request: CreateBookingRequest): Response<ApiResponse<Booking>>

    @GET("api/bookings")
    suspend fun getMyBookings(): Response<ApiResponse<List<Booking>>>

    @POST("api/bookings/check-in")
    suspend fun checkIn(@Body body: Map<String, String>): Response<ApiResponse<QueueEntry>>

    // Queue
    @GET("api/queue/{centreId}")
    suspend fun getLiveQueue(@Path("centreId") centreId: Int): Response<ApiResponse<LiveQueue>>

    @POST("api/queue/{centreId}/call-next")
    suspend fun callNext(
        @Path("centreId") centreId: Int,
        @Body body: Map<String, Int>
    ): Response<ApiResponse<QueueEntry>>

    @PUT("api/queue/status")
    suspend fun updateQueueStatus(@Body body: Map<String, Any>): Response<ApiResponse<QueueEntry>>

    // Errors
    @POST("api/errors/report")
    suspend fun reportError(@Body body: Map<String, @JvmSuppressWildcards Any?>): Response<ApiResponse<Map<String, String>>>
}
