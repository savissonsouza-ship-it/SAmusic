package com.samusic.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Original SAmusic identity — warm violet/amber, deliberately NOT Spotify green.
val SAmusicViolet = Color(0xFF8B5CF6)
val SAmusicAmber = Color(0xFFF59E0B)
val SAmusicInk = Color(0xFF0E0B14)
val SAmusicSurface = Color(0xFF1A1522)
val SAmusicSurfaceVariant = Color(0xFF261F33)

private val DarkColors = darkColorScheme(
    primary = SAmusicViolet,
    onPrimary = Color.White,
    secondary = SAmusicAmber,
    background = SAmusicInk,
    onBackground = Color(0xFFEDE9F4),
    surface = SAmusicSurface,
    onSurface = Color(0xFFEDE9F4),
    surfaceVariant = SAmusicSurfaceVariant,
    onSurfaceVariant = Color(0xFFB9AFCB),
)

private val LightColors = lightColorScheme(
    primary = SAmusicViolet,
    secondary = SAmusicAmber,
)

@Composable
fun SAmusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
