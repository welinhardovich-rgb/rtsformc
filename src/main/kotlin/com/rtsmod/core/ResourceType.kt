package com.rtsmod.core

/**
 * Represents the types of resources in the RTS game
 */
enum class ResourceType(val displayName: String) {
    GOLD("Gold"),
    WOOD("Wood"), 
    STONE("Stone"),
    FOOD("Food");

    companion object {
        fun fromString(name: String): ResourceType? {
            return values().find { it.name.equals(name, ignoreCase = true) }
        }
    }
}