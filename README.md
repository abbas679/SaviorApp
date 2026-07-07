# Savior — One-Tap Emergency Alert App

Savior is a personal safety app: in an emergency (theft, fire/explosion, accident, or
medical situation), a single flow lets the user alert their saved emergency contacts
with their live location and a situation-specific message, and points them to the
right emergency service to call.

This is a personal rebuild of an app concept I originally delivered for a private
client (Java/XML), reimplemented from scratch in Kotlin + Jetpack Compose as an
open-source portfolio project — no client code, data, or branding included.

## Features

- **One-tap emergency trigger** from the home screen
- **Situation picker** — Theft, Fire/Explosion, Accident, or Medical, each with its
  own alert wording and mapped emergency service number
- **Live location sharing** — fetches current location and includes a Google Maps
  link in the alert SMS
- **Up to 5 emergency contacts**, stored locally on-device
- **Direct dial shortcut** to the relevant emergency service after sending

## Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- Navigation Compose
- Fused Location Provider (Google Play Services)
- SharedPreferences for local contact storage (no backend — fully offline-capable)

## Project Structure

```
app/src/main/java/com/tahirabbas/savior/
├── MainActivity.kt
├── data/
│   ├── EmergencyContact.kt
│   ├── ContactRepository.kt
│   └── SituationType.kt
├── utils/
│   ├── LocationHelper.kt
│   └── SmsHelper.kt
├── navigation/
│   └── NavGraph.kt
└── ui/
    ├── theme/
    └── screens/
        ├── HomeScreen.kt
        ├── ContactSetupScreen.kt
        ├── SituationPickerScreen.kt
        └── SettingsScreen.kt
```

## Running it

1. Open the project root folder in Android Studio (Hedgehog or newer).
2. Let Gradle sync (it will pull the dependencies listed in `app/build.gradle.kts`).
3. Run on a device or emulator with Google Play Services (needed for location).
4. On first launch: add at least one emergency contact via the contacts icon,
   then use the home screen button to test the alert flow.

## Notes

- Emergency service numbers currently default to Pakistan (Police 15, Rescue/Ambulance
  1122, Fire 16) — see `SituationType.kt` to adjust for another country.
- SMS sending requires a physical device or an emulator with SMS capability;
  most emulators cannot actually send SMS, but the permission flow and message
  construction can still be tested.
