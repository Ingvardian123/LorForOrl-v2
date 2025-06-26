package com.lorfororl.reactor;

import com.lorfororl.LorForOrlPlugin;
import com.lorfororl.managers.EnergySource;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;

import java.util.UUID;

public class NuclearReactor implements EnergySource {
    
    private final LorForOrlPlugin plugin;
    private final Location center;
    private final UUID owner;
    private boolean active;
    private int temperature;
    private int fuelLevel;
    private int energyOutput;
    private int maxEnergyOutput;
    private long lastUpdate;
    private boolean overheating;
    
    public NuclearReactor(LorForOrlPlugin plugin, Location center, UUID owner) {
        this.plugin = plugin;
        this.center = center;
        this.owner = owner;
        this.active = false;
        this.temperature = 20; // Комнатная температура
        this.fuelLevel = 0;
        this.energyOutput = 0;
        this.maxEnergyOutput = 1000; // 1000 единиц энергии в тик
        this.lastUpdate = System.currentTimeMillis();
        this.overheating = false;
    }
    
    public boolean validateStructure() {
        // Проверяем структуру реактора 5x5x5
        Location base = center.clone().subtract(2, 2, 2);
        
        // Проверяем основание и верх из обсидиана
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                Block bottomBlock = base.clone().add(x, 0, z).getBlock();
                Block topBlock = base.clone().add(x, 4, z).getBlock();
                
                if (bottomBlock.getType() != Material.OBSIDIAN || 
                    topBlock.getType() != Material.OBSIDIAN) {
                    return false;
                }
            }
        }
        
        // Проверяем стены из железных блоков
        for (int y = 1; y < 4; y++) {
            // Передняя и задняя стены
            for (int x = 0; x < 5; x++) {
                Block frontBlock = base.clone().add(x, y, 0).getBlock();
                Block backBlock = base.clone().add(x, y, 4).getBlock();
                
                if (frontBlock.getType() != Material.IRON_BLOCK || 
                    backBlock.getType() != Material.IRON_BLOCK) {
                    return false;
                }
            }
            
            // Левая и правая стены
            for (int z = 1; z < 4; z++) {
                Block leftBlock = base.clone().add(0, y, z).getBlock();
                Block rightBlock = base.clone().add(4, y, z).getBlock();
                
                if (leftBlock.getType() != Material.IRON_BLOCK || 
                    rightBlock.getType() != Material.IRON_BLOCK) {
                    return false;
                }
            }
        }
        
        // Проверяем центральный маяк
        if (center.getBlock().getType() != Material.BEACON) {
            return false;
        }
        
        return true;
    }
    
    public void update() {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdate;
        lastUpdate = currentTime;
        
        if (!active) {
            // Реактор выключен - охлаждение
            if (temperature > 20) {
                temperature = Math.max(20, temperature - 2);
            }
            energyOutput = 0;
            overheating = false;
            return;
        }
        
        // Проверяем топливо
        if (fuelLevel <= 0) {
            active = false;
            return;
        }
        
        // Потребляем топливо
        if (deltaTime >= 1000) { // Каждую секунду
            fuelLevel = Math.max(0, fuelLevel - 1);
        }
        
        // Нагрев реактора
        if (active) {
            temperature += 5;
            
            // Проверяем перегрев
            if (temperature > 900) {
                overheating = true;
                
                if (temperature > 1000) {
                    // Критический перегрев - взрыв
                    meltdown();
                    return;
                }
            } else {
                overheating = false;
            }
        }
        
        // Рассчитываем выработку энергии
        if (temperature > 100) {
            double efficiency = Math.min(1.0, (temperature - 100) / 400.0);
            energyOutput = (int) (maxEnergyOutput * efficiency);
            
            // Штраф за перегрев
            if (overheating) {
                energyOutput = (int) (energyOutput * 0.5);
            }
        } else {
            energyOutput = 0;
        }
        
        // Визуальные эффекты
        showReactorEffects();
    }
    
    private void showReactorEffects() {
        if (active) {
            // Эффекты работающего реактора
            center.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, 
                center.clone().add(0, 3, 0), 10, 1, 1, 1, 0.1);
            
            if (overheating) {
                // Эффекты перегрева
                center.getWorld().spawnParticle(Particle.LAVA, 
                    center.clone().add(0, 2, 0), 20, 2, 2, 2, 0.1);
                center.getWorld().playSound(center, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 0.5f);
            }
        }
    }
    
    private void meltdown() {
        // Катастрофический взрыв реактора
        active = false;
        
        // Большой взрыв
        center.getWorld().createExplosion(center, 15.0f, true, true);
        
        // Создаем радиационную зону
        plugin.getRadiationZoneManager().createRadiationZone(
            center, 
            50, // Радиус 50 блоков
            15.0, // Максимальная радиация
            3600000L, // 1 час
            com.lorfororl.radiation.RadiationZone.RadiationZoneType.REACTOR_MELTDOWN
        );
        
        // Уведомляем всех игроков
        for (org.bukkit.entity.Player player : center.getWorld().getPlayers()) {
            if (player.getLocation().distance(center) <= 100) {
                player.sendTitle("§4§l⚠ АВАРИЯ РЕАКТОРА ⚠", 
                    "§cПроизошло расплавление активной зоны!", 
                    20, 100, 20);
            }
        }
        
        // Логирование
        plugin.getLogger().severe("Nuclear reactor meltdown at " + 
            center.getX() + "," + center.getY() + "," + center.getZ());
    }
    
    public boolean start() {
        if (!validateStructure()) {
            return false;
        }
        
        if (fuelLevel <= 0) {
            return false;
        }
        
        active = true;
        return true;
    }
    
    public void stop() {
        active = false;
    }
    
    public boolean addFuel(int amount) {
        if (fuelLevel + amount > 1000) { // Максимум 1000 единиц топлива
            return false;
        }
        
        fuelLevel += amount;
        return true;
    }
    
    // Геттеры
    public Location getCenter() { return center; }
    public UUID getOwner() { return owner; }
    public boolean isActive() { return active; }
    public int getTemperature() { return temperature; }
    public int getFuelLevel() { return fuelLevel; }
    public int getEnergyOutput() { return energyOutput; }
    public int getMaxEnergyOutput() { return maxEnergyOutput; }
    public boolean isOverheating() { return overheating; }
    
    // Реализация EnergySource
    @Override
    public int generateEnergy() {
        return energyOutput;
    }
    
    @Override
    public boolean isOperational() {
        return active && !overheating;
    }
    
    @Override
    public Location getLocation() {
        return center;
    }
}
