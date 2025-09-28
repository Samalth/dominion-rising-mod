package com.example.dominionrising.forge;

import com.example.dominionrising.DominionRising;
import com.example.dominionrising.forge.commands.NationCommands;
import com.example.dominionrising.forge.commands.UnitCommands;
import com.example.dominionrising.forge.registry.ModBlocks;
import com.example.dominionrising.forge.registry.ModEntities;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Forge entry point for Dominion Rising mod
 */
@Mod(DominionRising.MOD_ID)
public class DominionRisingForge {
    
    public DominionRisingForge() {
        // Initialize common mod functionality
        DominionRising.init();
        
        // Register mod registries
        @SuppressWarnings("removal")
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.register(modEventBus);
        ModBlocks.register(modEventBus);
        com.example.dominionrising.forge.registry.ModMenuTypes.register(modEventBus);
        
        // Register event listeners
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        NationCommands.register(event.getDispatcher());
        UnitCommands.register(event.getDispatcher());
        com.example.dominionrising.forge.commands.ArmyStationCommands.register(event.getDispatcher());
    }
}
