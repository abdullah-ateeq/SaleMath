package com.example.salemath.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object MoneyFormatter {

    fun getCurrencyPrefix(currency: String): String {
        return when (currency.trim()) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "INR" -> "₹"
            "PKR" -> "PKR "
            "AED" -> "AED "
            "SAR" -> "SAR "
            else -> if (currency.isNotBlank()) "${currency.trim()} " else ""
        }
    }

    /**
     * Formats a BigDecimal number respecting decimal places and comma separators.
     * E.g. 10000 -> "10,000.00" (if 2 decimals) or "10,000" (if 0 decimals).
     */
    fun formatNumber(number: BigDecimal, decimalPlaces: Int): String {
        val decimals = decimalPlaces.coerceIn(0, 4)
        val symbols = DecimalFormatSymbols(Locale.US)
        val pattern = if (decimals == 0) {
            "#,##0"
        } else {
            "#,##0." + "0".repeat(decimals)
        }
        val df = DecimalFormat(pattern, symbols)
        val scaled = number.setScale(decimals, RoundingMode.HALF_UP)
        return df.format(scaled)
    }

    /**
     * Formats money with currency prefix/code and specified decimal places.
     * E.g. "PKR 10,000" or "PKR 6,840.00"
     */
    fun formatMoney(amount: BigDecimal, currency: String, decimalPlaces: Int): String {
        val prefix = getCurrencyPrefix(currency)
        val formattedNumber = formatNumber(amount, decimalPlaces)
        return "$prefix$formattedNumber"
    }

    /**
     * Formats percentage with specified decimal places.
     * E.g. "31.60%" or "20%"
     */
    fun formatPercentage(
        percent: BigDecimal,
        decimalPlaces: Int = 2,
        trimTrailingZeros: Boolean = false
    ): String {
        val decimals = decimalPlaces.coerceIn(0, 4)
        val scaled = percent.setScale(decimals, RoundingMode.HALF_UP)
        val symbols = DecimalFormatSymbols(Locale.US)

        return if (trimTrailingZeros) {
            val df = DecimalFormat("#,##0.####", symbols)
            "${df.format(scaled)}%"
        } else {
            val pattern = if (decimals == 0) "#,##0" else "#,##0." + "0".repeat(decimals)
            val df = DecimalFormat(pattern, symbols)
            "${df.format(scaled)}%"
        }
    }

    /**
     * Format discount percentage for chips and input summaries (e.g. "20%", "17.5%").
     */
    fun formatDiscountInput(percent: BigDecimal): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#,##0.####", symbols)
        return "${df.format(percent)}%"
    }

    /**
     * Safely parses user input string into BigDecimal, returning null on failure or if negative.
     */
    fun parseBigDecimalOrNull(text: String): BigDecimal? {
        val cleaned = text.trim().replace(",", "")
        if (cleaned.isEmpty()) return null
        return try {
            val parsed = BigDecimal(cleaned)
            if (parsed < BigDecimal.ZERO) null else parsed
        } catch (_: Exception) {
            null
        }
    }
}
