package com.example.studentlife

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskAdd : ComponentActivity() {

    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.task_add)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnPreview = findViewById<Button>(R.id.btn_pratinjau)
        val deadlineInput = findViewById<TextView>(R.id.input_deadline)
        val containerDeadline = findViewById<LinearLayout>(R.id.input_deadline_container2)
        val taskNameInput = findViewById<EditText>(R.id.input_nama)
        val taskDescInput = findViewById<EditText>(R.id.input_deskripsi)
        val iconUpload = findViewById<ImageView>(R.id.ic_upload)
        val containerUpload = findViewById<LinearLayout>(R.id.upload_container)

        var selectedDate: Calendar? = null

        val incomingTaskName = intent.getStringExtra("task_name")
        val incomingDeadline = intent.getStringExtra("task_deadline")
        val incomingTaskDesc = intent.getStringExtra("task_description")
        val incomingImageUri = intent.getStringExtra("task_image")

        if (!incomingTaskName.isNullOrEmpty()) taskNameInput.setText(incomingTaskName)
        if (!incomingTaskDesc.isNullOrEmpty()) taskDescInput.setText(incomingTaskDesc)
        if (!incomingDeadline.isNullOrEmpty()) {
            deadlineInput.text = incomingDeadline
            deadlineInput.setTextColor(Color.parseColor("#101828"))
        }
        if (!incomingImageUri.isNullOrEmpty()) {
            val uri = Uri.parse(incomingImageUri)
            selectedImageUri = uri
            iconUpload.setImageURI(uri)
        }

        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null && uri.toString().isNotEmpty()) {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                selectedImageUri = uri
                iconUpload.visibility = View.VISIBLE
                iconUpload.setImageURI(uri)
            }
        }

        containerUpload.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        fun showDatePicker() {
            val calendar = selectedDate ?: Calendar.getInstance()

            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val pickedDate = Calendar.getInstance()
                pickedDate.set(year, month, dayOfMonth)
                selectedDate = pickedDate

                val timePicker = TimePickerDialog(this, { _, hour, minute ->
                    pickedDate.set(Calendar.HOUR_OF_DAY, hour)
                    pickedDate.set(Calendar.MINUTE, minute)

                    val format = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale("id", "ID"))
                    deadlineInput.text = format.format(pickedDate.time)
                    deadlineInput.setTextColor(Color.parseColor("#101828"))
                },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )

                timePicker.setOnCancelListener {
                    showDatePicker()
                }

                timePicker.show()

            },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        containerDeadline.setOnClickListener {
            showDatePicker()
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnPreview.setOnClickListener {
            val taskName = taskNameInput.text.toString()
            val deadline = deadlineInput.text.toString()
            val taskDesc = taskDescInput.text.toString()
            val position = intent.getIntExtra("task_position", -1)

            if (taskName.isEmpty() || deadline.isEmpty() || taskDesc.isEmpty()) {
                Toast.makeText(this, "Harap isi semua kolom dan unggah gambar!", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, TaskPreview::class.java).apply {
                    putExtra("task_name", taskName)
                    putExtra("task_deadline", deadline)
                    putExtra("task_description", taskDesc)
                    putExtra("task_image", selectedImageUri?.toString() ?: "")
                    putExtra("task_position", position)
                }
                startActivityForResult(intent, 1)
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data)
            finish()
        }
    }
}
