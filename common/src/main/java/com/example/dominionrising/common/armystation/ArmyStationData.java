package com.example.dominionrising.common.armystation;

/**
 * Data container for Army Station block entity
 * Stores nation information for persistence
 */
public class ArmyStationData {
    private String nationId;
    
    public ArmyStationData() {
        this.nationId = "";
    }
    
    public ArmyStationData(String nationId) {
        this.nationId = nationId != null ? nationId : "";
    }
    
    public String getNationId() {
        return nationId;
    }
    
    public void setNationId(String nationId) {
        this.nationId = nationId != null ? nationId : "";
    }
    
    public boolean hasNation() {
        return !nationId.isEmpty();
    }
}