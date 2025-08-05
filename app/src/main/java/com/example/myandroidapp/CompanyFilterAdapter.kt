package com.example.myandroidapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CompanyFilterAdapter(
    private var companies: List<CompanyFilter>,
    private val onCompanySelectionChanged: (CompanyFilter, Boolean) -> Unit
) : RecyclerView.Adapter<CompanyFilterAdapter.CompanyFilterViewHolder>() {

    fun updateCompanies(newCompanies: List<CompanyFilter>) {
        companies = newCompanies
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyFilterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_company_filter, parent, false)
        return CompanyFilterViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompanyFilterViewHolder, position: Int) {
        holder.bind(companies[position])
    }

    override fun getItemCount(): Int = companies.size

    inner class CompanyFilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.companyCheckBox)
        private val companyName: TextView = itemView.findViewById(R.id.companyName)
        private val deviceCount: TextView = itemView.findViewById(R.id.deviceCount)

        fun bind(company: CompanyFilter) {
            companyName.text = company.companyName
            deviceCount.text = "${company.deviceCount}개 기기"
            checkBox.isChecked = company.isSelected

            // 체크박스 클릭 이벤트
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                company.isSelected = isChecked
                onCompanySelectionChanged(company, isChecked)
            }

            // 아이템 전체 클릭 이벤트
            itemView.setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
            }
        }
    }
} 