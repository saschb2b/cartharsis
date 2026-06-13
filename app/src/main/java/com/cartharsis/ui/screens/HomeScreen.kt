package com.cartharsis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartharsis.ShopViewModel
import com.cartharsis.data.FakeCatalog
import com.cartharsis.data.formatPrice
import com.cartharsis.data.homeGreeting
import com.cartharsis.data.homeOrder
import com.cartharsis.data.homeShelves
import com.cartharsis.data.withPriceOverride
import com.cartharsis.ui.theme.ElectricPurple
import com.cartharsis.ui.theme.HotPink
import com.cartharsis.ui.theme.JuicyOrange
import com.cartharsis.ui.theme.MintGreen
import java.util.Calendar
import kotlinx.coroutines.delay

/** Chip icons: a category you can recognize before you can read it. */
private val categoryEmoji = mapOf(
    "All" to "🛍️", "Tech" to "📱", "Audio" to "🎧", "Gaming" to "🎮",
    "Home" to "🛋️", "Kitchen" to "🍳", "Fashion" to "👟", "Beauty" to "✨",
    "Self-Care" to "🧖", "Fitness" to "🏋️", "Snacks" to "🍜", "Outdoors" to "⛺",
    "Pets" to "🐾", "Hobbies" to "🎨", "Stationery" to "🖋️", "Chaos" to "🦖",
)

/** The wall-clock hour the seed was captured at — drives time-of-day greetings. */
private fun hourOfDayFor(seedMillis: Long): Int =
    Calendar.getInstance().apply { timeInMillis = seedMillis }.get(Calendar.HOUR_OF_DAY)

// The flash-deal banner's brushes are constant, so they're allocated once
// rather than rebuilt on every 1 Hz countdown tick.
private val FlashDealGradient = Brush.linearGradient(listOf(HotPink, ElectricPurple))
private val FlashDealGlow =
    Brush.radialGradient(listOf(Color.White.copy(alpha = 0.28f), Color.Transparent))

// Spacing rhythm (internal < external): the grid's spacedBy(10) is the tight
// base that binds items within a group; a section start adds this on top so a
// new "island" (a shelf, the grid) stands ~24dp clear of the one before it.
private val SectionGap = 14.dp

