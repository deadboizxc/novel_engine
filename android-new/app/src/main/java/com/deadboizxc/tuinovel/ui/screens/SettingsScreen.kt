package com.deadboizxc.tuinovel.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deadboizxc.tuinovel.ui.effects.*
import com.deadboizxc.tuinovel.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Настройки игры
 */
@Serializable
data class GameConfig(
    val textSpeed: Int = 20,           // Скорость печати текста (мс)
    val vibrationEnabled: Boolean = true,  // Вибрация
    val glitchEffects: Boolean = true,     // Глитч эффекты
    val scanlinesEnabled: Boolean = true,  // Scanlines
    val matrixRain: Boolean = false,       // Matrix rain на фоне
    val autoSave: Boolean = true           // Автосохранение
)

/**
 * Экран настроек
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Загружаем конфиг
    var config by remember { mutableStateOf(loadConfig(context)) }
    var showContent by remember { mutableStateOf(false) }
    var isGlitching by remember { mutableStateOf(false) }
    
    // Анимация появления
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }
    
    // Сохраняем при изменении
    fun updateConfig(newConfig: GameConfig) {
        config = newConfig
        saveConfig(context, newConfig)
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Scanlines
        if (config.scanlinesEnabled) {
            ScanlinesOverlay(enabled = true, lineAlpha = 0.03f)
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка назад
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, TerminalGreen.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .clickable {
                            scope.launch {
                                isGlitching = true
                                delay(100)
                                onBack()
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "< НАЗАД",
                        color = TerminalGreen,
                        fontFamily = TerminalFontFamily,
                        fontSize = 14.sp
                    )
                }
                
                Text(
                    text = "[ НАСТРОЙКИ ]",
                    color = TerminalGreen,
                    fontFamily = TerminalFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.width(80.dp))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Настройки
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically { -it / 4 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // === ГЕЙМПЛЕЙ ===
                    SettingsSection(title = "ГЕЙМПЛЕЙ") {
                        // Скорость текста
                        SettingsSlider(
                            label = "Скорость текста",
                            value = config.textSpeed.toFloat(),
                            valueRange = 5f..50f,
                            valueLabel = "${config.textSpeed} мс",
                            onValueChange = { 
                                updateConfig(config.copy(textSpeed = it.toInt()))
                            }
                        )
                        
                        // Автосохранение
                        SettingsToggle(
                            label = "Автосохранение",
                            description = "Сохранять прогресс после каждого выбора",
                            checked = config.autoSave,
                            onCheckedChange = { 
                                updateConfig(config.copy(autoSave = it))
                            }
                        )
                    }
                    
                    // === ЭФФЕКТЫ ===
                    SettingsSection(title = "ЭФФЕКТЫ") {
                        // Глитч эффекты
                        SettingsToggle(
                            label = "Глитч эффекты",
                            description = "Визуальные искажения и помехи",
                            checked = config.glitchEffects,
                            onCheckedChange = { 
                                updateConfig(config.copy(glitchEffects = it))
                            }
                        )
                        
                        // Scanlines
                        SettingsToggle(
                            label = "Scanlines",
                            description = "Эффект старого монитора",
                            checked = config.scanlinesEnabled,
                            onCheckedChange = { 
                                updateConfig(config.copy(scanlinesEnabled = it))
                            }
                        )
                        
                        // Matrix rain
                        SettingsToggle(
                            label = "Matrix Rain",
                            description = "Падающие символы на фоне",
                            checked = config.matrixRain,
                            onCheckedChange = { 
                                updateConfig(config.copy(matrixRain = it))
                            }
                        )
                        
                        // Вибрация
                        SettingsToggle(
                            label = "Вибрация",
                            description = "Тактильная обратная связь",
                            checked = config.vibrationEnabled,
                            onCheckedChange = { 
                                updateConfig(config.copy(vibrationEnabled = it))
                            }
                        )
                    }
                    
                    // === ДАННЫЕ ===
                    SettingsSection(title = "ДАННЫЕ") {
                        // Сбросить сохранения
                        SettingsButton(
                            label = "Удалить все сохранения",
                            description = "Это действие нельзя отменить",
                            buttonText = "УДАЛИТЬ",
                            isDanger = true,
                            onClick = {
                                scope.launch {
                                    deleteSaves(context)
                                    isGlitching = true
                                    delay(300)
                                    isGlitching = false
                                }
                            }
                        )
                        
                        // Сбросить настройки
                        SettingsButton(
                            label = "Сбросить настройки",
                            description = "Вернуть настройки по умолчанию",
                            buttonText = "СБРОС",
                            isDanger = false,
                            onClick = {
                                updateConfig(GameConfig())
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

/**
 * Секция настроек
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "═══ $title ═══",
            color = TerminalGreen.copy(alpha = 0.7f),
            fontFamily = TerminalFontFamily,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TerminalGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

/**
 * Переключатель настройки
 */
