package com.example.bedwars.shop;

import com.example.bedwars.game.GameManager;
import com.example.bedwars.game.ResourceType;
import com.example.bedwars.game.TeamData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ShopMenu {
    public static final String TITLE = "Bedwars Shop";

    public static void open(Player player, GameManager gameManager) {
        Inventory inv = player.getServer().createInventory(player, 27, TITLE);
        inv.setItem(10, item(Material.WHITE_WOOL, ChatColor.WHITE + "Wool x16", "Cost: 4 Iron"));
        inv.setItem(11, item(Material.STONE_SWORD, ChatColor.GRAY + "Stone Sword", "Cost: 10 Iron"));
        inv.setItem(12, item(Material.BOW, ChatColor.GOLD + "Bow", "Cost: 12 Gold"));
        inv.setItem(13, item(Material.ARROW, ChatColor.GREEN + "Arrows x8", "Cost: 4 Gold"));
        inv.setItem(15, item(Material.DIAMOND, ChatColor.AQUA + "Generator Tier", "Cost: 2/4 Diamonds"));
        inv.setItem(16, item(Material.EMERALD, ChatColor.GREEN + "Protection", "Cost: 2 Emeralds"));
        player.openInventory(inv);
    }

    public static void handleClick(Player player, int slot, GameManager gameManager) {
        if (!gameManager.isRunning()) {
            player.sendMessage(ChatColor.RED + "Game not running.");
            return;
        }
        TeamData team = gameManager.getTeamData(player.getUniqueId());
        if (team == null) {
            player.sendMessage(ChatColor.RED + "Join a game first.");
            return;
        }
        switch (slot) {
            case 10 -> buyItem(player, gameManager, ResourceType.IRON, 4, new ItemStack(Material.WHITE_WOOL, 16));
            case 11 -> buyItem(player, gameManager, ResourceType.IRON, 10, new ItemStack(Material.STONE_SWORD));
            case 12 -> buyItem(player, gameManager, ResourceType.GOLD, 12, new ItemStack(Material.BOW));
            case 13 -> buyItem(player, gameManager, ResourceType.GOLD, 4, new ItemStack(Material.ARROW, 8));
            case 15 -> upgradeGenerator(player, gameManager, team);
            case 16 -> upgradeProtection(player, gameManager, team);
            default -> {
            }
        }
    }

    private static void buyItem(Player player, GameManager gameManager, ResourceType type, int cost, ItemStack item) {
        if (!gameManager.takeCurrency(player, type, cost)) {
            player.sendMessage(ChatColor.RED + "Not enough " + type.name().toLowerCase() + ".");
            return;
        }
        player.getInventory().addItem(item);
        player.sendMessage(ChatColor.GREEN + "Purchased.");
    }

    private static void upgradeGenerator(Player player, GameManager gameManager, TeamData team) {
        int tier = team.getGeneratorTier();
        if (tier >= 3) {
            player.sendMessage(ChatColor.YELLOW + "Generator is already max.");
            return;
        }
        int cost = tier == 1 ? 2 : 4;
        if (!gameManager.takeCurrency(player, ResourceType.DIAMOND, cost)) {
            player.sendMessage(ChatColor.RED + "Not enough diamonds.");
            return;
        }
        gameManager.upgradeGeneratorTier(team);
        player.sendMessage(ChatColor.GREEN + "Generator upgraded to tier " + team.getGeneratorTier() + ".");
    }

    private static void upgradeProtection(Player player, GameManager gameManager, TeamData team) {
        if (team.getProtectionLevel() >= 4) {
            player.sendMessage(ChatColor.YELLOW + "Protection is already max.");
            return;
        }
        if (!gameManager.takeCurrency(player, ResourceType.EMERALD, 2)) {
            player.sendMessage(ChatColor.RED + "Not enough emeralds.");
            return;
        }
        gameManager.upgradeProtection(team);
        player.sendMessage(ChatColor.GREEN + "Protection level " + team.getProtectionLevel() + ".");
    }

    private static ItemStack item(Material material, String name, String lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(ChatColor.GRAY + lore));
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
