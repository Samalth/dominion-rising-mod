package com.example.dominionrising.forge.commands;

import com.example.dominionrising.common.nation.Nation;
import com.example.dominionrising.common.nation.NationManager;
import com.example.dominionrising.common.unit.NationUnit;
import com.example.dominionrising.common.unit.UnitManager;
import com.example.dominionrising.forge.entity.UnitEntity;
import com.example.dominionrising.forge.registry.ModEntities;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * Handles nation-related commands for Forge
 */
public class NationCommands {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nation")
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(NationCommands::createNation)))
                .then(Commands.literal("join")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(NationCommands::joinNation)))
                .then(Commands.literal("leave")
                        .executes(NationCommands::leaveNation))
                .then(Commands.literal("disband")
                        .executes(NationCommands::disbandNation))
                .then(Commands.literal("info")
                        .executes(NationCommands::nationInfo))
                .then(Commands.literal("promote")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .executes(NationCommands::promotePlayer)))
                .then(Commands.literal("demote")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .executes(NationCommands::demotePlayer)))
                .then(Commands.literal("kick")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .executes(NationCommands::kickPlayer)))
                .then(Commands.literal("spawnunit")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .executes(NationCommands::spawnUnit)))
                .then(Commands.literal("listunits")
                        .executes(NationCommands::listUnits))
                .then(Commands.literal("help")
                        .executes(NationCommands::showHelp)));
    }
    
    private static int createNation(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }
        
        String nationName = StringArgumentType.getString(context, "name");
        NationManager manager = NationManager.getInstance();
        NationManager.NationResult result = manager.createNation(nationName, player.getUUID());
        
        if (result.isSuccess()) {
            source.sendSuccess(() -> Component.literal(result.getMessage()), false);
            return 1;
        } else {
            source.sendFailure(Component.literal(result.getMessage()));
            return 0;
        }
    }
    
    private static int joinNation(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }
        
        String nationName = StringArgumentType.getString(context, "name");
        NationManager manager = NationManager.getInstance();
        NationManager.NationResult result = manager.joinNation(nationName, player.getUUID());
        
        if (result.isSuccess()) {
            source.sendSuccess(() -> Component.literal(result.getMessage()), false);
            return 1;
        } else {
            source.sendFailure(Component.literal(result.getMessage()));
            return 0;
        }
    }
    
    private static int leaveNation(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }
        
        NationManager manager = NationManager.getInstance();
        NationManager.NationResult result = manager.leaveNation(player.getUUID());
        
        if (result.isSuccess()) {
            source.sendSuccess(() -> Component.literal(result.getMessage()), false);
            return 1;
        } else {
            source.sendFailure(Component.literal(result.getMessage()));
            return 0;
        }
    }
    
    private static int nationInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }
        
        NationManager manager = NationManager.getInstance();
        var nation = manager.getPlayerNation(player.getUUID());
        
        if (nation == null) {
            source.sendFailure(Component.literal("You are not a member of any nation"));
            return 0;
        }
        
        source.sendSuccess(() -> Component.literal(String.format(
                "Nation: %s | Leader: %s | Members: %d | Balance: %.2f",
                nation.getName(),
                nation.isLeader(player.getUUID()) ? "You" : "Someone else",
                nation.getMemberCount(),
                nation.getBalance()
        )), false);
        
        return 1;
    }
    
    private static int disbandNation(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }
        
        NationManager manager = NationManager.getInstance();
        NationManager.NationResult result = manager.disbandNation(player.getUUID());
        
        if (result.isSuccess()) {
            source.sendSuccess(() -> Component.literal(result.getMessage()), false);
            return 1;
        } else {
            source.sendFailure(Component.literal(result.getMessage()));
            return 0;
        }
    }
    
    private static int showHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        String helpText = NationManager.getHelpText();
        source.sendSuccess(() -> Component.literal(helpText), false);
        
        return 1;
    }
    
    private static int promotePlayer(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }
        
        String targetPlayerName = StringArgumentType.getString(context, "player");
        
        // For now, we'll find the player by name in the current world
        // In a production implementation, you might want to store player name mappings
        ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayerByName(targetPlayerName);
        if (targetPlayer == null) {
            source.sendFailure(Component.literal("Player '" + targetPlayerName + "' not found"));
            return 0;
        }
        
        NationManager manager = NationManager.getInstance();
        NationManager.NationResult result = manager.promotePlayer(player.getUUID(), targetPlayer.getUUID());
        
        if (result.isSuccess()) {
            source.sendSuccess(() -> Component.literal(result.getMessage()), false);
            // Notify the target player
            targetPlayer.sendSystemMessage(Component.literal("You have been promoted in your nation!"));
            return 1;
        } else {
            source.sendFailure(Component.literal(result.getMessage()));
            return 0;
        }
    }
    
    private static int demotePlayer(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }
        
        String targetPlayerName = StringArgumentType.getString(context, "player");
        ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayerByName(targetPlayerName);
        if (targetPlayer == null) {
            source.sendFailure(Component.literal("Player '" + targetPlayerName + "' not found"));
            return 0;
        }
        
        NationManager manager = NationManager.getInstance();
        NationManager.NationResult result = manager.demotePlayer(player.getUUID(), targetPlayer.getUUID());
        
        if (result.isSuccess()) {
            source.sendSuccess(() -> Component.literal(result.getMessage()), false);
            // Notify the target player
            targetPlayer.sendSystemMessage(Component.literal("You have been demoted in your nation."));
            return 1;
        } else {
            source.sendFailure(Component.literal(result.getMessage()));
            return 0;
        }
    }
    
    private static int kickPlayer(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }
        
        String targetPlayerName = StringArgumentType.getString(context, "player");
        ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayerByName(targetPlayerName);
        if (targetPlayer == null) {
            source.sendFailure(Component.literal("Player '" + targetPlayerName + "' not found"));
            return 0;
        }
        
        NationManager manager = NationManager.getInstance();
        NationManager.NationResult result = manager.kickPlayer(player.getUUID(), targetPlayer.getUUID());
        
        if (result.isSuccess()) {
            source.sendSuccess(() -> Component.literal(result.getMessage()), false);
            // Notify the target player
            targetPlayer.sendSystemMessage(Component.literal("You have been kicked from your nation."));
            return 1;
        } else {
            source.sendFailure(Component.literal(result.getMessage()));
            return 0;
        }
    }
    
    private static int spawnUnit(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }
        
        String unitType = StringArgumentType.getString(context, "type");
        
        NationManager nationManager = NationManager.getInstance();
        Nation playerNation = nationManager.getPlayerNation(player.getUUID());
        
        if (playerNation == null) {
            source.sendFailure(Component.literal("You must be in a nation to spawn units"));
            return 0;
        }
        
        UnitManager unitManager = UnitManager.getInstance();
        
        if (!unitManager.isValidUnitType(unitType)) {
            source.sendFailure(Component.literal("Invalid unit type. Available types: " + 
                String.join(", ", unitManager.getAvailableUnitTypes())));
            return 0;
        }
        
        // Create the unit data
        NationUnit unit = unitManager.spawnUnit(unitType, playerNation);
        if (unit == null) {
            source.sendFailure(Component.literal("Failed to create unit"));
            return 0;
        }
        
        // Spawn the entity in the world
        UnitEntity unitEntity = new UnitEntity(ModEntities.NATION_UNIT.get(), player.level());
        unitEntity.initializeUnit(unit);
        unitEntity.setPos(player.getX(), player.getY(), player.getZ());
        
        if (player.level().addFreshEntity(unitEntity)) {
            source.sendSuccess(() -> Component.literal("Spawned " + unitType + " unit for " + playerNation.getName()), false);
            return 1;
        } else {
            // Remove from unit manager if entity spawn failed
            unitManager.removeUnit(unit.getId());
            source.sendFailure(Component.literal("Failed to spawn unit entity"));
            return 0;
        }
    }
    
    private static int listUnits(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }
        
        NationManager nationManager = NationManager.getInstance();
        Nation playerNation = nationManager.getPlayerNation(player.getUUID());
        
        if (playerNation == null) {
            source.sendFailure(Component.literal("You must be in a nation to list units"));
            return 0;
        }
        
        UnitManager unitManager = UnitManager.getInstance();
        List<NationUnit> units = unitManager.listUnits(playerNation);
        
        if (units.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Your nation has no units"), false);
            return 1;
        }
        
        source.sendSuccess(() -> Component.literal("=== " + playerNation.getName() + " Units ==="), false);
        for (NationUnit unit : units) {
            String unitInfo = String.format("%s (Lv.%d) - %d/%d HP", 
                unit.getType(), unit.getLevel(), unit.getHealth(), unit.getMaxHealth());
            source.sendSuccess(() -> Component.literal("• " + unitInfo), false);
        }
        
        return 1;
    }
}