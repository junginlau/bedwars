package com.example.bedwars.listener;

import com.example.bedwars.game.GameManager;
import com.example.bedwars.game.TeamData;
import com.example.bedwars.stats.StatsManager;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GameListener implements Listener {
    private final GameManager gameManager;
    private final StatsManager statsManager;

    public GameListener(GameManager gameManager, StatsManager statsManager) {
        this.gameManager = gameManager;
        this.statsManager = statsManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (gameManager.getTeamData(player.getUniqueId()) != null) {
            if (gameManager.isRunning()) {
                gameManager.handleDisconnect(player);
            } else {
                gameManager.leave(player);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        gameManager.tryReconnect(player);
    }

    @EventHandler
    public void onQueueLeave(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (gameManager.getTeamData(player.getUniqueId()) == null) {
            return;
        }
        if (gameManager.isRunning()) {
            return;
        }
        ItemStack item = event.getItem();
        if (!isLeaveItem(item)) {
            return;
        }
        event.setCancelled(true);
        gameManager.leave(player);
    }

    @EventHandler
    public void onSpectatorCompass(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!gameManager.isRunning()) {
            return;
        }
        ItemStack item = event.getItem();
        if (!isSpectatorCompass(item)) {
            return;
        }
        event.setCancelled(true);
        if (player.getGameMode() == GameMode.SPECTATOR) {
            gameManager.openSpectatorCompass(player);
        }
    }

    @EventHandler
    public void onQueueLeaveDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (isSpectatorCompass(event.getItemDrop().getItemStack()) && player.getGameMode() == GameMode.SPECTATOR) {
            event.setCancelled(true);
            return;
        }
        if (gameManager.getTeamData(player.getUniqueId()) == null || gameManager.isRunning()) {
            return;
        }
        if (isLeaveItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQueueLeaveMove(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (isSpectatorMenu(event.getView().getTitle())) {
            event.setCancelled(true);
            if (player.getGameMode() != GameMode.SPECTATOR) {
                return;
            }
            ItemStack current = event.getCurrentItem();
            if (current != null && current.getType() == Material.PLAYER_HEAD && current.getItemMeta() instanceof org.bukkit.inventory.meta.SkullMeta meta) {
                if (meta.getOwningPlayer() != null && meta.getOwningPlayer().getPlayer() != null) {
                    player.teleport(meta.getOwningPlayer().getPlayer());
                }
                player.closeInventory();
            }
            return;
        }
        if (gameManager.getTeamData(player.getUniqueId()) == null || gameManager.isRunning()) {
            return;
        }
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        int hotbarButton = event.getHotbarButton();

        boolean clickLeave = isLeaveItem(current) || isLeaveItem(cursor);
        boolean lockSlot = isPlayerSlot(event) && event.getSlot() == 8;
        boolean swapIntoSlot = hotbarButton == 8;

        if (clickLeave || lockSlot || swapIntoSlot) {
            event.setCancelled(true);
        }
    }

    private boolean isLeaveItem(ItemStack item) {
        if (item == null || item.getType() != Material.BARRIER) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        String expected = ChatColor.RED + "退出排队";
        return expected.equals(meta.getDisplayName());
    }

    private boolean isSpectatorCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        return (ChatColor.AQUA + "观战指南针").equals(meta.getDisplayName());
    }

    private boolean isSpectatorMenu(String title) {
        return (ChatColor.DARK_AQUA + "观战指南针").equals(title);
    }

    private boolean isPlayerSlot(InventoryClickEvent event) {
        int topSize = event.getView().getTopInventory().getSize();
        return event.getRawSlot() >= topSize;
    }

    @EventHandler
    public void onBedBreak(BlockBreakEvent event) {
        if (!gameManager.isRunning()) {
            return;
        }
        Material type = event.getBlock().getType();
        if (!type.name().endsWith("_BED")) {
            return;
        }
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        TeamData team = gameManager.getTeamByBedLocation(location);
        if (team == null) {
            return;
        }
        if (gameManager.getTeamData(player.getUniqueId()) == null) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You are not in the game.");
            return;
        }
        if (gameManager.getTeamOf(player.getUniqueId()) == team.getColor()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot break your own bed.");
            return;
        }
        if (gameManager.handleBedBreak(player, location)) {
            statsManager.recordBedDestroyed(player);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!gameManager.isRunning()) {
            return;
        }
        TeamData team = gameManager.getTeamData(player.getUniqueId());
        if (team == null) {
            return;
        }
        event.getDrops().clear();
        event.setDroppedExp(0);
        if (!team.isBedAlive()) {
            event.setDeathMessage(ChatColor.RED + player.getName() + " was eliminated.");
        }
        
        // Track stats
        statsManager.recordDeath(player);
        gameManager.resetKillStreak(player.getUniqueId());
        Player killer = player.getKiller();
        if (killer != null && gameManager.getTeamData(killer.getUniqueId()) != null) {
            statsManager.recordKill(killer);
            gameManager.handleKillStreak(killer, player);
        }
        
        gameManager.handlePlayerDeath(player);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!gameManager.isRunning()) {
            return;
        }
        TeamData team = gameManager.getTeamData(player.getUniqueId());
        if (team == null) {
            return;
        }
        if (team.isBedAlive()) {
            event.setRespawnLocation(team.getSpawn());
            player.getServer().getScheduler().runTask(gameManager.getPlugin(), () -> {
                player.setGameMode(GameMode.SURVIVAL);
                gameManager.handleRespawn(player);
                gameManager.applyRespawnProtection(player);
            });
        } else {
            event.setRespawnLocation(gameManager.getLobbySpawn());
            player.getServer().getScheduler().runTask(gameManager.getPlugin(), () -> {
                gameManager.eliminateAfterRespawn(player);
            });
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!gameManager.isRunning()) {
            return;
        }
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        TeamData team = gameManager.getTeamData(victim.getUniqueId());
        if (team == null) {
            return;
        }
        int level = team.getUpgrades().getProtectionLevel();
        if (level <= 0) {
            return;
        }
        double multiplier = 1.0 - (0.04 * level);
        event.setDamage(event.getDamage() * multiplier);
    }
}
