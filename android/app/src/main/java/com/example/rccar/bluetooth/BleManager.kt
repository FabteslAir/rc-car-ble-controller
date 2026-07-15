
package com.example.rccar.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import java.util.UUID

@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private var bluetoothGatt: BluetoothGatt? = null
    private var characteristicWrite: BluetoothGattCharacteristic? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    var onConnected: (() -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null
    var onBatteryUpdate: ((Int) -> Unit)? = null
    var onSignalUpdate: ((Int) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    companion object {
        private const val TAG = "BleManager"
        private const val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
        private const val CHARACTERISTIC_UUID = "beb5483b-36e1-4688-b7f5-ea07361b26a8"
        private const val BATTERY_SERVICE_UUID = "180f"
        private const val BATTERY_CHARACTERISTIC_UUID = "2a19"
        private const val DEVICE_NAME = "ESP32_RC_CAR"
    }

    fun startScan() {
        try {
            val scanner = bluetoothAdapter?.bluetoothLeScanner
            scanner?.startScan(
                listOf(
                    ScanFilter.Builder()
                        .setDeviceName(DEVICE_NAME)
                        .build()
                ),
                ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build(),
                scanCallback
            )
            Log.d(TAG, "Scan started")
        } catch (e: Exception) {
            Log.e(TAG, "Scan failed: ${e.message}")
            onError?.invoke("Erreur de scan: ${e.message}")
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (result.device.name == DEVICE_NAME) {
                Log.d(TAG, "Device found: ${result.device.name}")
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(this)
                connectToDevice(result.device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed with error code: $errorCode")
            onError?.invoke("Scan failed: $errorCode")
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        try {
            bluetoothGatt = device.connectGatt(context, false, gattCallback)
            Log.d(TAG, "Connecting to device: ${device.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed: ${e.message}")
            onError?.invoke("Connexion échouée: ${e.message}")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to device")
                scope.launch {
                    onConnected?.invoke()
                }
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from device")
                scope.launch {
                    onDisconnected?.invoke()
                }
                // Relancer la recherche
                scope.launch {
                    delay(2000)
                    startScan()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered")

                // Trouver la caractéristique de contrôle
                val service = gatt.getService(UUID.fromString(SERVICE_UUID))
                characteristicWrite = service?.getCharacteristic(
                    UUID.fromString(CHARACTERISTIC_UUID)
                )

                // S'abonner aux notifications batterie
                val batteryService = gatt.getService(UUID.fromString(BATTERY_SERVICE_UUID))
                val batteryChar = batteryService?.getCharacteristic(
                    UUID.fromString(BATTERY_CHARACTERISTIC_UUID)
                )
                if (batteryChar != null) {
                    gatt.setCharacteristicNotification(batteryChar, true)

                    // Configurer le descripteur
                    val descriptor = batteryChar.getDescriptor(
                        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                    )
                    descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                }
            } else {
                Log.e(TAG, "Service discovery failed with status: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            when (characteristic.uuid.toString()) {
                BATTERY_CHARACTERISTIC_UUID -> {
                    val batteryLevel = characteristic.value[0].toInt() and 0xFF
                    scope.launch {
                        onBatteryUpdate?.invoke(batteryLevel)
                    }
                    Log.d(TAG, "Battery: $batteryLevel%")
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic read successful")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Write failed with status: $status")
            }
        }
    }

    fun sendCommand(throttle: Int, yaw: Int, pitch: Int = 0, roll: Int = 0) {
        try {
            if (characteristicWrite == null) {
                Log.w(TAG, "Characteristic not found")
                return
            }

            val payload = byteArrayOf(
                throttle.coerceIn(-100, 100).toByte(),
                yaw.coerceIn(-100, 100).toByte(),
                pitch.coerceIn(-100, 100).toByte(),
                roll.coerceIn(-100, 100).toByte()
            )

            characteristicWrite?.value = payload
            bluetoothGatt?.writeCharacteristic(characteristicWrite!!)
        } catch (e: Exception) {
            Log.e(TAG, "Send command failed: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            bluetoothGatt?.close()
            scope.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Disconnect failed: ${e.message}")
        }
    }

    fun isConnected(): Boolean {
        return bluetoothGatt != null
    }
}
