package com.example.bedwars.lobby;

import com.example.bedwars.game.GameManager;
import com.example.bedwars.game.GameMode;
import com.example.bedwars.map.MapConfig;
import com.example.bedwars.map.MapManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LobbyListener implements Listener {
    private final LobbyNPCManager npcManager;
    private final MapManager mapManager;
    private final GameManager gameManager;
    private final Map<UUID, String> selectedMaps = new HashMap<>();

    public LobbyListener(LobbyNPCManager npcManager, MapManager mapManager, GameManager gameManager) {
        this.npcManager = npcManager;
        this.mapManager = mapManager;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onNPCClick(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        LobbyNPC npc = npcManager.findNPC(entity);
        if (npc == null) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        if (player.hasPermission("bedwars.admin") && player.isSneaking()) {
            java.util.List<LobbyNPC> list = npcManager.getNPCs();
            int index = list.indexOf(npc);
            if (index >= 0) {
                com.example.bedwars.BedwarsPlugin.instance.getSetupSessionManager()
                    .getSession(player.getUniqueId()).setTargetNpcIndex(index);
            }
            com.example.bedwars.setup.SetupMenu.openNpcEditor(player, npc.getDisplayName());
            return;
        }
        LobbyGUI.openMapSelector(player, mapManager);
    }

    @EventHandler
    public void onNPCInteractAt(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        LobbyNPC npc = npcManager.findNPC(entity);
        if (npc == null) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        String title = event.getView().getTitle();
        if (LobbyGUI.isMapSelector(title)) {
            event.setCancelled(true);
            handleMapSelect(player, event.getRawSlot());
            return;
        }
        if (LobbyGUI.isModeSelector(title)) {
            event.setCancelled(true);
            String mapName = LobbyGUI.extractMapName(title);
            handleModeSelect(player, mapName, event.getRawSlot());
        }
    }

    private void handleMapSelect(Player player, int slot) {
        if (slot < 0 || slot >= mapManager.getMapNames().size()) {
            return;
        }
        String mapName = mapManager.getMapNames().get(slot);
        MapConfig map = mapManager.getMap(mapName);
        if (map == null) {
            player.sendMessage(ChatColor.RED + "Map not found.");
            return;
        }
        selectedMaps.put(player.getUniqueId(), mapName);
        LobbyGUI.openModeSelector(player, map);
    }

    private void handleModeSelect(Player player, String mapName, int slot) {
        if (slot == 26) {
            LobbyGUI.openMapSelector(player, mapManager);
            return;
        }
        MapConfig map = mapManager.getMap(mapName);
        if (map == null) {
            player.sendMessage(ChatColor.RED + "Map not found.");
            player.closeInventory();
            return;
        }
        
        // Center slot (13) is where the single mode is displayed
        if (slot != 13) {
            return;
        }
        
        GameMode mode = map.getMode();
        player.closeInventory();
        gameManager.joinGame(player, mapName, mode);
        selectedMaps.remove(player.getUniqueId());
    }
}
