package com.rtsmod.network.payload;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;

/**
 * Payload for updating unit positions and states on the client.
 */
public record UnitUpdatePacket(Map<UUID, BlockPos> unitPositions) implements CustomPayload {
    public static final Id<UnitUpdatePacket> ID = new Id<>(Identifier.of("rtsmod", "unit_update"));

    public static final PacketCodec<RegistryByteBuf, UnitUpdatePacket> CODEC = PacketCodec.tuple(
        PacketCodecs.map(java.util.HashMap::new, PacketCodecs.UUID, BlockPos.PACKET_CODEC), UnitUpdatePacket::unitPositions,
        UnitUpdatePacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
