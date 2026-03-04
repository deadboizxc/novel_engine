package com.novelengine.engine

/**
 * JNI-based implementation of NovelEngine for Desktop (JVM).
 *
 * This implementation uses Java Native Interface to communicate
 * with the C core library (libnovel_engine.so/.dll/.dylib).
 *
 * The native library must be loaded before using this class:
 * ```kotlin
 * System.loadLibrary("novel_engine_jni")
 * ```
 */
actual class NovelEngine actual constructor() {
    
    /** Pointer to native NE_Engine instance */
    private var nativePtr: Long = 0L
    
    /** Event listener for callbacks */
    private var eventListener: EngineEventListener? = null
    
    init {
        nativePtr = nativeCreate()
        if (nativePtr == 0L) {
            throw RuntimeException("Failed to create native NovelEngine instance")
        }
        // Register this instance for callbacks
        nativeSetCallbackHandler(nativePtr, this)
    }
    
    // ═══════════════════════════════════════════════════════════════
    // STORY LOADING
    // ═══════════════════════════════════════════════════════════════
    
    actual fun loadStory(json: String): Boolean {
        return nativeLoadStoryJson(nativePtr, json) == 0
    }
    
    actual fun loadStoryFromFile(path: String): Boolean {
        return nativeLoadStoryDir(nativePtr, path) == 0
    }
    
    // ═══════════════════════════════════════════════════════════════
    // GAME STATE
    // ═══════════════════════════════════════════════════════════════
    
    actual fun newGame(startScene: String) {
        nativeNewGame(nativePtr, startScene)
    }
    
    actual fun resetGame(startScene: String) {
        nativeResetGame(nativePtr, startScene)
    }
    
    actual fun enterScene(): Boolean {
        return nativeEnterScene(nativePtr) == 0
    }
    
    actual fun getCurrentSceneText(): String {
        return nativeGetSceneText(nativePtr) ?: ""
    }
    
    actual fun getCurrentBackground(): String {
        return nativeGetBackground(nativePtr) ?: ""
    }
    
    actual fun getChoices(): List<String> {
        val count = nativeGetChoiceCount(nativePtr)
        return (0 until count).mapNotNull { i ->
            nativeGetChoiceText(nativePtr, i)
        }
    }
    
    actual fun getChoiceCount(): Int {
        return nativeGetChoiceCount(nativePtr)
    }
    
    actual fun selectChoice(index: Int): Boolean {
        return nativeSelectChoice(nativePtr, index) == 0
    }
    
    actual fun isGameOver(): Boolean {
        return nativeIsFinal(nativePtr)
    }
    
    actual fun hasScene(sceneId: String): Boolean {
        return nativeHasScene(nativePtr, sceneId)
    }
    
    actual fun getCurrentSceneId(): String {
        return nativeGetCurrentScene(nativePtr) ?: ""
    }
    
    // ═══════════════════════════════════════════════════════════════
    // STATE GETTERS
    // ═══════════════════════════════════════════════════════════════
    
    actual fun getCoins(): Int {
        return nativeGetCoins(nativePtr)
    }
    
    actual fun getVar(name: String): Int {
        return nativeGetVar(nativePtr, name)
    }
    
    actual fun getFlag(name: String): Boolean {
        return nativeGetFlag(nativePtr, name)
    }
    
    actual fun hasItem(item: String): Boolean {
        return nativeHasItem(nativePtr, item)
    }
    
    actual fun getItemCount(item: String): Int {
        return nativeGetItemCount(nativePtr, item)
    }
    
    // ═══════════════════════════════════════════════════════════════
    // SAVE/LOAD
    // ═══════════════════════════════════════════════════════════════
    
    actual fun saveToJson(): String {
        return nativeSaveToJson(nativePtr) ?: "{}"
    }
    
    actual fun loadFromJson(json: String): Boolean {
        return nativeLoadFromJson(nativePtr, json) == 0
    }
    
    actual fun saveToFile(path: String): Boolean {
        return nativeSaveToFile(nativePtr, path) == 0
    }
    
    actual fun loadFromFile(path: String): Boolean {
        return nativeLoadFromFile(nativePtr, path) == 0
    }
    
    // ═══════════════════════════════════════════════════════════════
    // EVENTS
    // ═══════════════════════════════════════════════════════════════
    
    actual fun setEventListener(listener: EngineEventListener?) {
        eventListener = listener
    }
    
    // Called from JNI when events occur
    @Suppress("unused")
    private fun onNativeEvent(
        eventType: Int,
        sceneId: String?,
        text: String?,
        name: String?,
        intValue: Int,
        floatValue: Float,
        boolValue: Boolean
    ) {
        val listener = eventListener ?: return
        
        when (eventType) {
            EVENT_SCENE_ENTER -> sceneId?.let { listener.onSceneEnter(it) }
            EVENT_TEXT_DISPLAY -> text?.let { listener.onTextDisplay(it) }
            EVENT_CHOICES_READY -> listener.onChoicesReady(getChoices())
            EVENT_ANIMATE -> name?.let { listener.onAnimate(it, floatValue) }
            EVENT_SOUND_PLAY -> name?.let { listener.onSoundPlay(it) }
            EVENT_MUSIC_PLAY -> name?.let { listener.onMusicPlay(it) }
            EVENT_BACKGROUND -> name?.let { listener.onBackgroundChange(it) }
            EVENT_COINS_CHANGED -> listener.onCoinsChanged(intValue, getCoins())
            EVENT_FLAG_CHANGED -> name?.let { listener.onFlagChanged(it, boolValue) }
            EVENT_VAR_CHANGED -> name?.let { listener.onVarChanged(it, intValue, getVar(it)) }
            EVENT_ITEM_CHANGED -> name?.let { listener.onItemChanged(it, intValue) }
            EVENT_GAME_END -> sceneId?.let { listener.onGameEnd(it) }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // LIFECYCLE
    // ═══════════════════════════════════════════════════════════════
    
    actual fun close() {
        if (nativePtr != 0L) {
            nativeDestroy(nativePtr)
            nativePtr = 0L
        }
        eventListener = null
    }
    
    protected fun finalize() {
        close()
    }
    
    // ═══════════════════════════════════════════════════════════════
    // NATIVE METHODS (JNI)
    // ═══════════════════════════════════════════════════════════════
    
    private external fun nativeCreate(): Long
    private external fun nativeDestroy(ptr: Long)
    private external fun nativeSetCallbackHandler(ptr: Long, handler: Any)
    
    private external fun nativeLoadStoryJson(ptr: Long, json: String): Int
    private external fun nativeLoadStoryDir(ptr: Long, path: String): Int
    
    private external fun nativeNewGame(ptr: Long, startScene: String)
    private external fun nativeResetGame(ptr: Long, startScene: String)
    private external fun nativeEnterScene(ptr: Long): Int
    
    private external fun nativeGetSceneText(ptr: Long): String?
    private external fun nativeGetBackground(ptr: Long): String?
    private external fun nativeGetChoiceCount(ptr: Long): Int
    private external fun nativeGetChoiceText(ptr: Long, index: Int): String?
    private external fun nativeSelectChoice(ptr: Long, index: Int): Int
    private external fun nativeIsFinal(ptr: Long): Boolean
    private external fun nativeHasScene(ptr: Long, sceneId: String): Boolean
    private external fun nativeGetCurrentScene(ptr: Long): String?
    
    private external fun nativeGetCoins(ptr: Long): Int
    private external fun nativeGetVar(ptr: Long, name: String): Int
    private external fun nativeGetFlag(ptr: Long, name: String): Boolean
    private external fun nativeHasItem(ptr: Long, item: String): Boolean
    private external fun nativeGetItemCount(ptr: Long, item: String): Int
    
    private external fun nativeSaveToJson(ptr: Long): String?
    private external fun nativeLoadFromJson(ptr: Long, json: String): Int
    private external fun nativeSaveToFile(ptr: Long, path: String): Int
    private external fun nativeLoadFromFile(ptr: Long, path: String): Int
    
    companion object {
        // Event type constants (must match C enum)
        private const val EVENT_SCENE_ENTER = 0
        private const val EVENT_TEXT_DISPLAY = 2
        private const val EVENT_CHOICES_READY = 3
        private const val EVENT_ANIMATE = 10
        private const val EVENT_SOUND_PLAY = 11
        private const val EVENT_MUSIC_PLAY = 12
        private const val EVENT_BACKGROUND = 13
        private const val EVENT_COINS_CHANGED = 9
        private const val EVENT_FLAG_CHANGED = 5
        private const val EVENT_VAR_CHANGED = 6
        private const val EVENT_ITEM_CHANGED = 7
        private const val EVENT_GAME_END = 14
        
        init {
            try {
                System.loadLibrary("novel_engine_jni")
            } catch (e: UnsatisfiedLinkError) {
                System.err.println("Failed to load novel_engine_jni: ${e.message}")
                System.err.println("Make sure libnovel_engine_jni.so is in java.library.path")
            }
        }
    }
}
