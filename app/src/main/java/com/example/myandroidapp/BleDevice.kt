package com.example.myandroidapp

import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

data class BleDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val isConnected: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromScanResult(result: ScanResult): BleDevice {
            return BleDevice(
                name = try {
                    result.device.name ?: "Unknown Device"
                } catch (e: SecurityException) {
                    "Unknown Device"
                },
                address = result.device.address,
                rssi = result.rssi
            )
        }
    }
    
    fun getSignalStrength(): String {
        return when {
            rssi >= -50 -> "Excellent"
            rssi >= -60 -> "Good"
            rssi >= -70 -> "Fair"
            rssi >= -80 -> "Poor"
            else -> "Very Poor"
        }
    }
    
    fun getSignalStrengthColor(): Int {
        return when {
            rssi >= -50 -> android.graphics.Color.rgb(0, 128, 0)  // 진한 녹색
            rssi >= -60 -> android.graphics.Color.rgb(34, 139, 34)  // Forest Green
            rssi >= -70 -> android.graphics.Color.rgb(255, 140, 0)  // 진한 주황색
            rssi >= -80 -> android.graphics.Color.rgb(255, 69, 0)   // Red Orange
            else -> android.graphics.Color.rgb(220, 20, 60)         // Crimson
        }
    }
} 