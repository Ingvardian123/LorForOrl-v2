package com.lorfororl.radiation;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RadiationManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, Double> playerRadiation;
    private BukkitRunnable radiationTask;
    private BukkitRunnable decayTask;
    private final Set<UUID> debugPlayers = new HashSet<>();
    
    public RadiationManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.playerRadiation = new HashMap<>();
    }
    
    public void startTasks() {
        // Задача проверки радиации каждые 5 секунд
        radiationTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    updatePlayerRadiation(player);
                }
            }
        };
        radiationTask.runTaskTimer(plugin, 0L, 100L); // 5 секунд
        
        // Задача естественного спада радиации каждую минуту
        decayTask = new BukkitRunnable() {
            @Override
            public void run() {
                decayRadiation();
            }
        };
        decayTask.runTaskTimer(plugin, 0L, 1200L); // 1 минута
    }
    
    public void shutdown() {
        if (radiationTask != null) radiationTask.cancel();
        if (decayTask != null) decayTask.cancel();
    }
    
    private void updatePlayerRadiation(Player player) {
        double totalRadiation = calculatePlayerRadiation(player);
        playerRadiation.put(player.getUniqueId(), totalRadiation);
        
        // Применяем эффекты
        applyRadiationEffects(player, totalRadiation);
        
        // Заражаем других игроков поблизости
        infectNearbyPlayers(player, totalRadiation);
    }
    
    private double calculatePlayerRadiation(Player player) {
        double radiation = 0.0;
        
        // Радиация от предметов в инвентаре
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isUraniumItem(item)) {
                radiation += getRadiation(item) * item.getAmount();
            }
        }
        
        // Радиация от радиационных зон
        if (plugin.getRadiationZoneManager() != null) {
            radiation += plugin.getRadiationZoneManager().getTotalRadiationAt(player.getLocation());
        }
        
        return radiation;
    }
    
    private boolean isUraniumItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().hasCustomModelData() && 
               item.getItemMeta().getCustomModelData() >= 1001 && 
               item.getItemMeta().getCustomModelData() <= 1005;
    }
    
    private double getRadiation(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) return 0.0;
        
        int cmd = item.getItemMeta().getCustomModelData();
        switch (cmd) {
            case 1001: return 0.1; // uranium_dust
            case 1002: return 0.3; // uranium_ingot
            case 1003: return 1.0; // uranium_block
            case 1005: return 0.5; // uranium_capsule
            default: return 0.0;
        }
    }
    
    private void applyRadiationEffects(Player player, double radiation) {
        // Убираем старые эффекты
        player.removePotionEffect(PotionEffectType.NAUSEA);
        player.removePotionEffect(PotionEffectType.WITHER);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        
        if (radiation >= 0.5) {
            // Тошнота
            player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 120, 0, false, false));
            
            // Зеленые частицы
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, 
                player.getLocation().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.1);
        }
        
        if (radiation >= 1.5) {
            // Урон от радиации
            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 120, 0, false, false));
            
            // Зеленые кольца
            player.getWorld().spawnParticle(Particle.COMPOSTER, 
                player.getLocation().add(0, 1, 0), 8, 0.5, 0.5, 0.5, 0.2);
        }
        
        if (radiation >= 3.0) {
            // Слепота
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 120, 0, false, false));
            
            // Большие зеленые облака
            player.getWorld().spawnParticle(Particle.SNEEZE, 
                player.getLocation().add(0, 1, 0), 15, 1, 1, 1, 0.3);
        }
        
        // Отладочная информация
        if (isInDebugMode(player)) {
            player.sendMessage(String.format("§7[DEBUG] Радиация: %.4f у.е.", radiation));
            if (radiation >= 0.5) {
                player.sendMessage("§7[DEBUG] Эффекты: Тошнота");
            }
            if (radiation >= 1.5) {
                player.sendMessage("§7[DEBUG] Эффекты: Урон от радиации");
            }
            if (radiation >= 3.0) {
                player.sendMessage("§7[DEBUG] Эффекты: Слепота");
            }
        }
    }
    
    private void infectNearbyPlayers(Player source, double radiation) {
        if (radiation < 1.0) return; // Заражение только при высокой радиации
        
        for (Player nearby : source.getWorld().getPlayers()) {
            if (nearby.equals(source)) continue;
            
            double distance = source.getLocation().distance(nearby.getLocation());
            if (distance <= 5.0) {
                // Добавляем небольшую радиацию соседним игрокам
                double currentRadiation = playerRadiation.getOrDefault(nearby.getUniqueId(), 0.0);
                double infectionAmount = (radiation * 0.1) / distance; // Чем дальше, тем меньше заражение
                playerRadiation.put(nearby.getUniqueId(), currentRadiation + infectionAmount);
            }
        }
    }
    
    private void decayRadiation() {
        // Естественный спад радиации на 1% в минуту
        playerRadiation.replaceAll((uuid, radiation) -> Math.max(0.0, radiation * 0.99));
        
        // Удаляем игроков с нулевой радиацией
        playerRadiation.entrySet().removeIf(entry -> entry.getValue() <= 0.001);
    }
    
    public double getPlayerRadiation(Player player) {
        return playerRadiation.getOrDefault(player.getUniqueId(), 0.0);
    }
    
    public void addRadiation(Player player, double amount) {
        double current = playerRadiation.getOrDefault(player.getUniqueId(), 0.0);
        playerRadiation.put(player.getUniqueId(), current + amount);
    }

    public void setPlayerRadiation(Player player, double radiation) {
        playerRadiation.put(player.getUniqueId(), Math.max(0.0, radiation));
    }

    public boolean toggleDebugMode(Player player) {
        UUID playerId = player.getUniqueId();
        if (debugPlayers.contains(playerId)) {
            debugPlayers.remove(playerId);
            return false;
        } else {
            debugPlayers.add(playerId);
            return true;
        }
    }

    public boolean isInDebugMode(Player player) {
        return debugPlayers.contains(player.getUniqueId());
    }
}
