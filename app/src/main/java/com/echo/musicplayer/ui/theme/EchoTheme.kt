package com.echo.musicplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EchoBackground = Color(0xFF101418)
private val EchoSurface = Color(0xFF171C22)
private val EchoSurfaceHigh = Color(0xFF202630)
private val EchoText = Color(0xFFF5F7FB)
private val EchoMuted = Color(0xFFA8B0BC)

@Composable
fun EchoTheme(
    primaryColorArgb: Long,
    content: @Composable () -> Unit,
) {
    val primary = Color(primaryColorArgb)
    MaterialTheme(
        colorScheme = echoDarkColorScheme(primary),
        typography = MaterialTheme.typography,
        content = content,
    )
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun echoDarkColorScheme(primary: Color): ColorScheme {
    isSystemInDarkTheme()
    return darkColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = primary.copy(alpha = 0.24f),
        onPrimaryContainer = EchoText,
        background = EchoBackground,
        onBackground = EchoText,
        surface = EchoSurface,
        onSurface = EchoText,
        surfaceVariant = EchoSurfaceHigh,
        onSurfaceVariant = EchoMuted,
        secondary = Color(0xFF69D2C8),
        onSecondary = Color(0xFF071A19),
        tertiary = Color(0xFFE8C766),
        onTertiary = Color(0xFF201800),
        error = Color(0xFFFF6B7A),
        onError = Color.White,
        outline = Color(0xFF3A424F),
    )
}
