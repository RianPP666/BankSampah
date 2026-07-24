package com.kkn.banksampah.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Transaksi(
    @DocumentId
    var id: String? = null,
    var idNasabah: String = "",
    var namaNasabah: String = "",
    var tanggal: Timestamp = Timestamp.now(),
    var jenisTransaksi: String = "", // "SETOR" or "TARIK"
    var detailSampah: List<DetailSampah> = emptyList(),
    var totalRupiah: Double = 0.0
)
