package com.tahirabbas.savior.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SaviorColorScheme = lightColorScheme(
    primary = EmergencyRed,
    onPrimary = SurfaceLight,
    secondary = AlertOrange,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun SaviorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SaviorColorScheme,
        typography = SaviorTypography,
        content = content
    )
}
