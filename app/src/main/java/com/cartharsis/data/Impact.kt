package com.cartharsis.data

/**
 * Pure logic for the Orders screen's "your impact" payoff: turning the abstract
 * "money not spent" figure into something felt — relatable equivalents, savings
 * milestones, and earned badges. The framing is always celebratory (what you
 * kept, never what you'd lose); nothing here inflates the true figure.
 */

/** A relatable stand-in for an abstract money figure, e.g. "≈ 3 weekend trips". */
data class KeptEquivalent(val emoji: String, val text: String)

private data class EquivUnit(val cents: Long, val emoji: String, val singular: String, val plural: String)

// Smallest → largest. The headline picks the largest unit you can afford ≥1 of,
// so a big number reads as something grand and a small one still reads as a treat.
private val equivUnits = listOf(
    EquivUnit(500, "☕", "fancy coffee", "fancy coffees"),
    EquivUnit(1_800, "🍔", "burger combo", "burger combos"),
    EquivUnit(6_000, "🎬", "movie night", "movie nights"),
    EquivUnit(15_000, "🍽️", "nice dinner out", "nice dinners out"),
    EquivUnit(45_000, "🎟️", "concert ticket", "concert tickets"),
    EquivUnit(120_000, "✈️", "flight to Tokyo", "flights to Tokyo"),
    EquivUnit(350_000, "🏝️", "dream getaway", "dream getaways"),
)

/** The headline equivalent: the grandest unit affordable ≥1 of, with its count. */
fun keptEquivalent(cents: Long): KeptEquivalent? {
    val unit = equivUnits.lastOrNull { cents >= it.cents } ?: return null
    val n = (cents / unit.cents).toInt()
    return KeptEquivalent(unit.emoji, "≈ $n ${if (n == 1) unit.singular else unit.plural}")
}

/** The always-fun small equivalent in coffees, once there's at least one. */
fun keptInCoffees(cents: Long): Int = (cents / 500).toInt()

// Savings milestones in cents: $100, $500, $1k, $2.5k, $5k, $10k, $25k.
val savingsMilestones = listOf(10_000L, 50_000L, 100_000L, 250_000L, 500_000L, 1_000_000L, 2_500_000L)

/** The next savings milestone above [cents], or null once the top is passed. */
fun nextSavingsMilestone(cents: Long): Long? = savingsMilestones.firstOrNull { it > cents }

/** The highest milestone already reached (0 if none yet). */
fun lastSavingsMilestone(cents: Long): Long = savingsMilestones.lastOrNull { it <= cents } ?: 0L

/**
 * Vault fill 0f..1f: how far [cents] is toward the next milestone, measured
 * from zero. This keeps the jar encouragingly full (a quarter of the way to
 * $500 reads as a quarter-full jar) and resets gently at each crossing — from
 * ~full down to the fraction the same total is of the *new*, larger goal —
 * rather than emptying to zero.
 */
fun savingsMilestoneProgress(cents: Long): Float {
    val next = nextSavingsMilestone(cents) ?: return 1f
    return (cents.toFloat() / next.toFloat()).coerceIn(0f, 1f)
}

/** An earned-or-not achievement on the milestone shelf. */
data class Badge(val id: String, val emoji: String, val label: String, val earned: Boolean)

/**
 * Badge ids in [current] that weren't in [previous] — the ones to celebrate.
 * Empty when [previous] is null (the first observation of a session) so an
 * already-earned collection never re-fires on every app launch; only a genuine
 * in-session crossing does.
 */
fun newlyEarned(previous: Set<String>?, current: Set<String>): Set<String> =
    if (previous == null) emptySet() else current - previous

/** All badges in a stable order; [earned] reflects current stats. Celebrate on cross. */
fun badges(ordersPlaced: Int, centsKept: Long, streakDays: Int): List<Badge> = listOf(
    Badge("first", "🌱", "First order", ordersPlaced >= 1),
    Badge("orders10", "🛍️", "10 orders", ordersPlaced >= 10),
    Badge("orders50", "🏆", "50 orders", ordersPlaced >= 50),
    Badge("kept100", "💵", "$100 kept", centsKept >= 10_000),
    Badge("kept1k", "💰", "$1k kept", centsKept >= 100_000),
    Badge("kept10k", "🤑", "$10k kept", centsKept >= 1_000_000),
    Badge("streak7", "🔥", "7-day streak", streakDays >= 7),
    Badge("streak30", "📆", "30-day streak", streakDays >= 30),
)
