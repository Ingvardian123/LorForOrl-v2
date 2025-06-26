package com.lorfororl.listeners;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Sound;

public class StructureCraftListener implements Listener {
    
    private final LorForOrlPlugin plugin;
    
    public StructureCraftListener(LorForOrlPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        CraftingInventory inventory = event.getInventory();
        ItemStack result = inventory.getResult();
        
        if (result == null) return;
        
        // Проверяем крафт набора для лаборатории - ТОЛЬКО для авторизованных
        if (result.getType() == Material.DIAMOND_BLOCK) {
            if (isLaboratoryKitCraft(inventory)) {
                if (!plugin.getLaboratoryManager().isAuthorized(player)) {
                    event.setCancelled(true);
                    player.sendMessage("§c❌ У вас нет разрешения на создание лаборатории!");
                    player.sendMessage("§7Обратитесь к администратору для получения доступа.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                event.setResult(plugin.getStructureItems().createLaboratoryKit());
                player.sendMessage("§a✅ Набор для лаборатории создан!");
            }
        }
        
        // Проверяем крафт набора для реактора
        if (result.getType() == Material.BEACON) {
            if (isReactorKitCraft(inventory)) {
                event.setResult(plugin.getStructureItems().createReactorKit());
            }
        }
        
        // Проверяем крафт набора для генератора
        if (result.getType() == Material.REDSTONE_BLOCK) {
            if (isGeneratorKitCraft(inventory)) {
                event.setResult(plugin.getStructureItems().createGeneratorKit());
            }
        }
        
        // Проверяем крафт набора для исследовательской станции
        if (result.getType() == Material.ENCHANTING_TABLE) {
            if (isResearchStationKitCraft(inventory)) {
                event.setResult(plugin.getStructureItems().createResearchStationKit());
            }
        }
        
        // Проверяем крафт инструмента строителя
        if (result.getType() == Material.GOLDEN_PICKAXE) {
            if (isConstructionToolCraft(inventory)) {
                event.setResult(plugin.getStructureItems().createConstructionTool());
            }
        }
    }
    
    private boolean isLaboratoryKitCraft(CraftingInventory inventory) {
        ItemStack[] matrix = inventory.getMatrix();
        
        // Рецепт набора для лаборатории:
        // [I][D][I]
        // [R][B][R]  где I = железный блок, D = алмазный блок, R = редстоун блок, B = книга
        // [G][G][G]  G = стекло
        
        if (matrix.length != 9) return false;
        
        return matrix[0] != null && matrix[0].getType() == Material.IRON_BLOCK &&
               matrix[1] != null && matrix[1].getType() == Material.DIAMOND_BLOCK &&
               matrix[2] != null && matrix[2].getType() == Material.IRON_BLOCK &&
               matrix[3] != null && matrix[3].getType() == Material.REDSTONE_BLOCK &&
               matrix[4] != null && matrix[4].getType() == Material.BOOK &&
               matrix[5] != null && matrix[5].getType() == Material.REDSTONE_BLOCK &&
               matrix[6] != null && matrix[6].getType() == Material.GLASS &&
               matrix[7] != null && matrix[7].getType() == Material.GLASS &&
               matrix[8] != null && matrix[8].getType() == Material.GLASS;
    }
    
    private boolean isReactorKitCraft(CraftingInventory inventory) {
        ItemStack[] matrix = inventory.getMatrix();
        
        // Рецепт набора для реактора:
        // [O][D][O]
        // [I][B][I]  где O = обсидиан, D = алмазный блок, I = железный блок, B = маяк
        // [O][U][O]  U = урановый блок
        
        if (matrix.length != 9) return false;
        
        // Проверяем урановый блок
        boolean hasUraniumBlock = matrix[7] != null && 
                                 plugin.getUraniumItems().isUraniumItem(matrix[7]) &&
                                 "block".equals(plugin.getUraniumItems().getUraniumType(matrix[7]));
        
        return matrix[0] != null && matrix[0].getType() == Material.OBSIDIAN &&
               matrix[1] != null && matrix[1].getType() == Material.DIAMOND_BLOCK &&
               matrix[2] != null && matrix[2].getType() == Material.OBSIDIAN &&
               matrix[3] != null && matrix[3].getType() == Material.IRON_BLOCK &&
               matrix[4] != null && matrix[4].getType() == Material.BEACON &&
               matrix[5] != null && matrix[5].getType() == Material.IRON_BLOCK &&
               matrix[6] != null && matrix[6].getType() == Material.OBSIDIAN &&
               hasUraniumBlock &&
               matrix[8] != null && matrix[8].getType() == Material.OBSIDIAN;
    }
    
    private boolean isGeneratorKitCraft(CraftingInventory inventory) {
        ItemStack[] matrix = inventory.getMatrix();
        
        // Рецепт набора для генератора:
        // [L][G][L]
        // [C][R][C]  где L = громоотвод, G = золотой блок, C = медный блок, R = редстоун блок
        // [C][C][C]
        
        if (matrix.length != 9) return false;
        
        return matrix[0] != null && matrix[0].getType() == Material.LIGHTNING_ROD &&
               matrix[1] != null && matrix[1].getType() == Material.GOLD_BLOCK &&
               matrix[2] != null && matrix[2].getType() == Material.LIGHTNING_ROD &&
               matrix[3] != null && matrix[3].getType() == Material.COPPER_BLOCK &&
               matrix[4] != null && matrix[4].getType() == Material.REDSTONE_BLOCK &&
               matrix[5] != null && matrix[5].getType() == Material.COPPER_BLOCK &&
               matrix[6] != null && matrix[6].getType() == Material.COPPER_BLOCK &&
               matrix[7] != null && matrix[7].getType() == Material.COPPER_BLOCK &&
               matrix[8] != null && matrix[8].getType() == Material.COPPER_BLOCK;
    }
    
    private boolean isResearchStationKitCraft(CraftingInventory inventory) {
        ItemStack[] matrix = inventory.getMatrix();
        
        // Рецепт набора для исследовательской станции:
        // [B][D][B]
        // [Q][E][Q]  где B = книжная полка, D = алмаз, Q = кварцевый блок, E = стол зачарований
        // [L][Q][L]  L = лекторн
        
        if (matrix.length != 9) return false;
        
        return matrix[0] != null && matrix[0].getType() == Material.BOOKSHELF &&
               matrix[1] != null && matrix[1].getType() == Material.DIAMOND &&
               matrix[2] != null && matrix[2].getType() == Material.BOOKSHELF &&
               matrix[3] != null && matrix[3].getType() == Material.QUARTZ_BLOCK &&
               matrix[4] != null && matrix[4].getType() == Material.ENCHANTING_TABLE &&
               matrix[5] != null && matrix[5].getType() == Material.QUARTZ_BLOCK &&
               matrix[6] != null && matrix[6].getType() == Material.LECTERN &&
               matrix[7] != null && matrix[7].getType() == Material.QUARTZ_BLOCK &&
               matrix[8] != null && matrix[8].getType() == Material.LECTERN;
    }
    
    private boolean isConstructionToolCraft(CraftingInventory inventory) {
        ItemStack[] matrix = inventory.getMatrix();
        
        // Рецепт инструмента строителя:
        // [ ][D][ ]
        // [ ][S][ ]  где D = алмаз, S = палка, G = золотой слиток
        // [G][G][G]
        
        if (matrix.length != 9) return false;
        
        return matrix[1] != null && matrix[1].getType() == Material.DIAMOND &&
               matrix[4] != null && matrix[4].getType() == Material.STICK &&
               matrix[6] != null && matrix[6].getType() == Material.GOLD_INGOT &&
               matrix[7] != null && matrix[7].getType() == Material.GOLD_INGOT &&
               matrix[8] != null && matrix[8].getType() == Material.GOLD_INGOT &&
               matrix[0] == null && matrix[2] == null && matrix[3] == null && matrix[5] == null;
    }
}
