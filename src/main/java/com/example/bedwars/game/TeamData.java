package com.example.bedwars.game;

import com.example.bedwars.config.TeamConfig;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TeamData {
    private final TeamColor color;
    private final TeamConfig config;
    private final Set<UUID> players = new HashSet<>();
    private boolean bedAlive = true;
    private int generatorTier = 1;
    private int protectionLevel = 0;
    private final TeamUpgrades upgrades = new TeamUpgrades();

    public TeamData(TeamColor color, TeamConfig config) {
        this.color = color;
        this.config = config;
    }

    public TeamColor getColor() {
        return color;
    }

    public TeamConfig getConfig() {
        return config;
    }

    public Set<UUID> getPlayers() {
        return players;
    }

    public boolean isBedAlive() {
        return bedAlive;
    }

    public void setBedAlive(boolean bedAlive) {
        this.bedAlive = bedAlive;
    }

    public int getGeneratorTier() {
        return generatorTier;
    }

    public void setGeneratorTier(int generatorTier) {
        this.generatorTier = generatorTier;
    }

    public int getProtectionLevel() {
        return protectionLevel;
    }

    public void setProtectionLevel(int protectionLevel) {
        this.protectionLevel = protectionLevel;
    }

    public TeamUpgrades getUpgrades() {
        return upgrades;
    }

    public Location getSpawn() {
        return config.getSpawn();
    }

    public Location getBedLocation() {
        return config.getBedLocation();
    }

    public Location getBaseGenerator() {
        return config.getBaseGenerator();
    }
}
