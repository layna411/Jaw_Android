package com.simats.newjaw.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.simats.newjaw.ble.JawBleManager
import kotlinx.coroutines.flow.StateFlow

class BleViewModel(application: Application) : AndroidViewModel(application) {
    private val bleManager = JawBleManager(application)
    val connectedDevices: StateFlow<List<android.bluetooth.BluetoothDevice>> = bleManager.connectedDevices

    private val _isScanning = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private var currentPatientId: String = "1"

    fun startScanning(patientId: String = "1") {
        currentPatientId = patientId
        _isScanning.value = true
        bleManager.startScanning(patientId)
    }

    fun stopScanning() {
        _isScanning.value = false
        bleManager.stopScanning()
    }

    fun disconnectAll() {
        bleManager.disconnectAll()
    }

    override fun onCleared() {
        super.onCleared()
        bleManager.stopScanning()
        bleManager.disconnectAll()
    }
}
