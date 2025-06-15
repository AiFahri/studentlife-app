package com.example.studentlife

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity // Atau androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.example.studentlife.model.Task // Pastikan import model Task yang sudah dimodifikasi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskAdd : ComponentActivity() { // Ganti ke AppCompatActivity jika menggunakan tema AppCompat

    private lateinit var etTaskName: EditText
    private lateinit var tvDeadline: TextView
    private lateinit var etTaskDesc: EditText
    private lateinit var ivTaskImagePreview: ImageView // ID untuk preview gambar, sesuaikan dengan layout Anda
    private lateinit var containerUpload: LinearLayout // Asumsi ini container untuk klik upload
    private lateinit var btnSimpanTugas: Button
    private lateinit var btnBack: ImageView
    private lateinit var tvUploadTextHint: TextView // Teks "Unggah file disini"

    private var selectedImageUri: Uri? = null
    private var currentBitmap: Bitmap? = null // Untuk menyimpan bitmap yang akan di-encode

    private var selectedDateCalendar: Calendar = Calendar.getInstance()

    private var isEditMode = false
    private var existingTaskId: String? = null
    private var originalGambarBase64: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val TAG = "TaskAdd"

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && uri.toString().isNotEmpty()) {
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                selectedImageUri = uri // Simpan URI untuk referensi jika perlu
                currentBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                ivTaskImagePreview.setImageBitmap(currentBitmap)
                ivTaskImagePreview.visibility = View.VISIBLE
                tvUploadTextHint.visibility = View.GONE // Sembunyikan teks "Unggah file disini"
                Log.d(TAG, "Gambar dipilih: $uri")
            } catch (e: Exception) {
                Log.e(TAG, "Error memproses URI gambar: $uri", e)
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
                resetImagePreviewToDefault()
            }
        } else {
            Log.d(TAG, "Tidak ada gambar yang dipilih.")
            // Hanya reset jika bukan mode edit dengan gambar asli, atau jika memang tidak ada gambar
            if (!isEditMode || originalGambarBase64.isNullOrEmpty()) {
                resetImagePreviewToDefault()
                currentBitmap = null
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.task_add) // Pastikan ini layout yang benar

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Inisialisasi Views dari task_add.xml
        etTaskName = findViewById(R.id.input_nama)
        tvDeadline = findViewById(R.id.input_deadline)
        etTaskDesc = findViewById(R.id.input_deskripsi)
        // Pastikan ID untuk ImageView preview dan teks di bawahnya sesuai dengan layout Anda
        ivTaskImagePreview = findViewById(R.id.ic_upload) // Ganti dengan ID ImageView sebenarnya untuk preview
        tvUploadTextHint = findViewById(R.id.text_upload) // ID untuk TextView "Unggah file disini"
        containerUpload = findViewById(R.id.upload_container)
        btnSimpanTugas = findViewById(R.id.btn_pratinjau) // ID ini akan menjadi tombol Simpan
        btnBack = findViewById(R.id.btn_back)
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        val containerDeadline = findViewById<LinearLayout>(R.id.input_deadline_container2)

        existingTaskId = intent.getStringExtra("task_id")
        if (existingTaskId != null) {
            isEditMode = true
            tvTitle.text = "Edit Tugas" // Sesuaikan string
            btnSimpanTugas.text = "Simpan Perubahan"
            Log.d(TAG, "Mode Edit. Task ID: $existingTaskId")
            loadExistingTaskData(existingTaskId!!)
        } else {
            isEditMode = false
            tvTitle.text = getString(R.string.add_new_task_text)
            btnSimpanTugas.text = getString(R.string.save_text) // Atau "Simpan"
            resetImagePreviewToDefault()
            Log.d(TAG, "Mode Tambah Tugas Baru.")
        }

        btnBack.setOnClickListener {
            finish()
        }

        containerDeadline.setOnClickListener {
            showDateTimePicker()
        }

        containerUpload.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        ivTaskImagePreview.setOnClickListener { // Izinkan klik pada gambar untuk memilih ulang
            pickImageLauncher.launch("image/*")
        }


        btnSimpanTugas.setOnClickListener {
            saveOrUpdateTask()
        }
    }

    private fun resetImagePreviewToDefault() {
        ivTaskImagePreview.setImageResource(R.drawable.upload) // Pastikan drawable ini ada
        tvUploadTextHint.visibility = View.VISIBLE
        currentBitmap = null
        selectedImageUri = null // Reset juga URI terpilih
    }

    private fun loadExistingTaskData(taskId: String) {
        val taskRef = database.reference.child("tasks").child(taskId)
        taskRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val task = snapshot.getValue(Task::class.java)
                if (task != null) {
                    etTaskName.setText(task.title)
                    tvDeadline.text = task.deadline
                    if (task.deadline.isNotBlank() && task.deadline != getString(R.string.choose_date_text)) {
                        tvDeadline.setTextColor(Color.parseColor("#101828"))
                        try {
                            val format = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale("id", "ID"))
                            val date = format.parse(task.deadline)
                            if (date != null) selectedDateCalendar.time = date
                        } catch (e: Exception) { Log.e(TAG, "Error parsing deadline: ${task.deadline}", e) }
                    }
                    etTaskDesc.setText(task.description)
                    originalGambarBase64 = task.gambarBase64
                    if (!originalGambarBase64.isNullOrEmpty()) {
                        try {
                            currentBitmap = decodeBase64ToBitmap(originalGambarBase64!!)
                            if (currentBitmap != null) {
                                ivTaskImagePreview.setImageBitmap(currentBitmap)
                                tvUploadTextHint.visibility = View.GONE
                            } else { resetImagePreviewToDefault() }
                        } catch (e: Exception) { Log.e(TAG, "Error decode Base64 lama", e); resetImagePreviewToDefault()}
                    } else {
                        resetImagePreviewToDefault()
                    }
                } else {
                    Toast.makeText(this@TaskAdd, "Gagal memuat data tugas.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TaskAdd, "Gagal memuat data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showDateTimePicker() {
        val currentCalendar = Calendar.getInstance()
        val initialCalendar = if (tvDeadline.text != getString(R.string.choose_date_text) && tvDeadline.text.isNotBlank()) {
            try {
                val format = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale("id", "ID"))
                Calendar.getInstance().apply { time = format.parse(tvDeadline.text.toString())!! }
            } catch (e: Exception) { currentCalendar }
        } else { currentCalendar }

        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            selectedDateCalendar.set(year, month, dayOfMonth)
            TimePickerDialog(this, { _, hourOfDay, minute ->
                selectedDateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDateCalendar.set(Calendar.MINUTE, minute)
                val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale("id", "ID"))
                tvDeadline.text = sdf.format(selectedDateCalendar.time)
                tvDeadline.setTextColor(Color.parseColor("#101828"))
            },
                initialCalendar.get(Calendar.HOUR_OF_DAY),
                initialCalendar.get(Calendar.MINUTE),
                true
            ).show()
        },
            initialCalendar.get(Calendar.YEAR),
            initialCalendar.get(Calendar.MONTH),
            initialCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun encodeBitmapToBase64(bitmap: Bitmap?): String? {
        if (bitmap == null) return null
        val outputStream = ByteArrayOutputStream()
        // Sesuaikan ukuran dan kualitas kompresi jika perlu
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 480, (bitmap.height.toFloat() / bitmap.width.toFloat() * 480).toInt(), true)
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal decode Base64 ke Bitmap", e); null
        }
    }

    private fun saveOrUpdateTask() {
        val taskName = etTaskName.text.toString().trim()
        val deadline = tvDeadline.text.toString()
        val taskDesc = etTaskDesc.text.toString().trim()

        if (taskName.isEmpty()) {
            Toast.makeText(this, "Nama tugas wajib diisi", Toast.LENGTH_SHORT).show(); return
        }
        if (deadline == getString(R.string.choose_date_text) || deadline.isEmpty()) {
            Toast.makeText(this, "Tenggat waktu wajib diisi", Toast.LENGTH_SHORT).show(); return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User belum login.", Toast.LENGTH_LONG).show(); return
        }
        val userId = currentUser.uid

        val gambarBase64ToSave = encodeBitmapToBase64(currentBitmap) ?: if (isEditMode) originalGambarBase64 else ""

        val taskIdToSave = if (isEditMode) existingTaskId!! else database.reference.child("tasks").push().key!!

        val task = Task(
            id = taskIdToSave,
            userId = userId,
            title = taskName,
            deadline = deadline,
            description = taskDesc,
            gambarBase64 = gambarBase64ToSave
        )

        Log.d(TAG, "Menyimpan tugas: $task")
        database.reference.child("tasks").child(taskIdToSave).setValue(task)
            .addOnSuccessListener {
                val message = if (isEditMode) "Tugas berhasil diperbarui" else "Tugas berhasil disimpan"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "$message. ID: $taskIdToSave")
                setResult(Activity.RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                val message = if (isEditMode) "Gagal memperbarui tugas" else "Gagal menyimpan tugas"
                Toast.makeText(this, "$message: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, message, e)
            }
    }
}