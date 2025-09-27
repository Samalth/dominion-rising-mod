package com.example.dominionrising.neoforge.events;

import com.example.dominionrising.neoforge.entity.UnitEntity;
import com.example.dominionrising.neoforge.registry.ModEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/**
 * Handles NeoForge entity attribute registration
 */
@EventBusSubscriber(modid = "dominionrising", bus = EventBusSubscriber.Bus.MOD)
public class EntityAttributeEvents {

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.NATION_UNIT.get(), UnitEntity.createAttributes().build());
    }
}