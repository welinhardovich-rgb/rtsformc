package com.rtsmod.network.packets

import com.rtsmod.core.Unit
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

/**
 * Packet for updating unit positions and states
 */
data class UnitUpdatePacket(
    val unitId: String,
    val positionX: Int,
    val positionY: Int,
    val positionZ: Int,
    val health: Int,
    val maxHealth: Int,
    val state: Unit.UnitState,
    val targetX: Int? = null,
    val targetY: Int? = null,
    val targetZ: Int? = null
) {
    
    /**
     * Write packet data to buffer
     */
    fun write(buf: PacketByteBuf) {
        buf.writeString(unitId)
        buf.writeInt(positionX)
        buf.writeInt(positionY)
        buf.writeInt(positionZ)
        buf.writeInt(health)
        buf.writeInt(maxHealth)
        buf.writeString(state.name)
        
        // Write target position if exists
        if (targetX != null && targetY != null && targetZ != null) {
            buf.writeBoolean(true)
            buf.writeInt(targetX)
            buf.writeInt(targetY)
            buf.writeInt(targetZ)
        } else {
            buf.writeBoolean(false)
        }
    }

    companion object {
        /**
         * Read packet data from buffer
         */
        fun read(buf: PacketByteBuf): UnitUpdatePacket {
            val unitId = buf.readString()
            val positionX = buf.readInt()
            val positionY = buf.readInt()
            val positionZ = buf.readInt()
            val health = buf.readInt()
            val maxHealth = buf.readInt()
            val state = Unit.UnitState.valueOf(buf.readString())
            
            val targetX: Int?
            val targetY: Int?
            val targetZ: Int?
            
            if (buf.readBoolean()) {
                targetX = buf.readInt()
                targetY = buf.readInt()
                targetZ = buf.readInt()
            } else {
                targetX = null
                targetY = null
                targetZ = null
            }
            
            return UnitUpdatePacket(
                unitId = unitId,
                positionX = positionX,
                positionY = positionY,
                positionZ = positionZ,
                health = health,
                maxHealth = maxHealth,
                state = state,
                targetX = targetX,
                targetY = targetY,
                targetZ = targetZ
            )
        }
    }
}