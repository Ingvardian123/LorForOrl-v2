package com.lorfororl.listeners;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

public class CapsuleListener implements Listener {
    
    private final LorForOrlPlugin plugin;
    private final NamespacedKey capsuleDataKey;
    
    public CapsuleListener(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.capsuleDataKey = new NamespacedKey(plugin, "capsule_data");
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null) return;
        
        // Проверяем взаимодействие с капсулой
        if (plugin.getUraniumItems().isUraniumCapsule(item)) {
            handleCapsuleInteraction(event);
        }
        // Проверяем взаимодействие с размещенной капсулой
        else if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.END_CRYSTAL) {
            handlePlacedCapsuleInteraction(event);
        }
    }
    
    private void handleCapsuleInteraction(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack capsule = event.getItem();
        
        if (!event.getAction().name().contains("RIGHT_CLICK")) return;
        
        // Проверяем, держит ли игрок урановую пыль в другой руке
        ItemStack otherHand = player.getInventory().getItemInOffHand();
        if (otherHand.equals(capsule)) {
            otherHand = player.getInventory().getItemInMainHand();
        }
        
        if (plugin.getUraniumItems().isUraniumItem(otherHand) && 
            "dust".equals(plugin.getUraniumItems().getUraniumType(otherHand))) {
            
            // Загружаем пыль в капсулу
            loadDustIntoCapsule(player, capsule, otherHand);
            event.setCancelled(true);
        }
    }
    
    private void handlePlacedCapsuleInteraction(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        
        if (!event.getAction().name().contains("RIGHT_CLICK")) return;
        
        // Проверяем, есть ли данные капсулы в блоке
        if (!block.getChunk().getPersistentDataContainer().has(
            new NamespacedKey(plugin, "capsule_" + block.getX() + "_" + block.getY() + "_" + block.getZ()), 
            PersistentDataType.INTEGER)) {
            return;
        }
        
        if (player.isSneaking()) {
            // Выгружаем пыль из капсулы
            unloadDustFromPlacedCapsule(player, block);
        } else {
            // Показываем информацию о капсуле
            showCapsuleInfo(player, block);
        }
        
        event.setCancelled(true);
    }
    
    private void loadDustIntoCapsule(Player player, ItemStack capsule, ItemStack dust) {
        int currentDust = plugin.getUraniumItems().getCapsuleDustAmount(capsule);
        int dustToAdd = dust.getAmount();
        int maxCapacity = 500;
        
        if (currentDust >= maxCapacity) {
            player.sendActionBar("§cКапсула заполнена! (500/500)");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
            return;
        }
        
        int canAdd = Math.min(dustToAdd, maxCapacity - currentDust);
        int newTotal = currentDust + canAdd;
        
        // Обновляем капсулу
        ItemStack updatedCapsule = plugin.getUraniumItems().updateCapsule(capsule, newTotal);
        
        // Обновляем предмет в руке игрока
        if (player.getInventory().getItemInMainHand().equals(capsule)) {
            player.getInventory().setItemInMainHand(updatedCapsule);
        } else {
            player.getInventory().setItemInOffHand(updatedCapsule);
        }
        
        // Уменьшаем количество пыли
        if (canAdd == dustToAdd) {
            dust.setAmount(0); // Убираем весь стак
        } else {
            dust.setAmount(dustToAdd - canAdd);
        }
        
        // Эффекты
        player.sendActionBar(String.format("§aЗагружено %d пыли! (%d/500)", canAdd, newTotal));
        player.playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 1.2f);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, 
            player.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.1);
    }
    
    private void unloadDustFromPlacedCapsule(Player player, Block block) {
        NamespacedKey dustKey = new NamespacedKey(plugin, "capsule_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
        int dustAmount = block.getChunk().getPersistentDataContainer().getOrDefault(dustKey, PersistentDataType.INTEGER, 0);
        
        if (dustAmount <= 0) {
            player.sendActionBar("§cКапсула пуста!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
            return;
        }
        
        // Выгружаем максимум 64 пыли за раз
        int toUnload = Math.min(64, dustAmount);
        ItemStack dustItem = plugin.getUraniumItems().createUraniumDust(toUnload);
        
        // Пытаемся добавить в инвентарь
        if (player.getInventory().firstEmpty() == -1) {
            player.sendActionBar("§cИнвентарь заполнен!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
            return;
        }
        
        player.getInventory().addItem(dustItem);
        
        // Обновляем данные капсулы
        int newAmount = dustAmount - toUnload;
        if (newAmount > 0) {
            block.getChunk().getPersistentDataContainer().set(dustKey, PersistentDataType.INTEGER, newAmount);
        } else {
            block.getChunk().getPersistentDataContainer().remove(dustKey);
        }
        
        // Эффекты
        player.sendActionBar(String.format("§aВыгружено %d пыли! (осталось: %d)", toUnload, newAmount));
        player.playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 0.8f);
        
        // Визуальные эффекты на блоке
        Location loc = block.getLocation().add(0.5, 1, 0.5);
        player.getWorld().spawnParticle(Particle.COMPOSTER, loc, 10, 0.3, 0.3, 0.3, 0.1);
    }
    
    private void showCapsuleInfo(Player player, Block block) {
        NamespacedKey dustKey = new NamespacedKey(plugin, "capsule_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
        int dustAmount = block.getChunk().getPersistentDataContainer().getOrDefault(dustKey, PersistentDataType.INTEGER, 0);
        double radiation = dustAmount * 0.01;
        
        player.sendActionBar(String.format("§bКапсула: §a%d§7/§a500 пыли §c(%.3f у.е.)", 
            dustAmount, radiation));
        
        // Звуковой сигнал в зависимости от заполненности
        float pitch = 0.5f + (dustAmount / 500.0f); // От 0.5 до 1.5
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, pitch);
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        
        if (!plugin.getUraniumItems().isUraniumCapsule(item)) return;
        
        // Сохраняем данные капсулы в блоке
        Block block = event.getBlockPlaced();
        int dustAmount = plugin.getUraniumItems().getCapsuleDustAmount(item);
        
        NamespacedKey dustKey = new NamespacedKey(plugin, "capsule_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
        block.getChunk().getPersistentDataContainer().set(dustKey, PersistentDataType.INTEGER, dustAmount);
        
        // Эффекты размещения
        Player player = event.getPlayer();
        player.sendActionBar(String.format("§bКапсула размещена! (%d/500 пыли)", dustAmount));
        player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0f, 1.0f);
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        
        if (block.getType() != Material.END_CRYSTAL) return;
        
        // Проверяем, есть ли данные капсулы
        NamespacedKey dustKey = new NamespacedKey(plugin, "capsule_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
        
        if (!block.getChunk().getPersistentDataContainer().has(dustKey, PersistentDataType.INTEGER)) {
            return; // Обычный кристалл Края
        }
        
        event.setCancelled(true); // Отменяем стандартный дроп
        
        int dustAmount = block.getChunk().getPersistentDataContainer().get(dustKey, PersistentDataType.INTEGER);
        
        // Создаем капсулу с сохраненной пылью
        ItemStack capsule = plugin.getUraniumItems().createUraniumCapsule(dustAmount);
        
        // Дропаем капсулу
        block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), capsule);
        
        // Удаляем блок и данные
        block.setType(Material.AIR);
        block.getChunk().getPersistentDataContainer().remove(dustKey);
        
        // Эффекты
        Player player = event.getPlayer();
        player.sendActionBar(String.format("§bКапсула собрана! (%d/500 пыли)", dustAmount));
        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.2f);
    }
}
