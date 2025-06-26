package com.lorfororl.managers;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ParticleSystemManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, BukkitTask> activeEffects;
    private final Map<String, ParticleSystem> activeSystems;
    
    public ParticleSystemManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.activeEffects = new HashMap<>();
        this.activeSystems = new HashMap<>();
    }
    
    public void shutdown() {
        // Останавливаем все активные эффекты
        for (BukkitTask task : activeEffects.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        activeEffects.clear();
        
        // Останавливаем все системы частиц
        for (ParticleSystem system : activeSystems.values()) {
            system.stop();
        }
        activeSystems.clear();
    }
    
    public void spawnParticles(Location location, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        if (location.getWorld() == null) return;
        
        try {
            location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка при создании частиц: " + e.getMessage());
        }
    }
    
    public void spawnColoredParticles(Location location, Color color, int count) {
        if (location.getWorld() == null) return;
        
        try {
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);
            location.getWorld().spawnParticle(Particle.DUST, location, count, 0.5, 0.5, 0.5, 0, dustOptions);
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка при создании цветных частиц: " + e.getMessage());
        }
    }
    
    public void createRadiationEffect(Location location, double intensity) {
        World world = location.getWorld();
        if (world == null) return;
        
        // Зеленые частицы для радиации
        Color radiationColor = Color.LIME;
        spawnColoredParticles(location, radiationColor, (int)(intensity * 3));
        
        // Дополнительные эффекты для высокой радиации
        if (intensity > 10) {
            spawnParticles(location, Particle.ANGRY_VILLAGER, 3, 1.0, 1.0, 1.0, 0.1);
        }
        
        // Случайные зеленые вспышки
        if (ThreadLocalRandom.current().nextDouble() < intensity / 50.0) {
            spawnParticles(location, Particle.HAPPY_VILLAGER, 5, 2.0, 2.0, 2.0, 0.2);
        }
    }
    
    public void createEnergyEffect(Location location, Color color) {
        spawnColoredParticles(location, color, 8);
        spawnParticles(location, Particle.ELECTRIC_SPARK, 4, 0.5, 0.5, 0.5, 0.1);
        
        // Дополнительные искры
        for (int i = 0; i < 3; i++) {
            Location sparkLoc = location.clone().add(
                (ThreadLocalRandom.current().nextDouble() - 0.5) * 2,
                (ThreadLocalRandom.current().nextDouble() - 0.5) * 2,
                (ThreadLocalRandom.current().nextDouble() - 0.5) * 2
            );
            spawnParticles(sparkLoc, Particle.ELECTRIC_SPARK, 1, 0.1, 0.1, 0.1, 0.05);
        }
    }
    
    public void createExplosionEffect(Location location, boolean large) {
        World world = location.getWorld();
        if (world == null) return;
        
        if (large) {
            world.spawnParticle(Particle.EXPLOSION, location, 1);
            world.spawnParticle(Particle.LARGE_SMOKE, location, 20, 2.0, 2.0, 2.0, 0.1);
            world.spawnParticle(Particle.FLAME, location, 15, 1.5, 1.5, 1.5, 0.2);
        } else {
            world.spawnParticle(Particle.EXPLOSION, location, 1);
            world.spawnParticle(Particle.LARGE_SMOKE, location, 10, 1.0, 1.0, 1.0, 0.1);
            world.spawnParticle(Particle.FLAME, location, 8, 1.0, 1.0, 1.0, 0.1);
        }
    }
    
    public void createLaserEffect(Location start, Location end, Color color) {
        if (start.getWorld() == null || end.getWorld() == null) return;
        if (!start.getWorld().equals(end.getWorld())) return;
        
        double distance = start.distance(end);
        int particles = Math.max(5, (int)(distance * 3));
        
        for (int i = 0; i < particles; i++) {
            double ratio = (double) i / particles;
            Location particleLoc = start.clone().add(
                (end.getX() - start.getX()) * ratio,
                (end.getY() - start.getY()) * ratio,
                (end.getZ() - start.getZ()) * ratio
            );
            
            spawnColoredParticles(particleLoc, color, 1);
            
            // Добавляем искры для эффекта
            if (i % 3 == 0) {
                spawnParticles(particleLoc, Particle.ELECTRIC_SPARK, 1, 0.1, 0.1, 0.1, 0.05);
            }
        }
    }
    
    public void startContinuousEffect(Player player, Particle particle, Color color, int duration) {
        stopContinuousEffect(player);
        
        BukkitTask effect = new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = duration * 20; // Convert seconds to ticks
            
            @Override
            public void run() {
                if (ticks >= maxTicks || !player.isOnline()) {
                    this.cancel();
                    activeEffects.remove(player.getUniqueId());
                    return;
                }
                
                Location loc = player.getLocation().add(0, 1, 0);
                if (color != null) {
                    spawnColoredParticles(loc, color, 2);
                } else {
                    spawnParticles(loc, particle, 2, 0.5, 0.5, 0.5, 0.1);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 5L); // Every 5 ticks (0.25 seconds)
        
        activeEffects.put(player.getUniqueId(), effect);
    }
    
    public void stopContinuousEffect(Player player) {
        BukkitTask effect = activeEffects.remove(player.getUniqueId());
        if (effect != null && !effect.isCancelled()) {
            effect.cancel();
        }
    }
    
    public void createPortalEffect(Location location) {
        World world = location.getWorld();
        if (world == null) return;
        
        String systemId = "portal_" + location.hashCode() + "_" + System.currentTimeMillis();
        
        ParticleSystem system = new ParticleSystem(systemId, 60) { // 3 seconds
            int ticks = 0;
            
            @Override
            public void update() {
                if (ticks >= 60) {
                    stop();
                    return;
                }
                
                // Спиральный эффект портала
                double angle = ticks * 0.5;
                double radius = 2.0 - (ticks * 0.02);
                
                for (int i = 0; i < 4; i++) {
                    double currentAngle = angle + (i * Math.PI / 2);
                    double x = Math.cos(currentAngle) * radius;
                    double z = Math.sin(currentAngle) * radius;
                    double y = ticks * 0.05;
                    
                    Location particleLoc = location.clone().add(x, y, z);
                    spawnParticles(particleLoc, Particle.PORTAL, 1, 0, 0, 0, 0);
                }
                
                // Центральные частицы
                spawnParticles(location.clone().add(0, ticks * 0.05, 0), Particle.ENCHANT, 2, 0.3, 0.1, 0.3, 0.1);
                
                ticks++;
            }
        };
        
        activeSystems.put(systemId, system);
        system.start();
    }
    
    public void createEnergyAura(Player player, String energyType, int duration) {
        stopContinuousEffect(player);
        
        Color auraColor = getEnergyColor(energyType);
        String systemId = "aura_" + player.getUniqueId() + "_" + System.currentTimeMillis();
        
        ParticleSystem system = new ParticleSystem(systemId, duration * 20) {
            int ticks = 0;
            
            @Override
            public void update() {
                if (!player.isOnline() || ticks >= duration * 20) {
                    stop();
                    return;
                }
                
                Location center = player.getLocation().add(0, 1, 0);
                
                // Основное кольцо энергии
                double radius = 1.5 + Math.sin(ticks * 0.1) * 0.3;
                createEnergyRing(center, radius, auraColor, 12);
                
                // Вертикальные потоки
                for (int i = 0; i < 3; i++) {
                    double height = i * 0.7;
                    double wave = Math.sin(ticks * 0.15 + i) * 0.2;
                    Location energyLoc = center.clone().add(wave, height - 1, wave * 0.5);
                    spawnColoredParticles(energyLoc, auraColor, 1);
                }
                
                // Орбитальные частицы
                for (int i = 0; i < 3; i++) {
                    double angle = (ticks * 0.05) + (i * Math.PI * 2 / 3);
                    double orbitalRadius = 2.0 + Math.sin(angle * 2) * 0.3;
                    double x = center.getX() + orbitalRadius * Math.cos(angle);
                    double z = center.getZ() + orbitalRadius * Math.sin(angle);
                    double y = center.getY() + Math.sin(angle * 3) * 0.5;
                    
                    Location orbitalLoc = new Location(center.getWorld(), x, y, z);
                    spawnColoredParticles(orbitalLoc, auraColor, 1);
                }
                
                ticks++;
            }
        };
        
        activeSystems.put(systemId, system);
        system.start();
    }
    
    private void createEnergyRing(Location center, double radius, Color color, int points) {
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            
            Location ringLoc = new Location(center.getWorld(), x, center.getY(), z);
            spawnColoredParticles(ringLoc, color, 1);
        }
    }
    
    private Color getEnergyColor(String energyType) {
        switch (energyType.toLowerCase()) {
            case "nuclear": return Color.LIME;
            case "solar": return Color.YELLOW;
            case "electric": return Color.AQUA;
            case "thermal": return Color.RED;
            case "plasma": return Color.PURPLE;
            default: return Color.WHITE;
        }
    }
    
    public void createReactorMeltdownEffect(Location center, int duration) {
        String systemId = "meltdown_" + center.hashCode() + "_" + System.currentTimeMillis();
        
        ParticleSystem system = new ParticleSystem(systemId, duration * 20) {
            int ticks = 0;
            final int maxRadius = 25;
            
            @Override
            public void update() {
                if (ticks >= duration * 20) {
                    stop();
                    return;
                }
                
                // Расширяющиеся кольца радиации
                double currentRadius = (ticks * 0.1) % maxRadius;
                createRadiationRing(center, currentRadius, 20);
                
                // Поднимающиеся радиоактивные частицы
                for (int i = 0; i < 10; i++) {
                    double angle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
                    double distance = ThreadLocalRandom.current().nextDouble() * maxRadius;
                    double x = center.getX() + distance * Math.cos(angle);
                    double z = center.getZ() + distance * Math.sin(angle);
                    double y = center.getY() + ThreadLocalRandom.current().nextDouble() * 10;
                    
                    Location particleLoc = new Location(center.getWorld(), x, y, z);
                    spawnParticles(particleLoc, Particle.ANGRY_VILLAGER, 1, 0.1, 0.1, 0.1, 0.05);
                }
                
                // Грибовидное облако
                if (ticks > 100) {
                    createMushroomCloud(center, ticks - 100);
                }
                
                ticks++;
            }
        };
        
        activeSystems.put(systemId, system);
        system.start();
    }
    
    private void createRadiationRing(Location center, double radius, int points) {
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            
            Location ringLoc = new Location(center.getWorld(), x, center.getY(), z);
            spawnParticles(ringLoc, Particle.ANGRY_VILLAGER, 1, 0.1, 0.1, 0.1, 0.05);
        }
    }
    
    private void createMushroomCloud(Location center, int age) {
        // Ствол гриба
        for (int y = 0; y < 15; y++) {
            Location stemLoc = center.clone().add(0, y, 0);
            spawnParticles(stemLoc, Particle.LARGE_SMOKE, 2, 0.5, 0.1, 0.5, 0.05);
        }
        
        // Шапка гриба
        double cloudRadius = Math.min(10, age * 0.05);
        for (int i = 0; i < 20; i++) {
            double angle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
            double distance = ThreadLocalRandom.current().nextDouble() * cloudRadius;
            double x = center.getX() + distance * Math.cos(angle);
            double z = center.getZ() + distance * Math.sin(angle);
            double y = center.getY() + 15 + ThreadLocalRandom.current().nextDouble() * 3;
            
            Location cloudLoc = new Location(center.getWorld(), x, y, z);
            spawnParticles(cloudLoc, Particle.EXPLOSION, 1, 0.5, 0.5, 0.5, 0.1);
        }
    }
    
    public void createLaboratoryActivationEffect(Location center, int duration) {
        String systemId = "lab_activation_" + center.hashCode() + "_" + System.currentTimeMillis();
        
        ParticleSystem system = new ParticleSystem(systemId, duration * 20) {
            int ticks = 0;
            
            @Override
            public void update() {
                if (ticks >= duration * 20) {
                    stop();
                    return;
                }
                
                // Спиральные потоки энергии
                for (int spiral = 0; spiral < 3; spiral++) {
                    double angle = (ticks * 0.2) + (spiral * 2 * Math.PI / 3);
                    double radius = 3.0 - (ticks * 0.01);
                    double height = ticks * 0.02;
                    
                    if (radius > 0.5) {
                        double x = center.getX() + radius * Math.cos(angle);
                        double z = center.getZ() + radius * Math.sin(angle);
                        double y = center.getY() + height;
                        
                        Location spiralLoc = new Location(center.getWorld(), x, y, z);
                        spawnParticles(spiralLoc, Particle.ENCHANT, 2, 0.1, 0.1, 0.1, 0.1);
                    }
                }
                
                // Центральный столб энергии
                for (int y = 0; y < 4; y++) {
                    Location pillarLoc = center.clone().add(0, y, 0);
                    double intensity = Math.sin(ticks * 0.1 + y * 0.5) * 0.5 + 0.5;
                    int particleCount = (int) (intensity * 5);
                    spawnParticles(pillarLoc, Particle.ELECTRIC_SPARK, particleCount, 0.3, 0.1, 0.3, 0.1);
                }
                
                ticks++;
            }
        };
        
        activeSystems.put(systemId, system);
        system.start();
    }
    
    // Абстрактный класс для систем частиц
    private abstract class ParticleSystem {
        protected final String id;
        protected final int duration;
        protected BukkitTask task;
        protected boolean running = false;
        
        public ParticleSystem(String id, int duration) {
            this.id = id;
            this.duration = duration;
        }
        
        public void start() {
            if (running) return;
            
            running = true;
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!running) {
                        cancel();
                        return;
                    }
                    update();
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
        
        public void stop() {
            running = false;
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
            activeSystems.remove(id);
        }
        
        public abstract void update();
        
        public String getId() { return id; }
        public boolean isRunning() { return running; }
    }
}
