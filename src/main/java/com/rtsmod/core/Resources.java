package com.rtsmod.core;

import java.util.EnumMap;
import java.util.Map;

/**
 * Container for resource amounts of different types.
 */
public class Resources {
    private final Map<ResourceType, Integer> amounts = new EnumMap<>(ResourceType.class);

    public Resources() {
        for (ResourceType type : ResourceType.values()) {
            amounts.put(type, 0);
        }
    }

    public void add(ResourceType type, int amount) {
        amounts.put(type, amounts.getOrDefault(type, 0) + amount);
    }

    public void subtract(ResourceType type, int amount) {
        amounts.put(type, Math.max(0, amounts.getOrDefault(type, 0) - amount));
    }

    public int get(ResourceType type) {
        return amounts.getOrDefault(type, 0);
    }

    public boolean hasEnough(ResourceType type, int amount) {
        return get(type) >= amount;
    }

    public Map<ResourceType, Integer> getAmounts() {
        return new EnumMap<>(amounts);
    }
}
