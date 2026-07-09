package com.tahirabbas.savior.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.tahirabbas.savior.utils.VolumeButtonAccessibilityService

private fun isAccessibilityServiceEnabled(context: android.content.Context): Boolean {
    val expectedComponent = "${context.packageName}/${VolumeButtonAccessibilityService::class.java.name}"
    val enabled = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: ""
    return enabled.split(":").any { it.equals(expectedComponent, ignoreCase = true) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenTriggerSetupScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var smsGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)
    }
    var locationGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    var accessibilityEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        smsGranted = results[Manifest.permission.SEND_SMS] ?: smsGranted
        locationGranted = results[Manifest.permission.ACCESS_FINE_LOCATION] ?: locationGranted
    }

    val accessibilitySettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        accessibilityEnabled = isAccessibilityServiceEnabled(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hidden Trigger Setup") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            Text(
                "Hold Volume Down for 3 seconds — even with the screen off or the " +
                        "phone hidden in your hand or pocket — to silently send an alert " +
                        "to your emergency contacts.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            SetupStep(
                title = "1. SMS permission",
                done = smsGranted,
                description = if (smsGranted) "Granted" else "Required to send the alert message",
                actionLabel = if (smsGranted) null else "Grant",
                onAction = {
                    permissionLauncher.launch(arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION))
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SetupStep(
                title = "2. Location permission",
                done = locationGranted,
                description = if (locationGranted) "Granted" else "Required to include your location in the alert",
                actionLabel = if (locationGranted) null else "Grant",
                onAction = {
                    permissionLauncher.launch(arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION))
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SetupStep(
                title = "3. Accessibility Service",
                done = accessibilityEnabled,
                description = if (accessibilityEnabled)
                    "Enabled — Savior can detect the volume-button hold"
                else
                    "Required for Savior to detect the button hold. This opens Android's Accessibility settings — find \"Savior\" in the list and turn it on.",
                actionLabel = if (accessibilityEnabled) null else "Open Settings",
                onAction = {
                    accessibilitySettingsLauncher.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (smsGranted && locationGranted && accessibilityEnabled) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Hidden trigger is fully set up. Hold Volume Down for 3 seconds any time to test it.",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Complete all 3 steps above for the hidden trigger to work.")
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupStep(
    title: String,
    done: Boolean,
    description: String,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Card {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (done) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (done) Color(0xFF2E7D32) else Color(0xFFE65100)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
            if (actionLabel != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}
