package com.lorfororl.structures;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BuildingStructure {
    
    private final Location location;
    private final StructureBuilder.StructureType type;
    private final UUID owner;
    private final Map<Material, Integer> requiredResources;
    private final Map<Material, Integer> addedResources;
    private final long startTime;
    
    public BuildingStructure(Location location, StructureBuilder.StructureType type, UUID owner) {
        this.location = location;
        this.type = type;
        this.owner = owner;
        this.requiredResources = new HashMap<>();
        this.addedResources = new HashMap<>();
        this.startTime = System.currentTimeMillis();
        
        initializeRequiredResources();
    }
    
    private void initializeRequiredResources() {
        switch (type) {
            case LABORATORY:
                requiredResources.put(Material.IRON_BLOCK, 16);
                requiredResources.put(Material.REDSTONE_BLOCK, 8);
                requiredResources.put(Material.GLASS, 18);
                requiredResources.put(Material.COPPER_BLOCK, 12);
                requiredResources.put(Material.GOLD_INGOT, 16);
                break;
            
            case NUCLEAR_REACTOR:
                requiredResources.put(Material.OBSIDIAN, 80);
                requiredResources.put(Material.IRON_BLOCK, 64);
                requiredResources.put(Material.COPPER_BLOCK, 32); // Заменили свинец на медь
                requiredResources.put(Material.DIAMOND_BLOCK, 8);
                requiredResources.put(Material.REDSTONE_BLOCK, 24);
                requiredResources.put(Material.GOLD_BLOCK, 16);
                break;
            
            case ENERGY_GENERATOR:
                requiredResources.put(Material.COPPER_BLOCK, 16);
                requiredResources.put(Material.LIGHTNING_ROD, 8);
                requiredResources.put(Material.GOLD_BLOCK, 4);
                requiredResources.put(Material.REDSTONE_BLOCK, 8);
                requiredResources.put(Material.IRON_BLOCK, 12);
                break;
            
            case RESEARCH_STATION:
                requiredResources.put(Material.QUARTZ_BLOCK, 16);
                requiredResources.put(Material.BOOKSHELF, 8);
                requiredResources.put(Material.LECTERN, 6);
                requiredResources.put(Material.DIAMOND, 16);
                requiredResources.put(Material.EMERALD, 12);
                requiredResources.put(Material.COPPER_BLOCK, 8);
                break;
        }
    }
    
    public void addResource(Material material, int amount) {
        addedResources.merge(material, amount, Integer::sum);
    }
    
    public int getNeededResource(Material material) {
        int required = requiredResources.getOrDefault(material, 0);
        int added = addedResources.getOrDefault(material, 0);
        return Math.max(0, required - added);
    }
    
    public double getProgress() {
        int totalRequired = requiredResources.values().stream().mapToInt(Integer::intValue).sum();
        int totalAdded = addedResources.values().stream().mapToInt(Integer::intValue).sum();
        
        return totalRequired > 0 ? (double) totalAdded / totalRequired : 0.0;
    }
    
    public boolean isComplete() {
        for (Map.Entry<Material, Integer> entry : requiredResources.entrySet()) {
            Material material = entry.getKey();
            int required = entry.getValue();
            int added = addedResources.getOrDefault(material, 0);
            
            if (added < required) {
                return false;
            }
        }
        return true;
    }
    
    public String getResourceStatus() {
        StringBuilder status = new StringBuilder();
        status.append("§6=== Требуемые ресурсы ===\n");
        
        for (Map.Entry<Material, Integer> entry : requiredResources.entrySet()) {
            Material material = entry.getKey();
            int required = entry.getValue();
            int added = addedResources.getOrDefault(material, 0);
            
            String color = added >= required ? "§a" : "§c";
            status.append(String.format("%s%s: %d/%d\n", color, material.name(), added, required));
        }
        
        status.append(String.format("§eПрогресс: %.1f%%", getProgress() * 100));
        return status.toString();
    }
    
    // Геттеры
    public Location getLocation() { return location; }
    public StructureBuilder.StructureType getType() { return type; }
    public UUID getOwner() { return owner; }
    public Map<Material, Integer> getRequiredResources() { return requiredResources; }
    public Map<Material, Integer> getAddedResources() { return addedResources; }
    public long getStartTime() { return startTime; }
}
