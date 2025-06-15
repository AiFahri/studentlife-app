package com.example.studentlife

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity // Atau androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentlife.adapter.TaskAdapter
import com.example.studentlife.model.Task // Pastikan import model Task yang sudah dimodifikasi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TaskList : ComponentActivity() { // Ganti ke AppCompatActivity jika menggunakan tema AppCompat

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private var taskList: MutableList<Task> = mutableListOf()

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private var taskValueEventListener: ValueEventListener? = null
    private val TAG = "TaskList"

    private val crudTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Operasi tambah/edit tugas selesai. Listener Firebase akan memperbarui daftar.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.task_list) // Pastikan ini layout yang benar

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User belum login.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        databaseRef = FirebaseDatabase.getInstance().getReference("tasks") // Path untuk tugas

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        taskAdapter = TaskAdapter(this, taskList, // Berikan context
            onItemClick = { task ->
                val intent = Intent(this, TaskAdd::class.java).apply {
                    putExtra("task_id", task.id)
                    // TaskAdd akan mengambil detail dari Firebase berdasarkan task_id
                }
                crudTaskLauncher.launch(intent)
            },
            onDeleteClick = { task ->
                showDeleteConfirmationDialog(task)
            }
        )
        recyclerView.adapter = taskAdapter

        val btnAdd = findViewById<ImageView>(R.id.btn_add)
        val iconBack = findViewById<ImageView>(R.id.iconBack) // Pastikan ID ini ada di task_list.xml

        iconBack.setOnClickListener {
            finish() // Atau onBackPressedDispatcher.onBackPressed()
        }
        btnAdd.setOnClickListener {
            val intent = Intent(this, TaskAdd::class.java)
            crudTaskLauncher.launch(intent)
        }
    }

    private fun setupRealtimeDataListener(userId: String) {
        if (taskValueEventListener != null) {
            val query = databaseRef.orderByChild("userId").equalTo(userId)
            query.removeEventListener(taskValueEventListener!!)
            Log.d(TAG, "Listener lama tugas dihapus.")
        }

        val query = databaseRef.orderByChild("userId").equalTo(userId)
        taskValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange tugas triggered. Jumlah item: ${snapshot.childrenCount}")
                taskList.clear()
                for (childSnapshot in snapshot.children) {
                    val task = childSnapshot.getValue(Task::class.java)
                    task?.let {
                        it.id = childSnapshot.key
                        taskList.add(it)
                    }
                }
                taskAdapter.notifyDataSetChanged()
                // Anda bisa menambahkan logika untuk menampilkan pesan jika daftar kosong
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Gagal memuat data tugas: ${error.message}", error.toException())
                Toast.makeText(this@TaskList, "Gagal memuat data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
        query.addValueEventListener(taskValueEventListener!!)
        Log.d(TAG, "Listener baru tugas ditambahkan untuk UID: $userId")
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
        taskValueEventListener?.let {
            val query = databaseRef.orderByChild("userId").equalTo(auth.currentUser?.uid ?: "")
            query.removeEventListener(it)
            Log.d(TAG, "Listener tugas dihapus.")
        }
        taskValueEventListener = null
    }

    private fun showDeleteConfirmationDialog(task: Task) {
        // Gunakan layout dialog yang sesuai, misal modal_delete.xml atau dialog_delete_item.xml
        val dialogView = LayoutInflater.from(this).inflate(R.layout.modal_delete, null) // Ganti jika perlu
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val btnCancel: Button = dialogView.findViewById(R.id.btnCancel)
        val btnDelete: Button = dialogView.findViewById(R.id.btnDelete)
        // val btnCloseIcon: ImageView? = dialogView.findViewById(R.id.btnClose) // Jika ada di layout dialog

        btnCancel.setOnClickListener { dialog.dismiss() }
        // btnCloseIcon?.setOnClickListener { dialog.dismiss() }

        btnDelete.setOnClickListener {
            if (task.id == null) {
                Toast.makeText(this, "Error: ID Tugas tidak valid.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            }
            databaseRef.child(task.id!!).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Tugas '${task.title.replace("📖 ", "")}' dihapus", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Tugas dihapus dari Firebase: ID ${task.id}")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menghapus tugas: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Gagal hapus tugas dari Firebase: ID ${task.id}", e)
                }
            dialog.dismiss()
        }
    }
}