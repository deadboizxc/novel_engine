cmake_minimum_required(VERSION 3.16)
project(novel_engine_jni VERSION 2.0.0 LANGUAGES C)

# Find JNI
find_package(JNI REQUIRED)

# Find novel_engine core
if(NOT TARGET novel_engine)
    find_library(NOVEL_ENGINE_LIB novel_engine HINTS "${CMAKE_SOURCE_DIR}/../../core/build")
    set(NOVEL_ENGINE_INCLUDE "${CMAKE_SOURCE_DIR}/../../core/include")
else()
    set(NOVEL_ENGINE_LIB novel_engine)
    set(NOVEL_ENGINE_INCLUDE "${CMAKE_SOURCE_DIR}/../../core/include")
endif()

# JNI shared library
add_library(novel_engine_jni SHARED novel_engine_jni.c)

target_include_directories(novel_engine_jni PRIVATE
    ${JNI_INCLUDE_DIRS}
    ${NOVEL_ENGINE_INCLUDE}
)

target_link_libraries(novel_engine_jni PRIVATE
    ${NOVEL_ENGINE_LIB}
    ${JNI_LIBRARIES}
)

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
