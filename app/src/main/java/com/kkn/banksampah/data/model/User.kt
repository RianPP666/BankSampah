package com.kkn.banksampah.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val nama: String = "",
    val role: String = "petugas" // "admin" or "petugas"
)
