package com.cartharsis.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cartharsis.ShopViewModel
import com.cartharsis.data.Badge
import com.cartharsis.data.CardPull
import com.cartharsis.data.FakeCatalog
import com.cartharsis.data.MopplingFigure
import com.cartharsis.data.MopplingWave
import com.cartharsis.data.badges
import com.cartharsis.data.decodeBinderCard

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
    val binder by viewModel.binder.collectAsState()
    val mopplings by viewModel.mopplings.collectAsState()

    Column(Modifier.fillMaxSize()) {
        NestedTopBar(onBack = onBack, title = "Milestones")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            // Every item here is a major section (stats, badges, binder,
            // shelf), so they break at a consistent ~22dp section gap rather
            // than the tight 12dp used within a section.
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
            CardBinder(binder = binder)
            MopplingShelf(shelf = mopplings)
        }
    }
}

/**
 * The card binder: every chase card pulled from a pack rip, kept forever.
 * Unpulled cards stay locked and unnamed — the mystery is the pull's to
 * spend, never the checklist's to spoil.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardBinder(binder: Set<String>) {
    val collected = remember(binder) { binder.mapNotNull(::decodeBinderCard).toSet() }
    val total = FakeCatalog.cardGameTitles.keys.sumOf { FakeCatalog.chaseCardsOf(it).size }
    // A pulled card can be held again: tapping its pill opens the full face.
    var inspecting by remember { mutableStateOf<Pair<String, CardPull>?>(null) }
    inspecting?.let { (game, card) ->
        Dialog(onDismissRequest = { inspecting = null }) {
            RipCardFace(
                card = card,
                theme = packTheme(game),
                faceDown = false,
                holo = true,
                modifier = Modifier.clickable(onClickLabel = "Close") { inspecting = null },
            )
        }
    }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
            Text(
                text = "Card binder",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${collected.size} of $total pulled",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (collected.isEmpty()) {
            Text(
                text = "Chase cards from ripped boosters land here — mint forever.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        FakeCatalog.cardGameTitles.forEach { (game, title) ->
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp, bottom = 2.dp),
            )
            // Each series is its own set page, the way a real binder splits
            // by expansion — with its own pulled count.
            FakeCatalog.chaseChecklistOf(game).forEach { (series, cards) ->
                val pulled = cards.count { (game to it.name) in collected }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
                ) {
                    Text(
                        text = series,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "$pulled of ${cards.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    cards.forEach { card ->
                        CollectiblePill(
                            emoji = card.emoji,
                            name = card.name,
                            owned = (game to card.name) in collected,
                            onInspect = { inspecting = game to card },
                        )
                    }
                }
            }
        }
    }
}

/**
 * One slot of a collection: the collectible when owned, a locked "???"
 * when not. Shared by the card binder and the Moppling shelf — a slot is
 * a slot. Only an owned one is tappable (and only when given an action).
 */
@Composable
private fun CollectiblePill(emoji: String, name: String, owned: Boolean, onInspect: (() -> Unit)? = null) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (owned) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        },
        // Only a pulled collectible opens — a locked slot keeps its mystery.
        modifier = if (owned && onInspect != null) {
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClickLabel = "View $name", onClick = onInspect)
        } else {
            Modifier
        },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Text(
                text = if (owned) emoji else "🔒",
                fontSize = 16.sp,
                modifier = if (owned) Modifier else Modifier.alpha(0.6f),
            )
            Text(
                text = if (owned) name else "???",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (owned) FontWeight.SemiBold else FontWeight.Normal,
                color = if (owned) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                },
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

// The cabinet's wood — a piece of furniture keeps its own finish, like the
// cards keep their print.
private val ShelfWood = Color(0xFFB08968)
private val ShelfWoodDark = Color(0xFF85603C)
private val ShelfWoodEdge = Color(0xFF5F452B)
private val ShelfBackboard = Color(0xFFF7EBD8)
private val ShelfPlaque = Color(0xFF6A4E33)
private val ShelfLabelInk = Color(0xFF6B5A45)

/**
 * The Moppling shelf as actual furniture: a wood-framed display cabinet
 * where found figures stand on planks (shadows and all) over little
 * museum labels, and unfound slots hold blank, unpainted silhouettes —
 * the "???" house rule in figurine form.
 */
