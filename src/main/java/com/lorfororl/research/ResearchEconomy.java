package com.lorfororl.research;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ResearchEconomy {
    
    private final LorForOrlPlugin plugin;
    private final Map<Material, Double> resourceValues;
    private final Map<String, Double> researchCosts;
    
    public ResearchEconomy(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.resourceValues = new HashMap<>();
        this.researchCosts = new HashMap<>();
        
        initializeResourceValues();
        calculateResearchCosts();
    }
    
    private void initializeResourceValues() {
        // Базовые ресурсы
        resourceValues.put(Material.IRON_INGOT, 1.0);
        resourceValues.put(Material.IRON_BLOCK, 9.0);
        resourceValues.put(Material.COPPER_INGOT, 1.5);
        resourceValues.put(Material.COPPER_BLOCK, 13.5);
        resourceValues.put(Material.REDSTONE, 2.0);
        resourceValues.put(Material.REDSTONE_BLOCK, 18.0);
        
        // Драгоценные ресурсы
        resourceValues.put(Material.GOLD_INGOT, 5.0);
        resourceValues.put(Material.GOLD_BLOCK, 45.0);
        resourceValues.put(Material.DIAMOND, 20.0);
        resourceValues.put(Material.DIAMOND_BLOCK, 180.0);
        resourceValues.put(Material.EMERALD, 25.0);
        resourceValues.put(Material.EMERALD_BLOCK, 225.0);
        
        // Редкие ресурсы
        resourceValues.put(Material.NETHERITE_INGOT, 100.0);
        resourceValues.put(Material.NETHERITE_BLOCK, 900.0);
        resourceValues.put(Material.NETHER_STAR, 500.0);
        resourceValues.put(Material.BEACON, 1500.0);
        resourceValues.put(Material.END_CRYSTAL, 200.0);
        resourceValues.put(Material.DRAGON_EGG, 10000.0);
        
        // Специальные блоки
        resourceValues.put(Material.OBSIDIAN, 3.0);
        resourceValues.put(Material.GLASS, 0.5);
        resourceValues.put(Material.QUARTZ_BLOCK, 8.0);
        resourceValues.put(Material.BOOKSHELF, 15.0);
        resourceValues.put(Material.LECTERN, 20.0);
        resourceValues.put(Material.OBSERVER, 25.0);
        resourceValues.put(Material.LIGHTNING_ROD, 10.0);
        
        // Инструменты
        resourceValues.put(Material.DIAMOND_PICKAXE, 60.0);
        
        // Органические ресурсы
        resourceValues.put(Material.LEATHER, 2.0);
        resourceValues.put(Material.LAPIS_LAZULI, 3.0);
    }
    
    private void calculateResearchCosts() {
        for (Research research : plugin.getResearchManager().getAvailableResearch().values()) {
            double totalCost = 0.0;
            
            for (Map.Entry<Material, Integer> entry : research.getRequiredResources().entrySet()) {
                Material material = entry.getKey();
                int amount = entry.getValue();
                double value = resourceValues.getOrDefault(material, 1.0);
                totalCost += value * amount;
            }
            
            researchCosts.put(research.getId(), totalCost);
        }
    }
    
    public double getResearchCost(String researchId) {
        return researchCosts.getOrDefault(researchId, 0.0);
    }
    
    public double getPlayerResourceValue(Player player) {
        double totalValue = 0.0;
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            
            double value = resourceValues.getOrDefault(item.getType(), 0.0);
            totalValue += value * item.getAmount();
        }
        
        return totalValue;
    }
    
    public String formatCost(double cost) {
        if (cost >= 10000) {
            return String.format("%.1fK", cost / 1000);
        } else if (cost >= 1000) {
            return String.format("%.2fK", cost / 1000);
        } else {
            return String.format("%.0f", cost);
        }
    }
    
    public Map<String, ResearchCostInfo> getResearchCostsByTier() {
        Map<String, ResearchCostInfo> costInfo = new HashMap<>();
        
        for (Research research : plugin.getResearchManager().getAvailableResearch().values()) {
            double cost = getResearchCost(research.getId());
            int tier = research.getTier(); // Используем getTier() вместо getProgression().getResearchTier()
            
            costInfo.put(research.getId(), new ResearchCostInfo(
                research.getName(),
                cost,
                tier,
                research.getResearchTimeMinutes()
            ));
        }
        
        return costInfo;
    }
    
    public static class ResearchCostInfo {
        public final String name;
        public final double cost;
        public final int tier;
        public final long timeMinutes;
        
        public ResearchCostInfo(String name, double cost, int tier, long timeMinutes) {
            this.name = name;
            this.cost = cost;
            this.tier = tier;
            this.timeMinutes = timeMinutes;
        }
    }
}
