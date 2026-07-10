package com.glidepath.app.domain.model

/** Supported display currencies. Symbol + grouping only — no exchange rates (§9). */
enum class Currency(val code: String, val symbol: String) {
    GBP("GBP", "£"),
    USD("USD", "$"),
    EUR("EUR", "€"),
    JPY("JPY", "¥"),
    AUD("AUD", "A$"),
    CAD("CAD", "C$"),
    INR("INR", "₹");

    companion object {
        fun fromCode(code: String): Currency = entries.firstOrNull { it.code == code } ?: GBP
    }
}

/**
 * Formats [pennies] as a whole-unit amount with thousands grouping and a leading symbol,
 * e.g. `£8,500`. Minor units are rounded to the nearest whole unit for display (§9).
 */
fun formatMoney(pennies: Long, currencyCode: String, withSymbol: Boolean = true): String {
    val currency = Currency.fromCode(currencyCode)
    val units = Math.round(pennies / 100.0)
    val grouped = groupThousands(units)
    return if (withSymbol) "${currency.symbol}$grouped" else grouped
}

/** Just the symbol for a currency code (used where the hero renders the symbol separately). */
fun currencySymbol(currencyCode: String): String = Currency.fromCode(currencyCode).symbol

private fun groupThousands(value: Long): String {
    val negative = value < 0
    val digits = kotlin.math.abs(value).toString()
    val sb = StringBuilder()
    for ((i, c) in digits.withIndex()) {
        if (i > 0 && (digits.length - i) % 3 == 0) sb.append(',')
        sb.append(c)
    }
    return if (negative) "-$sb" else sb.toString()
}
