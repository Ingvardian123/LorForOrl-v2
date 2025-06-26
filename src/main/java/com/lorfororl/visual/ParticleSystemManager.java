package com.lorfororl.visual;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class ParticleSystemManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<String, ParticleSystem> activeSystems;
    private final Map<UUID, Set<String>> playerSystems;
    private BukkitTask updateTask;
    
    public ParticleSystemManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.activeSystems = new ConcurrentHashMap<>();
        this.playerSystems = new ConcurrentHashMap<>();
        startUpdateTask();
    }
    
    private void startUpdateTask() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateAllSystems();
            }
        }.runTaskTimer(plugin, 0L, 1L); // Каждый тик для плавности
    }
    
    private void updateAllSystems() {
        // Адаптивное качество в зависимости от нагрузки
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        float qualityMultiplier = Math.max(0.3f, Math.min(1.0f, 20.0f / onlinePlayers));
        
        Iterator<Map.Entry<String, ParticleSystem>> iterator = activeSystems.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, ParticleSystem> entry = iterator.next();
            ParticleSystem system = entry.getValue();
            
            try {
                if (system.isExpired()) {
                    system.cleanup();
                    iterator.remove();
                    removeFromPlayerSystems(entry.getKey());
                } else {
                    system.setQualityMultiplier(qualityMultiplier);
                    system.update();
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка в системе частиц " + entry.getKey() + ": " + e.getMessage());
                iterator.remove();
            }
        }
    }
    
    public void createEnergyAura(Player player, String type, int duration) {
        String systemId = "energy_aura_" + player.getUniqueId() + "_" + System.currentTimeMillis();
        
        ParticleSystem system = new ParticleSystem(systemId, duration) {
            private int tick = 0;
            private final Color[] gradientColors = getEnergyGradient(type);
            
            @Override
            public void update() {
                if (!player.isOnline()) {
                    expire();
                    return;
                }
                
                Location center = player.getLocation().add(0, 1, 0);
                float quality = getQualityMultiplier();
                
                // Основное кольцо энергии с градиентом
                double radius = 1.5 + Math.sin(tick * 0.1) * 0.3;
                int ringParticles = (int)(20 * quality);
                createGradientRing(center, radius, gradientColors, ringParticles);
                
                // Вертикальные потоки энергии с волновым эффектом
                for (int i = 0; i < 3; i++) {
                    double height = i * 0.7;
                    double wave = Math.sin(tick * 0.15 + i) * 0.2;
                    Location energyLoc = center.clone().add(wave, height - 1, wave * 0.5);
                    
                    if (quality > 0.5f) {
                        spawnColoredParticle(energyLoc, gradientColors[tick % gradientColors.length], 
                            (int)(3 * quality));
                    }
                }
                
                // Орбитальные частицы с улучшенной траекторией
                int orbitalCount = (int)(4 * quality);
                for (int i = 0; i < orbitalCount; i++) {
                    double angle = (tick * 0.05) + (i * Math.PI * 2 / orbitalCount);
                    double orbitalRadius = 2.0 + Math.sin(angle * 3) * 0.3;
                    double x = center.getX() + orbitalRadius * Math.cos(angle);
                    double z = center.getZ() + orbitalRadius * Math.sin(angle);
                    double y = center.getY() + Math.sin(angle * 2) * 0.5 + Math.cos(tick * 0.1) * 0.2;
                    
                    Location orbitalLoc = new Location(center.getWorld(), x, y, z);
                    Color orbitalColor = gradientColors[(tick + i * 2) % gradientColors.length];
                    spawnColoredParticle(orbitalLoc, orbitalColor, 1);
                }
                
                tick++;
            }
        };
        
        activeSystems.put(systemId, system);
        addToPlayerSystems(player.getUniqueId(), systemId);
    }
    
    public void createReactorMeltdownEffect(Location center, int duration) {
        String systemId = "reactor_meltdown_" + center.hashCode() + "_" + System.currentTimeMillis();
        
        ParticleSystem system = new ParticleSystem(systemId, duration) {
            private int tick = 0;
            private final int maxRadius = 25;
            
            @Override
            public void update() {
                // Расширяющиеся кольца радиации
                double currentRadius = (tick * 0.1) % maxRadius;
                createRadiationRing(center, currentRadius, 30);
                
                // Поднимающиеся радиоактивные частицы
                for (int i = 0; i < 15; i++) {
                    double angle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
                    double distance = ThreadLocalRandom.current().nextDouble() * maxRadius;
                    double x = center.getX() + distance * Math.cos(angle);
                    double z = center.getZ() + distance * Math.sin(angle);
                    double y = center.getY() + ThreadLocalRandom.current().nextDouble() * 15;
                    
                    Location particleLoc = new Location(center.getWorld(), x, y, z);
                    spawnParticle(Particle.ANGRY_VILLAGER, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);
                }
                
                // Грибовидное облако
                if (tick > 100) {
                    createMushroomCloud(center, tick - 100);
                }
                
                // Звуковые эффекты
                if (tick % 40 == 0) {
                    center.getWorld().playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
                }
                
                tick++;
            }
        };
        
        activeSystems.put(systemId, system);
    }
    
    public void createLaboratoryActivationEffect(Location center, int duration) {
        String systemId = "lab_activation_" + center.hashCode() + "_" + System.currentTimeMillis();
        
        ParticleSystem system = new ParticleSystem(systemId, duration) {
            private int tick = 0;
            
            @Override
            public void update() {
                // Спиральные потоки энергии
                for (int spiral = 0; spiral < 3; spiral++) {
                    double angle = (tick * 0.2) + (spiral * 2 * Math.PI / 3);
                    double radius = 3.0 - (tick * 0.01);
                    double height = tick * 0.03;
                    
                    if (radius > 0.5) {
                        double x = center.getX() + radius * Math.cos(angle);
                        double z = center.getZ() + radius * Math.sin(angle);
                        double y = center.getY() + height;
                        
                        Location spiralLoc = new Location(center.getWorld(), x, y, z);
                        spawnParticle(Particle.ENCHANT, spiralLoc, 3, 0.1, 0.1, 0.1, 0.1);
                    }
                }
                
                // Центральный столб энергии
                for (int y = 0; y < 5; y++) {
                    Location pillarLoc = center.clone().add(0, y, 0);
                    double intensity = Math.sin(tick * 0.1 + y * 0.5) * 0.5 + 0.5;
                    int particleCount = (int) (intensity * 8);
                    spawnParticle(Particle.ELECTRIC_SPARK, pillarLoc, particleCount, 0.3, 0.1, 0.3, 0.1);
                }
                
                // Пульсирующие кольца
                if (tick % 20 == 0) {
                    createEnergyPulse(center, 4.0, Color.AQUA);
                }
                
                tick++;
            }
        };
        
        activeSystems.put(systemId, system);
    }
    
    public void createRailgunTrail(Location start, Location end, String mode) {
        String systemId = "railgun_trail_" + System.currentTimeMillis();
        
        ParticleSystem system = new ParticleSystem(systemId, 60) { // 3 секунды
            private int tick = 0;
            
            @Override
            public void update() {
                Vector direction = end.toVector().subtract(start.toVector()).normalize();
                double distance = start.distance(end);
                
                // Основной луч
                for (double d = 0; d < distance; d += 0.5) {
                    Location trailLoc = start.clone().add(direction.clone().multiply(d));
                    
                    // Добавляем случайное отклонение для эффекта энергии
                    trailLoc.add(
                        (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.2,
                        (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.2,
                        (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.2
                    );
                    
                    Particle particle = getRailgunParticle(mode);
                    spawnParticle(particle, trailLoc, 1, 0.05, 0.05, 0.05, 0.1);
                }
                
                // Затухающий эффект
                double fadeIntensity = 1.0 - (tick / 60.0);
                if (fadeIntensity <= 0) {
                    expire();
                }
                
                tick++;
            }
        };
        
        activeSystems.put(systemId, system);
    }
    
    public void createPowerArmorModeTransition(Player player, String fromMode, String toMode) {
        String systemId = "armor_transition_" + player.getUniqueId() + "_" + System.currentTimeMillis();
        
        ParticleSystem system = new ParticleSystem(systemId, 40) { // 2 секунды
            private int tick = 0;
            
            @Override
            public void update() {
                if (!player.isOnline()) {
                    expire();
                    return;
                }
                
                Location center = player.getLocation().add(0, 1, 0);
                
                // Расширяющаяся энергетическая волна
                double radius = tick * 0.15;
                createEnergyRing(center, radius, getModeColor(toMode), 15);
                
                // Частицы старого режима исчезают
                if (tick < 20) {
                    Particle oldParticle = getModeParticle(fromMode);
                    for (int i = 0; i < 10; i++) {
                        Location oldLoc = center.clone().add(
                            (ThreadLocalRandom.current().nextDouble() - 0.5) * 2,
                            ThreadLocalRandom.current().nextDouble() * 2 - 1,
                            (ThreadLocalRandom.current().nextDouble() - 0.5) * 2
                        );
                        spawnParticle(oldParticle, oldLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }
                
                // Частицы нового режима появляются
                if (tick > 20) {
                    Particle newParticle = getModeParticle(toMode);
                    for (int i = 0; i < 15; i++) {
                        Location newLoc = center.clone().add(
                            (ThreadLocalRandom.current().nextDouble() - 0.5) * 1.5,
                            ThreadLocalRandom.current().nextDouble() * 1.5 - 0.75,
                            (ThreadLocalRandom.current().nextDouble() - 0.5) * 1.5
                        );
                        spawnParticle(newParticle, newLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }
                
                tick++;
            }
        };
        
        activeSystems.put(systemId, system);
        addToPlayerSystems(player.getUniqueId(), systemId);
    }
    
    // Вспомогательные методы
    private void createEnergyRing(Location center, double radius, Color color, int points) {
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            
            Location ringLoc = new Location(center.getWorld(), x, center.getY(), z);
            spawnColoredParticle(ringLoc, color, 1);
        }
    }
    
    private void createRadiationRing(Location center, double radius, int points) {
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            
            Location ringLoc = new Location(center.getWorld(), x, center.getY(), z);
            spawnParticle(Particle.ANGRY_VILLAGER, ringLoc, 1, 0.1, 0.1, 0.1, 0.05);
        }
    }
    
    private void createMushroomCloud(Location center, int age) {
        // Ствол гриба
        for (int y = 0; y < 20; y++) {
            Location stemLoc = center.clone().add(0, y, 0);
            spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, stemLoc, 3, 0.5, 0.1, 0.5, 0.05);
        }
        
        // Шапка гриба
        double cloudRadius = Math.min(15, age * 0.1);
        for (int i = 0; i < 30; i++) {
            double angle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
            double distance = ThreadLocalRandom.current().nextDouble() * cloudRadius;
            double x = center.getX() + distance * Math.cos(angle);
            double z = center.getZ() + distance * Math.sin(angle);
            double y = center.getY() + 20 + ThreadLocalRandom.current().nextDouble() * 5;
            
            Location cloudLoc = new Location(center.getWorld(), x, y, z);
            spawnParticle(Particle.EXPLOSION_EMITTER, cloudLoc, 2, 1, 1, 1, 0.1);
        }
    }
    
    private void createEnergyPulse(Location center, double maxRadius, Color color) {
        new BukkitRunnable() {
            private double currentRadius = 0;
            
            @Override
            public void run() {
                if (currentRadius >= maxRadius) {
                    cancel();
                    return;
                }
                
                createEnergyRing(center, currentRadius, color, 25);
                currentRadius += 0.3;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        if (location.getWorld() != null) {
            location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
        }
    }
    
    private void spawnColoredParticle(Location location, Color color) {
        if (location.getWorld() != null) {
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);
            location.getWorld().spawnParticle(Particle.DUST, location, 1, 0, 0, 0, 0, dustOptions);
        }
    }

    private Color[] getEnergyGradient(String type) {
        switch (type.toLowerCase()) {
            case "nuclear":
                return new Color[]{Color.LIME, Color.GREEN, Color.YELLOW, Color.GREEN};
            case "solar":
                return new Color[]{Color.YELLOW, Color.ORANGE, Color.RED, Color.YELLOW};
            case "electric":
                return new Color[]{Color.CYAN, Color.BLUE, Color.WHITE, Color.CYAN};
            case "thermal":
                return new Color[]{Color.RED, Color.ORANGE, Color.YELLOW, Color.RED};
            default:
                return new Color[]{Color.WHITE, Color.GRAY, Color.GRAY, Color.WHITE};
        }
    }

    private void createGradientRing(Location center, double radius, Color[] colors, int points) {
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);

            Location ringLoc = new Location(center.getWorld(), x, center.getY(), z);
            Color color = colors[i % colors.length];
            spawnColoredParticle(ringLoc, color, 1);
        }
    }

    private void spawnColoredParticle(Location location, Color color, int count) {
        if (location.getWorld() != null) {
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);
            location.getWorld().spawnParticle(Particle.DUST, location, count, 0.05, 0.05, 0.05, 0, dustOptions);
        }
    }
    
    private Color getEnergyColor(String type) {
        switch (type.toLowerCase()) {
            case "nuclear": return Color.GREEN;
            case "solar": return Color.YELLOW;
            case "electric": return Color.BLUE;
            case "thermal": return Color.RED;
            default: return Color.WHITE;
        }
    }
    
    private Particle getEnergyParticle(String type) {
        switch (type.toLowerCase()) {
            case "nuclear": return Particle.ANGRY_VILLAGER;
            case "solar": return Particle.END_ROD;
            case "electric": return Particle.ELECTRIC_SPARK;
            case "thermal": return Particle.FLAME;
            default: return Particle.ENCHANT;
        }
    }
    
    private Particle getRailgunParticle(String mode) {
        switch (mode.toLowerCase()) {
            case "piercing": return Particle.CRIT;
            case "explosive": return Particle.FLAME;
            case "scatter": return Particle.FIREWORK;
            case "overcharge": return Particle.DRAGON_BREATH;
            case "emp": return Particle.ENCHANT;
            default: return Particle.ELECTRIC_SPARK;
        }
    }
    
    private Color getModeColor(String mode) {
        switch (mode.toLowerCase()) {
            case "combat": return Color.RED;
            case "stealth": return Color.PURPLE;
            case "flight": return Color.AQUA;
            case "shield": return Color.YELLOW;
            default: return Color.WHITE;
        }
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
    
    private void addToPlayerSystems(UUID playerId, String systemId) {
        playerSystems.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet()).add(systemId);
    }
    
    private void removeFromPlayerSystems(String systemId) {
        playerSystems.values().forEach(set -> set.remove(systemId));
    }
    
    public void removePlayerSystems(UUID playerId) {
        Set<String> systems = playerSystems.remove(playerId);
        if (systems != null) {
            for (String systemId : systems) {
                ParticleSystem system = activeSystems.remove(systemId);
                if (system != null) {
                    system.cleanup();
                }
            }
        }
    }
    
    public void shutdown() {
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
        }
        
        // Очищаем все активные системы
        for (ParticleSystem system : activeSystems.values()) {
            system.cleanup();
        }
        activeSystems.clear();
        playerSystems.clear();
    }
    
    // Абстрактный класс для систем частиц
    private abstract static class ParticleSystem {
        protected final String id;
        protected final long startTime;
        protected final int duration; // в тиках
        protected boolean expired = false;
        private float qualityMultiplier = 1.0f;
        
        public ParticleSystem(String id, int duration) {
            this.id = id;
            this.startTime = System.currentTimeMillis();
            this.duration = duration;
        }
        
        public abstract void update();
        
        public boolean isExpired() {
            return expired || (System.currentTimeMillis() - startTime) > (duration * 50); // 50мс на тик
        }
        
        public void expire() {
            this.expired = true;
        }
        
        public void cleanup() {
            // Переопределить при необходимости
        }
        
        public String getId() {
            return id;
        }
        
        public float getQualityMultiplier() {
            return qualityMultiplier;
        }
        
        public void setQualityMultiplier(float qualityMultiplier) {
            this.qualityMultiplier = qualityMultiplier;
        }
    }
}
