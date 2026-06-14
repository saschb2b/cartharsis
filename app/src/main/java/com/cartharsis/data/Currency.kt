package com.cartharsis.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * The currencies a shopper can pick during onboarding. Catalog prices are
 * authored once in USD cents; each currency carries a fixed, offline rate, so
 * the conversion is part of the fiction, not a live feed (the app never touches
 * the network). KRW drops the decimals and rounds to a tidy figure, the way a
 * real won price reads.
 *
 * Adding a currency is one row here, nothing else changes: [formatPrice] and
 * every price in the app pick it up through [CurrencyState].
 */
enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String,
    private val rate: Double,
    private val decimals: Int,
    private val roundTo: Long = 1,
    private val symbolBefore: Boolean = true,
) {
    USD("USD", "$", "US Dollar", 1.0, 2),
    EUR("EUR", "€", "Euro", 0.92, 2),
    KRW("KRW", "₩", "South Korean Won", 1_350.0, 0, roundTo = 10),
    ;

    /** Formats a base amount (USD cents) as a price string in this currency. */
    fun format(usdCents: Long): String {
        val amount = usdCents / 100.0 * rate
        val body = if (decimals == 0) {
            var whole = Math.round(amount)
            if (roundTo > 1) whole = (whole + roundTo / 2) / roundTo * roundTo
            "%,d".format(Locale.US, whole)
        } else {
            val minor = Math.round(amount * 100.0)
            "%,d.%02d".format(Locale.US, minor / 100, (minor % 100).toInt())
        }
        return if (symbolBefore) "$symbol$body" else "$body $symbol"
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
