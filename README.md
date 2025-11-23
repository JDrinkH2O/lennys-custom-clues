# Lenny's Custom Clues

A RuneLite plugin for capturing game state data during guessing games and puzzles in Old School RuneScape.

## Plugin Functionality

This plugin automatically captures the player's complete game state when specific trigger events occur **and the Event Key field contains non-whitespace text**. The captured data includes the player's location coordinates, inventory contents, and worn equipment. This information is useful for puzzle-solving games where the player's current state needs to be verified or recorded.

**Key Features:**
- Automatic game state capture on various trigger events
- JSON-formatted output for easy processing
- Console logging and side panel display
- Manual submission via button interface
- Real-time trigger detection and logging
- Event Key field for conditional capture control

## Event Key Field

The plugin includes an "Event Key" text field in its side panel interface. This field serves as a conditional gate for all game state capture operations:

- **Purpose**: Allows users to control when game state should be captured
- **Requirement**: Must contain non-whitespace text for any capture to occur
- **Behavior**: When empty or containing only whitespace, all trigger events are silently ignored
- **Debug Mode**: When debug mode is enabled in plugin settings, skipped captures will show debug messages in the game chat
- **JSON Integration**: The event key value is included in all captured game state JSON under the `event_key` field

## Current Trigger Events

The plugin captures game state data when any of the following events occur:

**Important**: All trigger events require the Event Key field in the plugin's UI to contain non-whitespace text. If the Event Key is empty, capture will be silently skipped (unless debug mode is enabled, which will show debug messages in chat).

### 1. Manual Button Submission
- **Trigger**: Clicking the "Submit Answer" button in the plugin's side panel
- **Requirement**: Event Key field must contain non-whitespace text
- **Use case**: Manual game state submission when needed

### 2. Player Emotes
- **Trigger**: Performing any of 50+ supported emotes (wave, dance, bow, etc.)
- **Supported emotes**: 
  - **Basic emotes**: Yes, No, Thinking, Bow, Angry, Cry, Laugh, Cheer, Wave, Beckon, Clap, Dance, Jump for Joy, Yawn, Spin, Shrug
  - **Special emotes**: Salute, Goblin bow, Goblin salute, Glass box, Climb rope, Lean, Glass wall
  - **Confirmed additional emotes**: Blow Kiss, Zombie Walk, Rabbit Hop
  - **Extended emotes (testing required)**: Jig, Headbang, Panic, Raspberry, Premier Shield, Sit down, Flex, Zombie Dance, Sit up, Push up, Star jump, Jog, Air Guitar, Uri transform, Explore, Fortis Salute, Idea, Stamp, Flap, Slap Head, Scared, Zombie Hand, Hypermobile Drinker, Smooth dance, Crazy dance, Party, Trick
- **Use case**: Discrete signaling during gameplay
- **Note**: Some extended emotes use estimated animation IDs and may require in-game testing to verify functionality

### 3. Digging with Spade
- **Trigger**: Using a spade to dig (animation ID 830)
- **Use case**: Treasure hunt and clue scroll activities

### 4. NPC Interactions
- **Trigger**: Any interaction with NPCs (Attack, Talk-to, Trade, Pickpocket, etc.)
- **Scope**: All NPC menu options (first through fifth options)
- **Use case**: Quest and dialogue-based puzzles

## File Architecture

The plugin follows a clean separation of concerns across multiple files:

### Core Files

| File | Responsibility |
|------|---------------|
| **LennysCustomCluesPlugin.java** | Event detection and plugin lifecycle management. Handles RuneLite event subscriptions and delegates processing to services. |
| **LennysCustomCluesPanel.java** | UI components and user interface interactions. Contains only Swing UI code and delegates business logic to services. |
| **GameStateService.java** | Business logic coordination, API integration, and workflow management. Orchestrates the entire capture and submission process. |
| **GameStateCapture.java** | Raw data extraction and formatting from the game client. Pure data collection without side effects. |
| **AnimationTriggers.java** | Animation ID constants and trigger detection logic. Determines which animations should trigger game state capture. |
| **LennysCustomCluesConfig.java** | Configuration interface defining plugin settings (debug mode, event key). |
| **ApiClient.java** | HTTP communication with external API. Handles JSON serialization and network requests. |

