package com.cartharsis.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartharsis.ShopViewModel
import com.cartharsis.data.Product
import com.cartharsis.data.fakeStockLeft
import com.cartharsis.data.formatPrice
import com.cartharsis.data.withPriceOverride
import com.cartharsis.ui.theme.JuicyOrange
import com.cartharsis.ui.theme.MintGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** One per add, at random — the ritual deserves fresh applause. */
private val cartSnackLines = listOf(
    "Added to cart. Feel that? That's the good stuff. ✨",
    "In the cart. Your serotonin says thank you. 💆",
    "Cart +1. Wallet ±0. Perfect balance. ⚖️",
    "Secured. The void ships shortly. 📦",
    "Excellent taste. It costs exactly nothing. 💸",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    viewModel: ShopViewModel,
    productId: Int,
    onBack: () -> Unit,
    onProductClick: (Int) -> Unit,
    onBuyNow: () -> Unit,
    onViewCart: () -> Unit = {},
) {
    val base: Product = viewModel.catalog.firstOrNull { it.id == productId } ?: return
    val wishlist by viewModel.wishlist.collectAsState()
    val priceDrops by viewModel.priceDrops.collectAsState()
    val product = base.withPriceOverride(priceDrops[base.id])
    val haptics = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var quantity by remember { mutableIntStateOf(1) }
    var justAdded by remember { mutableStateOf(false) }

    LaunchedEffect(productId) { viewModel.markViewed(productId) }
    LaunchedEffect(justAdded) {
        if (justAdded) {
            delay(1_500)
            justAdded = false
        }
    }

    fun addToCart() {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        viewModel.addToCart(product, quantity)
        justAdded = true
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = cartSnackLines.random(),
                actionLabel = "View cart",
            )
            if (result == SnackbarResult.ActionPerformed) onViewCart()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(
                            "←",
                            fontSize = 22.sp,
                            modifier = Modifier.clearAndSetSemantics { contentDescription = "Back" },
                        )
                    }
                },
                actions = {
                    WishHeart(
                        isWishlisted = product.id in wishlist,
                        onToggle = { viewModel.toggleWishlist(product.id) },
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BuyBar(
                quantity = quantity,
                onQuantityChange = { quantity = it },
                justAdded = justAdded,
                onAddToCart = ::addToCart,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            EmojiHero(
                emoji = product.emoji,
                modifier = Modifier.fillMaxWidth().height(250.dp),
                fontSize = 120,
                seed = product.id,
            )
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = product.category.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = product.tagline,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                RatingStars(rating = product.rating, reviewCount = product.reviewCount)

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    PriceRow(product, big = true)
                    product.originalPriceCents?.let { original ->
                        Text(
                            text = "You save ${formatPrice(original - product.priceCents)} " +
                                "(and also the other ${formatPrice(product.priceCents)})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MintGreen,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    product.fakeStockLeft?.let { left ->
                        Text(
                            text = "Only $left left in stock, allegedly",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = JuicyOrange,
                        )
                    }
                }

                DeliveryCard()

                FilledTonalButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.addToCart(product, quantity)
                        onBuyNow()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Text("Buy now — straight to checkout", fontWeight = FontWeight.Bold)
                }

                SectionHeader(title = "About this item", modifier = Modifier.padding(top = 8.dp))
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SpecRows(product)

                SectionHeader(title = "Reviews", modifier = Modifier.padding(top = 8.dp))
                RatingSummary(product)
                var showAllReviews by remember(productId) { mutableStateOf(false) }
                val visibleReviews =
                    if (showAllReviews) product.reviews else product.reviews.take(3)
                visibleReviews.forEachIndexed { index, review ->
                    ReviewCard(
                        author = review.author,
                        rating = review.rating,
                        text = review.text,
                        ageLabel = reviewAgeLabel(product.id, index),
                        helpfulCount = reviewHelpfulCount(product.id, index),
                    )
                }
                if (product.reviews.size > 3) {
                    TextButton(
                        onClick = { showAllReviews = !showAllReviews },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            if (showAllReviews) "Show fewer reviews"
                            else "Show all ${product.reviews.size} reviews",
                        )
                    }
                }

                AlsoBoughtRow(
                    viewModel = viewModel,
                    current = base,
                    onProductClick = onProductClick,
                )
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

/** Sticky buy bar: the purchase is always one thumb-reach away. */
@Composable
private fun BuyBar(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    justAdded: Boolean,
    onAddToCart: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuantityStepper(quantity = quantity, onQuantityChange = onQuantityChange)
            Button(
                onClick = onAddToCart,
                modifier = Modifier.weight(1f).height(48.dp),
            ) {
                Crossfade(targetState = justAdded, label = "addToCart") { added ->
                    Text(
                        text = if (added) "Added ✓" else "Add to cart",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun DeliveryCard() {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            DeliveryLine("🚚", DELIVERY_PROMISE)
            DeliveryLine("💳", "$0.00 at checkout — Imagination Express accepted")
            DeliveryLine("🧘", "Returns unnecessary; nothing will arrive")
        }
    }
}

@Composable
private fun DeliveryLine(emoji: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 16.sp)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun SpecRows(product: Product) {
    Column {
        SpecRow("Category", product.category)
        SpecRow(
            "Availability",
            product.fakeStockLeft?.let { "Only $it left (imaginary)" } ?: "In stock (imaginary)",
        )
        SpecRow("Shipping", "Free, everywhere, instantly-ish")
        SpecRow("Weight", "0g as delivered")
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Column {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(110.dp),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

/**
 * Big number + distribution bars. The distribution is synthesized from the
 * (fake) average so it always tells a coherent story.
 */
@Composable
private fun RatingSummary(product: Product) {
    val shares = remember(product.id) { ratingDistribution(product.rating) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${product.rating}",
                style = MaterialTheme.typography.headlineMedium,
            )
            RatingStars(rating = product.rating)
            Text(
                text = "%,d ratings".format(product.reviewCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            shares.forEachIndexed { index, share ->
                val stars = 5 - index
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = "$stars star: ${(share * 100).toInt()} percent"
                    },
                ) {
                    Text(
                        text = "$stars",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.width(12.dp),
                    )
                    LinearProgressIndicator(
                        progress = { share },
                        modifier = Modifier.weight(1f).height(8.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                        gapSize = 0.dp,
                        drawStopIndicator = {},
                    )
                    Text(
                        text = "${(share * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(36.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    )
                }
            }
        }
    }
}

/** Five shares (5★ first) that sum to ~1 and match the average's vibe. */
private fun ratingDistribution(rating: Double): List<Float> {
    val top = (((rating - 3.4) / 1.6).coerceIn(0.45, 0.9)).toFloat()
    val rest = 1f - top
    return listOf(top, rest * 0.55f, rest * 0.25f, rest * 0.12f, rest * 0.08f)
}

// Pool reviews are shared across products, so per-product metadata (age,
// helpful votes) is derived stably from (product, slot) instead of stored.
private fun reviewAgeLabel(productId: Int, index: Int): String {
    val days = (productId * 13 + index * 31) % 89 + 1
    return if (days < 14) "${days}d ago" else "${days / 7}w ago"
}

private fun reviewHelpfulCount(productId: Int, index: Int): Int =
    (productId * 37 + index * 53) % 412 + 3

@Composable
private fun ReviewCard(
    author: String,
    rating: Int,
    text: String,
    ageLabel: String,
    helpfulCount: Int,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = author.first().uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
                Column(Modifier.padding(start = 8.dp).weight(1f)) {
                    Text(
                        text = author,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = "✓ Verified non-buyer",
                        style = MaterialTheme.typography.labelSmall,
                        color = MintGreen,
                    )
                }
                Text(
                    text = "★".repeat(rating),
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "$ageLabel · $helpfulCount found this helpful",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

/** "Customers also bought" — nobody bought anything, but the rabbit hole must go on. */
@Composable
private fun AlsoBoughtRow(
    viewModel: ShopViewModel,
    current: Product,
    onProductClick: (Int) -> Unit,
) {
    val suggestions = remember(current.id) {
        val sameCategory = viewModel.catalog
            .filter { it.id != current.id && it.category == current.category }
        val filler = viewModel.catalog
            .filter { it.id != current.id && it.category != current.category }
            .sortedByDescending { it.rating }
        (sameCategory + filler).take(6)
    }
    if (suggestions.isEmpty()) return

    Column {
        SectionHeader(
            title = "Customers also \"bought\"",
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            suggestions.forEach { suggestion ->
                MiniProductCard(
                    product = viewModel.displayProduct(suggestion),
                    onClick = { onProductClick(suggestion.id) },
                )
            }
        }
    }
}
