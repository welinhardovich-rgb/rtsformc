package com.rtsmod.command

import com.rtsmod.RtsMod
import com.rtsmod.game.RtsGameManager
import com.rtsmod.core.ResourceType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.text.Style
import net.minecraft.util.Formatting
import java.util.concurrent.CompletableFuture

/**
 * Commands for testing and managing RTS mod features
 */
object RtsCommands {

    /**
     * Register all RTS commands
     */
    fun register() {
        val commandManager = net.minecraft.server.command.CommandManager.manager
        
        // Main RTS command
        commandManager.register(
            net.minecraft.server.command.CommandManager.literal("rts")
                .then(
                    net.minecraft.server.command.CommandManager.literal("create")
                        .executes { context -> createCity(context) }
                )
                .then(
                    net.minecraft.server.command.CommandManager.literal("resources")
                        .then(
                            net.minecraft.server.command.CommandManager.literal("add")
                                .then(
                                    net.minecraft.server.command.CommandManager.argument("type", StringArgumentType.string())
                                        .then(
                                            net.minecraft.server.command.CommandManager.argument("amount", IntegerArgumentType.integer())
                                                .executes { context -> addResources(context) }
                                        )
                                )
                        )
                        .then(
                            net.minecraft.server.command.CommandManager.literal("show")
                                .executes { context -> showResources(context) }
                        )
                )
                .then(
                    net.minecraft.server.command.CommandManager.literal("status")
                        .executes { context -> showCityStatus(context) }
                )
                .then(
                    net.minecraft.server.command.CommandManager.literal("stats")
                        .requires { it.hasPermissionLevel(2) } // Admin only
                        .executes { context -> showGameStats(context) }
                )
                .then(
                    net.minecraft.server.command.CommandManager.literal("help")
                        .executes { context -> showHelp(context) }
                )
        )
        
        RtsMod.LOGGER.info("RTS Commands registered successfully")
    }

    /**
     * Create a city for the player
     */
    private fun createCity(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source
        val player = source.player
        
        if (player == null) {
            source.sendError(LiteralText("This command can only be used by players"))
            return 0
        }

        val server = source.server
        
        // Check if player already has a city
        val existingCity = RtsGameManager.getCity(player.uuid)
        if (existingCity != null) {
            source.sendError(LiteralText("You already have a city! Use /rts status to see your city info"))
            return 0
        }

        // Create new city
        val city = RtsGameManager.createCity(server, player)
        
        if (city != null) {
            source.sendFeedback(
                LiteralText("✅ Created new city '${city.name}'!")
                    .setStyle(Style.EMPTY.withColor(Formatting.GREEN))
            )
            
            // Show city info
            showCityInfo(source, city)
        } else {
            source.sendError(LiteralText("Failed to create city. Please try again."))
        }
        
        return 1
    }

