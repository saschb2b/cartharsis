package com.cartharsis.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartharsis.ShopViewModel
import com.cartharsis.data.FakeCatalog
import com.cartharsis.data.Order
import com.cartharsis.data.OrderStatus
import com.cartharsis.data.formatOrderDate
import com.cartharsis.data.formatPrice
import com.cartharsis.ui.theme.LocalSavingsColor
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(viewModel: ShopViewModel, orderId: Int, onBack: () -> Unit, onShopMore: () -> Unit) {
    val orders by viewModel.orders.collectAsState()
    val order = orders.firstOrNull { it.id == orderId } ?: return

    // An arrival presents a sealed parcel and waits for the tap — the
    // celebration belongs to the user's action, not to a state change. The
    // ViewModel remembers which orders were opened, so the moment happens
    // exactly once whether the arrival is watched live, reached through the
    // delivered notification, or found later in history.
    val unboxedOrders by viewModel.unboxedOrders.collectAsState()
    val unboxed = order.id in unboxedOrders
    var celebrate by remember(orderId) { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    // A soft tick when a stage flips while watching — progress you can feel.
    // Arrival stays silent here; its moment belongs to the unbox tap.
    var lastStatus by remember(orderId) { mutableStateOf(order.status) }
    LaunchedEffect(order.status) {
        if (order.status != lastStatus) {
            lastStatus = order.status
            if (order.status != OrderStatus.DELIVERED) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }
    LaunchedEffect(celebrate) {
        if (celebrate) {
            delay(3_800)
            celebrate = false
        }
    }

    Scaffold(
        topBar = { NestedTopBar(onBack = onBack, title = "Order #${order.id}") },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Roughly one order in eight gets a courier upgrade. Purely
                // decorative, stable per order so revisits keep the same ride.
                val vehicle = remember(orderId) { if ((orderId * 31 + 7) % 8 == 0) "🚀" else "🛵" }
                if (order.status == OrderStatus.DELIVERED) {
                    // The haul: one emoji per ordered line. A Mystery Box shows
                    // what it hypothetically held — that's the content you came
                    // to see — rather than its own ❓.
                    val haul = remember(order.id) {
                        order.items.map { line ->
                            if (line.product.id == FakeCatalog.mysteryBox.id) {
                                FakeCatalog.mysteryRevealFor(order.id).emoji
                            } else {
                                line.product.emoji
                            }
                        }.take(7)
                    }
                    // `unboxed` (ViewModel) is the durable "opened ever" flag;
                    // `revealing` is local so the burst plays only on the live
                    // tap, never on a revisit.
                    var revealing by remember(orderId) { mutableStateOf(false) }
                    val phase = when {
                        !unboxed -> UnboxPhase.Sealed
                        revealing -> UnboxPhase.Revealing
                        else -> UnboxPhase.Opened
                    }
                    AnimatedContent(
                        targetState = phase,
                        label = "unbox",
                        transitionSpec = {
                            (
                                scaleIn(
                                    initialScale = 0.85f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                ) + fadeIn()
                                ).togetherWith(fadeOut(tween(120)))
                        },
                    ) { ph ->
                        when (ph) {
                            UnboxPhase.Sealed -> SealedParcel(
                                onUnbox = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    revealing = true
                                    viewModel.markUnboxed(order.id)
                                    celebrate = true
                                },
                            )
                            UnboxPhase.Revealing -> UnboxingReveal(
                                haul = haul,
                                onFinished = { revealing = false },
                            )
                            UnboxPhase.Opened -> DeliveredCelebration(order, onShopMore)
                        }
                    }
                } else {
                    CourierMap(
                        progress = if (order.status >= OrderStatus.ON_THE_WAY) order.progress else 0f,
                        onTheWay = order.status == OrderStatus.ON_THE_WAY,
                        vehicle = vehicle,
                    )
                    EtaCard(order)
                }
                StatusTracker(order)
                ItemsCard(order)
            }
            if (celebrate) {
                ConfettiOverlay(Modifier.fillMaxSize(), durationMillis = 2_600)
            }
        }
    }
}

@Composable
private fun CourierMap(progress: Float, onTheWay: Boolean, vehicle: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(180.dp)) {
            // A "map": a dashed route from the fake store to your real heart.
            // The traveled part fills in solid — progress you can watch. Trail
            // and courier share the goal-gradient easing so they stay glued.
            val eased = (progress * progress + progress) / 2f
            val routeColor = MaterialTheme.colorScheme.secondary
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
                    color = routeColor.copy(alpha = 0.35f),
                    style = Stroke(
                        width = 6f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f)),
                    ),
                )
                if (eased > 0f) {
                    val measure = PathMeasure().apply { setPath(path, false) }
                    val traveled = Path()
                    measure.getSegment(0f, measure.length * eased.coerceIn(0f, 1f), traveled, true)
                    drawPath(
                        path = traveled,
                        color = routeColor,
                        style = Stroke(width = 9f, cap = StrokeCap.Round),
                    )
                }
            }
            Courier(progress = eased, onTheWay = onTheWay, vehicle = vehicle)
            Text(
                "🏪",
                fontSize = 26.sp,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 6.dp, bottom = 24.dp),
            )
            HomePin(
                nearArrival = onTheWay && progress > 0.85f,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 6.dp, top = 36.dp),
            )
        }
    }
}

