package com.example.studentlife

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentlife.adapter.TaskAdapter
import com.example.studentlife.model.Task

class TaskList : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskList: ArrayList<Task>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.task_list)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadTaskData()
        taskAdapter = TaskAdapter(taskList) { task ->
            val position = taskList.indexOf(task)

            val intent = Intent(this, TaskAdd::class.java).apply {
                putExtra("task_name", task.title.substring(2))
                putExtra("task_deadline", task.deadline)
                putExtra("task_description", task.description)
                putExtra("task_image", task.imageURI)
                putExtra("task_position", position)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivityForResult(intent, 1)
        }
        recyclerView.adapter = taskAdapter

        val btnAdd = findViewById<ImageView>(R.id.btn_add)
        val iconBack = findViewById<ImageView>(R.id.iconBack)

        iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        btnAdd.setOnClickListener {
            val intent = Intent(this, TaskAdd::class.java)
            startActivityForResult(intent, 1)
        }
    }

    private fun loadTaskData() {
        taskList = arrayListOf(
            Task("Projek Akhir #1 PAM", "Sabtu, 29 Maret 2025 00:00", "Bikin 1 aplikasi mobile, tugas kelompok", ""),
            Task("Kriptografi", "Jumat, 04 April 2025 23.59", "Membuat classical crypto 1, tugas kelompok juga", ""),
            Task("Projek Akhir #2 PAM", "Selasa, 15 April 2025 00:00", "Melanjutkan proyek akhir #1", "")
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            val title = data.getStringExtra("task_name") ?: ""
            val deadline = data.getStringExtra("task_deadline") ?: ""
            val description = data.getStringExtra("task_description") ?: ""
            val imageURI = data.getStringExtra("task_image") ?: ""
            val position = data.getIntExtra("task_position", -1)

            if (title.isNotEmpty() && deadline.isNotEmpty() && description.isNotEmpty()) {
                if (position == -1) {
                    taskList.add(Task(title, deadline, description, imageURI))
                    taskAdapter.notifyItemInserted(taskList.size - 1)
                } else {
                    taskList[position] = Task(title, deadline, description, imageURI)
                    taskAdapter.notifyItemChanged(position)
                }
            }
        }
    }
}