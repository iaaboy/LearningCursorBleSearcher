package com.example.myandroidapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class DeviceAdapter : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {
    
    private var devices: List<BleDevice> = emptyList()
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    fun updateDevices(newDevices: List<BleDevice>) {
        devices = newDevices
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }
    
    override fun getItemCount(): Int = devices.size
    
    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deviceName: TextView = itemView.findViewById(R.id.deviceName)
        private val companyInfo: TextView = itemView.findViewById(R.id.companyInfo)
        private val manufacturerData: TextView = itemView.findViewById(R.id.manufacturerData)
        private val lastSeen: TextView = itemView.findViewById(R.id.lastSeen)
        private val rssiText: TextView = itemView.findViewById(R.id.rssiText)
        
        fun bind(device: BleDevice) {
            // 기기명과 MAC 주소를 함께 표시 (신호 강도에 따른 색상 적용)
            deviceName.text = "${device.name} (${device.address})"
            deviceName.setTextColor(device.getSignalStrengthColor())
            
            // Company 정보
            companyInfo.text = "Company: ${device.getCompanyName()}"
            
            // Manufacturer Data (16진수로 표시)
            manufacturerData.text = "Manufacturer Data: ${device.getManufacturerDataHex()}"
            
            // 마지막 감지 시간
            val timeString = dateFormat.format(Date(device.lastSeen))
            lastSeen.text = "Last seen: $timeString"
            
            // RSSI 정보
            rssiText.text = "${device.rssi} dBm (${device.getSignalStrength()})"
            rssiText.setTextColor(device.getSignalStrengthColor())
        }
    }
} 