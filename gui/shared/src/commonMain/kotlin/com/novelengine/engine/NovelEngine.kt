package com.novelengine.engine

/**
 * NovelEngine — cross-platform interface to the C core library.
 *
 * This is the expect declaration. Actual implementations provide
 * platform-specific bindings (JNI for Desktop/Android, cinterop for iOS).
 *
 * Usage:
 * ```kotlin
 * val engine = NovelEngine()
 * engine.loadStoryFromFile("stories/blue_frequency")
 * engine.newGame("prologue.start")
 *
 * while (!engine.isGameOver()) {
 *     println(engine.getCurrentSceneText())
 *     val choices = engine.getChoices()
 *     choices.forEachIndexed { i, text -> println("$i: $text") }
 *     val selection = readLine()?.toIntOrNull() ?: 0
 *     engine.selectChoice(selection)
 * }
 *
 * engine.close()
 * ```
 */
expect class NovelEngine() {
    /**
     * Load story from JSON string.
     * @param json JSON content of the story
     * @return true if loaded successfully
     */
    fun loadStory(json: String): Boolean

    /**
     * Load story from file or directory.
     * If path is a directory, loads all JSON files from it.
     * @param path Path to story file or directory
     * @return true if loaded successfully
     */
    fun loadStoryFromFile(path: String): Boolean

    /**
     * Start a new game from the specified scene.
     * @param startScene Scene ID to start from (e.g., "prologue.start")
     */
    fun newGame(startScene: String)

    /**
     * Reset game state to the specified scene.
     * @param startScene Scene ID to reset to
     */
    fun resetGame(startScene: String)

    /**
     * Enter the current scene (triggers events).
     * @return true if scene was entered successfully
     */
    fun enterScene(): Boolean

    /**
     * Get the text of the current scene.
     * @return Scene text (may contain markdown formatting)
     */
    fun getCurrentSceneText(): String

    /**
     * Get the background image ID for current scene.
     * @return Background ID or empty string if none
     */
    fun getCurrentBackground(): String

    /**
     * Get available choices for the current scene.
     * Only returns choices whose conditions are met.
     * @return List of choice texts
     */
    fun getChoices(): List<String>

    /**
     * Get the number of available choices.
     * @return Number of choices (0 if none)
     */
    fun getChoiceCount(): Int

    /**
     * Select a choice by index.
     * @param index Zero-based choice index
     * @return true if choice was valid and executed
     */
    fun selectChoice(index: Int): Boolean

    /**
     * Check if the current scene is a final scene.
     * @return true if game has ended
     */
    fun isGameOver(): Boolean

    /**
     * Check if a scene exists in the story.
     * @param sceneId Scene ID to check
     * @return true if scene exists
     */
    fun hasScene(sceneId: String): Boolean

    /**
     * Get the current scene ID.
     * @return Current scene ID
     */
    fun getCurrentSceneId(): String

    // ═══════════════════════════════════════════════════════════════
    // STATE GETTERS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get current coins (sanity) value.
     * @return Coins value (0-200)
     */
    fun getCoins(): Int

    /**
     * Get a variable value by name.
     * @param name Variable name
     * @return Variable value (0 if not set)
     */
    fun getVar(name: String): Int

    /**
     * Get a flag value by name.
     * @param name Flag name
     * @return Flag value (false if not set)
     */
    fun getFlag(name: String): Boolean

    /**
     * Check if an item is in inventory.
     * @param item Item name
     * @return true if item is present
     */
    fun hasItem(item: String): Boolean

    /**
     * Get the count of a specific item.
     * @param item Item name
     * @return Item count (0 if not present)
     */
    fun getItemCount(item: String): Int

    // ═══════════════════════════════════════════════════════════════
    // SAVE/LOAD
    // ═══════════════════════════════════════════════════════════════

    /**
     * Serialize current game state to JSON.
     * @return JSON string representing the current state
     */
    fun saveToJson(): String

    /**
     * Load game state from JSON.
     * @param json JSON string from saveToJson()
     * @return true if loaded successfully
     */
    fun loadFromJson(json: String): Boolean

    /**
     * Save game state to a file.
     * @param path File path to save to
     * @return true if saved successfully
     */
    fun saveToFile(path: String): Boolean

    /**
     * Load game state from a file.
     * @param path File path to load from
     * @return true if loaded successfully
     */
    fun loadFromFile(path: String): Boolean

    // ═══════════════════════════════════════════════════════════════
    // EVENTS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Set the event listener for engine events.
     * @param listener Listener to receive events, or null to remove
     */
    fun setEventListener(listener: EngineEventListener?)

    // ═══════════════════════════════════════════════════════════════
    // LIFECYCLE
    // ═══════════════════════════════════════════════════════════════

    /**
     * Release native resources.
     * Must be called when the engine is no longer needed.
     */
    fun close()
}

