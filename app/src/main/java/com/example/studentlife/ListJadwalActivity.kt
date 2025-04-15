package com.example.studentlife

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentlife.adapter.JadwalAdapter
import com.example.studentlife.model.JadwalModel

class ListJadwalActivity : AppCompatActivity() {

    private val listJadwal = mutableListOf<JadwalModel>()
    private lateinit var adapter: JadwalAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAdd: ImageButton

    companion object {
        const val REQUEST_CODE_TAMBAH = 1
    }
    private val tambahJadwalLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val matkul = result.data?.getStringExtra("saved_matkul") ?: return@registerForActivityResult
            val hari = result.data?.getStringExtra("saved_hari") ?: return@registerForActivityResult
            val jam = result.data?.getStringExtra("saved_jam") ?: return@registerForActivityResult

            val jadwalBaru = JadwalModel(matkul, hari, jam)
            listJadwal.add(jadwalBaru)
            adapter.notifyItemInserted(listJadwal.size - 1)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_jadwal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.list_jadwal)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        recyclerView = findViewById(R.id.rvJadwal)
        btnAdd = findViewById(R.id.btnAdd)

        adapter = JadwalAdapter(listJadwal)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            val intent = Intent(this, TambahJadwalActivity::class.java)
            tambahJadwalLauncher.launch(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_TAMBAH && resultCode == RESULT_OK) {
            val matkul = data?.getStringExtra("saved_matkul") ?: return
            val hari = data.getStringExtra("saved_hari") ?: return
            val jam = data.getStringExtra("saved_jam") ?: return

            listJadwal.add(JadwalModel(matkul, hari, jam))
            adapter.notifyItemInserted(listJadwal.size - 1)
        }
    }
}
