package com.cartharsis.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartharsis.data.CurrencyState
import com.cartharsis.data.FakeCatalog
import com.cartharsis.data.Product
import com.cartharsis.data.formatPrice
import com.cartharsis.ui.theme.CartharsisTheme
import com.cartharsis.ui.theme.ElectricPurple
import com.cartharsis.ui.theme.HotPink
import com.cartharsis.ui.theme.JuicyOrange
import com.cartharsis.ui.theme.LemonYellow
import com.cartharsis.ui.theme.MintGreen
import com.cartharsis.ui.theme.SkyBlue
import kotlin.math.abs
import kotlinx.coroutines.delay

/** "colors" / "editions" / "options" for the grid variant hint, pluralized. */
fun variantNoun(axis: String, count: Int): String {
    val base = when (axis) {
        "Color" -> "color"
        "Edition" -> "edition"
        "Capacity" -> "capacity"
        "Format" -> "format"
        else -> "option"
    }
    return if (count == 1) {
        base
    } else if (base == "capacity") {
        "capacities"
    } else {
        base + "s"
    }
}

/** "12,473" is for receipts; cards say "12.5k". */
fun formatCompactCount(n: Int): String = when {
    n < 1_000 -> "$n"
    n < 100_000 -> {
        val hundreds = n / 100
        if (hundreds % 10 == 0) "${hundreds / 10}k" else "${hundreds / 10}.${hundreds % 10}k"
    }
    else -> "${n / 1_000}k"
}

/**
 * Full star row — the product page treatment. The fill is fractional: a
 * gold star strip is clipped over a faint one to the exact `rating/5`, so a
 * 4.8 reads as nearly-five rather than a truncated four.
 */