@Composable
private fun Courier(progress: Float, onTheWay: Boolean, vehicle: String) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val t = progress.coerceIn(0f, 1f)

        // Same cubic Bézier as the canvas path, evaluated at t.
        fun cubic(p0: Float, p1: Float, p2: Float, p3: Float): Float {
            val u = 1 - t
            return u * u * u * p0 + 3 * u * u * t * p1 + 3 * u * t * t * p2 + t * t * t * p3
        }
        val bob = if (onTheWay) {
            val transition = rememberInfiniteTransition(label = "bob")
            transition.animateFloat(
                initialValue = -2f,
                targetValue = 2f,
                animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
                label = "bobValue",
            ).value
        } else {
            0f
        }
        val x = cubic(0.08f, 0.35f, 0.6f, 0.92f) * maxWidth.value
        val y = cubic(0.75f, 0.15f, 1.0f, 0.3f) * maxHeight.value
        Text(
            vehicle,
            fontSize = 28.sp,
            modifier = Modifier.offset(x = (x - 14).dp, y = (y - 14 + bob).dp),
        )
    }
}

/** The destination perks up when the courier gets close. */
@Composable
private fun HomePin(nearArrival: Boolean, modifier: Modifier = Modifier) {
    val scale = if (nearArrival) {
        val transition = rememberInfiniteTransition(label = "homePulse")
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
            label = "homeScale",
        ).value
    } else {
        1f
    }
    Text("🏠", fontSize = 26.sp, modifier = modifier.scale(scale))
}

/**
 * Tiny en-route vignettes — checking back occasionally pays off with a new
 * one. Bucketed on progress so each appears exactly once per trip.
 */
private val courierMoments = listOf(
    "🐕 Briefly stopped to pet a dog",
    "🚦 Caught every green light so far",
    "🌤️ Weather over the route: imaginary and mild",
    "🪢 Double-checked the straps — the nothing is secure",
    "🎶 Courier is humming. Good sign.",
)

/** The one thing people open a tracking screen for goes first, biggest. */
@Composable
private fun EtaCard(order: Order) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            val headline = if (order.status == OrderStatus.ON_THE_WAY) {
                val secondsLeft = ((1f - order.progress) * ShopViewModel.COURIER_TRIP_SECONDS).toInt() + 1
                "Arriving in ~${secondsLeft}s"
            } else {
                "${order.status.emoji} ${order.status.label}"
            }
            Text(
                text = headline,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = order.status.detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            if (order.status == OrderStatus.ON_THE_WAY) {
                val noun = if (order.itemCount == 1) "item" else "items"
                Text(
                    text = "Carrying 0 of your ${order.itemCount} $noun, exactly as ordered",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 4.dp),
                )
                val momentIndex = (order.progress * courierMoments.size)
                    .toInt().coerceAtMost(courierMoments.size - 1)
                AnimatedContent(targetState = momentIndex, label = "moment") { index ->
                    Text(
                        text = courierMoments[index],
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

/**
 * Pizza-tracker stage row: done stages filled, the current one breathing,
 * the courier leg filling live with trip progress.
 */
@Composable
private fun StatusTracker(order: Order) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OrderStatus.entries.forEachIndexed { index, status ->
                    if (index > 0) {
                        val fill = when {
                            order.status >= status -> 1f
                            order.status.ordinal == index - 1 && order.status == OrderStatus.ON_THE_WAY ->
                                order.progress
                            else -> 0f
                        }
                        Connector(fill = fill, modifier = Modifier.weight(1f))
                    }
                    TrackerNode(status = status, orderStatus = order.status)
                }
            }
            AnimatedContent(targetState = order.status, label = "stage") { status ->
                Column(Modifier.padding(top = 10.dp)) {
                    Text(
                        text = status.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
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

@Composable
private fun TrackerNode(status: OrderStatus, orderStatus: OrderStatus) {
    val reached = orderStatus >= status
    val current = orderStatus == status
    val scale = if (current && status != OrderStatus.DELIVERED) {
        val transition = rememberInfiniteTransition(label = "nodePulse")
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
            label = "nodeScale",
        ).value
    } else {
        1f
    }
    val stateLabel = when {
        current -> "current"
        reached -> "done"
        else -> "upcoming"
    }
    Surface(
        shape = CircleShape,
        color = if (reached) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .clearAndSetSemantics { contentDescription = "${status.label}: $stateLabel" },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = status.emoji,
                fontSize = 18.sp,
                modifier = Modifier.alpha(if (reached) 1f else 0.35f),
            )
        }
    }
}

@Composable
private fun Connector(fill: Float, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(targetValue = fill, animationSpec = tween(350), label = "connector")
    Box(
        modifier = modifier
            .padding(horizontal = 3.dp)
            .height(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated.coerceIn(0f, 1f))
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

@Composable
private fun ItemsCard(order: Order) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Placed ${formatOrderDate(order.placedAtMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            order.items.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))) {
                        EmojiHero(
                            emoji = item.product.emoji,
                            modifier = Modifier.fillMaxSize(),
                            fontSize = 18,
                            seed = item.product.id,
                        )
                    }
                    Text(
                        text = item.product.name + if (item.quantity > 1) "  ×${item.quantity}" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f).padding(start = 10.dp),
                    )
                }
            }
            Row(Modifier.padding(top = 4.dp)) {
                Text("Money kept", fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(
                    text = formatPrice(order.totalCents),
                    fontWeight = FontWeight.ExtraBold,
                    color = LocalSavingsColor.current,
                )
            }
        }
    }
}

