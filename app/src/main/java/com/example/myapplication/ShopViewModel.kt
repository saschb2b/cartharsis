package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CartItem
import com.example.myapplication.data.FakeCatalog
import com.example.myapplication.data.Order
import com.example.myapplication.data.OrderStatus
import com.example.myapplication.data.Product
import com.example.myapplication.data.StreakStore
import com.example.myapplication.data.WishlistStore
import com.example.myapplication.data.advanceStreak
import com.example.myapplication.data.effectiveStreak
import com.example.myapplication.data.formatPrice
import com.example.myapplication.data.withPriceOverride
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val FLASH_DEAL_SECONDS = 90
private const val COURIER_TRIP_MILLIS = ShopViewModel.COURIER_TRIP_SECONDS * 1_000L
private const val PRICE_DROP_INTERVAL_MILLIS = 25_000L
private const val PRICE_DROP_LIFETIME_MILLIS = 90_000L

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        /** How long the courier carries your nothing across town. UI ETAs derive from this. */
        const val COURIER_TRIP_SECONDS = 45
    }

    val catalog: List<Product> = FakeCatalog.products
    val categories: List<String> = FakeCatalog.categories

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _flashDeal = MutableStateFlow(FakeCatalog.dealCandidates.first())
    val flashDeal: StateFlow<Product> = _flashDeal.asStateFlow()

    private val _flashDealSecondsLeft = MutableStateFlow(FLASH_DEAL_SECONDS)
    val flashDealSecondsLeft: StateFlow<Int> = _flashDealSecondsLeft.asStateFlow()

    /** Bumped on every add-to-cart so the UI can bounce the cart badge. */
    private val _cartPulse = MutableStateFlow(0)
    val cartPulse: StateFlow<Int> = _cartPulse.asStateFlow()

    /** Product ids the user wants but will gloriously never own. */
    private val _wishlist = MutableStateFlow<Set<Int>>(emptySet())
    val wishlist: StateFlow<Set<Int>> = _wishlist.asStateFlow()

    /** Active fake price drops: product id → temporarily reduced price. */
    private val _priceDrops = MutableStateFlow<Map<Int, Long>>(emptyMap())
    val priceDrops: StateFlow<Map<Int, Long>> = _priceDrops.asStateFlow()

    /** Consecutive days with at least one fake order — the urge-resisted streak. */
    private val _streakDays = MutableStateFlow(0)
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    private var streakLastEpochDay = 0L

    private var nextOrderId = 1

    init {
        // Restore the wishlist; wanting survives process death even if orders don't.
        viewModelScope.launch {
            val saved = WishlistStore.load(getApplication())
            if (saved.isNotEmpty()) _wishlist.update { it + saved }
        }
        viewModelScope.launch {
            val saved = StreakStore.load(getApplication())
            streakLastEpochDay = saved.lastEpochDay
            _streakDays.value = effectiveStreak(saved.days, saved.lastEpochDay, todayEpochDay())
        }
        // Rotate the flash deal forever; urgency is the product.
        viewModelScope.launch {
            var dealIndex = 0
            while (true) {
                delay(1_000)
                val left = _flashDealSecondsLeft.value - 1
                if (left <= 0) {
                    dealIndex = (dealIndex + 1) % FakeCatalog.dealCandidates.size
                    _flashDeal.value = FakeCatalog.dealCandidates[dealIndex]
                    _flashDealSecondsLeft.value = FLASH_DEAL_SECONDS
                } else {
                    _flashDealSecondsLeft.value = left
                }
            }
        }
        // Periodically "drop" the price of something on the wishlist. The drop is
        // as fake as the price, but the notification ping is gloriously real.
        viewModelScope.launch {
            while (true) {
                delay(PRICE_DROP_INTERVAL_MILLIS)
                val candidates = _wishlist.value.filterNot { it in _priceDrops.value }
                if (candidates.isEmpty()) continue
                val product = FakeCatalog.byId(candidates.random()) ?: continue
                val discountPercent = Random.nextInt(15, 41)
                val newPrice = product.priceCents * (100 - discountPercent) / 100
                _priceDrops.update { it + (product.id to newPrice) }
                Notifier.notifyPriceDrop(
                    getApplication(),
                    product.id,
                    product.name,
                    discountPercent,
                    formatPrice(newPrice),
                )
                launch {
                    delay(PRICE_DROP_LIFETIME_MILLIS)
                    _priceDrops.update { it - product.id }
                }
            }
        }
    }

    /** The catalog product with any active price drop applied — use for display and cart adds. */
    fun displayProduct(product: Product): Product =
        product.withPriceOverride(_priceDrops.value[product.id])

    // ---- Wishlist ----

    fun toggleWishlist(productId: Int) {
        _wishlist.update { if (productId in it) it - productId else it + productId }
        // A vanished wish takes its fake deal with it.
        if (productId !in _wishlist.value) _priceDrops.update { it - productId }
        viewModelScope.launch { WishlistStore.save(getApplication(), _wishlist.value) }
    }

    // ---- Cart ----

    fun cartTotalCents(items: List<CartItem>): Long = items.sumOf { it.totalCents }

    fun addToCart(product: Product, quantity: Int = 1) {
        val snapshot = displayProduct(product)
        _cart.update { items ->
            val existing = items.firstOrNull { it.product.id == snapshot.id }
            if (existing == null) {
                items + CartItem(snapshot, quantity)
            } else {
                items.map {
                    if (it.product.id == snapshot.id) it.copy(quantity = it.quantity + quantity) else it
                }
            }
        }
        _cartPulse.update { it + 1 }
    }

    fun setQuantity(productId: Int, quantity: Int) {
        _cart.update { items ->
            if (quantity <= 0) items.filterNot { it.product.id == productId }
            else items.map { if (it.product.id == productId) it.copy(quantity = quantity) else it }
        }
    }

    fun removeFromCart(productId: Int) = setQuantity(productId, 0)

    /** The classic cart retreat: not buying it, not letting go of it either. */
    fun moveToWishlist(productId: Int) {
        removeFromCart(productId)
        _wishlist.update { it + productId }
        viewModelScope.launch { WishlistStore.save(getApplication(), _wishlist.value) }
    }

    // ---- Orders & delivery simulation ----

    /** Converts the cart into a fake order and starts its delivery sim. Returns the order id. */
    fun placeOrder(): Int {
        val items = _cart.value
        val order = Order(
            id = nextOrderId++,
            items = items,
            totalCents = cartTotalCents(items),
        )
        _orders.update { listOf(order) + it }
        _cart.value = emptyList()
        advanceUrgeStreak()
        runDeliverySimulation(order.id)
        return order.id
    }

    private fun todayEpochDay(): Long = System.currentTimeMillis() / 86_400_000L

    private fun advanceUrgeStreak() {
        val today = todayEpochDay()
        if (today == streakLastEpochDay) return
        _streakDays.value = advanceStreak(_streakDays.value, streakLastEpochDay, today)
        streakLastEpochDay = today
        viewModelScope.launch {
            StreakStore.save(getApplication(), StreakStore.Streak(_streakDays.value, today))
        }
    }

    private fun updateOrder(id: Int, transform: (Order) -> Order) {
        _orders.update { orders -> orders.map { if (it.id == id) transform(it) else it } }
    }

    private fun runDeliverySimulation(orderId: Int) {
        viewModelScope.launch {
            delay(3_000)
            updateOrder(orderId) { it.copy(status = OrderStatus.PACKING) }
            delay(5_000)
            updateOrder(orderId) { it.copy(status = OrderStatus.COURIER_ASSIGNED) }
            delay(4_000)
            updateOrder(orderId) { it.copy(status = OrderStatus.ON_THE_WAY) }
            val stepMillis = 250L
            var elapsed = 0L
            while (elapsed < COURIER_TRIP_MILLIS) {
                delay(stepMillis)
                elapsed += stepMillis
                val progress = (elapsed.toFloat() / COURIER_TRIP_MILLIS).coerceAtMost(1f)
                updateOrder(orderId) { it.copy(progress = progress) }
            }
            updateOrder(orderId) { it.copy(status = OrderStatus.DELIVERED, progress = 1f) }
            val delivered = _orders.value.firstOrNull { it.id == orderId } ?: return@launch
            Notifier.notifyDelivered(getApplication(), orderId, formatPrice(delivered.totalCents))
        }
    }

    // ---- Stats ----

    /** Real money not spent: the sum of every fake order ever placed. */
    fun totalSavedCents(orders: List<Order>): Long = orders.sumOf { it.totalCents }
}
