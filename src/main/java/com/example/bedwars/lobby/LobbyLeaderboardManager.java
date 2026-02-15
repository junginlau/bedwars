package com.example.bedwars.lobby;

import com.example.bedwars.stats.PlayerStats;
import com.example.bedwars.stats.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class LobbyLeaderboardManager {
    private static final String CONFIG_FILE = "lobby-leaderboard.yml";
    private static final int DISPLAY_COUNT = 10;
    private static final double LINE_GAP = 0.25;
    private static final long ROTATE_TICKS = 20L * 15;

    private final JavaPlugin plugin;
    private final StatsManager statsManager;
    private final File configFile;
    private final List<ArmorStand> stands = new ArrayList<>();
    private final Map<LeaderboardCategory, Location> fixedLocations = new EnumMap<>(LeaderboardCategory.class);
    private final Map<LeaderboardCategory, List<ArmorStand>> fixedStands = new EnumMap<>(LeaderboardCategory.class);

    private Location baseLocation;
    private BukkitTask rotateTask;
    private int categoryIndex;
    private boolean rotateMode = true;
    private int fixedSetIndex;

    public LobbyLeaderboardManager(JavaPlugin plugin, StatsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
        this.configFile = new File(plugin.getDataFolder(), CONFIG_FILE);
    }

    public void load() {
        if (!configFile.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        String mode = config.getString("mode", "rotate");
        rotateMode = !"fixed".equalsIgnoreCase(mode);

        String worldName = config.getString("rotate.world");
        World world = worldName != null ? Bukkit.getWorld(worldName) : null;
        if (world != null) {
            double x = config.getDouble("rotate.x");
            double y = config.getDouble("rotate.y");
            double z = config.getDouble("rotate.z");
            float yaw = (float) config.getDouble("rotate.yaw", 0.0);
            float pitch = (float) config.getDouble("rotate.pitch", 0.0);
            baseLocation = new Location(world, x, y, z, yaw, pitch);
        }

        fixedLocations.clear();
        for (LeaderboardCategory category : LeaderboardCategory.values()) {
            String path = "fixed." + category.name().toLowerCase();
            String fixedWorld = config.getString(path + ".world");
            World fixedW = fixedWorld != null ? Bukkit.getWorld(fixedWorld) : null;
            if (fixedW == null) {
                continue;
            }
            double x = config.getDouble(path + ".x");
            double y = config.getDouble(path + ".y");
            double z = config.getDouble(path + ".z");
            float yaw = (float) config.getDouble(path + ".yaw", 0.0);
            float pitch = (float) config.getDouble(path + ".pitch", 0.0);
            fixedLocations.put(category, new Location(fixedW, x, y, z, yaw, pitch));
        }
        spawn();
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("mode", rotateMode ? "rotate" : "fixed");
        if (baseLocation != null) {
            config.set("rotate.world", baseLocation.getWorld().getName());
            config.set("rotate.x", baseLocation.getX());
            config.set("rotate.y", baseLocation.getY());
            config.set("rotate.z", baseLocation.getZ());
            config.set("rotate.yaw", baseLocation.getYaw());
            config.set("rotate.pitch", baseLocation.getPitch());
        }
        for (Map.Entry<LeaderboardCategory, Location> entry : fixedLocations.entrySet()) {
            Location loc = entry.getValue();
            String path = "fixed." + entry.getKey().name().toLowerCase();
            config.set(path + ".world", loc.getWorld().getName());
            config.set(path + ".x", loc.getX());
            config.set(path + ".y", loc.getY());
            config.set(path + ".z", loc.getZ());
            config.set(path + ".yaw", loc.getYaw());
            config.set(path + ".pitch", loc.getPitch());
        }
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save lobby leaderboard: " + e.getMessage());
        }
    }

    public void setLocation(Location location) {
        baseLocation = location;
        save();
        respawn();
    }

    public void setFixedLocation(LeaderboardCategory category, Location location) {
        fixedLocations.put(category, location);
        save();
        respawn();
    }

    public void setNextFixedLocation(Location location) {
        LeaderboardCategory next = getNextFixedCategory();
        setFixedLocation(next, location);
        fixedSetIndex = (next.ordinal() + 1) % LeaderboardCategory.values().length;
    }

    public Location getLocation() {
        return baseLocation;
    }

    public boolean isRotateMode() {
        return rotateMode;
    }

    public void toggleMode() {
        rotateMode = !rotateMode;
        save();
        respawn();
    }

    public boolean hasRotateLocation() {
        return baseLocation != null;
    }

    public int getFixedCount() {
        return fixedLocations.size();
    }

    public int getFixedTotal() {
        return LeaderboardCategory.values().length;
    }

    public String getNextFixedCategoryName() {
        return getCategoryDisplayName(getNextFixedCategory());
    }

    public void remove() {
        stopRotation();
        for (ArmorStand stand : stands) {
            if (stand != null && !stand.isDead()) {
                stand.remove();
            }
        }
        stands.clear();
        for (List<ArmorStand> list : fixedStands.values()) {
            for (ArmorStand stand : list) {
                if (stand != null && !stand.isDead()) {
                    stand.remove();
                }
            }
        }
        fixedStands.clear();
    }

    public void respawn() {
        remove();
        spawn();
    }

    public void nextCategory() {
        if (!rotateMode) {
            return;
        }
        categoryIndex = (categoryIndex + 1) % LeaderboardCategory.values().length;
        updateRotateLines();
    }

    public void refreshNow() {
        if (rotateMode) {
            updateRotateLines();
        } else {
            updateFixedLines();
        }
    }

    private LeaderboardCategory getNextFixedCategory() {
        for (LeaderboardCategory category : LeaderboardCategory.values()) {
            if (!fixedLocations.containsKey(category)) {
                return category;
            }
        }
        LeaderboardCategory[] values = LeaderboardCategory.values();
        return values[fixedSetIndex % values.length];
    }

    private void spawn() {
        if (rotateMode) {
            spawnRotate();
        } else {
            spawnFixed();
        }
    }

    private void spawnRotate() {
        if (baseLocation == null || baseLocation.getWorld() == null) {
            return;
        }
        int lineCount = 1 + DISPLAY_COUNT;
        for (int i = 0; i < lineCount; i++) {
            Location loc = baseLocation.clone().subtract(0, LINE_GAP * i, 0);
            ArmorStand stand = (ArmorStand) baseLocation.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setInvulnerable(true);
            stand.setCustomNameVisible(true);
            stands.add(stand);
        }
        updateRotateLines();
        startRotation();
    }

    private void spawnFixed() {
        for (Map.Entry<LeaderboardCategory, Location> entry : fixedLocations.entrySet()) {
            Location base = entry.getValue();
            if (base == null || base.getWorld() == null) {
                continue;
            }
            int lineCount = 1 + DISPLAY_COUNT;
            List<ArmorStand> list = new ArrayList<>();
            for (int i = 0; i < lineCount; i++) {
                Location loc = base.clone().subtract(0, LINE_GAP * i, 0);
                ArmorStand stand = (ArmorStand) base.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
                stand.setVisible(false);
                stand.setGravity(false);
                stand.setMarker(true);
                stand.setInvulnerable(true);
                stand.setCustomNameVisible(true);
                list.add(stand);
            }
            fixedStands.put(entry.getKey(), list);
        }
        updateFixedLines();
    }

    private void startRotation() {
        stopRotation();
        rotateTask = new BukkitRunnable() {
            @Override
            public void run() {
                nextCategory();
            }
        }.runTaskTimer(plugin, ROTATE_TICKS, ROTATE_TICKS);
    }

    private void stopRotation() {
        if (rotateTask != null) {
            rotateTask.cancel();
            rotateTask = null;
        }
    }

    private void updateRotateLines() {
        if (stands.isEmpty()) {
            return;
        }
        LeaderboardCategory category = LeaderboardCategory.values()[categoryIndex];
        List<String> lines = buildLines(category);
        for (int i = 0; i < stands.size(); i++) {
            ArmorStand stand = stands.get(i);
            String text = i < lines.size() ? lines.get(i) : "";
            stand.setCustomName(text);
        }
    }

    private void updateFixedLines() {
        for (Map.Entry<LeaderboardCategory, List<ArmorStand>> entry : fixedStands.entrySet()) {
            List<String> lines = buildLines(entry.getKey());
            List<ArmorStand> list = entry.getValue();
            for (int i = 0; i < list.size(); i++) {
                ArmorStand stand = list.get(i);
                String text = i < lines.size() ? lines.get(i) : "";
                stand.setCustomName(text);
            }
        }
    }

    private String getCategoryDisplayName(LeaderboardCategory category) {
        var messages = com.example.bedwars.BedwarsPlugin.instance.getMessageManager();
        return messages.getMessage("leaderboard.category." + category.name().toLowerCase());
    }

    private List<String> buildLines(LeaderboardCategory category) {
        List<String> lines = new ArrayList<>();
        var messages = com.example.bedwars.BedwarsPlugin.instance.getMessageManager();
        lines.add(ChatColor.GOLD + "" + ChatColor.BOLD + messages.getMessage("leaderboard.title"));
        lines.add(ChatColor.AQUA + getCategoryDisplayName(category));

        List<PlayerStats> top = switch (category) {
            case KILLS -> statsManager.getTopKills(DISPLAY_COUNT);
            case BEDS -> statsManager.getTopBedsDestroyed(DISPLAY_COUNT);
            case WINS -> statsManager.getTopWins(DISPLAY_COUNT);
            case STREAK -> statsManager.getTopWinStreaks(DISPLAY_COUNT);
        };

        int rank = 1;
        for (PlayerStats stats : top) {
            String value = switch (category) {
                case KILLS -> String.valueOf(stats.getKills());
                case BEDS -> String.valueOf(stats.getBedsDestroyed());
                case WINS -> String.valueOf(stats.getWins());
                case STREAK -> String.valueOf(stats.getWinStreak());
            };
            lines.add(ChatColor.YELLOW + "#" + rank + " " + ChatColor.WHITE + stats.getPlayerName()
                    + ChatColor.GRAY + " - " + ChatColor.GREEN + value);
            rank++;
        }

        while (rank <= DISPLAY_COUNT) {
            lines.add(ChatColor.GRAY + "#" + rank + " " + messages.getMessage("leaderboard.empty-slot"));
            rank++;
        }

        return lines;
    }

    public enum LeaderboardCategory {
        KILLS("擊殺榜"),
        BEDS("破壞床榜"),
        WINS("勝局榜"),
        STREAK("連勝榜");

        private final String displayName;

        LeaderboardCategory(String displayName) {
            this.displayName = displayName;
        }
    }
}
