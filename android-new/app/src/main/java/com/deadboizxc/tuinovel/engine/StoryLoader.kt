package com.deadboizxc.tuinovel.engine

import android.content.Context
import org.yaml.snakeyaml.Yaml
import org.json.JSONObject
import java.io.InputStream

/**
 * Загрузчик историй из YAML и JSON файлов
 */
class StoryLoader(private val context: Context) {
    
    private val yaml = Yaml()
    
    /**
     * Загружает все истории из assets/story/
     */
    fun loadStory(): Map<String, Scene> {
        val story = mutableMapOf<String, Scene>()
        
        try {
            val storyFiles = context.assets.list("story") ?: emptyArray()
            
            for (fileName in storyFiles) {
                when {
                    fileName.endsWith(".yml") || fileName.endsWith(".yaml") -> {
                        val prefix = fileName.substringBeforeLast(".")
                        loadYamlFile("story/$fileName", prefix, story)
                    }
                    fileName.endsWith(".json") && fileName != "manifest.json" -> {
                        loadJsonFile("story/$fileName", story)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return story
    }
    
    private fun loadYamlFile(path: String, prefix: String, story: MutableMap<String, Scene>) {
        try {
            val inputStream: InputStream = context.assets.open(path)
            val data: Map<String, Any>? = yaml.load(inputStream)
            inputStream.close()
            
            data?.forEach { (key, value) ->
                val fullKey = "$prefix.$key"
                @Suppress("UNCHECKED_CAST")
                val sceneData = value as? Map<String, Any> ?: return@forEach
                story[fullKey] = parseScene(sceneData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun loadJsonFile(path: String, story: MutableMap<String, Scene>) {
        try {
            val inputStream: InputStream = context.assets.open(path)
            val jsonStr = inputStream.bufferedReader().readText()
            inputStream.close()
            
            val jsonObj = JSONObject(jsonStr)
            val keys = jsonObj.keys()
            
            while (keys.hasNext()) {
                val sceneId = keys.next()
                val sceneJson = jsonObj.getJSONObject(sceneId)
                story[sceneId] = parseJsonScene(sceneJson)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun parseJsonScene(json: JSONObject): Scene {
        val text = json.optString("text", "")
        val bg = json.optString("bg", null)
        
        val choices = mutableListOf<Choice>()
        val choicesArr = json.optJSONArray("choices")
        if (choicesArr != null) {
            for (i in 0 until choicesArr.length()) {
                val choiceJson = choicesArr.getJSONObject(i)
                choices.add(Choice(
                    text = choiceJson.optString("text", ""),
                    next = choiceJson.optString("next", null),
                    jump = choiceJson.optString("jump", null)
                ))
            }
        }
        
        val actions = mutableListOf<GameAction>()
        val actionsArr = json.optJSONArray("actions")
        if (actionsArr != null) {
            for (i in 0 until actionsArr.length()) {
                val actionJson = actionsArr.getJSONObject(i)
                actions.add(parseJsonAction(actionJson))
            }
        }
        
        return Scene(
            text = text,
            bg = bg,
            choices = choices,
            actions = actions
        )
    }
    
    private fun parseJsonAction(json: JSONObject): GameAction {
        return when {
            json.has("jump") -> GameAction.Jump(json.getString("jump"))
            json.has("set_flag") -> GameAction.SetFlag(json.getString("set_flag"))
            json.has("unset_flag") -> GameAction.UnsetFlag(json.getString("unset_flag"))
            json.has("add_coin") -> GameAction.AddCoin(json.getInt("add_coin"))
            json.has("remove_coin") -> GameAction.RemoveCoin(json.getInt("remove_coin"))
            json.has("increment") -> GameAction.Increment(json.getString("increment"))
            json.has("decrement") -> GameAction.Decrement(json.getString("decrement"))
            json.has("add_item") -> {
                val item = json.get("add_item")
                when (item) {
                    is String -> GameAction.AddItem(mapOf(item to 1))
                    else -> GameAction.AddItem(mapOf())
                }
            }
            json.has("remove_item") -> GameAction.RemoveItem(json.getString("remove_item"))
            json.has("animate") -> {
                val animJson = json.getJSONObject("animate")
                GameAction.Animate(
                    type = animJson.optString("type", ""),
                    duration = animJson.optDouble("duration", 1.0).toFloat(),
                    text = animJson.optString("text", null)
                )
            }
            else -> GameAction.None
        }
    }
    
    private fun loadStoryFile(path: String, prefix: String, story: MutableMap<String, Scene>) {
        loadYamlFile(path, prefix, story)
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun parseScene(data: Map<String, Any>): Scene {
        val text = data["text"] as? String ?: ""
        val bg = data["bg"] as? String
        val choices = (data["choices"] as? List<Map<String, Any>>)?.map { parseChoice(it) } ?: emptyList()
        val actions = (data["actions"] as? List<Map<String, Any>>)?.map { parseAction(it) } ?: emptyList()
        val conditions = parseConditions(data["conditions"])
        val fallback = data["fallback"] as? String
        
        return Scene(
            text = text,
            bg = bg,
            choices = choices,
            actions = actions,
            conditions = conditions,
            fallback = fallback
        )
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun parseChoice(data: Map<String, Any>): Choice {
        return Choice(
            text = data["text"] as? String ?: "",
            next = data["next"] as? String,
            jump = data["jump"] as? String,
            actions = (data["actions"] as? List<Map<String, Any>>)?.map { parseAction(it) } ?: emptyList(),
            conditions = parseConditions(data["condition"] ?: data["conditions"])
        )
    }
    
    private fun parseAction(data: Map<String, Any>): GameAction {
        return when {
            "jump" in data -> GameAction.Jump(data["jump"] as String)
            "set_flag" in data -> GameAction.SetFlag(data["set_flag"] as String)
            "unset_flag" in data -> GameAction.UnsetFlag(data["unset_flag"] as String)
            "add_coin" in data -> GameAction.AddCoin((data["add_coin"] as Number).toInt())
            "remove_coin" in data -> GameAction.RemoveCoin((data["remove_coin"] as Number).toInt())
            "increment" in data -> GameAction.Increment(data["increment"] as String)
            "decrement" in data -> GameAction.Decrement(data["decrement"] as String)
            "set_var" in data -> {
                @Suppress("UNCHECKED_CAST")
                val vars = data["set_var"] as Map<String, Number>
                GameAction.SetVar(vars.mapValues { it.value.toInt() })
            }
            "add_item" in data -> {
                when (val item = data["add_item"]) {
                    is String -> GameAction.AddItem(mapOf(item to 1))
                    is Map<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        GameAction.AddItem((item as Map<String, Number>).mapValues { it.value.toInt() })
                    }
                    is List<*> -> GameAction.AddItem(item.filterIsInstance<String>().associateWith { 1 })
                    else -> GameAction.None
                }
            }
            "remove_item" in data -> GameAction.RemoveItem(data["remove_item"] as String)
            "animate" in data -> {
                @Suppress("UNCHECKED_CAST")
                val animData = data["animate"] as? Map<String, Any> ?: emptyMap()
                GameAction.Animate(
                    type = animData["type"] as? String ?: "",
                    duration = (animData["duration"] as? Number)?.toFloat() ?: 1f,
                    text = animData["text"] as? String
                )
            }
            else -> GameAction.None
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun parseConditions(data: Any?): List<Condition> {
        if (data == null) return emptyList()
        
        return when (data) {
            is List<*> -> data.filterIsInstance<Map<String, Any>>().map { parseCondition(it) }
            is Map<*, *> -> listOf(parseCondition(data as Map<String, Any>))
            else -> emptyList()
        }
    }
    
    private fun parseCondition(data: Map<String, Any>): Condition {
        return Condition(
            flag = data["flag"] as? String,
            has = data["has"] as? String,
            varName = data["var"] as? String,
            more = (data["more"] as? Number)?.toInt(),
            less = (data["less"] as? Number)?.toInt(),
            equal = (data["equal"] as? Number)?.toInt(),
            notEqual = (data["not"] as? Number)?.toInt(),
            coins = (data["coins"] as? Number)?.toInt()
        )
    }
}

/**
 * Структура сцены
 */
data class Scene(
    val text: String,
    val bg: String? = null,
    val choices: List<Choice> = emptyList(),
    val actions: List<GameAction> = emptyList(),
    val conditions: List<Condition> = emptyList(),
    val fallback: String? = null
)

/**
 * Вариант выбора
 */
data class Choice(
    val text: String,
    val next: String? = null,
    val jump: String? = null,
    val actions: List<GameAction> = emptyList(),
    val conditions: List<Condition> = emptyList()
)

/**
 * Условие для проверки
 */
data class Condition(
    val flag: String? = null,
    val has: String? = null,
    val varName: String? = null,
    val more: Int? = null,
    val less: Int? = null,
    val equal: Int? = null,
    val notEqual: Int? = null,
    val coins: Int? = null
)

/**
 * Игровые действия
 */
sealed class GameAction {
    data class Jump(val target: String) : GameAction()
    data class SetFlag(val flag: String) : GameAction()
    data class UnsetFlag(val flag: String) : GameAction()
    data class AddCoin(val amount: Int) : GameAction()
    data class RemoveCoin(val amount: Int) : GameAction()
    data class Increment(val varName: String) : GameAction()
    data class Decrement(val varName: String) : GameAction()
    data class SetVar(val vars: Map<String, Int>) : GameAction()
    data class AddItem(val items: Map<String, Int>) : GameAction()
    data class RemoveItem(val item: String) : GameAction()
    data class Animate(val type: String, val duration: Float, val text: String?) : GameAction()
    data object None : GameAction()
}
