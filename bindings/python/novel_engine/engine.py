"""
Novel Engine — ctypes bindings to C library.

This module provides low-level ctypes bindings and a high-level Pythonic wrapper.
"""

import ctypes
import ctypes.util
import os
import sys
from dataclasses import dataclass
from enum import IntEnum
from pathlib import Path
from typing import Callable, Optional, List


# ═══════════════════════════════════════════════════════════════════════════════
# RESULT CODES
# ═══════════════════════════════════════════════════════════════════════════════

class Result(IntEnum):
    """Engine result codes."""
    OK = 0
    ERROR_NULL_PTR = 1
    ERROR_INVALID_JSON = 2
    ERROR_INVALID_YAML = 3
    ERROR_SCENE_NOT_FOUND = 4
    ERROR_NO_CHOICES = 5
    ERROR_INVALID_CHOICE = 6
    ERROR_CONDITION_FAILED = 7
    ERROR_OUT_OF_MEMORY = 8
    ERROR_FILE_NOT_FOUND = 9
    ERROR_IO = 10
    ERROR_NO_STORY = 11
    ERROR_NO_STATE = 12


# ═══════════════════════════════════════════════════════════════════════════════
# EVENT TYPES
# ═══════════════════════════════════════════════════════════════════════════════

class EventType(IntEnum):
    """Engine event types."""
    SCENE_ENTER = 0
    SCENE_EXIT = 1
    TEXT = 2
    CHOICES_READY = 3
    ACTION = 4
    FLAG_CHANGED = 5
    VAR_CHANGED = 6
    ITEM_CHANGED = 7
    COINS_CHANGED = 8
    ANIMATE = 9
    SOUND = 10
    MUSIC = 11
    BACKGROUND = 12
    GAME_END = 13


# ═══════════════════════════════════════════════════════════════════════════════
# EVENT DATA
# ═══════════════════════════════════════════════════════════════════════════════

@dataclass
class EngineEvent:
    """Event data from engine."""
    type: EventType
    scene_id: Optional[str] = None
    text: Optional[str] = None
    name: Optional[str] = None
    int_value: int = 0
    old_int_value: int = 0
    bool_value: bool = False
    anim_type: Optional[str] = None
    duration: float = 0.0


# ═══════════════════════════════════════════════════════════════════════════════
# CTYPES STRUCTURES
# ═══════════════════════════════════════════════════════════════════════════════

class _NE_Event(ctypes.Structure):
    """C NE_Event structure."""
    _fields_ = [
        ("type", ctypes.c_int),
        ("scene_id", ctypes.c_char_p),
        ("text", ctypes.c_char_p),
        ("name", ctypes.c_char_p),
        ("int_value", ctypes.c_int32),
        ("old_int_value", ctypes.c_int32),
        ("bool_value", ctypes.c_bool),
        ("anim_type", ctypes.c_char_p),
        ("duration", ctypes.c_float),
    ]


# Callback type
_EventCallbackType = ctypes.CFUNCTYPE(
    None, 
    ctypes.c_void_p,  # user_data
    ctypes.POINTER(_NE_Event)  # event
)


# ═══════════════════════════════════════════════════════════════════════════════
# LIBRARY LOADING
# ═══════════════════════════════════════════════════════════════════════════════

def _find_library() -> str:
    """Find the novel_engine shared library."""
    # Try various paths
    candidates = [
        # Development paths
        Path(__file__).parent.parent.parent.parent / "core" / "build" / "libnovel_engine.so",
        Path(__file__).parent.parent.parent.parent / "core" / "build" / "libnovel_engine.dylib",
        Path(__file__).parent.parent.parent.parent / "core" / "build" / "novel_engine.dll",
        Path(__file__).parent.parent.parent.parent / "core" / "build" / "Release" / "novel_engine.dll",
        # Installed paths
        Path("/usr/local/lib/libnovel_engine.so"),
        Path("/usr/lib/libnovel_engine.so"),
    ]
    
    for path in candidates:
        if path.exists():
            return str(path)
    
    # Try system library path
    lib = ctypes.util.find_library("novel_engine")
    if lib:
        return lib
    
    raise FileNotFoundError(
        "Cannot find libnovel_engine. Build it with:\n"
        "  cd core && cmake -B build && cmake --build build"
    )


