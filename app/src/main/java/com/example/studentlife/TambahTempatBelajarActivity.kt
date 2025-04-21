package com.example.studentlife

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.max

class TambahTempatBelajarActivity : AppCompatActivity() {
    private lateinit var iconUpload: ImageView
    private var selectedImageUri: Uri? = null
    private var currentEditPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_tempat_belajar)

        currentEditPosition = intent.getIntExtra("itemPosition", -1)

        iconUpload = findViewById(R.id.icon_upload)
        val uploadContainer = findViewById<ConstraintLayout>(R.id.upload_foto_container)

        uploadContainer.setOnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_PICK)
            intent.setType("image/*")
            startActivityForResult(
                Intent.createChooser(intent, "Pilih Gambar"),
                PICK_IMAGE_REQUEST
            )
        }

        // Inisialisasi tombol back
        val btnBack = findViewById<FrameLayout>(R.id.btnBack)

        // Deklarasi input field
        val etNamaTempat = findViewById<EditText>(R.id.etNamaTempat)
        val etAlamat = findViewById<EditText>(R.id.etAlamat)
        val btnPratinjau = findViewById<Button>(R.id.button_pratinjau)


        val tvTitle = findViewById<TextView>(R.id.tambah_temp)
        // Cek apakah sedang dalam mode edit
        val isEditMode = intent.getBooleanExtra("isEditMode", false)
        if (isEditMode) {
            tvTitle.text = "Edit Tempat Belajar"
        }

        val editNama = intent.getStringExtra("editNamaTempat")
        val editAlamat = intent.getStringExtra("editAlamatTempat")
        val editImageUriString = intent.getStringExtra("editImageUri")

        if (editNama != null) etNamaTempat.setText(editNama)
        if (editAlamat != null) etAlamat.setText(editAlamat)
        if (editImageUriString != null) {
            selectedImageUri = Uri.parse(editImageUriString)
            iconUpload.setImageURI(selectedImageUri)
        }

        // Event listener tombol back
        btnBack.setOnClickListener { v: View? ->
            finish() // Kembali ke ListTempatBelajarActivity
        }

        // Event listener tombol "Pratinjau"
        btnPratinjau.setOnClickListener { v: View? ->
            val namaTempat = etNamaTempat.text.toString().trim { it <= ' ' }
            val alamatTempat = etAlamat.text.toString().trim { it <= ' ' }
            if (!namaTempat.isEmpty() && !alamatTempat.isEmpty()) {
                val intent = Intent(
                    this@TambahTempatBelajarActivity,
                    DetailTempatBelajarActivity::class.java
                )
                intent.putExtra("namaTempat", namaTempat)
                intent.putExtra("alamatTempat", alamatTempat)
                intent.putExtra("itemPosition", currentEditPosition)

                if (selectedImageUri != null) {
                    intent.putExtra("imageUri", selectedImageUri.toString())
                }

                startActivityForResult(intent, 2)
            } else {
                Toast.makeText(this, "Harap isi semua data!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

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

    // TERIMA DATA DARI DetailTempatBelajarActivity DAN KEMBALIKAN KE ListTempatBelajarActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2 && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data)
            finish()
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val originalUri = data.data
            selectedImageUri =
                saveImageToInternalStorage(originalUri!!) // ⬅ Gunakan URI hasil salinan

            try {
                val inputStream = contentResolver.openInputStream(
                    selectedImageUri!!
                )
                val drawable = Drawable.createFromStream(inputStream, selectedImageUri.toString())
                iconUpload!!.setImageDrawable(drawable)
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Gagal memuat gambar.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToInternalStorage(sourceUri: Uri): Uri? {
        try {
            val inputStream = contentResolver.openInputStream(sourceUri)
            val file = File(filesDir, "upload_" + System.currentTimeMillis() + ".jpg")
            val outputStream: OutputStream = FileOutputStream(file)

            val buffer = ByteArray(4096)
            var bytesRead: Int
            while ((inputStream!!.read(buffer).also { bytesRead = it }) != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            inputStream.close()
            outputStream.close()

            return Uri.fromFile(file) // URI yang valid permanen di internal app
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 100
    }
}