package com.example.dominionrising.neoforge.registry;

import com.example.dominionrising.neoforge.entity.UnitEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry for Dominion Rising entities in NeoForge
 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, "dominionrising");

    public static final Supplier<EntityType<UnitEntity>> NATION_UNIT = ENTITIES.register("nation_unit",
            () -> EntityType.Builder.of(UnitEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .build("nation_unit"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}