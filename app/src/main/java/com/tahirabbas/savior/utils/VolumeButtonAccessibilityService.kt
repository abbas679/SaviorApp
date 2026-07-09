package com.tahirabbas.savior.utils

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.tahirabbas.savior.data.ContactRepository
import com.tahirabbas.savior.data.SavedPlaceRepository
import com.tahirabbas.savior.data.SituationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Detects a long-press (3+ seconds) on the Volume Down button and silently
 * triggers an emergency alert — designed for situations where the phone is
 * hidden (e.g. in a pocket or behind the back) and can't safely be opened.
 *
 * This uses Android's Accessibility Service key-event filtering, which is
 * the same mechanism real safety apps (bSafe, Watch Over Me, etc.) use —
 * it's the only reliable way for a third-party app to react to a hardware
 * button, since Android does not expose raw power-button events to apps
 * at all (that's reserved for the OS's own Emergency SOS feature).
 *
 * The user must manually enable this once via Accessibility Settings —
 * see HiddenTriggerSetupScreen for the guided flow.
 */
class VolumeButtonAccessibilityService : AccessibilityService() {

    private var volumeDownPressStartMs = 0L
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val HOLD_THRESHOLD_MS = 3000L
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.flags = info.flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        serviceInfo = info
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> {
                    if (volumeDownPressStartMs == 0L) {
                        volumeDownPressStartMs = System.currentTimeMillis()
                    }
                }
                KeyEvent.ACTION_UP -> {
                    val heldMillis = System.currentTimeMillis() - volumeDownPressStartMs
                    volumeDownPressStartMs = 0L
                    if (heldMillis >= HOLD_THRESHOLD_MS) {
                        triggerSilentAlert()
                    }
                }
            }
        }
        // Always return false: never block normal volume behavior. The alert
        // fires as a *side effect* of the hold — the user's volume control
        // still works exactly as expected.
        return false
    }

    private fun triggerSilentAlert() {
        serviceScope.launch {
            SilentModeHelper.silencePhone(applicationContext)

            val contactRepository = ContactRepository(applicationContext)
            val placeRepository = SavedPlaceRepository(applicationContext)
            val contacts = contactRepository.getContacts()
            if (contacts.isEmpty()) return@launch

            val location = LocationHelper(applicationContext).getCurrentLocation()
            val fallbackPlace = placeRepository.getDefaultPlace()
            val message = SituationType.OTHER.buildMessage(
                location?.latitude, location?.longitude, fallbackPlace
            )
            SmsHelper.sendEmergencyAlert(contacts, message)

            // A single short vibration is the only feedback — audible confirmation
            // would defeat the entire purpose of a hidden trigger.
            vibrateConfirmation()
        }
    }

    private fun vibrateConfirmation() {
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (e: Exception) {
            // Non-critical — never let a vibration failure affect the alert itself.
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not needed — we only care about key events, not accessibility events.
    }

    override fun onInterrupt() {
        // Required override; nothing to clean up here.
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
