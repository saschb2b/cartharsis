package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ShopViewModel
import com.example.myapplication.data.formatPrice
import com.example.myapplication.ui.theme.MintGreen

@Composable
fun CartScreen(
    viewModel: ShopViewModel,
    onCheckout: () -> Unit,
    onBrowse: () -> Unit,
) {
    val cart by viewModel.cart.collectAsState()

    if (cart.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("🛒", fontSize = 72.sp)
            Text(
                text = "Your cart is empty",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
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
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Your cart",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp),
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(cart, key = { it.product.id }) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))) {
                            EmojiHero(item.product.emoji, Modifier.fillMaxSize(), fontSize = 30)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = item.product.name,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatPrice(item.product.priceCents),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (item.quantity > 1) {
                                    Text(
                                        text = " · ${formatPrice(item.totalCents)} total",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            TextButton(
                                onClick = { viewModel.removeFromCart(item.product.id) },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            ) {
                                Text("Remove", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        OutlinedIconButton(onClick = {
                            viewModel.setQuantity(item.product.id, item.quantity - 1)
                        }) {
                            Text("−", modifier = Modifier.clearAndSetSemantics { contentDescription = "Decrease quantity" })
                        }
                        Text(
                            text = "${item.quantity}",
                            modifier = Modifier.padding(horizontal = 10.dp),
                            fontWeight = FontWeight.Bold,
                        )
                        OutlinedIconButton(onClick = {
                            viewModel.setQuantity(item.product.id, item.quantity + 1)
                        }) {
                            Text("+", modifier = Modifier.clearAndSetSemantics { contentDescription = "Increase quantity" })
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val total = viewModel.cartTotalCents(cart)
                SummaryRow("Subtotal", formatPrice(total))
                SummaryRow("Shipping (to nowhere)", "FREE")
                SummaryRow("Dopamine fee", "$0.00")
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                SummaryRow("Total", formatPrice(total), bold = true)
                Row {
                    Text(
                        text = "You will actually pay: ",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = "$0.00",
                        style = MaterialTheme.typography.titleSmall,
                        color = MintGreen,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Checkout — risk-free, everything-free", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
