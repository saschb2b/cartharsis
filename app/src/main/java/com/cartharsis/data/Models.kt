package com.cartharsis.data

import androidx.compose.runtime.Immutable

data class Review(val author: String, val rating: Int, val text: String)

/**
 * A catalog product. Marked [Immutable] because nothing here ever mutates in
 * place — the catalog is a fixed `val`, and price overrides / variant swaps
 * produce new instances via `copy`. The promise lets Compose skip a card
 * whose product is structurally unchanged, even across the fresh instances
 * that `withPriceOverride` hands out each recomposition.
 */
@Immutable
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
    /**
     * Bundle contents, one human line each ("Meteor Swift console", "Turbo Kart
     * Carnival (full game)"). Empty for ordinary products; non-empty turns the
     * listing into a bundle with a "What's included" card, mirroring how Amazon
     * sells a console+game as its own listing.
     */
    val includes: List<String> = emptyList(),
    /**
     * Variant grouping, mirroring Amazon's swatch selector. Products sharing a
     * [variantGroup] are siblings (e.g. controller colors); [variantLabel] and
     * [variantAxis] name the swatch ("Volcanic Red" under the "Color" axis).
     * Each variant stays its own listing — own id, price, reviews — so the
     * cart, search, and reveal all keep working; the PDP just swaps between
     * siblings in place.
     */
    val variantGroup: String? = null,
    val variantLabel: String? = null,
    val variantAxis: String = "Color",
) {
    val discountPercent: Int?
        get() = originalPriceCents?.let { ((1 - priceCents.toDouble() / it) * 100).toInt() }

    val isBundle: Boolean get() = includes.isNotEmpty()
}

/** A review the user wrote themselves — the one voice the pool can't fake. */
data class UserReview(val productId: Int, val rating: Int, val text: String, val createdAtMillis: Long)

// Codec for DataStore string-set persistence. The separator is a control
// character no soft keyboard produces, and the free-text field comes last
// (parsed with a limit) so it may contain anything at all.
private const val USER_REVIEW_SEP = '\u0001'

fun encodeUserReview(review: UserReview): String = buildString {
    append(review.productId)
    append(USER_REVIEW_SEP)
    append(review.rating)
    append(USER_REVIEW_SEP)
    append(review.createdAtMillis)
    append(USER_REVIEW_SEP)
    append(review.text)
}

fun decodeUserReview(encoded: String): UserReview? {
    val parts = encoded.split(USER_REVIEW_SEP, limit = 4)
    if (parts.size != 4) return null
    return UserReview(
        productId = parts[0].toIntOrNull() ?: return null,
        rating = parts[1].toIntOrNull()?.takeIf { it in 1..5 } ?: return null,
        text = parts[3],
        createdAtMillis = parts[2].toLongOrNull() ?: return null,
    )
}

// Codec for the card binder's DataStore string set: "game␁cardName". Same
// control-character separator trick as user reviews; card names are fixed
// catalog strings, so two fields and no free text.
private const val BINDER_SEP = '\u0001'

fun encodeBinderCard(game: String, cardName: String): String = "$game$BINDER_SEP$cardName"

fun decodeBinderCard(encoded: String): Pair<String, String>? {
    val parts = encoded.split(BINDER_SEP, limit = 2)
    if (parts.size != 2 || parts[0].isBlank() || parts[1].isBlank()) return null
    return parts[0] to parts[1]
}

data class CartItem(val product: Product, val quantity: Int) {
    val totalCents: Long get() = product.priceCents * quantity
}

enum class OrderStatus(
    val emoji: String,
    val label: String,
    val detail: String,
    /** Short uppercase pill shown on the tracking map header, courier-style. */
    val badge: String,
) {
    CONFIRMED("✅", "Order confirmed", "Your order of nothing has been received.", "CONFIRMED"),
    PACKING("📦", "Packing", "Someone is carefully wrapping the void.", "PACKING"),
    COURIER_ASSIGNED("🛵", "Courier assigned", "Your courier has the package and is heading out.", "DISPATCHED"),
    ON_THE_WAY("🚀", "On the way", "Your nothing is moving through the city.", "TRANSIT"),
    DELIVERED("🧘", "Delivered", "Nothing has arrived. Exactly as planned.", "ARRIVED"),
}

