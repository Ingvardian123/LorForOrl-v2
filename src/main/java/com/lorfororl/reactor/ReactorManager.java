package com.lorfororl.reactor;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReactorManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, NuclearReactor> reactors;
    private BukkitRunnable updateTask;
    private BukkitRunnable effectsTask;
    
    public ReactorManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.reactors = new HashMap<>();
    }
    
    public void startTasks() {
        // Задача обновления реакторов каждые 10 секунд
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateReactors();
            }
        };
        updateTask.runTaskTimer(plugin, 0L, 200L); // 10 секунд
        
        // Задача визуальных эффектов каждые 2 секунды
        effectsTask = new BukkitRunnable() {
            @Override
            public void run() {
                showReactorEffects();
            }
        };
        effectsTask.runTaskTimer(plugin, 0L, 40L); // 2 секунды
    }
    
    public void shutdown() {
        if (updateTask != null) updateTask.cancel();
        if (effectsTask != null) effectsTask.cancel();
    }
    
    private void updateReactors() {
        for (NuclearReactor reactor : reactors.values()) {
            reactor.update();
            
            // Регистрируем как источник энергии
            plugin.getEnergyManager().addEnergySource(reactor.getCenter(), reactor);
        }
    }
    
    private void showReactorEffects() {
        for (NuclearReactor reactor : reactors.values()) {
            Location center = reactor.getCenter();
            
            if (reactor.isActive()) {
                // Эффекты работающего реактора
                center.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, 
                    center.add(0, 3, 0), 10, 1, 1, 1, 0.1);
                
                // Звук работы
                center.getWorld().playSound(center, Sound.BLOCK_BEACON_AMBIENT, 0.5f, 0.8f);
                
                if (reactor.isOverheating()) {
                    // Эффекты перегрева
                    center.getWorld().spawnParticle(Particle.LAVA, 
                        center.add(0, 2, 0), 20, 2, 2, 2, 0.1);
                    center.getWorld().playSound(center, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 0.5f);
                }
            }
        }
    }
    
    public boolean createReactor(Player player, Location location) {
        NuclearReactor reactor = new NuclearReactor(location, player.getUniqueId());
        
        if (!reactor.validateStructure()) {
            return false;
        }
        
        UUID reactorId = UUID.randomUUID();
        reactors.put(reactorId, reactor);
        
        return true;
    }
    
    public NuclearReactor getReactorAt(Location location) {
        for (NuclearReactor reactor : reactors.values()) {
            if (reactor.getCenter().distance(location) <= 3) {
                return reactor;
            }
        }
        return null;
    }
    
    public Map<UUID, NuclearReactor> getReactors() {
        return reactors;
    }
}
