package com.kisanprocure.app.data.repository

import android.util.Log
import com.google.gson.Gson
import com.kisanprocure.app.data.model.LiveQueue
import com.kisanprocure.app.data.model.QueueEntry
import com.kisanprocure.app.data.remote.ApiClient
import com.kisanprocure.app.utils.AppConfig
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class QueueRepository {

    private var socket: Socket? = null
    private val gson = Gson()

    suspend fun getLiveQueue(centreId: Int): Result<LiveQueue> = withContext(Dispatchers.IO) {
        try {
            val res = ApiClient.apiService.getLiveQueue(centreId)
            if (res.isSuccessful && res.body()?.data != null) {
                Result.success(res.body()!!.data!!)
            } else {
                Result.failure(Exception(res.body()?.message ?: "Failed to get queue"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkIn(token: String): Result<QueueEntry> = withContext(Dispatchers.IO) {
        try {
            val res = ApiClient.apiService.checkIn(mapOf("token" to token))
            if (res.isSuccessful && res.body()?.data != null) {
                Result.success(res.body()!!.data!!)
            } else {
                Result.failure(Exception(res.body()?.message ?: "Check-in failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun callNext(centreId: Int, counterId: Int): Result<QueueEntry> = withContext(Dispatchers.IO) {
        try {
            val res = ApiClient.apiService.callNext(centreId, mapOf("counterId" to counterId))
            if (res.isSuccessful && res.body()?.data != null) {
                Result.success(res.body()!!.data!!)
            } else {
                Result.failure(Exception(res.body()?.message ?: "Call next failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateQueueStatus(queueId: Int, status: String): Result<QueueEntry> = withContext(Dispatchers.IO) {
        try {
            val res = ApiClient.apiService.updateQueueStatus(mapOf("queueId" to queueId, "status" to status))
            if (res.isSuccessful && res.body()?.data != null) {
                Result.success(res.body()!!.data!!)
            } else {
                Result.failure(Exception(res.body()?.message ?: "Update status failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeLiveQueue(centreId: Int): Flow<String> = callbackFlow {
        try {
            val opts = IO.Options().apply {
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 1000
                timeout = 10000
            }
            val socketUrl = AppConfig.getSocketUrl()
            Log.i("QueueRepo", "SOCKET_CONNECTING -> $socketUrl")
            socket = IO.socket(socketUrl, opts)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.i("QueueRepo", "SOCKET_CONNECTED -> ${socket?.id()}")
                socket?.emit("join:centre", centreId)
                socket?.emit("join_centre", centreId)
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.w("QueueRepo", "SOCKET_DISCONNECTED")
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val err = if (args.isNotEmpty()) args[0].toString() else "Unknown"
                Log.e("QueueRepo", "SOCKET_ERROR -> $err")
            }

            socket?.on("queue:update") { args ->
                Log.i("QueueRepo", "SOCKET_EVENT queue:update received")
                if (args.isNotEmpty()) {
                    trySend(args[0].toString())
                }
            }

            socket?.on("queue_updated") { args ->
                Log.i("QueueRepo", "SOCKET_EVENT queue_updated received")
                if (args.isNotEmpty()) {
                    trySend(args[0].toString())
                }
            }

            socket?.on("token_called") { args ->
                Log.i("QueueRepo", "SOCKET_EVENT token_called received")
                if (args.isNotEmpty()) {
                    trySend(args[0].toString())
                }
            }

            socket?.connect()
        } catch (e: Exception) {
            Log.e("QueueRepo", "Socket error: ${e.message}")
        }

        awaitClose {
            socket?.disconnect()
            socket?.off()
        }
    }
}
