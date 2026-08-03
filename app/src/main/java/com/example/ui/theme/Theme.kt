package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ArtisticAccent,
    secondary = ArtisticPrimaryContainer,
    tertiary = ArtisticTertiaryContainer,
    background = ArtisticCanvasBg,
    surface = ArtisticCanvasBg,
    onBackground = ArtisticBg,
    onSurface = ArtisticBg
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ArtisticPrimary,
    primaryContainer = ArtisticPrimaryContainer,
    onPrimaryContainer = ArtisticOnPrimaryContainer,
    secondary = ArtisticAccentDark,
    secondaryContainer = ArtisticSecondaryContainer,
    onSecondaryContainer = ArtisticOnSecondaryContainer,
    tertiary = ArtisticTertiaryContainer,
    onTertiaryContainer = ArtisticOnTertiaryContainer,
    background = ArtisticBg,
    surface = ArtisticBg,
    onBackground = ArtisticOnBg,
    onSurface = ArtisticOnBg
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Apply Artistic Flair Light studio theme by default
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

