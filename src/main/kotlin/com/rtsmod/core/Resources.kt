package com.rtsmod.core

/**
 * Container for game resources
 * Maps ResourceType to amount
 */
data class Resources(
    private val resources: MutableMap<ResourceType, Int> = mutableMapOf()
) {
    /**
     * Get the amount of a specific resource
     */
    fun get(resourceType: ResourceType): Int = resources.getOrDefault(resourceType, 0)

    /**
     * Set the amount of a specific resource
     */
    fun set(resourceType: ResourceType, amount: Int) {
        resources[resourceType] = amount.coerceAtLeast(0)
    }

    /**
     * Add amount to a specific resource
     */
    fun add(resourceType: ResourceType, amount: Int): Boolean {
        if (amount <= 0) return false
        resources[resourceType] = get(resourceType) + amount
        return true
    }

    /**
     * Remove amount from a specific resource
     */
    fun remove(resourceType: ResourceType, amount: Int): Boolean {
        if (amount <= 0) return false
        val currentAmount = get(resourceType)
        return if (currentAmount >= amount) {
            resources[resourceType] = currentAmount - amount
            true
        } else {
            false
        }
    }

    /**
     * Check if player can afford the resource cost
     */
    fun canAfford(cost: Resources): Boolean {
        return cost.resources.all { (resourceType, amount) -> 
            get(resourceType) >= amount 
        }
    }

    /**
     * Spend resources if affordable
     */
    fun spend(cost: Resources): Boolean {
        return if (canAfford(cost)) {
            cost.resources.forEach { (resourceType, amount) ->
                remove(resourceType, amount)
            }
            true
        } else {
            false
        }
    }

    /**
     * Get all resources as a map
     */
    fun getAll(): Map<ResourceType, Int> = resources.toMap()

    /**
     * Check if resources are empty (all zero)
     */
    fun isEmpty(): Boolean = resources.values.all { it <= 0 }

    /**
     * Get total amount of all resources
     */
    fun getTotal(): Int = resources.values.sum()

    override fun toString(): String {
        return resources.entries.joinToString(", ") { (type, amount) ->
            "${type.displayName}: $amount"
        }
    }

    companion object {
        /**
         * Create empty resources
         */
        fun empty(): Resources = Resources()

        /**
         * Create resources with initial values
         */
        fun of(vararg pairs: Pair<ResourceType, Int>): Resources {
            val resources = Resources()
            pairs.forEach { (type, amount) -> 
                if (amount > 0) resources.set(type, amount) 
            }
            return resources
        }
    }
}