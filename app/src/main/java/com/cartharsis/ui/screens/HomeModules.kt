package com.cartharsis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartharsis.data.HomeModule
import com.cartharsis.data.MoodEntry
import com.cartharsis.data.Product
import com.cartharsis.data.formatPrice

/**
 * The home "magazine" modules — the heterogeneous bands that replace the old
 * shelf stack. Each shape reads differently (a big editorial hero, mood tiles, a
 * mixed-size bento, an editors' list, a spotlight duo), so the page feels like a
 * curated store rather than a row of identical carousels. The seeded deck lives
 * in [com.cartharsis.data.homeModules]; these just render it.
 */

/**
 * The editorial hero: one product spotlighted on its own vivid gradient with
 * oversized bleeding art — the page's opening statement. Reframes the old flash
 * deal as a feature, not an alarm (no ticking countdown); the discount, if any,
 * reads as savings.
 */
@Composable
fun HeroFeature(product: Product, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = remember(product.id) { heroGradientColorsVivid(product.id) }
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(196.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "Today's feature: ${product.name}, ${formatPrice(product.priceCents)}"
            },
        // 20.dp matches the other large module card (EditorsList) and the
        // codebase's large-card radius, rather than a one-off 24.
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(colors))) {
            // Oversized art, bleeding off the right edge.
            Text(
                text = product.emoji,
                fontSize = 150.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 18.dp)
                    .clearAndSetSemantics {},
            )
            // A left scrim so the white copy stays legible over any gradient.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.28f), Color.Transparent)),
                    ),
            )
            Column(
                Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "✨ TODAY'S OBSESSION",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.92f),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(0.72f),
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatPrice(product.priceCents),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                    product.originalPriceCents?.let {
                        Text(
                            text = "  ${formatPrice(it)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.22f)) {
                    Text(
                        text = "Take a look  →",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

/** Renders one home module; the dispatcher the feed uses. */
@Composable
fun HomeModuleView(
    module: HomeModule,
    onProductClick: (Int) -> Unit,
    onMoodPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (module) {
        is HomeModule.Moods -> MoodTiles(module.entries, onMoodPick, modifier)
        is HomeModule.Bento -> BentoCollection(module, onProductClick, modifier)
        is HomeModule.Editors -> EditorsList(module, onProductClick, modifier)
        is HomeModule.Duo -> SpotlightDuo(module, onProductClick, modifier)
    }
}

/** Colorful "shop by mood" tiles — evocative doorways into the catalog. */
@Composable
private fun MoodTiles(entries: List<MoodEntry>, onPick: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        entries.forEach { mood ->
            val colors = remember(mood.label) { heroGradientColorsVivid(mood.label.hashCode()) }
            Box(
                modifier = Modifier
                    .size(width = 104.dp, height = 92.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(colors))
                    .clickable(onClickLabel = "Browse ${mood.label}") { onPick(mood.category) },
            ) {
                // The same bottom scrim the hero and product tiles use, so the
                // white label stays legible even on the light gradient pairs.
                Box(
                    Modifier
                        .matchParentSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)))),
                )
                Column(
                    modifier = Modifier.matchParentSize().padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(mood.emoji, fontSize = 28.sp, modifier = Modifier.clearAndSetSemantics {})
                    Text(
                        text = mood.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        lineHeight = 16.sp,
                    )
                }
            }
        }
    }
}

/** A themed collection as a bento: one big feature tile beside a 2×2 of smalls. */
@Composable
private fun BentoCollection(module: HomeModule.Bento, onProductClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        SectionHeader(title = module.title, modifier = Modifier.padding(bottom = 10.dp))
        Row(Modifier.height(196.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ArtTile(
                product = module.big,
                onClick = { onProductClick(module.big.id) },
                modifier = Modifier.weight(1.1f).fillMaxHeight(),
                emojiSize = 84,
            )
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                module.smalls.chunked(2).forEach { pair ->
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { p ->
                            ArtTile(
                                product = p,
                                onClick = { onProductClick(p.id) },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                emojiSize = 40,
                                showName = false,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** An editorial ranked list — the "from the editors" card. */
@Composable
private fun EditorsList(module: HomeModule.Editors, onProductClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "✎ FROM THE EDITORS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = module.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(10.dp))
            module.items.forEachIndexed { i, p ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClickLabel = "View ${p.name}") { onProductClick(p.id) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${i + 1}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.width(24.dp),
                    )
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))) {
                        EmojiHero(p.emoji, Modifier.fillMaxSize(), fontSize = 22, seed = p.id)
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(
                            text = p.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = p.tagline,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = formatPrice(p.priceCents),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
                if (i < module.items.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

/** Two medium feature tiles side by side. */
@Composable
private fun SpotlightDuo(module: HomeModule.Duo, onProductClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        SectionHeader(title = module.title, modifier = Modifier.padding(bottom = 10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(module.left, module.right).forEach { p ->
                ArtTile(
                    product = p,
                    onClick = { onProductClick(p.id) },
                    modifier = Modifier.weight(1f).height(150.dp),
                    emojiSize = 64,
                )
            }
        }
    }
}

/**
 * The shared art tile: a product's emoji on its tinted stage with the name and
 * price riding a bottom scrim — the building block for the bento and spotlight
 * shapes. Announces "name, price" as one button to TalkBack.
 */
@Composable
private fun ArtTile(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emojiSize: Int = 56,
    showName: Boolean = true,
) {
    Card(
        onClick = onClick,
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "${product.name}, ${formatPrice(product.priceCents)}"
        },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            EmojiHero(product.emoji, Modifier.fillMaxSize(), fontSize = emojiSize, seed = product.id)
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                if (showName) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        lineHeight = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = formatPrice(product.priceCents),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
            }
        }
    }
}
