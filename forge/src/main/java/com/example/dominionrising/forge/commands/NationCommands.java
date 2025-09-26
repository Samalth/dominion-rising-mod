package com.example.dominionrising.forge.commands;

import com.example.dominionrising.common.nation.NationManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

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
                        .executes(NationCommands::nationInfo)));
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
}