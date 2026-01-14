package com.rtsmod.storage

import com.rtsmod.core.City
import com.rtsmod.core.Resources
import net.minecraft.server.MinecraftServer
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.representer.Representer
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * Manages YAML data persistence for RTS game data
 * Stores city data, player progress, and game state
 */
object YamlDataManager {
    private val yaml = Yaml()

    /**
     * Get the directory where RTS data should be stored
     */
    private fun getDataDirectory(server: MinecraftServer): Path {
        return server.getSavePath(StorageSaveHandler.RTS_DATA_DIR)
    }

    /**
     * Get the cities directory within the data directory
     */
    private fun getCitiesDirectory(server: MinecraftServer): Path {
        return getDataDirectory(server).resolve("cities")
    }

    /**
     * Ensure directories exist
     */
    private fun ensureDirectories(server: MinecraftServer) {
        Files.createDirectories(getCitiesDirectory(server))
    }

    /**
     * Load city data for a specific player
     */
    fun loadCityData(server: MinecraftServer, playerId: UUID): City? {
        return try {
            ensureDirectories(server)
            val cityFile = getCitiesDirectory(server).resolve("$playerId.yml")
            
            if (Files.exists(cityFile)) {
                FileReader(cityFile).use { reader ->
                    val data = yaml.loadAs(reader, MapData::class.java)
                    data.toCity()
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Save city data for a specific player
     */
    fun saveCityData(server: MinecraftServer, city: City): Boolean {
        return try {
            ensureDirectories(server)
            val cityFile = getCitiesDirectory(server).resolve("${city.owner}.yml")
            
            val data = MapData.fromCity(city)
            
            FileWriter(cityFile).use { writer ->
                yaml.dump(data, writer)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Load all cities for all players
     */
    fun loadAllCities(server: MinecraftServer): Map<UUID, City> {
        return try {
            ensureDirectories(server)
            val citiesDir = getCitiesDirectory(server)
            val cities = mutableMapOf<UUID, City>()
            
            if (Files.exists(citiesDir)) {
                Files.list(citiesDir)
                    .filter { it.toString().endsWith(".yml") }
                    .forEach { file ->
                        try {
                            FileReader(file.toFile()).use { reader ->
                                val data = yaml.loadAs(reader, MapData::class.java)
                                val city = data.toCity()
                                cities[city.owner] = city
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
            }
            cities.toMap()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    /**
     * Delete city data for a player
     */
    fun deleteCityData(server: MinecraftServer, playerId: UUID): Boolean {
        return try {
            val cityFile = getCitiesDirectory(server).resolve("$playerId.yml")
            if (Files.exists(cityFile)) {
                Files.delete(cityFile)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Backup all city data
     */
    fun backupCityData(server: MinecraftServer): Boolean {
        return try {
            val citiesDir = getCitiesDirectory(server)
            val backupDir = citiesDir.parent.resolve("backup_${System.currentTimeMillis()}")
            
            if (Files.exists(citiesDir)) {
                Files.createDirectories(backupDir)
                Files.walk(citiesDir)
                    .forEach { source ->
                        val destination = backupDir.resolve(citiesDir.relativize(source))
                        Files.copy(source, destination)
                    }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Data class for YAML serialization/deserialization
     */
    private data class MapData(
        val id: String,
        val owner: String,
        val name: String,
        val locationX: Int,
        val locationY: Int,
        val locationZ: Int,
        val resources: Map<String, Int>,
        val buildings: List<Map<String, Any>>,
        val units: List<Map<String, Any>>,
        val population: Int,
        val maxPopulation: Int,
        val cityLevel: Int,
        val isActive: Boolean,
        val creationTime: Long
    ) {
        fun toCity(): City {
            return City(
                id = id,
                owner = UUID.fromString(owner),
                name = name,
                location = net.minecraft.util.math.BlockPos(locationX, locationY, locationZ),
                resources = Resources(resources.mapKeys { 
                    com.rtsmod.core.ResourceType.valueOf(it.key) 
                }.toMutableMap()),
                population = population,
                maxPopulation = maxPopulation,
                cityLevel = cityLevel,
                isActive = isActive,
                creationTime = creationTime
            ).apply {
                // Note: Buildings and units would need additional deserialization logic
                // For now, we'll keep them empty and let game logic handle them
            }
        }

        companion object {
            fun fromCity(city: City): MapData {
                return MapData(
                    id = city.id,
                    owner = city.owner.toString(),
                    name = city.name,
                    locationX = city.location.x,
                    locationY = city.location.y,
                    locationZ = city.location.z,
                    resources = city.resources.getAll().mapKeys { it.key.name },
                    buildings = city.getBuildings().map { building ->
                        mapOf(
                            "id" to building.id,
                            "type" to building.type.name,
                            "owner" to building.owner.toString(),
                            "x" to building.position.x,
                            "y" to building.position.y,
                            "z" to building.position.z,
                            "health" to building.health,
                            "maxHealth" to building.maxHealth,
                            "buildTime" to building.buildTime,
                            "isUnderConstruction" to building.isUnderConstruction,
                            "constructionProgress" to building.constructionProgress
                        )
                    },
                    units = city.getUnits().map { unit ->
                        mapOf(
                            "id" to unit.id,
                            "type" to unit.type.name,
                            "owner" to unit.owner.toString(),
                            "x" to unit.position.x,
                            "y" to unit.position.y,
                            "z" to unit.position.z,
                            "health" to unit.health,
                            "maxHealth" to unit.maxHealth,
                            "state" to unit.state.name
                        )
                    },
                    population = city.population,
                    maxPopulation = city.maxPopulation,
                    cityLevel = city.cityLevel,
                    isActive = city.isActive,
                    creationTime = city.creationTime
                )
            }
        }
    }

    /**
     * Custom storage save handler for Fabric
     */
    private object StorageSaveHandler : net.minecraft.world.storage.StorageSidecar {
        const val RTS_DATA_DIR = "rtsmod"
    }
}