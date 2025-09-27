package com.example.dominionrising.neoforge;

import com.example.dominionrising.DominionRising;
import com.example.dominionrising.neoforge.commands.NationCommands;
import com.example.dominionrising.neoforge.registry.ModEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * NeoForge entry point for Dominion Rising mod
 */
@Mod(DominionRising.MOD_ID)
public class DominionRisingNeoForge {
    
    public DominionRisingNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // Initialize common mod functionality
        DominionRising.init();
        
        // Register mod registries
        ModEntities.register(modEventBus);
        
        // Register event listeners
        NeoForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        NationCommands.register(event.getDispatcher());
    }
}
