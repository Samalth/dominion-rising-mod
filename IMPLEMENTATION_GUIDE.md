# Dominion Rising - Nation Management Implementation

## Overview

This is a working vertical slice implementation of the core nation and army management mechanics for the Dominion Rising Minecraft 1.21.1 mod. The implementation includes:

- **Common module**: Platform-agnostic core logic
- **Forge module**: Minecraft Forge specific implementation
- **NeoForge module**: NeoForge specific implementation

## Features Implemented

### Core Classes (Common Module)

1. **Nation** (`com.example.dominionrising.common.nation.Nation`)
   - Name, leader, members list, balance
   - Member management (add/remove)
   - Leader operations
   - Balance tracking

2. **Unit** (`com.example.dominionrising.common.army.Unit`)
   - Basic unit stub with type, level, health
   - Health management and status checking
   - Ready for future AI implementation

3. **NationManager** (`com.example.dominionrising.common.nation.NationManager`)
   - Thread-safe singleton pattern
   - In-memory storage of nations and player mappings
   - Nation creation and joining operations
   - Comprehensive error handling and validation

### Commands Available

Both Forge and NeoForge modules support the following commands:

- `/nation create <name>` - Create a new nation
- `/nation join <name>` - Join an existing nation
- `/nation leave` - Leave your current nation (non-leaders only)
- `/nation disband` - Disband your nation (leaders only)
- `/nation info` - Display information about your nation

## Building the Project

### Build All Modules
```powershell
.\gradlew build
```

### Build Specific Module
```powershell
# Forge only
.\gradlew :forge:build

# NeoForge only
.\gradlew :neoforge:build

# Common only
.\gradlew :common:build
```

## Testing the Implementation

### Known Issue: Java 21 Module System Compatibility

There are known compatibility issues between Forge 1.21.1 and Java 21's module system. If you encounter module resolution errors when running the client, try these solutions:

**Option 1: Use Java 17 instead**
```powershell
# If you have Java 17 installed, use it for running the client
.\gradlew -Dorg.gradle.java.home="C:\Program Files\Java\jdk-17" :forge:runClient
```

**Option 2: Test with NeoForge (Recommended)**
```powershell
# NeoForge has better Java 21 compatibility
.\gradlew :neoforge:runClient
```

**Option 3: Build and test manually**
```powershell
# Build the mod jar
.\gradlew build

# The mod jar will be available at:
# forge/build/libs/forge.jar
# neoforge/build/libs/neoforge-1.21.1.jar

# Install this jar in a Forge/NeoForge server or client manually
```

### Troubleshooting Build Failures

**Both Forge and NeoForge have compatibility issues with Java 21:**

- **Forge**: `Module jopt.simple not found` - Module system resolution failure
- **NeoForge**: `Module org.objectweb.asm.tree.analysis already on module path` - ASM library conflict

**Solutions (in order of preference):**

1. **Use Java 17** (strongly recommended):
   ```powershell
   # If you have Java 17 installed
   .\gradlew -Dorg.gradle.java.home="C:\Program Files\Java\jdk-17" :neoforge:runClient
   ```
   
   Or download Java 17 from: https://adoptium.net/

2. **Use the test script** (automated solution):
   ```powershell
   .\test-client.bat
   ```
   This script will automatically try different Java versions and approaches.

3. **Build and install manually**:
   ```powershell
   .\gradlew build
   # Then manually install the jars from build/libs/ into a working MC installation
   ```

4. **Use a development server** instead of client:
   ```powershell
   .\gradlew :neoforge:runServer
   # Then connect with a regular Minecraft client
   ```

### In-Game Testing Steps

Once the client starts and you create/join a world:

1. **Create a nation:**
   ```
   /nation create TestNation
   ```
   Expected: Success message confirming creation

2. **Check nation info:**
   ```
   /nation info
   ```
   Expected: Display nation details (name, leader status, member count, balance)

3. **Test with another player (or in multiplayer):**
   ```
   /nation join TestNation
   ```
   Expected: Success message confirming joining

4. **Test disband as leader:**
   ```
   /nation disband
   ```
   Expected: Success message confirming disbandment

5. **Test error cases:**
   ```
   /nation create TestNation
   ```
   Expected: Error message (nation already exists)
   
   ```
   /nation join NonExistentNation
   ```
   Expected: Error message (nation doesn't exist)
   
   ```
   /nation disband
   ```
   Expected: Error message (only leaders can disband)

## Project Structure

```
dominion-rising-mod/
├── common/
│   └── src/main/java/com/example/dominionrising/
│       ├── DominionRising.java (Main class with initialization)
│       ├── common/
│       │   ├── nation/
│       │   │   ├── Nation.java (Nation data class)
│       │   │   └── NationManager.java (Core nation management logic)
│       │   └── army/
│       │       └── Unit.java (Basic unit stub)
├── forge/
│   └── src/main/java/com/example/dominionrising/forge/
│       ├── DominionRisingForge.java (Forge entry point)
│       └── commands/
│           └── NationCommands.java (Forge command handlers)
└── neoforge/
    └── src/main/java/com/example/dominionrising/neoforge/
        ├── DominionRisingNeoForge.java (NeoForge entry point)
        └── commands/
            └── NationCommands.java (NeoForge command handlers)
```

## Technical Details

### Design Principles
- **Loader-neutral core**: All business logic is in the common module
- **Thread-safe operations**: Using ConcurrentHashMap for data storage
- **Comprehensive validation**: Input validation and error handling
- **Memory-based storage**: Ready for future persistence implementation
- **Result pattern**: Clean error handling with NationResult class

### Current Limitations (By Design)
- No persistent storage (in-memory only)
- No GUI implementation
- No AI unit logic (stub implementation)
- No economic integration
- Basic command set (expandable)

### Future Extension Points
- Add persistent storage backend
- Implement unit AI behaviors
- Add economic systems integration
- Create GUI interfaces
- Add more complex nation mechanics (wars, alliances, etc.)

## Command Details

### `/nation create <name>`
- Creates a new nation with the specified name
- Caller becomes the leader
- Nation name must be unique (case-insensitive)
- Maximum 32 characters
- Player cannot already be in a nation

### `/nation join <name>`
- Joins an existing nation
- Nation name is case-insensitive
- Player cannot already be in a nation
- Nation must exist

### `/nation leave`
- Leaves the current nation
- Leaders cannot leave (must transfer leadership first or disband)
- Player must be in a nation

### `/nation disband`
- Disbands the entire nation (permanently)
- Only the nation leader can use this command
- Removes all members from the nation
- Cannot be undone

### `/nation info`
- Shows current nation information
- Displays: name, leader status, member count, balance
- Player must be in a nation

## Build Requirements

- Java 21
- Gradle (included via wrapper)
- Minecraft 1.21.1
- Forge 52.0.17 or NeoForge 21.1.57

All dependencies are automatically managed by Gradle.