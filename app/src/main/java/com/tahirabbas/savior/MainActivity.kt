package com.tahirabbas.savior

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tahirabbas.savior.data.ContactRepository
import com.tahirabbas.savior.navigation.SaviorNavGraph
import com.tahirabbas.savior.ui.theme.SaviorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contactRepository = ContactRepository(applicationContext)

        setContent {
            SaviorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SaviorNavGraph(contactRepository = contactRepository)
                }
            }
        }
    }
}
