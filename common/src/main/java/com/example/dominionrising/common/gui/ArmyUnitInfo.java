package com.example.dominionrising.common.gui;

import java.util.UUID;

/**
 * Simple data container for Army Station unit information
 * Used for client-server communication
 */
public class ArmyUnitInfo {
    private final String type;
    private final int level;
    private final int health;
    private final UUID id;
    
    public ArmyUnitInfo(String type, int level, int health, UUID id) {
        this.type = type;
        this.level = level;
        this.health = health;
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public int getLevel() {
        return level;
    }
    
    public int getHealth() {
        return health;
    }
    
    public UUID getId() {
        return id;
    }
}