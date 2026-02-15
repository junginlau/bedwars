package com.example.bedwars;

import com.example.bedwars.arena.ArenaManager;
import com.example.bedwars.arena.QueueManager;
import com.example.bedwars.command.BedwarsCommand;
import com.example.bedwars.command.ShopCommand;
import com.example.bedwars.command.StatsCommand;
import com.example.bedwars.game.GameManager;
import com.example.bedwars.language.MessageManager;
import com.example.bedwars.listener.GameListener;
import com.example.bedwars.listener.ShopListener;
import com.example.bedwars.listener.SpecialItemListener;
import com.example.bedwars.lobby.LobbyListener;
import com.example.bedwars.lobby.LobbyLeaderboardManager;
import com.example.bedwars.lobby.LobbyNPCManager;
import com.example.bedwars.map.MapManager;
import com.example.bedwars.setup.SetupListener;
import com.example.bedwars.setup.SetupSessionManager;
import com.example.bedwars.stats.StatsManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class BedwarsPlugin extends JavaPlugin {
    private MapManager mapManager;
    private GameManager gameManager;
    private ArenaManager arenaManager;
    private QueueManager queueManager;
    private SetupSessionManager setupSessions;
    private LobbyNPCManager npcManager;
    private StatsManager statsManager;
    private LobbyLeaderboardManager leaderboardManager;
    private MessageManager messageManager;

    public static BedwarsPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Initialize Message Manager (load language files)
        String language = getConfig().getString("language", "zh_TW");
        messageManager = new MessageManager(this, language);
        
        // Initialize WorldEdit integration
        com.example.bedwars.integration.WorldEditIntegration.initialize();

        mapManager = new MapManager(this);
        mapManager.loadAll();

        gameManager = new GameManager(this, mapManager);
        
        // 初始化 Arena 系统
        arenaManager = new ArenaManager(this, mapManager);
        queueManager = new QueueManager(this, arenaManager);
        
        setupSessions = new SetupSessionManager();
        
        statsManager = new StatsManager(this);
        statsManager.loadAll();
        gameManager.setStatsManager(statsManager);

        leaderboardManager = new LobbyLeaderboardManager(this, statsManager);
        leaderboardManager.load();
        gameManager.setLeaderboardManager(leaderboardManager);
        
        npcManager = new LobbyNPCManager(this);
        npcManager.load();

        PluginCommand bwCommand = getCommand("bw");
        if (bwCommand != null) {
            BedwarsCommand executor = new BedwarsCommand(gameManager, mapManager, setupSessions, npcManager, arenaManager, queueManager);
            bwCommand.setExecutor(executor);
            bwCommand.setTabCompleter(executor);
        }

        PluginCommand shopCommand = getCommand("bwshop");
        if (shopCommand != null) {
            shopCommand.setExecutor(new ShopCommand(gameManager));
        }

        PluginCommand statsCommand = getCommand("bwstats");
        if (statsCommand != null) {
            StatsCommand executor = new StatsCommand(statsManager);
            statsCommand.setExecutor(executor);
            statsCommand.setTabCompleter(executor);
        }

        getServer().getPluginManager().registerEvents(new GameListener(gameManager, statsManager), this);
        getServer().getPluginManager().registerEvents(new ShopListener(gameManager, gameManager.getShopNPCManager()), this);
        getServer().getPluginManager().registerEvents(new SpecialItemListener(gameManager, this), this);
        getServer().getPluginManager().registerEvents(new SetupListener(this, mapManager, setupSessions, gameManager, npcManager, leaderboardManager), this);
        getServer().getPluginManager().registerEvents(new LobbyListener(npcManager, mapManager, gameManager), this);
    }
    
    @Override
    public void onDisable() {
        if (npcManager != null) {
            npcManager.removeAll();
        }
        if (leaderboardManager != null) {
            leaderboardManager.remove();
        }
        if (statsManager != null) {
            statsManager.saveAll();
        }
        if (arenaManager != null) {
            arenaManager.cleanup();
        }
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public GameManager getGameManager() {
        return gameManager;
    }
    
    public MapManager getMapManager() {
        return mapManager;
    }
    
    public ArenaManager getArenaManager() {
        return arenaManager;
    }
    
    public QueueManager getQueueManager() {
        return queueManager;
    }
    
    public SetupSessionManager getSetupSessionManager() {
        return setupSessions;
    }
    
    public LobbyNPCManager getNPCManager() {
        return npcManager;
    }
    
    public StatsManager getStatsManager() {
        return statsManager;
    }
    
    public LobbyLeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }
}
