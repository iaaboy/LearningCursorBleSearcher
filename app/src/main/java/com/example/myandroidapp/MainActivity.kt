package com.example.myandroidapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myandroidapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var bleScanner: BleScanner
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var deviceAdapter: DeviceAdapter
    
    private val bluetoothManager: BluetoothManager by lazy { getSystemService(BLUETOOTH_SERVICE) as BluetoothManager }
    private val bluetoothAdapter: BluetoothAdapter? by lazy { bluetoothManager.adapter }
    
    // 코루틴을 사용한 UI 업데이트
    private var updateJob: Job? = null
    private var updateInterval: Long = SettingsActivity.INTERVAL_1_SECOND
    
    // Activity Result API for Bluetooth enable
    private val bluetoothEnableLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            showToast("블루투스가 활성화되었습니다")
        } else {
            showToast("블루투스 활성화가 거부되었습니다")
        }
    }
    
    // Activity Result API for Settings
    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == SettingsActivity.RESULT_UPDATE_INTERVAL_CHANGED) {
            val newInterval = result.data?.getLongExtra(
                SettingsActivity.EXTRA_UPDATE_INTERVAL, 
                SettingsActivity.INTERVAL_1_SECOND
            ) ?: SettingsActivity.INTERVAL_1_SECOND
            
            updateInterval = newInterval
            showToast("업데이트 주기가 변경되었습니다")
            
            // 스캔 중이면 코루틴 재시작
            if (bleScanner.isScanning()) {
                stopPeriodicUpdate()
                startPeriodicUpdate()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeComponents()
        setupUI()
        checkPermissions()
    }
    
    private fun initializeComponents() {
        bleScanner = BleScanner(this)
        permissionHelper = PermissionHelper(this)
        deviceAdapter = DeviceAdapter()
        
        // BLE 스캐너 콜백 설정
        bleScanner.onDeviceDiscovered = { _ ->
            // 실시간 모드일 때만 즉시 업데이트
            if (updateInterval == SettingsActivity.INTERVAL_REALTIME) {
                runOnUiThread {
                    updateDeviceList()
                }
            }
        }
        
        bleScanner.onScanStarted = {
            runOnUiThread {
                updateScanButton(true)
                showToast("BLE 스캔 시작됨")
                startPeriodicUpdate() // 코루틴 시작
            }
        }
        
        bleScanner.onScanStopped = {
            runOnUiThread {
                updateScanButton(false)
                showToast("BLE 스캔 중지됨")
                stopPeriodicUpdate() // 코루틴 중지
            }
        }
        
        bleScanner.onError = { error ->
            runOnUiThread {
                showToast("오류: $error")
            }
        }
    }

    private fun setupUI() {
        binding.titleText.text = "BLE 기기 스캐너"
        
        // RecyclerView 설정
        binding.deviceList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = deviceAdapter
        }
        
        // 스캔 버튼 설정
        binding.scanButton.setOnClickListener {
            if (bleScanner.isScanning()) {
                stopScan()
            } else {
                startScan()
            }
        }
        
        // 클리어 버튼 설정
        binding.clearButton.setOnClickListener {
            bleScanner.clearDevices()
            updateDeviceList()
            showToast("기기 목록이 클리어되었습니다")
        }
        
        // 초기 상태 설정
        updateScanButton(false)
    }
    
    private fun startScan() {
        if (!permissionHelper.hasAllPermissions()) {
            showPermissionDialog()
            return
        }
        
        if (bluetoothAdapter?.isEnabled == false) {
            showBluetoothEnableDialog()
            return
        }
        
        bleScanner.startScan()
    }
    
    private fun stopScan() {
        bleScanner.stopScan()
    }
    
    private fun updateDeviceList() {
        val devices = bleScanner.getDiscoveredDevices()
        deviceAdapter.updateDevices(devices)
        binding.deviceCountText.text = "발견된 기기: ${devices.size}개"
    }
    
    private fun startPeriodicUpdate() {
        // 실시간 모드면 코루틴 시작하지 않음
        if (updateInterval == SettingsActivity.INTERVAL_REALTIME) {
            return
        }
        
        updateJob = lifecycleScope.launch {
            while (isActive) {
                updateDeviceList()
                delay(updateInterval) // 설정된 주기마다 업데이트
            }
        }
    }
    
    private fun stopPeriodicUpdate() {
        updateJob?.cancel()
        updateJob = null
    }
    
    private fun updateScanButton(isScanning: Boolean) {
        binding.scanButton.text = if (isScanning) "스캔 중지" else "스캔 시작"
        binding.scanButton.isEnabled = true
    }
    
    private fun checkPermissions() {
        if (!permissionHelper.hasAllPermissions()) {
            permissionHelper.requestPermissions()
        }
    }
    
    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("권한 필요")
            .setMessage("BLE 스캔을 위해 위치 권한이 필요합니다.")
            .setPositiveButton("권한 요청") { _, _ ->
                permissionHelper.requestPermissions()
            }
            .setNegativeButton("취소", null)
            .show()
    }
    
    private fun showBluetoothEnableDialog() {
        AlertDialog.Builder(this)
            .setTitle("블루투스 비활성화")
            .setMessage("BLE 스캔을 위해 블루투스를 활성화해야 합니다.")
            .setPositiveButton("활성화") { _, _ ->
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                bluetoothEnableLauncher.launch(enableBtIntent)
            }
            .setNegativeButton("취소", null)
            .show()
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PermissionHelper.PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                showToast("권한이 승인되었습니다")
            } else {
                showToast("권한이 거부되었습니다. 앱 설정에서 권한을 허용해주세요.")
            }
        }
    }
    

    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                openSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java).apply {
            putExtra(SettingsActivity.EXTRA_UPDATE_INTERVAL, updateInterval)
        }
        settingsLauncher.launch(intent)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopPeriodicUpdate() // 코루틴 정리
        bleScanner.stopScan()
    }
} 