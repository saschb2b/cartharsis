package com.cartharsis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.cartharsis.data.CartItem
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
            RouteMap(progress = 0.55f, onTheWay = true, vehicle = "🛵", onBack = {})
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
