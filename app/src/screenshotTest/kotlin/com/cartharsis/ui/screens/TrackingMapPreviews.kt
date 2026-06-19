package com.cartharsis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.cartharsis.data.CartItem
import com.cartharsis.data.Couriers
import com.cartharsis.data.FakeCatalog
import com.cartharsis.data.Order
import com.cartharsis.data.OrderStatus
import com.cartharsis.ui.theme.CartharsisTheme

private fun sampleOrder(status: OrderStatus, progress: Float): Order {
    val p = FakeCatalog.products.first { it.name == "NoiseGone Pro Headphones" }
    return Order(
        id = 7,
        items = listOf(CartItem(p, 1)),
        totalCents = p.priceCents,
        status = status,
        progress = progress,
        placedAtMillis = 0L,
    )
}

/** The routed map mid-trip — the iteration surface for the tracking redesign. */
@PreviewTest
@Preview(name = "Route map, mid-trip", showBackground = true, widthDp = 400)
@Composable
internal fun RouteMapMidPreview() {
    CartharsisTheme {
        Column {
            RouteMap(progress = 0.55f, onTheWay = true, vehicle = "🛵", onBack = {}, citySeed = 7L, orderId = 3)
        }
    }
}

/** The night-navigation palette — the dark-theme map, with its neon route. */
@PreviewTest
@Preview(name = "Route map, night", showBackground = true, widthDp = 400)
@Composable
internal fun RouteMapNightPreview() {
    CartharsisTheme(darkTheme = true) {
        Column {
            RouteMap(progress = 0.62f, onTheWay = true, vehicle = "🛵", onBack = {}, citySeed = 7L, orderId = 3)
        }
    }
}

/** Four addresses, four generated neighborhoods and approaches — the variety. */
@PreviewTest
@Preview(name = "Generated neighborhoods", showBackground = true, widthDp = 360, heightDp = 1340)
@Composable
internal fun GeneratedCitiesPreview() {
    CartharsisTheme {
        Column {
            listOf(1L to 2, 4L to 5, 9L to 1, 13L to 8).forEach { (city, order) ->
                RouteMap(progress = 0.55f, onTheWay = true, vehicle = "🛵", onBack = {
                }, citySeed = city, orderId = order)
            }
        }
    }
}

/** The map before the courier sets off — empty route, origin dot only. */
@PreviewTest
@Preview(name = "Route map, not started", showBackground = true, widthDp = 400)
@Composable
internal fun RouteMapStartPreview() {
    CartharsisTheme {
        Column {
            RouteMap(progress = 0f, onTheWay = false, vehicle = "🛵", onBack = {})
        }
    }
}

/** Near arrival — the home marker pulses, the trail nearly full. */
@PreviewTest
@Preview(name = "Route map, near arrival", showBackground = true, widthDp = 400)
@Composable
internal fun RouteMapArrivingPreview() {
    CartharsisTheme {
        Column {
            RouteMap(progress = 0.93f, onTheWay = true, vehicle = "🚀", onBack = {})
        }
    }
}

/** The full five-stage log, as the delivered ceremony now shows it. */
@PreviewTest
@Preview(name = "Delivery timeline, all stages", showBackground = true, widthDp = 360)
@Composable
internal fun DeliveryTimelineFullPreview() {
    CartharsisTheme {
        Column(Modifier.padding(20.dp)) {
            DeliveryTimeline(order = sampleOrder(OrderStatus.DELIVERED, 1f))
        }
    }
}

/** The bottom sheet's glanceable hero: status, whimsical ETA, progress, location. */
@PreviewTest
@Preview(name = "Status hero", showBackground = true, widthDp = 380)
@Composable
internal fun StatusHeroPreview() {
    CartharsisTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            StatusHero(order = sampleOrder(OrderStatus.ON_THE_WAY, 0.45f))
            StatusHero(order = sampleOrder(OrderStatus.PACKING, 0f))
        }
    }
}

/** The courier as a person: regular, first-time guest, and the rare rocket one. */
@PreviewTest
@Preview(name = "Courier card", showBackground = true, widthDp = 400)
@Composable
internal fun CourierCardPreview() {
    CartharsisTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CourierCard(courier = Couriers.minjun, nthDelivery = 4)
            CourierCard(courier = Couriers.aria, nthDelivery = 1)
            CourierCard(courier = Couriers.vega, nthDelivery = 1)
        }
    }
}

/** The note left at the door on arrival, signed by the courier. */
@PreviewTest
@Preview(name = "Courier sign-off", showBackground = true, widthDp = 400)
@Composable
internal fun CourierSignoffPreview() {
    CartharsisTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CourierSignoff(courier = Couriers.minjun, nthDelivery = 4)
            CourierSignoff(courier = Couriers.bo, nthDelivery = 7)
        }
    }
}

/** The whole concept on one render: map, overlapping header card, timeline. */
@PreviewTest
@Preview(name = "Tracking map, full", showBackground = true, widthDp = 400, heightDp = 880)
@Composable
internal fun TrackingMapFullPreview() {
    val order = sampleOrder(OrderStatus.ON_THE_WAY, 0.55f)
    CartharsisTheme {
        Column(Modifier.background(androidx.compose.ui.graphics.Color(0xFFFBF7F2))) {
            RouteMap(progress = order.progress, onTheWay = true, vehicle = "🛵", onBack = {})
            Column(Modifier.offset(y = (-24).dp).padding(horizontal = 16.dp)) {
                TrackingHeaderCard(order = order, location = "Anticipation Street, Dopamine City")
                Column(Modifier.padding(top = 18.dp, start = 4.dp)) {
                    DeliveryTimeline(order = order)
                }
            }
        }
    }
}
