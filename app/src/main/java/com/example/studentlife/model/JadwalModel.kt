package com.example.studentlife.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class JadwalModel(
    var id: String? = null,
    var userId: String? = null,
    var namaMatkul: String = "",
    var hari: String = "",
    var jam: String = "",
    var gambarBase64: String? = ""
) {
}