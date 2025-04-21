package com.example.studentlife

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentlife.adapters.PengeluaranAdapter
import com.example.studentlife.models.Pengeluaran

class DaftarPengeluaranActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_EDIT = 100
        const val REQUEST_TAMBAH = 1
        const val REQUEST_DETAIL = 102
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PengeluaranAdapter
    private lateinit var pengeluaranList: MutableList<Pengeluaran>

    private lateinit var btnAdd: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_pengeluaran)

        recyclerView = findViewById(R.id.recyclerViewPengeluaran)
        btnAdd = findViewById(R.id.btnAdd) // tombol +

        // 1️⃣ Inisialisasi list dan tambahkan data dummy
        pengeluaranList = mutableListOf(
            Pengeluaran("Beli Ayam Bu Elly", 15000, ""),
            Pengeluaran("Beli Naoki", 50000, ""),
            Pengeluaran("Beli Nasi Goreng 168", 20000, "")
        )

        // 2️⃣ Set adapter & RecyclerView
        adapter = PengeluaranAdapter(this, pengeluaranList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 3️⃣ Tombol tambah → buka TambahPengeluaranActivity
        btnAdd.setOnClickListener {
            Log.d("CEK", "Tombol + diklik")
            val intent = Intent(this@DaftarPengeluaranActivity, TambahPengeluaranActivity::class.java)
            startActivityForResult(intent, REQUEST_TAMBAH) // Memulai activity TambahPengeluaranActivity
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val nama = data.getStringExtra("namaPengeluaran")
            val jumlah = data.getIntExtra("jumlahPengeluaran", 0)
            val imageUri = data.getStringExtra("imageUri")

            if (requestCode == REQUEST_TAMBAH) {
                // Menambahkan pengeluaran baru ke daftar
                pengeluaranList.add(Pengeluaran(nama ?: "", jumlah, imageUri ?: ""))
                adapter.notifyDataSetChanged() // Memperbarui tampilan RecyclerView
            } else if (requestCode == REQUEST_EDIT) {
                // Update pengeluaran yang sudah ada
                val position = data.getIntExtra("position", -1)
                if (position != -1) {
                    pengeluaranList[position] = Pengeluaran(nama ?: "", jumlah, imageUri ?: "")
                    adapter.notifyItemChanged(position)
                }
            }
        }
    }
}
