package com.rtsmod.game

import com.rtsmod.RtsMod
import com.rtsmod.core.City
import com.rtsmod.core.Unit
import com.rtsmod.core.Building
import com.rtsmod.core.Resources
import com.rtsmod.core.ResourceType
import com.rtsmod.storage.YamlDataManager
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import java.util.concurrent.ConcurrentHashMap
import java.util.*

/**
 * Main game logic coordinator for the RTS mod
 * Manages all cities, units, and game state
 * Runs on server side only for authoritative gameplay
 */
object RtsGameManager {
    
    // All cities in the game, keyed by player UUID
    private val cities = ConcurrentHashMap<UUID, City>()
    
    // All units in the game, keyed by unit ID
    private val units = ConcurrentHashMap<String, Unit>()
    
    // All buildings in the game, keyed by building ID
    private val buildings = ConcurrentHashMap<String, Building>()
    
    // Game configuration
    private var isInitialized = false
    
    /**
     * Initialize the game manager
     */
    fun initialize() {
        if (isInitialized) {
            RtsMod.LOGGER.warn("RtsGameManager already initialized!")
            return
        }
        
        RtsMod.LOGGER.info("Initializing RTS Game Manager...")
        isInitialized = true
        RtsMod.LOGGER.info("RTS Game Manager initialized successfully")
    }

    /**
     * Called when the server starts
     */
    fun onServerStarted(server: MinecraftServer) {
        RtsMod.LOGGER.info("Loading RTS game data from storage...")
        
        // Load all cities from storage
        val loadedCities = YamlDataManager.loadAllCities(server)
        loadedCities.forEach { (playerId, city) ->
            cities[playerId] = city
            RtsMod.LOGGER.info("Loaded city for player: ${city.name}")
        }
        
        RtsMod.LOGGER.info("Loaded ${cities.size} cities from storage")
        
        // Start any background tasks
        startBackgroundTasks()
    }

    /**
     * Called when the server is stopping
     */
    fun onServerStopping(server: MinecraftServer) {
        RtsMod.LOGGER.info("Saving RTS game data to storage...")
        
        // Save all cities to storage
        var savedCount = 0
        cities.values.forEach { city ->
            if (YamlDataManager.saveCityData(server, city)) {
                savedCount++
            }
        }
        
        RtsMod.LOGGER.info("Saved $savedCount cities to storage")
        
        // Stop background tasks
        stopBackgroundTasks()
    }

    /**
     * Called every server tick (20 times per second)
     */
    fun onServerTick(server: MinecraftServer) {
        if (!isInitialized) return
        
        val deltaTime = 50L // 50ms per tick
        
        try {
            // Update all cities
            updateCities(deltaTime)
            
            // Update all units
            updateUnits(deltaTime)
            
            // Update all buildings
            updateBuildings(deltaTime)
            
            // Process resource generation
            processResourceGeneration(deltaTime)
            
            // Process construction
            processConstruction(deltaTime)
            
            // Save data periodically (every 5 minutes)
            if (System.currentTimeMillis() % 300000L < 20) { // Check every ~5 minutes
                saveAllCities(server)
            }
            
        } catch (e: Exception) {
            RtsMod.LOGGER.error("Error during RTS game tick", e)
        }
    }

