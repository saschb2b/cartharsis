package com.cartharsis.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartharsis.ShopViewModel
import com.cartharsis.data.FakeCatalog
import com.cartharsis.data.Product
import com.cartharsis.data.UserReview
import com.cartharsis.data.fakeStockLeft
import com.cartharsis.data.formatOrderDate
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
    // A tapped variant swatch swaps the displayed sibling in place (each is its
    // own listing), so selection lives here rather than re-navigating.
    var selectedId by remember(productId) { mutableIntStateOf(productId) }
    val base: Product = viewModel.catalog.firstOrNull { it.id == selectedId }
        ?: viewModel.catalog.firstOrNull { it.id == productId } ?: return
    val variants = remember(base.variantGroup) {
        base.variantGroup?.let { FakeCatalog.variantsOf(it) } ?: emptyList()
    }
    val wishlist by viewModel.wishlist.collectAsState()
    val priceDrops by viewModel.priceDrops.collectAsState()
    val product = base.withPriceOverride(priceDrops[base.id])
    val haptics = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var quantity by remember { mutableIntStateOf(1) }
    var justAdded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    // Window-space anchors so the rating row can jump to the review section;
    // their difference is scroll-invariant (both move together).
    var contentTopY by remember { mutableFloatStateOf(0f) }
    var reviewsTopY by remember { mutableFloatStateOf(0f) }

    fun jumpToReviews() {
        scope.launch {
            scrollState.animateScrollTo(
                (scrollState.value + reviewsTopY - contentTopY).toInt().coerceAtLeast(0),
            )
        }
    }

    LaunchedEffect(selectedId) { viewModel.markViewed(selectedId) }
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
            NestedTopBar(
                onBack = onBack,
                containerColor = MaterialTheme.colorScheme.background,
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
                .verticalScroll(scrollState)
                .onGloballyPositioned { contentTopY = it.positionInWindow().y },
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClickLabel = "Jump to reviews") {
                        jumpToReviews()
                    },
                ) {
                    RatingStars(rating = product.rating, reviewCount = product.reviewCount)
                    Text(
                        text = "  ›",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

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

                if (variants.size > 1) {
                    VariantPicker(
                        axis = base.variantAxis,
                        variants = variants,
                        selectedId = base.id,
                        onSelect = { id ->
                            if (id != base.id) {
                                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                selectedId = id
                            }
                        },
                    )
                }

                if (product.isBundle) {
                    BundleIncludesCard(product.includes)
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

                FrequentlyBoughtTogether(
                    viewModel = viewModel,
                    current = base,
                    onProductClick = onProductClick,
                    onAddAll = { set ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.addAllToCart(set)
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Added all ${set.size} to cart. Still \$0.00. 🛒",
                                actionLabel = "View cart",
                            )
                            if (result == SnackbarResult.ActionPerformed) onViewCart()
                        }
                    },
                )

                val userReviews by viewModel.userReviews.collectAsState()
                val ownReview = userReviews[product.id]
                var editingReview by remember(productId) { mutableStateOf(false) }
                SectionHeader(
                    title = "Reviews",
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .onGloballyPositioned { reviewsTopY = it.positionInWindow().y },
                    actionLabel = if (ownReview == null && !editingReview) "Write a review" else null,
                    onAction = { editingReview = true },
                )
                RatingSummary(product)
                if (editingReview) {
                    ReviewEditor(
                        initialRating = ownReview?.rating ?: 0,
                        initialText = ownReview?.text ?: "",
                        onCancel = { editingReview = false },
                        onPost = { rating, text ->
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.saveUserReview(product.id, rating, text)
                            editingReview = false
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Posted. Your opinion of nothing is now on record. ⭐",
                                )
                            }
                        },
                    )
                } else if (ownReview != null) {
                    OwnReviewCard(
                        review = ownReview,
                        onEdit = { editingReview = true },
                        onDelete = { viewModel.deleteUserReview(product.id) },
                    )
                }
                var showAllReviews by remember(productId) { mutableStateOf(false) }
                // Top reviews first, like any shop that sorts by helpfulness.
                val rankedReviews = remember(product.id) {
                    product.reviews.mapIndexed { index, review ->
                        Triple(
                            review,
                            reviewAgeLabel(product.id, index),
                            reviewHelpfulCount(product.id, index),
                        )
                    }.sortedByDescending { it.third }
                }
                val visibleReviews =
                    if (showAllReviews) rankedReviews else rankedReviews.take(3)
                visibleReviews.forEach { (review, ageLabel, helpfulCount) ->
                    ReviewCard(
                        author = review.author,
                        rating = review.rating,
                        text = review.text,
                        ageLabel = ageLabel,
                        helpfulCount = helpfulCount,
                    )
                }
                if (product.reviews.size > 3) {
                    TextButton(
                        onClick = { showAllReviews = !showAllReviews },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            if (showAllReviews) {
                                "Show fewer reviews"
                            } else {
                                "Show all ${product.reviews.size} reviews"
                            },
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
private fun BuyBar(quantity: Int, onQuantityChange: (Int) -> Unit, justAdded: Boolean, onAddToCart: () -> Unit) {
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

/** "What's included" — the bundle contents, Amazon's in-the-box list. */
@Composable
private fun BundleIncludesCard(includes: List<String>) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "📦 What's included",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            includes.forEach { line ->
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MintGreen,
                    )
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

/** Amazon-style variant swatch row: "Color: Volcanic Red" + tappable pills. */
@Composable
private fun VariantPicker(axis: String, variants: List<Product>, selectedId: Int, onSelect: (Int) -> Unit) {
    val selectedLabel = variants.firstOrNull { it.id == selectedId }?.variantLabel.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row {
            Text(
                text = "$axis: ",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = selectedLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            variants.forEach { v ->
                val selected = v.id == selectedId
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (selected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    border = BorderStroke(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                    ),
                    modifier = Modifier.clickable(
                        onClickLabel = "Choose ${v.variantLabel}",
                        role = Role.RadioButton,
                        onClick = { onSelect(v.id) },
                    ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        // A color dot only makes sense for the Color axis; an
                        // Edition/Capacity swatch is just its label.
                        if (axis == "Color") {
                            Box(
                                Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(swatchColor(v.variantLabel))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    .padding(end = 8.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = v.variantLabel.orEmpty(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

private fun swatchColor(label: String?): androidx.compose.ui.graphics.Color = when {
    label == null -> androidx.compose.ui.graphics.Color.Gray
    label.contains("Black", true) -> androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    label.contains("Red", true) -> androidx.compose.ui.graphics.Color(0xFFE53935)
    label.contains("Blue", true) -> androidx.compose.ui.graphics.Color(0xFF3949AB)
    label.contains("Silver", true) -> androidx.compose.ui.graphics.Color(0xFFB0BEC5)
    else -> androidx.compose.ui.graphics.Color.Gray
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

/** Star picker + text field — the whole "write a review" surface. */
@Composable
private fun ReviewEditor(
    initialRating: Int,
    initialText: String,
    onCancel: () -> Unit,
    onPost: (Int, String) -> Unit,
) {
    var rating by remember { mutableIntStateOf(initialRating) }
    var text by remember { mutableStateOf(initialText) }
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Your rating",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row {
                (1..5).forEach { star ->
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clickable(onClickLabel = "Rate $star out of 5") { rating = star },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (star <= rating) "★" else "☆",
                            fontSize = 26.sp,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.clearAndSetSemantics {
                                contentDescription =
                                    if (star <= rating) "$star stars, selected" else "$star stars"
                            },
                        )
                    }
                }
            }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What did the nothing mean to you?") },
                minLines = 2,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) { Text("Cancel") }
                Button(onClick = { onPost(rating, text) }, enabled = rating > 0) {
                    Text("Post review")
                }
            }
        }
    }
}

/** The user's own review — pinned above the regulars, editable, deletable. */
@Composable
private fun OwnReviewCard(review: UserReview, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Y",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Column(Modifier.padding(start = 8.dp).weight(1f)) {
                    Text(text = "You", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "Your review",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = "★".repeat(review.rating),
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            if (review.text.isNotBlank()) {
                Text(
                    text = review.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                Text(
                    text = "Posted ${formatOrderDate(review.createdAtMillis)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onEdit) { Text("Edit", style = MaterialTheme.typography.labelMedium) }
                TextButton(onClick = onDelete) { Text("Delete", style = MaterialTheme.typography.labelMedium) }
            }
        }
    }
}

// Pool reviews are shared across products, so per-product metadata (age,
// helpful votes) is derived stably from (product, slot) instead of stored.
private fun reviewAgeLabel(productId: Int, index: Int): String {
    val days = (productId * 13 + index * 31) % 89 + 1
    return if (days < 14) "${days}d ago" else "${days / 7}w ago"
}

private fun reviewHelpfulCount(productId: Int, index: Int): Int = (productId * 37 + index * 53) % 412 + 3

@Composable
private fun ReviewCard(author: String, rating: Int, text: String, ageLabel: String, helpfulCount: Int) {
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

/**
 * "Frequently bought together" — the curated cross-sell, Amazon-style: the
 * product plus its companions, an honest combined total (no fabricated
 * saving), and one tap to add them all.
 */
@Composable
private fun FrequentlyBoughtTogether(
    viewModel: ShopViewModel,
    current: Product,
    onProductClick: (Int) -> Unit,
    onAddAll: (List<Product>) -> Unit,
) {
    val companions = remember(current.id) { FakeCatalog.boughtTogether(current) }
    if (companions.isEmpty()) return
    val set = remember(current.id) { (listOf(current) + companions).map { viewModel.displayProduct(it) } }
    val total = set.sumOf { it.priceCents }

    Column {
        SectionHeader(title = "Frequently bought together", modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            set.forEachIndexed { index, item ->
                if (index > 0) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                MiniProductCard(
                    product = item,
                    // The first tile is the product you're already on.
                    onClick = { if (item.id != current.id) onProductClick(item.id) },
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Total: ",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatPrice(total),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        Button(
            onClick = { onAddAll(set) },
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(top = 8.dp),
        ) {
            Text("Add all ${set.size} to cart", fontWeight = FontWeight.Bold)
        }
    }
}

/** "Customers also bought" — nobody bought anything, but the rabbit hole must go on. */
@Composable
private fun AlsoBoughtRow(viewModel: ShopViewModel, current: Product, onProductClick: (Int) -> Unit) {
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
