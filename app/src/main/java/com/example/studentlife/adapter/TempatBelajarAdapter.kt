package com.example.studentlife.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentlife.R
import com.example.studentlife.TambahTempatBelajarActivity
import com.example.studentlife.model.TempatBelajar

class TempatBelajarAdapter(
    listTempat: ArrayList<TempatBelajar>,
    private val context: Context
) :
    RecyclerView.Adapter<TempatBelajarAdapter.ViewHolder>() {
    private val listTempat: MutableList<TempatBelajar> = listTempat

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_tempat_belajar, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tempat: TempatBelajar = listTempat[position]
        holder.tvNamaTempat.text = "📕 " + tempat.nama
        holder.tvAlamat.setText(tempat.alamat)

        holder.btnDelete.setOnClickListener { v: View? ->
            val dialogView =
                LayoutInflater.from(context).inflate(R.layout.dialog_delete_tempat_belajar, null)
            val builder = AlertDialog.Builder(context)
            builder.setView(dialogView)
            val dialog = builder.create()
            dialog.show()

            dialogView.findViewById<View>(R.id.btnCancelDelete)
                .setOnClickListener { view: View? -> dialog.dismiss() }
            dialogView.findViewById<View>(R.id.btnConfirmDelete)
                .setOnClickListener { view: View? ->
                    listTempat.removeAt(holder.adapterPosition)
                    notifyItemRemoved(holder.adapterPosition)
                    dialog.dismiss()
                }
        }

        // 🔥 On item click → edit mode
        holder.itemView.setOnClickListener { v: View? ->
            val intent = Intent(
                context,
                TambahTempatBelajarActivity::class.java
            )
            intent.putExtra("itemPosition", holder.adapterPosition) //
            intent.putExtra("isEditMode", true)
            intent.putExtra("editNamaTempat", tempat.nama)
            intent.putExtra("editAlamatTempat", tempat.alamat)
            intent.putExtra("editImageUri", tempat.imageUri)
            (context as Activity).startActivityForResult(intent, 3) // 3 = kode edit
        }
    }

    override fun getItemCount(): Int {
        return listTempat.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvNamaTempat: TextView = itemView.findViewById(R.id.tvNamaTempat)
        var tvAlamat: TextView = itemView.findViewById(R.id.tvAlamat)
        var btnDelete: ImageView =
            itemView.findViewById(R.id.btnDelete) //
    }
}