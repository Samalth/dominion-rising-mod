package com.example.dominionrising.common.network;

import com.example.dominionrising.common.gui.ArmyUnitInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data container for Army Station sync packet
 * Contains serializable unit data for client-server communication
 */
public class ArmyStationSyncPacketData {
    private final List<ArmyUnitInfo> units;
    
    public ArmyStationSyncPacketData(List<ArmyUnitInfo> units) {
        this.units = new ArrayList<>(units);
    }
    
    public List<ArmyUnitInfo> getUnits() {
        return new ArrayList<>(units);
    }
    
    /**
     * Serialize to byte array-like structure
     */
    public byte[] encode() {
        // Simple encoding - will be handled by loader-specific packet implementations
        StringBuilder sb = new StringBuilder();
        sb.append(units.size()).append("|");
        
        for (ArmyUnitInfo unit : units) {
            sb.append(unit.getType()).append(",");
            sb.append(unit.getLevel()).append(",");
            sb.append(unit.getHealth()).append(",");
            sb.append(unit.getId().toString()).append("|");
        }
        
        return sb.toString().getBytes();
    }
    
    /**
     * Deserialize from byte array-like structure
     */
    public static ArmyStationSyncPacketData decode(byte[] data) {
        List<ArmyUnitInfo> units = new ArrayList<>();
        String str = new String(data);
        String[] parts = str.split("\\|");
        
        if (parts.length < 1) return new ArmyStationSyncPacketData(units);
        
        try {
            int count = Integer.parseInt(parts[0]);
            
            for (int i = 1; i <= count && i < parts.length; i++) {
                String[] unitParts = parts[i].split(",");
                if (unitParts.length >= 4) {
                    String type = unitParts[0];
                    int level = Integer.parseInt(unitParts[1]);
                    int health = Integer.parseInt(unitParts[2]);
                    UUID id = UUID.fromString(unitParts[3]);
                    
                    units.add(new ArmyUnitInfo(type, level, health, id));
                }
            }
        } catch (Exception e) {
            // If decoding fails, return empty list
            units.clear();
        }
        
        return new ArmyStationSyncPacketData(units);
    }
}