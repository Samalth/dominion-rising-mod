package com.example.dominionrising.forge.client;

import com.example.dominionrising.forge.registry.ModMenuTypes;
import com.example.dominionrising.forge.screen.ArmyStationScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-side initialization for Forge
 */
@Mod.EventBusSubscriber(modid = "dominionrising", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.ARMY_STATION_MENU.get(), ArmyStationScreen::new);
        });
    }
}