package com.example.studentlife.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentlife.R
import com.example.studentlife.model.TempatBelajar

class TempatBelajarAdapter(
    private val context: Context, // Tambahkan Context
    private val listTempat: MutableList<TempatBelajar>, // Gunakan MutableList
    private val onItemClick: (TempatBelajar) -> Unit, // Callback untuk klik item (edit)
    private val onDeleteClick: (TempatBelajar) -> Unit  // Callback untuk klik tombol hapus
) :
    RecyclerView.Adapter<TempatBelajarAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_tempat_belajar, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tempat: TempatBelajar = listTempat[position]
        holder.tvNamaTempat.text = if (tempat.nama.startsWith("📕 ")) tempat.nama else "📕 ${tempat.nama}"
        holder.tvAlamat.text = tempat.alamat // Harusnya tvAlamat, bukan setText

        holder.btnDelete.setOnClickListener {
            if (holder.adapterPosition != RecyclerView.NO_POSITION) { // Cek posisi valid
                onDeleteClick(listTempat[holder.adapterPosition])
            }
        }

        holder.itemView.setOnClickListener {
            if (holder.adapterPosition != RecyclerView.NO_POSITION) { // Cek posisi valid
                onItemClick(listTempat[holder.adapterPosition])
            }
        }
    }

    override fun getItemCount(): Int {
        return listTempat.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvNamaTempat: TextView = itemView.findViewById(R.id.tvNamaTempat)
        var tvAlamat: TextView = itemView.findViewById(R.id.tvAlamat)
        var btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }
}