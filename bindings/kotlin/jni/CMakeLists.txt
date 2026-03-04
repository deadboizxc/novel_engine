cmake_minimum_required(VERSION 3.16)
project(novel_engine_jni VERSION 2.0.0 LANGUAGES C)

# Core library include path - use absolute path from cmake current source dir
get_filename_component(PROJECT_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../../.." ABSOLUTE)
set(NOVEL_ENGINE_INCLUDE "${PROJECT_ROOT}/core/include")

message(STATUS "Novel Engine include: ${NOVEL_ENGINE_INCLUDE}")

# Find JNI (works differently on Android vs Desktop)
if(ANDROID)
    # On Android, JNI is part of the NDK
    set(JNI_INCLUDE_DIRS "${ANDROID_NDK}/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include")
else()
    find_package(JNI REQUIRED)
endif()

# Find novel_engine core
if(NOT TARGET novel_engine)
    if(DEFINED NOVEL_ENGINE_LIB)
        # Use provided path
    else()
        find_library(NOVEL_ENGINE_LIB novel_engine HINTS "${CMAKE_SOURCE_DIR}/../../core/build")
    endif()
else()
    set(NOVEL_ENGINE_LIB novel_engine)
endif()

# JNI shared library
add_library(novel_engine_jni SHARED novel_engine_jni.c)

target_include_directories(novel_engine_jni PRIVATE
    ${JNI_INCLUDE_DIRS}
    ${NOVEL_ENGINE_INCLUDE}
)

if(ANDROID)
    target_link_libraries(novel_engine_jni PRIVATE
        ${NOVEL_ENGINE_LIB}
        log
    )
else()
    target_link_libraries(novel_engine_jni PRIVATE
        ${NOVEL_ENGINE_LIB}
        ${JNI_LIBRARIES}
    )
endif()

# Set library output name
set_target_properties(novel_engine_jni PROPERTIES
    OUTPUT_NAME "novel_engine_jni"
    PREFIX "lib"
)

# Platform-specific settings
if(WIN32)
    set_target_properties(novel_engine_jni PROPERTIES
        PREFIX ""
        SUFFIX ".dll"
    )
elseif(APPLE)
    set_target_properties(novel_engine_jni PROPERTIES
        SUFFIX ".dylib"
    )
endif()

# Install rules
install(TARGETS novel_engine_jni
    LIBRARY DESTINATION lib
    RUNTIME DESTINATION bin
)

message(STATUS "JNI include dirs: ${JNI_INCLUDE_DIRS}")
message(STATUS "Novel Engine lib: ${NOVEL_ENGINE_LIB}")
