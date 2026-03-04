package com.deadboizxc.tuinovel.ui.effects

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.deadboizxc.tuinovel.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * =============================================================================
 * TUI Novel - Триггеры психоделических эффектов
 * Анализ текста и активация эффектов на основе контента
 * =============================================================================
 */

/**
 * Ключевые слова для активации разных эффектов
 */
object PsychoTriggers {
    
    // Слова, вызывающие глитч эффект
    val GLITCH_TRIGGERS = setOf(
        "глитч", "glitch", "ошибка", "error", "сбой", "crash",
        "искажение", "distortion", "помехи", "static",
        "corrupted", "поврежден", "broken", "сломан"
    )
    
    // Слова для тряски экрана
    val SHAKE_TRIGGERS = setOf(
        "взрыв", "explosion", "удар", "hit", "падение", "fall",
        "землетрясение", "earthquake", "дрожь", "tremble",
        "крик", "scream", "страх", "fear", "ужас", "horror"
    )
    
    // Слова для инверсии цветов
    val INVERT_TRIGGERS = setOf(
        "тьма", "darkness", "негатив", "negative", "обратный", "reverse",
        "инверсия", "inversion", "зеркало", "mirror", "отражение"
    )
    
    // Слова для психоделических эффектов (красная вспышка, глаза)
    val PSYCHO_TRIGGERS = setOf(
        "психоз", "psycho", "безумие", "madness", "insane", "сумасшествие",
        "галлюцинация", "hallucination", "видение", "vision",
        "кровь", "blood", "смерть", "death", "убийство", "kill",
        "монстр", "monster", "демон", "demon", "призрак", "ghost",
        "кошмар", "nightmare", "паранойя", "paranoia",
        "таблетки", "pills", "лекарства", "drugs"
    )
    
    // Слова для VHS/ретро эффектов
    val VHS_TRIGGERS = setOf(
        "воспоминание", "memory", "прошлое", "past", "запись", "recording",
        "кассета", "tape", "видео", "video", "камера", "camera",
        "флешбэк", "flashback"
    )
    
    // Слова для Matrix эффекта
    val MATRIX_TRIGGERS = setOf(
        "матрица", "matrix", "код", "code", "программа", "program",
        "хакер", "hacker", "система", "system", "вирус", "virus",
        "симуляция", "simulation", "реальность", "reality"
    )
    
    // Специальные YAML action теги
    val ACTION_TRIGGERS = mapOf(
        "animate" to PsychoEffectType.GLITCH,
        "glitch" to PsychoEffectType.GLITCH,
        "static" to PsychoEffectType.STATIC_NOISE,
        "shake" to PsychoEffectType.SCREEN_SHAKE,
        "psycho" to PsychoEffectType.PSYCHO_FLASH
    )
    
    /**
     * Анализирует текст и возвращает набор эффектов для активации
     */
    fun analyzeText(text: String): Set<PsychoEffectType> {
        val lowercaseText = text.lowercase()
        val effects = mutableSetOf<PsychoEffectType>()
        
        // Проверяем каждую категорию триггеров
        if (GLITCH_TRIGGERS.any { lowercaseText.contains(it) }) {
            effects.add(PsychoEffectType.GLITCH)
            effects.add(PsychoEffectType.CHROMATIC_ABERRATION)
        }
        
        if (SHAKE_TRIGGERS.any { lowercaseText.contains(it) }) {
            effects.add(PsychoEffectType.SCREEN_SHAKE)
        }
        
        if (INVERT_TRIGGERS.any { lowercaseText.contains(it) }) {
            effects.add(PsychoEffectType.COLOR_INVERSION)
        }
        
        if (PSYCHO_TRIGGERS.any { lowercaseText.contains(it) }) {
            effects.add(PsychoEffectType.PSYCHO_FLASH)
            effects.add(PsychoEffectType.NOISE_OVERLAY)
            // Случайно добавляем глаза для особо страшных моментов
            if (Random.nextFloat() > 0.7f) {
                effects.add(PsychoEffectType.EYE_OVERLAY)
            }
        }
        
        if (VHS_TRIGGERS.any { lowercaseText.contains(it) }) {
            effects.add(PsychoEffectType.VHS_DISTORTION)
            effects.add(PsychoEffectType.SCANLINES)
        }
        
        if (MATRIX_TRIGGERS.any { lowercaseText.contains(it) }) {
            effects.add(PsychoEffectType.MATRIX_RAIN)
        }
        
        // Проверяем action теги
        ACTION_TRIGGERS.forEach { (trigger, effect) ->
            if (lowercaseText.contains("[$trigger]") || 
                lowercaseText.contains("[anim:$trigger]")) {
                effects.add(effect)
            }
        }
        
        return effects
    }
    
