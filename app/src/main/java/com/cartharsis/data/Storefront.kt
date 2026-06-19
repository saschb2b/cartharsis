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

/**
 * A heterogeneous "magazine" module for the home feed. The variety of *shapes*
 * down the page — not just of products — is what makes the storefront feel like
 * a curated store rather than a stack of identical shelves.
 */
sealed interface HomeModule {
    /** Colorful entry tiles; tapping one filters the grid to [MoodEntry.category]. */
    data class Moods(val entries: List<MoodEntry>) : HomeModule

    /** A themed collection as a bento — one big tile beside a cluster of smalls. */
    data class Bento(val title: String, val big: Product, val smalls: List<Product>) : HomeModule

    /** An editorial ranked list, "from the editors". */
    data class Editors(val title: String, val items: List<Product>) : HomeModule

    /** Two medium feature cards side by side. */
    data class Duo(val title: String, val left: Product, val right: Product) : HomeModule
}

/** A curated entry tile pairing an evocative mood with an existing [category]. */
data class MoodEntry(val label: String, val emoji: String, val category: String)

private val ALL_MOODS = listOf(
    MoodEntry("Treat yourself", "💎", "Beauty"),
    MoodEntry("Cozy night in", "🕯️", "Self-Care"),
    MoodEntry("Press start", "🎮", "Gaming"),
    MoodEntry("Snack run", "🍿", "Snacks"),
    MoodEntry("Geek out", "🤖", "Tech"),
    MoodEntry("Good vibes", "🎧", "Audio"),
    MoodEntry("Collector mode", "🎴", "Trading Cards"),
    MoodEntry("Pure chaos", "🌀", "Chaos"),
    MoodEntry("Get moving", "🏃", "Fitness"),
    MoodEntry("Nest mode", "🛋️", "Home"),
    MoodEntry("Fit check", "🧥", "Fashion"),
    MoodEntry("Touch grass", "🏕️", "Outdoors"),
)

private const val BENTO_SMALLS = 4
private const val MAX_CONTENT_MODULES = 4

/** One themed pool to draw a module from: an editorial title and its products. */
private data class Theme(val title: String, val pool: List<Product>)

/**
 * The home "magazine": a varied deck of heterogeneous modules dealt from a fixed
 * catalog and one per-open seed, so the page reads differently every open with
 * no backend and no repeats. Mood tiles lead; then a seeded shuffle of themed
 * collections, each rendered as one of three rotating shapes (bento / editors'
 * list / spotlight duo) so consecutive bands never look alike. Products are
 * deduped down the page, and [excludeIds] keeps the hero feature from recurring.
 * The variety is the reward; nothing here pressures the open or the scroll.
 */
fun homeModules(
    catalog: List<Product>,
    seed: Long,
    hourOfDay: Int,
    excludeIds: Set<Int> = emptySet(),
): List<HomeModule> {
    val rng = Random(seed)
    val used = excludeIds.toMutableSet()
    val modules = mutableListOf<HomeModule>()

    // Lead with mood tiles — evocative doorways into the catalog's real categories.
    val present = catalog.mapTo(mutableSetOf()) { it.category }
    val moods = ALL_MOODS.filter { it.category in present }.shuffled(rng).take(6)
    if (moods.size >= 3) modules += HomeModule.Moods(moods)

    // Titles are clean typographic headers — no trailing emoji. The decorative
    // glyphs live on the mood tiles and the editorial kickers, where an icon is
    // the element's job; a bold section header stays uncluttered.
    val timeTheme = when {
        hourOfDay < 12 -> Theme(
            "Slow morning picks",
            catalog.filter { it.category in setOf("Kitchen", "Beauty", "Self-Care", "Snacks") },
        )
        hourOfDay >= 22 || hourOfDay < 5 -> Theme(
            "The 2am temptations",
            catalog.filter { it.category in setOf("Gaming", "Snacks", "Tech", "Audio", "Trading Cards") },
        )
        else -> Theme("Afternoon wander", catalog)
    }
    val themes = listOf(
        Theme("New to the void", catalog),
        Theme("Under \$20, still nothing", catalog.filter { it.priceCents < 2_000 }),
        Theme("Treat yourself", catalog.sortedByDescending { it.priceCents }.take(40)),
        Theme("Everyone's not-buying", catalog),
        Theme("Weekend wander", catalog.filter { it.category in setOf("Chaos", "Hobbies", "Snacks", "Outdoors") }),
        Theme("Collector's corner", catalog.filter { it.category in setOf("Trading Cards", "Hobbies") }),
        Theme("Tiny luxuries", catalog.filter { it.priceCents in 2_000..6_000 }),
        Theme("The deep cuts", catalog.sortedByDescending { it.id }.take(70)),
        timeTheme,
    ).shuffled(rng)

    // Rotate shapes so consecutive emitted modules never share a shape.
    var shape = rng.nextInt(3)
    for (theme in themes) {
        if (modules.count { it !is HomeModule.Moods } >= MAX_CONTENT_MODULES) break
        val avail = theme.pool.filter { it.id !in used }.shuffled(rng)
        val module: HomeModule? = when (shape % 3) {
            0 -> if (avail.size >= 1 + BENTO_SMALLS) {
                HomeModule.Bento(theme.title, avail.first(), avail.drop(1).take(BENTO_SMALLS))
            } else {
                null
            }
            1 -> if (avail.size >= 4) HomeModule.Editors(theme.title, avail.take(5)) else null
            else -> if (avail.size >= 2) HomeModule.Duo(theme.title, avail[0], avail[1]) else null
        }
        if (module != null) {
            modules += module
            used += moduleProductIds(module)
            shape++
        }
    }
    return modules
}

/** The product ids a module shows, for cross-module dedup. */
fun moduleProductIds(module: HomeModule): List<Int> = when (module) {
    is HomeModule.Bento -> listOf(module.big.id) + module.smalls.map { it.id }
    is HomeModule.Editors -> module.items.map { it.id }
    is HomeModule.Duo -> listOf(module.left.id, module.right.id)
    is HomeModule.Moods -> emptyList()
}
