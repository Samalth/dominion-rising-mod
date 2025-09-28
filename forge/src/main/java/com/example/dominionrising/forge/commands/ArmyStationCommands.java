package com.example.dominionrising.forge.commands;

import com.example.dominionrising.forge.registry.ModBlocks;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Commands for Army Station testing
 */
public class ArmyStationCommands {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("armystation")
                .then(Commands.literal("give")
                        .executes(context -> giveArmyStation(context.getSource()))));
    }
    
    private static int giveArmyStation(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }
        
        ItemStack armyStationItem = new ItemStack(ModBlocks.ARMY_STATION_ITEM.get());
        player.getInventory().add(armyStationItem);
        
        source.sendSuccess(() -> Component.literal("Army Station block given!"), false);
        return 1;
    }
}