package com.cartharsis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.cartharsis.ui.theme.CartharsisTheme

/** Fractional star fills, the iteration surface for the rating polish. */
@PreviewTest
@Preview(name = "Rating stars, fractional", showBackground = true, widthDp = 280)
@Composable
internal fun RatingStarsPreview() {
    CartharsisTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(5.0, 4.8, 4.6, 4.0, 3.5, 2.7).forEach { r ->
                RatingStars(rating = r, reviewCount = (r * 3210).toInt())
            }
            Text("(empty / partial / full fills above)")
        }
    }
}

/** The PDP hero-image bleed — art overflowing its tinted stage. */
@PreviewTest
@Preview(name = "Product hero bleed", showBackground = true, widthDp = 380, heightDp = 360)
@Composable
internal fun ProductHeroPreview() {
    CartharsisTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.background)) {
            ProductHero(emoji = "🕶️", seed = 3)
            Text("Product name sits here", Modifier.padding(start = 20.dp))
        }
    }
}

/** A second product so the per-seed tint + a tall emoji both get checked. */
@PreviewTest
@Preview(name = "Product hero bleed, headphones", showBackground = true, widthDp = 380, heightDp = 360)
@Composable
internal fun ProductHeroHeadphonesPreview() {
    CartharsisTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.background)) {
            ProductHero(emoji = "🎧", seed = 1)
            Text("Product name sits here", Modifier.padding(start = 20.dp))
        }
    }
}

/**
 * The cart order summary, the ledger that must reconcile: subtotal at list
 * price, then FREE shipping and deal savings deduct down to the total. Top card
 * has a deal ($105.98 − $10.00 = $95.98); bottom has none (subtotal == total,
 * no savings line).
 */
@PreviewTest
@Preview(name = "Order summary, deal + none", showBackground = true, widthDp = 380)
@Composable
internal fun OrderSummaryPreview() {
    CartharsisTheme {
        Column {
            OrderSummary(
                subtotalCents = 10_598,
                dealSavingsCents = 1_000,
                totalCents = 9_598,
                onCheckout = {},
            )
            OrderSummary(
                subtotalCents = 4_299,
                dealSavingsCents = 0,
                totalCents = 4_299,
                onCheckout = {},
            )
        }
    }
}