/**
 * Event listener interface for engine events.
 *
 * The engine fires events during gameplay to notify the UI of changes.
 * Implement this interface to handle animations, sounds, and state updates.
 */
interface EngineEventListener {
    /**
     * Called when entering a new scene.
     * @param sceneId The ID of the scene being entered
     */
    fun onSceneEnter(sceneId: String)

    /**
     * Called when text should be displayed.
     * @param text The text to display
     */
    fun onTextDisplay(text: String)

    /**
     * Called when choices are ready to be shown.
     * @param choices List of choice texts
     */
    fun onChoicesReady(choices: List<String>)

    /**
     * Called when an animation should play.
     * @param type Animation type ("glitch", "static", "shake", etc.)
     * @param duration Animation duration in seconds
     */
    fun onAnimate(type: String, duration: Float)

    /**
     * Called when a sound should play.
     * @param soundId Sound identifier
     */
    fun onSoundPlay(soundId: String)

    /**
     * Called when music should change.
     * @param musicId Music identifier (empty to stop)
     */
    fun onMusicPlay(musicId: String)

    /**
     * Called when background should change.
     * @param backgroundId Background identifier
     */
    fun onBackgroundChange(backgroundId: String)

    /**
     * Called when coins (sanity) value changes.
     * @param oldValue Previous value
     * @param newValue New value
     */
    fun onCoinsChanged(oldValue: Int, newValue: Int)

    /**
     * Called when a flag changes.
     * @param name Flag name
     * @param value New value
     */
    fun onFlagChanged(name: String, value: Boolean)

    /**
     * Called when a variable changes.
     * @param name Variable name
     * @param oldValue Previous value
     * @param newValue New value
     */
    fun onVarChanged(name: String, oldValue: Int, newValue: Int)

    /**
     * Called when an item is added or removed.
     * @param item Item name
     * @param count New count (0 if removed)
     */
    fun onItemChanged(item: String, count: Int)

    /**
     * Called when the game ends (final scene reached).
     * @param sceneId The final scene ID
     */
    fun onGameEnd(sceneId: String)
}

/**
 * Default implementation of EngineEventListener with empty methods.
 * Extend this to only override the events you care about.
 */
open class DefaultEngineEventListener : EngineEventListener {
    override fun onSceneEnter(sceneId: String) {}
    override fun onTextDisplay(text: String) {}
    override fun onChoicesReady(choices: List<String>) {}
    override fun onAnimate(type: String, duration: Float) {}
    override fun onSoundPlay(soundId: String) {}
    override fun onMusicPlay(musicId: String) {}
    override fun onBackgroundChange(backgroundId: String) {}
    override fun onCoinsChanged(oldValue: Int, newValue: Int) {}
    override fun onFlagChanged(name: String, value: Boolean) {}
    override fun onVarChanged(name: String, oldValue: Int, newValue: Int) {}
    override fun onItemChanged(item: String, count: Int) {}
    override fun onGameEnd(sceneId: String) {}
}
