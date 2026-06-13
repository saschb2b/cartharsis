package com.cartharsis.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartharsis.ShopViewModel
import com.cartharsis.data.CartItem
import com.cartharsis.data.formatPrice
import com.cartharsis.ui.theme.LocalSavingsColor

@Composable
fun CartScreen(viewModel: ShopViewModel, onCheckout: () -> Unit, onBrowse: () -> Unit) {
    val cart by viewModel.cart.collectAsState()

    if (cart.isEmpty()) {
        EmptyCart(onBrowse)
        return
    }

    val itemCount = cart.sumOf { it.quantity }
    var confirmClear by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Your cart",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = if (itemCount == 1) "1 item" else "$itemCount items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // The concept's "Delete all" — emptying the whole cart at once,
            // confirmed (it's bulk and instant), never confirmshamed.
            TextButton(
                onClick = { confirmClear = true },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text("🗑 Clear", style = MaterialTheme.typography.labelLarge)
            }
        }
        if (confirmClear) {
            AlertDialog(
                onDismissRequest = { confirmClear = false },
                title = { Text("Clear the cart?") },
                text = {
                    Text(
                        if (itemCount == 1) {
                            "Removes the one thing you weren't going to buy anyway."
                        } else {
                            "Removes all $itemCount things you weren't going to buy anyway."
                        },
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearCart()
                            confirmClear = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) {
                        Text("Clear it all")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmClear = false }) {
                        Text("Keep wanting")
                    }
                },
            )
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(cart, key = { it.product.id }) { item ->
                CartLine(item, viewModel)
            }
        }
        SummaryCard(cart, viewModel, onCheckout)
    }
}

@Composable
private fun CartLine(item: CartItem, viewModel: ShopViewModel) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(14.dp))) {
                    EmojiHero(
                        emoji = item.product.emoji,
                        modifier = Modifier.fillMaxSize(),
                        fontSize = 32,
                        seed = item.product.id,
                    )
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        text = item.product.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    PriceRow(item.product)
                    if (item.quantity > 1) {
                        Text(
                            text = "${formatPrice(item.totalCents)} for ${item.quantity}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = { viewModel.removeFromCart(item.product.id) },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    Text("Remove", style = MaterialTheme.typography.labelMedium)
                }
                TextButton(
                    onClick = { viewModel.moveToWishlist(item.product.id) },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    Text("Save for later 💖", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.weight(1f))
                QuantityStepper(
                    quantity = item.quantity,
                    onQuantityChange = { viewModel.setQuantity(item.product.id, it) },
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(cart: List<CartItem>, viewModel: ShopViewModel, onCheckout: () -> Unit) {
    val total = viewModel.cartTotalCents(cart)
    val dealSavings = cart.sumOf { item ->
        item.product.originalPriceCents?.let { (it - item.product.priceCents) * item.quantity } ?: 0L
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SummaryRow("Subtotal", formatPrice(total))
            SummaryRow("Shipping (to nowhere)", "FREE", valueColor = LocalSavingsColor.current)
            if (dealSavings > 0) {
                SummaryRow("Deal savings", "−${formatPrice(dealSavings)}", valueColor = LocalSavingsColor.current)
            }
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(
                    text = formatPrice(total),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Row {
                Text(
                    text = "You will actually pay: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "$0.00",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalSavingsColor.current,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Button(
                onClick = onCheckout,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(50.dp),
            ) {
                Text("Checkout · ${formatPrice(total)}", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmptyCart(onBrowse: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🛒", fontSize = 72.sp)
        Text(
            text = "Your cart is empty",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Your serotonin doesn't have to be.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onBrowse, modifier = Modifier.padding(top = 16.dp)) {
            Text("Go get that dopamine")
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
        )
    }
}
