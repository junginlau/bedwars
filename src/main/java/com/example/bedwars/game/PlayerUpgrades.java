package com.example.bedwars.game;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerUpgrades {
    private final UUID playerId;
    private int swordTier = 0;  // 0=木剑, 1=石剑, 2=铁剑, 3=钻石剑
    private int pickaxeTier = 0;  // 0=木镐, 1=石镐, 2=铁镐, 3=钻石镐
    private int axeTier = 0;  // 0=木斧, 1=石斧, 2=铁斧, 3=钻石斧
    private int armorTier = 0;  // 0=无, 1=链甲, 2=铁, 3=钻石
    private boolean hasShears = false;

    public PlayerUpgrades(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getSwordTier() {
        return swordTier;
    }

    public void setSwordTier(int tier) {
        this.swordTier = Math.min(tier, 3);
    }

    public int getPickaxeTier() {
        return pickaxeTier;
    }

    public void setPickaxeTier(int tier) {
        this.pickaxeTier = Math.min(tier, 3);
    }

    public int getAxeTier() {
        return axeTier;
    }

    public void setAxeTier(int tier) {
        this.axeTier = Math.min(tier, 3);
    }

    public int getArmorTier() {
        return armorTier;
    }

    public void setArmorTier(int tier) {
        this.armorTier = Math.min(tier, 3);
    }

    public boolean hasShears() {
        return hasShears;
    }

    public void setHasShears(boolean hasShears) {
        this.hasShears = hasShears;
    }

    public void reset() {
        swordTier = 0;
        pickaxeTier = 0;
        axeTier = 0;
        armorTier = 0;
        hasShears = false;
    }
}
