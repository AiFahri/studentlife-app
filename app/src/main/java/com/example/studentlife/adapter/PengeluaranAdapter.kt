package com.example.studentlife.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Html
import android.util.login
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.studentlife.DaftarPengeluaranActivity
import com.example.studentlife.R
import com.example.studentlife.TambahPengeluaranActivity
import com.example.studentlife.models.Pengeluaran
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.util.*

class PengeluaranAdapter(private val context: Context, private val pengeluaranList: MutableList<Pengeluaran>) :
    RecyclerView.Adapter<PengeluaranAdapter.ViewHolder>() {

    private val TAG = "PengeluaranAdapter" // Tag untuk logging

    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_pengeluaran, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: ViewHolder, position: Int) {
        val pengeluaran = pengeluaranList[position]

        holder.tvNama.text = "💸 ${pengeluaran.nama}"

        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val jumlahFormatted = formatRupiah.format(pengeluaran.jumlah)

        val jumlahText = "<font color='#101828'>Jumlah Uang: </font>" +
                "<font color='#667085'>$jumlahFormatted</font>"
        holder.tvJumlah.text = Html.fromHtml(jumlahText)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, TambahPengeluaranActivity::class.java)
            intent.putExtra("isEdit", true)
            intent.putExtra("position", position)
            intent.putExtra("namaPengeluaran", pengeluaran.nama)
            intent.putExtra("jumlahPengeluaran", pengeluaran.jumlah)
            intent.putExtra("gambarBase64", pengeluaran.gambarBase64 ?: "")
            intent.putExtra("pengeluaranId", pengeluaran.id)
            (context as Activity).startActivityForResult(intent, DaftarPengeluaranActivity.REQUEST_EDIT)
        }

        holder.ivDelete.setOnClickListener {
            showDeleteConfirmationDialog(pengeluaran, position)
        }
    }

    override fun getItemCount(): Int = pengeluaranList.size

    private fun showDeleteConfirmationDialog(pengeluaranToDelete: Pengeluaran, position: Int) {
        val builder = AlertDialog.Builder(context) // Menggunakan androidx.appcompat.app.AlertDialog
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_confirmation, null)
        builder.setView(dialogView)

        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)

        val dialog = builder.create()
        dialog.show()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnDelete.setOnClickListener {
            deletePengeluaranFromFirebase(pengeluaranToDelete)
            dialog.dismiss()
        }
    }

    private fun deletePengeluaranFromFirebase(pengeluaran: Pengeluaran) {
        if (pengeluaran.id == null) {
            Toast.makeText(context, "Error: ID Pengeluaran tidak valid untuk dihapus.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Pengeluaran ID is null, cannot delete from Firebase.")
            return
        }

        Log.d(TAG, "Attempting to delete pengeluaran with ID: ${pengeluaran.id}")
        val databaseRef = FirebaseDatabase.getInstance().getReference("pengeluaran").child(pengeluaran.id!!)

        databaseRef.removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "Successfully deleted pengeluaran with ID: ${pengeluaran.id} from Firebase.")
                Toast.makeText(context, "Pengeluaran berhasil dihapus", Toast.LENGTH_SHORT).show()
                // Tidak perlu manipulasi list lokal di sini.
                // ValueEventListener di DaftarPengeluaranActivity akan menangani pembaruan UI.
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to delete pengeluaran with ID: ${pengeluaran.id} from Firebase.", exception)
                Toast.makeText(context, "Gagal menghapus pengeluaran: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaPengeluaran)
        val tvJumlah: TextView = itemView.findViewById(R.id.tvJumlahPengeluaran)
        val ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)
    }
}