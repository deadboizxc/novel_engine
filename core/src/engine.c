/**
 * @file engine.c
 * @brief Novel Engine — Core engine lifecycle and event system
 */

#include "../include/internal.h"

/* ═══════════════════════════════════════════════════════════════════════════════
 * VERSION
 * ═══════════════════════════════════════════════════════════════════════════════ */

NE_API const char* ne_version_string(void) {
    return NE_VERSION_STRING;
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * RESULT STRINGS
 * ═══════════════════════════════════════════════════════════════════════════════ */

static const char* result_strings[] = {
    [NE_OK]                   = "OK",
    [NE_ERROR_NULL_PTR]       = "Null pointer",
    [NE_ERROR_INVALID_JSON]   = "Invalid JSON",
    [NE_ERROR_INVALID_YAML]   = "Invalid YAML",
    [NE_ERROR_SCENE_NOT_FOUND]= "Scene not found",
    [NE_ERROR_NO_CHOICES]     = "No choices available",
    [NE_ERROR_INVALID_CHOICE] = "Invalid choice index",
    [NE_ERROR_CONDITION_FAILED]= "Condition not met",
    [NE_ERROR_OUT_OF_MEMORY]  = "Out of memory",
    [NE_ERROR_FILE_NOT_FOUND] = "File not found",
    [NE_ERROR_IO]             = "I/O error",
    [NE_ERROR_NO_STORY]       = "No story loaded",
    [NE_ERROR_NO_STATE]       = "Game not started",
};

NE_API const char* ne_result_string(NE_Result result) {
    if (result >= 0 && result <= NE_ERROR_NO_STATE) {
        return result_strings[result];
    }
    return "Unknown error";
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * ENGINE LIFECYCLE
 * ═══════════════════════════════════════════════════════════════════════════════ */

NE_API NE_Engine* ne_engine_create(void) {
    NE_Engine* engine = (NE_Engine*)calloc(1, sizeof(NE_Engine));
    if (!engine) return NULL;
    
    engine->story = (NE_Story*)calloc(1, sizeof(NE_Story));
    if (!engine->story) {
        free(engine);
        return NULL;
    }
    
    engine->state = NULL;  /* Created on ne_game_new() */
    engine->callback = NULL;
    engine->user_data = NULL;
    engine->filtered_count = 0;
    
    return engine;
}

NE_API void ne_engine_destroy(NE_Engine* engine) {
    if (!engine) return;
    
    if (engine->story) {
        free(engine->story);
    }
    if (engine->state) {
        free(engine->state);
    }
    
    free(engine);
}

NE_API void ne_engine_set_callback(NE_Engine* engine,
                                   NE_EventCallback callback,
                                   void* user_data) {
    if (!engine) return;
    engine->callback = callback;
    engine->user_data = user_data;
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * EVENT EMISSION
 * ═══════════════════════════════════════════════════════════════════════════════ */

void ne_emit_event(NE_Engine* engine, const NE_Event* event) {
    if (engine && engine->callback && event) {
        engine->callback(engine->user_data, event);
    }
}

/* Helper to emit simple events */
static void emit_simple(NE_Engine* engine, NE_EventType type, const char* text) {
    NE_Event ev = {0};
    ev.type = type;
    ev.text = text;
    ne_emit_event(engine, &ev);
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * MEMORY UTILITIES
 * ═══════════════════════════════════════════════════════════════════════════════ */

NE_API void ne_free_string(char* str) {
    free(str);
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * INTERNAL: Find scene by ID
 * ═══════════════════════════════════════════════════════════════════════════════ */

NE_Scene* ne_story_find_scene(const NE_Story* story, const char* scene_id) {
    if (!story || !scene_id) return NULL;
    
    for (int i = 0; i < story->scene_count; i++) {
        if (strcmp(story->scenes[i].id, scene_id) == 0) {
            return (NE_Scene*)&story->scenes[i];
        }
    }
    return NULL;
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * INTERNAL: Normalize target scene ID
 * ═══════════════════════════════════════════════════════════════════════════════ */

void ne_normalize_target(const char* current, const char* target, char* out, size_t out_len) {
    if (!target || !out) return;
    
    /* If target already has a dot, use as-is */
    if (strchr(target, '.')) {
        ne_strncpy(out, target, out_len);
        return;
    }
    
    /* Extract chapter from current scene */
    const char* dot = strchr(current, '.');
    if (dot && current) {
        size_t prefix_len = dot - current;
        if (prefix_len < out_len - 2) {
            strncpy(out, current, prefix_len);
            out[prefix_len] = '.';
            ne_strncpy(out + prefix_len + 1, target, out_len - prefix_len - 1);
            return;
        }
    }
    
    /* Fallback: use target as-is */
    ne_strncpy(out, target, out_len);
}
