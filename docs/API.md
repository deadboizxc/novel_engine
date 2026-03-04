# Novel Engine API Reference

## C Core API (`novel_engine.h`)

### Version

```c
#define NE_VERSION_MAJOR 2
#define NE_VERSION_MINOR 0
#define NE_VERSION_PATCH 0
```

### Types

#### Opaque Handle
```c
typedef struct NE_Engine NE_Engine;
```

#### Result Codes
```c
typedef enum {
    NE_OK = 0,
    NE_ERROR_NULL_PTR,
    NE_ERROR_INVALID_JSON,
    NE_ERROR_INVALID_YAML,
    NE_ERROR_SCENE_NOT_FOUND,
    NE_ERROR_NO_CHOICES,
    NE_ERROR_INVALID_CHOICE,
    NE_ERROR_CONDITION_FAILED,
    NE_ERROR_OUT_OF_MEMORY,
    NE_ERROR_FILE_NOT_FOUND,
    NE_ERROR_IO,
} NE_Result;
```

#### Event Types
```c
typedef enum {
    NE_EVENT_SCENE_ENTER,      // Entering a scene
    NE_EVENT_SCENE_EXIT,       // Leaving a scene
    NE_EVENT_TEXT,             // Text to display
    NE_EVENT_CHOICES_READY,    // Choices available
    NE_EVENT_CHOICE_SELECTED,  // Player made a choice
    NE_EVENT_FLAG_CHANGED,     // Flag value changed
    NE_EVENT_VAR_CHANGED,      // Variable changed
    NE_EVENT_ITEM_CHANGED,     // Inventory changed
    NE_EVENT_COINS_CHANGED,    // Coins (sanity) changed
    NE_EVENT_ANIMATE,          // Play animation effect
    NE_EVENT_SOUND,            // Play sound
    NE_EVENT_MUSIC,            // Play/change music
    NE_EVENT_BACKGROUND,       // Change background
    NE_EVENT_GAME_END,         // Game ended (final scene)
} NE_EventType;
```

### Engine Lifecycle

```c
// Create a new engine instance
NE_Engine* ne_engine_create(void);

// Destroy engine and free resources
void ne_engine_destroy(NE_Engine* engine);

// Get version string
const char* ne_version_string(void);

// Free string returned by engine (e.g., from ne_state_to_json)
void ne_free_string(char* str);
```

### Event Handling

```c
// Event structure
typedef struct {
    NE_EventType type;
    const char* scene_id;
    const char* text;
    const char* name;
    int value;
    float duration;
    bool bool_value;
} NE_Event;

// Event callback signature
typedef void (*NE_EventCallback)(void* user_data, const NE_Event* event);

// Set event callback
void ne_engine_set_callback(NE_Engine* engine, NE_EventCallback cb, void* user_data);
```

### Story Loading

```c
// Load story from JSON string
NE_Result ne_story_load_json(NE_Engine* engine, const char* json_str);

// Load story from YAML string (requires libyaml)
NE_Result ne_story_load_yaml(NE_Engine* engine, const char* yaml_str);

// Load story from file (auto-detect format)
NE_Result ne_story_load_file(NE_Engine* engine, const char* path);

// Load story from directory (multiple files)
NE_Result ne_story_load_dir(NE_Engine* engine, const char* dir_path);

// Check if scene exists
bool ne_story_has_scene(const NE_Engine* engine, const char* scene_id);

// Get number of loaded scenes
int ne_story_scene_count(const NE_Engine* engine);
```

### Game State

```c
// Start new game from scene
NE_Result ne_game_new(NE_Engine* engine, const char* start_scene);

// Reset game state to scene
NE_Result ne_state_reset(NE_Engine* engine, const char* start_scene);

// Get current scene ID
const char* ne_state_current_scene(const NE_Engine* engine);

// Get coins (sanity) value
int ne_state_get_coins(const NE_Engine* engine);

// Get variable value
int ne_state_get_var(const NE_Engine* engine, const char* name);

// Get flag value
bool ne_state_get_flag(const NE_Engine* engine, const char* name);

// Check if item is in inventory
bool ne_state_has_item(const NE_Engine* engine, const char* item);

// Get item count
int ne_state_get_item_count(const NE_Engine* engine, const char* item);
```

