package com.lorfororl.listeners;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class WeaponListener implements Listener {
    
    private final LorForOrlPlugin plugin;
    
    public WeaponListener(LorForOrlPlugin plugin) {
        this.plugin = plugin;
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
                // Переключение режима рельсотрона обрабатывается в EquipmentModeListener
                return;
            } else {
                // Обычный выстрел
                event.setCancelled(true);
                if (plugin.getRailgunSystem().fireRailgun(player, item)) {
                    player.sendActionBar("§aРельсотрон выстрелил!");
                }
            }
        }
    }
}
