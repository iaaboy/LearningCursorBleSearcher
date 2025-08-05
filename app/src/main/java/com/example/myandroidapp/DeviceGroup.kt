package com.example.myandroidapp

data class DeviceGroup(
    val companyName: String,
    val companyId: Int?,
    val devices: List<BleDevice>
) {
    val deviceCount: Int
        get() = devices.size
} 