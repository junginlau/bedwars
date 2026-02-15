package com.example.bedwars.arena;

import com.example.bedwars.game.GameManager;
import com.example.bedwars.game.GameMode;
import com.example.bedwars.map.MapConfig;
import org.bukkit.entity.Player;

import java.util.*;

public class Arena {
    private final String id;
    private final GameManager gameManager;
    private final MapConfig mapConfig;
    private final GameMode mode;
    private ArenaState state;
    private final Set<UUID> players;
    private final int maxPlayers;
    private final int minPlayers;

    public Arena(String id, GameManager gameManager, MapConfig mapConfig, GameMode mode) {
        this.id = id;
        this.gameManager = gameManager;
        this.mapConfig = mapConfig;
        this.mode = mode;
        this.state = ArenaState.WAITING;
        this.players = new HashSet<>();
        this.maxPlayers = mode.getMaxPlayers();
        this.minPlayers = Math.max(2, mode.getMaxPlayers() / 2);
    }

    public String getId() {
        return id;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public MapConfig getMapConfig() {
        return mapConfig;
    }

    public GameMode getMode() {
        return mode;
    }

    public ArenaState getState() {
        return state;
    }

    public void setState(ArenaState state) {
        this.state = state;
    }

    public Set<UUID> getPlayers() {
        return players;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    public boolean canJoin() {
        return state == ArenaState.WAITING && !isFull();
    }

    public boolean addPlayer(Player player) {
        if (!canJoin()) {
            return false;
        }
        if (players.add(player.getUniqueId())) {
            return gameManager.joinGame(player, mapConfig.getName(), mode);
        }
        return false;
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        gameManager.leave(player);
    }

    public boolean hasPlayer(UUID playerId) {
        return players.contains(playerId);
    }

    public String getDisplayName() {
        return mapConfig.getName() + " - " + mode.getDisplayName();
    }

    public enum ArenaState {
        WAITING,
        STARTING,
        RUNNING,
        ENDING
    }
}
