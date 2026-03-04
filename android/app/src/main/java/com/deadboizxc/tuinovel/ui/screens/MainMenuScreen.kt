package com.deadboizxc.tuinovel.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deadboizxc.tuinovel.engine.GameState
import com.deadboizxc.tuinovel.ui.effects.*
import com.deadboizxc.tuinovel.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 * Главное меню игры с анимациями и глитчами
 */
@Composable
fun MainMenuScreen(
    onNewGame: () -> Unit,
    onContinue: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Проверяем наличие сохранения
    val hasSave = remember { checkSaveExists(context) }
    
    // Анимации
    var showTitle by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }
    var isGlitching by remember { mutableStateOf(false) }
    var titleText by remember { mutableStateOf("") }
    
    // Глитч эффект для текста
    var glitchedTitle by remember { mutableStateOf("") }
    
    val targetTitle = "NOVEL ENGINE"
    val subtitle = "[ ПСИХОДЕЛИЧЕСКИЙ ТЕРМИНАЛ ]"
    
    // Анимация появления
    LaunchedEffect(Unit) {
        delay(300)
        
        // Печатаем заголовок посимвольно
        showTitle = true
        for (i in targetTitle.indices) {
            titleText = targetTitle.substring(0, i + 1)
            
            // Случайные микро-глитчи
            if (kotlin.random.Random.nextFloat() < 0.15f) {
                isGlitching = true
                delay(50)
                isGlitching = false
            }
            delay(40)
        }
        
        delay(200)
        
        // Глитч и показ подзаголовка
        isGlitching = true
        vibrate(context, 100)
        delay(300)
        isGlitching = false
        showSubtitle = true
        
        delay(400)
        
        // Показ кнопок с анимацией
        showButtons = true
    }
    
    // Периодические глитчи
    LaunchedEffect(Unit) {
        while (true) {
            delay(kotlin.random.Random.nextLong(3000, 8000))
            isGlitching = true
            glitchedTitle = glitchText(targetTitle, 0.4f)
            vibrate(context, 50)
            delay(150)
            glitchedTitle = glitchText(targetTitle, 0.6f)
            delay(100)
            glitchedTitle = targetTitle
            isGlitching = false
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Scanlines эффект
        ScanlinesOverlay(enabled = true, lineAlpha = 0.03f)
        
        // Matrix rain на фоне (слабый)
        MatrixRainOverlay(enabled = false, density = 15)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // Заголовок
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(animationSpec = tween(500))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Основной заголовок с глитчем
                    if (isGlitching) {
                        GlitchedTitle(
                            text = if (glitchedTitle.isNotEmpty()) glitchedTitle else titleText,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    } else {
                        Text(
                            text = titleText,
                            color = TerminalGreen,
                            fontFamily = TerminalFontFamily,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 8.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    // Подзаголовок
                    AnimatedVisibility(
                        visible = showSubtitle,
                        enter = fadeIn() + expandVertically()
                    ) {
                        Text(
                            text = subtitle,
                            color = TerminalGreen.copy(alpha = 0.6f),
                            fontFamily = TerminalFontFamily,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Кнопки меню
            AnimatedVisibility(
                visible = showButtons,
                enter = fadeIn(tween(500)) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(500)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.width(280.dp)
                ) {
                    // Продолжить (если есть сохранение)
                    if (hasSave) {
                        MenuButton(
                            text = "[ ПРОДОЛЖИТЬ ]",
                            onClick = {
                                scope.launch {
                                    isGlitching = true
                                    vibrate(context, 100)
                                    delay(200)
                                    onContinue()
                                }
                            },
                            isPrimary = true
                        )
                    }
                    
                    // Новая игра
                    MenuButton(
                        text = "[ НОВАЯ ИГРА ]",
                        onClick = {
                            scope.launch {
                                isGlitching = true
                                vibrate(context, 100)
                                delay(200)
                                onNewGame()
                            }
                        },
                        isPrimary = !hasSave
                    )
                    
                    // Настройки
                    MenuButton(
                        text = "[ НАСТРОЙКИ ]",
                        onClick = {
                            scope.launch {
                                isGlitching = true
                                vibrate(context, 50)
                                delay(150)
                                onSettings()
                            }
                        },
                        isPrimary = false
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Версия внизу
            AnimatedVisibility(
                visible = showButtons,
                enter = fadeIn(tween(1000))
            ) {
                Text(
                    text = "v1.0.0 // deadboizxc",
                    color = TerminalGreen.copy(alpha = 0.3f),
                    fontFamily = TerminalFontFamily,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * Глитч-заголовок с RGB split
 */
@Composable
private fun GlitchedTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Красный слой
        Text(
            text = text,
            color = Color.Red.copy(alpha = 0.7f),
            fontFamily = TerminalFontFamily,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 8.sp,
            modifier = Modifier.offset(x = 3.dp, y = 0.dp)
        )
        // Синий слой
        Text(
            text = text,
            color = Color.Blue.copy(alpha = 0.7f),
            fontFamily = TerminalFontFamily,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 8.sp,
            modifier = Modifier.offset(x = (-3).dp, y = 0.dp)
        )
        // Основной
        Text(
            text = text,
            color = TerminalGreen,
            fontFamily = TerminalFontFamily,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 8.sp
        )
    }
}

/**
 * Кнопка меню с анимацией
 */
@Composable
private fun MenuButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    var isHovered by remember { mutableStateOf(false) }
    
    val borderAlpha by animateFloatAsState(
        targetValue = if (isHovered || isPrimary) 1f else 0.5f,
        animationSpec = tween(200),
        label = "borderAlpha"
    )
    
    val bgAlpha by animateFloatAsState(
        targetValue = if (isHovered) 0.2f else if (isPrimary) 0.1f else 0.05f,
        animationSpec = tween(200),
        label = "bgAlpha"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = if (isPrimary) 2.dp else 1.dp,
                color = TerminalGreen.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(4.dp)
            )
            .background(TerminalGreen.copy(alpha = bgAlpha))
            .clickable { 
                isHovered = true
                onClick() 
            }
            .padding(vertical = 14.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = TerminalGreen,
            fontFamily = TerminalFontFamily,
            fontSize = 16.sp,
            fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Normal,
            letterSpacing = 2.sp
        )
    }
}

/**
 * Глитч-функция для текста
 */
private fun glitchText(text: String, intensity: Float): String {
    val glitchChars = "!@#$%^&*░▒▓█▀▄"
    return text.map { char ->
        if (kotlin.random.Random.nextFloat() < intensity && char.isLetter()) {
            if (char.code in 0x0400..0x04FF) {
                if (char.isUpperCase()) char.lowercaseChar() else char.uppercaseChar()
            } else {
                glitchChars.random()
            }
        } else char
    }.joinToString("")
}

/**
 * Проверка наличия сохранения
 */
private fun checkSaveExists(context: Context): Boolean {
    return com.deadboizxc.tuinovel.engine.SceneManager.hasSaveFile(context, 0)
}

/**
 * Вибрация
 */
private fun vibrate(context: Context, duration: Int) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator.vibrate(
                VibrationEffect.createOneShot(duration.toLong(), VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration.toLong())
            }
        }
    } catch (e: Exception) { }
}
