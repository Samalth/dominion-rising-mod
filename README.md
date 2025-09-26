# Dominion Rising

A Minecraft 1.21.1 mod that adds nation and army management mechanics to your world. Build nations, recruit members, and manage your empire!

## 📖 Description

Dominion Rising introduces a comprehensive nation management system to Minecraft. Players can create their own nations, invite others to join, and build powerful alliances. This mod focuses on political gameplay, diplomacy, and strategic nation building.

### 🌟 Key Features

- **Nation Creation**: Establish your own nation with a unique name
- **Member Management**: Invite players to join your nation and build a community
- **Leadership System**: Leaders have special permissions and responsibilities
- **Multi-Loader Support**: Available for both Forge and NeoForge
- **Thread-Safe**: Robust implementation that works reliably on multiplayer servers
- **In-Memory Storage**: Fast, efficient data management (persistence coming in future updates)

## 📋 Requirements

- Minecraft 1.21.1
- Either Minecraft Forge 52.0.17+ OR NeoForge 21.1.57+
- Java 17 or 21

## 🚀 Installation

1. Download the appropriate version:
   - **Forge**: Use `forge.jar`
   - **NeoForge**: Use `neoforge-1.21.1.jar`

2. Place the jar file in your `.minecraft/mods/` folder

3. Launch Minecraft with your chosen mod loader

## 🎮 Commands & Usage

All commands start with `/nation` and are available to all players:

### Core Commands

#### `/nation create <name>`
Creates a new nation with the specified name.
- **Who can use**: Any player not already in a nation
- **Requirements**: Nation name must be unique and 1-32 characters
- **Result**: You become the leader of the new nation

**Example:**
```
/nation create "Roman Empire"
/nation create Sparta
```

#### `/nation join <name>`
Join an existing nation.
- **Who can use**: Any player not already in a nation
- **Requirements**: Nation must exist
- **Result**: You become a member of the specified nation

**Example:**
```
/nation join "Roman Empire"
/nation join Sparta
```

#### `/nation leave`
Leave your current nation.
- **Who can use**: Nation members (except leaders)
- **Requirements**: Must be in a nation, cannot be the leader
- **Result**: You are removed from the nation
- **Note**: Leaders must transfer leadership or disband the nation

**Example:**
```
/nation leave
```

#### `/nation disband`
Permanently disband your nation.
- **Who can use**: Nation leaders only
- **Requirements**: Must be the leader of a nation
- **Result**: Nation is completely removed, all members are freed
- **Warning**: This action cannot be undone!

**Example:**
```
/nation disband
```

#### `/nation info`
Display information about your current nation.
- **Who can use**: Nation members
- **Requirements**: Must be in a nation
- **Shows**: Nation name, your role, member count, nation balance

**Example:**
```
/nation info
```

## 📊 Examples & Workflows

### Starting Your Nation
```bash
# Create your nation
/nation create "Kingdom of Avalon"

# Check your nation status
/nation info
# Output: Nation: Kingdom of Avalon | Leader: You | Members: 1 | Balance: 0.00
```

### Growing Your Nation
```bash
# As a leader, other players can join:
# Player2 runs: /nation join "Kingdom of Avalon"
# Player3 runs: /nation join "Kingdom of Avalon"

# Check updated info
/nation info
# Output: Nation: Kingdom of Avalon | Leader: You | Members: 3 | Balance: 0.00
```

### Managing Your Nation
```bash
# Members can leave (except leader)
/nation leave

# Leader can disband the entire nation
/nation disband
```

## ⚠️ Important Notes

### Leadership Rules
- **Leaders cannot leave** their nation using `/nation leave`
- **Only leaders can disband** their nation using `/nation disband`
- Disbanding removes ALL members and permanently deletes the nation

### Nation Names
- Must be **1-32 characters long**
- Must be **unique** (case-insensitive)
- Spaces and special characters are allowed

### Current Limitations
- **No persistence**: Nations are stored in memory and reset on server restart
- **No economy integration**: Balance tracking is implemented but not connected to economy systems
- **No GUI**: All interactions are command-based
- **No warfare system**: Combat mechanics not yet implemented

## 🔧 For Server Administrators

### Commands in Console
All player commands work from the server console, but require a player context. Use command blocks or other plugins to manage nations programmatically.

### Performance
- Thread-safe implementation using ConcurrentHashMap
- Minimal memory footprint
- No database dependencies
- Suitable for large multiplayer servers

## 🐛 Known Issues & Compatibility

### Java 21 Development Environment
If you're a mod developer experiencing build issues with `gradlew runClient`, this is due to Java 21 module system conflicts. The compiled mod works perfectly in production environments.

**Solutions:**
- Use Java 17 for development: `gradlew -Dorg.gradle.java.home="path/to/java17" runClient`
- Test with CurseForge/production instances instead of dev environment

### Mod Compatibility
- Compatible with most other mods
- No known conflicts with major modpacks
- Works with both single-player and multiplayer

## 🚧 Planned Features

- **Persistent Storage**: Database/file-based nation data storage
- **Economy Integration**: Connect with popular economy mods
- **Diplomacy System**: Nation alliances, wars, and treaties
- **Territory Claims**: Land protection and management
- **Army Management**: NPC units and military mechanics
- **GUI Interface**: User-friendly graphical interfaces
- **Nation Ranks**: Custom roles and permissions within nations

## 📝 Changelog

### Version 1.0.0 (Current)
- Initial release
- Basic nation creation and management
- Member join/leave system
- Leader-only disband functionality
- Multi-loader support (Forge + NeoForge)
- Thread-safe in-memory storage

## 🤝 Contributing

This mod is currently in active development. Bug reports and feature suggestions are welcome!

## 📄 License

This project is licensed under a Custom License - see the [LICENSE](LICENSE) file for details.

**Summary:**
- ✅ **Free for personal use** - Download and use for yourself
- ✅ **Personal modifications allowed** - Tweak the mod for your own use
- ❌ **No redistribution** - Only official developers may distribute this mod
- ❌ **No reuploading** - Do not upload to CurseForge, Modrinth, or other platforms
- ❌ **No commercial use** - Commercial use requires explicit permission

For permissions beyond personal use, please contact the project maintainers.

---

**Dominion Rising** - Build your empire, one nation at a time! 🏰