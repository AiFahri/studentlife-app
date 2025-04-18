package com.example.studentlife.adapter

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.studentlife.R
import com.example.studentlife.model.JadwalModel

class JadwalAdapter(private val context: Context, private val data: MutableList<JadwalModel>, private val onDeleteClick: (Int) -> Unit,  private val onItemClick: (Int) -> Unit)  :
    RecyclerView.Adapter<JadwalAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
        val tvMatkul: TextView = view.findViewById(R.id.tvMatkul)
        val tvHari: TextView = view.findViewById(R.id.tvHari)
        val tvJam: TextView = view.findViewById(R.id.tvJam)
//        val imagePreview: ImageView = view.findViewById(R.id.imagePreview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_jadwal, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.tvMatkul.text = "📖 ${item.namaMatkul}"
        holder.tvHari.text = item.hari
        holder.tvJam.text = item.jam
//        Log.d("JadwalAdapter", "Image URI: ${item.imageUri}")
//        if (!item.imageUri.isNullOrEmpty()) {
//            try {
//                holder.imagePreview.visibility = View.VISIBLE
//                holder.imagePreview.setImageURI(Uri.parse(item.imageUri))
//            } catch (e: Exception) {
//                e.printStackTrace()
//                holder.imagePreview.visibility = View.GONE
//            }
//        } else {
//            holder.imagePreview.visibility = View.GONE
//        }
        holder.btnDelete.setOnClickListener {
            onDeleteClick(position)
        }
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }
}

