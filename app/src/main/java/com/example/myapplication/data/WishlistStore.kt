package com.example.myapplication.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "cartharsis")
private val WISHLIST_KEY = stringSetPreferencesKey("wishlist_ids")

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
