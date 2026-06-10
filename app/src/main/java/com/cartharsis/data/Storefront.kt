package com.cartharsis.data

import kotlin.random.Random

/**
 * Pure functions that make the home screen feel fresh on every open from a
 * fixed catalog and one per-open seed. The seed (captured when the app comes
 * to the foreground) deterministically drives the greeting, the grid order,
 * and the themed shelves — so finite content reads as effectively infinite,
 * with no backend and no network. The variety is the reward; cost stays $0.00,
 * and nothing here pressures the open or the scroll.
 */

private val morningGreetings = listOf(
    "Good morning, wanderer ☀️",
    "A fresh day of not-buying",
    "Morning browse, zero regrets",
    "Coffee and window-shopping?",
)
private val afternoonGreetings = listOf(
    "Afternoon wander 🛍️",
    "What are we not-buying today?",
    "Midday treasure hunt",
    "Browse a little. Spend nothing.",
)
private val eveningGreetings = listOf(
    "Evening wind-down 🌆",
    "Welcome back, wanderer",
    "Look, don't pay",
    "Window-shopping, perfected",
)
private val nightGreetings = listOf(
    "Night owl mode 🌙",
    "The 2am cart is open (and free)",
    "Can't sleep? Browse the void",
    "Late-night laps, no damage",
)

/** A rotating, time-of-day-aware greeting; same seed → same line within a session. */
fun homeGreeting(seed: Long, hourOfDay: Int): String {
    val pool = when {
        hourOfDay < 5 -> nightGreetings
        hourOfDay < 12 -> morningGreetings
        hourOfDay < 18 -> afternoonGreetings
        hourOfDay < 22 -> eveningGreetings
        else -> nightGreetings
    }
    return pool[Math.floorMod(seed, pool.size.toLong()).toInt()]
}

/** The browse grid reshuffled per open, so the same product never always leads. */
fun homeOrder(products: List<Product>, seed: Long): List<Product> = products.shuffled(Random(seed))

/** A named, scrollable row of products on the home screen. */
data class HomeShelf(val title: String, val products: List<Product>)

private const val SHELF_SIZE = 8
private const val MIN_SHELF = 4

/**
 * The freshness engine: a daily-stable "collection of the day" (so repeat opens
 * within a day keep some continuity) followed by per-open shelves dealt from a
 * deck of themed generators. The personalized "Rediscover" exploit row appears
 * when there's history; the rest is explore. Contents are deduped across
 * shelves so the same product doesn't repeat down the page. All seeded — finite
 * catalog, effectively non-repeating sessions, no backend.
 */
fun homeShelves(
    catalog: List<Product>,
    seed: Long,
    recentlyViewedIds: List<Int>,
    wishlistIds: Set<Int>,
    hourOfDay: Int,
    epochDay: Long,
    count: Int = 5,
): List<HomeShelf> {
    val byId = catalog.associateBy { it.id }
    val used = mutableSetOf<Int>()
    val shelves = mutableListOf<HomeShelf>()

    fun add(title: String, pool: List<Product>, rng: Random, min: Int = MIN_SHELF) {
        if (shelves.size >= count || shelves.any { it.title == title }) return
        val pick = pool.filter { it.id !in used }.shuffled(rng).take(SHELF_SIZE)
        if (pick.size >= min) {
            shelves += HomeShelf(title, pick)
            used += pick.map { it.id }
        }
    }

    // Collection of the day — stable within a day, renews at midnight.
    val dailyTitles = listOf("Today's wander 🧭", "Fresh today 🌱", "The daily drop 🎯", "Picked for today ✨")
    val dailyTitle = dailyTitles[Math.floorMod(epochDay, dailyTitles.size.toLong()).toInt()]
    add(dailyTitle, catalog, Random(epochDay * 2654435761L))

    val rng = Random(seed)

    // Personalized exploit row — worth showing even with just a couple of
    // hearted/viewed items, so it uses a lower minimum than the explore shelves.
    val rediscover = (wishlistIds.mapNotNull { byId[it] } + recentlyViewedIds.mapNotNull { byId[it] }).distinct()
    if (rediscover.isNotEmpty()) add("Rediscover 💭", rediscover, rng, min = 2)

    val timeShelf = when {
        hourOfDay < 12 -> "Morning picks ☕" to catalog.filter {
            it.category in setOf("Kitchen", "Beauty", "Self-Care", "Snacks", "Fitness")
        }
        hourOfDay >= 22 || hourOfDay < 5 -> "Night owl finds 🌙" to catalog.filter {
            it.category in setOf("Gaming", "Snacks", "Self-Care", "Tech", "Audio")
        }
        else -> "Afternoon finds 🛍️" to catalog
    }

    // The explore deck — dealt in shuffled order until `count` is reached.
    val deck: List<Pair<String, List<Product>>> = listOf(
        "Fresh finds ✨" to catalog,
        "Under \$20 🪙" to catalog.filter { it.priceCents < 2_000 },
        "Treat yourself 💎" to catalog.sortedByDescending { it.priceCents }.take(40),
        "Trending right now 🔥" to catalog,
        "Weekend wander 🎢" to catalog.filter { it.category in setOf("Chaos", "Hobbies", "Snacks") },
        "Tiny luxuries 🤏" to catalog.filter { it.priceCents in 2_000..6_000 },
        "The deep cuts 🕳️" to catalog.sortedByDescending { it.id }.take(70),
        "One of each 🗂️" to catalog.groupBy { it.category }.values.mapNotNull { it.shuffled(rng).firstOrNull() },
        timeShelf,
    )
    deck.shuffled(rng).forEach { (title, pool) -> add(title, pool, rng) }
    return shelves
}
