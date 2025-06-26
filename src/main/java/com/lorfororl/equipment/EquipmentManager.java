package com.lorfororl.equipment;

import com.lorfororl.LorForOrlPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EquipmentManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, EquipmentStatus> playerEquipment;
    private BukkitRunnable equipmentTask;
    
    public EquipmentManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.playerEquipment = new HashMap<>();
    }
    
    public void startTasks() {
        equipmentTask = new BukkitRunnable() {
            @Override
            public void run() {
                updatePlayerEquipment();
            }
        };
        equipmentTask.runTaskTimer(plugin, 0L, 20L); // Каждую секунду
    }
    
    public void shutdown() {
        if (equipmentTask != null) {
            equipmentTask.cancel();
        }
    }
    
    private void updatePlayerEquipment() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updatePlayerEquipmentStatus(player);
            applyEquipmentEffects(player);
        }
    }
    
    private void updatePlayerEquipmentStatus(Player player) {
        EquipmentStatus status = playerEquipment.computeIfAbsent(player.getUniqueId(), k -> new EquipmentStatus());
        
        // Проверяем броню
        ItemStack chestplate = player.getInventory().getChestplate();
        
        if (chestplate != null && isLaboratoryItem(chestplate)) {
            String itemType = getLaboratoryItemType(chestplate);
            
            switch (itemType) {
                case "hazmat_suit":
                    status.setHasHazmatSuit(true);
                    status.setHasPowerArmor(false);
                    break;
                case "power_armor":
                    status.setHasPowerArmor(true);
                    status.setHasHazmatSuit(false);
                    break;
                default:
                    status.setHasHazmatSuit(false);
                    status.setHasPowerArmor(false);
                    break;
            }
        } else {
            status.setHasHazmatSuit(false);
            status.setHasPowerArmor(false);
        }
        
        // Проверяем оружие в руках
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand != null && isLaboratoryItem(mainHand)) {
            String itemType = getLaboratoryItemType(mainHand);
            status.setHasRailgun("railgun".equals(itemType));
        } else {
            status.setHasRailgun(false);
        }
    }
    
    private boolean isLaboratoryItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().hasCustomModelData() && 
               item.getItemMeta().getCustomModelData() >= 2001 && 
               item.getItemMeta().getCustomModelData() <= 2010;
    }
    
    private String getLaboratoryItemType(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) return "unknown";
        
        int cmd = item.getItemMeta().getCustomModelData();
        switch (cmd) {
            case 2001: return "hazmat_suit";
            case 2002: return "power_armor";
            case 2003: return "railgun";
            default: return "unknown";
        }
    }
    
    private void applyEquipmentEffects(Player player) {
        EquipmentStatus status = playerEquipment.get(player.getUniqueId());
        if (status == null) return;
        
        // Эффекты костюма химзащиты
        if (status.hasHazmatSuit()) {
            applyHazmatSuitEffects(player);
        }
        
        // Эффекты силовой брони
        if (status.hasPowerArmor()) {
            applyPowerArmorEffects(player);
        }
        
        // Защита от радиации
        if (status.hasHazmatSuit() || status.hasPowerArmor()) {
            reduceRadiation(player, status);
        }
    }
    
    private void applyHazmatSuitEffects(Player player) {
        // Защита от урона
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 1, false, false));
        
        // Защита от голода (фильтрация воздуха)
        if (player.getFoodLevel() < 20) {
            player.setFoodLevel(Math.min(20, player.getFoodLevel() + 1));
        }
    }
    
    private void applyPowerArmorEffects(Player player) {
        // Проверяем энергию
        int energy = plugin.getEnergyManager().getPlayerEnergy(player);
        
        if (energy > 0) {
            // Потребляем энергию
            plugin.getEnergyManager().consumePlayerEnergy(player, 1);
            
            // Эффекты при наличии энергии
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 2, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 40, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 40, 0, false, false));
            
            // Регенерация здоровья
            if (player.getHealth() < player.getMaxHealth()) {
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 0.5));
            }
        } else {
            // Без энергии - только базовая защита
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 0, false, false));
            
            // Уведомление о низкой энергии
            if (System.currentTimeMillis() % 5000 < 1000) { // Каждые 5 секунд
                sendActionBar(player, "§cНизкий заряд силовой брони! Найдите источник энергии.");
            }
        }
    }
    
    private void reduceRadiation(Player player, EquipmentStatus status) {
        double currentRadiation = plugin.getRadiationManager().getPlayerRadiation(player);
        
        if (currentRadiation > 0) {
            double reduction = 0;
            
            if (status.hasPowerArmor()) {
                reduction = currentRadiation * 0.95; // 95% защита
            } else if (status.hasHazmatSuit()) {
                reduction = currentRadiation * 0.90; // 90% защита
            }
            
            if (reduction > 0) {
                // Уменьшаем радиацию
                double newRadiation = Math.max(0, currentRadiation - reduction * 0.1); // Постепенное снижение
                plugin.getRadiationManager().setPlayerRadiation(player, newRadiation);
            }
        }
    }
    
    private void sendActionBar(Player player, String message) {
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        } catch (Exception e) {
            // Fallback для старых версий
            player.sendMessage(message);
        }
    }
    
    public EquipmentStatus getPlayerEquipment(Player player) {
        return playerEquipment.get(player.getUniqueId());
    }
    
    public static class EquipmentStatus {
        private boolean hasHazmatSuit;
        private boolean hasPowerArmor;
        private boolean hasRailgun;
        
        public boolean hasHazmatSuit() { return hasHazmatSuit; }
        public void setHasHazmatSuit(boolean hasHazmatSuit) { this.hasHazmatSuit = hasHazmatSuit; }
        
        public boolean hasPowerArmor() { return hasPowerArmor; }
        public void setHasPowerArmor(boolean hasPowerArmor) { this.hasPowerArmor = hasPowerArmor; }
        
        public boolean hasRailgun() { return hasRailgun; }
        public void setHasRailgun(boolean hasRailgun) { this.hasRailgun = hasRailgun; }
    }
}
