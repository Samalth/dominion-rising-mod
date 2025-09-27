package com.example.dominionrising.neoforge.commands;

import com.example.dominionrising.common.nation.Nation;
import com.example.dominionrising.common.nation.NationManager;
import com.example.dominionrising.common.nation.NationRole;
import com.example.dominionrising.common.unit.NationUnit;
import com.example.dominionrising.common.unit.UnitManager;
import com.example.dominionrising.neoforge.entity.UnitEntity;
import com.example.dominionrising.neoforge.registry.ModEntities;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

import java.util.List;

/**
 * Handles nation-related commands for NeoForge
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
        
        // Find the player by name in the current world
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
    
    private static int listMembers(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }

        NationManager manager = NationManager.getInstance();
        Nation playerNation = manager.getPlayerNation(player.getUUID());
        
        if (playerNation == null) {
            source.sendFailure(Component.literal("You are not in a nation"));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§6=== " + playerNation.getName() + " Members ==="), false);
        
        // Get leader info
        UUID leaderId = playerNation.getLeader();
        String leaderName = getPlayerNameFromUUID(source, leaderId);
        source.sendSuccess(() -> Component.literal("§e★ Leader: §f" + leaderName), false);
        
        // Get other members
        List<UUID> members = playerNation.getMembers();
        if (members.size() > 1) {
            source.sendSuccess(() -> Component.literal("§7Members:"), false);
            for (UUID memberId : members) {
                if (!memberId.equals(leaderId)) {
                    String memberName = getPlayerNameFromUUID(source, memberId);
                    String role = playerNation.getMemberRole(memberId).name();
                    source.sendSuccess(() -> Component.literal("§7• §f" + memberName + " §8(" + role + ")"), false);
                }
            }
        }
        
        // Show unit count
        UnitManager unitManager = UnitManager.getInstance();
        List<NationUnit> units = unitManager.listUnits(playerNation);
        source.sendSuccess(() -> Component.literal("§7Units: §f" + units.size()), false);
        
        return 1;
    }
    
    private static int invitePlayer(CommandContext<CommandSourceStack> context) {
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
        Nation playerNation = manager.getPlayerNation(player.getUUID());
        
        if (playerNation == null) {
            source.sendFailure(Component.literal("You are not in a nation"));
            return 0;
        }
        
        // Check if player has permission to invite (leader or commander)
        NationRole playerRole = playerNation.getMemberRole(player.getUUID());
        if (playerRole != NationRole.LEADER && playerRole != NationRole.COMMANDER) {
            source.sendFailure(Component.literal("You don't have permission to invite players"));
            return 0;
        }
        
        // Check if target is already in a nation
        if (manager.getPlayerNation(targetPlayer.getUUID()) != null) {
            source.sendFailure(Component.literal(targetPlayerName + " is already in a nation"));
            return 0;
        }
        
        // Send invite (for now, just auto-join - could implement invite system later)
        NationManager.NationResult result = manager.joinNation(playerNation.getName(), targetPlayer.getUUID());
        
        if (result.isSuccess()) {
            source.sendSuccess(() -> Component.literal("Successfully added " + targetPlayerName + " to " + playerNation.getName()), false);
            // Notify the target player
            targetPlayer.sendSystemMessage(Component.literal("§6You have been added to nation '" + playerNation.getName() + "'"));
            return 1;
        } else {
            source.sendFailure(Component.literal(result.getMessage()));
            return 0;
        }
    }
    
    private static int acceptInvite(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }

        String nationName = StringArgumentType.getString(context, "nation");
        NationManager manager = NationManager.getInstance();
        
        // For now, this just joins the nation directly
        NationManager.NationResult result = manager.joinNation(nationName, player.getUUID());
        
        if (result.isSuccess()) {
            source.sendSuccess(() -> Component.literal(result.getMessage()), false);
            return 1;
        } else {
            source.sendFailure(Component.literal(result.getMessage()));
            return 0;
        }
    }
    
    /**
     * Helper method to get player name from UUID
     */
    private static String getPlayerNameFromUUID(CommandSourceStack source, UUID playerId) {
        ServerPlayer player = source.getServer().getPlayerList().getPlayer(playerId);
        if (player != null) {
            return player.getName().getString();
        }
        return "Unknown Player";
    }
}