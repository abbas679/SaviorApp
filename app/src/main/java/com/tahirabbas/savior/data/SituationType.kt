package com.tahirabbas.savior.data

/**
 * Each emergency category has its own alert wording and a mapped
 * emergency-service phone number (defaults set for Pakistan — see SettingsScreen
 * to make this configurable per country).
 */
enum class SituationType(
    val label: String,
    val alertPrefix: String,
    val serviceName: String,
    val serviceNumber: String
) {
    THEFT(
        label = "Theft / Robbery",
        alertPrefix = "🚨 THEFT ALERT: I am being robbed or witnessing a theft.",
        serviceName = "Police",
        serviceNumber = "15"
    ),
    FIRE(
        label = "Fire / Explosion",
        alertPrefix = "🔥 FIRE/BLAST ALERT: There is a fire or explosion near me.",
        serviceName = "Fire Brigade",
        serviceNumber = "16"
    ),
    ACCIDENT(
        label = "Accident",
        alertPrefix = "🚗 ACCIDENT ALERT: I have been in an accident.",
        serviceName = "Rescue",
        serviceNumber = "1122"
    ),
    MEDICAL(
        label = "Medical Emergency",
        alertPrefix = "🏥 MEDICAL ALERT: I have a medical emergency and need help.",
        serviceName = "Ambulance",
        serviceNumber = "1122"
    ),
    OTHER(
        label = "Other Emergency",
        alertPrefix = "⚠️ EMERGENCY ALERT: I need immediate help.",
        serviceName = "Emergency",
        serviceNumber = "15"
    );

    /**
     * Builds the full SMS body, appending a Google Maps link built from
     * the user's current latitude/longitude.
     */
    fun buildMessage(latitude: Double?, longitude: Double?): String {
        val locationText = if (latitude != null && longitude != null) {
            "My live location: https://maps.google.com/?q=$latitude,$longitude"
        } else {
            "Location unavailable — please try calling me directly."
        }
        return "$alertPrefix\n$locationText\nSent via Savior app."
    }
}
