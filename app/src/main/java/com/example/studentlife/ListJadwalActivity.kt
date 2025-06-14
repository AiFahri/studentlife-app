package com.example.studentlife

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentlife.adapter.JadwalAdapter
import com.example.studentlife.model.JadwalModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ListJadwalActivity : AppCompatActivity() {

    private val listJadwal = mutableListOf<JadwalModel>()
    private lateinit var adapter: JadwalAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAdd: ImageButton

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private var jadwalValueEventListener: ValueEventListener? = null

    private val TAG = "ListJadwalActivity"

    private val crudJadwalLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
           
            Log.d(TAG, "Operasi tambah/edit jadwal selesai dengan sukses.")
            // Toast.makeText(this, "Memuat data terbaru...", Toast.LENGTH_SHORT).show()
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

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User belum login.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        databaseRef = FirebaseDatabase.getInstance().getReference("jadwal")

        recyclerView = findViewById(R.id.rvJadwal)
        btnAdd = findViewById(R.id.btnAdd)
        val iconBack = findViewById<ImageView>(R.id.iconBack)

        adapter = JadwalAdapter(this, listJadwal,
            onDeleteClick = { position ->
                if (position >= 0 && position < listJadwal.size) {
                    showDeleteConfirmationDialog(listJadwal[position])
                } else {
                    Log.w(TAG, "Posisi hapus tidak valid: $position")
                }
            },
            onItemClick = { position ->
                if (position >= 0 && position < listJadwal.size) {
                    val item = listJadwal[position]
                    val intent = Intent(this, TambahJadwalActivity::class.java).apply {
                        putExtra("jadwal_id", item.id) // Kirim ID Firebase
                        putExtra("edit_matkul", item.namaMatkul)
                        putExtra("edit_hari", item.hari)
                        putExtra("edit_jam", item.jam)
                        item.gambarBase64?.takeIf { it.isNotBlank() }?.let { base64Str ->
                            putExtra("edit_image_base64", base64Str)
                        }
                    }
                    crudJadwalLauncher.launch(intent)
                } else {
                    Log.w(TAG, "Posisi klik tidak valid: $position")
                }
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnAdd.setOnClickListener {
            val intent = Intent(this, TambahJadwalActivity::class.java)
            crudJadwalLauncher.launch(intent)
        }
    }

    private fun setupRealtimeDataListener(userId: String) {
        if (jadwalValueEventListener != null) {
            val query = databaseRef.orderByChild("userId").equalTo(userId)
            query.removeEventListener(jadwalValueEventListener!!)
            Log.d(TAG, "Listener lama jadwal dihapus.")
        }

        val query = databaseRef.orderByChild("userId").equalTo(userId)
        jadwalValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange jadwal triggered. Data: ${snapshot.childrenCount} items")
                listJadwal.clear()
                for (childSnapshot in snapshot.children) {
                    val jadwal = childSnapshot.getValue(JadwalModel::class.java)
                    jadwal?.let {
                        it.id = childSnapshot.key
                        listJadwal.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
                if (listJadwal.isEmpty()) {
                    // Toast.makeText(this@ListJadwalActivity, "Belum ada jadwal.", Toast.LENGTH_SHORT).show() // Opsional
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Gagal memuat data jadwal: ${error.message}", error.toException())
                Toast.makeText(this@ListJadwalActivity, "Gagal memuat data jadwal: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
        query.addValueEventListener(jadwalValueEventListener!!)
        Log.d(TAG, "Listener baru jadwal ditambahkan untuk UID: $userId")
    }

    override fun onStart() {
        super.onStart()
        auth.currentUser?.uid?.let {
            setupRealtimeDataListener(it)
        } ?: run {
            Toast.makeText(this, "Gagal mendapatkan UID user.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        jadwalValueEventListener?.let {
            val query = databaseRef.orderByChild("userId").equalTo(auth.currentUser?.uid ?: "")
            query.removeEventListener(it)
            Log.d(TAG, "Listener jadwal dihapus dari query.")
        }
        jadwalValueEventListener = null
    }


    private fun showDeleteConfirmationDialog(jadwal: JadwalModel) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_item, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val btnCancel: Button = dialogView.findViewById(R.id.btnCancel)
        val btnDelete: Button = dialogView.findViewById(R.id.btnDelete)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            if (jadwal.id == null || auth.currentUser?.uid == null) {
                Toast.makeText(this, "Error: Data tidak lengkap untuk dihapus", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            }
            databaseRef.child(jadwal.id!!).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Jadwal '${jadwal.namaMatkul}' dihapus", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Jadwal dihapus dari Firebase: ID ${jadwal.id}")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menghapus jadwal: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Gagal hapus jadwal dari Firebase: ID ${jadwal.id}", e)
                }
            dialog.dismiss()
        }
    }
}