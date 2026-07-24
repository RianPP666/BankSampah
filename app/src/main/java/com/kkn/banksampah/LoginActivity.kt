package com.kkn.banksampah

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.kkn.banksampah.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // Default admin credentials
    private val ADMIN_USERNAME = "admin"
    private val ADMIN_PASSWORD = "admin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if already logged in
        val prefs = getSharedPreferences("bank_sampah_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("is_logged_in", false)) {
            navigateToMain()
            return
        }

        setupLogin()
    }

    private fun setupLogin() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            binding.tvError.visibility = View.GONE

            if (username.isEmpty()) {
                binding.etUsername.error = "Username tidak boleh kosong"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = "Password tidak boleh kosong"
                return@setOnClickListener
            }

            if (username == ADMIN_USERNAME && password == ADMIN_PASSWORD) {
                // Save login state
                val prefs = getSharedPreferences("bank_sampah_prefs", MODE_PRIVATE)
                prefs.edit().putBoolean("is_logged_in", true).apply()
                navigateToMain()
            } else {
                binding.tvError.text = "Username atau password salah!"
                binding.tvError.visibility = View.VISIBLE
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
