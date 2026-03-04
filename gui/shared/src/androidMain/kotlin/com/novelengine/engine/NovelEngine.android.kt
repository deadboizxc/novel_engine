package com.novelengine.engine

/**
 * Android JNI implementation of NovelEngine.
 *
 * Uses System.loadLibrary to load the native library from jniLibs.
 */
actual class NovelEngine actual constructor() {
    
    private var nativePtr: Long = 0L
    private var eventListener: EngineEventListener? = null
    
    init {
        nativePtr = nativeCreate()
        if (nativePtr == 0L) {
            throw RuntimeException("Failed to create native NovelEngine instance")
        }
        nativeSetCallbackHandler(nativePtr, this)
    }
    
    actual fun loadStory(json: String): Boolean {
        return nativeLoadStoryJson(nativePtr, json) == 0
    }
    
    actual fun loadStoryFromFile(path: String): Boolean {
        return nativeLoadStoryDir(nativePtr, path) == 0
    }
    
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
        return (0 until count).mapNotNull { nativeGetChoiceText(nativePtr, it) }
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
    
    actual fun setEventListener(listener: EngineEventListener?) {
        eventListener = listener
    }
    
    @Suppress("unused")
    private fun onNativeEvent(
        eventType: Int, sceneId: String?, text: String?, name: String?,
        intValue: Int, floatValue: Float, boolValue: Boolean
    ) {
        val listener = eventListener ?: return
        when (eventType) {
            0 -> sceneId?.let { listener.onSceneEnter(it) }
            2 -> text?.let { listener.onTextDisplay(it) }
            3 -> listener.onChoicesReady(getChoices())
            10 -> name?.let { listener.onAnimate(it, floatValue) }
            11 -> name?.let { listener.onSoundPlay(it) }
            12 -> name?.let { listener.onMusicPlay(it) }
            13 -> name?.let { listener.onBackgroundChange(it) }
            9 -> listener.onCoinsChanged(intValue, getCoins())
            5 -> name?.let { listener.onFlagChanged(it, boolValue) }
            6 -> name?.let { listener.onVarChanged(it, intValue, getVar(it)) }
            7 -> name?.let { listener.onItemChanged(it, intValue) }
            14 -> sceneId?.let { listener.onGameEnd(it) }
        }
    }
    
    actual fun close() {
        if (nativePtr != 0L) {
            nativeDestroy(nativePtr)
            nativePtr = 0L
        }
        eventListener = null
    }
    
    protected fun finalize() { close() }
    
    // Native methods
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
        init {
            try {
                System.loadLibrary("novel_engine_jni")
            } catch (e: UnsatisfiedLinkError) {
                android.util.Log.e("NovelEngine", "Failed to load native library: ${e.message}")
            }
        }
    }
}
