package com.tahirabbas.savior.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tahirabbas.savior.data.ContactRepository
import com.tahirabbas.savior.ui.theme.EmergencyRed
import com.tahirabbas.savior.ui.theme.EmergencyRedDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    contactRepository: ContactRepository,
    onTriggerEmergency: () -> Unit,
    onOpenContacts: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val contactCount = contactRepository.getContacts().size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Savior", fontWeight = FontWeight.Bold) },
                actions = {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (contactCount == 0)
                    "Add at least one emergency contact to get started"
                else
                    "$contactCount emergency contact${if (contactCount > 1) "s" else ""} configured",
                textAlign = TextAlign.Center,
                color = if (contactCount == 0) EmergencyRed else Color.Gray,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // The core interaction: one tap opens the situation picker, which then
            // sends the alert — kept as two steps so a pocket-press can't fire it by accident.
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(
                        color = if (contactCount == 0) Color.Gray else EmergencyRedDark,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { if (contactCount > 0) onTriggerEmergency() },
                    modifier = Modifier.size(190.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (contactCount == 0) Color.LightGray else EmergencyRed
                    ),
                    enabled = contactCount > 0
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

            Text(
                text = "Tapping this will ask you to pick a situation, then instantly " +
                        "share your live location and an alert message with your saved contacts.",
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}
