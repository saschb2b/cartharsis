package com.cartharsis.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.cartharsis.data.FakeCatalog
import com.cartharsis.data.homeModules
import com.cartharsis.ui.theme.CartharsisTheme

private val heroProduct = FakeCatalog.products.first { it.originalPriceCents != null }
private val sampleModules = homeModules(FakeCatalog.products, seed = 7L, hourOfDay = 14)

/** The whole magazine feed in one render — hero + the varied module deck —
 *  the iteration surface for the storefront redesign. */
@PreviewTest
@Preview(name = "Home feed, magazine", showBackground = true, widthDp = 400, heightDp = 1500)
@Composable
internal fun HomeFeedPreview() {
    CartharsisTheme {
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            HeroFeature(product = heroProduct, onClick = {})
            sampleModules.forEach { module ->
                HomeModuleView(module = module, onProductClick = {}, onMoodPick = {})
            }
        }
    }
}

/** The same feed on a dark surface — the tiles, scrims, and hero must read at night. */
@PreviewTest
@Preview(
    name = "Home feed, dark",
    showBackground = true,
    widthDp = 400,
    heightDp = 1500,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
internal fun HomeFeedDarkPreview() {
    CartharsisTheme {
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            HeroFeature(product = heroProduct, onClick = {})
            sampleModules.forEach { module ->
                HomeModuleView(module = module, onProductClick = {}, onMoodPick = {})
            }
        }
    }
}
