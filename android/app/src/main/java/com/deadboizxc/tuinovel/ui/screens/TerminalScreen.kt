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
import androidx.compose.foundation.rememberScrollState
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
import com.deadboizxc.tuinovel.engine.*
import com.deadboizxc.tuinovel.ui.components.*
import com.deadboizxc.tuinovel.ui.effects.*
import com.deadboizxc.tuinovel.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * =============================================================================
 * TUI Novel - Главный терминальный экран (Kotlin Engine Version)
 * Хакерский интерфейс с психоделическими эффектами
 * =============================================================================
 */

@Composable
fun TerminalScreen(
    loadFromSave: Boolean = false,
    onExit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Kotlin движок
    val sceneManager = remember { SceneManager(context) }
    val gameState by sceneManager.gameState.collectAsState()
    val currentScene by sceneManager.currentScene.collectAsState()
    val availableChoices by sceneManager.availableChoices.collectAsState()
    val actionMessages by sceneManager.actionMessages.collectAsState()
    val animationRequests by sceneManager.animationRequests.collectAsState()
    val error by sceneManager.error.collectAsState()
    
    // UI состояния
    var terminalOutput by remember { mutableStateOf("") }
    var displayedText by remember { mutableStateOf("") } // Текст с временным glitch
    var isTyping by remember { mutableStateOf(false) }
    var showChoices by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    var showEndScreen by remember { mutableStateOf(false) } // Экран THE END
    
    // Психоделические эффекты
    val psychoController = rememberPsychoEffectState()
    var isGlitching by remember { mutableStateOf(false) }
    var isTextGlitched by remember { mutableStateOf(false) } // Временный glitch текста
    var isShaking by remember { mutableStateOf(false) }
    var showNoise by remember { mutableStateOf(false) }
    var showScanlines by remember { mutableStateOf(true) }
    var showVhs by remember { mutableStateOf(false) }
    var showEyes by remember { mutableStateOf(false) }
    var showMatrixRain by remember { mutableStateOf(false) }
    var colorInverted by remember { mutableStateOf(false) }
    
    // Glitch функция для текста
    fun glitchText(text: String, intensity: Float): String {
        val glitchChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?░▒▓█▀▄"
        val lines = text.split("\n")
        return lines.joinToString("\n") { line ->
            if (kotlin.random.Random.nextFloat() < intensity) {
                line.map { char ->
                    if (kotlin.random.Random.nextFloat() < intensity * 0.5f && char.isLetter()) {
                        if (char.code in 0x0400..0x04FF) {
                            // Кириллица - только меняем регистр
                            if (char.isUpperCase()) char.lowercaseChar() else char.uppercaseChar()
                        } else {
                            glitchChars.random()
                        }
                    } else char
                }.joinToString("")
            } else line
        }
    }
    
    // Временный glitch текста - ломается и восстанавливается
    fun applyTemporaryGlitch(text: String, onComplete: () -> Unit) {
        scope.launch {
            isTextGlitched = true
            // Показываем glitch версию
            displayedText = glitchText(text, intensity = 0.3f)
            delay(150)
            displayedText = glitchText(text, intensity = 0.5f)
            vibrate(context, 50)
            delay(100)
            displayedText = glitchText(text, intensity = 0.2f)
            delay(100)
            // Восстанавливаем нормальный текст
            displayedText = text
            isTextGlitched = false
            onComplete()
        }
    }
    
    // Типирование текста посимвольно
    suspend fun typeTextAnimated(text: String, onChar: (String) -> Unit, speed: Long = 20L) {
        isTyping = true
        var buffer = ""
        for (char in text) {
            buffer += char
            onChar(buffer)
            delay(speed)
            
            // Случайные микро-глитчи
            if (kotlin.random.Random.nextFloat() < 0.02f) {
                isGlitching = false
                delay(50)
                isGlitching = false
            }
        }
        isTyping = false
    }
    
    // Инициализация
    LaunchedEffect(Unit) {
        delay(300)
        
        // Начальная заставка
        val logo = "N O V E L   E N G I N E\n\n"
        
        typeTextAnimated(logo, { terminalOutput = it; displayedText = it }, 15L)
        
        delay(300)
        
        // Глитч при запуске
        isGlitching = false
        vibrate(context, 100)
        delay(200)
        isGlitching = false
        
        // Короткая инициализация
        terminalOutput += "[СИСТЕМА] Загрузка...\n"
        displayedText = terminalOutput
        delay(500)
        
        // Инициализируем движок
        sceneManager.initialize()
        
        // Загружаем или начинаем новую игру
        if (loadFromSave && sceneManager.loadGame(slot = 0)) {
            terminalOutput += "[СИСТЕМА] Прогресс восстановлен\n\n"
            displayedText = terminalOutput
        } else {
            // Новая игра - сбрасываем состояние
            sceneManager.resetGame()
            terminalOutput += "[СИСТЕМА] Новая игра\n\n"
            displayedText = terminalOutput
        }
        
        delay(300)
        isInitialized = true
    }
    
    // Обработка новой сцены
    LaunchedEffect(currentScene, isInitialized) {
        if (!isInitialized || currentScene == null) return@LaunchedEffect
        
        val scene = currentScene!!
        
        // Очищаем экран как в оригинальном движке (clear_screen)
        terminalOutput = ""
        displayedText = ""
        
        // Показываем название сцены
        terminalOutput = "--- ${gameState.current} ---\n\n"
        
        // Временный glitch при переходе
        applyTemporaryGlitch(terminalOutput) {
            scope.launch {
                // Печатаем текст сцены
                val sceneText = scene.text.trim()
                if (sceneText.isNotEmpty()) {
                    val startLen = terminalOutput.length
                    typeTextAnimated(sceneText, { typed ->
                        terminalOutput = terminalOutput.substring(0, startLen) + typed
                        displayedText = terminalOutput
                    })
                }
                
                // Показываем сообщения действий (если есть)
                if (actionMessages.isNotEmpty()) {
                    terminalOutput += "\n\n"
                    actionMessages.forEach { msg ->
                        terminalOutput += "$msg\n"
                    }
                    displayedText = terminalOutput
                }
                
                delay(300)
                
                // Проверяем конец игры (нет выборов = THE END)
                if (availableChoices.isEmpty()) {
                    delay(500)
                    showEndScreen = true
                } else {
                    showChoices = true
                }
            }
        }
    }
    
    // Обработка анимаций из движка
    LaunchedEffect(animationRequests) {
        for (request in animationRequests) {
            when (request.type) {
                "glitch" -> {
                    isGlitching = false
                    delay((request.duration * 1000).toLong())
                    isGlitching = false
                }
                "shake" -> {
                    isShaking = true
                    vibrate(context, (request.duration * 1000).toLong().toInt())
                    delay((request.duration * 1000).toLong())
                    isShaking = false
                }
                "noise" -> {
                    showNoise = true
                    delay((request.duration * 1000).toLong())
                    showNoise = false
                }
                "eyes" -> {
                    showEyes = true
                    delay((request.duration * 1000).toLong())
                    showEyes = false
                }
                "matrix" -> {
                    showMatrixRain = false
                    delay((request.duration * 1000).toLong())
                    showMatrixRain = false
                }
                "invert" -> {
                    colorInverted = true
                    delay((request.duration * 1000).toLong())
                    colorInverted = false
                }
            }
        }
        sceneManager.clearAnimations()
    }
    
    // Автоскролл
    LaunchedEffect(displayedText) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }
    
    // UI
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .statusBarsPadding()
            .displayCutoutPadding()
            .then(if (isShaking) Modifier.screenShake(intensity = 1.5f) else Modifier)
            .then(if (colorInverted) Modifier.colorInversion() else Modifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .then(if (isGlitching) Modifier.glitchEffect(intensity = psychoController.currentIntensity) else Modifier)
        ) {
            // Заголовок
            GameHeader(
                sanity = gameState.sanity,
                coins = gameState.coins,
                currentScene = gameState.current,
                isGlitching = isGlitching
            )
            
            // Область текста
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Текст с возможным глитчем
                    if (isGlitching || isTextGlitched) {
                        ChromaticText(
                            text = displayedText,
                            offset = 3f,
                            fontSize = 15.sp
                        )
                    } else {
                        Text(
                            text = displayedText,
                            color = TerminalGreen,
                            fontFamily = TerminalFontFamily,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }
                    
                    if (isTyping) {
                        BlinkingCursor()
                    }
                }
            }
            
            // Кнопки выбора
            AnimatedVisibility(
                visible = showChoices && availableChoices.isNotEmpty(),
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
                ChoiceButtons(
                    choices = availableChoices,
                    onChoiceSelected = { index ->
                        showChoices = false
                        scope.launch {
                            val choice = availableChoices[index]
                            
                            // Показываем что выбрали (кратко)
                            displayedText = "[Выбрано: ${choice.text}]"
                            
                            // Эффекты в зависимости от текста выбора
                            val choiceText = choice.text.lowercase()
                            when {
                                choiceText.contains("психоз") || choiceText.contains("таблетк") -> {
                                    isGlitching = false
                                    showEyes = true
                                    vibrate(context, 500)
                                    delay(800)
                                    isGlitching = false
                                    showEyes = false
                                }
                                choiceText.contains("страх") || choiceText.contains("крик") -> {
                                    isShaking = true
                                    vibrate(context, 300)
                                    delay(500)
                                    isShaking = false
                                }
                                else -> {
                                    // Мини-glitch при любом выборе
                                    isGlitching = false
                                    delay(150)
                                    isGlitching = false
                                }
                            }
                            
                            delay(200)
                            sceneManager.executeChoice(index)
                        }
                    },
                    onSystemCommand = { cmd ->
                        scope.launch {
                            when (cmd) {
                                "s", "v" -> {
                                    sceneManager.saveGame()
                                    // Показываем уведомление поверх текста
                                    val savedText = displayedText
                                    displayedText = "[✓] Игра сохранена"
                                    delay(1000)
                                    displayedText = savedText
                                }
                                "l" -> {
                                    if (sceneManager.loadGame()) {
                                        // При загрузке сцена обновится автоматически
                                        displayedText = "[✓] Загрузка..."
                                    } else {
                                        val savedText = displayedText
                                        displayedText = "[✗] Сохранение не найдено"
                                        delay(1500)
                                        displayedText = savedText
                                    }
                                }
                                "q" -> {
                                    displayedText = "[ВЫХОД] Завершение..."
                                }
                            }
                        }
                    }
                )
            }
        }
        
        // Экран THE END
        AnimatedVisibility(
            visible = showEndScreen,
            enter = fadeIn(tween(1000)),
            exit = fadeOut(tween(500))
        ) {
            TheEndScreen(
                onMenu = onExit,
                onNewGame = {
                    showEndScreen = false
                    sceneManager.resetGame()
                },
                onLoadLast = {
                    showEndScreen = false
                    if (sceneManager.loadGame(slot = 0)) {
                        // Загрузилось
                    }
                },
                onLoadSlot = { slot ->
                    showEndScreen = false
                    sceneManager.loadGame(slot = slot)
                },
                availableSlots = sceneManager.listSaves()
                    .mapNotNull { it.removeSuffix(".json").toIntOrNull() }
                    .filter { it > 0 }
                    .sorted()
            )
        }
        
        // Overlay эффекты
        ScanlinesOverlay(enabled = showScanlines, lineAlpha = 0.04f)
        NoiseOverlay(enabled = showNoise, intensity = 0.35f)
        VhsDistortionOverlay(enabled = showVhs, intensity = 1f)
        EyeOverlay(enabled = showEyes)
        MatrixRainOverlay(enabled = showMatrixRain, density = 30)
    }
}

