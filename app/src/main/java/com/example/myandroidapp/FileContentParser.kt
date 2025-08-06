package com.example.myandroidapp

import org.json.JSONArray
import org.json.JSONObject

class FileContentParser {
    
    companion object {
        fun parseFileContent(content: String, fileType: String): List<BleDevice>? {
            return try {
                android.util.Log.d("FileContentParser", "Parsing file type: $fileType")
                val result = when (fileType.lowercase()) {
                    "csv" -> parseCsvContent(content)
                    "json" -> parseJsonContent(content)
                    "txt" -> parseTxtContent(content)
                    else -> {
                        android.util.Log.d("FileContentParser", "Unknown file type: $fileType")
                        null
                    }
                }
                android.util.Log.d("FileContentParser", "Parse result: ${result?.size ?: 0} devices")
                result
            } catch (e: Exception) {
                android.util.Log.e("FileContentParser", "Parse error: ${e.message}")
                null
            }
        }
        
        private fun parseCsvContent(content: String): List<BleDevice> {
            val devices = mutableListOf<BleDevice>()
            val lines = content.split("\n")
            
            android.util.Log.d("FileContentParser", "CSV lines count: ${lines.size}")
            if (lines.isNotEmpty()) {
                android.util.Log.d("FileContentParser", "CSV header: ${lines[0]}")
            }
            
            // 첫 번째 줄은 헤더이므로 건너뛰기
            for (i in 1 until lines.size) {
                val line = lines[i].trim()
                if (line.isEmpty()) continue
                
                val columns = line.split(",")
                android.util.Log.d("FileContentParser", "CSV line $i: ${columns.size} columns")
                
                if (columns.size >= 7) {
                    try {
                        // CSV 형식: "기기명,MAC 주소,RSSI (dBm),신호 강도,회사명,Manufacturer Data,마지막 감지 시간"
                        val companyName = columns[4].trim().removeSurrounding("\"")
                        val companyId = extractCompanyIdFromName(companyName)
                        
                        val device = BleDevice(
                            name = columns[0].trim().removeSurrounding("\""),
                            address = columns[1].trim(),
                            rssi = columns[2].trim().toInt(),
                            isConnected = false,
                            lastSeen = System.currentTimeMillis(),
                            companyId = companyId,
                            manufacturerData = parseManufacturerData(columns[5].trim().removeSurrounding("\""))
                        )
                        devices.add(device)
                        android.util.Log.d("FileContentParser", "Added device: ${device.name}")
                    } catch (e: Exception) {
                        android.util.Log.e("FileContentParser", "Failed to parse CSV line $i: ${e.message}")
                        continue
                    }
                } else {
                    android.util.Log.d("FileContentParser", "Skipping CSV line $i: insufficient columns")
                }
            }
            android.util.Log.d("FileContentParser", "CSV parsing complete: ${devices.size} devices")
            return devices
        }
        
        private fun parseJsonContent(content: String): List<BleDevice> {
            val devices = mutableListOf<BleDevice>()
            
            try {
                val jsonObject = JSONObject(content)
                
                // 단일 기기 목록 형식인지 확인
                if (jsonObject.has("devices")) {
                    val devicesArray = jsonObject.getJSONArray("devices")
                    for (i in 0 until devicesArray.length()) {
                        val deviceObj = devicesArray.getJSONObject(i)
                        parseJsonDevice(deviceObj)?.let { devices.add(it) }
                    }
                }
                // 그룹화된 형식인지 확인
                else if (jsonObject.has("groups")) {
                    val groupsArray = jsonObject.getJSONArray("groups")
                    for (i in 0 until groupsArray.length()) {
                        val groupObj = groupsArray.getJSONObject(i)
                        val devicesArray = groupObj.getJSONArray("devices")
                        for (j in 0 until devicesArray.length()) {
                            val deviceObj = devicesArray.getJSONObject(j)
                            parseJsonDevice(deviceObj)?.let { devices.add(it) }
                        }
                    }
                }
            } catch (e: Exception) {
                // JSON 파싱 실패
                return emptyList()
            }
            
            return devices
        }
        
        private fun parseJsonDevice(jsonObject: JSONObject): BleDevice? {
            return try {
                val companyName = jsonObject.optString("companyName", "")
                val companyId = extractCompanyIdFromName(companyName)
                
                BleDevice(
                    name = jsonObject.optString("name", "Unknown Device"),
                    address = jsonObject.getString("address"),
                    rssi = jsonObject.getInt("rssi"),
                    isConnected = false,
                    lastSeen = System.currentTimeMillis(),
                    companyId = companyId,
                    manufacturerData = parseManufacturerData(jsonObject.optString("manufacturerData", ""))
                )
            } catch (e: Exception) {
                null
            }
        }
        
        private fun parseTxtContent(content: String): List<BleDevice> {
            val devices = mutableListOf<BleDevice>()
            val lines = content.split("\n")
            
            var currentDevice: MutableMap<String, String>? = null
            
            for (line in lines) {
                val trimmedLine = line.trim()
                if (trimmedLine.isEmpty()) {
                    // 빈 줄이면 현재 기기 저장
                    currentDevice?.let { deviceMap ->
                        try {
                            val device = BleDevice(
                                name = deviceMap["name"] ?: "Unknown Device",
                                address = deviceMap["address"] ?: "",
                                rssi = deviceMap["rssi"]?.toInt() ?: -100,
                                isConnected = false,
                                lastSeen = System.currentTimeMillis(),
                                companyId = deviceMap["companyId"]?.takeIf { it != "null" }?.toInt(),
                                manufacturerData = parseManufacturerData(deviceMap["manufacturerData"] ?: "")
                            )
                            devices.add(device)
                        } catch (e: Exception) {
                            // 파싱 실패한 기기는 건너뛰기
                        }
                    }
                    currentDevice = null
                } else if (trimmedLine.startsWith("기기명:") || trimmedLine.startsWith("Device:")) {
                    currentDevice = mutableMapOf()
                    currentDevice["name"] = trimmedLine.substringAfter(":").trim()
                } else if (currentDevice != null) {
                    val parts = trimmedLine.split(":", limit = 2)
                    if (parts.size == 2) {
                        val key = parts[0].trim()
                        val value = parts[1].trim()
                        when {
                            key.contains("MAC") || key.contains("주소") -> currentDevice["address"] = value
                            key.contains("RSSI") || key.contains("신호") -> {
                                currentDevice["rssi"] = value.replace("dBm", "").trim()
                            }
                            key.contains("Company") || key.contains("제조사") -> {
                                // Company ID 추출 (0x0000 형태)
                                val companyIdMatch = Regex("0x([0-9A-Fa-f]{4})").find(value)
                                companyIdMatch?.let {
                                    currentDevice["companyId"] = it.groupValues[1].toInt(16).toString()
                                }
                            }
                            key.contains("Manufacturer") || key.contains("제조사") -> {
                                currentDevice["manufacturerData"] = value
                            }
                        }
                    }
                }
            }
            
            // 마지막 기기 처리
            currentDevice?.let { deviceMap ->
                try {
                    val device = BleDevice(
                        name = deviceMap["name"] ?: "Unknown Device",
                        address = deviceMap["address"] ?: "",
                        rssi = deviceMap["rssi"]?.toInt() ?: -100,
                        isConnected = false,
                        lastSeen = System.currentTimeMillis(),
                        companyId = deviceMap["companyId"]?.takeIf { it != "null" }?.toInt(),
                        manufacturerData = parseManufacturerData(deviceMap["manufacturerData"] ?: "")
                    )
                    devices.add(device)
                } catch (e: Exception) {
                    // 파싱 실패한 기기는 건너뛰기
                }
            }
            
            return devices
        }
        
        private fun extractCompanyIdFromName(companyName: String): Int? {
            // "Apple (0x004C)" 형태에서 0x004C 추출
            val regex = Regex("\\(0x([0-9A-Fa-f]{4})\\)")
            val matchResult = regex.find(companyName)
            return matchResult?.groupValues?.get(1)?.toInt(16)
        }
        
        private fun parseManufacturerData(dataString: String): ByteArray? {
            if (dataString.isEmpty() || dataString == "N/A" || dataString == "null") {
                return null
            }
            
            return try {
                // 16진수 문자열을 ByteArray로 변환
                val hexString = dataString.replace(" ", "").replace("-", "")
                if (hexString.length % 2 != 0) return null
                
                ByteArray(hexString.length / 2) { i ->
                    hexString.substring(i * 2, i * 2 + 2).toInt(16).toByte()
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}