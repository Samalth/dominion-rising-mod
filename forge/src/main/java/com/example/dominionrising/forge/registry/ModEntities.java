package com.example.dominionrising.forge.registry;

import com.example.dominionrising.forge.entity.UnitEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry for Dominion Rising entities in Forge
 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "dominionrising");

    public static final RegistryObject<EntityType<UnitEntity>> NATION_UNIT = ENTITIES.register("nation_unit",
            () -> EntityType.Builder.of(UnitEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .build("nation_unit"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}