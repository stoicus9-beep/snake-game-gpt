package com.stoicus9.snakeclassic.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SnakeColorScheme = darkColorScheme(
    primary = ArcadeGreen,
    onPrimary = Color.Black,
    secondary = ArcadeGreenDark,
    onSecondary = SoftWhite,
    background = CharcoalBlack,
    onBackground = SoftWhite,
    surface = PanelGray,
    onSurface = SoftWhite
)

@Composable
fun SnakeClassicTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SnakeColorScheme,
        content = content
    )
}
