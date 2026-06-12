package com.cartharsis.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

/** The app's single DataStore file; all persisted fakery lives here. */
private val Context.dataStore by preferencesDataStore(name = "cartharsis")

private val WISHLIST_KEY = stringSetPreferencesKey("wishlist_ids")
private val USER_REVIEWS_KEY = stringSetPreferencesKey("user_reviews")
private val PROFILE_NAME_KEY = stringPreferencesKey("profile_name")
private val PROFILE_STREET_KEY = stringPreferencesKey("profile_street")
private val PROFILE_CITY_KEY = stringPreferencesKey("profile_city")
private val PROFILE_ONBOARDED_KEY = booleanPreferencesKey("profile_onboarded")
private val STREAK_DAYS_KEY = intPreferencesKey("streak_days")
private val STREAK_LAST_DAY_KEY = longPreferencesKey("streak_last_epoch_day")
private val BINDER_KEY = stringSetPreferencesKey("binder_cards")
private val MOPPLING_KEY = stringSetPreferencesKey("moppling_shelf")
private val STATS_ORDERS_KEY = intPreferencesKey("stats_orders_placed")
private val STATS_ITEMS_KEY = intPreferencesKey("stats_items_bought")
private val STATS_CENTS_KEY = longPreferencesKey("stats_cents_kept")

/**
 * Persists the wishlist across process death. Wanting things is free,
 * but forgetting what you wanted is a genuine loss.
 */
object WishlistStore {

    suspend fun load(context: Context): Set<Int> = context.dataStore.data.first()[WISHLIST_KEY]
        .orEmpty()
        .mapNotNull { it.toIntOrNull() }
        .toSet()

    suspend fun save(context: Context, ids: Set<Int>) {
        context.dataStore.edit { prefs ->
            prefs[WISHLIST_KEY] = ids.map { it.toString() }.toSet()
        }
    }
}

/**
 * Persists the card binder: every chase card ever pulled from a pack rip,
 * encoded via [encodeBinderCard]. Orders are session-only by design, but a
 * collection is a collection — the binder is forever.
 */
object BinderStore {

    suspend fun load(context: Context): Set<String> = context.dataStore.data.first()[BINDER_KEY].orEmpty()

    suspend fun save(context: Context, cards: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[BINDER_KEY] = cards
        }
    }
}

/**
 * Persists the Moppling shelf: every blind-box figure ever revealed,
 * encoded "wave␁figureName" with [encodeBinderCard] — the binder's codec
 * and the binder's promise. Orders are session-only; a collection is
 * forever.
 */
object MopplingShelfStore {

    suspend fun load(context: Context): Set<String> = context.dataStore.data.first()[MOPPLING_KEY].orEmpty()

    suspend fun save(context: Context, figures: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[MOPPLING_KEY] = figures
        }
    }
}

/**
 * The "account": a name and an imaginary address, living on this phone and
 * nowhere else. Existing installs that predate onboarding stay onboarded.
 */
object ProfileStore {

    data class Profile(
        val name: String = "",
        val street: String = DEFAULT_STREET,
        val city: String = DEFAULT_CITY,
        val onboarded: Boolean = false,
    )

    const val DEFAULT_STREET = "Apt ∞, Anticipation Street"
    const val DEFAULT_CITY = "Dopamine City"

    suspend fun load(context: Context): Profile {
        val prefs = context.dataStore.data.first()
        return Profile(
            name = prefs[PROFILE_NAME_KEY] ?: "",
            street = prefs[PROFILE_STREET_KEY] ?: DEFAULT_STREET,
            city = prefs[PROFILE_CITY_KEY] ?: DEFAULT_CITY,
            // An install with a wishlist or stats predates onboarding;
            // don't make a regular ask them to "create an account".
            onboarded = prefs[PROFILE_ONBOARDED_KEY]
                ?: (prefs[WISHLIST_KEY] != null || prefs[STATS_ORDERS_KEY] != null),
        )
    }

    suspend fun save(context: Context, profile: Profile) {
        context.dataStore.edit { prefs ->
            prefs[PROFILE_NAME_KEY] = profile.name
            prefs[PROFILE_STREET_KEY] = profile.street
            prefs[PROFILE_CITY_KEY] = profile.city
            prefs[PROFILE_ONBOARDED_KEY] = profile.onboarded
        }
    }
}

/**
 * Persists the reviews the user writes — the app's only user-generated
 * content. One review per product, keyed by product id.
 */
object ReviewStore {

    suspend fun load(context: Context): Map<Int, UserReview> = context.dataStore.data.first()[USER_REVIEWS_KEY]
        .orEmpty()
        .mapNotNull(::decodeUserReview)
        .associateBy { it.productId }

    suspend fun save(context: Context, reviews: Map<Int, UserReview>) {
        context.dataStore.edit { prefs ->
            prefs[USER_REVIEWS_KEY] = reviews.values.map(::encodeUserReview).toSet()
        }
    }
}

/**
 * Lifetime totals across every fake order ever placed. The order list itself
 * is session-only by design; the bragging numbers are forever.
 */
object StatsStore {

    data class Stats(val ordersPlaced: Int, val itemsBought: Int, val centsKept: Long)

    suspend fun load(context: Context): Stats {
        val prefs = context.dataStore.data.first()
        return Stats(
            ordersPlaced = prefs[STATS_ORDERS_KEY] ?: 0,
            itemsBought = prefs[STATS_ITEMS_KEY] ?: 0,
            centsKept = prefs[STATS_CENTS_KEY] ?: 0L,
        )
    }

    suspend fun save(context: Context, stats: Stats) {
        context.dataStore.edit { prefs ->
            prefs[STATS_ORDERS_KEY] = stats.ordersPlaced
            prefs[STATS_ITEMS_KEY] = stats.itemsBought
            prefs[STATS_CENTS_KEY] = stats.centsKept
        }
    }
}

/** Persists the "shopping urge resisted" streak: days + the last day it advanced. */
object StreakStore {

    data class Streak(val days: Int, val lastEpochDay: Long)

    suspend fun load(context: Context): Streak {
        val prefs = context.dataStore.data.first()
        return Streak(
            days = prefs[STREAK_DAYS_KEY] ?: 0,
            lastEpochDay = prefs[STREAK_LAST_DAY_KEY] ?: 0L,
        )
    }

    suspend fun save(context: Context, streak: Streak) {
        context.dataStore.edit { prefs ->
            prefs[STREAK_DAYS_KEY] = streak.days
            prefs[STREAK_LAST_DAY_KEY] = streak.lastEpochDay
        }
    }
}
