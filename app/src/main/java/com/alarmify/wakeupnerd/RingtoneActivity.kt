package com.alarmify.wakeupnerd

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.alarmify.wakeupnerd.databinding.ActivityRingtoneBinding
import com.alarmify.wakeupnerd.data.Ringtone
import com.alarmify.wakeupnerd.data.RingtoneAdapter
import java.io.File
import java.io.FileOutputStream

class RingtoneActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRingtoneBinding
    private lateinit var myRingtonesAdapter: RingtoneAdapter
    private val myRingtonesList = mutableListOf<Ringtone>()

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleSelectedFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRingtoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerViews()
        loadRingtones()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.fabAddRingtone.setOnClickListener {
            openFilePicker()
        }
    }

    private fun setupRecyclerViews() {
        myRingtonesAdapter = RingtoneAdapter(myRingtonesList) { position ->
            handleApplyRingtone(myRingtonesList[position])
        }

        binding.recyclerViewMyRingtones.apply {
            layoutManager = LinearLayoutManager(this@RingtoneActivity)
            adapter = myRingtonesAdapter
        }
    }

    private fun loadRingtones() {
        myRingtonesList.clear()

        // Load custom ringtones from storage
        val ringtonesDir = File(getExternalFilesDir(null), "Ringtones")
        if (ringtonesDir.exists()) {
            ringtonesDir.listFiles()?.forEach { file ->
                if (file.isFile && file.extension in listOf("mp3", "m4a", "wav", "ogg")) {
                    val duration = getAudioDurationFromFile(file)
                    myRingtonesList.add(Ringtone(file.nameWithoutExtension, duration))
                }
            }
        }

        myRingtonesAdapter.notifyDataSetChanged()
    }

    private fun handleApplyRingtone(ringtone: Ringtone) {
        // Pass the selected ringtone back to AddAlarmActivity
        val resultIntent = Intent().apply {
            putExtra("SELECTED_RINGTONE", ringtone.name)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun openFilePicker() {
        filePickerLauncher.launch("audio/*")
    }

    private fun handleSelectedFile(uri: Uri) {
        try {
            // Get the ringtones directory
            val ringtonesDir = File(getExternalFilesDir(null), "Ringtones")
            if (!ringtonesDir.exists()) {
                ringtonesDir.mkdirs()
            }

            // Get file name from URI
            val fileName = getFileNameFromUri(uri) ?: "ringtone_${System.currentTimeMillis()}.mp3"
            val destinationFile = File(ringtonesDir, fileName)

            // Copy file to app directory
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Get audio duration using MediaMetadataRetriever
            val duration = getAudioDuration(uri)

            // Add to my ringtones list
            myRingtonesList.add(0, Ringtone(fileName.substringBeforeLast("."), duration))
            myRingtonesAdapter.notifyItemInserted(0)

            Toast.makeText(this, "Ringtone added successfully", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Failed to add ringtone: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }

    private fun getAudioDuration(uri: Uri): String {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(this, uri)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            retriever.release()

            val seconds = (durationMs / 1000) % 60
            val minutes = (durationMs / (1000 * 60)) % 60
            String.format("%02d:%02d", minutes, seconds)
        } catch (e: Exception) {
            "00:00"
        }
    }

    private fun getAudioDurationFromFile(file: File): String {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            retriever.release()

            val seconds = (durationMs / 1000) % 60
            val minutes = (durationMs / (1000 * 60)) % 60
            String.format("%02d:%02d", minutes, seconds)
        } catch (e: Exception) {
            "00:00"
        }
    }
}
