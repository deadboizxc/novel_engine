package com.deadboizxc.tuinovel.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * =============================================================================
 * TUI Novel - Типографика
 * Моноширинные шрифты для терминального UI
 * =============================================================================
 */

/**
 * Моноширинный шрифт терминала
 * Используем стандартный Monospace, который доступен на всех устройствах
 * Для кастомного шрифта можно добавить .ttf в res/font/ и использовать Font()
 */
val TerminalFontFamily = FontFamily.Monospace

/**
 * Альтернативный шрифт для специальных эффектов
 * Serif для "глючного" текста
 */
val GlitchFontFamily = FontFamily.Serif

/**
 * Sans-serif для системных сообщений
 */
val SystemFontFamily = FontFamily.SansSerif
