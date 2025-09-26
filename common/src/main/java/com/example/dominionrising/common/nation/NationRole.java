package com.example.dominionrising.common.nation;

/**
 * Represents the different roles a player can have within a nation
 */
public enum NationRole {
    /**
     * Leader of the nation - has all permissions
     * Can create, disband, promote, demote, kick members
     */
    LEADER("Leader"),
    
    /**
     * Commander of the nation - has limited administrative permissions
     * Can invite players but cannot disband or change leadership
     */
    COMMANDER("Commander"),
    
    /**
     * Citizen of the nation - basic member
     * Can leave the nation but has no administrative permissions
     */
    CITIZEN("Citizen");
    
    private final String displayName;
    
    NationRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Check if this role can promote other members
     */
    public boolean canPromote() {
        return this == LEADER;
    }
    
    /**
     * Check if this role can demote other members
     */
    public boolean canDemote() {
        return this == LEADER;
    }
    
    /**
     * Check if this role can kick other members
     */
    public boolean canKick() {
        return this == LEADER;
    }
    
    /**
     * Check if this role can disband the nation
     */
    public boolean canDisband() {
        return this == LEADER;
    }
    
    /**
     * Check if this role can invite new members
     */
    public boolean canInvite() {
        return this == LEADER || this == COMMANDER;
    }
    
    /**
     * Get the priority/hierarchy of this role (higher = more important)
     */
    public int getPriority() {
        return switch (this) {
            case LEADER -> 3;
            case COMMANDER -> 2;
            case CITIZEN -> 1;
        };
    }
    
    /**
     * Check if this role outranks another role
     */
    public boolean outranks(NationRole other) {
        return this.getPriority() > other.getPriority();
    }
}