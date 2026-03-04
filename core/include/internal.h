/**
 * @file internal.h
 * @brief Novel Engine — Internal structures and helpers
 *
 * This header is NOT part of the public API.
 * Only used by engine implementation files.
 */

#ifndef NE_INTERNAL_H
#define NE_INTERNAL_H

#include "novel_engine.h"
#include "../deps/cjson/cJSON.h"
#include <stdlib.h>
#include <string.h>
#include <stdio.h>

/* ═══════════════════════════════════════════════════════════════════════════════
 * CONSTANTS
 * ═══════════════════════════════════════════════════════════════════════════════ */
#define NE_MAX_SCENES        1024
#define NE_MAX_CHOICES       16
#define NE_MAX_ACTIONS       32
#define NE_MAX_CONDITIONS    16
#define NE_MAX_FLAGS         256
#define NE_MAX_VARS          128
#define NE_MAX_ITEMS         128
#define NE_MAX_HISTORY       256

#define NE_KEY_LEN           256
#define NE_TEXT_LEN          8192

/* ═══════════════════════════════════════════════════════════════════════════════
 * ACTION TYPES
 * ═══════════════════════════════════════════════════════════════════════════════ */
typedef enum {
    NE_ACTION_NONE = 0,
    NE_ACTION_JUMP,           /* jump: "scene_id" */
    NE_ACTION_SET_FLAG,       /* set_flag: "flag_name" */
    NE_ACTION_UNSET_FLAG,     /* unset_flag: "flag_name" */
    NE_ACTION_ADD_COIN,       /* add_coin: 10 or add_coin: -5 */
    NE_ACTION_SET_VAR,        /* set_var: { name: value } */
    NE_ACTION_INCREMENT,      /* increment: "var_name" */
    NE_ACTION_DECREMENT,      /* decrement: "var_name" */
    NE_ACTION_ADD_ITEM,       /* add_item: "item" or { item: count } */
    NE_ACTION_REMOVE_ITEM,    /* remove_item: "item" */
    NE_ACTION_ANIMATE,        /* animate: { type, duration } */
    NE_ACTION_SOUND,          /* sfx: "sound_id" */
    NE_ACTION_MUSIC,          /* music: "track_id" */
} NE_ActionType;

typedef struct {
    NE_ActionType type;
    char          name[NE_KEY_LEN];
    int32_t       int_value;
    float         float_value;
    char          str_value[NE_KEY_LEN];
} NE_Action;

/* ═══════════════════════════════════════════════════════════════════════════════
 * CONDITION TYPES
 * ═══════════════════════════════════════════════════════════════════════════════ */
typedef enum {
    NE_COND_NONE = 0,
    NE_COND_FLAG,             /* flag: "flag_name" */
    NE_COND_NO_FLAG,          /* no_flag: "flag_name" */
    NE_COND_HAS_ITEM,         /* has: "item" */
    NE_COND_NO_ITEM,          /* no_item: "item" */
    NE_COND_COINS,            /* coins: 50 (minimum) */
    NE_COND_VAR_EQ,           /* var: name, equal: value */
    NE_COND_VAR_GT,           /* var: name, more: value */
    NE_COND_VAR_LT,           /* var: name, less: value */
    NE_COND_VAR_NE,           /* var: name, not: value */
} NE_ConditionType;

typedef struct {
    NE_ConditionType type;
    char             name[NE_KEY_LEN];
    int32_t          value;
} NE_Condition;

/* ═══════════════════════════════════════════════════════════════════════════════
 * CHOICE
 * ═══════════════════════════════════════════════════════════════════════════════ */
typedef struct {
    char          text[NE_TEXT_LEN];
    char          next[NE_KEY_LEN];       /* next scene (relative) */
    char          jump[NE_KEY_LEN];       /* jump scene (absolute) */
    NE_Action     actions[NE_MAX_ACTIONS];
    int           action_count;
    NE_Condition  conditions[NE_MAX_CONDITIONS];
    int           condition_count;
} NE_Choice;

