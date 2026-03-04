/**
 * @file novel_engine.h
 * @brief Novel Engine — Public C API
 * @version 2.0.0
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 * This header defines the complete public interface for the Novel Engine.
 * All language bindings (Python ctypes, Kotlin JNI, Go cgo) use this API.
 *
 * Memory ownership is explicit: every allocation has a matching _free() function.
 * All strings are null-terminated UTF-8.
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Lifecycle:
 *   NE_Engine* = ne_engine_create()     →  ne_engine_destroy(e)
 *   char*      = ne_state_to_json(e)    →  ne_free_string(str)
 */

#ifndef NOVEL_ENGINE_H
#define NOVEL_ENGINE_H

#include <stdint.h>
#include <stdbool.h>
#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

/* ═══════════════════════════════════════════════════════════════════════════════
 * VERSION
 * ═══════════════════════════════════════════════════════════════════════════════ */
#define NE_VERSION_MAJOR 2
#define NE_VERSION_MINOR 0
#define NE_VERSION_PATCH 0
#define NE_VERSION_STRING "2.0.0"

/* ═══════════════════════════════════════════════════════════════════════════════
 * EXPORT MACROS
 * ═══════════════════════════════════════════════════════════════════════════════ */
#ifdef _WIN32
    #ifdef NE_BUILD_DLL
        #define NE_API __declspec(dllexport)
    #else
        #define NE_API __declspec(dllimport)
    #endif
#else
    #define NE_API __attribute__((visibility("default")))
#endif

/* ═══════════════════════════════════════════════════════════════════════════════
 * OPAQUE TYPES
 * ═══════════════════════════════════════════════════════════════════════════════ */

/**
 * @brief Opaque handle to the engine instance.
 *        Contains story graph and current game state.
 */
typedef struct NE_Engine NE_Engine;

/* ═══════════════════════════════════════════════════════════════════════════════
 * RESULT CODES
 * ═══════════════════════════════════════════════════════════════════════════════ */
typedef enum {
    NE_OK = 0,                    /**< Success */
    NE_ERROR_NULL_PTR,            /**< Null pointer passed */
    NE_ERROR_INVALID_JSON,        /**< JSON parse error */
    NE_ERROR_INVALID_YAML,        /**< YAML parse error */
    NE_ERROR_SCENE_NOT_FOUND,     /**< Scene ID not found in story */
    NE_ERROR_NO_CHOICES,          /**< Scene has no available choices */
    NE_ERROR_INVALID_CHOICE,      /**< Choice index out of range */
    NE_ERROR_CONDITION_FAILED,    /**< Scene conditions not met */
    NE_ERROR_OUT_OF_MEMORY,       /**< Memory allocation failed */
    NE_ERROR_FILE_NOT_FOUND,      /**< File does not exist */
    NE_ERROR_IO,                  /**< I/O error */
    NE_ERROR_NO_STORY,            /**< No story loaded */
    NE_ERROR_NO_STATE,            /**< Game not started */
} NE_Result;

/**
 * @brief Get human-readable error message for result code.
 */
NE_API const char* ne_result_string(NE_Result result);

/* ═══════════════════════════════════════════════════════════════════════════════
 * EVENT SYSTEM
 * ═══════════════════════════════════════════════════════════════════════════════ */

/**
 * @brief Event types emitted by the engine.
 */
typedef enum {
    NE_EVENT_SCENE_ENTER,         /**< Entered a new scene */
    NE_EVENT_SCENE_EXIT,          /**< Exiting current scene */
    NE_EVENT_TEXT,                /**< Display text to user */
    NE_EVENT_CHOICES_READY,       /**< Choices available for display */
    NE_EVENT_ACTION,              /**< Action executed (add_coin, set_flag, etc.) */
    NE_EVENT_FLAG_CHANGED,        /**< Flag value changed */
    NE_EVENT_VAR_CHANGED,         /**< Variable value changed */
    NE_EVENT_ITEM_CHANGED,        /**< Inventory changed */
    NE_EVENT_COINS_CHANGED,       /**< Coins value changed */
    NE_EVENT_ANIMATE,             /**< Play animation (glitch, static, etc.) */
    NE_EVENT_SOUND,               /**< Play sound effect */
    NE_EVENT_MUSIC,               /**< Play/change background music */
    NE_EVENT_BACKGROUND,          /**< Change background image */
    NE_EVENT_GAME_END,            /**< Game ended (final scene) */
} NE_EventType;

