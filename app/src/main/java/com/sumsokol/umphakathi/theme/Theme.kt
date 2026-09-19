package com.sumsokol.umphakathi.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = CrisisRed,
    onPrimary = Color.White,
    primaryContainer = CrisisRedLight,
    secondary = AlertAmber,
    secondaryContainer = AlertAmberLight,
    tertiary = SafeTeal,
    tertiaryContainer = SafeTealLight,
    surface = SurfaceLight,
    background = SurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = CrisisRedLight,
    onPrimary = Color.Black,
    primaryContainer = CrisisRedDark,
    secondary = AlertAmberLight,
    secondaryContainer = AlertAmberDark,
    tertiary = SafeTealLight,
    tertiaryContainer = SafeTealDark,
    surface = SurfaceDark,
    background = SurfaceDark
)

@Composable
fun UmphakathiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
        typography = Typography,
        content = content
    )
}
