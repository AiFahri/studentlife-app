package com.example.studentlife.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class TempatBelajar(
    var id: String? = null,
    var userId: String? = null,
    var nama: String = "",
    var alamat: String = "",
    var gambarBase64: String? = ""
) {
}