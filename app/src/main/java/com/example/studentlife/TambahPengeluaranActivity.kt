package com.example.studentlife

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TambahPengeluaranActivity : AppCompatActivity() {

    private val TAG = "TambahPengeluaran"
    private lateinit var editNamaPengeluaran: EditText
    private lateinit var editJumlahPengeluaran: EditText
    private lateinit var btnPratinjau: Button
    private lateinit var ivImagePengeluaran: ImageView
    private lateinit var tvUpload: TextView
    private var imageUri: Uri? = null
    private var isEditMode = false  // Flag untuk memeriksa mode edit atau tambah
    private var position = -1  // Position untuk menyimpan posisi item ketika mode edit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pengeluaran)

        // Inisialisasi komponen UI
        editNamaPengeluaran = findViewById(R.id.editNamaPengeluaran)
        editJumlahPengeluaran = findViewById(R.id.editJumlahPengeluaran)
        btnPratinjau = findViewById(R.id.btnPratinjau)
        ivImagePengeluaran = findViewById(R.id.ivImagePengeluaran)
        tvUpload = findViewById(R.id.tvUpload)

        // Dapatkan data dari Intent
        val intent = intent
        isEditMode = intent.getBooleanExtra("isEdit", false) // Cek apakah dalam mode edit
        if (isEditMode) {
            // Mengisi data jika dalam mode edit
            position = intent.getIntExtra("position", -1)
            val nama = intent.getStringExtra("namaPengeluaran")
            val jumlah = intent.getIntExtra("jumlahPengeluaran", 0)
            val imageUriString = intent.getStringExtra("imageUri")

            editNamaPengeluaran.setText(nama)
            editJumlahPengeluaran.setText(jumlah.toString())

            // Set gambar jika ada
            if (!imageUriString.isNullOrEmpty()) {
                try {
                    imageUri = Uri.parse(imageUriString)
                    // Tambahkan permission untuk akses URI
                    contentResolver.takePersistableUriPermission(imageUri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    ivImagePengeluaran.setImageURI(imageUri)
                    ivImagePengeluaran.visibility = View.VISIBLE
                    tvUpload.visibility = View.GONE
                } catch (e: Exception) {
                    e.printStackTrace()
                    ivImagePengeluaran.visibility = View.GONE
                    tvUpload.visibility = View.VISIBLE
                }
            }
        }

        // Tombol Pilih Gambar
        tvUpload.setOnClickListener { openGallery() }
        findViewById<View>(R.id.form_unggah_foto).setOnClickListener { openGallery() }
        val iconBack = findViewById<ImageView>(R.id.iconBack)

        iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        // Tombol Pratinjau (menggunakan button untuk proses simpan/edit)
        btnPratinjau.setOnClickListener {
            val nama = editNamaPengeluaran.text.toString().trim()
            val jumlahStr = editJumlahPengeluaran.text.toString().trim()

            if (nama.isEmpty() || jumlahStr.isEmpty()) {
                Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val jumlah: Int
            try {
                jumlah = jumlahStr.toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Jumlah harus berupa angka", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kirim data ke DetailPengeluaranActivity
            val detailIntent = Intent(this, DetailPengeluaranActivity::class.java)
            detailIntent.putExtra("position", position)
            detailIntent.putExtra("namaPengeluaran", nama)
            detailIntent.putExtra("jumlahPengeluaran", jumlah)
            detailIntent.putExtra("imageUri", imageUri?.toString() ?: "")
            detailIntent.putExtra("isEdit", isEditMode) // Flag untuk membedakan add vs edit
            startActivityForResult(detailIntent, 102) // REQUEST_CODE untuk hasil dari Detail
        }
    }

    // Membuka galeri untuk memilih gambar
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivityForResult(intent, 1002) // PICK_IMAGE_REQUEST
    }

    // Menangani hasil pemilihan gambar dan hasil dari DetailPengeluaranActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 102 && resultCode == RESULT_OK && data != null) {
            // Teruskan data dari DetailPengeluaranActivity ke DaftarPengeluaranActivity
            setResult(RESULT_OK, data)
            finish()
        } else if (requestCode == 1002 && resultCode == RESULT_OK && data != null) {
            try {
                imageUri = data.data
                // Take persistable URI permission
                contentResolver.takePersistableUriPermission(imageUri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                ivImagePengeluaran.setImageURI(imageUri)
                ivImagePengeluaran.visibility = View.VISIBLE
                tvUpload.visibility = View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Gagal mengakses gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
