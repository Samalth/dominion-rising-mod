package com.example.dominionrising;

/**
 * Main class for Dominion Rising mod
 * Contains shared constants and common logic
 */
public class DominionRising {
    
    /**
     * The mod identifier used across all loaders
     */
    public static final String MOD_ID = "dominionrising";
    
    /**
     * Initialize common mod functionality
     * Called by loader-specific entry points
     */
    public static void init() {
        // Common initialization logic will go here
        System.out.println("Dominion Rising mod initialized!");
    }
}
