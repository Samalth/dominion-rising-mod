package com.example.dominionrising.forge.commands;

import com.example.dominionrising.common.nation.Nation;
import com.example.dominionrising.common.nation.NationManager;
import com.example.dominionrising.common.unit.NationUnit;
import com.example.dominionrising.common.unit.UnitManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/**
 * Handles unit tactical commands for Forge
 */
public class UnitCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("unit")
                .then(Commands.literal("attack")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(UnitCommands::attackTarget)))
                .then(Commands.literal("defend")
                        .executes(UnitCommands::defendCurrentPosition)
                        .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                .executes(UnitCommands::defendPosition)))))
                .then(Commands.literal("status")
                        .executes(UnitCommands::showUnitStatus))
                .then(Commands.literal("idle")
                        .executes(UnitCommands::setUnitsIdle))
                .then(Commands.literal("help")
                        .executes(UnitCommands::showUnitHelp)));
    }

    private static int attackTarget(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }

        try {
            Entity target = EntityArgument.getEntity(context, "target");
            
            NationManager nationManager = NationManager.getInstance();
            Nation playerNation = nationManager.getPlayerNation(player.getUUID());
            
            if (playerNation == null) {
                source.sendFailure(Component.literal("You must be in a nation to command units"));
                return 0;
            }

            UnitManager unitManager = UnitManager.getInstance();
            List<NationUnit> nearbyUnits = unitManager.getUnitsInRange(
                playerNation.getName(), 
                player.getX(), player.getY(), player.getZ(), 
                32.0 // 32 block command range
            );

            if (nearbyUnits.isEmpty()) {
                source.sendFailure(Component.literal("No units in range to command"));
                return 0;
            }

            // Command all nearby units to attack the target
            int commandedUnits = 0;
            UUID targetId = target.getUUID();
            
            for (NationUnit unit : nearbyUnits) {
                if (unitManager.commandAttack(unit.getId(), targetId)) {
                    commandedUnits++;
                }
            }

            final int finalCommandedUnits = commandedUnits;
            if (commandedUnits > 0) {
                source.sendSuccess(() -> Component.literal(
                    "Commanded " + finalCommandedUnits + " unit(s) to attack " + target.getDisplayName().getString()
                ), false);
                return 1;
            } else {
                source.sendFailure(Component.literal("Failed to command units to attack"));
                return 0;
            }
            
        } catch (Exception e) {
            source.sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int defendPosition(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }

        double x = DoubleArgumentType.getDouble(context, "x");
        double y = DoubleArgumentType.getDouble(context, "y");
        double z = DoubleArgumentType.getDouble(context, "z");

        NationManager nationManager = NationManager.getInstance();
        Nation playerNation = nationManager.getPlayerNation(player.getUUID());
        
        if (playerNation == null) {
            source.sendFailure(Component.literal("You must be in a nation to command units"));
            return 0;
        }

        UnitManager unitManager = UnitManager.getInstance();
        List<NationUnit> nearbyUnits = unitManager.getUnitsInRange(
            playerNation.getName(), 
            player.getX(), player.getY(), player.getZ(), 
            32.0 // 32 block command range
        );

        if (nearbyUnits.isEmpty()) {
            source.sendFailure(Component.literal("No units in range to command"));
            return 0;
        }

        // Command all nearby units to defend the position
        int commandedUnits = 0;
        
        for (NationUnit unit : nearbyUnits) {
            if (unitManager.commandDefend(unit.getId(), x, y, z)) {
                commandedUnits++;
            }
        }

        final int finalCommandedUnits = commandedUnits;
        if (commandedUnits > 0) {
            source.sendSuccess(() -> Component.literal(
                String.format("Commanded %d unit(s) to defend position %.1f, %.1f, %.1f", 
                             finalCommandedUnits, x, y, z)
            ), false);
            return 1;
        } else {
            source.sendFailure(Component.literal("Failed to command units to defend"));
            return 0;
        }
    }

    private static int defendCurrentPosition(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }

        NationManager nationManager = NationManager.getInstance();
        Nation playerNation = nationManager.getPlayerNation(player.getUUID());
        
        if (playerNation == null) {
            source.sendFailure(Component.literal("You must be in a nation to command units"));
            return 0;
        }

        UnitManager unitManager = UnitManager.getInstance();
        List<NationUnit> nearbyUnits = unitManager.getUnitsInRange(
            playerNation.getName(), 
            player.getX(), player.getY(), player.getZ(), 
            32.0 // 32 block command range
        );

        if (nearbyUnits.isEmpty()) {
            source.sendFailure(Component.literal("No units in range to command"));
            return 0;
        }

        // Command each unit to defend its current position
        int commandedUnits = 0;
        
        // Find actual unit entities in the world to get their positions
        for (NationUnit unit : nearbyUnits) {
            // Look for the unit entity in the world within reasonable range
            Entity unitEntity = null;
            List<com.example.dominionrising.forge.entity.UnitEntity> unitEntities = 
                player.level().getEntitiesOfClass(
                    com.example.dominionrising.forge.entity.UnitEntity.class,
                    player.getBoundingBox().inflate(50.0D)
                );
            
            for (com.example.dominionrising.forge.entity.UnitEntity ue : unitEntities) {
                if (ue.getUnitId() != null && ue.getUnitId().equals(unit.getId())) {
                    unitEntity = ue;
                    break;
                }
            }
            
            if (unitEntity != null) {
                Vec3 pos = unitEntity.position();
                if (unitManager.commandDefend(unit.getId(), pos.x, pos.y, pos.z)) {
                    commandedUnits++;
                }
            }
        }

        final int finalCommandedUnits = commandedUnits;
        if (commandedUnits > 0) {
            source.sendSuccess(() -> Component.literal(
                String.format("Commanded %d unit(s) to defend their current positions", 
                             finalCommandedUnits)
            ), false);
            return 1;
        } else {
            source.sendFailure(Component.literal("Failed to command units to defend"));
            return 0;
        }
    }

    private static int showUnitStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }

        NationManager nationManager = NationManager.getInstance();
        Nation playerNation = nationManager.getPlayerNation(player.getUUID());
        
        if (playerNation == null) {
            source.sendFailure(Component.literal("You must be in a nation to view unit status"));
            return 0;
        }

        UnitManager unitManager = UnitManager.getInstance();
        List<NationUnit> units = unitManager.listUnits(playerNation);

        if (units.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Your nation has no units"), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("=== " + playerNation.getName() + " Unit Status ==="), false);
        for (NationUnit unit : units) {
            source.sendSuccess(() -> Component.literal(unit.toString()), false);
        }

        return 1;
    }

    private static int setUnitsIdle(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }

        NationManager nationManager = NationManager.getInstance();
        Nation playerNation = nationManager.getPlayerNation(player.getUUID());
        
        if (playerNation == null) {
            source.sendFailure(Component.literal("You must be in a nation to command units"));
            return 0;
        }

        UnitManager unitManager = UnitManager.getInstance();
        List<NationUnit> nearbyUnits = unitManager.getUnitsInRange(
            playerNation.getName(), 
            player.getX(), player.getY(), player.getZ(), 
            32.0 // 32 block command range
        );

        if (nearbyUnits.isEmpty()) {
            source.sendFailure(Component.literal("No units in range to command"));
            return 0;
        }

        // Set all nearby units to idle
        int commandedUnits = 0;
        for (NationUnit unit : nearbyUnits) {
            unit.setIdle();
            commandedUnits++;
        }

        final int finalCommandedUnits = commandedUnits;
        source.sendSuccess(() -> Component.literal(
            "Set " + finalCommandedUnits + " unit(s) to idle state"
        ), false);
        return 1;
    }

    private static int showUnitHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        String helpText = "§6=== Unit Commands ===\n" +
                         "§e/unit attack <target>§f - Command nearby units to attack target\n" +
                         "§e/unit defend§f - Command nearby units to defend their current position\n" +
                         "§e/unit defend <x> <y> <z>§f - Command nearby units to defend specific position\n" +
                         "§e/unit status§f - Show status of all your nation's units\n" +
                         "§e/unit idle§f - Set nearby units back to idle/follow mode\n" +
                         "§e/unit help§f - Show this help message\n" +
                         "§7Command range: 32 blocks\n" +
                         "§7Units will attack hostile mobs and enemy players when defending";
        
        source.sendSuccess(() -> Component.literal(helpText), false);
        return 1;
    }
}