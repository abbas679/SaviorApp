package com.tahirabbas.savior.utils

import android.telephony.SmsManager
import com.tahirabbas.savior.data.EmergencyContact

/**
 * Caller must have SEND_SMS permission granted before calling sendEmergencyAlert.
 * Returns the list of contacts the message failed to send to (empty = all succeeded).
 */
object SmsHelper {

    fun sendEmergencyAlert(
        contacts: List<EmergencyContact>,
        message: String
    ): List<EmergencyContact> {
        val smsManager = SmsManager.getDefault()
        val failed = mutableListOf<EmergencyContact>()

        contacts.forEach { contact ->
            try {
                // SMS messages have a ~160 char limit per part; divideMessage handles
                // splitting longer alerts (with the maps link) into multiple parts.
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(
                    contact.phoneNumber, null, parts, null, null
                )
            } catch (e: Exception) {
                failed.add(contact)
            }
        }
        return failed
    }
}
