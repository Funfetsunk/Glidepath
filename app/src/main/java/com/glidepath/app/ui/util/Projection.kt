package com.glidepath.app.ui.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MonthYear = DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault())
private val DayMonth = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

/** Formats a projected completion date [monthsFromNow] months ahead as "MMM yyyy", or "—". */
fun projectDate(monthsFromNow: Int?, zone: ZoneId = ZoneId.systemDefault()): String {
    if (monthsFromNow == null) return "—"
    return Instant.now().atZone(zone).toLocalDate().plusMonths(monthsFromNow.toLong()).format(MonthYear)
}

/** Formats an epoch-millis payment date as "d MMM yyyy". */
fun formatDate(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate().format(DayMonth)
