package com.example.util

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {

    fun getCurrencySymbol(currency: String): String {
        return when (currency.lowercase()) {
            "inr" -> "₹"
            "usd" -> "$"
            "eur" -> "€"
            "gbp" -> "£"
            else -> "$"
        }
    }

    fun formatPrice(amount: Double?, currency: String = "inr"): String {
        if (amount == null) return "—"
        val symbol = getCurrencySymbol(currency)

        return if (currency.lowercase() == "inr") {
            val format = NumberFormat.getNumberInstance(Locale("en", "IN"))
            format.maximumFractionDigits = if (amount < 10) 4 else 2
            format.minimumFractionDigits = 2
            symbol + format.format(amount)
        } else {
            val format = NumberFormat.getNumberInstance(Locale.US)
            format.maximumFractionDigits = if (amount < 10) 4 else 2
            format.minimumFractionDigits = 2
            symbol + format.format(amount)
        }
    }

    fun formatCompact(amount: Double?, currency: String = "inr"): String {
        if (amount == null) return "—"
        val symbol = getCurrencySymbol(currency)
        val abs = Math.abs(amount)

        if (currency.lowercase() == "inr") {
            return when {
                abs >= 1e12 -> String.format(Locale.US, "%s%.2fT", symbol, amount / 1e12)
                abs >= 1e7 -> String.format(Locale.US, "%s%.2fCr", symbol, amount / 1e7)
                abs >= 1e5 -> String.format(Locale.US, "%s%.2fL", symbol, amount / 1e5)
                abs >= 1e3 -> String.format(Locale.US, "%s%.2fK", symbol, amount / 1e3)
                else -> String.format(Locale.US, "%s%.2f", symbol, amount)
            }
        }

        return when {
            abs >= 1e12 -> String.format(Locale.US, "%s%.2fT", symbol, amount / 1e12)
            abs >= 1e9 -> String.format(Locale.US, "%s%.2fB", symbol, amount / 1e9)
            abs >= 1e6 -> String.format(Locale.US, "%s%.2fM", symbol, amount / 1e6)
            abs >= 1e3 -> String.format(Locale.US, "%s%.2fK", symbol, amount / 1e3)
            else -> String.format(Locale.US, "%s%.2f", symbol, amount)
        }
    }

    fun formatPercentage(value: Double?): String {
        if (value == null) return "0.00%"
        val prefix = if (value > 0) "+" else ""
        return String.format(Locale.US, "%s%.2f%%", prefix, value)
    }
}
