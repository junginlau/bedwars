package com.example.bedwars.config;

import com.example.bedwars.game.TeamColor;
import org.bukkit.Location;

public class TeamConfig {
    private final TeamColor color;
    private Location spawn;
    private Location bedLocation;
    private Location baseGenerator;
    private Location shopNpcLocation;
    private boolean spawnSet;
    private boolean bedSet;
    private boolean baseGeneratorSet;
    private boolean shopNpcSet;

    public TeamConfig(TeamColor color, Location spawn, Location bedLocation, Location baseGenerator) {
        this(color, spawn, bedLocation, baseGenerator, spawn, false, false, false, false);
    }

    public TeamConfig(TeamColor color, Location spawn, Location bedLocation, Location baseGenerator,
                      Location shopNpcLocation, boolean spawnSet, boolean bedSet, boolean baseGeneratorSet,
                      boolean shopNpcSet) {
        this.color = color;
        this.spawn = spawn;
        this.bedLocation = bedLocation;
        this.baseGenerator = baseGenerator;
        this.shopNpcLocation = shopNpcLocation;
        this.spawnSet = spawnSet;
        this.bedSet = bedSet;
        this.baseGeneratorSet = baseGeneratorSet;
        this.shopNpcSet = shopNpcSet;
    }

    public TeamColor getColor() {
        return color;
    }

    public Location getSpawn() {
        return spawn;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
        this.spawnSet = true;
    }

    public Location getBedLocation() {
        return bedLocation;
    }

    public void setBedLocation(Location bedLocation) {
        this.bedLocation = bedLocation;
        this.bedSet = true;
    }

    public Location getBaseGenerator() {
        return baseGenerator;
    }

    public void setBaseGenerator(Location baseGenerator) {
        this.baseGenerator = baseGenerator;
        this.baseGeneratorSet = true;
    }

    public Location getShopNpcLocation() {
        return shopNpcLocation;
    }

    public void setShopNpcLocation(Location shopNpcLocation) {
        this.shopNpcLocation = shopNpcLocation;
        this.shopNpcSet = true;
    }

    public boolean isSpawnSet() {
        return spawnSet;
    }

    public void setSpawnSet(boolean spawnSet) {
        this.spawnSet = spawnSet;
    }

    public boolean isBedSet() {
        return bedSet;
    }

    public void setBedSet(boolean bedSet) {
        this.bedSet = bedSet;
    }

    public boolean isBaseGeneratorSet() {
        return baseGeneratorSet;
    }

    public void setBaseGeneratorSet(boolean baseGeneratorSet) {
        this.baseGeneratorSet = baseGeneratorSet;
    }

    public boolean isShopNpcSet() {
        return shopNpcSet;
    }

    public void setShopNpcSet(boolean shopNpcSet) {
        this.shopNpcSet = shopNpcSet;
    }
}
