package com.lorfororl.data;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    
    private final LorForOrlPlugin plugin;
    private final File dataFolder;
    private final Map<UUID, PlayerData> playerDataCache;
    private BukkitRunnable autoSaveTask;
    
    public DataManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        this.playerDataCache = new HashMap<>();
        
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        startAutoSave();
    }
    
    private void startAutoSave() {
        int interval = 6000; // 5 минут в тиках
        
        autoSaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                saveAllPlayerData();
            }
        };
        autoSaveTask.runTaskTimerAsynchronously(plugin, interval, interval);
    }
    
    public void shutdown() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
        saveAllPlayerData();
    }
    
    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }
    
    public PlayerData getPlayerData(UUID playerId) {
        return playerDataCache.computeIfAbsent(playerId, this::loadPlayerData);
    }
    
    private PlayerData loadPlayerData(UUID playerId) {
        File playerFile = new File(dataFolder, playerId.toString() + ".yml");
        
        if (!playerFile.exists()) {
            return new PlayerData(playerId);
        }
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            return PlayerData.fromConfig(playerId, config);
        } catch (Exception e) {
            plugin.getLogger().warning("Не удалось загрузить данные игрока " + playerId + ": " + e.getMessage());
            return new PlayerData(playerId);
        }
    }
    
    public void savePlayerData(UUID playerId) {
        PlayerData data = playerDataCache.get(playerId);
        if (data == null) return;
        
        File playerFile = new File(dataFolder, playerId.toString() + ".yml");
        
        try {
            FileConfiguration config = new YamlConfiguration();
            data.saveToConfig(config);
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить данные игрока " + playerId + ": " + e.getMessage());
        }
    }
    
    public void saveAllPlayerData() {
        for (UUID playerId : playerDataCache.keySet()) {
            savePlayerData(playerId);
        }
    }
    
    public void saveData() {
        saveAllPlayerData();
    }
    
    public void unloadPlayerData(UUID playerId) {
        savePlayerData(playerId);
        playerDataCache.remove(playerId);
    }
}
