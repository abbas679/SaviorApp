package com.tahirabbas.savior.utils

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings

/**
 * Silences the phone (ringer + notifications) the instant an emergency is
 * triggered — so if the person who caused the emergency is holding/watching
 * the phone, a call-back from a relative or responder doesn't ring out loud
 * and expose that help was called.
 *
 * IMPORTANT: since Android 6.0, apps can't just flip the ringer to silent —
 * that requires "Do Not Disturb access" (ACCESS_NOTIFICATION_POLICY), which
 * the user must grant once via a system settings screen. We can't skip this;
 * it's a deliberate OS restriction to stop apps silently muting phones.
 */
object SilentModeHelper {

    fun hasDndAccess(context: Context): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    /** Launches the system screen where the user grants Do Not Disturb access. */
    fun requestDndAccessIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
    }

    /**
     * Silences the ringer immediately. Returns true if it succeeded.
     * Safe to call even without DND access — it will simply no-op and
     * return false rather than crash, so the alert-sending flow is never blocked.
     */
    fun silencePhone(context: Context): Boolean {
        if (!hasDndAccess(context)) return false
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Lets the user manually restore normal ringer volume from Settings once safe. */
    fun restoreNormalMode(context: Context): Boolean {
        if (!hasDndAccess(context)) return false
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            true
        } catch (e: Exception) {
            false
        }
    }
}
