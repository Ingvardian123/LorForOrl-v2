package com.lorfororl.data;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerData {
    
    private final UUID playerId;
    private double radiation;
    private int energy;
    private Set<String> completedResearch;
    private Set<String> achievements;
    private Map<String, Integer> achievementProgress;
    private Map<String, Object> customData;
    
    public PlayerData(UUID playerId) {
        this.playerId = playerId;
        this.radiation = 0.0;
        this.energy = 0;
        this.completedResearch = new HashSet<>();
        this.achievements = new HashSet<>();
        this.achievementProgress = new HashMap<>();
        this.customData = new HashMap<>();
    }
    
    public static PlayerData fromConfig(UUID playerId, FileConfiguration config) {
        PlayerData data = new PlayerData(playerId);
        
        data.radiation = config.getDouble("radiation", 0.0);
        data.energy = config.getInt("energy", 0);
        data.completedResearch = new HashSet<>(config.getStringList("completed-research"));
        data.achievements = new HashSet<>(config.getStringList("achievements"));
        
        // Загружаем прогресс достижений
        if (config.contains("achievement-progress")) {
            for (String key : config.getConfigurationSection("achievement-progress").getKeys(false)) {
                data.achievementProgress.put(key, config.getInt("achievement-progress." + key));
            }
        }
        
        // Загружаем пользовательские данные
        if (config.contains("custom-data")) {
            for (String key : config.getConfigurationSection("custom-data").getKeys(false)) {
                data.customData.put(key, config.get("custom-data." + key));
            }
        }
        
        return data;
    }
    
    public void saveToConfig(FileConfiguration config) {
        config.set("radiation", radiation);
        config.set("energy", energy);
        config.set("completed-research", completedResearch.stream().toList());
        config.set("achievements", achievements.stream().toList());
        
        // Сохраняем прогресс достижений
        for (Map.Entry<String, Integer> entry : achievementProgress.entrySet()) {
            config.set("achievement-progress." + entry.getKey(), entry.getValue());
        }
        
        // Сохраняем пользовательские данные
        for (Map.Entry<String, Object> entry : customData.entrySet()) {
            config.set("custom-data." + entry.getKey(), entry.getValue());
        }
    }
    
    // Геттеры и сеттеры
    public UUID getPlayerId() { return playerId; }
    
    public double getRadiation() { return radiation; }
    public void setRadiation(double radiation) { this.radiation = Math.max(0, radiation); }
    
    public int getEnergy() { return energy; }
    public void setEnergy(int energy) { this.energy = Math.max(0, energy); }
    
    public Set<String> getCompletedResearch() { return completedResearch; }
    public void addCompletedResearch(String research) { completedResearch.add(research); }
    
    public Set<String> getAchievements() { return achievements; }
    public void addAchievement(String achievement) { achievements.add(achievement); }
    
    public Map<String, Integer> getAchievementProgress() { return achievementProgress; }
    public void setAchievementProgress(String achievement, int progress) { 
        achievementProgress.put(achievement, progress); 
    }
    
    public Object getCustomData(String key) { return customData.get(key); }
    public void setCustomData(String key, Object value) { customData.put(key, value); }
}
