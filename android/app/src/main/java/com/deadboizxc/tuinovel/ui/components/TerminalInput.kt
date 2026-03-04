package com.deadboizxc.tuinovel.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deadboizxc.tuinovel.ui.theme.*
import kotlinx.coroutines.delay

/**
 * =============================================================================
 * TUI Novel - Компонент ввода терминала
 * Стилизованный под хакерский терминал
 * =============================================================================
 */

/**
 * Терминальный ввод с мигающим курсором и зелёным текстом
 * 
 * @param value Текущее значение
 * @param onValueChange Callback при изменении
 * @param onSubmit Callback при отправке (Enter)
 * @param prompt Символ приглашения (по умолчанию ">")
 * @param enabled Активен ли ввод
 * @param modifier Модификатор
 */
@Composable
fun TerminalInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    prompt: String = ">",
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Автофокус при появлении
    LaunchedEffect(enabled) {
        if (enabled) {
            delay(100)
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(InputBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Символ приглашения
        Text(
            text = prompt,
            color = TerminalGreen,
            fontFamily = TerminalFontFamily,
            fontSize = 16.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
        
        // Поле ввода
        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                textStyle = TextStyle(
                    color = TerminalGreen,
                    fontFamily = TerminalFontFamily,
                    fontSize = 16.sp
                ),
                cursorBrush = SolidColor(TerminalGreen),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (value.isNotBlank()) {
                            onSubmit(value)
                            keyboardController?.hide()
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            
            // Мигающий курсор когда пусто
            if (value.isEmpty()) {
                BlinkingCursor()
            }
        }
    }
}

/**
 * Мигающий курсор терминала
 */
@Composable
fun BlinkingCursor(
    modifier: Modifier = Modifier,
    color: Color = CursorColor
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )
    
    Text(
        text = "█",
        color = color,
        fontFamily = TerminalFontFamily,
        fontSize = 16.sp,
        modifier = modifier.alpha(alpha)
    )
}

/**
 * Компонент для отображения выборов в терминале как кнопки
 * 
 * @param choices Список вариантов выбора
 * @param onChoiceSelected Callback при выборе
 * @param enabled Активны ли выборы
 */
@Composable
fun TerminalChoices(
    choices: List<String>,
    onChoiceSelected: (Int) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Кнопки выбора
        choices.forEachIndexed { index, choice ->
            ChoiceButton(
                text = choice,
                index = index + 1,
                enabled = enabled,
                isPsycho = choice.contains("[ПСИХОЗ]") || choice.contains("[PSYCHO]"),
                onClick = { onChoiceSelected(index) }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Системные кнопки в строку
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SystemButton(text = "⚙", hint = "s", onClick = { onChoiceSelected(-1) })
            SystemButton(text = "💾", hint = "v", onClick = { onChoiceSelected(-2) })
            SystemButton(text = "📂", hint = "l", onClick = { onChoiceSelected(-3) })
            SystemButton(text = "✖", hint = "q", onClick = { onChoiceSelected(-4) })
        }
    }
}

/**
 * Кнопка выбора в терминальном стиле
 */
@Composable
private fun ChoiceButton(
    text: String,
    index: Int,
    enabled: Boolean,
    isPsycho: Boolean = false,
    onClick: () -> Unit
) {
    val borderColor = if (isPsycho) PsychoRed else TerminalGreen
    val textColor = if (isPsycho) PsychoRed else TerminalGreen
    
    // Пульсация для психо-вариантов
    val infiniteTransition = rememberInfiniteTransition(label = "choice_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPsycho) 0.6f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = borderColor.copy(alpha = pulseAlpha),
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = borderColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Номер варианта
            Text(
                text = "$index)",
                color = textColor.copy(alpha = 0.7f),
                fontFamily = TerminalFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )
            
            // Текст варианта
            Text(
                text = text,
                color = textColor.copy(alpha = pulseAlpha),
                fontFamily = TerminalFontFamily,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Системная кнопка (сохранение, загрузка и т.д.)
 */
@Composable
private fun SystemButton(
    text: String,
    hint: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .border(
                width = 1.dp,
                color = TerminalGreenDim,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 18.sp
        )
        Text(
            text = "[$hint]",
            color = TerminalGreenDim,
            fontFamily = TerminalFontFamily,
            fontSize = 10.sp
        )
    }
}

/**
 * Индикатор загрузки в терминальном стиле (спиннер)
 */
@Composable
fun TerminalSpinner(
    text: String = "Загрузка",
    modifier: Modifier = Modifier
) {
    val spinnerChars = remember { listOf("/", "-", "\\", "|") }
    var currentIndex by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            currentIndex = (currentIndex + 1) % spinnerChars.size
        }
    }
    
    Row(modifier = modifier) {
        Text(
            text = text,
            color = TerminalGreen,
            fontFamily = TerminalFontFamily,
            fontSize = 14.sp
        )
        Text(
            text = " ${spinnerChars[currentIndex]}",
            color = TerminalGreenBright,
            fontFamily = TerminalFontFamily,
            fontSize = 14.sp
        )
    }
}

/**
 * Анимация точек (dots)
 */
@Composable
fun TerminalDots(
    text: String = "Загрузка",
    modifier: Modifier = Modifier
) {
    val dots = remember { listOf("", ".", "..", "...") }
    var currentIndex by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(400)
            currentIndex = (currentIndex + 1) % dots.size
        }
    }
    
    Text(
        text = "$text${dots[currentIndex]}",
        color = TerminalGreen,
        fontFamily = TerminalFontFamily,
        fontSize = 14.sp,
        modifier = modifier
    )
}

/**
 * Прогресс-бар в терминальном стиле
 */
@Composable
fun TerminalProgressBar(
    progress: Float,
    text: String = "Progress",
    length: Int = 30,
    modifier: Modifier = Modifier
) {
    val filled = (length * progress).toInt()
    val bar = "#".repeat(filled) + "-".repeat(length - filled)
    val percent = (progress * 100).toInt()
    
    Text(
        text = "$text: [$bar] $percent%",
        color = TerminalGreen,
        fontFamily = TerminalFontFamily,
        fontSize = 14.sp,
        modifier = modifier
    )
}

/**
 * Пульсирующий текст
 */
@Composable
fun PulsingText(
    text: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    Text(
        text = text,
        color = TerminalGreen,
        fontFamily = TerminalFontFamily,
        fontSize = 14.sp,
        modifier = modifier.alpha(alpha)
    )
}
