package com.lorfororl.energy;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SolarPanelManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, SolarPanel> solarPanels;
    private BukkitRunnable updateTask;
    private BukkitRunnable effectsTask;
    
    public SolarPanelManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.solarPanels = new HashMap<>();
    }
    
    public void startTasks() {
        // Задача обновления панелей каждые 5 секунд
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateSolarPanels();
            }
        };
        updateTask.runTaskTimer(plugin, 0L, 100L); // 5 секунд
        
        // Задача визуальных эффектов каждые 3 секунды
        effectsTask = new BukkitRunnable() {
            @Override
            public void run() {
                showSolarPanelEffects();
            }
        };
        effectsTask.runTaskTimer(plugin, 0L, 60L); // 3 секунды
    }
    
    public void shutdown() {
        if (updateTask != null) updateTask.cancel();
        if (effectsTask != null) effectsTask.cancel();
    }
    
    private void updateSolarPanels() {
        for (SolarPanel panel : solarPanels.values()) {
            panel.update();
            
            // Регистрируем как источник энергии
            plugin.getEnergyManager().addEnergySource(panel.getLocation(), panel);
        }
    }
    
    private void showSolarPanelEffects() {
        for (SolarPanel panel : solarPanels.values()) {
            Location center = panel.getLocation();
            
            if (panel.isActive()) {
                // Эффекты работающей панели
                center.getWorld().spawnParticle(Particle.END_ROD, 
                    center.add(0, 1, 0), 3, 0.5, 0.5, 0.5, 0.02);
                
                // Дополнительные эффекты при высокой эффективности
                if (panel.getEfficiency() > 0.8) {
                    center.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, 
                        center.add(0, 0.5, 0), 2, 0.3, 0.3, 0.3, 0.01);
                }
            }
        }
    }
    
    public boolean createSolarPanel(Player player, Location location) {
        SolarPanel panel = new SolarPanel(location, player.getUniqueId());
        
        if (!panel.validateStructure()) {
            return false;
        }
        
        UUID panelId = UUID.randomUUID();
        solarPanels.put(panelId, panel);
        
        return true;
    }
    
    public SolarPanel getSolarPanelAt(Location location) {
        for (SolarPanel panel : solarPanels.values()) {
            if (panel.getLocation().distance(location) <= 2) {
                return panel;
            }
        }
        return null;
    }
    
    public Map<UUID, SolarPanel> getSolarPanels() {
        return solarPanels;
    }
}
