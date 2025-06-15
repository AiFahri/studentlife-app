package com.example.studentlife.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Task(
    var id: String? = null,
    var userId: String? = null,
    var title: String = "",
    var deadline: String = "",
    var description: String = "",
    var gambarBase64: String? = ""
) {
}