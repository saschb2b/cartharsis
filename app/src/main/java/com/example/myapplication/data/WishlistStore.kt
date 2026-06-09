package com.example.myapplication.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

/** The app's single DataStore file; all persisted fakery lives here. */
private val Context.dataStore by preferencesDataStore(name = "cartharsis")

private val WISHLIST_KEY = stringSetPreferencesKey("wishlist_ids")
private val STREAK_DAYS_KEY = intPreferencesKey("streak_days")
private val STREAK_LAST_DAY_KEY = longPreferencesKey("streak_last_epoch_day")

/**
 * Persists the wishlist across process death. Wanting things is free,
 * but forgetting what you wanted is a genuine loss.
 */
object WishlistStore {

    suspend fun load(context: Context): Set<Int> =
        context.dataStore.data.first()[WISHLIST_KEY]
            .orEmpty()
            .mapNotNull { it.toIntOrNull() }
            .toSet()

    suspend fun save(context: Context, ids: Set<Int>) {
        context.dataStore.edit { prefs ->
            prefs[WISHLIST_KEY] = ids.map { it.toString() }.toSet()
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
