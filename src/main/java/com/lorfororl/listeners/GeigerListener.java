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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class GeigerListener implements Listener {
    
    private final LorForOrlPlugin plugin;
    
    public GeigerListener(LorForOrlPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !plugin.getUraniumItems().isGeigerCounter(item)) {
            return;
        }
        
        if (event.getAction().name().contains("RIGHT_CLICK")) {
            event.setCancelled(true);
            
            if (player.isSneaking()) {
                // Проверка области
                checkAreaRadiation(player);
            } else {
                // Проверка игрока
                checkPlayerRadiation(player);
            }
        }
    }
    
    private void checkPlayerRadiation(Player player) {
        double radiation = plugin.getRadiationManager().getPlayerRadiation(player);
        
        // Звуковые эффекты в зависимости от уровня радиации
        if (radiation >= 3.0) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.7f);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.9f);
        } else if (radiation >= 1.5) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CLICK, 1.0f, 1.0f);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CLICK, 1.0f, 1.2f);
        } else if (radiation >= 0.5) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CLICK, 0.5f, 1.5f);
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.3f, 2.0f);
        }
        
        // Визуальные эффекты
        showRadiationLevel(player, radiation);
        
        // Отправляем информацию через action bar
        String message = formatRadiationMessage(radiation);
        player.sendActionBar(message);
    }
    
    private void checkAreaRadiation(Player player) {
        Location center = player.getLocation();
        double totalRadiation = 0.0;
        int uraniumBlocks = 0;
        
        // Проверяем область 5x5x5 вокруг игрока
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    Location loc = center.clone().add(x, y, z);
                    Block block = loc.getBlock();
                    
                    // Проверяем, является ли блок урановым
                    if (block.getType() == Material.IRON_BLOCK) {
                        // Здесь нужно проверить NBT блока, но это сложно
                        // Для упрощения считаем все железные блоки потенциально урановыми
                        ItemStack blockItem = new ItemStack(block.getType());
                        if (plugin.getUraniumItems().isUraniumItem(blockItem)) {
                            totalRadiation += 1.0; // Радиация уранового блока
                            uraniumBlocks++;
                        }
                    }
                }
            }
        }
        
        // Также проверяем радиацию от других игроков поблизости
        for (Player nearbyPlayer : center.getWorld().getPlayers()) {
            if (nearbyPlayer.equals(player)) continue;
            
            double distance = center.distance(nearbyPlayer.getLocation());
            if (distance <= 5.0) {
                double playerRadiation = plugin.getRadiationManager().getPlayerRadiation(nearbyPlayer);
                totalRadiation += playerRadiation * (1.0 - distance / 5.0); // Уменьшается с расстоянием
            }
        }
        
        // Звуковые эффекты
        if (totalRadiation >= 2.0) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        } else if (totalRadiation >= 0.5) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CLICK, 0.8f, 1.0f);
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.8f);
        }
        
        // Показываем частицы в зависимости от уровня радиации
        if (totalRadiation > 0) {
            Particle particle = totalRadiation >= 1.0 ? Particle.ANGRY_VILLAGER : Particle.HAPPY_VILLAGER;
            int count = Math.min(20, (int) (totalRadiation * 10));
            player.getWorld().spawnParticle(particle, center.add(0, 1, 0), count, 2, 1, 2, 0.1);
        }
        
        // Сообщение в action bar
        String message = String.format("§6Область: §c%.3f у.е. §7(блоков: %d)", totalRadiation, uraniumBlocks);
        player.sendActionBar(message);
    }
    
    private void showRadiationLevel(Player player, double radiation) {
        Location loc = player.getLocation().add(0, 2, 0);
        
        if (radiation >= 3.0) {
            // Критический уровень - красные частицы
            player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, loc, 10, 0.5, 0.5, 0.5, 0.1);
        } else if (radiation >= 1.5) {
            // Высокий уровень - оранжевые частицы
            player.getWorld().spawnParticle(Particle.FLAME, loc, 5, 0.3, 0.3, 0.3, 0.05);
        } else if (radiation >= 0.5) {
            // Средний уровень - желтые частицы
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 3, 0.2, 0.2, 0.2, 0.05);
        } else {
            // Безопасный уровень - зеленые частицы
            player.getWorld().spawnParticle(Particle.COMPOSTER, loc, 1, 0.1, 0.1, 0.1, 0.01);
        }
    }
    
    private String formatRadiationMessage(double radiation) {
        String level;
        String color;
        
        if (radiation >= 3.0) {
            level = "КРИТИЧЕСКИЙ";
            color = "§4";
        } else if (radiation >= 1.5) {
            level = "ВЫСОКИЙ";
            color = "§c";
        } else if (radiation >= 0.5) {
            level = "СРЕДНИЙ";
            color = "§e";
        } else {
            level = "БЕЗОПАСНЫЙ";
            color = "§a";
        }
        
        return String.format("§6☢ Радиация: %s%.4f у.е. §7(%s%s§7)", 
            color, radiation, color, level);
    }
}
