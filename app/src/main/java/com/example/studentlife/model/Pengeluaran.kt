package com.example.studentlife.models

data class Pengeluaran(
    var id: String? = null,
    var userId: String? = null, 
    var nama: String = "",      
    var jumlah: Int = 0,        
    var gambarBase64: String = ""
) {
}