    /**
     * Add resources to player's city
     */
    private fun addResources(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source
        val player = source.player
        
        if (player == null) {
            source.sendError(LiteralText("This command can only be used by players"))
            return 0
        }

        val resourceTypeStr = StringArgumentType.getString(context, "type")
        val amount = IntegerArgumentType.getInteger(context, "amount")
        
        // Parse resource type
        val resourceType = ResourceType.fromString(resourceTypeStr)
        if (resourceType == null) {
            source.sendError(LiteralText("Invalid resource type: $resourceTypeStr"))
            source.sendFeedback(LiteralText("Valid types: GOLD, WOOD, STONE, FOOD").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)))
            return 0
        }

        val city = RtsGameManager.getCity(player.uuid)
        if (city == null) {
            source.sendError(LiteralText("You don't have a city! Use /rts create to create one"))
            return 0
        }

        // Add resources
        val success = city.addResource(resourceType, amount)
        
        if (success) {
            source.sendFeedback(
                LiteralText("✅ Added $amount ${resourceType.displayName} to your city")
                    .setStyle(Style.EMPTY.withColor(Formatting.GREEN))
            )
            showResources(context)
        } else {
            source.sendError(LiteralText("Failed to add resources"))
        }
        
        return 1
    }

    /**
     * Show player's city status
     */
    private fun showCityStatus(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source
        val player = source.player
        
        if (player == null) {
            source.sendError(LiteralText("This command can only be used by players"))
            return 0
        }

        val city = RtsGameManager.getCity(player.uuid)
        if (city == null) {
            source.sendFeedback(
                LiteralText("🏰 You don't have a city yet!")
                    .setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
            )
            source.sendFeedback(LiteralText("Use /rts create to create your first city").setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
            return 0
        }

        showCityInfo(source, city)
        return 1
    }

    /**
     * Show player's resources
     */
    private fun showResources(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source
        val player = source.player
        
        if (player == null) {
            source.sendError(LiteralText("This command can only be used by players"))
            return 0
        }

        val city = RtsGameManager.getCity(player.uuid)
        if (city == null) {
            source.sendFeedback(
                LiteralText("🏰 You don't have a city yet!")
                    .setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
            )
            return 0
        }

        source.sendFeedback(
            LiteralText("💰 Resources for ${city.name}:")
                .setStyle(Style.EMPTY.withColor(Formatting.GOLD))
        )

        val resources = city.resources.getAll()
        resources.forEach { (type, amount) ->
            val icon = when (type) {
                ResourceType.GOLD -> "💰"
                ResourceType.WOOD -> "🪵"
                ResourceType.STONE -> "🪨"
                ResourceType.FOOD -> "🍖"
            }
            source.sendFeedback(LiteralText("$icon $amount ${type.displayName}"))
        }

        return 1
    }

    /**
     * Show game statistics (admin only)
     */
    private fun showGameStats(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source
        
        val stats = RtsGameManager.getGameStats()
        
        source.sendFeedback(
            LiteralText("📊 RTS Game Statistics")
                .setStyle(Style.EMPTY.withColor(Formatting.AQUA))
        )
        source.sendFeedback(LiteralText("🏰 Total Cities: ${stats.totalCities}"))
        source.sendFeedback(LiteralText("⚡ Active Cities: ${stats.activeCities}"))
        source.sendFeedback(LiteralText("👥 Players: ${stats.playerCount}"))
        source.sendFeedback(LiteralText("🏗️ Buildings: ${stats.totalBuildings}"))
        source.sendFeedback(LiteralText("⚔️ Units: ${stats.totalUnits}"))
        
        return 1
    }

    /**
     * Show help information
     */
    private fun showHelp(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source
        
        source.sendFeedback(
            LiteralText("🎮 RTS Mod Commands")
                .setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE))
        )
        source.sendFeedback(LiteralText("/rts create - Create your first city"))
        source.sendFeedback(LiteralText("/rts resources add <type> <amount> - Add resources (GOLD, WOOD, STONE, FOOD)"))
        source.sendFeedback(LiteralText("/rts resources show - View your current resources"))
        source.sendFeedback(LiteralText("/rts status - View your city information"))
        source.sendFeedback(LiteralText("/rts stats - View game statistics (admin only)"))
        source.sendFeedback(LiteralText("/rts help - Show this help message"))
        
        return 1
    }

    /**
     * Display detailed city information
     */
    private fun showCityInfo(source: ServerCommandSource, city: com.rtsmod.core.City) {
        val stats = city.getCityStats()
        
        source.sendFeedback(
            LiteralText("🏰 City: ${stats.cityName}")
                .setStyle(Style.EMPTY.withColor(Formatting.GOLD))
        )
        source.sendFeedback(LiteralText("📊 Level: ${stats.level} | Population: ${stats.population}/${stats.maxPopulation}"))
        source.sendFeedback(LiteralText("⚡ City Power: ${stats.cityPower}"))
        source.sendFeedback(LiteralText("🏗️ Buildings: ${stats.buildings} | Units: ${stats.units}"))
        source.sendFeedback(LiteralText("👷 Workers: ${stats.workers} | Military: ${stats.militaryUnits}"))
        source.sendFeedback(LiteralText("📍 Location: ${city.location.x}, ${city.location.y}, ${city.location.z}"))
        
        if (city.isUnderAttack()) {
            source.sendFeedback(
                LiteralText("⚠️ City is under attack!")
                    .setStyle(Style.EMPTY.withColor(Formatting.RED))
            )
        }
        
        if (city.getAvailableBuildSlots() > 0) {
            source.sendFeedback(
                LiteralText("🏗️ Available Build Slots: ${stats.availableBuildSlots}")
                    .setStyle(Style.EMPTY.withColor(Formatting.GREEN))
            )
        } else {
            source.sendFeedback(
                LiteralText("🚫 No more build slots available - upgrade your city!")
                    .setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
            )
        }
    }
}