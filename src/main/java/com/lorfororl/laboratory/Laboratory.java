package com.lorfororl.laboratory;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Laboratory {
    
    private final Location center;
    private final UUID owner;
    private final Map<String, Location> structure;
    private boolean active;
    private String currentResearch;
    
    public Laboratory(Location center, UUID owner) {
        this.center = center;
        this.owner = owner;
        this.structure = new HashMap<>();
        this.active = false;
    }
    
    public boolean validateStructure() {
        // Проверяем структуру лаборатории 3x3x3
        Location base = center.clone().subtract(1, 1, 1);
        
        // Основание из железных блоков
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                Location loc = base.clone().add(x, 0, z);
                Block block = loc.getBlock();
                if (block.getType() != Material.IRON_BLOCK) {
                    return false;
                }
            }
        }
        
        // Центральный блок - алмазный блок
        Location centerBlock = base.clone().add(1, 1, 1);
        if (centerBlock.getBlock().getType() != Material.DIAMOND_BLOCK) {
            return false;
        }
        
        // Углы второго уровня - редстоун блоки
        Location[] corners = {
            base.clone().add(0, 1, 0),
            base.clone().add(2, 1, 0),
            base.clone().add(0, 1, 2),
            base.clone().add(2, 1, 2)
        };
        
        for (Location corner : corners) {
            if (corner.getBlock().getType() != Material.REDSTONE_BLOCK) {
                return false;
            }
        }
        
        // Верхний уровень - стеклянные блоки
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                Location loc = base.clone().add(x, 2, z);
                Block block = loc.getBlock();
                if (block.getType() != Material.GLASS) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public void activate() {
        if (validateStructure()) {
            this.active = true;
        }
    }
    
    public void deactivate() {
        this.active = false;
    }
    
    public boolean isActive() { return active; }
    public Location getCenter() { return center; }
    public UUID getOwner() { return owner; }
    public String getCurrentResearch() { return currentResearch; }
    public void setCurrentResearch(String research) { this.currentResearch = research; }
}
