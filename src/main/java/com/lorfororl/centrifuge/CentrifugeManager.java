package com.lorfororl.centrifuge;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class CentrifugeManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, Centrifuge> centrifuges;
    private final Random random;
    private BukkitRunnable mainTask;
    
    public CentrifugeManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.centrifuges = new HashMap<>();
        this.random = new Random();
    }
    
    public void startTasks() {
        mainTask = new BukkitRunnable() {
            @Override
            public void run() {
                processCentrifuges();
            }
        };
        mainTask.runTaskTimer(plugin, 0L, 60L); // Каждые 3 секунды
    }
    
    public void shutdown() {
        if (mainTask != null) {
            mainTask.cancel();
        }
    }
    
    private void processCentrifuges() {
        centrifuges.entrySet().removeIf(entry -> {
            Centrifuge centrifuge = entry.getValue();
            
            if (!centrifuge.isActive()) {
                return true; // Удаляем неактивные
            }
            
            if (centrifuge.isComplete()) {
                completeCentrifuge(centrifuge);
                return true; // Удаляем завершенные
            }
            
            // Выполняем шаг работы
            centrifuge.performStep();
            
            // Визуальные эффекты
            showWorkingEffects(centrifuge);
            
            return false;
        });
    }
    
    private void completeCentrifuge(Centrifuge centrifuge) {
        Location center = centrifuge.getCenter();
        
        // Определяем количество урановой пыли
        int dustAmount = calculateDustAmount();
        
        // Создаем урановую пыль
        ItemStack uraniumDust = createUraniumDust(dustAmount);
        
        // Дропаем в центре
        center.getWorld().dropItem(center.add(0, 1, 0), uraniumDust);
        
        // Эффекты завершения
        center.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, center, 5, 1, 1, 1, 0.1);
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);
    }
    
    private ItemStack createUraniumDust(int amount) {
        ItemStack item = new ItemStack(Material.GUNPOWDER, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aУрановая пыль");
            meta.setCustomModelData(1001);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private int calculateDustAmount() {
        double chance = random.nextDouble();
        if (chance < 0.05) return 4; // 5%
        if (chance < 0.15) return 3; // 10%
        if (chance < 0.40) return 2; // 25%
        return 1; // 60%
    }
    
    private void showWorkingEffects(Centrifuge centrifuge) {
        Location center = centrifuge.getCenter();
        
        // Зеленые частицы вокруг центрифуги
        center.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, 
            center.add(0, 1, 0), 3, 0.5, 0.5, 0.5, 0.1);
        
        // Частицы вокруг котлов
        for (Location cauldron : centrifuge.getCauldrons().values()) {
            cauldron.getWorld().spawnParticle(Particle.WATER_BUBBLE, 
                cauldron.add(0, 1, 0), 2, 0.3, 0.1, 0.3, 0.05);
        }
    }
    
    public void registerCentrifuge(Centrifuge centrifuge) {
        UUID id = UUID.randomUUID();
        centrifuges.put(id, centrifuge);
    }
    
    public boolean hasCentrifugeAt(Location location) {
        return centrifuges.values().stream()
            .anyMatch(c -> c.getCenter().equals(location));
    }
    
    public Map<UUID, Centrifuge> getCentrifuges() {
        return centrifuges;
    }
}
