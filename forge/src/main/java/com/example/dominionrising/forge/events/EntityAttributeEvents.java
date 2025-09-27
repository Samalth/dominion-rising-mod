package com.example.dominionrising.forge.events;

import com.example.dominionrising.forge.entity.UnitEntity;
import com.example.dominionrising.forge.registry.ModEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles Forge entity attribute registration
 */
@Mod.EventBusSubscriber(modid = "dominionrising", bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityAttributeEvents {

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.NATION_UNIT.get(), UnitEntity.createAttributes().build());
    }
}