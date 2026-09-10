package com.kisanprocure.app.data.repository

import com.kisanprocure.app.data.model.LoginRequest
import com.kisanprocure.app.data.model.LoginResponse
import com.kisanprocure.app.data.model.UserProfile
import com.kisanprocure.app.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {

    suspend fun login(email: String, pass: String, fcmToken: String? = null): Result<LoginResponse> =
        withContext(Dispatchers.IO) {
            try {
                val res = ApiClient.apiService.login(LoginRequest(email, pass, fcmToken))
                if (res.isSuccessful && res.body()?.data != null) {
                    val body = res.body()!!.data!!
                    ApiClient.setAuthToken(body.token)
                    Result.success(body)
                } else {
                    Result.failure(Exception(res.body()?.message ?: "Login failed (${res.code()})"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getProfile(): Result<UserProfile> =
        withContext(Dispatchers.IO) {
            try {
                val res = ApiClient.apiService.getProfile()
                if (res.isSuccessful && res.body()?.data != null) {
                    Result.success(res.body()!!.data!!)
                } else {
                    Result.failure(Exception(res.body()?.message ?: "Failed to load profile"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun updateFcmToken(token: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                ApiClient.apiService.updateFcmToken(mapOf("fcmToken" to token))
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun logout() {
        ApiClient.setAuthToken(null)
    }
}
