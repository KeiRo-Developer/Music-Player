package com.keiro.musicplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = AccentViolet,
    secondary = AccentCyan,
    background = DeepBlack,
    surface = SurfaceDark,
    surfaceVariant = SurfaceElevated,
    onBackground = Color(0xFFF2F1F7),
    onSurface = Color(0xFFF2F1F7),
    onSurfaceVariant = OnSurfaceMuted
)

@Composable
fun MusicPlayerTheme(content: @Composable () -> Unit) {
    // Always dark — a music player's now-playing screen looks best against
    // black regardless of system theme, so we don't branch on isSystemInDarkTheme.
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = MusicTypography,
        content = content
    )
}
