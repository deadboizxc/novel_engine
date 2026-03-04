package com.deadboizxc.tuinovel

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.deadboizxc.tuinovel.ui.screens.MainMenuScreen
import com.deadboizxc.tuinovel.ui.screens.SettingsScreen
import com.deadboizxc.tuinovel.ui.screens.TerminalScreen
import com.deadboizxc.tuinovel.ui.theme.TuiNovelTheme

/**
 * Экраны приложения
 */
enum class AppScreen {
    MAIN_MENU,
    GAME,
    SETTINGS
}

/**
 * =============================================================================
 * TUI Novel - MainActivity
 * Главная Activity приложения с полноэкранным терминальным UI
 * =============================================================================
 * 
 * Это точка входа в приложение. Здесь:
 * - Устанавливается полноэкранный иммерсивный режим
 * - Инициализируется Compose UI
 * - Управляется навигация между экранами
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge режим (но со статус баром)
        enableEdgeToEdge()
        setupWindowMode()
        
        // Устанавливаем Compose UI
        setContent {
            TuiNovelTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.MAIN_MENU) }
                var startFromSave by remember { mutableStateOf(false) }
                
                // Обработка кнопки "назад"
                BackHandler(enabled = currentScreen != AppScreen.MAIN_MENU) {
                    currentScreen = AppScreen.MAIN_MENU
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                        },
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            AppScreen.MAIN_MENU -> {
                                MainMenuScreen(
                                    onNewGame = {
                                        startFromSave = false
                                        currentScreen = AppScreen.GAME
                                    },
                                    onContinue = {
                                        startFromSave = true
                                        currentScreen = AppScreen.GAME
                                    },
                                    onSettings = {
                                        currentScreen = AppScreen.SETTINGS
                                    }
                                )
                            }
                            AppScreen.GAME -> {
                                TerminalScreen(
                                    loadFromSave = startFromSave,
                                    onExit = {
                                        currentScreen = AppScreen.MAIN_MENU
                                    }
                                )
                            }
                            AppScreen.SETTINGS -> {
                                SettingsScreen(
                                    onBack = {
                                        currentScreen = AppScreen.MAIN_MENU
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Настройка окна с поддержкой вырезов (cutout/notch)
     * Статус бар виден, контент рисуется под системными барами
     */
    private fun setupWindowMode() {
        // Рисуем под системными барами (edge-to-edge)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Поддержка вырезов (cutout) на Android 9+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        
        // Держим экран включённым во время игры
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
