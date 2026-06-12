package com.cartharsis.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
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
    "🐭", "Nibbletuft", "Common", "Hoards crumbs by the hearth. Eating them is not the point.",
    type = "Basic Meadow Critter",
    stat = "50 HP",
)
private val sampleUncommon = CardPull(
    "🦉", "Duskhoot", "Uncommon", "Asks 'who?' rhetorically. It knows.",
    type = "Stage 1 Dream Critter",
    stat = "90 HP",
)

// With the chases, this completes all seven element washes in the gallery —
// Sky appears on no other rendered card.
private val sampleSky = CardPull(
    "🐦", "Chirplet", "Common", "Knows one song. Commits to it at first light.",
    type = "Basic Sky Critter",
    stat = "40 HP",
)

@PreviewTest
@Preview(name = "Critters common + uncommon", showBackground = true, widthDp = 740)
@Composable
internal fun CommonAndUncommonPreview() {
    CartharsisTheme {
        Row(Modifier.padding(12.dp)) {
            listOf(sampleCommon, sampleUncommon, sampleSky).forEachIndexed { i, card ->
                RipCardFace(
                    card = card,
                    theme = packTheme("critters"),
                    faceDown = false,
                    modifier = Modifier.padding(start = if (i == 0) 0.dp else 12.dp),
                )
            }
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

// A trap is inlined (the commons pool is private by design); the other four
// kinds — effect monster, ritual, fusion, relic/spell — are all chases.
private val sampleTrap = CardPull(
    "⚱️", "Sealed Urn", "Common", "Do not open. It gets cold.",
    type = "[Trap / Counter]",
)

@PreviewTest
@Preview(name = "Duelbound kinds, framed by color", showBackground = true, widthDp = 1230)
@Composable
internal fun DuelboundKindsPreview() {
    // The genre reads a card's kind off its frame color before any text:
    // amber monster, blue ritual, violet fusion, green relic, rose trap —
    // one representative chase per kind, plus the inlined trap.
    val kinds = listOf(
        "The Nameless Archivist", "Serpent of the Sealed Vault",
        "Crimson Regent, Twice-Risen", "Relic of the First Duel",
    )
    CartharsisTheme {
        Row(Modifier.padding(12.dp)) {
            val chases = FakeCatalog.chaseCardsOf("duelbound")
            (kinds.map { name -> chases.first { it.name == name } } + sampleTrap).forEachIndexed { i, card ->
                RipCardFace(
                    card = card,
                    theme = packTheme("duelbound"),
                    faceDown = false,
                    modifier = Modifier.padding(start = if (i == 0) 0.dp else 12.dp),
                )
            }
        }
    }
}

@PreviewTest
@Preview(name = "Manaforge identities, framed by color", showBackground = true, widthDp = 990)
@Composable
internal fun ManaforgeIdentitiesPreview() {
    // Magic's convention: the frame wears the card's color identity — one
    // representative chase each for blue wizard, red elemental, green
    // enchantment, colorless artifact.
    val identities = listOf(
        "Archmage of the Ashveil", "Caldera Sovereign",
        "The Verdant Throne, Reborn", "Hourglass of Convergence",
    )
    CartharsisTheme {
        Row(Modifier.padding(12.dp)) {
            val chases = FakeCatalog.chaseCardsOf("manaforge")
            identities.map { name -> chases.first { it.name == name } }.forEachIndexed { i, card ->
                RipCardFace(
                    card = card,
                    theme = packTheme("manaforge"),
                    faceDown = false,
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
@Preview(name = "Sealed boosters, all series", showBackground = true, widthDp = 820)
@Composable
internal fun BoosterPacksPreview() {
    // Every series, not just every game: each set wears its own wrapper.
    CartharsisTheme {
        Column(Modifier.padding(12.dp)) {
            FakeCatalog.cardSeriesTitles.entries.chunked(3).forEach { rowOfSeries ->
                Row {
                    rowOfSeries.forEach { (group, series) ->
                        Row(Modifier.width(264.dp)) {
                            BoosterPackTear(
                                theme = packTheme(group.substringBefore('-'), group),
                                series = series,
                                onTorn = {},
                            )
                        }
                    }
                }
            }
        }
    }
}