    /**
     * Create a new city for a player
     */
    fun createCity(server: MinecraftServer, player: ServerPlayerEntity, cityName: String = "New City"): City? {
        val playerId = player.uuid
        
        // Check if player already has a city
        if (cities.containsKey(playerId)) {
            RtsMod.LOGGER.warn("Player ${player.displayName} already has a city!")
            return null
        }
        
        // Create new city at player's current position
        val cityLocation = BlockPos.ofFloored(player.pos.x, player.pos.y, player.pos.z)
        
        val city = City(
            owner = playerId,
            name = cityName,
            location = cityLocation,
            resources = Resources.of(ResourceType.FOOD to 200, ResourceType.WOOD to 150, ResourceType.STONE to 100, ResourceType.GOLD to 100)
        )
        
        // Create town center
        val townCenter = Building(
            type = Building.BuildingType.TOWN_CENTER,
            owner = playerId,
            position = cityLocation,
            health = Building.BuildingType.TOWN_CENTER.maxHealth,
            maxHealth = Building.BuildingType.TOWN_CENTER.maxHealth
        )
        
        city.addBuilding(townCenter)
        buildings[townCenter.id] = townCenter
        
        // Create initial worker
        val worker = Unit(
            type = Unit.UnitType.WORKER,
            owner = playerId,
            position = cityLocation,
            health = Unit.UnitType.WORKER.maxHealth,
            maxHealth = Unit.UnitType.WORKER.maxHealth
        )
        
        city.addUnit(worker)
        units[worker.id] = worker
        
        // Save city
        cities[playerId] = city
        YamlDataManager.saveCityData(server, city)
        
        RtsMod.LOGGER.info("Created new city '${cityName}' for player ${player.displayName}")
        return city
    }

    /**
     * Get city for a player
     */
    fun getCity(playerId: UUID): City? = cities[playerId]

    /**
     * Get city for a player by name
     */
    fun getCityByName(playerName: String): City? {
        return cities.values.find { it.name.equals(playerName, ignoreCase = true) }
    }

    /**
     * Get all cities
     */
    fun getAllCities(): List<City> = cities.values.toList()

    /**
     * Get unit by ID
     */
    fun getUnit(unitId: String): Unit? = units[unitId]

    /**
     * Get building by ID
     */
    fun getBuilding(buildingId: String): Building? = buildings[buildingId]

    /**
     * Remove city (delete)
     */
    fun removeCity(server: MinecraftServer, playerId: UUID): Boolean {
        val city = cities.remove(playerId) ?: return false
        
        // Remove associated units and buildings
        city.getUnits().forEach { units.remove(it.id) }
        city.getBuildings().forEach { buildings.remove(it.id) }
        
        // Delete from storage
        YamlDataManager.deleteCityData(server, playerId)
        
        RtsMod.LOGGER.info("Removed city for player $playerId")
        return true
    }

    /**
     * Update all cities
     */
    private fun updateCities(deltaTime: Long) {
        cities.values.forEach { city ->
            city.update(deltaTime)
            
            // Check for city upgrades
            if (city.cityLevel < 5 && city.upgradeCity()) {
                RtsMod.LOGGER.info("City ${city.name} upgraded to level ${city.cityLevel}")
            }
            
            // Check if city should be inactive (destroyed)
            val townCenter = city.getTownCenter()
            if (townCenter != null && townCenter.isDestroyed()) {
                city.isActive = false
                RtsMod.LOGGER.warn("City ${city.name} has been destroyed!")
            }
        }
    }

    /**
     * Update all units
     */
    private fun updateUnits(deltaTime: Long) {
        units.values.forEach { unit ->
            if (unit.isDead()) return@forEach
            
            // Process unit orders
            val currentOrder = unit.getCurrentOrder()
            if (currentOrder != null) {
                processUnitOrder(unit, currentOrder, deltaTime)
            }
            
            // Update unit state based on environment
            updateUnitState(unit, deltaTime)
        }
    }

    /**
     * Update all buildings
     */
    private fun updateBuildings(deltaTime: Long) {
        buildings.values.forEach { building ->
            if (building.isDestroyed()) return@forEach
            
            // Update construction progress
            if (building.isUnderConstruction) {
                val completed = building.updateConstruction((deltaTime / 50).toInt())
                if (completed) {
                    RtsMod.LOGGER.info("Building ${building.getDisplayName()} construction completed!")
                }
            }
            
            // Check if building should be destroyed
            if (building.health <= 0) {
                RtsMod.LOGGER.info("Building ${building.getDisplayName()} has been destroyed!")
            }
        }
    }

