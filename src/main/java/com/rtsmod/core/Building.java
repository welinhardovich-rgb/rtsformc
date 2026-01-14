package com.rtsmod.core;

import net.minecraft.util.math.BlockPos;
import java.util.UUID;

/**
 * Represents a constructed building in the RTS mod.
 */
public class Building {
    private BuildingType type;
    private UUID owner;
    private BlockPos position;
    private int health;
    private int maxHealth;
    private int buildTime;
    private Resources cost;

    public Building(BuildingType type, UUID owner, BlockPos position, int maxHealth, int buildTime, Resources cost) {
        this.type = type;
        this.owner = owner;
        this.position = position;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.buildTime = buildTime;
        this.cost = cost;
    }

    // Getters and setters
    public BuildingType getType() { return type; }
    public void setType(BuildingType type) { this.type = type; }

    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }

    public BlockPos getPosition() { return position; }
    public void setPosition(BlockPos position) { this.position = position; }

    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }

    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }

    public int getBuildTime() { return buildTime; }
    public void setBuildTime(int buildTime) { this.buildTime = buildTime; }

    public Resources getCost() { return cost; }
    public void setCost(Resources cost) { this.cost = cost; }
}
