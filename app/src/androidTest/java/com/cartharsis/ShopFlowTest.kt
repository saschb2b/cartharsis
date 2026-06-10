package com.cartharsis

import android.Manifest
import android.os.Build
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
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
        // A fresh install lands on the fake signup; walk it. Devices that
        // already have a profile skip straight to the shop.
        compose.waitForIdle()
        if (compose.onAllNodesWithText("Create your account").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithText("Create your account").performClick()
            compose.onNodeWithText("Your name").performTextInput("Test Pilot")
            compose.onNodeWithText("Continue").performClick()
            compose.onNodeWithText("Continue").performClick() // address comes prefilled
            compose.onNodeWithText("Add card and start shopping").performClick()
        }
        compose.onNodeWithText("Cartharsis").assertIsDisplayed()

        // The home grid is shuffled per open, so search to surface a known
        // product deterministically (search results are unshuffled). A partial
        // query avoids colliding with the product card's full-name text node.
        compose.onNodeWithText("Search for things you'll never receive")
            .performTextInput("Ultra Max")
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText("AuraPhone 17 Ultra Max").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("AuraPhone 17 Ultra Max").performClick()
        compose.onNodeWithText("Add to cart").performClick()

        // Bottom bar → cart → checkout.
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Cart").performClick()
        compose.onNodeWithText("Checkout · ", substring = true).performClick()

        // A plain click would release the hold-to-pay button too early; place
        // the order through its accessibility action (the screen-reader path).
        compose.onNodeWithText("Hold to pay ", substring = true)
            .performSemanticsAction(SemanticsActions.OnClick)
        // "Order placed!" or the first-order milestone "First order placed!".
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithText("order placed!", substring = true, ignoreCase = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Track your nothing 🚚").assertIsDisplayed()
    }
}
