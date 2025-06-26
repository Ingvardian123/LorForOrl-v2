package com.lorfororl.balance;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BalanceManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, PlayerBalance> playerBalances;
    
    public BalanceManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.playerBalances = new HashMap<>();
    }
    
    public PlayerBalance getPlayerBalance(Player player) {
        return playerBalances.computeIfAbsent(player.getUniqueId(), k -> new PlayerBalance());
    }
    
    public double calculateCentrifugeEfficiency(Player player) {
        PlayerBalance balance = getPlayerBalance(player);
        
        // Базовая эффективность
        double efficiency = 1.0;
        
        // Бонус от опыта
        double experienceBonus = Math.min(0.5, balance.centrifugeExperience * 0.001); // Максимум +50%
        efficiency += experienceBonus;
        
        // Штраф от количества центрифуг (убывающая отдача)
        int centrifugeCount = balance.activeCentrifuges;
        double diminishingReturns = 1.0 / (1.0 + centrifugeCount * 0.1);
        efficiency *= diminishingReturns;
        
        // Бонус от исследований
        if (hasCompletedResearch(player, "centrifuge_optimization")) {
            efficiency *= 1.25; // +25% от исследования
        }
        
        return Math.max(0.1, efficiency); // Минимум 10% эффективности
    }
    
    private boolean hasCompletedResearch(Player player, String researchId) {
        // Заглушка для проверки исследований
        return plugin.getResearchManager().isResearchCompleted(player, researchId);
    }
    
    public double calculateRadiationResistance(Player player) {
        PlayerBalance balance = getPlayerBalance(player);
        
        // Базовая сопротивляемость
        double resistance = 0.0;
        
        // Сопротивляемость от опыта работы с радиацией
        double experienceResistance = Math.min(0.3, balance.radiationExposure * 0.0001); // Максимум +30%
        resistance += experienceResistance;
        
        return resistance;
    }
    
    public double calculateEnergyEfficiency(Player player) {
        PlayerBalance balance = getPlayerBalance(player);
        
        double efficiency = 1.0;
        
        // Бонус от опыта работы с энергией
        double experienceBonus = Math.min(0.4, balance.energyExperience * 0.0005); // Максимум +40%
        efficiency += experienceBonus;
        
        return efficiency;
    }
    
    public void addCentrifugeExperience(Player player, int amount) {
        PlayerBalance balance = getPlayerBalance(player);
        balance.centrifugeExperience += amount;
        
        // Проверяем достижения
        plugin.getAchievementManager().checkAchievement(player, "uranium_collector", balance.centrifugeExperience);
    }
    
    public void addRadiationExposure(Player player, double amount) {
        PlayerBalance balance = getPlayerBalance(player);
        balance.radiationExposure += amount;
        
        // Проверяем достижения
        plugin.getAchievementManager().onRadiationGained(player, amount);
    }
    
    public void addEnergyExperience(Player player, int amount) {
        PlayerBalance balance = getPlayerBalance(player);
        balance.energyExperience += amount;
    }
    
    public void setCentrifugeCount(Player player, int count) {
        PlayerBalance balance = getPlayerBalance(player);
        balance.activeCentrifuges = count;
    }
    
    public static class PlayerBalance {
        public int centrifugeExperience = 0;
        public double radiationExposure = 0.0;
        public int energyExperience = 0;
        public int activeCentrifuges = 0;
        public long totalPlayTime = 0;
    }
}
