package com.lorfororl.radiation;

import org.bukkit.Location;

public class RadiationZone {

    public enum RadiationZoneType {
        NONE("Нет зоны"),
        LOW("Низкая"),
        MEDIUM("Средняя"),
        HIGH("Высокая"),
        EXTREME("Экстремальная"),
        NUCLEAR_BOMB("Ядерный взрыв"),
        REACTOR_EXPLOSION("Взрыв реактора"),
        URANIUM_SPILL("Разлив урана"),
        NATURAL_DECAY("Естественный распад"),
        EXPERIMENTAL("Экспериментальная");

        private final String displayName;

        RadiationZoneType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final Location center;
    private final int radius;
    private final double radiationLevel;
    private final long creationTime;
    private final long duration;
    private final RadiationZoneType type;

    public RadiationZone(Location center, int radius, double radiationLevel, long duration, RadiationZoneType type) {
        this.center = center.clone();
        this.radius = radius;
        this.radiationLevel = radiationLevel;
        this.creationTime = System.currentTimeMillis();
        this.duration = duration;
        this.type = type;
    }

    public RadiationZoneType getType() {
        return type;
    }

    public double getRadius() {
        return radius;
    }

    public Location getCenter() {
        return center.clone();
    }

    public double getRadiationLevel() {
        return radiationLevel;
    }

    public boolean isInZone(Location location) {
        if (!location.getWorld().equals(center.getWorld())) {
            return false;
        }
        double distance = location.distance(center);
        return distance <= radius;
    }

    public boolean isInZone(double x, double z) {
        double distance = Math.sqrt(Math.pow(x - center.getX(), 2) + Math.pow(z - center.getZ(), 2));
        return distance <= radius;
    }

    public double getRadiationAt(Location location) {
        if (!isInZone(location)) {
            return 0.0;
        }
        
        double distance = location.distance(center);
        double falloff = 1.0 - (distance / radius);
        return radiationLevel * falloff * getDecayFactor();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - creationTime > duration;
    }

    public double getDecayedRadiation() {
        return radiationLevel * getDecayFactor();
    }

    private double getDecayFactor() {
        if (duration <= 0) return 1.0;
        
        long elapsed = System.currentTimeMillis() - creationTime;
        double progress = (double) elapsed / duration;
        
        // Экспоненциальный спад радиации
        return Math.exp(-progress * 2.0);
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getDuration() {
        return duration;
    }

    public long getRemainingTime() {
        return Math.max(0, duration - (System.currentTimeMillis() - creationTime));
    }
}
