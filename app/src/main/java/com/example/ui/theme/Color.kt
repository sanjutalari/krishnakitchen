package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

// Dynamic global state for dark mode
var isThemeDarkGlobal by mutableStateOf(false)

// Brand Colors (Apple Liquid Glass Design System)
// Liquid Crimson/Flame Red (Apple Signature iOS Coral/Crimson Red)
val FlameRed: Color get() = if (isThemeDarkGlobal) Color(0xFFFF453A) else Color(0xFFFF3B30)

// Liquid Amber Glow (Apple Warm Yellow/Amber)
val AmberGlow: Color get() = if (isThemeDarkGlobal) Color(0xFFFF9F0A) else Color(0xFFFF9500)

// Charcoal Black (Apple Space Gray & Off-White Text contrast)
val CharcoalBlack: Color get() = if (isThemeDarkGlobal) Color(0xFFF2F2F7) else Color(0xFF1C1C1E)

// Base desktop canvas background (resembles macOS high-end frosted background)
val SurfaceDark: Color get() = if (isThemeDarkGlobal) Color(0xFF0A0A0C) else Color(0xFFEAF0F6)

// Frosted glass panel backdrop (high opacity for solid premium layering)
val SurfaceElevated: Color get() = if (isThemeDarkGlobal) Color(0xD91E1E24) else Color(0xE6FFFFFF)

// Frosted glass card body (translucent with beautiful lighting)
val SurfaceContainerLow: Color get() = if (isThemeDarkGlobal) Color(0x992C2C35) else Color(0xBFFFFFFF)

// Translucent border line to simulate glass thickness and light refraction
val SurfaceContainer: Color get() = if (isThemeDarkGlobal) Color(0x3DFFFFFF) else Color(0x24000000)

// Bright reflection edge highlight (gives the "liquid glass" edge shine)
val SurfaceContainerHigh: Color get() = if (isThemeDarkGlobal) Color(0x66FFFFFF) else Color(0x3B000000)

// Stronger accent border/seperator for layout structure
val SurfaceContainerHighest: Color get() = if (isThemeDarkGlobal) Color(0x8CFFFFFF) else Color(0x5C000000)

// Typography & Text (Apple sf-pro style contrast)
val OnSurfaceLight: Color get() = if (isThemeDarkGlobal) Color(0xFFFFFFFF) else Color(0xFF1C1C1E)
val OnSurfaceVariantLight: Color get() = if (isThemeDarkGlobal) Color(0xFFAEB2B6) else Color(0xFF636366)

// Semantic State Colors (Apple system hues)
val StatusPending: Color get() = if (isThemeDarkGlobal) Color(0xFFBF5AF2) else Color(0xFFAF52DE) // Purple for pending
val StatusCooking: Color get() = if (isThemeDarkGlobal) Color(0xFFFF9F0A) else Color(0xFFFF9500) // Orange/Amber
val StatusReady: Color get() = if (isThemeDarkGlobal) Color(0xFF30D158) else Color(0xFF34C759)   // Green
val StatusDelivered: Color get() = if (isThemeDarkGlobal) Color(0xFF64D2FF) else Color(0xFF007AFF) // Sky blue / Blue
val StatusCritical: Color get() = if (isThemeDarkGlobal) Color(0xFFFF453A) else Color(0xFFFF3B30) // Red
val ErrorColor: Color get() = if (isThemeDarkGlobal) Color(0x3DFF453A) else Color(0xFFFDF2F2)
val OnErrorColor: Color get() = if (isThemeDarkGlobal) Color(0xFFFF9F0A) else Color(0xFFD70015)

/**
 * Extension modifier to instantly apply the "Apple Liquid Glass" look:
 * - Rounded corners (default 16dp)
 * - Frosted/translucent gradient background
 * - Bright refraction hairline border
 * - Soft layered drop-shadow
 */
fun Modifier.appleLiquidGlass(
    cornerRadius: Int = 16,
    borderAlpha: Float = if (isThemeDarkGlobal) 0.25f else 0.15f
): Modifier {
    val shape = RoundedCornerShape(cornerRadius.dp)
    val backgroundBrush = if (isThemeDarkGlobal) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xCC25252E),
                Color(0x991E1E24)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xE6FFFFFF),
                Color(0xB3F2F6FA)
            )
        )
    }

    val borderColor = if (isThemeDarkGlobal) {
        Color.White.copy(alpha = borderAlpha)
    } else {
        Color.Black.copy(alpha = borderAlpha)
    }

    return this
        .shadow(
            elevation = 6.dp,
            shape = shape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.05f),
            spotColor = Color.Black.copy(alpha = 0.1f)
        )
        .clip(shape)
        .background(backgroundBrush)
        .border(1.dp, borderColor, shape)
}
