package com.example.studentlife

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.*

class DetailPengeluaranActivity : AppCompatActivity() {

    private lateinit var tvDetailNama: TextView
    private lateinit var tvDetailJumlah: TextView
    private lateinit var btnSimpan: Button
    private lateinit var btnBack: ImageView
    private lateinit var ivImagePengeluaran: ImageView
    private var nama: String? = null
    private var jumlah: Int = 0
    private var imageUri: Uri? = null
    private var position: Int = -1
    private var isEdit: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_pengeluaran)

        // Ambil data dari intent
        val intent = intent
        nama = intent.getStringExtra("namaPengeluaran")
        jumlah = intent.getIntExtra("jumlahPengeluaran", 0)
        val imageUriString = intent.getStringExtra("imageUri")
        position = intent.getIntExtra("position", -1)
        isEdit = intent.getBooleanExtra("isEdit", false)

        // Inisialisasi komponen UI
        tvDetailNama = findViewById(R.id.tvDetailNama)
        tvDetailJumlah = findViewById(R.id.tvDetailJumlah)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnBack = findViewById(R.id.iconBack)
        ivImagePengeluaran = findViewById(R.id.imagePengeluaran)

        // Format jumlah ke format Rupiah
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val jumlahFormatted = formatRupiah.format(jumlah)

        // Set data ke tampilan
        tvDetailNama.text = nama
        tvDetailJumlah.text = jumlahFormatted

        // Menampilkan gambar jika ada
        if (!imageUriString.isNullOrEmpty()) {
            imageUri = Uri.parse(imageUriString)
            ivImagePengeluaran.setImageURI(imageUri)
            ivImagePengeluaran.visibility = ImageView.VISIBLE
        } else {
            ivImagePengeluaran.visibility = ImageView.GONE
        }

        // Tombol Simpan → Kirim data kembali ke activity sebelumnya
        btnSimpan.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("namaPengeluaran", nama)
            resultIntent.putExtra("jumlahPengeluaran", jumlah)
            resultIntent.putExtra("imageUri", imageUri?.toString() ?: "")

            // Tambahkan position jika ini adalah edit
            if (position != -1) {
                resultIntent.putExtra("position", position)
            }

            setResult(RESULT_OK, resultIntent)
            finish()
        }

        // Tombol back
        btnBack.setOnClickListener {
            finish()
        }
    }
}