@Composable
fun RatingStars(rating: Double, reviewCount: Int? = null) {
    val description = "Rated $rating out of 5" +
        (reviewCount?.let { ", %,d reviews".format(it) } ?: "")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clearAndSetSemantics { contentDescription = description },
    ) {
        val fraction = (rating / 5.0).coerceIn(0.0, 1.0).toFloat()
        Box {
            Text(
                text = "★★★★★",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f),
                style = MaterialTheme.typography.bodyMedium,
            )
            // The gold layer, clipped to the exact fractional width.
            Text(
                text = "★★★★★",
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.drawWithContent {
                    clipRect(right = size.width * fraction) { this@drawWithContent.drawContent() }
                },
            )
        }
        Text(
            text = " $rating" + (reviewCount?.let { " (%,d)".format(it) } ?: ""),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

/** Compact one-line rating for cards: "★ 4.8 · 12.5k". */
@Composable
fun RatingBadge(rating: Double, reviewCount: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = "Rated $rating out of 5, %,d reviews".format(reviewCount)
        },
    ) {
        Text(
            text = "★",
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = " $rating",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = " · ${formatCompactCount(reviewCount)}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * The price is the boldest thing in any block it appears in; the accent
 * palette stays reserved for CTAs and savings, so price reads in ink.
 */
@Composable
fun PriceRow(product: Product, big: Boolean = false, showDiscountBadge: Boolean = true) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = formatPrice(product.priceCents),
            style = if (big) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        product.originalPriceCents?.let { original ->
            Text(
                text = " " + formatPrice(original),
                style = MaterialTheme.typography.bodySmall,
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (showDiscountBadge) {
                DiscountBadge(
                    percent = product.discountPercent ?: 0,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
    }
}

@Composable
fun DiscountBadge(percent: Int, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.tertiary,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier,
    ) {
        Text(
            text = "-$percent%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
        )
    }
}

/**
 * Fades + lifts its content in once, replaying whenever [key] changes. Used to
 * stagger the home sections on each open (different [delayMillis] per section)
 * so the storefront feels alive every time, even when content repeats.
 *
 * Pass [enabled] = false once the entrance has played to render the content
 * plainly — a lazy item recycled back on-screen mid-scroll must not restart
 * the fade, which both costs animation frames and flickers during a fling.
 */
@Composable
fun AppearOnce(key: Any, delayMillis: Int, enabled: Boolean = true, content: @Composable () -> Unit) {
    if (!enabled) {
        content()
        return
    }
    var shown by remember(key) { mutableStateOf(false) }
    LaunchedEffect(key) {
        delay(delayMillis.toLong())
        shown = true
    }
    val progress by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = 320),
        label = "appear",
    )
    Box(
        Modifier.graphicsLayer {
            alpha = progress
            translationY = (1f - progress) * 28f
        },
    ) { content() }
}

// Soft tile gradients, assigned stably per product so the grid reads colorful
// but each product keeps its own "packaging" everywhere it appears.
private val heroGradients = listOf(
    listOf(HotPink, ElectricPurple),
    listOf(ElectricPurple, SkyBlue),
    listOf(JuicyOrange, HotPink),
    listOf(MintGreen, SkyBlue),
    listOf(LemonYellow, JuicyOrange),
    listOf(SkyBlue, ElectricPurple),
)

// Each gradient pair's softened (alpha 0.14) form, precomputed once — the
// grid churns these hardest during a fling, so we don't re-soften per card.
private val softHeroGradients: List<List<Color>> =
    heroGradients.map { pair -> pair.map { it.copy(alpha = 0.14f) } }

/** The softened per-product gradient colors for a [seed] — the product's
 * stable "packaging" tint, shared by EmojiHero and the PDP hero stage. */
internal fun heroGradientColors(seed: Int): List<Color> = softHeroGradients[abs(seed) % softHeroGradients.size]

/** The (stage, glow) brush pair for a card's hero-bleed, memoized per seed. */
@Composable
private fun rememberStageBrushes(seed: Int): Pair<Brush, Brush> = remember(seed) {
    val colors = heroGradientColors(seed)
    Brush.linearGradient(colors) to
        Brush.radialGradient(listOf(colors.first().copy(alpha = 0.4f), Color.Transparent))
}

/** Big emoji on a soft per-product gradient — the entire "product photography" budget. */
@Composable
fun EmojiHero(emoji: String, modifier: Modifier = Modifier, fontSize: Int = 64, seed: Int = 0) {
    val brush = remember(seed) { Brush.linearGradient(heroGradientColors(seed)) }
    Box(
        modifier = modifier.background(brush),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = fontSize.sp)
    }
}

/**
 * The product-page hero: the art runs oversized and breaks past its tinted
 * stage — "too big to contain" — rather than sitting tidily inside a band. A
 * soft glow pools behind it so the overflow reads as depth, not a clipping
 * bug. (The stage clips its own background; the emoji is a sibling on top, so
 * it spills freely.)
 */
@Composable
fun ProductHero(emoji: String, seed: Int, modifier: Modifier = Modifier) {
    val colors = remember(seed) { heroGradientColors(seed) }
    Box(
        modifier = modifier.fillMaxWidth().height(300.dp),
        contentAlignment = Alignment.Center,
    ) {
        // The tinted stage — the frame the art breaks out of.
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(200.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(colors)),
        )
        // A soft glow grounding the oversized art.
        Box(
            Modifier
                .size(260.dp)
                .background(
                    Brush.radialGradient(listOf(colors.first().copy(alpha = 0.38f), Color.Transparent)),
                    CircleShape,
                ),
        )
        Text(text = emoji, fontSize = 240.sp)
    }
}

/** Section title with an optional trailing action — one header style everywhere. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

/**
 * The back affordance, hand-rolled to the Material spec (a left-pointing
 * arrow: 24dp glyph, ~2dp rounded strokes) so we get the canonical Material
 * icon without pulling in the material-icons artifact. Kept decorative; the
 * "Back" label lives on the enclosing button via [clearAndSetSemantics].
 */
