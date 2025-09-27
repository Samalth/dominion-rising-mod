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

    // === TACTICAL COMMAND METHODS ===

    /**
     * Command a unit to attack a target
     * @param unitId Unit ID
     * @param targetId Target entity ID
     * @return true if command was successful
     */
    public boolean commandAttack(UUID unitId, UUID targetId) {
        NationUnit unit = units.get(unitId);
        if (unit != null && unit.isAlive() && targetId != null) {
            unit.setAttackTarget(targetId);
            return true;
        }
        return false;
    }

    /**
     * Command a unit to defend a position
     * @param unitId Unit ID
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return true if command was successful
     */
    public boolean commandDefend(UUID unitId, double x, double y, double z) {
        NationUnit unit = units.get(unitId);
        if (unit != null && unit.isAlive()) {
            unit.setDefendPosition(x, y, z);
            return true;
        }
        return false;
    }

    /**
     * Get units within command range of a player
     * @param playerNation Player's nation
     * @param playerX Player X coordinate
     * @param playerY Player Y coordinate  
     * @param playerZ Player Z coordinate
     * @param maxDistance Maximum command distance
     * @return List of units within range
     */
    public List<NationUnit> getUnitsInRange(String playerNation, double playerX, double playerY, double playerZ, double maxDistance) {
        return listUnits(playerNation).stream()
                .filter(unit -> {
                    if (unit.isDefending()) {
                        double dx = unit.getDefendX() - playerX;
                        double dy = unit.getDefendY() - playerY;
                        double dz = unit.getDefendZ() - playerZ;
                        return Math.sqrt(dx*dx + dy*dy + dz*dz) <= maxDistance;
                    }
                    // For non-defending units, assume they're near the player for now
                    return true;
                })
                .collect(Collectors.toList());
    }

    // === PERSISTENCE METHODS ===

    /**
     * Serialize all units to string for world persistence
     * @return Serialized unit data
     */
    public String serializeUnits() {
        StringBuilder sb = new StringBuilder();
        for (NationUnit unit : units.values()) {
            if (unit.isAlive()) {
                sb.append(unit.serialize()).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Load units from serialized data
     * @param data Serialized unit data
     */
    public void loadUnits(String data) {
        if (data == null || data.trim().isEmpty()) {
            return;
        }

        // Clear existing data
        units.clear();
        nationUnits.clear();

        String[] lines = data.split("\n");
        int loaded = 0;
        
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            
            NationUnit unit = NationUnit.deserialize(line.trim());
            if (unit != null) {
                // Register unit
                units.put(unit.getId(), unit);
                
                // Add to nation's unit set
                String nationName = unit.getOwnerNation().toLowerCase();
                nationUnits.computeIfAbsent(nationName, k -> ConcurrentHashMap.newKeySet()).add(unit.getId());
                
                loaded++;
            }
        }
        
        System.out.println("UnitManager: Loaded " + loaded + " units from persistence data");
    }

    /**
     * Add a pre-existing unit (for loading from persistence)
     * @param unit The unit to add
     * @return true if added successfully
     */
    public boolean addExistingUnit(NationUnit unit) {
        if (unit == null || units.containsKey(unit.getId())) {
            return false;
        }
        
        // Register unit
        units.put(unit.getId(), unit);
        
        // Add to nation's unit set
        String nationName = unit.getOwnerNation().toLowerCase();
        nationUnits.computeIfAbsent(nationName, k -> ConcurrentHashMap.newKeySet()).add(unit.getId());
        
        return true;
    }

    /**
     * Get unit data for world persistence integration
     * @return Map of unit persistence data
     */
    public Map<String, Object> getPersistenceData() {
        Map<String, Object> data = new HashMap<>();
        data.put("units", serializeUnits());
        data.put("totalUnits", getTotalUnitCount());
        data.put("lastSaveTime", System.currentTimeMillis());
        return data;
    }

    /**
     * Load unit data from world persistence
     * @param data Persistence data map
     */
    public void loadFromPersistenceData(Map<String, Object> data) {
        if (data.containsKey("units")) {
            String unitData = (String) data.get("units");
            loadUnits(unitData);
        }
        
        if (data.containsKey("lastSaveTime")) {
            System.out.println("UnitManager: Restored from save at " + data.get("lastSaveTime"));
        }
    }
}