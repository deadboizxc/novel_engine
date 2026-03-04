package com.deadboizxc.tuinovel.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deadboizxc.tuinovel.ui.components.*
import com.deadboizxc.tuinovel.ui.effects.*
import com.deadboizxc.tuinovel.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * =============================================================================
 * TUI Novel - Splash Screen (Загрузочный экран)
 * Хакерская заставка с психоделическими эффектами
 * =============================================================================
 */

@Composable
fun SplashScreen(
    onLoadingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    var loadingText by remember { mutableStateOf("Инициализация...") }
    var showGlitch by remember { mutableStateOf(false) }
    var logoVisible by remember { mutableStateOf(false) }
    
    // Анимация загрузки
    LaunchedEffect(Unit) {
        delay(500)
        logoVisible = true
        
        // Симуляция загрузки
        val loadingSteps = listOf(
            "Инициализация матрицы..." to 0.1f,
            "Загрузка сознания..." to 0.25f,
            "Синхронизация воспоминаний..." to 0.4f,
            "Калибровка реальности..." to 0.55f,
            "Подключение к системе..." to 0.7f,
            "Проверка целостности..." to 0.85f,
            "Готово." to 1f
        )
        
        for ((text, progress) in loadingSteps) {
            loadingText = text
            
            // Плавная анимация прогресса
            while (loadingProgress < progress) {
                loadingProgress += 0.01f
                delay(30)
            }
            
            // Случайные глитчи
            if (kotlin.random.Random.nextFloat() > 0.5f) {
                showGlitch = true
                delay(200)
                showGlitch = false
            }
            
            delay(300)
        }
        
        // Финальный глитч перед переходом
        showGlitch = true
        delay(500)
        showGlitch = false
        
        delay(300)
        onLoadingComplete()
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .then(
                if (showGlitch) Modifier.glitchEffect(intensity = 1.5f)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Scanlines
        ScanlinesOverlay(enabled = true, lineAlpha = 0.03f)
        
        // Noise при глитче
        NoiseOverlay(enabled = showGlitch, intensity = 0.5f)
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // ASCII логотип
            AnimatedVisibility(
                visible = logoVisible,
                enter = fadeIn(animationSpec = tween(1000))
            ) {
                Text(
                    text = """
   ████████╗██╗   ██╗██╗
   ╚══██╔══╝██║   ██║██║
      ██║   ██║   ██║██║
      ██║   ╚██████╔╝██║
      ╚═╝    ╚═════╝ ╚═╝
                    """.trimIndent(),
                    color = if (showGlitch) PsychoMagenta else TerminalGreen,
                    fontFamily = TerminalFontFamily,
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Подзаголовок
            Text(
                text = if (showGlitch) 
                    GlitchTextGenerator.corrupt("N O V E L   E N G I N E", 0.4f)
                else 
                    "N O V E L   E N G I N E",
                color = TerminalGreenBright,
                fontFamily = TerminalFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Прогресс бар
            TerminalProgressBar(
                progress = loadingProgress,
                text = "",
                length = 25
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Текст загрузки
            Text(
                text = if (showGlitch)
                    GlitchTextGenerator.corrupt(loadingText, 0.3f)
                else
                    loadingText,
                color = TerminalGreenDim,
                fontFamily = TerminalFontFamily,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Версия
            Text(
                text = "v1.0.0 // PSYCHO TERMINAL",
                color = TerminalGreenDim.copy(alpha = 0.5f),
                fontFamily = TerminalFontFamily,
                fontSize = 10.sp
            )
        }
        
        // Предупреждение внизу
        Text(
            text = "⚠ ВНИМАНИЕ: Содержит мигающие эффекты",
            color = WarningYellow.copy(alpha = 0.7f),
            fontFamily = TerminalFontFamily,
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

/**
 * Экран "Игра окончена"
 */
@Composable
fun GameOverScreen(
    endingTitle: String,
    endingDescription: String,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showContent by remember { mutableStateOf(false) }
    var glitchActive by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(500)
        glitchActive = false
        delay(500)
        showContent = true
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .then(
                if (glitchActive) Modifier.glitchEffect()
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Эффекты
        ScanlinesOverlay(enabled = true)
        NoiseOverlay(enabled = glitchActive, intensity = 0.6f)
        
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(2000))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                // Заголовок КОНЕЦ
                Text(
                    text = "К О Н Е Ц",
                    color = TerminalGreenBright,
                    fontFamily = TerminalFontFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Название концовки
                Text(
                    text = "「 $endingTitle 」",
                    color = PsychoCyan,
                    fontFamily = TerminalFontFamily,
                    fontSize = 18.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Описание
                Text(
                    text = endingDescription,
                    color = TerminalGreen,
                    fontFamily = TerminalFontFamily,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Кнопки
                Text(
                    text = "[1] Начать заново",
                    color = TerminalGreen,
                    fontFamily = TerminalFontFamily,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { onRestart() }
                        .padding(8.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "[2] Главное меню",
                    color = TerminalGreen,
                    fontFamily = TerminalFontFamily,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { onMainMenu() }
                        .padding(8.dp)
                )
            }
        }
    }
}

/**
 * Главное меню
 */
@Composable
fun MainMenuScreen(
    onNewGame: () -> Unit,
    onContinue: () -> Unit,
    onSettings: () -> Unit,
    hasSaveFile: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    var showGlitch by remember { mutableStateOf(false) }
    
    // Случайные глитчи
    LaunchedEffect(Unit) {
        while (true) {
            delay(kotlin.random.Random.nextLong(3000, 8000))
            showGlitch = true
            delay(200)
            showGlitch = false
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .then(
                if (showGlitch) Modifier.glitchEffect(intensity = 0.8f)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Фоновые эффекты
        ScanlinesOverlay(enabled = true, lineAlpha = 0.03f)
        MatrixRainOverlay(enabled = false, density = 15)
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Логотип
            Text(
                text = if (showGlitch)
                    GlitchTextGenerator.corrupt("TUI NOVEL", 0.4f)
                else
                    "TUI NOVEL",
                color = if (showGlitch) PsychoMagenta else TerminalGreenBright,
                fontFamily = TerminalFontFamily,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "PSYCHO TERMINAL",
                color = TerminalGreenDim,
                fontFamily = TerminalFontFamily,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Пункты меню
            MenuItem(
                text = "НОВАЯ ИГРА",
                isSelected = selectedIndex == 0,
                onClick = onNewGame
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (hasSaveFile) {
                MenuItem(
                    text = "ПРОДОЛЖИТЬ",
                    isSelected = selectedIndex == 1,
                    onClick = onContinue
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            MenuItem(
                text = "НАСТРОЙКИ",
                isSelected = selectedIndex == 2,
                onClick = onSettings
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Подсказка
            Text(
                text = ">> Выберите пункт меню <<",
                color = TerminalGreenDim.copy(alpha = 0.5f),
                fontFamily = TerminalFontFamily,
                fontSize = 12.sp
            )
        }
        
        // Версия внизу
        Text(
            text = "v1.0.0",
            color = TerminalGreenDim.copy(alpha = 0.3f),
            fontFamily = TerminalFontFamily,
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
private fun MenuItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "menu_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "menu_alpha"
    )
    
    Text(
        text = if (isSelected) "[ $text ]" else "  $text  ",
        color = if (isSelected) 
            TerminalGreenBright.copy(alpha = alpha) 
        else 
            TerminalGreen,
        fontFamily = TerminalFontFamily,
        fontSize = 18.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
