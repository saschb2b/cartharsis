package com.cartharsis.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartharsis.Chime
import com.cartharsis.HoldHaptics
import com.cartharsis.ShopViewModel
import com.cartharsis.data.CardPull
import com.cartharsis.data.Courier
import com.cartharsis.data.Couriers
import com.cartharsis.data.FakeCatalog
import com.cartharsis.data.MopplingFigure
import com.cartharsis.data.MopplingWave
import com.cartharsis.data.Order
import com.cartharsis.data.OrderStatus
import com.cartharsis.data.deliveriesTogetherLine
import com.cartharsis.data.formatOrderDate
import com.cartharsis.data.formatPrice
import com.cartharsis.data.trackingCode
import com.cartharsis.ui.theme.ElectricPurple
import com.cartharsis.ui.theme.HotPink
import com.cartharsis.ui.theme.JuicyOrange
import com.cartharsis.ui.theme.LemonYellow
import com.cartharsis.ui.theme.LocalSavingsColor
import com.cartharsis.ui.theme.Motion
import com.cartharsis.ui.theme.SkyBlue
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    // The courier: usually your regular, sometimes a guest, rarely the rocket
    // one. Their own ride is the vehicle on the map, and the relationship count
    // is how many times they've delivered to you — this trip included until it
    // lands, at which point the persisted count already covers it.
    val regularId by viewModel.regularCourierId.collectAsState()
    val courierDeliveries by viewModel.courierDeliveries.collectAsState()
    val courier = remember(orderId, regularId) {
        Couriers.forOrder(orderId, regularId.ifBlank { Couriers.minjun.id })
    }
    // The map's neighborhood is seeded from the saved address, so it's the same
    // streets every time you track, and different from someone at another address.
    val profile by viewModel.profile.collectAsState()
    val citySeed = remember(profile?.street, profile?.city) {
        (profile?.street.orEmpty() + "|" + profile?.city.orEmpty()).hashCode().toLong()
    }
    val completedWithCourier = courierDeliveries[courier.id] ?: 0
    val nthDelivery = if (order.status == OrderStatus.DELIVERED) {
        maxOf(1, completedWithCourier)
    } else {
        completedWithCourier + 1
    }

    // The screen has two faces: the in-transit map + bottom sheet, and the
    // delivered unbox ceremony. AnimatedContent crossfades between them on a
    // spatial spring, so arrival eases from one layout into the other (the
    // parcel scaling up as the map fades) instead of hard-cutting.
    AnimatedContent(
        targetState = order.status == OrderStatus.DELIVERED,
        transitionSpec = {
            (fadeIn(Motion.effects()) + scaleIn(initialScale = 0.92f, animationSpec = Motion.spatial()))
                .togetherWith(fadeOut(Motion.effects()))
        },
        label = "trackingPhase",
    ) { delivered ->
        if (!delivered) {
            TransitTracking(
                order = order,
                courier = courier,
                nthDelivery = nthDelivery,
                citySeed = citySeed,
                onBack = onBack,
            )
            return@AnimatedContent
        }

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                    run {
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
                        // A trading-card order unboxes into the pack-rip ceremony
                        // (tear the foil, tap through the commons, flip the chase)
                        // instead of the generic haul burst. Every pack gets its
                        // own rip — one per quantity of each card line, each with
                        // its own deal — capped so the ceremony stays a ceremony.
                        val ripPacks = remember(order.id) {
                            order.items
                                .flatMap { line -> List(line.quantity) { i -> line.product to i } }
                                .mapNotNull { (product, i) ->
                                    FakeCatalog.packRipFor(order.id, product, i)?.let { cards ->
                                        RipPack(
                                            game = product.variantGroup!!.substringBefore('-'),
                                            seriesGroup = product.variantGroup!!,
                                            series = FakeCatalog.cardSeriesTitles[product.variantGroup].orEmpty(),
                                            cards = cards,
                                        )
                                    }
                                }
                                .take(MAX_PACK_RIPS)
                        }
                        // A blind-box order unboxes into the shake-and-pop reveal
                        // — one opening per unit, capped like the pack rips.
                        val blindBoxes = remember(order.id) {
                            order.items
                                .flatMap { line -> List(line.quantity) { i -> line.product to i } }
                                .mapNotNull { (product, i) ->
                                    FakeCatalog.mopplingPullsFor(order.id, product, i)
                                }
                                .take(MAX_PACK_RIPS)
                        }
                        // Whether the parcel holds its own follow-on ceremony (a
                        // card rip or a blind-box shake). If so the shipping box
                        // opens on a single tap, because the suspense lives in the
                        // tear/shake to come; otherwise it's pried open over a few
                        // escalating taps — that pry *is* the generic haul's
                        // anticipation beat.
                        val hasInnerCeremony = ripPacks.isNotEmpty() || blindBoxes.isNotEmpty()
                        // `unboxed` (ViewModel) is the durable "opened ever" flag;
                        // `revealing` is local so the burst plays only on the live
                        // tap, never on a revisit. Mixed orders rip first, then
                        // shake — every ceremony in the parcel gets its moment.
                        var revealing by remember(orderId) { mutableStateOf(false) }
                        var ripsDone by remember(orderId) { mutableStateOf(false) }
                        val phase = when {
                            !unboxed -> UnboxPhase.Sealed
                            revealing && ripPacks.isNotEmpty() && !ripsDone -> UnboxPhase.Ripping
                            revealing && blindBoxes.isNotEmpty() -> UnboxPhase.Shaking
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
                                    prySteps = if (hasInnerCeremony) 1 else PRY_TAPS_TO_OPEN,
                                    onUnbox = {
                                        revealing = true
                                        viewModel.markUnboxed(order.id)
                                        // The box-open crack for the inner
                                        // ceremonies. The generic haul instead
                                        // fires its own synced climax
                                        // (haptic+chime+confetti) at the burst in
                                        // UnboxingReveal, so nothing fires here.
                                        if (hasInnerCeremony) {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                    },
                                )
                                UnboxPhase.Ripping -> {
                                    var packIdx by remember(order.id) { mutableIntStateOf(0) }
                                    val lastPack = packIdx == ripPacks.lastIndex
                                    AnimatedContent(
                                        targetState = packIdx,
                                        label = "packQueue",
                                        transitionSpec = {
                                            (
                                                fadeIn(Motion.effects()) +
                                                    scaleIn(initialScale = 0.92f, animationSpec = Motion.spatial())
                                                ).togetherWith(fadeOut(Motion.effects()))
                                        },
                                    ) { idx ->
                                        PackRipReveal(
                                            pack = ripPacks[idx],
                                            packNumber = idx + 1,
                                            packCount = ripPacks.size,
                                            onChaseRevealed = {
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                Chime.playSuccess()
                                                // The flipped chase goes into the
                                                // persistent binder.
                                                viewModel.recordPull(
                                                    game = ripPacks[idx].game,
                                                    card = ripPacks[idx].cards.last(),
                                                )
                                                // Every chase lands with haptic+chime;
                                                // the confetti is the finale's.
                                                if (lastPack) celebrate = true
                                            },
                                            onFinished = {
                                                if (!lastPack) {
                                                    packIdx += 1
                                                } else if (blindBoxes.isNotEmpty()) {
                                                    ripsDone = true
                                                } else {
                                                    revealing = false
                                                }
                                            },
                                        )
                                    }
                                }
                                UnboxPhase.Shaking -> {
                                    var boxIdx by remember(order.id) { mutableIntStateOf(0) }
                                    val lastBox = boxIdx == blindBoxes.lastIndex
                                    AnimatedContent(
                                        targetState = boxIdx,
                                        label = "boxQueue",
                                        transitionSpec = {
                                            (
                                                fadeIn(Motion.effects()) +
                                                    scaleIn(initialScale = 0.92f, animationSpec = Motion.spatial())
                                                ).togetherWith(fadeOut(Motion.effects()))
                                        },
                                    ) { idx ->
                                        val (wave, figures) = blindBoxes[idx]
                                        BlindBoxReveal(
                                            wave = wave,
                                            figures = figures,
                                            boxNumber = idx + 1,
                                            boxCount = blindBoxes.size,
                                            onOpened = {
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                Chime.playSuccess()
                                                // Revealed figures go onto the
                                                // persistent Moppling shelf.
                                                viewModel.recordMopplings(wave.key, figures)
                                                // Every pop lands with haptic+chime;
                                                // the confetti is the finale's.
                                                if (lastBox) celebrate = true
                                            },
                                            onFinished = {
                                                if (lastBox) revealing = false else boxIdx += 1
                                            },
                                        )
                                    }
                                }
                                UnboxPhase.Revealing -> UnboxingReveal(
                                    haul = haul,
                                    onReveal = { celebrate = true },
                                    onFinished = { revealing = false },
                                )
                                UnboxPhase.Opened -> DeliveredCelebration(order, courier, nthDelivery, onShopMore)
                            }
                        }
                    }
                    // The same vertical timeline the transit map uses — here the
                    // full log, all five stages latest-first — in a card so it
                    // sits among the ceremony's other cards.
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = "Delivery timeline",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp),
                            )
                            DeliveryTimeline(order)
                        }
                    }
                    ItemsCard(order)
                }
                if (celebrate) {
                    ConfettiOverlay(Modifier.fillMaxSize(), durationMillis = 2_600)
                }
            }
        }
    }
}

