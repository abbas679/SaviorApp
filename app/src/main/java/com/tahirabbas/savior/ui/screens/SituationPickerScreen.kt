package com.tahirabbas.savior.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tahirabbas.savior.data.ContactRepository
import com.tahirabbas.savior.data.SituationType
import com.tahirabbas.savior.utils.LocationHelper
import com.tahirabbas.savior.utils.SmsHelper
import kotlinx.coroutines.launch

private sealed class SendState {
    object Idle : SendState()
    object Sending : SendState()
    data class Done(val situation: SituationType, val failedCount: Int, val totalCount: Int) : SendState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SituationPickerScreen(
    contactRepository: ContactRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sendState by remember { mutableStateOf<SendState>(SendState.Idle) }
    var pendingSituation by remember { mutableStateOf<SituationType?>(null) }

    // Requests SEND_SMS + location permissions together; only proceeds to send once granted.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.all { it }
        val situation = pendingSituation
        if (granted && situation != null) {
            sendAlert(context, contactRepository, situation, scope) { failed, total ->
                sendState = SendState.Done(situation, failed, total)
            }
        } else {
            sendState = SendState.Idle
        }
    }

    fun triggerSituation(situation: SituationType) {
        pendingSituation = situation
        sendState = SendState.Sending
        permissionLauncher.launch(
            arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("What's happening?") },
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
                LazyColumn(
                    modifier = Modifier.padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(SituationType.values().toList()) { situation ->
                        Card(onClick = { triggerSituation(situation) }) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(situation.label, style = MaterialTheme.typography.titleLarge)
                                Text(
                                    "Alerts contacts + suggests calling ${situation.serviceName}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
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
                    Text("Sending your alert…")
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
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (state.failedCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${state.failedCount} message(s) failed — please call them directly.",
                            color = MaterialTheme.colorScheme.error
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
                        modifier = Modifier.fillMaxWidth()
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

/**
 * Fetches the current location (best-effort) and sends the situational alert
 * SMS to every saved contact. Reports back how many sends failed.
 */
private fun sendAlert(
    context: android.content.Context,
    contactRepository: ContactRepository,
    situation: SituationType,
    scope: kotlinx.coroutines.CoroutineScope,
    onResult: (failedCount: Int, totalCount: Int) -> Unit
) {
    scope.launch {
        val contacts = contactRepository.getContacts()
        val location = LocationHelper(context).getCurrentLocation()
        val message = situation.buildMessage(location?.latitude, location?.longitude)
        val failed = SmsHelper.sendEmergencyAlert(contacts, message)
        onResult(failed.size, contacts.size)
    }
}
