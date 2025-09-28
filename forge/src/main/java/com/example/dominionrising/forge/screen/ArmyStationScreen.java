package com.example.dominionrising.forge.screen;

import com.example.dominionrising.common.gui.ArmyStationTab;
import com.example.dominionrising.common.gui.ArmyUnitInfo;
import com.example.dominionrising.common.network.GroupUnitsPacket;
import com.example.dominionrising.forge.gui.ArmyStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side screen for Army Station GUI (Forge)
 */
public class ArmyStationScreen extends AbstractContainerScreen<ArmyStationMenu> {
    private List<ArmyUnitInfo> unitInfos = new ArrayList<>();
    private boolean dataLoaded = false;
    
    // Tab system
    private ArmyStationTab currentTab = ArmyStationTab.UNITS;
    private Button unitsTabButton;
    private Button groupTabButton;
    
    // GROUP tab components
    private Button groupSelectedButton;
    private Map<UUID, EditBox> unitCountFields = new HashMap<>();
    private static final int TAB_BUTTON_WIDTH = 60;
    private static final int TAB_BUTTON_HEIGHT = 20;
    
    public ArmyStationScreen(ArmyStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 200; // Increased height for tabs and GROUP content
    }
    
    @Override
    protected void init() {
        super.init();
        loadUnitData();
        setupTabs();
        setupGroupComponents();
        updateTabVisibility(); // Move this after all components are initialized
    }
    
    private void setupTabs() {
        int tabY = topPos - 25;
        
        // Units tab button
        unitsTabButton = Button.builder(Component.literal("Units"), (button) -> {
            currentTab = ArmyStationTab.UNITS;
            updateTabVisibility();
        }).bounds(leftPos + 10, tabY, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT).build();
        
        // Group tab button
        groupTabButton = Button.builder(Component.literal("Group"), (button) -> {
            currentTab = ArmyStationTab.GROUP;
            updateTabVisibility();
        }).bounds(leftPos + 75, tabY, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT).build();
        
        addRenderableWidget(unitsTabButton);
        addRenderableWidget(groupTabButton);
    }
    
    private void setupGroupComponents() {
        // Group Selected Units button
        groupSelectedButton = Button.builder(Component.literal("Group Selected Units"), (button) -> {
            sendGroupUnitsPacket();
        }).bounds(leftPos + 10, topPos + 160, 156, 20).build();
        
        addRenderableWidget(groupSelectedButton);
        
        // Setup text fields for each unit (will be populated when data loads)
        updateGroupComponents();
    }
    
    private void updateTabVisibility() {
        groupSelectedButton.visible = (currentTab == ArmyStationTab.GROUP);
        
        // Update text field visibility
        for (EditBox field : unitCountFields.values()) {
            field.setVisible(currentTab == ArmyStationTab.GROUP);
        }
    }
    
    private void updateGroupComponents() {
        // Clear existing text fields
        unitCountFields.values().forEach(this::removeWidget);
        unitCountFields.clear();
        
        if (currentTab == ArmyStationTab.GROUP && dataLoaded) {
            int yOffset = 40;
            for (int i = 0; i < unitInfos.size() && i < 8; i++) {
                ArmyUnitInfo unit = unitInfos.get(i);
                
                // Create text field for unit count
                EditBox countField = new EditBox(font, leftPos + 120, topPos + yOffset, 30, 12, Component.literal("Count"));
                countField.setValue("0");
                countField.setMaxLength(3);
                countField.setVisible(currentTab == ArmyStationTab.GROUP);
                
                unitCountFields.put(unit.getId(), countField);
                addRenderableWidget(countField);
                
                yOffset += 15;
            }
        }
    }
    
    private void sendGroupUnitsPacket() {
        Map<UUID, Integer> unitCounts = new HashMap<>();
        
        for (Map.Entry<UUID, EditBox> entry : unitCountFields.entrySet()) {
            try {
                int count = Integer.parseInt(entry.getValue().getValue());
                if (count > 0) {
                    unitCounts.put(entry.getKey(), count);
                }
            } catch (NumberFormatException e) {
                // Invalid number, skip this unit
            }
        }
        
        if (!unitCounts.isEmpty()) {
            GroupUnitsPacket packet = new GroupUnitsPacket(unitCounts);
            // TODO: Send packet to server (would need networking implementation)
            minecraft.player.sendSystemMessage(Component.literal("Grouping " + packet.getTotalUnits() + " units"));
        }
    }
    
