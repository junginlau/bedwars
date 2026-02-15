package com.example.bedwars.command;

import com.example.bedwars.arena.ArenaManager;
import com.example.bedwars.arena.QueueManager;
import com.example.bedwars.game.GameManager;
import com.example.bedwars.game.GameState;
import com.example.bedwars.lobby.LobbyNPCManager;
import com.example.bedwars.map.MapConfig;
import com.example.bedwars.map.MapManager;
import com.example.bedwars.setup.SetupMenu;
import com.example.bedwars.setup.SetupSessionManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BedwarsCommand implements CommandExecutor, TabCompleter {
    private final GameManager gameManager;
    private final MapManager mapManager;
    private final SetupSessionManager sessions;
    private final LobbyNPCManager npcManager;
    private final ArenaManager arenaManager;
    private final QueueManager queueManager;
    private final org.bukkit.plugin.Plugin plugin;

    public BedwarsCommand(GameManager gameManager, MapManager mapManager, SetupSessionManager sessions,
                          LobbyNPCManager npcManager, ArenaManager arenaManager, QueueManager queueManager) {
        this.gameManager = gameManager;
        this.mapManager = mapManager;
        this.sessions = sessions;
        this.npcManager = npcManager;
        this.arenaManager = arenaManager;
        this.queueManager = queueManager;
        this.plugin = gameManager.getPlugin();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                player.teleport(gameManager.getMainLobbySpawn());
                player.sendMessage(ChatColor.GREEN + "已傳送到 Bedwars 大廳。");
                return true;
            }

            sender.sendMessage(ChatColor.GOLD + "=== Bedwars 指令 ===");
            sender.sendMessage(ChatColor.YELLOW + "/bw queue <地圖> <模式>" + ChatColor.GRAY + " - 加入排隊");
            sender.sendMessage(ChatColor.YELLOW + "/bw quickjoin" + ChatColor.GRAY + " - 快速加入遊戲");
            sender.sendMessage(ChatColor.YELLOW + "/bw arenas" + ChatColor.GRAY + " - 查看房間列表");
            sender.sendMessage(ChatColor.YELLOW + "/bw queues" + ChatColor.GRAY + " - 查看排隊狀態");
            sender.sendMessage(ChatColor.YELLOW + "/bw leave" + ChatColor.GRAY + " - 離開遊戲/排隊");
            if (sender.hasPermission("bedwars.admin")) {
                sender.sendMessage(ChatColor.AQUA + "/bw gui" + ChatColor.GRAY + " - 打開管理界面");
                sender.sendMessage(ChatColor.AQUA + "/bw setlobby" + ChatColor.GRAY + " - 設置主大廳位置");
            }
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "join" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                gameManager.join(player);
                return true;
            }
            case "leave" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                gameManager.leave(player);
                return true;
            }
            case "start" -> {
                if (!sender.hasPermission("bedwars.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                if (gameManager.getState() == GameState.RUNNING) {
                    sender.sendMessage(ChatColor.RED + "Game already running.");
                    return true;
                }
                gameManager.startGame();
                return true;
            }
            case "stop" -> {
                if (!sender.hasPermission("bedwars.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                gameManager.stopGame();
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("bedwars.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                if (gameManager.isRunning()) {
                    sender.sendMessage(ChatColor.RED + "Stop the game before reload.");
                    return true;
                }
                gameManager.reloadActiveMap();
                sender.sendMessage(ChatColor.GREEN + "Map reloaded.");
                return true;
            }
            case "gui" -> {
                if (!sender.hasPermission("bedwars.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                sessions.getSession(player.getUniqueId()).setAwaitingMapName(false);
                SetupMenu.openMapList(player, mapManager);
                return true;
            }
            case "setlobby" -> {
                if (!sender.hasPermission("bedwars.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                Location loc = player.getLocation();
                plugin.getConfig().set("lobby-spawn.world", loc.getWorld().getName());
                plugin.getConfig().set("lobby-spawn.x", loc.getX());
                plugin.getConfig().set("lobby-spawn.y", loc.getY());
                plugin.getConfig().set("lobby-spawn.z", loc.getZ());
                plugin.getConfig().set("lobby-spawn.yaw", (double) loc.getYaw());
                plugin.getConfig().set("lobby-spawn.pitch", (double) loc.getPitch());
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "主大廳位置已設置為你當前位置。");
                player.sendMessage(ChatColor.GRAY + "玩家退出遊戲或遊戲結束後將傳送至此。");
                return true;
            }
            case "language" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }

                if (args.length < 2) {
                    List<String> languages = com.example.bedwars.BedwarsPlugin.instance
                        .getMessageManager().getAvailableLanguages();
                    player.sendMessage(ChatColor.GOLD + "=== 可用語言 ===");
                    for (String lang : languages) {
                        player.sendMessage(ChatColor.YELLOW + "  /bw language " + lang);
                    }
                    return true;
                }

                String language = args[1];
                List<String> available = com.example.bedwars.BedwarsPlugin.instance
                    .getMessageManager().getAvailableLanguages();

                if (!available.contains(language)) {
                    player.sendMessage(ChatColor.RED + "未知的語言: " + language);
                    player.sendMessage(ChatColor.YELLOW + "可用語言: " + String.join(", ", available));
                    return true;
                }

                com.example.bedwars.BedwarsPlugin.instance.getMessageManager().setLanguage(language);
                plugin.getConfig().set("language", language);
                plugin.saveConfig();

                player.sendMessage(ChatColor.GREEN + "語言已更改為: " + language);
                return true;
            }
            case "queue" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /bw queue <map> <solo|doubles|squad>");
                    return true;
                }
                String mapName = args[1];
                com.example.bedwars.game.GameMode mode;
                try {
                    mode = com.example.bedwars.game.GameMode.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "無效的模式。使用: solo, doubles, squad");
                    return true;
                }
                queueManager.joinQueue(player, mapName, mode);
                return true;
            }
            case "leavequeue", "lq" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                queueManager.leaveQueue(player);
                return true;
            }
            case "quickjoin", "qj" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                arenaManager.quickJoin(player);
                return true;
            }
            case "arenas", "list" -> {
                var arenas = arenaManager.getArenas();
                if (arenas.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "目前沒有活動的房間。");
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "=== 房間列表 ===");
                for (var arena : arenas) {
                    String status = switch (arena.getState()) {
                        case WAITING -> ChatColor.GREEN + "等待中";
                        case STARTING -> ChatColor.YELLOW + "開始中";
                        case RUNNING -> ChatColor.RED + "遊戲中";
                        case ENDING -> ChatColor.GRAY + "結束中";
                    };
                    sender.sendMessage(ChatColor.AQUA + arena.getId() + ChatColor.GRAY + " - "
                        + arena.getDisplayName() + " " + status
                        + ChatColor.GRAY + " [" + arena.getPlayerCount() + "/" + arena.getMaxPlayers() + "]");
                }
                return true;
            }
            case "queues" -> {
                var queues = queueManager.getQueues();
                if (queues.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "目前沒有排隊中的玩家。");
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "=== 排隊列表 ===");
                for (var queue : queues) {
                    sender.sendMessage(ChatColor.AQUA + queue.getMapName() + " - " + queue.getMode().getDisplayName()
                        + ChatColor.GRAY + " [" + queue.getPlayerCount() + "/" + queue.getMode().getMaxPlayers() + "]");
                }
                return true;
            }
            default -> {
                sender.sendMessage(ChatColor.YELLOW + "Usage: /bw <join|leave|start|stop|reload|gui|queue|quickjoin|arenas|queues|setlobby|language>");
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("join", "leave", "start", "stop", "reload", "gui",
                "queue", "leavequeue", "quickjoin", "arenas", "queues", "list",
                "setlobby", "language"), args[0]);
        }
        if (args.length == 2) {
            if ("queue".equalsIgnoreCase(args[0])) {
                return filter(new ArrayList<>(mapManager.getMapNames()), args[1]);
            }
            if ("language".equalsIgnoreCase(args[0])) {
                return filter(com.example.bedwars.BedwarsPlugin.instance.getMessageManager().getAvailableLanguages(), args[1]);
            }
        }
        if (args.length == 3 && "queue".equalsIgnoreCase(args[0])) {
            return filter(Arrays.asList("solo", "doubles", "squad"), args[2]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return new ArrayList<>(options);
        }
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.startsWith(lower)) {
                result.add(option);
            }
        }
        return result;
    }
}
