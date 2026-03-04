"""
Novel Engine — Terminal User Interface (TUI) runner.

Provides a simple terminal-based interface for playing visual novels.
"""

import os
import sys
import time
from typing import Optional

from .engine import NovelEngine, EngineEvent, EventType


class TUIRunner:
    """
    Terminal-based visual novel player.
    
    Example:
        runner = TUIRunner("stories/blue_frequency")
        runner.run()
    """
    
    def __init__(
        self,
        story_path: str,
        start_scene: str = "prologue.start",
        type_delay: float = 0.02,
        save_dir: str = "saves",
    ):
        """
        Initialize TUI runner.
        
        Args:
            story_path: Path to story directory or file
            start_scene: Starting scene ID
            type_delay: Delay between characters (typewriter effect)
            save_dir: Directory for save files
        """
        self.engine = NovelEngine()
        self.story_path = story_path
        self.start_scene = start_scene
        self.type_delay = type_delay
        self.save_dir = save_dir
        
        # Create save directory
        os.makedirs(save_dir, exist_ok=True)
        
        # Register event handlers
        self.engine.add_event_handler(self._on_event)
        
        # Load story
        if os.path.isdir(story_path):
            self.engine.load_story_dir(story_path)
        else:
            self.engine.load_story_file(story_path)
    
    def _clear_screen(self):
        """Clear terminal screen."""
        os.system("cls" if os.name == "nt" else "clear")
    
    def _type_text(self, text: str, delay: Optional[float] = None):
        """Print text with typewriter effect."""
        delay = delay if delay is not None else self.type_delay
        
        for char in text:
            sys.stdout.write(char)
            sys.stdout.flush()
            time.sleep(delay)
        
        print()  # Newline
    
    def _on_event(self, event: EngineEvent):
        """Handle engine events."""
        if event.type == EventType.ANIMATE:
            self._play_animation(event.anim_type, event.duration)
        elif event.type == EventType.COINS_CHANGED:
            diff = event.int_value - event.old_int_value
            sign = "+" if diff >= 0 else ""
            print(f"  💰 {sign}{diff} монет → {event.int_value}")
        elif event.type == EventType.FLAG_CHANGED:
            status = "✓" if event.bool_value else "✗"
            print(f"  {status} Флаг: {event.name}")
    
    def _play_animation(self, anim_type: Optional[str], duration: float):
        """Play terminal animation."""
        if not anim_type:
            return
        
        import itertools
        import random
        
        end_time = time.time() + duration
        
        if anim_type == "spinner":
            spinner = itertools.cycle("/-\\|")
            while time.time() < end_time:
                sys.stdout.write(f"\r  {next(spinner)} ")
                sys.stdout.flush()
                time.sleep(0.1)
            sys.stdout.write("\r    \r")
        
        elif anim_type == "dots":
            dots = ["", ".", "..", "..."]
            i = 0
            while time.time() < end_time:
                sys.stdout.write(f"\r  {dots[i % 4]}   ")
                sys.stdout.flush()
                i += 1
                time.sleep(0.4)
            sys.stdout.write("\r       \r")
        
        elif anim_type == "glitch":
            chars = "█▓▒░"
            text = "ЗАГРУЗКА"
            while time.time() < end_time:
                glitch = ''.join(random.choice([c, random.choice(chars)]) for c in text)
                sys.stdout.write(f"\r  {glitch}")
                sys.stdout.flush()
                time.sleep(0.1)
            sys.stdout.write("\r" + " " * 20 + "\r")
        
        elif anim_type == "static":
            chars = ".▄▀█ "
            while time.time() < end_time:
                line = ''.join(random.choice(chars) for _ in range(30))
                sys.stdout.write(f"\r  {line}")
                sys.stdout.flush()
                time.sleep(0.05)
            sys.stdout.write("\r" + " " * 35 + "\r")
        
        sys.stdout.flush()
    
    def _prompt_choice(self) -> str:
        """Display choices and get user input."""
        choices = self.engine.choices
        
        print()
        for i, choice in enumerate(choices, 1):
            print(f"  {i}) {choice}")
        
        print()
        print("  [s] Сохранить  [l] Загрузить  [q] Выйти")
        
        return input("\n  > ").strip().lower()
    
    def _prompt_save(self):
        """Save game dialog."""
        self._clear_screen()
        print("═══════════════════════════════════════")
        print("  СОХРАНЕНИЕ")
        print("═══════════════════════════════════════\n")
        
        saves = self._list_saves()
        if saves:
            print("  Существующие сохранения:")
            for i, name in enumerate(saves, 1):
                print(f"    {i}) {name}")
            print()
        
        print("  [n] Новое сохранение")
        print("  [b] Назад")
        
        choice = input("\n  > ").strip().lower()
        
        if choice == "n":
            name = input("  Имя сохранения: ").strip()
            if name:
                path = os.path.join(self.save_dir, f"{name}.json")
                self.engine.save_to_file(path)
                print(f"  ✓ Сохранено: {path}")
                time.sleep(1)
        elif choice == "b":
            return
        else:
            try:
                idx = int(choice) - 1
                if 0 <= idx < len(saves):
                    path = os.path.join(self.save_dir, saves[idx])
                    self.engine.save_to_file(path)
                    print(f"  ✓ Перезаписано: {path}")
                    time.sleep(1)
            except (ValueError, IndexError):
                pass
    
    def _prompt_load(self) -> bool:
        """Load game dialog. Returns True if game was loaded."""
        self._clear_screen()
        print("═══════════════════════════════════════")
        print("  ЗАГРУЗКА")
        print("═══════════════════════════════════════\n")
        
        saves = self._list_saves()
        if not saves:
            print("  Нет сохранений")
            time.sleep(1)
            return False
        
        for i, name in enumerate(saves, 1):
            print(f"  {i}) {name}")
        
        print("\n  [b] Назад")
        
        choice = input("\n  > ").strip().lower()
        
        if choice == "b":
            return False
        
        try:
            idx = int(choice) - 1
            if 0 <= idx < len(saves):
                path = os.path.join(self.save_dir, saves[idx])
                self.engine.load_from_file(path)
                print(f"  ✓ Загружено: {path}")
                time.sleep(1)
                return True
        except (ValueError, IndexError, FileNotFoundError):
            print("  ✗ Ошибка загрузки")
            time.sleep(1)
        
        return False
    
    def _list_saves(self) -> list:
        """List save files."""
        if not os.path.exists(self.save_dir):
            return []
        return [f for f in os.listdir(self.save_dir) if f.endswith('.json')]
    
    def run(self):
        """Main game loop."""
        # Check for existing saves
        saves = self._list_saves()
        if saves:
            print("Загрузить сохранение? (y/n): ", end="")
            if input().strip().lower() == "y":
                if self._prompt_load():
                    pass  # Continue from loaded state
                else:
                    self.engine.new_game(self.start_scene)
            else:
                self.engine.new_game(self.start_scene)
        else:
            self.engine.new_game(self.start_scene)
        
        while True:
            self._clear_screen()
            
            # Header
            scene_id = self.engine.current_scene or "???"
            coins = self.engine.coins
            print(f"═══════════════════════════════════════")
            print(f"  {scene_id}  |  💰 {coins}")
            print(f"═══════════════════════════════════════\n")
            
            # Scene text
            text = self.engine.scene_text
            if text:
                self._type_text(text)
            
            # Check if game ended
            if self.engine.is_final:
                print("\n" + "═" * 40)
                print("  КОНЕЦ")
                print("═" * 40)
                input("\n  Нажмите Enter...")
                break
            
            # Get user choice
            user_input = self._prompt_choice()
            
            if user_input == "s":
                self._prompt_save()
                continue
            elif user_input == "l":
                self._prompt_load()
                continue
            elif user_input == "q":
                save = input("  Сохранить перед выходом? (y/n): ").strip().lower()
                if save == "y":
                    self._prompt_save()
                break
            
            # Select choice
            try:
                idx = int(user_input) - 1
                if 0 <= idx < self.engine.choice_count:
                    self.engine.select_choice(idx)
                else:
                    print("  Неверный выбор")
                    time.sleep(0.5)
            except ValueError:
                print("  Неверный ввод")
                time.sleep(0.5)


def main():
    """CLI entry point."""
    import argparse
    
    parser = argparse.ArgumentParser(description="Novel Engine TUI")
    parser.add_argument("story", nargs="?", default="stories",
                       help="Path to story directory or file")
    parser.add_argument("--start", default="prologue.start",
                       help="Starting scene ID")
    parser.add_argument("--delay", type=float, default=0.02,
                       help="Type delay in seconds")
    parser.add_argument("--saves", default="saves",
                       help="Save directory")
    parser.add_argument("--version", action="store_true",
                       help="Show version")
    
    args = parser.parse_args()
    
    if args.version:
        print(f"Novel Engine v{NovelEngine.version()}")
        return
    
    runner = TUIRunner(
        story_path=args.story,
        start_scene=args.start,
        type_delay=args.delay,
        save_dir=args.saves,
    )
    
    try:
        runner.run()
    except KeyboardInterrupt:
        print("\n\n  Прервано.")


if __name__ == "__main__":
    main()
