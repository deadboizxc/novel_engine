package com.deadboizxc.tuinovel.engine

/**
 * Native Engine - JNI bridge to C core
 * Replaces pure Kotlin engine with native implementation
 */
class NativeEngine {
    
    companion object {
        init {
            System.loadLibrary("novel_engine_jni")
        }
    }
    
    // ========================================================================
    // Lifecycle
    // ========================================================================
    
    external fun init(): Boolean
    external fun destroy()
    
    // ========================================================================
    // Story
    // ========================================================================
    
    external fun loadStory(jsonData: String): Boolean
    
    // ========================================================================
    // Game state
    // ========================================================================
    
    external fun getCurrentScene(): String?
    external fun startGame(startScene: String): Boolean
    external fun reset(startScene: String): Boolean
    
    // ========================================================================
    // Variables
    // ========================================================================
    
    external fun getVar(name: String): Int
    external fun getCoins(): Int
    
    // ========================================================================
    // Flags
    // ========================================================================
    
    external fun getFlag(name: String): Boolean
    
    // ========================================================================
    // Items
    // ========================================================================
    
    external fun getItem(name: String): Int
    external fun hasItem(name: String): Boolean
    
    // ========================================================================
    // Scene data
    // ========================================================================
    
    external fun enterScene(): Boolean
    external fun getSceneText(): String?
    external fun getSceneBackground(): String?
    external fun getChoiceCount(): Int
    external fun getChoiceText(index: Int): String?
    external fun selectChoice(index: Int): Boolean
    external fun isFinalScene(): Boolean
    
    // ========================================================================
    // Save/Load
    // ========================================================================
    
    external fun saveState(): String?
    external fun loadState(jsonData: String): Boolean
    
    // ========================================================================
    // Info
    // ========================================================================
    
    external fun getVersion(): String?
}
