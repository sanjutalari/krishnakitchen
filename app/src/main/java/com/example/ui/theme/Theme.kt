package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightBrandColorScheme = lightColorScheme(
    primary = FlameRed,
    onPrimary = Color.White,
    primaryContainer = AmberGlow,
    onPrimaryContainer = CharcoalBlack,
    secondary = AmberGlow,
    onSecondary = CharcoalBlack,
    secondaryContainer = SurfaceContainer,
    onSecondaryContainer = OnSurfaceLight,
    background = SurfaceDark,
    onBackground = OnSurfaceLight,
    surface = SurfaceContainerLow, // White background for cards
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceElevated, // Cream background for containers
    onSurfaceVariant = OnSurfaceVariantLight,
    error = ErrorColor,
    onError = OnErrorColor
)

private val DarkBrandColorScheme = darkColorScheme(
    primary = FlameRed,
    onPrimary = CharcoalBlack,
    primaryContainer = AmberGlow,
    onPrimaryContainer = OnSurfaceLight,
    secondary = AmberGlow,
    onSecondary = CharcoalBlack,
    secondaryContainer = SurfaceContainer,
    onSecondaryContainer = OnSurfaceLight,
    background = SurfaceDark,
    onBackground = OnSurfaceLight,
    surface = SurfaceContainerLow,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = ErrorColor,
    onError = OnErrorColor
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isThemeDarkGlobal,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkBrandColorScheme else LightBrandColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
