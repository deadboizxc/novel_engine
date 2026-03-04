/**
 * Novel Engine JNI Bridge for Android
 * Pure C implementation - no KMP
 */

#include <jni.h>
#include <android/log.h>
#include <string.h>
#include <stdlib.h>

#include "novel_engine.h"

#define LOG_TAG "NovelEngineJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global engine instance
static NE_Engine* g_engine = NULL;

// Helper: Convert jstring to C string (caller must free)
static char* jstring_to_cstr(JNIEnv* env, jstring jstr) {
    if (!jstr) return NULL;
    const char* utf = (*env)->GetStringUTFChars(env, jstr, NULL);
    char* copy = strdup(utf);
    (*env)->ReleaseStringUTFChars(env, jstr, utf);
    return copy;
}

// Helper: Convert C string to jstring
static jstring cstr_to_jstring(JNIEnv* env, const char* str) {
    if (!str) return NULL;
    return (*env)->NewStringUTF(env, str);
}

// ============================================================================
// Engine lifecycle
// ============================================================================

JNIEXPORT jboolean JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_init(JNIEnv* env, jobject thiz) {
    if (g_engine) {
        ne_engine_destroy(g_engine);
    }
    g_engine = ne_engine_create();
    if (!g_engine) {
        LOGE("Failed to create engine");
        return JNI_FALSE;
    }
    LOGI("Engine initialized");
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_destroy(JNIEnv* env, jobject thiz) {
    if (g_engine) {
        ne_engine_destroy(g_engine);
        g_engine = NULL;
        LOGI("Engine destroyed");
    }
}

// ============================================================================
// Story loading
// ============================================================================

JNIEXPORT jboolean JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_loadStory(JNIEnv* env, jobject thiz, jstring json_data) {
    if (!g_engine) return JNI_FALSE;
    
    char* data = jstring_to_cstr(env, json_data);
    if (!data) return JNI_FALSE;
    
    NE_Result result = ne_story_load_json(g_engine, data);
    free(data);
    
    if (result != NE_OK) {
        LOGE("Failed to load story: %d", result);
        return JNI_FALSE;
    }
    
    LOGI("Story loaded successfully");
    return JNI_TRUE;
}

// ============================================================================
// Game state
// ============================================================================

JNIEXPORT jstring JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_getCurrentScene(JNIEnv* env, jobject thiz) {
    if (!g_engine) return NULL;
    const char* scene = ne_state_current_scene(g_engine);
    return cstr_to_jstring(env, scene);
}

JNIEXPORT jboolean JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_startGame(JNIEnv* env, jobject thiz, jstring start_scene) {
    if (!g_engine) return JNI_FALSE;
    char* scene = jstring_to_cstr(env, start_scene);
    if (!scene) return JNI_FALSE;
    NE_Result result = ne_game_new(g_engine, scene);
    free(scene);
    return result == NE_OK ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_reset(JNIEnv* env, jobject thiz, jstring start_scene) {
    if (!g_engine) return JNI_FALSE;
    char* scene = jstring_to_cstr(env, start_scene);
    if (!scene) return JNI_FALSE;
    NE_Result result = ne_game_reset(g_engine, scene);
    free(scene);
    LOGI("Game reset");
    return result == NE_OK ? JNI_TRUE : JNI_FALSE;
}

// ============================================================================
// Variables
// ============================================================================

JNIEXPORT jint JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_getVar(JNIEnv* env, jobject thiz, jstring name) {
    if (!g_engine) return 0;
    char* n = jstring_to_cstr(env, name);
    if (!n) return 0;
    int val = ne_state_get_var(g_engine, n);
    free(n);
    return val;
}

JNIEXPORT jint JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_getCoins(JNIEnv* env, jobject thiz) {
    if (!g_engine) return 0;
    return ne_state_get_coins(g_engine);
}

// ============================================================================
// Flags
// ============================================================================

JNIEXPORT jboolean JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_getFlag(JNIEnv* env, jobject thiz, jstring name) {
    if (!g_engine) return JNI_FALSE;
    char* n = jstring_to_cstr(env, name);
    if (!n) return JNI_FALSE;
    bool val = ne_state_get_flag(g_engine, n);
    free(n);
    return val ? JNI_TRUE : JNI_FALSE;
}

// ============================================================================
// Items
// ============================================================================

JNIEXPORT jint JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_getItem(JNIEnv* env, jobject thiz, jstring name) {
    if (!g_engine) return 0;
    char* n = jstring_to_cstr(env, name);
    if (!n) return 0;
    int val = ne_state_get_item(g_engine, n);
    free(n);
    return val;
}

JNIEXPORT jboolean JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_hasItem(JNIEnv* env, jobject thiz, jstring name) {
    if (!g_engine) return JNI_FALSE;
    char* n = jstring_to_cstr(env, name);
    if (!n) return JNI_FALSE;
    bool val = ne_state_has_item(g_engine, n);
    free(n);
    return val ? JNI_TRUE : JNI_FALSE;
}

// ============================================================================
// Scene data
// ============================================================================

JNIEXPORT jboolean JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_enterScene(JNIEnv* env, jobject thiz) {
    if (!g_engine) return JNI_FALSE;
    NE_Result result = ne_scene_enter(g_engine);
    return result == NE_OK ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jstring JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_getSceneText(JNIEnv* env, jobject thiz) {
    if (!g_engine) return NULL;
    const char* text = ne_scene_get_text(g_engine);
    return cstr_to_jstring(env, text);
}

JNIEXPORT jstring JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_getSceneBackground(JNIEnv* env, jobject thiz) {
    if (!g_engine) return NULL;
    const char* bg = ne_scene_get_background(g_engine);
    return cstr_to_jstring(env, bg);
}

JNIEXPORT jint JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_getChoiceCount(JNIEnv* env, jobject thiz) {
    if (!g_engine) return 0;
    return ne_scene_choice_count(g_engine);
}

JNIEXPORT jstring JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_getChoiceText(JNIEnv* env, jobject thiz, jint index) {
    if (!g_engine) return NULL;
    const char* text = ne_scene_choice_text(g_engine, index);
    return cstr_to_jstring(env, text);
}

JNIEXPORT jboolean JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_selectChoice(JNIEnv* env, jobject thiz, jint index) {
    if (!g_engine) return JNI_FALSE;
    NE_Result result = ne_scene_select(g_engine, index);
    return result == NE_OK ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_isFinalScene(JNIEnv* env, jobject thiz) {
    if (!g_engine) return JNI_FALSE;
    return ne_scene_is_final(g_engine) ? JNI_TRUE : JNI_FALSE;
}

// ============================================================================
// Save/Load
// ============================================================================

JNIEXPORT jstring JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_saveState(JNIEnv* env, jobject thiz) {
    if (!g_engine) return NULL;
    char* json = ne_state_to_json(g_engine);
    if (!json) return NULL;
    jstring result = cstr_to_jstring(env, json);
    ne_free_string(json);
    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_loadState(JNIEnv* env, jobject thiz, jstring json_data) {
    if (!g_engine) return JNI_FALSE;
    char* data = jstring_to_cstr(env, json_data);
    if (!data) return JNI_FALSE;
    NE_Result result = ne_state_from_json(g_engine, data);
    free(data);
    return result == NE_OK ? JNI_TRUE : JNI_FALSE;
}

// ============================================================================
// Version info
// ============================================================================

JNIEXPORT jstring JNICALL
Java_com_deadboizxc_tuinovel_engine_NativeEngine_getVersion(JNIEnv* env, jobject thiz) {
    return cstr_to_jstring(env, ne_version_string());
}