// Whimsical waypoints the nothing passes through while on the way — the
// header card's "current location", bucketed on trip progress.
private val transitWaypoints = listOf(
    "Imaginary Highway, Exit 0",
    "Midtown, paused at a red light",
    "Crossing the Anticipation Bridge",
    "Anticipation Street, Dopamine City",
)

private fun currentLocation(order: Order): String = when {
    order.status < OrderStatus.ON_THE_WAY -> "Cartharsis fulfillment void"
    order.status == OrderStatus.ON_THE_WAY -> {
        val i = (order.progress * transitWaypoints.size).toInt().coerceIn(0, transitWaypoints.lastIndex)
        transitWaypoints[i]
    }
    else -> "Your doorstep, Dopamine City"
}

/**
 * The in-transit tracking experience — the redesigned concept: a full-bleed
 * routed map, an overlapping order header card, the delivery timeline, and
 * the order summary. Its own full-bleed layout with a floating back button,
 * no app bar; the delivered branch keeps the ceremony's Scaffold.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransitTracking(order: Order, courier: Courier, nthDelivery: Int, citySeed: Long, onBack: () -> Unit) {
    // Map + bottom sheet (researched): the live essentials — status, ETA, and
    // the courier — sit in the sheet's peek, glanceable above the fold; the
    // detailed timeline and order summary are revealed by dragging the sheet up
    // (progressive disclosure) rather than stacked down a long scroll.
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true,
        ),
    )
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 300.dp,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetShadowElevation = 8.dp,
        containerColor = MaterialTheme.colorScheme.background,
        sheetContent = { TransitSheet(order, courier, nthDelivery) },
    ) {
        // The map fills the whole area behind the sheet; the destination and the
        // courier's approach stay above the peek, the origin tucks behind it.
        RouteMap(
            progress = if (order.status >= OrderStatus.ON_THE_WAY) order.progress else 0f,
            onTheWay = order.status == OrderStatus.ON_THE_WAY,
            vehicle = courier.vehicle,
            onBack = onBack,
            fill = true,
            citySeed = citySeed,
            orderId = order.id,
        )
    }
}

/** The sheet over the map: the hero (status/ETA/courier) in the peek, the rest below. */
@Composable
private fun TransitSheet(order: Order, courier: Courier, nthDelivery: Int) {
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
    ) {
        StatusHero(order)
        Spacer(Modifier.height(16.dp))
        // The person behind the delivery, in the glanceable peek.
        CourierCard(courier = courier, nthDelivery = nthDelivery)
        if (order.status == OrderStatus.ON_THE_WAY) {
            // The en-route moments, in this courier's own voice.
            val moments = courier.moments
            val index = (order.progress * moments.size).toInt().coerceAtMost(moments.lastIndex)
            AnimatedContent(targetState = index, label = "moment") { i ->
                Text(
                    text = moments[i],
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp, start = 4.dp),
                )
            }
        }
        // Below the peek: the full history and the order summary, on drag-up.
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Delivery updates",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp),
        )
        Column(Modifier.padding(top = 12.dp, start = 4.dp)) {
            DeliveryTimeline(order = order)
        }
        Spacer(Modifier.height(16.dp))
        ItemsCard(order)
    }
}

