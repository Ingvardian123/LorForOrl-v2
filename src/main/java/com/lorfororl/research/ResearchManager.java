package com.lorfororl.research;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class ResearchManager {

    private Map<String, Research> availableResearch;
    private Map<UUID, Set<String>> playerCompletedResearch;
    private Map<UUID, ActiveResearch> activeResearches;

    public ResearchManager() {
        availableResearch = new HashMap<>();
        playerCompletedResearch = new HashMap<>();
        activeResearches = new HashMap<>();
        initializeResearch();
    }

    private void initializeResearch() {
        Research research;

        // Уровень 1 - Базовые технологии
        research = new Research("basic_energy", "Основы энергетики", "Изучение базовых принципов энергетики", 
                               Research.ResearchCategory.ENERGY, 1, 60);
        research.addRequiredResource(Material.REDSTONE, 32);
        research.addRequiredResource(Material.COPPER_INGOT, 16);
        research.addRequiredResource(Material.IRON_INGOT, 24);
        research.addRequiredResource(Material.GOLD_INGOT, 8);
        availableResearch.put("basic_energy", research);

        research = new Research("basic_automation", "Базовая автоматизация", "Основы автоматизации процессов", 
                               Research.ResearchCategory.AUTOMATION, 1, 75);
        research.addRequiredResource(Material.REDSTONE, 48);
        research.addRequiredResource(Material.IRON_INGOT, 32);
        research.addRequiredResource(Material.PISTON, 8);
        research.addRequiredResource(Material.HOPPER, 4);
        availableResearch.put("basic_automation", research);

        // Уровень 2 - Продвинутые технологии
        research = new Research("advanced_energy", "Продвинутая энергетика", "Сложные энергетические системы", 
                               Research.ResearchCategory.ENERGY, 2, 150);
        research.addRequiredResource(Material.GOLD_BLOCK, 8);
        research.addRequiredResource(Material.REDSTONE_BLOCK, 16);
        research.addRequiredResource(Material.COPPER_BLOCK, 12);
        research.addRequiredResource(Material.DIAMOND, 16);
        availableResearch.put("advanced_energy", research);

        research = new Research("energy_weapons", "Энергетическое оружие", "Разработка энергетического оружия", 
                               Research.ResearchCategory.WEAPONS, 2, 180);
        research.addRequiredResource(Material.DIAMOND, 24);
        research.addRequiredResource(Material.GOLD_BLOCK, 6);
        research.addRequiredResource(Material.REDSTONE_BLOCK, 12);
        research.addRequiredResource(Material.LIGHTNING_ROD, 4);
        availableResearch.put("energy_weapons", research);

        // Уровень 4 - Мега-проекты
        research = new Research("quantum_tech", "Квантовые технологии", "Изучение квантовых эффектов", 
                               Research.ResearchCategory.ADVANCED, 4, 720);
        research.addRequiredResource(Material.NETHERITE_INGOT, 16);
        research.addRequiredResource(Material.DIAMOND_BLOCK, 32);
        research.addRequiredResource(Material.EMERALD_BLOCK, 24);
        research.addRequiredResource(Material.NETHER_STAR, 8);
        research.addRequiredResource(Material.BEACON, 4);
        availableResearch.put("quantum_tech", research);

        // Уровень 5 - Финальные технологии
        research = new Research("fusion_reactor", "Термоядерный реактор", "Создание термоядерного реактора", 
                               Research.ResearchCategory.ENERGY, 5, 1440);
        research.addRequiredResource(Material.NETHERITE_BLOCK, 32);
        research.addRequiredResource(Material.DIAMOND_BLOCK, 64);
        research.addRequiredResource(Material.EMERALD_BLOCK, 48);
        research.addRequiredResource(Material.COPPER_BLOCK, 128);
        research.addRequiredResource(Material.GOLD_BLOCK, 64);
        research.addRequiredResource(Material.REDSTONE_BLOCK, 96);
        research.addRequiredResource(Material.NETHER_STAR, 16);
        research.addRequiredResource(Material.BEACON, 8);
        research.addRequiredResource(Material.END_CRYSTAL, 32);
        research.addRequiredResource(Material.DRAGON_EGG, 1);
        availableResearch.put("fusion_reactor", research);
    }

    public Research getResearch(String researchId) {
        return availableResearch.get(researchId);
    }

    public Map<String, Research> getAvailableResearch() {
        return availableResearch;
    }

    // Player-specific research methods
    public Set<String> getPlayerCompletedResearch(Player player) {
        return playerCompletedResearch.getOrDefault(player.getUniqueId(), new HashSet<>());
    }

    public Set<String> getCompletedResearch(Player player) {
        return getPlayerCompletedResearch(player);
    }

    public boolean hasPlayerCompletedResearch(Player player, String researchId) {
        Set<String> completed = playerCompletedResearch.get(player.getUniqueId());
        return completed != null && completed.contains(researchId);
    }

    public boolean isResearchCompleted(Player player, String researchId) {
        return hasPlayerCompletedResearch(player, researchId);
    }

    public void setPlayerCompletedResearch(Player player, String researchId) {
        playerCompletedResearch.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(researchId);
    }

    public ActiveResearch getActiveResearch(Player player) {
        return activeResearches.get(player.getUniqueId());
    }

    public void startResearch(Player player, String researchId) {
        Research research = availableResearch.get(researchId);
        if (research != null) {
            ActiveResearch activeResearch = new ActiveResearch(researchId, System.currentTimeMillis(), 
                                                              research.getResearchTimeMinutes() * 60 * 1000);
            activeResearches.put(player.getUniqueId(), activeResearch);
        }
    }

    public void completeResearch(Player player) {
        ActiveResearch active = activeResearches.get(player.getUniqueId());
        if (active != null && active.isComplete()) {
            setPlayerCompletedResearch(player, active.getResearchId());
            activeResearches.remove(player.getUniqueId());
        }
    }

    public List<Research> getAvailableResearchForPlayer(Player player) {
        List<Research> available = new ArrayList<>();
        Set<String> completed = getPlayerCompletedResearch(player);
        
        for (Research research : availableResearch.values()) {
            if (!completed.contains(research.getId()) && canPlayerStartResearch(player, research)) {
                available.add(research);
            }
        }
        
        return available;
    }

    private boolean canPlayerStartResearch(Player player, Research research) {
        // Проверка предварительных исследований - заглушка
        return true;
    }

    public static class ActiveResearch {
        private final String researchId;
        private final long startTime;
        private final long duration;

        public ActiveResearch(String researchId, long startTime, long duration) {
            this.researchId = researchId;
            this.startTime = startTime;
            this.duration = duration;
        }

        public String getResearchId() { return researchId; }
        public long getStartTime() { return startTime; }
        public long getDuration() { return duration; }
        
        public double getProgress() {
            long elapsed = System.currentTimeMillis() - startTime;
            return Math.min(1.0, (double) elapsed / duration);
        }
        
        public long getRemainingTime() {
            long elapsed = System.currentTimeMillis() - startTime;
            return Math.max(0, duration - elapsed);
        }
        
        public boolean isComplete() {
            return getProgress() >= 1.0;
        }
    }
}
