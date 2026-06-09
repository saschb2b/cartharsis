package com.cartharsis

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** End-to-end smoke test of the core ritual: browse → cart → checkout → confetti. */
@RunWith(AndroidJUnit4::class)
class ShopFlowTest {

    @get:Rule(order = 0)
    val permission: GrantPermissionRule =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            GrantPermissionRule.grant()
        }

    @get:Rule(order = 1)
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun browseAddToCartCheckoutPlaceOrder() {
        compose.onNodeWithText("Cartharsis").assertIsDisplayed()

        // Open the first catalog product and add it to the cart.
        compose.onAllNodesWithText("AuraPhone 17 Ultra Max").onFirst().performClick()
        compose.onNodeWithText("Add to cart 🛒").performClick()

        // Bottom bar → cart → checkout.
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Cart").performClick()
        compose.onNodeWithText("Checkout — risk-free, everything-free").performClick()

        // Place the order and wait out the fake processing spinner.
        compose.onNodeWithText("Place order — pay nothing 🎉").performClick()
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithText("Order placed!").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Track your nothing 🚚").assertIsDisplayed()
    }
}
