package com.example.dominionrising.neoforge.blockentity;

import com.example.dominionrising.common.armystation.ArmyStationData;
import com.example.dominionrising.common.armystation.ArmyStationManager;
import com.example.dominionrising.common.nation.Nation;
import com.example.dominionrising.common.nation.NationManager;
import com.example.dominionrising.common.unit.NationUnit;
import com.example.dominionrising.neoforge.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
    private List<NationUnit> cachedUnits = List.of();
    
    public ArmyStationBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlocks.ARMY_STATION_BLOCK_ENTITY.get(), pos, blockState);
        this.data = new ArmyStationData();
    }
    
    public void updateNation(UUID playerId) {
        NationManager nationManager = NationManager.getInstance();
        Nation playerNation = nationManager.getPlayerNation(playerId);
        
        if (playerNation != null) {
            data.setNationId(playerNation.getName());
            // Cache the units for this nation
            this.cachedUnits = ArmyStationManager.getPlayerNationUnits(playerId);
            
            // If no real units are found, show a message in chat instead of test units
            if (this.cachedUnits.isEmpty()) {
                this.cachedUnits = new java.util.ArrayList<>();
            }
            
            // Sync data to client
            syncToClient();
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
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        String nationId = tag.getString("nationId");
        data.setNationId(nationId);
        
        // Load cached units from NBT
        this.cachedUnits = new java.util.ArrayList<>();
        if (tag.contains("cachedUnits")) {
            ListTag unitsList = tag.getList("cachedUnits", 10); // 10 = CompoundTag
            for (int i = 0; i < unitsList.size(); i++) {
                CompoundTag unitTag = unitsList.getCompound(i);
                String type = unitTag.getString("type");
                int level = unitTag.getInt("level");
                int health = unitTag.getInt("health");
                UUID id = UUID.fromString(unitTag.getString("id"));
                
                // Create a simple NationUnit for client-side display
                NationUnit unit = new NationUnit(type, data.getNationId(), level);
                // Override the ID and health from saved data
                java.lang.reflect.Field idField;
                try {
                    idField = NationUnit.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(unit, id);
                    
                    java.lang.reflect.Field healthField = NationUnit.class.getDeclaredField("health");
                    healthField.setAccessible(true);
                    healthField.set(unit, health);
                } catch (Exception e) {
                    // If reflection fails, just use what we have
                }
                
                this.cachedUnits.add(unit);
            }
        }
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putString("nationId", data.getNationId());
        
        // Save cached units for client sync
        ListTag unitsList = new ListTag();
        for (NationUnit unit : cachedUnits) {
            CompoundTag unitTag = new CompoundTag();
            unitTag.putString("type", unit.getType());
            unitTag.putInt("level", unit.getLevel());
            unitTag.putInt("health", unit.getHealth());
            unitTag.putString("id", unit.getId().toString());
            unitsList.add(unitTag);
        }
        tag.put("cachedUnits", unitsList);
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.armystation");
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.example.dominionrising.neoforge.gui.ArmyStationMenu(containerId, playerInventory, this.getBlockPos(), this.getLevel());
    }
    
    public ArmyStationData getData() {
        return data;
    }
    
    public List<NationUnit> getCachedUnits() {
        return cachedUnits;
    }
    
    // Client synchronization methods
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, provider);
        return tag;
    }
    
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        loadAdditional(tag, provider);
    }
    
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    // Call this method when data changes to sync to client
    public void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            setChanged();
        }
    }
}