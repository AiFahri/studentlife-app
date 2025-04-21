package com.example.studentlife.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.studentlife.R
import com.example.studentlife.model.Task

class TaskAdapter(private val taskList: ArrayList<Task>, private val listener: (Task) -> Unit) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTitle: TextView = itemView.findViewById(R.id.task_title)
        val taskDeadline: TextView = itemView.findViewById(R.id.task_deadline)
        val btnDelete: ImageView = itemView.findViewById(R.id.btn_delete)
        private val container: View = itemView.findViewById(R.id.task_item_container)

        fun bind(task: Task) {
            taskTitle.text = task.title
            taskDeadline.text = task.deadline

            container.setOnClickListener {
                listener(task)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_item_card, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.taskTitle.text = task.title
        holder.taskDeadline.text = task.deadline
        holder.bind(task)

        holder.btnDelete.setOnClickListener {
            showDeleteDialog(holder.itemView.context, position)
        }
    }

    override fun getItemCount(): Int = taskList.size

    private fun showDeleteDialog(context: Context, position: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.modal_delete, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnDelete).setOnClickListener {
            taskList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, taskList.size)
            dialog.dismiss()
        }

        dialogView.findViewById<ImageView>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }
}
