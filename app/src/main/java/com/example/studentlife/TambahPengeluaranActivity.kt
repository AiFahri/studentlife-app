package com.example.studentlife

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.studentlife.models.Pengeluaran
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream

class TambahPengeluaranActivity : AppCompatActivity() {

    private lateinit var editNamaPengeluaran: EditText
    private lateinit var editJumlahPengeluaran: EditText
    private lateinit var btnSimpan: Button
    private lateinit var ivImagePengeluaran: ImageView
    private lateinit var tvUpload: TextView
    private lateinit var iconBack: ImageView

    private var imageUri: Uri? = null
    private var bitmapImage: Bitmap? = null
    private var isEditMode = false
    private var position = -1
    private var pengeluaranId: String? = null

    private val PICK_IMAGE_REQUEST = 1002
    private val TAG = "TambahPengeluaran"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pengeluaran)

        editNamaPengeluaran = findViewById(R.id.editNamaPengeluaran)
        editJumlahPengeluaran = findViewById(R.id.editJumlahPengeluaran)
        btnSimpan = findViewById(R.id.btnPratinjau)
        ivImagePengeluaran = findViewById(R.id.ivImagePengeluaran)
        tvUpload = findViewById(R.id.tvUpload)
        iconBack = findViewById(R.id.iconBack)

        iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        Log.d(TAG, "onCreate: Activity created. Edit mode: $isEditMode")

        if (intent.hasExtra("isEdit") && intent.getBooleanExtra("isEdit", false)) {
            isEditMode = true
            pengeluaranId = intent.getStringExtra("pengeluaranId")
            position = intent.getIntExtra("position", -1)
            Log.d(TAG, "onCreate: Edit mode. Pengeluaran ID: $pengeluaranId, Position: $position")


            val namaFromIntent = intent.getStringExtra("namaPengeluaran")
            val jumlahFromIntent = intent.getIntExtra("jumlahPengeluaran", 0)

            editNamaPengeluaran.setText(namaFromIntent)
            editJumlahPengeluaran.setText(jumlahFromIntent.toString())
            btnSimpan.text = "Simpan Perubahan"

            if (pengeluaranId != null) {
                fetchPengeluaranDetailsFromFirebase(pengeluaranId!!)
            }
        } else {
            isEditMode = false
            btnSimpan.text = "Simpan"
            Log.d(TAG, "onCreate: Add new mode.")
        }

        tvUpload.setOnClickListener {
            Log.d(TAG, "tvUpload clicked, opening gallery.")
            openGallery()
        }

        btnSimpan.setOnClickListener {
            Log.d(TAG, "btnSimpan clicked.")
            val nama = editNamaPengeluaran.text.toString().trim()
            val jumlahStr = editJumlahPengeluaran.text.toString().trim()

            if (nama.isEmpty() || jumlahStr.isEmpty()) {
                Log.w(TAG, "Validation failed: Nama atau Jumlah kosong.")
                Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val jumlah: Int = try {
                jumlahStr.toInt()
            } catch (e: NumberFormatException) {
                Log.w(TAG, "Validation failed: Jumlah bukan angka valid.", e)
                Toast.makeText(this, "Jumlah harus berupa angka", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d(TAG, "Validation successful. Nama: $nama, Jumlah: $jumlah")
            val gambarBase64ToSave = bitmapImage?.let {
                Log.d(TAG, "Encoding image to Base64.")
                encodeImageToBase64(it)
            } ?: run {
                Log.d(TAG, "No new image selected or available for Base64 encoding.")
                ""
            }

            savePengeluaranToFirebase(nama, jumlah, gambarBase64ToSave)
        }
    }

    private fun fetchPengeluaranDetailsFromFirebase(id: String) {
        Log.d(TAG, "fetchPengeluaranDetailsFromFirebase called for ID: $id")
        val databaseRef = FirebaseDatabase.getInstance().getReference("pengeluaran").child(id)
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()){
                    Log.w(TAG, "fetchPengeluaranDetailsFromFirebase: No data found for ID $id")
                    Toast.makeText(this@TambahPengeluaranActivity, "Detail pengeluaran tidak ditemukan di Firebase", Toast.LENGTH_SHORT).show()
                    return
                }
                val pengeluaran = snapshot.getValue(Pengeluaran::class.java)
                if (pengeluaran != null) {
                    Log.d(TAG, "fetchPengeluaranDetailsFromFirebase: Data fetched: $pengeluaran")
                    editNamaPengeluaran.setText(pengeluaran.nama)
                    editJumlahPengeluaran.setText(pengeluaran.jumlah.toString())

                    if (pengeluaran.gambarBase64.isNotEmpty()) {
                        Log.d(TAG, "fetchPengeluaranDetailsFromFirebase: Attempting to decode Base64 image.")
                        try {
                            val decodedBytes = Base64.decode(pengeluaran.gambarBase64, Base64.DEFAULT)
                            bitmapImage = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            ivImagePengeluaran.setImageBitmap(bitmapImage)
                            ivImagePengeluaran.visibility = View.VISIBLE
                            tvUpload.visibility = View.GONE
                            Log.d(TAG, "fetchPengeluaranDetailsFromFirebase: Image decoded and displayed.")
                        } catch (e: Exception) {
                            Log.e(TAG, "fetchPengeluaranDetailsFromFirebase: Error decoding Base64 image.", e)
                            bitmapImage = null
                            ivImagePengeluaran.visibility = View.GONE
                            tvUpload.visibility = View.VISIBLE
                            Toast.makeText(this@TambahPengeluaranActivity, "Gagal memuat gambar dari Firebase", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.d(TAG, "fetchPengeluaranDetailsFromFirebase: No image Base64 string in Firebase.")
                        bitmapImage = null
                        ivImagePengeluaran.visibility = View.GONE
                        tvUpload.visibility = View.VISIBLE
                    }
                } else {
                    Log.w(TAG, "fetchPengeluaranDetailsFromFirebase: Pengeluaran object is null after parsing snapshot for ID $id")
                    Toast.makeText(this@TambahPengeluaranActivity, "Detail pengeluaran tidak dapat diproses", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "fetchPengeluaranDetailsFromFirebase: Firebase onCancelled.", error.toException())
                Toast.makeText(this@TambahPengeluaranActivity, "Gagal memuat detail dari Firebase: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        val targetWidth = 300
        val targetHeight = (bitmap.height.toFloat() / bitmap.width.toFloat() * targetWidth).toInt()
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
        Log.d(TAG, "encodeImageToBase64: Encoded string length: ${base64String.length}")
        return base64String
    }

    private fun savePengeluaranToFirebase(nama: String, jumlah: Int, gambarBase64: String) {
        Log.d(TAG, "savePengeluaranToFirebase called. Nama: $nama, Jumlah: $jumlah, Image Base64 Length: ${gambarBase64.length}")
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.w(TAG, "savePengeluaranToFirebase: User not logged in.")
            Toast.makeText(this, "User belum login, silakan login kembali", Toast.LENGTH_LONG).show()
            return
        }

        val uid = currentUser.uid
        val databaseRef = FirebaseDatabase.getInstance().getReference("pengeluaran")

        val currentPengeluaranId = if (isEditMode && pengeluaranId != null) {
            Log.d(TAG, "savePengeluaranToFirebase: Edit mode, using existing ID: $pengeluaranId")
            pengeluaranId
        } else {
            val newId = databaseRef.push().key
            Log.d(TAG, "savePengeluaranToFirebase: Add mode, generated new ID: $newId")
            newId
        }

        if (currentPengeluaranId == null) {
            Log.e(TAG, "savePengeluaranToFirebase: Failed to get Pengeluaran ID (currentPengeluaranId is null).")
            Toast.makeText(this, "Gagal mendapatkan ID pengeluaran, coba lagi", Toast.LENGTH_SHORT).show()
            return
        }

        val pengeluaranToSave = Pengeluaran(
            id = currentPengeluaranId,
            userId = uid,
            nama = nama,
            jumlah = jumlah,
            gambarBase64 = gambarBase64
        )

        Log.d(TAG, "savePengeluaranToFirebase: Attempting to save to Firebase path: pengeluaran/$currentPengeluaranId, Data: $pengeluaranToSave")

        databaseRef.child(currentPengeluaranId).setValue(pengeluaranToSave)
            .addOnSuccessListener {
                Log.d(TAG, "savePengeluaranToFirebase: Data save successful for ID: $currentPengeluaranId")
                Toast.makeText(this, if (isEditMode) "Data berhasil diperbarui" else "Data berhasil disimpan", Toast.LENGTH_SHORT).show()

                val resultIntent = Intent().apply {
                    putExtra("pengeluaranId", pengeluaranToSave.id)
                    putExtra("namaPengeluaran", pengeluaranToSave.nama)
                    putExtra("jumlahPengeluaran", pengeluaranToSave.jumlah)
                    putExtra("isEdit", isEditMode)
                    if (isEditMode) {
                        putExtra("position", position)
                    }
                }
                setResult(RESULT_OK, resultIntent)
                Log.d(TAG, "savePengeluaranToFirebase: Calling finish().")
                finish()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "savePengeluaranToFirebase: Data save failed for ID: $currentPengeluaranId", exception)
                Toast.makeText(this, "Gagal menyimpan data ke Firebase: ${exception.message}", Toast.LENGTH_LONG).show()
                // Tampilkan detail error di log untuk Firebase Rules atau masalah jaringan
                exception.printStackTrace()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            Log.d(TAG, "onActivityResult: Image selected, Uri: $imageUri")
            try {
                contentResolver.takePersistableUriPermission(imageUri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                bitmapImage = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                ivImagePengeluaran.setImageBitmap(bitmapImage)
                ivImagePengeluaran.visibility = View.VISIBLE
                tvUpload.visibility = View.GONE
                Log.d(TAG, "onActivityResult: Image loaded into ivImagePengeluaran and bitmapImage set.")
            } catch (e: Exception) {
                Log.e(TAG, "onActivityResult: Error processing selected image.", e)
                Toast.makeText(this, "Gagal memproses gambar yang dipilih", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d(TAG, "onActivityResult: No image selected or result not OK.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Activity destroyed.")
    }
}