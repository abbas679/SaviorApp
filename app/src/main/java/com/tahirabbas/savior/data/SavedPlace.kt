package com.tahirabbas.savior.data

/**
 * A pre-saved address (Home, Office, School, or custom) used as a location
 * fallback when live GPS isn't available, and always offered as a
 * backup line in the alert SMS.
 */
data class SavedPlace(
    val label: String,       // e.g. "Home", "Office", "School", or a custom name
    val address: String      // free-text address the user typed in themselves
)
