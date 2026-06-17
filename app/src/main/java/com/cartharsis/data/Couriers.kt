package com.cartharsis.data

/**
 * The one real person in an app full of fakery: the courier who shows up. Each
 * has a face, their own ride, a rating, a one-line bio, a voice for the
 * en-route moments, and a handwritten sign-off for the doorstep. The satire
 * stays in the frame, they take genuine, deadpan pride in delivering nothing,
 * perfectly, on time.
 */
data class Courier(
    val id: String,
    val name: String,
    /** A face for the courier card; an emoji, in the app's no-image-library spirit. */
    val avatar: String,
    /** Their own ride, shown on the map and the card. */
    val vehicle: String,
    val rating: Double,
    /** A one-line bio under the name. */
    val tagline: String,
    /** En-route vignettes in this courier's own voice; bucketed across the trip. */
    val moments: List<String>,
    /** The note they leave at the door, signed. */
    val signoff: String,
)

object Couriers {

    val minjun = Courier(
        id = "minjun",
        name = "Min-jun",
        avatar = "🧑",
        vehicle = "🛵",
        rating = 4.99,
        tagline = "Eleven years delivering nothing. Never once late.",
        moments = listOf(
            "🛵 Took the quiet streets. Your nothing prefers them.",
            "🚦 Hit every green light. The city's with us today.",
            "🪢 Pulled over to recheck the straps. The void is secure.",
            "🏠 Turning onto your street now. I never lose one.",
        ),
        signoff = "Set your nothing down gently, right by the door, squared away just so. " +
            "Rest easy. See you next time. — Min-jun",
    )

    val aria = Courier(
        id = "aria",
        name = "Aria",
        avatar = "👩‍🦰",
        vehicle = "🚲",
        rating = 4.97,
        tagline = "Bikes the whole route. Says hi to every nothing.",
        moments = listOf(
            "🚲 Told your nothing a joke. It's still thinking about it.",
            "☕ Stopped for a coffee. Your package waited politely outside.",
            "🎶 Humming to it now. It has no notes, but it listens.",
            "🏡 Coming up your block. It's a little shy, be kind.",
        ),
        signoff = "Dropped your nothing off with a wave. We had a lovely chat on the way. " +
            "Catch you on the next one! — Aria",
    )

    val dario = Courier(
        id = "dario",
        name = "Dario",
        avatar = "🧔",
        vehicle = "🏍️",
        rating = 4.92,
        tagline = "Fastest empty hands in the city. Allegedly.",
        moments = listOf(
            "🏍️ New personal best to the bridge. Nothing slows me down.",
            "💨 Weaved through the traffic. The void held on tight.",
            "⏱️ Beat my own record again. Don't tell the others.",
            "🏁 Final stretch. Hold onto your nothing.",
        ),
        signoff = "Delivered in record time, like always. Didn't spill a single nothing. " +
            "— Dario",
    )

    val yuki = Courier(
        id = "yuki",
        name = "Yuki",
        avatar = "🧘",
        vehicle = "🛴",
        rating = 4.98,
        tagline = "Delivers nothing. Carries it lightly.",
        moments = listOf(
            "🛴 The lighter the load, the freer the ride. Yours is the lightest.",
            "🌊 Paused at the river. Watched it carry nothing, beautifully.",
            "🍃 No weight, no worry. We drift toward your door.",
            "🧘 Arriving soon. Breathe with me.",
        ),
        signoff = "Brought your nothing the long, calm way. Leave it unopened a moment, " +
            "the quiet is the real gift. — Yuki",
    )

    val bo = Courier(
        id = "bo",
        name = "Bo",
        avatar = "🧓",
        vehicle = "🚐",
        rating = 4.95,
        tagline = "Drives the van. Knows every dog on the route.",
        moments = listOf(
            "🚐 Three good dogs so far. Your nothing approves of all of them.",
            "🐈 A cat judged me at the corner. Fair enough.",
            "🦆 Slowed for a duck crossing. Worth every second.",
            "🐕 Rounding your block. The neighbor's dog is already excited.",
        ),
        signoff = "Parked the van and walked your nothing to the step myself. Gave the " +
            "local dog a pat on your behalf. — Bo",
    )

    /** The rare one: a guest you only meet about one order in thirteen. */
    val vega = Courier(
        id = "vega",
        name = "Captain Vega",
        avatar = "🧑‍🚀",
        vehicle = "🚀",
        rating = 5.0,
        tagline = "Interstellar courier. Delivers the void at escape velocity.",
        moments = listOf(
            "🚀 Cleared the stratosphere. Your nothing is weightless up here.",
            "🪐 Slingshot around a streetlight for speed. Standard maneuver.",
            "🛰️ Re-entry initiated. Cargo bay: gloriously empty.",
            "✨ Final descent to your doorstep. Hold steady.",
        ),
        signoff = "Touched down at your door from low orbit. The void traveled first class. " +
            "It was an honor. — Capt. Vega 🚀",
    )

    /** The regulars you might be assigned; Vega is the rare rocket guest apart. */
    val commons = listOf(minjun, aria, dario, yuki, bo)
    val all = commons + vega

    fun byId(id: String): Courier = all.firstOrNull { it.id == id } ?: minjun

    /** Picks a shopper's regular from a stable seed, once, then it persists. */
    fun pickRegular(seed: Long): Courier = commons[Math.floorMod(seed, commons.size.toLong()).toInt()]

    /**
     * The courier for an order: usually your regular, sometimes a guest, and
     * rarely the rocket courier. Seeded by order id, so a revisit always shows
     * the same face. Same stable hash idiom as the tracking code.
     */
    fun forOrder(orderId: Int, regularId: String): Courier {
        val h = Math.floorMod(orderId.toLong() * 2_654_435_761L + 1_013L, 1L shl 31).toInt()
        if (h % 13 == 0) return vega
        if (h % 10 < 7) return byId(regularId)
        val guests = commons.filter { it.id != regularId }
        return guests[(h / 10) % guests.size]
    }
}

/** "1st" / "2nd" / "3rd" / "11th" — for the "your Nth delivery together" line. */
fun ordinal(n: Int): String {
    val suffix = if (n % 100 in 11..13) {
        "th"
    } else {
        when (n % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }
    return "$n$suffix"
}

/** The relationship line on the courier card: warm on the very first meeting. */
fun deliveriesTogetherLine(courierName: String, nth: Int): String =
    if (nth <= 1) "Your first delivery with $courierName" else "Your ${ordinal(nth)} delivery with $courierName"