@Composable
private fun SettingsToggle(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = TerminalGreen,
                fontFamily = TerminalFontFamily,
                fontSize = 14.sp
            )
            Text(
                text = description,
                color = TerminalGreen.copy(alpha = 0.5f),
                fontFamily = TerminalFontFamily,
                fontSize = 11.sp
            )
        }
        
        // Кастомный переключатель
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, TerminalGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .background(if (checked) TerminalGreen.copy(alpha = 0.3f) else Color.Transparent)
                .clickable { onCheckedChange(!checked) }
        ) {
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .size(20.dp)
                    .offset(x = if (checked) 24.dp else 0.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (checked) TerminalGreen else TerminalGreen.copy(alpha = 0.3f))
            )
        }
    }
}

/**
 * Слайдер настройки
 */
@Composable
private fun SettingsSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = TerminalGreen,
                fontFamily = TerminalFontFamily,
                fontSize = 14.sp
            )
            Text(
                text = valueLabel,
                color = TerminalGreen.copy(alpha = 0.7f),
                fontFamily = TerminalFontFamily,
                fontSize = 14.sp
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = TerminalGreen,
                activeTrackColor = TerminalGreen,
                inactiveTrackColor = TerminalGreen.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Кнопка в настройках
 */
@Composable
private fun SettingsButton(
    label: String,
    description: String,
    buttonText: String,
    isDanger: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = TerminalGreen,
                fontFamily = TerminalFontFamily,
                fontSize = 14.sp
            )
            Text(
                text = description,
                color = TerminalGreen.copy(alpha = 0.5f),
                fontFamily = TerminalFontFamily,
                fontSize = 11.sp
            )
        }
        
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .border(
                    1.dp, 
                    if (isDanger) Color.Red.copy(alpha = 0.7f) else TerminalGreen.copy(alpha = 0.5f),
                    RoundedCornerShape(4.dp)
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = buttonText,
                color = if (isDanger) Color.Red else TerminalGreen,
                fontFamily = TerminalFontFamily,
                fontSize = 12.sp
            )
        }
    }
}

// === Функции работы с конфигом ===

private val json = Json { 
    ignoreUnknownKeys = true 
    prettyPrint = true
}

fun loadConfig(context: Context): GameConfig {
    return try {
        val configFile = File(context.filesDir, "config.json")
        if (configFile.exists()) {
            json.decodeFromString(configFile.readText())
        } else {
            GameConfig()
        }
    } catch (e: Exception) {
        GameConfig()
    }
}

fun saveConfig(context: Context, config: GameConfig) {
    try {
        val configFile = File(context.filesDir, "config.json")
        configFile.writeText(json.encodeToString(config))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun deleteSaves(context: Context) {
    try {
        val saveDir = File(context.filesDir, "saves")
        if (saveDir.exists()) {
            saveDir.listFiles()?.forEach { it.delete() }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
