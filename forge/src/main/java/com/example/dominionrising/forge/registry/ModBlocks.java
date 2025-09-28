package com.example.dominionrising.forge.registry;

import com.example.dominionrising.forge.block.ArmyStationBlock;
import com.example.dominionrising.forge.blockentity.ArmyStationBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry for Army Station blocks and items in Forge
 */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "dominionrising");
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "dominionrising");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "dominionrising");

    // Blocks
    public static final RegistryObject<Block> ARMY_STATION = BLOCKS.register("army_station", ArmyStationBlock::new);

    // Block Items
    public static final RegistryObject<Item> ARMY_STATION_ITEM = ITEMS.register("army_station",
            () -> new BlockItem(ARMY_STATION.get(), new Item.Properties()));

    // Block Entities
    public static final RegistryObject<BlockEntityType<ArmyStationBlockEntity>> ARMY_STATION_BLOCK_ENTITY = BLOCK_ENTITIES.register("army_station",
            () -> BlockEntityType.Builder.of(ArmyStationBlockEntity::new, ARMY_STATION.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }
}