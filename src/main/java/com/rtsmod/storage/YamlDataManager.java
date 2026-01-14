package com.rtsmod.storage;

import com.rtsmod.core.City;
import com.rtsmod.core.Resources;
import com.rtsmod.core.ResourceType;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages loading and saving city data using YAML.
 */
public class YamlDataManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("RtsMod-Storage");
    private final Path storagePath;
    private final Yaml yaml;

    public YamlDataManager(Path worldDir) {
        this.storagePath = worldDir.resolve("rtsmod/cities");
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yaml = new Yaml(options);
        
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            LOGGER.error("Could not create storage directory: {}", storagePath, e);
        }
    }

    public void saveCityData(City city) {
        Path path = storagePath.resolve(city.getOwner().toString() + ".yml");
        Map<String, Object> data = serializeCity(city);
        try (FileWriter writer = new FileWriter(path.toFile())) {
            yaml.dump(data, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save city data for {}", city.getOwner(), e);
        }
    }

    public City loadCityData(UUID playerId) {
        Path path = storagePath.resolve(playerId.toString() + ".yml");
        if (!Files.exists(path)) {
            return null;
        }

        try (FileReader reader = new FileReader(path.toFile())) {
            Map<String, Object> data = yaml.load(reader);
            if (data == null) return null;
            return deserializeCity(playerId, data);
        } catch (IOException e) {
            LOGGER.error("Failed to load city data for {}", playerId, e);
            return null;
        }
    }

    public Map<UUID, City> loadAllCities() {
        Map<UUID, City> cities = new HashMap<>();
        File folder = storagePath.toFile();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        
        if (files != null) {
            for (File file : files) {
                String name = file.getName().replace(".yml", "");
                try {
                    UUID uuid = UUID.fromString(name);
                    City city = loadCityData(uuid);
                    if (city != null) {
                        cities.put(uuid, city);
                    }
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Invalid city file name: {}", file.getName());
                }
            }
        }
        return cities;
    }

    private Map<String, Object> serializeCity(City city) {
        Map<String, Object> data = new HashMap<>();
        data.put("owner", city.getOwner().toString());
        data.put("location", serializeBlockPos(city.getLocation()));
        
        Map<String, Integer> resourcesData = new HashMap<>();
        for (ResourceType type : ResourceType.values()) {
            resourcesData.put(type.name(), city.getResources().get(type));
        }
        data.put("resources", resourcesData);
        
        return data;
    }

    private City deserializeCity(UUID playerId, Map<String, Object> data) {
        Map<String, Object> locationData = (Map<String, Object>) data.get("location");
        BlockPos location = deserializeBlockPos(locationData);
        City city = new City(playerId, location);
        
        Map<String, Integer> resourcesData = (Map<String, Integer>) data.get("resources");
        if (resourcesData != null) {
            for (Map.Entry<String, Integer> entry : resourcesData.entrySet()) {
                try {
                    ResourceType type = ResourceType.valueOf(entry.getKey());
                    city.addResource(type, entry.getValue());
                } catch (IllegalArgumentException ignored) {}
            }
        }
        
        return city;
    }

    private Map<String, Object> serializeBlockPos(BlockPos pos) {
        Map<String, Object> data = new HashMap<>();
        data.put("x", pos.getX());
        data.put("y", pos.getY());
        data.put("z", pos.getZ());
        return data;
    }

    private BlockPos deserializeBlockPos(Map<String, Object> data) {
        if (data == null) return BlockPos.ORIGIN;
        int x = ((Number) data.getOrDefault("x", 0)).intValue();
        int y = ((Number) data.getOrDefault("y", 0)).intValue();
        int z = ((Number) data.getOrDefault("z", 0)).intValue();
        return new BlockPos(x, y, z);
    }
}
