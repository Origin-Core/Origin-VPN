package com.originvpn.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NeonBlue = Color(0xFF3DDC97)
val DeepSpace = Color(0xFF0B0F1A)
val CardDark = Color(0xFF141A2A)
val AccentPurple = Color(0xFF6C5CE7)
val DangerRed = Color(0xFFFF5C5C)

private val OriginColorScheme = darkColorScheme(
    primary = NeonBlue,
    secondary = AccentPurple,
    background = DeepSpace,
    surface = CardDark,
    error = DangerRed
)

@Composable
fun OriginVpnTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OriginColorScheme,
        content = content
    )
}
