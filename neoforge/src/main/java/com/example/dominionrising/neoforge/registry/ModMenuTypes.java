package com.example.dominionrising.neoforge.registry;

import com.example.dominionrising.neoforge.gui.ArmyStationMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry for menu types in NeoForge
 */
public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = 
        DeferredRegister.create(BuiltInRegistries.MENU, "dominionrising");

    public static final Supplier<MenuType<ArmyStationMenu>> ARMY_STATION_MENU = 
        MENU_TYPES.register("armystation", 
            () -> IMenuTypeExtension.create((windowId, inv, data) -> {
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