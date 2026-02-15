package com.example.bedwars.lobby;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class LobbyNPC {
    private final JavaPlugin plugin;
    private final Location location;
    private final String displayName;
    private Villager npc;
    private UUID npcUuid;

    public LobbyNPC(JavaPlugin plugin, Location location, String displayName) {
        this.plugin = plugin;
        this.location = location;
        this.displayName = displayName;
    }

    public void spawn() {
        remove();
        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.setCustomName(displayName);
        villager.setCustomNameVisible(true);
        villager.setAI(false);
        villager.setGravity(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);
        villager.setProfession(Villager.Profession.NONE);
        villager.setCollidable(false);
        this.npc = villager;
        this.npcUuid = villager.getUniqueId();
    }

    public void remove() {
        if (npc != null && !npc.isDead()) {
            npc.remove();
        }
        if (npcUuid != null && location.getWorld() != null) {
            for (Entity entity : location.getWorld().getEntities()) {
                if (entity.getUniqueId().equals(npcUuid)) {
                    entity.remove();
                    break;
                }
            }
        }
        npc = null;
        npcUuid = null;
    }

    public boolean isNPC(Entity entity) {
        return npcUuid != null && entity.getUniqueId().equals(npcUuid);
    }

    public Location getLocation() {
        return location;
    }

    public String getDisplayName() {
        return displayName;
    }
}
