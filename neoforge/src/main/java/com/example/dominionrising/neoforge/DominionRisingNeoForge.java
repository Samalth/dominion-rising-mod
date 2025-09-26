package com.example.dominionrising.neoforge;

import com.example.dominionrising.DominionRising;
import com.example.dominionrising.neoforge.commands.NationCommands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * NeoForge entry point for Dominion Rising mod
 */
@Mod(DominionRising.MOD_ID)
public class DominionRisingNeoForge {
    
    public DominionRisingNeoForge() {
        // Initialize common mod functionality
        DominionRising.init();
        
        // Register event listeners
        NeoForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        NationCommands.register(event.getDispatcher());
    }
}
