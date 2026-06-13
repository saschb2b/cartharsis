package com.cartharsis.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.cartharsis.data.FakeCatalog
import com.cartharsis.data.formatPrice
import com.cartharsis.ui.theme.CartharsisTheme

// Real catalog products picked for the states that change a card's look, so
// the previews track the actual data, not invented fixtures.
private val plainProduct =
    FakeCatalog.products.first { it.originalPriceCents == null && it.variantGroup == null && it.name.length < 22 }
private val saleProduct = FakeCatalog.products.first { it.originalPriceCents != null }
private val variantProduct = FakeCatalog.collapseVariants(FakeCatalog.products)
    .first { it.variantGroup != null && FakeCatalog.variantsOf(it.variantGroup!!).size > 2 }
private val longNameProduct = FakeCatalog.products.maxByOrNull { it.name.length }!!

/**
 * ProductCard — the grid card on every browse surface — in the states that
 * change its look: plain, on sale (badge + strikethrough), wishlisted, a
 * variant hint ("N colors"), and a long name (two-line clamp).
 */
@PreviewTest
@Preview(name = "ProductCard, all states", showBackground = true, widthDp = 380)
@Composable
internal fun ProductCardStatesPreview() {
    CartharsisTheme {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProductCard(plainProduct, {}, isWishlisted = false, {}, Modifier.width(168.dp))
                ProductCard(saleProduct, {}, isWishlisted = false, {}, Modifier.width(168.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProductCard(saleProduct, {}, isWishlisted = true, {}, Modifier.width(168.dp))
                ProductCard(variantProduct, {}, isWishlisted = false, {}, Modifier.width(168.dp))
            }
            ProductCard(longNameProduct, {}, isWishlisted = false, {}, Modifier.width(168.dp))
        }
    }
}

/** The cards on a dark surface — the hero-bleed glow and tinted stages
 * need to read against night, not just the cream light theme. */
@PreviewTest
@Preview(
    name = "Cards, dark",
    showBackground = true,
    widthDp = 380,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
internal fun CardsDarkPreview() {
    CartharsisTheme {
        Column(
            Modifier.background(MaterialTheme.colorScheme.background).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProductCard(plainProduct, {}, isWishlisted = false, {}, Modifier.width(168.dp))
                ProductCard(saleProduct, {}, isWishlisted = true, {}, Modifier.width(168.dp))
            }
            MiniProductCard(product = variantProduct, onClick = {})
        }
    }
}

/** MiniProductCard — the horizontal-strip card — plain and on sale. */
@PreviewTest
@Preview(name = "MiniProductCard", showBackground = true, widthDp = 320)
@Composable
internal fun MiniProductCardPreview() {
    CartharsisTheme {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniProductCard(product = plainProduct, onClick = {})
            MiniProductCard(product = saleProduct, onClick = {})
        }
    }
}

/** QuantityStepper at the minimum (minus disabled) and mid-range. */
@PreviewTest
@Preview(name = "QuantityStepper, min + mid", showBackground = true, widthDp = 220)
@Composable
internal fun QuantityStepperPreview() {
    CartharsisTheme {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            QuantityStepper(quantity = 1, onQuantityChange = {})
            QuantityStepper(quantity = 3, onQuantityChange = {})
        }
    }
}

/** The fake "Imagination Express" payment card. */
@PreviewTest
@Preview(name = "Imagination card", showBackground = true, widthDp = 340)
@Composable
internal fun ImaginationCardPreview() {
    CartharsisTheme {
        Column(Modifier.padding(16.dp)) {
            ImaginationCard(cardHolder = "SASCHA BECKER")
        }
    }
}

/** The savings vault on Orders, at low / mid / near-milestone fills. */
@PreviewTest
@Preview(name = "Savings vault, three fills", showBackground = true, widthDp = 360, heightDp = 520)
@Composable
internal fun SavingsVaultPreview() {
    CartharsisTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SavingsVault(centsKept = 6_400)
            SavingsVault(centsKept = 52_000)
            SavingsVault(centsKept = 94_500)
        }
    }
}

/** The home flash-deal promo banner. */
@PreviewTest
@Preview(name = "Flash deal banner", showBackground = true, widthDp = 380)
@Composable
internal fun FlashDealBannerPreview() {
    CartharsisTheme {
        Column(Modifier.padding(12.dp)) {
            FlashDealBanner(
                emoji = saleProduct.emoji,
                name = saleProduct.name,
                price = formatPrice(saleProduct.priceCents),
                originalPrice = saleProduct.originalPriceCents?.let(::formatPrice),
                countdown = "1:42",
                onClick = {},
            )
        }
    }
}
