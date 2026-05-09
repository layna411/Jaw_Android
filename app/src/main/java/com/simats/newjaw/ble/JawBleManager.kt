package com.simats.newjaw.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import com.simats.newjaw.data.network.SocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

@SuppressLint("MissingPermission") // Ensure permissions are requested in UI before using
class JawBleManager(private val context: Context) {
    private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bleScanner = bluetoothAdapter?.bluetoothLeScanner

    // Keep track of connected sensors (expecting 2)
    private val _connectedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val connectedDevices: StateFlow<List<BluetoothDevice>> = _connectedDevices.asStateFlow()

    private val activeGattConnections = mutableMapOf<String, BluetoothGatt>()

    // Replace with actual UUIDs for your hardware
    private val JAW_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
    private val JAW_CHARACTERISTIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                val name = device.name ?: "Unknown"
                Log.d("JawBleManager", "Scanned device: $name (${device.address})")
                
                if (name.contains("JAW", ignoreCase = true) || name.contains("Sensor", ignoreCase = true)) {
                    if (!_connectedDevices.value.any { it.address == device.address }) {
                        if (_connectedDevices.value.size < 2) {
                            Log.d("JawBleManager", "Target device found: $name, connecting...")
                            connectToDevice(device)
                        } else {
                            Log.d("JawBleManager", "Already connected to 2 devices, stopping scan.")
                            stopScanning()
                        }
                    }
                }
            }
        }
        override fun onScanFailed(errorCode: Int) {
            Log.e("JawBleManager", "Scan failed with error: $errorCode")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("JawBleManager", "Connected to ${gatt.device.name ?: deviceAddress}")
                val devices = _connectedDevices.value.toMutableList()
                if (!devices.any { it.address == deviceAddress }) {
                    devices.add(gatt.device)
                    _connectedDevices.value = devices
                }
                activeGattConnections[deviceAddress] = gatt
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("JawBleManager", "Disconnected from ${gatt.device.name ?: deviceAddress}")
                val devices = _connectedDevices.value.filter { it.address != deviceAddress }
                _connectedDevices.value = devices
                activeGattConnections.remove(deviceAddress)
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("JawBleManager", "Services discovered for ${gatt.device.name}")
                
                // Log all discovered services to help user identify correct UUIDs
                gatt.services.forEach { service ->
                    Log.d("JawBleManager", "Service: ${service.uuid}")
                    service.characteristics.forEach { char ->
                        Log.d("JawBleManager", "  Characteristic: ${char.uuid}, Properties: ${char.properties}")
                    }
                }

                val service = gatt.getService(JAW_SERVICE_UUID)
                var characteristic = service?.getCharacteristic(JAW_CHARACTERISTIC_UUID)
                
                // FALLBACK: If specific characteristic not found, look for any NOTIFY characteristic in the service
                if (characteristic == null && service != null) {
                    characteristic = service.characteristics.find { 
                        (it.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0 
                    }
                    if (characteristic != null) {
                        Log.d("JawBleManager", "Using fallback characteristic: ${characteristic.uuid}")
                    }
                }

                if (characteristic != null) {
                    gatt.setCharacteristicNotification(characteristic, true)
                    
                    val CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                    val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
                    if (descriptor != null) {
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(descriptor)
                        Log.d("JawBleManager", "Notifications enabled for characteristic: ${characteristic.uuid}")
                    }
                } else {
                    Log.e("JawBleManager", "No compatible characteristic found (Target or Fallback)!")
                }
            } else {
                Log.e("JawBleManager", "Service discovery failed with status: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val rawData = characteristic.value
            Log.d("JawBleManager", "Data received: ${rawData.size} bytes from ${gatt.device.name}")
            val dataString = String(rawData)
            
            // Send data to backend automatically
            sendDataToBackend(gatt.device.name ?: "Unknown_Device", dataString)
        }
    }

    private var currentPatientId: String = "1"

    fun startScanning(patientId: String = "1") {
        this.currentPatientId = patientId
        if (bluetoothAdapter?.isEnabled == true) {
            bleScanner?.startScan(scanCallback)
            Log.d("JawBleManager", "Started BLE Scanning for Patient $patientId")
        }
    }

    fun stopScanning() {
        bleScanner?.stopScan(scanCallback)
        Log.d("JawBleManager", "Stopped BLE Scanning")
    }

    private fun connectToDevice(device: BluetoothDevice) {
        device.connectGatt(context, false, gattCallback)
    }

    fun disconnectAll() {
        activeGattConnections.values.forEach { it.disconnect() }
    }

    private fun sendDataToBackend(deviceName: String, dataString: String) {
        Log.d("JawBleManager", "Raw string from $deviceName: $dataString")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cleanData = dataString.trim().replace("\n", "").replace("\r", "")
                val vals = cleanData.split(",")
                    .map { it.trim().toDoubleOrNull() ?: 0.0 }
                    .toDoubleArray()
                
                Log.d("JawBleManager", "Parsed ${vals.size} values from $deviceName")
                
                if (vals.size >= 6) {
                    if (deviceName.contains("UPPER", ignoreCase = true)) {
                        SocketManager.sendSensorData(currentPatientId, upper = vals)
                    } else if (deviceName.contains("LOWER", ignoreCase = true)) {
                        SocketManager.sendSensorData(currentPatientId, lower = vals)
                    }
                    Log.d("JawBleManager", "Successfully sent $deviceName data to Socket")
                } else {
                    Log.w("JawBleManager", "Data from $deviceName too short: only ${vals.size} values. Expected 6+ (Ax,Ay,Az,Gx,Gy,Gz)")
                }
            } catch (e: Exception) {
                Log.e("JawBleManager", "Failed to process data from $deviceName: ${e.message}")
            }
        }
    }
}
