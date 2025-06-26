package com.lorfororl.security;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SecurityManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, PlayerLimits> playerLimits;
    private final Map<UUID, Long> lastBuildTime;
    private final Map<UUID, Integer> actionsPerMinute;
    private final Map<UUID, Long> lastActionTime;
    
    public SecurityManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.playerLimits = new HashMap<>();
        this.lastBuildTime = new HashMap<>();
        this.actionsPerMinute = new HashMap<>();
        this.lastActionTime = new HashMap<>();
    }
    
    public boolean canBuild(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Проверяем кулдаун между постройками
        if (lastBuildTime.containsKey(playerId)) {
            long timeSinceLastBuild = currentTime - lastBuildTime.get(playerId);
            long cooldown = plugin.getConfigManager().getBuildCooldown();
            
            if (timeSinceLastBuild < cooldown) {
                long remaining = (cooldown - timeSinceLastBuild) / 1000;
                player.sendMessage("§cПодождите " + remaining + " секунд перед следующей постройкой!");
                return false;
            }
        }
        
        return true;
    }
    
    public void recordBuild(Player player) {
        lastBuildTime.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    public boolean canPerformAction(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Сбрасываем счетчик каждую минуту
        if (lastActionTime.containsKey(playerId)) {
            long timeSinceLastAction = currentTime - lastActionTime.get(playerId);
            if (timeSinceLastAction > 60000) { // 1 минута
                actionsPerMinute.put(playerId, 0);
            }
        }
        
        int actions = actionsPerMinute.getOrDefault(playerId, 0);
        if (actions >= 60) { // Максимум 60 действий в минуту
            player.sendMessage("§cСлишком много действий! Подождите немного.");
            return false;
        }
        
        actionsPerMinute.put(playerId, actions + 1);
        lastActionTime.put(playerId, currentTime);
        return true;
    }
    
    public boolean canCreateCentrifuge(Player player) {
        PlayerLimits limits = getPlayerLimits(player);
        int maxCentrifuges = plugin.getConfigManager().getMaxCentrifugesPerPlayer();
        
        if (limits.centrifuges >= maxCentrifuges) {
            player.sendMessage("§cВы достигли лимита центрифуг (" + maxCentrifuges + ")!");
            return false;
        }
        
        return true;
    }
    
    public boolean canCreateReactor(Player player) {
        PlayerLimits limits = getPlayerLimits(player);
        int maxReactors = plugin.getConfigManager().getMaxReactorsPerPlayer();
        
        if (limits.reactors >= maxReactors) {
            player.sendMessage("§cВы достигли лимита реакторов (" + maxReactors + ")!");
            return false;
        }
        
        return true;
    }
    
    public boolean canCreateLaboratory(Player player) {
        PlayerLimits limits = getPlayerLimits(player);
        int maxLaboratories = plugin.getConfigManager().getMaxLaboratoriesPerPlayer();
        
        if (limits.laboratories >= maxLaboratories) {
            player.sendMessage("§cВы достигли лимита лабораторий (" + maxLaboratories + ")!");
            return false;
        }
        
        return true;
    }
    
    public boolean canAccessStructure(Player player, UUID ownerId) {
        if (!plugin.getConfigManager().isOwnerOnlyAccess()) {
            return true;
        }
        
        if (player.getUniqueId().equals(ownerId)) {
            return true;
        }
        
        if (player.hasPermission("lorfororl.admin")) {
            return true;
        }
        
        player.sendMessage("§cЭта структура принадлежит другому игроку!");
        return false;
    }
    
    public void incrementStructureCount(Player player, StructureType type) {
        PlayerLimits limits = getPlayerLimits(player);
        
        switch (type) {
            case CENTRIFUGE:
                limits.centrifuges++;
                break;
            case REACTOR:
                limits.reactors++;
                break;
            case LABORATORY:
                limits.laboratories++;
                break;
        }
    }
    
    public void decrementStructureCount(Player player, StructureType type) {
        PlayerLimits limits = getPlayerLimits(player);
        
        switch (type) {
            case CENTRIFUGE:
                limits.centrifuges = Math.max(0, limits.centrifuges - 1);
                break;
            case REACTOR:
                limits.reactors = Math.max(0, limits.reactors - 1);
                break;
            case LABORATORY:
                limits.laboratories = Math.max(0, limits.laboratories - 1);
                break;
        }
    }
    
    private PlayerLimits getPlayerLimits(Player player) {
        return playerLimits.computeIfAbsent(player.getUniqueId(), k -> new PlayerLimits());
    }
    
    public enum StructureType {
        CENTRIFUGE, REACTOR, LABORATORY
    }
    
    private static class PlayerLimits {
        int centrifuges = 0;
        int reactors = 0;
        int laboratories = 0;
    }
}
