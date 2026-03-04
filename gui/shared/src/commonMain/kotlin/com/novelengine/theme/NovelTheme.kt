package com.novelengine.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Novel Engine color palette.
 *
 * Dark, atmospheric colors inspired by psychological horror themes.
 */
object NovelColors {
    // Primary colors
    val DarkPurple = Color(0xFF1A0A2E)
    val DeepPurple = Color(0xFF2D1B4E)
    val MidPurple = Color(0xFF4A1942)
    val AccentPink = Color(0xFFFF6B6B)
    val AccentCyan = Color(0xFF4ECDC4)
    
    // Background colors
    val Background = Color(0xFF0D0D0D)
    val Surface = Color(0xFF1A1A2E)
    val SurfaceVariant = Color(0xFF252540)
    
    // Text colors
    val TextPrimary = Color(0xFFE0E0E0)
    val TextSecondary = Color(0xFFA0A0A0)
    val TextMuted = Color(0xFF606060)
    
    // State colors
    val Error = Color(0xFFCF6679)
    val Warning = Color(0xFFFFB74D)
    val Success = Color(0xFF81C784)
    
    // Effect colors
    val GlitchRed = Color(0xFFFF0040)
    val GlitchBlue = Color(0xFF0080FF)
    val StaticWhite = Color(0xFFFFFFFF)
    val StaticBlack = Color(0xFF000000)
    
    // Sanity indicator colors
    val SanityHigh = Color(0xFF4ECDC4)    // 70-100%
    val SanityMedium = Color(0xFFFFB74D)  // 30-70%
    val SanityLow = Color(0xFFFF6B6B)     // 0-30%
    
    fun sanityColor(coins: Int, maxCoins: Int = 100): Color {
        val percentage = (coins.toFloat() / maxCoins).coerceIn(0f, 1f)
        return when {
            percentage >= 0.7f -> SanityHigh
            percentage >= 0.3f -> SanityMedium
            else -> SanityLow
        }
    }
}

/**
 * Material 3 color scheme for Novel Engine (dark theme).
 */
private val NovelDarkColorScheme = darkColorScheme(
    primary = NovelColors.AccentPink,
    onPrimary = NovelColors.Background,
    primaryContainer = NovelColors.MidPurple,
    onPrimaryContainer = NovelColors.TextPrimary,
    
    secondary = NovelColors.AccentCyan,
    onSecondary = NovelColors.Background,
    secondaryContainer = NovelColors.DeepPurple,
    onSecondaryContainer = NovelColors.TextPrimary,
    
    tertiary = NovelColors.AccentCyan,
    onTertiary = NovelColors.Background,
    
    background = NovelColors.Background,
    onBackground = NovelColors.TextPrimary,
    
    surface = NovelColors.Surface,
    onSurface = NovelColors.TextPrimary,
    surfaceVariant = NovelColors.SurfaceVariant,
    onSurfaceVariant = NovelColors.TextSecondary,
    
    error = NovelColors.Error,
    onError = NovelColors.Background,
    
    outline = NovelColors.MidPurple,
    outlineVariant = NovelColors.DeepPurple,
)

/**
 * Typography for Novel Engine.
 *
 * Uses a mix of monospace (for horror effect) and clean sans-serif.
 */
val NovelTypography = Typography(
    // Used for scene text
    bodyLarge = Typography().bodyLarge.copy(
        letterSpacing = Typography().bodyLarge.letterSpacing * 1.05f,
        lineHeight = Typography().bodyLarge.lineHeight * 1.3f
    ),
    
    // Used for choices
    bodyMedium = Typography().bodyMedium.copy(
        letterSpacing = Typography().bodyMedium.letterSpacing * 1.02f
    ),
    
    // Used for status bar
    labelSmall = Typography().labelSmall.copy(
        letterSpacing = Typography().labelSmall.letterSpacing * 1.1f
    )
)

/**
 * Novel Engine theme composable.
 *
 * Always uses dark theme for horror atmosphere.
 */
@Composable
fun NovelEngineTheme(
    darkTheme: Boolean = true, // Always dark for horror aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NovelDarkColorScheme,
        typography = NovelTypography,
        content = content
    )
}
