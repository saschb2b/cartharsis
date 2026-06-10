package com.cartharsis.data

/**
 * Decides when a fake event deserves a real notification. The guiding rule is
 * wellness over engagement: the app never interrupts someone who is already
 * looking at it, never pings about deals at night, and never pings about deals
 * more than once per half hour. Pure functions so the calm is unit-testable.
 */
object NotificationPolicy {

    /** Minimum gap between two wishlist price-drop pings. */
    const val DROP_PING_COOLDOWN_MILLIS = 30 * 60_000L

    /** Deal pings sleep from [QUIET_START_HOUR] until [QUIET_END_HOUR]. */
    const val QUIET_START_HOUR = 22
    const val QUIET_END_HOUR = 8

    fun isQuietHour(hourOfDay: Int): Boolean = hourOfDay >= QUIET_START_HOUR || hourOfDay < QUIET_END_HOUR

    /**
     * Delivery pings fire only when the app is in the background; in the
     * foreground the tracking screen is already telling the story. They are
     * exempt from quiet hours: a delivery is the expected payoff of an order
     * placed a minute earlier, not an unsolicited interruption.
     */
    fun shouldPingDelivery(appVisible: Boolean): Boolean = !appVisible

    /**
     * Price-drop pings are the optional garnish, so every gate applies:
     * background only, daytime only, and at most one per cooldown window.
     */
    fun shouldPingPriceDrop(appVisible: Boolean, hourOfDay: Int, lastPingMillis: Long, nowMillis: Long): Boolean =
        !appVisible &&
            !isQuietHour(hourOfDay) &&
            nowMillis - lastPingMillis >= DROP_PING_COOLDOWN_MILLIS
}
