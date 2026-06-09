package com.cartharsis.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartharsis.data.Product
import com.cartharsis.data.formatPrice
import com.cartharsis.ui.theme.CartharsisTheme
import com.cartharsis.ui.theme.ElectricPurple
import com.cartharsis.ui.theme.HotPink
import com.cartharsis.ui.theme.JuicyOrange
import com.cartharsis.ui.theme.LemonYellow
import com.cartharsis.ui.theme.MintGreen
import com.cartharsis.ui.theme.SkyBlue
import kotlin.math.abs

/** "12,473" is for receipts; cards say "12.5k". */
fun formatCompactCount(n: Int): String = when {
    n < 1_000 -> "$n"
    n < 100_000 -> {
        val hundreds = n / 100
        if (hundreds % 10 == 0) "${hundreds / 10}k" else "${hundreds / 10}.${hundreds % 10}k"
    }
    else -> "${n / 1_000}k"
}

/** Full star row — the product page treatment. */
@Composable
fun RatingStars(rating: Double, reviewCount: Int? = null) {
    val description = "Rated $rating out of 5" +
        (reviewCount?.let { ", %,d reviews".format(it) } ?: "")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clearAndSetSemantics { contentDescription = description },
    ) {
        val full = rating.toInt()
        Text(
            text = "★".repeat(full) + "☆".repeat(5 - full),
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = " $rating" + (reviewCount?.let { " (%,d)".format(it) } ?: ""),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Compact one-line rating for cards: "★ 4.8 · 12.5k". */
@Composable
fun RatingBadge(rating: Double, reviewCount: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = "Rated $rating out of 5, %,d reviews".format(reviewCount)
        },
    ) {
        Text(
            text = "★",
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = " $rating",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = " · ${formatCompactCount(reviewCount)}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * The price is the boldest thing in any block it appears in; the accent
 * palette stays reserved for CTAs and savings, so price reads in ink.
 */
@Composable
fun PriceRow(product: Product, big: Boolean = false, showDiscountBadge: Boolean = true) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = formatPrice(product.priceCents),
            style = if (big) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        product.originalPriceCents?.let { original ->
            Text(
                text = " " + formatPrice(original),
                style = MaterialTheme.typography.bodySmall,
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (showDiscountBadge) {
                DiscountBadge(
                    percent = product.discountPercent ?: 0,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
    }
}

@Composable
fun DiscountBadge(percent: Int, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.tertiary,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier,
    ) {
        Text(
            text = "-$percent%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
        )
    }
}

// Soft tile gradients, assigned stably per product so the grid reads colorful
// but each product keeps its own "packaging" everywhere it appears.
private val heroGradients = listOf(
    listOf(HotPink, ElectricPurple),
    listOf(ElectricPurple, SkyBlue),
    listOf(JuicyOrange, HotPink),
    listOf(MintGreen, SkyBlue),
    listOf(LemonYellow, JuicyOrange),
    listOf(SkyBlue, ElectricPurple),
)

/** Big emoji on a soft per-product gradient — the entire "product photography" budget. */
@Composable
fun EmojiHero(emoji: String, modifier: Modifier = Modifier, fontSize: Int = 64, seed: Int = 0) {
    val colors = heroGradients[abs(seed) % heroGradients.size].map { it.copy(alpha = 0.14f) }
    Box(
        modifier = modifier.background(Brush.linearGradient(colors)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = fontSize.sp)
    }
}

/** Section title with an optional trailing action — one header style everywhere. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

/**
 * Shared quantity stepper: a quiet pill, 48dp touch targets, the count
 * animates its width so multi-digit quantities don't jump the layout.
 */
@Composable
fun QuantityStepper(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 1,
) {
    Surface(
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepButton(
                glyph = "−",
                label = "Decrease quantity",
                enabled = quantity > min,
                onClick = { onQuantityChange(quantity - 1) },
            )
            Text(
                text = "$quantity",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .defaultMinSize(minWidth = 20.dp)
                    .animateContentSize(),
            )
            StepButton(
                glyph = "+",
                label = "Increase quantity",
                enabled = true,
                onClick = { onQuantityChange(quantity + 1) },
            )
        }
    }
}

@Composable
private fun StepButton(glyph: String, label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clickable(enabled = enabled, onClick = onClick, role = Role.Button, onClickLabel = label),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = glyph,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.clearAndSetSemantics { contentDescription = label },
        )
    }
}

/**
 * Grid card. Fixed attribute set in a fixed order — image, name, rating,
 * price — because scanning a grid is comparison work and consistency is
 * what makes it effortless.
 */
@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    isWishlisted: Boolean,
    onToggleWishlist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            Box {
                EmojiHero(
                    emoji = product.emoji,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    seed = product.id,
                )
                product.discountPercent?.let {
                    DiscountBadge(percent = it, modifier = Modifier.padding(8.dp))
                }
                WishHeart(
                    isWishlisted = isWishlisted,
                    onToggle = onToggleWishlist,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                RatingBadge(rating = product.rating, reviewCount = product.reviewCount)
                // The image badge already announces the deal; one badge per card.
                PriceRow(product, showDiscountBadge = false)
            }
        }
    }
}

