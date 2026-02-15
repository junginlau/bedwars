package com.example.bedwars.arena;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VoteSystem {
    private final Arena arena;
    private final Map<UUID, String> votes;
    private final Set<String> availableMaps;

    public VoteSystem(Arena arena, Set<String> availableMaps) {
        this.arena = arena;
        this.votes = new ConcurrentHashMap<>();
        this.availableMaps = availableMaps;
    }

    public void vote(Player player, String mapName) {
        if (!arena.hasPlayer(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "你不在這個房間中。");
            return;
        }

        if (!availableMaps.contains(mapName)) {
            player.sendMessage(ChatColor.RED + "無效的地圖。");
            return;
        }

        String oldVote = votes.get(player.getUniqueId());
        votes.put(player.getUniqueId(), mapName);

        if (oldVote != null && !oldVote.equals(mapName)) {
            player.sendMessage(ChatColor.YELLOW + "已將投票從 " + oldVote + " 改為 " + mapName);
        } else {
            player.sendMessage(ChatColor.GREEN + "已投票給: " + mapName);
        }
    }

    public String getWinningMap() {
        if (votes.isEmpty()) {
            return new ArrayList<>(availableMaps).get(new Random().nextInt(availableMaps.size()));
        }

        Map<String, Integer> counts = new HashMap<>();
        for (String mapName : votes.values()) {
            counts.put(mapName, counts.getOrDefault(mapName, 0) + 1);
        }

        return counts.entrySet().stream()
            .max(Comparator.comparingInt(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElse(new ArrayList<>(availableMaps).get(0));
    }

    public Map<String, Integer> getVoteCounts() {
        Map<String, Integer> counts = new HashMap<>();
        for (String mapName : votes.values()) {
            counts.put(mapName, counts.getOrDefault(mapName, 0) + 1);
        }
        return counts;
    }

    public void clear() {
        votes.clear();
    }

    public Set<String> getAvailableMaps() {
        return availableMaps;
    }

    public String getVoteStatus() {
        Map<String, Integer> counts = getVoteCounts();
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.GOLD).append("=== 地圖投票 ===\n");
        
        for (String mapName : availableMaps) {
            int count = counts.getOrDefault(mapName, 0);
            sb.append(ChatColor.YELLOW).append(mapName)
              .append(ChatColor.GRAY).append(": ")
              .append(ChatColor.WHITE).append(count)
              .append(ChatColor.GRAY).append(" 票\n");
        }
        
        return sb.toString();
    }
}
