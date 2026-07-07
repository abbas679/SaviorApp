package com.tahirabbas.savior.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tahirabbas.savior.data.ContactRepository
import com.tahirabbas.savior.data.EmergencyContact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSetupScreen(
    contactRepository: ContactRepository,
    onBack: () -> Unit
) {
    var contacts by remember { mutableStateOf(contactRepository.getContacts()) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Contacts") },
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
                "Add up to ${ContactRepository.MAX_CONTACTS} people who should be alerted " +
                        "in an emergency (${contacts.size}/${ContactRepository.MAX_CONTACTS} added).",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(contacts) { contact ->
                    ListItem(
                        headlineContent = { Text(contact.name) },
                        supportingContent = { Text(contact.phoneNumber) },
                        trailingContent = {
                            IconButton(onClick = {
                                contactRepository.removeContact(contact)
                                contacts = contactRepository.getContacts()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove")
                            }
                        }
                    )
                    Divider()
                }
            }

            if (contacts.size < ContactRepository.MAX_CONTACTS) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone number (with country code)") },
                    modifier = Modifier.fillMaxWidth()
                )
                errorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (name.isBlank() || phone.isBlank()) {
                            errorText = "Both fields are required."
                        } else {
                            val added = contactRepository.addContact(EmergencyContact(name.trim(), phone.trim()))
                            if (added) {
                                contacts = contactRepository.getContacts()
                                name = ""
                                phone = ""
                                errorText = null
                            } else {
                                errorText = "You've reached the ${ContactRepository.MAX_CONTACTS}-contact limit."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Contact")
                }
            }
        }
    }
}
