package com.cartharsis.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartharsis.data.Order
import com.cartharsis.data.OrderStatus
import com.cartharsis.data.trackingCode
import com.cartharsis.ui.theme.ElectricPurple
import com.cartharsis.ui.theme.HotPink
import com.cartharsis.ui.theme.MintGreen
import kotlin.math.hypot

// The map "paper" and its road network — a stylized city, not a real one.
private val MapPaper = Color(0xFFE7E4EE)
private val MapPaperLo = Color(0xFFDCD8E6)
private val MapRoad = Color(0xFFF7F5FB)
private val MapRoadShadow = Color(0xFFCBC6D6)
private val MapBlock = Color(0xFFEFEDF4)

// The delivery route, as normalized (0..1) waypoints — an angular, routed
// path the way a real map app draws turn-by-turn, stepping up and to the
// right from the origin dot to the destination pin.
private val ROUTE = listOf(
    Offset(0.12f, 0.78f),
    Offset(0.12f, 0.52f),
    Offset(0.44f, 0.52f),
    Offset(0.44f, 0.30f),
    Offset(0.82f, 0.30f),
)

/** The point a fraction [t] of the way along [ROUTE], by arc length. */
private fun pointAlongRoute(t: Float): Offset {
    val clamped = t.coerceIn(0f, 1f)
    val lengths = ROUTE.zipWithNext { a, b -> hypot(b.x - a.x, b.y - a.y) }
    val total = lengths.sum()
    if (total == 0f) return ROUTE.first()
    var target = clamped * total
    for (i in lengths.indices) {
        if (target <= lengths[i]) {
            val f = if (lengths[i] == 0f) 0f else target / lengths[i]
            return Offset(
                ROUTE[i].x + (ROUTE[i + 1].x - ROUTE[i].x) * f,
                ROUTE[i].y + (ROUTE[i + 1].y - ROUTE[i].y) * f,
            )
        }
        target -= lengths[i]
    }
    return ROUTE.last()
}

/**
 * The tracking map: a stylized street grid with a routed accent path from the
 * origin dot to the destination pin, the courier crawling along it, and a
 * floating back button — the centerpiece of the redesigned tracking screen.
 * The map is decorative (nothing is really being delivered), so the grid is
 * fixed rather than seeded.
 */
@Composable
internal fun RouteMap(
    progress: Float,
    onTheWay: Boolean,
    vehicle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Same goal-gradient easing the old map used, so the courier and the
    // filled trail accelerate into the destination together.
    val eased = (progress * progress + progress) / 2f
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(330.dp)
            .background(Brush.verticalGradient(listOf(MapPaper, MapPaperLo)))
            .clearAndSetSemantics { contentDescription = "Delivery route map" },
    ) {
        val w = maxWidth.value
        val h = maxHeight.value
        Canvas(Modifier.fillMaxSize()) {
            drawStreetGrid()
            drawRoute(eased)
        }
        // Origin marker (the fake store) and destination (home), placed in dp.
        RouteDot(
            color = ElectricPurple,
            modifier = Modifier.offset {
                IntOffset(
                    (ROUTE.first().x * w - 7).dp.roundToPx(),
                    (ROUTE.first().y * h - 7).dp.roundToPx(),
                )
            },
        )
        HomeMarker(
            nearArrival = onTheWay && progress > 0.85f,
            modifier = Modifier.offset {
                IntOffset(
                    (ROUTE.last().x * w - 18).dp.roundToPx(),
                    (ROUTE.last().y * h - 18).dp.roundToPx(),
                )
            },
        )
        // The courier, interpolated along the route by arc length.
        val bob = if (onTheWay) {
            val t = rememberInfiniteTransition(label = "bob")
            t.animateFloat(
                initialValue = -2f,
                targetValue = 2f,
                animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
                label = "bobValue",
            ).value
        } else {
            0f
        }
        Text(
            text = vehicle,
            fontSize = 26.sp,
            // Layout-phase offset: the courier bobs every frame while en route,
            // so a composition-phase offset would recompose this each frame.
            modifier = Modifier.offset {
                val p = pointAlongRoute(eased)
                IntOffset((p.x * w - 13).dp.roundToPx(), (p.y * h - 9 + bob).dp.roundToPx())
            },
        )
        FloatingBackButton(
            onBack = onBack,
            // The map bleeds under the status bar; the button dodges it.
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(12.dp),
        )
    }
}