@Composable
fun BackArrowIcon(modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.onSurface) {
    Canvas(
        modifier
            .size(24.dp)
            .clearAndSetSemantics { contentDescription = "Back" },
    ) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.083f // 2dp on a 24dp glyph, per the Material spec
        val tip = Offset(w * 0.16f, h * 0.5f)
        drawLine(
            color = tint,
            start = Offset(w * 0.90f, h * 0.5f),
            end = tip,
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(tint, tip, Offset(w * 0.42f, h * 0.27f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(tint, tip, Offset(w * 0.42f, h * 0.73f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

/**
 * One top app bar for every nested (pushed) screen — a standard-height
 * Material small top app bar with a clearly identifiable back button in a
 * full 48dp touch target, an optional title, and an optional actions slot.
 * Decision surfaces stay calm: title in [MaterialTheme.typography.titleLarge],
 * the bar tinted to [containerColor] (surface by default).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NestedTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor),
        navigationIcon = {
            IconButton(onClick = onBack) { BackArrowIcon() }
        },
        actions = actions,
    )
}

/**
 * Shared quantity stepper: a quiet pill, 48dp touch targets, the count
 * animates its width so multi-digit quantities don't jump the layout.
 */
@Composable
fun QuantityStepper(quantity: Int, onQuantityChange: (Int) -> Unit, modifier: Modifier = Modifier, min: Int = 1) {
    Surface(
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepButton(
                glyph = "−",
                label = "Decrease quantity",
                enabled = quantity > min,
                onClick = { onQuantityChange(quantity - 1) },
            )
            Text(
                text = "$quantity",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .defaultMinSize(minWidth = 20.dp)
                    .animateContentSize(),
            )
            StepButton(
                glyph = "+",
                label = "Increase quantity",
                enabled = true,
                onClick = { onQuantityChange(quantity + 1) },
            )
        }
    }
}

@Composable
private fun StepButton(glyph: String, label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clickable(enabled = enabled, onClick = onClick, role = Role.Button, onClickLabel = label),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = glyph,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            },
            modifier = Modifier.clearAndSetSemantics { contentDescription = label },
        )
    }
}

/**
 * Grid card. Fixed attribute set in a fixed order — image, name, rating,
 * price — because scanning a grid is comparison work and consistency is
 * what makes it effortless.
 */
@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    isWishlisted: Boolean,
    onToggleWishlist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            // The art runs oversized and breaks past a rounded tinted stage —
            // the PDP hero-bleed, card-scaled — so the grid reads alive rather
            // than as a flat colored band.
            Box(Modifier.fillMaxWidth().height(128.dp), contentAlignment = Alignment.Center) {
                // Brushes memoized per product so a fling doesn't re-allocate
                // the gradient and glow on every card that scrolls past.
                val (stageBrush, glowBrush) = rememberStageBrushes(product.id)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .padding(top = 14.dp)
                        .height(82.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(stageBrush),
                )
                Box(Modifier.size(108.dp).background(glowBrush, CircleShape))
                Text(product.emoji, fontSize = 78.sp)
                product.discountPercent?.let {
                    DiscountBadge(percent = it, modifier = Modifier.align(Alignment.TopStart).padding(10.dp))
                }
                WishHeart(
                    isWishlisted = isWishlisted,
                    onToggle = onToggleWishlist,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }
            // Spacing rhythm over a flat 4dp stack: name + rating sit together
            // as the "what it is", then a wider gap isolates the price as the
            // card's focal answer. The 14dp inset lines the text's left edge
            // up with the image stage above.
            Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 14.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                RatingBadge(rating = product.rating, reviewCount = product.reviewCount)
                Spacer(Modifier.height(10.dp))
                // The image badge already announces the deal; one badge per card.
                PriceRow(product, showDiscountBadge = false)
                // Amazon-style "N options" hint so variants are discoverable
                // from the grid, not only on the base listing.
                product.variantGroup?.let { group ->
                    val count = remember(group) { FakeCatalog.variantsOf(group).size }
                    if (count > 1) {
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = "$count ${variantNoun(product.variantAxis, count)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/** Small tappable product card for horizontal suggestion strips. */
@Composable
fun MiniProductCard(product: Product, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(132.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        // The same hero-bleed as the grid card, scaled down: art spills past
        // a rounded inset stage, grounded by a glow.
        Box(Modifier.fillMaxWidth().height(92.dp), contentAlignment = Alignment.Center) {
            val (stageBrush, glowBrush) = rememberStageBrushes(product.id)
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp)
                    .height(58.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(stageBrush),
            )
            Box(Modifier.size(78.dp).background(glowBrush, CircleShape))
            Text(product.emoji, fontSize = 52.sp)
        }
        Column(Modifier.padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 12.dp)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.labelMedium,
                lineHeight = 15.sp,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = formatPrice(product.priceCents),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/** Heart toggle — wanting things is free, and we keep it that way. */
@Composable
fun WishHeart(isWishlisted: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable(
                onClick = onToggle,
                role = Role.Checkbox,
                onClickLabel = if (isWishlisted) "Remove from wishlist" else "Add to wishlist",
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isWishlisted) "❤️" else "🤍",
            fontSize = 20.sp,
            modifier = Modifier.clearAndSetSemantics {
                contentDescription = if (isWishlisted) "Wishlisted" else "Not wishlisted"
            },
        )
    }
}

/** The one delivery promise we can make with a straight face. */
const val DELIVERY_PROMISE = "Free delivery · arrives ~1 minute after checkout"

/**
 * A money string that ticks up to its value — cumulative numbers feel earned
 * when you watch them arrive. Whole units only; the cents never mattered. Runs
 * in the selected currency: converts to its whole units, ticks to that, and
 * formats with its symbol and grouping.
 */
@Composable
fun animatedMoney(cents: Long, delayMillis: Int = 0): String {
    val currency = CurrencyState.active
    val animated by androidx.compose.animation.core.animateIntAsState(
        targetValue = currency.majorUnits(cents).toInt(),
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 900,
            delayMillis = delayMillis,
        ),
        label = "money",
    )
    return currency.formatMajorUnits(animated.toLong())
}

/** A plain count that ticks up to its value. */
@Composable
fun animatedCount(value: Int): String {
    val animated by androidx.compose.animation.core.animateIntAsState(
        targetValue = value,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 700),
        label = "count",
    )
    return "$animated"
}

fun formatCountdown(totalSeconds: Int): String = "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)

private val previewProduct = Product(
    id = 0,
    name = "Preview Phone Ultra Max",
    emoji = "📱",
    tagline = "Only renders, never ships.",
    description = "A product that exists exclusively inside Android Studio.",
    priceCents = 129_900,
    category = "Tech",
    rating = 4.8,
    reviewCount = 12_473,
    reviews = emptyList(),
    originalPriceCents = 159_900,
)

@Preview(showBackground = true)
@Composable
private fun ProductCardPreview() {
    CartharsisTheme {
        ProductCard(
            product = previewProduct,
            onClick = {},
            isWishlisted = true,
            onToggleWishlist = {},
            modifier = Modifier.width(180.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MiniProductCardPreview() {
    CartharsisTheme {
        MiniProductCard(product = previewProduct, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun QuantityStepperPreview() {
    CartharsisTheme {
        QuantityStepper(quantity = 2, onQuantityChange = {})
    }
}

/**
 * The "payment method", drawn with real card anatomy — ISO 1.586:1 ratio,
 * EMV chip, contactless mark, embossed digit groups, holder and expiry
 * blocks — because the ceremony only works if the props look real.
 * Balance: infinite, by definition. Shared by onboarding and checkout.
 */
@Composable
fun ImaginationCard(cardHolder: String, modifier: Modifier = Modifier) {
    val embossed = Shadow(color = Color.Black.copy(alpha = 0.35f), offset = Offset(0f, 3f), blurRadius = 4f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(ElectricPurple, HotPink))),
    ) {
        // A diagonal sheen so the surface reads as plastic, not as a fill.
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0f to Color.White.copy(alpha = 0.18f),
                            0.45f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.12f),
                        ),
                    ),
                ),
        )
        Column(Modifier.fillMaxSize().padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "IMAGINATION EXPRESS",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                )
                Spacer(Modifier.weight(1f))
                ContactlessMark()
            }
            Spacer(Modifier.weight(1f))
            EmvChip()
            Spacer(Modifier.height(10.dp))
            // Shrinks to one line instead of wrapping — a wrapped card number
            // pushed the holder row past the card's clipped bottom edge on
            // narrower cards.
            BasicText(
                text = "5310  0000  0000  0000",
                maxLines = 1,
                softWrap = false,
                autoSize = TextAutoSize.StepBased(minFontSize = 13.sp, maxFontSize = 22.sp, stepSize = 0.5.sp),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp,
                    shadow = embossed,
                ),
            )
            Spacer(Modifier.height(12.dp))
            // Two fields, the way real cards lay the bottom out: the holder
            // spans the whole left, the expiry pins right. The "scheme logo"
            // is the IMAGINATION EXPRESS wordmark up top, so nothing crowds
            // the name here.
            Row(verticalAlignment = Alignment.Bottom) {
                CardField(
                    label = "CARD HOLDER",
                    value = cardHolder.trim().uppercase().ifBlank { "YOUR IMAGINATION" },
                    shadow = embossed,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(16.dp))
                CardField(
                    label = "VALID THRU",
                    value = "∞∞ / ∞∞",
                    shadow = embossed,
                    alignment = Alignment.End,
                )
            }
        }
    }
}

