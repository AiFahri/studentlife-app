package com.example.studentlife

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentlife.adapter.TempatBelajarAdapter
import com.example.studentlife.model.TempatBelajar
import kotlin.math.max

class ListTempatBelajarActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TempatBelajarAdapter
    private lateinit var tempatBelajarList: ArrayList<TempatBelajar>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_list_tempat_belajar)

        recyclerView = findViewById(R.id.recyclerViewTempatBelajar)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Tambahkan data ke list
        tempatBelajarList = ArrayList<TempatBelajar>()
        tempatBelajarList!!.add(TempatBelajar("CW Coffe Malang", "Jl. Polisi Baik No. 87"))
        tempatBelajarList!!.add(TempatBelajar("Starbucks Malang", "Jl. Kawi No. 10"))
        tempatBelajarList!!.add(TempatBelajar("Kedai Kopi Hitam", "Jl. Veteran No. 45"))

        adapter = TempatBelajarAdapter(tempatBelajarList, this)
        recyclerView.setAdapter(adapter)

        // Event listener untuk tombol tambah tempat belajar
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        btnAdd.setOnClickListener { v: View? ->
            val intent = Intent(
                this@ListTempatBelajarActivity,
                TambahTempatBelajarActivity::class.java
            )
            startActivityForResult(intent, 1)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val namaTempat = data.getStringExtra("namaTempat")
            val alamatTempat = data.getStringExtra("alamatTempat")
            val position = data.getIntExtra("itemPosition", -1)
            val imageUri = data.getStringExtra("imageUri")

            if (namaTempat != null && alamatTempat != null) {
                val newItem: TempatBelajar = TempatBelajar(namaTempat, alamatTempat)
                newItem.imageUri = imageUri

                if (requestCode == 1) {
                    tempatBelajarList!!.add(newItem)
                } else if (requestCode == 3 && position >= 0 && position < tempatBelajarList!!.size) {
                    tempatBelajarList!![position] = newItem
                }

                adapter.notifyDataSetChanged()
            }
        }
    }
}