package com.rtsmod.core

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import java.util.*

/**
 * Represents a building in the RTS game
 */
data class Building(
    val id: String = UUID.randomUUID().toString(),
    val type: BuildingType,
    val owner: UUID,
    val position: BlockPos,
    var health: Int,
    val maxHealth: Int,
    val buildTime: Int = 0,
    val constructionCost: Resources = Resources.empty(),
    var isUnderConstruction: Boolean = false,
    var constructionProgress: Int = 0
) {
    /**
     * Get building display name
     */
    fun getDisplayName(): String = type.displayName

    /**
     * Get building health as percentage (0-100)
     */
    fun getHealthPercentage(): Float = if (maxHealth > 0) (health.toFloat() / maxHealth.toFloat()) * 100f else 0f

    /**
     * Check if building is destroyed
     */
    fun isDestroyed(): Boolean = health <= 0

    /**
     * Damage the building
     */
    fun damage(amount: Int) {
        health = (health - amount).coerceAtLeast(0)
    }

    /**
     * Repair the building
     */
    fun repair(amount: Int) {
        health = (health + amount).coerceAtLeast(0)
        health = health.coerceAtMost(maxHealth)
    }

    /**
     * Check if building can produce units
     */
    fun canProduceUnits(): Boolean = when (type) {
        BuildingType.TOWN_CENTER, BuildingType.BARRACKS -> true
        else -> false
    }

    /**
     * Check if building can gather resources
     */
    fun canGatherResources(): Boolean = type == BuildingType.RESOURCE_BUILDING

    /**
     * Check if building can defend
     */
    fun canDefend(): Boolean = when (type) {
        BuildingType.TOWER, BuildingType.BARRACKS -> true
        else -> false
    }

    /**
     * Update construction progress
     */
    fun updateConstruction(delta: Int): Boolean {
        if (!isUnderConstruction) return false
        
        constructionProgress = (constructionProgress + delta).coerceAtLeast(0)
        
        if (constructionProgress >= buildTime) {
            isUnderConstruction = false
            constructionProgress = buildTime
            return true // Construction completed
        }
        return false
    }

    /**
     * Get construction progress as percentage
     */
    fun getConstructionPercentage(): Float = if (buildTime > 0) {
        (constructionProgress.toFloat() / buildTime.toFloat()) * 100f
    } else {
        100f
    }

    /**
     * Types of buildings in the RTS game
     */
    enum class BuildingType(
        val displayName: String,
        val maxHealth: Int,
        val buildTime: Int,
        val cost: Resources,
        val description: String
    ) {
        TOWN_CENTER(
            "Town Center",
            1000,
            0,
            Resources.of(),
            "Main building that serves as the city center and can train workers"
        ),
        BARRACKS(
            "Barracks", 
            500,
            300,
            Resources.of(ResourceType.WOOD to 100, ResourceType.STONE to 50),
            "Building for training military units"
        ),
        RESOURCE_BUILDING(
            "Resource Building",
            300,
            200,
            Resources.of(ResourceType.WOOD to 75),
            "Building for resource gathering operations"
        ),
        TOWER(
            "Tower",
            400,
            250,
            Resources.of(ResourceType.STONE to 100, ResourceType.WOOD to 50),
            "Defensive structure for city protection"
        ),
        STORAGE(
            "Storage",
            200,
            150,
            Resources.of(ResourceType.WOOD to 80),
            "Increases resource storage capacity"
        );

        companion object {
            fun fromString(name: String): BuildingType? {
                return values().find { it.name.equals(name, ignoreCase = true) }
            }
        }
    }
}