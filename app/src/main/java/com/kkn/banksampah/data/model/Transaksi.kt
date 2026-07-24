package com.kkn.banksampah.data.model

data class Transaksi(
    val id: String = "",
    val idNasabah: String = "",
    val namaNasabah: String = "",
    val jenisTransaksi: String = "", // "SETOR" or "TARIK"
    val detailSampah: List<DetailSampah> = emptyList(),
    val totalRupiah: Double = 0.0,
    val tanggal: Long = System.currentTimeMillis(),
    val petugasId: String = "",
    val petugasNama: String = "",
    val catatan: String = ""
)

data class DetailSampah(
    val idJenisSampah: String = "",
    val namaSampah: String = "",
    val beratKg: Double = 0.0,
    val hargaPerKg: Double = 0.0,
    val subtotal: Double = 0.0
)
