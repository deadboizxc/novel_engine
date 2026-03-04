/**
 * @file scene.c
 * @brief Novel Engine — Scene processing and choice handling
 */

#include "../include/internal.h"

/* ═══════════════════════════════════════════════════════════════════════════════
 * INTERNAL: Filter available choices
 * ═══════════════════════════════════════════════════════════════════════════════ */

static void filter_choices(NE_Engine* engine, const NE_Scene* scene) {
    engine->filtered_count = 0;
    
    if (!scene || !engine->state) return;
    
    for (int i = 0; i < scene->choice_count; i++) {
        const NE_Choice* choice = &scene->choices[i];
        
        if (ne_check_conditions(engine->state, choice->conditions, choice->condition_count)) {
            if (engine->filtered_count < NE_MAX_CHOICES) {
                engine->filtered_choices[engine->filtered_count++] = i;
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * SCENE ENTER
 * ═══════════════════════════════════════════════════════════════════════════════ */

NE_API NE_Result ne_scene_enter(NE_Engine* engine) {
    if (!engine) return NE_ERROR_NULL_PTR;
    if (!engine->story || engine->story->scene_count == 0) return NE_ERROR_NO_STORY;
    if (!engine->state) return NE_ERROR_NO_STATE;
    
    const char* scene_id = engine->state->current;
    NE_Scene* scene = ne_story_find_scene(engine->story, scene_id);
    
    if (!scene) return NE_ERROR_SCENE_NOT_FOUND;
    
    /* Check scene conditions */
    if (!ne_check_conditions(engine->state, scene->conditions, scene->condition_count)) {
        /* Try fallback */
        if (scene->fallback[0] != '\0') {
            char target[NE_KEY_LEN];
            ne_normalize_target(scene_id, scene->fallback, target, NE_KEY_LEN);
            ne_strncpy(engine->state->current, target, NE_KEY_LEN);
            return ne_scene_enter(engine);  /* Recursive enter fallback */
        }
        return NE_ERROR_CONDITION_FAILED;
    }
    
    /* Emit SCENE_ENTER event */
    NE_Event event = {0};
    event.type = NE_EVENT_SCENE_ENTER;
    event.scene_id = scene_id;
    ne_emit_event(engine, &event);
    
    /* Emit BACKGROUND event if set */
    if (scene->background[0] != '\0') {
        event.type = NE_EVENT_BACKGROUND;
        event.text = scene->background;
        ne_emit_event(engine, &event);
    }
    
    /* Emit MUSIC event if set */
    if (scene->music[0] != '\0') {
        event.type = NE_EVENT_MUSIC;
        event.text = scene->music;
        ne_emit_event(engine, &event);
    }
    
    /* Emit SFX event if set */
    if (scene->sfx[0] != '\0') {
        event.type = NE_EVENT_SOUND;
        event.text = scene->sfx;
        ne_emit_event(engine, &event);
    }
    
    /* Execute scene actions (may trigger animations, state changes, etc.) */
    ne_execute_actions(engine, scene->actions, scene->action_count);
    
    /* Check if scene changed after actions (jump action) */
    if (strcmp(engine->state->current, scene_id) != 0) {
        /* Scene changed, enter new scene */
        return ne_scene_enter(engine);
    }
    
    /* Emit TEXT event */
    if (scene->text[0] != '\0') {
        event.type = NE_EVENT_TEXT;
        event.text = scene->text;
        ne_emit_event(engine, &event);
    }
    
    /* Check if final scene */
    if (scene->is_final) {
        event.type = NE_EVENT_GAME_END;
        event.scene_id = scene_id;
        ne_emit_event(engine, &event);
        return NE_OK;
    }
    
    /* Filter available choices */
    filter_choices(engine, scene);
    
    /* Emit CHOICES_READY event */
    event.type = NE_EVENT_CHOICES_READY;
    event.int_value = engine->filtered_count;
    ne_emit_event(engine, &event);
    
    /* Auto-next if no choices but auto_next is set */
    if (engine->filtered_count == 0 && scene->auto_next[0] != '\0') {
        char target[NE_KEY_LEN];
        ne_normalize_target(scene_id, scene->auto_next, target, NE_KEY_LEN);
        ne_strncpy(engine->state->current, target, NE_KEY_LEN);
        return ne_scene_enter(engine);
    }
    
    return NE_OK;
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * SCENE GETTERS
 * ═══════════════════════════════════════════════════════════════════════════════ */

NE_API const char* ne_scene_get_text(const NE_Engine* engine) {
    if (!engine || !engine->state || !engine->story) return NULL;
    
    NE_Scene* scene = ne_story_find_scene(engine->story, engine->state->current);
    return scene ? scene->text : NULL;
}

NE_API const char* ne_scene_get_background(const NE_Engine* engine) {
    if (!engine || !engine->state || !engine->story) return NULL;
    
    NE_Scene* scene = ne_story_find_scene(engine->story, engine->state->current);
    return (scene && scene->background[0]) ? scene->background : NULL;
}

NE_API int ne_scene_choice_count(const NE_Engine* engine) {
    if (!engine) return 0;
    return engine->filtered_count;
}

NE_API const char* ne_scene_choice_text(const NE_Engine* engine, int index) {
    if (!engine || !engine->story || !engine->state) return NULL;
    if (index < 0 || index >= engine->filtered_count) return NULL;
    
    NE_Scene* scene = ne_story_find_scene(engine->story, engine->state->current);
    if (!scene) return NULL;
    
    int real_index = engine->filtered_choices[index];
    if (real_index < 0 || real_index >= scene->choice_count) return NULL;
    
    return scene->choices[real_index].text;
}

NE_API bool ne_scene_is_final(const NE_Engine* engine) {
    if (!engine || !engine->state || !engine->story) return false;
    
    NE_Scene* scene = ne_story_find_scene(engine->story, engine->state->current);
    return scene ? scene->is_final : false;
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * CHOICE SELECTION
 * ═══════════════════════════════════════════════════════════════════════════════ */

NE_API NE_Result ne_scene_select(NE_Engine* engine, int index) {
    if (!engine) return NE_ERROR_NULL_PTR;
    if (!engine->story || engine->story->scene_count == 0) return NE_ERROR_NO_STORY;
    if (!engine->state) return NE_ERROR_NO_STATE;
    if (index < 0 || index >= engine->filtered_count) return NE_ERROR_INVALID_CHOICE;
    
    NE_Scene* scene = ne_story_find_scene(engine->story, engine->state->current);
    if (!scene) return NE_ERROR_SCENE_NOT_FOUND;
    
    int real_index = engine->filtered_choices[index];
    const NE_Choice* choice = &scene->choices[real_index];
    
    /* Execute choice actions */
    ne_execute_actions(engine, choice->actions, choice->action_count);
    
    /* Check if scene already changed by jump action */
    const char* old_scene = engine->state->current;
    
    /* Determine next scene */
    char target[NE_KEY_LEN] = {0};
    
    if (choice->jump[0] != '\0') {
        /* Absolute jump */
        ne_strncpy(target, choice->jump, NE_KEY_LEN);
    } else if (choice->next[0] != '\0') {
        /* Relative next */
        ne_normalize_target(old_scene, choice->next, target, NE_KEY_LEN);
    }
    
    /* If we have a target and scene hasn't changed */
    if (target[0] != '\0' && strcmp(engine->state->current, old_scene) == 0) {
        /* Emit exit event */
        NE_Event event = {0};
        event.type = NE_EVENT_SCENE_EXIT;
        event.scene_id = old_scene;
        ne_emit_event(engine, &event);
        
        /* Update current scene */
        ne_strncpy(engine->state->current, target, NE_KEY_LEN);
        
        /* Add to history */
        if (engine->state->history_count < NE_MAX_HISTORY) {
            ne_strncpy(engine->state->history[engine->state->history_count++], 
                      target, NE_KEY_LEN);
        }
    }
    
    /* Enter new scene */
    return ne_scene_enter(engine);
}
