package com.lorfororl.listeners;

import com.lorfororl.LorForOrlPlugin;
import com.lorfororl.equipment.PowerArmorSystem;
import com.lorfororl.weapons.RailgunSystem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public class MouseWheelListener implements Listener {
    
    private final LorForOrlPlugin plugin;
    
    public MouseWheelListener(LorForOrlPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        
        // Проверяем, зажат ли Shift
        if (!player.isSneaking()) return;
        
        // Определяем направление прокрутки
        int previousSlot = event.getPreviousSlot();
        int newSlot = event.getNewSlot();
        
        boolean scrollUp = (newSlot == 0 && previousSlot == 8) || 
                          (newSlot < previousSlot && !(newSlot == 0 && previousSlot == 8));
        
        // Проверяем силовую броню
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate != null && 
            plugin.getLaboratoryItems().isLaboratoryItem(chestplate) &&
            "power_armor".equals(plugin.getLaboratoryItems().getLaboratoryItemType(chestplate))) {
            
            event.setCancelled(true);
            cyclePowerArmorMode(player, scrollUp);
            return;
        }
        
        // Проверяем рельсотрон в руке
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem != null && 
            plugin.getLaboratoryItems().isLaboratoryItem(heldItem) &&
            "railgun".equals(plugin.getLaboratoryItems().getLaboratoryItemType(heldItem))) {
            
            event.setCancelled(true);
            cycleRailgunMode(player, scrollUp);
            return;
        }
    }
    
    private void cyclePowerArmorMode(Player player, boolean scrollUp) {
        PowerArmorSystem.PowerArmorData data = plugin.getPowerArmorSystem().getArmorData(player);
        if (data == null) return;
        
        PowerArmorSystem.PowerArmorMode currentMode = data.getMode();
        PowerArmorSystem.PowerArmorMode nextMode;
        
        // Убираем режим FLIGHT из списка доступных режимов
        PowerArmorSystem.PowerArmorMode[] modes = {
            PowerArmorSystem.PowerArmorMode.NORMAL,
            PowerArmorSystem.PowerArmorMode.COMBAT,
            PowerArmorSystem.PowerArmorMode.STEALTH,
            PowerArmorSystem.PowerArmorMode.SHIELD
        };
        
        int currentIndex = -1;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == currentMode) {
                currentIndex = i;
                break;
            }
        }
        
        if (currentIndex == -1) {
            nextMode = PowerArmorSystem.PowerArmorMode.NORMAL;
        } else {
            if (scrollUp) {
                nextMode = modes[(currentIndex + 1) % modes.length];
            } else {
                nextMode = modes[(currentIndex - 1 + modes.length) % modes.length];
            }
        }
        
        if (plugin.getPowerArmorSystem().switchMode(player, nextMode)) {
            // Визуальные эффекты переключения
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.5f);
            
            // Показываем HUD с новым режимом
            String modeColor = getModeColor(nextMode);
            player.sendActionBar("§6⚡ Режим брони: " + modeColor + nextMode.getDisplayName());
        }
    }
    
    private void cycleRailgunMode(Player player, boolean scrollUp) {
        RailgunSystem.RailgunData data = plugin.getRailgunSystem().getRailgunData(player);
        if (data == null) return;
        
        RailgunSystem.RailgunMode currentMode = data.getMode();
        RailgunSystem.RailgunMode[] modes = RailgunSystem.RailgunMode.values();
        
        int currentIndex = -1;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == currentMode) {
                currentIndex = i;
                break;
            }
        }
        
        if (currentIndex == -1) {
            currentIndex = 0;
        }
        
        RailgunSystem.RailgunMode nextMode;
        if (scrollUp) {
            nextMode = modes[(currentIndex + 1) % modes.length];
        } else {
            nextMode = modes[(currentIndex - 1 + modes.length) % modes.length];
        }
        
        if (plugin.getRailgunSystem().switchMode(player, nextMode)) {
            // Визуальные эффекты переключения
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.8f);
            
            // Показываем HUD с новым режимом
            String modeColor = getRailgunModeColor(nextMode);
            player.sendActionBar("§c⚡ Режим рельсотрона: " + modeColor + nextMode.getDisplayName());
        }
    }
    
    private String getModeColor(PowerArmorSystem.PowerArmorMode mode) {
        switch (mode) {
            case NORMAL: return "§a";
            case COMBAT: return "§c";
            case STEALTH: return "§5";
            case SHIELD: return "§e";
            case LOW_POWER: return "§8";
            default: return "§7";
        }
    }
    
    private String getRailgunModeColor(RailgunSystem.RailgunMode mode) {
        switch (mode) {
            case STANDARD: return "§7";
            case PIERCING: return "§b";
            case EXPLOSIVE: return "§c";
            case SCATTER: return "§6";
            case OVERCHARGE: return "§d";
            case EMP: return "§9";
            default: return "§7";
        }
    }
}
