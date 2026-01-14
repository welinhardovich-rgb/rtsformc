package com.rtsmod.core

import net.minecraft.util.math.BlockPos
import java.util.*

/**
 * Represents a unit in the RTS game
 */
data class Unit(
    val id: String = UUID.randomUUID().toString(),
    val type: UnitType,
    val owner: UUID,
    var position: BlockPos,
    var health: Int,
    val maxHealth: Int,
    var target: BlockPos? = null,
    val orders: MutableList<Order> = mutableListOf(),
    var state: UnitState = UnitState.IDLE
) {
    /**
     * Get unit display name
     */
    fun getDisplayName(): String = type.displayName

    /**
     * Get unit health as percentage (0-100)
     */
    fun getHealthPercentage(): Float = if (maxHealth > 0) (health.toFloat() / maxHealth.toFloat()) * 100f else 0f

    /**
     * Check if unit is dead
     */
    fun isDead(): Boolean = health <= 0

    /**
     * Damage the unit
     */
    fun damage(amount: Int) {
        health = (health - amount).coerceAtLeast(0)
        if (isDead()) {
            state = UnitState.DEAD
        }
    }

    /**
     * Heal the unit
     */
    fun heal(amount: Int) {
        health = (health + amount).coerceAtLeast(0)
        health = health.coerceAtMost(maxHealth)
        if (health > 0 && state == UnitState.DEAD) {
            state = UnitState.IDLE
        }
    }

    /**
     * Check if unit can gather resources
     */
    fun canGatherResources(): Boolean = type == UnitType.WORKER

    /**
     * Check if unit can attack
     */
    fun canAttack(): Boolean = when (type) {
        UnitType.SOLDIER, UnitType.RANGED_UNIT -> true
        else -> false
    }

    /**
     * Check if unit can build
     */
    fun canBuild(): Boolean = type == UnitType.WORKER

    /**
     * Add a new order to the unit
     */
    fun addOrder(order: Order) {
        orders.add(order)
    }

    /**
     * Clear all orders
     */
    fun clearOrders() {
        orders.clear()
    }

    /**
     * Get current order (first in queue)
     */
    fun getCurrentOrder(): Order? = orders.firstOrNull()

    /**
     * Complete current order
     */
    fun completeCurrentOrder() {
        if (orders.isNotEmpty()) {
            orders.removeFirst()
        }
    }

    /**
     * Move unit to new position
     */
    fun moveTo(position: BlockPos) {
        this.position = position
        this.state = UnitState.MOVING
    }

    /**
     * Set unit to attack mode
     */
    fun attack(target: BlockPos) {
        this.target = target
        this.state = UnitState.ATTACKING
    }

    /**
     * Set unit to gather mode
     */
    fun gather(resource: BlockPos) {
        this.target = resource
        this.state = UnitState.GATHERING
    }

    /**
     * Set unit to build mode
     */
    fun build(building: BlockPos) {
        this.target = building
        this.state = UnitState.BUILDING
    }

    /**
     * Types of units in the RTS game
     */
    enum class UnitType(
        val displayName: String,
        val maxHealth: Int,
        val buildTime: Int,
        val cost: Resources,
        val description: String
    ) {
        WORKER(
            "Worker",
            50,
            50,
            Resources.of(ResourceType.FOOD to 50),
            "Can gather resources and construct buildings"
        ),
        SOLDIER(
            "Soldier",
            100,
            100,
            Resources.of(ResourceType.FOOD to 75, ResourceType.STONE to 25),
            "Basic combat unit for ground warfare"
        ),
        RANGED_UNIT(
            "Ranged Unit",
            80,
            120,
            Resources.of(ResourceType.FOOD to 100, ResourceType.STONE to 50),
            "Long-range combat unit"
        );

        companion object {
            fun fromString(name: String): UnitType? {
                return values().find { it.name.equals(name, ignoreCase = true) }
            }
        }
    }

    /**
     * Current state of the unit
     */
    enum class UnitState {
        IDLE,
        MOVING,
        ATTACKING,
        GATHERING,
        BUILDING,
        CONSTRUCTING,
        DEAD
    }

    /**
     * Represents an order given to a unit
     */
    data class Order(
        val type: OrderType,
        val target: BlockPos? = null,
        val buildingType: Building.BuildingType? = null,
        val priority: Int = 0
    )

    /**
     * Types of orders units can receive
     */
    enum class OrderType {
        MOVE,
        ATTACK,
        GATHER,
        BUILD,
        STOP
    }
}