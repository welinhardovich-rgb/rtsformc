package com.rtsmod.network.packets

import com.rtsmod.core.City
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

/**
 * Packet for syncing city data from server to client
 */
data class CityDataSyncPacket(
    val city: City
) {
    
    /**
     * Write packet data to buffer
     */
    fun write(buf: PacketByteBuf) {
        buf.writeString(city.id)
        buf.writeString(city.owner.toString())
        buf.writeString(city.name)
        buf.writeInt(city.location.x)
        buf.writeInt(city.location.y)
        buf.writeInt(city.location.z)
        buf.writeInt(city.population)
        buf.writeInt(city.maxPopulation)
        buf.writeInt(city.cityLevel)
        buf.writeBoolean(city.isActive)
        buf.writeLong(city.creationTime)
        
        // Write resources
        val resources = city.resources.getAll()
        buf.writeInt(resources.size)
        resources.forEach { (type, amount) ->
            buf.writeString(type.name)
            buf.writeInt(amount)
        }
        
        // Write buildings count
        val buildings = city.getBuildings()
        buf.writeInt(buildings.size)
        buildings.forEach { building ->
            buf.writeString(building.id)
            buf.writeString(building.type.name)
            buf.writeString(building.owner.toString())
            buf.writeInt(building.position.x)
            buf.writeInt(building.position.y)
            buf.writeInt(building.position.z)
            buf.writeInt(building.health)
            buf.writeInt(building.maxHealth)
            buf.writeBoolean(building.isUnderConstruction)
            buf.writeInt(building.constructionProgress)
        }
        
        // Write units count
        val units = city.getUnits()
        buf.writeInt(units.size)
        units.forEach { unit ->
            buf.writeString(unit.id)
            buf.writeString(unit.type.name)
            buf.writeString(unit.owner.toString())
            buf.writeInt(unit.position.x)
            buf.writeInt(unit.position.y)
            buf.writeInt(unit.position.z)
            buf.writeInt(unit.health)
            buf.writeInt(unit.maxHealth)
            buf.writeString(unit.state.name)
        }
    }

    companion object {
        /**
         * Read packet data from buffer
         */
        fun read(buf: PacketByteBuf): CityDataSyncPacket {
            val cityId = buf.readString()
            val owner = java.util.UUID.fromString(buf.readString())
            val name = buf.readString()
            val x = buf.readInt()
            val y = buf.readInt()
            val z = buf.readInt()
            val population = buf.readInt()
            val maxPopulation = buf.readInt()
            val cityLevel = buf.readInt()
            val isActive = buf.readBoolean()
            val creationTime = buf.readLong()
            
            // Read resources
            val resourcesMap = mutableMapOf<com.rtsmod.core.ResourceType, Int>()
            val resourcesCount = buf.readInt()
            repeat(resourcesCount) {
                val typeName = buf.readString()
                val amount = buf.readInt()
                val resourceType = com.rtsmod.core.ResourceType.valueOf(typeName)
                resourcesMap[resourceType] = amount
            }
            
            // Read buildings
            val buildingsList = mutableListOf<com.rtsmod.core.Building>()
            val buildingsCount = buf.readInt()
            repeat(buildingsCount) {
                val buildingId = buf.readString()
                val buildingType = com.rtsmod.core.Building.BuildingType.valueOf(buf.readString())
                val buildingOwner = java.util.UUID.fromString(buf.readString())
                val bx = buf.readInt()
                val by = buf.readInt()
                val bz = buf.readInt()
                val bHealth = buf.readInt()
                val bMaxHealth = buf.readInt()
                val bIsUnderConstruction = buf.readBoolean()
                val bConstructionProgress = buf.readInt()
                
                val building = com.rtsmod.core.Building(
                    id = buildingId,
                    type = buildingType,
                    owner = buildingOwner,
                    position = net.minecraft.util.math.BlockPos(bx, by, bz),
                    health = bHealth,
                    maxHealth = bMaxHealth,
                    isUnderConstruction = bIsUnderConstruction,
                    constructionProgress = bConstructionProgress
                )
                buildingsList.add(building)
            }
            
            // Read units
            val unitsList = mutableListOf<com.rtsmod.core.Unit>()
            val unitsCount = buf.readInt()
            repeat(unitsCount) {
                val unitId = buf.readString()
                val unitType = com.rtsmod.core.Unit.UnitType.valueOf(buf.readString())
                val unitOwner = java.util.UUID.fromString(buf.readString())
                val ux = buf.readInt()
                val uy = buf.readInt()
                val uz = buf.readInt()
                val uHealth = buf.readInt()
                val uMaxHealth = buf.readInt()
                val uState = com.rtsmod.core.Unit.UnitState.valueOf(buf.readString())
                
                val unit = com.rtsmod.core.Unit(
                    id = unitId,
                    type = unitType,
                    owner = unitOwner,
                    position = net.minecraft.util.math.BlockPos(ux, uy, uz),
                    health = uHealth,
                    maxHealth = uMaxHealth,
                    state = uState
                )
                unitsList.add(unit)
            }
            
            // Create city
            val city = com.rtsmod.core.City(
                id = cityId,
                owner = owner,
                name = name,
                location = net.minecraft.util.math.BlockPos(x, y, z),
                resources = com.rtsmod.core.Resources(resourcesMap),
                population = population,
                maxPopulation = maxPopulation,
                cityLevel = cityLevel,
                isActive = isActive,
                creationTime = creationTime
            )
            
            // Add buildings and units to city (they would be added in actual game logic)
            
            return CityDataSyncPacket(city)
        }
    }
}