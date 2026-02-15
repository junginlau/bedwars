package com.example.bedwars.language;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageManager {
    private static final String DEFAULT_LANGUAGE = "en_US";
    private String currentLanguage;
    private Map<String, YamlConfiguration> languageFiles = new HashMap<>();
    private JavaPlugin plugin;

    public MessageManager(JavaPlugin plugin, String language) {
        this.plugin = plugin;
        this.currentLanguage = language;
        loadLanguageFile(DEFAULT_LANGUAGE);
        if (!language.equals(DEFAULT_LANGUAGE)) {
            loadLanguageFile(language);
        }
    }

    private void loadLanguageFile(String language) {
        try {
            File dataFolder = plugin.getDataFolder();
            File langFolder = new File(dataFolder, "languages");
            
            if (!langFolder.exists()) {
                langFolder.mkdirs();
            }
            
            File langFile = new File(langFolder, "messages_" + language + ".yml");
            
            // 如果文件不存在，从資源中複製
            if (!langFile.exists()) {
                try (InputStreamReader reader = new InputStreamReader(
                        plugin.getResource("messages_" + language + ".yml"))) {
                    if (reader != null) {
                        YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);
                        defaultConfig.save(langFile);
                    }
                } catch (Exception ignored) {
                    // 資源不存在，使用已加載的版本
                }
            }
            
            YamlConfiguration config = YamlConfiguration.loadConfiguration(langFile);
            languageFiles.put(language, config);
            
            plugin.getLogger().info("Loaded language: " + language);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load language " + language + ": " + e.getMessage());
        }
    }

    /**
     * 獲取訊息
     * @param key 訊息鑰匙，例如 "gui.maps"
     * @return 格式化的訊息
     */
    public String getMessage(String key) {
        // 首先嘗試當前語言
        YamlConfiguration config = languageFiles.get(currentLanguage);
        String message = null;
        
        if (config != null) {
            message = config.getString(key);
        }
        
        // 如果訊息不存在，使用默認語言
        if (message == null) {
            config = languageFiles.get(DEFAULT_LANGUAGE);
            if (config != null) {
                message = config.getString(key);
            }
        }
        
        // 如果仍然不存在，返回鑰匙本身
        if (message == null) {
            return "§cMissing key: " + key;
        }
        
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * 獲取訊息並替換佔位符
     * @param key 訊息鑰匙
     * @param replacements 替換對，例如 "name", "value", "count", "5"
     * @return 格式化的訊息
     */
    public String getMessage(String key, Object... replacements) {
        String message = getMessage(key);
        
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                String placeholder = "{" + replacements[i] + "}";
                String value = replacements[i + 1].toString();
                message = message.replace(placeholder, value);
            }
        }
        
        return message;
    }

    /**
     * 獲取訊息並添加顏色代碼
     * @param key 訊息鑰匙
     * @param color 顏色代碼
     * @return 格式化的訊息
     */
    public String getColoredMessage(String key, ChatColor color) {
        return color + getMessage(key);
    }

    /**
     * 改變當前語言
     * @param language 語言代碼，例如 "zh_TW", "en_US"
     */
    public void setLanguage(String language) {
        if (!languageFiles.containsKey(language)) {
            loadLanguageFile(language);
        }
        this.currentLanguage = language;
        plugin.getLogger().info("Language changed to: " + language);
    }

    /**
     * 獲取當前語言
     * @return 語言代碼
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * 重新加載所有語言文件
     */
    public void reload() {
        languageFiles.clear();
        loadLanguageFile(DEFAULT_LANGUAGE);
        if (!currentLanguage.equals(DEFAULT_LANGUAGE)) {
            loadLanguageFile(currentLanguage);
        }
        plugin.getLogger().info("Language files reloaded");
    }

    /**
     * 獲取 GUI 菜單標題
     */
    public String getGuiTitle(String key) {
        return getMessage("gui." + key);
    }

    /**
     * 獲取 GUI 菜單項
     */
    public String getGuiItem(String menu, String item) {
        return getMessage(menu + "." + item);
    }

    /**
     * 獲取命令幫助訊息
     */
    public String getCommandMessage(String key) {
        return getMessage("command." + key);
    }

    /**
     * 獲取遊戲訊息
     */
    public String getGameMessage(String key) {
        return getMessage("game." + key);
    }

    /**
     * 獲取雜項訊息
     */
    public String getMiscMessage(String key) {
        return getMessage("misc." + key);
    }

    /**
     * 獲取可用的語言列表
     */
    public List<String> getAvailableLanguages() {
        List<String> languages = new ArrayList<>();
        try {
            File dataFolder = plugin.getDataFolder();
            File langFolder = new File(dataFolder, "languages");
            
            if (!langFolder.exists()) {
                langFolder.mkdirs();
            }
            
            File[] files = langFolder.listFiles((dir, name) -> name.startsWith("messages_") && name.endsWith(".yml"));
            
            if (files != null) {
                for (File file : files) {
                    String filename = file.getName();
                    // Extract language code from filename (messages_zh_TW.yml -> zh_TW)
                    String langCode = filename.substring(9, filename.length() - 4);
                    languages.add(langCode);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to list available languages: " + e.getMessage());
        }
        
        // Ensure at least default language is available
        if (languages.isEmpty()) {
            languages.add(DEFAULT_LANGUAGE);
        }
        
        return languages;
    }
}
