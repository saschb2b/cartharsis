package com.cartharsis.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cartharsis.ShopViewModel
import com.cartharsis.data.ProfileStore
import com.cartharsis.data.formatMemberSince

/**
 * The wallet: the Imagination Express card given a home of its own, pushed from
 * the Account screen. The card is the whole point here, so it gets the room to
 * be played with — tap to re-roll its look, swipe to turn it over and read the
 * deadpan fine print. Everything else on the page is the membership flourish
 * around it: an honest set of "terms" that are all jokes, and the keep-it-local
 * promise the rest of the app makes too.
 */
@Composable
fun WalletScreen(viewModel: ShopViewModel, onBack: () -> Unit) {
    val profile by viewModel.profile.collectAsState()
    val cardSeed by viewModel.cardSeed.collectAsState()
    WalletContent(
        profile = profile,
        cardSeed = cardSeed,
        onShuffleCard = viewModel::shuffleCard,
        onBack = onBack,
    )
}

@Composable
internal fun WalletContent(
    profile: ProfileStore.Profile?,
    cardSeed: Long,
    onShuffleCard: () -> Unit,
    onBack: () -> Unit,
) {
    val memberSince = formatMemberSince(profile?.memberSinceEpochDay ?: 0L)
    Column(Modifier.fillMaxSize()) {
        NestedTopBar(onBack = onBack, title = "Wallet")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ImaginationCard(
                cardHolder = profile?.name.orEmpty(),
                seed = cardSeed,
                onShuffle = onShuffleCard,
                flippable = true,
                memberSince = memberSince,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Tap or swipe to flip the card",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onShuffleCard) {
                    Text("🎲 Shuffle the look")
                }
            }

            SectionHeader("Your terms")
            Text(
                text = "Credit limit: ∞ · APR: 0.00% · Annual fee: imaginary\n" +
                    "Member since $memberSince · Rewards: the money you kept",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "This card lives on your phone and nowhere else. There is no number to " +
                    "steal, no balance to drain, and nothing to pay off — there was never " +
                    "anything real to lose.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