    /**
     * Возвращает интенсивность эффекта на основе "уровня безумия"
     * Чем ниже sanity, тем сильнее эффекты
     */
    fun calculateIntensity(sanity: Int): Float {
        return when {
            sanity <= 0 -> 1.5f    // Максимальное безумие
            sanity <= 25 -> 1.2f
            sanity <= 50 -> 1.0f
            sanity <= 75 -> 0.7f
            else -> 0.5f           // Нормальное состояние
        }
    }
    
    /**
     * Возвращает длительность эффекта в миллисекундах
     */
    fun calculateDuration(effectType: PsychoEffectType): Long {
        return when (effectType) {
            PsychoEffectType.PSYCHO_FLASH -> 200L
            PsychoEffectType.SCREEN_SHAKE -> 500L
            PsychoEffectType.GLITCH -> 2000L
            PsychoEffectType.COLOR_INVERSION -> 1000L
            PsychoEffectType.EYE_OVERLAY -> 300L
            PsychoEffectType.VHS_DISTORTION -> 3000L
            PsychoEffectType.MATRIX_RAIN -> 5000L
            else -> 2000L
        }
    }
}

/**
 * Composable хук для управления психоделическими эффектами
 */
@Composable
fun rememberPsychoEffectState(): PsychoEffectController {
    val activeEffects = remember { mutableStateOf(setOf<PsychoEffectType>()) }
    val intensity = remember { mutableFloatStateOf(1f) }
    
    return remember {
        PsychoEffectController(
            activeEffects = activeEffects,
            intensity = intensity
        )
    }
}

/**
 * Контроллер психоделических эффектов
 */
class PsychoEffectController(
    private val activeEffects: MutableState<Set<PsychoEffectType>>,
    private val intensity: MutableFloatState
) {
    val effects: Set<PsychoEffectType>
        get() = activeEffects.value
    
    val currentIntensity: Float
        get() = intensity.floatValue
    
    /**
     * Активировать эффект на определённое время
     */
    suspend fun triggerEffect(effect: PsychoEffectType, durationMs: Long = 2000L) {
        activeEffects.value = activeEffects.value + effect
        delay(durationMs)
        activeEffects.value = activeEffects.value - effect
    }
    
    /**
     * Активировать несколько эффектов
     */
    suspend fun triggerEffects(effects: Set<PsychoEffectType>) {
        effects.forEach { effect ->
            activeEffects.value = activeEffects.value + effect
        }
        
        // Находим максимальную длительность
        val maxDuration = effects.maxOfOrNull { 
            PsychoTriggers.calculateDuration(it) 
        } ?: 2000L
        
        delay(maxDuration)
        
        effects.forEach { effect ->
            activeEffects.value = activeEffects.value - effect
        }
    }
    
    /**
     * Обработать текст и активировать соответствующие эффекты
     */
    suspend fun processText(text: String, sanity: Int = 100) {
        val triggeredEffects = PsychoTriggers.analyzeText(text)
        if (triggeredEffects.isNotEmpty()) {
            intensity.floatValue = PsychoTriggers.calculateIntensity(sanity)
            triggerEffects(triggeredEffects)
        }
    }
    
    /**
     * Установить интенсивность эффектов
     */
    fun setIntensity(newIntensity: Float) {
        intensity.floatValue = newIntensity.coerceIn(0f, 2f)
    }
    
    /**
     * Проверить, активен ли эффект
     */
    fun isEffectActive(effect: PsychoEffectType): Boolean {
        return effect in activeEffects.value
    }
    
    /**
     * Отключить все эффекты
     */
    fun clearAllEffects() {
        activeEffects.value = emptySet()
    }
}

