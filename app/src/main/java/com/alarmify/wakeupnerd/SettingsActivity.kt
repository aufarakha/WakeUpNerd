package com.alarmify.wakeupnerd

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import com.alarmify.wakeupnerd.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences

    // Settings keys
    companion object {
        const val PREFS_NAME = "WakeUpNerdSettings"
        const val KEY_DIFFICULTY = "math_difficulty"
        const val KEY_QUESTION_COUNT = "question_count"

        // Default values
        const val DEFAULT_DIFFICULTY = "Medium"
        const val DEFAULT_QUESTION_COUNT = 3
        const val MIN_QUESTIONS = 1
        const val MAX_QUESTIONS = 10
    }

    private var currentQuestionCount = DEFAULT_QUESTION_COUNT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Load saved settings
        loadSettings()

        // Setup click listeners
        setupClickListeners()
    }

    private fun loadSettings() {
        // Load difficulty
        val savedDifficulty = sharedPreferences.getString(KEY_DIFFICULTY, DEFAULT_DIFFICULTY)
        setDifficultySelection(savedDifficulty ?: DEFAULT_DIFFICULTY)

        // Load question count
        currentQuestionCount = sharedPreferences.getInt(KEY_QUESTION_COUNT, DEFAULT_QUESTION_COUNT)
        updateQuestionCountDisplay()
    }

    private fun setDifficultySelection(difficulty: String) {
        when (difficulty) {
            "Easy" -> binding.toggleGroupDifficulty.check(R.id.btnEasy)
            "Medium" -> binding.toggleGroupDifficulty.check(R.id.btnMedium)
            "Hard" -> binding.toggleGroupDifficulty.check(R.id.btnHard)
            else -> binding.toggleGroupDifficulty.check(R.id.btnMedium)
        }
    }

    private fun getSelectedDifficulty(): String {
        return when (binding.toggleGroupDifficulty.checkedButtonId) {
            R.id.btnEasy -> "Easy"
            R.id.btnMedium -> "Medium"
            R.id.btnHard -> "Hard"
            else -> DEFAULT_DIFFICULTY
        }
    }

    private fun setupClickListeners() {
        binding.backHome.setOnClickListener {
            finish()
        }

        // Decrease question count
        binding.imageButton12.setOnClickListener {
            if (currentQuestionCount > MIN_QUESTIONS) {
                currentQuestionCount--
                updateQuestionCountDisplay()
            } else {
                Toast.makeText(this, "Minimum $MIN_QUESTIONS question", Toast.LENGTH_SHORT).show()
            }
        }

        // Increase question count
        binding.imageButton9.setOnClickListener {
            if (currentQuestionCount < MAX_QUESTIONS) {
                currentQuestionCount++
                updateQuestionCountDisplay()
            } else {
                Toast.makeText(this, "Maximum $MAX_QUESTIONS questions", Toast.LENGTH_SHORT).show()
            }
        }

        // Save settings
        binding.btnSimpan.setOnClickListener {
            saveSettings()
        }
    }

    private fun updateQuestionCountDisplay() {
        binding.tvQuestionCount.text = currentQuestionCount.toString()
    }

    private fun saveSettings() {
        val difficulty = getSelectedDifficulty()

        // Save to SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString(KEY_DIFFICULTY, difficulty)
        editor.putInt(KEY_QUESTION_COUNT, currentQuestionCount)
        editor.apply()

        Toast.makeText(
            this,
            "Settings saved!\nDifficulty: $difficulty\nQuestions: $currentQuestionCount",
            Toast.LENGTH_SHORT
        ).show()
    }
}