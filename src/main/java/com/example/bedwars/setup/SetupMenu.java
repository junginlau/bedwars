package com.example.bedwars.setup;

import com.example.bedwars.config.TeamConfig;
import com.example.bedwars.game.TeamColor;
import com.example.bedwars.map.MapConfig;
import com.example.bedwars.map.MapManager;
import com.example.bedwars.lobby.LobbyNPC;
import com.example.bedwars.lobby.LobbyNPCManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetupMenu {
    public static final String TITLE_MAPS = "Bedwars 地圖";
    public static final String TITLE_EDITOR_PREFIX = "地圖編輯器: ";
    public static final String TITLE_TEAM_SELECT_PREFIX = "隊伍選擇: ";
    public static final String TITLE_TEAM_EDITOR_PREFIX = "隊伍編輯器: ";
    public static final String TITLE_GENERATORS_PREFIX = "生成器: ";
    public static final String TITLE_MODES_PREFIX = "遊戲模式: ";
    public static final String TITLE_NPC_MANAGER = "遊戲大廳NPC";
    public static final String TITLE_NPC_EDITOR_PREFIX = "NPC 編輯器: ";
    public static final String TITLE_LEADERBOARD = "遊戲大廳排行榜";
    public static final String TITLE_DUMMY = "機器人測試";

    public static void openMapList(Player player, MapManager mapManager) {
        Inventory inv = player.getServer().createInventory(player, 54, TITLE_MAPS);
        fillBorders(inv);

        inv.setItem(10, item(Material.LIME_WOOL, ChatColor.GREEN + "創建地圖", "點擊並輸入名稱"));
        inv.setItem(12, item(Material.VILLAGER_SPAWN_EGG, ChatColor.GOLD + "遊戲大廳NPC", "遊戲外NPC"));
        inv.setItem(14, item(Material.ARMOR_STAND, ChatColor.AQUA + "遊戲大廳排行榜", "全息圖管理"));
        inv.setItem(16, item(Material.ARMOR_STAND, ChatColor.YELLOW + "機器人測試", "填充開始位置"));
        inv.setItem(49, item(Material.BARRIER, ChatColor.RED + "關閉", ""));

        int slot = 28;
        for (String name : mapManager.getMapNames()) {
            MapConfig map = mapManager.getMap(name);
            boolean active = map != null && map.getName().equalsIgnoreCase(mapManager.getActiveMapName());
            String lore = active ? "活躍" : "點擊編輯";
            inv.setItem(slot++, item(Material.PAPER, ChatColor.YELLOW + name, lore));
            if (slot == 34) {
                slot = 37;
            }
            if (slot >= inv.getSize() - 1) {
                break;
            }
        }
        player.openInventory(inv);
    }

    public static void openEditor(Player player, MapConfig map) {
        Inventory inv = player.getServer().createInventory(player, 54, TITLE_EDITOR_PREFIX + map.getName());
        fillBorders(inv);

        inv.setItem(10, item(Material.BEACON, ChatColor.AQUA + "設置遊戲大廳出生點", "使用當前位置"));
        inv.setItem(12, item(Material.RED_BED, ChatColor.RED + "隊伍設置", "配置隊伍位置"));
        inv.setItem(14, item(Material.DIAMOND, ChatColor.AQUA + "生成器", "管理中立生成器"));
        inv.setItem(16, item(Material.GRASS_BLOCK, ChatColor.GREEN + "設置世界", "使用當前世界"));
        
        // Arena Bounds - Manual/WorldEdit
        String boundsText = map.isArenaBoundsSet() ? ChatColor.GREEN + "地圖邊界 ✓" : ChatColor.YELLOW + "地圖邊界";
        inv.setItem(20, item(Material.BARRIER, boundsText, "點擊手動設置"));
        
        // WorldEdit Selection
        boolean weAvailable = com.example.bedwars.integration.WorldEditIntegration.isAvailable();
        if (weAvailable) {
            inv.setItem(22, item(Material.WOODEN_AXE, ChatColor.GOLD + "WorldEdit選區", "使用 //wand 然後輸入 'ok'"));
        }
        
        // Game Mode Selection (4队/8队)
        String modeText = (map.getMode().getMaxTeams() == 4 ? ChatColor.GREEN + "4隊模式 ✓" : ChatColor.GREEN + "8隊模式 ✓");
        inv.setItem(24, item(Material.COMPARATOR, modeText, "左鍵: 4隊 | 右鍵: 8隊"));
        
        inv.setItem(30, item(Material.WRITABLE_BOOK, ChatColor.YELLOW + "保存", "寫入文件"));
        inv.setItem(32, item(Material.NETHER_STAR, ChatColor.GOLD + "設為活躍", "使用此地圖"));
        inv.setItem(34, item(Material.NAME_TAG, ChatColor.YELLOW + "重命名地圖", "輸入新名稱"));
        inv.setItem(38, item(Material.BARRIER, ChatColor.RED + "刪除地圖", "移除地圖文件"));
        inv.setItem(49, item(Material.ARROW, ChatColor.GRAY + "返回", ""));
        player.openInventory(inv);
    }

    public static void openTeamSelect(Player player, MapConfig map) {
        // 根据地图模式决定显示4队还是8队
        boolean is8Teams = map.getMode().getMaxTeams() == 8;
        
        Inventory inv = player.getServer().createInventory(player, 54, TITLE_TEAM_SELECT_PREFIX + map.getName());
        fillBorders(inv);
        
        if (is8Teams) {
            // 8队模式：两行显示
            inv.setItem(19, item(Material.RED_WOOL, ChatColor.RED + "RED", ""));
            inv.setItem(21, item(Material.GREEN_WOOL, ChatColor.GREEN + "GREEN", ""));
            inv.setItem(23, item(Material.BLUE_WOOL, ChatColor.BLUE + "BLUE", ""));
            inv.setItem(25, item(Material.YELLOW_WOOL, ChatColor.YELLOW + "YELLOW", ""));
            inv.setItem(28, item(Material.CYAN_WOOL, ChatColor.AQUA + "AQUA", ""));
            inv.setItem(30, item(Material.PINK_WOOL, ChatColor.LIGHT_PURPLE + "PINK", ""));
            inv.setItem(32, item(Material.PURPLE_WOOL, ChatColor.DARK_PURPLE + "PURPLE", ""));
            inv.setItem(34, item(Material.LIGHT_GRAY_WOOL, ChatColor.GRAY + "GRAY", ""));
        } else {
            // 4队模式：一行显示
            inv.setItem(19, item(Material.RED_WOOL, ChatColor.RED + "RED", ""));
            inv.setItem(21, item(Material.BLUE_WOOL, ChatColor.BLUE + "BLUE", ""));
            inv.setItem(23, item(Material.GREEN_WOOL, ChatColor.GREEN + "GREEN", ""));
            inv.setItem(25, item(Material.YELLOW_WOOL, ChatColor.YELLOW + "YELLOW", ""));
        }
        
        inv.setItem(49, item(Material.ARROW, ChatColor.GRAY + "Back", ""));
        player.openInventory(inv);
    }

    public static void openTeamEditor(Player player, MapConfig map, TeamColor team) {
        Inventory inv = player.getServer().createInventory(player, 54, TITLE_TEAM_EDITOR_PREFIX + map.getName() + " " + team.name());
        fillBorders(inv);
        inv.setItem(10, item(Material.ENDER_PEARL, ChatColor.AQUA + "設置出生點", "使用當前位置"));
        inv.setItem(12, item(Material.RED_BED, ChatColor.RED + "設置床", "站在床塊上"));
        inv.setItem(14, item(Material.HOPPER, ChatColor.GOLD + "設置基地生成器", "使用當前位置"));
        inv.setItem(16, item(Material.VILLAGER_SPAWN_EGG, ChatColor.GOLD + "設置商店NPC", "使用當前位置"));
        inv.setItem(28, item(Material.BARRIER, ChatColor.RED + "清除出生點", "取消設置出生點"));
        inv.setItem(30, item(Material.BARRIER, ChatColor.RED + "清除床", "取消設置床"));
        inv.setItem(32, item(Material.BARRIER, ChatColor.RED + "清除生成器", "取消設置基地生成器"));
        inv.setItem(34, item(Material.BARRIER, ChatColor.RED + "清除商店NPC", "取消設置商店NPC"));
        inv.setItem(40, item(Material.CHEST, ChatColor.YELLOW + "獲得隊伍工具", "使用物品放置"));
        inv.setItem(49, item(Material.ARROW, ChatColor.GRAY + "返回", ""));
        player.openInventory(inv);
    }

    public static void openGeneratorEditor(Player player, MapConfig map) {
        Inventory inv = player.getServer().createInventory(player, 54, TITLE_GENERATORS_PREFIX + map.getName());
        fillBorders(inv);
        inv.setItem(10, item(Material.DIAMOND, ChatColor.AQUA + "添加鑽石生成器", "使用當前區塊"));
        inv.setItem(12, item(Material.EMERALD, ChatColor.GREEN + "添加綠寶石生成器", "使用當前區塊"));
        inv.setItem(14, item(Material.BARRIER, ChatColor.YELLOW + "移除最近鑽石", "3格內"));
        inv.setItem(16, item(Material.BARRIER, ChatColor.YELLOW + "移除最近綠寶石", "3格內"));
        inv.setItem(28, item(Material.BARRIER, ChatColor.RED + "清除所有鑽石", "數量: " + map.getDiamondGenerators().size()));
        inv.setItem(30, item(Material.BARRIER, ChatColor.RED + "清除所有綠寶石", "數量: " + map.getEmeraldGenerators().size()));
        inv.setItem(40, item(Material.CHEST, ChatColor.YELLOW + "獲得生成器工具", "使用物品放置"));
        inv.setItem(49, item(Material.ARROW, ChatColor.GRAY + "返回", ""));
        player.openInventory(inv);
    }

    public static void openNpcManager(Player player, LobbyNPCManager npcManager) {
        Inventory inv = player.getServer().createInventory(player, 54, TITLE_NPC_MANAGER);
        fillBorders(inv);
        inv.setItem(10, item(Material.VILLAGER_SPAWN_EGG, ChatColor.GREEN + "添加NPC", "在當前位置創建"));
        inv.setItem(49, item(Material.ARROW, ChatColor.GRAY + "返回", ""));

        int slot = 28;
        int index = 0;
        for (LobbyNPC npc : npcManager.getNPCs()) {
            if (slot == 34) {
                slot = 37;
            }
            if (slot >= inv.getSize() - 1) {
                break;
            }
            String name = npc.getDisplayName();
            inv.setItem(slot, item(Material.PAPER, ChatColor.YELLOW + name,
                "左鍵: 移動 | 右鍵: 重命名 | Shift+點擊: 刪除"));
            slot++;
            index++;
        }
        player.openInventory(inv);
    }

        public static void openNpcEditor(Player player, String npcName) {
        Inventory inv = player.getServer().createInventory(player, 54, TITLE_NPC_EDITOR_PREFIX + npcName);
        fillBorders(inv);
        var messages = com.example.bedwars.BedwarsPlugin.instance.getMessageManager();
        inv.setItem(20, item(Material.ENDER_PEARL, ChatColor.GREEN + messages.getMessage("npc-editor.move"),
            messages.getMessage("npc-editor.move-desc")));
        inv.setItem(22, item(Material.NAME_TAG, ChatColor.YELLOW + messages.getMessage("npc-editor.rename"),
            messages.getMessage("npc-editor.rename-desc")));
        inv.setItem(24, item(Material.BARRIER, ChatColor.RED + messages.getMessage("npc-editor.delete"),
            messages.getMessage("npc-editor.delete-desc")));
        inv.setItem(49, item(Material.ARROW, ChatColor.GRAY + messages.getMessage("npc-editor.back"), ""));
        player.openInventory(inv);
        }

    public static boolean isMapList(String title) {
        return TITLE_MAPS.equals(title);
    }

    public static boolean isEditor(String title) {
        return title.startsWith(TITLE_EDITOR_PREFIX);
    }

    public static boolean isTeamSelect(String title) {
        return title.startsWith(TITLE_TEAM_SELECT_PREFIX);
    }

    public static boolean isTeamEditor(String title) {
        return title.startsWith(TITLE_TEAM_EDITOR_PREFIX);
    }

    public static boolean isGeneratorEditor(String title) {
        return title.startsWith(TITLE_GENERATORS_PREFIX);
    }

    public static boolean isModeEditor(String title) {
        return title.startsWith(TITLE_MODES_PREFIX);
    }

    public static boolean isNpcManager(String title) {
        return TITLE_NPC_MANAGER.equals(title);
    }

    public static boolean isNpcEditor(String title) {
        return title.startsWith(TITLE_NPC_EDITOR_PREFIX);
    }

    public static void openModeEditor(Player player, MapConfig map) {
        Inventory inv = player.getServer().createInventory(player, 54, TITLE_MODES_PREFIX + map.getName());
        fillBorders(inv);
        
        // 顯示當前单一模式
        com.example.bedwars.game.GameMode currentMode = map.getMode();
        boolean solo = (currentMode == com.example.bedwars.game.GameMode.SOLO);
        boolean doubles = (currentMode == com.example.bedwars.game.GameMode.DOUBLES);
        boolean squad = (currentMode == com.example.bedwars.game.GameMode.SQUAD);

        int soloPlayers = com.example.bedwars.game.GameMode.SOLO.getMaxPlayers();
        int doublesPlayers = com.example.bedwars.game.GameMode.DOUBLES.getMaxPlayers();
        int squadPlayers = com.example.bedwars.game.GameMode.SQUAD.getMaxPlayers();

        inv.setItem(20, item(Material.IRON_SWORD, ChatColor.GOLD + "單人",
            (solo ? "已啟用" : "未啟用") + " | " + soloPlayers + " 玩家"));
        inv.setItem(22, item(Material.DIAMOND_SWORD, ChatColor.GOLD + "雙人",
            (doubles ? "已啟用" : "未啟用") + " | " + doublesPlayers + " 玩家"));
        inv.setItem(24, item(Material.NETHERITE_SWORD, ChatColor.GOLD + "隊伍",
            (squad ? "已啟用" : "未啟用") + " | " + squadPlayers + " 玩家"));
        inv.setItem(49, item(Material.ARROW, ChatColor.GRAY + "返回", ""));
        player.openInventory(inv);
    }

    public static boolean isLeaderboard(String title) {
        return TITLE_LEADERBOARD.equals(title);
    }

    public static boolean isDummy(String title) {
        return TITLE_DUMMY.equals(title);
    }

    public static void openDummy(Player player) {
        Inventory inv = player.getServer().createInventory(player, 54, TITLE_DUMMY);
        fillBorders(inv);
        inv.setItem(20, item(Material.LIME_WOOL, ChatColor.GREEN + "添加 +1", "添加一個機器人"));
        inv.setItem(22, item(Material.YELLOW_WOOL, ChatColor.YELLOW + "添加 +4", "添加四個機器人"));
        inv.setItem(24, item(Material.ORANGE_WOOL, ChatColor.GOLD + "填充", "填充到所需數量"));
        inv.setItem(30, item(Material.RED_WOOL, ChatColor.RED + "移除 -1", "移除一個機器人"));
        inv.setItem(32, item(Material.BARRIER, ChatColor.RED + "清除", "移除所有機器人"));
        inv.setItem(40, item(Material.PAPER, ChatColor.GRAY + "狀態", "使用 /bw dummy list 查看詳細信息"));
        inv.setItem(49, item(Material.ARROW, ChatColor.GRAY + "Back", ""));
        player.openInventory(inv);
    }

    public static void openLeaderboard(Player player, boolean rotateMode, boolean hasRotateLocation,
                                       int fixedCount, int fixedTotal, String nextFixedName) {
        Inventory inv = player.getServer().createInventory(player, 54, TITLE_LEADERBOARD);
        fillBorders(inv);
        var messages = com.example.bedwars.BedwarsPlugin.instance.getMessageManager();
        String modeText = rotateMode ? messages.getMessage("leaderboard.rotate-mode")
            : messages.getMessage("leaderboard.fixed-mode");
        String rotateStatus = hasRotateLocation ? messages.getMessage("leaderboard.rotate-enabled")
            : messages.getMessage("leaderboard.rotate-disabled");
        String statusText = messages.getMessage("leaderboard.rotate-label") + ": " + rotateStatus
            + ", " + messages.getMessage("leaderboard.fixed-label") + ": "
            + fixedCount + "/" + fixedTotal;

        inv.setItem(20, item(Material.LIME_WOOL, ChatColor.GREEN + messages.getMessage("leaderboard.set-rotate"),
            messages.getMessage("leaderboard.set-rotate-desc")));
        inv.setItem(22, item(Material.ENDER_PEARL, ChatColor.AQUA + messages.getMessage("leaderboard.set-next-fixed"),
            messages.getMessage("leaderboard.next") + nextFixedName));
        inv.setItem(24, item(Material.BOOK, ChatColor.YELLOW + messages.getMessage("leaderboard.next-category"),
            messages.getMessage("leaderboard.next-category-desc")));
        inv.setItem(29, item(Material.CLOCK, ChatColor.YELLOW + messages.getMessage("leaderboard.refresh"),
            messages.getMessage("leaderboard.refresh-desc")));
        inv.setItem(31, item(Material.BARRIER, ChatColor.RED + messages.getMessage("leaderboard.remove"),
            messages.getMessage("leaderboard.remove-desc")));
        inv.setItem(33, item(Material.LEVER, ChatColor.GOLD + messages.getMessage("leaderboard.toggle-mode"),
            messages.getMessage("leaderboard.current") + modeText));
        inv.setItem(40, item(Material.PAPER, ChatColor.GRAY + messages.getMessage("leaderboard.status"), statusText));
        inv.setItem(49, item(Material.ARROW, ChatColor.GRAY + messages.getMessage("map-editor.back"), ""));
        player.openInventory(inv);
    }

    private static void fillBorders(Inventory inv) {
        ItemStack pane = filler();
        int size = inv.getSize();
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, pane);
        }
        for (int i = size - 9; i < size; i++) {
            inv.setItem(i, pane);
        }
        for (int i = 9; i < size - 9; i += 9) {
            inv.setItem(i, pane);
            inv.setItem(i + 8, pane);
        }
    }

    private static ItemStack filler() {
        ItemStack stack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public static String extractMapName(String title, String prefix) {
        return title.substring(prefix.length()).trim();
    }

    public static TeamColor extractTeam(String title) {
        String[] parts = title.substring(TITLE_TEAM_EDITOR_PREFIX.length()).split(" ");
        if (parts.length < 2) {
            return null;
        }
        return TeamColor.fromName(parts[1]);
    }

    public static String extractMapNameFromTeamEditor(String title) {
        String remainder = title.substring(TITLE_TEAM_EDITOR_PREFIX.length()).trim();
        int spaceIndex = remainder.lastIndexOf(' ');
        if (spaceIndex <= 0) {
            return remainder;
        }
        return remainder.substring(0, spaceIndex).trim();
    }

    public static void setLobby(MapConfig map, Location location) {
        map.setLobbySpawn(location);
    }

    public static void setWorld(MapConfig map, Location location) {
        map.setWorldName(location.getWorld().getName());
    }

    public static void setTeamSpawn(MapConfig map, TeamColor team, Location location) {
        TeamConfig teamConfig = map.getTeamConfigs().get(team);
        if (teamConfig != null) {
            teamConfig.setSpawn(location);
        }
    }

    public static void setTeamBed(MapConfig map, TeamColor team, Location location) {
        TeamConfig teamConfig = map.getTeamConfigs().get(team);
        if (teamConfig != null) {
            teamConfig.setBedLocation(location.getBlock().getLocation());
        }
    }

    public static void setTeamGenerator(MapConfig map, TeamColor team, Location location) {
        TeamConfig teamConfig = map.getTeamConfigs().get(team);
        if (teamConfig != null) {
            teamConfig.setBaseGenerator(location);
        }
    }

    public static void setTeamShopNpc(MapConfig map, TeamColor team, Location location) {
        TeamConfig teamConfig = map.getTeamConfigs().get(team);
        if (teamConfig != null) {
            teamConfig.setShopNpcLocation(location);
        }
    }

    public static ItemStack item(Material material, String name, String lore) {
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
