/**
 * @file state.c
 * @brief Novel Engine — Game state management
 */

#include "../include/internal.h"

/* ═══════════════════════════════════════════════════════════════════════════════
 * STATE CREATION
 * ═══════════════════════════════════════════════════════════════════════════════ */

static NE_GameState* create_state(const char* start_scene) {
    NE_GameState* state = (NE_GameState*)calloc(1, sizeof(NE_GameState));
    if (!state) return NULL;
    
    ne_strncpy(state->current, start_scene, NE_KEY_LEN);
    state->history_count = 0;
    state->coins = 100;  /* Starting coins */
    state->flag_count = 0;
    state->var_count = 0;
    state->item_count = 0;
    
    /* Add initial history entry */
    ne_strncpy(state->history[0], start_scene, NE_KEY_LEN);
    state->history_count = 1;
    
    return state;
}

NE_API NE_Result ne_game_new(NE_Engine* engine, const char* start_scene) {
    if (!engine) return NE_ERROR_NULL_PTR;
    if (!engine->story || engine->story->scene_count == 0) return NE_ERROR_NO_STORY;
    if (!start_scene) return NE_ERROR_NULL_PTR;
    
    /* Check if start scene exists */
    if (!ne_story_find_scene(engine->story, start_scene)) {
        return NE_ERROR_SCENE_NOT_FOUND;
    }
    
    /* Free existing state */
    if (engine->state) {
        free(engine->state);
    }
    
    engine->state = create_state(start_scene);
    if (!engine->state) return NE_ERROR_OUT_OF_MEMORY;
    
    engine->filtered_count = 0;
    
    return NE_OK;
}

