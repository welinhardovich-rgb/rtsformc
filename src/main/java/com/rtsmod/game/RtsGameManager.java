package com.rtsmod.game;

import com.rtsmod.core.City;
import com.rtsmod.core.Unit;
import com.rtsmod.storage.YamlDataManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main game logic coordinator for the RTS mod.
 */
public class RtsGameManager {
    private static RtsGameManager instance;
    private final Map<UUID, City> cities = new ConcurrentHashMap<>();
    private final Map<UUID, Unit> units = new ConcurrentHashMap<>();
    private YamlDataManager dataManager;

    private RtsGameManager() {}

    public static RtsGameManager getInstance() {
        if (instance == null) {
            instance = new RtsGameManager();
        }
        return instance;
    }

    public void onServerStarting(MinecraftServer server) {
        Path worldDir = server.getSavePath(WorldSavePath.ROOT);
        this.dataManager = new YamlDataManager(worldDir);
        cities.clear();
        cities.putAll(dataManager.loadAllCities());
    }

    public void onServerStopping(MinecraftServer server) {
        if (dataManager != null) {
            for (City city : cities.values()) {
                dataManager.saveCityData(city);
            }
        }
    }

    public void tick(MinecraftServer server) {
        // Handle game logic for each tick
    }

    public City createCity(UUID owner, BlockPos location) {
        if (cities.containsKey(owner)) {
            return cities.get(owner);
        }
        City city = new City(owner, location);
        cities.put(owner, city);
        if (dataManager != null) {
            dataManager.saveCityData(city);
        }
        return city;
    }

    public City getCity(UUID owner) {
        return cities.get(owner);
    }

    public Map<UUID, City> getCities() {
        return cities;
    }

    public Map<UUID, Unit> getUnits() {
        return units;
    }
}
