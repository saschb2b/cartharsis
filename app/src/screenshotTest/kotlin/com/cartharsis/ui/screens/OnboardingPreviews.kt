package com.cartharsis.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.cartharsis.ui.theme.CartharsisTheme

// The four onboarding steps at one fixed phone size. The point of the gallery
// is to check that the primary CTA lands at the SAME height on every step (the
// bug being fixed), so each preview frames a step in the same 360x720 box with
// the same 24dp screen padding the real screen uses.
@Composable
private fun StepFrame(content: @Composable () -> Unit) {
    CartharsisTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(Modifier.fillMaxSize().padding(24.dp)) { content() }
        }
    }
}

@PreviewTest
@Preview(name = "Onboarding 1 welcome", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
internal fun OnboardingWelcomePreview() {
    StepFrame { WelcomeStep(onStart = {}) }
}

@PreviewTest
@Preview(name = "Onboarding 2 account", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
internal fun OnboardingAccountPreview() {
    StepFrame { AccountStep(name = "Sascha", onNameChange = {}, onContinue = {}) }
}

@PreviewTest
@Preview(name = "Onboarding 3 address", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
internal fun OnboardingAddressPreview() {
    StepFrame {
        AddressStep(
            street = "12 Nowhere Lane",
            onStreetChange = {},
            city = "Imagination City",
            onCityChange = {},
            onContinue = {},
        )
    }
}

@PreviewTest
@Preview(name = "Onboarding 4 payment", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
internal fun OnboardingPaymentPreview() {
    StepFrame {
        PaymentStep(
            name = "Sascha",
            currency = com.cartharsis.data.Currency.KRW,
            onCurrencyChange = {},
            onAddCard = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "Onboarding account, dark",
    showBackground = true,
    widthDp = 360,
    heightDp = 720,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
internal fun OnboardingAccountDarkPreview() {
    StepFrame { AccountStep(name = "Sascha", onNameChange = {}, onContinue = {}) }
}

@PreviewTest
@Preview(
    name = "Onboarding payment, dark",
    showBackground = true,
    widthDp = 360,
    heightDp = 720,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
internal fun OnboardingPaymentDarkPreview() {
    StepFrame {
        PaymentStep(
            name = "Sascha",
            currency = com.cartharsis.data.Currency.KRW,
            onCurrencyChange = {},
            onAddCard = {},
        )
    }
}
