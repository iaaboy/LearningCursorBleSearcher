package com.example.myandroidapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myandroidapp.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    
    companion object {
        const val EXTRA_UPDATE_INTERVAL = "update_interval"
        const val RESULT_UPDATE_INTERVAL_CHANGED = 100
        
        const val INTERVAL_REALTIME = 0L
        const val INTERVAL_1_SECOND = 1000L
        const val INTERVAL_3_SECONDS = 3000L
        const val INTERVAL_5_SECONDS = 5000L
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 현재 설정값 로드
        val currentInterval = intent.getLongExtra(EXTRA_UPDATE_INTERVAL, INTERVAL_1_SECOND)
        setCurrentSelection(currentInterval)
        
        // 확인 버튼 클릭
        binding.btnConfirm.setOnClickListener {
            val selectedInterval = getSelectedInterval()
            val resultIntent = Intent().apply {
                putExtra(EXTRA_UPDATE_INTERVAL, selectedInterval)
            }
            setResult(RESULT_UPDATE_INTERVAL_CHANGED, resultIntent)
            finish()
        }
        
        // 취소 버튼 클릭
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }
    
    private fun setCurrentSelection(interval: Long) {
        val radioButtonId = when (interval) {
            INTERVAL_REALTIME -> R.id.radioRealtime
            INTERVAL_1_SECOND -> R.id.radio1Second
            INTERVAL_3_SECONDS -> R.id.radio3Seconds
            INTERVAL_5_SECONDS -> R.id.radio5Seconds
            else -> R.id.radio1Second
        }
        binding.updateIntervalGroup.check(radioButtonId)
    }
    
    private fun getSelectedInterval(): Long {
        return when (binding.updateIntervalGroup.checkedRadioButtonId) {
            R.id.radioRealtime -> INTERVAL_REALTIME
            R.id.radio1Second -> INTERVAL_1_SECOND
            R.id.radio3Seconds -> INTERVAL_3_SECONDS
            R.id.radio5Seconds -> INTERVAL_5_SECONDS
            else -> INTERVAL_1_SECOND
        }
    }
} 