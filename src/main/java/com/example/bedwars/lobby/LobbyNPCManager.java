package com.example.bedwars.lobby;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LobbyNPCManager {
    private final JavaPlugin plugin;
    private final File configFile;
    private final List<LobbyNPC> npcs = new ArrayList<>();

    public LobbyNPCManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "lobby-npcs.yml");
    }

    public void load() {
        removeAll();
        if (!configFile.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            String worldName = section.getString("world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                continue;
            }
            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            float yaw = (float) section.getDouble("yaw", 0);
            float pitch = (float) section.getDouble("pitch", 0);
            String name = section.getString("name", "Bedwars");
            Location location = new Location(world, x, y, z, yaw, pitch);
            LobbyNPC npc = new LobbyNPC(plugin, location, name);
            npc.spawn();
            npcs.add(npc);
        }
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        int index = 0;
        for (LobbyNPC npc : npcs) {
            String key = "npc-" + index;
            Location loc = npc.getLocation();
            config.set(key + ".world", loc.getWorld().getName());
            config.set(key + ".x", loc.getX());
            config.set(key + ".y", loc.getY());
            config.set(key + ".z", loc.getZ());
            config.set(key + ".yaw", loc.getYaw());
            config.set(key + ".pitch", loc.getPitch());
            config.set(key + ".name", npc.getDisplayName());
            index++;
        }
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save lobby NPCs: " + e.getMessage());
        }
    }

    public void addNPC(Location location, String displayName) {
        LobbyNPC npc = new LobbyNPC(plugin, location, displayName);
        npc.spawn();
        npcs.add(npc);
        save();
    }

    public boolean removeNPC(LobbyNPC npc) {
        if (npc == null) {
            return false;
        }
        npc.remove();
        boolean removed = npcs.remove(npc);
        if (removed) {
            save();
        }
        return removed;
    }

    public LobbyNPC updateNPC(LobbyNPC npc, Location newLocation, String newName) {
        if (npc == null) {
            return null;
        }
        int index = npcs.indexOf(npc);
        if (index < 0) {
            return null;
        }
        npc.remove();
        LobbyNPC updated = new LobbyNPC(plugin, newLocation, newName);
        updated.spawn();
        npcs.set(index, updated);
        save();
        return updated;
    }

    public LobbyNPC findNearestNPC(Location location, double radius) {
        LobbyNPC nearest = null;
        double closest = Double.MAX_VALUE;
        for (LobbyNPC npc : npcs) {
            if (!npc.getLocation().getWorld().equals(location.getWorld())) {
                continue;
            }
            double dist = npc.getLocation().distance(location);
            if (dist <= radius && dist < closest) {
                closest = dist;
                nearest = npc;
            }
        }
        return nearest;
    }

    public void removeAll() {
        for (LobbyNPC npc : npcs) {
            npc.remove();
        }
        npcs.clear();
    }

    public LobbyNPC findNPC(Entity entity) {
        for (LobbyNPC npc : npcs) {
            if (npc.isNPC(entity)) {
                return npc;
            }
        }
        return null;
    }

    public List<LobbyNPC> getNPCs() {
        return new ArrayList<>(npcs);
    }

    public void respawnAll() {
        for (LobbyNPC npc : npcs) {
            npc.spawn();
        }
    }
}
