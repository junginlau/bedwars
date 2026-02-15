package com.example.bedwars.stats;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class StatsManager {
    private final JavaPlugin plugin;
    private final File statsFile;
    private final Map<UUID, PlayerStats> statsCache = new HashMap<>();

    public StatsManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.statsFile = new File(plugin.getDataFolder(), "player-stats.yml");
    }

    public void loadAll() {
        statsCache.clear();
        if (!statsFile.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(statsFile);
        ConfigurationSection playersSection = config.getConfigurationSection("players");
        if (playersSection == null) {
            return;
        }
        for (String uuidStr : playersSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection section = playersSection.getConfigurationSection(uuidStr);
                if (section == null) {
                    continue;
                }
                String name = section.getString("name", "Unknown");
                PlayerStats stats = new PlayerStats(uuid, name);
                stats.addKill(); // dummy to set initial value
                // Load actual values
                loadStatsFromSection(stats, section);
                statsCache.put(uuid, stats);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in stats: " + uuidStr);
            }
        }
    }

    private void loadStatsFromSection(PlayerStats stats, ConfigurationSection section) {
        // Reset and load properly
        int kills = section.getInt("kills", 0);
        int deaths = section.getInt("deaths", 0);
        int beds = section.getInt("beds_destroyed", 0);
        int wins = section.getInt("wins", 0);
        int losses = section.getInt("losses", 0);
        int games = section.getInt("games_played", 0);
        int winStreak = section.getInt("win_streak", 0);
        
        // Use reflection or direct field access - for simplicity, recreate
        PlayerStats loaded = new PlayerStats(stats.getPlayerId(), stats.getPlayerName());
        for (int i = 0; i < kills; i++) loaded.addKill();
        for (int i = 0; i < deaths; i++) loaded.addDeath();
        for (int i = 0; i < beds; i++) loaded.addBedDestroyed();
        for (int i = 0; i < wins; i++) loaded.addWin();
        for (int i = 0; i < losses; i++) loaded.addLoss();
        for (int i = 0; i < games; i++) loaded.addGamePlayed();
        loaded.setWinStreak(winStreak);
        
        statsCache.put(loaded.getPlayerId(), loaded);
    }

    public void saveAll() {
        YamlConfiguration config = new YamlConfiguration();
        for (PlayerStats stats : statsCache.values()) {
            String path = "players." + stats.getPlayerId().toString();
            config.set(path + ".name", stats.getPlayerName());
            config.set(path + ".kills", stats.getKills());
            config.set(path + ".deaths", stats.getDeaths());
            config.set(path + ".beds_destroyed", stats.getBedsDestroyed());
            config.set(path + ".wins", stats.getWins());
            config.set(path + ".losses", stats.getLosses());
            config.set(path + ".games_played", stats.getGamesPlayed());
            config.set(path + ".win_streak", stats.getWinStreak());
        }
        try {
            config.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player stats: " + e.getMessage());
        }
    }

    public PlayerStats getStats(Player player) {
        return getStats(player.getUniqueId(), player.getName());
    }

    public PlayerStats getStats(UUID uuid, String name) {
        PlayerStats stats = statsCache.get(uuid);
        if (stats == null) {
            stats = new PlayerStats(uuid, name);
            statsCache.put(uuid, stats);
        } else {
            stats.setPlayerName(name); // Update name if changed
        }
        return stats;
    }

    public List<PlayerStats> getTopKills(int limit) {
        return statsCache.values().stream()
                .sorted(Comparator.comparingInt(PlayerStats::getKills).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<PlayerStats> getTopBedsDestroyed(int limit) {
        return statsCache.values().stream()
                .sorted(Comparator.comparingInt(PlayerStats::getBedsDestroyed).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<PlayerStats> getTopWins(int limit) {
        return statsCache.values().stream()
                .sorted(Comparator.comparingInt(PlayerStats::getWins).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<PlayerStats> getTopWinStreaks(int limit) {
        return statsCache.values().stream()
                .sorted(Comparator.comparingInt(PlayerStats::getWinStreak).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void recordKill(Player killer) {
        getStats(killer).addKill();
    }

    public void recordDeath(Player victim) {
        getStats(victim).addDeath();
    }

    public void recordBedDestroyed(Player breaker) {
        getStats(breaker).addBedDestroyed();
    }

    public void recordWin(UUID playerId, String playerName) {
        PlayerStats stats = getStats(playerId, playerName);
        stats.addWin();
        stats.addGamePlayed();
    }

    public void recordLoss(UUID playerId, String playerName) {
        PlayerStats stats = getStats(playerId, playerName);
        stats.addLoss();
        stats.addGamePlayed();
    }
}
