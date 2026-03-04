package com.deadboizxc.tuinovel.engine

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * Менеджер сцен - аналог Python SceneManager
 * Основной контроллер игровой логики
 */
class SceneManager(private val context: Context) {
    
    private lateinit var story: Map<String, Scene>
    private var _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    // Текущая сцена
    private var _currentScene = MutableStateFlow<Scene?>(null)
    val currentScene: StateFlow<Scene?> = _currentScene.asStateFlow()
    
    // Доступные выборы
    private var _availableChoices = MutableStateFlow<List<Choice>>(emptyList())
    val availableChoices: StateFlow<List<Choice>> = _availableChoices.asStateFlow()
    
    // Сообщения от действий
    private var _actionMessages = MutableStateFlow<List<String>>(emptyList())
    val actionMessages: StateFlow<List<String>> = _actionMessages.asStateFlow()
    
    // Запросы анимаций
    private var _animationRequests = MutableStateFlow<List<ActionExecutor.AnimationRequest>>(emptyList())
    val animationRequests: StateFlow<List<ActionExecutor.AnimationRequest>> = _animationRequests.asStateFlow()
    
    // Статус ошибки
    private var _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Инициализация движка
     */
    fun initialize() {
        val loader = StoryLoader(context)
        story = loader.loadStory()
        
        if (story.isEmpty()) {
            _error.value = "Не удалось загрузить историю"
            return
        }
        
        processCurrentScene()
    }
    
    /**
     * Обработка текущей сцены
     */
    fun processCurrentScene(): SceneResult {
        val state = _gameState.value
        val scene = story[state.current]
        
        if (scene == null) {
            return handleMissingScene()
        }
        
        // Проверяем условия сцены
        if (!ConditionChecker.check(state, scene.conditions)) {
            return handleFailedConditions(scene)
        }
        
        _currentScene.value = scene
        
        // Выполняем действия сцены
        val actionResult = ActionExecutor.execute(state, scene.actions)
        _actionMessages.value = actionResult.messages
        _animationRequests.value = actionResult.animations
        
        if (actionResult.sceneChanged) {
            _gameState.value = state.copy()
            return SceneResult.SceneChanged
        }
        
        // Обновляем доступные выборы
        _availableChoices.value = getAvailableChoices(scene)
        _gameState.value = state.copy()
        
        return SceneResult.Continue
    }
    
    /**
     * Выполнение выбора игрока
     */
    fun executeChoice(choiceIndex: Int): SceneResult {
        val choices = _availableChoices.value
        if (choiceIndex !in choices.indices) {
            _error.value = "Неверный выбор"
            return SceneResult.Error
        }
        
        val choice = choices[choiceIndex]
        val state = _gameState.value
        
        // Выполняем действия выбора
        val actionResult = ActionExecutor.execute(state, choice.actions)
        _actionMessages.value = actionResult.messages
        _animationRequests.value = actionResult.animations
        
        // Переходим к следующей сцене
        val nextScene = choice.next ?: choice.jump
        if (nextScene != null) {
            state.current = ActionExecutor.normalizeTarget(state, nextScene)
            state.addToHistory(state.current)
            _gameState.value = state.copy()
            // Автосохранение после каждого выбора (слот 0)
            saveGame(slot = 0)
            return processCurrentScene()
        }
        
        if (actionResult.sceneChanged) {
            _gameState.value = state.copy()
            // Автосохранение после каждого выбора (слот 0)
            saveGame(slot = 0)
            return processCurrentScene()
        }
        
        _gameState.value = state.copy()
        return SceneResult.End
    }
    
    /**
     * Выполнение системной команды
     */
    fun executeCommand(command: String): Boolean {
        return when (command.lowercase()) {
            "s" -> { saveGame(); true }
            "l" -> { loadGame(); true }
            "q" -> false // Выход
            "v" -> { saveGame(); true }
            else -> true
        }
    }
    
