package com.alarmify.wakeupnerd

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alarmify.wakeupnerd.databinding.ActivityAddAlarmBinding
import android.content.Intent
import android.graphics.Color
import android.text.InputType
import android.widget.EditText
import android.widget.NumberPicker

class AddAlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddAlarmBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_alarm)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityAddAlarmBinding.inflate(layoutInflater) // ← inisialisasi
        setContentView(binding.root)

        //block edit number picker
        binding.firsNum.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        binding.secNum.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        

        binding.btnCancel.setOnClickListener {
           finish()

        }
        binding.backHome.setOnClickListener {
            finish()

        }


        binding.firsNum.setMinValue(0);
        binding.firsNum.setMaxValue(24);
        binding.secNum.setMinValue(0);
        binding.secNum.setMaxValue(59);

        binding.btnSave.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)

        }

        binding.cardLabel.setOnClickListener {
            showLabelDialog()
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
            if (newLabel.isNotEmpty()) {
                binding.labeltxt.text = newLabel
            }
            dialog.dismiss()
        }

        dialog.show()
    }

}