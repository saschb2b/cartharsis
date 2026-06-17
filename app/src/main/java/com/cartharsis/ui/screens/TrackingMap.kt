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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
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
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.random.Random

// The map palette, tuned to map-app cartography (the Google Maps "basic"
// scheme): warm off-white land, muted building blocks with crisp edges, white
// roads over grey casing, a soft-yellow highway, pastel-green parks, serene
// blue water. The neutral base lets the pink delivery route carry the eye.
private val MapPaper = Color(0xFFF2EFE8)
private val MapPaperLo = Color(0xFFEAE6DC)
private val MapRoad = Color(0xFFFFFFFF)
private val MapRoadCasing = Color(0xFFD9D5CB)
private val MapHighway = Color(0xFFFBE7A6)
private val MapHighwayCasing = Color(0xFFEACF82)
private val MapPark = Color(0xFFC4E1A6)
private val MapPark2 = Color(0xFFB7D998)
private val MapWater = Color(0xFFAAD4F2)
private val MapWaterEdge = Color(0xFF93C4E8)
private val MapBuildingLine = Color(0xFFD7D2C7)
private val MapBuildingShadow = Color(0x16000000)
private val buildingShades = listOf(Color(0xFFEAE5DC), Color(0xFFE4DED4), Color(0xFFEEE9E1))

/**
 * One road: a normalized position (x for an avenue, y for a street), a stroke
 * width (thick = a full-span arterial, thin = a local street), and the span it
 * runs over along the other axis. Locals often stop short, so the network reads
 * as a hierarchy with T-junctions and superblocks rather than a perfect lattice.
 */
internal data class CityRoad(val pos: Float, val width: Float, val from: Float, val to: Float) {
    val arterial: Boolean get() = width >= 12f
}

/** One building footprint: a normalized rect and a shade index into [buildingShades]. */
internal data class Building(val rect: Rect, val shade: Int)

/**
 * A generated, stylized city: a seeded road grid, building footprints filling
 * the blocks, a park or two, maybe some water, a highway, and the home marker —
 * all derived from the shopper's address, so each neighborhood is its own and
 * stays put across orders. The route is generated separately (per order), so the
 * approach direction varies.
 */
internal data class CityMap(
    val avenues: List<CityRoad>,
    val streets: List<CityRoad>,
    val highway: Pair<Offset, Offset>?,
    val parks: List<Rect>,
    val water: List<Rect>,
    val buildings: List<Building>,
    val home: Offset,
)

/**
 * Roads spread across the map with jitter, in a hierarchy: one or two run as
 * full-span thick arterials, and the thin locals between them often stop short
 * at an edge, leaving T-junctions and superblocks instead of a perfect grid.
 */
private fun spacedRoads(count: Int, rng: Random): List<CityRoad> {
    val step = 0.78f / (count - 1)
    val arterials = mutableSetOf(1 + rng.nextInt((count - 1).coerceAtLeast(1)))
    if (rng.nextBoolean()) arterials += rng.nextInt(count)
    return (0 until count).map { i ->
        val pos = (0.11f + step * i + (rng.nextFloat() - 0.5f) * step * 0.4f).coerceIn(0.06f, 0.94f)
        when {
            i in arterials -> CityRoad(pos, 13f, -0.05f, 1.05f)
            rng.nextFloat() < 0.55f -> CityRoad(pos, 7f, -0.05f, 1.05f)
            else -> {
                // A local street that runs in from one edge and dead-ends.
                val len = 0.4f + rng.nextFloat() * 0.45f
                if (rng.nextBoolean()) {
                    CityRoad(pos, 7f, -0.05f, -0.05f + len)
                } else {
                    CityRoad(pos, 7f, 1.05f - len, 1.05f)
                }
            }
        }
    }
}

/** A block bounded by two adjacent avenues and two adjacent streets, inset a bit. */
private fun cell(avenues: List<CityRoad>, streets: List<CityRoad>, rng: Random): Rect {
    val ai = rng.nextInt(avenues.size - 1)
    val si = rng.nextInt(streets.size - 1)
    val x0 = avenues[ai].pos
    val x1 = avenues[ai + 1].pos
    val y0 = streets[si].pos
    val y1 = streets[si + 1].pos
    val ix = (x1 - x0) * 0.16f
    val iy = (y1 - y0) * 0.16f
    return Rect(x0 + ix, y0 + iy, x1 - ix, y1 - iy)
}

