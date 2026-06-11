package com.cartharsis.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.cartharsis.data.CardPull
import com.cartharsis.data.FakeCatalog
import com.cartharsis.ui.theme.CartharsisTheme

/**
 * The card gallery: every face the pack rip can deal, rendered straight from
 * Gradle — `updateDebugScreenshotTest` regenerates the reference PNGs (the
 * design iteration loop, no emulator ride needed) and
 * `validateDebugScreenshotTest` turns them into a visual regression gate.
 */

// Commons are inlined (their pool is private by design); chases come from
// the catalog so the gallery tracks the real cards.
private val sampleCommon = CardPull(
    "🐭", "Nibbletuft", "Common", "Hoards crumbs it has no intention of eating.",
    type = "Basic Meadow Critter",
    stat = "50 HP",
)
private val sampleUncommon = CardPull(
    "🦉", "Duskhoot", "Uncommon", "Asks 'who?' rhetorically. It knows.",
    type = "Stage 1 Dream Critter",
    stat = "90 HP",
)

@PreviewTest
@Preview(name = "Critters common + uncommon", showBackground = true)
@Composable
internal fun CommonAndUncommonPreview() {
    CartharsisTheme {
        Row(Modifier.padding(12.dp)) {
            RipCardFace(card = sampleCommon, theme = packTheme("critters"), faceDown = false)
            RipCardFace(
                card = sampleUncommon,
                theme = packTheme("critters"),
                faceDown = false,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}

@PreviewTest
@Preview(name = "Chase cards, all games", showBackground = true, widthDp = 740)
@Composable
internal fun ChaseCardsPreview() {
    CartharsisTheme {
        Row(Modifier.padding(12.dp)) {
            FakeCatalog.cardGameTitles.keys.forEachIndexed { i, game ->
                RipCardFace(
                    card = FakeCatalog.chaseCardsOf(game).first(),
                    theme = packTheme(game),
                    faceDown = false,
                    holo = true,
                    modifier = Modifier.padding(start = if (i == 0) 0.dp else 12.dp),
                )
            }
        }
    }
}

@PreviewTest
@Preview(name = "Card backs, all games", showBackground = true, widthDp = 740)
@Composable
internal fun CardBacksPreview() {
    CartharsisTheme {
        Row(Modifier.padding(12.dp)) {
            FakeCatalog.cardGameTitles.keys.forEachIndexed { i, game ->
                RipCardFace(
                    card = FakeCatalog.chaseCardsOf(game).first(),
                    theme = packTheme(game),
                    faceDown = true,
                    modifier = Modifier.padding(start = if (i == 0) 0.dp else 12.dp),
                )
            }
        }
    }
}

@PreviewTest
@Preview(name = "Chase card, dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ChaseCardDarkPreview() {
    CartharsisTheme {
        RipCardFace(
            card = FakeCatalog.chaseCardsOf("duelbound").first(),
            theme = packTheme("duelbound"),
            faceDown = false,
            holo = true,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@PreviewTest
@Preview(name = "Sealed boosters, all games", showBackground = true, widthDp = 820)
@Composable
internal fun BoosterPacksPreview() {
    CartharsisTheme {
        Row(Modifier.padding(12.dp)) {
            FakeCatalog.cardGameTitles.keys.forEach { game ->
                val series = FakeCatalog.cardSeriesTitles.entries
                    .first { it.key.startsWith(game) }
                    .value
                Row(Modifier.width(264.dp)) {
                    BoosterPackTear(theme = packTheme(game), series = series, onTorn = {})
                }
            }
        }
    }
}
