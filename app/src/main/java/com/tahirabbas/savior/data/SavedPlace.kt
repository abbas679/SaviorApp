package com.tahirabbas.savior.data

/**
 * A pre-saved address (Home, Office, School, or custom) used as a location
 * fallback when live GPS isn't available, and always offered as a
 * backup line in the alert SMS.
 *
 * isDefault marks which single place is actually used as the fallback —
 * deliberately explicit (user picks it) rather than guessing based on
 * whichever was added first.
 */
data class SavedPlace(
    val label: String,       // e.g. "Home", "Office", "School", or a custom name
    val address: String,     // free-text address the user typed in themselves
    val isDefault: Boolean = false
)
