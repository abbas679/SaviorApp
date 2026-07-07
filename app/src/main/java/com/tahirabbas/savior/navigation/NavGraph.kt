package com.tahirabbas.savior.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tahirabbas.savior.data.ContactRepository
import com.tahirabbas.savior.ui.screens.ContactSetupScreen
import com.tahirabbas.savior.ui.screens.HomeScreen
import com.tahirabbas.savior.ui.screens.SettingsScreen
import com.tahirabbas.savior.ui.screens.SituationPickerScreen

private object Routes {
    const val HOME = "home"
    const val SITUATION_PICKER = "situation_picker"
    const val CONTACT_SETUP = "contact_setup"
    const val SETTINGS = "settings"
}

@Composable
fun SaviorNavGraph(
    contactRepository: ContactRepository,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                contactRepository = contactRepository,
                onTriggerEmergency = { navController.navigate(Routes.SITUATION_PICKER) },
                onOpenContacts = { navController.navigate(Routes.CONTACT_SETUP) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.SITUATION_PICKER) {
            SituationPickerScreen(
                contactRepository = contactRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CONTACT_SETUP) {
            ContactSetupScreen(
                contactRepository = contactRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
