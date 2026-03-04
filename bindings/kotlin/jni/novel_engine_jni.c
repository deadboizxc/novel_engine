/**
 * JNI Bridge for Novel Engine
 *
 * This file provides the JNI (Java Native Interface) implementation
 * that connects the Kotlin/JVM code to the C core library.
 *
 * Build with:
 *   gcc -shared -fPIC -I$JAVA_HOME/include -I$JAVA_HOME/include/linux \
 *       -I../../core/include novel_engine_jni.c -L../../core/build -lnovel_engine \
 *       -o libnovel_engine_jni.so
 *
 * For Windows:
 *   cl /LD /I"%JAVA_HOME%\include" /I"%JAVA_HOME%\include\win32" \
 *      /I..\..\core\include novel_engine_jni.c /link /LIBPATH:..\..\core\build novel_engine.lib \
 *      /OUT:novel_engine_jni.dll
 */

#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include "novel_engine.h"

#ifdef __ANDROID__
#include <android/log.h>
#define LOG_TAG "NovelEngineJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#else
#define LOGI(...)
#define LOGE(...)
#endif

/* JNI version compatibility */
#ifndef JNI_VERSION_1_6
#define JNI_VERSION_1_6 0x00010006
#endif

/* ═══════════════════════════════════════════════════════════════════════════
 * JNI CLASS AND METHOD REFERENCES
 * ═══════════════════════════════════════════════════════════════════════════ */

static JavaVM* g_jvm = NULL;

typedef struct {
    NE_Engine* engine;
    jobject    handler;      /* Global reference to Kotlin handler */
    jmethodID  on_event_mid; /* Method ID for onNativeEvent */
} JNI_EngineContext;

/* ═══════════════════════════════════════════════════════════════════════════
 * EVENT CALLBACK
 * ═══════════════════════════════════════════════════════════════════════════ */

static void jni_event_callback(void* user_data, const NE_Event* event) {
    JNI_EngineContext* ctx = (JNI_EngineContext*)user_data;
    if (!ctx || !ctx->handler || !g_jvm) return;

    JNIEnv* env = NULL;
    int attached = 0;

    /* Get JNIEnv for current thread */
    jint result = (*g_jvm)->GetEnv(g_jvm, (void**)&env, JNI_VERSION_1_6);
    if (result == JNI_EDETACHED) {
#ifdef __ANDROID__
        if ((*g_jvm)->AttachCurrentThread(g_jvm, &env, NULL) != 0) {
#else
        if ((*g_jvm)->AttachCurrentThread(g_jvm, (void**)&env, NULL) != 0) {
#endif
            return;
        }
        attached = 1;
    } else if (result != JNI_OK) {
        return;
    }

    /* Prepare strings */
    jstring j_scene_id = event->scene_id ? (*env)->NewStringUTF(env, event->scene_id) : NULL;
    jstring j_text = event->text ? (*env)->NewStringUTF(env, event->text) : NULL;
    jstring j_name = event->name ? (*env)->NewStringUTF(env, event->name) : NULL;

    /* Call Kotlin handler */
    (*env)->CallVoidMethod(env, ctx->handler, ctx->on_event_mid,
        (jint)event->type,
        j_scene_id,
        j_text,
        j_name,
        (jint)event->int_value,
        (jfloat)event->duration,
        (jboolean)event->bool_value
    );

    /* Clean up local references */
    if (j_scene_id) (*env)->DeleteLocalRef(env, j_scene_id);
    if (j_text) (*env)->DeleteLocalRef(env, j_text);
    if (j_name) (*env)->DeleteLocalRef(env, j_name);

    if (attached) {
        (*g_jvm)->DetachCurrentThread(g_jvm);
    }
}

/* ═══════════════════════════════════════════════════════════════════════════
 * JNI LIFECYCLE
 * ═══════════════════════════════════════════════════════════════════════════ */

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    (void)reserved;
    g_jvm = vm;
    LOGI("JNI_OnLoad: Novel Engine JNI loaded");
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
    (void)vm;
    (void)reserved;
    g_jvm = NULL;
}

