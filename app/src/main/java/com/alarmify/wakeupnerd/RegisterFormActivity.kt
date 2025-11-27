package com.alarmify.wakeupnerd

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alarmify.wakeupnerd.databinding.ActivityRegisterFormBinding
import android.content.Intent
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterFormBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        binding.backSignIn.setOnClickListener{
            finish()
        }

        binding.toLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val username = binding.username.text.toString().trim()
        val email = binding.Email.text.toString().trim()
        val password = binding.Password.text.toString().trim()

        // Validation
        if (username.isEmpty()) {
            binding.username.error = "Username required"
            binding.username.requestFocus()
            return
        }

        if (email.isEmpty()) {
            binding.Email.error = "Email required"
            binding.Email.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.Email.error = "Please enter valid email"
            binding.Email.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.Password.error = "Password required"
            binding.Password.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.Password.error = "Password must be at least 6 characters"
            binding.Password.requestFocus()
            return
        }

        // Disable button to prevent double click
        binding.btnSignUp.isEnabled = false

        // Create user with Firebase
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign up success, update user profile with username
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            // Send email verification
                            user.sendEmailVerification()
                                .addOnCompleteListener { emailTask ->
                                    binding.btnSignUp.isEnabled = true
                                    if (emailTask.isSuccessful) {
                                        Toast.makeText(
                                            this,
                                            "Registration successful! Please check your email to verify your account.",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        // Sign out user until they verify email
                                        auth.signOut()

                                        // Go to login
                                        val intent = Intent(this, LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Failed to send verification email: ${emailTask.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                    }
                } else {
                    // If sign up fails, display a message to the user.
                    binding.btnSignUp.isEnabled = true
                    Toast.makeText(
                        this,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}