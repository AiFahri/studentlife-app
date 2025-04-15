package com.example.studentlife

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DetailJadwalActivity : AppCompatActivity() {

    private lateinit var tvMatkul: TextView
    private lateinit var tvHari: TextView
    private lateinit var tvJam: TextView
    private lateinit var btnSimpan: Button
    private lateinit var btnBack: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_jadwal)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_jadwal)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val matkul = intent.getStringExtra("matkul") ?: "Mata Kuliah Kosong"
        val hari = intent.getStringExtra("hari") ?: "-"
        val jam = intent.getStringExtra("jam") ?: "-"

        tvMatkul = findViewById(R.id.tvMatkul)
        tvHari = findViewById(R.id.tvHari)
        tvJam = findViewById(R.id.tvJam)
        btnSimpan = findViewById(R.id.btnSimpan)
        val iconBack = findViewById<ImageView>(R.id.iconBack)
        val tvGreetingSub = findViewById<TextView>(R.id.tvSubtext)
        tvGreetingSub.text = "Check ulang sebelum menyimpan data, ya!"

        tvMatkul.text = matkul
        tvHari.text = hari
        tvJam.text = jam


        iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
        btnSimpan.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("saved_matkul", matkul)
            resultIntent.putExtra("saved_hari", hari)
            resultIntent.putExtra("saved_jam", jam)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
