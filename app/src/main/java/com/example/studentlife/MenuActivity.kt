package com.example.studentlife

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.menu)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Log.d("MenuActivity", "MenuActivity is created")
        val btnJadwal = findViewById<ImageButton>(R.id.btnJadwal)
        val btnTugas = findViewById<ImageButton>(R.id.btnTugas)
        val btnSinau = findViewById<ImageButton>(R.id.btnSinau)
        val btnDuit = findViewById<ImageButton>(R.id.btnDuit)
        val username = intent.getStringExtra("userEmail")
        val tvGreeting = findViewById<TextView>(R.id.tvGreetings)
        tvGreeting.text = "Hai, $username 👋"
        val tvGreetingBox = findViewById<TextView>(R.id.tvGreeting)
        tvGreetingBox.text = "Siap untuk produktif?"
        val tvGreetingSub = findViewById<TextView>(R.id.tvSubtext)
        tvGreetingSub.text = "Coba semua fitur di bawah ini, yuk!"
        val notificationIcon = findViewById<ImageView>(R.id.notificationIcon)
        btnJadwal.setOnClickListener {
            Log.d("MenuActivity", "Navigating to ListJadwalActivity")
            val intent = Intent(this@MenuActivity, ListJadwalActivity::class.java)
            intent.putExtra("userEmail", username)
            startActivity(intent)
        }

        btnTugas.setOnClickListener {
            val intent = Intent(this@MenuActivity, TaskList::class.java)
            intent.putExtra("userEmail", username)
            startActivity(intent)
        }

        btnSinau.setOnClickListener {
            val intent = Intent(this@MenuActivity, ListTempatBelajarActivity::class.java)
            intent.putExtra("userEmail", username)
            startActivity(intent)
        }

        btnDuit.setOnClickListener {
            val intent = Intent(this@MenuActivity, DaftarPengeluaranActivity::class.java)
            intent.putExtra("userEmail", username)
            startActivity(intent)
        }
        notificationIcon.setOnClickListener {
            Toast.makeText(this, "Fitur notifikasi belum tersedia!", Toast.LENGTH_SHORT).show()
        }
    }
}