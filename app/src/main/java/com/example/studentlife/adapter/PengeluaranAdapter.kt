package com.example.studentlife.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.studentlife.DaftarPengeluaranActivity
import com.example.studentlife.R
import com.example.studentlife.TambahPengeluaranActivity
import com.example.studentlife.models.Pengeluaran
import java.text.NumberFormat
import java.util.*

class PengeluaranAdapter(private val context: Context, private val pengeluaranList: MutableList<Pengeluaran>) :
    RecyclerView.Adapter<PengeluaranAdapter.ViewHolder>() {

    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_pengeluaran, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: ViewHolder, position: Int) {
        val pengeluaran = pengeluaranList[position]

        // Set Nama
        holder.tvNama.text = "💸 ${pengeluaran.nama}"

        // Format jumlah jadi Rp5.000.000
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val jumlahFormatted = formatRupiah.format(pengeluaran.jumlah)

        // Tampilkan 2 warna: label hitam, nilai abu
        val jumlahText = "<font color='#101828'>Jumlah Uang: </font>" +
                "<font color='#667085'>$jumlahFormatted</font>"
        holder.tvJumlah.text = Html.fromHtml(jumlahText)

        // Tambahkan onClick listener untuk item
        holder.itemView.setOnClickListener {
            // Buka activity edit dengan data pengeluaran yang dipilih
            val intent = Intent(context, TambahPengeluaranActivity::class.java)
            intent.putExtra("isEdit", true)  // Tandai sebagai mode edit
            intent.putExtra("position", position)
            intent.putExtra("namaPengeluaran", pengeluaran.nama)
            intent.putExtra("jumlahPengeluaran", pengeluaran.jumlah)
            intent.putExtra("imageUri", pengeluaran.imageUri)  // Gambar URI
            (context as Activity).startActivityForResult(intent, DaftarPengeluaranActivity.REQUEST_EDIT)
        }

        // Menambahkan listener untuk ikon delete
        holder.ivDelete.setOnClickListener {
            // Tampilkan modal konfirmasi hapus
            showDeleteConfirmationDialog(position)
        }
    }

    override fun getItemCount(): Int {
        return pengeluaranList.size
    }

    // Fungsi untuk menghapus item
    private fun removeItem(position: Int) {
        pengeluaranList.removeAt(position)
        notifyItemRemoved(position)
    }

    // Fungsi untuk menampilkan konfirmasi hapus
    private fun showDeleteConfirmationDialog(position: Int) {
        val builder = android.app.AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_confirmation, null)
        builder.setView(dialogView)

        val tvDeleteConfirmation = dialogView.findViewById<TextView>(R.id.tvDeleteConfirmation)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)

        val dialog = builder.create()
        dialog.show()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnDelete.setOnClickListener {
            // Hapus item dari daftar dan beri tahu adapter
            removeItem(position)
            dialog.dismiss()
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaPengeluaran)
        val tvJumlah: TextView = itemView.findViewById(R.id.tvJumlahPengeluaran)
        val ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)
    }

    // Fungsi untuk memperbarui item yang sudah ada
    fun updateItem(position: Int, pengeluaran: Pengeluaran) {
        pengeluaranList[position] = pengeluaran  // Update data item
        notifyItemChanged(position)  // Notifikasi RecyclerView
    }

    // Fungsi untuk menambahkan item baru
    fun addItem(pengeluaran: Pengeluaran) {
        pengeluaranList.add(pengeluaran)
        notifyItemInserted(pengeluaranList.size - 1)  // Tambahkan item di akhir list
    }
}
