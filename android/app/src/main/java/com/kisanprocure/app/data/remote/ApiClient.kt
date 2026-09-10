package com.kisanprocure.app.data.remote

import android.content.Context
import com.kisanprocure.app.utils.AppConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit

object ApiClient {

    private var authToken: String? = null
    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String = ""

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun getAuthToken(): String? = authToken

    private fun getOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("X-Request-ID", "REQ-" + UUID.randomUUID().toString().take(8).uppercase())
                .header("User-Agent", "KisanProcure-Android/1.0.0")

            authToken?.let {
                requestBuilder.header("Authorization", "Bearer $it")
            }

            chain.proceed(requestBuilder.build())
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val apiService: ApiService
        get() {
            val baseUrl = if (AppConfig.apiBaseUrl.endsWith("/")) AppConfig.apiBaseUrl else "${AppConfig.apiBaseUrl}/"
            if (retrofit == null || currentBaseUrl != baseUrl) {
                currentBaseUrl = baseUrl
                retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit!!.create(ApiService::class.java)
        }

    val errorsApi: ApiService
        get() = apiService
}
