package com.example.myandroidapp

import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.util.SparseArray
import androidx.core.app.ActivityCompat

data class BleDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val isConnected: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val companyId: Int? = null,
    val manufacturerData: ByteArray? = null
) {
    companion object {
        fun fromScanResult(result: ScanResult): BleDevice {
            var companyId: Int? = null
            var manufacturerData: ByteArray? = null
            
            // Manufacturer Specific Data 추출
            result.scanRecord?.let { scanRecord ->
                val manufacturerSpecificData: SparseArray<ByteArray>? = scanRecord.manufacturerSpecificData
                if (manufacturerSpecificData != null && manufacturerSpecificData.size() > 0) {
                    // 첫 번째 Company ID와 데이터를 사용
                    val firstKey = manufacturerSpecificData.keyAt(0)
                    val dataBytes = manufacturerSpecificData.valueAt(0)
                    
                    if (dataBytes != null && dataBytes.size >= 2) {
                        companyId = firstKey
                        // Company ID를 제외한 나머지 데이터
                        manufacturerData = dataBytes.copyOfRange(2, dataBytes.size)
                    }
                }
            }
            
            return BleDevice(
                name = try {
                    result.device.name ?: "Unknown Device"
                } catch (e: SecurityException) {
                    "Unknown Device"
                },
                address = result.device.address,
                rssi = result.rssi,
                companyId = companyId,
                manufacturerData = manufacturerData
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
    
    fun getCompanyName(): String {
        return companyId?.let { id ->
            val companyName = CompanyId.getCompanyName(id)
            "$companyName (0x${id.toString(16).uppercase().padStart(4, '0')})"
        } ?: "N/A"
    }
    
    fun getManufacturerDataHex(): String {
        return manufacturerData?.let { data ->
            data.joinToString(" ") { "%02X".format(it) }
        } ?: "N/A"
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BleDevice

        if (name != other.name) return false
        if (address != other.address) return false
        if (rssi != other.rssi) return false
        if (isConnected != other.isConnected) return false
        if (lastSeen != other.lastSeen) return false
        if (companyId != other.companyId) return false
        if (manufacturerData != null) {
            if (other.manufacturerData == null) return false
            if (!manufacturerData.contentEquals(other.manufacturerData)) return false
        } else if (other.manufacturerData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + rssi
        result = 31 * result + isConnected.hashCode()
        result = 31 * result + lastSeen.hashCode()
        result = 31 * result + (companyId ?: 0)
        result = 31 * result + (manufacturerData?.contentHashCode() ?: 0)
        return result
    }
} 