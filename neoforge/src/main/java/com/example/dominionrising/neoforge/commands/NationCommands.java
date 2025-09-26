package com.example.dominionrising.neoforge.commands;

import com.example.dominionrising.common.nation.NationManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

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
}