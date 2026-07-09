package com.tahirabbas.savior.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Stores user-entered addresses (Home, Office, School, custom) as plain text.
 * Deliberately free-text and offline: no geocoding API call is needed to save
 * or use these, so they work even with zero internet/data connectivity —
 * only cellular signal for the SMS itself is required.
 */
class SavedPlaceRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("savior_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PLACES = "saved_places"
        const val MAX_PLACES = 6
    }

    fun getPlaces(): List<SavedPlace> {
        val raw = prefs.getString(KEY_PLACES, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(";;").mapNotNull { entry ->
            val parts = entry.split("|", limit = 2)
            if (parts.size == 2) SavedPlace(parts[0], parts[1]) else null
        }
    }

    fun savePlaces(places: List<SavedPlace>) {
        val limited = places.take(MAX_PLACES)
        val raw = limited.joinToString(";;") { "${it.label}|${it.address}" }
        prefs.edit().putString(KEY_PLACES, raw).apply()
    }

    fun addOrUpdatePlace(place: SavedPlace): Boolean {
        val current = getPlaces().filterNot { it.label.equals(place.label, ignoreCase = true) }
        if (current.size >= MAX_PLACES) return false
        savePlaces(current + place)
        return true
    }

    fun removePlace(place: SavedPlace) {
        savePlaces(getPlaces().filterNot { it.label == place.label })
    }
}
