package com.cartharsis.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Walks the journeys whose first frames were janky and records the classes and
 * methods touched, so ProfileInstaller can pre-compile them: cold start, the
 * onboarding flow, scrolling the home grid, and opening a product detail.
 *
 * Run on an API 28+ device or emulator:
 *   ./gradlew :app:generateBaselineProfile
 *
 * The UI is driven by the visible copy, so if those strings change, update the
 * selectors here. The onboarding walk is best-effort (wrapped) so the profile
 * still captures startup and scrolling even if the device is already onboarded.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = "com.cartharsis") {
        startActivityAndWait()
        device.waitForIdle()

        // Fresh install lands on onboarding; walk it through to the shop.
        runCatching {
            tap("Why would I want that?")
            tap("Create your account")
            device.findObject(By.clazz("android.widget.EditText"))?.text = "Profiler"
            tap("Continue") // account -> address
            tap("Continue") // address -> payment
            tap("Add card and start shopping")
        }

        // In the shop: fling the product grid both ways, then open a product
        // and come back — the screens whose first composition was slow.
        device.wait(Until.hasObject(By.scrollable(true)), 5_000)
        val grid = device.findObject(By.scrollable(true))
        grid?.setGestureMargin(device.displayWidth / 5)
        repeat(2) {
            grid?.fling(Direction.DOWN)
            device.waitForIdle()
        }
        repeat(2) {
            grid?.fling(Direction.UP)
            device.waitForIdle()
        }
        runCatching {
            device.findObject(By.scrollable(true))?.children?.firstOrNull()?.click()
            device.waitForIdle()
            device.pressBack()
            device.waitForIdle()
        }
    }

    private fun MacrobenchmarkScope.tap(text: String) {
        device.wait(Until.findObject(By.text(text)), 3_000)?.click()
        device.waitForIdle()
    }
}
