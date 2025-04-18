package com.example.studentlife

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
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
            val imageUri = result.data?.getStringExtra("saved_image")
            val isEdit = result.data?.getBooleanExtra("isEdit", false) ?: false
            val position = result.data?.getIntExtra("position", -1) ?: -1

            val updatedJadwal = JadwalModel(matkul, hari, jam, imageUri)

            if (isEdit && position >= 0) {
                listJadwal[position] = updatedJadwal
                adapter.notifyItemChanged(position)
            } else {
                listJadwal.add(updatedJadwal)
                adapter.notifyItemInserted(listJadwal.size - 1)
            }
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

        adapter = JadwalAdapter(this, listJadwal,
            onDeleteClick = { position -> showDeleteConfirmationDialog(position) },
            onItemClick = { position ->
                val item = listJadwal[position]
                val intent = Intent(this, TambahJadwalActivity::class.java).apply {
                    putExtra("edit_matkul", item.namaMatkul)
                    putExtra("edit_hari", item.hari)
                    putExtra("edit_jam", item.jam)
                    putExtra("position", position)
                    item.imageUri?.takeIf { it.isNotBlank() }?.let { uriStr ->
                        putExtra("edit_image", uriStr)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }
                tambahJadwalLauncher.launch(intent)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            val intent = Intent(this, TambahJadwalActivity::class.java)
            tambahJadwalLauncher.launch(intent)
        }
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        val item = listJadwal[position]

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
            listJadwal.removeAt(position)
            adapter.notifyItemRemoved(position)
            Toast.makeText(this, "Jadwal dihapus: ${item.namaMatkul}", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

}
