package com.example.dominionrising.neoforge.gui;

import com.example.dominionrising.common.gui.ArmyUnitInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side registry for Army Station unit data
 * Simple solution for data transfer between server and client
 */
public class ArmyStationDataRegistry {
    private static final ConcurrentHashMap<UUID, List<ArmyUnitInfo>> playerUnitData = new ConcurrentHashMap<>();
    
    /**
     * Store unit data for a player (called from server-side)
     */
    public static void storePlayerUnits(UUID playerId, List<ArmyUnitInfo> units) {
        playerUnitData.put(playerId, new ArrayList<>(units));
    }
    
    /**
     * Get unit data for a player (called from client-side)
     */
    public static List<ArmyUnitInfo> getPlayerUnits(UUID playerId) {
        return playerUnitData.getOrDefault(playerId, new ArrayList<>());
    }
    
    /**
     * Clear all data (cleanup)
     */
    public static void clear() {
        playerUnitData.clear();
    }
    
    /**
     * Remove data for a specific player
     */
    public static void removePlayer(UUID playerId) {
        playerUnitData.remove(playerId);
    }
}