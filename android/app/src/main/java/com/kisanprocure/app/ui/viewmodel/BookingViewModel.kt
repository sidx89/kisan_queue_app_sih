package com.kisanprocure.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kisanprocure.app.data.model.*
import com.kisanprocure.app.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BookingRepository(application)

    private val _centres = MutableStateFlow<List<ProcurementCentre>>(emptyList())
    val centres: StateFlow<List<ProcurementCentre>> = _centres.asStateFlow()

    private val _crops = MutableStateFlow<List<Crop>>(emptyList())
    val crops: StateFlow<List<Crop>> = _crops.asStateFlow()

    private val _slots = MutableStateFlow<List<Slot>>(emptyList())
    val slots: StateFlow<List<Slot>> = _slots.asStateFlow()

    private val _myBookings = MutableStateFlow<List<Booking>>(emptyList())
    val myBookings: StateFlow<List<Booking>> = _myBookings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _bookingSuccess = MutableStateFlow<Booking?>(null)
    val bookingSuccess: StateFlow<Booking?> = _bookingSuccess.asStateFlow()

    fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getCentres().onSuccess { _centres.value = it }
            repository.getCrops().onSuccess { _crops.value = it }
            repository.getMyBookings().onSuccess { _myBookings.value = it }
            _isLoading.value = false
        }
    }

    fun loadSlots(centreId: Int, date: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getSlots(centreId, date).fold(
                onSuccess = { _slots.value = it },
                onFailure = { _errorMessage.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun createBooking(centreId: Int, slotId: Int, cropId: Int, qty: Double, notes: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            val req = CreateBookingRequest(centreId, slotId, cropId, qty, notes)
            repository.createBooking(req).fold(
                onSuccess = {
                    _bookingSuccess.value = it
                    loadInitialData()
                },
                onFailure = { _errorMessage.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun clearBookingSuccess() {
        _bookingSuccess.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
