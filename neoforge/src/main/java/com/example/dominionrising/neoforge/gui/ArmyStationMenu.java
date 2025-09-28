package com.example.dominionrising.neoforge.gui;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;

/**
 * NeoForge implementation of Army Station menu
 */
public class ArmyStationMenu extends AbstractContainerMenu {
    private final BlockPos blockPos;
    private final Level level;
    
    public ArmyStationMenu(int containerId, Inventory playerInventory, BlockPos blockPos, Level level) {
        super(com.example.dominionrising.neoforge.registry.ModMenuTypes.ARMY_STATION_MENU.get(), containerId);
        this.blockPos = blockPos;
        this.level = level;
    }
    
    // Constructor for client-side
    public ArmyStationMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, BlockPos.ZERO, playerInventory.player.level());
    }
    
    @Override
    public boolean stillValid(Player player) {
        return this.level.getBlockEntity(this.blockPos) != null && 
               player.distanceToSqr(this.blockPos.getX() + 0.5, this.blockPos.getY() + 0.5, this.blockPos.getZ() + 0.5) <= 64.0;
    }
    
    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        // No inventory slots, so no quick move needed
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
    
    public BlockPos getBlockPos() {
        return blockPos;
    }
    
    public Level getLevel() {
        return level;
    }
}