@Composable
private fun CardField(
    label: String,
    value: String,
    shadow: Shadow,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.Start,
) {
    Column(modifier, horizontalAlignment = alignment) {
        Text(label, color = Color.White.copy(alpha = 0.65f), fontSize = 8.sp, letterSpacing = 1.sp)
        Text(
            text = value,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                shadow = shadow,
            ),
        )
    }
}

/** Gold contact chip, simplified to the pattern everyone recognizes. */
@Composable
private fun EmvChip() {
    Canvas(Modifier.size(width = 42.dp, height = 31.dp)) {
        drawRoundRect(
            brush = Brush.linearGradient(listOf(Color(0xFFEED787), Color(0xFFC49A45))),
            cornerRadius = CornerRadius(6.dp.toPx()),
        )
        val line = Color(0xFF8A6B2F)
        val strokeWidth = 1.4.dp.toPx()
        val w = size.width
        val h = size.height
        drawLine(line, Offset(0f, h / 2), Offset(w, h / 2), strokeWidth)
        drawLine(line, Offset(w * 0.33f, 0f), Offset(w * 0.33f, h), strokeWidth)
        drawLine(line, Offset(w * 0.67f, 0f), Offset(w * 0.67f, h), strokeWidth)
        drawRoundRect(
            color = line,
            topLeft = Offset(w * 0.33f, h * 0.28f),
            size = Size(w * 0.34f, h * 0.44f),
            cornerRadius = CornerRadius(3.dp.toPx()),
            style = Stroke(strokeWidth),
        )
    }
}

/** The radio-waves mark; ours broadcasts nothing, contactlessly. */
@Composable
private fun ContactlessMark() {
    Canvas(Modifier.size(22.dp)) {
        val color = Color.White.copy(alpha = 0.85f)
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val cx = size.width * 0.18f
        val cy = size.height * 0.5f
        listOf(0.28f, 0.52f, 0.76f).forEach { f ->
            val r = size.width * f
            drawArc(
                color = color,
                startAngle = -38f,
                sweepAngle = 76f,
                useCenter = false,
                topLeft = Offset(cx - r, cy - r),
                size = Size(2 * r, 2 * r),
                style = stroke,
            )
        }
    }
}

@Preview
@Composable
private fun ImaginationCardPreview() {
    CartharsisTheme {
        ImaginationCard(cardHolder = "Preview Person")
    }
}
