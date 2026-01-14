package com.rtsmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.rtsmod.core.City;
import com.rtsmod.core.ResourceType;
import com.rtsmod.game.RtsGameManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Registers and handles all RTS-related commands.
 */
public class RtsCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("rts")
                .then(CommandManager.literal("create")
                    .executes(context -> createCity(context.getSource())))
                .then(CommandManager.literal("resources")
                    .then(CommandManager.literal("add")
                        .then(CommandManager.argument("type", EnumArgumentType.enumArgument(ResourceType.class))
                            .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                .executes(context -> addResources(
                                    context.getSource(),
                                    EnumArgumentType.getEnumArgument(context, "type", ResourceType.class),
                                    IntegerArgumentType.getInteger(context, "amount")
                                )))))
                    .then(CommandManager.literal("get")
                        .executes(context -> getResources(context.getSource()))))
                .then(CommandManager.literal("status")
                    .executes(context -> getStatus(context.getSource())))
            );
        });
    }

    private static int createCity(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        City city = RtsGameManager.getInstance().createCity(player.getUuid(), player.getBlockPos());
        source.sendFeedback(() -> Text.literal("City created at " + player.getBlockPos().toShortString()), false);
        return 1;
    }

    private static int addResources(ServerCommandSource source, ResourceType type, int amount) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        City city = RtsGameManager.getInstance().getCity(player.getUuid());
        if (city == null) {
            source.sendError(Text.literal("You don't have a city! Use /rts create first."));
            return 0;
        }

        city.addResource(type, amount);
        source.sendFeedback(() -> Text.literal("Added " + amount + " " + type.name() + " to your city."), false);
        return 1;
    }

    private static int getResources(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        City city = RtsGameManager.getInstance().getCity(player.getUuid());
        if (city == null) {
            source.sendError(Text.literal("You don't have a city!"));
            return 0;
        }

        StringBuilder sb = new StringBuilder("Resources: ");
        for (ResourceType type : ResourceType.values()) {
            sb.append(type.name()).append(": ").append(city.getResource(type)).append(", ");
        }
        source.sendFeedback(() -> Text.literal(sb.toString()), false);
        return 1;
    }

    private static int getStatus(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        City city = RtsGameManager.getInstance().getCity(player.getUuid());
        if (city == null) {
            source.sendError(Text.literal("You don't have a city!"));
            return 0;
        }

        source.sendFeedback(() -> Text.literal("City Status:"), false);
        source.sendFeedback(() -> Text.literal("- Location: " + city.getLocation().toShortString()), false);
        source.sendFeedback(() -> Text.literal("- Buildings: " + city.getBuildings().size()), false);
        source.sendFeedback(() -> Text.literal("- Units: " + city.getUnits().size()), false);
        
        return getResources(source);
    }
}
