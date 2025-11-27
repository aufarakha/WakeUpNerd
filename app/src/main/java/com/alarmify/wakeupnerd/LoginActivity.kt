package com.alarmify.wakeupnerd

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Toast
import com.alarmify.wakeupnerd.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        binding.backLogin.setOnClickListener {
            finish()
        }

        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.tosignUp.setOnClickListener {
            val intent = Intent(this, RegisterFormActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser() {
        val email = binding.Email.text.toString().trim()
        val password = binding.Password.text.toString().trim()

        // Validation
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

        // Disable button to prevent double click
        binding.btnLogin.isEnabled = false

        // Sign in with Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, check if email is verified
                    val user = auth.currentUser

                    if (user != null && user.isEmailVerified) {
                        // Email is verified, proceed to home
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        // Email not verified
                        binding.btnLogin.isEnabled = true
                        showEmailNotVerifiedDialog(user)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun showEmailNotVerifiedDialog(user: com.google.firebase.auth.FirebaseUser?) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Email Not Verified")
        builder.setMessage("Your email is not verified yet. Please check your email inbox for the verification link.\n\nWould you like us to resend the verification email?")

        builder.setPositiveButton("Resend Email") { dialog, _ ->
            user?.sendEmailVerification()
                ?.addOnCompleteListener { emailTask ->
                    if (emailTask.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Verification email sent! Please check your inbox.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Failed to send email: ${emailTask.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            dialog.dismiss()
            // Sign out user
            auth.signOut()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
            // Sign out user
            auth.signOut()
        }

        builder.setCancelable(false)
        builder.show()
    }

    override fun onStart() {
        super.onStart()
        // Check if user is already signed in and email is verified
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            // User is signed in and verified, go to home
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}