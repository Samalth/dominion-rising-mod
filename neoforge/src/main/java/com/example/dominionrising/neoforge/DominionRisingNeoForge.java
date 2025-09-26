package com.example.dominionrising.neoforge;

import com.example.dominionrising.DominionRising;
import net.neoforged.fml.common.Mod;

/**
 * NeoForge entry point for Dominion Rising mod
 */
@Mod(DominionRising.MOD_ID)
public class DominionRisingNeoForge {
    
    public DominionRisingNeoForge() {
        // Initialize common mod functionality
        DominionRising.init();
    }
}
