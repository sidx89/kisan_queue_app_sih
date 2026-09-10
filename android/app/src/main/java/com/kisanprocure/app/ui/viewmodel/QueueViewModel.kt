package com.kisanprocure.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanprocure.app.data.model.LiveQueue
import com.kisanprocure.app.data.model.QueueEntry
import com.kisanprocure.app.data.repository.QueueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QueueViewModel(
    private val repository: QueueRepository = QueueRepository()
) : ViewModel() {

    private val _liveQueue = MutableStateFlow<LiveQueue?>(null)
    val liveQueue: StateFlow<LiveQueue?> = _liveQueue.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _checkInSuccess = MutableStateFlow<QueueEntry?>(null)
    val checkInSuccess: StateFlow<QueueEntry?> = _checkInSuccess.asStateFlow()

    private var currentCentreId: Int? = null

    fun loadLiveQueue(centreId: Int) {
        currentCentreId = centreId
        viewModelScope.launch {
            _isLoading.value = true
            repository.getLiveQueue(centreId).fold(
                onSuccess = { _liveQueue.value = it },
                onFailure = { _errorMessage.value = it.message }
            )
            _isLoading.value = false
        }
        startSocketObserver(centreId)
    }

    private fun startSocketObserver(centreId: Int) {
        viewModelScope.launch {
            repository.observeLiveQueue(centreId).collect { _ ->
                // Refresh live queue whenever event fires
                repository.getLiveQueue(centreId).onSuccess {
                    _liveQueue.value = it
                }
            }
        }
    }

    fun checkIn(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.checkIn(token).fold(
                onSuccess = {
                    _checkInSuccess.value = it
                    currentCentreId?.let { cid -> loadLiveQueue(cid) }
                },
                onFailure = { _errorMessage.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun callNext(centreId: Int, counterId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.callNext(centreId, counterId).fold(
                onSuccess = { loadLiveQueue(centreId) },
                onFailure = { _errorMessage.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun updateStatus(queueId: Int, status: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateQueueStatus(queueId, status).fold(
                onSuccess = { currentCentreId?.let { cid -> loadLiveQueue(cid) } },
                onFailure = { _errorMessage.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearCheckInSuccess() {
        _checkInSuccess.value = null
    }
}
