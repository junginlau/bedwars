package com.example.bedwars.shop;

import com.example.bedwars.game.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class CategoryShop {
    public static final String TITLE_MAIN = "快速购买";
    public static final String TITLE_BLOCKS = "方块";
    public static final String TITLE_WEAPONS = "武器";
    public static final String TITLE_ARMOR = "盔甲";
    public static final String TITLE_TOOLS = "工具";
    public static final String TITLE_POTIONS = "药水";
    public static final String TITLE_SPECIAL = "特殊道具";
    public static final String TITLE_UPGRADES = "团队升级";

    public static void openMain(Player player) {
        Inventory inv = player.getServer().createInventory(player, 54, TITLE_MAIN);
        
        // 快速购买栏
        inv.setItem(1, quickBuyItem(Material.WHITE_WOOL, "羊毛", "4 铁锭"));
        inv.setItem(2, quickBuyItem(Material.STONE_SWORD, "石剑", "10 铁锭"));
        inv.setItem(3, quickBuyItem(Material.CHAINMAIL_BOOTS, "链甲护甲", "24 铁锭"));
        inv.setItem(4, quickBuyItem(Material.BOW, "弓", "12 金锭"));
        inv.setItem(5, quickBuyItem(Material.ARROW, "箭矢", "2 金锭"));
        inv.setItem(6, quickBuyItem(Material.GOLDEN_APPLE, "金苹果", "3 金锭"));
        inv.setItem(7, quickBuyItem(Material.TNT, "TNT", "4 金锭"));
        
        // 分类导航
        inv.setItem(19, categoryItem(Material.TERRACOTTA, ChatColor.GOLD + "方块", "购买建筑方块"));
        inv.setItem(20, categoryItem(Material.GOLDEN_SWORD, ChatColor.GOLD + "武器", "购买近战和远程武器"));
        inv.setItem(21, categoryItem(Material.CHAINMAIL_CHESTPLATE, ChatColor.GOLD + "盔甲", "购买护甲装备"));
        inv.setItem(22, categoryItem(Material.STONE_PICKAXE, ChatColor.GOLD + "工具", "购买工具"));
        inv.setItem(23, categoryItem(Material.POTION, ChatColor.GOLD + "药水", "购买药水"));
        inv.setItem(24, categoryItem(Material.TNT, ChatColor.GOLD + "特殊道具", "购买特殊物品"));
        inv.setItem(25, categoryItem(Material.DIAMOND, ChatColor.GOLD + "团队升级", "升级团队能力"));
        
        player.openInventory(inv);
    }

    public static void openBlocks(Player player) {
        Inventory inv = player.getServer().createInventory(player, 54, TITLE_BLOCKS);
        
        inv.setItem(10, shopItem(Material.WHITE_WOOL, "羊毛 x16", Arrays.asList(
            ChatColor.GRAY + "可以用来快速搭桥",
            ChatColor.GRAY + "和保护床铺",
            "",
            ChatColor.GOLD + "花费: 4 铁锭"
        )));
        inv.setItem(11, shopItem(Material.TERRACOTTA, "硬化粘土 x16", Arrays.asList(
            ChatColor.GRAY + "硬化粘土比羊毛",
            ChatColor.GRAY + "更难破坏",
            "",
            ChatColor.GOLD + "花费: 12 铁锭"
        )));
        inv.setItem(12, shopItem(Material.END_STONE, "末地石 x12", Arrays.asList(
            ChatColor.GRAY + "末地石非常",
            ChatColor.GRAY + "坚固",
            "",
            ChatColor.GOLD + "花费: 24 铁锭"
        )));
        inv.setItem(13, shopItem(Material.OAK_PLANKS, "木板 x16", Arrays.asList(
            ChatColor.GRAY + "木板可以用来",
            ChatColor.GRAY + "建造防御工事",
            "",
            ChatColor.GOLD + "花费: 4 金锭"
        )));
        inv.setItem(14, shopItem(Material.OBSIDIAN, "黑曜石 x4", Arrays.asList(
            ChatColor.GRAY + "黑曜石是最坚固",
            ChatColor.GRAY + "的防御方块",
            "",
            ChatColor.GOLD + "花费: 4 绿宝石"
        )));
        inv.setItem(15, shopItem(Material.LADDER, "梯子 x16", Arrays.asList(
            ChatColor.GRAY + "快速爬升",
            "",
            ChatColor.GOLD + "花费: 4 铁锭"
        )));
        
        inv.setItem(49, backItem());
        player.openInventory(inv);
    }

    public static void openWeapons(Player player, GameManager gameManager) {
        Inventory inv = player.getServer().createInventory(player, 54, TITLE_WEAPONS);
        
        TeamData team = gameManager.getTeamData(player.getUniqueId());
        PlayerUpgrades upgrades = gameManager.getPlayerUpgrades(player.getUniqueId());
        int swordTier = upgrades.getSwordTier();
        
        // 剑升级系统
        if (swordTier < 1) {
            inv.setItem(10, shopItem(Material.STONE_SWORD, "石剑", Arrays.asList(
                ChatColor.GRAY + "升级你的剑",
                "",
                ChatColor.GOLD + "花费: 10 铁锭"
            )));
        } else if (swordTier < 2) {
            inv.setItem(10, shopItem(Material.IRON_SWORD, "铁剑", Arrays.asList(
                ChatColor.GRAY + "升级你的剑",
                ChatColor.GREEN + "✓ 已拥有: 石剑",
                "",
                ChatColor.GOLD + "花费: 7 金锭"
            )));
        } else if (swordTier < 3) {
            inv.setItem(10, shopItem(Material.DIAMOND_SWORD, "钻石剑", Arrays.asList(
                ChatColor.GRAY + "升级你的剑",
                ChatColor.GREEN + "✓ 已拥有: 铁剑",
                "",
                ChatColor.GOLD + "花费: 4 绿宝石"
            )));
        } else {
            inv.setItem(10, shopItem(Material.DIAMOND_SWORD, "钻石剑", Arrays.asList(
                ChatColor.GREEN + "✓ 已升级到最高级"
            )));
        }
        
        inv.setItem(11, shopItem(Material.STICK, "击退棒", Arrays.asList(
            ChatColor.GRAY + "击退 I",
            "",
            ChatColor.GOLD + "花费: 5 金锭"
        )));
        
        inv.setItem(12, shopItem(Material.BOW, "弓", Arrays.asList(
            ChatColor.GRAY + "用于远程攻击",
            "",
            ChatColor.GOLD + "花费: 12 金锭"
        )));
        
        inv.setItem(13, shopItem(Material.BOW, "强力弓", Arrays.asList(
            ChatColor.GRAY + "力量 I",
            ChatColor.GRAY + "冲击 I",
            "",
            ChatColor.GOLD + "花费: 24 金锭"
        )));
        
        inv.setItem(14, shopItem(Material.ARROW, "箭矢 x8", Arrays.asList(
            ChatColor.GRAY + "远程武器弹药",
            "",
            ChatColor.GOLD + "花费: 2 金锭"
        )));
        
        inv.setItem(49, backItem());
        player.openInventory(inv);
    }

    public static void openArmor(Player player, GameManager gameManager) {
        Inventory inv = player.getServer().createInventory(player, 54, TITLE_ARMOR);
        
        PlayerUpgrades upgrades = gameManager.getPlayerUpgrades(player.getUniqueId());
        int armorTier = upgrades.getArmorTier();
        
        // 护甲升级系统
        if (armorTier < 1) {
            inv.setItem(10, shopItem(Material.CHAINMAIL_BOOTS, "铁链护甲", Arrays.asList(
                ChatColor.GRAY + "链甲靴子和护腿",
                ChatColor.GRAY + "永久保留",
                "",
                ChatColor.GOLD + "花费: 24 铁锭"
            )));
        } else if (armorTier < 2) {
            inv.setItem(10, shopItem(Material.IRON_BOOTS, "铁护甲", Arrays.asList(
                ChatColor.GRAY + "铁靴子和护腿",
                ChatColor.GRAY + "永久保留",
                ChatColor.GREEN + "✓ 已拥有: 链甲护甲",
                "",
                ChatColor.GOLD + "花费: 12 金锭"
            )));
        } else if (armorTier < 3) {
            inv.setItem(10, shopItem(Material.DIAMOND_BOOTS, "钻石护甲", Arrays.asList(
                ChatColor.GRAY + "钻石靴子和护腿",
                ChatColor.GRAY + "永久保留",
                ChatColor.GREEN + "✓ 已拥有: 铁护甲",
                "",
                ChatColor.GOLD + "花费: 6 绿宝石"
            )));
        } else {
            inv.setItem(10, shopItem(Material.DIAMOND_BOOTS, "钻石护甲", Arrays.asList(
                ChatColor.GREEN + "✓ 已升级到最高级"
            )));
        }
        
        inv.setItem(49, backItem());
        player.openInventory(inv);
    }

    public static void openTools(Player player, GameManager gameManager) {
        Inventory inv = player.getServer().createInventory(player, 54, TITLE_TOOLS);
        
        PlayerUpgrades upgrades = gameManager.getPlayerUpgrades(player.getUniqueId());
        int pickTier = upgrades.getPickaxeTier();
        int axeTier = upgrades.getAxeTier();
        
        // 镐子升级
        if (pickTier < 2) {
            inv.setItem(10, shopItem(Material.STONE_PICKAXE, pickTier == 0 ? "石镐" : "铁镐", Arrays.asList(
                ChatColor.GRAY + "升级你的镐子",
                pickTier > 0 ? ChatColor.GREEN + "✓ 已拥有: 石镐" : "",
                "",
                ChatColor.GOLD + "花费: " + (pickTier == 0 ? "10 铁锭" : "3 金锭")
            )));
        } else if (pickTier < 3) {
            inv.setItem(10, shopItem(Material.DIAMOND_PICKAXE, "钻石镐", Arrays.asList(
                ChatColor.GRAY + "升级你的镐子",
                ChatColor.GREEN + "✓ 已拥有: 铁镐",
                "",
                ChatColor.GOLD + "花费: 6 金锭"
            )));
        } else {
            inv.setItem(10, shopItem(Material.DIAMOND_PICKAXE, "钻石镐", Arrays.asList(
                ChatColor.GREEN + "✓ 已升级到最高级"
            )));
        }
        
        // 斧子升级
        if (axeTier < 2) {
            inv.setItem(11, shopItem(Material.STONE_AXE, axeTier == 0 ? "石斧" : "铁斧", Arrays.asList(
                ChatColor.GRAY + "升级你的斧子",
                axeTier > 0 ? ChatColor.GREEN + "✓ 已拥有: 石斧" : "",
                "",
                ChatColor.GOLD + "花费: " + (axeTier == 0 ? "10 铁锭" : "3 金锭")
            )));
        } else if (axeTier < 3) {
            inv.setItem(11, shopItem(Material.DIAMOND_AXE, "钻石斧", Arrays.asList(
                ChatColor.GRAY + "升级你的斧子",
                ChatColor.GREEN + "✓ 已拥有: 铁斧",
                "",
                ChatColor.GOLD + "花费: 6 金锭"
            )));
        } else {
            inv.setItem(11, shopItem(Material.DIAMOND_AXE, "钻石斧", Arrays.asList(
                ChatColor.GREEN + "✓ 已升级到最高级"
            )));
        }
        
        if (!upgrades.hasShears()) {
            inv.setItem(12, shopItem(Material.SHEARS, "剪刀", Arrays.asList(
                ChatColor.GRAY + "快速剪羊毛",
                ChatColor.GRAY + "永久保留",
                "",
                ChatColor.GOLD + "花费: 20 铁锭"
            )));
        } else {
            inv.setItem(12, shopItem(Material.SHEARS, "剪刀", Arrays.asList(
                ChatColor.GREEN + "✓ 已购买"
            )));
        }
        
        inv.setItem(49, backItem());
        player.openInventory(inv);
    }

    public static void openPotions(Player player) {
        Inventory inv = player.getServer().createInventory(player, 54, TITLE_POTIONS);
        
        inv.setItem(10, shopItem(Material.POTION, "速度药水 II (45秒)", Arrays.asList(
            ChatColor.GRAY + "速度 II (45秒)",
            "",
            ChatColor.GOLD + "花费: 1 绿宝石"
        )));
        
        inv.setItem(11, shopItem(Material.POTION, "跳跃提升药水 V (45秒)", Arrays.asList(
            ChatColor.GRAY + "跳跃提升 V (45秒)",
            "",
            ChatColor.GOLD + "花费: 1 绿宝石"
        )));
        
        inv.setItem(12, shopItem(Material.POTION, "隐身药水 (30秒)", Arrays.asList(
            ChatColor.GRAY + "隐身 (30秒)",
            ChatColor.GRAY + "护甲也会隐身",
            "",
            ChatColor.GOLD + "花费: 2 绿宝石"
        )));
        
        inv.setItem(49, backItem());
        player.openInventory(inv);
    }

    public static void openSpecial(Player player) {
        Inventory inv = player.getServer().createInventory(player, 54, TITLE_SPECIAL);
        
        inv.setItem(10, shopItem(Material.TNT, "TNT", Arrays.asList(
            ChatColor.GRAY + "立刻爆炸!",
            "",
            ChatColor.GOLD + "花费: 4 金锭"
        )));
        
        inv.setItem(11, shopItem(Material.ENDER_PEARL, "末影珍珠", Arrays.asList(
            ChatColor.GRAY + "快速传送",
            "",
            ChatColor.GOLD + "花费: 4 绿宝石"
        )));
        
        inv.setItem(12, shopItem(Material.FIRE_CHARGE, "火球", Arrays.asList(
            ChatColor.GRAY + "右键发射!",
            ChatColor.GRAY + "击退敌人并点燃方块",
            "",
            ChatColor.GOLD + "花费: 40 铁锭"
        )));
        
        inv.setItem(13, shopItem(Material.EGG, "桥蛋", Arrays.asList(
            ChatColor.GRAY + "扔出自动生成桥",
            "",
            ChatColor.GOLD + "花费: 1 绿宝石"
        )));
        
        inv.setItem(14, shopItem(Material.SNOWBALL, "爆炸球", Arrays.asList(
            ChatColor.GRAY + "击退敌人!",
            "",
            ChatColor.GOLD + "花费: 2 金锭"
        )));
        
        inv.setItem(15, shopItem(Material.COMPASS, "追踪指南针", Arrays.asList(
            ChatColor.GRAY + "指向最近的敌人",
            "",
            ChatColor.GOLD + "花费: 2 金锭"
        )));
        
        inv.setItem(16, shopItem(Material.PLAYER_HEAD, "梦之守卫者", Arrays.asList(
            ChatColor.GRAY + "保护床铺的陷阱",
            ChatColor.GRAY + "困住接近床的敌人",
            "",
            ChatColor.GOLD + "花费: 1 钻石"
        )));
        
        inv.setItem(17, shopItem(Material.GOLDEN_APPLE, "金苹果", Arrays.asList(
            ChatColor.GRAY + "恢复生命值",
            "",
            ChatColor.GOLD + "花费: 3 金锭"
        )));
        
        inv.setItem(49, backItem());
        player.openInventory(inv);
    }

    public static void openUpgrades(Player player, GameManager gameManager) {
        Inventory inv = player.getServer().createInventory(player, 54, TITLE_UPGRADES);
        
        TeamData team = gameManager.getTeamData(player.getUniqueId());
        if (team == null) {
            player.sendMessage(ChatColor.RED + "你不在游戏中！");
            return;
        }
        
        TeamUpgrades upgrades = team.getUpgrades();
        
        // 锋利附魔
        inv.setItem(10, upgradeItem(Material.IRON_SWORD, "锋利附魔",
            upgrades.getSharpnessLevel(),
            Arrays.asList(
                ChatColor.GRAY + "你的队伍将对敌人",
                ChatColor.GRAY + "造成额外伤害!",
                "",
                upgrades.getSharpnessLevel() == 0 ? 
                    ChatColor.GOLD + "花费: 4 钻石" : 
                    ChatColor.GREEN + "✓ 已购买"
            )
        ));
        
        // 保护附魔
        int protLevel = upgrades.getProtectionLevel();
        String protCost = protLevel == 0 ? "2 钻石" : 
                         protLevel == 1 ? "4 钻石" : 
                         protLevel == 2 ? "8 钻石" : 
                         protLevel == 3 ? "16 钻石" : "";
        inv.setItem(11, upgradeItem(Material.IRON_CHESTPLATE, "强化保护",
            protLevel,
            Arrays.asList(
                ChatColor.GRAY + "你的队伍受到的",
                ChatColor.GRAY + "伤害减少!",
                "",
                protLevel < 4 ? ChatColor.GOLD + "花费: " + protCost : ChatColor.GREEN + "✓ 已升到最高级"
            )
        ));
        
        // 急迫效果
        int hasteLevel = upgrades.getHasteLevel();
        inv.setItem(12, upgradeItem(Material.GOLDEN_PICKAXE, "急迫效果",
            hasteLevel,
            Arrays.asList(
                ChatColor.GRAY + "你的队伍挖掘",
                ChatColor.GRAY + "速度加快!",
                "",
                hasteLevel == 0 ? ChatColor.GOLD + "花费: 2 钻石" :
                hasteLevel == 1 ? ChatColor.GOLD + "花费: 4 钻石" :
                ChatColor.GREEN + "✓ 已升到最高级"
            )
        ));
        
        // 铁矿资源生成升级
        int genLevel = upgrades.getResourceGenLevel();
        inv.setItem(13, upgradeItem(Material.IRON_INGOT, "铁锭+金锭生成",
            genLevel - 1,
            Arrays.asList(
                ChatColor.GRAY + "升级基地资源",
                ChatColor.GRAY + "生成点速度",
                "",
                genLevel == 1 ? ChatColor.GOLD + "花费: 2 钻石" :
                genLevel == 2 ? ChatColor.GOLD + "花费: 4 钻石" :
                genLevel == 3 ? ChatColor.GOLD + "花费: 6 钻石" :
                ChatColor.GREEN + "✓ 已升到最高级"
            )
        ));
        
        // 治疗池
        inv.setItem(14, shopItem(Material.BEACON, "团队治疗池", Arrays.asList(
            ChatColor.GRAY + "在基地附近",
            ChatColor.GRAY + "持续恢复生命值!",
            "",
            upgrades.hasHealPool() ? 
                ChatColor.GREEN + "✓ 已购买" : 
                ChatColor.GOLD + "花费: 3 钻石"
        )));
        
        inv.setItem(49, backItem());
        player.openInventory(inv);
    }

    private static ItemStack shopItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack quickBuyItem(Material material, String name, String cost) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + name);
            meta.setLore(Arrays.asList(
                "",
                ChatColor.GOLD + "花费: " + cost,
                "",
                ChatColor.YELLOW + "左键: 查看详情",
                ChatColor.YELLOW + "右键: 快速购买"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack categoryItem(Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + description
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack upgradeItem(Material material, String name, int level, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + name);
            if (level > 0 && level <= 4) {
                meta.addEnchant(Enchantment.DURABILITY, level, true);
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack backItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "返回");
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isShopInventory(String title) {
        return title.equals(TITLE_MAIN) || title.equals(TITLE_BLOCKS) || 
               title.equals(TITLE_WEAPONS) || title.equals(TITLE_ARMOR) ||
               title.equals(TITLE_TOOLS) || title.equals(TITLE_POTIONS) ||
               title.equals(TITLE_SPECIAL) || title.equals(TITLE_UPGRADES);
    }
}
