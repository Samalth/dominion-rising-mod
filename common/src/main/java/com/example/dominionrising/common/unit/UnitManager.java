package com.example.dominionrising.common.unit;

import com.example.dominionrising.common.nation.Nation;
import com.example.dominionrising.common.nation.NationManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages all nation units in memory
 * Thread-safe singleton for unit operations
 */
public class UnitManager {
    private static UnitManager instance;
    
    // Map of unit ID to NationUnit
    private final Map<UUID, NationUnit> units = new ConcurrentHashMap<>();
    
    // Map of nation name to set of unit IDs for quick lookups
    private final Map<String, Set<UUID>> nationUnits = new ConcurrentHashMap<>();
    
    private UnitManager() {}
    
    public static UnitManager getInstance() {
        if (instance == null) {
            synchronized (UnitManager.class) {
                if (instance == null) {
                    instance = new UnitManager();
                }
            }
        }
        return instance;
    }

    /**
     * Spawn a new unit for a nation
     * @param type Unit type (soldier, archer, knight, etc.)
     * @param owner The nation that owns this unit
     * @param level Initial level of the unit
     * @return The created NationUnit, or null if failed
     */
    public NationUnit spawnUnit(String type, Nation owner, int level) {
        if (owner == null || type == null || type.trim().isEmpty()) {
            return null;
        }
        
        level = Math.max(1, level); // Minimum level 1
        NationUnit unit = new NationUnit(type.trim(), owner.getName(), level);
        
        // Register unit
        units.put(unit.getId(), unit);
        
        // Add to nation's unit set
        String nationName = owner.getName().toLowerCase();
        nationUnits.computeIfAbsent(nationName, k -> ConcurrentHashMap.newKeySet()).add(unit.getId());
        
        return unit;
    }

    /**
     * Spawn a unit with default level 1
     * @param type Unit type
     * @param owner The nation that owns this unit
     * @return The created NationUnit, or null if failed
     */
    public NationUnit spawnUnit(String type, Nation owner) {
        return spawnUnit(type, owner, 1);
    }

    /**
     * Get all units belonging to a nation
     * @param owner The nation
     * @return List of units belonging to this nation
     */
    public List<NationUnit> listUnits(Nation owner) {
        if (owner == null) {
            return new ArrayList<>();
        }
        
        String nationName = owner.getName().toLowerCase();
        Set<UUID> unitIds = nationUnits.get(nationName);
        
        if (unitIds == null || unitIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return unitIds.stream()
                .map(units::get)
                .filter(Objects::nonNull)
                .filter(NationUnit::isAlive)
                .collect(Collectors.toList());
    }

    /**
     * Get all units belonging to a nation by nation name
     * @param nationName The nation name
     * @return List of units belonging to this nation
     */
    public List<NationUnit> listUnits(String nationName) {
        Nation nation = NationManager.getInstance().getNationByName(nationName);
        return listUnits(nation);
    }

    /**
     * Get a unit by its ID
     * @param unitId The unit ID
     * @return The unit, or null if not found
     */
    public NationUnit getUnit(UUID unitId) {
        return units.get(unitId);
    }

    /**
     * Remove a unit (when it dies or is dismissed)
     * @param unitId The unit ID to remove
     * @return true if unit was removed, false if not found
     */
    public boolean removeUnit(UUID unitId) {
        NationUnit unit = units.remove(unitId);
        if (unit == null) {
            return false;
        }
        
        // Remove from nation's unit set
        String nationName = unit.getOwnerNation().toLowerCase();
        Set<UUID> unitIds = nationUnits.get(nationName);
        if (unitIds != null) {
            unitIds.remove(unitId);
            if (unitIds.isEmpty()) {
                nationUnits.remove(nationName);
            }
        }
        
        return true;
    }

    /**
     * Get all units for a specific player's nation
     * @param playerNation The nation name
     * @return List of alive units
     */
    public List<NationUnit> getPlayerNationUnits(String playerNation) {
        if (playerNation == null) {
            return new ArrayList<>();
        }
        
        return listUnits(playerNation);
    }

    /**
     * Count total units for a nation
     * @param nation The nation
     * @return Number of alive units
     */
    public int getUnitCount(Nation nation) {
        return listUnits(nation).size();
    }

    /**
     * Count units by type for a nation
     * @param nation The nation
     * @param type Unit type to count
     * @return Number of units of this type
     */
    public long countUnitsByType(Nation nation, String type) {
        return listUnits(nation).stream()
                .filter(unit -> unit.getType().equalsIgnoreCase(type))
                .count();
    }

    /**
     * Get all available unit types
     * @return Set of supported unit types
     */
    public Set<String> getAvailableUnitTypes() {
        return Set.of("soldier", "archer", "knight", "mage");
    }

    /**
     * Check if a unit type is valid
     * @param type Unit type to check
     * @return true if type is supported
     */
    public boolean isValidUnitType(String type) {
        return getAvailableUnitTypes().contains(type.toLowerCase());
    }

    /**
     * Clean up dead units (called periodically)
     * @return Number of units cleaned up
     */
    public int cleanupDeadUnits() {
        List<UUID> deadUnits = units.values().stream()
                .filter(unit -> !unit.isAlive())
                .map(NationUnit::getId)
                .collect(Collectors.toList());
        
        int cleaned = 0;
        for (UUID deadUnitId : deadUnits) {
            if (removeUnit(deadUnitId)) {
                cleaned++;
            }
        }
        
        return cleaned;
    }

    /**
     * Get total number of units across all nations
     * @return Total unit count
     */
    public int getTotalUnitCount() {
        return (int) units.values().stream().filter(NationUnit::isAlive).count();
    }

    /**
     * Clear all units (for testing or reset)
     */
    public void clearAllUnits() {
        units.clear();
        nationUnits.clear();
    }

    /**
     * Get summary statistics
     * @return Map with various statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUnits", getTotalUnitCount());
        stats.put("totalNationsWithUnits", nationUnits.size());
        stats.put("averageUnitsPerNation", nationUnits.isEmpty() ? 0 : getTotalUnitCount() / (double) nationUnits.size());
        
        // Count by type
        Map<String, Long> typeCount = units.values().stream()
                .filter(NationUnit::isAlive)
                .collect(Collectors.groupingBy(NationUnit::getType, Collectors.counting()));
        stats.put("unitsByType", typeCount);
        
        return stats;
    }
}