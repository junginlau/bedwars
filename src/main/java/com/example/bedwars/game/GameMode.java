package com.example.bedwars.game;

public enum GameMode {
    SOLO(1, "Solo", 8),
    DOUBLES(2, "Doubles", 8),
    SQUAD(4, "Squad", 4);

    private final int teamSize;
    private final String displayName;
    private final int maxTeams;

    GameMode(int teamSize, String displayName, int maxTeams) {
        this.teamSize = teamSize;
        this.displayName = displayName;
        this.maxTeams = maxTeams;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxTeams() {
        return maxTeams;
    }

    public int getMaxPlayers() {
        return teamSize * maxTeams;
    }

    public static GameMode fromName(String name) {
        for (GameMode mode : values()) {
            if (mode.name().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return null;
    }
}
