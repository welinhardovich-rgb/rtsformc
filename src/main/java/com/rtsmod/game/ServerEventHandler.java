package com.rtsmod.game;

import com.rtsmod.core.City;
import com.rtsmod.network.payload.CityDataSyncPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Listens to Fabric server events and triggers RTS game logic.
 */
public class ServerEventHandler {
    public static void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            RtsGameManager.getInstance().tick(server);
            
            // Periodically sync data to players
            if (server.getTicks() % 100 == 0) { // Every 5 seconds
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    City city = RtsGameManager.getInstance().getCity(player.getUuid());
                    if (city != null) {
                        ServerPlayNetworking.send(player, new CityDataSyncPacket(
                            city.getOwner(),
                            city.getLocation(),
                            city.getResources().getAmounts()
                        ));
                    }
                }
            }
        });
    }
}
