package com.deadboizxc.tuinovel.engine

/**
 * Выполнение действий - аналог Python actions.py
 */
object ActionExecutor {
    
    data class ActionResult(
        val messages: List<String> = emptyList(),
        val sceneChanged: Boolean = false,
        val animations: List<AnimationRequest> = emptyList()
    )
    
    data class AnimationRequest(
        val type: String,
        val duration: Float,
        val text: String?
    )
    
    /**
     * Выполняет список действий и возвращает результат
     */
    fun execute(state: GameState, actions: List<GameAction>): ActionResult {
        val messages = mutableListOf<String>()
        var sceneChanged = false
        val animations = mutableListOf<AnimationRequest>()
        
        for (action in actions) {
            val result = executeSingle(state, action)
            messages.addAll(result.messages)
            if (result.sceneChanged) sceneChanged = true
            animations.addAll(result.animations)
        }
        
        return ActionResult(messages, sceneChanged, animations)
    }
    
    private fun executeSingle(state: GameState, action: GameAction): ActionResult {
        return when (action) {
            is GameAction.Jump -> {
                state.current = normalizeTarget(state, action.target)
                state.addToHistory(state.current)
                ActionResult(listOf("→ ПЕРЕХОД: ${state.current}"), sceneChanged = true)
            }
            
            is GameAction.SetFlag -> {
                state.setFlag(action.flag, true)
                ActionResult(listOf("✓ ФЛАГ: ${action.flag}"))
            }
            
            is GameAction.UnsetFlag -> {
                state.setFlag(action.flag, false)
                ActionResult(listOf("✗ ФЛАГ: ${action.flag}"))
            }
            
            is GameAction.AddCoin -> {
                val oldCoins = state.coins
                state.addCoins(action.amount)
                val sign = if (action.amount >= 0) "+" else ""
                ActionResult(listOf("💰 $sign${action.amount} монет → ${state.coins}"))
            }
            
            is GameAction.RemoveCoin -> {
                state.removeCoins(action.amount)
                ActionResult(listOf("💰 -${action.amount} монет → ${state.coins}"))
            }
            
            is GameAction.Increment -> {
                state.incrementVar(action.varName)
                ActionResult(listOf("📈 +1 ${action.varName} → ${state.getVar(action.varName)}"))
            }
            
            is GameAction.Decrement -> {
                state.decrementVar(action.varName)
                ActionResult(listOf("📉 -1 ${action.varName} → ${state.getVar(action.varName)}"))
            }
            
            is GameAction.SetVar -> {
                val msgs = action.vars.map { (k, v) ->
                    state.setVar(k, v)
                    "⚙️ $k = $v"
                }
                ActionResult(msgs)
            }
            
            is GameAction.AddItem -> {
                val msgs = action.items.map { (item, count) ->
                    state.addItem(item, count)
                    "🎁 +$count $item"
                }
                ActionResult(msgs)
            }
            
            is GameAction.RemoveItem -> {
                state.removeItem(action.item)
                ActionResult(listOf("🎁 -${action.item}"))
            }
            
            is GameAction.Animate -> {
                ActionResult(
                    animations = listOf(AnimationRequest(action.type, action.duration, action.text))
                )
            }
            
            is GameAction.None -> ActionResult()
        }
    }
    
    /**
     * Нормализует цель перехода (добавляет текущий prefix если нужно)
     */
    fun normalizeTarget(state: GameState, target: String): String {
        return if ("." in target) {
            target
        } else {
            val prefix = state.current.substringBefore(".")
            "$prefix.$target"
        }
    }
}
