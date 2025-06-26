package com.lorfororl.visual;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EffectManager {
    
    private final LorForOrlPlugin plugin;
    
    public EffectManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void playStructureActivationEffect(Location location, String structureType) {
        switch (structureType.toLowerCase()) {
            case "laboratory":
                playLaboratoryActivationEffect(location);
                break;
            case "reactor":
                playReactorActivationEffect(location);
                break;
            case "solar_panel":
                playSolarPanelActivationEffect(location);
                break;
            case "generator":
                playGeneratorActivationEffect(location);
                break;
        }
    }
    
    private void playLaboratoryActivationEffect(Location center) {
        new BukkitRunnable() {
            private int tick = 0;
            
            @Override
            public void run() {
                if (tick >= 100) {
                    cancel();
                    return;
                }
                
                // Спиральные частицы вокруг лаборатории
                double angle = tick * 0.3;
                double radius = 3.0 - (tick * 0.02);
                double height = tick * 0.05;
                
                for (int i = 0; i < 3; i++) {
                    double currentAngle = angle + (i * 2 * Math.PI / 3);
                    double x = center.getX() + radius * Math.cos(currentAngle);
                    double z = center.getZ() + radius * Math.sin(currentAngle);
                    double y = center.getY() + height;
                    
                    Location particleLoc = new Location(center.getWorld(), x, y, z);
                    center.getWorld().spawnParticle(Particle.ENCHANT, particleLoc, 3, 0.1, 0.1, 0.1, 0.1);
                }
                
                // Центральный столб энергии
                if (tick % 5 == 0) {
                    for (int y = 0; y < 4; y++) {
                        Location energyLoc = center.clone().add(0, y, 0);
                        center.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, energyLoc, 5, 0.3, 0.1, 0.3, 0.1);
                    }
                }
                
                // Звуковые эффекты
                if (tick == 0) {
                    center.getWorld().playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 2.0f, 1.0f);
                } else if (tick == 50) {
                    center.getWorld().playSound(center, Sound.BLOCK_CONDUIT_ACTIVATE, 2.0f, 1.2f);
                } else if (tick == 90) {
                    center.getWorld().playSound(center, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.0f);
                    // Финальный взрыв частиц
                    center.getWorld().spawnParticle(Particle.FIREWORK, center.add(0, 2, 0), 50, 2, 2, 2, 0.3);
                }
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void playReactorActivationEffect(Location center) {
        new BukkitRunnable() {
            private int tick = 0;
            
            @Override
            public void run() {
                if (tick >= 120) {
                    cancel();
                    return;
                }
                
                // Пульсирующие кольца энергии
                if (tick % 10 == 0) {
                    double radius = (tick % 40) * 0.2;
                    createEnergyRing(center.clone().add(0, 2, 0), radius, Particle.ELECTRIC_SPARK);
                }
                
                // Радиационные частицы
                for (int i = 0; i < 5; i++) {
                    double angle = Math.random() * 2 * Math.PI;
                    double distance = Math.random() * 8;
                    double x = center.getX() + distance * Math.cos(angle);
                    double z = center.getZ() + distance * Math.sin(angle);
                    double y = center.getY() + Math.random() * 6;
                    
                    Location particleLoc = new Location(center.getWorld(), x, y, z);
                    center.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, particleLoc, 1, 0, 0, 0, 0);
                }
                
                // Звуки реактора
                if (tick % 20 == 0) {
                    center.getWorld().playSound(center, Sound.BLOCK_BEACON_AMBIENT, 1.0f, 0.8f);
                }
                
                if (tick == 100) {
                    center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 0.5f);
                }
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void playSolarPanelActivationEffect(Location center) {
        new BukkitRunnable() {
            private int tick = 0;
            
            @Override
            public void run() {
                if (tick >= 80) {
                    cancel();
                    return;
                }
                
                // Солнечные лучи
                for (int i = 0; i < 8; i++) {
                    double angle = (2 * Math.PI * i) / 8;
                    Vector direction = new Vector(Math.cos(angle), -0.5, Math.sin(angle));
                    
                    for (int j = 0; j < 10; j++) {
                        Location rayLoc = center.clone().add(0, 10, 0).add(direction.clone().multiply(j));
                        center.getWorld().spawnParticle(Particle.END_ROD, rayLoc, 1, 0, 0, 0, 0);
                    }
                }
                
                // Энергетические искры на панели
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (Math.random() < 0.3) {
                            Location sparkLoc = center.clone().add(x, 0.5, z);
                            center.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, sparkLoc, 2, 0.1, 0.1, 0.1, 0.1);
                        }
                    }
                }
                
                if (tick == 0) {
                    center.getWorld().playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 1.5f, 1.5f);
                } else if (tick == 60) {
                    center.getWorld().playSound(center, Sound.BLOCK_CONDUIT_ACTIVATE, 1.5f, 1.8f);
                }
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void playGeneratorActivationEffect(Location center) {
        new BukkitRunnable() {
            private int tick = 0;
            
            @Override
            public void run() {
                if (tick >= 100) {
                    cancel();
                    return;
                }
                
                // Электрические разряды между громоотводами
                if (tick % 15 == 0) {
                    createLightningEffect(center);
                }
                
                // Энергетическое поле вокруг генератора
                double radius = 2.0 + Math.sin(tick * 0.2) * 0.5;
                createEnergyRing(center.clone().add(0, 1, 0), radius, Particle.ELECTRIC_SPARK);
                
                // Звуки электричества
                if (tick % 25 == 0) {
                    center.getWorld().playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 2.0f);
                }
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void createEnergyRing(Location center, double radius, Particle particle) {
        int points = 20;
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            
            Location particleLoc = new Location(center.getWorld(), x, center.getY(), z);
            center.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0.1);
        }
    }
    
    private void createLightningEffect(Location center) {
        // Создаем искусственную молнию между точками
        Location[] points = {
            center.clone().add(2, 2, 0),
            center.clone().add(-2, 2, 0),
            center.clone().add(0, 2, 2),
            center.clone().add(0, 2, -2)
        };
        
        for (int i = 0; i < points.length; i++) {
            Location start = points[i];
            Location end = points[(i + 1) % points.length];
            
            Vector direction = end.toVector().subtract(start.toVector()).normalize();
            double distance = start.distance(end);
            
            for (double d = 0; d < distance; d += 0.3) {
                Location sparkLoc = start.clone().add(direction.clone().multiply(d));
                // Добавляем случайное отклонение для эффекта молнии
                sparkLoc.add((Math.random() - 0.5) * 0.3, (Math.random() - 0.5) * 0.3, (Math.random() - 0.5) * 0.3);
                center.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, sparkLoc, 1, 0, 0, 0, 0.2);
            }
        }
    }
    
    public void playResearchCompleteEffect(Player player) {
        Location center = player.getLocation();
        
        new BukkitRunnable() {
            private int tick = 0;
            
            @Override
            public void run() {
                if (tick >= 60) {
                    cancel();
                    return;
                }
                
                // Спиральные частицы знаний вокруг игрока
                double angle = tick * 0.5;
                double radius = 2.0;
                double height = Math.sin(tick * 0.3) * 2;
                
                for (int i = 0; i < 5; i++) {
                    double currentAngle = angle + (i * 2 * Math.PI / 5);
                    double x = center.getX() + radius * Math.cos(currentAngle);
                    double z = center.getZ() + radius * Math.sin(currentAngle);
                    double y = center.getY() + 1 + height;
                    
                    Location particleLoc = new Location(center.getWorld(), x, y, z);
                    center.getWorld().spawnParticle(Particle.ENCHANT, particleLoc, 3, 0.1, 0.1, 0.1, 0.1);
                }
                
                // Книги летают вокруг
                if (tick % 10 == 0) {
                    for (int i = 0; i < 3; i++) {
                        double bookAngle = Math.random() * 2 * Math.PI;
                        double bookRadius = 1.5 + Math.random();
                        double x = center.getX() + bookRadius * Math.cos(bookAngle);
                        double z = center.getZ() + bookRadius * Math.sin(bookAngle);
                        double y = center.getY() + 1 + Math.random() * 2;
                        
                        Location bookLoc = new Location(center.getWorld(), x, y, z);
                        center.getWorld().spawnParticle(Particle.ENCHANT, bookLoc, 5, 0.2, 0.2, 0.2, 0.1);
                    }
                }
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
        
        // Звуковые эффекты
        player.playSound(center, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                player.playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            }
        }.runTaskLater(plugin, 20L);
    }
    
    public void playPowerArmorModeSwitch(Player player, String newMode) {
        Location center = player.getLocation().add(0, 1, 0);
        
        // Энергетическая волна при смене режима
        new BukkitRunnable() {
            private int tick = 0;
            
            @Override
            public void run() {
                if (tick >= 20) {
                    cancel();
                    return;
                }
                
                double radius = tick * 0.3;
                createEnergyRing(center, radius, Particle.ELECTRIC_SPARK);
                
                // Цветные частицы в зависимости от режима
                Particle modeParticle = getModeParticle(newMode);
                center.getWorld().spawnParticle(modeParticle, center, 5, 0.5, 0.5, 0.5, 0.1);
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        player.playSound(center, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
    }
    
    private Particle getModeParticle(String mode) {
        switch (mode.toLowerCase()) {
            case "combat": return Particle.CRIT;
            case "stealth": return Particle.SMOKE;
            case "flight": return Particle.CLOUD;
            case "shield": return Particle.ENCHANT;
            default: return Particle.ELECTRIC_SPARK;
        }
    }
    
    public void playRailgunChargeEffect(Player player) {
        Location muzzle = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(1.5));
        
        new BukkitRunnable() {
            private int tick = 0;
            
            @Override
            public void run() {
                if (tick >= 30) {
                    cancel();
                    return;
                }
                
                // Энергия собирается в дуле
                double intensity = tick / 30.0;
                int particleCount = (int) (intensity * 20);
                
                muzzle.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, muzzle, particleCount, 0.1, 0.1, 0.1, 0.2);
                
                // Звук зарядки
                if (tick % 5 == 0) {
                    float pitch = 0.5f + (intensity * 1.5f);
                    player.playSound(muzzle, Sound.BLOCK_NOTE_BLOCK_HARP, 0.3f, pitch);
                }
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
