package com.example.studentlife.models

data class Pengeluaran(
    val nama: String,
    val jumlah: Int,
    var imageUri: String = "" // Default value for imageUri is an empty string
) {
    // The constructor handles the null or empty imageUri to prevent NullPointerException
    init {
        if (imageUri.isNullOrEmpty()) {
            // Make sure imageUri is not null or empty
            this.imageUri = ""
        }
    }
}