private enum class UnboxPhase { Sealed, Revealing, Opened }

/**
 * The missing middle of the unbox: the box bursts open, the haul erupts as
 * big emoji and hangs proud for a beat, then floats up and evaporates into
 * nothing — the dissolve itself is the punchline, handing off to the calm
 * "nothing has arrived" truth. Hand-rolled from one 0→1 driver.
 */
@Composable
private fun UnboxingReveal(haul: List<String>, onFinished: () -> Unit) {
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        t.animateTo(1f, tween(durationMillis = 2_800, easing = LinearEasing))
        onFinished()
    }
    val p = t.value
    val burst = (p / 0.32f).coerceIn(0f, 1f)
    val dissolve = ((p - 0.64f) / 0.36f).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clearAndSetSemantics { contentDescription = "Unboxing your order" },
        ) {
            val w = maxWidth.value
            val h = maxHeight.value
            val boxX = w / 2f
            val boxY = h * 0.64f
            val rowY = h * 0.46f
            val n = haul.size
            val mid = (n - 1) / 2f
            val spacing = if (n <= 1) 0f else minOf(64f, (w * 0.74f) / n)

            // Celebratory headline; fades out as the haul evaporates.
            Text(
                text = "Ta-da! ✨",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 28.dp)
                    .alpha(burst * (1f - dissolve)),
            )

            // The box, popping its lid as the haul leaves, then dissolving too.
            Text(
                text = "📦",
                fontSize = 56.sp,
                modifier = Modifier
                    .offset(x = (boxX - 28f).dp, y = (boxY - 28f).dp)
                    .scale(1f + 0.18f * easeOutBack((burst * 1.4f).coerceAtMost(1f)) * (1f - burst * 0.4f))
                    .alpha(1f - dissolve),
            )

            haul.forEachIndexed { i, emoji ->
                val targetX = boxX + (i - mid) * spacing
                val arcLift = (mid - kotlin.math.abs(i - mid)) * 7f
                val targetY = rowY - arcLift
                val localBurst = easeOutBack(
                    (((p - i * 0.05f) / 0.32f)).coerceIn(0f, 1f),
                )
                val x = boxX + (targetX - boxX) * localBurst
                val y = boxY + (targetY - boxY) * localBurst - dissolve * 120f
                val scale = 0.2f + 0.85f * localBurst.coerceIn(0f, 1f)
                Text(
                    text = emoji,
                    fontSize = 46.sp,
                    modifier = Modifier
                        .offset(x = (x - 25f).dp, y = (y - 25f).dp)
                        .scale(scale)
                        .rotate((i - mid) * 7f)
                        .alpha(1f - dissolve),
                )
            }
        }
    }
}

/** Overshoot ease — the springy pop that makes a reveal feel alive. */
private fun easeOutBack(x: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    val u = x - 1f
    return 1f + c3 * u * u * u + c1 * u * u
}

/** The parcel wiggles just enough to say "I'm waiting"; the tap is the payoff. */
@Composable
private fun SealedParcel(onUnbox: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "parcelWiggle")
    val angle by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(420), RepeatMode.Reverse),
        label = "parcelAngle",
    )
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.clickable(onClickLabel = "Unbox your order", onClick = onUnbox),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("📦", fontSize = 64.sp, modifier = Modifier.rotate(angle))
            Text(
                text = "It's here.",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "Tap to unbox",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun DeliveredCelebration(order: Order, onShopMore: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🧘", fontSize = 64.sp)
            Text(
                text = "Nothing has arrived.",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = "Exactly as planned.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = "${animatedDollars(order.totalCents, delayMillis = 300)} still yours",
                style = MaterialTheme.typography.headlineSmall,
                color = LocalSavingsColor.current,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (order.items.any { it.product.id == FakeCatalog.mysteryBox.id }) {
                val reveal = remember(order.id) { FakeCatalog.mysteryRevealFor(order.id) }
                Text(
                    text = "Inside the Mystery Box, hypothetically:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    text = "${reveal.emoji} ${reveal.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Button(onClick = onShopMore, modifier = Modifier.padding(top = 16.dp)) {
                Text("Shop the next nothing", fontWeight = FontWeight.Bold)
            }
        }
    }
}
