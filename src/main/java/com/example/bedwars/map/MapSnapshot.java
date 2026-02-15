package com.example.bedwars.map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.HashMap;
import java.util.Map;

/**
 * 保存地图区域的方块快照，用于游戏结束后重置地图
 */
public class MapSnapshot {
    private final Map<BlockPosition, BlockData> blocks = new HashMap<>();
    private final Location min;
    private final Location max;
    
    public MapSnapshot(Location min, Location max) {
        this.min = min;
        this.max = max;
    }
    
    /**
     * 保存区域内的所有方块
     */
    public void capture() {
        blocks.clear();
        if (min == null || max == null || min.getWorld() == null) {
            return;
        }
        
        int minX = Math.min(min.getBlockX(), max.getBlockX());
        int minY = Math.min(min.getBlockY(), max.getBlockY());
        int minZ = Math.min(min.getBlockZ(), max.getBlockZ());
        int maxX = Math.max(min.getBlockX(), max.getBlockX());
        int maxY = Math.max(min.getBlockY(), max.getBlockY());
        int maxZ = Math.max(min.getBlockZ(), max.getBlockZ());
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = min.getWorld().getBlockAt(x, y, z);
                    // 只保存非空气方块和重要方块
                    if (block.getType() != Material.AIR || isImportantAirBlock(block)) {
                        blocks.put(new BlockPosition(x, y, z), block.getBlockData().clone());
                    }
                }
            }
        }
    }
    
    /**
     * 恢复保存的所有方块
     */
    public void restore() {
        if (min == null || min.getWorld() == null) {
            return;
        }
        
        // 首先清空区域（设置为空气）
        int minX = Math.min(min.getBlockX(), max.getBlockX());
        int minY = Math.min(min.getBlockY(), max.getBlockY());
        int minZ = Math.min(min.getBlockZ(), max.getBlockZ());
        int maxX = Math.max(min.getBlockX(), max.getBlockX());
        int maxY = Math.max(min.getBlockY(), max.getBlockY());
        int maxZ = Math.max(min.getBlockZ(), max.getBlockZ());
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = min.getWorld().getBlockAt(x, y, z);
                    block.setType(Material.AIR, false);
                }
            }
        }
        
        // 然后恢复保存的方块
        for (Map.Entry<BlockPosition, BlockData> entry : blocks.entrySet()) {
            BlockPosition pos = entry.getKey();
            Block block = min.getWorld().getBlockAt(pos.x, pos.y, pos.z);
            block.setBlockData(entry.getValue(), false);
        }
    }
    
    /**
     * 检查是否为重要的空气方块（例如床的一部分被破坏）
     */
    private boolean isImportantAirBlock(Block block) {
        // 可以在这里添加特殊逻辑
        return false;
    }
    
    public int getBlockCount() {
        return blocks.size();
    }
    
    public boolean isEmpty() {
        return blocks.isEmpty();
    }
    
    /**
     * 方块位置记录
     */
    private static class BlockPosition {
        final int x, y, z;
        
        BlockPosition(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof BlockPosition other)) {
                return false;
            }
            return x == other.x && y == other.y && z == other.z;
        }
        
        @Override
        public int hashCode() {
            return (x * 31 + y) * 31 + z;
        }
    }
}
