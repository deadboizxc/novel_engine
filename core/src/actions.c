/**
 * @file actions.c
 * @brief Novel Engine — Action execution system
 */

#include "../include/internal.h"

/* ═══════════════════════════════════════════════════════════════════════════════
 * HELPER: Set or add variable
 * ═══════════════════════════════════════════════════════════════════════════════ */

static void set_var(NE_GameState* state, const char* name, int32_t value) {
    NE_KVInt* kv = ne_state_find_var(state, name);
    if (kv) {
        kv->value = value;
    } else if (state->var_count < NE_MAX_VARS) {
        ne_strncpy(state->vars[state->var_count].key, name, NE_KEY_LEN);
        state->vars[state->var_count].value = value;
        state->var_count++;
    }
}

static void set_flag(NE_GameState* state, const char* name, bool value) {
    NE_KVBool* kv = ne_state_find_flag(state, name);
    if (kv) {
        kv->value = value;
    } else if (state->flag_count < NE_MAX_FLAGS) {
        ne_strncpy(state->flags[state->flag_count].key, name, NE_KEY_LEN);
        state->flags[state->flag_count].value = value;
        state->flag_count++;
    }
}

static void add_item(NE_GameState* state, const char* item, int count) {
    NE_KVInt* kv = ne_state_find_item(state, item);
    if (kv) {
        kv->value += count;
        if (kv->value < 0) kv->value = 0;
    } else if (state->item_count < NE_MAX_ITEMS && count > 0) {
        ne_strncpy(state->items[state->item_count].key, item, NE_KEY_LEN);
        state->items[state->item_count].value = count;
        state->item_count++;
    }
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * EXECUTE SINGLE ACTION
 * ═══════════════════════════════════════════════════════════════════════════════ */

void ne_execute_action(NE_Engine* engine, const NE_Action* action) {
    if (!engine || !engine->state || !action) return;
    if (action->type == NE_ACTION_NONE) return;
    
    NE_GameState* state = engine->state;
    NE_Event event = {0};
    
    switch (action->type) {
        case NE_ACTION_JUMP: {
            char target[NE_KEY_LEN];
            ne_normalize_target(state->current, action->name, target, NE_KEY_LEN);
            
            event.type = NE_EVENT_SCENE_EXIT;
            event.scene_id = state->current;
            ne_emit_event(engine, &event);
            
            ne_strncpy(state->current, target, NE_KEY_LEN);
            
            /* Add to history */
            if (state->history_count < NE_MAX_HISTORY) {
                ne_strncpy(state->history[state->history_count++], target, NE_KEY_LEN);
            }
            break;
        }
        
        case NE_ACTION_SET_FLAG: {
            bool old_value = ne_state_get_flag(engine, action->name);
            set_flag(state, action->name, true);
            
            event.type = NE_EVENT_FLAG_CHANGED;
            event.name = action->name;
            event.bool_value = true;
            ne_emit_event(engine, &event);
            break;
        }
        
        case NE_ACTION_UNSET_FLAG: {
            set_flag(state, action->name, false);
            
            event.type = NE_EVENT_FLAG_CHANGED;
            event.name = action->name;
            event.bool_value = false;
            ne_emit_event(engine, &event);
            break;
        }
        
        case NE_ACTION_ADD_COIN: {
            int32_t old_coins = state->coins;
            state->coins += action->int_value;
            if (state->coins < 0) state->coins = 0;
            
            event.type = NE_EVENT_COINS_CHANGED;
            event.old_int_value = old_coins;
            event.int_value = state->coins;
            ne_emit_event(engine, &event);
            break;
        }
        
        case NE_ACTION_SET_VAR: {
            int32_t old_value = ne_state_get_var(engine, action->name);
            set_var(state, action->name, action->int_value);
            
            event.type = NE_EVENT_VAR_CHANGED;
            event.name = action->name;
            event.old_int_value = old_value;
            event.int_value = action->int_value;
            ne_emit_event(engine, &event);
            break;
        }
        
        case NE_ACTION_INCREMENT: {
            int32_t old_value = ne_state_get_var(engine, action->name);
            set_var(state, action->name, old_value + 1);
            
            event.type = NE_EVENT_VAR_CHANGED;
            event.name = action->name;
            event.old_int_value = old_value;
            event.int_value = old_value + 1;
            ne_emit_event(engine, &event);
            break;
        }
        
        case NE_ACTION_DECREMENT: {
            int32_t old_value = ne_state_get_var(engine, action->name);
            int32_t new_value = old_value > 0 ? old_value - 1 : 0;
            set_var(state, action->name, new_value);
            
            event.type = NE_EVENT_VAR_CHANGED;
            event.name = action->name;
            event.old_int_value = old_value;
            event.int_value = new_value;
            ne_emit_event(engine, &event);
            break;
        }
        
        case NE_ACTION_ADD_ITEM: {
            int old_count = ne_state_get_item(engine, action->name);
            add_item(state, action->name, action->int_value > 0 ? action->int_value : 1);
            
            event.type = NE_EVENT_ITEM_CHANGED;
            event.name = action->name;
            event.old_int_value = old_count;
            event.int_value = ne_state_get_item(engine, action->name);
            ne_emit_event(engine, &event);
            break;
        }
        
        case NE_ACTION_REMOVE_ITEM: {
            int old_count = ne_state_get_item(engine, action->name);
            add_item(state, action->name, -(action->int_value > 0 ? action->int_value : 1));
            
            event.type = NE_EVENT_ITEM_CHANGED;
            event.name = action->name;
            event.old_int_value = old_count;
            event.int_value = ne_state_get_item(engine, action->name);
            ne_emit_event(engine, &event);
            break;
        }
        
        case NE_ACTION_ANIMATE: {
            event.type = NE_EVENT_ANIMATE;
            event.anim_type = action->str_value;
            event.duration = action->float_value;
            ne_emit_event(engine, &event);
            break;
        }
        
        case NE_ACTION_SOUND: {
            event.type = NE_EVENT_SOUND;
            event.text = action->name;
            ne_emit_event(engine, &event);
            break;
        }
        
        case NE_ACTION_MUSIC: {
            event.type = NE_EVENT_MUSIC;
            event.text = action->name;
            ne_emit_event(engine, &event);
            break;
        }
        
        default:
            break;
    }
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * EXECUTE ALL ACTIONS
 * ═══════════════════════════════════════════════════════════════════════════════ */

void ne_execute_actions(NE_Engine* engine, const NE_Action* actions, int count) {
    if (!actions || count == 0) return;
    
    for (int i = 0; i < count; i++) {
        ne_execute_action(engine, &actions[i]);
    }
}
