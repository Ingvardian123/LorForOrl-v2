package com.lorfororl.research;

import org.bukkit.entity.Player;

import java.util.*;

public class ResearchProgression {
    
    private final ResearchManager researchManager;
    private final Map<Integer, List<String>> researchByTier;
    private final Map<String, Set<String>> dependencies;
    
    public ResearchProgression(ResearchManager researchManager) {
        this.researchManager = researchManager;
        this.researchByTier = new HashMap<>();
        this.dependencies = new HashMap<>();
        
        initializeProgression();
    }
    
    private void initializeProgression() {
        // Уровень 1 - Базовые технологии
        researchByTier.put(1, Arrays.asList("hazmat_suit", "basic_energy", "basic_automation"));
        
        // Уровень 2 - Продвинутые технологии  
        researchByTier.put(2, Arrays.asList("advanced_energy", "electric_vehicle", "energy_weapons", "auto_miner"));
        
        // Уровень 3 - Сложные технологии
        researchByTier.put(3, Arrays.asList("power_armor", "railgun", "nuclear_reactor"));
        
        // Уровень 4 - Мега-проекты
        researchByTier.put(4, Arrays.asList("quantum_tech"));
        
        // Уровень 5 - Финальные технологии
        researchByTier.put(5, Arrays.asList("fusion_reactor"));
        
        // Настройка зависимостей
        dependencies.put("advanced_energy", Set.of("basic_energy"));
        dependencies.put("electric_vehicle", Set.of("basic_energy"));
        dependencies.put("energy_weapons", Set.of("basic_energy"));
        dependencies.put("auto_miner", Set.of("basic_automation"));
        
        dependencies.put("power_armor", Set.of("hazmat_suit", "advanced_energy"));
        dependencies.put("railgun", Set.of("energy_weapons", "advanced_energy"));
        dependencies.put("nuclear_reactor", Set.of("advanced_energy"));
        
        dependencies.put("quantum_tech", Set.of("nuclear_reactor", "power_armor"));
        
        dependencies.put("fusion_reactor", Set.of("quantum_tech", "nuclear_reactor"));
    }
    
    public boolean canStartResearch(Player player, String researchId) {
        Set<String> completed = researchManager.getPlayerCompletedResearch(player);
        
        // Проверяем зависимости
        Set<String> requiredResearch = dependencies.get(researchId);
        if (requiredResearch != null) {
            for (String required : requiredResearch) {
                if (!completed.contains(required)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public Set<String> getMissingDependencies(Player player, String researchId) {
        Set<String> completed = researchManager.getPlayerCompletedResearch(player);
        Set<String> missing = new HashSet<>();
        
        Set<String> requiredResearch = dependencies.get(researchId);
        if (requiredResearch != null) {
            for (String required : requiredResearch) {
                if (!completed.contains(required)) {
                    missing.add(required);
                }
            }
        }
        
        return missing;
    }
    
    public List<String> getResearchByTier(int tier) {
        return researchByTier.getOrDefault(tier, new ArrayList<>());
    }
    
    public int getResearchTier(String researchId) {
        for (Map.Entry<Integer, List<String>> entry : researchByTier.entrySet()) {
            if (entry.getValue().contains(researchId)) {
                return entry.getKey();
            }
        }
        return 1; // По умолчанию первый уровень
    }
}