/** The whole city for an address [seed]: roads, blocks of buildings, parks,
 *  water, a highway, and home. */
internal fun generateCity(seed: Long): CityMap {
    val rng = Random(seed)
    val rawAvenues = spacedRoads(3 + rng.nextInt(2), rng) // 3-4 avenues
    val rawStreets = spacedRoads(4 + rng.nextInt(2), rng) // 4-5 streets
    val highway = when {
        rng.nextFloat() >= 0.5f -> null
        rng.nextBoolean() -> Offset(-0.05f, 0.95f) to Offset(1.05f, 0.05f)
        else -> Offset(-0.05f, 0.05f) to Offset(1.05f, 0.95f)
    }

    // Home sits in the upper-center band (visible above the bottom sheet); its
    // two roads are promoted to full-span arterials so the route overlays real
    // road right to the door.
    fun pickIndex(roads: List<CityRoad>, band: ClosedFloatingPointRange<Float>): Int {
        val inBand = roads.indices.filter { roads[it].pos in band }
        val pool = inBand.ifEmpty { roads.indices.toList() }
        return pool[rng.nextInt(pool.size)]
    }
    fun promote(r: CityRoad) = r.copy(width = 13f, from = -0.05f, to = 1.05f)
    val homeAvIdx = pickIndex(rawAvenues, 0.26f..0.78f)
    val homeStIdx = pickIndex(rawStreets, 0.16f..0.46f)
    val avenues = rawAvenues.mapIndexed { i, r -> if (i == homeAvIdx) promote(r) else r }
    val streets = rawStreets.mapIndexed { i, r -> if (i == homeStIdx) promote(r) else r }
    val home = Offset(avenues[homeAvIdx].pos, streets[homeStIdx].pos)
    val homeCell = Rect(home.x - 0.08f, home.y - 0.08f, home.x + 0.08f, home.y + 0.08f)
    fun patches(n: Int): List<Rect> {
        val out = mutableListOf<Rect>()
        repeat(n * 4) {
            if (out.size < n) {
                val r = cell(avenues, streets, rng)
                if (!r.overlaps(homeCell) && out.none { it.overlaps(r) }) out += r
            }
        }
        return out
    }
    val parks = patches(1 + rng.nextInt(2))
    val water = if (rng.nextFloat() < 0.55f) patches(1) else emptyList()
    // Building footprints fill each block: the dense built environment that
    // makes a map read as a city rather than a wireframe grid. Each block is
    // subdivided into a small grid of footprints with alley gaps and a few empty
    // lots; buildings keep off the parks and water.
    val features = parks + water
    val buildings = buildList {
        for (i in 0 until avenues.size - 1) {
            for (j in 0 until streets.size - 1) {
                val x0 = avenues[i].pos
                val x1 = avenues[i + 1].pos
                val y0 = streets[j].pos
                val y1 = streets[j + 1].pos
                val bx0 = x0 + (x1 - x0) * 0.14f
                val bx1 = x1 - (x1 - x0) * 0.14f
                val by0 = y0 + (y1 - y0) * 0.14f
                val by1 = y1 - (y1 - y0) * 0.14f
                if (bx1 - bx0 < 0.03f || by1 - by0 < 0.03f) continue
                val cols = 2 + rng.nextInt(2)
                val rows = 2 + rng.nextInt(2)
                val gap = 0.009f
                val cw = (bx1 - bx0 - gap * (cols - 1)) / cols
                val rh = (by1 - by0 - gap * (rows - 1)) / rows
                for (c in 0 until cols) {
                    for (r in 0 until rows) {
                        if (rng.nextFloat() < 0.16f) continue // a few empty lots
                        val rx0 = bx0 + c * (cw + gap)
                        val ry0 = by0 + r * (rh + gap)
                        val rect = Rect(rx0, ry0, rx0 + cw, ry0 + rh)
                        if (features.none { it.overlaps(rect) }) {
                            add(Building(rect, rng.nextInt(buildingShades.size)))
                        }
                    }
                }
            }
        }
    }
    return CityMap(avenues, streets, highway, parks, water, buildings, home)
}

/** A grid line to enter along, preferring one that isn't the home's own line. */
private fun pickLine(positions: List<Float>, avoid: Float, rng: Random): Float {
    val options = positions.filter { abs(it - avoid) > 0.01f }
    val pool = options.ifEmpty { positions }
    return pool[rng.nextInt(pool.size)]
}