### Architecture Benefits

- **Single Responsibility**: Each file has one clear, focused purpose
- **Improved Testability**: Business logic can be tested independently of UI components
- **Better Maintainability**: Changes to game state logic don't require modifying UI code
- **Enhanced Readability**: Smaller, focused files are easier to understand and navigate
- **Reduced Coupling**: Components depend on clear interfaces rather than mixed concerns

### Data Flow

1. **Event Detection**: `LennysCustomCluesPlugin` receives RuneLite events
2. **Trigger Validation**: `AnimationTriggers` determines if the event should trigger capture
3. **Service Coordination**: `GameStateService` orchestrates the capture workflow
4. **Data Extraction**: `GameStateCapture` extracts raw data from the game client
5. **API Communication**: `ApiClient` submits the formatted data to the external service
6. **UI Updates**: `LennysCustomCluesPanel` displays the results to the user

## JSON Schema

The plugin generates JSON objects with the following structure:

```json
{
  "location": {
    "world": {
      "x": <integer>,
      "y": <integer>, 
      "plane": <integer>
    },
    "local": {
      "sceneX": <integer>,
      "sceneY": <integer>
    }
  },
  "inventory": [
    {
      "slot": <integer>,
      "id": <integer>,
      "quantity": <integer>
    }
  ],
  "worn_items": [
    {
      "slot": <integer>,
      "id": <integer>,
      "quantity": <integer>
    }
  ],
  "emote_id": <integer|null>,
  "npc_id": <integer|null>,
  "interaction_type": <string|null>,
  "event_key": <string>,
  "rsn": <string|null>
}
```

### Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| `location.world.x` | integer | World X coordinate |
| `location.world.y` | integer | World Y coordinate |
| `location.world.plane` | integer | World plane/floor level |
| `location.local.sceneX` | integer | Local scene X coordinate |
| `location.local.sceneY` | integer | Local scene Y coordinate |
| `inventory` | array | List of items in player's inventory |
| `inventory[].slot` | integer | Inventory slot number (0-27) |
| `inventory[].id` | integer | Item ID |
| `inventory[].quantity` | integer | Stack size |
| `worn_items` | array | List of equipped items |
| `worn_items[].slot` | integer | Equipment slot number |
| `worn_items[].id` | integer | Item ID |
| `worn_items[].quantity` | integer | Stack size |
| `emote_id` | integer/null | Animation ID if triggered by emote/dig, null otherwise |
| `npc_id` | integer/null | NPC ID if triggered by NPC interaction, null otherwise |
| `interaction_type` | string/null | Menu option text (e.g., "Attack", "Talk-to") if NPC interaction, null otherwise |
| `event_key` | string | User-provided event key from the UI text field |
| `rsn` | string/null | Player's RuneScape Name (display name), null if player not found |

### Example JSON Output

**Emote Trigger:**
```json
{
  "location": {"world": {"x": 3200, "y": 3200, "plane": 0}, "local": {"sceneX": 32, "sceneY": 32}},
  "inventory": [{"slot": 0, "id": 995, "quantity": 1000}],
  "worn_items": [{"slot": 3, "id": 1277, "quantity": 1}],
  "emote_id": 863,
  "npc_id": null,
  "interaction_type": null,
  "event_key": "puzzle-1-wave",
  "rsn": "PlayerName123"
}
```

**NPC Interaction:**
```json
{
  "location": {"world": {"x": 3200, "y": 3200, "plane": 0}, "local": {"sceneX": 32, "sceneY": 32}},
  "inventory": [{"slot": 0, "id": 995, "quantity": 1000}],
  "worn_items": [{"slot": 3, "id": 1277, "quantity": 1}],
  "emote_id": null,
  "npc_id": 1234,
  "interaction_type": "Talk-to",
  "event_key": "quest-step-5",
  "rsn": "PlayerName123"
}
```