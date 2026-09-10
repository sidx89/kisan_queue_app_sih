package com.kisanprocure.app.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Centralised app configuration.
 * API base URL is NEVER hardcoded to localhost — it is stored in SharedPreferences
 * and can be changed at runtime via the Settings screen.
 *
 * Default: LOCAL (same network as dev machine)
 * Can be switched to: TUNNEL (Cloudflare) or PRODUCTION
 */
object AppConfig {

    private const val TAG = "AppConfig"
    private const val PREFS_NAME = "kisan_config"
    private const val KEY_API_URL = "api_base_url"
    private const val KEY_ENV = "environment"

    enum class Environment { LOCAL, TUNNEL, PRODUCTION }

    // Defaults — change LOCAL_IP to your machine's LAN IP before building
    private const val DEFAULT_LOCAL_URL = "http://192.168.1.100:5000"
    private const val DEFAULT_TUNNEL_URL = "https://your-tunnel.trycloudflare.com"
    private const val DEFAULT_PROD_URL = "https://api.kisanprocure.gov.in"

    private lateinit var prefs: SharedPreferences

    var apiBaseUrl: String = DEFAULT_LOCAL_URL
        private set

    var environment: Environment = Environment.LOCAL
        private set

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        apiBaseUrl = prefs.getString(KEY_API_URL, DEFAULT_LOCAL_URL) ?: DEFAULT_LOCAL_URL
        environment = try {
            Environment.valueOf(prefs.getString(KEY_ENV, Environment.LOCAL.name) ?: "LOCAL")
        } catch (e: IllegalArgumentException) {
            Environment.LOCAL
        }
        Log.i(TAG, "Config loaded: env=$environment url=$apiBaseUrl")
    }

    fun setEnvironment(context: Context, env: Environment) {
        environment = env
        apiBaseUrl = when (env) {
            Environment.LOCAL -> DEFAULT_LOCAL_URL
            Environment.TUNNEL -> DEFAULT_TUNNEL_URL
            Environment.PRODUCTION -> DEFAULT_PROD_URL
        }
        prefs.edit()
            .putString(KEY_ENV, env.name)
            .putString(KEY_API_URL, apiBaseUrl)
            .apply()
        Log.i(TAG, "Environment changed to $env → $apiBaseUrl")
    }

    fun setCustomUrl(url: String) {
        apiBaseUrl = url.trimEnd('/')
        prefs.edit().putString(KEY_API_URL, apiBaseUrl).apply()
    }

    fun getSocketUrl(): String = apiBaseUrl
}
