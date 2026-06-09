package com.example.myapplication.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ShopViewModel
import com.example.myapplication.data.Order
import com.example.myapplication.data.OrderStatus
import com.example.myapplication.data.formatPrice
import com.example.myapplication.ui.theme.ElectricPurple
import com.example.myapplication.ui.theme.MintGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    viewModel: ShopViewModel,
    orderId: Int,
    onBack: () -> Unit,
    onShopMore: () -> Unit,
) {
    val orders by viewModel.orders.collectAsState()
    val order = orders.firstOrNull { it.id == orderId } ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order #${order.id}") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←", fontSize = 22.sp) }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (order.status == OrderStatus.DELIVERED) {
                DeliveredCelebration(order, onShopMore)
            } else {
                CourierMap(progress = if (order.status >= OrderStatus.ON_THE_WAY) order.progress else 0f)
                EtaCard(order)
            }
            StatusStepper(order)
            ItemsCard(order)
        }
    }
}

@Composable
private fun CourierMap(progress: Float) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Box(Modifier.fillMaxWidth().height(180.dp)) {
            // A "map": a dashed route from the fake store to your real heart.
            Canvas(Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(size.width * 0.08f, size.height * 0.75f)
                    cubicTo(
                        size.width * 0.35f, size.height * 0.15f,
                        size.width * 0.6f, size.height * 1.0f,
                        size.width * 0.92f, size.height * 0.3f,
                    )
                }
                drawPath(
                    path = path,
                    color = ElectricPurple.copy(alpha = 0.5f),
                    style = Stroke(
                        width = 6f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f)),
                    ),
                )
            }
            BoxWithCourier(progress = progress)
            Text(
                "🏪",
                fontSize = 26.sp,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 6.dp, bottom = 24.dp),
            )
            Text(
                "🏠",
                fontSize = 26.sp,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 6.dp, top = 36.dp),
            )
        }
    }
}

@Composable
private fun BoxWithCourier(progress: Float) {
    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize()) {
        val t = progress.coerceIn(0f, 1f)
        // Same cubic Bézier as the canvas path, evaluated at t.
        fun cubic(p0: Float, p1: Float, p2: Float, p3: Float): Float {
            val u = 1 - t
            return u * u * u * p0 + 3 * u * u * t * p1 + 3 * u * t * t * p2 + t * t * t * p3
        }
        val x = cubic(0.08f, 0.35f, 0.6f, 0.92f) * maxWidth.value
        val y = cubic(0.75f, 0.15f, 1.0f, 0.3f) * maxHeight.value
        Text(
            "🛵",
            fontSize = 28.sp,
            modifier = Modifier.offset(x = (x - 14).dp, y = (y - 14).dp),
        )
    }
}

@Composable
private fun EtaCard(order: Order) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text(
                text = "${order.status.emoji} ${order.status.label}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = order.status.detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            if (order.status == OrderStatus.ON_THE_WAY) {
                val secondsLeft = ((1f - order.progress) * 45).toInt() + 1
                Text(
                    text = "Arriving in ~${secondsLeft}s with 0 of your ${order.itemCount} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun StatusStepper(order: Order) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OrderStatus.entries.forEach { status ->
                val reached = order.status >= status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (reached) status.emoji else "⚪",
                        fontSize = 20.sp,
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = status.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (reached) FontWeight.Bold else FontWeight.Normal,
                            color = if (reached) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (reached) {
                            Text(
                                text = status.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemsCard(order: Order) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            order.items.forEach { item ->
                Text(
                    "${item.product.emoji} ${item.product.name} ×${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                )
            }
            Row(Modifier.padding(top = 6.dp)) {
                Text("Money kept", fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(formatPrice(order.totalCents), fontWeight = FontWeight.ExtraBold, color = MintGreen)
            }
        }
    }
}

@Composable
private fun DeliveredCelebration(order: Order, onShopMore: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🧘", fontSize = 64.sp)
            Text(
                text = "Nothing has arrived.",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = "Exactly as planned. Your ${formatPrice(order.totalCents)} is still yours.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(top = 6.dp),
            )
            Button(onClick = onShopMore, modifier = Modifier.padding(top = 16.dp)) {
                Text("Shop the next nothing", fontWeight = FontWeight.Bold)
            }
        }
    }
}
