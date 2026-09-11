package com.kisanprocure.app.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Centralised app configuration.
 * Automatically configured to the live Cloudflare public URL,
 * enabling seamless real-time data transfer anywhere in India (e.g. Gujarat to Karnataka).
 */
object AppConfig {

    private const val TAG = "AppConfig"
    private const val PREFS_NAME = "kisan_config"
    private const val KEY_API_URL = "api_base_url"
    private const val KEY_ENV = "environment"

    enum class Environment {
        TUNNEL,
        LOCAL_USB,
        LOCAL_NETWORK,
        CUSTOM
    }

    // Live Public Cloudflare Tunnel URL — updated each session by START_KISAN_SYSTEM.bat
    const val LIVE_CLOUDFLARE_URL = "https://enhanced-explained-employment-fireplace.trycloudflare.com"
    const val USB_LOCAL_URL = "http://127.0.0.1:5000"

    // All known stale / expired tunnel slugs — add new ones here when they expire
    private val STALE_TUNNEL_SLUGS = setOf(
        "artwork-configuring-amazing-supporting",
        "witnesses-adapted-teddy-scheme",
        "partly-llc-continent-separate",
        "soldiers-blog-limit-intention",
        "bigger-layer-show-seats",
        "allowing-month-msie-workplace",
        "navigation-optics-composite-oriented",
        "your-tunnel",
        "192.168.1.100"
    )

    private lateinit var prefs: SharedPreferences

    var apiBaseUrl: String = LIVE_CLOUDFLARE_URL
        private set

    var environment: Environment = Environment.TUNNEL
        private set

    private fun isStale(url: String?): Boolean {
        if (url.isNullOrBlank()) return true
        return STALE_TUNNEL_SLUGS.any { url.contains(it) }
    }

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_API_URL, null)

        // Replace any stale/expired tunnel URL with the current live one
        apiBaseUrl = if (isStale(saved)) {
            prefs.edit().putString(KEY_API_URL, LIVE_CLOUDFLARE_URL).apply()
            LIVE_CLOUDFLARE_URL
        } else {
            saved!!
        }

        environment = try {
            Environment.valueOf(prefs.getString(KEY_ENV, Environment.TUNNEL.name) ?: "TUNNEL")
        } catch (e: IllegalArgumentException) {
            Environment.TUNNEL
        }

        Log.i(TAG, "Config loaded: env=$environment url=$apiBaseUrl")
    }

    fun setEnvironment(context: Context, env: Environment) {
        environment = env
        apiBaseUrl = when (env) {
            Environment.TUNNEL -> LIVE_CLOUDFLARE_URL
            Environment.LOCAL_USB -> USB_LOCAL_URL
            Environment.LOCAL_NETWORK -> "http://192.168.1.100:5000"
            Environment.CUSTOM -> apiBaseUrl
        }
        prefs.edit()
            .putString(KEY_ENV, env.name)
            .putString(KEY_API_URL, apiBaseUrl)
            .apply()
        Log.i(TAG, "Environment changed to $env -> $apiBaseUrl")
    }

    fun setCustomUrl(url: String) {
        val sanitized = url.trim().trimEnd('/')
        apiBaseUrl = sanitized
        prefs.edit().putString(KEY_API_URL, sanitized).apply()
        Log.i(TAG, "Custom URL updated: $apiBaseUrl")
    }

    fun getSocketUrl(): String = apiBaseUrl
}
