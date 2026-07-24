package com.kkn.banksampah.model

import com.google.firebase.firestore.DocumentId

data class JenisSampah(
    @DocumentId
    var id: String? = null,
    var namaSampah: String = "",
    var hargaPerKg: Double = 0.0
)
