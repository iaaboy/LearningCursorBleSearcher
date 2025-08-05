package com.example.myandroidapp

data class CompanyFilter(
    val companyName: String,
    val companyId: Int?,
    val deviceCount: Int,
    var isSelected: Boolean = true
) 