package com.example.bedwars.game;

public class TeamUpgrades {
    private int sharpnessLevel = 0;
    private int protectionLevel = 0;
    private int hasteLevel = 0;
    private boolean healPool = false;
    private int resourceGenLevel = 1;

    public int getSharpnessLevel() {
        return sharpnessLevel;
    }

    public void upgradeSharpness() {
        if (sharpnessLevel < 1) {
            sharpnessLevel++;
        }
    }

    public int getProtectionLevel() {
        return protectionLevel;
    }

    public void upgradeProtection() {
        if (protectionLevel < 4) {
            protectionLevel++;
        }
    }

    public int getHasteLevel() {
        return hasteLevel;
    }

    public void upgradeHaste() {
        if (hasteLevel < 2) {
            hasteLevel++;
        }
    }

    public boolean hasHealPool() {
        return healPool;
    }

    public void enableHealPool() {
        this.healPool = true;
    }

    public int getResourceGenLevel() {
        return resourceGenLevel;
    }

    public void upgradeResourceGen() {
        if (resourceGenLevel < 4) {
            resourceGenLevel++;
        }
    }

    public void reset() {
        sharpnessLevel = 0;
        protectionLevel = 0;
        hasteLevel = 0;
        healPool = false;
        resourceGenLevel = 1;
    }
}
