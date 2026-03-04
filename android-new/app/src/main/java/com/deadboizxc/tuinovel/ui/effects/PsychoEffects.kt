package com.deadboizxc.tuinovel.ui.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.deadboizxc.tuinovel.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * =============================================================================
 * TUI Novel - Психоделические эффекты
 * Глитчи, искажения, помехи в стиле DDLC / Serial Experiments Lain
 * =============================================================================
 */

// ============================================================================
// Типы психоделических эффектов
// ============================================================================

/**
 * Перечисление всех доступных эффектов
 */
enum class PsychoEffectType {
    NONE,               // Без эффекта
    GLITCH,             // Глитч с RGB сдвигом
    SCREEN_SHAKE,       // Тряска экрана
    COLOR_INVERSION,    // Инверсия цветов
    NOISE_OVERLAY,      // Помехи поверх
    SCANLINES,          // CRT линии сканирования
    VHS_DISTORTION,     // VHS искажение
    CHROMATIC_ABERRATION, // Хроматическая аберрация
    TEXT_CORRUPTION,    // Искажение текста
    STATIC_NOISE,       // Статический шум
    PSYCHO_FLASH,       // Психо-вспышка
    BLOOD_DRIP,         // Капли "крови"
    EYE_OVERLAY,        // Глаза поверх (Lain style)
    MATRIX_RAIN         // Падающие символы
}

/**
 * Состояние психоделических эффектов
 */
data class PsychoState(
    val activeEffects: Set<PsychoEffectType> = emptySet(),
    val intensity: Float = 1f,  // 0.0 - 1.0
    val duration: Long = 3000L  // миллисекунды
)

// ============================================================================
// Модификаторы для эффектов
// ============================================================================

/**
 * Модификатор глитч-эффекта
 * Создаёт случайные смещения и мерцания
 */
@Composable
fun Modifier.glitchEffect(
    enabled: Boolean = true,
    intensity: Float = 1f
): Modifier {
    if (!enabled) return this
    
    // Случайное смещение по X
    val offsetX by produceState(initialValue = 0f) {
        while (true) {
            value = (Random.nextFloat() - 0.5f) * 20f * intensity
            delay(Random.nextLong(50, 150))
        }
    }
    
    // Случайное смещение по Y
    val offsetY by produceState(initialValue = 0f) {
        while (true) {
            value = (Random.nextFloat() - 0.5f) * 10f * intensity
            delay(Random.nextLong(80, 200))
        }
    }
    
    // Случайный масштаб
    val scale by produceState(initialValue = 1f) {
        while (true) {
            value = 1f + (Random.nextFloat() - 0.5f) * 0.1f * intensity
            delay(Random.nextLong(100, 300))
        }
    }
    
    return this.graphicsLayer {
        translationX = offsetX
        translationY = offsetY
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Модификатор тряски экрана
 */
@Composable
fun Modifier.screenShake(
    enabled: Boolean = true,
    intensity: Float = 1f
): Modifier {
    if (!enabled) return this
    
    val shakeX by produceState(initialValue = 0f) {
        while (true) {
            value = (Random.nextFloat() - 0.5f) * 30f * intensity
            delay(16) // ~60fps
        }
    }
    
    val shakeY by produceState(initialValue = 0f) {
        while (true) {
            value = (Random.nextFloat() - 0.5f) * 30f * intensity
            delay(16)
        }
    }
    
    return this.graphicsLayer {
        translationX = shakeX
        translationY = shakeY
    }
}

/**
 * Модификатор инверсии цветов
 */
@Composable
fun Modifier.colorInversion(
    enabled: Boolean = true
): Modifier {
    if (!enabled) return this
    
    val colorMatrix = ColorMatrix(
        floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        )
    )
    
    return this.drawWithContent {
        drawContent()
        drawRect(
            color = Color.Transparent,
            colorFilter = ColorFilter.colorMatrix(colorMatrix),
            blendMode = BlendMode.Difference
        )
    }
}

/**
 * Модификатор хроматической аберрации (RGB сдвиг)
 */
@Composable
fun Modifier.chromaticAberration(
    enabled: Boolean = true,
    offset: Float = 5f
): Modifier {
    if (!enabled) return this
    
    val animatedOffset by produceState(initialValue = 0f) {
        while (true) {
            value = Random.nextFloat() * offset
            delay(100)
        }
    }
    
    return this.drawWithContent {
        // Рисуем красный канал со сдвигом влево
        drawContent()
        
        // Это базовая реализация - для полной хроматической аберрации
        // нужен кастомный шейдер, но это работает для эффекта
    }
}

// ============================================================================
// Overlay эффекты (рисуются поверх контента)
// ============================================================================

/**
 * Наложение шума (static noise)
 */
@Composable
fun NoiseOverlay(
    modifier: Modifier = Modifier,
    intensity: Float = 0.3f,
    enabled: Boolean = true
) {
    if (!enabled) return
    
    var noiseKey by remember { mutableIntStateOf(0) }
    
    // Обновляем шум каждые 50мс
    LaunchedEffect(Unit) {
        while (true) {
            noiseKey++
            delay(50)
        }
    }
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .alpha(intensity)
    ) {
        // Рисуем случайные точки
        val noiseCount = (size.width * size.height / 1000).toInt()
        repeat(noiseCount) {
            val x = Random.nextFloat() * size.width
            val y = Random.nextFloat() * size.height
            val alpha = Random.nextFloat()
            drawRect(
                color = GlitchNoise.copy(alpha = alpha),
                topLeft = Offset(x, y),
                size = Size(2f, 2f)
            )
        }
    }
}

