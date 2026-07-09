package com.tahirabbas.savior.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tahirabbas.savior.data.SavedPlace
import com.tahirabbas.savior.data.SavedPlaceRepository

private fun iconFor(label: String): ImageVector = when (label.lowercase()) {
    "home" -> Icons.Default.Home
    "office", "work" -> Icons.Default.Work
    "school" -> Icons.Default.School
    else -> Icons.Default.LocationOn
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPlacesScreen(
    placeRepository: SavedPlaceRepository,
    onBack: () -> Unit
) {
    var places by remember { mutableStateOf(placeRepository.getPlaces()) }
    var label by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Places") },
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
                "Tap the star to choose which single address is sent if live GPS " +
                        "fails — this is always explicit, never guessed.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                items(places) { place ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        colors = if (place.isDefault)
                            CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                        else
                            CardDefaults.cardColors()
                    ) {
                        ListItem(
                            leadingContent = {
                                Icon(iconFor(place.label), contentDescription = null)
                            },
                            headlineContent = { Text(place.label) },
                            supportingContent = {
                                Text(
                                    if (place.isDefault) "${place.address} · Default fallback" else place.address
                                )
                            },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { placeRepository.setDefaultPlace(place); places = placeRepository.getPlaces() }) {
                                        Icon(
                                            if (place.isDefault) Icons.Default.Star else Icons.Outlined.Star,
                                            contentDescription = "Set as default",
                                            tint = if (place.isDefault) Color(0xFFF9A825) else Color.Gray
                                        )
                                    }
                                    IconButton(onClick = {
                                        placeRepository.removePlace(place)
                                        places = placeRepository.getPlaces()
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                                    }
                                }
                            }
                        )
                    }
                }
            }

            if (places.size < SavedPlaceRepository.MAX_PLACES) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Quick add:", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Home", "Office", "School").forEach { preset ->
                        AssistChip(
                            onClick = { label = preset },
                            label = { Text(preset) },
                            leadingIcon = { Icon(iconFor(preset), contentDescription = null) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (e.g. Home, Office, School)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Full address") },
                    modifier = Modifier.fillMaxWidth()
                )
                errorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (label.isBlank() || address.isBlank()) {
                            errorText = "Both fields are required."
                        } else {
                            val added = placeRepository.addOrUpdatePlace(SavedPlace(label.trim(), address.trim()))
                            if (added) {
                                places = placeRepository.getPlaces()
                                label = ""
                                address = ""
                                errorText = null
                            } else {
                                errorText = "You've reached the ${SavedPlaceRepository.MAX_PLACES}-place limit."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Place")
                }
            }
        }
    }
}
