package com.example.bedwars.game;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum ResourceType {
    IRON(Material.IRON_INGOT),
    GOLD(Material.GOLD_INGOT),
    DIAMOND(Material.DIAMOND),
    EMERALD(Material.EMERALD);

    private final Material material;

    ResourceType(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    public ItemStack toItem(int amount) {
        return new ItemStack(material, amount);
    }
}
