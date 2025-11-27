package com.alarmify.wakeupnerd

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alarmify.wakeupnerd.databinding.ActivityAlarmPlayBinding
import com.alarmify.wakeupnerd.data.SettingsManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class AlarmPlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmPlayBinding
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var vibrator: Vibrator

    private var totalQuestions = 3
    private var currentQuestionIndex = 0
    private var wrongAnswers = 0
    private var currentAnswer = 0
    private var difficulty = "Medium"

    // Alarm data from intent
    private var alarmRingtone = "Alarm clock"
    private var alarmVibrate = true
    private var alarmVolume = 50

    private val questions = mutableListOf<MathQuestion>()
    private var waitTimer: CountDownTimer? = null

    data class MathQuestion(
        val num1: Int,
        val num2: Int,
        val operator: String,
        val answer: Int,
        val displayText: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAlarmPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep screen on and show over lockscreen
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Lock the task to prevent user from leaving
        startLockTask()

        // Get alarm data from intent
        alarmRingtone = intent.getStringExtra("ALARM_RINGTONE") ?: "Alarm clock"
        alarmVibrate = intent.getBooleanExtra("ALARM_VIBRATE", true)
        alarmVolume = intent.getIntExtra("ALARM_VOLUME", 50)

        // Initialize vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Load settings
        loadSettings()

        // Generate questions
        generateQuestions()

        // Start alarm sound and vibration
        startAlarmSound()
        if (alarmVibrate) {
            startVibration()
        }

        // Display current time
        updateTimeDisplay()

        // Show first question
        showQuestion()

        // Setup listeners
        setupListeners()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Prevent user from leaving by bringing activity back to front
        val intent = Intent(this, AlarmPlayActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
    }

    private fun loadSettings() {
        difficulty = SettingsManager.getDifficulty(this)
        totalQuestions = SettingsManager.getQuestionCount(this)
    }

    private fun generateQuestions() {
        questions.clear()
        repeat(totalQuestions) {
            questions.add(generateQuestion(difficulty))
        }
    }

    private fun generateQuestion(difficulty: String): MathQuestion {
        return when (difficulty) {
            "Easy" -> generateEasyQuestion()
            "Medium" -> generateMediumQuestion()
            "Hard" -> generateHardQuestion()
            else -> generateMediumQuestion()
        }
    }

    private fun generateEasyQuestion(): MathQuestion {
        val num1 = Random.nextInt(1, 50)
        val num2 = Random.nextInt(1, 50)
        val answer = num1 + num2
        return MathQuestion(
            num1, num2, "+", answer,
            "$num1 + $num2 ="
        )
    }

    private fun generateMediumQuestion(): MathQuestion {
        val num1 = Random.nextInt(10, 100)
        val num2 = Random.nextInt(1, num1) // Ensure positive result
        val answer = num1 - num2
        return MathQuestion(
            num1, num2, "-", answer,
            "$num1 - $num2 ="
        )
    }

    private fun generateHardQuestion(): MathQuestion {
        val num1 = Random.nextInt(5, 20)
        val num2 = Random.nextInt(2, 15)
        val answer = num1 * num2
        return MathQuestion(
            num1, num2, "*", answer,
            "$num1 × $num2 ="
        )
    }

    private fun showQuestion() {
        if (currentQuestionIndex < questions.size) {
            val question = questions[currentQuestionIndex]
            currentAnswer = question.answer

            binding.problemTextView.text = question.displayText
            binding.progressTextView.text = "${currentQuestionIndex + 1} of ${questions.size} questions"
            binding.progressBar4.max = questions.size
            binding.progressBar4.progress = currentQuestionIndex + 1

            // Reset progress bar color to default (gray)
            binding.progressBar4.progressTintList =
                android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#9E9E9E"))

            binding.answerEditText.text.clear()
            binding.answerEditText.requestFocus()
        }
    }

    private fun setupListeners() {
        binding.stopButton.setOnClickListener {
            checkAnswer()
        }

        binding.answerEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkAnswer()
                true
            } else {
                false
            }
        }

        binding.speakerIcon.setOnClickListener {
            // Toggle sound
            if (::mediaPlayer.isInitialized) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    Toast.makeText(this, "Sound paused", Toast.LENGTH_SHORT).show()
                } else {
                    mediaPlayer.start()
                    Toast.makeText(this, "Sound resumed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkAnswer() {
        val userAnswer = binding.answerEditText.text.toString().toIntOrNull()

        if (userAnswer == null) {
            Toast.makeText(this, "Please enter an answer", Toast.LENGTH_SHORT).show()
            return
        }

        if (userAnswer == currentAnswer) {
            // Correct answer - change progress bar to green
            binding.progressBar4.progressTintList =
                android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50"))

            Toast.makeText(this, "Correct! ✓", Toast.LENGTH_SHORT).show()

            // Move to next question
            currentQuestionIndex++

            if (currentQuestionIndex >= questions.size) {
                // All questions answered correctly
                dismissAlarm()
            } else {
                // Show next question after a short delay
                binding.answerEditText.postDelayed({
                    showQuestion()
                }, 500)
            }
        } else {
            // Wrong answer - change progress bar to red
            binding.progressBar4.progressTintList =
                android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F44336"))

            // Always increment wrong answer counter
            wrongAnswers++

            Toast.makeText(this, "Wrong! The answer is $currentAnswer", Toast.LENGTH_SHORT).show()

            // Check if too many wrong answers
            if (wrongAnswers > totalQuestions) {
                // Too many wrong answers - show dialog
                showFailureDialog()
            } else {
                // Clear answer and let user try again
                binding.answerEditText.text.clear()
            }
        }
    }

    private fun showFailureDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Too Many Wrong Answers")
        builder.setMessage("You've made too many mistakes. Choose an option:")
        builder.setCancelable(false)

        builder.setPositiveButton("Answer ${totalQuestions} More Questions") { dialog, _ ->
            // Generate new questions
            wrongAnswers = 0
            currentQuestionIndex = 0
            generateQuestions()
            showQuestion()
            dialog.dismiss()
        }

        builder.setNegativeButton("Wait 5 Minutes") { dialog, _ ->
            startWaitTimer()
            dialog.dismiss()
        }

        builder.show()
    }

    private fun startWaitTimer() {
        // Disable input
        binding.answerEditText.isEnabled = false
        binding.stopButton.isEnabled = false

        // 5 minutes = 300,000 milliseconds
        waitTimer = object : CountDownTimer(300000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.problemTextView.text = "Wait: ${String.format("%02d:%02d", minutes, seconds)}"
            }

            override fun onFinish() {
                dismissAlarm()
            }
        }.start()
    }

    private fun dismissAlarm() {
        Toast.makeText(this, "Alarm dismissed!", Toast.LENGTH_SHORT).show()
        stopAlarmSound()
        if (alarmVibrate) {
            stopVibration()
        }
        waitTimer?.cancel()

        // Stop lock task to allow user to leave
        try {
            stopLockTask()
        } catch (e: Exception) {
            // Lock task not active
        }

        finish()
    }

    private fun startAlarmSound() {
        try {
            // Try to find custom ringtone first
            val ringtonesDir = java.io.File(getExternalFilesDir(null), "Ringtones")
            var ringtoneUri: Uri? = null

            if (ringtonesDir.exists()) {
                // Look for the ringtone file
                ringtonesDir.listFiles()?.forEach { file ->
                    if (file.nameWithoutExtension == alarmRingtone) {
                        ringtoneUri = Uri.fromFile(file)
                        return@forEach
                    }
                }
            }

            // If custom ringtone not found, use default alarm sound
            if (ringtoneUri == null) {
                ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmPlayActivity, ringtoneUri!!)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true

                // Set volume based on alarm settings
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                val targetVolume = ((alarmVolume / 100f) * maxVolume).toInt()
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, targetVolume, 0)

                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error playing alarm sound", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopAlarmSound() {
        if (::mediaPlayer.isInitialized) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        }
    }

    private fun startVibration() {
        if (vibrator.hasVibrator()) {
            val pattern = longArrayOf(0, 1000, 500, 1000, 500) // Vibrate pattern
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(pattern, 0)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, 0)
            }
        }
    }

    private fun stopVibration() {
        vibrator.cancel()
    }

    private fun updateTimeDisplay() {
        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("hh : mm", Locale.getDefault())
        val amPmFormat = SimpleDateFormat("a", Locale.getDefault())

        binding.timeTextView1.text = timeFormat.format(calendar.time)
        binding.timeTextView2.text = amPmFormat.format(calendar.time)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmSound()
        if (alarmVibrate) {
            stopVibration()
        }
        waitTimer?.cancel()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Prevent back button from dismissing alarm
        Toast.makeText(this, "Solve the math problems to dismiss alarm", Toast.LENGTH_SHORT).show()
    }
}