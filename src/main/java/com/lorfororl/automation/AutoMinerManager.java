package com.lorfororl.automation;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AutoMinerManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<Location, AutoMiner> activeMiners;
    private final Map<UUID, Set<Location>> playerMiners;
    
    public AutoMinerManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.activeMiners = new ConcurrentHashMap<>();
        this.playerMiners = new HashMap<>();
    }
    
    public boolean createAutoMiner(Player player, Location location) {
        if (activeMiners.containsKey(location)) {
            player.sendMessage("§c❌ В этом месте уже установлен автошахтер!");
            return false;
        }
        
        // Проверяем лимит автошахтеров на игрока
        Set<Location> playerMinerSet = playerMiners.getOrDefault(player.getUniqueId(), new HashSet<>());
        if (playerMinerSet.size() >= getMaxMinersForPlayer(player)) {
            player.sendMessage("§c❌ Достигнут лимит автошахтеров! Максимум: " + getMaxMinersForPlayer(player));
            return false;
        }
        
        // Создаем автошахтер
        AutoMiner miner = new AutoMiner(plugin, location, player.getUniqueId());
        activeMiners.put(location, miner);
        
        playerMinerSet.add(location);
        playerMiners.put(player.getUniqueId(), playerMinerSet);
        
        // Регистрируем как потребителя энергии
        plugin.getEnergyManager().addEnergyConsumer(location, miner);
        
        player.sendMessage("§a✅ Автошахтер установлен!");
        return true;
    }
    
    public boolean removeAutoMiner(Player player, Location location) {
        AutoMiner miner = activeMiners.get(location);
        if (miner == null) {
            player.sendMessage("§c❌ Автошахтер не найден!");
            return false;
        }
        
        if (!miner.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("lorfororl.admin")) {
            player.sendMessage("§c❌ Это не ваш автошахтер!");
            return false;
        }
        
        // Останавливаем автошахтер
        miner.stop();
        
        // Удаляем из списков
        activeMiners.remove(location);
        Set<Location> playerMinerSet = playerMiners.get(player.getUniqueId());
        if (playerMinerSet != null) {
            playerMinerSet.remove(location);
        }
        
        // Убираем из энергосистемы
        plugin.getEnergyManager().removeEnergyConsumer(location);
        
        player.sendMessage("§a✅ Автошахтер демонтирован!");
        return true;
    }
    
    public AutoMiner getAutoMiner(Location location) {
        return activeMiners.get(location);
    }
    
    public Set<Location> getPlayerMiners(UUID playerId) {
        return playerMiners.getOrDefault(playerId, new HashSet<>());
    }
    
    public Collection<AutoMiner> getAllMiners() {
        return activeMiners.values();
    }
    
    private int getMaxMinersForPlayer(Player player) {
        if (player.hasPermission("lorfororl.miners.unlimited")) {
            return Integer.MAX_VALUE;
        } else if (player.hasPermission("lorfororl.miners.10")) {
            return 10;
        } else if (player.hasPermission("lorfororl.miners.5")) {
            return 5;
        } else {
            return 3; // По умолчанию
        }
    }
    
    public void startTasks() {
        // Задачи уже запускаются в отдельных автошахтерах
    }
    
    public void shutdown() {
        for (AutoMiner miner : activeMiners.values()) {
            miner.stop();
        }
        activeMiners.clear();
        playerMiners.clear();
    }
}