/**
 * Заголовок игры
 */
@Composable
private fun GameHeader(
    sanity: Int,
    coins: Int,
    currentScene: String,
    isGlitching: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0A))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "TUI NOVEL",
            color = TerminalGreen,
            fontFamily = TerminalFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Монеты
            Text(
                text = "💰 $coins",
                color = Color(0xFFFFD700),
                fontFamily = TerminalFontFamily,
                fontSize = 13.sp
            )
            
            // Sanity
            val sanityColor = when {
                sanity > 70 -> TerminalGreen
                sanity > 40 -> Color(0xFFFFAA00)
                else -> Color(0xFFFF4444)
            }
            Text(
                text = "🧠 $sanity%",
                color = if (isGlitching) Color.Red else sanityColor,
                fontFamily = TerminalFontFamily,
                fontSize = 13.sp
            )
        }
    }
}

/**
 * Кнопки выбора вариантов
 */
@Composable
private fun ChoiceButtons(
    choices: List<Choice>,
    onChoiceSelected: (Int) -> Unit,
    onSystemCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0A))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Варианты выбора как кнопки
        choices.forEachIndexed { index, choice ->
            ChoiceButton(
                text = choice.text,
                index = index + 1,
                onClick = { onChoiceSelected(index) }
            )
        }
        
        // Системные кнопки
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SystemButton(
                text = "[S] Сохранить",
                onClick = { onSystemCommand("s") },
                modifier = Modifier.weight(1f)
            )
            SystemButton(
                text = "[L] Загрузить",
                onClick = { onSystemCommand("l") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Кнопка выбора варианта
 */
@Composable
private fun ChoiceButton(
    text: String,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) TerminalGreen.copy(alpha = 0.3f) else Color(0xFF1A1A1A),
        animationSpec = tween(100),
        label = "choiceBg"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isPressed) TerminalGreen else TerminalGreen.copy(alpha = 0.5f),
        animationSpec = tween(100),
        label = "choiceBorder"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable {
                isPressed = true
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Номер
            Text(
                text = "$index)",
                color = TerminalGreen.copy(alpha = 0.7f),
                fontFamily = TerminalFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Текст варианта
            Text(
                text = text,
                color = TerminalGreen,
                fontFamily = TerminalFontFamily,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Системная кнопка
 */
@Composable
private fun SystemButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, TerminalGreen.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .background(Color(0xFF0D0D0D))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = TerminalGreen.copy(alpha = 0.7f),
            fontFamily = TerminalFontFamily,
            fontSize = 11.sp
        )
    }
}

/**
 * Хроматический текст (RGB split)
 */
@Composable
private fun ChromaticText(
    text: String,
    offset: Float = 2f,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp
) {
    Box {
        // Красный слой
        Text(
            text = text,
            color = Color.Red.copy(alpha = 0.5f),
            fontFamily = TerminalFontFamily,
            fontSize = fontSize,
            lineHeight = fontSize * 1.4f,
            modifier = Modifier.offset(x = offset.dp, y = 0.dp)
        )
        // Синий слой
        Text(
            text = text,
            color = Color.Blue.copy(alpha = 0.5f),
            fontFamily = TerminalFontFamily,
            fontSize = fontSize,
            lineHeight = fontSize * 1.4f,
            modifier = Modifier.offset(x = (-offset).dp, y = 0.dp)
        )
        // Основной зелёный
        Text(
            text = text,
            color = TerminalGreen,
            fontFamily = TerminalFontFamily,
            fontSize = fontSize,
            lineHeight = fontSize * 1.4f
        )
    }
}

/**
 * Мигающий курсор
 */
@Composable
private fun BlinkingCursor() {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )
    
    Text(
        text = "█",
        color = TerminalGreen.copy(alpha = alpha),
        fontFamily = TerminalFontFamily,
        fontSize = 15.sp
    )
}

