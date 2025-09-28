package com.example.dominionrising.neoforge.client;

import com.example.dominionrising.neoforge.registry.ModMenuTypes;
import com.example.dominionrising.neoforge.screen.ArmyStationScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Client-side initialization for NeoForge
 */
@EventBusSubscriber(modid = "dominionrising", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ARMY_STATION_MENU.get(), ArmyStationScreen::new);
    }
}