/* ═══════════════════════════════════════════════════════════════════════════════
 * SCENE
 * ═══════════════════════════════════════════════════════════════════════════════ */
typedef struct {
    char          id[NE_KEY_LEN];
    char          text[NE_TEXT_LEN];
    char          background[NE_KEY_LEN];
    char          music[NE_KEY_LEN];
    char          sfx[NE_KEY_LEN];
    char          fallback[NE_KEY_LEN];
    char          auto_next[NE_KEY_LEN];
    bool          is_final;
    NE_Action     actions[NE_MAX_ACTIONS];
    int           action_count;
    NE_Condition  conditions[NE_MAX_CONDITIONS];
    int           condition_count;
    NE_Choice     choices[NE_MAX_CHOICES];
    int           choice_count;
} NE_Scene;

/* ═══════════════════════════════════════════════════════════════════════════════
 * STORY
 * ═══════════════════════════════════════════════════════════════════════════════ */
typedef struct {
    NE_Scene scenes[NE_MAX_SCENES];
    int      scene_count;
} NE_Story;

/* ═══════════════════════════════════════════════════════════════════════════════
 * KEY-VALUE STORES
 * ═══════════════════════════════════════════════════════════════════════════════ */
typedef struct {
    char    key[NE_KEY_LEN];
    int32_t value;
} NE_KVInt;

typedef struct {
    char key[NE_KEY_LEN];
    bool value;
} NE_KVBool;

/* ═══════════════════════════════════════════════════════════════════════════════
 * GAME STATE
 * ═══════════════════════════════════════════════════════════════════════════════ */
typedef struct {
    char      current[NE_KEY_LEN];
    char      history[NE_MAX_HISTORY][NE_KEY_LEN];
    int       history_count;
    int32_t   coins;
    NE_KVBool flags[NE_MAX_FLAGS];
    int       flag_count;
    NE_KVInt  vars[NE_MAX_VARS];
    int       var_count;
    NE_KVInt  items[NE_MAX_ITEMS];
    int       item_count;
} NE_GameState;

/* ═══════════════════════════════════════════════════════════════════════════════
 * ENGINE
 * ═══════════════════════════════════════════════════════════════════════════════ */
struct NE_Engine {
    NE_Story*         story;
    NE_GameState*     state;
    NE_EventCallback  callback;
    void*             user_data;
    
    /* Cached filtered choices for current scene */
    int               filtered_choices[NE_MAX_CHOICES];
    int               filtered_count;
};

/* ═══════════════════════════════════════════════════════════════════════════════
 * INTERNAL HELPERS
 * ═══════════════════════════════════════════════════════════════════════════════ */

/* String helpers */
static inline void ne_strncpy(char* dst, const char* src, size_t n) {
    if (src) {
        strncpy(dst, src, n - 1);
        dst[n - 1] = '\0';
    } else {
        dst[0] = '\0';
    }
}

/* Find scene by ID */
NE_Scene* ne_story_find_scene(const NE_Story* story, const char* scene_id);

/* Normalize scene target (add chapter prefix if needed) */
void ne_normalize_target(const char* current, const char* target, char* out, size_t out_len);

/* Check single condition */
bool ne_check_condition(const NE_GameState* state, const NE_Condition* cond);

/* Check all conditions */
bool ne_check_conditions(const NE_GameState* state, const NE_Condition* conds, int count);

/* Execute single action */
void ne_execute_action(NE_Engine* engine, const NE_Action* action);

/* Execute all actions */
void ne_execute_actions(NE_Engine* engine, const NE_Action* actions, int count);

/* Emit event */
void ne_emit_event(NE_Engine* engine, const NE_Event* event);

/* Parse story from cJSON */
NE_Result ne_story_parse_json(NE_Story* story, cJSON* root);

/* State helpers */
NE_KVInt* ne_state_find_var(NE_GameState* state, const char* name);
NE_KVBool* ne_state_find_flag(NE_GameState* state, const char* name);
NE_KVInt* ne_state_find_item(NE_GameState* state, const char* item);

#endif /* NE_INTERNAL_H */
