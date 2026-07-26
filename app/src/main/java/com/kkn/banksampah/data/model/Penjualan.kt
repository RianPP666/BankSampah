package com.kkn.banksampah.data.model

data class Penjualan(
    val id: String = "",
    val namaPengepul: String = "",
    val tanggal: Long = System.currentTimeMillis(),
    val detailSampah: List<DetailSampahJual> = emptyList(),
    val totalBeratKg: Double = 0.0,
    val totalHargaJual: Double = 0.0,
    val petugasId: String = "",
    val petugasNama: String = "",
    val catatan: String = ""
)

data class DetailSampahJual(
    val namaJenisSampah: String = "",
    val beratKg: Double = 0.0,
    val hargaJualPerKg: Double = 0.0,
    val subtotal: Double = 0.0
)
