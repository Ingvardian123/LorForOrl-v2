package com.lorfororl.listeners;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

public class RadiationListener implements Listener {
    
    private final LorForOrlPlugin plugin;
    
    public RadiationListener(LorForOrlPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        CraftingInventory inventory = event.getInventory();
        ItemStack result = inventory.getResult();
        
        if (result == null) return;
        
        // Проверяем крафт уранового слитка (9 пыли -> 1 слиток)
        if (result.getType() == Material.IRON_INGOT) {
            if (isUraniumIngotCraft(inventory)) {
                event.setResult(plugin.getUraniumItems().createUraniumIngot(1));
            }
        }
        
        // Проверяем крафт уранового блока (9 слитков -> 1 блок)
        if (result.getType() == Material.IRON_BLOCK) {
            if (isUraniumBlockCraft(inventory)) {
                event.setResult(plugin.getUraniumItems().createUraniumBlock(1));
            }
        }

        // Проверяем крафт дозиметра Гейгера
        if (result.getType() == Material.CLOCK) {
            if (isGeigerCounterCraft(inventory)) {
                event.setResult(plugin.getUraniumItems().createGeigerCounter(1));
            }
        }

        // Проверяем крафт урановой капсулы
        if (result.getType() == Material.END_CRYSTAL) {
            if (isUraniumCapsuleCraft(inventory)) {
                event.setResult(plugin.getUraniumItems().createEmptyCapsule());
            }
        }
    }
    
    private boolean isUraniumIngotCraft(CraftingInventory inventory) {
        ItemStack[] matrix = inventory.getMatrix();
        
        // Проверяем, что все 9 слотов заполнены урановой пылью
        for (ItemStack item : matrix) {
            if (item == null || !plugin.getUraniumItems().isUraniumItem(item)) {
                return false;
            }
            if (!"dust".equals(plugin.getUraniumItems().getUraniumType(item))) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isUraniumBlockCraft(CraftingInventory inventory) {
        ItemStack[] matrix = inventory.getMatrix();
        
        // Проверяем, что все 9 слотов заполнены урановыми слитками
        for (ItemStack item : matrix) {
            if (item == null || !plugin.getUraniumItems().isUraniumItem(item)) {
                return false;
            }
            if (!"ingot".equals(plugin.getUraniumItems().getUraniumType(item))) {
                return false;
            }
        }
        
        return true;
    }

    private boolean isGeigerCounterCraft(CraftingInventory inventory) {
        ItemStack[] matrix = inventory.getMatrix();
    
        // Проверяем рецепт дозиметра:
        // [R][I][R]
        // [I][C][I]  где R = редстоун, I = железный слиток, C = часы
        // [R][I][R]
    
        if (matrix.length != 9) return false;
    
        // Проверяем центр - должны быть часы
        if (matrix[4] == null || matrix[4].getType() != Material.CLOCK) {
            return false;
        }
    
        // Проверяем углы - должен быть редстоун
        int[] redstoneSlots = {0, 2, 6, 8};
        for (int slot : redstoneSlots) {
            if (matrix[slot] == null || matrix[slot].getType() != Material.REDSTONE) {
                return false;
            }
        }
    
        // Проверяем стороны - должны быть железные слитки
        int[] ironSlots = {1, 3, 5, 7};
        for (int slot : ironSlots) {
            if (matrix[slot] == null || matrix[slot].getType() != Material.IRON_INGOT) {
                return false;
            }
        }
    
        return true;
    }

    private boolean isUraniumCapsuleCraft(CraftingInventory inventory) {
        ItemStack[] matrix = inventory.getMatrix();

        // Проверяем рецепт капсулы:
        // [G][I][G]
        // [I][D][I]  где G = стекло, I = железный слиток, D = алмаз
        // [G][I][G]

        if (matrix.length != 9) return false;

        // Проверяем центр - должен быть алмаз
        if (matrix[4] == null || matrix[4].getType() != Material.DIAMOND) {
            return false;
        }

        // Проверяем углы - должно быть стекло
        int[] glassSlots = {0, 2, 6, 8};
        for (int slot : glassSlots) {
            if (matrix[slot] == null || matrix[slot].getType() != Material.GLASS) {
                return false;
            }
        }

        // Проверяем стороны - должны быть железные слитки
        int[] ironSlots = {1, 3, 5, 7};
        for (int slot : ironSlots) {
            if (matrix[slot] == null || matrix[slot].getType() != Material.IRON_INGOT) {
                return false;
            }
        }

        return true;
    }
}
