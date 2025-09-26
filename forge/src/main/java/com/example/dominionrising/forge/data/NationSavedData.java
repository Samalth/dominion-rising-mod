package com.example.dominionrising.forge.data;

import com.example.dominionrising.common.nation.NationDataSerializer;
import com.example.dominionrising.common.nation.NationManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;

/**
 * Forge implementation for persisting nation data using SavedData
 */
public class NationSavedData extends SavedData {
    private static final String DATA_NAME = "dominion_rising_nations";
    
    private String nationData = "";
    
    public NationSavedData() {
        super();
    }
    
    public NationSavedData(String nationData) {
        this();
        this.nationData = nationData;
    }
    
    /**
     * Get or create the nation saved data for a world
     */
    public static NationSavedData get(ServerLevel world) {
        DimensionDataStorage storage = world.getDataStorage();
        return storage.computeIfAbsent(
            new SavedData.Factory<>(
                NationSavedData::new,
                NationSavedData::load,
                null
            ),
            DATA_NAME
        );
    }
    
    /**
     * Load nation data from NBT
     */
    public static NationSavedData load(CompoundTag nbt, HolderLookup.Provider provider) {
        String data = nbt.getString("nationData");
        return new NationSavedData(data);
    }
    
    /**
     * Save nation data to NBT
     */
    @Override
    @Nonnull
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        nbt.putString("nationData", nationData);
        return nbt;
    }
    
    /**
     * Load nation data into the NationManager
     */
    public void loadIntoManager() {
        if (!nationData.isEmpty()) {
            try {
                NationDataSerializer.NationData data = NationDataSerializer.deserializeNations(nationData);
                NationManager manager = NationManager.getInstance();
                manager.loadData(data.nations, data.playerToNation);
            } catch (Exception e) {
                System.err.println("Failed to load nation data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Save nation data from the NationManager
     */
    public void saveFromManager() {
        try {
            NationManager manager = NationManager.getInstance();
            this.nationData = NationDataSerializer.serializeNations(
                manager.getAllNations(),
                manager.getPlayerToNationMap()
            );
            setDirty();
        } catch (Exception e) {
            System.err.println("Failed to save nation data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}