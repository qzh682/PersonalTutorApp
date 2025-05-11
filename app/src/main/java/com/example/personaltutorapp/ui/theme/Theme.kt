// File: ui/theme/Theme.kt
package com.example.personaltutorapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Dark color scheme for the application, used when dark theme is enabled.
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Black,
    surface = NeutralLight, // Shallow gray for cards in dark theme
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    surfaceVariant = NeutralLight,
    onSurfaceVariant = White
)

// Light color scheme for the application, used when light theme is enabled.
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = White, // White background for entire app
    surface = LightGreen, // Shallow green for cards and text boxes
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = DarkGray, // Dark gray for text contrast
    onSurface = DarkGray, // Dark gray for text contrast
    surfaceVariant = LightGreen, // Shallow green for variant surfaces
    onSurfaceVariant = DarkGray // Dark gray for variant text contrast
)

// Composable function to apply the application's Material Design theme.
// Supports dynamic colors on Android 12+ and falls back to static color schemes otherwise.
@Composable
fun PersonalTutorAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Enable dynamic colors for Android 12+ (Material You) when true.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Defined in Type.kt
        shapes = Shapes, // Defined in Shapes.kt
        content = content
    )
}