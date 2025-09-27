package com.example.dominionrising.forge.data;

import com.example.dominionrising.common.nation.NationDataSerializer;
import com.example.dominionrising.common.nation.NationManager;
import com.example.dominionrising.common.unit.UnitManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;

/**
 * Forge implementation for persisting nation and unit data using SavedData
 */
public class NationSavedData extends SavedData {
    private static final String DATA_NAME = "dominion_rising_nations";
    
    private String nationData = "";
    private String unitData = "";
    
    public NationSavedData() {
        super();
    }
    
    public NationSavedData(String nationData, String unitData) {
        this();
        this.nationData = nationData;
        this.unitData = unitData;
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
     * Load nation and unit data from NBT
     */
    public static NationSavedData load(CompoundTag nbt, HolderLookup.Provider provider) {
        String nationData = nbt.getString("nationData");
        String unitData = nbt.getString("unitData");
        return new NationSavedData(nationData, unitData);
    }
    
    /**
     * Save nation and unit data to NBT
     */
    @Override
    @Nonnull
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        nbt.putString("nationData", nationData);
        nbt.putString("unitData", unitData);
        return nbt;
    }
    
    /**
     * Load nation and unit data into the managers
     */
    public void loadIntoManager() {
        // Load nation data
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
        
        // Load unit data
        if (!unitData.isEmpty()) {
            try {
                UnitManager unitManager = UnitManager.getInstance();
                unitManager.loadUnits(unitData);
            } catch (Exception e) {
                System.err.println("Failed to load unit data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Save nation and unit data from the managers
     */
    public void saveFromManager() {
        try {
            // Save nation data
            NationManager manager = NationManager.getInstance();
            this.nationData = NationDataSerializer.serializeNations(
                manager.getAllNations(),
                manager.getPlayerToNationMap()
            );
            
            // Save unit data
            UnitManager unitManager = UnitManager.getInstance();
            this.unitData = unitManager.serializeUnits();
            
            setDirty();
        } catch (Exception e) {
            System.err.println("Failed to save nation and unit data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}