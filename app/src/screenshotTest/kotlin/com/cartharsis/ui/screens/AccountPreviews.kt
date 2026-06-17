package com.cartharsis.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.cartharsis.data.Currency
import com.cartharsis.data.ProfileStore
import com.cartharsis.ui.theme.CartharsisTheme

/**
 * The account area: editable details, the currency selector (so the choice can
 * change after onboarding), and the read-only card. One fixed phone size to
 * check the section rhythm and that the CTA/footer sit right.
 */
@PreviewTest
@Preview(name = "Account", showBackground = true, widthDp = 380, heightDp = 900)
@Composable
internal fun AccountContentPreview() {
    CartharsisTheme {
        AccountContent(
            profile = ProfileStore.Profile(
                name = "Sascha",
                street = "12 Nowhere Lane",
                city = "Imagination City",
                onboarded = true,
            ),
            currency = Currency.EUR,
            cardSeed = 42L,
            onOpenWallet = {},
            onSave = { _, _, _ -> },
            onSelectCurrency = {},
            onBack = {},
        )
    }
}

/**
 * The wallet pushed from Account: the card as hero (front), the shuffle/flip
 * hint, and the membership "terms" that are all jokes.
 */
@PreviewTest
@Preview(name = "Wallet", showBackground = true, widthDp = 380, heightDp = 640)
@Composable
internal fun WalletContentPreview() {
    CartharsisTheme {
        WalletContent(
            profile = ProfileStore.Profile(
                name = "Sascha Becker",
                onboarded = true,
                memberSinceEpochDay = 20_600L,
            ),
            cardSeed = 11L,
            onShuffleCard = {},
            onBack = {},
        )
    }
}