/** A light, map-like grid: blocks, then the white road network over them. */
private fun DrawScope.drawStreetGrid() {
    // Faint block fills so the paper isn't a flat plane.
    val cols = 5
    val rows = 6
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            if ((r + c) % 2 == 0) {
                drawRect(
                    color = MapBlock,
                    topLeft = Offset(size.width * c / cols, size.height * r / rows),
                    size = androidx.compose.ui.geometry.Size(size.width / cols, size.height / rows),
                )
            }
        }
    }
    fun road(x1: Float, y1: Float, x2: Float, y2: Float, width: Float) {
        // A soft shadow under each road, then the road itself — gives the
        // network a faint sense of depth without a real map renderer.
        drawLine(
            MapRoadShadow,
            Offset(size.width * x1, size.height * y1 + 1.5f),
            Offset(size.width * x2, size.height * y2 + 1.5f),
            strokeWidth = width + 2f,
            cap = StrokeCap.Round,
        )
        drawLine(
            MapRoad,
            Offset(size.width * x1, size.height * y1),
            Offset(size.width * x2, size.height * y2),
            strokeWidth = width,
            cap = StrokeCap.Round,
        )
    }
    // Avenues (thick) and side streets (thin).
    listOf(0.12f, 0.44f, 0.82f).forEach { x -> road(x, -0.05f, x, 1.05f, 13f) }
    listOf(0.28f, 0.62f).forEach { x -> road(x, -0.05f, x, 1.05f, 7f) }
    listOf(0.30f, 0.52f, 0.78f).forEach { y -> road(-0.05f, y, 1.05f, y, 13f) }
    listOf(0.16f, 0.40f, 0.66f, 0.90f).forEach { y -> road(-0.05f, y, 1.05f, y, 7f) }
    // One diagonal boulevard, the way real downtowns have one.
    road(-0.05f, 0.95f, 1.05f, 0.05f, 9f)
}

/** Draws the route: a dashed full path, then the traveled part filled solid. */
private fun DrawScope.drawRoute(traveledFraction: Float) {
    val pts = ROUTE.map { Offset(it.x * size.width, it.y * size.height) }
    val full = Path().apply {
        moveTo(pts.first().x, pts.first().y)
        pts.drop(1).forEach { lineTo(it.x, it.y) }
    }
    val accent = HotPink
    // A soft white casing under the route, like a highlighted road.
    drawPath(full, Color.White.copy(alpha = 0.7f), style = Stroke(width = 13f, cap = StrokeCap.Round))
    drawPath(full, accent.copy(alpha = 0.28f), style = Stroke(width = 7f, cap = StrokeCap.Round))

    if (traveledFraction > 0f) {
        val lengths = ROUTE.zipWithNext { a, b -> hypot(b.x - a.x, b.y - a.y) }
        val total = lengths.sum()
        var remaining = traveledFraction.coerceIn(0f, 1f) * total
        val traveled = Path().apply { moveTo(pts.first().x, pts.first().y) }
        for (i in lengths.indices) {
            if (remaining <= 0f) break
            val f = (remaining / lengths[i]).coerceAtMost(1f)
            val end = Offset(
                pts[i].x + (pts[i + 1].x - pts[i].x) * f,
                pts[i].y + (pts[i + 1].y - pts[i].y) * f,
            )
            traveled.lineTo(end.x, end.y)
            remaining -= lengths[i]
        }
        drawPath(traveled, accent, style = Stroke(width = 8f, cap = StrokeCap.Round))
    }
}

/** The origin marker: a solid dot lifted off the map by a shadow — not a
 * white-ringed concentric dot, which would read as a record. */
@Composable
private fun RouteDot(color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        color = color,
        shadowElevation = 2.dp,
        modifier = modifier.size(15.dp),
        content = {},
    )
}

@Composable
private fun HomeMarker(nearArrival: Boolean, modifier: Modifier = Modifier) {
    val scale = if (nearArrival) {
        val t = rememberInfiniteTransition(label = "homePulse")
        t.animateFloat(
            initialValue = 1f,
            targetValue = 1.18f,
            animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
            label = "homeScale",
        ).value
    } else {
        1f
    }
    Surface(
        shape = CircleShape,
        color = ElectricPurple,
        shadowElevation = 3.dp,
        modifier = modifier.size(36.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("🏠", fontSize = (17 * scale).sp)
        }
    }
}

