package com.example.dominionrising.common.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Packet data for grouping selected units
 */
public class GroupUnitsPacket {
    private final Map<UUID, Integer> unitCounts; // unit ID -> count to group
    
    public GroupUnitsPacket(Map<UUID, Integer> unitCounts) {
        this.unitCounts = new HashMap<>(unitCounts);
    }
    
    public Map<UUID, Integer> getUnitCounts() {
        return unitCounts;
    }
    
    /**
     * Check if packet is valid (has at least one unit with count > 0)
     */
    public boolean isValid() {
        return unitCounts.values().stream().anyMatch(count -> count > 0);
    }
    
    /**
     * Get total number of units to be grouped
     */
    public int getTotalUnits() {
        return unitCounts.values().stream().mapToInt(Integer::intValue).sum();
    }
}