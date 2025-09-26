package com.example.dominionrising.common.nation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages nations and their members
 * Provides thread-safe operations for nation creation and management
 */
public class NationManager {
    private static NationManager instance;
    
    private final Map<String, Nation> nations;
    private final Map<UUID, String> playerToNation;
    
    private NationManager() {
        this.nations = new ConcurrentHashMap<>();
        this.playerToNation = new ConcurrentHashMap<>();
    }
    
    public static NationManager getInstance() {
        if (instance == null) {
            synchronized (NationManager.class) {
                if (instance == null) {
                    instance = new NationManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Create a new nation
     * @param name The name of the nation
     * @param player The player who will be the leader
     * @return Result of the operation
     */
    public NationResult createNation(String name, UUID player) {
        if (name == null || name.trim().isEmpty()) {
            return NationResult.error("Nation name cannot be empty");
        }
        
        if (name.length() > 32) {
            return NationResult.error("Nation name cannot be longer than 32 characters");
        }
        
        // Check if nation already exists (case insensitive)
        if (nations.containsKey(name.toLowerCase())) {
            return NationResult.error("Nation with name '" + name + "' already exists");
        }
        
        // Check if player is already in a nation
        if (playerToNation.containsKey(player)) {
            return NationResult.error("You are already a member of nation '" + playerToNation.get(player) + "'");
        }
        
        // Create the nation
        Nation nation = new Nation(name, player);
        nations.put(name.toLowerCase(), nation);
        playerToNation.put(player, name);
        
        return NationResult.success("Nation '" + name + "' created successfully! You are now the leader.");
    }
    
    /**
     * Join an existing nation
     * @param nationName The name of the nation to join
     * @param player The player who wants to join
     * @return Result of the operation
     */
    public NationResult joinNation(String nationName, UUID player) {
        if (nationName == null || nationName.trim().isEmpty()) {
            return NationResult.error("Nation name cannot be empty");
        }
        
        // Check if player is already in a nation
        if (playerToNation.containsKey(player)) {
            return NationResult.error("You are already a member of nation '" + playerToNation.get(player) + "'");
        }
        
        // Find the nation (case insensitive)
        Nation nation = nations.get(nationName.toLowerCase());
        if (nation == null) {
            return NationResult.error("Nation '" + nationName + "' does not exist");
        }
        
        // Add player to nation
        if (nation.addMember(player)) {
            playerToNation.put(player, nation.getName());
            return NationResult.success("Successfully joined nation '" + nation.getName() + "'!");
        } else {
            return NationResult.error("Failed to join nation '" + nationName + "' (already a member?)");
        }
    }
    
    /**
     * Leave current nation
     * @param player The player who wants to leave
     * @return Result of the operation
     */
    public NationResult leaveNation(UUID player) {
        String currentNationName = playerToNation.get(player);
        if (currentNationName == null) {
            return NationResult.error("You are not a member of any nation");
        }
        
        Nation nation = nations.get(currentNationName.toLowerCase());
        if (nation == null) {
            // Clean up inconsistent state
            playerToNation.remove(player);
            return NationResult.error("Your nation no longer exists");
        }
        
        if (nation.isLeader(player)) {
            return NationResult.error("Leaders cannot leave their nation. Transfer leadership first or disband the nation");
        }
        
        if (nation.removeMember(player)) {
            playerToNation.remove(player);
            return NationResult.success("Successfully left nation '" + nation.getName() + "'");
        } else {
            return NationResult.error("Failed to leave nation");
        }
    }
    
    /**
     * Disband current nation (only leaders can do this)
     * @param player The player who wants to disband their nation
     * @return Result of the operation
     */
    public NationResult disbandNation(UUID player) {
        String currentNationName = playerToNation.get(player);
        if (currentNationName == null) {
            return NationResult.error("You are not a member of any nation");
        }
        
        Nation nation = nations.get(currentNationName.toLowerCase());
        if (nation == null) {
            // Clean up inconsistent state
            playerToNation.remove(player);
            return NationResult.error("Your nation no longer exists");
        }
        
        if (!nation.isLeader(player)) {
            return NationResult.error("Only the nation leader can disband the nation");
        }
        
        // Remove all members from the nation mapping
        for (UUID member : nation.getMembers()) {
            playerToNation.remove(member);
        }
        
        // Remove the nation itself
        String nationName = nation.getName();
        nations.remove(currentNationName.toLowerCase());
        
        return NationResult.success("Nation '" + nationName + "' has been disbanded successfully");
    }
    
    /**
     * Get the nation a player belongs to
     * @param player The player
     * @return The nation, or null if not a member of any nation
     */
    public Nation getPlayerNation(UUID player) {
        String nationName = playerToNation.get(player);
        if (nationName != null) {
            return nations.get(nationName.toLowerCase());
        }
        return null;
    }
    
    /**
     * Get a nation by name
     * @param name The nation name (case insensitive)
     * @return The nation, or null if not found
     */
    public Nation getNation(String name) {
        if (name == null) return null;
        return nations.get(name.toLowerCase());
    }
    
    /**
     * Get all nations
     * @return A copy of all nations map
     */
    public Map<String, Nation> getAllNations() {
        return new HashMap<>(nations);
    }
    
    /**
     * Check if a player is in any nation
     * @param player The player
     * @return true if the player is in a nation
     */
    public boolean isPlayerInNation(UUID player) {
        return playerToNation.containsKey(player);
    }
    
    /**
     * Get the number of nations
     * @return Total number of nations
     */
    public int getNationCount() {
        return nations.size();
    }
    
    /**
     * Result class for nation operations
     */
    public static class NationResult {
        private final boolean success;
        private final String message;
        
        private NationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public static NationResult success(String message) {
            return new NationResult(true, message);
        }
        
        public static NationResult error(String message) {
            return new NationResult(false, message);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
}