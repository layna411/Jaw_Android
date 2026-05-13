package com.simats.newjaw.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

import com.simats.newjaw.data.network.SocketManager

data class JawSensorData(
    val protrusiveAngle: Double = 0.0,
    val protrusiveDisp: Double = 0.0,
    val maxProtrusiveAngle: Double = 0.0,
    val maxProtrusiveDisp: Double = 0.0
)

class SensorViewModel : ViewModel() {
    private val _sensorData = MutableStateFlow(JawSensorData())
    val sensorData: StateFlow<JawSensorData> = _sensorData.asStateFlow()

    private val _statusMessage = MutableStateFlow("Waiting for calibration...")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _statusType = MutableStateFlow("info")
    val statusType: StateFlow<String> = _statusType.asStateFlow()

    private val _isSocketConnected = MutableStateFlow(false)
    val isSocketConnected: StateFlow<Boolean> = _isSocketConnected.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        SocketManager.connect()
        SocketManager.onMetricsReceived { json ->
            Log.d("SensorViewModel", "Metrics received: $json")
            _sensorData.value = JawSensorData(
                protrusiveAngle = json.optDouble("protrusive_angle", 0.0),
                protrusiveDisp = json.optDouble("protrusive_disp", 0.0),
                maxProtrusiveAngle = json.optDouble("max_angle", 0.0),
                maxProtrusiveDisp = json.optDouble("max_disp", 0.0)
            )
        }

        SocketManager.onStatusReceived { message, type ->
            _statusMessage.value = message
            _statusType.value = type
        }

        // Track connection status
        SocketManager.onConnect { _isSocketConnected.value = true }
        SocketManager.onDisconnect { _isSocketConnected.value = false }
    }

    fun startPolling() {
        // Reset backend state when starting a new session
        viewModelScope.launch {
            try {
                com.simats.newjaw.data.network.RetrofitClient.api.resetState()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to reset server state: ${e.message}"
            }
        }
    }

    fun stopPolling() {
        // WebSocket stays connected for session duration
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.disconnect()
    }
}
