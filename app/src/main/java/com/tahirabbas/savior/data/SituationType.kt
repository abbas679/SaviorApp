package com.tahirabbas.savior.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Each emergency category has its own alert wording, a mapped
 * emergency-service phone number (defaults set for Pakistan — see SavedPlacesScreen
 * / Settings to make this configurable per country), and a distinct color + icon
 * so the picker screen is instantly scannable in a stressful moment.
 */
enum class SituationType(
    val label: String,
    val alertPrefix: String,
    val serviceName: String,
    val serviceNumber: String,
    val color: Color,
    val icon: ImageVector
) {
    THEFT(
        label = "Theft / Robbery",
        alertPrefix = "🚨 THEFT ALERT: I am being robbed or witnessing a theft.",
        serviceName = "Police",
        serviceNumber = "15",
        color = Color(0xFF1E3A8A),
        icon = Icons.Default.Shield
    ),
    FIRE(
        label = "Fire / Explosion",
        alertPrefix = "🔥 FIRE/BLAST ALERT: There is a fire or explosion near me.",
        serviceName = "Fire Brigade",
        serviceNumber = "16",
        color = Color(0xFFE65100),
        icon = Icons.Default.LocalFireDepartment
    ),
    ACCIDENT(
        label = "Accident",
        alertPrefix = "🚗 ACCIDENT ALERT: I have been in an accident.",
        serviceName = "Rescue",
        serviceNumber = "1122",
        color = Color(0xFF6A1B9A),
        icon = Icons.Default.DirectionsCar
    ),
    MEDICAL(
        label = "Medical Emergency",
        alertPrefix = "🏥 MEDICAL ALERT: I have a medical emergency and need help.",
        serviceName = "Ambulance",
        serviceNumber = "1122",
        color = Color(0xFF00796B),
        icon = Icons.Default.LocalHospital
    ),
    OTHER(
        label = "Other Emergency",
        alertPrefix = "⚠️ EMERGENCY ALERT: I need immediate help.",
        serviceName = "Emergency",
        serviceNumber = "15",
        color = Color(0xFFB71C1C),
        icon = Icons.Default.PriorityHigh
    );

    /**
     * Builds the full SMS body. Prefers live GPS (as a Google Maps link); if GPS
     * is unavailable (no signal / location off), falls back to the user's closest
     * saved place so contacts still get a usable location with zero internet needed —
     * a plain-text address works over SMS alone.
     */
    fun buildMessage(
        latitude: Double?,
        longitude: Double?,
        fallbackPlace: SavedPlace? = null
    ): String {
        val locationText = when {
            latitude != null && longitude != null ->
                "My live location: https://maps.google.com/?q=$latitude,$longitude"
            fallbackPlace != null ->
                "Live GPS unavailable. Nearest saved location — ${fallbackPlace.label}: ${fallbackPlace.address}"
            else ->
                "Location unavailable — please try calling me directly."
        }
        return "$alertPrefix\n$locationText\nSent via Savior app."
    }
}

