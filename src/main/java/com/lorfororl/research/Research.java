package com.lorfororl.research;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Research {
    
    private final String id;
    private final String name;
    private final String description;
    private final Map<Material, Integer> requiredResources;
    private final long researchTimeMinutes;
    private final ResearchCategory category;
    private final int tier;
    
    public Research(String id, String name, String description, ResearchCategory category, int tier, long researchTimeMinutes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.tier = tier;
        this.researchTimeMinutes = researchTimeMinutes;
        this.requiredResources = new HashMap<>();
    }
    
    public Research addRequiredResource(Material material, int amount) {
        requiredResources.put(material, amount);
        return this;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Map<Material, Integer> getRequiredResources() { return requiredResources; }
    public long getResearchTimeMinutes() { return researchTimeMinutes; }
    public ResearchCategory getCategory() { return category; }
    public int getTier() { return tier; }
    
    public enum ResearchCategory {
        ENERGY("§eЭнергетика"),
        PROTECTION("§9Защита"),
        TRANSPORT("§aТранспорт"),
        WEAPONS("§cОружие"),
        AUTOMATION("§6Автоматизация"),
        ADVANCED("§dПродвинутые");
        
        private final String displayName;
        
        ResearchCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
}
