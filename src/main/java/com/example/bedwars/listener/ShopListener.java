package com.example.bedwars.listener;

import com.example.bedwars.game.GameManager;
import com.example.bedwars.shop.CategoryShop;
import com.example.bedwars.shop.PurchaseHandler;
import com.example.bedwars.shop.ShopNPCManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class ShopListener implements Listener {
    private final GameManager gameManager;
    private final ShopNPCManager shopNPCManager;
    private final PurchaseHandler purchaseHandler;

    public ShopListener(GameManager gameManager, ShopNPCManager shopNPCManager) {
        this.gameManager = gameManager;
        this.shopNPCManager = shopNPCManager;
        this.purchaseHandler = new PurchaseHandler(gameManager);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        String title = event.getView().getTitle();

        if (!CategoryShop.isShopInventory(title)) {
            return;
        }

        event.setCancelled(true);

        if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return;
        }

        int slot = event.getRawSlot();

        if (title.equals(CategoryShop.TITLE_MAIN)) {
            handleMainShopClick(player, slot, event.isRightClick());
        } else if (title.equals(CategoryShop.TITLE_BLOCKS)) {
            handleShopClick(player, CategoryShop.TITLE_BLOCKS, slot);
        } else if (title.equals(CategoryShop.TITLE_WEAPONS)) {
            handleShopClick(player, CategoryShop.TITLE_WEAPONS, slot);
        } else if (title.equals(CategoryShop.TITLE_ARMOR)) {
            handleShopClick(player, CategoryShop.TITLE_ARMOR, slot);
        } else if (title.equals(CategoryShop.TITLE_TOOLS)) {
            handleShopClick(player, CategoryShop.TITLE_TOOLS, slot);
        } else if (title.equals(CategoryShop.TITLE_POTIONS)) {
            handleShopClick(player, CategoryShop.TITLE_POTIONS, slot);
        } else if (title.equals(CategoryShop.TITLE_SPECIAL)) {
            handleShopClick(player, CategoryShop.TITLE_SPECIAL, slot);
        } else if (title.equals(CategoryShop.TITLE_UPGRADES)) {
            handleShopClick(player, CategoryShop.TITLE_UPGRADES, slot);
        }
    }
    
    private void handleMainShopClick(Player player, int slot, boolean rightClick) {
        if (rightClick) {
            if (purchaseHandler.handleQuickBuy(player, slot)) {
                CategoryShop.openMain(player);
            }
            return;
        }

        // 第一排(slot 1-7)直接购买快速购买栏的物品
        if (slot >= 1 && slot <= 7) {
            if (purchaseHandler.handleQuickBuy(player, slot)) {
                CategoryShop.openMain(player);
            }
            return;
        }

        // 第三排(slot 19-25)打开分类菜单
        switch (slot) {
            case 19 -> CategoryShop.openBlocks(player);
            case 20 -> CategoryShop.openWeapons(player, gameManager);
            case 21 -> CategoryShop.openArmor(player, gameManager);
            case 22 -> CategoryShop.openTools(player, gameManager);
            case 23 -> CategoryShop.openPotions(player);
            case 24 -> CategoryShop.openSpecial(player);
            case 25 -> CategoryShop.openUpgrades(player, gameManager);
        }
    }
    
    private void handleShopClick(Player player, String category, int slot) {
        // 返回按钮
        if (slot == 49) {
            CategoryShop.openMain(player);
            return;
        }
        
        // 尝试购买
        if (purchaseHandler.handlePurchase(player, category, slot)) {
            // 刷新当前界面显示升级后的内容
            refreshShop(player, category);
        }
    }
    
    private void refreshShop(Player player, String category) {
        switch (category) {
            case CategoryShop.TITLE_WEAPONS -> CategoryShop.openWeapons(player, gameManager);
            case CategoryShop.TITLE_ARMOR -> CategoryShop.openArmor(player, gameManager);
            case CategoryShop.TITLE_TOOLS -> CategoryShop.openTools(player, gameManager);
            case CategoryShop.TITLE_UPGRADES -> CategoryShop.openUpgrades(player, gameManager);
            default -> {} // 其他分类无需刷新
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        if (CategoryShop.isShopInventory(title)) {
            event.getInventory().clear();
        }
    }

    @EventHandler
    public void onShopNPCClick(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!shopNPCManager.isShopNPC(entity)) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        CategoryShop.openMain(player);
    }

    @EventHandler
    public void onShopNPCInteractAt(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!shopNPCManager.isShopNPC(entity)) {
            return;
        }
        event.setCancelled(true);
    }
}
