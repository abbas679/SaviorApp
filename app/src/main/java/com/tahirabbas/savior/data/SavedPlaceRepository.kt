package com.tahirabbas.savior.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Stores user-entered addresses (Home, Office, School, custom) as plain text.
 * Deliberately free-text and offline: no geocoding API call is needed to save
 * or use these, so they work even with zero internet/data connectivity —
 * only cellular signal for the SMS itself is required.
 *
 * Exactly one place can be marked "default" — that's the one used as the
 * GPS fallback. This is intentional and user-controlled rather than picking
 * whichever place happens to be first in the list.
 */
class SavedPlaceRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("savior_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PLACES = "saved_places_v2"
        const val MAX_PLACES = 6
    }

    fun getPlaces(): List<SavedPlace> {
        val raw = prefs.getString(KEY_PLACES, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(";;").mapNotNull { entry ->
            val parts = entry.split("|", limit = 3)
            if (parts.size == 3) {
                SavedPlace(parts[0], parts[1], parts[2] == "1")
            } else null
        }
    }

    fun savePlaces(places: List<SavedPlace>) {
        val limited = places.take(MAX_PLACES)
        val raw = limited.joinToString(";;") { "${it.label}|${it.address}|${if (it.isDefault) "1" else "0"}" }
        prefs.edit().putString(KEY_PLACES, raw).apply()
    }

    fun addOrUpdatePlace(place: SavedPlace): Boolean {
        val existing = getPlaces()
        val withoutThisLabel = existing.filterNot { it.label.equals(place.label, ignoreCase = true) }
        if (withoutThisLabel.size >= MAX_PLACES) return false

        // First place saved automatically becomes the default, so there's
        // always a usable fallback without requiring an extra manual step.
        val shouldBeDefault = place.isDefault || existing.isEmpty()
        val newPlace = place.copy(isDefault = shouldBeDefault)

        val updatedList = if (shouldBeDefault) {
            withoutThisLabel.map { it.copy(isDefault = false) } + newPlace
        } else {
            withoutThisLabel + newPlace
        }
        savePlaces(updatedList)
        return true
    }

    fun removePlace(place: SavedPlace) {
        val remaining = getPlaces().filterNot { it.label == place.label }
        // If we just removed the default place, promote another one so
        // there's still a fallback available.
        if (place.isDefault && remaining.isNotEmpty() && remaining.none { it.isDefault }) {
            savePlaces(remaining.mapIndexed { index, p -> if (index == 0) p.copy(isDefault = true) else p })
        } else {
            savePlaces(remaining)
        }
    }

    fun setDefaultPlace(place: SavedPlace) {
        val updated = getPlaces().map { it.copy(isDefault = it.label == place.label) }
        savePlaces(updated)
    }

    fun getDefaultPlace(): SavedPlace? = getPlaces().firstOrNull { it.isDefault }
}
