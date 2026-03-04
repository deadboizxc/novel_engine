/**
 * @file test_engine.c
 * @brief Novel Engine — Basic unit tests
 */

#include "novel_engine.h"
#include <stdio.h>
#include <string.h>
#include <assert.h>

/* ═══════════════════════════════════════════════════════════════════════════════
 * TEST HELPERS
 * ═══════════════════════════════════════════════════════════════════════════════ */

static int tests_run = 0;
static int tests_passed = 0;

#define TEST(name) \
    static void test_##name(void); \
    static void run_test_##name(void) { \
        tests_run++; \
        printf("  [TEST] %s... ", #name); \
        test_##name(); \
        tests_passed++; \
        printf("OK\n"); \
    } \
    static void test_##name(void)

#define ASSERT(cond) \
    do { \
        if (!(cond)) { \
            printf("FAILED\n"); \
            printf("    Assertion failed: %s\n", #cond); \
            printf("    at %s:%d\n", __FILE__, __LINE__); \
            return; \
        } \
    } while (0)

#define ASSERT_EQ(a, b) ASSERT((a) == (b))
#define ASSERT_STR_EQ(a, b) ASSERT(strcmp((a), (b)) == 0)

/* ═══════════════════════════════════════════════════════════════════════════════
 * TEST: Version
 * ═══════════════════════════════════════════════════════════════════════════════ */

TEST(version) {
    const char* ver = ne_version_string();
    ASSERT(ver != NULL);
    ASSERT_STR_EQ(ver, NE_VERSION_STRING);
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * TEST: Engine create/destroy
 * ═══════════════════════════════════════════════════════════════════════════════ */

TEST(engine_lifecycle) {
    NE_Engine* e = ne_engine_create();
    ASSERT(e != NULL);
    
    /* Should have 0 scenes initially */
    ASSERT_EQ(ne_story_scene_count(e), 0);
    
    ne_engine_destroy(e);
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * TEST: Load simple story
 * ═══════════════════════════════════════════════════════════════════════════════ */

static const char* SIMPLE_STORY = 
    "{"
    "  \"test.start\": {"
    "    \"text\": \"Hello, world!\","
    "    \"choices\": ["
    "      { \"text\": \"Continue\", \"next\": \"end\" }"
    "    ]"
    "  },"
    "  \"test.end\": {"
    "    \"text\": \"The end.\","
    "    \"final\": true"
    "  }"
    "}";

TEST(story_load_json) {
    NE_Engine* e = ne_engine_create();
    ASSERT(e != NULL);
    
    NE_Result r = ne_story_load_json(e, SIMPLE_STORY);
    ASSERT_EQ(r, NE_OK);
    
    ASSERT_EQ(ne_story_scene_count(e), 2);
    ASSERT(ne_story_has_scene(e, "test.start"));
    ASSERT(ne_story_has_scene(e, "test.end"));
    ASSERT(!ne_story_has_scene(e, "nonexistent"));
    
    ne_engine_destroy(e);
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * TEST: Game state
 * ═══════════════════════════════════════════════════════════════════════════════ */

TEST(game_state) {
    NE_Engine* e = ne_engine_create();
    ne_story_load_json(e, SIMPLE_STORY);
    
    NE_Result r = ne_game_new(e, "test.start");
    ASSERT_EQ(r, NE_OK);
    
    const char* scene = ne_state_current_scene(e);
    ASSERT(scene != NULL);
    ASSERT_STR_EQ(scene, "test.start");
    
    /* Starting coins = 100 */
    ASSERT_EQ(ne_state_get_coins(e), 100);
    
    ne_engine_destroy(e);
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * TEST: Scene enter and text
 * ═══════════════════════════════════════════════════════════════════════════════ */

TEST(scene_enter) {
    NE_Engine* e = ne_engine_create();
    ne_story_load_json(e, SIMPLE_STORY);
    ne_game_new(e, "test.start");
    
    NE_Result r = ne_scene_enter(e);
    ASSERT_EQ(r, NE_OK);
    
    const char* text = ne_scene_get_text(e);
    ASSERT(text != NULL);
    ASSERT_STR_EQ(text, "Hello, world!");
    
    ASSERT_EQ(ne_scene_choice_count(e), 1);
    
    const char* choice = ne_scene_choice_text(e, 0);
    ASSERT(choice != NULL);
    ASSERT_STR_EQ(choice, "Continue");
    
    ne_engine_destroy(e);
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * TEST: Choice selection
 * ═══════════════════════════════════════════════════════════════════════════════ */

TEST(choice_select) {
    NE_Engine* e = ne_engine_create();
    ne_story_load_json(e, SIMPLE_STORY);
    ne_game_new(e, "test.start");
    ne_scene_enter(e);
    
    NE_Result r = ne_scene_select(e, 0);
    ASSERT_EQ(r, NE_OK);
    
    const char* scene = ne_state_current_scene(e);
    ASSERT_STR_EQ(scene, "test.end");
    
    ASSERT(ne_scene_is_final(e));
    
    ne_engine_destroy(e);
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * TEST: State serialization
 * ═══════════════════════════════════════════════════════════════════════════════ */

TEST(state_serialization) {
    NE_Engine* e = ne_engine_create();
    ne_story_load_json(e, SIMPLE_STORY);
    ne_game_new(e, "test.start");
    
    char* json = ne_state_to_json(e);
    ASSERT(json != NULL);
    
    /* Check JSON contains expected data */
    ASSERT(strstr(json, "\"current\"") != NULL);
    ASSERT(strstr(json, "test.start") != NULL);
    
    /* Load back */
    NE_Engine* e2 = ne_engine_create();
    ne_story_load_json(e2, SIMPLE_STORY);
    NE_Result r = ne_state_from_json(e2, json);
    ASSERT_EQ(r, NE_OK);
    
    ASSERT_STR_EQ(ne_state_current_scene(e2), "test.start");
    ASSERT_EQ(ne_state_get_coins(e2), 100);
    
    ne_free_string(json);
    ne_engine_destroy(e);
    ne_engine_destroy(e2);
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * TEST: Actions
 * ═══════════════════════════════════════════════════════════════════════════════ */

static const char* ACTION_STORY = 
    "{"
    "  \"test.start\": {"
    "    \"text\": \"Start\","
    "    \"actions\": ["
    "      { \"add_coin\": 50 },"
    "      { \"set_flag\": \"visited\" },"
    "      { \"set_var\": { \"count\": 5 } }"
    "    ],"
    "    \"choices\": ["
    "      { \"text\": \"Next\", \"next\": \"end\" }"
    "    ]"
    "  },"
    "  \"test.end\": {"
    "    \"text\": \"End\","
    "    \"final\": true"
    "  }"
    "}";

TEST(actions) {
    NE_Engine* e = ne_engine_create();
    ne_story_load_json(e, ACTION_STORY);
    ne_game_new(e, "test.start");
    ne_scene_enter(e);
    
    /* Check actions executed */
    ASSERT_EQ(ne_state_get_coins(e), 150);  /* 100 + 50 */
    ASSERT(ne_state_get_flag(e, "visited"));
    ASSERT_EQ(ne_state_get_var(e, "count"), 5);
    
    ne_engine_destroy(e);
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * TEST: Conditions
 * ═══════════════════════════════════════════════════════════════════════════════ */

static const char* CONDITION_STORY = 
    "{"
    "  \"test.start\": {"
    "    \"text\": \"Start\","
    "    \"choices\": ["
    "      { \"text\": \"Free\", \"next\": \"end\" },"
    "      { \"text\": \"Paid\", \"next\": \"end\", \"conditions\": [{ \"coins\": 200 }] },"
    "      { \"text\": \"Secret\", \"next\": \"end\", \"conditions\": [{ \"flag\": \"secret\" }] }"
    "    ]"
    "  },"
    "  \"test.end\": {"
    "    \"text\": \"End\","
    "    \"final\": true"
    "  }"
    "}";

TEST(conditions) {
    NE_Engine* e = ne_engine_create();
    ne_story_load_json(e, CONDITION_STORY);
    ne_game_new(e, "test.start");
    ne_scene_enter(e);
    
    /* Only "Free" choice should be available (coins=100, no secret flag) */
    ASSERT_EQ(ne_scene_choice_count(e), 1);
    ASSERT_STR_EQ(ne_scene_choice_text(e, 0), "Free");
    
    ne_engine_destroy(e);
}

/* ═══════════════════════════════════════════════════════════════════════════════
 * MAIN
 * ═══════════════════════════════════════════════════════════════════════════════ */

int main(void) {
    printf("\n");
    printf("═══════════════════════════════════════════════════════════════\n");
    printf("  Novel Engine — Unit Tests\n");
    printf("═══════════════════════════════════════════════════════════════\n\n");
    
    run_test_version();
    run_test_engine_lifecycle();
    run_test_story_load_json();
    run_test_game_state();
    run_test_scene_enter();
    run_test_choice_select();
    run_test_state_serialization();
    run_test_actions();
    run_test_conditions();
    
    printf("\n═══════════════════════════════════════════════════════════════\n");
    printf("  Results: %d/%d tests passed\n", tests_passed, tests_run);
    printf("═══════════════════════════════════════════════════════════════\n\n");
    
    return tests_passed == tests_run ? 0 : 1;
}
