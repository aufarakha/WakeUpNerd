package com.alarmify.wakeupnerd

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.alarmify.wakeupnerd.databinding.ActivityHomeBinding
import android.content.Intent
import com.alarmify.wakeupnerd.data.Alarm
import com.alarmify.wakeupnerd.data.AlarmAdapter
import com.alarmify.wakeupnerd.data.AlarmStorageManager


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var alarmAdapter: AlarmAdapter
    private val alarmList = mutableListOf<Alarm>()

    // ActivityResultLauncher for AddAlarmActivity
    private val addEditAlarmLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Reload alarms when returning from AddAlarmActivity
            loadAlarms()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        loadAlarms()

        binding.tambahAlarm.setOnClickListener {
            val intent = Intent(this, AddAlarmActivity::class.java)
            addEditAlarmLauncher.launch(intent)
        }

        binding.btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Check permissions on first launch
        checkAndRequestPermissions()

        binding.tvTitle.setOnClickListener {
            val intent = Intent(this, AlarmPlayActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        checkPermissionsIfNeeded()
        // Reload alarms when returning to this activity
        loadAlarms()
    }

    private fun setupRecyclerView() {
        alarmAdapter = AlarmAdapter(
            alarmList,
            onSwitchChanged = { position, isChecked ->
                // Update alarm enabled state
                val alarm = alarmList[position]
                val updatedAlarm = alarm.copy(isEnabled = isChecked)
                AlarmStorageManager.saveAlarm(this, updatedAlarm)
                alarmList[position] = updatedAlarm
            },
            onAlarmClick = { position ->
                openEditAlarm(position)
            }
        )

        binding.recyclerViewAlarms.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = alarmAdapter
        }
    }

    private fun openEditAlarm(position: Int) {
        val alarm = alarmList[position]
        val intent = Intent(this, AddAlarmActivity::class.java).apply {
            putExtra("EDIT_MODE", true)
            putExtra("ALARM_ID", alarm.id)
        }
        addEditAlarmLauncher.launch(intent)
    }

    private fun loadAlarms() {
        alarmList.clear()

        // Load alarms from storage
        val storedAlarms = AlarmStorageManager.getAllAlarms(this)
        alarmList.addAll(storedAlarms)

        // Sort alarms by time (optional)
        alarmList.sortWith(compareBy({ it.isPM }, { it.hour }, { it.minute }))

        alarmAdapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun updateEmptyState() {
        val isEmpty = alarmList.isEmpty()
        binding.ivWeather.isVisible = isEmpty
        binding.tvNoAlarm.isVisible = isEmpty
        binding.tvAddAlarmHint.isVisible = isEmpty
        binding.recyclerViewAlarms.isVisible = !isEmpty
    }


    private fun checkAndRequestPermissions() {
        // Check if this is first launch or permissions are missing
        val prefs = getSharedPreferences("WakeUpNerdPrefs", MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("isFirstLaunch", true)

        if (isFirstLaunch || !com.alarmify.wakeupnerd.utils.PermissionHelper.checkAllPermissions(this)) {
            // Mark as not first launch
            prefs.edit().putBoolean("isFirstLaunch", false).apply()

            // Show permission dialog
            com.alarmify.wakeupnerd.utils.PermissionHelper.requestMissingPermissions(this)
        }
    }

    private fun checkPermissionsIfNeeded() {
        // Silently check if permissions are still missing
        if (!com.alarmify.wakeupnerd.utils.PermissionHelper.checkAllPermissions(this)) {
            // Don't show dialog every time, just on first launch
            // User can manually grant from settings
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            com.alarmify.wakeupnerd.utils.PermissionHelper.REQUEST_CODE_POST_NOTIFICATIONS -> {
                // After POST_NOTIFICATIONS, check next permission
                com.alarmify.wakeupnerd.utils.PermissionHelper.requestMissingPermissions(this)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            com.alarmify.wakeupnerd.utils.PermissionHelper.REQUEST_CODE_SYSTEM_ALERT_WINDOW,
            com.alarmify.wakeupnerd.utils.PermissionHelper.REQUEST_CODE_SCHEDULE_EXACT_ALARM,
            com.alarmify.wakeupnerd.utils.PermissionHelper.REQUEST_CODE_NOTIFICATIONS -> {
                // After each permission, check next one
                com.alarmify.wakeupnerd.utils.PermissionHelper.requestMissingPermissions(this)
            }
        }
    }
}