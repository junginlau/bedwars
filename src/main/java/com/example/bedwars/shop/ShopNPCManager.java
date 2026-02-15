package com.example.bedwars.shop;

import com.example.bedwars.game.GameManager;
import com.example.bedwars.game.TeamColor;
import com.example.bedwars.game.TeamData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

import java.util.*;

public class ShopNPCManager {
    private final GameManager gameManager;
    private final Map<TeamColor, Villager> shopNPCs = new EnumMap<>(TeamColor.class);

    public ShopNPCManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void spawnShopNPCs() {
        removeAll();
        for (TeamData team : gameManager.getTeams().values()) {
            if (!team.getConfig().isShopNpcSet()) {
                continue;
            }
            Location shopLoc = team.getConfig().getShopNpcLocation();
            if (shopLoc == null || shopLoc.getWorld() == null) {
                continue;
            }
            Villager villager = (Villager) shopLoc.getWorld().spawnEntity(shopLoc, EntityType.VILLAGER);
            villager.setCustomName(team.getColor().getChatColor() + "商店");
            villager.setCustomNameVisible(true);
            villager.setAI(false);
            villager.setGravity(false);
            villager.setInvulnerable(true);
            villager.setSilent(true);
            villager.setProfession(Villager.Profession.NONE);
            villager.setCollidable(false);
            shopNPCs.put(team.getColor(), villager);
        }
    }

    public void removeAll() {
        for (Villager npc : shopNPCs.values()) {
            if (npc != null && !npc.isDead()) {
                npc.remove();
            }
        }
        shopNPCs.clear();
    }

    public boolean isShopNPC(Entity entity) {
        for (Villager npc : shopNPCs.values()) {
            if (npc != null && npc.getUniqueId().equals(entity.getUniqueId())) {
                return true;
            }
        }
        return false;
    }
}
