package com.example.myapplication

import com.example.myapplication.data.CartItem
import com.example.myapplication.data.FakeCatalog
import com.example.myapplication.data.Order
import com.example.myapplication.data.advanceStreak
import com.example.myapplication.data.effectiveStreak
import com.example.myapplication.data.fakeStockLeft
import com.example.myapplication.data.formatPrice
import com.example.myapplication.data.withPriceOverride
import org.junit.Assert.assertEquals
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
    fun `price formatting groups thousands and pads cents`() {
        assertEquals("$0.00", formatPrice(0))
        assertEquals("$3.07", formatPrice(307))
        assertEquals("$1,299.00", formatPrice(129_900))
        assertEquals("$3,999.00", formatPrice(399_900))
    }
}