    /**
     * Process unit orders
     */
    private fun processUnitOrder(unit: Unit, order: Unit.Order, deltaTime: Long) {
        when (order.type) {
            Unit.OrderType.MOVE -> {
                // TODO: Implement pathfinding and movement
                unit.completeCurrentOrder()
            }
            Unit.OrderType.ATTACK -> {
                // TODO: Implement combat logic
                unit.completeCurrentOrder()
            }
            Unit.OrderType.GATHER -> {
                if (unit.canGatherResources()) {
                    // TODO: Implement resource gathering
                    unit.completeCurrentOrder()
                }
            }
            Unit.OrderType.BUILD -> {
                if (unit.canBuild()) {
                    // TODO: Implement building construction
                    unit.completeCurrentOrder()
                }
            }
            Unit.OrderType.STOP -> {
                unit.clearOrders()
                unit.state = Unit.UnitState.IDLE
            }
        }
    }

    /**
     * Update unit state
     */
    private fun updateUnitState(unit: Unit, deltaTime: Long) {
        // Simple AI for units
        when (unit.state) {
            Unit.UnitState.IDLE -> {
                // Workers should gather resources if city needs them
                if (unit.type == Unit.UnitType.WORKER) {
                    val city = cities[unit.owner]
                    if (city != null && (city.getResource(ResourceType.WOOD) < 50 || city.getResource(ResourceType.STONE) < 50)) {
                        // Send worker to gather
                        unit.state = Unit.UnitState.GATHERING
                    }
                }
            }
            else -> { /* Other states handled in order processing */ }
        }
    }

    /**
     * Process resource generation
     */
    private fun processResourceGeneration(deltaTime: Long) {
        // Resource buildings generate resources
        buildings.values.forEach { building ->
            if (building.canGatherResources() && !building.isDestroyed()) {
                val city = cities[building.owner]
                if (city != null) {
                    // Generate resources based on building type
                    when (building.type) {
                        Building.BuildingType.RESOURCE_BUILDING -> {
                            city.addResource(ResourceType.WOOD, 1)
                            city.addResource(ResourceType.STONE, 1)
                        }
                        else -> { /* No resource generation */ }
                    }
                }
            }
        }
    }

    /**
     * Process construction queues
     */
    private fun processConstruction(deltaTime: Long) {
        // Buildings under construction are updated in updateBuildings()
        // Units under construction are handled in unit processing
    }

    /**
     * Start background tasks
     */
    private fun startBackgroundTasks() {
        // TODO: Start periodic maintenance tasks
        RtsMod.LOGGER.info("Started RTS background tasks")
    }

    /**
     * Stop background tasks
     */
    private fun stopBackgroundTasks() {
        // TODO: Stop background tasks
        RtsMod.LOGGER.info("Stopped RTS background tasks")
    }

    /**
     * Save all cities to storage
     */
    private fun saveAllCities(server: MinecraftServer) {
        var savedCount = 0
        cities.values.forEach { city ->
            if (YamlDataManager.saveCityData(server, city)) {
                savedCount++
            }
        }
        RtsMod.LOGGER.debug("Autosaved $savedCount cities")
    }

    /**
     * Get game statistics
     */
    fun getGameStats(): GameStats {
        val totalCities = cities.size
        val totalUnits = units.size
        val totalBuildings = buildings.size
        val activeCities = cities.values.count { it.isActive }
        
        return GameStats(
            totalCities = totalCities,
            activeCities = activeCities,
            totalUnits = totalUnits,
            totalBuildings = totalBuildings,
            playerCount = cities.size
        )
    }

    /**
     * Data class for game statistics
     */
    data class GameStats(
        val totalCities: Int,
        val activeCities: Int,
        val totalUnits: Int,
        val totalBuildings: Int,
        val playerCount: Int
    )
}