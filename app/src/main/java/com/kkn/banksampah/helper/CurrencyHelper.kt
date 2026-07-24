package com.kkn.banksampah.helper

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyHelper {
    private val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    private val formatter = DecimalFormat("#,###", symbols)

    fun formatRupiah(amount: Double): String {
        return "Rp ${formatter.format(amount.toLong())}"
    }

    fun formatKg(weight: Double): String {
        return if (weight == weight.toLong().toDouble()) {
            "${weight.toLong()} Kg"
        } else {
            "${"%,.1f".format(weight)} Kg"
        }
    }
}