/**
 * Экран THE END с анимацией как в главном меню
 */
@Composable
private fun TheEndScreen(
    onMenu: () -> Unit,
    onNewGame: () -> Unit,
    onLoadLast: () -> Unit,
    onLoadSlot: (Int) -> Unit,
    availableSlots: List<Int>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showTitle by remember { mutableStateOf(false) }
    var titleText by remember { mutableStateOf("") }
    var showButtons by remember { mutableStateOf(false) }
    var isGlitching by remember { mutableStateOf(false) }
    var glitchedTitle by remember { mutableStateOf("") }
    var showSlotSelector by remember { mutableStateOf(false) }
    
    val targetTitle = "THE END"
    
    // Функция глитча
    fun glitchText(text: String, intensity: Float): String {
        val glitchChars = "!@#$%^&*░▒▓█▀▄"
        return text.map { char ->
            if (kotlin.random.Random.nextFloat() < intensity && char.isLetter()) {
                glitchChars.random()
            } else char
        }.joinToString("")
    }
    
    // Анимация появления
    LaunchedEffect(Unit) {
        delay(500)
        showTitle = true
        
        // Печатаем THE END посимвольно
        for (i in targetTitle.indices) {
            titleText = targetTitle.substring(0, i + 1)
            if (kotlin.random.Random.nextFloat() < 0.2f) {
                isGlitching = false
                delay(50)
                isGlitching = false
            }
            delay(100)
        }
        
        delay(300)
        
        // Глитч эффект
        isGlitching = false
        vibrate(context, 200)
        delay(400)
        isGlitching = false
        
        delay(300)
        showButtons = true
    }
    
    // Периодические глитчи
    LaunchedEffect(Unit) {
        while (true) {
            delay(kotlin.random.Random.nextLong(2000, 5000))
            isGlitching = false
            glitchedTitle = glitchText(targetTitle, 0.5f)
            vibrate(context, 50)
            delay(100)
            glitchedTitle = glitchText(targetTitle, 0.7f)
            delay(80)
            glitchedTitle = targetTitle
            isGlitching = false
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentAlignment = Alignment.Center
    ) {
        // Matrix rain на фоне
        MatrixRainOverlay(enabled = false, density = 20)
        ScanlinesOverlay(enabled = true, lineAlpha = 0.03f)
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // THE END заголовок
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(tween(500))
            ) {
                Box {
                    if (isGlitching) {
                        // RGB split эффект
                        Text(
                            text = if (glitchedTitle.isNotEmpty()) glitchedTitle else titleText,
                            color = Color.Red.copy(alpha = 0.7f),
                            fontFamily = TerminalFontFamily,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 12.sp,
                            modifier = Modifier.offset(x = 4.dp)
                        )
                        Text(
                            text = if (glitchedTitle.isNotEmpty()) glitchedTitle else titleText,
                            color = Color.Blue.copy(alpha = 0.7f),
                            fontFamily = TerminalFontFamily,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 12.sp,
                            modifier = Modifier.offset(x = (-4).dp)
                        )
                    }
                    Text(
                        text = if (isGlitching && glitchedTitle.isNotEmpty()) glitchedTitle else titleText,
                        color = TerminalGreen,
                        fontFamily = TerminalFontFamily,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Кнопки
            AnimatedVisibility(
                visible = showButtons && !showSlotSelector,
                enter = fadeIn(tween(500)) + slideInVertically { it / 2 }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.width(260.dp)
                ) {
                    // В меню
                    EndScreenButton(
                        text = "[ В МЕНЮ ]",
                        onClick = {
                            scope.launch {
                                isGlitching = false
                                vibrate(context, 100)
                                delay(150)
                                onMenu()
                            }
                        }
                    )
                    
                    // Начать заново
                    EndScreenButton(
                        text = "[ НАЧАТЬ ЗАНОВО ]",
                        onClick = {
                            scope.launch {
                                isGlitching = false
                                vibrate(context, 100)
                                delay(150)
                                onNewGame()
                            }
                        }
                    )
                    
                    // Загрузить последнее
                    EndScreenButton(
                        text = "[ ЗАГРУЗИТЬ ПОСЛЕДНЕЕ ]",
                        onClick = {
                            scope.launch {
                                isGlitching = false
                                vibrate(context, 100)
                                delay(150)
                                onLoadLast()
                            }
                        }
                    )
                    
                    // Выбрать сохранение (если есть)
                    if (availableSlots.isNotEmpty()) {
                        EndScreenButton(
                            text = "[ ВЫБРАТЬ СЕЙВ ]",
                            onClick = {
                                showSlotSelector = true
                            }
                        )
                    }
                }
            }
            
            // Выбор слота
            AnimatedVisibility(
                visible = showSlotSelector,
                enter = fadeIn() + slideInVertically { -it / 4 },
                exit = fadeOut() + slideOutVertically { -it / 4 }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.width(260.dp)
                ) {
                    Text(
                        text = "═══ СОХРАНЕНИЯ ═══",
                        color = TerminalGreen.copy(alpha = 0.7f),
                        fontFamily = TerminalFontFamily,
                        fontSize = 14.sp
                    )
                    
                    availableSlots.forEach { slot ->
                        EndScreenButton(
                            text = "[ СЛОТ $slot ]",
                            onClick = {
                                scope.launch {
                                    isGlitching = false
                                    vibrate(context, 100)
                                    delay(150)
                                    onLoadSlot(slot)
                                }
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    EndScreenButton(
                        text = "[ НАЗАД ]",
                        onClick = { showSlotSelector = false }
                    )
                }
            }
        }
    }
}

/**
 * Кнопка на экране THE END
 */
@Composable
private fun EndScreenButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val bgAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.2f else 0.05f,
        animationSpec = tween(100),
        label = "endBtnBg"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, TerminalGreen.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .background(TerminalGreen.copy(alpha = bgAlpha))
            .clickable {
                isPressed = true
                onClick()
            }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = TerminalGreen,
            fontFamily = TerminalFontFamily,
            fontSize = 14.sp,
            letterSpacing = 1.sp
        )
    }
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
    } catch (e: Exception) {
        // Ignore vibration errors
    }
}
