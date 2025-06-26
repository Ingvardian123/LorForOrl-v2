package com.lorfororl.energy;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnergyManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, Integer> playerEnergy; // Энергия игроков (для силовой брони)
    private final Map<Location, EnergySource> energySources; // Источники энергии (реакторы)
    private final Map<Location, EnergyConsumer> energyConsumers; // Потребители энергии
    
    public EnergyManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.playerEnergy = new HashMap<>();
        this.energySources = new HashMap<>();
        this.energyConsumers = new HashMap<>();
    }
    
    public void addEnergySource(Location location, EnergySource source) {
        energySources.put(location, source);
    }
    
    public void removeEnergySource(Location location) {
        energySources.remove(location);
    }
    
    public void addEnergyConsumer(Location location, EnergyConsumer consumer) {
        energyConsumers.put(location, consumer);
    }
    
    public void removeEnergyConsumer(Location location) {
        energyConsumers.remove(location);
    }
    
    public int getPlayerEnergy(Player player) {
        return playerEnergy.getOrDefault(player.getUniqueId(), 0);
    }
    
    public void setPlayerEnergy(Player player, int energy) {
        playerEnergy.put(player.getUniqueId(), Math.max(0, energy));
    }
    
    public void addPlayerEnergy(Player player, int energy) {
        int current = getPlayerEnergy(player);
        setPlayerEnergy(player, current + energy);
    }
    
    public boolean consumePlayerEnergy(Player player, int energy) {
        int current = getPlayerEnergy(player);
        if (current >= energy) {
            setPlayerEnergy(player, current - energy);
            return true;
        }
        return false;
    }
    
    public int getAvailableEnergyAt(Location location, int radius) {
        int totalEnergy = 0;
        
        for (Map.Entry<Location, EnergySource> entry : energySources.entrySet()) {
            if (entry.getKey().distance(location) <= radius) {
                totalEnergy += entry.getValue().getEnergyOutput();
            }
        }
        
        return totalEnergy;
    }
    
    public boolean distributeEnergy() {
        // Распределяем энергию от источников к потребителям
        for (Map.Entry<Location, EnergyConsumer> consumerEntry : energyConsumers.entrySet()) {
            Location consumerLoc = consumerEntry.getKey();
            EnergyConsumer consumer = consumerEntry.getValue();
            
            int requiredEnergy = consumer.getEnergyRequired();
            int availableEnergy = getAvailableEnergyAt(consumerLoc, 50); // Радиус 50 блоков
            
            if (availableEnergy >= requiredEnergy) {
                consumer.receiveEnergy(requiredEnergy);
                // Уменьшаем энергию источников
                consumeEnergyFromSources(consumerLoc, requiredEnergy, 50);
            } else {
                consumer.receiveEnergy(availableEnergy);
                consumeEnergyFromSources(consumerLoc, availableEnergy, 50);
            }
        }
        
        return true;
    }
    
    private void consumeEnergyFromSources(Location location, int energy, int radius) {
        int remaining = energy;
        
        for (Map.Entry<Location, EnergySource> entry : energySources.entrySet()) {
            if (remaining <= 0) break;
            
            if (entry.getKey().distance(location) <= radius) {
                EnergySource source = entry.getValue();
                int consumed = Math.min(remaining, source.getEnergyOutput());
                source.consumeEnergy(consumed);
                remaining -= consumed;
            }
        }
    }
    
    public Map<Location, EnergySource> getEnergySources() {
        return energySources;
    }
    
    public Map<Location, EnergyConsumer> getEnergyConsumers() {
        return energyConsumers;
    }
}
