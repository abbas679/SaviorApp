package com.tahirabbas.savior.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tahirabbas.savior.data.ContactRepository
import com.tahirabbas.savior.data.SavedPlaceRepository
import com.tahirabbas.savior.ui.screens.ContactSetupScreen
import com.tahirabbas.savior.ui.screens.HiddenTriggerSetupScreen
import com.tahirabbas.savior.ui.screens.HomeScreen
import com.tahirabbas.savior.ui.screens.SavedPlacesScreen
import com.tahirabbas.savior.ui.screens.SettingsScreen
import com.tahirabbas.savior.ui.screens.SituationPickerScreen

private object Routes {
    const val HOME = "home"
    const val SITUATION_PICKER = "situation_picker"
    const val CONTACT_SETUP = "contact_setup"
    const val SAVED_PLACES = "saved_places"
    const val SETTINGS = "settings"
    const val HIDDEN_TRIGGER_SETUP = "hidden_trigger_setup"
}

@Composable
fun SaviorNavGraph(
    contactRepository: ContactRepository,
    placeRepository: SavedPlaceRepository,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                contactRepository = contactRepository,
                placeRepository = placeRepository,
                onTriggerEmergency = { navController.navigate(Routes.SITUATION_PICKER) },
                onOpenContacts = { navController.navigate(Routes.CONTACT_SETUP) },
                onOpenPlaces = { navController.navigate(Routes.SAVED_PLACES) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.SITUATION_PICKER) {
            SituationPickerScreen(
                contactRepository = contactRepository,
                placeRepository = placeRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CONTACT_SETUP) {
            ContactSetupScreen(
                contactRepository = contactRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SAVED_PLACES) {
            SavedPlacesScreen(
                placeRepository = placeRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenHiddenTrigger = { navController.navigate(Routes.HIDDEN_TRIGGER_SETUP) }
            )
        }

        composable(Routes.HIDDEN_TRIGGER_SETUP) {
            HiddenTriggerSetupScreen(onBack = { navController.popBackStack() })
        }
    }
}