def _load_library() -> ctypes.CDLL:
    """Load and configure the C library."""
    lib = ctypes.CDLL(_find_library())
    
    # ne_version_string
    lib.ne_version_string.argtypes = []
    lib.ne_version_string.restype = ctypes.c_char_p
    
    # ne_result_string
    lib.ne_result_string.argtypes = [ctypes.c_int]
    lib.ne_result_string.restype = ctypes.c_char_p
    
    # ne_engine_create
    lib.ne_engine_create.argtypes = []
    lib.ne_engine_create.restype = ctypes.c_void_p
    
    # ne_engine_destroy
    lib.ne_engine_destroy.argtypes = [ctypes.c_void_p]
    lib.ne_engine_destroy.restype = None
    
    # ne_engine_set_callback
    lib.ne_engine_set_callback.argtypes = [
        ctypes.c_void_p, _EventCallbackType, ctypes.c_void_p
    ]
    lib.ne_engine_set_callback.restype = None
    
    # ne_story_load_json
    lib.ne_story_load_json.argtypes = [ctypes.c_void_p, ctypes.c_char_p]
    lib.ne_story_load_json.restype = ctypes.c_int
    
    # ne_story_load_file
    lib.ne_story_load_file.argtypes = [ctypes.c_void_p, ctypes.c_char_p]
    lib.ne_story_load_file.restype = ctypes.c_int
    
    # ne_story_load_dir
    lib.ne_story_load_dir.argtypes = [ctypes.c_void_p, ctypes.c_char_p]
    lib.ne_story_load_dir.restype = ctypes.c_int
    
    # ne_story_scene_count
    lib.ne_story_scene_count.argtypes = [ctypes.c_void_p]
    lib.ne_story_scene_count.restype = ctypes.c_int
    
    # ne_story_has_scene
    lib.ne_story_has_scene.argtypes = [ctypes.c_void_p, ctypes.c_char_p]
    lib.ne_story_has_scene.restype = ctypes.c_bool
    
    # ne_game_new
    lib.ne_game_new.argtypes = [ctypes.c_void_p, ctypes.c_char_p]
    lib.ne_game_new.restype = ctypes.c_int
    
    # ne_game_reset
    lib.ne_game_reset.argtypes = [ctypes.c_void_p, ctypes.c_char_p]
    lib.ne_game_reset.restype = ctypes.c_int
    
    # ne_state_to_json
    lib.ne_state_to_json.argtypes = [ctypes.c_void_p]
    lib.ne_state_to_json.restype = ctypes.c_char_p
    
    # ne_state_from_json
    lib.ne_state_from_json.argtypes = [ctypes.c_void_p, ctypes.c_char_p]
    lib.ne_state_from_json.restype = ctypes.c_int
    
    # ne_state_save_file
    lib.ne_state_save_file.argtypes = [ctypes.c_void_p, ctypes.c_char_p]
    lib.ne_state_save_file.restype = ctypes.c_int
    
    # ne_state_load_file
    lib.ne_state_load_file.argtypes = [ctypes.c_void_p, ctypes.c_char_p]
    lib.ne_state_load_file.restype = ctypes.c_int
    
    # ne_state_current_scene
    lib.ne_state_current_scene.argtypes = [ctypes.c_void_p]
    lib.ne_state_current_scene.restype = ctypes.c_char_p
    
    # ne_state_get_coins
    lib.ne_state_get_coins.argtypes = [ctypes.c_void_p]
    lib.ne_state_get_coins.restype = ctypes.c_int
    
    # ne_state_get_var
    lib.ne_state_get_var.argtypes = [ctypes.c_void_p, ctypes.c_char_p]
    lib.ne_state_get_var.restype = ctypes.c_int
    
    # ne_state_get_flag
    lib.ne_state_get_flag.argtypes = [ctypes.c_void_p, ctypes.c_char_p]
    lib.ne_state_get_flag.restype = ctypes.c_bool
    
    # ne_state_get_item
    lib.ne_state_get_item.argtypes = [ctypes.c_void_p, ctypes.c_char_p]
    lib.ne_state_get_item.restype = ctypes.c_int
    
    # ne_state_has_item
    lib.ne_state_has_item.argtypes = [ctypes.c_void_p, ctypes.c_char_p]
    lib.ne_state_has_item.restype = ctypes.c_bool
    
    # ne_scene_enter
    lib.ne_scene_enter.argtypes = [ctypes.c_void_p]
    lib.ne_scene_enter.restype = ctypes.c_int
    
    # ne_scene_get_text
    lib.ne_scene_get_text.argtypes = [ctypes.c_void_p]
    lib.ne_scene_get_text.restype = ctypes.c_char_p
    
    # ne_scene_get_background
    lib.ne_scene_get_background.argtypes = [ctypes.c_void_p]
    lib.ne_scene_get_background.restype = ctypes.c_char_p
    
    # ne_scene_choice_count
    lib.ne_scene_choice_count.argtypes = [ctypes.c_void_p]
    lib.ne_scene_choice_count.restype = ctypes.c_int
    
    # ne_scene_choice_text
    lib.ne_scene_choice_text.argtypes = [ctypes.c_void_p, ctypes.c_int]
    lib.ne_scene_choice_text.restype = ctypes.c_char_p
    
    # ne_scene_select
    lib.ne_scene_select.argtypes = [ctypes.c_void_p, ctypes.c_int]
    lib.ne_scene_select.restype = ctypes.c_int
    
    # ne_scene_is_final
    lib.ne_scene_is_final.argtypes = [ctypes.c_void_p]
    lib.ne_scene_is_final.restype = ctypes.c_bool
    
    # ne_free_string
    lib.ne_free_string.argtypes = [ctypes.c_char_p]
    lib.ne_free_string.restype = None
    
    return lib