/**
 * The order header card, overlapping the map's bottom edge per the concept:
 * the package thumbnail, an "Order Id" label + the courier-style tracking
 * code, the status badge, and a location row.
 */
@Composable
internal fun TrackingHeaderCard(order: Order, location: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))) {
                    EmojiHero(
                        emoji = order.items.first().product.emoji,
                        modifier = Modifier.fillMaxSize(),
                        fontSize = 24,
                        seed = order.items.first().product.id,
                    )
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        text = "Order Id",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    // Shrinks instead of clipping at large font scales — the
                    // code is an identifier, it shouldn't truncate.
                    BasicText(
                        text = trackingCode(order.id),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        maxLines = 1,
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 12.sp,
                            maxFontSize = 16.sp,
                            stepSize = 1.sp,
                        ),
                    )
                }
                StatusBadge(order.status)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 14.dp),
            ) {
                Text("📍", fontSize = 14.sp)
                Column(Modifier.padding(start = 8.dp)) {
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

/** The status pill — mint once arrived, accent while still in motion. */
@Composable
private fun StatusBadge(status: OrderStatus) {
    val accent = if (status == OrderStatus.DELIVERED) MintGreen else HotPink
    Surface(
        shape = RoundedCornerShape(50),
        color = accent.copy(alpha = 0.15f),
    ) {
        Text(
            text = status.badge,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

// Cumulative seconds from order placement to each stage — mirrors the timing
// in ShopViewModel.runDeliverySimulation (3s pack, +5s courier, +4s on the
// way, +45s trip). The whole delivery lands inside a minute, which is the
// joke; "+12s" reads as the wink the chrome is allowed.
private val STAGE_OFFSET_SECONDS = mapOf(
    OrderStatus.CONFIRMED to 0,
    OrderStatus.PACKING to 3,
    OrderStatus.COURIER_ASSIGNED to 8,
    OrderStatus.ON_THE_WAY to 12,
    OrderStatus.DELIVERED to 57,
)

/**
 * The delivery timeline, latest event on top, the way the concept logs an
 * order's history: a connected column of ringed dot markers, each with a
 * bold stage label, the elapsed offset on the right, and the detail line.
 * Shows only events that have happened (status <= current), so it reads as
 * a log rather than a checklist.
 */
@Composable
internal fun DeliveryTimeline(order: Order, modifier: Modifier = Modifier) {
    val events = OrderStatus.entries.filter { it <= order.status }.reversed()
    Column(modifier) {
        events.forEachIndexed { index, status ->
            TimelineRow(
                status = status,
                isCurrent = status == order.status,
                isLast = index == events.lastIndex,
            )
        }
    }
}

@Composable
private fun TimelineRow(status: OrderStatus, isCurrent: Boolean, isLast: Boolean) {
    Row(Modifier.height(IntrinsicSize.Min)) {
        // Marker column: a ringed dot, then a line filling down to the next.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp),
        ) {
            TimelineDot(isCurrent = isCurrent)
            if (!isLast) {
                Box(
                    Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(HotPink.copy(alpha = 0.35f)),
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp, bottom = if (isLast) 0.dp else 18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = status.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "+${STAGE_OFFSET_SECONDS[status] ?: 0}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = status.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/** A solid accent marker, pulsing when current. Deliberately not a ringed
 * "donut" — a concentric dot reads as a vinyl record. */
@Composable
private fun TimelineDot(isCurrent: Boolean) {
    val scale = if (isCurrent) {
        val t = rememberInfiniteTransition(label = "dotPulse")
        t.animateFloat(
            initialValue = 1f,
            targetValue = 1.22f,
            animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
            label = "dotScale",
        ).value
    } else {
        1f
    }
    Box(
        modifier = Modifier
            .padding(top = 2.dp)
            .size(14.dp)
            .scale(scale)
            .background(HotPink, CircleShape),
    )
}

/** The rounded, floating back affordance the concept overlays on the map. */
@Composable
private fun FloatingBackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = modifier
            .size(40.dp)
            .clickable(onClick = onBack),
    ) {
        Box(contentAlignment = Alignment.Center) {
            BackArrowIcon(tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}