/** The glanceable hero: current status, a whimsical ETA, a slim progress track, location. */
@Composable
internal fun StatusHero(order: Order) {
    val fraction by animateFloatAsState(targetValue = overallFraction(order), label = "trackProgress")
    Column(Modifier.padding(top = 8.dp)) {
        Text(
            text = order.status.label,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = etaLine(order),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = LocalSavingsColor.current,
            modifier = Modifier.padding(top = 2.dp),
        )
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(HotPink, ElectricPurple))),
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📍", fontSize = 13.sp)
            Text(
                text = currentLocation(order),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f).padding(start = 6.dp),
            )
            Text(
                text = trackingCode(order.id),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** A whimsical ETA — anticipation is the point, and it's post-commit, so it's fair game. */
private fun etaLine(order: Order): String = when (order.status) {
    OrderStatus.CONFIRMED -> "Confirmed. Preparing your nothing."
    OrderStatus.PACKING -> "Carefully wrapping the void"
    OrderStatus.COURIER_ASSIGNED -> "Your courier is heading out"
    OrderStatus.ON_THE_WAY -> {
        val remaining = ((1f - order.progress) * ShopViewModel.COURIER_TRIP_SECONDS).roundToInt()
        if (remaining > 3) "Arriving in ~${remaining}s" else "Seconds away"
    }
    OrderStatus.DELIVERED -> "Arrived. Exactly as planned."
}

/** Overall completion across the five stages, for the hero's slim progress track. */
private fun overallFraction(order: Order): Float = when (order.status) {
    OrderStatus.CONFIRMED -> 0.06f
    OrderStatus.PACKING -> 0.22f
    OrderStatus.COURIER_ASSIGNED -> 0.42f
    OrderStatus.ON_THE_WAY -> 0.5f + order.progress * 0.46f
    OrderStatus.DELIVERED -> 1f
}

/**
 * The courier, made a person: their face, name, rating, the ride they actually
 * use, a line of who they are, and how many times they've delivered to you.
 */
@Composable
internal fun CourierCard(courier: Courier, nthDelivery: Int) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar, with their ride badged on the corner.
            Box {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(courier.avatar, fontSize = 26.sp)
                }
                Text(
                    text = courier.vehicle,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp),
                )
            }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = courier.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "★ ${courier.rating}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = courier.tagline,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 1.dp),
                )
                Text(
                    text = deliveriesTogetherLine(courier.name, nthDelivery),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LocalSavingsColor.current,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
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

private enum class UnboxPhase { Sealed, Ripping, Shaking, Revealing, Opened }

/**
 * The missing middle of the unbox: the box bursts open, the haul erupts as
 * big emoji and hangs proud for a beat, then floats up and evaporates into
 * nothing — the dissolve itself is the punchline, handing off to the calm
 * "nothing has arrived" truth. Hand-rolled from one 0→1 driver.
 */
/** The reveal's whole arc. Long enough to let the haul *dwell* — the
 *  inspection beat the old 2.8s version skipped by dissolving the haul almost
 *  as soon as it appeared. */
private const val REVEAL_TOTAL_MS = 3_800

@Composable
private fun UnboxingReveal(haul: List<String>, onReveal: () -> Unit, onFinished: () -> Unit) {
    val context = LocalContext.current
    val vibrator = remember { HoldHaptics.vibrator(context) }
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        // The burst IS the climax: thunk, chime, and confetti fire on the same
        // frame the haul explodes out — not on the tap that triggered it. (The
        // old path fired the confetti at the tap and never played the chime.)
        HoldHaptics.thunk(vibrator)
        Chime.playSuccess()
        onReveal()
        t.animateTo(1f, tween(durationMillis = REVEAL_TOTAL_MS, easing = LinearEasing))
        onFinished()
    }
    val p = t.value
    // Bursts out fast (~0.5s), holds for a long inspection dwell, then dissolves
    // only in the final ~0.75s as it settles into the resting payoff.
    val burst = (p / 0.13f).coerceIn(0f, 1f)
    val dissolve = ((p - 0.80f) / 0.20f).coerceIn(0f, 1f)

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
                    (((p - i * 0.022f) / 0.13f)).coerceIn(0f, 1f),
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

/** How many taps it takes to pry the generic shipping box open — the
 *  anticipation beat. Each non-final tap jolts and strains the lid looser with
 *  a climbing haptic; the last one pops it. Tunable for snappier/longer build. */
private const val PRY_TAPS_TO_OPEN = 4

/**
 * The sealed parcel. With [prySteps] > 1 it's pried open over that many
 * escalating taps — the lid jolts and strains looser each time, a haptic tick
 * climbing with it, until the final tap pops it (the generic haul's
 * anticipation, per the loot-box research: the build-up is the reward). With
 * [prySteps] == 1 it opens on one tap, for parcels whose suspense lives in the
 * tear/shake that follows. Idle, it wiggles just enough to say "I'm waiting."
 */
@Composable
private fun SealedParcel(prySteps: Int, onUnbox: () -> Unit) {
    val context = LocalContext.current
    val vibrator = remember { HoldHaptics.vibrator(context) }
    val scope = rememberCoroutineScope()
    var pries by remember { mutableIntStateOf(0) }
    // Strain ratchets up and holds (the lid creeping open); jolt is a per-tap
    // recoil kick that springs back, flipping side each tap for a real shake.
    val strain = remember { Animatable(0f) }
    val jolt = remember { Animatable(0f) }
    var joltDir by remember { mutableIntStateOf(1) }

    val transition = rememberInfiniteTransition(label = "parcelWiggle")
    val wiggle by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(420), RepeatMode.Reverse),
        label = "parcelAngle",
    )

    fun pry() {
        if (pries >= prySteps) return
        pries += 1
        if (pries < prySteps) {
            val frac = pries.toFloat() / prySteps
            HoldHaptics.tick(vibrator, 0.4f + 0.55f * frac)
            joltDir = -joltDir
            scope.launch { strain.animateTo(frac, Motion.spatialFast()) }
            scope.launch {
                jolt.snapTo(0.4f + 0.6f * frac)
                jolt.animateTo(0f, spring(dampingRatio = 0.32f, stiffness = Spring.StiffnessMedium))
            }
        } else {
            // The pop. The crack/boom belongs to the burst that follows, so the
            // tap just hands off — no big haptic competing here.
            onUnbox()
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.clickable(
            onClickLabel = if (prySteps > 1) "Pry your parcel open" else "Unbox your order",
            onClick = { pry() },
        ),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "📦",
                fontSize = 64.sp,
                modifier = Modifier.graphicsLayer {
                    val s = strain.value
                    // The idle wiggle fades as the lid is gripped and strained.
                    rotationZ = wiggle * (1f - s) + jolt.value * 11f * joltDir
                    scaleX = 1f + 0.07f * s + jolt.value * 0.05f
                    scaleY = 1f + 0.07f * s + jolt.value * 0.05f
                    translationY = -s * 7.dp.toPx()
                },
            )
            Text(
                text = if (prySteps > 1 && pries > 0) "Almost…" else "It's here.",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = when {
                    prySteps <= 1 -> "Tap to unbox"
                    pries == 0 -> "Tap to pry it open"
                    else -> "It's coming loose — keep prying"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun DeliveredCelebration(order: Order, courier: Courier, nthDelivery: Int, onShopMore: () -> Unit) {
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
                text = "${animatedMoney(order.totalCents, delayMillis = 300)} still yours",
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
            // The pack-rip moment for trading-card orders: a seeded chase-card
            // "pull", revealed only here — after the courier, never before.
            val pull = remember(order.id) {
                order.items.firstNotNullOfOrNull { FakeCatalog.cardPullFor(order.id, it.product) }
            }
            if (pull != null) {
                Text(
                    text = "Your top pull, hypothetically:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    text = "${pull.emoji} ${pull.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    text = "${pull.rarity} · mint forever",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            // The note at the door: the one person who showed up, signing off.
            Spacer(Modifier.height(20.dp))
            CourierSignoff(courier = courier, nthDelivery = nthDelivery)
            Button(onClick = onShopMore, modifier = Modifier.padding(top = 16.dp)) {
                Text("Shop the next nothing", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** The courier's handwritten-feeling note, left at the door on arrival. */
@Composable
internal fun CourierSignoff(courier: Courier, nthDelivery: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(courier.avatar, fontSize = 20.sp)
                }
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text(
                        text = "A note from ${courier.name}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = deliveriesTogetherLine(courier.name, nthDelivery),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "★ ${courier.rating}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = courier.signoff,
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

// ---- The pack rip ----
// A trading-card order doesn't dump its haul; it stages it the way the
// genre's best opener does: present the pack, make the player tear the foil
// themselves, deal the commons one tap at a time, and save the chase card
// for last behind a glow and a flip. Gesture → suspense → resolution; the
// confetti, haptic, and chime all fire together at the flip's completion.

/** Rips per order, max — enough to feel abundant, few enough to stay a ceremony. */
private const val MAX_PACK_RIPS = 3

/** One rip's worth: which game, which series wrapper, and the five cards. */
private data class RipPack(val game: String, val seriesGroup: String, val series: String, val cards: List<CardPull>)

internal data class PackTheme(val game: String, val title: String, val emoji: String, val wrapper: List<Color>)

/**
 * The game's dress. With a [seriesGroup], the wrapper tints to that set —
 * Abyssal Tides doesn't ship in Emberglow's flames — while card faces and
 * backs keep the game-wide theme, the way real games keep one back forever.
 */
internal fun packTheme(game: String, seriesGroup: String? = null): PackTheme {
    val title = FakeCatalog.cardGameTitles[game] ?: "Trading Cards"
    val base = when (game) {
        "critters" -> PackTheme(game, title, "🐲", listOf(JuicyOrange, HotPink))
        "duelbound" -> PackTheme(game, title, "🃏", listOf(Color(0xFF4527A0), Color(0xFF1A1233)))
        else -> PackTheme(game, title, "🔮", listOf(SkyBlue, ElectricPurple))
    }
    val seriesWrapper = when (seriesGroup) {
        "critters-abyssal" -> listOf(Color(0xFF00ACC1), Color(0xFF1A237E))
        "duelbound-eclipse" -> listOf(Color(0xFFC62828), Color(0xFF260A12))
        "manaforge-verdant" -> listOf(Color(0xFF66BB6A), Color(0xFF1B5E20))
        else -> null
    }
    return if (seriesWrapper != null) base.copy(wrapper = seriesWrapper) else base
}

@Composable
private fun PackRipReveal(
    pack: RipPack,
    packNumber: Int,
    packCount: Int,
    onChaseRevealed: () -> Unit,
    onFinished: () -> Unit,
) {
    val theme = remember(pack.game) { packTheme(pack.game) }
    val wrapperTheme = remember(pack.seriesGroup) { packTheme(pack.game, pack.seriesGroup) }
    var torn by remember { mutableStateOf(false) }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        if (packCount > 1) {
            Text(
                text = "Pack $packNumber of $packCount",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AnimatedContent(
            targetState = torn,
            label = "rip",
            transitionSpec = {
                (
                    fadeIn(Motion.effects()) +
                        scaleIn(initialScale = 0.92f, animationSpec = Motion.spatial())
                    ).togetherWith(fadeOut(Motion.effects()))
            },
        ) { isTorn ->
            if (!isTorn) {
                BoosterPackTear(wrapperTheme, series = pack.series, onTorn = { torn = true })
            } else {
                CardStackReveal(
                    theme = theme,
                    cards = pack.cards,
                    finishLabel = if (packNumber < packCount) "Rip the next pack" else "Take your haul",
                    onChaseRevealed = onChaseRevealed,
                    onFinished = onFinished,
                )
            }
        }
    }
}

/**
 * Stage one: the sealed booster, bobbing gently, opened by dragging across
 * the foil. The tear follows the finger with a haptic tick at every fifth;
 * letting go early springs it shut. Taps nudge the tear too, so the moment
 * never gates on a gesture someone can't make.
 */
@Composable
internal fun BoosterPackTear(theme: PackTheme, series: String, onTorn: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    var dragging by remember { mutableStateOf(false) }
    var tearTarget by remember { mutableFloatStateOf(0f) }
    var lastTick by remember { mutableIntStateOf(0) }
    var done by remember { mutableStateOf(false) }
    val tear by animateFloatAsState(
        targetValue = tearTarget,
        animationSpec = if (dragging) snap() else spring(stiffness = Spring.StiffnessMedium),
        label = "tear",
    )
    val fly = remember { Animatable(0f) }

    fun advanceTear(by: Float) {
        if (done) return
        tearTarget = (tearTarget + by).coerceIn(0f, 1f)
        val tick = (tearTarget * 5).toInt()
        if (tick > lastTick) {
            lastTick = tick
            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
        }
        if (tearTarget >= 1f) done = true
    }

    LaunchedEffect(done) {
        if (done) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            fly.animateTo(1f, tween(durationMillis = 420, easing = FastOutSlowInEasing))
            delay(180)
            onTorn()
        }
    }

    val idle = rememberInfiniteTransition(label = "packIdle")
    val bob by idle.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1700, easing = LinearEasing), RepeatMode.Reverse),
        label = "bob",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(230.dp, 330.dp)
                .graphicsLayer {
                    if (!done) {
                        translationY = bob * 5.dp.toPx() * (1f - tear)
                        rotationZ = bob * 1.2f * (1f - tear)
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragging = true },
                        onDragCancel = {
                            dragging = false
                            if (!done) tearTarget = 0f
                        },
                        onDragEnd = {
                            dragging = false
                            if (!done) tearTarget = 0f
                        },
                        // One committed swipe across the pack completes the
                        // tear — the divisor stays well under the pack width
                        // so the gesture never falls just short and snaps back.
                    ) { _, dragAmount -> advanceTear(abs(dragAmount) / (size.width * 0.7f)) }
                }
                .clickable(onClickLabel = "Rip the pack open") {
                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    advanceTear(0.34f)
                },
        ) {
            // Pack body — everything below the tear line.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(theme.wrapper)),
            ) {
                // The exposed inner foil, waiting under the strip.
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .background(Color.Black.copy(alpha = 0.35f)),
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.matchParentSize().padding(top = 46.dp),
                ) {
                    Text(theme.emoji, fontSize = 76.sp, modifier = Modifier.clearAndSetSemantics {})
                    Text(
                        text = theme.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    if (series.isNotBlank()) {
                        // The set is the thing being collected — it gets the
                        // second-loudest line on the wrapper.
                        Text(
                            text = series.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = LemonYellow,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text(
                        text = "BOOSTER PACK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
                // A still diagonal gloss, so the wrapper reads as foil.
                Canvas(Modifier.matchParentSize()) {
                    drawRect(
                        Brush.linearGradient(
                            0.0f to Color.Transparent,
                            0.38f to Color.Transparent,
                            0.5f to Color.White.copy(alpha = 0.10f),
                            0.62f to Color.Transparent,
                            1.0f to Color.Transparent,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, 0f),
                        ),
                    )
                }
                // The contents promise, like the real wrappers print — no
                // card count (the listing states the pack size; the rip
                // shows the highlights), just the one promise every pack
                // keeps: the last card up is a foil.
                Text(
                    text = "GUARANTEED FOIL INSIDE",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    letterSpacing = 1.5.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 22.dp),
                )
                // Bottom crimp, serrated like pressed foil.
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.18f)),
                ) { CrimpSerration() }
            }
            // The foil strip that tears away.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .graphicsLayer {
                        translationX = tear * 12.dp.toPx() + fly.value * 360.dp.toPx()
                        translationY = -fly.value * 160.dp.toPx()
                        rotationZ = fly.value * 24f
                        alpha = 1f - fly.value * 0.9f
                    }
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Brush.linearGradient(theme.wrapper))
                    .background(Color.White.copy(alpha = 0.12f)),
            ) {
                CrimpSerration()
                Text(
                    text = "✂ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1,
                    modifier = Modifier.align(Alignment.BottomStart).padding(start = 10.dp, bottom = 2.dp),
                )
            }
            // The tear itself: a jagged white edge chasing the finger.
            Canvas(Modifier.matchParentSize()) {
                if (tear > 0.01f) {
                    val y = 46.dp.toPx()
                    val jag = 3.dp.toPx()
                    val path = Path().apply {
                        moveTo(0f, y)
                        var x = 0f
                        var up = true
                        while (x < size.width * tear) {
                            x += 9.dp.toPx()
                            lineTo(minOf(x, size.width * tear), if (up) y - jag else y + jag)
                            up = !up
                        }
                    }
                    drawPath(path, Color.White, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
                }
            }
        }
        Text(
            text = if (done) "Ripped!" else "Swipe across the foil to rip it open",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 12.dp)
                .alpha(if (done) 1f else 0.55f + 0.45f * (1f - abs(bob))),
        )
    }
}

