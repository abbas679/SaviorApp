package com.tahirabbas.savior.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Simple SharedPreferences-backed store for the user's emergency contacts.
 * Kept dependency-free (no Room/Gson) so the project stays easy to read end-to-end.
 * Contacts are stored as "name|phone" entries joined by ";;".
 */
class ContactRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("savior_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CONTACTS = "emergency_contacts"
        const val MAX_CONTACTS = 5
    }

    fun getContacts(): List<EmergencyContact> {
        val raw = prefs.getString(KEY_CONTACTS, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(";;").mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size == 2) EmergencyContact(parts[0], parts[1]) else null
        }
    }

    fun saveContacts(contacts: List<EmergencyContact>) {
        val limited = contacts.take(MAX_CONTACTS)
        val raw = limited.joinToString(";;") { "${it.name}|${it.phoneNumber}" }
        prefs.edit().putString(KEY_CONTACTS, raw).apply()
    }

    fun addContact(contact: EmergencyContact): Boolean {
        val current = getContacts()
        if (current.size >= MAX_CONTACTS) return false
        saveContacts(current + contact)
        return true
    }

    fun removeContact(contact: EmergencyContact) {
        val current = getContacts().filterNot { it.phoneNumber == contact.phoneNumber }
        saveContacts(current)
    }
}
