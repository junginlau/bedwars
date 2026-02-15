package com.example.bedwars.game;

import com.example.bedwars.map.MapConfig;
import com.example.bedwars.map.MapManager;
import com.example.bedwars.map.MapSnapshot;
import com.example.bedwars.config.TeamConfig;
import com.example.bedwars.stats.StatsManager;
import com.example.bedwars.shop.ShopNPCManager;
import com.example.bedwars.lobby.LobbyLeaderboardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GameManager {
    private final JavaPlugin plugin;
    private final MapManager mapManager;
    private StatsManager statsManager;
    private ShopNPCManager shopNPCManager;
    private ScoreboardManager scoreboardManager;
    private LobbyLeaderboardManager leaderboardManager;
    private final Map<TeamColor, TeamData> teams = new EnumMap<>(TeamColor.class);
    private final Map<UUID, TeamColor> playerTeams = new HashMap<>();
    private final java.util.Set<UUID> dummyPlayers = new java.util.HashSet<>();
    private final Map<UUID, PlayerUpgrades> playerUpgrades = new HashMap<>();
    private final List<ResourceGenerator> generators = new ArrayList<>();
    private final Map<TeamColor, List<ResourceGenerator>> baseGenerators = new EnumMap<>(TeamColor.class);
    private final Map<UUID, Integer> killStreaks = new HashMap<>();
    private final Map<UUID, ReconnectData> reconnectingPlayers = new HashMap<>();
    private final List<GameEvent> gameEvents = new ArrayList<>();
    private int diamondTier = 1;
    private int emeraldTier = 1;
    private GameState state = GameState.WAITING;
    private com.example.bedwars.game.GameMode currentMode = com.example.bedwars.game.GameMode.SQUAD;
    private String currentMapName = null;
    private BukkitTask countdownTask;
    private int countdownRemaining;
    private BossBar gameBossBar;
    private BukkitTask gameTimerTask;
    private int gameTimeRemaining;
    private BossBar suddenDeathBossBar;
    private BukkitTask suddenDeathTask;
    private final int reconnectWindowSeconds;
    private final int suddenDeathSeconds;
    private MapSnapshot mapSnapshot;

    public GameManager(JavaPlugin plugin, MapManager mapManager) {
        this.plugin = plugin;
        this.mapManager = mapManager;
        this.shopNPCManager = new ShopNPCManager(this);
        this.scoreboardManager = new ScoreboardManager(this, plugin);
        this.reconnectWindowSeconds = Math.max(10, plugin.getConfig().getInt("reconnect-window-seconds", 60));
        this.suddenDeathSeconds = Math.max(30, plugin.getConfig().getInt("sudden-death-seconds", 120));
        rebuildTeams();
    }

    public void setStatsManager(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    public ShopNPCManager getShopNPCManager() {
        return shopNPCManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public LobbyLeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }

    public void setLeaderboardManager(LobbyLeaderboardManager leaderboardManager) {
        this.leaderboardManager = leaderboardManager;
    }

    public Map<TeamColor, TeamData> getTeams() {
        return teams;
    }

    public PlayerUpgrades getPlayerUpgrades(UUID playerId) {
        return playerUpgrades.computeIfAbsent(playerId, PlayerUpgrades::new);
    }

    public void rebuildTeams() {
        teams.clear();
        MapConfig config = getConfig();
        if (config == null) {
            return;
        }
        for (TeamConfig teamConfig : config.getTeamConfigs().values()) {
            teams.put(teamConfig.getColor(), new TeamData(teamConfig.getColor(), teamConfig));
        }
    }

    private void rebuildTeamsForMode(MapConfig config, com.example.bedwars.game.GameMode mode) {
        teams.clear();
        TeamColor[] colors = TeamColor.values();
        int numTeams = Math.min(mode.getMaxTeams(), colors.length);
        for (int i = 0; i < numTeams; i++) {
            TeamColor color = colors[i];
            TeamConfig teamConfig = config.getTeamConfigs().get(color);
            if (teamConfig != null) {
                teams.put(color, new TeamData(color, teamConfig));
            }
        }
    }

    public GameState getState() {
        return state;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public boolean join(Player player) {
        return joinGame(player, mapManager.getActiveMapName(), com.example.bedwars.game.GameMode.SQUAD);
    }

    public boolean joinGame(Player player, String mapName, com.example.bedwars.game.GameMode mode) {
        MapConfig config = mapManager.getMap(mapName);
        if (config == null) {
            player.sendMessage(ChatColor.RED + "Map not found.");
            return false;
        }
        if (!config.isModeEnabled(mode)) {
            player.sendMessage(ChatColor.RED + "Mode " + mode.getDisplayName() + " is not enabled for this map.");
            return false;
        }
        if (state == GameState.RUNNING || state == GameState.ENDING) {
            player.sendMessage(ChatColor.RED + "Game already running.");
            return false;
        }
        if (playerTeams.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "You are already in the game.");
            return false;
        }
        if (currentMapName == null) {
            currentMapName = mapName;
            currentMode = mode;
            rebuildTeamsForMode(config, mode);
        } else if (!currentMapName.equals(mapName) || currentMode != mode) {
            player.sendMessage(ChatColor.RED + "This game is for " + currentMapName + " (" + currentMode.getDisplayName() + ")");
            return false;
        }
        TeamData team = pickTeam();
        if (team == null) {
            player.sendMessage(ChatColor.RED + "No team slots available.");
            return false;
        }
        if (team.getPlayers().size() >= currentMode.getTeamSize()) {
            team = pickTeam();
            if (team == null || team.getPlayers().size() >= currentMode.getTeamSize()) {
                player.sendMessage(ChatColor.RED + "All teams are full.");
                return false;
            }
        }
        team.getPlayers().add(player.getUniqueId());
        playerTeams.put(player.getUniqueId(), team.getColor());
        player.teleport(config.getLobbySpawn());
        player.setGameMode(org.bukkit.GameMode.ADVENTURE);
        player.getInventory().clear();
        player.getInventory().setItem(8, createLeaveItem());
        player.sendMessage(team.getColor().getChatColor() + "Joined team " + team.getColor().name() + " - " + mode.getDisplayName() + " mode");

        if (state == GameState.WAITING && playerTeams.size() >= getRequiredPlayers()) {
            startCountdown();
        }
        return true;
    }

    private ItemStack createLeaveItem() {
        ItemStack item = new ItemStack(org.bukkit.Material.BARRIER);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "退出排队");
            item.setItemMeta(meta);
        }
        return item;
    }

    public void leave(Player player) {
        TeamColor color = playerTeams.remove(player.getUniqueId());
        if (color != null) {
            TeamData team = teams.get(color);
            if (team != null) {
                team.getPlayers().remove(player.getUniqueId());
            }
        }
        player.getInventory().clear();
        player.teleport(getMainLobbySpawn());
        player.setGameMode(org.bukkit.GameMode.ADVENTURE);
        player.sendMessage(ChatColor.YELLOW + "You left the game.");
        if (gameBossBar != null) {
            gameBossBar.removePlayer(player);
        }
        if (suddenDeathBossBar != null) {
            suddenDeathBossBar.removePlayer(player);
        }
        scoreboardManager.removeScoreboard(player);
        if (state == GameState.COUNTDOWN && playerTeams.size() < getRequiredPlayers()) {
            stopCountdown();
        }
        if (state == GameState.RUNNING) {
            checkWinCondition();
        }
    }

    public void startGame() {
        MapConfig config = getConfig();
        if (config == null) {
            return;
        }
        var messages = com.example.bedwars.BedwarsPlugin.instance.getMessageManager();
        List<String> mapErrors = config.validateSetup(messages);
        if (!mapErrors.isEmpty()) {
            broadcast(ChatColor.RED + messages.getMessage("validation.cannot-start"));
            for (String error : mapErrors) {
                broadcast(ChatColor.RED + "- " + error);
            }
            return;
        }
        if (state == GameState.RUNNING) {
            return;
        }
        if (playerTeams.size() < getRequiredPlayers()) {
            broadcast(ChatColor.RED + "Not enough players for " + currentMode.getDisplayName() + " mode.");
            return;
        }
        stopCountdown();
        killStreaks.clear();
        reconnectingPlayers.clear();
        stopSuddenDeath();
        initializeGameEvents(config);
        diamondTier = 1;
        emeraldTier = 1;
        state = GameState.RUNNING;
        
        // 保存地图快照
        if (config.isArenaBoundsSet()) {
            mapSnapshot = new MapSnapshot(config.getArenaMin(), config.getArenaMax());
            mapSnapshot.capture();
            broadcast(ChatColor.GRAY + "已保存地图快照 (" + mapSnapshot.getBlockCount() + "个方块)");
        } else {
            broadcast(ChatColor.YELLOW + "警告: 未设置地图边界，游戏结束后不会自动恢复地图。");
            mapSnapshot = null;
        }
        
        for (TeamData team : teams.values()) {
            team.setBedAlive(true);
            team.setGeneratorTier(1);
            team.setProtectionLevel(0);
            placeBed(team);  // 放置床方块
            for (UUID uuid : team.getPlayers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) {
                    continue;
                }
                player.teleport(team.getSpawn());
                player.setGameMode(org.bukkit.GameMode.SURVIVAL);
                player.getInventory().clear();
                player.getInventory().addItem(new ItemStack(org.bukkit.Material.WOODEN_SWORD));
                scoreboardManager.createScoreboard(player);
            }
        }
        startGenerators();
        shopNPCManager.spawnShopNPCs();

        // 启动记分板
        scoreboardManager.startScoreboard();
        startGameTimer(config);
        
        broadcast(ChatColor.GREEN + "Game started!");
    }

    public void stopGame() {
        endGame(null);
    }

    public void endGame(TeamColor winner) {
        MapConfig config = getConfig();
        if (config == null) {
            return;
        }
        state = GameState.ENDING;
        stopGenerators();
        shopNPCManager.removeAll();
        scoreboardManager.stopScoreboard();
        stopGameTimer();
        stopSuddenDeath();
        killStreaks.clear();
        reconnectingPlayers.clear();
        gameEvents.clear();
        diamondTier = 1;
        emeraldTier = 1;
        
        // Record stats for all players
        if (statsManager != null) {
            for (UUID uuid : new ArrayList<>(playerTeams.keySet())) {
                if (dummyPlayers.contains(uuid)) {
                    continue;
                }
                Player player = Bukkit.getPlayer(uuid);
                String playerName = player != null ? player.getName() : "Unknown";
                TeamColor playerTeam = playerTeams.get(uuid);
                if (winner != null && playerTeam == winner) {
                    statsManager.recordWin(uuid, playerName);
                } else {
                    statsManager.recordLoss(uuid, playerName);
                }
            }
            statsManager.saveAll();
            if (leaderboardManager != null) {
                leaderboardManager.refreshNow();
            }
        }
        
        if (winner != null) {
            broadcast(winner.getChatColor() + "Team " + winner.name() + " wins!");
        } else {
            broadcast(ChatColor.RED + "Game ended.");
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                // 恢复地图
                if (mapSnapshot != null && !mapSnapshot.isEmpty()) {
                    broadcast(ChatColor.GRAY + "正在恢复地图...");
                    long startTime = System.currentTimeMillis();
                    mapSnapshot.restore();
                    long duration = System.currentTimeMillis() - startTime;
                    broadcast(ChatColor.GREEN + "地图已恢复 (耗时: " + duration + "ms)");
                }
                
                Location mainLobby = getMainLobbySpawn();
                for (UUID uuid : new ArrayList<>(playerTeams.keySet())) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        player.getInventory().clear();
                        player.teleport(mainLobby);
                        player.setGameMode(org.bukkit.GameMode.ADVENTURE);
                        scoreboardManager.removeScoreboard(player);
                    }
                }
                playerTeams.clear();
                dummyPlayers.clear();
                playerUpgrades.clear();
                for (TeamData team : teams.values()) {
                    team.getPlayers().clear();
                    team.getUpgrades().reset();
                }
                teams.clear();
                currentMapName = null;
                currentMode = com.example.bedwars.game.GameMode.SQUAD;
                state = GameState.WAITING;
            }
        }.runTaskLater(plugin, 20L * 5);
    }

    public boolean handleBedBreak(Player breaker, Location bedLocation) {
        TeamData targetTeam = getTeamByBedLocation(bedLocation);
        if (targetTeam == null || !targetTeam.isBedAlive()) {
            return false;
        }
        TeamColor breakerTeam = getTeamOf(breaker.getUniqueId());
        if (breakerTeam == targetTeam.getColor()) {
            breaker.sendMessage(ChatColor.RED + "You cannot break your own bed.");
            return false;
        }
        targetTeam.setBedAlive(false);
        broadcast(targetTeam.getColor().getChatColor() + "Team " + targetTeam.getColor().name() + " bed destroyed!");
        triggerBedDestroyedEffects(targetTeam, bedLocation, breaker);
        checkSuddenDeathStart();
        return true;
    }

    public void handlePlayerDeath(Player player) {
        TeamData team = getTeamData(player.getUniqueId());
        if (team == null) {
            return;
        }
    }

    public void handleRespawn(Player player) {
        TeamData team = getTeamData(player.getUniqueId());
        if (team == null) {
            return;
        }
        player.getInventory().clear();
        
        // 给予升级后的装备
        PlayerUpgrades upgrades = getPlayerUpgrades(player.getUniqueId());
        TeamUpgrades teamUpgrades = team.getUpgrades();
        
        // 剑
        giveSword(player, upgrades.getSwordTier(), teamUpgrades);
        
        // 护甲（自动穿戴）
        giveArmor(player, upgrades.getArmorTier(), teamUpgrades, team.getColor());
        
        // 工具
        if (upgrades.getPickaxeTier() > 0) {
            givePickaxe(player, upgrades.getPickaxeTier());
        }
        if (upgrades.getAxeTier() > 0) {
            giveAxe(player, upgrades.getAxeTier());
        }
        if (upgrades.hasShears()) {
            player.getInventory().addItem(new ItemStack(org.bukkit.Material.SHEARS));
        }
        
        // 应用团队急迫效果
        int hasteLevel = teamUpgrades.getHasteLevel();
        if (hasteLevel > 0) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.FAST_DIGGING, 
                Integer.MAX_VALUE, 
                hasteLevel - 1
            ));
        }
    }
    
    private void giveSword(Player player, int tier, TeamUpgrades teamUpgrades) {
        org.bukkit.Material swordMat = switch (tier) {
            case 1 -> org.bukkit.Material.STONE_SWORD;
            case 2 -> org.bukkit.Material.IRON_SWORD;
            case 3 -> org.bukkit.Material.DIAMOND_SWORD;
            default -> org.bukkit.Material.WOODEN_SWORD;
        };
        
        org.bukkit.inventory.ItemStack sword = new org.bukkit.inventory.ItemStack(swordMat);
        if (teamUpgrades.getSharpnessLevel() > 0) {
            org.bukkit.inventory.meta.ItemMeta meta = sword.getItemMeta();
            meta.addEnchant(org.bukkit.enchantments.Enchantment.DAMAGE_ALL, 1, true);
            sword.setItemMeta(meta);
        }
        
        player.getInventory().addItem(sword);
    }
    
    private void giveArmor(Player player, int tier, TeamUpgrades teamUpgrades, TeamColor color) {
        // 头盔和胸甲始终是团队颜色的皮革
        org.bukkit.inventory.ItemStack helmet = new org.bukkit.inventory.ItemStack(org.bukkit.Material.LEATHER_HELMET);
        org.bukkit.inventory.ItemStack chestplate = new org.bukkit.inventory.ItemStack(org.bukkit.Material.LEATHER_CHESTPLATE);
        
        org.bukkit.inventory.meta.LeatherArmorMeta helmetMeta = (org.bukkit.inventory.meta.LeatherArmorMeta) helmet.getItemMeta();
        org.bukkit.inventory.meta.LeatherArmorMeta chestMeta = (org.bukkit.inventory.meta.LeatherArmorMeta) chestplate.getItemMeta();
        
        helmetMeta.setColor(color.getArmorColor());
        chestMeta.setColor(color.getArmorColor());
        
        int protLevel = teamUpgrades.getProtectionLevel();
        if (protLevel > 0) {
            helmetMeta.addEnchant(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, protLevel, true);
            chestMeta.addEnchant(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, protLevel, true);
        }
        
        helmetMeta.setUnbreakable(true);
        chestMeta.setUnbreakable(true);
        helmet.setItemMeta(helmetMeta);
        chestplate.setItemMeta(chestMeta);
        
        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        
        // 护腿和靴子根据升级等级
        if (tier > 0) {
            org.bukkit.Material bootsMat, legsMat;
            switch (tier) {
                case 1 -> {
                    bootsMat = org.bukkit.Material.CHAINMAIL_BOOTS;
                    legsMat = org.bukkit.Material.CHAINMAIL_LEGGINGS;
                }
                case 2 -> {
                    bootsMat = org.bukkit.Material.IRON_BOOTS;
                    legsMat = org.bukkit.Material.IRON_LEGGINGS;
                }
                case 3 -> {
                    bootsMat = org.bukkit.Material.DIAMOND_BOOTS;
                    legsMat = org.bukkit.Material.DIAMOND_LEGGINGS;
                }
                default -> {
                    return;
                }
            }
            
            org.bukkit.inventory.ItemStack boots = new org.bukkit.inventory.ItemStack(bootsMat);
            org.bukkit.inventory.ItemStack legs = new org.bukkit.inventory.ItemStack(legsMat);
            
            if (protLevel > 0) {
                org.bukkit.inventory.meta.ItemMeta bootsMeta = boots.getItemMeta();
                bootsMeta.addEnchant(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, protLevel, true);
                bootsMeta.setUnbreakable(true);
                boots.setItemMeta(bootsMeta);
                
                org.bukkit.inventory.meta.ItemMeta legsMeta = legs.getItemMeta();
                legsMeta.addEnchant(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, protLevel, true);
                legsMeta.setUnbreakable(true);
                legs.setItemMeta(legsMeta);
            } else {
                org.bukkit.inventory.meta.ItemMeta bootsMeta = boots.getItemMeta();
                bootsMeta.setUnbreakable(true);
                boots.setItemMeta(bootsMeta);
                
                org.bukkit.inventory.meta.ItemMeta legsMeta = legs.getItemMeta();
                legsMeta.setUnbreakable(true);
                legs.setItemMeta(legsMeta);
            }
            
            player.getInventory().setBoots(boots);
            player.getInventory().setLeggings(legs);
        }
    }
    
    private void givePickaxe(Player player, int tier) {
        org.bukkit.Material pickMat = switch (tier) {
            case 1 -> org.bukkit.Material.STONE_PICKAXE;
            case 2 -> org.bukkit.Material.IRON_PICKAXE;
            case 3 -> org.bukkit.Material.DIAMOND_PICKAXE;
            default -> org.bukkit.Material.WOODEN_PICKAXE;
        };
        
        org.bukkit.inventory.ItemStack pick = new org.bukkit.inventory.ItemStack(pickMat);
        org.bukkit.inventory.meta.ItemMeta meta = pick.getItemMeta();
        meta.addEnchant(org.bukkit.enchantments.Enchantment.DIG_SPEED, 1, true);
        meta.setUnbreakable(true);
        pick.setItemMeta(meta);
        
        player.getInventory().addItem(pick);
    }
    
    private void giveAxe(Player player, int tier) {
        org.bukkit.Material axeMat = switch (tier) {
            case 1 -> org.bukkit.Material.STONE_AXE;
            case 2 -> org.bukkit.Material.IRON_AXE;
            case 3 -> org.bukkit.Material.DIAMOND_AXE;
            default -> org.bukkit.Material.WOODEN_AXE;
        };
        
        org.bukkit.inventory.ItemStack axe = new org.bukkit.inventory.ItemStack(axeMat);
        org.bukkit.inventory.meta.ItemMeta meta = axe.getItemMeta();
        meta.addEnchant(org.bukkit.enchantments.Enchantment.DIG_SPEED, 1, true);
        meta.setUnbreakable(true);
        axe.setItemMeta(meta);
        
        player.getInventory().addItem(axe);
    }

    public void eliminateAfterRespawn(Player player) {
        TeamColor color = playerTeams.remove(player.getUniqueId());
        if (color != null) {
            TeamData team = teams.get(color);
            if (team != null) {
                team.getPlayers().remove(player.getUniqueId());
            }
        }
        player.getInventory().clear();
        player.setGameMode(org.bukkit.GameMode.SPECTATOR);
        player.sendMessage(ChatColor.RED + "You are eliminated.");
        giveSpectatorCompass(player);
        if (gameBossBar != null) {
            gameBossBar.removePlayer(player);
        }
        resetKillStreak(player.getUniqueId());
        checkWinCondition();
    }

    public void startCountdown() {
        MapConfig config = getConfig();
        if (config == null) {
            return;
        }
        if (state == GameState.COUNTDOWN) {
            return;
        }
        if (playerTeams.size() < getRequiredPlayers()) {
            return;
        }
        state = GameState.COUNTDOWN;
        countdownRemaining = config.getCountdownSeconds();
        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (playerTeams.size() < getRequiredPlayers()) {
                    stopCountdown();
                    broadcast(ChatColor.RED + "Not enough players. Countdown cancelled.");
                    return;
                }
                if (countdownRemaining <= 0) {
                    startGame();
                    return;
                }
                for (UUID uuid : playerTeams.keySet()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        player.sendTitle(ChatColor.GOLD + "游戏开始倒计时", ChatColor.YELLOW + String.valueOf(countdownRemaining), 0, 20, 0);
                    }
                }
                countdownRemaining--;
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void stopCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        if (state == GameState.COUNTDOWN) {
            state = GameState.WAITING;
        }
        for (UUID uuid : playerTeams.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendTitle("", "", 0, 0, 0);
            }
        }
    }

    private int getRequiredPlayers() {
        return currentMode.getMaxPlayers();
    }

    public int getRequiredPlayersCount() {
        return getRequiredPlayers();
    }

    public int getCurrentPlayerCount() {
        return playerTeams.size();
    }

    public int getDummyCount() {
        return dummyPlayers.size();
    }

    public int addDummyPlayers(int count) {
        if (state == GameState.RUNNING) {
            return 0;
        }
        if (!ensureGameContextForDummy()) {
            return 0;
        }
        int added = 0;
        for (int i = 0; i < count; i++) {
            TeamData team = pickTeam();
            if (team == null) {
                break;
            }
            UUID id = UUID.randomUUID();
            dummyPlayers.add(id);
            playerTeams.put(id, team.getColor());
            team.getPlayers().add(id);
            added++;
        }
        if (state == GameState.WAITING && playerTeams.size() >= getRequiredPlayers()) {
            startCountdown();
        }
        return added;
    }

    public int removeDummyPlayers(int count) {
        if (dummyPlayers.isEmpty()) {
            return 0;
        }
        int removed = 0;
        java.util.Iterator<UUID> it = dummyPlayers.iterator();
        while (it.hasNext() && removed < count) {
            UUID id = it.next();
            it.remove();
            TeamColor color = playerTeams.remove(id);
            if (color != null) {
                TeamData team = teams.get(color);
                if (team != null) {
                    team.getPlayers().remove(id);
                }
            }
            removed++;
        }
        if (state == GameState.COUNTDOWN && playerTeams.size() < getRequiredPlayers()) {
            stopCountdown();
        }
        return removed;
    }

    public void clearDummyPlayers() {
        removeDummyPlayers(dummyPlayers.size());
    }

    private boolean ensureGameContextForDummy() {
        if (currentMapName != null) {
            return true;
        }
        MapConfig config = getConfig();
        if (config == null) {
            return false;
        }
        currentMapName = config.getName();
        currentMode = com.example.bedwars.game.GameMode.SQUAD;
        rebuildTeamsForMode(config, currentMode);
        return true;
    }

    private void startGameTimer(MapConfig config) {
        stopGameTimer();
        gameTimeRemaining = config.getGameDurationSeconds();
        if (gameTimeRemaining <= 0) {
            return;
        }
        gameBossBar = Bukkit.createBossBar(formatGameTimeTitle(gameTimeRemaining), BarColor.BLUE, BarStyle.SOLID);
        for (UUID uuid : playerTeams.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                gameBossBar.addPlayer(player);
            }
        }
        gameBossBar.setProgress(1.0);

        gameTimerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isRunning()) {
                    stopGameTimer();
                    return;
                }
                gameTimeRemaining--;
                if (gameTimeRemaining <= 0) {
                    if (gameBossBar != null) {
                        gameBossBar.setProgress(0.0);
                        gameBossBar.setTitle(ChatColor.RED + "游戏时间结束");
                    }
                    stopGameTimer();
                    return;
                }
                if (gameBossBar != null) {
                    gameBossBar.setTitle(formatGameTimeTitle(gameTimeRemaining));
                    double progress = Math.max(0.0, (double) gameTimeRemaining / config.getGameDurationSeconds());
                    gameBossBar.setProgress(progress);
                }
                checkAndTriggerEvents();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void stopGameTimer() {
        if (gameTimerTask != null) {
            gameTimerTask.cancel();
            gameTimerTask = null;
        }
        if (gameBossBar != null) {
            gameBossBar.removeAll();
            gameBossBar = null;
        }
    }

    public void handleKillStreak(Player killer, Player victim) {
        if (killer == null || victim == null || killer.getUniqueId().equals(victim.getUniqueId())) {
            return;
        }
        resetKillStreak(victim.getUniqueId());
        int newStreak = killStreaks.getOrDefault(killer.getUniqueId(), 0) + 1;
        killStreaks.put(killer.getUniqueId(), newStreak);
        String message = getKillStreakMessage(killer, newStreak);
        if (message != null) {
            broadcast(message);
            playSoundToAll(Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
        }
    }

    public void resetKillStreak(UUID playerId) {
        killStreaks.remove(playerId);
    }

    private String getKillStreakMessage(Player killer, int streak) {
        return switch (streak) {
            case 2 -> ChatColor.GOLD + killer.getName() + ChatColor.YELLOW + " 正在连杀! (2)";
            case 3 -> ChatColor.GOLD + killer.getName() + ChatColor.YELLOW + " 正在疯狂连杀! (3)";
            case 4 -> ChatColor.GOLD + killer.getName() + ChatColor.YELLOW + " 已经无人能挡! (4)";
            case 5 -> ChatColor.RED + "终结者! " + ChatColor.GOLD + killer.getName() + ChatColor.YELLOW + " 连杀 5!";
            case 7 -> ChatColor.DARK_RED + "屠戮之王! " + ChatColor.GOLD + killer.getName() + ChatColor.YELLOW + " 连杀 7!";
            case 10 -> ChatColor.DARK_PURPLE + "神! " + ChatColor.GOLD + killer.getName() + ChatColor.YELLOW + " 连杀 10!";
            default -> null;
        };
    }

    public void handleDisconnect(Player player) {
        if (!isRunning()) {
            leave(player);
            return;
        }
        TeamData team = getTeamData(player.getUniqueId());
        if (team == null) {
            return;
        }
        if (team.isBedAlive()) {
            reconnectingPlayers.put(player.getUniqueId(), new ReconnectData(team.getColor(), currentMapName, currentMode,
                System.currentTimeMillis() + (reconnectWindowSeconds * 1000L)));
        }
        resetKillStreak(player.getUniqueId());
        removePlayerFromTeam(player.getUniqueId());
        scoreboardManager.removeScoreboard(player);
        if (gameBossBar != null) {
            gameBossBar.removePlayer(player);
        }
        if (suddenDeathBossBar != null) {
            suddenDeathBossBar.removePlayer(player);
        }
        checkWinCondition();
    }

    public boolean tryReconnect(Player player) {
        ReconnectData data = reconnectingPlayers.get(player.getUniqueId());
        if (data == null) {
            return false;
        }
        reconnectingPlayers.remove(player.getUniqueId());
        if (!isRunning() || currentMapName == null || !currentMapName.equals(data.mapName) || currentMode != data.mode) {
            return false;
        }
        if (System.currentTimeMillis() > data.expireAt) {
            return false;
        }
        TeamData team = teams.get(data.teamColor);
        if (team == null) {
            return false;
        }
        team.getPlayers().add(player.getUniqueId());
        playerTeams.put(player.getUniqueId(), data.teamColor);
        scoreboardManager.createScoreboard(player);
        if (gameBossBar != null) {
            gameBossBar.addPlayer(player);
        }
        if (suddenDeathBossBar != null) {
            suddenDeathBossBar.addPlayer(player);
        }
        if (team.isBedAlive()) {
            player.teleport(team.getSpawn());
            player.setGameMode(org.bukkit.GameMode.SURVIVAL);
            handleRespawn(player);
            applyRespawnProtection(player);
            player.sendMessage(ChatColor.GREEN + "已成功重连，回到战场。");
        } else {
            player.getInventory().clear();
            player.setGameMode(org.bukkit.GameMode.SPECTATOR);
            giveSpectatorCompass(player);
            player.sendMessage(ChatColor.RED + "床已被摧毁，无法复活。");
        }
        return true;
    }

    private void removePlayerFromTeam(UUID playerId) {
        TeamColor color = playerTeams.remove(playerId);
        if (color == null) {
            return;
        }
        TeamData team = teams.get(color);
        if (team != null) {
            team.getPlayers().remove(playerId);
        }
    }

    private void triggerBedDestroyedEffects(TeamData targetTeam, Location bedLocation, Player breaker) {
        String title = targetTeam.getColor().getChatColor() + "床已被摧毁";
        String subtitle = ChatColor.YELLOW + "破坏者: " + breaker.getName();
        for (UUID uuid : playerTeams.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendTitle(title, subtitle, 10, 40, 10);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.7f, 1.0f);
            }
        }
        if (bedLocation.getWorld() != null) {
            bedLocation.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, bedLocation, 1, 0.2, 0.2, 0.2, 0.0);
            bedLocation.getWorld().playSound(bedLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
        }
    }

    private void checkSuddenDeathStart() {
        if (suddenDeathTask != null || !isRunning()) {
            return;
        }
        boolean anyBedAlive = teams.values().stream()
            .filter(team -> !team.getPlayers().isEmpty() || hasReconnectPlayers(team.getColor()))
            .anyMatch(TeamData::isBedAlive);
        if (!anyBedAlive) {
            startSuddenDeath();
        }
    }

    private void startSuddenDeath() {
        stopSuddenDeath();
        final int[] remaining = { suddenDeathSeconds };
        suddenDeathBossBar = Bukkit.createBossBar(formatSuddenDeathTitle(remaining[0]), BarColor.RED, BarStyle.SOLID);
        for (UUID uuid : playerTeams.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                suddenDeathBossBar.addPlayer(player);
            }
        }
        suddenDeathBossBar.setProgress(1.0);
        broadcast(ChatColor.RED + "所有床已被摧毁! 进入决胜阶段。");
        for (UUID uuid : playerTeams.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendTitle(ChatColor.DARK_RED + "决胜阶段", ChatColor.YELLOW + "剩余时间: " + formatTime(remaining[0]), 10, 40, 10);
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.7f, 0.8f);
            }
        }
        suddenDeathTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isRunning()) {
                    stopSuddenDeath();
                    return;
                }
                remaining[0]--;
                if (remaining[0] <= 0) {
                    stopSuddenDeath();
                    resolveSuddenDeathEnd();
                    return;
                }
                if (suddenDeathBossBar != null) {
                    suddenDeathBossBar.setTitle(formatSuddenDeathTitle(remaining[0]));
                    suddenDeathBossBar.setProgress(Math.max(0.0, (double) remaining[0] / suddenDeathSeconds));
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void resolveSuddenDeathEnd() {
        TeamColor winner = null;
        int bestCount = -1;
        for (TeamData team : teams.values()) {
            int alive = countOnlinePlayers(team);
            if (alive <= 0) {
                continue;
            }
            if (alive > bestCount) {
                bestCount = alive;
                winner = team.getColor();
            } else if (alive == bestCount) {
                winner = null;
            }
        }
        if (winner != null) {
            broadcast(ChatColor.GOLD + "决胜阶段结束，胜利队伍: " + winner.name());
            endGame(winner);
        } else {
            broadcast(ChatColor.RED + "决胜阶段结束，未分出胜负。");
            endGame(null);
        }
    }

    private int countOnlinePlayers(TeamData team) {
        int count = 0;
        for (UUID uuid : team.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline() && player.getGameMode() == org.bukkit.GameMode.SURVIVAL) {
                count++;
            }
        }
        return count;
    }

    private void stopSuddenDeath() {
        if (suddenDeathTask != null) {
            suddenDeathTask.cancel();
            suddenDeathTask = null;
        }
        if (suddenDeathBossBar != null) {
            suddenDeathBossBar.removeAll();
            suddenDeathBossBar = null;
        }
    }

    private String formatSuddenDeathTitle(int seconds) {
        return ChatColor.RED + "决胜阶段: " + ChatColor.YELLOW + formatTime(seconds);
    }

    private String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    public void giveSpectatorCompass(Player player) {
        ItemStack compass = new ItemStack(org.bukkit.Material.COMPASS);
        org.bukkit.inventory.meta.ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "观战指南针");
            compass.setItemMeta(meta);
        }
        player.getInventory().setItem(0, compass);
    }

    public void openSpectatorCompass(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 27, ChatColor.DARK_AQUA + "观战指南针");
        int slot = 0;
        for (TeamData team : teams.values()) {
            for (UUID uuid : team.getPlayers()) {
                Player target = Bukkit.getPlayer(uuid);
                if (target == null || !target.isOnline()) {
                    continue;
                }
                ItemStack head = new ItemStack(org.bukkit.Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                if (meta != null) {
                    meta.setOwningPlayer(target);
                    meta.setDisplayName(team.getColor().getChatColor() + target.getName());
                    head.setItemMeta(meta);
                }
                inventory.setItem(slot++, head);
                if (slot >= inventory.getSize()) {
                    break;
                }
            }
            if (slot >= inventory.getSize()) {
                break;
            }
        }
        player.openInventory(inventory);
    }

    public void applyRespawnProtection(Player player) {
        player.setInvulnerable(true);
        player.sendMessage(ChatColor.GREEN + "你获得了3秒无敌时间！");
        player.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.setInvulnerable(false);
                player.sendMessage(ChatColor.YELLOW + "无敌时间结束！");
            }
        }, 60L);
    }

    private void playSoundToAll(Sound sound, float volume, float pitch) {
        for (UUID uuid : playerTeams.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.playSound(player.getLocation(), sound, volume, pitch);
            }
        }
    }

    private String formatGameTimeTitle(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return ChatColor.AQUA + "游戏剩余时间: " + String.format("%02d:%02d", mins, secs);
    }

    public void reloadActiveMap() {
        if (state == GameState.RUNNING) {
            return;
        }
        mapManager.loadAll();
        rebuildTeams();
    }

    public boolean isRunning() {
        return state == GameState.RUNNING;
    }

    public TeamColor getTeamOf(UUID uuid) {
        return playerTeams.get(uuid);
    }

    public TeamData getTeamData(UUID uuid) {
        TeamColor color = playerTeams.get(uuid);
        return color != null ? teams.get(color) : null;
    }

    public TeamData getTeamByBedLocation(Location location) {
        for (TeamData team : teams.values()) {
            Location bed = team.getBedLocation();
            if (!bed.getWorld().equals(location.getWorld())) {
                continue;
            }
            int dx = Math.abs(bed.getBlockX() - location.getBlockX());
            int dy = Math.abs(bed.getBlockY() - location.getBlockY());
            int dz = Math.abs(bed.getBlockZ() - location.getBlockZ());
            if (dy == 0 && ((dx == 0 && dz == 0) || (dx + dz == 1))) {
                return team;
            }
        }
        return null;
    }

    public Location getLobbySpawn() {
        MapConfig config = getConfig();
        return config != null ? config.getLobbySpawn() : locationFallback();
    }

    public void checkWinCondition() {
        List<TeamData> alive = new ArrayList<>();
        for (TeamData team : teams.values()) {
            if (!team.getPlayers().isEmpty() || hasReconnectPlayers(team.getColor())) {
                alive.add(team);
            }
        }
        if (alive.size() == 1 && state == GameState.RUNNING) {
            endGame(alive.get(0).getColor());
        }
        if (alive.isEmpty() && state == GameState.RUNNING) {
            endGame(null);
        }
    }

    private boolean hasReconnectPlayers(TeamColor color) {
        for (ReconnectData data : reconnectingPlayers.values()) {
            if (data.teamColor == color && System.currentTimeMillis() <= data.expireAt) {
                return true;
            }
        }
        return false;
    }

    private TeamData pickTeam() {
        TeamData chosen = null;
        for (TeamData team : teams.values()) {
            if (team.getPlayers().size() >= currentMode.getTeamSize()) {
                continue;
            }
            if (chosen == null || team.getPlayers().size() < chosen.getPlayers().size()) {
                chosen = team;
            }
        }
        return chosen;
    }

    public boolean takeCurrency(Player player, ResourceType type, int amount) {
        ItemStack cost = new ItemStack(type.getMaterial(), amount);
        if (!player.getInventory().containsAtLeast(cost, amount)) {
            return false;
        }
        Map<Integer, ItemStack> leftover = player.getInventory().removeItem(cost);
        return leftover.isEmpty();
    }

    public int getGeneratorInterval(ResourceType type, int tier) {
        if (type == ResourceType.IRON) {
            return switch (tier) {
                case 1 -> 40;
                case 2 -> 30;
                default -> 20;
            };
        }
        if (type == ResourceType.GOLD) {
            return switch (tier) {
                case 1 -> 100;
                case 2 -> 80;
                default -> 60;
            };
        }
        if (type == ResourceType.DIAMOND) {
            return switch (tier) {
                case 1 -> 240;
                case 2 -> 180;
                default -> 120;
            };
        }
        if (type == ResourceType.EMERALD) {
            return switch (tier) {
                case 1 -> 600;
                case 2 -> 480;
                default -> 360;
            };
        }
        return 600;
    }

    public void upgradeGeneratorTier(TeamData team) {
        if (team.getGeneratorTier() >= 3) {
            return;
        }
        team.setGeneratorTier(team.getGeneratorTier() + 1);
        List<ResourceGenerator> list = baseGenerators.get(team.getColor());
        if (list == null) {
            return;
        }
        for (ResourceGenerator generator : list) {
            int interval = getGeneratorInterval(generator.getType(), team.getGeneratorTier());
            generator.setIntervalTicks(interval);
            generator.start(plugin, this::isRunning);
        }
    }

    public void upgradeProtection(TeamData team) {
        if (team.getProtectionLevel() >= 4) {
            return;
        }
        team.setProtectionLevel(team.getProtectionLevel() + 1);
    }

    public void updateGeneratorSpeed(TeamData team) {
        int tier = team.getUpgrades().getResourceGenLevel();
        List<ResourceGenerator> list = baseGenerators.get(team.getColor());
        if (list != null) {
            for (ResourceGenerator generator : list) {
                int interval = getGeneratorInterval(generator.getType(), tier);
                generator.setIntervalTicks(interval);
                generator.stop();
                generator.start(plugin, this::isRunning);
            }
        }
    }

    private void startGenerators() {
        MapConfig config = getConfig();
        if (config == null) {
            return;
        }
        stopGenerators();
        baseGenerators.clear();
        for (TeamData team : teams.values()) {
            List<ResourceGenerator> list = new ArrayList<>();
            int tier = team.getUpgrades().getResourceGenLevel();
            ResourceGenerator ironGen = new ResourceGenerator(ResourceType.IRON, team.getBaseGenerator(), getGeneratorInterval(ResourceType.IRON, tier), 1);
            ResourceGenerator goldGen = new ResourceGenerator(ResourceType.GOLD, team.getBaseGenerator(), getGeneratorInterval(ResourceType.GOLD, tier), 1);
            list.add(ironGen);
            list.add(goldGen);
            baseGenerators.put(team.getColor(), list);
            generators.addAll(list);
        }
        for (Location location : config.getDiamondGenerators()) {
            generators.add(new ResourceGenerator(ResourceType.DIAMOND, location, getGeneratorInterval(ResourceType.DIAMOND, diamondTier), 1));
        }
        for (Location location : config.getEmeraldGenerators()) {
            generators.add(new ResourceGenerator(ResourceType.EMERALD, location, getGeneratorInterval(ResourceType.EMERALD, emeraldTier), 1));
        }
        for (ResourceGenerator generator : generators) {
            generator.start(plugin, this::isRunning);
        }
    }

    private void stopGenerators() {
        for (ResourceGenerator generator : generators) {
            generator.stop();
        }
        generators.clear();
        baseGenerators.clear();
    }

    private void broadcast(String message) {
        Bukkit.getServer().broadcastMessage(message);
    }

    private MapConfig getConfig() {
        return mapManager.getActiveMap();
    }

    private Location locationFallback() {
        return Bukkit.getWorlds().get(0).getSpawnLocation();
    }

    public Location getMainLobbySpawn() {
        org.bukkit.configuration.ConfigurationSection lobbySection = plugin.getConfig().getConfigurationSection("lobby-spawn");
        if (lobbySection != null) {
            String worldName = lobbySection.getString("world", "world");
            org.bukkit.World world = Bukkit.getWorld(worldName);
            if (world != null) {
                double x = lobbySection.getDouble("x", 0.5);
                double y = lobbySection.getDouble("y", 65.0);
                double z = lobbySection.getDouble("z", 0.5);
                float yaw = (float) lobbySection.getDouble("yaw", 0.0);
                float pitch = (float) lobbySection.getDouble("pitch", 0.0);
                return new Location(world, x, y, z, yaw, pitch);
            }
        }
        // 如果配置不存在或世界不存在，返回主世界出生点
        return locationFallback();
    }

    private void initializeGameEvents(MapConfig config) {
        gameEvents.clear();
        int gameDuration = config.getGameDurationSeconds();
        
        // 资源升级阶段 (根据游戏时长动态调整)
        int diamondUpgrade1 = Math.min(180, gameDuration / 4);  // 3分钟或游戏1/4时
        int diamondUpgrade2 = Math.min(360, gameDuration / 2);  // 6分钟或游戏1/2时
        int emeraldUpgrade1 = Math.min(240, gameDuration / 3);  // 4分钟或游戏1/3时
        int emeraldUpgrade2 = Math.min(480, gameDuration * 2 / 3); // 8分钟或游戏2/3时
        
        if (diamondUpgrade1 < gameDuration) {
            gameEvents.add(GameEvent.createDiamondUpgrade(diamondUpgrade1));
        }
        if (diamondUpgrade2 < gameDuration && diamondUpgrade2 > diamondUpgrade1) {
            gameEvents.add(GameEvent.createDiamondUpgradeMax(diamondUpgrade2));
        }
        if (emeraldUpgrade1 < gameDuration) {
            gameEvents.add(GameEvent.createEmeraldUpgrade(emeraldUpgrade1));
        }
        if (emeraldUpgrade2 < gameDuration && emeraldUpgrade2 > emeraldUpgrade1) {
            gameEvents.add(GameEvent.createEmeraldUpgradeMax(emeraldUpgrade2));
        }
        
        // 床摧毁阶段 (游戏80%时)
        int bedDestroyTime = (int) (gameDuration * 0.8);
        if (bedDestroyTime > 300 && bedDestroyTime < gameDuration - 60) {
            gameEvents.add(GameEvent.createBedDestroy(bedDestroyTime));
        }
        
        // 游戏结束警告 (最后30秒)
        if (gameDuration > 60) {
            gameEvents.add(GameEvent.createGameEnd(30));
        }
        
        // 按时间排序
        gameEvents.sort((a, b) -> Integer.compare(b.getTriggerAtSecond(), a.getTriggerAtSecond()));
    }

    private void checkAndTriggerEvents() {
        if (gameEvents.isEmpty()) {
            return;
        }
        
        int elapsed = getConfig().getGameDurationSeconds() - gameTimeRemaining;
        
        for (GameEvent event : new ArrayList<>(gameEvents)) {
            if (!event.isTriggered() && elapsed >= event.getTriggerAtSecond()) {
                triggerEvent(event);
                event.setTriggered(true);
            }
        }
    }

    private void triggerEvent(GameEvent event) {
        // 通知所有玩家
        for (UUID uuid : playerTeams.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendTitle(event.getTitle(), event.getSubtitle(), 10, 40, 10);
                if (event.getSound() != null) {
                    player.playSound(player.getLocation(), event.getSound(), 1.0f, 1.0f);
                }
            }
        }
        
        String broadcastMsg = ChatColor.YELLOW + "▶ " + event.getType().getDisplayName() + " " + event.getSubtitle();
        broadcast(broadcastMsg);
        
        // 执行事件效果
        switch (event.getType()) {
            case DIAMOND_UPGRADE -> {
                diamondTier = 2;
                upgradeDiamondGenerators();
            }
            case DIAMOND_UPGRADE_MAX -> {
                diamondTier = 3;
                upgradeDiamondGenerators();
            }
            case EMERALD_UPGRADE -> {
                emeraldTier = 2;
                upgradeEmeraldGenerators();
            }
            case EMERALD_UPGRADE_MAX -> {
                emeraldTier = 3;
                upgradeEmeraldGenerators();
            }
            case BED_DESTROY -> destroyAllBeds();
            case GAME_END -> {
                // 加时赛：30秒后结算
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (isRunning()) {
                            resolveSuddenDeathEnd();
                        }
                    }
                }.runTaskLater(plugin, 20L * 30);
            }
        }
    }

    private void upgradeDiamondGenerators() {
        for (ResourceGenerator gen : generators) {
            if (gen.getType() == ResourceType.DIAMOND) {
                gen.setIntervalTicks(getGeneratorInterval(ResourceType.DIAMOND, diamondTier));
                gen.stop();
                gen.start(plugin, this::isRunning);
            }
        }
    }

    private void upgradeEmeraldGenerators() {
        for (ResourceGenerator gen : generators) {
            if (gen.getType() == ResourceType.EMERALD) {
                gen.setIntervalTicks(getGeneratorInterval(ResourceType.EMERALD, emeraldTier));
                gen.stop();
                gen.start(plugin, this::isRunning);
            }
        }
    }

    private void placeBed(TeamData team) {
        Location bedLoc = team.getBedLocation();
        if (bedLoc == null || bedLoc.getWorld() == null) {
            return;
        }
        
        // 根据队伍颜色选择床的颜色
        org.bukkit.Material bedMaterial = switch (team.getColor()) {
            case RED -> org.bukkit.Material.RED_BED;
            case BLUE -> org.bukkit.Material.BLUE_BED;
            case GREEN -> org.bukkit.Material.GREEN_BED;
            case YELLOW -> org.bukkit.Material.YELLOW_BED;
            case AQUA -> org.bukkit.Material.CYAN_BED;
            case WHITE -> org.bukkit.Material.WHITE_BED;
            case PINK -> org.bukkit.Material.PINK_BED;
            case GRAY -> org.bukkit.Material.GRAY_BED;
        };
        
        // 放置床的底部
        bedLoc.getBlock().setType(bedMaterial);
        
        // 设置床的方向 (床需要两个方块)
        org.bukkit.block.data.type.Bed bedData = (org.bukkit.block.data.type.Bed) bedLoc.getBlock().getBlockData();
        bedData.setPart(org.bukkit.block.data.type.Bed.Part.FOOT);
        bedLoc.getBlock().setBlockData(bedData);
        
        // 放置床的头部
        org.bukkit.block.BlockFace facing = bedData.getFacing();
        Location headLoc = bedLoc.getBlock().getRelative(facing).getLocation();
        headLoc.getBlock().setType(bedMaterial);
        org.bukkit.block.data.type.Bed headData = (org.bukkit.block.data.type.Bed) headLoc.getBlock().getBlockData();
        headData.setPart(org.bukkit.block.data.type.Bed.Part.HEAD);
        headData.setFacing(facing);
        headLoc.getBlock().setBlockData(headData);
    }

    private void destroyAllBeds() {
        for (TeamData team : teams.values()) {
            if (team.isBedAlive()) {
                team.setBedAlive(false);
                Location bedLoc = team.getBedLocation();
                if (bedLoc.getWorld() != null) {
                    org.bukkit.block.Block bedBlock = bedLoc.getBlock();
                    if (bedBlock.getBlockData() instanceof org.bukkit.block.data.type.Bed bedData) {
                        org.bukkit.block.BlockFace facing = bedData.getFacing();
                        Location otherLoc = bedData.getPart() == org.bukkit.block.data.type.Bed.Part.FOOT
                            ? bedBlock.getRelative(facing).getLocation()
                            : bedBlock.getRelative(facing.getOppositeFace()).getLocation();
                        otherLoc.getBlock().setType(org.bukkit.Material.AIR);
                    }
                    bedBlock.setType(org.bukkit.Material.AIR);
                    bedLoc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, bedLoc, 3, 0.5, 0.5, 0.5, 0.0);
                }
            }
        }
        broadcast(ChatColor.RED + "所有床已被摧毁！無法復活。");
    }

    private static class ReconnectData {
        private final TeamColor teamColor;
        private final String mapName;
        private final com.example.bedwars.game.GameMode mode;
        private final long expireAt;

        private ReconnectData(TeamColor teamColor, String mapName, com.example.bedwars.game.GameMode mode, long expireAt) {
            this.teamColor = teamColor;
            this.mapName = mapName;
            this.mode = mode;
            this.expireAt = expireAt;
        }
    }
}
