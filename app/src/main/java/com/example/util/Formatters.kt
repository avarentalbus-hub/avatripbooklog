package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
  private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
  private val shortDateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
  private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
  private val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
    maximumFractionDigits = 1
  }

  fun formatDate(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) return "—"
    return dateFormat.format(Date(timestamp))
  }

  fun formatShortDate(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) return "—"
    return shortDateFormat.format(Date(timestamp))
  }

  fun formatTime(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) return "—"
    return timeFormat.format(Date(timestamp))
  }

  fun formatKm(km: Double?): String {
    if (km == null) return "—"
    return "${numberFormat.format(km)} km"
  }

  fun formatAed(amount: Double?): String {
    if (amount == null) return "0.00 AED"
    return String.format(Locale.US, "%.2f AED", amount)
  }
}
