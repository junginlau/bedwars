package com.example.bedwars.map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapManager {
    private final JavaPlugin plugin;
    private final File mapsFolder;
    private final Map<String, MapConfig> maps = new LinkedHashMap<>();
    private String activeMapName = "";

    public MapManager(JavaPlugin plugin) {
        this.plugin = plugin;
        String folderName = plugin.getConfig().getString("maps-folder", "maps");
        this.mapsFolder = new File(plugin.getDataFolder(), folderName);
    }

    public void loadAll() {
        maps.clear();
        if (!mapsFolder.exists()) {
            mapsFolder.mkdirs();
        }
        File[] files = mapsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String mapName = file.getName().replace(".yml", "");
                MapConfig map = MapConfig.load(file, mapName, getDefaultWorld());
                maps.put(mapName.toLowerCase(), map);
            }
        }
        String configuredActive = plugin.getConfig().getString("active-map", "");
        if (configuredActive != null && !configuredActive.isBlank()) {
            setActiveMap(configuredActive);
        } else if (!maps.isEmpty()) {
            setActiveMap(new ArrayList<>(maps.values()).get(0).getName());
        }
    }

    public MapConfig createMap(String name, Player player) {
        String key = name.toLowerCase();
        Location lobby = player.getLocation();
        MapConfig map = new MapConfig(name, player.getWorld().getName(), lobby);
        maps.put(key, map);
        save(map);
        return map;
    }

    public void save(MapConfig map) {
        if (!mapsFolder.exists()) {
            mapsFolder.mkdirs();
        }
        File file = new File(mapsFolder, map.getName() + ".yml");
        map.save(file);
    }

    public boolean deleteMap(String name) {
        MapConfig map = getMap(name);
        if (map == null) {
            return false;
        }
        maps.remove(map.getName().toLowerCase());
        File file = new File(mapsFolder, map.getName() + ".yml");
        if (file.exists()) {
            file.delete();
        }
        if (map.getName().equalsIgnoreCase(activeMapName)) {
            activeMapName = "";
            if (!maps.isEmpty()) {
                activeMapName = maps.values().iterator().next().getName();
            }
            saveActiveMapName();
        }
        return true;
    }

    public boolean renameMap(String oldName, String newName) {
        if (oldName == null || newName == null) {
            return false;
        }
        MapConfig map = getMap(oldName);
        if (map == null) {
            return false;
        }
        if (getMap(newName) != null) {
            return false;
        }
        String oldKey = map.getName().toLowerCase();
        File oldFile = new File(mapsFolder, map.getName() + ".yml");

        map.setName(newName);
        maps.remove(oldKey);
        maps.put(newName.toLowerCase(), map);

        if (oldFile.exists()) {
            oldFile.delete();
        }
        save(map);

        if (activeMapName != null && activeMapName.equalsIgnoreCase(oldName)) {
            activeMapName = newName;
            saveActiveMapName();
        }
        return true;
    }

    public void saveActiveMapName() {
        plugin.getConfig().set("active-map", activeMapName);
        plugin.saveConfig();
    }

    public MapConfig getMap(String name) {
        if (name == null) {
            return null;
        }
        return maps.get(name.toLowerCase());
    }

    public List<String> getMapNames() {
        List<String> names = new ArrayList<>();
        for (MapConfig map : maps.values()) {
            names.add(map.getName());
        }
        return names;
    }

    public void setActiveMap(String name) {
        MapConfig map = getMap(name);
        if (map == null) {
            return;
        }
        activeMapName = map.getName();
        saveActiveMapName();
    }

    public MapConfig getActiveMap() {
        if (activeMapName == null || activeMapName.isBlank()) {
            return null;
        }
        return getMap(activeMapName);
    }

    public String getActiveMapName() {
        return activeMapName;
    }

    private World getDefaultWorld() {
        World world = Bukkit.getWorlds().get(0);
        return world != null ? world : Bukkit.getWorld("world");
    }
}
