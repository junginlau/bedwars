package com.example.bedwars.listener;

import com.example.bedwars.game.GameManager;
import com.example.bedwars.game.TeamColor;
import com.example.bedwars.game.TeamData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SpecialItemListener implements Listener {
    private final GameManager gameManager;
    private final JavaPlugin plugin;

    public SpecialItemListener(GameManager gameManager, JavaPlugin plugin) {
        this.gameManager = gameManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!gameManager.isRunning()) {
            return;
        }

        Projectile projectile = event.getEntity();
        
        // 桥蛋
        if (projectile instanceof Egg && projectile.hasMetadata("bridge_egg")) {
            handleBridgeEgg(event);
        }
        
        // 爆炸球
        if (projectile instanceof Snowball && projectile.hasMetadata("knockback_ball")) {
            handleKnockbackBall(event);
        }
        
        // 火球
        if (projectile instanceof Fireball && projectile.hasMetadata("bedwars_fireball")) {
            handleFireball(event);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!gameManager.isRunning()) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !event.hasItem()) {
            return;
        }

        // 追踪指南针
        if (item.getType() == Material.COMPASS && 
            (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            handleTrackingCompass(player);
        }
        
        // 桥蛋投掷
        if (item.getType() == Material.EGG && 
            item.hasItemMeta() && 
            item.getItemMeta().hasDisplayName() &&
            item.getItemMeta().getDisplayName().contains("桥蛋")) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Egg egg = player.launchProjectile(Egg.class);
                egg.setMetadata("bridge_egg", new FixedMetadataValue(plugin, true));
                egg.setShooter(player);
            }
        }
        
        // 爆炸球投掷
        if (item.getType() == Material.SNOWBALL && 
            item.hasItemMeta() && 
            item.getItemMeta().hasDisplayName() &&
            item.getItemMeta().getDisplayName().contains("爆炸球")) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Snowball snowball = player.launchProjectile(Snowball.class);
                snowball.setMetadata("knockback_ball", new FixedMetadataValue(plugin, true));
                snowball.setShooter(player);
            }
        }
        
        // 火球投掷
        if (item.getType() == Material.FIRE_CHARGE && 
            (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            Fireball fireball = player.launchProjectile(Fireball.class);
            fireball.setMetadata("bedwars_fireball", new FixedMetadataValue(plugin, true));
            fireball.setShooter(player);
            fireball.setYield(0); // 不破坏方块
            item.setAmount(item.getAmount() - 1);
        }
        
        // 梦之守卫者放置
        if (item.getType() == Material.PLAYER_HEAD && 
            item.hasItemMeta() && 
            item.getItemMeta().hasDisplayName() &&
            item.getItemMeta().getDisplayName().contains("梦之守卫者")) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                placeDreamDefender(player, event.getClickedBlock().getLocation().add(0, 1, 0));
                item.setAmount(item.getAmount() - 1);
            }
        }
    }

    private void handleBridgeEgg(ProjectileHitEvent event) {
        Location loc = event.getEntity().getLocation();
        Player shooter = (Player) event.getEntity().getShooter();
        
        if (shooter == null) {
            return;
        }
        
        TeamData team = gameManager.getTeamData(shooter.getUniqueId());
        if (team == null) {
            return;
        }
        
        // 获取队伍羊毛颜色
        Material wool = getTeamWool(team.getColor());
        
        // 在落地点生成一座桥
        Vector direction = shooter.getLocation().getDirection().normalize();
        direction.setY(0); // 保持水平
        
        Location current = loc.clone();
        for (int i = 0; i < 30; i++) {
            current.add(direction.multiply(1));
            
            // 如果这里已经有方块，停止
            if (current.getBlock().getType().isSolid()) {
                break;
            }
            
            // 向下寻找最近的表面（最多向下5格）
            Location bridgeLoc = current.clone();
            for (int down = 0; down < 5; down++) {
                if (bridgeLoc.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) {
                    break;
                }
                bridgeLoc.subtract(0, 1, 0);
            }
            
            // 放置羊毛
            if (bridgeLoc.getBlock().getType() == Material.AIR) {
                bridgeLoc.getBlock().setType(wool);
            }
        }
    }

    private void handleKnockbackBall(ProjectileHitEvent event) {
        Location loc = event.getEntity().getLocation();
        Player shooter = (Player) event.getEntity().getShooter();
        
        // 对周围的实体造成击退
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 3, 3, 3)) {
            if (entity instanceof Player target) {
                if (shooter != null && target.getUniqueId().equals(shooter.getUniqueId())) {
                    continue; // 不击退自己
                }
                
                // 检查是否是队友
                if (shooter != null) {
                    TeamColor shooterTeam = gameManager.getTeamData(shooter.getUniqueId()).getColor();
                    TeamData targetTeam = gameManager.getTeamData(target.getUniqueId());
                    if (targetTeam != null && targetTeam.getColor() == shooterTeam) {
                        continue; // 不击退队友
                    }
                }
                
                // 应用击退
                Vector direction = target.getLocation().toVector().subtract(loc.toVector()).normalize();
                direction.setY(0.5); // 向上击退
                target.setVelocity(direction.multiply(1.5));
            }
        }
        
        // 特效
        loc.getWorld().createExplosion(loc, 0F, false, false);
    }

    private void handleFireball(ProjectileHitEvent event) {
        Location loc = event.getEntity().getLocation();
        Player shooter = (Player) event.getEntity().getShooter();
        
        // 对周围的实体造成伤害和击退
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 3, 3, 3)) {
            if (entity instanceof Player target) {
                if (shooter != null && target.getUniqueId().equals(shooter.getUniqueId())) {
                    continue;
                }
                
                // 检查是否是队友
                if (shooter != null) {
                    TeamData shooterTeam = gameManager.getTeamData(shooter.getUniqueId());
                    TeamData targetTeam = gameManager.getTeamData(target.getUniqueId());
                    if (shooterTeam != null && targetTeam != null && targetTeam.getColor() == shooterTeam.getColor()) {
                        continue;
                    }
                }
                
                // 点燃
                target.setFireTicks(100);
                
                // 击退
                Vector direction = target.getLocation().toVector().subtract(loc.toVector()).normalize();
                direction.setY(0.4);
                target.setVelocity(direction.multiply(1.2));
            }
        }
        
        // 爆炸效果
        loc.getWorld().createExplosion(loc, 0F, false, false);
    }

    private void handleTrackingCompass(Player player) {
        TeamData playerTeam = gameManager.getTeamData(player.getUniqueId());
        if (playerTeam == null) {
            return;
        }
        
        Player nearestEnemy = null;
        double minDistance = Double.MAX_VALUE;
        
        // 查找最近的敌人
        for (TeamData team : gameManager.getTeams().values()) {
            if (team.getColor() == playerTeam.getColor()) {
                continue;
            }
            
            for (java.util.UUID uuid : team.getPlayers()) {
                Player enemy = player.getServer().getPlayer(uuid);
                if (enemy != null && enemy.isOnline()) {
                    double distance = player.getLocation().distance(enemy.getLocation());
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestEnemy = enemy;
                    }
                }
            }
        }
        
        if (nearestEnemy != null) {
            player.setCompassTarget(nearestEnemy.getLocation());
            player.sendMessage(ChatColor.GREEN + "指南针指向 " + nearestEnemy.getName() + " (" + (int)minDistance + "m)");
        } else {
            player.sendMessage(ChatColor.RED + "没有找到敌人！");
        }
    }

    private void placeDreamDefender(Player player, Location loc) {
        TeamData team = gameManager.getTeamData(player.getUniqueId());
        if (team == null) {
            return;
        }
        
        // 在该位置放置一个隐形的盔甲架作为陷阱标记
        ArmorStand trap = loc.getWorld().spawn(loc, ArmorStand.class);
        trap.setVisible(false);
        trap.setGravity(false);
        trap.setInvulnerable(true);
        trap.setCustomName("dream_defender_" + team.getColor().name());
        trap.setCustomNameVisible(false);
        trap.setMetadata("dream_defender", new FixedMetadataValue(plugin, team.getColor().name()));
        
        player.sendMessage(ChatColor.GREEN + "放置了梦之守卫者！");
        
        // 启动检测任务（30秒后移除）
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (!gameManager.isRunning() || !trap.isValid() || ticks >= 600) {
                    trap.remove();
                    cancel();
                    return;
                }
                
                // 检测附近的敌人
                for (Entity entity : trap.getNearbyEntities(3, 3, 3)) {
                    if (entity instanceof Player enemy) {
                        TeamData enemyTeam = gameManager.getTeamData(enemy.getUniqueId());
                        if (enemyTeam != null && enemyTeam.getColor() != team.getColor()) {
                            // 触发陷阱
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                            enemy.sendMessage(ChatColor.RED + "你触发了梦之守卫者！");
                            player.sendMessage(ChatColor.GREEN + enemy.getName() + " 触发了你的陷阱！");
                            
                            trap.remove();
                            cancel();
                            return;
                        }
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }

    private Material getTeamWool(TeamColor color) {
        return switch (color) {
            case RED -> Material.RED_WOOL;
            case BLUE -> Material.BLUE_WOOL;
            case GREEN -> Material.GREEN_WOOL;
            case YELLOW -> Material.YELLOW_WOOL;
            case AQUA -> Material.CYAN_WOOL;
            case WHITE -> Material.WHITE_WOOL;
            case PINK -> Material.PINK_WOOL;
            case GRAY -> Material.GRAY_WOOL;
        };
    }
}
