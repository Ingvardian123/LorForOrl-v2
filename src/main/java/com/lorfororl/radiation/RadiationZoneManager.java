package com.lorfororl.radiation;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RadiationZoneManager {
    
    private final LorForOrlPlugin plugin;
    private final List<RadiationZone> radiationZones;
    private BukkitRunnable zoneTask;
    
    public RadiationZoneManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.radiationZones = new ArrayList<>();
    }
    
    public void startTasks() {
        zoneTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateRadiationZones();
            }
        };
        zoneTask.runTaskTimer(plugin, 0L, 100L); // Каждые 5 секунд
    }
    
    public void shutdown() {
        if (zoneTask != null) {
            zoneTask.cancel();
        }
    }
    
    private void updateRadiationZones() {
        Iterator<RadiationZone> iterator = radiationZones.iterator();
        
        while (iterator.hasNext()) {
            RadiationZone zone = iterator.next();
            
            // Удаляем истекшие зоны
            if (zone.isExpired()) {
                iterator.remove();
                continue;
            }
            
            // Показываем визуальные эффекты
            showZoneEffects(zone);
            
            // Воздействуем на игроков в зоне
            affectPlayersInZone(zone);
        }
    }
    
    private void showZoneEffects(RadiationZone zone) {
        Location center = zone.getCenter();
        double radius = zone.getRadius();
        double radiation = zone.getDecayedRadiation();
        
        if (radiation <= 0.01) return; // Слишком слабая радиация для эффектов
        
        // Количество частиц зависит от уровня радиации
        int particleCount = (int) Math.min(50, radiation * 10);
        
        // Цвет частиц зависит от типа зоны
        Particle particle = getParticleForZone(zone);
        
        // Показываем частицы по периметру зоны
        for (int i = 0; i < 8; i++) {
            double angle = (2 * Math.PI * i) / 8;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            
            Location particleLoc = new Location(center.getWorld(), x, center.getY() + 1, z);
            center.getWorld().spawnParticle(particle, particleLoc, particleCount / 8, 0.5, 1, 0.5, 0.1);
        }
        
        // Центральные эффекты для сильных зон
        if (radiation > 2.0) {
            center.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, 
                center.add(0, 2, 0), 5, 1, 1, 1, 0.1);
        }
    }
    
    private Particle getParticleForZone(RadiationZone zone) {
        switch (zone.getType()) {
            case REACTOR_EXPLOSION:
                return Particle.LAVA;
            case URANIUM_SPILL:
                return Particle.VILLAGER_HAPPY;
            case NATURAL_DECAY:
                return Particle.COMPOSTER;
            case EXPERIMENTAL:
                return Particle.PORTAL;
            default:
                return Particle.SNEEZE;
        }
    }
    
    private void affectPlayersInZone(RadiationZone zone) {
        for (Player player : zone.getCenter().getWorld().getPlayers()) {
            if (zone.isInZone(player.getLocation())) {
                double radiation = zone.getRadiationAt(player.getLocation());
                
                // Добавляем радиацию игроку
                plugin.getRadiationManager().addRadiation(player, radiation * 0.1); // 10% от зоны за тик
                
                // Уведомление игрока
                if (radiation > 0.5) {
                    String zoneName = zone.getType().getDisplayName();
                    sendActionBar(player, String.format("§c☢ Радиационная зона: %s (%.2f у.е.)", 
                        zoneName, radiation));
                }
            }
        }
    }
    
    private void sendActionBar(Player player, String message) {
        try {
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
        } catch (Exception e) {
            // Fallback для старых версий
            player.sendMessage(message);
        }
    }
    
    public void createRadiationZone(Location center, int radius, double radiationLevel, 
                                   long duration, RadiationZone.RadiationZoneType type) {
        RadiationZone zone = new RadiationZone(center, radius, radiationLevel, duration, type);
        radiationZones.add(zone);
    }
    
    public void createReactorExplosionZone(Location center) {
        // Большая зона от взрыва реактора
        createRadiationZone(center, 25, 5.0, 3600000L, RadiationZone.RadiationZoneType.REACTOR_EXPLOSION); // 1 час
    }
    
    public void createUraniumSpillZone(Location center) {
        // Средняя зона от разлива урана
        createRadiationZone(center, 10, 2.0, 1800000L, RadiationZone.RadiationZoneType.URANIUM_SPILL); // 30 минут
    }
    
    public double getTotalRadiationAt(Location location) {
        double totalRadiation = 0.0;
        
        for (RadiationZone zone : radiationZones) {
            if (zone.isInZone(location)) {
                totalRadiation += zone.getRadiationAt(location);
            }
        }
        
        return totalRadiation;
    }
    
    public List<RadiationZone> getRadiationZones() {
        return new ArrayList<>(radiationZones);
    }
    
    public void clearZonesAt(Location location, int radius) {
        radiationZones.removeIf(zone -> zone.getCenter().distance(location) <= radius);
    }
}
