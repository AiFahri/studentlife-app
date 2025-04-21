package com.example.studentlife

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.util.Log
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.example.studentlife.MenuActivity
import com.example.studentlife.R

class MainActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: AppCompatButton
    private lateinit var cbRemember: CheckBox
    private lateinit var ivPasswordToggle: ImageView
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvRegister: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        cbRemember = findViewById(R.id.cbRemember)
        ivPasswordToggle = findViewById(R.id.passwordToggle)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvRegister = findViewById(R.id.tvCreateAccount)


        btnLogin.setOnClickListener {
            onLoginClick()
        }
        ivPasswordToggle.setOnClickListener {
            togglePasswordVisibility()
        }
        tvForgotPassword.setOnClickListener {
            showToast("Fitur Lupa Password Belum Tersedia")
        }

        tvRegister.setOnClickListener {
            showToast("Fitur Daftar Belum Tersedia")
        }
    }

    private fun onLoginClick() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this@MainActivity, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
        } else if (email == "studentlife" && password == "12345678") {
            Toast.makeText(this@MainActivity, "Berhasil Log In", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@MainActivity, MenuActivity::class.java)
            intent.putExtra("userEmail", email)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this@MainActivity, "Login Gagal. Email atau Password salah", Toast.LENGTH_SHORT).show()
        }
    }
    private fun togglePasswordVisibility() {
        if (etPassword.inputType == android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT
        } else {
            etPassword.inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            ivPasswordToggle.setImageResource(R.drawable.ic_visibility)
        }
        etPassword.setSelection(etPassword.text.length)
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}