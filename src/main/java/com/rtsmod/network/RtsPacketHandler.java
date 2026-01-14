package com.rtsmod.network;

import com.rtsmod.network.payload.CityDataSyncPacket;
import com.rtsmod.network.payload.UnitUpdatePacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Handles registration of network packets for the RTS mod.
 */
public class RtsPacketHandler {
    public static void registerPackets() {
        // Register payloads for server-to-client communication
        PayloadTypeRegistry.playS2C().register(CityDataSyncPacket.ID, CityDataSyncPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(UnitUpdatePacket.ID, UnitUpdatePacket.CODEC);
    }
}