/* ═══════════════════════════════════════════════════════════════════════════
 * NATIVE METHODS
 * ═══════════════════════════════════════════════════════════════════════════ */

/* Helper to get context from pointer */
static inline JNI_EngineContext* get_ctx(jlong ptr) {
    return (JNI_EngineContext*)(intptr_t)ptr;
}

/* Create engine */
JNIEXPORT jlong JNICALL
Java_com_novelengine_engine_NovelEngine_nativeCreate(JNIEnv* env, jobject thiz) {
    (void)env;
    (void)thiz;

    JNI_EngineContext* ctx = (JNI_EngineContext*)calloc(1, sizeof(JNI_EngineContext));
    if (!ctx) return 0;

    ctx->engine = ne_engine_create();
    if (!ctx->engine) {
        free(ctx);
        return 0;
    }

    return (jlong)(intptr_t)ctx;
}

/* Destroy engine */
JNIEXPORT void JNICALL
Java_com_novelengine_engine_NovelEngine_nativeDestroy(JNIEnv* env, jobject thiz, jlong ptr) {
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx) return;

    if (ctx->handler) {
        (*env)->DeleteGlobalRef(env, ctx->handler);
    }
    if (ctx->engine) {
        ne_engine_destroy(ctx->engine);
    }
    free(ctx);
}

/* Set callback handler */
JNIEXPORT void JNICALL
Java_com_novelengine_engine_NovelEngine_nativeSetCallbackHandler(
    JNIEnv* env, jobject thiz, jlong ptr, jobject handler
) {
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx) return;

    /* Delete old handler if exists */
    if (ctx->handler) {
        (*env)->DeleteGlobalRef(env, ctx->handler);
        ctx->handler = NULL;
    }

    if (handler) {
        /* Create global reference */
        ctx->handler = (*env)->NewGlobalRef(env, handler);

        /* Get method ID for onNativeEvent */
        jclass cls = (*env)->GetObjectClass(env, handler);
        ctx->on_event_mid = (*env)->GetMethodID(env, cls,
            "onNativeEvent",
            "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;IFZ)V"
        );
        (*env)->DeleteLocalRef(env, cls);

        /* Set C callback */
        ne_engine_set_callback(ctx->engine, jni_event_callback, ctx);
    } else {
        ne_engine_set_callback(ctx->engine, NULL, NULL);
    }
}

/* ═══════════════════════════════════════════════════════════════════════════
 * STORY LOADING
 * ═══════════════════════════════════════════════════════════════════════════ */

JNIEXPORT jint JNICALL
Java_com_novelengine_engine_NovelEngine_nativeLoadStoryJson(
    JNIEnv* env, jobject thiz, jlong ptr, jstring json
) {
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx || !json) return -1;

    const char* json_str = (*env)->GetStringUTFChars(env, json, NULL);
    NE_Result result = ne_story_load_json(ctx->engine, json_str);
    (*env)->ReleaseStringUTFChars(env, json, json_str);

    return (jint)result;
}

JNIEXPORT jint JNICALL
Java_com_novelengine_engine_NovelEngine_nativeLoadStoryDir(
    JNIEnv* env, jobject thiz, jlong ptr, jstring path
) {
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx || !path) return -1;

    const char* path_str = (*env)->GetStringUTFChars(env, path, NULL);
    NE_Result result = ne_story_load_dir(ctx->engine, path_str);
    (*env)->ReleaseStringUTFChars(env, path, path_str);

    return (jint)result;
}

/* ═══════════════════════════════════════════════════════════════════════════
 * GAME STATE
 * ═══════════════════════════════════════════════════════════════════════════ */

JNIEXPORT void JNICALL
Java_com_novelengine_engine_NovelEngine_nativeNewGame(
    JNIEnv* env, jobject thiz, jlong ptr, jstring start_scene
) {
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx || !start_scene) return;

    const char* scene_str = (*env)->GetStringUTFChars(env, start_scene, NULL);
    ne_game_new(ctx->engine, scene_str);
    (*env)->ReleaseStringUTFChars(env, start_scene, scene_str);
}

