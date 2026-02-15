package com.example.bedwars.stats;

import java.util.UUID;

public class PlayerStats {
    private final UUID playerId;
    private String playerName;
    private int kills;
    private int deaths;
    private int bedsDestroyed;
    private int wins;
    private int losses;
    private int gamesPlayed;
    private int winStreak;

    public PlayerStats(UUID playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.kills = 0;
        this.deaths = 0;
        this.bedsDestroyed = 0;
        this.wins = 0;
        this.losses = 0;
        this.gamesPlayed = 0;
        this.winStreak = 0;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        this.kills++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        this.deaths++;
    }

    public int getBedsDestroyed() {
        return bedsDestroyed;
    }

    public void addBedDestroyed() {
        this.bedsDestroyed++;
    }

    public int getWins() {
        return wins;
    }

    public void addWin() {
        this.wins++;
        this.winStreak++;
    }

    public int getLosses() {
        return losses;
    }

    public void addLoss() {
        this.losses++;
        this.winStreak = 0;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void addGamePlayed() {
        this.gamesPlayed++;
    }

    public int getWinStreak() {
        return winStreak;
    }

    public void setWinStreak(int winStreak) {
        this.winStreak = Math.max(0, winStreak);
    }

    public double getKDRatio() {
        if (deaths == 0) {
            return kills;
        }
        return (double) kills / deaths;
    }

    public double getWinRate() {
        if (gamesPlayed == 0) {
            return 0.0;
        }
        return (double) wins / gamesPlayed * 100;
    }
}
