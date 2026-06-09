package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ShopViewModel
import com.example.myapplication.data.Product
import com.example.myapplication.data.formatPrice
import com.example.myapplication.data.withPriceOverride
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    viewModel: ShopViewModel,
    productId: Int,
    onBack: () -> Unit,
    onProductClick: (Int) -> Unit,
) {
    val base: Product = viewModel.catalog.firstOrNull { it.id == productId } ?: return
    val wishlist by viewModel.wishlist.collectAsState()
    val priceDrops by viewModel.priceDrops.collectAsState()
    val product = base.withPriceOverride(priceDrops[base.id])
    val haptics = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var quantity by remember { mutableIntStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.category) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←", fontSize = 22.sp) }
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedIconButton(onClick = { if (quantity > 1) quantity-- }) { Text("−") }
                Text(
                    text = "$quantity",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                OutlinedIconButton(onClick = { quantity++ }) { Text("+") }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.addToCart(product, quantity)
                        scope.launch {
                            snackbarHostState.showSnackbar("Added to cart. Feel that? That's the good stuff. ✨")
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Add to cart 🛒", fontWeight = FontWeight.Bold)
                }
            }
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
                modifier = Modifier.fillMaxWidth().height(220.dp),
                fontSize = 110,
            )
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = product.tagline,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
                RatingStars(rating = product.rating, reviewCount = product.reviewCount)
                PriceRow(product, big = true)
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "🚚 Free delivery of nothing • 💳 $0.00 at checkout • ↩️ Returns impossible",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                AlsoBoughtRow(
                    viewModel = viewModel,
                    current = base,
                    onProductClick = onProductClick,
                )

                Text(
                    text = "Reviews",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                )
                product.reviews.forEach { review ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = review.author,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = "★".repeat(review.rating),
                                    color = MaterialTheme.colorScheme.tertiary,
                                )
                            }
                            Text(
                                text = review.text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
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
        Text(
            text = "Customers also \"bought\"",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            suggestions.forEach { suggestion ->
                val displayed = viewModel.displayProduct(suggestion)
                Card(
                    modifier = Modifier
                        .width(132.dp)
                        .clickable { onProductClick(suggestion.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    EmojiHero(
                        emoji = displayed.emoji,
                        modifier = Modifier.fillMaxWidth().height(72.dp),
                        fontSize = 36,
                    )
                    Column(Modifier.padding(8.dp)) {
                        Text(
                            text = displayed.name,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 2,
                            minLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = formatPrice(displayed.priceCents),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
