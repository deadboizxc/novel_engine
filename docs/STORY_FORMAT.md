# Story Format Specification

## Overview

Novel Engine stories are written in JSON or YAML format. Each story consists of:
- **Scenes** — Individual narrative moments with text and choices
- **Choices** — Player decisions that lead to other scenes
- **Actions** — State changes (coins, flags, variables, items)
- **Conditions** — Requirements for choices or actions to be available

## File Structure

### Single File

```json
{
  "scene_id": { ... },
  "another_scene": { ... }
}
```

### Directory Structure

```
story_name/
├── manifest.json      # Story metadata
├── prologue.json      # Chapter file
├── chapter1.json      # Chapter file
├── chapter2.json      # Chapter file
└── endings.json       # Endings
```

## Scene Format

### Basic Scene

```json
{
  "scene_id": {
    "text": "Scene narrative text here.",
    "choices": [
      { "text": "First choice", "next": "next_scene" },
      { "text": "Second choice", "next": "another_scene" }
    ]
  }
}
```

### Full Scene

```json
{
  "prologue.awakening": {
    "bg": "bedroom_dark",
    "music": "ambient_drone",
    "text": "You wake up in darkness.\n\nThe ceiling is unfamiliar.",
    "actions": [
      { "animate": { "type": "static", "duration": 1.5 } }
    ],
    "choices": [
      {
        "text": "Look around",
        "next": "look_around",
        "conditions": []
      },
      {
        "text": "Go back to sleep",
        "next": "sleep_again",
        "actions": [{ "add_coin": -10 }]
      }
    ],
    "final": false
  }
}
```

### Scene Properties

| Property | Type | Description |
|----------|------|-------------|
| `text` | string | Scene narrative (supports markdown) |
| `choices` | array | Available player choices |
| `actions` | array | Actions executed on scene enter |
| `bg` | string | Background image identifier |
| `music` | string | Background music identifier |
| `sound` | string | Sound effect to play |
| `final` | boolean | If true, this is an ending scene |

## Scene ID Format

Scene IDs use a hierarchical format:

```
chapter.scene_name
```

Examples:
- `prologue.start`
- `chapter1.bathroom_entry`
- `end.accept_truth`

When referencing scenes within the same chapter, you can omit the chapter prefix:

```json
{
  "prologue.start": {
    "choices": [
      { "text": "Continue", "next": "awakening" }  // Resolves to prologue.awakening
    ]
  }
}
```

## Choices

### Basic Choice

```json
{ "text": "Open the door", "next": "door_opened" }
```

### Choice with Jump

Jump to a different chapter:

```json
{ "text": "Leave", "jump": "chapter2.start" }
```

### Choice with Actions

```json
{
  "text": "Take the money",
  "next": "money_taken",
  "actions": [
    { "add_coin": 50 },
    { "add_item": "wallet" },
    { "set_flag": "stole_money" }
  ]
}
```

### Choice with Conditions

```json
{
  "text": "Use the key",
  "next": "door_unlocked",
  "conditions": [
    { "has_item": "rusty_key" }
  ]
}
```

## Actions

Actions modify the game state.

### Coin Actions

```json
{ "add_coin": 25 }      // Add 25 coins
{ "add_coin": -10 }     // Remove 10 coins
{ "set_coin": 100 }     // Set coins to exactly 100
```

### Flag Actions

```json
{ "set_flag": "door_opened" }      // Set flag to true
{ "unset_flag": "door_opened" }    // Set flag to false
```

### Variable Actions

```json
{ "set_var": { "name": "trust", "value": 5 } }
{ "increment": "pills_taken" }     // Add 1 to variable
{ "decrement": "health" }          // Subtract 1 from variable
```

### Item Actions

```json
{ "add_item": "flashlight" }       // Add item to inventory
{ "remove_item": "flashlight" }    // Remove item from inventory
{ "add_item": { "name": "coin", "count": 5 } }  // Add multiple
```

### Animation Actions

