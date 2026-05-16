package com.example.devarakadu.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightColorPalette = lightColors(
    primary = LeafGreen,
    primaryVariant = DarkGreen,
    secondary = PurpleGrey80
)

@Composable
fun DevaraKaduTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = LightColorPalette,
        typography = Typography, // This connects to the Type.kt file
        content = content
    )
}