/**
 * Генератор "испорченного" текста для глитч-эффекта
 */
object GlitchTextGenerator {
    
    private val glitchChars = "█▓▒░╔╗╚╝║═┌┐└┘├┤┬┴┼▀▄▌▐■□●○◘◙♠♣♥♦"
    private val zalgoUp = "̍̎̏̐̑̒̓̔̕̚̕̚"
    private val zalgoDown = "̡̢̖̗̘̙̜̝̞̟̠̣̤̥̦̩̪̫̬̭̮̯̰̱̲̳̹̺̻̼"
    
    /**
     * Генерирует испорченную версию текста
     */
    fun corrupt(text: String, corruptionLevel: Float = 0.3f): String {
        return text.map { char ->
            if (Random.nextFloat() < corruptionLevel) {
                when (Random.nextInt(4)) {
                    0 -> glitchChars.random()
                    1 -> char.uppercaseChar()
                    2 -> if (Random.nextBoolean()) '█' else '░'
                    else -> char
                }
            } else {
                char
            }
        }.joinToString("")
    }
    
    /**
     * Добавляет "Zalgo" эффект к тексту
     */
    fun zalgofy(text: String, intensity: Int = 2): String {
        return text.map { char ->
            buildString {
                append(char)
                repeat(Random.nextInt(intensity)) {
                    if (Random.nextBoolean()) {
                        append(zalgoUp.random())
                    } else {
                        append(zalgoDown.random())
                    }
                }
            }
        }.joinToString("")
    }
    
    /**
     * Создаёт эффект "двоения" текста
     */
    fun echo(text: String, echoCount: Int = 2): String {
        val lines = mutableListOf(text)
        repeat(echoCount) { i ->
            val offset = " ".repeat(i + 1)
            lines.add("$offset$text")
        }
        return lines.joinToString("\n")
    }
}

/**
 * Цвета для анимированного текста
 */
object AnimatedTextColors {
    
    /**
     * Возвращает цвет, пульсирующий между двумя значениями
     */
    @Composable
    fun pulsingColor(
        color1: Color = TerminalGreen,
        color2: Color = TerminalGreenBright,
        durationMs: Int = 1000
    ): Color {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val animatedFraction by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMs, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_fraction"
        )
        
        return lerp(color1, color2, animatedFraction)
    }
    
    /**
     * Возвращает цвет, случайно меняющийся (глитч)
     */
    @Composable
    fun glitchColor(baseColor: Color = TerminalGreen): Color {
        var currentColor by remember { mutableStateOf(baseColor) }
        
        LaunchedEffect(Unit) {
            while (true) {
                if (Random.nextFloat() > 0.9f) {
                    currentColor = listOf(
                        PsychoRed,
                        PsychoCyan,
                        PsychoMagenta,
                        PsychoYellow,
                        baseColor
                    ).random()
                } else {
                    currentColor = baseColor
                }
                delay(Random.nextLong(50, 200))
            }
        }
        
        return currentColor
    }
    
    /**
     * Интерполяция между двумя цветами
     */
    private fun lerp(start: Color, end: Color, fraction: Float): Color {
        return Color(
            red = start.red + (end.red - start.red) * fraction,
            green = start.green + (end.green - start.green) * fraction,
            blue = start.blue + (end.blue - start.blue) * fraction,
            alpha = start.alpha + (end.alpha - start.alpha) * fraction
        )
    }
}

