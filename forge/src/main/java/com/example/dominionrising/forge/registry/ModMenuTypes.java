package com.example.dominionrising.forge.registry;

import com.example.dominionrising.forge.gui.ArmyStationMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry for menu types in Forge
 */
public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = 
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, "dominionrising");

    public static final RegistryObject<MenuType<ArmyStationMenu>> ARMY_STATION_MENU = 
        MENU_TYPES.register("armystation", 
            () -> IForgeMenuType.create((windowId, inv, data) -> {
                if (data != null) {
                    return new ArmyStationMenu(windowId, inv, data.readBlockPos(), inv.player.level());
                } else {
                    // Client-side fallback when no data is provided
                    return new ArmyStationMenu(windowId, inv);
                }
            }));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}