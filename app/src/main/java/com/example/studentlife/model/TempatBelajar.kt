package com.example.studentlife.model

class TempatBelajar {
    var nama: String
        private set
    var alamat: String
        private set
    var imageUri: String? = null // Tambahan

    constructor(nama: String, alamat: String) {
        this.nama = nama
        this.alamat = alamat
    }

    // Tambahan constructor dengan imageUri
    constructor(nama: String, alamat: String, imageUri: String?) {
        this.nama = nama
        this.alamat = alamat
        this.imageUri = imageUri
    }
}