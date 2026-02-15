package com.example.bedwars.setup;

import com.example.bedwars.game.GameManager;
import com.example.bedwars.game.GameMode;
import com.example.bedwars.game.TeamColor;
import com.example.bedwars.integration.WorldEditIntegration;
import com.example.bedwars.lobby.LobbyNPC;
import com.example.bedwars.lobby.LobbyNPCManager;
import com.example.bedwars.lobby.LobbyLeaderboardManager;
import com.example.bedwars.map.MapConfig;
import com.example.bedwars.map.MapManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.block.Action;

public class SetupListener implements Listener {
    private final JavaPlugin plugin;
    private final MapManager mapManager;
    private final SetupSessionManager sessions;
    private final GameManager gameManager;
    private final LobbyNPCManager npcManager;
    private final LobbyLeaderboardManager leaderboardManager;

    public SetupListener(JavaPlugin plugin, MapManager mapManager, SetupSessionManager sessions, GameManager gameManager, LobbyNPCManager npcManager, LobbyLeaderboardManager leaderboardManager) {
        this.plugin = plugin;
        this.mapManager = mapManager;
        this.sessions = sessions;
        this.gameManager = gameManager;
        this.npcManager = npcManager;
        this.leaderboardManager = leaderboardManager;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        SetupSession session = sessions.getSession(player.getUniqueId());
        if (!session.isAwaitingMapName() && !session.isAwaitingMapRename() && !session.isAwaitingMapDeleteConfirm() 
                && !session.isAwaitingNpcRename() && !session.isAwaitingBoundsPos1() && !session.isAwaitingBoundsPos2()
                && !session.isAwaitingWorldEditConfirm()) {
            return;
        }
        event.setCancelled(true);
        String name = event.getMessage().trim();

        if (session.isAwaitingMapDeleteConfirm()) {
            session.setAwaitingMapDeleteConfirm(false);
            String target = session.getTargetMapName();
            session.setTargetMapName(null);
            if (!"DELETE".equalsIgnoreCase(name)) {
                player.sendMessage(ChatColor.YELLOW + "Delete cancelled.");
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                boolean ok = mapManager.deleteMap(target);
                if (ok) {
                    player.sendMessage(ChatColor.GREEN + "Map deleted: " + target);
                    gameManager.reloadActiveMap();
                    SetupMenu.openMapList(player, mapManager);
                } else {
                    player.sendMessage(ChatColor.RED + "Map not found.");
                }
            });
            return;
        }

        if (session.isAwaitingMapRename()) {
            session.setAwaitingMapRename(false);
            String target = session.getTargetMapName();
            session.setTargetMapName(null);
            if (!name.matches("[a-zA-Z0-9_-]+")) {
                player.sendMessage(ChatColor.RED + "Invalid name. Use letters, numbers, _ or -.");
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                boolean ok = mapManager.renameMap(target, name);
                if (ok) {
                    player.sendMessage(ChatColor.GREEN + "Map renamed to: " + name);
                    gameManager.reloadActiveMap();
                    MapConfig map = mapManager.getMap(name);
                    if (map != null) {
                        SetupMenu.openEditor(player, map);
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Rename failed. Check name or conflicts.");
                }
            });
            return;
        }

        if (session.isAwaitingNpcRename()) {
            session.setAwaitingNpcRename(false);
            Integer index = session.getTargetNpcIndex();
            session.setTargetNpcIndex(null);
            if (index == null) {
                if (slot == 19) {
                    .getMessage("command.npc-not-found"));
                } else if (slot == 21) {
            }
                } else if (slot == 23) {
                java.util.List<LobbyNPC> list = npcManager.getNPCs();
                } else if (slot == 25) {
                    player.sendMessage(ChatColor.RED + com.example.bedwars.BedwarsPlugin.instance.getMessageManager()
                } else if (slot == 28) {
                    return;
                } else if (slot == 30) {
                LobbyNPC npc = list.get(index);
                } else if (slot == 32) {
                player.sendMessage(ChatColor.GREEN + com.example.bedwars.BedwarsPlugin.instance.getMessageManager()
                } else if (slot == 34) {
                SetupMenu.openNpcManager(player, npcManager);
            });
            return;
        }

