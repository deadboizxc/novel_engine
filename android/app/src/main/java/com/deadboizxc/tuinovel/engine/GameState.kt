package com.deadboizxc.tuinovel.engine

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Состояние игры - полный аналог Python GameState
 */
@Serializable
data class GameState(
    var current: String = "prologue.start",
    var history: MutableList<String> = mutableListOf("prologue.start"),
    var vars: MutableMap<String, Int> = mutableMapOf(
        "pills_taken" to 0,
        "sanity" to 100,
        "loop_count" to 0
    ),
    var items: MutableMap<String, Int> = mutableMapOf(),
    var flags: MutableMap<String, Boolean> = mutableMapOf(),
    var stats: MutableMap<String, Int> = mutableMapOf("coins" to 100)
) {
    val sanity: Int get() = vars["sanity"] ?: 100
    val coins: Int get() = stats["coins"] ?: 100
    
    fun addToHistory(scene: String) {
        history.add(scene)
    }
    
    fun setFlag(flag: String, value: Boolean = true) {
        flags[flag] = value
    }
    
    fun hasFlag(flag: String): Boolean = flags[flag] == true
    
    fun hasItem(item: String): Boolean = (items[item] ?: 0) > 0
    
    fun addItem(item: String, count: Int = 1) {
        items[item] = (items[item] ?: 0) + count
    }
    
    fun removeItem(item: String, count: Int = 1) {
        val current = items[item] ?: 0
        val newCount = (current - count).coerceAtLeast(0)
        if (newCount == 0) {
            items.remove(item)
        } else {
            items[item] = newCount
        }
    }
    
    fun addCoins(amount: Int) {
        stats["coins"] = ((stats["coins"] ?: 0) + amount).coerceAtLeast(0)
    }
    
    fun removeCoins(amount: Int) {
        addCoins(-amount)
    }
    
    fun incrementVar(name: String) {
        vars[name] = (vars[name] ?: 0) + 1
    }
    
    fun decrementVar(name: String) {
        vars[name] = ((vars[name] ?: 0) - 1).coerceAtLeast(0)
    }
    
    fun setVar(name: String, value: Int) {
        vars[name] = value
    }
    
    fun getVar(name: String): Int = vars[name] ?: 0
    
    companion object {
        private val json = Json { 
            ignoreUnknownKeys = true 
            prettyPrint = true
        }
        
        fun save(state: GameState, file: File) {
            file.parentFile?.mkdirs()
            file.writeText(json.encodeToString(serializer(), state))
        }
        
        fun load(file: File): GameState? {
            return try {
                if (file.exists()) {
                    json.decodeFromString(serializer(), file.readText())
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
}
