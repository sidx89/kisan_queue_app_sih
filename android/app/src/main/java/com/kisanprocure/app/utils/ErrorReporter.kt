package com.kisanprocure.app.utils

import android.content.Context
import android.util.Log
import com.kisanprocure.app.data.remote.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * End-to-end error reporter.
 * Catches uncaught exceptions, formats an ERR-YYYYMMDD-XXXXX style payload,
 * and POSTs to /api/errors/report.
 */
object ErrorReporter {

    private const val TAG = "ErrorReporter"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var appContext: Context? = null
    private var appVersion: String = "1.0.0"

    fun init(context: Context) {
        appContext = context.applicationContext
        val info = try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) { null }
        appVersion = info?.versionName ?: "unknown"

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            reportCrash(throwable, thread.name)
            defaultHandler?.uncaughtException(thread, throwable)
        }
        Log.i(TAG, "ErrorReporter initialised")
    }

    /** Called automatically for uncaught crashes */
    private fun reportCrash(throwable: Throwable, threadName: String) {
        val ctx = appContext ?: return
        scope.launch {
            try {
                val body = mapOf(
                    "errorType" to "CRASH",
                    "severity" to "CRITICAL",
                    "message" to (throwable.message ?: "Unknown crash"),
                    "stackTrace" to throwable.stackTraceToString(),
                    "deviceInfo" to buildDeviceInfo(ctx),
                    "appVersion" to appVersion,
                    "thread" to threadName,
                    "timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                )
                ApiClient.errorsApi.reportError(body)
                Log.i(TAG, "Crash report submitted")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to submit crash report: ${e.message}")
            }
        }
    }

    /** Called manually for non-fatal errors (e.g., API failures, UI exceptions) */
    fun reportError(
        errorType: String = "RUNTIME",
        severity: String = "ERROR",
        message: String,
        stackTrace: String? = null,
        metadata: Map<String, Any?> = emptyMap()
    ) {
        val ctx = appContext ?: return
        scope.launch {
            try {
                val body = mutableMapOf<String, Any?>(
                    "errorType" to errorType,
                    "severity" to severity,
                    "message" to message,
                    "stackTrace" to (stackTrace ?: ""),
                    "deviceInfo" to buildDeviceInfo(ctx),
                    "appVersion" to appVersion,
                    "timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                )
                body.putAll(metadata)
                ApiClient.errorsApi.reportError(body)
                Log.w(TAG, "Error reported: [$severity] $message")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to report error: ${e.message}")
            }
        }
    }

    private fun buildDeviceInfo(context: Context): String {
        return buildString {
            append("Android ${android.os.Build.VERSION.RELEASE} ")
            append("(SDK ${android.os.Build.VERSION.SDK_INT}) ")
            append("| ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} ")
            append("| App $appVersion")
        }
    }
}
