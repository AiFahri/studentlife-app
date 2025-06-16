package com.example.studentlife

import android.app.Activity // Import Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts // Import ActivityResultContracts
import androidx.appcompat.app.AlertDialog // Import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentlife.adapter.TempatBelajarAdapter
import com.example.studentlife.model.TempatBelajar // Pastikan import model yang sudah dimodifikasi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.math.max

class ListTempatBelajarActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TempatBelajarAdapter
    private var tempatBelajarList: MutableList<TempatBelajar> = mutableListOf() // Gunakan MutableList

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private var tempatBelajarValueEventListener: ValueEventListener? = null
    private val TAG = "ListTempatBelajar"

    // Launcher untuk Tambah/Edit Tempat Belajar
    private val crudTempatBelajarLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Operasi tambah/edit tempat belajar selesai. Listener Firebase akan memperbarui daftar.")
            // Tidak perlu lagi memproses data dari result.data di sini.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list_tempat_belajar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
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
        // Path untuk data tempat belajar, misal "tempat_belajar"
        databaseRef = FirebaseDatabase.getInstance().getReference("tempat_belajar")

        recyclerView = findViewById(R.id.recyclerViewTempatBelajar)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TempatBelajarAdapter(this, tempatBelajarList, // Berikan context
            onItemClick = { tempat ->
                val intent = Intent(this, TambahTempatBelajarActivity::class.java).apply {
                    putExtra("tempat_id", tempat.id) // Kirim ID Firebase untuk edit
                    // Data lain akan diambil dari Firebase di TambahTempatBelajarActivity
                }
                crudTempatBelajarLauncher.launch(intent)
            },
            onDeleteClick = { tempat ->
                showDeleteConfirmationDialog(tempat)
            }
        )
        recyclerView.adapter = adapter // Set adapter

        val btnAdd = findViewById<Button>(R.id.btnAdd) // Asumsi ID btnAdd adalah Button
        btnAdd.setOnClickListener {
            val intent = Intent(this, TambahTempatBelajarActivity::class.java)
            crudTempatBelajarLauncher.launch(intent)
        }
        val iconBack = findViewById<ImageView>(R.id.iconBack)
        iconBack.setOnClickListener {
            finish() // Atau onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRealtimeDataListener(userId: String) {
        if (tempatBelajarValueEventListener != null) {
            val query = databaseRef.orderByChild("userId").equalTo(userId)
            query.removeEventListener(tempatBelajarValueEventListener!!)
            Log.d(TAG, "Listener lama tempat belajar dihapus.")
        }

        val query = databaseRef.orderByChild("userId").equalTo(userId)
        tempatBelajarValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange tempat belajar triggered. Jumlah item: ${snapshot.childrenCount}")
                tempatBelajarList.clear()
                for (childSnapshot in snapshot.children) {
                    val tempat = childSnapshot.getValue(TempatBelajar::class.java)
                    tempat?.let {
                        it.id = childSnapshot.key // Simpan Firebase key sebagai ID
                        tempatBelajarList.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Gagal memuat data tempat belajar: ${error.message}", error.toException())
                Toast.makeText(this@ListTempatBelajarActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
        query.addValueEventListener(tempatBelajarValueEventListener!!)
        Log.d(TAG, "Listener baru tempat belajar ditambahkan untuk UID: $userId")
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
        tempatBelajarValueEventListener?.let {
            val query = databaseRef.orderByChild("userId").equalTo(auth.currentUser?.uid ?: "")
            query.removeEventListener(it)
            Log.d(TAG, "Listener tempat belajar dihapus.")
        }
        tempatBelajarValueEventListener = null
    }

    private fun showDeleteConfirmationDialog(tempat: TempatBelajar) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_tempat_belajar, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.show()

        val btnCancel: Button = dialogView.findViewById(R.id.btnCancelDelete) // Sesuaikan ID jika berbeda
        val btnDelete: Button = dialogView.findViewById(R.id.btnConfirmDelete) // Sesuaikan ID jika berbeda

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnDelete.setOnClickListener {
            if (tempat.id == null) {
                Toast.makeText(this, "Error: ID Tempat tidak valid.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            }
            databaseRef.child(tempat.id!!).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Tempat '${tempat.nama}' dihapus", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Tempat belajar dihapus dari Firebase: ID ${tempat.id}")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menghapus tempat: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Gagal hapus tempat dari Firebase: ID ${tempat.id}", e)
                }
            dialog.dismiss()
        }
    }

}