package com.example.studentlife

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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

    private val previewLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val savedMatkul = result.data?.getStringExtra("saved_matkul") ?: return@registerForActivityResult
            val savedHari = result.data?.getStringExtra("saved_hari") ?: return@registerForActivityResult
            val savedJam = result.data?.getStringExtra("saved_jam") ?: return@registerForActivityResult

            val resultIntent = Intent()
            resultIntent.putExtra("saved_matkul", savedMatkul)
            resultIntent.putExtra("saved_hari", savedHari)
            resultIntent.putExtra("saved_jam", savedJam)
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
        val iconBack = findViewById<ImageView>(R.id.iconBack)

        val hariOptions = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat")
        val jamOptions = listOf("07.00", "09.00", "13.00", "19.00")

        spinnerHari.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, hariOptions)
        spinnerJam.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, jamOptions)

        iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnPratinjau.setOnClickListener {
            val matkul = etMatkul.text.toString().trim()
            val hari = spinnerHari.selectedItem.toString()
            val jam = spinnerJam.selectedItem.toString()

            if (matkul.isEmpty()) {
                Toast.makeText(this, "Nama mata kuliah wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, DetailJadwalActivity::class.java)
            intent.putExtra("matkul", matkul)
            intent.putExtra("hari", hari)
            intent.putExtra("jam", jam)
            previewLauncher.launch(intent)
        }
    }
}
