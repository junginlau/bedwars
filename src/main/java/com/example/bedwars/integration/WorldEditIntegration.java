package com.example.bedwars.integration;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * WorldEdit 集成类，用于获取玩家的选区
 */
public class WorldEditIntegration {
    private static boolean available = false;
    
    /**
     * 检查 WorldEdit 是否可用
     */
    public static boolean isAvailable() {
        return available;
    }
    
    /**
     * 初始化 WorldEdit 集成
     */
    public static void initialize() {
        Plugin worldEdit = Bukkit.getPluginManager().getPlugin("WorldEdit");
        available = worldEdit != null && worldEdit.isEnabled();
        if (available) {
            Bukkit.getLogger().info("[Bedwars] WorldEdit integration enabled");
        }
    }
    
    /**
     * 获取玩家当前的选区
     * @return Location[2] 数组，[0] 为最小点，[1] 为最大点，如果没有选区则返回 null
     */
    public static Location[] getSelection(Player player) {
        if (!available) {
            return null;
        }
        
        try {
            LocalSession session = WorldEdit.getInstance()
                    .getSessionManager()
                    .get(BukkitAdapter.adapt(player));
            
            Region region = session.getSelection(BukkitAdapter.adapt(player.getWorld()));
            
            if (region == null) {
                return null;
            }
            
            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();
            
            Location minLoc = new Location(
                    player.getWorld(),
                    min.getX(),
                    min.getY(),
                    min.getZ()
            );
            
            Location maxLoc = new Location(
                    player.getWorld(),
                    max.getX(),
                    max.getY(),
                    max.getZ()
            );
            
            return new Location[] { minLoc, maxLoc };
            
        } catch (IncompleteRegionException e) {
            return null;
        } catch (Exception e) {
            Bukkit.getLogger().warning("[Bedwars] Failed to get WorldEdit selection: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 检查玩家是否有完整的选区
     */
    public static boolean hasSelection(Player player) {
        return getSelection(player) != null;
    }
}
