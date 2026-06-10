package com.cartharsis

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewModelScope
import com.cartharsis.data.CartItem
import com.cartharsis.data.FakeCatalog
import com.cartharsis.data.NotificationPolicy
import com.cartharsis.data.Order
import com.cartharsis.data.OrderStatus
import com.cartharsis.data.Product
import com.cartharsis.data.ProfileStore
import com.cartharsis.data.ReviewStore
import com.cartharsis.data.StatsStore
import com.cartharsis.data.StreakStore
import com.cartharsis.data.UserReview
import com.cartharsis.data.WishlistStore
import com.cartharsis.data.advanceStreak
import com.cartharsis.data.effectiveStreak
import com.cartharsis.data.formatPrice
import com.cartharsis.data.plusProduct
import com.cartharsis.data.withPriceOverride
import java.util.Calendar
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val FLASH_DEAL_SECONDS = 90
private const val COURIER_TRIP_MILLIS = ShopViewModel.COURIER_TRIP_SECONDS * 1_000L
private const val PRICE_DROP_INTERVAL_MILLIS = 60_000L
private const val PRICE_DROP_LIFETIME_MILLIS = 5 * 60_000L

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

    /** Reviews the user wrote, one per product — the app's only user content. */
    private val _userReviews = MutableStateFlow<Map<Int, UserReview>>(emptyMap())
    val userReviews: StateFlow<Map<Int, UserReview>> = _userReviews.asStateFlow()

    /** The fake "account"; null while DataStore loads, then gates onboarding. */
    private val _profile = MutableStateFlow<ProfileStore.Profile?>(null)
    val profile: StateFlow<ProfileStore.Profile?> = _profile.asStateFlow()

    /** Lifetime fake-shopping totals; survive process death unlike the order list. */
    private val _lifetimeStats = MutableStateFlow(StatsStore.Stats(0, 0, 0L))
    val lifetimeStats: StateFlow<StatsStore.Stats> = _lifetimeStats.asStateFlow()

    /** Recently opened product ids, newest first — feeds the "keep browsing" row. */
    private val _recentlyViewed = MutableStateFlow<List<Int>>(emptyList())
    val recentlyViewed: StateFlow<List<Int>> = _recentlyViewed.asStateFlow()

    /** Consecutive days with at least one fake order — the urge-resisted streak. */
    private val _streakDays = MutableStateFlow(0)
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    private var streakLastEpochDay = 0L

    private var nextOrderId = 1

    /**
     * When the last wishlist-drop notification fired. Seeded with "now" so the
     * first ping can come no sooner than one full cooldown after launch — the
     * app never chases someone who just put it down.
     */
    private var lastDropPingMillis = System.currentTimeMillis()

    init {
        // Restore the wishlist; wanting survives process death even if orders don't.
        viewModelScope.launch {
            val saved = WishlistStore.load(getApplication())
            if (saved.isNotEmpty()) _wishlist.update { it + saved }
        }
        viewModelScope.launch {
            val saved = ReviewStore.load(getApplication())
            // Reviews written before the load finished win over their saved versions.
            if (saved.isNotEmpty()) _userReviews.update { saved + it }
        }
        viewModelScope.launch {
            _profile.value = ProfileStore.load(getApplication())
        }
        viewModelScope.launch {
            val saved = StreakStore.load(getApplication())
            streakLastEpochDay = saved.lastEpochDay
            _streakDays.value = effectiveStreak(saved.days, saved.lastEpochDay, todayEpochDay())
        }
        viewModelScope.launch {
            val saved = StatsStore.load(getApplication())
            _lifetimeStats.update { current ->
                // Orders placed before the load finished are already counted in `current`.
                StatsStore.Stats(
                    ordersPlaced = saved.ordersPlaced + current.ordersPlaced,
                    itemsBought = saved.itemsBought + current.itemsBought,
                    centsKept = saved.centsKept + current.centsKept,
                )
            }
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
        // Periodically "drop" the price of something on the wishlist. The drop
        // itself stays an in-app delight (badges, struck-through prices); a real
        // notification only goes out when NotificationPolicy says it would be
        // welcome — app in the background, daytime, and not pinged recently.
        viewModelScope.launch {
            while (true) {
                delay(PRICE_DROP_INTERVAL_MILLIS)
                val candidates = _wishlist.value.filterNot { it in _priceDrops.value }
                if (candidates.isEmpty()) continue
                val product = FakeCatalog.byId(candidates.random()) ?: continue
                val discountPercent = Random.nextInt(15, 41)
                val newPrice = product.priceCents * (100 - discountPercent) / 100
                _priceDrops.update { it + (product.id to newPrice) }
                val now = System.currentTimeMillis()
                val shouldPing = NotificationPolicy.shouldPingPriceDrop(
                    appVisible = appInForeground(),
                    hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    lastPingMillis = lastDropPingMillis,
                    nowMillis = now,
                )
                if (shouldPing) {
                    lastDropPingMillis = now
                    Notifier.notifyPriceDrop(
                        getApplication(),
                        product.id,
                        product.name,
                        discountPercent,
                        formatPrice(newPrice),
                    )
                }
                launch {
                    delay(PRICE_DROP_LIFETIME_MILLIS)
                    _priceDrops.update { it - product.id }
                }
            }
        }
    }

    /** The catalog product with any active price drop applied — use for display and cart adds. */
    fun displayProduct(product: Product): Product = product.withPriceOverride(_priceDrops.value[product.id])

    /** Remember that a product page was opened; newest first, capped at 10. */
    fun markViewed(productId: Int) {
        _recentlyViewed.update { recent ->
            (listOf(productId) + recent.filterNot { it == productId }).take(10)
        }
    }

    // ---- Profile / onboarding ----

    /** Finishes onboarding: the "account" is created, locally and forever. */
    fun completeOnboarding(name: String, street: String, city: String) {
        val profile = ProfileStore.Profile(
            name = name.trim(),
            street = street.trim().ifBlank { ProfileStore.DEFAULT_STREET },
            city = city.trim().ifBlank { ProfileStore.DEFAULT_CITY },
            onboarded = true,
        )
        _profile.value = profile
        viewModelScope.launch { ProfileStore.save(getApplication(), profile) }
    }

    // ---- User reviews ----

    /** Saves (or replaces) the user's review of a product. */
    fun saveUserReview(productId: Int, rating: Int, text: String) {
        val review = UserReview(productId, rating, text.trim(), System.currentTimeMillis())
        _userReviews.update { it + (productId to review) }
        viewModelScope.launch { ReviewStore.save(getApplication(), _userReviews.value) }
    }

    fun deleteUserReview(productId: Int) {
        _userReviews.update { it - productId }
        viewModelScope.launch { ReviewStore.save(getApplication(), _userReviews.value) }
    }

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
        _cart.update { it.plusProduct(snapshot, quantity) }
        _cartPulse.update { it + 1 }
    }

    fun setQuantity(productId: Int, quantity: Int) {
        _cart.update { items ->
            if (quantity <= 0) {
                items.filterNot { it.product.id == productId }
            } else {
                items.map { if (it.product.id == productId) it.copy(quantity = quantity) else it }
            }
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
        _lifetimeStats.update {
            StatsStore.Stats(
                ordersPlaced = it.ordersPlaced + 1,
                itemsBought = it.itemsBought + order.itemCount,
                centsKept = it.centsKept + order.totalCents,
            )
        }
        viewModelScope.launch { StatsStore.save(getApplication(), _lifetimeStats.value) }
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
            var nearbyPinged = false
            while (elapsed < COURIER_TRIP_MILLIS) {
                delay(stepMillis)
                elapsed += stepMillis
                val progress = (elapsed.toFloat() / COURIER_TRIP_MILLIS).coerceAtMost(1f)
                updateOrder(orderId) { it.copy(progress = progress) }
                // Anticipation peaks before arrival: one nearby ping on the
                // final approach, background-only like every delivery ping.
                if (!nearbyPinged && progress >= 0.8f &&
                    NotificationPolicy.shouldPingDelivery(appInForeground())
                ) {
                    nearbyPinged = true
                    Notifier.notifyCourierNearby(getApplication(), orderId)
                }
            }
            updateOrder(orderId) { it.copy(status = OrderStatus.DELIVERED, progress = 1f) }
            val delivered = _orders.value.firstOrNull { it.id == orderId } ?: return@launch
            // In the foreground the tracking screen already celebrates the arrival;
            // the system notification is only for someone who wandered off.
            if (NotificationPolicy.shouldPingDelivery(appInForeground())) {
                Notifier.notifyDelivered(getApplication(), orderId, formatPrice(delivered.totalCents))
            }
        }
    }

    /** Whether any of our UI is on screen; viewModelScope runs on Main, so reading this is safe. */
    private fun appInForeground(): Boolean =
        ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
}
