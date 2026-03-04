package com.deadboizxc.tuinovel.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * =============================================================================
 * Novel Engine - Цветовая палитра
 * Элегантный фиолетовый стиль
 * =============================================================================
 */

// ============================================================================
// Основные фиолетовые цвета
// ============================================================================

/** Основной фиолетовый */
val PrimaryPurple = Color(0xFF7B2CBF)

/** Светлый фиолетовый */
val PrimaryPurpleLight = Color(0xFF9D4EDD)

/** Тёмный фиолетовый */
val PrimaryPurpleDark = Color(0xFF5A189A)

/** Акцентный фиолетовый */
val AccentPurple = Color(0xFFE0AAFF)

/** Глубокий фиолетовый */
val DeepPurple = Color(0xFF3C096C)

// ============================================================================
// Совместимость со старыми именами
// ============================================================================

val TerminalGreen = PrimaryPurple
val TerminalGreenDim = PrimaryPurpleDark
val TerminalGreenBright = PrimaryPurpleLight
val TerminalGreenPhosphor = AccentPurple

// ============================================================================
// Фоны
// ============================================================================

/** Тёмный фиолетовый фон */
val BackgroundBlack = Color(0xFF10002B)

/** Чуть светлее для контраста */
val BackgroundDark = Color(0xFF1A0033)

/** Фон для ввода */
val InputBackground = Color(0xFF240046)

// ============================================================================
// Текстовые цвета
// ============================================================================

/** Основной текст - светлый */
val TextPrimary = Color(0xFFE0E0E0)

/** Вторичный текст */
val TextSecondary = Color(0xFFB0B0B0)

/** Акцентный текст */
val TextAccent = AccentPurple

// ============================================================================
// Совместимость - убраны психоделические эффекты
// ============================================================================

val PsychoRed = PrimaryPurple
val PsychoDarkRed = PrimaryPurpleDark
val PsychoMagenta = PrimaryPurpleLight
val PsychoCyan = AccentPurple
val PsychoYellow = AccentPurple
val PsychoPurple = PrimaryPurple
val PsychoOrange = PrimaryPurpleLight
val PsychoPink = AccentPurple

// ============================================================================
// Системные сообщения
// ============================================================================

val ErrorRed = Color(0xFFFF6B6B)
val WarningYellow = Color(0xFFFFE66D)
val InfoBlue = Color(0xFF4ECDC4)

// ============================================================================
// Эффекты отключены - заглушки
// ============================================================================

val GlitchScanline = Color(0x00000000)
val GlitchNoise = Color(0x00000000)
val GlitchRgbRed = Color(0x00000000)
val GlitchRgbCyan = Color(0x00000000)

// ============================================================================
// Текст
// ============================================================================

val HighlightColor = AccentPurple
val ShadowColor = DeepPurple
val CursorColor = AccentPurple
val CorruptedTextColor = PrimaryPurpleLight
