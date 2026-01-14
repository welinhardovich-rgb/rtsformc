package com.rtsmod.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * Block entity data storage for the Obelisk block.
 */
public class ObeliskBlockEntity extends BlockEntity {
    private UUID cityId = UUID.randomUUID();
    private String ownerName = "Unknown";

    public ObeliskBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.OBELISK_ENTITY_TYPE, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putUuid("CityId", cityId);
        nbt.putString("OwnerName", ownerName);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        if (nbt.containsUuid("CityId")) {
            cityId = nbt.getUuid("CityId");
        }
        if (nbt.contains("OwnerName")) {
            ownerName = nbt.getString("OwnerName");
        }
    }

    public UUID getCityId() { return cityId; }
    public void setCityId(UUID cityId) { this.cityId = cityId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
}
