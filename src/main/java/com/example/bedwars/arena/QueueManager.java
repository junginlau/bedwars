package com.example.bedwars.arena;

import com.example.bedwars.game.GameMode;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QueueManager {
    private final JavaPlugin plugin;
    private final ArenaManager arenaManager;
    private final Map<String, Queue> queues;

    public QueueManager(JavaPlugin plugin, ArenaManager arenaManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.queues = new ConcurrentHashMap<>();
    }

    public boolean joinQueue(Player player, String mapName, GameMode mode) {
        String queueId = getQueueId(mapName, mode);
        
        // 检查是否已在队列
        for (Queue queue : queues.values()) {
            if (queue.hasPlayer(player.getUniqueId())) {
                player.sendMessage(ChatColor.YELLOW + "你已在排隊中。");
                return false;
            }
        }

        Queue queue = queues.computeIfAbsent(queueId, id -> new Queue(mapName, mode));
        queue.addPlayer(player);

        var messages = com.example.bedwars.BedwarsPlugin.instance.getMessageManager();
        player.sendMessage(ChatColor.GREEN + "已加入排隊: " + mapName + " - " + mode.getDisplayName());
        player.sendMessage(ChatColor.GRAY + "排隊人數: " + queue.getPlayerCount() + "/" + mode.getMaxPlayers());
        int needed = Math.max(0, mode.getMaxPlayers() - queue.getPlayerCount());
        player.sendMessage(ChatColor.YELLOW + messages.getMessage("queue.need-players", "count", String.valueOf(needed)));
        
        // 检查是否可以开始游戏
        checkQueueStart(queue);
        
        return true;
    }

    public void leaveQueue(Player player) {
        for (Queue queue : queues.values()) {
            if (queue.removePlayer(player.getUniqueId())) {
                player.sendMessage(ChatColor.YELLOW + "已退出排隊。");
                
                // 如果队列空了，移除队列
                if (queue.isEmpty()) {
                    queues.remove(getQueueId(queue.getMapName(), queue.getMode()));
                }
                return;
            }
        }
    }

    public Queue getPlayerQueue(UUID playerId) {
        for (Queue queue : queues.values()) {
            if (queue.hasPlayer(playerId)) {
                return queue;
            }
        }
        return null;
    }

    private void checkQueueStart(Queue queue) {
        if (queue.getPlayerCount() >= queue.getMode().getMaxPlayers()) {
            startGame(queue);
        }
    }

    private void startGame(Queue queue) {
        List<Player> players = queue.getPlayers();
        queues.remove(getQueueId(queue.getMapName(), queue.getMode()));
        
        // 尝试通过 ArenaManager 加入游戏
        for (Player player : players) {
            arenaManager.joinArena(player, queue.getMapName(), queue.getMode());
        }
    }

    private String getQueueId(String mapName, GameMode mode) {
        return mapName + "_" + mode.name();
    }

    public Collection<Queue> getQueues() {
        return queues.values();
    }

    public static class Queue {
        private final String mapName;
        private final GameMode mode;
        private final Set<UUID> players;

        public Queue(String mapName, GameMode mode) {
            this.mapName = mapName;
            this.mode = mode;
            this.players = new HashSet<>();
        }

        public void addPlayer(Player player) {
            players.add(player.getUniqueId());
        }

        public boolean removePlayer(UUID playerId) {
            return players.remove(playerId);
        }

        public boolean hasPlayer(UUID playerId) {
            return players.contains(playerId);
        }

        public int getPlayerCount() {
            return players.size();
        }

        public boolean isEmpty() {
            return players.isEmpty();
        }

        public List<Player> getPlayers() {
            List<Player> result = new ArrayList<>();
            for (UUID uuid : players) {
                Player player = org.bukkit.Bukkit.getPlayer(uuid);
                if (player != null) {
                    result.add(player);
                }
            }
            return result;
        }

        public String getMapName() {
            return mapName;
        }

        public GameMode getMode() {
            return mode;
        }
    }
}
