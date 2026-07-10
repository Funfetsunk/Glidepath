package com.glidepath.app.domain

import com.glidepath.app.domain.model.appendMoneyDigit
import com.glidepath.app.domain.model.appendMoneySeparator
import com.glidepath.app.domain.model.clampMoneyRawDecimals
import com.glidepath.app.domain.model.formatMoney
import com.glidepath.app.domain.model.moneyRawDisplay
import com.glidepath.app.domain.model.moneyRawToPennies
import com.glidepath.app.domain.model.penniesToMoneyRaw
import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyInputTest {

    @Test
    fun whole_amount_parses_to_pennies() {
        assertEquals(1200L, moneyRawToPennies("12"))
        assertEquals(850000L, moneyRawToPennies("8500"))
    }

    @Test
    fun decimal_amount_parses_to_pennies() {
        assertEquals(1250L, moneyRawToPennies("12.5"))
        assertEquals(1250L, moneyRawToPennies("12.50"))
        assertEquals(1234L, moneyRawToPennies("12.34"))
        assertEquals(1200L, moneyRawToPennies("12."))
    }

    @Test
    fun empty_is_zero() {
        assertEquals(0L, moneyRawToPennies(""))
    }

    @Test
    fun digit_entry_respects_two_minor_digits() {
        var raw = ""
        raw = appendMoneyDigit(raw, '1', 2)
        raw = appendMoneyDigit(raw, '2', 2)
        raw = appendMoneySeparator(raw, 2)
        raw = appendMoneyDigit(raw, '5', 2)
        raw = appendMoneyDigit(raw, '0', 2)
        // A third minor digit is ignored.
        raw = appendMoneyDigit(raw, '9', 2)
        assertEquals("12.50", raw)
        assertEquals(1250L, moneyRawToPennies(raw))
    }

    @Test
    fun leading_zeros_are_dropped_in_major_part() {
        var raw = ""
        raw = appendMoneyDigit(raw, '0', 2)
        raw = appendMoneyDigit(raw, '0', 2)
        raw = appendMoneyDigit(raw, '5', 2)
        assertEquals("5", raw)
    }

    @Test
    fun separator_suppressed_for_zero_decimal_currency() {
        val raw = appendMoneySeparator("12", 0)
        assertEquals("12", raw)
    }

    @Test
    fun separator_added_only_once() {
        var raw = appendMoneySeparator("12", 2)
        raw = appendMoneySeparator(raw, 2)
        assertEquals("12.", raw)
    }

    @Test
    fun gbp_formats_with_period_and_comma_grouping() {
        assertEquals("£8,500", formatMoney(850000, "GBP"))
        assertEquals("£8,500.50", formatMoney(850050, "GBP"))
        assertEquals("£12.05", formatMoney(1205, "GBP"))
    }

    @Test
    fun eur_uses_comma_decimals_and_period_grouping() {
        assertEquals("€1.234,56", formatMoney(123456, "EUR"))
        assertEquals("€1.234", formatMoney(123400, "EUR"))
    }

    @Test
    fun jpy_never_shows_decimals() {
        assertEquals("¥1,234", formatMoney(123400, "JPY"))
        // Even if minor units somehow present, they are not displayed.
        assertEquals("¥1,234", formatMoney(123456, "JPY"))
    }

    @Test
    fun raw_display_uses_currency_separator() {
        assertEquals("8,500", moneyRawDisplay("8500", "GBP"))
        assertEquals("8.500", moneyRawDisplay("8500", "EUR"))
        assertEquals("12,50", moneyRawDisplay("12.50", "EUR"))
        assertEquals("0", moneyRawDisplay("", "GBP"))
    }

    @Test
    fun clamp_drops_decimals_for_zero_decimal_currency() {
        assertEquals("8500", clampMoneyRawDecimals("8500.50", 0))
        assertEquals("8500", clampMoneyRawDecimals("8500", 0))
    }

    @Test
    fun clamp_truncates_excess_minor_digits() {
        assertEquals("12.5", clampMoneyRawDecimals("12.567", 1))
        assertEquals("12.56", clampMoneyRawDecimals("12.56", 2))
    }

    @Test
    fun pennies_round_trip_to_raw() {
        assertEquals("450", penniesToMoneyRaw(45000, 2))
        assertEquals("450.50", penniesToMoneyRaw(45050, 2))
        assertEquals("450", penniesToMoneyRaw(45000, 0))
    }
}