/**
 * The courier's route for an order: an L-shaped path along real grid roads from
 * one of the four edges to home. Seeded by the order, so the approach comes from
 * a different direction once in a while rather than always the same way.
 */
internal fun routeFor(city: CityMap, orderSeed: Int): List<Offset> {
    val rng = Random(orderSeed * 2_654_435_761L + 17L)
    val home = city.home
    // Approach along full-span arterials, so the courier travels on real road.
    val avX = city.avenues.filter { it.arterial }.map { it.pos }.ifEmpty { city.avenues.map { it.pos } }
    val stY = city.streets.filter { it.arterial }.map { it.pos }.ifEmpty { city.streets.map { it.pos } }
    return when (rng.nextInt(4)) {
        0 -> pickLine(stY, home.y, rng).let { y -> listOf(Offset(-0.06f, y), Offset(home.x, y), home) }
        1 -> pickLine(avX, home.x, rng).let { x -> listOf(Offset(x, -0.06f), Offset(x, home.y), home) }
        2 -> pickLine(avX, home.x, rng).let { x -> listOf(Offset(x, 1.06f), Offset(x, home.y), home) }
        else -> pickLine(stY, home.y, rng).let { y -> listOf(Offset(1.06f, y), Offset(home.x, y), home) }
    }
}

/** The point a fraction [t] of the way along [route], by arc length. */
private fun pointAlongRoute(route: List<Offset>, t: Float): Offset {
    val clamped = t.coerceIn(0f, 1f)
    val lengths = route.zipWithNext { a, b -> hypot(b.x - a.x, b.y - a.y) }
    val total = lengths.sum()
    if (total == 0f) return route.first()
    var target = clamped * total
    for (i in lengths.indices) {
        if (target <= lengths[i]) {
            val f = if (lengths[i] == 0f) 0f else target / lengths[i]
            return Offset(
                route[i].x + (route[i + 1].x - route[i].x) * f,
                route[i].y + (route[i + 1].y - route[i].y) * f,
            )
        }
        target -= lengths[i]
    }
    return route.last()
}

/**
 * The tracking map: a stylized street network with a routed accent path from
 * the origin dot to the destination pin, the courier crawling along it, and a
 * floating back button — the centerpiece of the redesigned tracking screen. The
 * city is generated from the shopper's address (so each neighborhood is its
 * own) and the route from the order (so the approach direction varies).
 */
