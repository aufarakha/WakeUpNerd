package com.alarmify.wakeupnerd

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alarmify.wakeupnerd.databinding.ActivityHomeRegisterBinding
import android.content.Intent


class HomeRegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityHomeRegisterBinding.inflate(layoutInflater) // ← inisialisasi
        setContentView(binding.root)

        binding.btntoSignUp.setOnClickListener {
           val  intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        binding.toLogin.setOnClickListener {
            val  intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
}}