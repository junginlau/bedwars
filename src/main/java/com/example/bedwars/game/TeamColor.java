package com.example.bedwars.game;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public enum TeamColor {
    RED(ChatColor.RED, Color.RED),
    BLUE(ChatColor.BLUE, Color.BLUE),
    GREEN(ChatColor.GREEN, Color.GREEN),
    YELLOW(ChatColor.YELLOW, Color.YELLOW),
    AQUA(ChatColor.AQUA, Color.AQUA),
    WHITE(ChatColor.WHITE, Color.WHITE),
    PINK(ChatColor.LIGHT_PURPLE, Color.FUCHSIA),
    GRAY(ChatColor.GRAY, Color.GRAY);

    private final ChatColor chatColor;
    private final Color armorColor;

    TeamColor(ChatColor chatColor, Color armorColor) {
        this.chatColor = chatColor;
        this.armorColor = armorColor;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public Color getArmorColor() {
        return armorColor;
    }

    public static TeamColor fromName(String name) {
        for (TeamColor color : values()) {
            if (color.name().equalsIgnoreCase(name)) {
                return color;
            }
        }
        return null;
    }
}