# ═══════════════════════════════════════════════════════════════════════════════
# HIGH-LEVEL WRAPPER
# ═══════════════════════════════════════════════════════════════════════════════

class NovelEngine:
    """
    High-level Python wrapper for the Novel Engine.
    
    Example:
        engine = NovelEngine()
        engine.load_story_dir("stories/blue_frequency")
        engine.new_game("prologue.start")
        
        while not engine.is_final:
            print(engine.scene_text)
            for i, choice in enumerate(engine.choices):
                print(f"  {i+1}) {choice}")
            
            idx = int(input("> ")) - 1
            engine.select_choice(idx)
    """
    
    _lib: Optional[ctypes.CDLL] = None
    
    def __init__(self):
        """Create a new engine instance."""
        if NovelEngine._lib is None:
            NovelEngine._lib = _load_library()
        
        self._handle = self._lib.ne_engine_create()
        if not self._handle:
            raise MemoryError("Failed to create engine")
        
        self._event_handlers: List[Callable[[EngineEvent], None]] = []
        self._callback = None
        self._setup_callback()
    
    def __del__(self):
        """Destroy engine instance."""
        if hasattr(self, '_handle') and self._handle:
            self._lib.ne_engine_destroy(self._handle)
            self._handle = None
    
    def _setup_callback(self):
        """Set up event callback."""
        def callback(user_data, event_ptr):
            if not event_ptr:
                return
            event = event_ptr.contents
            py_event = EngineEvent(
                type=EventType(event.type),
                scene_id=event.scene_id.decode('utf-8') if event.scene_id else None,
                text=event.text.decode('utf-8') if event.text else None,
                name=event.name.decode('utf-8') if event.name else None,
                int_value=event.int_value,
                old_int_value=event.old_int_value,
                bool_value=event.bool_value,
                anim_type=event.anim_type.decode('utf-8') if event.anim_type else None,
                duration=event.duration,
            )
            for handler in self._event_handlers:
                handler(py_event)
        
        self._callback = _EventCallbackType(callback)
        self._lib.ne_engine_set_callback(self._handle, self._callback, None)
    
    def add_event_handler(self, handler: Callable[[EngineEvent], None]):
        """Add event handler."""
        self._event_handlers.append(handler)
    
    def remove_event_handler(self, handler: Callable[[EngineEvent], None]):
        """Remove event handler."""
        self._event_handlers.remove(handler)
    
    # ─────────────────────────────────────────────────────────────────────────────
    # Story loading
    # ─────────────────────────────────────────────────────────────────────────────
    
    def load_story_json(self, json_str: str) -> None:
        """Load story from JSON string."""
        result = self._lib.ne_story_load_json(
            self._handle, json_str.encode('utf-8')
        )
        if result != Result.OK:
            raise ValueError(f"Failed to load story: {Result(result).name}")
    
    def load_story_file(self, path: str) -> None:
        """Load story from file."""
        result = self._lib.ne_story_load_file(
            self._handle, path.encode('utf-8')
        )
        if result != Result.OK:
            raise FileNotFoundError(f"Failed to load story: {path}")
    
    def load_story_dir(self, path: str) -> None:
        """Load story from directory."""
        result = self._lib.ne_story_load_dir(
            self._handle, path.encode('utf-8')
        )
        if result != Result.OK:
            raise FileNotFoundError(f"Failed to load stories from: {path}")
    
    @property
    def scene_count(self) -> int:
        """Get number of scenes in story."""
        return self._lib.ne_story_scene_count(self._handle)
    
    def has_scene(self, scene_id: str) -> bool:
        """Check if scene exists."""
        return self._lib.ne_story_has_scene(
            self._handle, scene_id.encode('utf-8')
        )
    
    # ─────────────────────────────────────────────────────────────────────────────
    # Game state
    # ─────────────────────────────────────────────────────────────────────────────
    
    def new_game(self, start_scene: str = "prologue.start") -> None:
        """Start new game."""
        result = self._lib.ne_game_new(
            self._handle, start_scene.encode('utf-8')
        )
        if result != Result.OK:
            raise ValueError(f"Failed to start game: {Result(result).name}")
        
        # Enter first scene
        self._lib.ne_scene_enter(self._handle)
    
    def reset(self, start_scene: str = "prologue.start") -> None:
        """Reset game to start scene."""
        result = self._lib.ne_game_reset(
            self._handle, start_scene.encode('utf-8')
        )
        if result != Result.OK:
            raise ValueError(f"Failed to reset game: {Result(result).name}")
    
    @property
    def current_scene(self) -> Optional[str]:
        """Get current scene ID."""
        result = self._lib.ne_state_current_scene(self._handle)
        return result.decode('utf-8') if result else None
    
    @property
    def coins(self) -> int:
        """Get coins count."""
        return self._lib.ne_state_get_coins(self._handle)
    
    def get_var(self, name: str) -> int:
        """Get variable value."""
        return self._lib.ne_state_get_var(
            self._handle, name.encode('utf-8')
        )
    
    def get_flag(self, name: str) -> bool:
        """Get flag value."""
        return self._lib.ne_state_get_flag(
            self._handle, name.encode('utf-8')
        )
    
    def get_item(self, item: str) -> int:
        """Get item count."""
        return self._lib.ne_state_get_item(
            self._handle, item.encode('utf-8')
        )
    
    def has_item(self, item: str) -> bool:
        """Check if player has item."""
        return self._lib.ne_state_has_item(
            self._handle, item.encode('utf-8')
        )
    
    # ─────────────────────────────────────────────────────────────────────────────
    # Save/Load
    # ─────────────────────────────────────────────────────────────────────────────
    
    def save_to_json(self) -> str:
        """Serialize state to JSON."""
        result = self._lib.ne_state_to_json(self._handle)
        if not result:
            return "{}"
        json_str = result.decode('utf-8')
        self._lib.ne_free_string(result)
        return json_str
    
    def load_from_json(self, json_str: str) -> None:
        """Load state from JSON."""
        result = self._lib.ne_state_from_json(
            self._handle, json_str.encode('utf-8')
        )
        if result != Result.OK:
            raise ValueError(f"Failed to load state: {Result(result).name}")
    
    def save_to_file(self, path: str) -> None:
        """Save state to file."""
        result = self._lib.ne_state_save_file(
            self._handle, path.encode('utf-8')
        )
        if result != Result.OK:
            raise IOError(f"Failed to save state to: {path}")
    
    def load_from_file(self, path: str) -> None:
        """Load state from file."""
        result = self._lib.ne_state_load_file(
            self._handle, path.encode('utf-8')
        )
        if result != Result.OK:
            raise FileNotFoundError(f"Failed to load state from: {path}")
    
    # ─────────────────────────────────────────────────────────────────────────────
    # Scene interaction
    # ─────────────────────────────────────────────────────────────────────────────
    
    @property
    def scene_text(self) -> Optional[str]:
        """Get current scene text."""
        result = self._lib.ne_scene_get_text(self._handle)
        return result.decode('utf-8') if result else None
    
    @property
    def background(self) -> Optional[str]:
        """Get current scene background."""
        result = self._lib.ne_scene_get_background(self._handle)
        return result.decode('utf-8') if result else None
    
    @property
    def choice_count(self) -> int:
        """Get number of available choices."""
        return self._lib.ne_scene_choice_count(self._handle)
    
    def get_choice_text(self, index: int) -> Optional[str]:
        """Get choice text by index."""
        result = self._lib.ne_scene_choice_text(self._handle, index)
        return result.decode('utf-8') if result else None
    
    @property
    def choices(self) -> List[str]:
        """Get all available choices."""
        return [
            self.get_choice_text(i) or ""
            for i in range(self.choice_count)
        ]
    
    def select_choice(self, index: int) -> None:
        """Select choice and transition to next scene."""
        result = self._lib.ne_scene_select(self._handle, index)
        if result not in (Result.OK, ):
            if result == Result.ERROR_INVALID_CHOICE:
                raise IndexError(f"Invalid choice index: {index}")
            # Game end is not an error
    
    @property
    def is_final(self) -> bool:
        """Check if current scene is final."""
        return self._lib.ne_scene_is_final(self._handle)
    
    # ─────────────────────────────────────────────────────────────────────────────
    # Utilities
    # ─────────────────────────────────────────────────────────────────────────────
    
    @classmethod
    def version(cls) -> str:
        """Get engine version."""
        if cls._lib is None:
            cls._lib = _load_library()
        return cls._lib.ne_version_string().decode('utf-8')
