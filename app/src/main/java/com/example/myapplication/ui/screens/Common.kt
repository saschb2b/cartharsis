package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.Product
import com.example.myapplication.data.formatPrice
import com.example.myapplication.ui.theme.ElectricPurple
import com.example.myapplication.ui.theme.HotPink

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

@Composable
fun PriceRow(product: Product, big: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = formatPrice(product.priceCents),
            style = if (big) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        product.originalPriceCents?.let { original ->
            Text(
                text = " " + formatPrice(original),
                style = MaterialTheme.typography.bodySmall,
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                color = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.padding(start = 6.dp),
            ) {
                Text(
                    text = "-${product.discountPercent}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                )
            }
        }
    }
}

/** Big emoji on a soft gradient — the entire "product photography" budget. */
@Composable
fun EmojiHero(emoji: String, modifier: Modifier = Modifier, fontSize: Int = 64) {
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    listOf(HotPink.copy(alpha = 0.12f), ElectricPurple.copy(alpha = 0.12f))
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = fontSize.sp)
    }
}

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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            Box {
                EmojiHero(emoji = product.emoji, modifier = Modifier.fillMaxWidth().height(110.dp))
                WishHeart(
                    isWishlisted = isWishlisted,
                    onToggle = onToggleWishlist,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                RatingStars(rating = product.rating, reviewCount = product.reviewCount)
                PriceRow(product)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        EmojiHero(
            emoji = product.emoji,
            modifier = Modifier.fillMaxWidth().height(72.dp),
            fontSize = 36,
        )
        Column(Modifier.padding(8.dp)) {
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
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
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

fun formatCountdown(totalSeconds: Int): String = "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
