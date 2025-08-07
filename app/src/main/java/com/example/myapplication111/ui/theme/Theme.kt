package com.example.myapplication111.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = EpnBlue,
    secondary = EpnRed,
    background = LightSilver,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onBackground = DarkGray,
    onSurface = DarkGray
)

private val LightColorScheme = lightColorScheme(
    primary = EpnBlue,
    secondary = White,
    background = LightSilver,
    surface = EpnRed,
    onPrimary = White,
    onSecondary = Black,
    onBackground = LightSilver,
    onSurface = White
)
@Composable
fun MyApplication111Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
