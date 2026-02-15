package com.example.bedwars.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {
    private final GameManager gameManager;
    private final JavaPlugin plugin;
    private final Map<UUID, Scoreboard> playerBoards = new HashMap<>();
    private BukkitRunnable updateTask;

    public ScoreboardManager(GameManager gameManager, JavaPlugin plugin) {
        this.gameManager = gameManager;
        this.plugin = plugin;
    }

    public void startScoreboard() {
        stopScoreboard();
        
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameManager.isRunning()) {
                    return;
                }
                updateAllScoreboards();
            }
        };
        updateTask.runTaskTimer(plugin, 20L, 20L);
    }

    public void stopScoreboard() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        
        // 清除所有玩家的记分板
        for (UUID uuid : playerBoards.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
        }
        playerBoards.clear();
    }

    public void createScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("bedwars", "dummy", 
            ChatColor.YELLOW + "" + ChatColor.BOLD + "BEDWARS");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        playerBoards.put(player.getUniqueId(), board);
        player.setScoreboard(board);
        
        updateScoreboard(player);
    }

    public void removeScoreboard(Player player) {
        playerBoards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    private void updateAllScoreboards() {
        for (UUID uuid : playerBoards.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                updateScoreboard(player);
            }
        }
    }

    private void updateScoreboard(Player player) {
        Scoreboard board = playerBoards.get(player.getUniqueId());
        if (board == null) {
            return;
        }

        Objective obj = board.getObjective("bedwars");
        if (obj == null) {
            return;
        }

        // 清除旧内容
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        TeamData playerTeam = gameManager.getTeamData(player.getUniqueId());
        
        int line = 15;
        
        // 空行
        obj.getScore(" ").setScore(line--);
        
        // 显示所有队伍状态
        for (TeamColor color : TeamColor.values()) {
            TeamData team = gameManager.getTeams().get(color);
            if (team == null || team.getPlayers().isEmpty()) {
                continue;
            }
            
            StringBuilder teamLine = new StringBuilder();
            teamLine.append(color.getChatColor());
            teamLine.append(color.name());
            teamLine.append(": ");
            
            // 床状态
            if (team.isBedAlive()) {
                teamLine.append(ChatColor.GREEN).append("✓");
            } else {
                teamLine.append(ChatColor.RED).append("✗");
            }
            
            // 在线玩家数
            long onlineCount = team.getPlayers().stream()
                .filter(uuid -> Bukkit.getPlayer(uuid) != null)
                .count();
            teamLine.append(ChatColor.GRAY).append(" [").append(onlineCount).append("]");
            
            // 如果是自己的队伍，添加标记
            if (playerTeam != null && playerTeam.getColor() == color) {
                teamLine.append(ChatColor.YELLOW).append(" ◀");
            }
            
            String lineText = teamLine.toString();
            if (lineText.length() > 40) {
                lineText = lineText.substring(0, 40);
            }
            
            obj.getScore(lineText).setScore(line--);
        }
        
        // 空行
        obj.getScore("  ").setScore(line--);
        
        // 显示玩家升级信息
        if (playerTeam != null) {
            PlayerUpgrades upgrades = gameManager.getPlayerUpgrades(player.getUniqueId());
            TeamUpgrades teamUpgrades = playerTeam.getUpgrades();
            
            obj.getScore(ChatColor.GOLD + "你的升级:").setScore(line--);
            
            // 剑升级
            String swordTier = getSwordName(upgrades.getSwordTier());
            obj.getScore(ChatColor.GRAY + "剑: " + ChatColor.WHITE + swordTier).setScore(line--);
            
            // 护甲升级
            String armorTier = getArmorName(upgrades.getArmorTier());
            obj.getScore(ChatColor.GRAY + "护甲: " + ChatColor.WHITE + armorTier).setScore(line--);
            
            // 空行
            obj.getScore("   ").setScore(line--);
            
            // 团队升级
            obj.getScore(ChatColor.AQUA + "团队升级:").setScore(line--);
            
            if (teamUpgrades.getSharpnessLevel() > 0) {
                obj.getScore(ChatColor.GRAY + "✓ 锋利 I").setScore(line--);
            }
            
            if (teamUpgrades.getProtectionLevel() > 0) {
                obj.getScore(ChatColor.GRAY + "✓ 保护 " + toRoman(teamUpgrades.getProtectionLevel())).setScore(line--);
            }
            
            if (teamUpgrades.getHasteLevel() > 0) {
                obj.getScore(ChatColor.GRAY + "✓ 急迫 " + toRoman(teamUpgrades.getHasteLevel())).setScore(line--);
            }
        }
        
        // 空行
        obj.getScore("    ").setScore(line--);
        
        // 服务器信息
        obj.getScore(ChatColor.GRAY + "mc.example.com").setScore(line--);
    }

    private String getSwordName(int tier) {
        return switch (tier) {
            case 0 -> "木剑";
            case 1 -> "石剑";
            case 2 -> "铁剑";
            case 3 -> "钻石剑";
            default -> "无";
        };
    }

    private String getArmorName(int tier) {
        return switch (tier) {
            case 0 -> "无";
            case 1 -> "锁链";
            case 2 -> "铁";
            case 3 -> "钻石";
            default -> "无";
        };
    }

    private String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            default -> String.valueOf(number);
        };
    }
}
