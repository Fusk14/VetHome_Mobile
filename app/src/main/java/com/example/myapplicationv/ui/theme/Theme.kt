package com.example.myapplicationv.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de colores para VetHome
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50),           // Verde naturaleza
    secondary = Color(0xFF2196F3),         // Azul confianza
    tertiary = Color(0xFFFF9800),          // Naranja cálido
    background = Color(0xFFF5F5F5),        // Gris muy claro
    surface = Color(0xFFFFFFFF),           // Blanco
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF333333),
    onSurface = Color(0xFF333333),
)

@Composable
fun VetHomeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme  // Por ahora solo usamos modo claro

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}