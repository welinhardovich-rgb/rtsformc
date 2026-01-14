package com.rtsmod;

import com.rtsmod.block.ModBlocks;
import com.rtsmod.command.RtsCommands;
import com.rtsmod.game.RtsGameManager;
import com.rtsmod.game.ServerEventHandler;
import com.rtsmod.network.RtsPacketHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the RTS mod.
 */
public class RtsMod implements ModInitializer {
    public static final String MOD_ID = "rtsmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing RTS Mod...");

        // Register core components
        ModBlocks.registerBlocks();
        RtsCommands.register();
        RtsPacketHandler.registerPackets();
        ServerEventHandler.registerEvents();

        // Server lifecycle hooks
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            RtsGameManager.getInstance().onServerStarting(server);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            RtsGameManager.getInstance().onServerStopping(server);
        });

        LOGGER.info("RTS Mod initialized successfully!");
    }
}
