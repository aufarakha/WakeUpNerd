package com.alarmify.wakeupnerd

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alarmify.wakeupnerd.databinding.ActivityRepeatBinding

class RepeatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRepeatBinding
    private var selectedOption: String = "Sekali" // Default selection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRepeatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get current selection if passed from AddAlarmActivity
        selectedOption = intent.getStringExtra("CURRENT_REPEAT") ?: "Sekali"
        updateSelectedOption(selectedOption)

        binding.backHome.setOnClickListener {
            finish()
        }

        binding.optionOnce.setOnClickListener {
            selectOption("Sekali")
        }

        binding.optionDaily.setOnClickListener {
            selectOption("Setiap Hari")
        }

        binding.optionMonFri.setOnClickListener {
            selectOption("Senin - Jumat")
        }
    }

    private fun selectOption(option: String) {
        selectedOption = option
        updateSelectedOption(option)

        // Return the selected option to AddAlarmActivity
        val resultIntent = Intent().apply {
            putExtra("SELECTED_REPEAT", option)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun updateSelectedOption(option: String) {
        // Reset all options to white background and black text
        binding.optionOnce.setBackgroundResource(R.drawable.rounded_white_bg)
        binding.optionDaily.setBackgroundResource(R.drawable.rounded_white_bg)
        binding.optionMonFri.setBackgroundResource(R.drawable.rounded_white_bg)

        binding.textOnce.setTextColor(getColor(android.R.color.black))
        binding.textDaily.setTextColor(getColor(android.R.color.black))
        binding.textMonFri.setTextColor(getColor(android.R.color.black))

        // Set selected option to blue background and white text
        when (option) {
            "Sekali" -> {
                binding.optionOnce.setBackgroundResource(R.drawable.rounder_bluebg)
                binding.textOnce.setTextColor(getColor(android.R.color.white))
            }
            "Setiap Hari" -> {
                binding.optionDaily.setBackgroundResource(R.drawable.rounder_bluebg)
                binding.textDaily.setTextColor(getColor(android.R.color.white))
            }
            "Senin - Jumat" -> {
                binding.optionMonFri.setBackgroundResource(R.drawable.rounder_bluebg)
                binding.textMonFri.setTextColor(getColor(android.R.color.white))
            }
        }
    }
}