NE_API NE_Result ne_game_reset(NE_Engine* engine, const char* start_scene) {
    return ne_game_new(engine, start_scene);
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * STATE GETTERS
 * ═══════════════════════════════════════════════════════════════════════════════ */

NE_API const char* ne_state_current_scene(const NE_Engine* engine) {
    if (!engine || !engine->state) return NULL;
    return engine->state->current;
}

NE_API int ne_state_get_coins(const NE_Engine* engine) {
    if (!engine || !engine->state) return 0;
    return engine->state->coins;
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * INTERNAL: Find helpers
 * ═══════════════════════════════════════════════════════════════════════════════ */

NE_KVInt* ne_state_find_var(NE_GameState* state, const char* name) {
    if (!state || !name) return NULL;
    for (int i = 0; i < state->var_count; i++) {
        if (strcmp(state->vars[i].key, name) == 0) {
            return &state->vars[i];
        }
    }
    return NULL;
}

NE_KVBool* ne_state_find_flag(NE_GameState* state, const char* name) {
    if (!state || !name) return NULL;
    for (int i = 0; i < state->flag_count; i++) {
        if (strcmp(state->flags[i].key, name) == 0) {
            return &state->flags[i];
        }
    }
    return NULL;
}

NE_KVInt* ne_state_find_item(NE_GameState* state, const char* item) {
    if (!state || !item) return NULL;
    for (int i = 0; i < state->item_count; i++) {
        if (strcmp(state->items[i].key, item) == 0) {
            return &state->items[i];
        }
    }
    return NULL;
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * STATE GETTERS (continued)
 * ═══════════════════════════════════════════════════════════════════════════════ */

NE_API int ne_state_get_var(const NE_Engine* engine, const char* name) {
    if (!engine || !engine->state) return 0;
    NE_KVInt* kv = ne_state_find_var((NE_GameState*)engine->state, name);
    return kv ? kv->value : 0;
}

NE_API bool ne_state_get_flag(const NE_Engine* engine, const char* name) {
    if (!engine || !engine->state) return false;
    NE_KVBool* kv = ne_state_find_flag((NE_GameState*)engine->state, name);
    return kv ? kv->value : false;
}

NE_API int ne_state_get_item(const NE_Engine* engine, const char* item) {
    if (!engine || !engine->state) return 0;
    NE_KVInt* kv = ne_state_find_item((NE_GameState*)engine->state, item);
    return kv ? kv->value : 0;
}

NE_API bool ne_state_has_item(const NE_Engine* engine, const char* item) {
    return ne_state_get_item(engine, item) > 0;
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * STATE SERIALIZATION
 * ═══════════════════════════════════════════════════════════════════════════════ */

NE_API char* ne_state_to_json(const NE_Engine* engine) {
    if (!engine || !engine->state) return NULL;
    
    const NE_GameState* s = engine->state;
    
    cJSON* root = cJSON_CreateObject();
    if (!root) return NULL;
    
    cJSON_AddStringToObject(root, "current", s->current);
    cJSON_AddNumberToObject(root, "coins", s->coins);
    
    /* History */
    cJSON* history = cJSON_CreateArray();
    for (int i = 0; i < s->history_count; i++) {
        cJSON_AddItemToArray(history, cJSON_CreateString(s->history[i]));
    }
    cJSON_AddItemToObject(root, "history", history);
    
    /* Flags */
    cJSON* flags = cJSON_CreateObject();
    for (int i = 0; i < s->flag_count; i++) {
        cJSON_AddBoolToObject(flags, s->flags[i].key, s->flags[i].value);
    }
    cJSON_AddItemToObject(root, "flags", flags);
    
    /* Vars */
    cJSON* vars = cJSON_CreateObject();
    for (int i = 0; i < s->var_count; i++) {
        cJSON_AddNumberToObject(vars, s->vars[i].key, s->vars[i].value);
    }
    cJSON_AddItemToObject(root, "vars", vars);
    
    /* Items */
    cJSON* items = cJSON_CreateObject();
    for (int i = 0; i < s->item_count; i++) {
        cJSON_AddNumberToObject(items, s->items[i].key, s->items[i].value);
    }
    cJSON_AddItemToObject(root, "items", items);
    
    char* json = cJSON_PrintUnformatted(root);
    cJSON_Delete(root);
    
    return json;
}

NE_API NE_Result ne_state_from_json(NE_Engine* engine, const char* json_str) {
    if (!engine || !json_str) return NE_ERROR_NULL_PTR;
    
    cJSON* root = cJSON_Parse(json_str);
    if (!root) return NE_ERROR_INVALID_JSON;
    
    /* Create new state */
    if (engine->state) free(engine->state);
    engine->state = (NE_GameState*)calloc(1, sizeof(NE_GameState));
    if (!engine->state) {
        cJSON_Delete(root);
        return NE_ERROR_OUT_OF_MEMORY;
    }
    
    NE_GameState* s = engine->state;
    
    /* Parse current */
    cJSON* current = cJSON_GetObjectItem(root, "current");
    if (cJSON_IsString(current)) {
        ne_strncpy(s->current, current->valuestring, NE_KEY_LEN);
    }
    
    /* Parse coins */
    cJSON* coins = cJSON_GetObjectItem(root, "coins");
    if (cJSON_IsNumber(coins)) {
        s->coins = coins->valueint;
    }
    
    /* Parse history */
    cJSON* history = cJSON_GetObjectItem(root, "history");
    if (cJSON_IsArray(history)) {
        cJSON* item;
        cJSON_ArrayForEach(item, history) {
            if (cJSON_IsString(item) && s->history_count < NE_MAX_HISTORY) {
                ne_strncpy(s->history[s->history_count++], item->valuestring, NE_KEY_LEN);
            }
        }
    }
    
    /* Parse flags */
    cJSON* flags = cJSON_GetObjectItem(root, "flags");
    if (cJSON_IsObject(flags)) {
        cJSON* item;
        cJSON_ArrayForEach(item, flags) {
            if (s->flag_count < NE_MAX_FLAGS) {
                ne_strncpy(s->flags[s->flag_count].key, item->string, NE_KEY_LEN);
                s->flags[s->flag_count].value = cJSON_IsTrue(item);
                s->flag_count++;
            }
        }
    }
    
    /* Parse vars */
    cJSON* vars = cJSON_GetObjectItem(root, "vars");
    if (cJSON_IsObject(vars)) {
        cJSON* item;
        cJSON_ArrayForEach(item, vars) {
            if (cJSON_IsNumber(item) && s->var_count < NE_MAX_VARS) {
                ne_strncpy(s->vars[s->var_count].key, item->string, NE_KEY_LEN);
                s->vars[s->var_count].value = item->valueint;
                s->var_count++;
            }
        }
    }
    
    /* Parse items */
    cJSON* items = cJSON_GetObjectItem(root, "items");
    if (cJSON_IsObject(items)) {
        cJSON* item;
        cJSON_ArrayForEach(item, items) {
            if (cJSON_IsNumber(item) && s->item_count < NE_MAX_ITEMS) {
                ne_strncpy(s->items[s->item_count].key, item->string, NE_KEY_LEN);
                s->items[s->item_count].value = item->valueint;
                s->item_count++;
            }
        }
    }
    
    cJSON_Delete(root);
    engine->filtered_count = 0;
    
    return NE_OK;
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * FILE I/O
 * ═══════════════════════════════════════════════════════════════════════════════ */

NE_API NE_Result ne_state_save_file(const NE_Engine* engine, const char* path) {
    if (!engine || !path) return NE_ERROR_NULL_PTR;
    
    char* json = ne_state_to_json(engine);
    if (!json) return NE_ERROR_OUT_OF_MEMORY;
    
    FILE* f = fopen(path, "w");
    if (!f) {
        ne_free_string(json);
        return NE_ERROR_IO;
    }
    
    fprintf(f, "%s", json);
    fclose(f);
    ne_free_string(json);
    
    return NE_OK;
}

NE_API NE_Result ne_state_load_file(NE_Engine* engine, const char* path) {
    if (!engine || !path) return NE_ERROR_NULL_PTR;
    
    FILE* f = fopen(path, "r");
    if (!f) return NE_ERROR_FILE_NOT_FOUND;
    
    fseek(f, 0, SEEK_END);
    long size = ftell(f);
    fseek(f, 0, SEEK_SET);
    
    char* buffer = (char*)malloc(size + 1);
    if (!buffer) {
        fclose(f);
        return NE_ERROR_OUT_OF_MEMORY;
    }
    
    fread(buffer, 1, size, f);
    buffer[size] = '\0';
    fclose(f);
    
    NE_Result result = ne_state_from_json(engine, buffer);
    free(buffer);
    
    return result;
}
