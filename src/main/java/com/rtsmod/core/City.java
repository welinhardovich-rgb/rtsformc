package com.rtsmod.core;

import net.minecraft.util.math.BlockPos;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a player's city/base in the RTS mod.
 */
public class City {
    private UUID owner;
    private BlockPos location;
    private Resources resources;
    private Map<BlockPos, Building> buildings;
    private Map<UUID, Unit> units;

    public City(UUID owner, BlockPos location) {
        this.owner = owner;
        this.location = location;
        this.resources = new Resources();
        this.buildings = new HashMap<>();
        this.units = new HashMap<>();
    }

    public void addBuilding(Building building) {
        buildings.put(building.getPosition(), building);
    }

    public void removeBuilding(BlockPos pos) {
        buildings.remove(pos);
    }

    public int getResource(ResourceType type) {
        return resources.get(type);
    }

    public void addResource(ResourceType type, int amount) {
        resources.add(type, amount);
    }

    // Getters
    public UUID getOwner() { return owner; }
    public BlockPos getLocation() { return location; }
    public Resources getResources() { return resources; }
    public Map<BlockPos, Building> getBuildings() { return buildings; }
    public Map<UUID, Unit> getUnits() { return units; }
    
    public void setLocation(BlockPos location) { this.location = location; }
}
