package com.example.dominionrising.forge.block;

import com.example.dominionrising.common.armystation.ArmyStationManager;
import com.example.dominionrising.common.unit.NationUnit;
import com.example.dominionrising.forge.blockentity.ArmyStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * Army Station block for Forge - allows Leaders and Commanders to view their nation's units
 */
public class ArmyStationBlock extends BaseEntityBlock {

    public ArmyStationBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.5F)
                .requiresCorrectToolForDrops());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArmyStationBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // Required codec method for 1.21.1
    private static final com.mojang.serialization.MapCodec<ArmyStationBlock> CODEC = 
        com.mojang.serialization.MapCodec.unit(ArmyStationBlock::new);
        
    @Override  
    protected com.mojang.serialization.MapCodec<ArmyStationBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        
        // Check if player can access Army Station
        var accessResult = ArmyStationManager.canPlayerAccess(serverPlayer.getUUID());
        
        if (!accessResult.isSuccess()) {
            serverPlayer.sendSystemMessage(Component.literal(accessResult.getMessage()));
            return InteractionResult.FAIL;
        }
        
        // Get or create the block entity
        if (level.getBlockEntity(pos) instanceof ArmyStationBlockEntity blockEntity) {
            // Set the nation ID in the block entity based on the player's nation
            blockEntity.updateNation(serverPlayer.getUUID());
            blockEntity.setChanged();
            
            // Show unit list in chat (simple implementation)
            showUnitList(serverPlayer);
        }
        
        return InteractionResult.SUCCESS;
    }
    
    private void showUnitList(ServerPlayer player) {
        List<NationUnit> units = ArmyStationManager.getPlayerNationUnits(player.getUUID());
        
        player.sendSystemMessage(Component.literal("§6=== Army Station - Unit List ==="));
        
        if (units.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7No units found for your nation."));
        } else {
            for (NationUnit unit : units) {
                String unitInfo = ArmyStationManager.formatUnitInfo(unit);
                player.sendSystemMessage(Component.literal("§f• " + unitInfo));
            }
        }
        
        player.sendSystemMessage(Component.literal("§6========================"));
    }
}