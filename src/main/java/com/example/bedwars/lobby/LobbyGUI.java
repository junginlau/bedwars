package com.example.bedwars.lobby;

import com.example.bedwars.game.GameMode;
import com.example.bedwars.map.MapConfig;
import com.example.bedwars.map.MapManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class LobbyGUI {
    public static final String TITLE_MAP_SELECT = "Select Map";
    public static final String TITLE_MODE_SELECT_PREFIX = "Select Mode: ";

    public static void openMapSelector(Player player, MapManager mapManager) {
        Inventory inv = player.getServer().createInventory(player, 27, TITLE_MAP_SELECT);
        int slot = 0;
        for (String mapName : mapManager.getMapNames()) {
            MapConfig map = mapManager.getMap(mapName);
            if (map == null) {
                continue;
            }
            inv.setItem(slot++, item(Material.MAP, ChatColor.YELLOW + mapName, "Click to select mode"));
            if (slot >= inv.getSize()) {
                break;
            }
        }
        player.openInventory(inv);
    }

    public static void openModeSelector(Player player, MapConfig map) {
        Inventory inv = player.getServer().createInventory(player, 27, TITLE_MODE_SELECT_PREFIX + map.getName());
        
        // Show the single mode for this map
        GameMode mode = map.getMode();
        Material material = switch (mode) {
            case SOLO -> Material.IRON_SWORD;
            case DOUBLES -> Material.DIAMOND_SWORD;
            case SQUAD -> Material.NETHERITE_SWORD;
        };
        String desc = mode.getMaxTeams() + " teams, " + mode.getTeamSize() + " per team";
        inv.setItem(13, item(material, ChatColor.GOLD + mode.getDisplayName(), desc));
        
        inv.setItem(26, item(Material.ARROW, ChatColor.GRAY + "Back", ""));
        player.openInventory(inv);
    }

    public static boolean isMapSelector(String title) {
        return TITLE_MAP_SELECT.equals(title);
    }

    public static boolean isModeSelector(String title) {
        return title.startsWith(TITLE_MODE_SELECT_PREFIX);
    }

    public static String extractMapName(String title) {
        return title.substring(TITLE_MODE_SELECT_PREFIX.length()).trim();
    }

    private static ItemStack item(Material material, String name, String lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(Arrays.asList(ChatColor.GRAY + lore));
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
