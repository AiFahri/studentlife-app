package com.example.studentlife

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentlife.adapters.PengeluaranAdapter
import com.example.studentlife.models.Pengeluaran
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DaftarPengeluaranActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_TAMBAH = 1
        const val REQUEST_EDIT = 100
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PengeluaranAdapter
    private var pengeluaranList: MutableList<Pengeluaran> = mutableListOf()

    private lateinit var btnAdd: Button
    private lateinit var databaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var valueEventListener: ValueEventListener? = null 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_pengeluaran)

        recyclerView = findViewById(R.id.recyclerViewPengeluaran)
        btnAdd = findViewById(R.id.btnAdd)
        val iconBack = findViewById<ImageView>(R.id.iconBack)

        iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("pengeluaran")

        adapter = PengeluaranAdapter(this, pengeluaranList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            Log.d("CEK", "Tombol + diklik")
            val intent = Intent(this@DaftarPengeluaranActivity, TambahPengeluaranActivity::class.java)
            startActivityForResult(intent, REQUEST_TAMBAH)
        }
    }

    private fun setupRealtimeDataListener(uid: String) {
        if (valueEventListener != null) {
            databaseRef.removeEventListener(valueEventListener!!)
        }
        val query = databaseRef.orderByChild("userId").equalTo(uid)
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<Pengeluaran>()
                for (childSnapshot in snapshot.children) {
                    val pengeluaran = childSnapshot.getValue(Pengeluaran::class.java)
                    pengeluaran?.let {
                        it.id = childSnapshot.key
                        tempList.add(it)
                    }
                }
                pengeluaranList.clear()
                pengeluaranList.addAll(tempList)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DaftarPengeluaranActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                Log.e("DaftarPengeluaran", "loadDataRealtime:onCancelled", error.toException())
            }
        }
        query.addValueEventListener(valueEventListener!!)
    }

    override fun onStart() {
        super.onStart()
        auth.currentUser?.uid?.let {
            setupRealtimeDataListener(it)
        }
    }

    override fun onStop() {
        super.onStop()
        valueEventListener?.let {
            databaseRef.removeEventListener(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
        }
    }
}