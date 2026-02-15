package com.example.bedwars.arena;

import com.example.bedwars.game.GameManager;
import com.example.bedwars.game.GameMode;
import com.example.bedwars.map.MapConfig;
import com.example.bedwars.map.MapManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ArenaManager {
    private final JavaPlugin plugin;
    private final MapManager mapManager;
    private final Map<String, Arena> arenas;
    private final Map<UUID, String> playerArenas;
    private int arenaCounter;

    public ArenaManager(JavaPlugin plugin, MapManager mapManager) {
        this.plugin = plugin;
        this.mapManager = mapManager;
        this.arenas = new ConcurrentHashMap<>();
        this.playerArenas = new ConcurrentHashMap<>();
        this.arenaCounter = 0;
    }

    public Arena createArena(MapConfig mapConfig, GameMode mode, GameManager gameManager) {
        String id = "arena_" + (++arenaCounter);
        Arena arena = new Arena(id, gameManager, mapConfig, mode);
        arenas.put(id, arena);
        return arena;
    }

    public Arena findAvailableArena(MapConfig mapConfig, GameMode mode) {
        for (Arena arena : arenas.values()) {
            if (arena.getMapConfig().getName().equals(mapConfig.getName())
                && arena.getMode() == mode
                && arena.canJoin()) {
                return arena;
            }
        }
        return null;
    }

    public Arena findAnyAvailableArena() {
        return arenas.values().stream()
            .filter(Arena::canJoin)
            .min(Comparator.comparingInt(a -> a.getMaxPlayers() - a.getPlayerCount()))
            .orElse(null);
    }

    public boolean joinArena(Player player, String mapName, GameMode mode) {
        MapConfig mapConfig = mapManager.getMap(mapName);
        if (mapConfig == null) {
            player.sendMessage(ChatColor.RED + "地圖不存在。");
            return false;
        }

        if (!mapConfig.isModeEnabled(mode)) {
            player.sendMessage(ChatColor.RED + "此地圖不支援 " + mode.getDisplayName() + " 模式。");
            return false;
        }

        // 检查玩家是否已在房间
        String currentArenaId = playerArenas.get(player.getUniqueId());
        if (currentArenaId != null) {
            Arena currentArena = arenas.get(currentArenaId);
            if (currentArena != null) {
                player.sendMessage(ChatColor.YELLOW + "你已在房間中。");
                return false;
            }
        }

        // 查找或创建房间
        Arena arena = findAvailableArena(mapConfig, mode);
        if (arena == null) {
            // 需要创建新的 GameManager 实例和 Arena
            player.sendMessage(ChatColor.YELLOW + "正在創建新房間...");
            // TODO: 这里需要创建新的游戏实例
            return false;
        }

        if (arena.addPlayer(player)) {
            playerArenas.put(player.getUniqueId(), arena.getId());
            player.sendMessage(ChatColor.GREEN + "加入房間: " + arena.getDisplayName() + " [" + arena.getPlayerCount() + "/" + arena.getMaxPlayers() + "]");
            return true;
        }

        return false;
    }

    public boolean quickJoin(Player player) {
        // 检查玩家是否已在房间
        if (playerArenas.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "你已在房間中。");
            return false;
        }

        Arena arena = findAnyAvailableArena();
        if (arena == null) {
            player.sendMessage(ChatColor.RED + "目前沒有可用的房間。");
            return false;
        }

        if (arena.addPlayer(player)) {
            playerArenas.put(player.getUniqueId(), arena.getId());
            player.sendMessage(ChatColor.GREEN + "快速加入: " + arena.getDisplayName() + " [" + arena.getPlayerCount() + "/" + arena.getMaxPlayers() + "]");
            return true;
        }

        return false;
    }

    public void leaveArena(Player player) {
        String arenaId = playerArenas.remove(player.getUniqueId());
        if (arenaId == null) {
            return;
        }

        Arena arena = arenas.get(arenaId);
        if (arena != null) {
            arena.removePlayer(player);
            
            // 如果房间空了且不在运行，移除房间
            if (arena.getPlayerCount() == 0 && arena.getState() == Arena.ArenaState.WAITING) {
                arenas.remove(arenaId);
            }
        }
    }

    public Arena getPlayerArena(UUID playerId) {
        String arenaId = playerArenas.get(playerId);
        return arenaId != null ? arenas.get(arenaId) : null;
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    public List<Arena> getArenasByMap(String mapName) {
        List<Arena> result = new ArrayList<>();
        for (Arena arena : arenas.values()) {
            if (arena.getMapConfig().getName().equals(mapName)) {
                result.add(arena);
            }
        }
        return result;
    }

    public int getTotalPlayers() {
        return playerArenas.size();
    }

    public void cleanup() {
        arenas.clear();
        playerArenas.clear();
    }
}
