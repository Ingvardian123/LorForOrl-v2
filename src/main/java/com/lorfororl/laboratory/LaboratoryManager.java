package com.lorfororl.laboratory;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class LaboratoryManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, Laboratory> laboratories;
    private final Set<UUID> authorizedPlayers;
    private BukkitRunnable effectsTask;
    
    public LaboratoryManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.laboratories = new HashMap<>();
        this.authorizedPlayers = new HashSet<>();
    }
    
    public void startTasks() {
        effectsTask = new BukkitRunnable() {
            @Override
            public void run() {
                showLaboratoryEffects();
            }
        };
        effectsTask.runTaskTimer(plugin, 0L, 40L); // Каждые 2 секунды
    }
    
    public void shutdown() {
        if (effectsTask != null) {
            effectsTask.cancel();
        }
    }
    
    private void showLaboratoryEffects() {
        for (Laboratory lab : laboratories.values()) {
            if (lab.isActive()) {
                Location center = lab.getCenter();
                
                // Эффекты работающей лаборатории
                center.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, 
                    center.add(0, 2, 0), 10, 1, 1, 1, 0.1);
                
                // Если идет исследование
                if (lab.getCurrentResearch() != null) {
                    center.getWorld().spawnParticle(Particle.PORTAL, 
                        center.add(0, 1.5, 0), 5, 0.5, 0.5, 0.5, 0.05);
                }
            }
        }
    }
    
    public boolean createLaboratory(Player player, Location location) {
        if (!isAuthorized(player)) {
            return false;
        }
        
        Laboratory lab = new Laboratory(location, player.getUniqueId());
        if (!lab.validateStructure()) {
            return false;
        }
        
        lab.activate();
        laboratories.put(UUID.randomUUID(), lab);
        return true;
    }
    
    public Laboratory getLaboratoryAt(Location location) {
        for (Laboratory lab : laboratories.values()) {
            if (lab.getCenter().distance(location) <= 2) {
                return lab;
            }
        }
        return null;
    }
    
    public boolean isAuthorized(Player player) {
        return authorizedPlayers.contains(player.getUniqueId()) || player.hasPermission("lorfororl.admin");
    }
    
    public void authorizePlayer(UUID playerId) {
        authorizedPlayers.add(playerId);
    }
    
    public void unauthorizePlayer(UUID playerId) {
        authorizedPlayers.remove(playerId);
    }
    
    public Set<UUID> getAuthorizedPlayers() {
        return new HashSet<>(authorizedPlayers);
    }
    
    public Collection<Laboratory> getLaboratories() {
        return laboratories.values();
    }
}
