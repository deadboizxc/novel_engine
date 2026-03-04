cmake_minimum_required(VERSION 3.18)
project(novel_engine_jni)

set(CMAKE_C_STANDARD 11)

# Novel Engine core sources
set(CORE_DIR ${CMAKE_SOURCE_DIR}/../../../../../core)

add_library(novel_engine STATIC
    ${CORE_DIR}/src/engine.c
    ${CORE_DIR}/src/state.c
    ${CORE_DIR}/src/story.c
    ${CORE_DIR}/src/scene.c
    ${CORE_DIR}/src/actions.c
    ${CORE_DIR}/src/conditions.c
    ${CORE_DIR}/deps/cjson/cJSON.c
)

target_include_directories(novel_engine PUBLIC
    ${CORE_DIR}/include
    ${CORE_DIR}/deps/cjson
)

# JNI bridge
add_library(novel_engine_jni SHARED
    novel_engine_jni.c
)

target_link_libraries(novel_engine_jni
    novel_engine
    log
)
