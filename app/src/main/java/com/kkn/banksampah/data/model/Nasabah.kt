package com.kkn.banksampah.data.model

data class Nasabah(
    val id: String = "",
    val nama: String = "",
    val alamat: String = "",
    val noHp: String = "",
    val saldo: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
