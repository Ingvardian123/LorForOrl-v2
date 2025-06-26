package com.lorfororl.centrifuge;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;

import java.util.HashMap;
import java.util.Map;

public class Centrifuge {
    
    private final Map<String, Location> cauldrons;
    private final Location center;
    private boolean active;
    private long startTime;
    private int currentStep;
    
    // Время работы в тиках (1300 минут = 78000 секунд = 1560000 тиков)
    private static final long WORK_DURATION = 1560000L;
    private static final long STEP_DURATION = 60L; // 3 секунды в тиках
    
    public Centrifuge(Location center) {
        this.center = center;
        this.cauldrons = new HashMap<>();
        this.active = false;
        this.currentStep = 0;
    }
    
    public void setCauldronPositions(Location a2, Location b1, Location b3, Location c2) {
        cauldrons.put("A2", a2);
        cauldrons.put("B1", b1);
        cauldrons.put("B3", b3);
        cauldrons.put("C2", c2);
    }
    
    public boolean validateStructure() {
        for (Location loc : cauldrons.values()) {
            Block block = loc.getBlock();
            if (block.getType() != Material.WATER_CAULDRON) {
                return false;
            }
            
            // Проверяем уровень воды
            Levelled cauldron = (Levelled) block.getBlockData();
            if (cauldron.getLevel() < 1) {
                return false;
            }
        }
        return true;
    }
    
    public void activate() {
        if (validateStructure()) {
            this.active = true;
            this.startTime = System.currentTimeMillis();
            this.currentStep = 0;
        }
    }
    
    public void deactivate() {
        this.active = false;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public boolean isComplete() {
        if (!active) return false;
        return (System.currentTimeMillis() - startTime) >= (WORK_DURATION * 50); // Конвертируем тики в миллисекунды
    }
    
    public void performStep() {
        if (!active || !validateStructure()) {
            deactivate();
            return;
        }
        
        // Логика движения поршней (визуальные эффекты)
        currentStep = (currentStep + 1) % 4;
        
        // Звуковые эффекты
        center.getWorld().playSound(center, "lorfororl.centrifuge_loop", 1.0f, 1.0f);
    }
    
    public Location getCenter() {
        return center;
    }
    
    public Map<String, Location> getCauldrons() {
        return cauldrons;
    }
    
    public long getWorkTime() {
        if (!active) return 0;
        return System.currentTimeMillis() - startTime;
    }
    
    public double getProgress() {
        if (!active) return 0.0;
        long elapsed = System.currentTimeMillis() - startTime;
        long total = WORK_DURATION * 50;
        return Math.min(1.0, (double) elapsed / total);
    }
}
