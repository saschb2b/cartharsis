package com.cartharsis.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartharsis.ShopViewModel
import com.cartharsis.data.Badge
import com.cartharsis.data.badges

/**
 * The trophy room, pushed from Orders so the impact screen stays a glanceable
 * payoff instead of a stats wall. Career stats up top, then every badge with
 * room to breathe — earned ones in full color, locked ones faint with their
 * requirement spelled out as the label (a forward pull, never a loss frame).
 */
@Composable
fun MilestonesScreen(viewModel: ShopViewModel, onBack: () -> Unit) {
    val streakDays by viewModel.streakDays.collectAsState()
    val stats by viewModel.lifetimeStats.collectAsState()

    Column(Modifier.fillMaxSize()) {
        NestedTopBar(onBack = onBack, title = "Milestones")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CareerStats(
                ordersPlaced = stats.ordersPlaced,
                itemsBought = stats.itemsBought,
                streakDays = streakDays,
            )
            BadgeShelf(
                ordersPlaced = stats.ordersPlaced,
                centsKept = stats.centsKept,
                streakDays = streakDays,
            )
        }
    }
}

/**
 * The supporting stats, wrapped so big values never truncate on narrow phones.
 * FlowRow reflows to 2-up when the width is tight.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CareerStats(ordersPlaced: Int, itemsBought: Int, streakDays: Int) {
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

/** Every badge as a roomy 3-up grid — earned in color, the rest as "next up". */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BadgeShelf(ordersPlaced: Int, centsKept: Long, streakDays: Int) {
    val all = remember(ordersPlaced, centsKept, streakDays) { badges(ordersPlaced, centsKept, streakDays) }
    val earned = all.count { it.earned }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)) {
            Text(
                text = "Badges",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "$earned of ${all.size} earned",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 3,
        ) {
            all.forEach { badge -> BadgePill(badge, Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun BadgePill(badge: Badge, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (badge.earned) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (badge.earned) badge.emoji else "🔒",
                fontSize = 26.sp,
                modifier = if (badge.earned) Modifier else Modifier.alpha(0.6f),
            )
            Text(
                text = badge.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (badge.earned) FontWeight.SemiBold else FontWeight.Normal,
                color = if (badge.earned) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
                maxLines = 2,
            )
        }
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