    private void loadUnitData() {
        try {
            // Get unit data from registry instead of BlockEntity
            this.unitInfos = com.example.dominionrising.forge.gui.ArmyStationDataRegistry.getPlayerUnits(this.minecraft.player.getUUID());
            this.dataLoaded = !this.unitInfos.isEmpty();
            
            // Update GROUP tab components when data changes
            if (dataLoaded) {
                updateGroupComponents();
            }
        } catch (Exception e) {
            this.dataLoaded = false;
        }
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
    
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Simple background - no texture needed for now
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xC0101010);
        graphics.fill(this.leftPos + 1, this.topPos + 1, this.leftPos + this.imageWidth - 1, this.topPos + this.imageHeight - 1, 0xC0C0C0C0);
    }
    
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Title
        graphics.drawString(this.font, this.title, 8, 6, 4210752, false);
        
        // Try to reload data if not loaded yet or periodically refresh
        if (!dataLoaded || this.minecraft.level.getGameTime() % 20 == 0) {
            loadUnitData();
        }
        
        // Show fallback message if still no data
        if (!dataLoaded) {
            graphics.drawString(this.font, "No units under your command", 8, 25, 0xFF6666, false);
            return;
        }
        
        // Render content based on current tab
        if (currentTab == ArmyStationTab.UNITS) {
            renderUnitsTab(graphics);
        } else if (currentTab == ArmyStationTab.GROUP) {
            renderGroupTab(graphics);
        }
    }
    
    private void renderUnitsTab(GuiGraphics graphics) {
        // Render unit list (existing UNITS tab code)
        if (!unitInfos.isEmpty()) {
            graphics.drawString(this.font, "Units:", 8, 25, 4210752, false);
            
            int y = 40;
            int maxLines = 8; // Limit display to prevent overflow
            int count = Math.min(unitInfos.size(), maxLines);
            
            for (int i = 0; i < count; i++) {
                ArmyUnitInfo unit = unitInfos.get(i);
                String unitText = unit.getType() + " | Lv " + unit.getLevel() + " | HP " + unit.getHealth();
                graphics.drawString(this.font, unitText, 8, y, 0xFFFFFF, false);
                y += 12;
            }
            
            if (unitInfos.size() > maxLines) {
                graphics.drawString(this.font, "... and " + (unitInfos.size() - maxLines) + " more", 8, y, 0x888888, false);
            }
        } else {
            graphics.drawString(this.font, "No units found", 8, 25, 0x888888, false);
        }
    }
    
    private void renderGroupTab(GuiGraphics graphics) {
        if (!unitInfos.isEmpty()) {
            graphics.drawString(this.font, "Select units to group:", 8, 25, 4210752, false);
            
            int y = 40;
            int maxLines = 8; // Limit display to prevent overflow
            int count = Math.min(unitInfos.size(), maxLines);
            
            for (int i = 0; i < count; i++) {
                ArmyUnitInfo unit = unitInfos.get(i);
                
                // Unit info text
                String unitText = unit.getType() + " | Lv " + unit.getLevel();
                graphics.drawString(this.font, unitText, 8, y, 0xFFFFFF, false);
                
                // Available count
                graphics.drawString(this.font, "{available}", 155, y, 0x888888, false);
                
                y += 15;
            }
            
            if (unitInfos.size() > maxLines) {
                graphics.drawString(this.font, "... and " + (unitInfos.size() - maxLines) + " more units", 8, y, 0x888888, false);
            }
        } else {
            graphics.drawString(this.font, "No units available for grouping", 8, 25, 0x888888, false);
        }
    }
    
    @Override
    public void onClose() {
        super.onClose();
        unitInfos.clear();
    }
}