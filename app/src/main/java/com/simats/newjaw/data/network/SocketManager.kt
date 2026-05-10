package com.simats.newjaw.data.network

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object SocketManager {
    private var socket: Socket? = null
    
    // 10.0.2.2 points to emulator localhost. Change to your computer's IP (e.g. 192.168.x.x) if using physical device.
    private const val SOCKET_URL = "http://10.135.130.148:5000"

    fun connect() {
        try {
            val options = IO.Options()
            socket = IO.socket(SOCKET_URL, options)
            
            socket?.on(Socket.EVENT_CONNECT) {
                Log.d("SocketManager", "Connected to WebSocket Server")
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d("SocketManager", "Disconnected from WebSocket Server")
            }
            
            socket?.connect()
        } catch (e: Exception) {
            Log.e("SocketManager", "Error connecting: ${e.message}")
        }
    }

    fun sendSensorData(patientId: String, upper: DoubleArray? = null, lower: DoubleArray? = null) {
        val json = JSONObject().apply {
            put("patient_id", patientId)
            
            upper?.let {
                val upperArray = org.json.JSONArray()
                it.forEach { value -> upperArray.put(value) }
                put("upper", upperArray)
            }
            
            lower?.let {
                val lowerArray = org.json.JSONArray()
                it.forEach { value -> lowerArray.put(value) }
                put("lower", lowerArray)
            }
        }
        
        socket?.emit("sensor_data", json)
        Log.d("SocketManager", "Emitted sensor_data for patient: $patientId")
    }

    fun onMetricsReceived(onMetrics: (JSONObject) -> Unit) {
        socket?.on("metrics") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                onMetrics(data)
            }
        }
    }

    fun onStatusReceived(onStatus: (String, String) -> Unit) {
        socket?.on("session_status") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val message = data.optString("message", "")
                val type = data.optString("type", "info")
                onStatus(message, type)
            }
        }
    }

    fun onConnect(callback: () -> Unit) {
        socket?.on(Socket.EVENT_CONNECT) { callback() }
    }

    fun onDisconnect(callback: () -> Unit) {
        socket?.on(Socket.EVENT_DISCONNECT) { callback() }
    }

    fun disconnect() {
        socket?.disconnect()
    }
}