    /**
     * Сохранение игры
     */
    fun saveGame(slot: Int = 1) {
        val saveDir = getSaveDir()
        val saveFile = File(saveDir, "$slot.json")
        GameState.save(_gameState.value, saveFile)
    }
    
    /**
     * Загрузка игры
     */
    fun loadGame(slot: Int = 1): Boolean {
        val saveDir = getSaveDir()
        val saveFile = File(saveDir, "$slot.json")
        val loaded = GameState.load(saveFile)
        
        return if (loaded != null) {
            _gameState.value = loaded
            processCurrentScene()
            true
        } else {
            _error.value = "Сохранение не найдено"
            false
        }
    }
    
    /**
     * Список сохранений
     */
    fun listSaves(): List<String> {
        val saveDir = getSaveDir()
        return if (saveDir.exists()) {
            saveDir.listFiles()?.filter { it.extension == "json" }?.map { it.name } ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    /**
     * Новая игра
     */
    fun newGame() {
        _gameState.value = GameState()
        _error.value = null
        processCurrentScene()
    }
    
    /**
     * Сброс игры (алиас для newGame)
     */
    fun resetGame() {
        newGame()
    }
    
    /**
     * Проверка наличия сохранения
     */
    fun hasSaveGame(slot: Int = 0): Boolean {
        val saveDir = getSaveDir()
        val saveFile = File(saveDir, "$slot.json")
        return saveFile.exists()
    }
    
    /**
     * Очистка ошибки
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Очистка анимаций
     */
    fun clearAnimations() {
        _animationRequests.value = emptyList()
    }
    
    // === Приватные методы ===
    
    private fun handleMissingScene(): SceneResult {
        val state = _gameState.value
        _error.value = "Сцена ${state.current} не найдена"
        
        // Пробуем перейти к .start
        val fallbackScene = state.current.substringBefore(".") + ".start"
        if (fallbackScene in story && fallbackScene != state.current) {
            state.current = fallbackScene
            _gameState.value = state.copy()
            return processCurrentScene()
        }
        
        return SceneResult.Error
    }
    
    private fun handleFailedConditions(scene: Scene): SceneResult {
        val state = _gameState.value
        
        if (scene.fallback != null) {
            state.current = ActionExecutor.normalizeTarget(state, scene.fallback)
            _gameState.value = state.copy()
            return processCurrentScene()
        }
        
        _error.value = "Условия не выполнены для ${state.current}"
        return SceneResult.Error
    }
    
    private fun getAvailableChoices(scene: Scene): List<Choice> {
        val state = _gameState.value
        return scene.choices.filter { choice ->
            ConditionChecker.check(state, choice.conditions)
        }
    }
    
    private fun getSaveDir(): File {
        // Используем внутреннее хранилище для надёжности
        return File(context.filesDir, "saves").also { it.mkdirs() }
    }
    
    companion object {
        /**
         * Статическая проверка наличия сохранения (для меню)
         */
        fun hasSaveFile(context: Context, slot: Int = 0): Boolean {
            val saveDir = File(context.filesDir, "saves")
            val saveFile = File(saveDir, "$slot.json")
            return saveFile.exists()
        }
        
        /**
         * Получить список сохранений
         */
        fun getSaveSlots(context: Context): List<Int> {
            val saveDir = File(context.filesDir, "saves")
            return if (saveDir.exists()) {
                saveDir.listFiles()
                    ?.filter { it.extension == "json" }
                    ?.mapNotNull { it.nameWithoutExtension.toIntOrNull() }
                    ?.filter { it > 0 }
                    ?.sorted()
                    ?: emptyList()
            } else {
                emptyList()
            }
        }
    }
}

/**
 * Результат обработки сцены
 */
enum class SceneResult {
    Continue,    // Сцена отображена, ждём выбора
    SceneChanged, // Переход на другую сцену
    End,         // Конец пути
    Error        // Ошибка
}
