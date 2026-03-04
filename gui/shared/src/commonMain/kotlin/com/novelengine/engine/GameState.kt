package com.novelengine.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents the current state of a novel game.
 *
 * This is a Kotlin-native representation synchronized with the C core state.
 * Use this for UI binding and state observation.
 */
data class GameState(
    /** Current scene ID */
    val currentSceneId: String = "",
    
    /** Current scene text (may contain markdown) */
    val sceneText: String = "",
    
    /** Current background image ID */
    val background: String = "",
    
    /** Available choices for current scene */
    val choices: List<Choice> = emptyList(),
    
    /** Current coins (sanity) value */
    val coins: Int = 100,
    
    /** Active flags */
    val flags: Map<String, Boolean> = emptyMap(),
    
    /** Variables */
    val variables: Map<String, Int> = emptyMap(),
    
    /** Inventory items with counts */
    val inventory: Map<String, Int> = emptyMap(),
    
    /** Whether the game has ended */
    val isGameOver: Boolean = false,
    
    /** Whether a scene is currently loading */
    val isLoading: Boolean = false,
    
    /** Current animation (null if none) */
    val currentAnimation: Animation? = null
)

/**
 * Represents a choice in a scene.
 */
data class Choice(
    /** Zero-based index */
    val index: Int,
    
    /** Display text for the choice */
    val text: String,
    
    /** Whether the choice is enabled (conditions met) */
    val enabled: Boolean = true,
    
    /** Optional tooltip explaining why disabled */
    val disabledReason: String? = null
)

/**
 * Represents an animation effect.
 */
data class Animation(
    /** Animation type (glitch, static, shake, etc.) */
    val type: String,
    
    /** Duration in seconds */
    val duration: Float,
    
    /** Start time in milliseconds */
    val startTimeMs: Long = System.currentTimeMillis()
) {
    /** Check if animation is still active */
    val isActive: Boolean
        get() = System.currentTimeMillis() - startTimeMs < (duration * 1000).toLong()
    
    /** Progress from 0.0 to 1.0 */
    val progress: Float
        get() {
            val elapsed = System.currentTimeMillis() - startTimeMs
            return (elapsed / (duration * 1000)).coerceIn(0f, 1f)
        }
}

/**
 * Observable game state holder that wraps NovelEngine.
 *
 * Provides StateFlow for reactive UI updates.
 *
 * Usage:
 * ```kotlin
 * val game = GameStateHolder()
 * game.loadStory("stories/blue_frequency")
 * game.newGame()
 *
 * // Observe in Compose:
 * val state by game.state.collectAsState()
 *
 * // Make choice:
 * game.selectChoice(0)
 * ```
 */
class GameStateHolder {
    private val engine: NovelEngine = NovelEngine()
    
    private val _state = MutableStateFlow(GameState())
    
    /** Observable game state */
    val state: StateFlow<GameState> = _state.asStateFlow()
    
    /** Current state snapshot */
    val currentState: GameState get() = _state.value
    
    init {
        engine.setEventListener(object : DefaultEngineEventListener() {
            override fun onSceneEnter(sceneId: String) {
                updateState { it.copy(currentSceneId = sceneId, isLoading = false) }
            }
            
            override fun onTextDisplay(text: String) {
                updateState { it.copy(sceneText = text) }
            }
            
            override fun onChoicesReady(choices: List<String>) {
                val choiceList = choices.mapIndexed { index, text ->
                    Choice(index = index, text = text)
                }
                updateState { it.copy(choices = choiceList) }
            }
            
            override fun onAnimate(type: String, duration: Float) {
                updateState { it.copy(currentAnimation = Animation(type, duration)) }
            }
            
            override fun onBackgroundChange(backgroundId: String) {
                updateState { it.copy(background = backgroundId) }
            }
            
            override fun onCoinsChanged(oldValue: Int, newValue: Int) {
                updateState { it.copy(coins = newValue) }
            }
            
            override fun onFlagChanged(name: String, value: Boolean) {
                updateState { state ->
                    val newFlags = state.flags.toMutableMap()
                    if (value) newFlags[name] = true else newFlags.remove(name)
                    state.copy(flags = newFlags)
                }
            }
            
            override fun onVarChanged(name: String, oldValue: Int, newValue: Int) {
                updateState { state ->
                    val newVars = state.variables.toMutableMap()
                    newVars[name] = newValue
                    state.copy(variables = newVars)
                }
            }
            
            override fun onItemChanged(item: String, count: Int) {
                updateState { state ->
                    val newInventory = state.inventory.toMutableMap()
                    if (count > 0) newInventory[item] = count else newInventory.remove(item)
                    state.copy(inventory = newInventory)
                }
            }
            
            override fun onGameEnd(sceneId: String) {
                updateState { it.copy(isGameOver = true) }
            }
        })
    }
    
    /**
     * Load story from file or directory.
     */
    fun loadStory(path: String): Boolean {
        return engine.loadStoryFromFile(path)
    }
    
    /**
     * Load story from JSON string.
     */
    fun loadStoryJson(json: String): Boolean {
        return engine.loadStory(json)
    }
    
    /**
     * Start a new game.
     * @param startScene Starting scene ID (uses manifest default if null)
     */
    fun newGame(startScene: String = "prologue.start") {
        _state.value = GameState(isLoading = true)
        engine.newGame(startScene)
        engine.enterScene()
    }
    
    /**
     * Reset the game to initial state.
     */
    fun resetGame(startScene: String = "prologue.start") {
        _state.value = GameState(isLoading = true)
        engine.resetGame(startScene)
        engine.enterScene()
    }
    
    /**
     * Select a choice by index.
     */
    fun selectChoice(index: Int): Boolean {
        updateState { it.copy(isLoading = true, choices = emptyList()) }
        val result = engine.selectChoice(index)
        if (result) {
            engine.enterScene()
        } else {
            updateState { it.copy(isLoading = false) }
        }
        return result
    }
    
    /**
     * Save game state to JSON string.
     */
    fun saveToJson(): String = engine.saveToJson()
    
    /**
     * Load game state from JSON string.
     */
    fun loadFromJson(json: String): Boolean {
        val result = engine.loadFromJson(json)
        if (result) {
            engine.enterScene()
        }
        return result
    }
    
    /**
     * Save game to file.
     */
    fun saveToFile(path: String): Boolean = engine.saveToFile(path)
    
    /**
     * Load game from file.
     */
    fun loadFromFile(path: String): Boolean {
        val result = engine.loadFromFile(path)
        if (result) {
            engine.enterScene()
        }
        return result
    }
    
    /**
     * Clear current animation.
     */
    fun clearAnimation() {
        updateState { it.copy(currentAnimation = null) }
    }
    
    /**
     * Release native resources.
     */
    fun close() {
        engine.close()
    }
    
    private inline fun updateState(transform: (GameState) -> GameState) {
        _state.value = transform(_state.value)
    }
}
