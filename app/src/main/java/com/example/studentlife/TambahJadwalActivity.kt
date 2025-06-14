package com.example.studentlife

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.studentlife.model.JadwalModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase 
import java.io.ByteArrayOutputStream

class TambahJadwalActivity : AppCompatActivity() {
    private lateinit var etMatkul: EditText
    private lateinit var spinnerHari: Spinner
    private lateinit var spinnerJam: Spinner
    private lateinit var btnSimpanJadwal: Button 
    // private lateinit var btnBack: ImageButton
    private lateinit var previewImage: ImageView
    private lateinit var uploadClick: TextView
    private var selectedImageUri: Uri? = null
    private var currentBitmap: Bitmap? = null

    private var isEditMode = false
    private var existingJadwalId: String? = null
    private var originalGambarBase64: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && uri.toString().isNotEmpty()) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                selectedImageUri = uri
                currentBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                previewImage.visibility = View.VISIBLE
                previewImage.setImageBitmap(currentBitmap)
                Log.d("TambahJadwal", "Gambar dipilih dan di-preview: $uri")
            } catch (e: Exception) {
                Log.e("TambahJadwal", "Error memproses URI gambar: $uri", e)
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
                setDefaultPreviewImage()
            }
        } else {
            Log.d("TambahJadwal", "Tidak ada gambar yang dipilih")
            if (!isEditMode || originalGambarBase64.isNullOrEmpty()){
                setDefaultPreviewImage()
                currentBitmap = null
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_jadwal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tambah_jadwal)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        etMatkul = findViewById(R.id.etMatkul)
        spinnerHari = findViewById(R.id.spinnerHari)
        spinnerJam = findViewById(R.id.spinnerJam)
        btnSimpanJadwal = findViewById(R.id.btnPratinjau)
        previewImage = findViewById(R.id.previewImage)
        uploadClick = findViewById(R.id.uploadClick)
        uploadClick.paintFlags = uploadClick.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        tvGreeting.text = "Halo, User 👋"

        val iconBack = findViewById<ImageView>(R.id.iconBack)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)

        val hariOptions = listOf("Pilih Hari...", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
        val jamOptions = (0..23).map { String.format("%02d.00", it) }.toMutableList()
        jamOptions.add(0, "Pilih Jam...")


        spinnerHari.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, hariOptions)
        spinnerJam.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, jamOptions)

        existingJadwalId = intent.getStringExtra("jadwal_id")
        if (existingJadwalId != null) {
            isEditMode = true
            tvTitle.text = "Edit Jadwal Kuliah"
            btnSimpanJadwal.text = "Simpan Perubahan"
            Log.d("TambahJadwal", "Mode Edit. Jadwal ID: $existingJadwalId")
            loadExistingData()
        } else {
            isEditMode = false
            tvTitle.text = "Tambah Jadwal Kuliah"
            btnSimpanJadwal.text = "Simpan"
            setDefaultPreviewImage()
            Log.d("TambahJadwal", "Mode Tambah Baru.")
        }

        iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        uploadClick.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        previewImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }


        btnSimpanJadwal.setOnClickListener {
            simpanAtauUpdateJadwal()
        }
    }

    private fun setDefaultPreviewImage() {
        previewImage.setImageResource(R.drawable.ic_upload)
        previewImage.visibility = View.VISIBLE
    }


    private fun loadExistingData() {
        etMatkul.setText(intent.getStringExtra("edit_matkul"))

        val editHari = intent.getStringExtra("edit_hari")
        val hariAdapter = spinnerHari.adapter as ArrayAdapter<String>
        spinnerHari.setSelection(hariAdapter.getPosition(editHari).takeIf { it >= 0 } ?: 0)

        val editJam = intent.getStringExtra("edit_jam")
        val jamAdapter = spinnerJam.adapter as ArrayAdapter<String>
        spinnerJam.setSelection(jamAdapter.getPosition(editJam).takeIf { it >= 0 } ?: 0)

        originalGambarBase64 = intent.getStringExtra("edit_image_base64")
        if (!originalGambarBase64.isNullOrEmpty()) {
            try {
                currentBitmap = decodeBase64ToBitmap(originalGambarBase64!!)
                if (currentBitmap != null) {
                    previewImage.setImageBitmap(currentBitmap)
                    previewImage.visibility = View.VISIBLE
                    Log.d("TambahJadwal", "Gambar lama (Base64) berhasil di-load untuk diedit.")
                } else {
                    Log.w("TambahJadwal", "Gagal decode Base64 gambar lama.")
                    setDefaultPreviewImage()
                }
            } catch (e: Exception) {
                Log.e("TambahJadwal", "Error decode Base64 gambar lama: $originalGambarBase64", e)
                setDefaultPreviewImage()
            }
        } else {
            setDefaultPreviewImage()
        }
    }

    private fun encodeBitmapToBase64(bitmap: Bitmap?): String? {
        if (bitmap == null) return null
        val outputStream = ByteArrayOutputStream()
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, (bitmap.height.toFloat() / bitmap.width.toFloat() * 300).toInt(), true)
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream) 
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            Log.e("TambahJadwal", "Error decode Base64: String tidak valid", e)
            null
        } catch (e: Exception) {
            Log.e("TambahJadwal", "Error umum decode Base64", e)
            null
        }
    }


    private fun simpanAtauUpdateJadwal() {
        val matkul = etMatkul.text.toString().trim()
        val hari = spinnerHari.selectedItem.toString()
        val jam = spinnerJam.selectedItem.toString()

        if (matkul.isEmpty()) {
            Toast.makeText(this, "Nama mata kuliah wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }
        if (hari == "Pilih Hari...") {
            Toast.makeText(this, "Harap pilih hari kuliah", Toast.LENGTH_SHORT).show()
            return
        }
        if (jam == "Pilih Jam...") {
            Toast.makeText(this, "Harap pilih jam kuliah", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User belum login. Silakan login kembali.", Toast.LENGTH_LONG).show()
            return
        }
        val userId = currentUser.uid

        val gambarBase64ToSave = encodeBitmapToBase64(currentBitmap) ?: if (isEditMode) originalGambarBase64 else ""


        val jadwalIdToSave = if (isEditMode) existingJadwalId!! else database.reference.child("jadwal").push().key!!

        val jadwal = JadwalModel(
            id = jadwalIdToSave,
            userId = userId,
            namaMatkul = matkul,
            hari = hari,
            jam = jam,
            gambarBase64 = gambarBase64ToSave
        )

        Log.d("TambahJadwal", "Menyimpan jadwal: $jadwal")
        database.reference.child("jadwal").child(jadwalIdToSave).setValue(jadwal)
            .addOnSuccessListener {
                val message = if (isEditMode) "Jadwal berhasil diperbarui" else "Jadwal berhasil disimpan"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                Log.d("TambahJadwal", "$message. ID: $jadwalIdToSave")
                setResult(Activity.RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                val message = if (isEditMode) "Gagal memperbarui jadwal" else "Gagal menyimpan jadwal"
                Toast.makeText(this, "$message: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("TambahJadwal", message, e)
            }
    }
}