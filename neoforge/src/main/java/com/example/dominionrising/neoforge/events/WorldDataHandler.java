package com.example.dominionrising.neoforge.events;

import com.example.dominionrising.neoforge.data.NationSavedData;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * Handles world events for loading and saving nation data in NeoForge
 */
@EventBusSubscriber
public class WorldDataHandler {
    
    private static NationSavedData savedData;
    
    /**
     * Load nation data when world loads
     */
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            // Only process overworld to avoid multiple loads
            if (serverLevel.dimension() == ServerLevel.OVERWORLD) {
                savedData = NationSavedData.get(serverLevel);
                savedData.loadIntoManager();
                System.out.println("Dominion Rising: Loaded nation data");
            }
        }
    }
    
    /**
     * Save nation data when world unloads
     */
    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            // Only process overworld to avoid multiple saves
            if (serverLevel.dimension() == ServerLevel.OVERWORLD && savedData != null) {
                savedData.saveFromManager();
                System.out.println("Dominion Rising: Saved nation data");
            }
        }
    }
    
    /**
     * Save nation data when server stops
     */
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        if (savedData != null) {
            savedData.saveFromManager();
            System.out.println("Dominion Rising: Saved nation data on server stop");
        }
    }
}