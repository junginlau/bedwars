package com.example.bedwars.game;

import org.bukkit.ChatColor;
import org.bukkit.Sound;

public class GameEvent {
    private final EventType type;
    private final int triggerAtSecond;
    private final String title;
    private final String subtitle;
    private final Sound sound;
    private boolean triggered;

    public GameEvent(EventType type, int triggerAtSecond, String title, String subtitle, Sound sound) {
        this.type = type;
        this.triggerAtSecond = triggerAtSecond;
        this.title = title;
        this.subtitle = subtitle;
        this.sound = sound;
        this.triggered = false;
    }

    public EventType getType() {
        return type;
    }

    public int getTriggerAtSecond() {
        return triggerAtSecond;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public Sound getSound() {
        return sound;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public void setTriggered(boolean triggered) {
        this.triggered = triggered;
    }

    public enum EventType {
        DIAMOND_UPGRADE(ChatColor.AQUA + "鑽石 II"),
        DIAMOND_UPGRADE_MAX(ChatColor.AQUA + "鑽石 III"),
        EMERALD_UPGRADE(ChatColor.GREEN + "綠寶石 II"),
        EMERALD_UPGRADE_MAX(ChatColor.GREEN + "綠寶石 III"),
        BED_DESTROY(ChatColor.RED + "床即將摧毀"),
        SUDDEN_DEATH(ChatColor.DARK_RED + "決勝階段"),
        GAME_END(ChatColor.GOLD + "遊戲即將結束");

        private final String displayName;

        EventType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static GameEvent createDiamondUpgrade(int second) {
        return new GameEvent(
            EventType.DIAMOND_UPGRADE,
            second,
            ChatColor.AQUA + "鑽石生成器升級",
            ChatColor.YELLOW + "等級 II",
            Sound.BLOCK_NOTE_BLOCK_PLING
        );
    }

    public static GameEvent createDiamondUpgradeMax(int second) {
        return new GameEvent(
            EventType.DIAMOND_UPGRADE_MAX,
            second,
            ChatColor.AQUA + "鑽石生成器升級",
            ChatColor.YELLOW + "等級 III (最高)",
            Sound.BLOCK_NOTE_BLOCK_PLING
        );
    }

    public static GameEvent createEmeraldUpgrade(int second) {
        return new GameEvent(
            EventType.EMERALD_UPGRADE,
            second,
            ChatColor.GREEN + "綠寶石生成器升級",
            ChatColor.YELLOW + "等級 II",
            Sound.BLOCK_NOTE_BLOCK_PLING
        );
    }

    public static GameEvent createEmeraldUpgradeMax(int second) {
        return new GameEvent(
            EventType.EMERALD_UPGRADE_MAX,
            second,
            ChatColor.GREEN + "綠寶石生成器升級",
            ChatColor.YELLOW + "等級 III (最高)",
            Sound.BLOCK_NOTE_BLOCK_PLING
        );
    }

    public static GameEvent createBedDestroy(int second) {
        return new GameEvent(
            EventType.BED_DESTROY,
            second,
            ChatColor.RED + "床即將摧毀",
            ChatColor.YELLOW + "所有床將在此時被摧毀",
            Sound.ENTITY_WITHER_SPAWN
        );
    }

    public static GameEvent createSuddenDeath(int second) {
        return new GameEvent(
            EventType.SUDDEN_DEATH,
            second,
            ChatColor.DARK_RED + "決勝階段",
            ChatColor.YELLOW + "無法復活",
            Sound.ENTITY_ENDER_DRAGON_GROWL
        );
    }

    public static GameEvent createGameEnd(int second) {
        return new GameEvent(
            EventType.GAME_END,
            second,
            ChatColor.GOLD + "加時賽",
            ChatColor.YELLOW + "遊戲即將結束",
            Sound.BLOCK_END_PORTAL_SPAWN
        );
    }
}