@Composable
internal fun MopplingShelf(shelf: Set<String>, modifier: Modifier = Modifier) {
    val found = remember(shelf) { shelf.mapNotNull(::decodeBinderCard).toSet() }
    val total = FakeCatalog.mopplingWaves.sumOf { it.figures.size }
    Column(modifier) {
        // top=6 matches the other section headers; the Column's 16dp gap is
        // what sets this shelf clear of the binder above.
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
            Text(
                text = "Moppling shelf",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${found.size} of $total found",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (found.isEmpty()) {
            Text(
                text = "Blind-box figures land here, wave by wave — found forever.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 6.dp),
            )
        }
        // The cabinet: backboard inside a wood frame.
        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.verticalGradient(listOf(ShelfWood, ShelfWoodDark)))
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ShelfBackboard)
                .padding(horizontal = 10.dp, vertical = 12.dp),
        ) {
            FakeCatalog.mopplingWaves.forEachIndexed { waveIndex, wave ->
                val waveFound = wave.figures.count { (wave.key to it.name) in found }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = if (waveIndex == 0) 0.dp else 14.dp, bottom = 2.dp),
                ) {
                    // The engraved plaque naming the wave.
                    Text(
                        text = wave.title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = ShelfBackboard,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ShelfPlaque)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "$waveFound of ${wave.figures.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ShelfLabelInk,
                    )
                }
                wave.figures.chunked(5).forEach { boardFigures ->
                    ShelfBoard(wave = wave, figures = boardFigures, found = found)
                }
            }
        }
    }
}

/** One plank of the cabinet: up to five figures standing on the board. */
@Composable
private fun ShelfBoard(wave: MopplingWave, figures: List<MopplingFigure>, found: Set<Pair<String, String>>) {
    Column(Modifier.fillMaxWidth().padding(top = 6.dp)) {
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
            figures.forEach { figure ->
                ShelfFigure(
                    figure = figure,
                    owned = (wave.key to figure.name) in found,
                    modifier = Modifier.weight(1f),
                )
            }
            repeat(5 - figures.size) { Spacer(Modifier.weight(1f)) }
        }
        // The plank: a lit top face over a darker front edge.
        Box(
            Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Brush.verticalGradient(listOf(ShelfWood, ShelfWoodDark))),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 3.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
                .background(ShelfWoodEdge.copy(alpha = 0.55f)),
        )
        // Museum labels under the board.
        Row(Modifier.fillMaxWidth().padding(top = 2.dp)) {
            figures.forEach { figure ->
                Text(
                    text = if ((wave.key to figure.name) in found) figure.name else "???",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    color = if ((wave.key to figure.name) in found) {
                        ShelfLabelInk
                    } else {
                        ShelfLabelInk.copy(alpha = 0.45f)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            repeat(5 - figures.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

/** A figure on the board — or the blank, unpainted silhouette of one. */
@Composable
private fun ShelfFigure(figure: MopplingFigure, owned: Boolean, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .height(46.dp)
            .clearAndSetSemantics {
                contentDescription = if (owned) "${figure.name}, found" else "An unfound figure"
            },
    ) {
        // The standing shadow that glues the figure to the plank.
        Canvas(Modifier.fillMaxSize()) {
            drawOval(
                color = Color.Black.copy(alpha = if (owned) 0.14f else 0.07f),
                topLeft = Offset(size.width / 2 - 13.dp.toPx(), size.height - 5.dp.toPx()),
                size = Size(26.dp.toPx(), 5.dp.toPx()),
            )
        }
        if (owned) {
            Text(figure.emoji, fontSize = 27.sp, modifier = Modifier.padding(bottom = 2.dp))
        } else {
            // The unpainted blank: a body and a head, waiting.
            Canvas(Modifier.size(26.dp, 34.dp).padding(bottom = 2.dp)) {
                val blank = ShelfLabelInk.copy(alpha = 0.22f)
                drawCircle(blank, radius = size.width * 0.30f, center = Offset(size.width / 2, size.width * 0.32f))
                drawRoundRect(
                    color = blank,
                    topLeft = Offset(size.width * 0.14f, size.width * 0.52f),
                    size = Size(size.width * 0.72f, size.height - size.width * 0.55f),
                    cornerRadius = CornerRadius(7.dp.toPx()),
                )
            }
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
