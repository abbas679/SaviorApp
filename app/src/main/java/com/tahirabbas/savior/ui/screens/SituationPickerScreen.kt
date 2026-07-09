package com.tahirabbas.savior.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tahirabbas.savior.data.ContactRepository
import com.tahirabbas.savior.data.SavedPlaceRepository
import com.tahirabbas.savior.data.SituationType
import com.tahirabbas.savior.utils.LocationHelper
import com.tahirabbas.savior.utils.SilentModeHelper
import com.tahirabbas.savior.utils.SmsHelper
import kotlinx.coroutines.launch

private sealed class SendState {
    object Idle : SendState()
    object Sending : SendState()
    data class Done(
        val situation: SituationType,
        val failedCount: Int,
        val totalCount: Int,
        val silenced: Boolean,
        val usedFallbackPlace: Boolean
    ) : SendState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SituationPickerScreen(
    contactRepository: ContactRepository,
    placeRepository: SavedPlaceRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sendState by remember { mutableStateOf<SendState>(SendState.Idle) }
    var pendingSituation by remember { mutableStateOf<SituationType?>(null) }
    var dndGranted by remember { mutableStateOf(SilentModeHelper.hasDndAccess(context)) }

    val dndSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        dndGranted = SilentModeHelper.hasDndAccess(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.all { it }
        val situation = pendingSituation
        if (granted && situation != null) {
            val silenced = SilentModeHelper.silencePhone(context)
            sendAlert(context, contactRepository, placeRepository, situation, scope) { failed, total, usedFallback ->
                sendState = SendState.Done(situation, failed, total, silenced, usedFallback)
            }
        } else {
            sendState = SendState.Idle
        }
    }

    fun triggerSituation(situation: SituationType) {
        pendingSituation = situation
        sendState = SendState.Sending
        // Silence immediately, before permissions even resolve — every second
        // counts if the phone is about to be watched by whoever caused the emergency.
        SilentModeHelper.silencePhone(context)
        permissionLauncher.launch(
            arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("What's happening?", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = sendState) {
            is SendState.Idle -> {
                Column(modifier = Modifier.padding(padding)) {
                    if (!dndGranted) {
                        Card(
                            onClick = { dndSettingsLauncher.launch(SilentModeHelper.requestDndAccessIntent()) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp, 16.dp, 16.dp, 0.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.VolumeOff, contentDescription = null, tint = Color(0xFFE65100))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Enable Silent Mode Protection", fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "Tap to allow Savior to mute your phone the instant you send an alert.",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(SituationType.values().toList()) { situation ->
                            SituationCard(situation = situation, onClick = { triggerSituation(situation) })
                        }
                    }
                }
            }

            is SendState.Sending -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Sending your alert…", fontWeight = FontWeight.Medium)
                }
            }

            is SendState.Done -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val sentCount = state.totalCount - state.failedCount
                    Text(
                        "Alert sent to $sentCount of ${state.totalCount} contacts",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    if (state.usedFallbackPlace) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Live GPS wasn't available — shared your saved address instead.",
                            color = Color(0xFF6A1B9A),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    if (state.failedCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${state.failedCount} message(s) failed — please call them directly.",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (state.silenced) Icons.Default.NotificationsOff else Icons.Default.VolumeOff,
                            contentDescription = null,
                            tint = if (state.silenced) Color(0xFF00796B) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (state.silenced) "Phone silenced" else "Silent mode not enabled",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (state.silenced) Color(0xFF00796B) else Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse("tel:${state.situation.serviceNumber}")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = state.situation.color)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Call ${state.situation.serviceName} (${state.situation.serviceNumber})")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
private fun SituationCard(situation: SituationType, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(situation.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(situation.icon, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(situation.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    "Alerts contacts + suggests calling ${situation.serviceName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Fetches the current location (best-effort). If GPS is unavailable, falls back
 * to the user's first saved place so the SMS still carries a usable location —
 * plain text works over SMS with zero internet required.
 */
private fun sendAlert(
    context: android.content.Context,
    contactRepository: ContactRepository,
    placeRepository: SavedPlaceRepository,
    situation: SituationType,
    scope: kotlinx.coroutines.CoroutineScope,
    onResult: (failedCount: Int, totalCount: Int, usedFallbackPlace: Boolean) -> Unit
) {
    scope.launch {
        val contacts = contactRepository.getContacts()
        val location = LocationHelper(context).getCurrentLocation()
        val fallbackPlace = placeRepository.getPlaces().firstOrNull()
        val usedFallback = location == null && fallbackPlace != null
        val message = situation.buildMessage(location?.latitude, location?.longitude, fallbackPlace)
        val failed = SmsHelper.sendEmergencyAlert(contacts, message)
        onResult(failed.size, contacts.size, usedFallback)
    }
}
