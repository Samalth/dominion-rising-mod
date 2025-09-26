package com.example.dominionrising.forge;

import com.example.dominionrising.DominionRising;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge entry point for Dominion Rising mod
 */
@Mod(DominionRising.MOD_ID)
public class DominionRisingForge {
    
    public DominionRisingForge() {
        // Initialize common mod functionality
        DominionRising.init();
    }
}
