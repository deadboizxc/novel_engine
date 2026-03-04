/**
 * @file story.c
 * @brief Novel Engine — Story loading and parsing
 */

#include "../include/internal.h"
#include <ctype.h>

/* ═══════════════════════════════════════════════════════════════════════════════
 * STORY INFO
 * ═══════════════════════════════════════════════════════════════════════════════ */

NE_API int ne_story_scene_count(const NE_Engine* engine) {
    if (!engine || !engine->story) return 0;
    return engine->story->scene_count;
}

NE_API bool ne_story_has_scene(const NE_Engine* engine, const char* scene_id) {
    if (!engine || !engine->story || !scene_id) return false;
    return ne_story_find_scene(engine->story, scene_id) != NULL;
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * INTERNAL: Parse single condition from cJSON
 * ═══════════════════════════════════════════════════════════════════════════════ */

static void parse_condition(NE_Condition* cond, cJSON* json) {
    if (!cond || !json) return;
    
    cJSON* flag = cJSON_GetObjectItem(json, "flag");
    if (flag && cJSON_IsString(flag)) {
        cond->type = NE_COND_FLAG;
        ne_strncpy(cond->name, flag->valuestring, NE_KEY_LEN);
        return;
    }
    
    cJSON* no_flag = cJSON_GetObjectItem(json, "no_flag");
    if (no_flag && cJSON_IsString(no_flag)) {
        cond->type = NE_COND_NO_FLAG;
        ne_strncpy(cond->name, no_flag->valuestring, NE_KEY_LEN);
        return;
    }
    
    cJSON* has = cJSON_GetObjectItem(json, "has");
    if (has && cJSON_IsString(has)) {
        cond->type = NE_COND_HAS_ITEM;
        ne_strncpy(cond->name, has->valuestring, NE_KEY_LEN);
        return;
    }
    
    cJSON* coins = cJSON_GetObjectItem(json, "coins");
    if (coins && cJSON_IsNumber(coins)) {
        cond->type = NE_COND_COINS;
        cond->value = coins->valueint;
        return;
    }
    
    /* Variable conditions */
    cJSON* var = cJSON_GetObjectItem(json, "var");
    if (var && cJSON_IsString(var)) {
        ne_strncpy(cond->name, var->valuestring, NE_KEY_LEN);
        
        cJSON* eq = cJSON_GetObjectItem(json, "equal");
        if (eq && cJSON_IsNumber(eq)) {
            cond->type = NE_COND_VAR_EQ;
            cond->value = eq->valueint;
            return;
        }
        
        cJSON* gt = cJSON_GetObjectItem(json, "more");
        if (gt && cJSON_IsNumber(gt)) {
            cond->type = NE_COND_VAR_GT;
            cond->value = gt->valueint;
            return;
        }
        
        cJSON* lt = cJSON_GetObjectItem(json, "less");
        if (lt && cJSON_IsNumber(lt)) {
            cond->type = NE_COND_VAR_LT;
            cond->value = lt->valueint;
            return;
        }
        
        cJSON* ne = cJSON_GetObjectItem(json, "not");
        if (ne && cJSON_IsNumber(ne)) {
            cond->type = NE_COND_VAR_NE;
            cond->value = ne->valueint;
            return;
        }
    }
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * INTERNAL: Parse single action from cJSON
 * ═══════════════════════════════════════════════════════════════════════════════ */

static void parse_action(NE_Action* act, cJSON* json) {
    if (!act || !json || !cJSON_IsObject(json)) return;
    
    cJSON* item = json->child;
    if (!item) return;
    
    const char* key = item->string;
    
    if (strcmp(key, "jump") == 0 && cJSON_IsString(item)) {
        act->type = NE_ACTION_JUMP;
        ne_strncpy(act->name, item->valuestring, NE_KEY_LEN);
    }
    else if (strcmp(key, "set_flag") == 0 && cJSON_IsString(item)) {
        act->type = NE_ACTION_SET_FLAG;
        ne_strncpy(act->name, item->valuestring, NE_KEY_LEN);
    }
    else if (strcmp(key, "unset_flag") == 0 && cJSON_IsString(item)) {
        act->type = NE_ACTION_UNSET_FLAG;
        ne_strncpy(act->name, item->valuestring, NE_KEY_LEN);
    }
    else if (strcmp(key, "add_coin") == 0 && cJSON_IsNumber(item)) {
        act->type = NE_ACTION_ADD_COIN;
        act->int_value = item->valueint;
    }
    else if (strcmp(key, "increment") == 0 && cJSON_IsString(item)) {
        act->type = NE_ACTION_INCREMENT;
        ne_strncpy(act->name, item->valuestring, NE_KEY_LEN);
    }
    else if (strcmp(key, "decrement") == 0 && cJSON_IsString(item)) {
        act->type = NE_ACTION_DECREMENT;
        ne_strncpy(act->name, item->valuestring, NE_KEY_LEN);
    }
    else if (strcmp(key, "add_item") == 0) {
        act->type = NE_ACTION_ADD_ITEM;
        if (cJSON_IsString(item)) {
            ne_strncpy(act->name, item->valuestring, NE_KEY_LEN);
            act->int_value = 1;
        } else if (cJSON_IsObject(item)) {
            cJSON* first = item->child;
            if (first) {
                ne_strncpy(act->name, first->string, NE_KEY_LEN);
                act->int_value = cJSON_IsNumber(first) ? first->valueint : 1;
            }
        }
    }
    else if (strcmp(key, "remove_item") == 0 && cJSON_IsString(item)) {
        act->type = NE_ACTION_REMOVE_ITEM;
        ne_strncpy(act->name, item->valuestring, NE_KEY_LEN);
        act->int_value = 1;
    }
    else if (strcmp(key, "set_var") == 0 && cJSON_IsObject(item)) {
        act->type = NE_ACTION_SET_VAR;
        cJSON* first = item->child;
        if (first) {
            ne_strncpy(act->name, first->string, NE_KEY_LEN);
            act->int_value = cJSON_IsNumber(first) ? first->valueint : 0;
        }
    }
    else if (strcmp(key, "animate") == 0 && cJSON_IsObject(item)) {
        act->type = NE_ACTION_ANIMATE;
        cJSON* type = cJSON_GetObjectItem(item, "type");
        cJSON* duration = cJSON_GetObjectItem(item, "duration");
        if (type && cJSON_IsString(type)) {
            ne_strncpy(act->str_value, type->valuestring, NE_KEY_LEN);
        }
        act->float_value = (duration && cJSON_IsNumber(duration)) ? 
                           (float)duration->valuedouble : 2.0f;
    }
    else if (strcmp(key, "sfx") == 0 && cJSON_IsString(item)) {
        act->type = NE_ACTION_SOUND;
        ne_strncpy(act->name, item->valuestring, NE_KEY_LEN);
    }
    else if (strcmp(key, "music") == 0 && cJSON_IsString(item)) {
        act->type = NE_ACTION_MUSIC;
        ne_strncpy(act->name, item->valuestring, NE_KEY_LEN);
    }
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * INTERNAL: Parse choice from cJSON
 * ═══════════════════════════════════════════════════════════════════════════════ */

static void parse_choice(NE_Choice* choice, cJSON* json) {
    if (!choice || !json) return;
    
    cJSON* text = cJSON_GetObjectItem(json, "text");
    if (text && cJSON_IsString(text)) {
        ne_strncpy(choice->text, text->valuestring, NE_TEXT_LEN);
    }
    
    cJSON* next = cJSON_GetObjectItem(json, "next");
    if (next && cJSON_IsString(next)) {
        ne_strncpy(choice->next, next->valuestring, NE_KEY_LEN);
    }
    
    cJSON* jump = cJSON_GetObjectItem(json, "jump");
    if (jump && cJSON_IsString(jump)) {
        ne_strncpy(choice->jump, jump->valuestring, NE_KEY_LEN);
    }
    
    /* Parse conditions */
    cJSON* conditions = cJSON_GetObjectItem(json, "conditions");
    if (conditions && cJSON_IsArray(conditions)) {
        cJSON* cond_json;
        cJSON_ArrayForEach(cond_json, conditions) {
            if (choice->condition_count < NE_MAX_CONDITIONS) {
                parse_condition(&choice->conditions[choice->condition_count++], cond_json);
            }
        }
    }
    /* Single condition object */
    cJSON* condition = cJSON_GetObjectItem(json, "condition");
    if (condition && cJSON_IsObject(condition)) {
        if (choice->condition_count < NE_MAX_CONDITIONS) {
            parse_condition(&choice->conditions[choice->condition_count++], condition);
        }
    }
    
    /* Parse actions */
    cJSON* actions = cJSON_GetObjectItem(json, "actions");
    if (actions && cJSON_IsArray(actions)) {
        cJSON* act_json;
        cJSON_ArrayForEach(act_json, actions) {
            if (choice->action_count < NE_MAX_ACTIONS) {
                parse_action(&choice->actions[choice->action_count++], act_json);
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * INTERNAL: Parse scene from cJSON
 * ═══════════════════════════════════════════════════════════════════════════════ */

static void parse_scene(NE_Scene* scene, const char* id, cJSON* json) {
    if (!scene || !json) return;
    
    ne_strncpy(scene->id, id, NE_KEY_LEN);
    
    cJSON* text = cJSON_GetObjectItem(json, "text");
    if (text && cJSON_IsString(text)) {
        ne_strncpy(scene->text, text->valuestring, NE_TEXT_LEN);
    }
    
    cJSON* bg = cJSON_GetObjectItem(json, "bg");
    if (bg && cJSON_IsString(bg)) {
        ne_strncpy(scene->background, bg->valuestring, NE_KEY_LEN);
    }
    
    cJSON* music = cJSON_GetObjectItem(json, "music");
    if (music && cJSON_IsString(music)) {
        ne_strncpy(scene->music, music->valuestring, NE_KEY_LEN);
    }
    
    cJSON* sfx = cJSON_GetObjectItem(json, "sfx");
    if (sfx && cJSON_IsString(sfx)) {
        ne_strncpy(scene->sfx, sfx->valuestring, NE_KEY_LEN);
    }
    
    cJSON* fallback = cJSON_GetObjectItem(json, "fallback");
    if (fallback && cJSON_IsString(fallback)) {
        ne_strncpy(scene->fallback, fallback->valuestring, NE_KEY_LEN);
    }
    
    cJSON* auto_next = cJSON_GetObjectItem(json, "auto_next");
    if (auto_next && cJSON_IsString(auto_next)) {
        ne_strncpy(scene->auto_next, auto_next->valuestring, NE_KEY_LEN);
    }
    
    cJSON* final = cJSON_GetObjectItem(json, "final");
    scene->is_final = final && cJSON_IsTrue(final);
    
    /* Parse conditions */
    cJSON* conditions = cJSON_GetObjectItem(json, "conditions");
    if (conditions && cJSON_IsArray(conditions)) {
        cJSON* cond_json;
        cJSON_ArrayForEach(cond_json, conditions) {
            if (scene->condition_count < NE_MAX_CONDITIONS) {
                parse_condition(&scene->conditions[scene->condition_count++], cond_json);
            }
        }
    }
    
    /* Parse actions */
    cJSON* actions = cJSON_GetObjectItem(json, "actions");
    if (actions && cJSON_IsArray(actions)) {
        cJSON* act_json;
        cJSON_ArrayForEach(act_json, actions) {
            if (scene->action_count < NE_MAX_ACTIONS) {
                parse_action(&scene->actions[scene->action_count++], act_json);
            }
        }
    }
    
    /* Parse choices */
    cJSON* choices = cJSON_GetObjectItem(json, "choices");
    if (choices && cJSON_IsArray(choices)) {
        cJSON* ch_json;
        cJSON_ArrayForEach(ch_json, choices) {
            if (scene->choice_count < NE_MAX_CHOICES) {
                parse_choice(&scene->choices[scene->choice_count++], ch_json);
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * LOAD FROM JSON STRING
 * ═══════════════════════════════════════════════════════════════════════════════ */

NE_API NE_Result ne_story_load_json(NE_Engine* engine, const char* json_str) {
    if (!engine || !json_str) return NE_ERROR_NULL_PTR;
    
    cJSON* root = cJSON_Parse(json_str);
    if (!root) return NE_ERROR_INVALID_JSON;
    
    /* Clear existing story */
    memset(engine->story, 0, sizeof(NE_Story));
    
    /* Parse scenes - expects { "chapter.scene": {...}, ... } */
    cJSON* scene_json = root->child;
    while (scene_json && engine->story->scene_count < NE_MAX_SCENES) {
        if (scene_json->string && cJSON_IsObject(scene_json)) {
            NE_Scene* scene = &engine->story->scenes[engine->story->scene_count];
            memset(scene, 0, sizeof(NE_Scene));
            parse_scene(scene, scene_json->string, scene_json);
            engine->story->scene_count++;
        }
        scene_json = scene_json->next;
    }
    
    cJSON_Delete(root);
    return NE_OK;
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * LOAD FROM YAML STRING (placeholder - requires libyaml)
 * ═══════════════════════════════════════════════════════════════════════════════ */

NE_API NE_Result ne_story_load_yaml(NE_Engine* engine, const char* yaml_str) {
    /* TODO: Implement with libyaml */
    (void)engine;
    (void)yaml_str;
    return NE_ERROR_INVALID_YAML;
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * LOAD FROM FILE
 * ═══════════════════════════════════════════════════════════════════════════════ */

NE_API NE_Result ne_story_load_file(NE_Engine* engine, const char* path) {
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
    
    /* Detect format by extension */
    const char* ext = strrchr(path, '.');
    NE_Result result;
    
    if (ext && (strcmp(ext, ".yaml") == 0 || strcmp(ext, ".yml") == 0)) {
        result = ne_story_load_yaml(engine, buffer);
    } else {
        result = ne_story_load_json(engine, buffer);
    }
    
    free(buffer);
    return result;
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * LOAD FROM DIRECTORY
 * ═══════════════════════════════════════════════════════════════════════════════ */

#ifdef _WIN32
#include <windows.h>
#else
#include <dirent.h>
#endif

NE_API NE_Result ne_story_load_dir(NE_Engine* engine, const char* dir_path) {
    if (!engine || !dir_path) return NE_ERROR_NULL_PTR;
    
    /* Clear existing story */
    memset(engine->story, 0, sizeof(NE_Story));
    
#ifdef _WIN32
    char pattern[512];
    snprintf(pattern, sizeof(pattern), "%s\\*.json", dir_path);
    
    WIN32_FIND_DATAA fd;
    HANDLE hFind = FindFirstFileA(pattern, &fd);
    
    if (hFind == INVALID_HANDLE_VALUE) {
        return NE_ERROR_FILE_NOT_FOUND;
    }
    
    do {
        char filepath[512];
        snprintf(filepath, sizeof(filepath), "%s\\%s", dir_path, fd.cFileName);
        ne_story_load_file(engine, filepath);
    } while (FindNextFileA(hFind, &fd));
    
    FindClose(hFind);
#else
    DIR* dir = opendir(dir_path);
    if (!dir) return NE_ERROR_FILE_NOT_FOUND;
    
    struct dirent* entry;
    while ((entry = readdir(dir)) != NULL) {
        const char* name = entry->d_name;
        const char* ext = strrchr(name, '.');
        
        if (ext && (strcmp(ext, ".json") == 0 || 
                    strcmp(ext, ".yaml") == 0 || 
                    strcmp(ext, ".yml") == 0)) {
            char filepath[512];
            snprintf(filepath, sizeof(filepath), "%s/%s", dir_path, name);
            ne_story_load_file(engine, filepath);
        }
    }
    
    closedir(dir);
#endif
    
    return engine->story->scene_count > 0 ? NE_OK : NE_ERROR_FILE_NOT_FOUND;
}
