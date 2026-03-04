# ═══════════════════════════════════════════════════════════════════════════════
# Novel Engine — Core Library CMakeLists.txt
# ═══════════════════════════════════════════════════════════════════════════════

cmake_minimum_required(VERSION 3.16)
project(novel_engine VERSION 2.0.0 LANGUAGES C)

# ─────────────────────────────────────────────────────────────────────────────────
# Options
# ─────────────────────────────────────────────────────────────────────────────────
option(NE_BUILD_SHARED "Build shared library" ON)
option(NE_BUILD_STATIC "Build static library" ON)
option(NE_BUILD_TESTS  "Build tests" ON)
option(NE_BUILD_CLI    "Build CLI executable" ON)

# ─────────────────────────────────────────────────────────────────────────────────
# Compiler settings
# ─────────────────────────────────────────────────────────────────────────────────
set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

if(MSVC)
    add_compile_options(/W4 /WX)
else()
    add_compile_options(-Wall -Wextra -Wpedantic)
    if(CMAKE_BUILD_TYPE STREQUAL "Release")
        add_compile_options(-O2)
    endif()
endif()

# ─────────────────────────────────────────────────────────────────────────────────
# Sources
# ─────────────────────────────────────────────────────────────────────────────────
set(NE_SOURCES
    src/engine.c
    src/state.c
    src/story.c
    src/scene.c
    src/actions.c
    src/conditions.c
    deps/cjson/cJSON.c
)

set(NE_HEADERS
    include/novel_engine.h
    include/internal.h
)

# ─────────────────────────────────────────────────────────────────────────────────
# Shared library
# ─────────────────────────────────────────────────────────────────────────────────
if(NE_BUILD_SHARED)
    add_library(novel_engine SHARED ${NE_SOURCES})
    target_include_directories(novel_engine PUBLIC 
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
        $<INSTALL_INTERFACE:include>
    )
    target_include_directories(novel_engine PRIVATE 
        ${CMAKE_CURRENT_SOURCE_DIR}/deps
    )
    target_compile_definitions(novel_engine PRIVATE NE_BUILD_DLL)
    
    set_target_properties(novel_engine PROPERTIES
        VERSION ${PROJECT_VERSION}
        SOVERSION ${PROJECT_VERSION_MAJOR}
        PUBLIC_HEADER "include/novel_engine.h"
    )
    
    # Symbol visibility
    if(NOT MSVC)
        target_compile_options(novel_engine PRIVATE -fvisibility=hidden)
    endif()
endif()

# ─────────────────────────────────────────────────────────────────────────────────
# Static library
# ─────────────────────────────────────────────────────────────────────────────────
if(NE_BUILD_STATIC)
    add_library(novel_engine_static STATIC ${NE_SOURCES})
    target_include_directories(novel_engine_static PUBLIC 
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
        $<INSTALL_INTERFACE:include>
    )
    target_include_directories(novel_engine_static PRIVATE 
        ${CMAKE_CURRENT_SOURCE_DIR}/deps
    )
    set_target_properties(novel_engine_static PROPERTIES
        OUTPUT_NAME novel_engine
    )
endif()

# ─────────────────────────────────────────────────────────────────────────────────
# Tests
# ─────────────────────────────────────────────────────────────────────────────────
if(NE_BUILD_TESTS)
    enable_testing()
    
    add_executable(test_engine tests/test_engine.c)
    target_link_libraries(test_engine PRIVATE novel_engine_static)
    target_include_directories(test_engine PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/include)
    
    add_test(NAME test_engine COMMAND test_engine)
endif()

# ─────────────────────────────────────────────────────────────────────────────────
# Install
# ─────────────────────────────────────────────────────────────────────────────────
include(GNUInstallDirs)

if(NE_BUILD_SHARED)
    install(TARGETS novel_engine
        LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
        ARCHIVE DESTINATION ${CMAKE_INSTALL_LIBDIR}
        RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR}
        PUBLIC_HEADER DESTINATION ${CMAKE_INSTALL_INCLUDEDIR}
    )
endif()

if(NE_BUILD_STATIC)
    install(TARGETS novel_engine_static
        ARCHIVE DESTINATION ${CMAKE_INSTALL_LIBDIR}
    )
endif()

# ─────────────────────────────────────────────────────────────────────────────────
# Package config
# ─────────────────────────────────────────────────────────────────────────────────
include(CMakePackageConfigHelpers)
write_basic_package_version_file(
    "${CMAKE_CURRENT_BINARY_DIR}/novel_engine-config-version.cmake"
    VERSION ${PROJECT_VERSION}
    COMPATIBILITY SameMajorVersion
)
