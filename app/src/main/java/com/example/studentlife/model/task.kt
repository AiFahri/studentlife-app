package com.example.studentlife.model

data class Task(
    private var _title: String,
    val deadline: String,
    val description: String,
    val imageURI: String
) {
    var title: String
        get() = "\uD83D\uDCD5 $_title"
        set(value) {
            _title = value
        }
}