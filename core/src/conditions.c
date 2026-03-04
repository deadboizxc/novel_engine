/**
 * @file conditions.c
 * @brief Novel Engine — Condition checking system
 */

#include "../include/internal.h"

/* ═══════════════════════════════════════════════════════════════════════════════
 * SINGLE CONDITION CHECK
 * ═══════════════════════════════════════════════════════════════════════════════ */

bool ne_check_condition(const NE_GameState* state, const NE_Condition* cond) {
    if (!state || !cond) return true;
    if (cond->type == NE_COND_NONE) return true;
    
    switch (cond->type) {
        case NE_COND_FLAG: {
            for (int i = 0; i < state->flag_count; i++) {
                if (strcmp(state->flags[i].key, cond->name) == 0) {
                    return state->flags[i].value;
                }
            }
            return false;  /* Flag not found = false */
        }
        
        case NE_COND_NO_FLAG: {
            for (int i = 0; i < state->flag_count; i++) {
                if (strcmp(state->flags[i].key, cond->name) == 0) {
                    return !state->flags[i].value;
                }
            }
            return true;  /* Flag not found = true (no flag) */
        }
        
        case NE_COND_HAS_ITEM: {
            for (int i = 0; i < state->item_count; i++) {
                if (strcmp(state->items[i].key, cond->name) == 0) {
                    return state->items[i].value > 0;
                }
            }
            return false;
        }
        
        case NE_COND_NO_ITEM: {
            for (int i = 0; i < state->item_count; i++) {
                if (strcmp(state->items[i].key, cond->name) == 0) {
                    return state->items[i].value <= 0;
                }
            }
            return true;
        }
        
        case NE_COND_COINS: {
            return state->coins >= cond->value;
        }
        
        case NE_COND_VAR_EQ: {
            for (int i = 0; i < state->var_count; i++) {
                if (strcmp(state->vars[i].key, cond->name) == 0) {
                    return state->vars[i].value == cond->value;
                }
            }
            return cond->value == 0;  /* Undefined var == 0 */
        }
        
        case NE_COND_VAR_GT: {
            for (int i = 0; i < state->var_count; i++) {
                if (strcmp(state->vars[i].key, cond->name) == 0) {
                    return state->vars[i].value > cond->value;
                }
            }
            return 0 > cond->value;
        }
        
        case NE_COND_VAR_LT: {
            for (int i = 0; i < state->var_count; i++) {
                if (strcmp(state->vars[i].key, cond->name) == 0) {
                    return state->vars[i].value < cond->value;
                }
            }
            return 0 < cond->value;
        }
        
        case NE_COND_VAR_NE: {
            for (int i = 0; i < state->var_count; i++) {
                if (strcmp(state->vars[i].key, cond->name) == 0) {
                    return state->vars[i].value != cond->value;
                }
            }
            return cond->value != 0;
        }
        
        default:
            return true;
    }
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * CHECK ALL CONDITIONS (AND logic)
 * ═══════════════════════════════════════════════════════════════════════════════ */

bool ne_check_conditions(const NE_GameState* state, const NE_Condition* conds, int count) {
    if (!conds || count == 0) return true;
    
    for (int i = 0; i < count; i++) {
        if (!ne_check_condition(state, &conds[i])) {
            return false;
        }
    }
    
    return true;
}
