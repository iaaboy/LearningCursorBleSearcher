package com.example.myandroidapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TableDeviceAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    private var devices: List<BleDevice> = emptyList()
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_DEVICE = 1
    }
    
    fun updateDevices(newDevices: List<BleDevice>) {
        devices = newDevices
        notifyDataSetChanged()
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_DEVICE
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_table_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_table_device, parent, false)
                DeviceViewHolder(view)
            }
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                // 헤더는 고정 텍스트
            }
            is DeviceViewHolder -> {
                holder.bind(devices[position - 1]) // 헤더 때문에 -1
            }
        }
    }
    
    override fun getItemCount(): Int = devices.size + 1 // 헤더 포함
    
    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 헤더는 레이아웃에서 고정 텍스트로 처리
    }
    
    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deviceName: TextView = itemView.findViewById(R.id.tableDeviceName)
        private val deviceAddress: TextView = itemView.findViewById(R.id.tableDeviceAddress)
        private val companyName: TextView = itemView.findViewById(R.id.tableCompanyName)
        private val rssi: TextView = itemView.findViewById(R.id.tableRssi)
        private val lastSeen: TextView = itemView.findViewById(R.id.tableLastSeen)
        
        fun bind(device: BleDevice) {
            deviceName.text = device.name
            deviceAddress.text = device.address
            companyName.text = device.getCompanyName().split(" (")[0] // Company ID 제외하고 이름만
            rssi.text = "${device.rssi} dBm"
            rssi.setTextColor(device.getSignalStrengthColor())
            lastSeen.text = dateFormat.format(Date(device.lastSeen))
        }
    }
}