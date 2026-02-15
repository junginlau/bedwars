package com.example.bedwars.shop;

import com.example.bedwars.game.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PurchaseHandler {
    private final GameManager gameManager;

    public PurchaseHandler(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public boolean handlePurchase(Player player, String category, int slot) {
        TeamData team = gameManager.getTeamData(player.getUniqueId());
        if (team == null) {
            player.sendMessage(ChatColor.RED + "你不在游戏中！");
            return false;
        }

        PlayerUpgrades upgrades = gameManager.getPlayerUpgrades(player.getUniqueId());
        
        switch (category) {
            case CategoryShop.TITLE_BLOCKS -> {
                return buyBlocks(player, slot);
            }
            case CategoryShop.TITLE_WEAPONS -> {
                return buyWeapons(player, slot, upgrades);
            }
            case CategoryShop.TITLE_ARMOR -> {
                return buyArmor(player, slot, upgrades);
            }
            case CategoryShop.TITLE_TOOLS -> {
                return buyTools(player, slot, upgrades);
            }
            case CategoryShop.TITLE_POTIONS -> {
                return buyPotions(player, slot);
            }
            case CategoryShop.TITLE_SPECIAL -> {
                return buySpecial(player, slot);
            }
            case CategoryShop.TITLE_UPGRADES -> {
                return buyUpgrades(player, slot, team);
            }
        }
        return false;
    }

    public boolean handleQuickBuy(Player player, int slot) {
        TeamData team = gameManager.getTeamData(player.getUniqueId());
        if (team == null) {
            player.sendMessage(ChatColor.RED + "你不在游戏中！");
            return false;
        }

        PlayerUpgrades upgrades = gameManager.getPlayerUpgrades(player.getUniqueId());

        return switch (slot) {
            case 1 -> buyBlocks(player, 10); // 羊毛
            case 2 -> buyWeapons(player, 10, upgrades); // 剑升级
            case 3 -> buyArmor(player, 10, upgrades); // 护甲升级
            case 4 -> buyWeapons(player, 12, upgrades); // 弓
            case 5 -> buyWeapons(player, 14, upgrades); // 箭
            case 6 -> buySpecial(player, 17); // 金苹果
            case 7 -> buySpecial(player, 10); // TNT
            default -> false;
        };
    }

    private boolean buyBlocks(Player player, int slot) {
        return switch (slot) {
            case 10 -> takeCurrency(player, ResourceType.IRON, 4) && giveItem(player, new ItemStack(Material.WHITE_WOOL, 16));
            case 11 -> takeCurrency(player, ResourceType.IRON, 12) && giveItem(player, new ItemStack(Material.TERRACOTTA, 16));
            case 12 -> takeCurrency(player, ResourceType.IRON, 24) && giveItem(player, new ItemStack(Material.END_STONE, 12));
            case 13 -> takeCurrency(player, ResourceType.GOLD, 4) && giveItem(player, new ItemStack(Material.OAK_PLANKS, 16));
            case 14 -> takeCurrency(player, ResourceType.EMERALD, 4) && giveItem(player, new ItemStack(Material.OBSIDIAN, 4));
            case 15 -> takeCurrency(player, ResourceType.IRON, 4) && giveItem(player, new ItemStack(Material.LADDER, 16));
            default -> false;
        };
    }

    private boolean buyWeapons(Player player, int slot, PlayerUpgrades upgrades) {
        int swordTier = upgrades.getSwordTier();
        
        return switch (slot) {
            case 10 -> {
                if (swordTier == 0 && takeCurrency(player, ResourceType.IRON, 10)) {
                    upgrades.setSwordTier(1);
                    giveSword(player, 1, upgrades);
                    yield true;
                } else if (swordTier == 1 && takeCurrency(player, ResourceType.GOLD, 7)) {
                    upgrades.setSwordTier(2);
                    giveSword(player, 2, upgrades);
                    yield true;
                } else if (swordTier == 2 && takeCurrency(player, ResourceType.EMERALD, 4)) {
                    upgrades.setSwordTier(3);
                    giveSword(player, 3, upgrades);
                    yield true;
                }
                yield false;
            }
            case 11 -> {
                if (takeCurrency(player, ResourceType.GOLD, 5)) {
                    ItemStack stick = new ItemStack(Material.STICK);
                    ItemMeta meta = stick.getItemMeta();
                    meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
                    meta.setDisplayName(ChatColor.GREEN + "击退棒");
                    stick.setItemMeta(meta);
                    yield giveItem(player, stick);
                }
                yield false;
            }
            case 12 -> takeCurrency(player, ResourceType.GOLD, 12) && giveItem(player, new ItemStack(Material.BOW));
            case 13 -> {
                if (takeCurrency(player, ResourceType.GOLD, 24)) {
                    ItemStack bow = new ItemStack(Material.BOW);
                    ItemMeta meta = bow.getItemMeta();
                    meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
                    meta.addEnchant(Enchantment.ARROW_KNOCKBACK, 1, true);
                    meta.setDisplayName(ChatColor.GREEN + "强力弓");
                    bow.setItemMeta(meta);
                    yield giveItem(player, bow);
                }
                yield false;
            }
            case 14 -> takeCurrency(player, ResourceType.GOLD, 2) && giveItem(player, new ItemStack(Material.ARROW, 8));
            default -> false;
        };
    }

    private boolean buyArmor(Player player, int slot, PlayerUpgrades upgrades) {
        int armorTier = upgrades.getArmorTier();
        
        if (slot == 10) {
            if (armorTier == 0 && takeCurrency(player, ResourceType.IRON, 24)) {
                upgrades.setArmorTier(1);
                giveArmor(player, 1);
                return true;
            } else if (armorTier == 1 && takeCurrency(player, ResourceType.GOLD, 12)) {
                upgrades.setArmorTier(2);
                giveArmor(player, 2);
                return true;
            } else if (armorTier == 2 && takeCurrency(player, ResourceType.EMERALD, 6)) {
                upgrades.setArmorTier(3);
                giveArmor(player, 3);
                return true;
            }
        }
        return false;
    }

    private boolean buyTools(Player player, int slot, PlayerUpgrades upgrades) {
        int pickTier = upgrades.getPickaxeTier();
        int axeTier = upgrades.getAxeTier();
        
        return switch (slot) {
            case 10 -> {
                if (pickTier == 0 && takeCurrency(player, ResourceType.IRON, 10)) {
                    upgrades.setPickaxeTier(1);
                    givePickaxe(player, 1);
                    yield true;
                } else if (pickTier == 1 && takeCurrency(player, ResourceType.GOLD, 3)) {
                    upgrades.setPickaxeTier(2);
                    givePickaxe(player, 2);
                    yield true;
                } else if (pickTier == 2 && takeCurrency(player, ResourceType.GOLD, 6)) {
                    upgrades.setPickaxeTier(3);
                    givePickaxe(player, 3);
                    yield true;
                }
                yield false;
            }
            case 11 -> {
                if (axeTier == 0 && takeCurrency(player, ResourceType.IRON, 10)) {
                    upgrades.setAxeTier(1);
                    giveAxe(player, 1);
                    yield true;
                } else if (axeTier == 1 && takeCurrency(player, ResourceType.GOLD, 3)) {
                    upgrades.setAxeTier(2);
                    giveAxe(player, 2);
                    yield true;
                } else if (axeTier == 2 && takeCurrency(player, ResourceType.GOLD, 6)) {
                    upgrades.setAxeTier(3);
                    giveAxe(player, 3);
                    yield true;
                }
                yield false;
            }
            case 12 -> {
                if (!upgrades.hasShears() && takeCurrency(player, ResourceType.IRON, 20)) {
                    upgrades.setHasShears(true);
                    yield giveItem(player, new ItemStack(Material.SHEARS));
                }
                yield false;
            }
            default -> false;
        };
    }

    private boolean buyPotions(Player player, int slot) {
        return switch (slot) {
            case 10 -> {
                if (takeCurrency(player, ResourceType.EMERALD, 1)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 900, 1));
                    player.sendMessage(ChatColor.GREEN + "你获得了速度效果！");
                    yield true;
                }
                yield false;
            }
            case 11 -> {
                if (takeCurrency(player, ResourceType.EMERALD, 1)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 900, 4));
                    player.sendMessage(ChatColor.GREEN + "你获得了跳跃提升效果！");
                    yield true;
                }
                yield false;
            }
            case 12 -> {
                if (takeCurrency(player, ResourceType.EMERALD, 2)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 600, 0));
                    player.sendMessage(ChatColor.GREEN + "你获得了隐身效果！");
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    private boolean buySpecial(Player player, int slot) {
        return switch (slot) {
            case 10 -> takeCurrency(player, ResourceType.GOLD, 4) && giveItem(player, new ItemStack(Material.TNT));
            case 11 -> takeCurrency(player, ResourceType.EMERALD, 4) && giveItem(player, new ItemStack(Material.ENDER_PEARL));
            case 12 -> takeCurrency(player, ResourceType.IRON, 40) && giveItem(player, new ItemStack(Material.FIRE_CHARGE));
            case 13 -> takeCurrency(player, ResourceType.EMERALD, 1) && giveItem(player, createBridgeEgg());
            case 14 -> takeCurrency(player, ResourceType.GOLD, 2) && giveItem(player, createKnockbackBall());
            case 15 -> takeCurrency(player, ResourceType.GOLD, 2) && giveItem(player, new ItemStack(Material.COMPASS));
            case 16 -> takeCurrency(player, ResourceType.DIAMOND, 1) && giveItem(player, createTrap());
            case 17 -> takeCurrency(player, ResourceType.GOLD, 3) && giveItem(player, new ItemStack(Material.GOLDEN_APPLE));
            default -> false;
        };
    }

    private boolean buyUpgrades(Player player, int slot, TeamData team) {
        TeamUpgrades upgrades = team.getUpgrades();
        
        return switch (slot) {
            case 10 -> {
                if (upgrades.getSharpnessLevel() == 0 && takeCurrency(player, ResourceType.DIAMOND, 4)) {
                    upgrades.upgradeSharpness();
                    team.getPlayers().forEach(uuid -> {
                        Player p = player.getServer().getPlayer(uuid);
                        if (p != null) {
                            p.sendMessage(ChatColor.GREEN + player.getName() + " 购买了团队锋利附魔！");
                        }
                    });
                    yield true;
                }
                yield false;
            }
            case 11 -> {
                int protLevel = upgrades.getProtectionLevel();
                int cost = protLevel == 0 ? 2 : protLevel == 1 ? 4 : protLevel == 2 ? 8 : protLevel == 3 ? 16 : 0;
                if (protLevel < 4 && takeCurrency(player, ResourceType.DIAMOND, cost)) {
                    upgrades.upgradeProtection();
                    team.getPlayers().forEach(uuid -> {
                        Player p = player.getServer().getPlayer(uuid);
                        if (p != null) {
                            p.sendMessage(ChatColor.GREEN + player.getName() + " 购买了团队保护 " + (protLevel + 1) + "！");
                        }
                    });
                    yield true;
                }
                yield false;
            }
            case 12 -> {
                int hasteLevel = upgrades.getHasteLevel();
                int cost = hasteLevel == 0 ? 2 : 4;
                if (hasteLevel < 2 && takeCurrency(player, ResourceType.DIAMOND, cost)) {
                    upgrades.upgradeHaste();
                    team.getPlayers().forEach(uuid -> {
                        Player p = player.getServer().getPlayer(uuid);
                        if (p != null) {
                            p.sendMessage(ChatColor.GREEN + player.getName() + " 购买了团队急迫 " + (hasteLevel + 1) + "！");
                            p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, hasteLevel));
                        }
                    });
                    yield true;
                }
                yield false;
            }
            case 13 -> {
                int genLevel = upgrades.getResourceGenLevel();
                int cost = genLevel == 1 ? 2 : genLevel == 2 ? 4 : genLevel == 3 ? 6 : 0;
                if (genLevel < 4 && takeCurrency(player, ResourceType.DIAMOND, cost)) {
                    upgrades.upgradeResourceGen();
                    gameManager.updateGeneratorSpeed(team);
                    team.getPlayers().forEach(uuid -> {
                        Player p = player.getServer().getPlayer(uuid);
                        if (p != null) {
                            p.sendMessage(ChatColor.GREEN + player.getName() + " 升级了资源生成器！");
                        }
                    });
                    yield true;
                }
                yield false;
            }
            case 14 -> {
                if (!upgrades.hasHealPool() && takeCurrency(player, ResourceType.DIAMOND, 3)) {
                    upgrades.enableHealPool();
                    team.getPlayers().forEach(uuid -> {
                        Player p = player.getServer().getPlayer(uuid);
                        if (p != null) {
                            p.sendMessage(ChatColor.GREEN + player.getName() + " 购买了团队治疗池！");
                        }
                    });
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    private boolean takeCurrency(Player player, ResourceType type, int amount) {
        ItemStack cost = new ItemStack(type.getMaterial(), amount);
        if (!player.getInventory().containsAtLeast(cost, amount)) {
            player.sendMessage(ChatColor.RED + "资源不足！需要 " + amount + " " + getResourceName(type));
            return false;
        }
        player.getInventory().removeItem(cost);
        return true;
    }

    private String getResourceName(ResourceType type) {
        return switch (type) {
            case IRON -> "铁锭";
            case GOLD -> "金锭";
            case DIAMOND -> "钻石";
            case EMERALD -> "绿宝石";
        };
    }

    private boolean giveItem(Player player, ItemStack item) {
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "背包已满！");
            return false;
        }
        player.getInventory().addItem(item);
        player.sendMessage(ChatColor.GREEN + "购买成功！");
        return true;
    }

    private void giveSword(Player player, int tier, PlayerUpgrades upgrades) {
        // 移除旧剑
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && (item.getType().name().endsWith("_SWORD") || item.getType() == Material.STICK)) {
                player.getInventory().remove(item);
            }
        }
        
        Material swordMat = switch (tier) {
            case 1 -> Material.STONE_SWORD;
            case 2 -> Material.IRON_SWORD;
            case 3 -> Material.DIAMOND_SWORD;
            default -> Material.WOODEN_SWORD;
        };
        
        ItemStack sword = new ItemStack(swordMat);
        TeamData team = gameManager.getTeamData(player.getUniqueId());
        if (team != null && team.getUpgrades().getSharpnessLevel() > 0) {
            ItemMeta meta = sword.getItemMeta();
            meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
            sword.setItemMeta(meta);
        }
        
        giveItem(player, sword);
    }

    private void givePickaxe(Player player, int tier) {
        // 移除旧镐
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType().name().endsWith("_PICKAXE")) {
                player.getInventory().remove(item);
            }
        }
        
        Material pickMat = switch (tier) {
            case 1 -> Material.STONE_PICKAXE;
            case 2 -> Material.IRON_PICKAXE;
            case 3 -> Material.DIAMOND_PICKAXE;
            default -> Material.WOODEN_PICKAXE;
        };
        
        ItemStack pick = new ItemStack(pickMat);
        ItemMeta meta = pick.getItemMeta();
        meta.addEnchant(Enchantment.DIG_SPEED, 1, true);
        meta.setUnbreakable(true);
        pick.setItemMeta(meta);
        
        giveItem(player, pick);
    }

    private void giveAxe(Player player, int tier) {
        // 移除旧斧
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType().name().endsWith("_AXE")) {
                player.getInventory().remove(item);
            }
        }
        
        Material axeMat = switch (tier) {
            case 1 -> Material.STONE_AXE;
            case 2 -> Material.IRON_AXE;
            case 3 -> Material.DIAMOND_AXE;
            default -> Material.WOODEN_AXE;
        };
        
        ItemStack axe = new ItemStack(axeMat);
        ItemMeta meta = axe.getItemMeta();
        meta.addEnchant(Enchantment.DIG_SPEED, 1, true);
        meta.setUnbreakable(true);
        axe.setItemMeta(meta);
        
        giveItem(player, axe);
    }

    private void giveArmor(Player player, int tier) {
        Material bootsMat, legsMat;
        switch (tier) {
            case 1 -> {
                bootsMat = Material.CHAINMAIL_BOOTS;
                legsMat = Material.CHAINMAIL_LEGGINGS;
            }
            case 2 -> {
                bootsMat = Material.IRON_BOOTS;
                legsMat = Material.IRON_LEGGINGS;
            }
            case 3 -> {
                bootsMat = Material.DIAMOND_BOOTS;
                legsMat = Material.DIAMOND_LEGGINGS;
            }
            default -> {
                return;
            }
        }
        
        ItemStack boots = new ItemStack(bootsMat);
        ItemStack legs = new ItemStack(legsMat);
        
        TeamData team = gameManager.getTeamData(player.getUniqueId());
        if (team != null) {
            int protLevel = team.getUpgrades().getProtectionLevel();
            if (protLevel > 0) {
                ItemMeta bootsMeta = boots.getItemMeta();
                bootsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, protLevel, true);
                boots.setItemMeta(bootsMeta);
                
                ItemMeta legsMeta = legs.getItemMeta();
                legsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, protLevel, true);
                legs.setItemMeta(legsMeta);
            }
        }
        
        boots.getItemMeta().setUnbreakable(true);
        legs.getItemMeta().setUnbreakable(true);
        
        player.getInventory().setBoots(boots);
        player.getInventory().setLeggings(legs);
        player.sendMessage(ChatColor.GREEN + "护甲已自动穿戴！");
    }

    private ItemStack createBridgeEgg() {
        ItemStack egg = new ItemStack(Material.EGG);
        ItemMeta meta = egg.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "桥蛋");
        egg.setItemMeta(meta);
        return egg;
    }

    private ItemStack createKnockbackBall() {
        ItemStack ball = new ItemStack(Material.SNOWBALL);
        ItemMeta meta = ball.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "爆炸球");
        ball.setItemMeta(meta);
        return ball;
    }

    private ItemStack createTrap() {
        ItemStack trap = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = trap.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "梦之守卫者");
        trap.setItemMeta(meta);
        return trap;
    }
}
