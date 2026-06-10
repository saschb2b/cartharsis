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
