package com.glidepath.app.domain.model

/**
 * Pure helpers for keypad money entry. The in-progress value is held as a raw string that always
 * uses a canonical '.' as its decimal point, independent of the currency's display separator — so
 * switching currency mid-entry never corrupts the value. Rendering swaps '.' for the currency's
 * own separator. Amounts are capped at [decimals] minor digits; when decimals is 0 (e.g. JPY) the
 * separator can never be added.
 */
private const val CANON_SEP = '.'
private const val MAX_MAJOR_DIGITS = 12

/** Appends a digit '0'..'9', enforcing the minor-digit cap and dropping leading zeros. */
fun appendMoneyDigit(raw: String, digit: Char, decimals: Int): String {
    val dot = raw.indexOf(CANON_SEP)
    if (dot >= 0) {
        val minor = raw.substring(dot + 1)
        if (minor.length >= decimals) return raw
        return raw + digit
    }
    val next = (raw + digit).trimStart('0')
    return next.take(MAX_MAJOR_DIGITS)
}

/** Adds the decimal separator once, if the currency supports minor units. */
fun appendMoneySeparator(raw: String, decimals: Int): String {
    if (decimals <= 0 || raw.contains(CANON_SEP)) return raw
    return (if (raw.isEmpty()) "0" else raw) + CANON_SEP
}

/** Removes the last entered character. */
fun backspaceMoney(raw: String): String = raw.dropLast(1)

/**
 * Re-clamps an entry to a currency with [decimals] minor digits — e.g. when the user switches
 * currency mid-entry. Drops the separator and minor digits entirely for zero-decimal currencies,
 * and truncates any excess minor digits otherwise.
 */
fun clampMoneyRawDecimals(raw: String, decimals: Int): String {
    val dot = raw.indexOf(CANON_SEP)
    if (dot < 0) return raw
    if (decimals <= 0) return raw.substring(0, dot)
    val major = raw.substring(0, dot)
    val minor = raw.substring(dot + 1).take(decimals)
    return "$major$CANON_SEP$minor"
}

/** Converts the raw entry to integer minor units (pennies). */
fun moneyRawToPennies(raw: String): Long {
    if (raw.isEmpty()) return 0L
    val dot = raw.indexOf(CANON_SEP)
    val majorPart = if (dot >= 0) raw.substring(0, dot) else raw
    val minorPart = if (dot >= 0) raw.substring(dot + 1) else ""
    val major = majorPart.ifEmpty { "0" }.toLongOrNull() ?: 0L
    val minor = (minorPart + "00").take(2).toLongOrNull() ?: 0L
    return major * 100 + minor
}

/** Produces an editable raw string (canonical '.') for a fixed pennies value, e.g. a quick chip. */
fun penniesToMoneyRaw(pennies: Long, decimals: Int): String {
    val major = pennies / 100
    val minor = (pennies % 100).toInt()
    return if (decimals <= 0 || minor == 0) major.toString()
    else "$major$CANON_SEP${minor.toString().padStart(2, '0')}"
}

/**
 * Renders the raw entry for display (no symbol), grouping the major part and using the currency's
 * decimal separator. A trailing separator (user just tapped it) is preserved so the field shows it.
 */
fun moneyRawDisplay(raw: String, currencyCode: String): String {
    if (raw.isEmpty()) return "0"
    val currency = Currency.fromCode(currencyCode)
    val dot = raw.indexOf(CANON_SEP)
    val majorPart = if (dot >= 0) raw.substring(0, dot) else raw
    val minorPart = if (dot >= 0) raw.substring(dot + 1) else null
    val major = majorPart.ifEmpty { "0" }.toLongOrNull() ?: 0L
    val grouped = groupForDisplay(major, currency.thousandsSep)
    return if (minorPart != null) "$grouped${currency.decimalSep}$minorPart" else grouped
}

private fun groupForDisplay(value: Long, separator: Char): String {
    val digits = value.toString()
    val sb = StringBuilder()
    for ((i, c) in digits.withIndex()) {
        if (i > 0 && (digits.length - i) % 3 == 0) sb.append(separator)
        sb.append(c)
    }
    return sb.toString()
}
