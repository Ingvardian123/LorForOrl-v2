package com.lorfororl.managers;

import com.lorfororl.research.Research;
import org.bukkit.entity.Player;

import java.util.*;

public class ResearchManager {
    
    private final Map<UUID, Set<String>> completedResearch;
    private final Map<UUID, ActiveResearch> activeResearch;
    private final Map<String, Research> availableResearch;
    
    public ResearchManager() {
        this.completedResearch = new HashMap<>();
        this.activeResearch = new HashMap<>();
        this.availableResearch = new HashMap<>();
        initializeResearch();
    }
    
    private void initializeResearch() {
        // Базовые исследования
        availableResearch.put("hazmat_suit", new Research("hazmat_suit", "Костюм химзащиты", 
            "Защитный костюм от радиации", Research.ResearchCategory.PROTECTION, 1, 30));
        
        availableResearch.put("power_armor", new Research("power_armor", "Силовая броня", 
            "Мощная броня с энергетическими системами", Research.ResearchCategory.PROTECTION, 2, 60));
        
        availableResearch.put("railgun", new Research("railgun", "Рельсотрон", 
            "Электромагнитное оружие", Research.ResearchCategory.WEAPONS, 2, 45));
        
        availableResearch.put("nuclear_reactor", new Research("nuclear_reactor", "Ядерный реактор", 
            "Источник ядерной энергии", Research.ResearchCategory.ENERGY, 3, 120));
    }
    
    public Set<String> getCompletedResearch(Player player) {
        return completedResearch.getOrDefault(player.getUniqueId(), new HashSet<>());
    }
    
    public ActiveResearch getActiveResearch(Player player) {
        return activeResearch.get(player.getUniqueId());
    }
    
    public boolean startResearch(Player player, String researchId) {
        if (activeResearch.containsKey(player.getUniqueId())) {
            return false; // Уже есть активное исследование
        }
        
        Research research = availableResearch.get(researchId);
        if (research == null) {
            return false;
        }
        
        ActiveResearch active = new ActiveResearch(researchId, research.getResearchTimeMinutes() * 60 * 1000);
        activeResearch.put(player.getUniqueId(), active);
        return true;
    }
    
    public boolean completeResearch(Player player) {
        ActiveResearch active = activeResearch.remove(player.getUniqueId());
        if (active == null || !active.isComplete()) {
            return false;
        }
        
        completedResearch.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>())
                         .add(active.getResearchId());
        return true;
    }
    
    public static class ActiveResearch {
        private final String researchId;
        private final long startTime;
        private final long duration;
        
        public ActiveResearch(String researchId, long duration) {
            this.researchId = researchId;
            this.startTime = System.currentTimeMillis();
            this.duration = duration;
        }
        
        public String getResearchId() { return researchId; }
        public double getProgress() { 
            return Math.min(1.0, (System.currentTimeMillis() - startTime) / (double) duration);
        }
        public long getRemainingTime() { 
            return Math.max(0, duration - (System.currentTimeMillis() - startTime));
        }
        public boolean isComplete() { return getProgress() >= 1.0; }
    }
}
