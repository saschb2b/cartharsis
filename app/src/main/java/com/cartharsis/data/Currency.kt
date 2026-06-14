package com.cartharsis.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Digit grouping shape: Western 3-3-3 (1,234,567) or Indian 3-2-2 (12,34,567). */
enum class Grouping { WESTERN, INDIAN }

/**
 * The currencies a shopper can pick during onboarding. Catalog prices are
 * authored once in USD cents; each currency carries a fixed, offline rate, so
 * the conversion is part of the fiction, not a live feed (the app never touches
 * the network). Grouping and separators are pinned per currency, so a price
 * reads the local way (₹1,07,817 / R$6.495,00) regardless of device locale, and
 * zero-decimal currencies (KRW, JPY) drop the cents.
 *
 * The roster tracks where this kind of app is spreading: Korea (the origin) and
 * the US first, then the wider no-spend audience (UK, EU, Japan) and the next
 * markets (China, India, Canada, Australia, Brazil). Adding one is a single row.
 */
enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String,
    private val rate: Double,
    private val decimals: Int,
    private val roundTo: Long = 1,
    private val symbolBefore: Boolean = true,
    private val grouping: Grouping = Grouping.WESTERN,
    private val groupSeparator: Char = ',',
    private val decimalSeparator: Char = '.',
) {
    USD("USD", "$", "US Dollar", 1.0, 2),
    EUR("EUR", "€", "Euro", 0.92, 2),
    KRW("KRW", "₩", "South Korean Won", 1_350.0, 0, roundTo = 10),
    GBP("GBP", "£", "British Pound", 0.79, 2),
    JPY("JPY", "¥", "Japanese Yen", 150.0, 0),
    CNY("CNY", "CN¥", "Chinese Yuan", 7.2, 2),
    INR("INR", "₹", "Indian Rupee", 83.0, 2, grouping = Grouping.INDIAN),
    CAD("CAD", "CA$", "Canadian Dollar", 1.36, 2),
    AUD("AUD", "A$", "Australian Dollar", 1.52, 2),
    BRL("BRL", "R$", "Brazilian Real", 5.0, 2, groupSeparator = '.', decimalSeparator = ','),
    ;

    /** Formats a base amount (USD cents) as a price string in this currency. */
    fun format(usdCents: Long): String {
        val amount = usdCents / 100.0 * rate
        val body = if (decimals == 0) {
            var whole = Math.round(amount)
            if (roundTo > 1) whole = (whole + roundTo / 2) / roundTo * roundTo
            group(whole)
        } else {
            val minor = Math.round(amount * 100.0)
            "${group(minor / 100)}$decimalSeparator${"%02d".format(minor % 100)}"
        }
        return if (symbolBefore) "$symbol$body" else "$body $symbol"
    }

    /** Groups the (non-negative) integer part with this currency's separator. */
    private fun group(value: Long): String {
        val digits = value.toString()
        return when {
            digits.length <= 3 -> digits
            grouping == Grouping.INDIAN -> {
                // Last three digits, then the rest in pairs: 1,23,45,678. Reverse
                // so chunking groups from the right, then reverse the whole back.
                val head = digits.dropLast(3)
                val pairs = head.reversed().chunked(2).joinToString(groupSeparator.toString()).reversed()
                "$pairs$groupSeparator${digits.takeLast(3)}"
            }
            else -> digits.reversed().chunked(3).joinToString(groupSeparator.toString()).reversed()
        }
    }

    companion object {
        fun fromCode(code: String?): Currency = entries.firstOrNull { it.code == code } ?: USD
    }
}

/**
 * The currency prices render in right now. Backed by a Compose snapshot state,
 * so the moment it changes every formatted price recomposes on its own, with no
 * currency parameter threaded through the hundreds of call sites. Read by
 * [formatPrice]; set by the ViewModel on load and when the choice changes.
 */
object CurrencyState {
    var active by mutableStateOf(Currency.USD)
}
