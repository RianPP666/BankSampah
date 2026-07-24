package com.kkn.banksampah.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateHelper {
    private val fullFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    private val dateOnly = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    private val monthYear = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
    
    fun formatFull(millis: Long): String = fullFormat.format(Date(millis))
    fun formatDate(millis: Long): String = dateOnly.format(Date(millis))
    fun formatMonthYear(millis: Long): String = monthYear.format(Date(millis))
}
