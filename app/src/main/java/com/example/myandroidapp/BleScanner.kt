package com.example.myandroidapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.concurrent.ConcurrentHashMap

class BleScanner(private val context: Context) {
    
    private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    
    private val discoveredDevices = ConcurrentHashMap<String, BleDevice>()
    private var isScanning = false
    
    var onDeviceDiscovered: ((BleDevice) -> Unit)? = null
    var onScanStarted: (() -> Unit)? = null
    var onScanStopped: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            
            val device = BleDevice.fromScanResult(result)
            discoveredDevices[device.address] = device
            
            onDeviceDiscovered?.invoke(device)
            Log.d("BleScanner", "Discovered: ${device.name} (${device.address}) RSSI: ${device.rssi}")
        }
        
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            val errorMessage = when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED -> "Scan already started"
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "Application registration failed"
                SCAN_FAILED_FEATURE_UNSUPPORTED -> "BLE not supported"
                SCAN_FAILED_INTERNAL_ERROR -> "Internal error"
                else -> "Unknown error: $errorCode"
            }
            onError?.invoke(errorMessage)
            Log.e("BleScanner", "Scan failed: $errorMessage")
        }
    }
    
    fun startScan(): Boolean {
        if (isScanning) {
            Log.w("BleScanner", "Scan already in progress")
            return false
        }
        
        if (bluetoothAdapter == null) {
            onError?.invoke("Bluetooth not supported")
            return false
        }
        
        if (!bluetoothAdapter.isEnabled) {
            onError?.invoke("Bluetooth is disabled")
            return false
        }
        
        if (bluetoothLeScanner == null) {
            onError?.invoke("BLE not supported")
            return false
        }
        
        // 권한 확인
        if (!hasRequiredPermissions()) {
            onError?.invoke("Required permissions not granted")
            return false
        }
        
        try {
            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
            
            bluetoothLeScanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
            onScanStarted?.invoke()
            Log.d("BleScanner", "BLE scan started")
            return true
            
        } catch (e: SecurityException) {
            onError?.invoke("Permission denied: ${e.message}")
            return false
        } catch (e: Exception) {
            onError?.invoke("Failed to start scan: ${e.message}")
            return false
        }
    }
    
    fun stopScan() {
        if (!isScanning) {
            return
        }
        
        try {
            bluetoothLeScanner?.stopScan(scanCallback)
            isScanning = false
            onScanStopped?.invoke()
            Log.d("BleScanner", "BLE scan stopped")
        } catch (e: Exception) {
            Log.e("BleScanner", "Failed to stop scan: ${e.message}")
        }
    }
    
    fun getDiscoveredDevices(): List<BleDevice> {
        return discoveredDevices.values.toList().sortedByDescending { it.rssi }
    }
    
    fun clearDevices() {
        discoveredDevices.clear()
    }
    
    fun isScanning(): Boolean = isScanning
    
    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }
} 