/**
 * Stage two: the cards, dealt one tap at a time — commons up front, the
 * chase card last, face-down under a building glow. The flip is the
 * resolution: confetti, haptic, and chime fire together as it lands.
 */
@Composable
private fun CardStackReveal(
    theme: PackTheme,
    cards: List<CardPull>,
    finishLabel: String,
    onChaseRevealed: () -> Unit,
    onFinished: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var index by remember { mutableIntStateOf(0) }
    var chaseFlipped by remember { mutableStateOf(false) }
    val exit = remember { Animatable(0f) }
    val flip = remember { Animatable(0f) }
    val isChase = index == cards.lastIndex

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .clickable(
                    onClickLabel = when {
                        !isChase -> "Next card"
                        !chaseFlipped -> "Flip the last card"
                        else -> "Continue"
                    },
                ) {
                    when {
                        !isChase -> scope.launch {
                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            exit.animateTo(1f, tween(durationMillis = 230, easing = FastOutSlowInEasing))
                            index += 1
                            exit.snapTo(0f)
                        }
                        !chaseFlipped -> scope.launch {
                            // The chase reveal lands on a spatial spring — a
                            // small overshoot as the card settles face-up.
                            flip.animateTo(180f, Motion.spatial())
                            chaseFlipped = true
                            onChaseRevealed()
                        }
                        else -> onFinished()
                    }
                },
        ) {
            if (isChase) ChaseGlow(flipped = chaseFlipped)
            // The next card peeking from behind; the chase always peeks face-down.
            if (!isChase) {
                RipCardFace(
                    card = cards[index + 1],
                    theme = theme,
                    faceDown = index + 1 == cards.lastIndex,
                    modifier = Modifier.scale(0.92f).alpha(0.65f),
                )
            }
            if (!isChase) {
                RipCardFace(
                    card = cards[index],
                    theme = theme,
                    faceDown = false,
                    modifier = Modifier.graphicsLayer {
                        translationY = -exit.value * 850f
                        rotationZ = -exit.value * 13f
                        alpha = 1f - exit.value * 0.5f
                    },
                )
            } else {
                Box(
                    Modifier.graphicsLayer {
                        rotationY = flip.value
                        cameraDistance = 16f * density
                    },
                ) {
                    if (flip.value < 90f) {
                        RipCardFace(cards[index], theme, faceDown = true)
                    } else {
                        Box(Modifier.graphicsLayer { rotationY = 180f }) {
                            RipCardFace(cards[index], theme, faceDown = false, holo = true)
                        }
                    }
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .padding(top = 10.dp)
                .clearAndSetSemantics { contentDescription = "Card ${index + 1} of ${cards.size}" },
        ) {
            cards.indices.forEach { i ->
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (i < index || (i == index && (!isChase || chaseFlipped))) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                        ),
                )
            }
        }
        when {
            !isChase -> Text(
                text = "Tap for the next card",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp),
            )
            !chaseFlipped -> Text(
                text = "The last one feels heavier. Tap to flip.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 10.dp),
            )
            else -> Button(onClick = onFinished, modifier = Modifier.padding(top = 10.dp)) {
                Text(finishLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** The pulsing halo that says "this one matters" before the chase flip. */
@Composable
private fun ChaseGlow(flipped: Boolean) {
    val t = rememberInfiniteTransition(label = "glow")
    val pulse by t.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(850, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    Canvas(Modifier.size(340.dp).scale(if (flipped) pulse * 1.12f else pulse)) {
        drawCircle(
            Brush.radialGradient(
                listOf(LemonYellow.copy(alpha = if (flipped) 0.5f else 0.32f), Color.Transparent),
            ),
        )
    }
}

/**
 * One card of the rip. The face and back both come from per-game designs:
 * GameCardFace lays the front out in its genre's structure (CardFaces.kt),
 * GameCardBack paints the game's permanent back (CardBacks.kt).
 */
@Composable
internal fun RipCardFace(
    card: CardPull,
    theme: PackTheme,
    faceDown: Boolean,
    modifier: Modifier = Modifier,
    holo: Boolean = false,
) {
    Box(
        modifier = modifier
            .size(220.dp, 308.dp)
            .clearAndSetSemantics {
                contentDescription = when {
                    faceDown -> "A face-down card"
                    card.type.isBlank() -> "${card.name}, ${card.rarity}"
                    else -> "${card.name}, ${card.type}, ${card.rarity}"
                }
            }
            .clip(RoundedCornerShape(14.dp)),
    ) {
        if (faceDown) {
            // The back owns its whole surface, border included, the way the
            // real ones do — no wrapper frame. One back per game, forever.
            GameCardBack(theme, Modifier.matchParentSize())
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Brush.linearGradient(theme.wrapper))
                    .padding(7.dp),
            ) {
                GameCardFace(card, theme, holo)
            }
        }
    }
}

/** The wave's wrapper colors — each lineup boxes itself differently. */
private fun waveColors(key: String): List<Color> = when (key) {
    "bog" -> listOf(Color(0xFF6A994E), Color(0xFF386641))
    "cloud" -> listOf(SkyBlue, ElectricPurple)
    "charm" -> listOf(HotPink, JuicyOrange)
    else -> listOf(Color(0xFF8D99AE), Color(0xFF5C677D))
}

/**
 * The blind-box ceremony: a sealed mystery box that wants shaking. Three
 * taps — each a wobble and a haptic tick — then the pop: figures spring
 * in with haptic, chime and (on the last box) the confetti, together.
 * Gesture → suspense → resolution, the house rules.
 */
@Composable
private fun BlindBoxReveal(
    wave: MopplingWave,
    figures: List<MopplingFigure>,
    boxNumber: Int,
    boxCount: Int,
    onOpened: () -> Unit,
    onFinished: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    var shakes by remember(boxNumber) { mutableIntStateOf(0) }
    val opened = shakes >= 3
    val wobble = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        if (boxCount > 1) {
            Text(
                text = "Box $boxNumber of $boxCount",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AnimatedContent(
            targetState = opened,
            label = "boxPop",
            transitionSpec = {
                (
                    scaleIn(
                        initialScale = 0.7f,
                        animationSpec = Motion.spatialExpressive(),
                    ) + fadeIn(Motion.effects())
                    ).togetherWith(fadeOut(Motion.effects()))
            },
        ) { isOpen ->
            if (!isOpen) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .size(190.dp, 205.dp)
                            .graphicsLayer { rotationZ = wobble.value }
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.linearGradient(waveColors(wave.key)))
                            .clickable(onClickLabel = "Shake the box") {
                                shakes += 1
                                if (shakes >= 3) {
                                    onOpened()
                                } else {
                                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    scope.launch {
                                        wobble.snapTo(0f)
                                        wobble.animateTo(9f, tween(70))
                                        wobble.animateTo(-7f, tween(90))
                                        wobble.animateTo(
                                            0f,
                                            spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                        )
                                    }
                                }
                            },
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("?", fontSize = 58.sp, color = Color.White.copy(alpha = 0.95f))
                            Text(
                                text = "MOPPLING",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                color = Color.White,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                            Text(
                                text = wave.title.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 1.5.sp,
                                color = LemonYellow,
                            )
                        }
                        CrimpSerration()
                    }
                    Text(
                        text = when (shakes) {
                            0 -> "Shake it. You know you want to."
                            1 -> "Something shifted in there."
                            else -> "One more shake."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) {
                        figures.forEach { figure ->
                            FigureCell(figure, wave, modifier = Modifier.padding(horizontal = 5.dp))
                        }
                    }
                    Button(
                        onClick = onFinished,
                        modifier = Modifier.padding(top = 14.dp),
                    ) {
                        Text(if (boxNumber == boxCount) "Take your haul" else "Open the next box")
                    }
                }
            }
        }
    }
}

/** One revealed figure: the toy, its name, and its slot in the lineup. */
@Composable
private fun FigureCell(figure: MopplingFigure, wave: MopplingWave, modifier: Modifier = Modifier) {
    val slot = wave.figures.indexOfFirst { it.name == figure.name } + 1
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "${figure.name}, ${wave.title} figure $slot of ${wave.figures.size}"
        },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(waveColors(wave.key).map { it.copy(alpha = 0.25f) })),
        ) {
            Text(figure.emoji, fontSize = 30.sp)
        }
        Text(
            text = figure.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = "№$slot of ${wave.figures.size}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** The pressed-foil texture on a wrapper crimp: faint vertical ticks. */
@Composable
private fun CrimpSerration() {
    Canvas(Modifier.fillMaxSize()) {
        val step = 5.dp.toPx()
        var x = step / 2f
        while (x < size.width) {
            drawLine(
                color = Color.White.copy(alpha = 0.10f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1.5.dp.toPx(),
            )
            x += step
        }
    }
}
