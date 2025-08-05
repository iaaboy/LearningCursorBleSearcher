package com.example.myandroidapp

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class BleDataExporter(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    enum class ExportFormat {
        CSV, JSON, TXT
    }
    
    /**
     * BLE 데이터를 파일로 저장
     */
    fun exportBleData(
        devices: List<BleDevice>,
        format: ExportFormat,
        filename: String? = null
    ): File? {
        return try {
            val fileName = filename ?: "ble_scan_${dateFormat.format(Date())}"
            val file = createFile(fileName, format)
            
            when (format) {
                ExportFormat.CSV -> exportToCsv(devices, file)
                ExportFormat.JSON -> exportToJson(devices, file)
                ExportFormat.TXT -> exportToTxt(devices, file)
            }
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 그룹화된 BLE 데이터를 파일로 저장
     */
    fun exportGroupedBleData(
        deviceGroups: List<DeviceGroup>,
        format: ExportFormat,
        filename: String? = null
    ): File? {
        return try {
            val fileName = filename ?: "ble_scan_grouped_${dateFormat.format(Date())}"
            val file = createFile(fileName, format)
            
            when (format) {
                ExportFormat.CSV -> exportGroupedToCsv(deviceGroups, file)
                ExportFormat.JSON -> exportGroupedToJson(deviceGroups, file)
                ExportFormat.TXT -> exportGroupedToTxt(deviceGroups, file)
            }
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun createFile(fileName: String, format: ExportFormat): File {
        val extension = when (format) {
            ExportFormat.CSV -> ".csv"
            ExportFormat.JSON -> ".json"
            ExportFormat.TXT -> ".txt"
        }
        
        // 앱 내부 저장소의 files 디렉토리 사용
        val filesDir = context.filesDir
        return File(filesDir, "$fileName$extension")
    }
    
    private fun exportToCsv(devices: List<BleDevice>, file: File) {
        FileWriter(file).use { writer ->
            // CSV 헤더
            writer.append("기기명,MAC 주소,RSSI (dBm),신호 강도,회사명,Manufacturer Data,마지막 감지 시간\n")
            
            // 데이터
            devices.forEach { device ->
                writer.append("\"${device.name}\",")
                writer.append("${device.address},")
                writer.append("${device.rssi},")
                writer.append("${device.getSignalStrength()},")
                writer.append("\"${device.getCompanyName()}\",")
                writer.append("\"${device.getManufacturerDataHex()}\",")
                writer.append("${timeFormat.format(Date(device.lastSeen))}\n")
            }
        }
    }
    
    private fun exportToJson(devices: List<BleDevice>, file: File) {
        FileWriter(file).use { writer ->
            writer.append("{\n")
            writer.append("  \"scanTime\": \"${timeFormat.format(Date())}\",\n")
            writer.append("  \"totalDevices\": ${devices.size},\n")
            writer.append("  \"devices\": [\n")
            
            devices.forEachIndexed { index, device ->
                writer.append("    {\n")
                writer.append("      \"name\": \"${device.name}\",\n")
                writer.append("      \"address\": \"${device.address}\",\n")
                writer.append("      \"rssi\": ${device.rssi},\n")
                writer.append("      \"signalStrength\": \"${device.getSignalStrength()}\",\n")
                writer.append("      \"companyName\": \"${device.getCompanyName()}\",\n")
                writer.append("      \"manufacturerData\": \"${device.getManufacturerDataHex()}\",\n")
                writer.append("      \"lastSeen\": \"${timeFormat.format(Date(device.lastSeen))}\"\n")
                writer.append("    }${if (index < devices.size - 1) "," else ""}\n")
            }
            
            writer.append("  ]\n")
            writer.append("}\n")
        }
    }
    
    private fun exportToTxt(devices: List<BleDevice>, file: File) {
        FileWriter(file).use { writer ->
            writer.append("BLE 스캔 결과\n")
            writer.append("스캔 시간: ${timeFormat.format(Date())}\n")
            writer.append("총 기기 수: ${devices.size}개\n")
            writer.append("=".repeat(50) + "\n\n")
            
            devices.forEachIndexed { index, device ->
                writer.append("기기 ${index + 1}:\n")
                writer.append("  기기명: ${device.name}\n")
                writer.append("  MAC 주소: ${device.address}\n")
                writer.append("  RSSI: ${device.rssi} dBm (${device.getSignalStrength()})\n")
                writer.append("  회사: ${device.getCompanyName()}\n")
                writer.append("  Manufacturer Data: ${device.getManufacturerDataHex()}\n")
                writer.append("  마지막 감지: ${timeFormat.format(Date(device.lastSeen))}\n")
                writer.append("\n")
            }
        }
    }
    
    private fun exportGroupedToCsv(deviceGroups: List<DeviceGroup>, file: File) {
        FileWriter(file).use { writer ->
            // CSV 헤더
            writer.append("회사명,기기 수,기기명,MAC 주소,RSSI (dBm),신호 강도,Manufacturer Data,마지막 감지 시간\n")
            
            // 데이터
            deviceGroups.forEach { group ->
                group.devices.forEach { device ->
                    writer.append("\"${group.companyName}\",")
                    writer.append("${group.deviceCount},")
                    writer.append("\"${device.name}\",")
                    writer.append("${device.address},")
                    writer.append("${device.rssi},")
                    writer.append("${device.getSignalStrength()},")
                    writer.append("\"${device.getManufacturerDataHex()}\",")
                    writer.append("${timeFormat.format(Date(device.lastSeen))}\n")
                }
            }
        }
    }
    
    private fun exportGroupedToJson(deviceGroups: List<DeviceGroup>, file: File) {
        FileWriter(file).use { writer ->
            writer.append("{\n")
            writer.append("  \"scanTime\": \"${timeFormat.format(Date())}\",\n")
            writer.append("  \"totalCompanies\": ${deviceGroups.size},\n")
            writer.append("  \"totalDevices\": ${deviceGroups.sumOf { it.deviceCount }},\n")
            writer.append("  \"groups\": [\n")
            
            deviceGroups.forEachIndexed { groupIndex, group ->
                writer.append("    {\n")
                writer.append("      \"companyName\": \"${group.companyName}\",\n")
                writer.append("      \"deviceCount\": ${group.deviceCount},\n")
                writer.append("      \"devices\": [\n")
                
                group.devices.forEachIndexed { deviceIndex, device ->
                    writer.append("        {\n")
                    writer.append("          \"name\": \"${device.name}\",\n")
                    writer.append("          \"address\": \"${device.address}\",\n")
                    writer.append("          \"rssi\": ${device.rssi},\n")
                    writer.append("          \"signalStrength\": \"${device.getSignalStrength()}\",\n")
                    writer.append("          \"manufacturerData\": \"${device.getManufacturerDataHex()}\",\n")
                    writer.append("          \"lastSeen\": \"${timeFormat.format(Date(device.lastSeen))}\"\n")
                    writer.append("        }${if (deviceIndex < group.devices.size - 1) "," else ""}\n")
                }
                
                writer.append("      ]\n")
                writer.append("    }${if (groupIndex < deviceGroups.size - 1) "," else ""}\n")
            }
            
            writer.append("  ]\n")
            writer.append("}\n")
        }
    }
    
    private fun exportGroupedToTxt(deviceGroups: List<DeviceGroup>, file: File) {
        FileWriter(file).use { writer ->
            writer.append("BLE 스캔 결과 (회사별 그룹화)\n")
            writer.append("스캔 시간: ${timeFormat.format(Date())}\n")
            writer.append("총 회사 수: ${deviceGroups.size}개\n")
            writer.append("총 기기 수: ${deviceGroups.sumOf { it.deviceCount }}개\n")
            writer.append("=".repeat(60) + "\n\n")
            
            deviceGroups.forEachIndexed { groupIndex, group ->
                writer.append("회사 ${groupIndex + 1}: ${group.companyName}\n")
                writer.append("기기 수: ${group.deviceCount}개\n")
                writer.append("-".repeat(40) + "\n")
                
                group.devices.forEachIndexed { deviceIndex, device ->
                    writer.append("  기기 ${deviceIndex + 1}:\n")
                    writer.append("    기기명: ${device.name}\n")
                    writer.append("    MAC 주소: ${device.address}\n")
                    writer.append("    RSSI: ${device.rssi} dBm (${device.getSignalStrength()})\n")
                    writer.append("    Manufacturer Data: ${device.getManufacturerDataHex()}\n")
                    writer.append("    마지막 감지: ${timeFormat.format(Date(device.lastSeen))}\n")
                    writer.append("\n")
                }
                writer.append("\n")
            }
        }
    }
} 