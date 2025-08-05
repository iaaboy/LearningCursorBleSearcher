package com.example.myandroidapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class GroupedDeviceAdapter : RecyclerView.Adapter<GroupedDeviceAdapter.GroupViewHolder>() {
    
    private var deviceGroups: List<DeviceGroup> = emptyList()
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    fun updateDeviceGroups(newGroups: List<DeviceGroup>) {
        deviceGroups = newGroups
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device_group, parent, false)
        return GroupViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(deviceGroups[position])
    }
    
    override fun getItemCount(): Int = deviceGroups.size
    
    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupHeader: TextView = itemView.findViewById(R.id.groupHeader)
        private val deviceCount: TextView = itemView.findViewById(R.id.deviceCount)
        private val devicesRecyclerView: RecyclerView = itemView.findViewById(R.id.devicesRecyclerView)
        private val deviceAdapter = DeviceAdapter()
        
        init {
            devicesRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            devicesRecyclerView.adapter = deviceAdapter
        }
        
        fun bind(group: DeviceGroup) {
            // 그룹 헤더 (회사명)
            groupHeader.text = group.companyName
            
            // 기기 개수
            deviceCount.text = "${group.deviceCount}개 기기"
            
            // 하위 기기들 표시
            deviceAdapter.updateDevices(group.devices)
        }
    }
} 