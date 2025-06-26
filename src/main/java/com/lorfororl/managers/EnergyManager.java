package com.lorfororl.managers;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class EnergyManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, Integer> playerEnergy;
    private final Map<UUID, Integer> playerMaxEnergy;
    private final List<EnergySource> energySources;
    private File energyFile;
    private FileConfiguration energyConfig;
    private BukkitRunnable energyTask;
    
    public EnergyManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.playerEnergy = new HashMap<>();
        this.playerMaxEnergy = new HashMap<>();
        this.energySources = new ArrayList<>();
        setupEnergyFile();
        loadEnergyData();
    }
    
    public void startTasks() {
        energyTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateEnergyGeneration();
                updatePlayerEnergy();
            }
        };
        energyTask.runTaskTimer(plugin, 0L, 20L); // Каждую секунду
    }
    
    public void shutdown() {
        if (energyTask != null) {
            energyTask.cancel();
        }
        saveEnergyData();
        playerEnergy.clear();
        playerMaxEnergy.clear();
        energySources.clear();
    }
    
    private void setupEnergyFile() {
        energyFile = new File(plugin.getDataFolder(), "energy.yml");
        if (!energyFile.exists()) {
            try {
                energyFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create energy.yml file!");
                e.printStackTrace();
            }
        }
        energyConfig = YamlConfiguration.loadConfiguration(energyFile);
    }
    
    public void loadEnergyData() {
        for (String key : energyConfig.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(key);
                int energy = energyConfig.getInt(key + ".energy", 1000);
                int maxEnergy = energyConfig.getInt(key + ".maxEnergy", 10000);
                
                playerEnergy.put(playerId, energy);
                playerMaxEnergy.put(playerId, maxEnergy);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in energy.yml: " + key);
            }
        }
    }
    
    public void saveEnergyData() {
        for (Map.Entry<UUID, Integer> entry : playerEnergy.entrySet()) {
            String key = entry.getKey().toString();
            energyConfig.set(key + ".energy", entry.getValue());
            energyConfig.set(key + ".maxEnergy", playerMaxEnergy.getOrDefault(entry.getKey(), 10000));
        }
        
        try {
            energyConfig.save(energyFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save energy.yml file!");
            e.printStackTrace();
        }
    }
    
    private void updateEnergyGeneration() {
        // Обновляем все источники энергии
        for (EnergySource source : energySources) {
            if (source.isOperational()) {
                int generated = source.generateEnergy();
                // Здесь можно добавить логику распределения энергии по сети
                distributeEnergy(source.getLocation(), generated);
            }
        }
    }
    
    private void updatePlayerEnergy() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            // Естественная регенерация энергии
            int currentEnergy = getPlayerEnergy(player);
            int maxEnergy = getPlayerMaxEnergy(player);
            
            if (currentEnergy < maxEnergy) {
                // Медленная регенерация 1 энергии в секунду
                addPlayerEnergy(player, 1);
            }
            
            // Проверяем критически низкую энергию
            if (currentEnergy < maxEnergy * 0.1) {
                plugin.getNotificationManager().sendEnergyWarning(player, currentEnergy);
            }
        }
    }
    
    private void distributeEnergy(org.bukkit.Location sourceLocation, int amount) {
        // Простое распределение - даем энергию ближайшим игрокам
        for (Player player : sourceLocation.getWorld().getPlayers()) {
            if (player.getLocation().distance(sourceLocation) <= 50) { // Радиус 50 блоков
                addPlayerEnergy(player, amount / 10); // Делим на количество игроков
            }
        }
    }
    
    public int getPlayerEnergy(Player player) {
        return playerEnergy.getOrDefault(player.getUniqueId(), 1000);
    }
    
    public void setPlayerEnergy(Player player, int amount) {
        int maxEnergy = getPlayerMaxEnergy(player);
        playerEnergy.put(player.getUniqueId(), Math.max(0, Math.min(maxEnergy, amount)));
    }
    
    public void addPlayerEnergy(Player player, int amount) {
        int currentEnergy = getPlayerEnergy(player);
        setPlayerEnergy(player, currentEnergy + amount);
    }
    
    public boolean consumePlayerEnergy(Player player, int amount) {
        int currentEnergy = getPlayerEnergy(player);
        if (currentEnergy >= amount) {
            setPlayerEnergy(player, currentEnergy - amount);
            return true;
        }
        return false;
    }
    
    public boolean hasEnoughEnergy(Player player, int amount) {
        return getPlayerEnergy(player) >= amount;
    }
    
    public int getPlayerMaxEnergy(Player player) {
        return playerMaxEnergy.getOrDefault(player.getUniqueId(), 10000);
    }
    
    public void setPlayerMaxEnergy(Player player, int maxEnergy) {
        playerMaxEnergy.put(player.getUniqueId(), Math.max(1000, maxEnergy));
        
        // Убеждаемся, что текущая энергия не превышает максимум
        int currentEnergy = getPlayerEnergy(player);
        if (currentEnergy > maxEnergy) {
            setPlayerEnergy(player, maxEnergy);
        }
    }
    
    public void rechargePlayerEnergy(Player player, int amount) {
        addPlayerEnergy(player, amount);
    }
    
    public double getPlayerEnergyPercentage(Player player) {
        int current = getPlayerEnergy(player);
        int max = getPlayerMaxEnergy(player);
        return max > 0 ? (double) current / max * 100.0 : 0.0;
    }
    
    public void addEnergySource(EnergySource source) {
        energySources.add(source);
    }
    
    public void removeEnergySource(EnergySource source) {
        energySources.remove(source);
    }
    
    public List<EnergySource> getEnergySourcesNear(org.bukkit.Location location, double radius) {
        List<EnergySource> nearSources = new ArrayList<>();
        for (EnergySource source : energySources) {
            if (source.getLocation().getWorld().equals(location.getWorld()) &&
                source.getLocation().distance(location) <= radius) {
                nearSources.add(source);
            }
        }
        return nearSources;
    }
    
    public int getTotalEnergyGeneration() {
        int total = 0;
        for (EnergySource source : energySources) {
            if (source.isOperational()) {
                total += source.generateEnergy();
            }
        }
        return total;
    }
    
    public String getEnergyReport(Player player) {
        int current = getPlayerEnergy(player);
        int max = getPlayerMaxEnergy(player);
        double percentage = getPlayerEnergyPercentage(player);
        
        StringBuilder report = new StringBuilder();
        report.append("§6=== ЭНЕРГЕТИЧЕСКИЙ ОТЧЕТ ===\n");
        report.append(String.format("§fТекущая энергия: §a%d§7/§e%d §7(%.1f%%)\n", current, max, percentage));
        
        // Ближайшие источники энергии
        List<EnergySource> nearSources = getEnergySourcesNear(player.getLocation(), 100);
        if (!nearSources.isEmpty()) {
            report.append("§fБлижайшие источники энергии:\n");
            for (EnergySource source : nearSources) {
                double distance = player.getLocation().distance(source.getLocation());
                report.append(String.format("§7- %s: §a%d ед/с §7(%.1fm)\n", 
                    source.getEnergySourceType(), source.generateEnergy(), distance));
            }
        } else {
            report.append("§cНет источников энергии поблизости\n");
        }
        
        return report.toString();
    }
    
    // Методы для совместимости с другими классами
    public int getEnergy(Player player) {
        return getPlayerEnergy(player);
    }
    
    public void setEnergy(Player player, int amount) {
        setPlayerEnergy(player, amount);
    }
    
    public void addEnergy(Player player, int amount) {
        addPlayerEnergy(player, amount);
    }
    
    public boolean consumeEnergy(Player player, int amount) {
        return consumePlayerEnergy(player, amount);
    }
    
    public void rechargeEnergy(Player player, int amount) {
        rechargePlayerEnergy(player, amount);
    }
    
    public int getMaxEnergy(Player player) {
        return getPlayerMaxEnergy(player);
    }
    
    public double getEnergyPercentage(Player player) {
        return getPlayerEnergyPercentage(player);
    }
}
