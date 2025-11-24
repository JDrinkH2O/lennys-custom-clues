# Lenny's Custom Clues

A RuneLite plugin for capturing game state data for community-run clue scroll puzzles in Old School RuneScape.

## Using the Plugin as a Player

As a player, you will have received some kind of riddle along with an event key from a fellow player, clean leader, or similar. Click the **"Set Event Key"** button and provide the key to begin playing. Once a valid event key is set, the client will capture information like worn equipment, inventory, and location when the player does things like dig, emote, and interact with Gielinor. This information is sent to a backend server where the answer for your event key is stored, and you will be alerted if you solve their riddle!

## Event Management

Event hosts can create and manage custom puzzle events through the plugin's event management interface. This system allows hosts to design puzzle solutions, track participant progress, and edit existing events.

### Creating Events

1. Click the **"Create a new event"** button in the plugin panel
2. The Answer Builder dialog opens, allowing you to:
   - Define reward text that players receive upon solving the puzzle
   - Add constraints (location, inventory, equipment, actions, etc)
   - Submit the event to the server with a unique event key **and a secret key** which is required to manage your event later
4. Share the event key with players so they can participate

See the [Answer Builder](#answer-builder) section for detailed information about creating puzzles.

### Managing Existing Events

Event hosts can view and edit their existing events using the **"Manage existing event"** button:

#### Loading an Event

1. Click **"Manage existing event"** in the plugin panel
2. Enter your **Event Key** and **Secret Key** (both are required)
3. Click **"Load Event"** to retrieve the event information

**Important**: You choose a secret key when you first create an event, and it is not changeable. Keep it secure as it's required for all management operations.

#### Event Information View

Once an event is loaded, the Event Information dialog displays:

**Answer Summary**
- Reward text players receive when they solve the puzzle
- Required trigger action (e.g., "Dig with a spade")
- List of all constraints defining the solution

**Leaderboard**
- Ranked table of all players who have solved the event
- Player names (RSN)
- Completion timestamps (displayed in your local time)
- Shows "No players have solved this event yet" if no completions

**Available Actions**
- **Edit Answer**: Modify the event's reward text or constraints
- **Close**: Exit the event information view

#### Editing Events

From the Event Information dialog, click **"Edit Answer"** to:
- Update the reward text
- Modify existing constraints
- Add or remove constraints
- Submit changes to the server using the same event key and secret key

## Current Trigger Events

The plugin captures game state data when any of the following events occur:

### 1. Digging with Spade
- **Trigger**: Using a spade to dig (animation ID 830)

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
- **Submit to Server**: Create the event with a unique event key and a secret key

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