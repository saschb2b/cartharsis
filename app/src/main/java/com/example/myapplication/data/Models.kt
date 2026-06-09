package com.example.myapplication.data

data class Review(
    val author: String,
    val rating: Int,
    val text: String,
)

data class Product(
    val id: Int,
    val name: String,
    val emoji: String,
    val tagline: String,
    val description: String,
    val priceCents: Long,
    val category: String,
    val rating: Double,
    val reviewCount: Int,
    val reviews: List<Review>,
    /** When set, the product is "on sale" and this is the crossed-out price. */
    val originalPriceCents: Long? = null,
) {
    val discountPercent: Int?
        get() = originalPriceCents?.let { ((1 - priceCents.toDouble() / it) * 100).toInt() }
}

data class CartItem(
    val product: Product,
    val quantity: Int,
) {
    val totalCents: Long get() = product.priceCents * quantity
}

enum class OrderStatus(val emoji: String, val label: String, val detail: String) {
    CONFIRMED("✅", "Order confirmed", "Your order of nothing has been received."),
    PACKING("📦", "Packing", "Someone is carefully wrapping the void."),
    COURIER_ASSIGNED("🛵", "Courier assigned", "Min-jun has accepted your delivery."),
    ON_THE_WAY("🚀", "On the way", "Your nothing is moving through the city."),
    DELIVERED("🧘", "Delivered", "Nothing has arrived. Exactly as planned."),
}

data class Order(
    val id: Int,
    val items: List<CartItem>,
    val totalCents: Long,
    val status: OrderStatus = OrderStatus.CONFIRMED,
    /** Courier progress across town, 0f..1f, only meaningful from COURIER_ASSIGNED on. */
    val progress: Float = 0f,
    val placedAtMillis: Long = System.currentTimeMillis(),
) {
    val itemCount: Int get() = items.sumOf { it.quantity }
}

/**
 * Applies a fake "price drop" to a product for display and cart snapshots.
 * The old price becomes the crossed-out anchor (keeping an existing, higher
 * anchor if the product was already on sale). Ignores non-drops.
 */
fun Product.withPriceOverride(overrideCents: Long?): Product =
    if (overrideCents == null || overrideCents >= priceCents) this
    else copy(
        priceCents = overrideCents,
        originalPriceCents = maxOf(originalPriceCents ?: 0, priceCents),
    )

fun formatPrice(cents: Long): String {
    val dollars = cents / 100
    val rest = cents % 100
    return "$%,d.%02d".format(dollars, rest)
}

/** "Jun 9, 8:51 PM" — order history needs a when, even for orders of nothing. */
fun formatOrderDate(millis: Long): String =
    java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.getDefault())
        .format(java.util.Date(millis))
