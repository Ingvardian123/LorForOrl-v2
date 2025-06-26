package com.lorfororl.managers;

import com.lorfororl.LorForOrlPlugin;
import com.lorfororl.reactor.NuclearReactor;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class ReactorManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<String, NuclearReactor> reactors;
    private BukkitRunnable reactorTask;
    
    public ReactorManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.reactors = new HashMap<>();
        startReactorTask();
    }
    
    private void startReactorTask() {
        reactorTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (NuclearReactor reactor : reactors.values()) {
                    reactor.update();
                }
            }
        };
        reactorTask.runTaskTimer(plugin, 0L, 20L); // Каждую секунду
    }
    
    public void createReactor(Location location) {
        String key = locationToString(location);
        reactors.put(key, new NuclearReactor(plugin, location));
    }
    
    public NuclearReactor getReactorAt(Location location) {
        return reactors.get(locationToString(location));
    }
    
    public void removeReactor(Location location) {
        reactors.remove(locationToString(location));
    }
    
    private String locationToString(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }
    
    public void shutdown() {
        if (reactorTask != null) {
            reactorTask.cancel();
        }
    }
}
