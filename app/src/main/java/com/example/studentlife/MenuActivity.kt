package com.example.studentlife

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

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
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val notificationIcon = findViewById<ImageView>(R.id.notificationIcon)

        val prefs = getSharedPreferences("StudentLifePrefs", MODE_PRIVATE)
        val username = intent.getStringExtra("userEmail") ?: prefs.getString("userEmail", "User")

        val tvGreeting = findViewById<TextView>(R.id.tvGreetings)
        tvGreeting.text = "Hai, $username 👋"
        val tvGreetingBox = findViewById<TextView>(R.id.tvGreeting)
        tvGreetingBox.text = "Siap untuk produktif?"
        val tvGreetingSub = findViewById<TextView>(R.id.tvSubtext)
        tvGreetingSub.text = "Coba semua fitur di bawah ini, yuk!"

        btnJadwal.setOnClickListener {
            val intent = Intent(this, ListJadwalActivity::class.java)
            intent.putExtra("userEmail", username)
            startActivity(intent)
        }

        btnTugas.setOnClickListener {
            val intent = Intent(this, TaskList::class.java)
            intent.putExtra("userEmail", username)
            startActivity(intent)
        }

        btnSinau.setOnClickListener {
            val intent = Intent(this, ListTempatBelajarActivity::class.java)
            intent.putExtra("userEmail", username)
            startActivity(intent)
        }

        btnDuit.setOnClickListener {
            val intent = Intent(this, DaftarPengeluaranActivity::class.java)
            intent.putExtra("userEmail", username)
            startActivity(intent)
        }

        notificationIcon.setOnClickListener {
            Toast.makeText(this, "Fitur notifikasi belum tersedia!", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            val prefs = getSharedPreferences("StudentLifePrefs", MODE_PRIVATE)
            with(prefs.edit()) {
                clear()
                apply()
            }

            FirebaseAuth.getInstance().signOut()
            GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()

            Toast.makeText(this, "Berhasil logout", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