// A stable, courier-style alphanumeric — believable on the tracking header,
// the wink kept subtle ("CT" = Cartharsis Transit). Ambiguous glyphs (I/O/
// 0/1) are left out the way real carriers do. Seeded from the order id so a
// revisit shows the same code.
private const val CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

fun trackingCode(orderId: Int): String {
    var seed = Math.floorMod(orderId * 2_654_435_761L + 1_013L, 1L shl 40)
    val block = buildString {
        repeat(5) {
            append(CODE_ALPHABET[(seed % CODE_ALPHABET.length).toInt()])
            seed /= CODE_ALPHABET.length
        }
    }
    return "#$block-CT%03d".format(Math.floorMod(orderId * 37 + 11, 1000))
}

/**
 * An order, [Immutable] for the same reason as [Product]: the items list is
 * snapshotted at checkout and never mutated, and status/progress changes
 * create a new instance via `copy`. Lets order rows skip while only the
 * actively-delivering order's progress ticks.
 */
@Immutable
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
    if (overrideCents == null || overrideCents >= priceCents) {
        this
    } else {
        copy(
            priceCents = overrideCents,
            originalPriceCents = maxOf(originalPriceCents ?: 0, priceCents),
        )
    }

/**
 * Formats a base price (authored in USD cents) in the currency the shopper
 * picked during onboarding. Reads [CurrencyState], a snapshot value, so every
 * price in the app reformats the instant the currency changes. Each currency
 * pins the grouping locale, so a number never turns "1,299.00" into "1.299,00".
 */
fun formatPrice(cents: Long): String = CurrencyState.active.format(cents)

/**
 * Adds a product snapshot to cart lines: merges into an existing line for the
 * same product (keeping that line's price snapshot) or appends a new one.
 */
fun List<CartItem>.plusProduct(snapshot: Product, quantity: Int): List<CartItem> {
    val existing = firstOrNull { it.product.id == snapshot.id }
        ?: return this + CartItem(snapshot, quantity)
    return map {
        if (it.product.id == snapshot.id) it.copy(quantity = it.quantity + quantity) else it
    }
}

/**
 * Fake scarcity for roughly a third of the catalog, derived from the id so it
 * is stable across runs. The stock is as imaginary as the product, which is
 * why it never actually runs out.
 */
val Product.fakeStockLeft: Int?
    get() = if (id % 3 == 0) 2 + id % 4 else null

/**
 * One fake order per day keeps the real spending away. Advances the
 * "urge resisted" streak: same day is a no-op, the next day extends it,
 * any gap restarts at 1.
 */
fun advanceStreak(currentDays: Int, lastEpochDay: Long, todayEpochDay: Long): Int = when {
    todayEpochDay == lastEpochDay -> currentDays
    todayEpochDay - lastEpochDay == 1L -> currentDays + 1
    else -> 1
}

/** What a saved streak is worth today: a gap of more than a day means it's broken. */
fun effectiveStreak(savedDays: Int, lastEpochDay: Long, todayEpochDay: Long): Int =
    if (todayEpochDay - lastEpochDay > 1) 0 else savedDays

/** "Jun 9, 8:51 PM" — order history needs a when, even for orders of nothing. */
fun formatOrderDate(millis: Long): String = java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.getDefault())
    .format(java.util.Date(millis))

/**
 * "JUN 2026" for the wallet's MEMBER SINCE line. Installs that predate the
 * stored join date (epoch day 0) get the deadpan "DAY ONE" instead.
 */
fun formatMemberSince(epochDay: Long): String = if (epochDay <= 0L) {
    "DAY ONE"
} else {
    java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault())
        .format(java.util.Date(epochDay * 86_400_000L))
        .uppercase(java.util.Locale.getDefault())
}
