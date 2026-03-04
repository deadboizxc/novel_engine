package com.deadboizxc.tuinovel.engine

/**
 * Проверка условий - аналог Python conditions.py
 */
object ConditionChecker {
    
    /**
     * Проверяет список условий
     */
    fun check(state: GameState, conditions: List<Condition>): Boolean {
        if (conditions.isEmpty()) return true
        return conditions.all { checkSingle(state, it) }
    }
    
    /**
     * Проверяет одно условие
     */
    private fun checkSingle(state: GameState, condition: Condition): Boolean {
        // Проверка флага
        condition.flag?.let { flag ->
            if (!state.hasFlag(flag)) return false
        }
        
        // Проверка предмета
        condition.has?.let { item ->
            if (!state.hasItem(item)) return false
        }
        
        // Проверка переменных
        condition.varName?.let { varName ->
            val value = state.getVar(varName)
            
            condition.more?.let { if (value <= it) return false }
            condition.less?.let { if (value >= it) return false }
            condition.equal?.let { if (value != it) return false }
            condition.notEqual?.let { if (value == it) return false }
        }
        
        // Проверка монет
        condition.coins?.let { required ->
            if (state.coins < required) return false
        }
        
        return true
    }
}
