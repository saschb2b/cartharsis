package com.cartharsis

import com.cartharsis.data.CartItem
import com.cartharsis.data.FakeCatalog
import com.cartharsis.data.NotificationPolicy
import com.cartharsis.data.Order
import com.cartharsis.data.advanceStreak
import com.cartharsis.data.effectiveStreak
import com.cartharsis.data.fakeStockLeft
import com.cartharsis.data.formatPrice
import com.cartharsis.data.plusProduct
import com.cartharsis.data.withPriceOverride
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeShopTest {

    @Test
    fun `catalog has unique ids and lookup works`() {
        val ids = FakeCatalog.products.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        FakeCatalog.products.forEach { assertEquals(it, FakeCatalog.byId(it.id)) }
    }

    @Test
    fun `every product belongs to a listed category`() {
        FakeCatalog.products.forEach {
            assertTrue("${it.name} has unknown category ${it.category}", it.category in FakeCatalog.categories)
        }
    }

    @Test
    fun `deal candidates are discounted and discount percent is sane`() {
        assertTrue(FakeCatalog.dealCandidates.isNotEmpty())
        FakeCatalog.dealCandidates.forEach { product ->
            val original = product.originalPriceCents!!
            assertTrue(product.priceCents < original)
            assertTrue(product.discountPercent!! in 1..99)
        }
    }

    @Test
    fun `cart item total multiplies price by quantity`() {
        val product = FakeCatalog.products.first()
        assertEquals(product.priceCents * 3, CartItem(product, 3).totalCents)
    }

    @Test
    fun `order counts items across lines`() {
        val items = FakeCatalog.products.take(2).map { CartItem(it, 2) }
        val order = Order(id = 1, items = items, totalCents = items.sumOf { it.totalCents })
        assertEquals(4, order.itemCount)
    }

    @Test
    fun `price override drops the price and keeps the old one as anchor`() {
        val product = FakeCatalog.products.first { it.originalPriceCents == null }
        val dropped = product.withPriceOverride(product.priceCents / 2)
        assertEquals(product.priceCents / 2, dropped.priceCents)
        assertEquals(product.priceCents, dropped.originalPriceCents)
    }

    @Test
    fun `price override keeps a higher existing sale anchor`() {
        val product = FakeCatalog.dealCandidates.first()
        val dropped = product.withPriceOverride(product.priceCents / 2)
        assertEquals(product.originalPriceCents, dropped.originalPriceCents)
    }

    @Test
    fun `price override ignores null and non-drops`() {
        val product = FakeCatalog.products.first()
        assertEquals(product, product.withPriceOverride(null))
        assertEquals(product, product.withPriceOverride(product.priceCents))
        assertEquals(product, product.withPriceOverride(product.priceCents * 2))
    }

    @Test
    fun `price override is idempotent on an already dropped product`() {
        val product = FakeCatalog.products.first()
        val price = product.priceCents / 3
        val once = product.withPriceOverride(price)
        assertEquals(once, once.withPriceOverride(price))
    }

    @Test
    fun `fake scarcity is stable, small, and hits part of the catalog`() {
        val scarce = FakeCatalog.products.filter { it.fakeStockLeft != null }
        assertTrue(scarce.isNotEmpty())
        assertTrue(scarce.size < FakeCatalog.products.size)
        scarce.forEach { product ->
            assertTrue(product.fakeStockLeft!! in 2..5)
            assertEquals(product.fakeStockLeft, product.fakeStockLeft) // deterministic
        }
    }

    @Test
    fun `adding a product appends a new line or merges into an existing one`() {
        val (first, second) = FakeCatalog.products.take(2)
        val cart = emptyList<CartItem>()
            .plusProduct(first, 1)
            .plusProduct(second, 2)
            .plusProduct(first, 3)
        assertEquals(2, cart.size)
        assertEquals(4, cart.first { it.product.id == first.id }.quantity)
        assertEquals(2, cart.first { it.product.id == second.id }.quantity)
    }

    @Test
    fun `merging keeps the original line's price snapshot`() {
        val product = FakeCatalog.products.first()
        val dropped = product.withPriceOverride(product.priceCents / 2)
        // First added at the dropped price, then again at full price after the
        // drop expired: the line keeps its original snapshot.
        val cart = emptyList<CartItem>().plusProduct(dropped, 1).plusProduct(product, 1)
        assertEquals(1, cart.size)
        assertEquals(2, cart.single().quantity)
        assertEquals(dropped.priceCents, cart.single().product.priceCents)
    }

    @Test
    fun `streak advances on consecutive days, holds same-day, restarts after a gap`() {
        assertEquals(4, advanceStreak(3, lastEpochDay = 100, todayEpochDay = 101))
        assertEquals(3, advanceStreak(3, lastEpochDay = 100, todayEpochDay = 100))
        assertEquals(1, advanceStreak(3, lastEpochDay = 100, todayEpochDay = 103))
        assertEquals(1, advanceStreak(0, lastEpochDay = 0, todayEpochDay = 20_000))
    }

    @Test
    fun `saved streak survives one missed day but not two`() {
        assertEquals(5, effectiveStreak(5, lastEpochDay = 100, todayEpochDay = 100))
        assertEquals(5, effectiveStreak(5, lastEpochDay = 100, todayEpochDay = 101))
        assertEquals(0, effectiveStreak(5, lastEpochDay = 100, todayEpochDay = 102))
    }

    @Test
    fun `every product carries 4 to 6 distinct reviews with sane ratings`() {
        FakeCatalog.products.forEach { product ->
            val reviews = product.reviews
            assertTrue("${product.name} has ${reviews.size} reviews", reviews.size in 4..6)
            assertEquals("${product.name} repeats a reviewer", reviews.size, reviews.toSet().size)
            reviews.forEach { assertTrue(it.rating in 1..5) }
        }
    }

    @Test
    fun `the low-star satire actually appears somewhere in the catalog`() {
        val allShown = FakeCatalog.products.flatMap { it.reviews }
        assertTrue(allShown.any { it.rating <= 3 })
    }

    @Test
    fun `quiet hours cover the night and only the night`() {
        listOf(22, 23, 0, 3, 7).forEach { assertTrue("$it should be quiet", NotificationPolicy.isQuietHour(it)) }
        listOf(8, 12, 18, 21).forEach { assertFalse("$it should be awake", NotificationPolicy.isQuietHour(it)) }
    }

    @Test
    fun `delivery pings only reach a backgrounded app`() {
        assertTrue(NotificationPolicy.shouldPingDelivery(appVisible = false))
        assertFalse(NotificationPolicy.shouldPingDelivery(appVisible = true))
    }

    @Test
    fun `price-drop ping fires once the app is away, it is daytime, and the cooldown passed`() {
        val cooldown = NotificationPolicy.DROP_PING_COOLDOWN_MILLIS
        assertTrue(
            NotificationPolicy.shouldPingPriceDrop(
                appVisible = false, hourOfDay = 14, lastPingMillis = 0, nowMillis = cooldown,
            ),
        )
    }

    @Test
    fun `price-drop ping stays silent while the app is open`() {
        assertFalse(
            NotificationPolicy.shouldPingPriceDrop(
                appVisible = true, hourOfDay = 14, lastPingMillis = 0,
                nowMillis = NotificationPolicy.DROP_PING_COOLDOWN_MILLIS,
            ),
        )
    }

    @Test
    fun `price-drop ping stays silent at night`() {
        assertFalse(
            NotificationPolicy.shouldPingPriceDrop(
                appVisible = false, hourOfDay = 23, lastPingMillis = 0,
                nowMillis = NotificationPolicy.DROP_PING_COOLDOWN_MILLIS,
            ),
        )
    }

    @Test
    fun `price-drop ping respects the cooldown window`() {
        val cooldown = NotificationPolicy.DROP_PING_COOLDOWN_MILLIS
        assertFalse(
            NotificationPolicy.shouldPingPriceDrop(
                appVisible = false, hourOfDay = 14, lastPingMillis = 1_000, nowMillis = cooldown,
            ),
        )
        assertTrue(
            NotificationPolicy.shouldPingPriceDrop(
                appVisible = false, hourOfDay = 14, lastPingMillis = 1_000, nowMillis = cooldown + 1_000,
            ),
        )
    }

    @Test
    fun `price formatting groups thousands and pads cents`() {
        assertEquals("$0.00", formatPrice(0))
        assertEquals("$3.07", formatPrice(307))
        assertEquals("$1,299.00", formatPrice(129_900))
        assertEquals("$3,999.00", formatPrice(399_900))
    }
}
