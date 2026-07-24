package com.kkn.banksampah.model

import com.google.firebase.firestore.DocumentId

data class Nasabah(
    @DocumentId
    var id: String? = null,
    var nama: String = "",
    var alamat: String = "",
    var noHp: String = "",
    var saldo: Double = 0.0
)
