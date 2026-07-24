package com.kkn.banksampah.data.model

data class JenisSampah(
    val id: String = "",
    val nama: String = "",
    val kategori: String = "", // Organik, Plastik, Kertas, Logam, Kaca, Elektronik
    val satuan: String = "Kg",
    val hargaPerSatuan: Double = 0.0,
    val deskripsi: String = ""
)
