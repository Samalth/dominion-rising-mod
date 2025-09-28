package com.example.dominionrising.neoforge.registry;

import com.example.dominionrising.neoforge.block.ArmyStationBlock;
import com.example.dominionrising.neoforge.blockentity.ArmyStationBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry for Army Station blocks and items in NeoForge
 */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, "dominionrising");
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, "dominionrising");  
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "dominionrising");

    // Blocks
    public static final Supplier<Block> ARMY_STATION = BLOCKS.register("army_station", ArmyStationBlock::new);

    // Block Items
    public static final Supplier<Item> ARMY_STATION_ITEM = ITEMS.register("army_station",
            () -> new BlockItem(ARMY_STATION.get(), new Item.Properties()));

    // Block Entities
    public static final Supplier<BlockEntityType<ArmyStationBlockEntity>> ARMY_STATION_BLOCK_ENTITY = BLOCK_ENTITIES.register("army_station",
            () -> BlockEntityType.Builder.of(ArmyStationBlockEntity::new, ARMY_STATION.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }
}