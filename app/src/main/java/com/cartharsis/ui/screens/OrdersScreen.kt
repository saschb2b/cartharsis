package com.cartharsis.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartharsis.ShopViewModel
import com.cartharsis.data.formatOrderDate
import com.cartharsis.data.formatPrice
import com.cartharsis.data.keptEquivalent
import com.cartharsis.data.keptInCoffees
import com.cartharsis.data.nextSavingsMilestone
import com.cartharsis.data.savingsMilestoneProgress
import com.cartharsis.ui.theme.ElectricPurple
import com.cartharsis.ui.theme.HotPink
import com.cartharsis.ui.theme.MintGreen

@Composable
fun OrdersScreen(viewModel: ShopViewModel, onOrderClick: (Int) -> Unit) {
    val orders by viewModel.orders.collectAsState()
    val streakDays by viewModel.streakDays.collectAsState()
    val stats by viewModel.lifetimeStats.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Your impact",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 2.dp),
            )
        }

        item { KeptHero(centsKept = stats.centsKept) }

        if (stats.centsKept > 0) {
            item { SavingsVault(centsKept = stats.centsKept) }
        }

        item {
            SecondaryStats(
                ordersPlaced = stats.ordersPlaced,
                itemsBought = stats.itemsBought,
                streakDays = streakDays,
            )
        }

        if (orders.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("🗃️", fontSize = 56.sp)
                    Text(
                        text = "No orders this session",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(
                        text = "Place one. It's free. It's literally nothing.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            item {
                Text(
                    text = "Your nothing, delivered",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        items(orders, key = { it.id }) { order ->
            OrderCard(order, onOrderClick)
        }
    }
}

/** The hero: one giant count-up number that is the whole emotional point. */
@Composable
private fun KeptHero(centsKept: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(HotPink, ElectricPurple)))
            .padding(horizontal = 20.dp, vertical = 22.dp),
    ) {
        Column {
            Text(
                text = "MONEY YOU KEPT",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.85f),
            )
            // Full-width and auto-sizing so even a seven-figure total never
            // clips on a narrow phone; the hero owns its own row instead of
            // fighting for a cramped grid cell.
            BasicText(
                text = animatedDollars(centsKept, delayMillis = 150),
                style = TextStyle(color = Color.White, fontWeight = FontWeight.ExtraBold),
                maxLines = 1,
                softWrap = false,
                autoSize = TextAutoSize.StepBased(minFontSize = 28.sp, maxFontSize = 52.sp, stepSize = 2.sp),
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            )
            val equiv = keptEquivalent(centsKept)
            val line = when {
                equiv != null -> "${equiv.emoji} ${equiv.text} · ${keptInCoffees(centsKept)} coffees"
                centsKept > 0 -> "Every dollar you didn't spend, still yours."
                else -> "Browse instead of buying — watch this grow."
            }
            Text(
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.95f),
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

/**
 * A filling jar that makes the abstract "kept" total physical — the research's
 * "people save when they can see their money." Fills to the true progress
 * toward the next milestone (never inflated), with the goal and remainder
 * spelled out beside it.
 */
@Composable
private fun SavingsVault(centsKept: Long) {
    val next = nextSavingsMilestone(centsKept)
    val target = savingsMilestoneProgress(centsKept)
    val progress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 900, delayMillis = 250),
        label = "vaultFill",
    )
    val liquidTop = MintGreen
    val liquidBottom = MintGreen.copy(alpha = 0.65f)
    val outline = MaterialTheme.colorScheme.outlineVariant
    val glass = MaterialTheme.colorScheme.surfaceVariant

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Canvas(Modifier.size(78.dp, 96.dp)) {
                val w = size.width
                val h = size.height
                val lidH = h * 0.13f
                val bodyTop = lidH
                val left = w * 0.10f
                val right = w * 0.90f
                val radius = CornerRadius(14f, 14f)
                val body = Path().apply {
                    addRoundRect(RoundRect(left, bodyTop, right, h, radius))
                }
                // Glass body + lid.
                drawPath(body, color = glass)
                drawRoundRect(
                    color = outline,
                    topLeft = Offset(w * 0.22f, 0f),
                    size = Size(w * 0.56f, lidH * 1.3f),
                    cornerRadius = CornerRadius(8f, 8f),
                )
                // Liquid, clipped to the jar, filling from the bottom.
                clipPath(body) {
                    val fillTop = bodyTop + (h - bodyTop) * (1f - progress)
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(liquidTop, liquidBottom),
                            startY = fillTop,
                            endY = h,
                        ),
                        topLeft = Offset(0f, fillTop),
                        size = Size(w, h - fillTop),
                    )
                    if (progress in 0.02f..0.98f) {
                        // A shallow meniscus so the top reads as liquid.
                        drawArc(
                            color = liquidTop,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = true,
                            topLeft = Offset(left, fillTop - 7f),
                            size = Size(right - left, 14f),
                        )
                    }
                }
                drawPath(body, color = outline, style = Stroke(width = 3f))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text(
                    text = "Your savings vault",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (next != null) {
                    Text(
                        text = "${formatPrice(next)} milestone",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${formatPrice(next - centsKept)} to go",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MintGreen,
                    )
                } else {
                    Text(
                        text = "Top tier reached 🏔️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MintGreen,
                    )
                    Text(
                        text = "and still climbing",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * The supporting stats, wrapped so big values never truncate on narrow phones
 * (the old fixed 4-column row squeezed them). FlowRow reflows to 2-up when the
 * width is tight.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SecondaryStats(ordersPlaced: Int, itemsBought: Int, streakDays: Int) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StatChip("🛍️", animatedCount(ordersPlaced), "orders placed", Modifier.weight(1f))
        StatChip("📦", animatedCount(itemsBought), "items \"kept\"", Modifier.weight(1f))
        StatChip(
            "🔥",
            animatedCount(streakDays),
            if (streakDays == 1) "day resisted" else "days resisted",
            Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatChip(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
        ) {
            Text(emoji, fontSize = 22.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun OrderCard(order: com.cartharsis.data.Order, onOrderClick: (Int) -> Unit) {
    Card(
        modifier = Modifier.clickable { onOrderClick(order.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Order #${order.id}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = "${order.status.emoji} ${order.status.label}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
            Text(
                text = "Placed ${formatOrderDate(order.placedAtMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                order.items.take(5).forEach { item ->
                    Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp))) {
                        EmojiHero(
                            emoji = item.product.emoji,
                            modifier = Modifier.fillMaxSize(),
                            fontSize = 16,
                            seed = item.product.id,
                        )
                    }
                }
                if (order.items.size > 5) {
                    Text(
                        text = "+${order.items.size - 5}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${order.itemCount} " + (if (order.itemCount == 1) "item" else "items") + "  ›",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row {
                Text(
                    text = "You kept",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = formatPrice(order.totalCents),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MintGreen,
                )
            }
        }
    }
}
