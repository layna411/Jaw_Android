package com.simats.newjaw.ui.viewmodel

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
    val jawDisp: Double = 0.0,
    val velocity: Double = 0.0,
    val acceleration: Double = 0.0,
    val rom: Double = 0.0,
    val chewingFrequency: Double = 0.0,
    val symmetry: Double = 0.0,
    val pitch: Double = 0.0,
    val roll: Double = 0.0,
    val jawOpening: Double = 0.0
)

class SensorViewModel : ViewModel() {
    private val _sensorData = MutableStateFlow(JawSensorData())
    val sensorData: StateFlow<JawSensorData> = _sensorData.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        SocketManager.connect()
        SocketManager.onMetricsReceived { json ->
            _sensorData.value = JawSensorData(
                jawDisp = json.optDouble("disp", 0.0),
                velocity = json.optDouble("vel", 0.0),
                acceleration = json.optDouble("acc", 0.0),
                rom = json.optDouble("rom", 0.0),
                chewingFrequency = json.optDouble("freq", 0.0),
                symmetry = json.optDouble("sym", 0.0),
                pitch = json.optDouble("pitch", 0.0),
                roll = json.optDouble("roll", 0.0),
                jawOpening = json.optDouble("jaw_opening", 0.0)
            )
        }
    }

    fun startPolling() {
        // No longer polling since we use WebSocket events
    }

    fun stopPolling() {
        // WebSocket stays connected for session duration
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.disconnect()
    }
}
