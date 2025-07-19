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
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return DeviceViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }
    
    override fun getItemCount(): Int = devices.size
    
    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)
        
        fun bind(device: BleDevice) {
            text1.text = "${device.name}\n${device.address}"
            text1.setTextColor(device.getSignalStrengthColor())
            
            val timeString = dateFormat.format(Date(device.lastSeen))
            text2.text = "RSSI: ${device.rssi} dBm (${device.getSignalStrength()})\nLast seen: $timeString"
            text2.setTextColor(android.graphics.Color.rgb(33, 33, 33)) // 진한 회색으로 변경
        }
    }
} 