/**
 * @brief Event data passed to callback.
 */
typedef struct {
    NE_EventType type;            /**< Event type */
    const char*  scene_id;        /**< Scene ID (for SCENE_ENTER/EXIT) */
    const char*  text;            /**< Text content (for TEXT, SOUND, MUSIC, BG) */
    const char*  name;            /**< Name (for FLAG/VAR/ITEM changed) */
    int32_t      int_value;       /**< Integer value (coins, var value) */
    int32_t      old_int_value;   /**< Previous integer value */
    bool         bool_value;      /**< Boolean value (for flags) */
    const char*  anim_type;       /**< Animation type (for ANIMATE) */
    float        duration;        /**< Duration in seconds (for ANIMATE) */
} NE_Event;

/**
 * @brief Event callback function type.
 * @param user_data User-provided context pointer
 * @param event     Event data (valid only during callback)
 */
typedef void (*NE_EventCallback)(void* user_data, const NE_Event* event);

/* ═══════════════════════════════════════════════════════════════════════════════
 * ENGINE LIFECYCLE
 * ═══════════════════════════════════════════════════════════════════════════════ */

/**
 * @brief Create a new engine instance.
 * @return Engine handle, or NULL on allocation failure.
 */
NE_API NE_Engine* ne_engine_create(void);

/**
 * @brief Destroy engine instance and free all resources.
 * @param engine Engine handle (may be NULL)
 */
NE_API void ne_engine_destroy(NE_Engine* engine);

/**
 * @brief Set event callback for the engine.
 * @param engine    Engine handle
 * @param callback  Callback function (NULL to disable)
 * @param user_data User context passed to callback
 */
NE_API void ne_engine_set_callback(NE_Engine* engine,
                                   NE_EventCallback callback,
                                   void* user_data);

/* ═══════════════════════════════════════════════════════════════════════════════
 * STORY LOADING
 * ═══════════════════════════════════════════════════════════════════════════════ */

/**
 * @brief Load story from JSON string.
 * @param engine   Engine handle
 * @param json_str NULL-terminated JSON string
 * @return NE_OK on success, error code otherwise
 */
NE_API NE_Result ne_story_load_json(NE_Engine* engine, const char* json_str);

/**
 * @brief Load story from YAML string.
 * @param engine   Engine handle
 * @param yaml_str NULL-terminated YAML string
 * @return NE_OK on success, error code otherwise
 */
NE_API NE_Result ne_story_load_yaml(NE_Engine* engine, const char* yaml_str);

/**
 * @brief Load story from file (auto-detect JSON/YAML by extension).
 * @param engine Engine handle
 * @param path   Path to .json or .yaml/.yml file
 * @return NE_OK on success, error code otherwise
 */
NE_API NE_Result ne_story_load_file(NE_Engine* engine, const char* path);

/**
 * @brief Load story from directory (all .json/.yaml/.yml files).
 * @param engine   Engine handle
 * @param dir_path Path to directory containing story files
 * @return NE_OK on success, error code otherwise
 */
NE_API NE_Result ne_story_load_dir(NE_Engine* engine, const char* dir_path);

/**
 * @brief Get number of scenes in loaded story.
 * @param engine Engine handle
 * @return Scene count, or 0 if no story loaded
 */
NE_API int ne_story_scene_count(const NE_Engine* engine);

/**
 * @brief Check if scene exists in story.
 * @param engine   Engine handle
 * @param scene_id Scene identifier
 * @return true if scene exists
 */
NE_API bool ne_story_has_scene(const NE_Engine* engine, const char* scene_id);

/* ═══════════════════════════════════════════════════════════════════════════════
 * GAME STATE
 * ═══════════════════════════════════════════════════════════════════════════════ */

/**
 * @brief Start new game from specified scene.
 * @param engine      Engine handle
 * @param start_scene Starting scene ID
 * @return NE_OK on success, error code otherwise
 */
NE_API NE_Result ne_game_new(NE_Engine* engine, const char* start_scene);

/**
 * @brief Reset game to specified scene (keeps story loaded).
 * @param engine      Engine handle
 * @param start_scene Starting scene ID
 * @return NE_OK on success, error code otherwise
 */
NE_API NE_Result ne_game_reset(NE_Engine* engine, const char* start_scene);

/**
 * @brief Serialize game state to JSON string.
 * @param engine Engine handle
 * @return JSON string (caller must free with ne_free_string), or NULL on error
 */
NE_API char* ne_state_to_json(const NE_Engine* engine);