JNIEXPORT void JNICALL
Java_com_novelengine_engine_NovelEngine_nativeResetGame(
    JNIEnv* env, jobject thiz, jlong ptr, jstring start_scene
) {
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx || !start_scene) return;

    const char* scene_str = (*env)->GetStringUTFChars(env, start_scene, NULL);
    ne_game_reset(ctx->engine, scene_str);
    (*env)->ReleaseStringUTFChars(env, start_scene, scene_str);
}

JNIEXPORT jint JNICALL
Java_com_novelengine_engine_NovelEngine_nativeEnterScene(
    JNIEnv* env, jobject thiz, jlong ptr
) {
    (void)env;
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx) return -1;

    return (jint)ne_scene_enter(ctx->engine);
}

JNIEXPORT jstring JNICALL
Java_com_novelengine_engine_NovelEngine_nativeGetSceneText(
    JNIEnv* env, jobject thiz, jlong ptr
) {
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx) return NULL;

    const char* text = ne_scene_get_text(ctx->engine);
    return text ? (*env)->NewStringUTF(env, text) : NULL;
}

JNIEXPORT jstring JNICALL
Java_com_novelengine_engine_NovelEngine_nativeGetBackground(
    JNIEnv* env, jobject thiz, jlong ptr
) {
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx) return NULL;

    const char* bg = ne_scene_get_background(ctx->engine);
    return bg ? (*env)->NewStringUTF(env, bg) : NULL;
}

JNIEXPORT jint JNICALL
Java_com_novelengine_engine_NovelEngine_nativeGetChoiceCount(
    JNIEnv* env, jobject thiz, jlong ptr
) {
    (void)env;
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx) return 0;

    return (jint)ne_scene_choice_count(ctx->engine);
}

JNIEXPORT jstring JNICALL
Java_com_novelengine_engine_NovelEngine_nativeGetChoiceText(
    JNIEnv* env, jobject thiz, jlong ptr, jint index
) {
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx) return NULL;

    const char* text = ne_scene_choice_text(ctx->engine, index);
    return text ? (*env)->NewStringUTF(env, text) : NULL;
}

JNIEXPORT jint JNICALL
Java_com_novelengine_engine_NovelEngine_nativeSelectChoice(
    JNIEnv* env, jobject thiz, jlong ptr, jint index
) {
    (void)env;
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx) return -1;

    return (jint)ne_scene_select(ctx->engine, index);
}

JNIEXPORT jboolean JNICALL
Java_com_novelengine_engine_NovelEngine_nativeIsFinal(
    JNIEnv* env, jobject thiz, jlong ptr
) {
    (void)env;
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx) return JNI_FALSE;

    return ne_scene_is_final(ctx->engine) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_novelengine_engine_NovelEngine_nativeHasScene(
    JNIEnv* env, jobject thiz, jlong ptr, jstring scene_id
) {
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx || !scene_id) return JNI_FALSE;

    const char* id_str = (*env)->GetStringUTFChars(env, scene_id, NULL);
    jboolean result = ne_story_has_scene(ctx->engine, id_str) ? JNI_TRUE : JNI_FALSE;
    (*env)->ReleaseStringUTFChars(env, scene_id, id_str);

    return result;
}

JNIEXPORT jstring JNICALL
Java_com_novelengine_engine_NovelEngine_nativeGetCurrentScene(
    JNIEnv* env, jobject thiz, jlong ptr
) {
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx) return NULL;

    const char* scene = ne_state_current_scene(ctx->engine);
    return scene ? (*env)->NewStringUTF(env, scene) : NULL;
}

/* ═══════════════════════════════════════════════════════════════════════════
 * STATE GETTERS
 * ═══════════════════════════════════════════════════════════════════════════ */

JNIEXPORT jint JNICALL
Java_com_novelengine_engine_NovelEngine_nativeGetCoins(
    JNIEnv* env, jobject thiz, jlong ptr
) {
    (void)env;
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx) return 0;

    return (jint)ne_state_get_coins(ctx->engine);
}

