package com.cartharsis.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.cartharsis.ui.theme.CartharsisTheme

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
