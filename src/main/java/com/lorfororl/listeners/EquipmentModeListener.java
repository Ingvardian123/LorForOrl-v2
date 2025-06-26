package com.lorfororl.listeners;

import com.lorfororl.LorForOrlPlugin;
import com.lorfororl.equipment.PowerArmorSystem;
import com.lorfororl.weapons.RailgunSystem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

public class EquipmentModeListener implements Listener {
    
    private final LorForOrlPlugin plugin;
    
    public EquipmentModeListener(LorForOrlPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        
        if (!event.isSneaking()) return;
        
        // Проверяем силовую броню
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate != null && 
            plugin.getLaboratoryItems().isLaboratoryItem(chestplate) &&
            "power_armor".equals(plugin.getLaboratoryItems().getLaboratoryItemType(chestplate))) {
            
            // Переключаем режим силовой брони при приседании + прыжок
            if (player.isOnGround()) {
                cyclePowerArmorMode(player);
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT_CLICK")) return;
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !plugin.getLaboratoryItems().isLaboratoryItem(item)) return;
        
        String itemType = plugin.getLaboratoryItems().getLaboratoryItemType(item);
        
        if ("railgun".equals(itemType)) {
            if (player.isSneaking()) {
                // Переключение режима рельсотрона
                event.setCancelled(true);
                cycleRailgunMode(player);
            } else {
                // Обычный выстрел
                event.setCancelled(true);
                if (plugin.getRailgunSystem().fireRailgun(player, item)) {
                    player.sendActionBar("§aРельсотрон выстрелил!");
                }
            }
        }
    }
    
    private void cyclePowerArmorMode(Player player) {
        PowerArmorSystem.PowerArmorData data = plugin.getPowerArmorSystem().getArmorData(player);
        if (data == null) return;
        
        PowerArmorSystem.PowerArmorMode currentMode = data.getMode();
        PowerArmorSystem.PowerArmorMode nextMode;
        
        switch (currentMode) {
            case NORMAL:
                nextMode = PowerArmorSystem.PowerArmorMode.COMBAT;
                break;
            case COMBAT:
                nextMode = PowerArmorSystem.PowerArmorMode.STEALTH;
                break;
            case STEALTH:
                nextMode = PowerArmorSystem.PowerArmorMode.FLIGHT;
                break;
            case FLIGHT:
                nextMode = PowerArmorSystem.PowerArmorMode.SHIELD;
                break;
            case SHIELD:
                nextMode = PowerArmorSystem.PowerArmorMode.NORMAL;
                break;
            default:
                nextMode = PowerArmorSystem.PowerArmorMode.NORMAL;
                break;
        }
        
        plugin.getPowerArmorSystem().switchMode(player, nextMode);
    }
    
    private void cycleRailgunMode(Player player) {
        RailgunSystem.RailgunData data = plugin.getRailgunSystem().getRailgunData(player);
        if (data == null) return;
        
        RailgunSystem.RailgunMode currentMode = data.getMode();
        RailgunSystem.RailgunMode nextMode;
        
        switch (currentMode) {
            case STANDARD:
                nextMode = RailgunSystem.RailgunMode.PIERCING;
                break;
            case PIERCING:
                nextMode = RailgunSystem.RailgunMode.EXPLOSIVE;
                break;
            case EXPLOSIVE:
                nextMode = RailgunSystem.RailgunMode.SCATTER;
                break;
            case SCATTER:
                nextMode = RailgunSystem.RailgunMode.OVERCHARGE;
                break;
            case OVERCHARGE:
                nextMode = RailgunSystem.RailgunMode.EMP;
                break;
            case EMP:
                nextMode = RailgunSystem.RailgunMode.STANDARD;
                break;
            default:
                nextMode = RailgunSystem.RailgunMode.STANDARD;
                break;
        }
        
        plugin.getRailgunSystem().switchMode(player, nextMode);
    }
}
