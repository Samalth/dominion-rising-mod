package com.example.dominionrising.forge;

import com.example.dominionrising.DominionRising;
import com.example.dominionrising.forge.commands.NationCommands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge entry point for Dominion Rising mod
 */
@Mod(DominionRising.MOD_ID)
public class DominionRisingForge {
    
    public DominionRisingForge() {
        // Initialize common mod functionality
        DominionRising.init();
        
        // Register event listeners
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        NationCommands.register(event.getDispatcher());
    }
}
