package com.alarmify.wakeupnerd

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alarmify.wakeupnerd.databinding.ActivityAddAlarmBinding
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import com.alarmify.wakeupnerd.data.Alarm
import com.alarmify.wakeupnerd.data.AlarmStorageManager
import com.alarmify.wakeupnerd.data.AlarmScheduler

class AddAlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddAlarmBinding
    private var isEditMode = false
    private var currentAlarmId: String? = null
    private var isAmSelected = true // Track AM/PM selection
    private lateinit var audioManager: AudioManager

    // BroadcastReceiver for volume changes
    private val volumeChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "android.media.VOLUME_CHANGED_ACTION") {
                updateSeekBarFromPhoneVolume()
            }
        }
    }

    // ActivityResultLauncher for RingtoneActivity
    private val ringtonePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedRingtone = result.data?.getStringExtra("SELECTED_RINGTONE")
            selectedRingtone?.let {
                binding.valueRingtone.text = it
            }
        }
    }

    // ActivityResultLauncher for RepeatActivity
    private val repeatPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedRepeat = result.data?.getStringExtra("SELECTED_REPEAT")
            selectedRepeat?.let {
                binding.valueRepeat.text = it
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup number pickers
        setupNumberPickers()

        // Check if in edit mode
        isEditMode = intent.getBooleanExtra("EDIT_MODE", false)
        currentAlarmId = intent.getStringExtra("ALARM_ID")

        // Update UI based on mode
        if (isEditMode && currentAlarmId != null) {
            binding.tvTitle.text = "Edit Alarm"
            binding.btnCancel.text = "Hapus"
            loadAlarmData(currentAlarmId!!)
        } else {
            binding.tvTitle.text = "Tambah Alarm"
            binding.btnCancel.text = "Batal"
            // Set default values for new alarm
            setDefaultValues()
        }

        setupClickListeners()

        // Initialize AudioManager first
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        // Set volume control to alarm stream
        volumeControlStream = AudioManager.STREAM_ALARM

        setupSeekBar()
        registerVolumeReceiver()
    }

    private fun registerVolumeReceiver() {
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        registerReceiver(volumeChangeReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(volumeChangeReceiver)
        } catch (e: Exception) {
            // Receiver not registered
        }
    }

    private fun updateSeekBarFromPhoneVolume() {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)

        // Convert to percentage (0-100)
        val volumePercentage = ((currentVolume.toFloat() / maxVolume) * 100).toInt()

        // Update seekbar
        binding.seekBar11.progress = volumePercentage
    }

    private fun setupSeekBar() {
        // Initialize with phone's current alarm volume
        updateSeekBarFromPhoneVolume()
    }

    private fun setupNumberPickers() {
        // Block edit on number pickers
        binding.firsNum.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        binding.secNum.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        // Set ranges
        binding.firsNum.minValue = 1
        binding.firsNum.maxValue = 12
        binding.firsNum.value = 7 // Default 7:00 AM

        binding.secNum.minValue = 0
        binding.secNum.maxValue = 59
        binding.secNum.value = 0
    }

    private fun setDefaultValues() {
        binding.labeltxt.text = ""
        binding.valueRingtone.text = "Alarm clock"
        binding.valueRepeat.text = "Sekali"
        binding.valueVibrate.text = "Aktif"
        selectAmPm(true) // Default to AM
    }

    private fun setupClickListeners() {
        binding.btnCancel.setOnClickListener {
            if (isEditMode && currentAlarmId != null) {
                // Delete alarm
                deleteAlarm()
            } else {
                finish()
            }
        }

        binding.backHome.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            saveAlarm()
        }

        binding.cardLabel.setOnClickListener {
            showLabelDialog()
        }

        // AM/PM toggle functionality
        binding.btnAm.setOnClickListener {
            selectAmPm(true)
        }

        binding.btnPm.setOnClickListener {
            selectAmPm(false)
        }

        binding.cardRingtone.setOnClickListener {
            val intent = Intent(this, RingtoneActivity::class.java)
            ringtonePickerLauncher.launch(intent)
        }

        binding.cardRepeat.setOnClickListener {
            val intent = Intent(this, RepeatActivity::class.java)
            intent.putExtra("CURRENT_REPEAT", binding.valueRepeat.text.toString())
            repeatPickerLauncher.launch(intent)
        }

        binding.cardVibrate.setOnClickListener {
            if (binding.valueVibrate.text.toString() == "Aktif") {
                binding.valueVibrate.text = "Nonaktif"
            } else {
                binding.valueVibrate.text = "Aktif"
            }
        }
    }

    private fun loadAlarmData(alarmId: String) {
        val alarm = AlarmStorageManager.getAlarmById(this, alarmId)

        if (alarm != null) {
            // Set time
            binding.firsNum.value = alarm.hour
            binding.secNum.value = alarm.minute

            // Set AM/PM
            selectAmPm(!alarm.isPM)

            // Set label
            binding.labeltxt.text = alarm.label

            // Set ringtone
            binding.valueRingtone.text = alarm.ringtone

            // Set repeat
            binding.valueRepeat.text = alarm.repeat

            // Set vibrate
            binding.valueVibrate.text = if (alarm.vibrate) "Aktif" else "Nonaktif"

            // Set volume
            binding.seekBar11.progress = alarm.volume
        }
    }

    private fun saveAlarm() {
        val hour = binding.firsNum.value
        val minute = binding.secNum.value
        val label = binding.labeltxt.text.toString()
        val ringtone = binding.valueRingtone.text.toString()
        val repeat = binding.valueRepeat.text.toString()
        val vibrate = binding.valueVibrate.text.toString() == "Aktif"
        val volume = binding.seekBar11.progress

        val alarm = Alarm(
            id = currentAlarmId ?: System.currentTimeMillis().toString(),
            hour = hour,
            minute = minute,
            isPM = !isAmSelected,
            label = label,
            ringtone = ringtone,
            repeat = repeat,
            vibrate = vibrate,
            volume = volume,
            isEnabled = true
        )

        val success = AlarmStorageManager.saveAlarm(this, alarm)

        if (success) {
            // Schedule the alarm
            AlarmScheduler.scheduleAlarm(this, alarm)

            Toast.makeText(this, "Alarm tersimpan dan dijadwalkan", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Gagal menyimpan alarm", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteAlarm() {
        currentAlarmId?.let { id ->
            val success = AlarmStorageManager.deleteAlarm(this, id)

            if (success) {
                // Cancel the scheduled alarm
                AlarmScheduler.cancelAlarm(this, id)

                Toast.makeText(this, "Alarm dihapus", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Gagal menghapus alarm", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectAmPm(isAm: Boolean) {
        isAmSelected = isAm

        if (isAm) {
            // AM selected - blue background, white text
            binding.btnAm.setBackgroundResource(R.drawable.rounder_bluebg)
            binding.btnAm.setTextColor(getColor(android.R.color.white))

            // PM unselected - white background, black text
            binding.btnPm.setBackgroundResource(R.drawable.rounded_white_bg)
            binding.btnPm.setTextColor(getColor(android.R.color.black))
        } else {
            // PM selected - blue background, white text
            binding.btnPm.setBackgroundResource(R.drawable.rounder_bluebg)
            binding.btnPm.setTextColor(getColor(android.R.color.white))

            // AM unselected - white background, black text
            binding.btnAm.setBackgroundResource(R.drawable.rounded_white_bg)
            binding.btnAm.setTextColor(getColor(android.R.color.black))
        }
    }

    private fun showLabelDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_label, null)
        val editText = dialogView.findViewById<EditText>(R.id.editLabel)

        editText.setText(binding.labeltxt.text)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDialogCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDialogSet).setOnClickListener {
            val newLabel = editText.text.toString()
            binding.labeltxt.text = newLabel
            dialog.dismiss()
        }

        dialog.show()
    }
}