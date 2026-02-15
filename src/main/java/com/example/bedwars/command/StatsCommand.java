package com.example.bedwars.command;

import com.example.bedwars.stats.PlayerStats;
import com.example.bedwars.stats.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StatsCommand implements CommandExecutor, TabCompleter {
    private final StatsManager statsManager;

    public StatsCommand(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "请指定玩家名称。");
                return true;
            }
            showStats(sender, player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "top" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "用法: /bwstats top <kills|beds|wins>");
                    return true;
                }
                showLeaderboard(sender, args[1].toLowerCase());
                return true;
            }
            default -> {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "玩家不在线。");
                    return true;
                }
                showStats(sender, target);
                return true;
            }
        }
    }

    private void showStats(CommandSender sender, Player player) {
        PlayerStats stats = statsManager.getStats(player);
        sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.YELLOW + player.getName() + " 的统计数据 " + ChatColor.GOLD + "==========");
        sender.sendMessage(ChatColor.AQUA + "击杀数: " + ChatColor.WHITE + stats.getKills());
        sender.sendMessage(ChatColor.AQUA + "死亡数: " + ChatColor.WHITE + stats.getDeaths());
        sender.sendMessage(ChatColor.AQUA + "K/D 比率: " + ChatColor.WHITE + String.format("%.2f", stats.getKDRatio()));
        sender.sendMessage(ChatColor.AQUA + "拆床数: " + ChatColor.WHITE + stats.getBedsDestroyed());
        sender.sendMessage(ChatColor.AQUA + "胜场数: " + ChatColor.WHITE + stats.getWins());
        sender.sendMessage(ChatColor.AQUA + "败场数: " + ChatColor.WHITE + stats.getLosses());
        sender.sendMessage(ChatColor.AQUA + "游戏场次: " + ChatColor.WHITE + stats.getGamesPlayed());
        sender.sendMessage(ChatColor.AQUA + "胜率: " + ChatColor.WHITE + String.format("%.1f%%", stats.getWinRate()));
    }

    private void showLeaderboard(CommandSender sender, String type) {
        List<PlayerStats> top;
        String title;
        switch (type) {
            case "kills", "kill" -> {
                top = statsManager.getTopKills(10);
                title = "击杀榜";
            }
            case "beds", "bed" -> {
                top = statsManager.getTopBedsDestroyed(10);
                title = "拆床榜";
            }
            case "wins", "win" -> {
                top = statsManager.getTopWins(10);
                title = "胜场榜";
            }
            default -> {
                sender.sendMessage(ChatColor.RED + "未知类型。使用: kills, beds, wins");
                return;
            }
        }

        sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.YELLOW + title + " (前10名) " + ChatColor.GOLD + "==========");
        int rank = 1;
        for (PlayerStats stats : top) {
            String rankColor = switch (rank) {
                case 1 -> ChatColor.GOLD.toString();
                case 2 -> ChatColor.GRAY.toString();
                case 3 -> ChatColor.DARK_RED.toString();
                default -> ChatColor.WHITE.toString();
            };
            int value = switch (type) {
                case "kills", "kill" -> stats.getKills();
                case "beds", "bed" -> stats.getBedsDestroyed();
                case "wins", "win" -> stats.getWins();
                default -> 0;
            };
            sender.sendMessage(rankColor + "#" + rank + " " + ChatColor.AQUA + stats.getPlayerName() + 
                             ChatColor.WHITE + " - " + ChatColor.YELLOW + value);
            rank++;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(Arrays.asList("top"));
            for (Player player : Bukkit.getOnlinePlayers()) {
                suggestions.add(player.getName());
            }
            return filter(suggestions, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("top")) {
            return filter(Arrays.asList("kills", "beds", "wins"), args[1]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return new ArrayList<>(options);
        }
        String lower = prefix.toLowerCase();
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(lower))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}
