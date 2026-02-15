package com.example.bedwars.game;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.BooleanSupplier;

public class ResourceGenerator {
    private final ResourceType type;
    private final Location location;
    private int intervalTicks;
    private int amount;
    private BukkitTask task;

    public ResourceGenerator(ResourceType type, Location location, int intervalTicks, int amount) {
        this.type = type;
        this.location = location;
        this.intervalTicks = intervalTicks;
        this.amount = amount;
    }

    public void start(JavaPlugin plugin, BooleanSupplier shouldRun) {
        stop();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!shouldRun.getAsBoolean()) {
                    return;
                }
                ItemStack stack = type.toItem(amount);
                Item item = location.getWorld().dropItemNaturally(location, stack);
                item.setPickupDelay(0);
            }
        }.runTaskTimer(plugin, intervalTicks, intervalTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public ResourceType getType() {
        return type;
    }

    public void setIntervalTicks(int intervalTicks) {
        this.intervalTicks = intervalTicks;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getIntervalTicks() {
        return intervalTicks;
    }
}
