package com.example.studentlife

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TambahJadwalActivity : AppCompatActivity() {
    private lateinit var etMatkul: EditText
    private lateinit var spinnerHari: Spinner
    private lateinit var spinnerJam: Spinner
    private lateinit var btnPratinjau: Button
    private lateinit var btnBack: ImageButton
    private lateinit var previewImage: ImageView
    private lateinit var uploadClick: TextView
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && uri.toString().isNotEmpty()) {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            Log.d("TambahJadwal", "Gambar dipilih: $uri")
            selectedImageUri = uri
            previewImage.visibility = View.VISIBLE
            previewImage.setImageURI(uri)
        } else {
            Log.d("TambahJadwal", "Tidak ada gambar yang dipilih")
        }
    }

    private val previewLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val savedMatkul = result.data?.getStringExtra("saved_matkul") ?: return@registerForActivityResult
            val savedHari = result.data?.getStringExtra("saved_hari") ?: return@registerForActivityResult
            val savedJam = result.data?.getStringExtra("saved_jam") ?: return@registerForActivityResult
            val savedImage = result.data?.getStringExtra("saved_image")
            val isEdit = result.data?.getBooleanExtra("isEdit", false) ?: false
            val position   = result.data!!.getIntExtra("position", -1)
            val resultIntent = Intent()
            resultIntent.putExtra("saved_matkul", savedMatkul)
            resultIntent.putExtra("saved_hari", savedHari)
            resultIntent.putExtra("saved_jam", savedJam)
            resultIntent.putExtra("saved_image", savedImage)
            resultIntent.putExtra("isEdit", isEdit)
            resultIntent.putExtra("position",  position)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_jadwal)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tambah_jadwal)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etMatkul = findViewById(R.id.etMatkul)
        spinnerHari = findViewById(R.id.spinnerHari)
        spinnerJam = findViewById(R.id.spinnerJam)
        btnPratinjau = findViewById(R.id.btnPratinjau)
        previewImage = findViewById(R.id.previewImage)
        previewImage.visibility = View.VISIBLE
        previewImage.setImageResource(R.drawable.ic_upload)
        Log.d("TambahJadwal", "onCreate edit_image = ${intent.getStringExtra("edit_image")}")
        uploadClick = findViewById(R.id.uploadClick)
        uploadClick.paintFlags = uploadClick.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        val iconBack = findViewById<ImageView>(R.id.iconBack)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val hariOptions = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat")
        val jamOptions = listOf("07.00", "09.00", "13.00", "19.00")
        val editPosition = intent.getIntExtra("position", -1)
        val isEdit     = editPosition >= 0
        if (isEdit) {
            etMatkul.setText(intent.getStringExtra("edit_matkul"))
            spinnerHari.setSelection(hariOptions.indexOf(intent.getStringExtra("edit_hari")))
            spinnerJam.setSelection(jamOptions.indexOf(intent.getStringExtra("edit_jam")))
            intent.getStringExtra("edit_image")?.takeIf { it.isNotBlank() }?.let { uriStr ->
                try {
                    val uri = Uri.parse(uriStr)
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    selectedImageUri = uri
                    previewImage.visibility = View.VISIBLE
                    previewImage.setImageURI(uri)
                } catch (e: Exception) {
                    Log.e("TambahJadwal", "Gagal parse/gain permission URI: $uriStr", e)
                    previewImage.setImageResource(R.drawable.ic_upload)
                }
            }?: run {
                    // kalau tidak ada edit_image sama sekali
                    previewImage.setImageResource(R.drawable.ic_upload)
                }
            tvTitle.text = "Edit Jadwal Kuliah"
            btnPratinjau.text = "Edit"
        }
        spinnerHari.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, hariOptions)
        spinnerJam.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, jamOptions)

        iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        uploadClick.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnPratinjau.setOnClickListener {
            val matkul = etMatkul.text.toString().trim()
            val hari = spinnerHari.selectedItem.toString()
            val jam = spinnerJam.selectedItem.toString()

            if (matkul.isEmpty()) {
                Toast.makeText(this, "Nama mata kuliah wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val detailIntent = Intent(this, DetailJadwalActivity::class.java).apply {
                putExtra("matkul", matkul)
                putExtra("hari", hari)
                putExtra("jam", jam)
                putExtra("isEdit", isEdit)
                putExtra("position", editPosition)
                selectedImageUri?.toString()?.let { uriStr ->
                    putExtra("image_uri", uriStr)
                }
            }
            previewLauncher.launch(detailIntent)
        }
    }
}
