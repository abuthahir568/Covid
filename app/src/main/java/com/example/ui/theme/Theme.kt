package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = CrawlerYellow,
  onPrimary = Color.Black,
  secondary = CardBorderPurple,
  onSecondary = Color.White,
  tertiary = PlayGreenLight,
  background = DeepStarryNight,
  onBackground = Color.White,
  surface = CardBgDark,
  onSurface = Color.White
)

private val LightColorScheme = darkColorScheme(
  primary = CrawlerYellow,
  onPrimary = Color.Black,
  secondary = CardBorderPurple,
  onSecondary = Color.White,
  tertiary = PlayGreenLight,
  background = DeepStarryNight,
  onBackground = Color.White,
  surface = CardBgDark,
  onSurface = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep game aesthetics consistent
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
