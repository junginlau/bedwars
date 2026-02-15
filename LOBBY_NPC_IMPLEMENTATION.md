# Lobby NPC System Implementation

## Summary
Successfully implemented a lobby NPC system with game mode selection for the Bedwars plugin. Players can now right-click NPCs to select maps and modes (Solo/Doubles/Squad) with dynamic team assignment.

## New Files Created

### 1. GameMode.java
- Enum defining three game modes:
  - **SOLO**: 1 player per team, up to 8 teams
  - **DOUBLES**: 2 players per team, up to 8 teams
  - **SQUAD**: 4 players per team, up to 4 teams
- Each mode has configurable team size and max teams

### 2. LobbyNPC.java
- Represents a single lobby NPC using ArmorStand
- Handles spawning, removal, and entity tracking
- Customizable display name visible above NPC

### 3. LobbyNPCManager.java
- Manages multiple lobby NPCs across the server
- Saves/loads NPC locations to `lobby-npcs.yml`
- Auto-spawns NPCs on plugin enable
- Provides NPC lookup by clicked entity

### 4. LobbyGUI.java
- Two-stage GUI system:
  - **Map Selection**: Shows available maps
  - **Mode Selection**: Shows enabled modes for chosen map (with back button)
- Material icons for modes (Iron/Diamond/Netherite swords)

### 5. LobbyListener.java
- Handles NPC right-click to open map selector
- Processes GUI inventory clicks
- Routes player to game join with selected map and mode

## Modified Files

### MapConfig.java
- Added `List<GameMode> enabledModes` field
- Defaults to all three modes enabled
- Saves/loads enabled modes from YAML
- Added `isModeEnabled()` and `getEnabledModes()` methods

### GameManager.java
- Removed `org.bukkit.GameMode` import to avoid name collision
- Added `currentMode` and `currentMapName` fields to track active game session
- New `joinGame(Player, String mapName, GameMode mode)` method
- Existing `join()` method now delegates to `joinGame()` with default mode
- `rebuildTeamsForMode()` dynamically limits teams based on mode
- `pickTeam()` now respects team size limits from current mode
- Players joining must match current map/mode (creates session-based lobbies)
- Game reset on end clears `currentMapName` and resets `currentMode`

### BedwarsCommand.java
- Added `LobbyNPCManager` dependency
- New `/bw setnpc [displayName]` command
  - Creates NPC at admin's location
  - Optional custom display name (defaults to "Bedwars")
- Updated tab completions

### BedwarsPlugin.java
- Initialized `LobbyNPCManager` and loaded NPCs on enable
- Registered `LobbyListener` for NPC interactions
- Added `onDisable()` to clean up NPCs

## How to Use

### Admin Setup
1. Use `/bw setup` to configure maps with spawn points, beds, and generators
2. Use `/bw setnpc` to create a lobby NPC at your location
   - Example: `/bw setnpc §6§lClick to Play!`
3. NPCs persist across server restarts

### Player Experience
1. Right-click the lobby NPC
2. Select a map from the GUI
3. Select a game mode (Solo/Doubles/Squad)
4. Auto-join the game with appropriate team assignment

### Game Modes Explained
- **Solo**: Many small teams with 1 player each (like EggWars solo)
- **Doubles**: Teams of 2 players
- **Squad**: Standard 4-player teams

## Technical Details

### Dynamic Team Assignment
- GameManager creates teams based on mode when first player joins
- Subsequent join attempts must match the same map + mode
- Team picker skips full teams (respects `mode.getTeamSize()`)
- All teams cleared on game end for fresh lobby

### NPC Persistence
- ArmorStands store location + display name in `lobby-npcs.yml`
- UUID tracking prevents duplicate spawns
- Respawned on plugin reload/server restart

### Map Mode Configuration
- Each map stores enabled modes in YAML
- Admins can disable certain modes per map via config editing
- GUI only shows modes that are enabled for selected map

## Build Status
✅ Compiled successfully with Gradle
✅ No compilation errors
⚠️ Some deprecation warnings from Paper API (expected)

## Next Steps (Future Features)
1. **Leaderboard System** (step 2):
   - Track player stats (kills, beds destroyed, wins)
   - `/bw stats` command
   - Holographic leaderboard displays
   
2. **Map Mode Editor**:
   - GUI in `/bw setup` to toggle enabled modes per map
   
3. **NPC Management**:
   - `/bw removenpc` command
   - `/bw listnpcs` command
