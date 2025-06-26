package com.lorfororl.listeners;

import com.lorfororl.LorForOrlPlugin;
import com.lorfororl.structures.BuildingStructure;
import com.lorfororl.structures.StructureBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class StructureListener implements Listener {
    
    private final LorForOrlPlugin plugin;
    
    public StructureListener(LorForOrlPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null) return;
        
        // Проверяем строительные наборы
        if (plugin.getStructureItems().isStructureItem(item)) {
            String structureType = plugin.getStructureItems().getStructureType(item);
            
            if ("CONSTRUCTION_TOOL".equals(structureType)) {
                handleConstructionTool(event);
            } else {
                handleStructureKit(event);
            }
        }
    }
    
    private void handleStructureKit(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        StructureBuilder.StructureType type = plugin.getStructureItems().getStructureTypeEnum(item);
        if (type == null) return;
        
        // Определяем место строительства
        Location buildLocation;
        if (event.getClickedBlock() != null) {
            buildLocation = event.getClickedBlock().getLocation().add(0, 1, 0);
        } else {
            buildLocation = player.getLocation().add(0, -1, 0);
        }
        
        // Начинаем строительство
        if (plugin.getStructureBuilder().startBuilding(player, buildLocation, type, item)) {
            event.setCancelled(true);
        }
    }
    
    private void handleConstructionTool(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (event.getClickedBlock() == null) return;
        
        Location clickedLocation = event.getClickedBlock().getLocation();
        BuildingStructure structure = plugin.getStructureBuilder().getBuildingStructure(player);
        
        if (structure == null) {
            player.sendMessage("§cВы не строите никакую структуру!");
            return;
        }
        
        if (!structure.getLocation().equals(clickedLocation)) {
            player.sendMessage("§cЭто не ваша строительная площадка!");
            return;
        }
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                // Показать прогресс
                event.setCancelled(true);
                showBuildingProgress(player, structure);
            } else {
                // Добавить ресурсы
                event.setCancelled(true);
                addResourcesFromInventory(player, structure);
            }
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && player.isSneaking()) {
            // Отменить строительство
            event.setCancelled(true);
            plugin.getStructureBuilder().cancelBuilding(player);
        }
    }
    
    private void showBuildingProgress(Player player, BuildingStructure structure) {
        player.sendMessage("§6=== Прогресс строительства ===");
        player.sendMessage("§eСтруктура: " + structure.getType().getDisplayName());
        player.sendMessage(structure.getResourceStatus());
        
        long buildTime = (System.currentTimeMillis() - structure.getStartTime()) / 1000;
        player.sendMessage("§7Время строительства: " + buildTime + " секунд");
    }
    
    private void addResourcesFromInventory(Player player, BuildingStructure structure) {
        boolean addedAny = false;
        
        // Проходим по инвентарю и добавляем нужные ресурсы
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            
            Material material = item.getType();
            int needed = structure.getNeededResource(material);
            
            if (needed > 0) {
                int toAdd = Math.min(item.getAmount(), needed);
                structure.addResource(material, toAdd);
                item.setAmount(item.getAmount() - toAdd);
                
                player.sendMessage(String.format("§aДобавлено %dx %s", toAdd, material.name()));
                addedAny = true;
            }
        }
        
        if (!addedAny) {
            player.sendMessage("§cВ вашем инвентаре нет нужных ресурсов!");
            player.sendMessage("§eТребуется:");
            
            for (Material material : structure.getRequiredResources().keySet()) {
                int needed = structure.getNeededResource(material);
                if (needed > 0) {
                    player.sendMessage("§7• " + needed + "x " + material.name());
                }
            }
        } else {
            double progress = structure.getProgress() * 100;
            player.sendMessage(String.format("§eПрогресс строительства: %.1f%%", progress));
            
            if (structure.isComplete()) {
                player.sendMessage("§aВсе ресурсы добавлены! Структура будет построена автоматически.");
            }
        }
    }
}
