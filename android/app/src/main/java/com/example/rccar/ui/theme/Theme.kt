
package com.example.rccar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00BCD4),
    primaryContainer = Color(0xFF004D5C),
    secondary = Color(0xFF03DAC6),
    secondaryContainer = Color(0xFF005047),
    tertiary = Color(0xFF03DAC6),
    tertiaryContainer = Color(0xFF005047),
    error = Color(0xFFCF6679),
    background = Color(0xFF1A1A1A),
    surface = Color(0xFF2A2A2A),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFF2A2A2A),
    secondary = Color(0xFF00BCD4),
    secondaryContainer = Color(0xFF004D5C),
    tertiary = Color(0xFF00BCD4),
    tertiaryContainer = Color(0xFF004D5C),
    error = Color(0xFFB00020),
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    onSurface = Color(0xFF000000),
)

@Composable
fun RCCarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