/**
 * @brief Load game state from JSON string.
 * @param engine   Engine handle
 * @param json_str JSON state string
 * @return NE_OK on success, error code otherwise
 */
NE_API NE_Result ne_state_from_json(NE_Engine* engine, const char* json_str);

/**
 * @brief Save game state to file.
 * @param engine Engine handle
 * @param path   File path
 * @return NE_OK on success, error code otherwise
 */
NE_API NE_Result ne_state_save_file(const NE_Engine* engine, const char* path);

/**
 * @brief Load game state from file.
 * @param engine Engine handle
 * @param path   File path
 * @return NE_OK on success, error code otherwise
 */
NE_API NE_Result ne_state_load_file(NE_Engine* engine, const char* path);

/* ═══════════════════════════════════════════════════════════════════════════════
 * STATE GETTERS
 * ═══════════════════════════════════════════════════════════════════════════════ */

/**
 * @brief Get current scene ID.
 * @param engine Engine handle
 * @return Scene ID (internal pointer, do not free), or NULL
 */
NE_API const char* ne_state_current_scene(const NE_Engine* engine);

/**
 * @brief Get coins count.
 * @param engine Engine handle
 * @return Coins value
 */
NE_API int ne_state_get_coins(const NE_Engine* engine);

/**
 * @brief Get variable value.
 * @param engine Engine handle
 * @param name   Variable name
 * @return Variable value, or 0 if not set
 */
NE_API int ne_state_get_var(const NE_Engine* engine, const char* name);

/**
 * @brief Get flag value.
 * @param engine Engine handle
 * @param name   Flag name
 * @return true if flag is set
 */
NE_API bool ne_state_get_flag(const NE_Engine* engine, const char* name);

/**
 * @brief Get item count.
 * @param engine Engine handle
 * @param item   Item name
 * @return Item count, or 0 if not in inventory
 */
NE_API int ne_state_get_item(const NE_Engine* engine, const char* item);

/**
 * @brief Check if player has item.
 * @param engine Engine handle
 * @param item   Item name
 * @return true if item count > 0
 */
NE_API bool ne_state_has_item(const NE_Engine* engine, const char* item);

/* ═══════════════════════════════════════════════════════════════════════════════
 * SCENE INTERACTION
 * ═══════════════════════════════════════════════════════════════════════════════ */

/**
 * @brief Enter current scene (triggers events: TEXT, CHOICES_READY, ANIMATE, etc.)
 * @param engine Engine handle
 * @return NE_OK on success, error code otherwise
 */
NE_API NE_Result ne_scene_enter(NE_Engine* engine);

/**
 * @brief Get current scene text.
 * @param engine Engine handle
 * @return Text (internal pointer, do not free), or NULL
 */
NE_API const char* ne_scene_get_text(const NE_Engine* engine);

/**
 * @brief Get current scene background ID.
 * @param engine Engine handle
 * @return Background ID (internal pointer), or NULL
 */
NE_API const char* ne_scene_get_background(const NE_Engine* engine);

/**
 * @brief Get number of available choices (filtered by conditions).
 * @param engine Engine handle
 * @return Choice count
 */
NE_API int ne_scene_choice_count(const NE_Engine* engine);

/**
 * @brief Get choice text by index.
 * @param engine Engine handle
 * @param index  Choice index (0-based)
 * @return Choice text (internal pointer), or NULL if index invalid
 */
NE_API const char* ne_scene_choice_text(const NE_Engine* engine, int index);

/**
 * @brief Select choice and transition to next scene.
 * @param engine Engine handle
 * @param index  Choice index (0-based)
 * @return NE_OK on success, NE_ERROR_GAME_END if game ended
 */
NE_API NE_Result ne_scene_select(NE_Engine* engine, int index);

/**
 * @brief Check if current scene is final (game over).
 * @param engine Engine handle
 * @return true if scene has 'final: true'
 */
NE_API bool ne_scene_is_final(const NE_Engine* engine);

/* ═══════════════════════════════════════════════════════════════════════════════
 * UTILITIES
 * ═══════════════════════════════════════════════════════════════════════════════ */

/**
 * @brief Get engine version string.
 * @return Version string (e.g., "2.0.0")
 */
NE_API const char* ne_version_string(void);

/**
 * @brief Free string allocated by engine.
 * @param str String to free (may be NULL)
 */
NE_API void ne_free_string(char* str);

#ifdef __cplusplus
}
#endif

#endif /* NOVEL_ENGINE_H */
