package com.example.studentlife.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentlife.R
import com.example.studentlife.model.Task

class TaskAdapter(
    private val context: Context, // Tambahkan Context
    private var taskList: MutableList<Task>, // Gunakan MutableList
    private val onItemClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTitle: TextView = itemView.findViewById(R.id.task_title)
        val taskDeadline: TextView = itemView.findViewById(R.id.task_deadline)
        val btnDelete: ImageView = itemView.findViewById(R.id.btn_delete) // Pastikan ID ini benar
        private val container: View = itemView.findViewById(R.id.task_item_container) // ID dari root item card

        fun bind(task: Task) {
            // Menambahkan emoji "📖 " di sini saat menampilkan
            taskTitle.text = if (task.title.startsWith("📖 ")) task.title else "📖 ${task.title}"
            taskDeadline.text = task.deadline

            container.setOnClickListener {
                onItemClick(task)
            }
            btnDelete.setOnClickListener {
                onDeleteClick(task)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_item_card, parent, false) // Pastikan nama layout item benar
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(taskList[position])
    }

    override fun getItemCount(): Int = taskList.size

    // showDeleteDialog dipindahkan ke TaskList Activity
}