JNIEXPORT jint JNICALL
Java_com_novelengine_engine_NovelEngine_nativeGetVar(
    JNIEnv* env, jobject thiz, jlong ptr, jstring name
) {
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx || !name) return 0;

    const char* name_str = (*env)->GetStringUTFChars(env, name, NULL);
    jint result = (jint)ne_state_get_var(ctx->engine, name_str);
    (*env)->ReleaseStringUTFChars(env, name, name_str);

    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_novelengine_engine_NovelEngine_nativeGetFlag(
    JNIEnv* env, jobject thiz, jlong ptr, jstring name
) {
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx || !name) return JNI_FALSE;

    const char* name_str = (*env)->GetStringUTFChars(env, name, NULL);
    jboolean result = ne_state_get_flag(ctx->engine, name_str) ? JNI_TRUE : JNI_FALSE;
    (*env)->ReleaseStringUTFChars(env, name, name_str);

    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_novelengine_engine_NovelEngine_nativeHasItem(
    JNIEnv* env, jobject thiz, jlong ptr, jstring item
) {
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx || !item) return JNI_FALSE;

    const char* item_str = (*env)->GetStringUTFChars(env, item, NULL);
    jboolean result = ne_state_has_item(ctx->engine, item_str) ? JNI_TRUE : JNI_FALSE;
    (*env)->ReleaseStringUTFChars(env, item, item_str);

    return result;
}

JNIEXPORT jint JNICALL
Java_com_novelengine_engine_NovelEngine_nativeGetItemCount(
    JNIEnv* env, jobject thiz, jlong ptr, jstring item
) {
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx || !item) return 0;

    const char* item_str = (*env)->GetStringUTFChars(env, item, NULL);
    jint result = (jint)ne_state_get_item(ctx->engine, item_str);
    (*env)->ReleaseStringUTFChars(env, item, item_str);

    return result;
}

/* ═══════════════════════════════════════════════════════════════════════════
 * SAVE/LOAD
 * ═══════════════════════════════════════════════════════════════════════════ */

JNIEXPORT jstring JNICALL
Java_com_novelengine_engine_NovelEngine_nativeSaveToJson(
    JNIEnv* env, jobject thiz, jlong ptr
) {
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx) return NULL;

    char* json = ne_state_to_json(ctx->engine);
    if (!json) return NULL;

    jstring result = (*env)->NewStringUTF(env, json);
    ne_free_string(json);

    return result;
}

JNIEXPORT jint JNICALL
Java_com_novelengine_engine_NovelEngine_nativeLoadFromJson(
    JNIEnv* env, jobject thiz, jlong ptr, jstring json
) {
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx || !json) return -1;

    const char* json_str = (*env)->GetStringUTFChars(env, json, NULL);
    NE_Result result = ne_state_from_json(ctx->engine, json_str);
    (*env)->ReleaseStringUTFChars(env, json, json_str);

    return (jint)result;
}

JNIEXPORT jint JNICALL
Java_com_novelengine_engine_NovelEngine_nativeSaveToFile(
    JNIEnv* env, jobject thiz, jlong ptr, jstring path
) {
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx || !path) return -1;

    const char* path_str = (*env)->GetStringUTFChars(env, path, NULL);
    NE_Result result = ne_state_save_file(ctx->engine, path_str);
    (*env)->ReleaseStringUTFChars(env, path, path_str);

    return (jint)result;
}

JNIEXPORT jint JNICALL
Java_com_novelengine_engine_NovelEngine_nativeLoadFromFile(
    JNIEnv* env, jobject thiz, jlong ptr, jstring path
) {
    (void)thiz;
    JNI_EngineContext* ctx = get_ctx(ptr);
    if (!ctx || !path) return -1;

    const char* path_str = (*env)->GetStringUTFChars(env, path, NULL);
    NE_Result result = ne_state_load_file(ctx->engine, path_str);
    (*env)->ReleaseStringUTFChars(env, path, path_str);

    return (jint)result;
}
