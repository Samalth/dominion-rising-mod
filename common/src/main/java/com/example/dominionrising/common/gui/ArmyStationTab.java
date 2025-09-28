package com.example.dominionrising.common.gui;

/**
 * Enum for Army Station GUI tabs
 */
public enum ArmyStationTab {
    UNITS("Units"),
    GROUP("Group");
    
    private final String displayName;
    
    ArmyStationTab(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}