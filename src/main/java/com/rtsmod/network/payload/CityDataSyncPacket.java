package com.rtsmod.network.payload;

import com.rtsmod.core.ResourceType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Payload for syncing city data to the client.
 */
public record CityDataSyncPacket(UUID owner, BlockPos location, Map<ResourceType, Integer> resources) implements CustomPayload {
    public static final Id<CityDataSyncPacket> ID = new Id<>(Identifier.of("rtsmod", "city_sync"));
    
    public static final PacketCodec<RegistryByteBuf, CityDataSyncPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.UUID, CityDataSyncPacket::owner,
        BlockPos.PACKET_CODEC, CityDataSyncPacket::location,
        PacketCodecs.map(EnumMap::new, PacketCodecs.enumValue(ResourceType.class), PacketCodecs.VAR_INT), CityDataSyncPacket::resources,
        CityDataSyncPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
