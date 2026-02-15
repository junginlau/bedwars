package com.example.bedwars.map;

import com.example.bedwars.config.TeamConfig;
import com.example.bedwars.game.GameMode;
import com.example.bedwars.game.TeamColor;
import com.example.bedwars.language.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class MapConfig {
    private String name;
    private String worldName;
    private Location lobbySpawn;
    private int minPlayers;
    private int countdownSeconds;
    private int gameDurationSeconds;
    private final Map<TeamColor, TeamConfig> teamConfigs = new EnumMap<>(TeamColor.class);
    private final List<Location> diamondGenerators = new ArrayList<>();
    private final List<Location> emeraldGenerators = new ArrayList<>();
    private GameMode mode = GameMode.SQUAD;
    private Location arenaMin;
    private Location arenaMax;
    private boolean arenaBoundsSet = false;

    public MapConfig(String name, String worldName, Location lobbySpawn) {
        this.name = name;
        this.worldName = worldName;
        this.lobbySpawn = lobbySpawn;
        this.minPlayers = 4;
        this.countdownSeconds = 10;
        this.gameDurationSeconds = 1800;
        this.mode = GameMode.SQUAD;
        initDefaults();
    }

    public static MapConfig load(File file, String mapName, World fallbackWorld) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String worldName = config.getString("world", fallbackWorld.getName());
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            world = fallbackWorld;
        }
        Location lobby = readLocation(config.getConfigurationSection("lobby-spawn"), world);
        MapConfig map = new MapConfig(mapName, worldName, lobby);
        map.minPlayers = config.getInt("min-players", 4);
        map.countdownSeconds = config.getInt("countdown-seconds", 10);
        map.gameDurationSeconds = config.getInt("game-duration-seconds", 1800);

        // 加载地图边界
        ConfigurationSection boundsMinSection = config.getConfigurationSection("arena-bounds.min");
        ConfigurationSection boundsMaxSection = config.getConfigurationSection("arena-bounds.max");
        if (boundsMinSection != null && boundsMaxSection != null) {
            map.arenaMin = readBlockLocation(boundsMinSection, world);
            map.arenaMax = readBlockLocation(boundsMaxSection, world);
            map.arenaBoundsSet = true;
        }

        map.teamConfigs.clear();
        ConfigurationSection teamsSection = config.getConfigurationSection("teams");
        if (teamsSection != null) {
            for (String key : teamsSection.getKeys(false)) {
                TeamColor color = TeamColor.fromName(key);
                if (color == null) {
                    continue;
                }
                ConfigurationSection teamSection = teamsSection.getConfigurationSection(key);
                if (teamSection == null) {
                    continue;
                }
                ConfigurationSection spawnSection = teamSection.getConfigurationSection("spawn");
                ConfigurationSection bedSection = teamSection.getConfigurationSection("bed");
                ConfigurationSection baseGenSection = teamSection.getConfigurationSection("base-generator");
                ConfigurationSection shopNpcSection = teamSection.getConfigurationSection("shop-npc");

                Location spawn = readLocation(spawnSection, world);
                Location bed = readBlockLocation(bedSection, world);
                Location baseGen = readLocation(baseGenSection, world);
                Location shopNpc = readLocation(shopNpcSection, world);

                boolean spawnSet = teamSection.getBoolean("spawn-set", true);
                boolean bedSet = teamSection.getBoolean("bed-set", true);
                boolean baseGenSet = teamSection.getBoolean("base-generator-set", true);
                boolean shopNpcSet = teamSection.getBoolean("shop-npc-set", false);

                map.teamConfigs.put(color, new TeamConfig(color, spawn, bed, baseGen, shopNpc,
                        spawnSet, bedSet, baseGenSet, shopNpcSet));
            }
        }
        map.ensureTeams();

        map.diamondGenerators.clear();
        map.emeraldGenerators.clear();
        for (Map<?, ?> entry : config.getMapList("middle-generators.diamonds")) {
            map.diamondGenerators.add(readMapLocation(entry, world));
        }
        for (Map<?, ?> entry : config.getMapList("middle-generators.emeralds")) {
            map.emeraldGenerators.add(readMapLocation(entry, world));
        }
        
        // Load single mode instead of list
        String modeStr = config.getString("mode", "SQUAD");
        GameMode loadedMode = GameMode.fromName(modeStr);
        map.mode = (loadedMode != null) ? loadedMode : GameMode.SQUAD;
        
        return map;
    }

    public void save(File file) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("world", worldName);
        writeLocation(config.createSection("lobby-spawn"), lobbySpawn);
        config.set("min-players", minPlayers);
        config.set("countdown-seconds", countdownSeconds);
        config.set("game-duration-seconds", gameDurationSeconds);

        // 保存地图边界
        if (arenaBoundsSet && arenaMin != null && arenaMax != null) {
            writeBlockLocation(config.createSection("arena-bounds.min"), arenaMin);
            writeBlockLocation(config.createSection("arena-bounds.max"), arenaMax);
        }

        ConfigurationSection teamsSection = config.createSection("teams");
        for (Map.Entry<TeamColor, TeamConfig> entry : teamConfigs.entrySet()) {
            ConfigurationSection teamSection = teamsSection.createSection(entry.getKey().name());
            writeLocation(teamSection.createSection("spawn"), entry.getValue().getSpawn());
            writeBlockLocation(teamSection.createSection("bed"), entry.getValue().getBedLocation());
            writeLocation(teamSection.createSection("base-generator"), entry.getValue().getBaseGenerator());
            writeLocation(teamSection.createSection("shop-npc"), entry.getValue().getShopNpcLocation());
            teamSection.set("spawn-set", entry.getValue().isSpawnSet());
            teamSection.set("bed-set", entry.getValue().isBedSet());
            teamSection.set("base-generator-set", entry.getValue().isBaseGeneratorSet());
            teamSection.set("shop-npc-set", entry.getValue().isShopNpcSet());
        }

        List<Map<String, Object>> diamonds = new ArrayList<>();
        for (Location location : diamondGenerators) {
            diamonds.add(toMap(location));
        }
        List<Map<String, Object>> emeralds = new ArrayList<>();
        for (Location location : emeraldGenerators) {
            emeralds.add(toMap(location));
        }
        config.set("middle-generators.diamonds", diamonds);
        config.set("middle-generators.emeralds", emeralds);

        // Save single mode
        config.set("mode", mode.name());

        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save map " + name + ": " + e.getMessage());
        }
    }

    private void initDefaults() {
        ensureTeams();
    }

    private void ensureTeams() {
        World world = getWorld();
        Location base = lobbySpawn != null ? lobbySpawn : world.getSpawnLocation();
        for (TeamColor color : TeamColor.values()) {
            if (!teamConfigs.containsKey(color)) {
                teamConfigs.put(color, new TeamConfig(color, base, base, base));
            }
        }
    }

    private static Location readLocation(ConfigurationSection section, World world) {
        if (section == null) {
            return world.getSpawnLocation();
        }
        return new Location(
                world,
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z"),
                (float) section.getDouble("yaw", 0),
                (float) section.getDouble("pitch", 0)
        );
    }

    private static Location readBlockLocation(ConfigurationSection section, World world) {
        if (section == null) {
            return world.getSpawnLocation().getBlock().getLocation();
        }
        return new Location(
                world,
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z")
        );
    }

    private static Location readMapLocation(Map<?, ?> entry, World world) {
        double x = asDouble(entry.get("x"));
        double y = asDouble(entry.get("y"));
        double z = asDouble(entry.get("z"));
        return new Location(world, x, y, z);
    }

    private static double asDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private static void writeLocation(ConfigurationSection section, Location location) {
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
        section.set("yaw", location.getYaw());
        section.set("pitch", location.getPitch());
    }

    private static void writeBlockLocation(ConfigurationSection section, Location location) {
        section.set("x", location.getBlockX());
        section.set("y", location.getBlockY());
        section.set("z", location.getBlockZ());
    }

    private static Map<String, Object> toMap(Location location) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("x", location.getBlockX());
        map.put("y", location.getBlockY());
        map.put("z", location.getBlockZ());
        return map;
    }

    private World getWorld() {
        World world = Bukkit.getWorld(worldName);
        return world != null ? world : Bukkit.getWorlds().get(0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public void setLobbySpawn(Location lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getCountdownSeconds() {
        return countdownSeconds;
    }

    public void setCountdownSeconds(int countdownSeconds) {
        this.countdownSeconds = countdownSeconds;
    }

    public int getGameDurationSeconds() {
        return gameDurationSeconds;
    }

    public void setGameDurationSeconds(int gameDurationSeconds) {
        this.gameDurationSeconds = gameDurationSeconds;
    }

    public List<String> validateSetup(MessageManager messages) {
        List<String> errors = new ArrayList<>();
        int maxTeams = mode != null ? mode.getMaxTeams() : TeamColor.values().length;
        TeamColor[] colors = TeamColor.values();
        int limit = Math.min(maxTeams, colors.length);
        for (int i = 0; i < limit; i++) {
            TeamColor color = colors[i];
            TeamConfig team = teamConfigs.get(color);
            if (team == null) {
                errors.add(getValidationMessage(messages, "validation.missing-team", "team", color.name()));
                continue;
            }
            if (!team.isSpawnSet()) {
                errors.add(getValidationMessage(messages, "validation.spawn-missing", "team", color.name()));
            }
            if (!team.isBedSet()) {
                errors.add(getValidationMessage(messages, "validation.bed-missing", "team", color.name()));
            }
            if (!team.isBaseGeneratorSet()) {
                errors.add(getValidationMessage(messages, "validation.generator-missing", "team", color.name()));
            }
            if (!team.isShopNpcSet()) {
                errors.add(getValidationMessage(messages, "validation.shop-missing", "team", color.name()));
            }
        }

        if (diamondGenerators.size() < 2) {
            errors.add(getValidationMessage(messages, "validation.diamond-min", "count", "2"));
        }
        if (emeraldGenerators.size() < 1) {
            errors.add(getValidationMessage(messages, "validation.emerald-min", "count", "1"));
        }

        return errors;
    }

    private String getValidationMessage(MessageManager messages, String key, Object... replacements) {
        if (messages == null) {
            return key;
        }
        return messages.getMessage(key, replacements);
    }

    public Map<TeamColor, TeamConfig> getTeamConfigs() {
        return teamConfigs;
    }

    public List<Location> getDiamondGenerators() {
        return diamondGenerators;
    }

    public List<Location> getEmeraldGenerators() {
        return emeraldGenerators;
    }

    public boolean removeNearestDiamond(Location location, double radius) {
        return removeNearest(location, radius, diamondGenerators);
    }

    public boolean removeNearestEmerald(Location location, double radius) {
        return removeNearest(location, radius, emeraldGenerators);
    }

    private boolean removeNearest(Location location, double radius, List<Location> list) {
        Location nearest = null;
        double closest = Double.MAX_VALUE;
        for (Location loc : list) {
            if (!loc.getWorld().equals(location.getWorld())) {
                continue;
            }
            double dist = loc.distance(location);
            if (dist <= radius && dist < closest) {
                closest = dist;
                nearest = loc;
            }
        }
        if (nearest != null) {
            list.remove(nearest);
            return true;
        }
        return false;
    }

    public void addDiamondGenerator(Location location) {
        diamondGenerators.add(location.getBlock().getLocation());
    }

    public void addEmeraldGenerator(Location location) {
        emeraldGenerators.add(location.getBlock().getLocation());
    }

    public void clearDiamonds() {
        diamondGenerators.clear();
    }

    public void clearEmeralds() {
        emeraldGenerators.clear();
    }

    public GameMode getMode() {
        return mode;
    }
    
    public void setMode(GameMode mode) {
        this.mode = mode;
    }

    public boolean isModeEnabled(GameMode mode) {
        return this.mode == mode;
    }

    public Location getArenaMin() {
        return arenaMin;
    }

    public void setArenaMin(Location arenaMin) {
        this.arenaMin = arenaMin;
        checkBoundsSet();
    }

    public Location getArenaMax() {
        return arenaMax;
    }

    public void setArenaMax(Location arenaMax) {
        this.arenaMax = arenaMax;
        checkBoundsSet();
    }

    public boolean isArenaBoundsSet() {
        return arenaBoundsSet;
    }

    private void checkBoundsSet() {
        arenaBoundsSet = arenaMin != null && arenaMax != null;
    }
}
