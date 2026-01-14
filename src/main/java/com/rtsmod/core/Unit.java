package com.rtsmod.core;

import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a game unit in the RTS mod.
 */
public class Unit {
    private UUID id;
    private UUID owner;
    private UnitType type;
    private BlockPos position;
    private int health;
    private int maxHealth;
    private UUID target; // Using UUID for target entity/unit
    private List<String> orders = new ArrayList<>();

    public Unit(UUID id, UUID owner, UnitType type, BlockPos position, int maxHealth) {
        this.id = id;
        this.owner = owner;
        this.type = type;
        this.position = position;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }

    public UnitType getType() { return type; }
    public void setType(UnitType type) { this.type = type; }

    public BlockPos getPosition() { return position; }
    public void setPosition(BlockPos position) { this.position = position; }

    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }

    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }

    public UUID getTarget() { return target; }
    public void setTarget(UUID target) { this.target = target; }

    public List<String> getOrders() { return orders; }
    public void setOrders(List<String> orders) { this.orders = orders; }
}
