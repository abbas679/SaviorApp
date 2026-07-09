package com.tahirabbas.savior.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tahirabbas.savior.data.ContactRepository
import com.tahirabbas.savior.data.SavedPlaceRepository
import com.tahirabbas.savior.ui.theme.EmergencyRed
import com.tahirabbas.savior.ui.theme.EmergencyRedDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    contactRepository: ContactRepository,
    placeRepository: SavedPlaceRepository,
    onTriggerEmergency: () -> Unit,
    onOpenContacts: () -> Unit,
    onOpenPlaces: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val contactCount = contactRepository.getContacts().size
    val placeCount = placeRepository.getPlaces().size
    val ready = contactCount > 0

    // Subtle pulsing ring behind the button — draws the eye without being
    // distracting, and reinforces "this button is always live and ready".
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Savior", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = onOpenPlaces) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Saved Places")
                    }
                    IconButton(onClick = onOpenContacts) {
                        Icon(Icons.Default.ContactPhone, contentDescription = "Emergency Contacts")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFF5F5), Color(0xFFFFFFFF))
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Status chips: contacts + saved places at a glance
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusChip(
                    label = if (contactCount == 0) "No contacts yet" else "$contactCount contact${if (contactCount != 1) "s" else ""}",
                    isWarning = contactCount == 0
                )
                StatusChip(
                    label = if (placeCount == 0) "No saved places" else "$placeCount place${if (placeCount != 1) "s" else ""}",
                    isWarning = false
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(if (ready) pulseScale else 1f)
                    .background(
                        color = (if (ready) EmergencyRedDark else Color.Gray).copy(alpha = 0.25f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { if (ready) onTriggerEmergency() },
                    modifier = Modifier.size(190.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (ready) EmergencyRed else Color.LightGray
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    enabled = ready
                ) {
                    Text(
                        text = "I NEED\nHELP",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            if (!ready) {
                Text(
                    "Add at least one emergency contact to get started",
                    textAlign = TextAlign.Center,
                    color = EmergencyRed,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Text(
                text = "Tapping this will ask you to pick a situation, then instantly " +
                        "share your location and an alert message with your saved contacts " +
                        "— and silence your phone so a call-back stays private.",
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun StatusChip(label: String, isWarning: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (isWarning) Color(0xFFFFEBEE) else Color(0xFFF1F1F1),
        modifier = Modifier
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            fontSize = 12.sp,
            color = if (isWarning) EmergencyRed else Color.DarkGray,
            fontWeight = FontWeight.Medium
        )
    }
}
