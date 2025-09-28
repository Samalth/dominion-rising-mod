package com.example.dominionrising.common.armystation;

import com.example.dominionrising.common.nation.Nation;
import com.example.dominionrising.common.nation.NationManager;
import com.example.dominionrising.common.nation.NationRole;
import com.example.dominionrising.common.unit.NationUnit;
import com.example.dominionrising.common.unit.UnitManager;

import java.util.List;
import java.util.UUID;

/**
 * Common logic for Army Station functionality
 * Handles permission checks and unit retrieval
 */
public class ArmyStationManager {
    
    /**
     * Check if a player can access the Army Station
     * @param playerId The player's UUID
     * @return Result containing success/failure and message
     */
    public static ArmyStationResult canPlayerAccess(UUID playerId) {
        NationManager nationManager = NationManager.getInstance();
        Nation playerNation = nationManager.getPlayerNation(playerId);
        
        if (playerNation == null) {
            return ArmyStationResult.error("You must be in a nation to use this Army Station.");
        }
        
        NationRole playerRole = playerNation.getMemberRole(playerId);
        if (playerRole != NationRole.LEADER && playerRole != NationRole.COMMANDER) {
            return ArmyStationResult.error("You must be a Leader or Commander in your nation to use this Army Station.");
        }
        
        return ArmyStationResult.success("Access granted");
    }
    
    /**
     * Get all units for a player's nation
     * @param playerId The player's UUID
     * @return List of units, or empty list if no access/nation
     */
    public static List<NationUnit> getPlayerNationUnits(UUID playerId) {
        NationManager nationManager = NationManager.getInstance();
        Nation playerNation = nationManager.getPlayerNation(playerId);
        
        if (playerNation == null) {
            return List.of();
        }
        
        UnitManager unitManager = UnitManager.getInstance();
        return unitManager.listUnits(playerNation);
    }
    
    /**
     * Get formatted unit information for display
     * @param unit The unit to format
     * @return Formatted string with unit info
     */
    public static String formatUnitInfo(NationUnit unit) {
        return String.format("%s (Lv.%d) - %d/%d HP", 
            capitalize(unit.getType()), 
            unit.getLevel(), 
            unit.getHealth(), 
            unit.getMaxHealth());
    }
    
    /**
     * Capitalize first letter of a string
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    /**
     * Result class for Army Station operations
     */
    public static class ArmyStationResult {
        private final boolean success;
        private final String message;
        
        private ArmyStationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public static ArmyStationResult success(String message) {
            return new ArmyStationResult(true, message);
        }
        
        public static ArmyStationResult error(String message) {
            return new ArmyStationResult(false, message);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
}