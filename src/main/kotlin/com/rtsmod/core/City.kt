package com.rtsmod.core

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import java.util.*

/**
 * Represents a player's city/base in the RTS game
 * Contains all buildings, units, and resources for a player
 */
data class City(
    val id: String = UUID.randomUUID().toString(),
    val owner: UUID,
    var name: String = "New City",
    var location: BlockPos,
    var resources: Resources = Resources.empty(),
    private val buildings: MutableList<Building> = mutableListOf(),
    private val units: MutableList<Unit> = mutableListOf(),
    var population: Int = 0,
    val maxPopulation: Int = 100,
    var cityLevel: Int = 1,
    var isActive: Boolean = true,
    val creationTime: Long = System.currentTimeMillis()
) {
    
    /**
     * Get all buildings in the city
     */
    fun getBuildings(): List<Building> = buildings.toList()

    /**
     * Get all units in the city
     */
    fun getUnits(): List<Unit> = units.toList()

    /**
     * Add a building to the city
     */
    fun addBuilding(building: Building): Boolean {
        return if (building.owner == owner) {
            buildings.add(building)
            true
        } else {
            false
        }
    }

    /**
     * Remove a building from the city
     */
    fun removeBuilding(building: Building): Boolean {
        return buildings.remove(building)
    }

    /**
     * Get buildings of specific type
     */
    fun getBuildingsByType(type: Building.BuildingType): List<Building> {
        return buildings.filter { it.type == type }
    }

    /**
     * Get the main town center (should be unique per city)
     */
    fun getTownCenter(): Building? {
        return buildings.find { it.type == Building.BuildingType.TOWN_CENTER }
    }

    /**
     * Check if city has a town center
     */
    fun hasTownCenter(): Boolean = getTownCenter() != null

    /**
     * Add a unit to the city
     */
    fun addUnit(unit: Unit): Boolean {
        return if (unit.owner == owner && population < maxPopulation) {
            units.add(unit)
            population++
            true
        } else {
            false
        }
    }

    /**
     * Remove a unit from the city
     */
    fun removeUnit(unit: Unit): Boolean {
        return if (units.remove(unit)) {
            population = (population - 1).coerceAtLeast(0)
            true
        } else {
            false
        }
    }

    /**
     * Get units of specific type
     */
    fun getUnitsByType(type: Unit.UnitType): List<Unit> {
        return units.filter { it.type == type }
    }

    /**
     * Get all workers in the city
     */
    fun getWorkers(): List<Unit> = getUnitsByType(Unit.UnitType.WORKER)

    /**
     * Get all military units
     */
    fun getMilitaryUnits(): List<Unit> {
        return units.filter { it.type == Unit.UnitType.SOLDIER || it.type == Unit.UnitType.RANGED_UNIT }
    }

    /**
     * Get city resources
     */
    fun getResource(type: ResourceType): Int = resources.get(type)

    /**
     * Add resources to city
     */
    fun addResource(type: ResourceType, amount: Int): Boolean = resources.add(type, amount)

    /**
     * Remove resources from city
     */
    fun removeResource(type: ResourceType, amount: Int): Boolean = resources.remove(type, amount)

    /**
     * Check if city can afford a resource cost
     */
    fun canAfford(cost: Resources): Boolean = resources.canAfford(cost)

    /**
     * Spend resources from city
     */
    fun spend(cost: Resources): Boolean = resources.spend(cost)

    /**
     * Calculate city power (based on buildings and units)
     */
    fun getCityPower(): Int {
        val buildingPower = buildings.sumOf { 
            when (it.type) {
                Building.BuildingType.TOWN_CENTER -> 100
                Building.BuildingType.BARRACKS -> 50
                Building.BuildingType.TOWER -> 75
                Building.BuildingType.RESOURCE_BUILDING -> 25
                Building.BuildingType.STORAGE -> 10
            }
        }
        
        val unitPower = units.sumOf { 
            when (it.type) {
                Unit.UnitType.WORKER -> 5
                Unit.UnitType.SOLDIER -> 20
                Unit.UnitType.RANGED_UNIT -> 30
            }
        }
        
        return buildingPower + unitPower
    }

    /**
     * Get available build slots (capacity for more buildings)
     */
    fun getAvailableBuildSlots(): Int {
        val baseSlots = when (cityLevel) {
            1 -> 10
            2 -> 15
            3 -> 25
            else -> 25 + ((cityLevel - 3) * 5)
        }
        
        return baseSlots - buildings.size
    }

    /**
     * Check if city can build more buildings
     */
    fun canBuildMore(): Boolean = getAvailableBuildSlots() > 0

    /**
     * Upgrade city level (when conditions are met)
     */
    fun upgradeCity(): Boolean {
        if (cityLevel >= 5) return false // Max level
        
        val requiredPower = cityLevel * 1000
        val requiredBuildings = cityLevel * 3
        
        return if (getCityPower() >= requiredPower && buildings.size >= requiredBuildings) {
            cityLevel++
            maxPopulation += 20
            true
        } else {
            false
        }
    }

    /**
     * Get city statistics
     */
    fun getCityStats(): CityStats {
        return CityStats(
            cityName = name,
            level = cityLevel,
            population = population,
            maxPopulation = maxPopulation,
            buildings = buildings.size,
            units = units.size,
            workers = getWorkers().size,
            militaryUnits = getMilitaryUnits().size,
            cityPower = getCityPower(),
            resources = resources.getAll(),
            availableBuildSlots = getAvailableBuildSlots()
        )
    }

    /**
     * Check if city is under attack (low health buildings/units)
     */
    fun isUnderAttack(): Boolean {
        return buildings.any { it.health < it.maxHealth * 0.3 } ||
               units.any { it.health < it.maxHealth * 0.5 }
    }

    /**
     * Get defensive readiness
     */
    fun getDefensiveReadiness(): Float {
        val defensiveBuildings = buildings.filter { it.canDefend() }
        val militaryUnits = getMilitaryUnits()
        
        val buildingDefense = defensiveBuildings.size * 20f
        val unitDefense = militaryUnits.size * 15f
        
        return if (buildingDefense + unitDefense > 0) {
            (buildingDefense + unitDefense) / 100f
        } else {
            0f
        }
    }

    /**
     * Update city state (called each tick)
     */
    fun update(deltaTime: Long) {
        // Update construction progress for buildings under construction
        buildings.filter { it.isUnderConstruction }.forEach { building ->
            building.updateConstruction((deltaTime / 50).toInt()) // Convert ms to ticks
        }
        
        // Update unit states and orders
        units.forEach { unit ->
            if (unit.state == Unit.UnitState.CONSTRUCTING) {
                // Process construction orders
                unit.completeCurrentOrder()
            }
        }
    }

    /**
     * Data class for city statistics display
     */
    data class CityStats(
        val cityName: String,
        val level: Int,
        val population: Int,
        val maxPopulation: Int,
        val buildings: Int,
        val units: Int,
        val workers: Int,
        val militaryUnits: Int,
        val cityPower: Int,
        val resources: Map<ResourceType, Int>,
        val availableBuildSlots: Int
    )
}