/** Small tappable product card for horizontal suggestion strips. */
@Composable
fun MiniProductCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(132.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        EmojiHero(
            emoji = product.emoji,
            modifier = Modifier.fillMaxWidth().height(76.dp),
            fontSize = 36,
            seed = product.id,
        )
        Column(Modifier.padding(10.dp)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatPrice(product.priceCents),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/** Heart toggle — wanting things is free, and we keep it that way. */
@Composable
fun WishHeart(isWishlisted: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable(
                onClick = onToggle,
                role = Role.Checkbox,
                onClickLabel = if (isWishlisted) "Remove from wishlist" else "Add to wishlist",
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isWishlisted) "❤️" else "🤍",
            fontSize = 20.sp,
            modifier = Modifier.clearAndSetSemantics {
                contentDescription = if (isWishlisted) "Wishlisted" else "Not wishlisted"
            },
        )
    }
}

/** The one delivery promise we can make with a straight face. */
const val DELIVERY_PROMISE = "Free delivery · arrives ~1 minute after checkout"

/**
 * A money string that ticks up to its value — cumulative numbers feel earned
 * when you watch them arrive. Whole dollars only; the cents never mattered.
 */
@Composable
fun animatedDollars(cents: Long, delayMillis: Int = 0): String {
    val animated by androidx.compose.animation.core.animateIntAsState(
        targetValue = (cents / 100).toInt(),
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 900,
            delayMillis = delayMillis,
        ),
        label = "dollars",
    )
    return "$%,d".format(animated)
}

/** A plain count that ticks up to its value. */
@Composable
fun animatedCount(value: Int): String {
    val animated by androidx.compose.animation.core.animateIntAsState(
        targetValue = value,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 700),
        label = "count",
    )
    return "$animated"
}

fun formatCountdown(totalSeconds: Int): String = "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)

private val previewProduct = Product(
    id = 0,
    name = "Preview Phone Ultra Max",
    emoji = "📱",
    tagline = "Only renders, never ships.",
    description = "A product that exists exclusively inside Android Studio.",
    priceCents = 129_900,
    category = "Tech",
    rating = 4.8,
    reviewCount = 12_473,
    reviews = emptyList(),
    originalPriceCents = 159_900,
)

@Preview(showBackground = true)
@Composable
private fun ProductCardPreview() {
    CartharsisTheme {
        ProductCard(
            product = previewProduct,
            onClick = {},
            isWishlisted = true,
            onToggleWishlist = {},
            modifier = Modifier.width(180.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MiniProductCardPreview() {
    CartharsisTheme {
        MiniProductCard(product = previewProduct, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun QuantityStepperPreview() {
    CartharsisTheme {
        QuantityStepper(quantity = 2, onQuantityChange = {})
    }
}
