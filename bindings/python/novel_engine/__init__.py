"""
Novel Engine — Python bindings via ctypes.

This module provides a Pythonic interface to the C core library.
"""

from .engine import NovelEngine, EngineEvent, EventType
from .tui import TUIRunner

__version__ = "2.0.0"
__all__ = ["NovelEngine", "EngineEvent", "EventType", "TUIRunner"]
