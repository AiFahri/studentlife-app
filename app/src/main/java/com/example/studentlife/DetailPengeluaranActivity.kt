package com.example.studentlife

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.studentlife.models.Pengeluaran
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
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
    private var imageKey: String? = null
    private var gambarBase64: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_pengeluaran)

        tvDetailNama = findViewById(R.id.tvDetailNama)
        tvDetailJumlah = findViewById(R.id.tvDetailJumlah)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnBack = findViewById(R.id.iconBack)
        ivImagePengeluaran = findViewById(R.id.imagePengeluaran)

        // Ambil data dari Intent
        val intent = intent
        nama = intent.getStringExtra("namaPengeluaran")
        jumlah = intent.getIntExtra("jumlahPengeluaran", 0)
        imageKey = intent.getStringExtra("imageKey")

        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        tvDetailNama.text = nama
        tvDetailJumlah.text = formatRupiah.format(jumlah)

        // Load gambar jika ada
        if (!imageKey.isNullOrEmpty()) {
            loadImageFromFirebase(imageKey!!)
        }

        // Tombol untuk simpan pengeluaran ke Firebase
        btnSimpan.setOnClickListener {
            simpanKeFirebase()
        }

        // Tombol kembali ke activity sebelumnya
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadImageFromFirebase(imageKey: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("temp_images").child(imageKey)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    gambarBase64 = snapshot.child("gambarBase64").getValue(String::class.java) ?: ""

                    if (gambarBase64.isNotEmpty()) {
                        // Decode dan tampilkan gambar
                        try {
                            val decodedBytes = Base64.decode(gambarBase64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            ivImagePengeluaran.setImageBitmap(bitmap)
                            ivImagePengeluaran.visibility = View.VISIBLE
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailPengeluaranActivity, "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fungsi untuk menyimpan data ke Firebase
    private fun simpanKeFirebase() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = currentUser.uid
        val databaseRef = FirebaseDatabase.getInstance().getReference("pengeluaran")

        val newRef = databaseRef.push()
        val pengeluaranBaru = Pengeluaran(
            id = newRef.key,
            userId = uid,
            nama = nama ?: "",
            jumlah = jumlah,
            gambarBase64 = gambarBase64
        )

        newRef.setValue(pengeluaranBaru)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Hapus gambar sementara setelah berhasil simpan
                    if (!imageKey.isNullOrEmpty()) {
                        deleteTemporaryImage(imageKey!!)
                    }

                    Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                    kembaliKeActivitySebelumnya(pengeluaranBaru)
                } else {
                    Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun deleteTemporaryImage(imageKey: String) {
        val tempImageRef = FirebaseDatabase.getInstance().getReference("temp_images").child(imageKey)
        tempImageRef.removeValue()
    }

    // Kembali ke activity sebelumnya setelah data berhasil disimpan
    private fun kembaliKeActivitySebelumnya(pengeluaran: Pengeluaran) {
        val resultIntent = Intent().apply {
            putExtra("pengeluaranId", pengeluaran.id)
            putExtra("namaPengeluaran", pengeluaran.nama)
            putExtra("jumlahPengeluaran", pengeluaran.jumlah)
            putExtra("gambarBase64", pengeluaran.gambarBase64)
        }
        setResult(RESULT_OK, resultIntent)

        // Kembali ke DaftarPengeluaranActivity
        val intent = Intent(this, DaftarPengeluaranActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}