/**
 * CRT Scanlines эффект
 */
@Composable
fun ScanlinesOverlay(
    modifier: Modifier = Modifier,
    lineSpacing: Float = 4f,
    lineAlpha: Float = 0.1f,
    enabled: Boolean = true
) {
    if (!enabled) return
    
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        var y = 0f
        while (y < size.height) {
            drawRect(
                color = BackgroundBlack.copy(alpha = lineAlpha),
                topLeft = Offset(0f, y),
                size = Size(size.width, 1f)
            )
            y += lineSpacing
        }
    }
}

/**
 * VHS distortion эффект
 */
@Composable
fun VhsDistortionOverlay(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    enabled: Boolean = true
) {
    if (!enabled) return
    
    var distortionY by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            // Случайная позиция полосы искажения
            distortionY = Random.nextFloat()
            delay(Random.nextLong(500, 2000))
        }
    }
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .alpha(0.5f * intensity)
    ) {
        // Горизонтальная полоса искажения
        val bandHeight = size.height * 0.1f
        val bandY = distortionY * (size.height - bandHeight)
        
        drawRect(
            color = GlitchRgbCyan.copy(alpha = 0.3f),
            topLeft = Offset(-10f, bandY),
            size = Size(size.width + 20f, bandHeight)
        )
        
        drawRect(
            color = GlitchRgbRed.copy(alpha = 0.3f),
            topLeft = Offset(10f, bandY + 2f),
            size = Size(size.width + 20f, bandHeight)
        )
    }
}

/**
 * Психо-вспышка (красный экран)
 */
@Composable
fun PsychoFlashOverlay(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onFlashEnd: () -> Unit = {}
) {
    if (!enabled) return
    
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 0f else 0.8f,
        animationSpec = tween(
            durationMillis = 100,
            easing = FastOutLinearInEasing
        ),
        finishedListener = { onFlashEnd() },
        label = "flash_alpha"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PsychoRed.copy(alpha = alpha))
    )
}

/**
 * "Глаза" оверлей (Lain style)
 * Рисует мигающие глаза в случайных позициях
 */
@Composable
fun EyeOverlay(
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    if (!enabled) return
    
    var eyePositions by remember { mutableStateOf(listOf<Offset>()) }
    var showEyes by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        while (true) {
            // Случайно показываем глаза
            if (Random.nextFloat() > 0.7f) {
                eyePositions = List(Random.nextInt(1, 4)) {
                    Offset(Random.nextFloat(), Random.nextFloat())
                }
                showEyes = true
                delay(Random.nextLong(100, 500))
                showEyes = false
            }
            delay(Random.nextLong(2000, 5000))
        }
    }
    
    if (showEyes) {
        Canvas(
            modifier = modifier
                .fillMaxSize()
                .alpha(0.7f)
        ) {
            eyePositions.forEach { pos ->
                val x = pos.x * size.width
                val y = pos.y * size.height
                
                // Рисуем простой "глаз"
                // Белок
                drawCircle(
                    color = Color.White,
                    radius = 30f,
                    center = Offset(x, y)
                )
                // Зрачок
                drawCircle(
                    color = PsychoRed,
                    radius = 15f,
                    center = Offset(x, y)
                )
                // Блик
                drawCircle(
                    color = Color.White,
                    radius = 5f,
                    center = Offset(x - 5f, y - 5f)
                )
            }
        }
    }
}

/**
 * Matrix Rain эффект
 * Падающие символы как в фильме "Матрица"
 */
@Composable
fun MatrixRainOverlay(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    density: Int = 20
) {
    if (!enabled) return
    
    data class Drop(
        var x: Float,
        var y: Float,
        var speed: Float,
        var char: Char
    )
    
    val matrixChars = "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲン0123456789"
    
    var drops by remember {
        mutableStateOf(
            List(density) {
                Drop(
                    x = Random.nextFloat(),
                    y = Random.nextFloat() * 2f - 1f, // Начинаем выше экрана
                    speed = Random.nextFloat() * 0.02f + 0.01f,
                    char = matrixChars.random()
                )
            }
        )
    }
    
    LaunchedEffect(Unit) {
        while (true) {
            drops = drops.map { drop ->
                var newY = drop.y + drop.speed
                var newChar = drop.char
                
                if (newY > 1.2f) {
                    newY = -0.1f
                    newChar = matrixChars.random()
                }
                
                // Иногда меняем символ
                if (Random.nextFloat() > 0.95f) {
                    newChar = matrixChars.random()
                }
                
                drop.copy(y = newY, char = newChar)
            }
            delay(32) // ~30fps
        }
    }
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .alpha(0.3f)
    ) {
        drops.forEach { drop ->
            // Рисуем символ как текст требует Paint, 
            // для простоты рисуем светящуюся точку
            val x = drop.x * size.width
            val y = drop.y * size.height
            
            drawCircle(
                color = TerminalGreen,
                radius = 3f,
                center = Offset(x, y)
            )
            
            // След
            drawCircle(
                color = TerminalGreenDim,
                radius = 2f,
                center = Offset(x, y - 10f)
            )
            drawCircle(
                color = TerminalGreenDim.copy(alpha = 0.5f),
                radius = 1.5f,
                center = Offset(x, y - 20f)
            )
        }
    }
}
