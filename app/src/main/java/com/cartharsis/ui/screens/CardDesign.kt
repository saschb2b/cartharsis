package com.cartharsis.ui.screens

import androidx.compose.ui.graphics.Color
import com.cartharsis.ui.theme.ElectricPurple
import com.cartharsis.ui.theme.HotPink
import com.cartharsis.ui.theme.JuicyOrange
import com.cartharsis.ui.theme.LemonYellow
import com.cartharsis.ui.theme.MintGreen
import com.cartharsis.ui.theme.SkyBlue
import kotlin.random.Random

/**
 * One generated look for the Imagination Express card: an invented payment
 * network (fake-adjacent, never a real brand — house rule), a fake number whose
 * lead digit nods at the network's real-world range, and a gradient theme.
 * Derived deterministically from a seed, so the same seed always draws the same
 * card and every screen agrees on it.
 */
data class CardDesign(val network: String, val number: String, val gradient: List<Color>)

private data class CardNetwork(val name: String, val lead: Int)

// Invented networks. The lead digit echoes the real card-number ranges (4 = the
// blue one, 5 = the two-circles one, 3 = travel, 6 = the orange one) as a wink.
private val cardNetworks = listOf(
    CardNetwork("MASTERMIND", 5),
    CardNetwork("VISTA", 4),
    CardNetwork("NOMADO", 3),
    CardNetwork("DISCOVERIE", 6),
    CardNetwork("PLUSH", 5),
    CardNetwork("ZAPCASH", 4),
)

private val cardGradients = listOf(
    listOf(ElectricPurple, HotPink),
    listOf(JuicyOrange, HotPink),
    listOf(SkyBlue, ElectricPurple),
    listOf(MintGreen, SkyBlue),
    listOf(LemonYellow, JuicyOrange),
    listOf(HotPink, ElectricPurple, SkyBlue),
    listOf(Color(0xFF2B2B3C), Color(0xFF52527A)),
    listOf(Color(0xFF0E7C66), Color(0xFF24C39E)),
)

fun cardDesignFromSeed(seed: Long): CardDesign {
    val rng = Random(seed)
    val network = cardNetworks[rng.nextInt(cardNetworks.size)]
    val gradient = cardGradients[rng.nextInt(cardGradients.size)]
    val number = buildString {
        append("%d%03d".format(network.lead, rng.nextInt(1000)))
        repeat(3) {
            append("  ")
            append("%04d".format(rng.nextInt(10000)))
        }
    }
    return CardDesign(network.name, number, gradient)
}
