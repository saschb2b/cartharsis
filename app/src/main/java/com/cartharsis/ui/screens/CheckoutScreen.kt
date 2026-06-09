package com.cartharsis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartharsis.ShopViewModel
import com.cartharsis.data.formatPrice
import com.cartharsis.ui.theme.ElectricPurple
import com.cartharsis.ui.theme.HotPink
import com.cartharsis.ui.theme.MintGreen
import kotlinx.coroutines.delay

private sealed interface CheckoutPhase {
    data object Form : CheckoutPhase
    data object Processing : CheckoutPhase
    data class Success(val orderId: Int) : CheckoutPhase
}

@Composable
fun CheckoutScreen(
    viewModel: ShopViewModel,
    onTrackOrder: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val cart by viewModel.cart.collectAsState()
    var phase by remember { mutableStateOf<CheckoutPhase>(CheckoutPhase.Form) }
    val haptics = LocalHapticFeedback.current

    when (val p = phase) {
        is CheckoutPhase.Form -> {
            if (cart.isEmpty()) {
                LaunchedEffect(Unit) { onBack() }
                return
            }
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Checkout",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("📍 Deliver to", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text("Your Imagination", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Apt ∞, Anticipation Street, Dopamine City",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // The "payment method". Balance: infinite, by definition.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.linearGradient(listOf(ElectricPurple, HotPink)))
                        .padding(18.dp),
                ) {
                    Text(
                        "IMAGINATION EXPRESS",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(Modifier.height(18.dp))
                    Text(
                        "••••  ••••  ••••  0000",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row {
                        Text("BALANCE", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Spacer(Modifier.weight(1f))
                        Text("∞", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        cart.forEach { item ->
                            Row {
                                Text("${item.product.emoji} ${item.product.name} ×${item.quantity}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                )
                                Text(formatPrice(item.totalCents), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        HorizontalDivider(Modifier.padding(vertical = 6.dp))
                        Row {
                            Text("Total", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            Text(formatPrice(viewModel.cartTotalCents(cart)), fontWeight = FontWeight.Bold)
                        }
                        Row {
                            Text("Charged to your card", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            Text("$0.00", fontWeight = FontWeight.ExtraBold, color = MintGreen)
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        phase = CheckoutPhase.Processing
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Place order — pay nothing 🎉", fontWeight = FontWeight.Bold)
                }
            }
        }

        is CheckoutPhase.Processing -> {
            LaunchedEffect(Unit) {
                delay(1_800)
                val orderId = viewModel.placeOrder()
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                phase = CheckoutPhase.Success(orderId)
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Not charging your card…",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }

        is CheckoutPhase.Success -> {
            Box(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("🎉", fontSize = 84.sp)
                    Text(
                        text = "Order placed!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "You just bought everything you wanted\nand spent absolutely nothing.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Button(
                        onClick = { onTrackOrder(p.orderId) },
                        modifier = Modifier.padding(top = 24.dp),
                    ) {
                        Text("Track your nothing 🚚", fontWeight = FontWeight.Bold)
                    }
                }
                ConfettiOverlay(Modifier.fillMaxSize())
            }
        }
    }
}
