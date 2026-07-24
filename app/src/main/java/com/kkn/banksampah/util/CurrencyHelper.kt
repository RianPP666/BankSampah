package com.kkn.banksampah.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyHelper {
    private val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    fun formatRupiah(amount: Double): String = formatter.format(amount)
}
