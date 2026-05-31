package com.echo.musicplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.echo.musicplayer.domain.model.ThemeMode

private val EchoBackground = Color(0xFF101418)
private val EchoSurface = Color(0xFF171C22)
private val EchoSurfaceHigh = Color(0xFF202630)
private val EchoText = Color(0xFFF5F7FB)
private val EchoMuted = Color(0xFFA8B0BC)
private val EchoLightBackground = Color(0xFFF8FAFC)
private val EchoLightSurface = Color.White
private val EchoLightSurfaceHigh = Color(0xFFE8EEF5)
private val EchoLightText = Color(0xFF111827)
private val EchoLightMuted = Color(0xFF5F6977)

@Composable
fun EchoTheme(
    primaryColorArgb: Long,
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val primary = Color(primaryColorArgb.toInt())
    val useDark = when (themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (useDark) echoDarkColorScheme(primary) else echoLightColorScheme(primary),
        typography = MaterialTheme.typography,
        content = content,
    )
}

private fun echoDarkColorScheme(primary: Color): ColorScheme {
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

private fun echoLightColorScheme(primary: Color): ColorScheme {
    return lightColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = primary.copy(alpha = 0.16f),
        onPrimaryContainer = EchoLightText,
        background = EchoLightBackground,
        onBackground = EchoLightText,
        surface = EchoLightSurface,
        onSurface = EchoLightText,
        surfaceVariant = EchoLightSurfaceHigh,
        onSurfaceVariant = EchoLightMuted,
        secondary = Color(0xFF00897B),
        onSecondary = Color.White,
        tertiary = Color(0xFF9A6A00),
        onTertiary = Color.White,
        error = Color(0xFFBA1A1A),
        onError = Color.White,
        outline = Color(0xFFCAD2DC),
    )
}