        if (session.isAwaitingBoundsPos1()) {
            if ("pos1".equalsIgnoreCase(name)) {
                if (slot == 19) {
                session.setBoundsPos1(player.getLocation().getBlock().getLocation());
                } else if (slot == 21) {
                player.sendMessage(ChatColor.YELLOW + "现在请站在地图的对角位置并输入: " + ChatColor.AQUA + "pos2");
                } else if (slot == 23) {
            } else {
                } else if (slot == 25) {
            }
            return;
        }

        if (session.isAwaitingBoundsPos2()) {
            if ("pos2".equalsIgnoreCase(name)) {
                session.setAwaitingBoundsPos2(false);
                org.bukkit.Location pos2 = player.getLocation().getBlock().getLocation();
                org.bukkit.Location pos1 = session.getBoundsPos1();
                MapConfig map = session.getCurrentMap();
                
                if (map == null) {
                    player.sendMessage(ChatColor.RED + "地图配置丢失，请重试");
                    return;
                }
                
                Bukkit.getScheduler().runTask(plugin, () -> {
                    map.setArenaMin(pos1);
                    map.setArenaMax(pos2);
                    mapManager.save(map);
                    
                    int dx = Math.abs(pos1.getBlockX() - pos2.getBlockX()) + 1;
                    int dy = Math.abs(pos1.getBlockY() - pos2.getBlockY()) + 1;
                    int dz = Math.abs(pos1.getBlockZ() - pos2.getBlockZ()) + 1;
                    int volume = dx * dy * dz;
                    
                    player.sendMessage(ChatColor.GREEN + "地图边界已设置！");
                    player.sendMessage(ChatColor.GRAY + "区域大小: " + dx + "x" + dy + "x" + dz + " (" + volume + " 方块)");
                    player.sendMessage(ChatColor.GRAY + "游戏结束后将自动恢复此区域内的方块");
                    
                    SetupMenu.openEditor(player, map);
                });
                
                session.setBoundsPos1(null);
                session.setCurrentMap(null);
            } else {
                player.sendMessage(ChatColor.RED + "请输入 'pos2' 来设置第二个点，或输入 'cancel' 取消");
            }
            return;
        }

        if (session.isAwaitingWorldEditConfirm()) {
            if ("ok".equalsIgnoreCase(name)) {
                session.setAwaitingWorldEditConfirm(false);
                MapConfig map = session.getCurrentMap();
                
                if (map == null) {
                    player.sendMessage(ChatColor.RED + "地图配置丢失，请重试");
                    return;
                }
                
                Bukkit.getScheduler().runTask(plugin, () -> {
                    org.bukkit.Location[] selection = WorldEditIntegration.getSelection(player);
                    
                    if (selection == null || selection.length != 2) {
                        player.sendMessage(ChatColor.RED + "无法获取WorldEdit选区！");
                        player.sendMessage(ChatColor.GRAY + "请确保你已使用 //wand 并选择了两个点");
                        SetupMenu.openEditor(player, map);
                        return;
                    }
                    
                    org.bukkit.Location min = selection[0];
                    org.bukkit.Location max = selection[1];
                    
                    map.setArenaMin(min);
                    map.setArenaMax(max);
                    mapManager.save(map);
                    
                    int dx = Math.abs(min.getBlockX() - max.getBlockX()) + 1;
                    int dy = Math.abs(min.getBlockY() - max.getBlockY()) + 1;
                    int dz = Math.abs(min.getBlockZ() - max.getBlockZ()) + 1;
                    int volume = dx * dy * dz;
                    
                    player.sendMessage(ChatColor.GREEN + "地图边界已通过WorldEdit设置！");
                    player.sendMessage(ChatColor.GRAY + "区域大小: " + dx + "x" + dy + "x" + dz + " (" + volume + " 方块)");
                    player.sendMessage(ChatColor.GRAY + "游戏结束后将自动恢复此区域内的方块");
                    
                    SetupMenu.openEditor(player, map);
                });
                
                session.setCurrentMap(null);
            } else if ("cancel".equalsIgnoreCase(name)) {
                session.setAwaitingWorldEditConfirm(false);
                MapConfig map = session.getCurrentMap();
                session.setCurrentMap(null);
                
                if (map != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(ChatColor.YELLOW + "已取消WorldEdit选区设置");
                        SetupMenu.openEditor(player, map);
                    });
                }
            } else {
                player.sendMessage(ChatColor.RED + "请输入 'ok' 确认选区，或输入 'cancel' 取消");
            }
            return;
        }

        if (!name.matches("[a-zA-Z0-9_-]+")) {
            player.sendMessage(ChatColor.RED + "Invalid name. Use letters, numbers, _ or -.");
            return;
        }
        if (mapManager.getMap(name) != null) {
            player.sendMessage(ChatColor.RED + "Map already exists.");
            return;
        }
        session.setAwaitingMapName(false);
        Bukkit.getScheduler().runTask(plugin, () -> {
            MapConfig map = mapManager.createMap(name, player);
            mapManager.setActiveMap(name);
            mapManager.save(map);
            SetupMenu.openEditor(player, map);
            player.sendMessage(ChatColor.GREEN + "Map created: " + name);
        });
    }

    @EventHandler
    public void onSetupToolUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }
        String name = meta.getDisplayName();
        if (!name.startsWith(ChatColor.YELLOW + "Setup:")) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        event.setCancelled(true);

        SetupSession session = sessions.getSession(player.getUniqueId());
        String mapName = session.getEditingMapName();
        MapConfig map = mapName != null ? mapManager.getMap(mapName) : null;
        if (map == null) {
            player.sendMessage(ChatColor.RED + "No map selected. Use the setup GUI first.");
            return;
        }

        var messages = com.example.bedwars.BedwarsPlugin.instance.getMessageManager();

        Block clicked = event.getClickedBlock();
        String toolName = ChatColor.stripColor(name);
        if (toolName == null) {
            return;
        }

        switch (toolName) {
            case "Setup: Team Spawn" -> {
                TeamColor team = session.getEditingTeam();
                if (team == null) {
                    player.sendMessage(ChatColor.RED + messages.getMessage("command.team-not-selected"));
                    return;
                }
                SetupMenu.setTeamSpawn(map, team, player.getLocation());
                mapManager.save(map);
                player.sendMessage(ChatColor.GREEN + messages.getMessage("command.team-updated",
                    "team", formatTeamName(team),
                    "item", messages.getMessage("command.team-item-spawn")));
            }
            case "Setup: Team Bed" -> {
                TeamColor team = session.getEditingTeam();
                if (team == null) {
                    player.sendMessage(ChatColor.RED + messages.getMessage("command.team-not-selected"));
                    return;
                }
                if (clicked != null) {
                    SetupMenu.setTeamBed(map, team, clicked.getLocation());
                } else {
                    SetupMenu.setTeamBed(map, team, player.getLocation());
                }
                mapManager.save(map);
                player.sendMessage(ChatColor.GREEN + messages.getMessage("command.team-updated",
                    "team", formatTeamName(team),
                    "item", messages.getMessage("command.team-item-bed")));
            }
            case "Setup: Base Generator" -> {
                TeamColor team = session.getEditingTeam();
                if (team == null) {
                    player.sendMessage(ChatColor.RED + messages.getMessage("command.team-not-selected"));
                    return;
                }
                SetupMenu.setTeamGenerator(map, team, player.getLocation());
                mapManager.save(map);
                player.sendMessage(ChatColor.GREEN + messages.getMessage("command.team-updated",
                    "team", formatTeamName(team),
                    "item", messages.getMessage("command.team-item-generator")));
            }
            case "Setup: Shop NPC" -> {
                TeamColor team = session.getEditingTeam();
                if (team == null) {
                    player.sendMessage(ChatColor.RED + messages.getMessage("command.team-not-selected"));
                    return;
                }
                SetupMenu.setTeamShopNpc(map, team, player.getLocation());
                mapManager.save(map);
                player.sendMessage(ChatColor.GREEN + messages.getMessage("command.team-updated",
                    "team", formatTeamName(team),
                    "item", messages.getMessage("command.team-item-shop")));
            }
            case "Setup: Diamond Gen" -> {
                if (clicked != null) {
                    map.addDiamondGenerator(clicked.getLocation());
                } else {
                    map.addDiamondGenerator(player.getLocation());
                }
                mapManager.save(map);
                player.sendMessage(ChatColor.GREEN + "Diamond generator added.");
            }
            case "Setup: Emerald Gen" -> {
                if (clicked != null) {
                    map.addEmeraldGenerator(clicked.getLocation());
                } else {
                    map.addEmeraldGenerator(player.getLocation());
                }
                mapManager.save(map);
                player.sendMessage(ChatColor.GREEN + "Emerald generator added.");
            }
            default -> {
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        String title = event.getView().getTitle();
        if (SetupMenu.isMapList(title) || SetupMenu.isEditor(title) || SetupMenu.isTeamSelect(title)
                || SetupMenu.isTeamEditor(title) || SetupMenu.isGeneratorEditor(title) || SetupMenu.isModeEditor(title)
                || SetupMenu.isNpcManager(title) || SetupMenu.isNpcEditor(title)
                || SetupMenu.isLeaderboard(title) || SetupMenu.isDummy(title)) {
            event.setCancelled(true);
        } else {
            return;
        }
        int slot = event.getRawSlot();
        if (SetupMenu.isMapList(title)) {
            handleMapList(player, slot);
            return;
        }
        if (SetupMenu.isEditor(title)) {
            String mapName = SetupMenu.extractMapName(title, SetupMenu.TITLE_EDITOR_PREFIX);
            MapConfig map = mapManager.getMap(mapName);
            if (map != null) {
                SetupSession session = sessions.getSession(player.getUniqueId());
                session.setEditingMapName(map.getName());
                session.setEditingTeam(null);
                handleEditor(player, slot, map, event.isRightClick());
            }
            return;
        }
        if (SetupMenu.isTeamSelect(title)) {
            String mapName = SetupMenu.extractMapName(title, SetupMenu.TITLE_TEAM_SELECT_PREFIX);
            MapConfig map = mapManager.getMap(mapName);
            if (map != null) {
                SetupSession session = sessions.getSession(player.getUniqueId());
                session.setEditingMapName(map.getName());
                session.setEditingTeam(null);
                handleTeamSelect(player, slot, map);
            }
            return;
        }
        if (SetupMenu.isTeamEditor(title)) {
            String mapName = SetupMenu.extractMapNameFromTeamEditor(title);
            MapConfig map = mapManager.getMap(mapName);
            TeamColor team = SetupMenu.extractTeam(title);
            if (map != null && team != null) {
                SetupSession session = sessions.getSession(player.getUniqueId());
                session.setEditingMapName(map.getName());
                session.setEditingTeam(team);
                handleTeamEditor(player, slot, map, team);
            }
            return;
        }
        if (SetupMenu.isGeneratorEditor(title)) {
            String mapName = SetupMenu.extractMapName(title, SetupMenu.TITLE_GENERATORS_PREFIX);
            MapConfig map = mapManager.getMap(mapName);
            if (map != null) {
                SetupSession session = sessions.getSession(player.getUniqueId());
                session.setEditingMapName(map.getName());
                session.setEditingTeam(null);
                handleGeneratorEditor(player, slot, map);
            }
            return;
        }
        if (SetupMenu.isModeEditor(title)) {
            String mapName = SetupMenu.extractMapName(title, SetupMenu.TITLE_MODES_PREFIX);
            MapConfig map = mapManager.getMap(mapName);
            if (map != null) {
                handleModeEditor(player, slot, map);
            }
            return;
        }
        if (SetupMenu.isNpcManager(title)) {
            handleNpcManager(player, slot, event.isRightClick(), event.isShiftClick());
            return;
        }
        if (SetupMenu.isNpcEditor(title)) {
            handleNpcEditor(player, slot);
            return;
        }
        if (SetupMenu.isLeaderboard(title)) {
            handleLeaderboard(player, slot);
            return;
        }
        if (SetupMenu.isDummy(title)) {
            handleDummy(player, slot);
        }
    }

    private void handleMapList(Player player, int slot) {
        if (slot == 10) {
            sessions.getSession(player.getUniqueId()).setAwaitingMapName(true);
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + com.example.bedwars.BedwarsPlugin.instance.getMessageManager()
                .getMessage("command.create-map-input"));
            return;
        }
        if (slot == 12) {
            SetupMenu.openNpcManager(player, npcManager);
            return;
        }
        if (slot == 14) {
            openLeaderboardMenu(player);
            return;
        }
        if (slot == 16) {
            SetupMenu.openDummy(player);
            return;
        }
        if (slot == 49) {
            player.closeInventory();
            return;
        }
        int index;
        if (slot >= 28 && slot <= 34) {
            index = slot - 28;
        } else if (slot >= 37 && slot <= 43) {
            index = (slot - 37) + 7;
        } else {
            return;
        }
        if (index < 0) {
            return;
        }
        if (index >= mapManager.getMapNames().size()) {
            return;
        }
        String mapName = mapManager.getMapNames().get(index);
        MapConfig map = mapManager.getMap(mapName);
        if (map != null) {
            SetupMenu.openEditor(player, map);
        }
    }

    private void handleEditor(Player player, int slot, MapConfig map, boolean rightClick) {
        switch (slot) {
            case 10 -> {
                SetupMenu.setLobby(map, player.getLocation());
                mapManager.save(map);
                player.sendMessage(ChatColor.GREEN + "Lobby spawn updated.");
            }
            case 12 -> SetupMenu.openTeamSelect(player, map);
            case 14 -> SetupMenu.openGeneratorEditor(player, map);
            case 16 -> {
                SetupMenu.setWorld(map, player.getLocation());
                mapManager.save(map);
                player.sendMessage(ChatColor.GREEN + "World set to " + player.getWorld().getName());
            }
            case 20 -> {
                // 手动设置边界
                player.closeInventory();
                SetupSession session = sessions.getSession(player.getUniqueId());
                session.setAwaitingBoundsPos1(true);
                session.setCurrentMap(map);
                player.sendMessage(ChatColor.YELLOW + "请站在地图的第一个角落位置并输入: " + ChatColor.AQUA + "pos1");
                player.sendMessage(ChatColor.GRAY + "这将设置地图边界的第一个点");
            }
            case 22 -> {
                // WorldEdit选区
                if (!com.example.bedwars.integration.WorldEditIntegration.isAvailable()) {
                    player.sendMessage(ChatColor.RED + "WorldEdit未安装!");
                    return;
                }
                player.closeInventory();
                SetupSession session = sessions.getSession(player.getUniqueId());
                session.setAwaitingWorldEditConfirm(true);
                session.setCurrentMap(map);
                player.sendMessage(ChatColor.GREEN + "请使用木斧 (//wand) 选择地图区域");
                player.sendMessage(ChatColor.YELLOW + "选择完成后，输入: " + ChatColor.AQUA + "ok");
            }
            case 24 -> {
                // 4队/8队模式选择
                if (rightClick) {
                    // 右键：8队模式 (SOLO)
                    map.setMode(com.example.bedwars.game.GameMode.SOLO);
                    mapManager.save(map);
                    player.sendMessage(ChatColor.GREEN + "已设置为 8队模式 (Solo)");
                } else {
                    // 左键：4队模式 (SQUAD)
                    map.setMode(com.example.bedwars.game.GameMode.SQUAD);
                    mapManager.save(map);
                    player.sendMessage(ChatColor.GREEN + "已设置为 4队模式 (Squad)");
                }
                SetupMenu.openEditor(player, map);
            }
            case 28 -> SetupMenu.openModeEditor(player, map);
            case 30 -> {
                if (!validateMap(player, map)) {
                    return;
                }
                mapManager.save(map);
                player.sendMessage(ChatColor.GREEN + "Map saved.");
            }
            case 32 -> {
                if (!validateMap(player, map)) {
                    return;
                }
                mapManager.setActiveMap(map.getName());
                gameManager.rebuildTeams();
                player.sendMessage(ChatColor.GREEN + "Active map set to " + map.getName());
            }
            case 34 -> {
                SetupSession session = sessions.getSession(player.getUniqueId());
                session.setAwaitingMapRename(true);
                session.setTargetMapName(map.getName());
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + com.example.bedwars.BedwarsPlugin.instance.getMessageManager()
                    .getMessage("command.rename-map-input"));
            }
            case 38 -> {
                SetupSession session = sessions.getSession(player.getUniqueId());
                session.setAwaitingMapDeleteConfirm(true);
                session.setTargetMapName(map.getName());
                player.closeInventory();
                player.sendMessage(ChatColor.RED + com.example.bedwars.BedwarsPlugin.instance.getMessageManager()
                    .getMessage("command.delete-map-input") + map.getName());
            }
            case 49 -> SetupMenu.openMapList(player, mapManager);
            default -> {
            }
        }
    }

    private void handleTeamSelect(Player player, int slot, MapConfig map) {
        TeamColor team = null;
        
        // Check if this is an 8-team map (SOLO or DOUBLES mode)
        boolean is8Team = map.getMode() == GameMode.SOLO || map.getMode() == GameMode.DOUBLES;
        
        if (is8Team) {
            // 8-team layout: Row 1 (20-23), Row 2 (29-32)
            if (slot == 20) {
                team = TeamColor.RED;
            } else if (slot == 21) {
                team = TeamColor.GREEN;
            } else if (slot == 22) {
                team = TeamColor.BLUE;
            } else if (slot == 23) {
                team = TeamColor.YELLOW;
            } else if (slot == 29) {
                team = TeamColor.AQUA;
            } else if (slot == 30) {
                team = TeamColor.PINK;
            } else if (slot == 31) {
                team = TeamColor.WHITE;
            } else if (slot == 32) {
                team = TeamColor.GRAY;
            } else if (slot == 49) {
                SetupMenu.openEditor(player, map);
                return;
            }
        } else {
            // 4-team layout: Single row (20, 22, 24, 26)
            if (slot == 20) {
                team = TeamColor.RED;
            } else if (slot == 22) {
                team = TeamColor.BLUE;
            } else if (slot == 24) {
                team = TeamColor.GREEN;
            } else if (slot == 26) {
                team = TeamColor.YELLOW;
            } else if (slot == 49) {
                SetupMenu.openEditor(player, map);
                return;
            }
        }
        
        if (team != null) {
            SetupMenu.openTeamEditor(player, map, team);
        }
    }

    private void handleTeamEditor(Player player, int slot, MapConfig map, TeamColor team) {
        var messages = com.example.bedwars.BedwarsPlugin.instance.getMessageManager();
        switch (slot) {
            case 10 -> {
                SetupMenu.setTeamSpawn(map, team, player.getLocation());
                mapManager.save(map);
                player.sendMessage(ChatColor.GREEN + messages.getMessage("command.team-updated",
                    "team", formatTeamName(team),
                    "item", messages.getMessage("command.team-item-spawn")));
            }
            case 12 -> {
                SetupMenu.setTeamBed(map, team, player.getLocation());
                mapManager.save(map);
                player.sendMessage(ChatColor.GREEN + messages.getMessage("command.team-updated",
                    "team", formatTeamName(team),
                    "item", messages.getMessage("command.team-item-bed")));
            }
            case 14 -> {
                SetupMenu.setTeamGenerator(map, team, player.getLocation());
                mapManager.save(map);
                player.sendMessage(ChatColor.GREEN + messages.getMessage("command.team-updated",
                    "team", formatTeamName(team),
                    "item", messages.getMessage("command.team-item-generator")));
            }
            case 16 -> {
                SetupMenu.setTeamShopNpc(map, team, player.getLocation());
                mapManager.save(map);
                player.sendMessage(ChatColor.GREEN + messages.getMessage("command.team-updated",
                    "team", formatTeamName(team),
                    "item", messages.getMessage("command.team-item-shop")));
            }
            case 40 -> {
                giveTeamTools(player, team);
                player.sendMessage(ChatColor.YELLOW + messages.getMessage("command.team-tools-given",
                    "team", formatTeamName(team)));
            }
            case 28 -> {
                SetupMenu.setTeamSpawn(map, team, map.getLobbySpawn());
                map.getTeamConfigs().get(team).setSpawnSet(false);
                mapManager.save(map);
                player.sendMessage(ChatColor.YELLOW + messages.getMessage("command.team-cleared",
                    "team", formatTeamName(team),
                    "item", messages.getMessage("command.team-item-spawn")));
            }
            case 30 -> {
                SetupMenu.setTeamBed(map, team, map.getLobbySpawn());
                map.getTeamConfigs().get(team).setBedSet(false);
                mapManager.save(map);
                player.sendMessage(ChatColor.YELLOW + messages.getMessage("command.team-cleared",
                    "team", formatTeamName(team),
                    "item", messages.getMessage("command.team-item-bed")));
            }
            case 32 -> {
                SetupMenu.setTeamGenerator(map, team, map.getLobbySpawn());
                map.getTeamConfigs().get(team).setBaseGeneratorSet(false);
                mapManager.save(map);
                player.sendMessage(ChatColor.YELLOW + messages.getMessage("command.team-cleared",
                    "team", formatTeamName(team),
                    "item", messages.getMessage("command.team-item-generator")));
            }
            case 34 -> {
                SetupMenu.setTeamShopNpc(map, team, map.getLobbySpawn());
                map.getTeamConfigs().get(team).setShopNpcSet(false);
                mapManager.save(map);
                player.sendMessage(ChatColor.YELLOW + messages.getMessage("command.team-cleared",
                    "team", formatTeamName(team),
                    "item", messages.getMessage("command.team-item-shop")));
            }
            case 49 -> SetupMenu.openTeamSelect(player, map);
            default -> {
            }
        }
    }

    private void handleGeneratorEditor(Player player, int slot, MapConfig map) {
        switch (slot) {
            case 10 -> {
                map.addDiamondGenerator(player.getLocation());
                mapManager.save(map);
                player.sendMessage(ChatColor.GREEN + "Diamond generator added.");
                SetupMenu.openGeneratorEditor(player, map);
            }
            case 12 -> {
                map.addEmeraldGenerator(player.getLocation());
                mapManager.save(map);
                player.sendMessage(ChatColor.GREEN + "Emerald generator added.");
                SetupMenu.openGeneratorEditor(player, map);
            }
            case 14 -> {
                boolean removed = map.removeNearestDiamond(player.getLocation(), 3.0);
                if (removed) {
                    mapManager.save(map);
                    player.sendMessage(ChatColor.YELLOW + "Diamond generator removed.");
                } else {
                    player.sendMessage(ChatColor.RED + "No diamond generator nearby.");
                }
                SetupMenu.openGeneratorEditor(player, map);
            }
            case 16 -> {
                boolean removed = map.removeNearestEmerald(player.getLocation(), 3.0);
                if (removed) {
                    mapManager.save(map);
                    player.sendMessage(ChatColor.YELLOW + "Emerald generator removed.");
                } else {
                    player.sendMessage(ChatColor.RED + "No emerald generator nearby.");
                }
                SetupMenu.openGeneratorEditor(player, map);
            }
            case 40 -> {
                giveGeneratorTools(player);
                player.sendMessage(ChatColor.YELLOW + "Generator tools given.");
            }
            case 28 -> {
                map.clearDiamonds();
                mapManager.save(map);
                player.sendMessage(ChatColor.YELLOW + "Diamond generators cleared.");
                SetupMenu.openGeneratorEditor(player, map);
            }
            case 30 -> {
                map.clearEmeralds();
                mapManager.save(map);
                player.sendMessage(ChatColor.YELLOW + "Emerald generators cleared.");
                SetupMenu.openGeneratorEditor(player, map);
            }
            case 49 -> SetupMenu.openEditor(player, map);
            default -> {
            }
        }
    }

    private void handleModeEditor(Player player, int slot, MapConfig map) {
        switch (slot) {
            case 20 -> toggleMode(player, map, com.example.bedwars.game.GameMode.SOLO);
            case 22 -> toggleMode(player, map, com.example.bedwars.game.GameMode.DOUBLES);
            case 24 -> toggleMode(player, map, com.example.bedwars.game.GameMode.SQUAD);
            case 49 -> SetupMenu.openEditor(player, map);
            default -> {
            }
        }
    }

    private void toggleMode(Player player, MapConfig map, com.example.bedwars.game.GameMode mode) {
        // Since each map now has only one mode, simply set it
        map.setMode(mode);
        mapManager.save(map);
        player.sendMessage(ChatColor.GREEN + mode.getDisplayName() + " selected as map mode.");
        SetupMenu.openModeEditor(player, map);
    }

    private void handleLeaderboard(Player player, int slot) {
        switch (slot) {
            case 20 -> {
                leaderboardManager.setLocation(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Leaderboard set at your position.");
                openLeaderboardMenu(player);
            }
            case 22 -> {
                leaderboardManager.setNextFixedLocation(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Fixed leaderboard set.");
                openLeaderboardMenu(player);
            }
            case 24 -> {
                leaderboardManager.nextCategory();
                player.sendMessage(ChatColor.YELLOW + "Leaderboard rotated.");
            }
            case 29 -> {
                leaderboardManager.refreshNow();
                player.sendMessage(ChatColor.YELLOW + "Leaderboard refreshed.");
            }
            case 31 -> {
                leaderboardManager.remove();
                player.sendMessage(ChatColor.RED + "Leaderboard removed.");
                openLeaderboardMenu(player);
            }
            case 33 -> {
                leaderboardManager.toggleMode();
                player.sendMessage(ChatColor.YELLOW + "Leaderboard mode toggled.");
                openLeaderboardMenu(player);
            }
            case 49 -> SetupMenu.openMapList(player, mapManager);
            default -> {
            }
        }
    }

    private void handleDummy(Player player, int slot) {
        switch (slot) {
            case 20 -> {
                int added = gameManager.addDummyPlayers(1);
                player.sendMessage(ChatColor.GREEN + "Added dummy players: " + added);
                SetupMenu.openDummy(player);
            }
            case 22 -> {
                int added = gameManager.addDummyPlayers(4);
                player.sendMessage(ChatColor.GREEN + "Added dummy players: " + added);
                SetupMenu.openDummy(player);
            }
            case 24 -> {
                int needed = gameManager.getRequiredPlayersCount() - gameManager.getCurrentPlayerCount();
                int added = needed > 0 ? gameManager.addDummyPlayers(needed) : 0;
                player.sendMessage(ChatColor.GREEN + "Added dummy players: " + added);
                SetupMenu.openDummy(player);
            }
            case 30 -> {
                int removed = gameManager.removeDummyPlayers(1);
                player.sendMessage(ChatColor.YELLOW + "Removed dummy players: " + removed);
                SetupMenu.openDummy(player);
            }
            case 32 -> {
                gameManager.clearDummyPlayers();
                player.sendMessage(ChatColor.YELLOW + "All dummy players cleared.");
                SetupMenu.openDummy(player);
            }
            case 49 -> SetupMenu.openMapList(player, mapManager);
            default -> {
            }
        }
    }

    private void giveTeamTools(Player player, TeamColor team) {
        player.getInventory().addItem(
                createTeamTool(Material.ENDER_PEARL, team, "Setup: Team Spawn"),
                createTeamTool(Material.RED_BED, team, "Setup: Team Bed"),
                createTeamTool(Material.HOPPER, team, "Setup: Base Generator"),
                createTeamTool(Material.VILLAGER_SPAWN_EGG, team, "Setup: Shop NPC")
        );
    }

    private void giveGeneratorTools(Player player) {
        player.getInventory().addItem(
                createTool(Material.DIAMOND, "Setup: Diamond Gen"),
                createTool(Material.EMERALD, "Setup: Emerald Gen")
        );
    }

    private ItemStack createTool(Material material, String name) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + name);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private ItemStack createTeamTool(Material material, TeamColor team, String name) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "Setup: " + team.getChatColor() + name.substring("Setup: ".length()));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private String formatTeamName(TeamColor team) {
        return team.getChatColor() + team.name();
    }

    private String formatLocation(org.bukkit.Location loc) {
        return String.format("(%d, %d, %d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private void openLeaderboardMenu(Player player) {
        SetupMenu.openLeaderboard(
                player,
                leaderboardManager.isRotateMode(),
                leaderboardManager.hasRotateLocation(),
                leaderboardManager.getFixedCount(),
                leaderboardManager.getFixedTotal(),
                leaderboardManager.getNextFixedCategoryName()
        );
    }

    private void handleNpcManager(Player player, int slot, boolean rightClick, boolean shiftClick) {
        var messages = com.example.bedwars.BedwarsPlugin.instance.getMessageManager();
        if (slot == 10) {
            npcManager.addNPC(player.getLocation(), ChatColor.GOLD + "Bedwars");
            player.sendMessage(ChatColor.GREEN + messages.getMessage("command.npc-created"));
            SetupMenu.openNpcManager(player, npcManager);
            return;
        }
        if (slot == 49) {
            SetupMenu.openMapList(player, mapManager);
            return;
        }
        int index;
        if (slot >= 28 && slot <= 34) {
            index = slot - 28;
        } else if (slot >= 37 && slot <= 43) {
            index = (slot - 37) + 7;
        } else {
            return;
        }
        java.util.List<LobbyNPC> list = npcManager.getNPCs();
        if (index < 0 || index >= list.size()) {
            return;
        }
        LobbyNPC npc = list.get(index);

        if (shiftClick) {
            npcManager.removeNPC(npc);
            player.sendMessage(ChatColor.YELLOW + messages.getMessage("command.npc-deleted"));
            SetupMenu.openNpcManager(player, npcManager);
            return;
        }

        if (rightClick) {
            SetupSession session = sessions.getSession(player.getUniqueId());
            session.setAwaitingNpcRename(true);
            session.setTargetNpcIndex(index);
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + messages.getMessage("command.npc-rename-input"));
            return;
        }

        npcManager.updateNPC(npc, player.getLocation(), npc.getDisplayName());
        player.sendMessage(ChatColor.GREEN + messages.getMessage("command.npc-moved"));
        SetupMenu.openNpcManager(player, npcManager);
    }

    private void handleNpcEditor(Player player, int slot) {
        SetupSession session = sessions.getSession(player.getUniqueId());
        Integer index = session.getTargetNpcIndex();
        var messages = com.example.bedwars.BedwarsPlugin.instance.getMessageManager();
        if (index == null) {
            player.sendMessage(ChatColor.RED + messages.getMessage("command.npc-not-found"));
            player.closeInventory();
            return;
        }
        java.util.List<LobbyNPC> list = npcManager.getNPCs();
        if (index < 0 || index >= list.size()) {
            player.sendMessage(ChatColor.RED + messages.getMessage("command.npc-not-found"));
            player.closeInventory();
            return;
        }
        LobbyNPC npc = list.get(index);

        if (slot == 20) {
            npcManager.updateNPC(npc, player.getLocation(), npc.getDisplayName());
            player.sendMessage(ChatColor.GREEN + messages.getMessage("command.npc-moved"));
            SetupMenu.openNpcEditor(player, npc.getDisplayName());
            return;
        }
        if (slot == 22) {
            session.setAwaitingNpcRename(true);
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + messages.getMessage("command.npc-rename-input"));
            return;
        }
        if (slot == 24) {
            npcManager.removeNPC(npc);
            session.setTargetNpcIndex(null);
            player.sendMessage(ChatColor.YELLOW + messages.getMessage("command.npc-deleted"));
            player.closeInventory();
            return;
        }
        if (slot == 49) {
            session.setTargetNpcIndex(null);
            SetupMenu.openNpcManager(player, npcManager);
        }
    }

    private boolean validateMap(Player player, MapConfig map) {
        var messages = com.example.bedwars.BedwarsPlugin.instance.getMessageManager();
        java.util.List<String> errors = map.validateSetup(messages);
        if (errors.isEmpty()) {
            return true;
        }
        player.sendMessage(ChatColor.RED + messages.getMessage("validation.incomplete"));
        for (String error : errors) {
            player.sendMessage(ChatColor.RED + "- " + error);
        }
        return false;
    }
}
