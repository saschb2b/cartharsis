package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ShopViewModel
import com.example.myapplication.data.formatPrice
import com.example.myapplication.data.withPriceOverride
import com.example.myapplication.ui.theme.ElectricPurple
import com.example.myapplication.ui.theme.HotPink
import com.example.myapplication.ui.theme.JuicyOrange

@Composable
fun HomeScreen(
    viewModel: ShopViewModel,
    onProductClick: (Int) -> Unit,
) {
    val lifetimeStats by viewModel.lifetimeStats.collectAsState()
    val flashDeal by viewModel.flashDeal.collectAsState()
    val secondsLeft by viewModel.flashDealSecondsLeft.collectAsState()
    val wishlist by viewModel.wishlist.collectAsState()
    val priceDrops by viewModel.priceDrops.collectAsState()
    val recentlyViewed by viewModel.recentlyViewed.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    var query by remember { mutableStateOf("") }

    val products = remember(selectedCategory, query, priceDrops) {
        val inCategory =
            if (selectedCategory == "All") viewModel.catalog
            else viewModel.catalog.filter { it.category == selectedCategory }
        val matching =
            if (query.isBlank()) inCategory
            else inCategory.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.tagline.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
            }
        matching.map { it.withPriceOverride(priceDrops[it.id]) }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Text(
                    text = "Cartharsis",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Add to cart. Feel better. Buy nothing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val saved = lifetimeStats.centsKept
                if (saved > 0) {
                    Text(
                        text = "💸 ${formatPrice(saved)} not spent so far",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
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
                shape = RoundedCornerShape(16.dp),
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            FlashDealBanner(
                emoji = flashDeal.emoji,
                name = flashDeal.name,
                price = formatPrice(flashDeal.priceCents),
                originalPrice = flashDeal.originalPriceCents?.let(::formatPrice),
                countdown = formatCountdown(secondsLeft),
                onClick = { onProductClick(flashDeal.id) },
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                viewModel.categories.forEach { category ->
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                    )
                }
            }
        }

        val recentProducts = recentlyViewed.mapNotNull { id -> viewModel.catalog.firstOrNull { it.id == id } }
        if (query.isBlank() && recentProducts.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Text(
                        text = "Keep browsing (you know you want to)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
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
            )
        }
    }
}

@Composable
private fun FlashDealBanner(
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
            .background(Brush.linearGradient(listOf(HotPink, ElectricPurple)))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = emoji, fontSize = 44.sp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "⚡ FLASH DEAL — ends in $countdown",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
            )
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
            )
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
