package com.kisanprocure.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanprocure.app.data.model.UserProfile
import com.kisanprocure.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Authenticated(val user: UserProfile) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    fun login(email: String, pass: String, fcmToken: String? = null) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val res = authRepository.login(email, pass, fcmToken)
            res.fold(
                onSuccess = {
                    _currentUser.value = it.user
                    _uiState.value = AuthUiState.Authenticated(it.user)
                },
                onFailure = {
                    _uiState.value = AuthUiState.Error(it.message ?: "Login failed")
                }
            )
        }
    }

    fun logout() {
        authRepository.logout()
        _currentUser.value = null
        _uiState.value = AuthUiState.Idle
    }

    fun clearError() {
        _uiState.value = AuthUiState.Idle
    }
}
