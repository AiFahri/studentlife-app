package com.example.studentlife

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap // Import Bitmap
import android.graphics.BitmapFactory // Import BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore // Import MediaStore
import android.util.Base64 // Import Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts // Import ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout // Import ConstraintLayout jika digunakan
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.studentlife.model.TempatBelajar // Import model TempatBelajar Anda
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.max

class TambahTempatBelajarActivity : AppCompatActivity() {
    private lateinit var iconUpload: ImageView // Untuk menampilkan preview gambar yang diupload
    private lateinit var etNamaTempat: EditText
    private lateinit var etAlamat: EditText
    private lateinit var btnSimpanTempat: Button // Menggantikan button_pratinjau
    private lateinit var tvUploadHint: TextView // Teks "Unggah file disini" jika ada
    private lateinit var uploadContainer: ConstraintLayout


    private var selectedImageUri: Uri? = null
    private var currentBitmap: Bitmap? = null // Untuk menyimpan bitmap yang akan di-encode

    private var isEditMode = false
    private var existingTempatId: String? = null
    private var originalGambarBase64: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val TAG = "TambahTempatBelajar"

    // Launcher untuk memilih gambar dari galeri
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && uri.toString().isNotEmpty()) {
            try {
                selectedImageUri = uri
                currentBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                iconUpload.setImageBitmap(currentBitmap)
                iconUpload.visibility = View.VISIBLE
                tvUploadHint.visibility = View.GONE // Sembunyikan hint jika gambar terpilih
                Log.d(TAG, "Gambar dipilih: $uri")
            } catch (e: Exception) {
                Log.e(TAG, "Error memproses URI gambar: $uri", e)
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
                resetImagePreviewToDefault()
            }
        } else {
            Log.d(TAG, "Tidak ada gambar yang dipilih.")
            if (!isEditMode || originalGambarBase64.isNullOrEmpty()) {
                resetImagePreviewToDefault()
                currentBitmap = null
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_tempat_belajar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        iconUpload = findViewById(R.id.icon_upload) // ImageView untuk preview
        uploadContainer = findViewById(R.id.upload_foto_container) // Container yang bisa diklik
        tvUploadHint = findViewById(R.id.tvUploadUnderline) // Teks "disini" atau "Unggah file"
        etNamaTempat = findViewById(R.id.etNamaTempat)
        etAlamat = findViewById(R.id.etAlamat)
        btnSimpanTempat = findViewById(R.id.button_pratinjau) // Ganti nama variabel atau ID jika perlu
        val btnBack = findViewById<FrameLayout>(R.id.btnBack)
        val tvTitle = findViewById<TextView>(R.id.tambah_temp)


        existingTempatId = intent.getStringExtra("tempat_id")
        if (existingTempatId != null) {
            isEditMode = true
            tvTitle.text = "Edit Tempat Belajar" // Sesuaikan string jika perlu
            btnSimpanTempat.text = "Simpan Perubahan"
            Log.d(TAG, "Mode Edit. Tempat ID: $existingTempatId")
            loadExistingTempatData(existingTempatId!!)
        } else {
            isEditMode = false
            tvTitle.text = "Tambah Tempat Belajar" // Sesuaikan string jika perlu
            btnSimpanTempat.text = "Simpan" // Sesuaikan string jika perlu
            resetImagePreviewToDefault()
            Log.d(TAG, "Mode Tambah Baru.")
        }

        btnBack.setOnClickListener {
            finish()
        }

        uploadContainer.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        iconUpload.setOnClickListener { // Izinkan klik pada gambar untuk memilih ulang
            pickImageLauncher.launch("image/*")
        }

        btnSimpanTempat.setOnClickListener {
            saveOrUpdateTempatBelajar()
        }
    }

    private fun resetImagePreviewToDefault() {
        iconUpload.setImageResource(R.drawable.ic_upload) // Pastikan drawable ini ada
        iconUpload.visibility = View.VISIBLE // Atau sesuai desain awal Anda
        tvUploadHint.visibility = View.VISIBLE // Tampilkan kembali hint
        currentBitmap = null
        selectedImageUri = null
    }

    private fun loadExistingTempatData(tempatId: String) {
        val tempatRef = database.reference.child("tempat_belajar").child(tempatId) // Sesuaikan path jika perlu
        tempatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempat = snapshot.getValue(TempatBelajar::class.java)
                if (tempat != null) {
                    etNamaTempat.setText(tempat.nama)
                    etAlamat.setText(tempat.alamat)
                    originalGambarBase64 = tempat.gambarBase64
                    if (!originalGambarBase64.isNullOrEmpty()) {
                        try {
                            currentBitmap = decodeBase64ToBitmap(originalGambarBase64!!)
                            if (currentBitmap != null) {
                                iconUpload.setImageBitmap(currentBitmap)
                                tvUploadHint.visibility = View.GONE
                            } else { resetImagePreviewToDefault() }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error decode Base64 lama", e)
                            resetImagePreviewToDefault()
                        }
                    } else {
                        resetImagePreviewToDefault()
                    }
                } else {
                    Toast.makeText(this@TambahTempatBelajarActivity, "Gagal memuat data tempat.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TambahTempatBelajarActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun encodeBitmapToBase64(bitmap: Bitmap?): String? {
        if (bitmap == null) return null
        val outputStream = ByteArrayOutputStream()
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 480, (bitmap.height.toFloat() / bitmap.width.toFloat() * 480).toInt(), true)
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal decode Base64 ke Bitmap", e); null
        }
    }

    private fun saveOrUpdateTempatBelajar() {
        val namaTempat = etNamaTempat.text.toString().trim()
        val alamatTempat = etAlamat.text.toString().trim()

        if (namaTempat.isEmpty() || alamatTempat.isEmpty()) {
            Toast.makeText(this, "Nama tempat dan alamat wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User belum login.", Toast.LENGTH_LONG).show()
            return
        }
        val userId = currentUser.uid

        val gambarBase64ToSave = encodeBitmapToBase64(currentBitmap) ?: if (isEditMode) originalGambarBase64 else ""

        val tempatIdToSave = if (isEditMode) existingTempatId!! else database.reference.child("tempat_belajar").push().key!!

        val tempatBelajar = TempatBelajar(
            id = tempatIdToSave,
            userId = userId,
            nama = namaTempat,
            alamat = alamatTempat,
            gambarBase64 = gambarBase64ToSave
        )

        Log.d(TAG, "Menyimpan tempat belajar: $tempatBelajar")
        database.reference.child("tempat_belajar").child(tempatIdToSave).setValue(tempatBelajar)
            .addOnSuccessListener {
                val message = if (isEditMode) "Tempat belajar berhasil diperbarui" else "Tempat belajar berhasil disimpan"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "$message. ID: $tempatIdToSave")
                setResult(Activity.RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                val message = if (isEditMode) "Gagal memperbarui tempat" else "Gagal menyimpan tempat"
                Toast.makeText(this, "$message: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, message, e)
            }
    }
}