@Composable
fun HomeScreen(viewModel: ShopViewModel, onProductClick: (Int) -> Unit) {
    val lifetimeStats by viewModel.lifetimeStats.collectAsState()
    val flashDeal by viewModel.flashDeal.collectAsState()
    val wishlist by viewModel.wishlist.collectAsState()
    val priceDrops by viewModel.priceDrops.collectAsState()
    val recentlyViewed by viewModel.recentlyViewed.collectAsState()
    val homeSeed by viewModel.homeSeed.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    var query by remember { mutableStateOf("") }

    val products = remember(selectedCategory, query, priceDrops, homeSeed) {
        val inCategory =
            if (selectedCategory == "All") {
                viewModel.catalog
            } else {
                viewModel.catalog.filter { it.category == selectedCategory }
            }
        val matching =
            if (query.isBlank()) {
                inCategory
            } else {
                inCategory.filter {
                    it.name.contains(query, ignoreCase = true) ||
                        it.tagline.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
                }
            }
        val collapsed = FakeCatalog.collapseVariants(matching)
        // Browse surfaces (no active search) reshuffle each open so the grid
        // never leads with the same product twice; search stays stable.
        val ordered = if (query.isBlank()) homeOrder(collapsed, homeSeed) else collapsed
        ordered.map { it.withPriceOverride(priceDrops[it.id]) }
    }

    // Resolve the recently-viewed strip once per id-set change, not on every
    // recomposition (each lookup is a linear catalog scan).
    val recentProducts = remember(recentlyViewed) {
        recentlyViewed.mapNotNull { id -> viewModel.catalog.firstOrNull { it.id == id } }
    }

    // The entrance animation plays once per fresh open, then retires: lazy
    // items are disposed off-screen, so without this flag every scroll back
    // up would replay the staggered fade — animation work (and a flicker)
    // in the middle of a fling.
    var entranceDone by remember(homeSeed) { mutableStateOf(false) }
    LaunchedEffect(homeSeed) {
        delay(1_200)
        entranceDone = true
    }

    // Themed shelves, recomputed only when the seed changes (a fresh open), so
    // they stay put while you browse but renew on every re-open.
    val shelves = remember(homeSeed) {
        homeShelves(
            catalog = viewModel.catalog,
            seed = homeSeed,
            recentlyViewedIds = recentlyViewed,
            wishlistIds = wishlist,
            hourOfDay = hourOfDayFor(homeSeed),
            epochDay = viewModel.todayEpochDayValue,
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            AppearOnce(homeSeed, delayMillis = 0, enabled = !entranceDone) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Cartharsis",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        val greeting = remember(homeSeed) {
                            homeGreeting(homeSeed, hourOfDayFor(homeSeed))
                        }
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (lifetimeStats.centsKept > 0) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MintGreen.copy(alpha = 0.15f),
                        ) {
                            Text(
                                text = "💸 ${formatPrice(lifetimeStats.centsKept)} kept",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                // Distinct top controls breathe at the medium gap (~16dp),
                // not the tight 10dp base used within a group.
                modifier = Modifier.padding(top = 6.dp).fillMaxWidth(),
                placeholder = { Text("Search for things you'll never receive") },
                leadingIcon = { Text("🔍") },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Text("✕", modifier = Modifier.clearAndSetSemantics { contentDescription = "Clear search" })
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
            )
        }

        if (query.isBlank()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.padding(top = 6.dp)) {
                    AppearOnce(homeSeed, delayMillis = 90, enabled = !entranceDone) {
                        val deal = flashDeal.withPriceOverride(priceDrops[flashDeal.id])
                        // The 1Hz countdown is collected here, inside the banner
                        // item, so each tick recomposes one row — not the whole
                        // scrolling grid.
                        val secondsLeft by viewModel.flashDealSecondsLeft.collectAsState()
                        FlashDealBanner(
                            emoji = deal.emoji,
                            name = deal.name,
                            price = formatPrice(deal.priceCents),
                            originalPrice = deal.originalPriceCents?.let(::formatPrice),
                            countdown = formatCountdown(secondsLeft),
                            onClick = { onProductClick(deal.id) },
                        )
                    }
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                viewModel.categories.forEach { category ->
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = { selectedCategory = category },
                        shape = RoundedCornerShape(50),
                        label = {
                            Text(categoryEmoji[category]?.let { "$it $category" } ?: category)
                        },
                    )
                }
            }
        }

        if (query.isBlank() && recentProducts.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.padding(top = SectionGap)) {
                    // Header binds down to its row (8dp), clear of what's above.
                    SectionHeader(
                        title = "Keep browsing",
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        recentProducts.forEach { recent ->
                            MiniProductCard(
                                product = viewModel.displayProduct(recent),
                                onClick = { onProductClick(recent.id) },
                            )
                        }
                    }
                }
            }
        }

        // Themed shelves — the freshness engine, default browse view only.
        if (query.isBlank() && selectedCategory == "All") {
            itemsIndexed(shelves, span = { _, _ ->
                GridItemSpan(maxLineSpan)
            }, key = { _, s -> "shelf-${s.title}" }) { index, shelf ->
                AppearOnce(homeSeed, delayMillis = 150 + index * 70, enabled = !entranceDone) {
                    Column(Modifier.animateItem().padding(top = SectionGap)) {
                        SectionHeader(
                            title = shelf.title,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            shelf.products.forEach { p ->
                                MiniProductCard(
                                    product = viewModel.displayProduct(p),
                                    onClick = { onProductClick(p.id) },
                                )
                            }
                        }
                    }
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            // The grid is its own island: a section gap above, then the rows
            // follow at the tight base spacing so the header binds to them.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = SectionGap),
            ) {
                Text(
                    text = when {
                        query.isNotBlank() -> "Results"
                        selectedCategory == "All" -> "Everything"
                        else -> selectedCategory
                    },
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = if (products.size == 1) "1 item" else "${products.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (products.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("🔍", fontSize = 48.sp)
                    Text(
                        text = "Nothing matches \"$query\"",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(
                        text = "Which is fitting, because nothing is what we sell.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        items(products, key = { it.id }) { product ->
            ProductCard(
                product = product,
                onClick = { onProductClick(product.id) },
                isWishlisted = product.id in wishlist,
                onToggleWishlist = { viewModel.toggleWishlist(product.id) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
internal fun FlashDealBanner(
    emoji: String,
    name: String,
    price: String,
    originalPrice: String?,
    countdown: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(FlashDealGradient)
            .clickable(onClick = onClick, onClickLabel = "View the flash deal")
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The art runs oversized and breaks past a soft spotlight glow — the
        // same hero-bleed the product cards use, so the deal feels alive.
        Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(72.dp)
                    .background(FlashDealGlow, CircleShape),
            )
            Text(text = emoji, fontSize = 60.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⚡ FLASH DEAL",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f),
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(start = 6.dp),
                ) {
                    Text(
                        text = countdown,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                    )
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
                originalPrice?.let {
                    Text(
                        text = "  $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                }
            }
        }
        // A real gap before the CTA so the name never butts against it.
        Spacer(Modifier.width(12.dp))
        Surface(color = JuicyOrange, shape = RoundedCornerShape(12.dp)) {
            Text(
                text = "GRAB IT",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}