```json
{
  "animate": {
    "type": "glitch",
    "duration": 2.0
  }
}
```

Animation types:
- `glitch` — RGB split and offset
- `static` — TV noise
- `shake` — Screen shake
- `flicker` — Light flicker
- `fade_out` — Fade to black
- `fade_in` — Fade from black

### Sound Actions

```json
{ "sound": "door_creak" }
{ "music": "tension_loop" }
{ "music": null }  // Stop music
```

## Conditions

Conditions determine if choices are available.

### Flag Conditions

```json
{ "has_flag": "key_found" }
{ "not_flag": "door_opened" }
```

### Variable Conditions

```json
{ "var_eq": { "name": "trust", "value": 5 } }
{ "var_gt": { "name": "coins", "value": 50 } }
{ "var_lt": { "name": "sanity", "value": 20 } }
{ "var_gte": { "name": "strength", "value": 3 } }
{ "var_lte": { "name": "health", "value": 10 } }
```

### Item Conditions

```json
{ "has_item": "flashlight" }
{ "not_item": "broken_key" }
{ "item_count": { "name": "coin", "min": 5 } }
```

### Coin Conditions

```json
{ "coins_gt": 50 }
{ "coins_lt": 20 }
{ "coins_gte": 100 }
```

### Combining Conditions

All conditions in an array must be true (AND logic):

```json
{
  "text": "Bribe the guard",
  "conditions": [
    { "has_item": "gold_coin" },
    { "coins_gte": 50 },
    { "not_flag": "guard_hostile" }
  ]
}
```

## Manifest File

The manifest defines story metadata:

```json
{
  "name": "Blue Frequency",
  "name_ru": "Синяя частота",
  "version": "1.0.0",
  "author": "Novel Engine Team",
  "description": "A psychological horror story",
  "start_scene": "prologue.start",
  "chapters": [
    {
      "id": "prologue",
      "name": "Prologue",
      "file": "prologue.json"
    },
    {
      "id": "chapter1",
      "name": "Chapter 1",
      "file": "chapter1.json"
    }
  ],
  "settings": {
    "default_coins": 100,
    "min_coins": 0,
    "max_coins": 200
  },
  "themes": {
    "primary": "#1a0a2e",
    "accent": "#ff6b6b"
  },
  "warnings": [
    "psychological_horror",
    "death"
  ]
}
```

## Text Formatting

Scene text supports basic markdown:

```
**bold text**
*italic text*
> quoted text
# Heading

- List item
- Another item

Paragraph break with empty line.
```

## Best Practices

### 1. Meaningful Scene IDs

```json
// Good
"chapter1.bathroom_discovery"
"end.redemption"

// Bad
"scene1"
"s42"
```

### 2. Consistent Chapter Prefixes

All scenes in a chapter should share a prefix:

```json
{
  "chapter2.entrance": { ... },
  "chapter2.hallway": { ... },
  "chapter2.bedroom": { ... }
}
```

### 3. Actions Before Choices

Execute state changes when entering scenes, not in choices:

```json
// Good - action on scene enter
{
  "found_key": {
    "text": "You find a rusty key.",
    "actions": [{ "add_item": "rusty_key" }],
    "choices": [{ "text": "Continue", "next": "hallway" }]
  }
}

// Also valid - action in choice
{
  "chest": {
    "choices": [
      {
        "text": "Take the key",
        "next": "key_taken",
        "actions": [{ "add_item": "rusty_key" }]
      }
    ]
  }
}
```

### 4. Balance Sanity Changes

```json
// Good - meaningful impact
{ "add_coin": -25 }  // Significant event
{ "add_coin": 10 }   // Small reward

// Bad - too extreme
{ "add_coin": -90 }  // Nearly instant death
{ "add_coin": 200 }  // Trivializes the system
```

### 5. Clear Ending Scenes

```json
{
  "end.redemption": {
    "text": "════════════════\n\n** THE END **\n\n════════════════",
    "final": true
  }
}
```
