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
import kotlinx.coroutines.delay
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

    private val _scannedDevices = MutableStateFlow<List<ScanResult>>(emptyList())
    val scannedDevices: StateFlow<List<ScanResult>> = _scannedDevices.asStateFlow()

    private val activeGattConnections = mutableMapOf<String, BluetoothGatt>()
    private val deviceNameMap = mutableMapOf<String, String>() // Store names by address

    // Replace with actual UUIDs for your hardware
    // UUIDs from your latest GATT dump
    private val JAW_SERVICE_UUID = UUID.fromString("0000180c-0000-1000-8000-00805f9b34fb")
    private val JAW_CHARACTERISTIC_UUID = UUID.fromString("00002a56-0000-1000-8000-00805f9b34fb")

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let { res ->
                val device = res.device
                val name = device.name ?: "Unknown"
                Log.d("JawBleManager", "Scanned device: $name (${device.address})")
                
                // Update scanned list
                val currentList = _scannedDevices.value.toMutableList()
                val index = currentList.indexOfFirst { it.device.address == device.address }
                if (index != -1) {
                    currentList[index] = res
                } else {
                    currentList.add(res)
                }
                _scannedDevices.value = currentList

                if (name.contains("JAW", ignoreCase = true) || name.contains("Sensor", ignoreCase = true)) {
                    deviceNameMap[device.address] = name // Save the name
                    if (!_connectedDevices.value.any { it.address == device.address }) {
                        if (_connectedDevices.value.size < 2) {
                            Log.d("JawBleManager", "Target device found: $name, connecting...")
                            connectToDevice(device)
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
                Log.d("JawBleManager", "Requesting MTU (512) for ${gatt.device.name}...")
                if (!gatt.requestMtu(512)) {
                    Log.w("JawBleManager", "MTU request failed, starting service discovery anyway...")
                    gatt.discoverServices()
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("JawBleManager", "Disconnected from ${gatt.device.name ?: deviceAddress}")
                val devices = _connectedDevices.value.filter { it.address != deviceAddress }
                _connectedDevices.value = devices
                activeGattConnections.remove(deviceAddress)
                gatt.close()
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.d("JawBleManager", "MTU changed to $mtu for ${gatt.device.name}. Discovering services...")
            gatt.discoverServices()
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("JawBleManager", "Services discovered for ${gatt.device.name}. Waiting 500ms...")
                
                // Small delay helps hardware stabilize
                CoroutineScope(Dispatchers.Main).launch {
                    delay(500)
                    enableNotifications(gatt)
                }
            } else {
                Log.e("JawBleManager", "Service discovery failed with status: $status")
            }
        }

        private fun enableNotifications(gatt: BluetoothGatt) {
            val service = gatt.getService(JAW_SERVICE_UUID)
            val characteristic = service?.getCharacteristic(JAW_CHARACTERISTIC_UUID)
            
            if (characteristic != null) {
                gatt.setCharacteristicNotification(characteristic, true)
                val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                if (descriptor != null) {
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                    Log.d("JawBleManager", "✅ Notifications ENABLED for ${gatt.device.name}")
                }
            } else {
                Log.e("JawBleManager", "❌ Failed to find JAW characteristic for ${gatt.device.name}")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val rawData = characteristic.value
            val deviceAddress = gatt.device.address
            val savedName = deviceNameMap[deviceAddress] ?: gatt.device.name ?: "Unknown"
            
            val dataString = String(rawData)
            sendDataToBackend(savedName, dataString)
        }
    }

    private var currentPatientId: String = "1"

    fun startScanning(patientId: String = "1") {
        this.currentPatientId = patientId
        if (bluetoothAdapter?.isEnabled == true) {
            _scannedDevices.value = emptyList() // Clear previous results
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
                    // UPPER_JAW or NanoBLE1 -> Routes to 'upper' slot in backend
                    if (deviceName.contains("UPPER", true) || deviceName.contains("NanoBLE1", true)) {
                        Log.d("JawBleManager", "Routing $deviceName to UPPER slot")
                        SocketManager.sendSensorData(currentPatientId, upper = vals)
                    } 
                    // LOWER_JAW or NanoBLE2 -> Routes to 'lower' slot in backend
                    else if (deviceName.contains("LOWER", true) || deviceName.contains("NanoBLE2", true)) {
                        Log.d("JawBleManager", "Routing $deviceName to LOWER slot")
                        SocketManager.sendSensorData(currentPatientId, lower = vals)
                    }
                    else {
                        Log.w("JawBleManager", "Unknown device name '$deviceName', data not routed.")
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
