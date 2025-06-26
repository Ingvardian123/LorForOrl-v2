package com.lorfororl.energy;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.UUID;

public class SolarPanel implements EnergySource {
    
    private final Location location;
    private final UUID owner;
    private int storedEnergy;
    private final int maxStorage;
    private int dailyProduction;
    private int totalProduction;
    private double efficiency;
    private long lastCleanTime;
    
    public SolarPanel(Location location, UUID owner) {
        this.location = location;
        this.owner = owner;
        this.storedEnergy = 0;
        this.maxStorage = 1000;
        this.dailyProduction = 0;
        this.totalProduction = 0;
        this.efficiency = 1.0;
        this.lastCleanTime = System.currentTimeMillis();
    }
    
    public void update() {
        // Проверяем условия для выработки энергии
        if (canGenerateEnergy()) {
            int generated = calculateEnergyGeneration();
            storedEnergy = Math.min(maxStorage, storedEnergy + generated);
            dailyProduction += generated;
            totalProduction += generated;
        }
        
        // Снижаем эффективность со временем (загрязнение)
        long timeSinceClean = System.currentTimeMillis() - lastCleanTime;
        long daysWithoutCleaning = timeSinceClean / (1000 * 60 * 60 * 24);
        efficiency = Math.max(0.5, 1.0 - (daysWithoutCleaning * 0.1));
    }
    
    private boolean canGenerateEnergy() {
        World world = location.getWorld();
        if (world == null) return false;
        
        // Проверяем время суток (день)
        long time = world.getTime();
        boolean isDaytime = time < 12300 || time > 23850;
        
        // Проверяем прямой доступ к солнцу
        boolean hasDirectSunlight = world.getHighestBlockYAt(location) <= location.getBlockY();
        
        // Проверяем погоду
        boolean isClear = !world.hasStorm();
        
        return isDaytime && hasDirectSunlight && isClear;
    }
    
    private int calculateEnergyGeneration() {
        // Базовая выработка 25 ед/тик в идеальных условиях
        double baseGeneration = 25.0;
        
        // Применяем эффективность (загрязнение)
        baseGeneration *= efficiency;
        
        // Учитываем время суток (максимум в полдень)
        World world = location.getWorld();
        if (world != null) {
            long time = world.getTime();
            double timeMultiplier = 1.0;
            
            if (time >= 0 && time <= 6000) {
                // Утро - нарастающая эффективность
                timeMultiplier = 0.3 + (time / 6000.0) * 0.7;
            } else if (time >= 6000 && time <= 12000) {
                // День - максимальная эффективность
                timeMultiplier = 1.0;
            } else if (time >= 12000 && time <= 18000) {
                // Вечер - убывающая эффективность
                timeMultiplier = 1.0 - ((time - 12000) / 6000.0) * 0.7;
            } else {
                // Ночь - нет выработки
                timeMultiplier = 0.0;
            }
            
            baseGeneration *= timeMultiplier;
        }
        
        return (int) Math.round(baseGeneration);
    }
    
    public boolean validateStructure() {
        // Проверяем структуру солнечной панели 3x3
        Location base = location.clone().subtract(1, 0, 1);
        
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                Location panelLoc = base.clone().add(x, 0, z);
                if (panelLoc.getBlock().getType() != Material.DAYLIGHT_DETECTOR) {
                    return false;
                }
            }
        }
        
        // Проверяем основание из медных блоков
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                Location baseLoc = base.clone().add(x, -1, z);
                if (baseLoc.getBlock().getType() != Material.COPPER_BLOCK) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public void clean() {
        lastCleanTime = System.currentTimeMillis();
        efficiency = 1.0;
    }
    
    public int extractEnergy(int amount) {
        int extracted = Math.min(amount, storedEnergy);
        storedEnergy -= extracted;
        return extracted;
    }
    
    public void resetDailyProduction() {
        dailyProduction = 0;
    }
    
    // Реализация EnergySource
    @Override
    public int getEnergyOutput() {
        return canGenerateEnergy() ? calculateEnergyGeneration() : 0;
    }
    
    @Override
    public int getMaxEnergyOutput() {
        return 25;
    }
    
    @Override
    public void consumeEnergy(int amount) {
        // Солнечная панель не потребляет энергию
    }
    
    @Override
    public boolean isActive() {
        return canGenerateEnergy();
    }
    
    @Override
    public void setActive(boolean active) {
        // Солнечная панель всегда активна при наличии солнца
    }
    
    // Геттеры
    public Location getLocation() { return location; }
    public UUID getOwner() { return owner; }
    public int getStoredEnergy() { return storedEnergy; }
    public int getMaxStorage() { return maxStorage; }
    public int getDailyProduction() { return dailyProduction; }
    public int getTotalProduction() { return totalProduction; }
    public double getEfficiency() { return efficiency; }
}
