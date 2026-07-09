package com.tahirabbas.savior.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, onOpenHiddenTrigger: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            Card(onClick = onOpenHiddenTrigger, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hidden Trigger (Volume Button)", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Send an alert silently by holding Volume Down",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("About Savior", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Savior sends a one-tap emergency alert with your live location to your " +
                        "saved contacts, and suggests the right emergency service to call based " +
                        "on the situation you select."
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text("Privacy", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Your contacts and saved places are stored only on this device. Location " +
                        "is only accessed at the moment you trigger an alert — it is not tracked " +
                        "in the background."
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Emergency service numbers shown are defaults for Pakistan. " +
                        "Editable per-country numbers can be added in a future update.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
