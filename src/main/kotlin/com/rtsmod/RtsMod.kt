package com.rtsmod

import com.rtsmod.block.ModBlocks
import com.rtsmod.command.RtsCommands
import com.rtsmod.game.RtsGameManager
import com.rtsmod.network.RtsPacketHandler
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Main entry point for the RTS Mod
 * Initializes all mod components and sets up event listeners
 */
object RtsMod : ModInitializer {
    const val MOD_ID = "rtsmod"
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        LOGGER.info("Initializing RTS Mod...")

        // Register core components
        ModBlocks.register()
        RtsPacketHandler.register()
        RtsCommands.register()

        // Initialize game manager
        RtsGameManager.initialize()

        // Set up server events
        setupServerEvents()

        LOGGER.info("RTS Mod initialized successfully!")
    }

    /**
     * Sets up server lifecycle and tick events
     */
    private fun setupServerEvents() {
        // Server starting
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            LOGGER.info("RTS Mod: Server started, loading game data...")
            RtsGameManager.onServerStarted(server)
        }

        // Server stopping
        ServerLifecycleEvents.SERVER_STOPPING.register { server ->
            LOGGER.info("RTS Mod: Server stopping, saving game data...")
            RtsGameManager.onServerStopping(server)
        }

        // Server ticks for game logic
        ServerTickEvents.END_SERVER_TICK.register { server ->
            RtsGameManager.onServerTick(server)
        }
    }
}