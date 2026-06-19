package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberIndigo,
    secondary = CyberCyan,
    tertiary = CyberRose,
    background = CosmicDarkBg,
    surface = CosmicDarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    error = CyberRed
)

private val LightColorScheme = darkColorScheme( // Fallback/default is also dark-glassmorphic as requested!
    primary = CyberIndigo,
    secondary = CyberCyan,
    tertiary = CyberRose,
    background = CosmicDarkBg,
    surface = CosmicDarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    error = CyberRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force premium dark mode-first
    dynamicColor: Boolean = false, // Disable device dynamic color engine to maintain cinematic brand guidelines
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
