package com.glidepath.app.domain.model

/**
 * Supported display currencies. Symbol + grouping only — no exchange rates (§9).
 *
 * @property decimals Number of minor-unit digits (2 for most, 0 for JPY which has no sub-unit).
 * @property decimalSep Character shown between major and minor units on display (',' for EUR).
 * @property thousandsSep Grouping character for the major part.
 */
enum class Currency(
    val code: String,
    val symbol: String,
    val decimals: Int,
    val decimalSep: Char,
    val thousandsSep: Char,
) {
    GBP("GBP", "£", 2, '.', ','),
    USD("USD", "$", 2, '.', ','),
    EUR("EUR", "€", 2, ',', '.'),
    JPY("JPY", "¥", 0, '.', ','),
    AUD("AUD", "A$", 2, '.', ','),
    CAD("CAD", "C$", 2, '.', ','),
    INR("INR", "₹", 2, '.', ',');

    companion object {
        fun fromCode(code: String): Currency = entries.firstOrNull { it.code == code } ?: GBP
    }
}

/** Just the symbol for a currency code (used where the hero renders the symbol separately). */
fun currencySymbol(currencyCode: String): String = Currency.fromCode(currencyCode).symbol

/** Number of minor-unit digits for a currency code (0 = no decimal entry, e.g. JPY). */
fun currencyDecimals(currencyCode: String): Int = Currency.fromCode(currencyCode).decimals

/** The decimal separator character to display for a currency code. */
fun currencyDecimalSeparator(currencyCode: String): Char = Currency.fromCode(currencyCode).decimalSep

/**
 * Formats [pennies] with thousands grouping and a leading symbol, e.g. `£8,500` or `£8,500.50`.
 * Minor units are shown only when non-zero (whole amounts stay clean, §9) and never for
 * zero-decimal currencies like JPY. EUR uses `.` grouping and `,` decimals.
 */
fun formatMoney(pennies: Long, currencyCode: String, withSymbol: Boolean = true): String {
    val currency = Currency.fromCode(currencyCode)
    val negative = pennies < 0
    val abs = kotlin.math.abs(pennies)
    val major = abs / 100
    val minor = (abs % 100).toInt()
    val majorGrouped = groupThousands(major, currency.thousandsSep)
    val body = if (currency.decimals == 0 || minor == 0) {
        majorGrouped
    } else {
        "$majorGrouped${currency.decimalSep}${minor.toString().padStart(2, '0')}"
    }
    val signed = if (negative) "-$body" else body
    return if (withSymbol) "${currency.symbol}$signed" else signed
}

private fun groupThousands(value: Long, separator: Char): String {
    val digits = value.toString()
    val sb = StringBuilder()
    for ((i, c) in digits.withIndex()) {
        if (i > 0 && (digits.length - i) % 3 == 0) sb.append(separator)
        sb.append(c)
    }
    return sb.toString()
}
