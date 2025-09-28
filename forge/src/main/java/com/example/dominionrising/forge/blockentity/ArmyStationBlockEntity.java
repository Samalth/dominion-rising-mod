package com.example.dominionrising.forge.blockentity;

import com.example.dominionrising.common.armystation.ArmyStationData;
import com.example.dominionrising.common.armystation.ArmyStationManager;
import com.example.dominionrising.common.nation.Nation;
import com.example.dominionrising.common.nation.NationManager;
import com.example.dominionrising.common.unit.NationUnit;
import com.example.dominionrising.forge.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Block Entity for Army Station - stores nation data and provides GUI
 */
public class ArmyStationBlockEntity extends BlockEntity implements MenuProvider {
    private ArmyStationData data;
    
    public ArmyStationBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlocks.ARMY_STATION_BLOCK_ENTITY.get(), pos, blockState);
        this.data = new ArmyStationData();
    }
    
    public void updateNation(UUID playerId) {
        NationManager nationManager = NationManager.getInstance();
        Nation playerNation = nationManager.getPlayerNation(playerId);
        
        if (playerNation != null) {
            data.setNationId(playerNation.getName());
        }
    }
    
    public List<NationUnit> getNationUnits() {
        if (!data.hasNation()) {
            return List.of();
        }
        
        NationManager nationManager = NationManager.getInstance();
        Nation nation = nationManager.getNation(data.getNationId());
        
        if (nation != null) {
            return ArmyStationManager.getPlayerNationUnits(nation.getLeader());
        }
        
        return List.of();
    }
    
    @Override
    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        String nationId = tag.getString("nationId");
        data.setNationId(nationId);
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putString("nationId", data.getNationId());
    }
    
    @Override
    public Component getDisplayName() {
        return Component.literal("Army Station");
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        // For now, we'll return null and handle GUI differently
        // A proper container menu would be needed for full GUI implementation
        return null;
    }
    
    public ArmyStationData getData() {
        return data;
    }
}