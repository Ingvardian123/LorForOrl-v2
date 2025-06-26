package ru.lorfororl.systems;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.lorfororl.managers.EnergyManager;
import com.lorfororl.managers.NotificationManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PowerArmorSystem {
    private final EnergyManager energyManager;
    private final NotificationManager notificationManager;
    private final Map<UUID, PowerArmorData> activeSuits;
    
    public PowerArmorSystem(EnergyManager energyManager, NotificationManager notificationManager) {
        this.energyManager = energyManager;
        this.notificationManager = notificationManager;
        this.activeSuits = new HashMap<>();
    }
    
    public void activatePowerArmor(Player player) {
        if (!hasPowerArmor(player)) {
            notificationManager.sendMessage(player, "§cУ вас нет силовой брони!");
            return;
        }
        
        if (!energyManager.hasEnoughEnergy(player, 100)) {
            notificationManager.sendMessage(player, "§cНедостаточно энергии для активации брони!");
            return;
        }
        
        PowerArmorData armorData = new PowerArmorData();
        activeSuits.put(player.getUniqueId(), armorData);
        
        // Применяем эффекты силовой брони
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 2, false, false));
        
        energyManager.consumeEnergy(player, 100);
        notificationManager.sendMessage(player, "§aСиловая броня активирована!");
    }
    
    public void deactivatePowerArmor(Player player) {
        UUID playerId = player.getUniqueId();
        if (!activeSuits.containsKey(playerId)) {
            return;
        }
        
        activeSuits.remove(playerId);
        
        // Убираем эффекты силовой брони
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        
        notificationManager.sendMessage(player, "§cСиловая броня деактивирована!");
    }
    
    public boolean isPowerArmorActive(Player player) {
        return activeSuits.containsKey(player.getUniqueId());
    }
    
    public void updatePowerArmor(Player player) {
        UUID playerId = player.getUniqueId();
        if (!activeSuits.containsKey(playerId)) {
            return;
        }
        
        // Потребляем энергию каждую секунду
        if (!energyManager.hasEnoughEnergy(player, 1)) {
            deactivatePowerArmor(player);
            return;
        }
        
        energyManager.consumeEnergy(player, 1);
        
        PowerArmorData armorData = activeSuits.get(playerId);
        armorData.updateUsageTime();
        
        // Проверяем износ брони
        if (armorData.getUsageTime() > 3600) { // 1 час использования
            damageArmor(player);
            armorData.resetUsageTime();
        }
    }
    
    private boolean hasPowerArmor(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();
        
        return isPowerArmorPiece(helmet) && isPowerArmorPiece(chestplate) && 
               isPowerArmorPiece(leggings) && isPowerArmorPiece(boots);
    }
    
    private boolean isPowerArmorPiece(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName() && meta.getDisplayName().contains("Силовая броня");
    }
    
    private void damageArmor(Player player) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack piece : armor) {
            if (piece != null && isPowerArmorPiece(piece)) {
                // Уменьшаем прочность брони
                piece.setDurability((short) (piece.getDurability() + 10));
                if (piece.getDurability() >= piece.getType().getMaxDurability()) {
                    piece.setAmount(0); // Ломаем броню
                }
            }
        }
        player.getInventory().setArmorContents(armor);
    }
    
    public PowerArmorData getPowerArmorData(Player player) {
        return activeSuits.get(player.getUniqueId());
    }
    
    public void shutdown() {
        // Деактивируем всю активную броню при выключении плагина
        for (UUID playerId : activeSuits.keySet()) {
            Player player = org.bukkit.Bukkit.getPlayer(playerId);
            if (player != null) {
                deactivatePowerArmor(player);
            }
        }
        activeSuits.clear();
    }
    
    public static class PowerArmorData {
        private long usageTime;
        private long activationTime;
        
        public PowerArmorData() {
            this.activationTime = System.currentTimeMillis();
            this.usageTime = 0;
        }
        
        public void updateUsageTime() {
            this.usageTime++;
        }
        
        public long getUsageTime() {
            return usageTime;
        }
        
        public void resetUsageTime() {
            this.usageTime = 0;
        }
        
        public long getActivationTime() {
            return activationTime;
        }
    }
}