@Composable
internal fun RouteMap(
    progress: Float,
    onTheWay: Boolean,
    vehicle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    /** Fill the available height (for the full-bleed map behind the bottom sheet). */
    fill: Boolean = false,
    /** Seeds the generated city (the address) and the courier's route (the order). */
    citySeed: Long = 0L,
    orderId: Int = 0,
) {
    // Same goal-gradient easing the old map used, so the courier and the
    // filled trail accelerate into the destination together.
    val eased = (progress * progress + progress) / 2f
    // The neighborhood is the address's; the route through it is the order's.
    val city = remember(citySeed) { generateCity(citySeed) }
    val route = remember(citySeed, orderId) { routeFor(city, orderId) }
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .then(if (fill) Modifier.fillMaxHeight() else Modifier.height(330.dp))
            .background(Brush.verticalGradient(listOf(MapPaper, MapPaperLo)))
            .clearAndSetSemantics { contentDescription = "Delivery route map" },
    ) {
        val w = maxWidth.value
        val h = maxHeight.value
        Canvas(Modifier.fillMaxSize()) {
            drawCity(city)
            drawRoute(route, eased)
        }
        // Origin marker (where the courier set off) and destination (home).
        RouteDot(
            color = ElectricPurple,
            modifier = Modifier.offset {
                IntOffset(
                    (route.first().x * w - 7).dp.roundToPx(),
                    (route.first().y * h - 7).dp.roundToPx(),
                )
            },
        )
        HomeMarker(
            nearArrival = onTheWay && progress > 0.85f,
            modifier = Modifier.offset {
                IntOffset(
                    (route.last().x * w - 18).dp.roundToPx(),
                    (route.last().y * h - 18).dp.roundToPx(),
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
                val p = pointAlongRoute(route, eased)
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

/** The generated city: water and parks, building footprints on the blocks, then
 *  the road network and a highway on top — layered like a real map. */
private fun DrawScope.drawCity(city: CityMap) {
    fun rectOf(r: Rect) = Offset(size.width * r.left, size.height * r.top) to
        Size(size.width * r.width, size.height * r.height)

    // Natural features first, with a crisp darker edge so they read cleanly.
    val featureRadius = CornerRadius(size.minDimension * 0.05f)
    fun feature(edge: Color, fill: Color, r: Rect) {
        val (tl, sz) = rectOf(r)
        drawRoundRect(edge, topLeft = tl, size = sz, cornerRadius = featureRadius)
        drawRoundRect(
            fill,
            topLeft = Offset(tl.x + 1.5f, tl.y + 1.5f),
            size = Size(sz.width - 3f, sz.height - 3f),
            cornerRadius = featureRadius,
        )
    }
    city.water.forEach { feature(MapWaterEdge, MapWater, it) }
    city.parks.forEach { feature(MapPark2, MapPark, it) }

    // Building footprints: a faint drop shadow, a muted fill, a crisp edge —
    // the density that makes the blocks read as a built city.
    val bRadius = CornerRadius(size.minDimension * 0.006f)
    city.buildings.forEach { b ->
        val (tl, sz) = rectOf(b.rect)
        drawRoundRect(MapBuildingShadow, topLeft = Offset(tl.x, tl.y + 1.2f), size = sz, cornerRadius = bRadius)
        drawRoundRect(buildingShades[b.shade], topLeft = tl, size = sz, cornerRadius = bRadius)
        drawRoundRect(MapBuildingLine, topLeft = tl, size = sz, cornerRadius = bRadius, style = Stroke(1f))
    }

    fun road(x1: Float, y1: Float, x2: Float, y2: Float, width: Float, fill: Color, casing: Color) {
        drawLine(
            casing,
            Offset(size.width * x1, size.height * y1),
            Offset(size.width * x2, size.height * y2),
            strokeWidth = width + 3f,
            cap = StrokeCap.Round,
        )
        drawLine(
            fill,
            Offset(size.width * x1, size.height * y1),
            Offset(size.width * x2, size.height * y2),
            strokeWidth = width,
            cap = StrokeCap.Round,
        )
    }
    // The street network — each road over its own span, so locals dead-end —
    // then the highway over it (a prominent through-road).
    city.avenues.forEach { road(it.pos, it.from, it.pos, it.to, it.width, MapRoad, MapRoadCasing) }
    city.streets.forEach { road(it.from, it.pos, it.to, it.pos, it.width, MapRoad, MapRoadCasing) }
    city.highway?.let { (a, b) -> road(a.x, a.y, b.x, b.y, 13f, MapHighway, MapHighwayCasing) }
}

/** Draws the route: the full path as a faint road, then the traveled part solid. */
private fun DrawScope.drawRoute(route: List<Offset>, traveledFraction: Float) {
    val pts = route.map { Offset(it.x * size.width, it.y * size.height) }
    val full = Path().apply {
        moveTo(pts.first().x, pts.first().y)
        pts.drop(1).forEach { lineTo(it.x, it.y) }
    }
    val accent = HotPink
    // A soft white casing under the route, like a highlighted road.
    drawPath(full, Color.White.copy(alpha = 0.7f), style = Stroke(width = 13f, cap = StrokeCap.Round))
    drawPath(full, accent.copy(alpha = 0.28f), style = Stroke(width = 7f, cap = StrokeCap.Round))

    if (traveledFraction > 0f) {
        val lengths = route.zipWithNext { a, b -> hypot(b.x - a.x, b.y - a.y) }
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
    // The current stage breathes — except DELIVERED, which is a destination,
    // not an in-progress step (mirrors the old horizontal tracker).
    val pulsing = isCurrent && status != OrderStatus.DELIVERED
    Row(Modifier.height(IntrinsicSize.Min)) {
        // Marker column: a solid dot, then a line filling down to the next.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp),
        ) {
            TimelineDot(pulsing = pulsing)
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

/** A solid accent marker, breathing while [pulsing]. Deliberately not a
 * ringed "donut" — a concentric dot reads as a vinyl record. */
@Composable
private fun TimelineDot(pulsing: Boolean) {
    val scale = if (pulsing) {
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
            // BackArrowIcon already carries the "Back" label; the bare clickable
            // just lacked a role, so TalkBack never announced it as a button.
            .clickable(onClick = onBack, role = Role.Button),
    ) {
        Box(contentAlignment = Alignment.Center) {
            BackArrowIcon(tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}
