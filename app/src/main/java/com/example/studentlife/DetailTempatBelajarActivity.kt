package com.example.studentlife

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.max

class DetailTempatBelajarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_detail_tempat_belajar)

        val imagePreview = findViewById<ImageView>(R.id.imagePreview)
        val imageUriString = intent.getStringExtra("imageUri")

        if (imageUriString != null) {
            imagePreview.visibility = View.VISIBLE
            try {
                val imageUri = Uri.parse(imageUriString)
                val inputStream = contentResolver.openInputStream(imageUri)
                val drawable = Drawable.createFromStream(inputStream, imageUri.toString())
                imagePreview.setImageDrawable(drawable)
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Gagal menampilkan gambar.", Toast.LENGTH_SHORT).show()
            }
        } else {
            imagePreview.visibility = View.GONE
        }

        // Ambil data dari intent
        val namaTempat = intent.getStringExtra("namaTempat")
        val alamatTempat = intent.getStringExtra("alamatTempat")
        val itemPosition = intent.getIntExtra("itemPosition", -1)

        // Inisialisasi elemen UI
        val tvNamaTempat = findViewById<TextView>(R.id.tvNamaTempat)
        val tvAlamat = findViewById<TextView>(R.id.tvAlamat)
        val btnBack = findViewById<FrameLayout>(R.id.btnBack)
        val btnSimpan = findViewById<Button>(R.id.button_simpan)

        // Tampilkan data yang diterima
        tvNamaTempat.text = namaTempat
        tvAlamat.text = alamatTempat

        // Fungsi tombol "Simpan" untuk mengembalikan data ke ListTempatBelajarActivity
        btnSimpan.setOnClickListener { v: View? ->
            val resultIntent = Intent()
            resultIntent.putExtra("namaTempat", namaTempat)
            resultIntent.putExtra("alamatTempat", alamatTempat)
            resultIntent.putExtra("itemPosition", itemPosition)

            if (imageUriString != null) {
                resultIntent.putExtra("imageUri", imageUriString)
            }
            setResult(RESULT_OK, resultIntent)
            Toast.makeText(this, "Data Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Fungsi tombol back
        btnBack.setOnClickListener { v: View? -> finish() }

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                max(v.paddingLeft.toDouble(), systemBars.left.toDouble()).toInt(),
                max(v.paddingTop.toDouble(), systemBars.top.toDouble()).toInt(),
                max(v.paddingRight.toDouble(), systemBars.right.toDouble()).toInt(),
                max(v.paddingBottom.toDouble(), systemBars.bottom.toDouble()).toInt()
            )
            insets
        }
    }
}