### Scene Navigation

```c
// Enter current scene (triggers events)
NE_Result ne_scene_enter(NE_Engine* engine);

// Get scene text
const char* ne_scene_get_text(const NE_Engine* engine);

// Get scene background
const char* ne_scene_get_background(const NE_Engine* engine);

// Get number of available choices
int ne_scene_choice_count(const NE_Engine* engine);

// Get choice text by index
const char* ne_scene_choice_text(const NE_Engine* engine, int index);

// Select a choice (transitions to next scene)
NE_Result ne_scene_select_choice(NE_Engine* engine, int index);

// Check if current scene is final
bool ne_scene_is_final(const NE_Engine* engine);
```

### Save/Load

```c
// Serialize state to JSON string (caller must free with ne_free_string)
char* ne_state_to_json(const NE_Engine* engine);

// Load state from JSON string
NE_Result ne_state_from_json(NE_Engine* engine, const char* json);

// Save state to file
NE_Result ne_state_save_file(const NE_Engine* engine, const char* path);

// Load state from file
NE_Result ne_state_load_file(NE_Engine* engine, const char* path);
```

---

## Python Bindings

### Installation

```bash
cd bindings/python
pip install -e .
```

### Usage

```python
from novel_engine import NovelEngine, TUIRunner

# Create engine
engine = NovelEngine()

# Load story
engine.load_story_dir("stories/blue_frequency")

# Start game
engine.new_game("prologue.start")

# Run TUI
runner = TUIRunner(engine)
runner.run()
```

### NovelEngine Class

```python
class NovelEngine:
    def __init__(self, lib_path: str = None)
    
    # Story loading
    def load_story_json(self, json_str: str) -> bool
    def load_story_dir(self, path: str) -> bool
    
    # Game state
    def new_game(self, start_scene: str) -> None
    def reset_game(self, start_scene: str) -> None
    def enter_scene(self) -> bool
    
    # Scene info
    def get_scene_text(self) -> str
    def get_background(self) -> str
    def get_choice_count(self) -> int
    def get_choice_text(self, index: int) -> str
    def select_choice(self, index: int) -> bool
    def is_game_over(self) -> bool
    
    # State getters
    def get_coins(self) -> int
    def get_var(self, name: str) -> int
    def get_flag(self, name: str) -> bool
    def has_item(self, item: str) -> bool
    def get_item_count(self, item: str) -> int
    
    # Save/Load
    def save_to_json(self) -> str
    def load_from_json(self, json_str: str) -> bool
    def save_to_file(self, path: str) -> bool
    def load_from_file(self, path: str) -> bool
    
    # Events
    def set_event_callback(self, callback: Callable) -> None
```

---

## Kotlin GUI

### Setup

1. Build C core library
2. Build JNI library
3. Run Gradle

```bash
# Build core
cd core && cmake -B build && cmake --build build

# Build JNI
cd bindings/kotlin/jni
cmake -B build && cmake --build build

# Run GUI
cd gui && ./gradlew run
```

### NovelEngine (Kotlin)

```kotlin
class NovelEngine {
    fun loadStory(json: String): Boolean
    fun loadStoryFromFile(path: String): Boolean
    
    fun newGame(startScene: String)
    fun resetGame(startScene: String)
    fun enterScene(): Boolean
    
    fun getCurrentSceneText(): String
    fun getChoices(): List<String>
    fun selectChoice(index: Int): Boolean
    fun isGameOver(): Boolean
    
    fun getCoins(): Int
    fun getVar(name: String): Int
    fun getFlag(name: String): Boolean
    
    fun saveToJson(): String
    fun loadFromJson(json: String): Boolean
    
    fun setEventListener(listener: EngineEventListener?)
    fun close()
}
```

### Event Listener

```kotlin
interface EngineEventListener {
    fun onSceneEnter(sceneId: String)
    fun onTextDisplay(text: String)
    fun onAnimate(type: String, duration: Float)
    fun onCoinsChanged(oldValue: Int, newValue: Int)
    fun onGameEnd(sceneId: String)
    // ... more events
}
```
