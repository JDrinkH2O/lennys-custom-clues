# Lenny's Custom Clues

A RuneLite plugin for capturing game state data for community-run clue scroll puzzles in Old School RuneScape.

## Plugin Functionality

As a player, set an event key given to you by your event host. Only when a valid event key is set, this plugin automatically captures the player's complete game state when specific trigger events occur. The captured data includes the player's location coordinates, inventory contents, and worn equipment. This information is sent to the Lenny's Labyrinth backend API, compared to the stored answer restrictions defined for the provided event key, and gives the player fireworks and a reward message if they have guessed correctly.

As an event host, you can click the "Create a new Answer" button in the plugin panel to open the Answer Builder window. This allows you to create a new event to be stored in the event database. You will choose an event key, answer requirements, and the reward message to be sent to players upon completion.

**Key Features:**
- Automatic game state capture on various trigger events
- JSON-formatted output for easy processing
- Console logging and side panel display
- Real-time trigger detection and logging
- Event Key management for conditional capture control
- Answer Builder window for creating custom puzzles

## Event Key Management

The plugin includes Event Key management buttons in its side panel interface. The event key serves as a conditional gate for all game state capture operations:

- **Purpose**: Allows users to control when game state should be captured
- **UI**: Three buttons manage the event key state:
  - "Set Event Key" - Opens a dialog to enter an event key
  - "Unset Event Key" - Clears the current event key
  - "Change Event Key" - Opens a dialog to change the existing event key
- **Requirement**: Must contain non-whitespace text for any capture to occur
- **Behavior**: When empty, all trigger events are silently ignored
- **Debug Mode**: When debug mode is enabled in plugin settings, skipped captures will show debug messages in the game chat
- **JSON Integration**: The event key value is included in all captured game state JSON under the `event_key` field

## Current Trigger Events

The plugin captures game state data when any of the following events occur:

### 1. Digging with Spade
- **Trigger**: Using a spade to dig (animation ID 830)
- **Use case**: Treasure hunt and clue scroll activities

### 2. Player Emotes - **NOT YET SUPPORTED**
- **Trigger**: Performing any of a list of supported emotes (wave, dance, bow, etc.)

### 3. NPC Interactions - **NOT YET SUPPORTED**
- **Trigger**: Any interaction with NPCs (Attack, Talk-to, Trade, Pickpocket, etc.)

## Configuration

The plugin provides several configuration options accessible through RuneLite's plugin settings:

### Available Settings

| Setting | Description | Default |
|---------|-------------|---------|
| **Debug Mode** | Shows all animation IDs and debug messages in the game chat when events occur | Disabled |
| **Victory sound effects** | Plays a celebratory sound when you solve a puzzle correctly | Enabled |
| **Victory fireworks** | Shows fireworks when you solve a puzzle correctly | Enabled |

## Answer Builder

The Answer Builder is a separate window for event hosts to create custom puzzles. Click the "Create a new Answer" button in the plugin panel to open the Answer Builder window.

### Opening the Answer Builder

1. Open the Lenny's Custom Clues plugin panel
2. Click the "Create a new Answer" button (available in all states)
3. The Answer Builder window will open

### Answer Builder Window

The Answer Builder window allows event hosts to:

- **Define Reward Text**: Enter the message that players will see when they solve the puzzle correctly (required)
- **Add Constraints**: Define the conditions that must be met for a correct answer
- **Submit to Server**: Create the event with a unique event key

### Constraint Types

Event hosts can add multiple constraints to define puzzle requirements:

1. **Location Constraints**
   - Define specific world coordinates where the player must be
   - Configure coordinate ranges and plane/floor requirements
   - Use the "Use Current Location" feature to capture your current position

2. **Inventory Constraints** *(In Development)*
   - Define required items in the player's inventory
   - Support for "contains", "exact match", and other matching modes

3. **Equipment Constraints** *(In Development)*
   - Define required equipped items
   - Support for specific gear requirements

4. **Action Constraints** *(In Development)*
   - Define required actions (emotes, NPC interactions, etc.)
   - Link to specific trigger events

### Creating a Puzzle

1. Click "Create a new Answer" in the plugin panel
2. Enter the reward text players will receive
3. Add one or more constraints defining the solution
4. Click "Submit Answer to Server"
5. Enter a unique event key for your puzzle
6. Share the event key with players

### Constraint Management

- **View Constraints**: All added constraints appear in the scrollable constraints panel
- **Remove Constraints**: Click the "X" button on any constraint to remove it
- **Clear All**: Use the "Clear All" button to reset and start over
- **Submit Button**: Only enabled when both reward text and at least one constraint are defined

## File Architecture

The plugin follows a clean separation of concerns across multiple files:

### Core Files

| File | Responsibility |
|------|---------------|
| **LennysCustomCluesPlugin.java** | Event detection and plugin lifecycle management. Handles RuneLite event subscriptions and delegates processing to services. |
| **MainPanel.java** | Primary panel container that displays the plugin UI. |
| **LennysCustomCluesPanel.java** | Player mode UI with event key management buttons and "Create a new Answer" button. Displays submission status and results. |
| **dialogs/AnswerBuilderDialog.java** | Answer builder dialog window for event hosts to create puzzles with constraints and reward text. |
| **dialogs/LocationConstraintDialog.java** | Dialog for configuring location constraints with live coordinate display and "Use Current Location" feature. |
| **dialogs/SubmitAnswerDialog.java** | Dialog for submitting completed answers to the server with event key validation. |
| **GameStateService.java** | Business logic coordination, API integration, and workflow management. Orchestrates the entire capture and submission process. Manages event key state. |
| **GameStateCapture.java** | Raw data extraction and formatting from the game client. Pure data collection without side effects. |
| **AnimationTriggers.java** | Animation ID constants and trigger detection logic. Determines which animations should trigger game state capture. |
| **CelebrationManager.java** | Manages victory celebrations including fireworks and sound effects when puzzles are solved correctly. |
| **LennysCustomCluesConfig.java** | Configuration interface defining plugin settings (debug mode, celebration options). |
| **ApiClient.java** | HTTP communication with external API. Handles JSON serialization and network requests. |

### Data Flow (Player Mode)

1. **Event Detection**: `LennysCustomCluesPlugin` receives RuneLite events
2. **Trigger Validation**: `AnimationTriggers` determines if the event should trigger capture
3. **Event Key Check**: `GameStateService` verifies that a valid event key is set
4. **Data Extraction**: `GameStateCapture` extracts raw data from the game client
5. **API Communication**: `ApiClient` submits the formatted data to the external service
6. **Response Handling**: `GameStateService` processes the API response
7. **Celebration**: If successful, `CelebrationManager` triggers fireworks and sound effects
8. **UI Updates**: `LennysCustomCluesPanel` displays the results to the user

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