package com.alarmify.wakeupnerd

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.alarmify.wakeupnerd.databinding.ActivityListAlarmBinding
import android.content.Intent
import com.alarmify.wakeupnerd.data.Alarm
import com.alarmify.wakeupnerd.data.AlarmAdapter

class ListAlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListAlarmBinding
    private lateinit var alarmAdapter: AlarmAdapter
    private val alarmList = mutableListOf<Alarm>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityListAlarmBinding.inflate(layoutInflater)
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
            startActivity(intent)
        }

        binding.btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        alarmAdapter = AlarmAdapter(alarmList) { position, isChecked ->
            alarmList[position].isEnabled = isChecked
        }

        binding.recyclerViewAlarms.apply {
            layoutManager = LinearLayoutManager(this@ListAlarmActivity)
            adapter = alarmAdapter
        }
    }

    private fun loadAlarms() {
        alarmList.clear()
        alarmList.addAll(listOf(
            Alarm("07:00", "Sekali, Hari Minggu", true, false),
            Alarm("12:00", "Harian, Kerja Kantor", false, true),
            Alarm("09:30", "Sekali, Sarapan", true, false)
        ))
        alarmAdapter.notifyDataSetChanged()
    }
}