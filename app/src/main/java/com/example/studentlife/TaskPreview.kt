package com.example.studentlife

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class TaskPreview : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.task_preview)

        val btnBack = findViewById<ImageView>(R.id.btn_back)

        btnBack.setOnClickListener {
            finish()
        }

        val btnSave = findViewById<Button>(R.id.btn_lanjut)
        btnSave.setOnClickListener {
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        val taskName = intent.getStringExtra("task_name")
        val deadline = intent.getStringExtra("deadline")
        val taskDesc = intent.getStringExtra("task_description")
        val imageURI = intent.getStringExtra("task_image")

        val taskNameText = findViewById<TextView>(R.id.tv_task_title)
        val deadlineText = findViewById<TextView>(R.id.deadline)
        val taskDescText = findViewById<TextView>(R.id.taskDescription)
        val imagePreview = findViewById<ImageView>(R.id.imgPreview)

        taskNameText.text = taskName
        deadlineText.text = deadline
        taskDescText.text = taskDesc

        if (!imageURI.isNullOrEmpty()) {
            imagePreview.setImageURI(Uri.parse(imageURI))
            imagePreview.visibility = View.VISIBLE
        }
    }
}