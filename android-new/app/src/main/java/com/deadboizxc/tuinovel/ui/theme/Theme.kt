package com.deadboizxc.tuinovel.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * =============================================================================
 * TUI Novel - Тема Compose
 * Хакерский терминал в стиле Matrix / Lain / DDLC
 * =============================================================================
 */

/**
 * Цветовая схема терминала
 * Полностью тёмная с зелёными акцентами
 */
private val TerminalColorScheme = darkColorScheme(
    // Основные цвета
    primary = TerminalGreen,
    onPrimary = BackgroundBlack,
    primaryContainer = TerminalGreenDim,
    onPrimaryContainer = TerminalGreenBright,
    
    // Вторичные цвета
    secondary = TerminalGreenDim,
    onSecondary = BackgroundBlack,
    secondaryContainer = BackgroundDark,
    onSecondaryContainer = TerminalGreen,
    
    // Третичные (для акцентов и эффектов)
    tertiary = PsychoCyan,
    onTertiary = BackgroundBlack,
    tertiaryContainer = PsychoPurple,
    onTertiaryContainer = PsychoMagenta,
    
    // Фоны
    background = BackgroundBlack,
    onBackground = TerminalGreen,
    surface = BackgroundBlack,
    onSurface = TerminalGreen,
    surfaceVariant = BackgroundDark,
    onSurfaceVariant = TerminalGreenDim,
    
    // Ошибки
    error = ErrorRed,
    onError = BackgroundBlack,
    errorContainer = PsychoDarkRed,
    onErrorContainer = ErrorRed,
    
    // Контуры
    outline = TerminalGreenDim,
    outlineVariant = BackgroundDark,
    
    // Скрим и инверсия
    scrim = BackgroundBlack,
    inverseSurface = TerminalGreen,
    inverseOnSurface = BackgroundBlack,
    inversePrimary = BackgroundBlack
)

/**
 * Главная тема приложения TUI Novel
 * 
 * @param darkTheme Всегда true для терминального стиля
 * @param content Содержимое с применённой темой
 */
@Composable
fun TuiNovelTheme(
    darkTheme: Boolean = true, // Терминал всегда тёмный
    content: @Composable () -> Unit
) {
    val colorScheme = TerminalColorScheme
    
    // Настройка системных баров
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Чёрный статус-бар
            window.statusBarColor = Color.Black.toArgb()
            // Светлые иконки на тёмном фоне
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
