package com.lorfororl.protection;

import com.lorfororl.LorForOrlPlugin;
import com.lorfororl.energy.EnergyConsumer;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class EnergyShield implements EnergyConsumer {
    
    private final LorForOrlPlugin plugin;
    private final Location center;
    private final UUID ownerId;
    
    // Настройки щита
    private int radius = 10;
    private int energyCapacity = 50000;
    private int currentEnergy = 0;
    private int energyPerTick = 10;
    private int energyPerBlock = 100;
    private boolean isActive = false;
    private ShieldMode mode = ShieldMode.DEFENSIVE;
    
    // Защищенная область
    private final Set<Location> protectedBlocks;
    private final Map<UUID, Long> lastDamageTime;
    
    // Визуальные эффекты
    private BukkitRunnable visualTask;
    private BukkitRunnable energyTask;
    private final List<Location> shieldParticles;
    
    // Статистика
    private int blocksProtected = 0;
    private int projectilesBlocked = 0;
    private int playersRepelled = 0;
    private long activationTime = 0;
    
    public EnergyShield(LorForOrlPlugin plugin, Location center, UUID ownerId) {
        this.plugin = plugin;
        this.center = center.clone();
        this.ownerId = ownerId;
        this.protectedBlocks = new HashSet<>();
        this.lastDamageTime = new HashMap<>();
        this.shieldParticles = new ArrayList<>();
        
        generateProtectedArea();
        generateShieldParticles();
    }
    
    public void activate() {
        if (isActive) return;
        
        isActive = true;
        activationTime = System.currentTimeMillis();
        
        startVisualEffects();
        startEnergyConsumption();
        
        // Эффекты активации
        showActivationEffects();
        
        Player owner = Bukkit.getPlayer(ownerId);
        if (owner != null && owner.isOnline()) {
            owner.sendMessage("§a🛡 Энергощит активирован!");
            owner.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 1.5f);
        }
    }
    
    public void deactivate() {
        if (!isActive) return;
        
        isActive = false;
        
        stopVisualEffects();
        stopEnergyConsumption();
        
        // Эффекты деактивации
        showDeactivationEffects();
        
        Player owner = Bukkit.getPlayer(ownerId);
        if (owner != null && owner.isOnline()) {
            owner.sendMessage("§c🛡 Энергощит деактивирован!");
            owner.playSound(center, Sound.BLOCK_BEACON_DEACTIVATE, 2.0f, 0.8f);
        }
    }
    
    private void generateProtectedArea() {
        protectedBlocks.clear();
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x*x + y*y + z*z);
                    if (distance <= radius) {
                        Location loc = center.clone().add(x, y, z);
                        protectedBlocks.add(loc);
                    }
                }
            }
        }
    }
    
    private void generateShieldParticles() {
        shieldParticles.clear();
        
        // Создаем сферу из частиц
        for (int i = 0; i < 360; i += 10) {
            for (int j = 0; j < 180; j += 10) {
                double x = radius * Math.sin(Math.toRadians(j)) * Math.cos(Math.toRadians(i));
                double y = radius * Math.cos(Math.toRadians(j));
                double z = radius * Math.sin(Math.toRadians(j)) * Math.sin(Math.toRadians(i));
                
                Location particleLoc = center.clone().add(x, y, z);
                shieldParticles.add(particleLoc);
            }
        }
    }
    
    private void startVisualEffects() {
        visualTask = new BukkitRunnable() {
            private int tick = 0;
            
            @Override
            public void run() {
                if (!isActive) {
                    cancel();
                    return;
                }
                
                showShieldParticles(tick);
                tick++;
            }
        };
        
        visualTask.runTaskTimer(plugin, 0L, 2L); // Каждые 0.1 секунды
    }
    
    private void startEnergyConsumption() {
        energyTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive) {
                    cancel();
                    return;
                }
                
                if (currentEnergy >= energyPerTick) {
                    currentEnergy -= energyPerTick;
                } else {
                    // Недостаточно энергии - отключаем щит
                    deactivate();
                    
                    Player owner = Bukkit.getPlayer(ownerId);
                    if (owner != null && owner.isOnline()) {
                        owner.sendMessage("§c⚡ Энергощит отключен - недостаточно энергии!");
                    }
                }
            }
        };
        
        energyTask.runTaskTimer(plugin, 0L, 20L); // Каждую секунду
    }
    
    private void stopVisualEffects() {
        if (visualTask != null) {
            visualTask.cancel();
            visualTask = null;
        }
    }
    
    private void stopEnergyConsumption() {
        if (energyTask != null) {
            energyTask.cancel();
            energyTask = null;
        }
    }
    
    private void showShieldParticles(int tick) {
        World world = center.getWorld();
        if (world == null) return;
        
        // Показываем частицы с волновым эффектом
        double wave = Math.sin(tick * 0.1) * 0.2;
        
        for (int i = 0; i < Math.min(20, shieldParticles.size()); i++) {
            int index = (tick + i * 5) % shieldParticles.size();
            Location particleLoc = shieldParticles.get(index).clone().add(0, wave, 0);
            
            // Разные цвета в зависимости от режима
            Particle particle = getShieldParticle();
            world.spawnParticle(particle, particleLoc, 1, 0.1, 0.1, 0.1, 0);
        }
        
        // Дополнительные эффекты для центра
        if (tick % 20 == 0) {
            world.spawnParticle(Particle.END_ROD, center.clone().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.05);
        }
    }
    
    private Particle getShieldParticle() {
        switch (mode) {
            case DEFENSIVE: return Particle.ELECTRIC_SPARK;
            case AGGRESSIVE: return Particle.FLAME;
            case STEALTH: return Particle.SMOKE;
            default: return Particle.ELECTRIC_SPARK;
        }
    }
    
    public boolean isLocationProtected(Location location) {
        if (!isActive) return false;
        
        return center.distance(location) <= radius;
    }
    
    public boolean canBreakBlock(Player player, Location blockLocation) {
        if (!isActive || !isLocationProtected(blockLocation)) {
            return true;
        }
        
        // Владелец может ломать блоки
        if (player.getUniqueId().equals(ownerId)) {
            return true;
        }
        
        // Потребляем энергию за защиту блока
        if (currentEnergy >= energyPerBlock) {
            currentEnergy -= energyPerBlock;
            blocksProtected++;
            
            // Визуальные эффекты защиты
            showBlockProtectionEffects(blockLocation);
            
            return false;
        } else {
            // Недостаточно энергии для защиты
            return true;
        }
    }
    
    public boolean canDamageEntity(Entity attacker, Entity target, Location damageLocation) {
        if (!isActive || !isLocationProtected(damageLocation)) {
            return true;
        }
        
        // Владелец может атаковать
        if (attacker instanceof Player && ((Player) attacker).getUniqueId().equals(ownerId)) {
            return true;
        }
        
        // Защищаем сущность
        if (currentEnergy >= energyPerBlock) {
            currentEnergy -= energyPerBlock;
            
            // Визуальные эффекты защиты
            showEntityProtectionEffects(damageLocation);
            
            // Отталкиваем атакующего в агрессивном режиме
            if (mode == ShieldMode.AGGRESSIVE && attacker instanceof Player) {
                repelPlayer((Player) attacker);
            }
            
            return false;
        }
        
        return true;
    }
    
    public boolean canProjectilePass(Projectile projectile) {
        if (!isActive || !isLocationProtected(projectile.getLocation())) {
            return true;
        }
        
        // Проверяем владельца снаряда
        if (projectile.getShooter() instanceof Player) {
            Player shooter = (Player) projectile.getShooter();
            if (shooter.getUniqueId().equals(ownerId)) {
                return true;
            }
        }
        
        // Блокируем снаряд
        if (currentEnergy >= energyPerBlock) {
            currentEnergy -= energyPerBlock;
            projectilesBlocked++;
            
            // Визуальные эффекты блокировки
            showProjectileBlockEffects(projectile.getLocation());
            
            // Уничтожаем снаряд
            projectile.remove();
            
            return false;
        }
        
        return true;
    }
    
    private void repelPlayer(Player player) {
        Vector direction = player.getLocation().toVector().subtract(center.toVector()).normalize();
        direction.multiply(2.0).setY(0.5);
        
        player.setVelocity(direction);
        player.sendMessage("§c⚡ Вы были оттолкнуты энергощитом!");
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
        
        playersRepelled++;
    }
    
    private void showActivationEffects() {
        World world = center.getWorld();
        if (world == null) return;
        
        // Волна активации
        for (int i = 0; i < 360; i += 10) {
            double x = Math.cos(Math.toRadians(i)) * radius;
            double z = Math.sin(Math.toRadians(i)) * radius;
            
            Location effectLoc = center.clone().add(x, 0, z);
            world.spawnParticle(Particle.ELECTRIC_SPARK, effectLoc, 10, 0.5, 0.5, 0.5, 0.1);
        }
        
        // Звук активации
        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 1.0f, 2.0f);
    }
    
    private void showDeactivationEffects() {
        World world = center.getWorld();
        if (world == null) return;
        
        // Эффекты деактивации
        world.spawnParticle(Particle.EXPLOSION, center, 5, 2, 2, 2, 0);
        world.spawnParticle(Particle.SMOKE, center, 20, 3, 3, 3, 0.1);
        
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
    }
    
    private void showBlockProtectionEffects(Location blockLocation) {
        World world = blockLocation.getWorld();
        if (world == null) return;
        
        world.spawnParticle(Particle.ELECTRIC_SPARK, blockLocation.clone().add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 0.1);
        world.playSound(blockLocation, Sound.BLOCK_ANVIL_HIT, 0.5f, 2.0f);
    }
    
    private void showEntityProtectionEffects(Location location) {
        World world = location.getWorld();
        if (world == null) return;
        
        world.spawnParticle(Particle.HEART, location.clone().add(0, 1, 0), 3, 0.5, 0.5, 0.5, 0);
        world.spawnParticle(Particle.ELECTRIC_SPARK, location, 8, 0.5, 0.5, 0.5, 0.1);
        world.playSound(location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
    }
    
    private void showProjectileBlockEffects(Location location) {
        World world = location.getWorld();
        if (world == null) return;
        
        world.spawnParticle(Particle.CRIT, location, 10, 0.3, 0.3, 0.3, 0.1);
        world.spawnParticle(Particle.ELECTRIC_SPARK, location, 5, 0.2, 0.2, 0.2, 0.05);
        world.playSound(location, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.8f);
    }
    
    // Геттеры и сеттеры
    public boolean isActive() { return isActive; }
    public int getRadius() { return radius; }
    public void setRadius(int radius) { 
        this.radius = Math.max(5, Math.min(50, radius)); 
        generateProtectedArea();
        generateShieldParticles();
    }
    
    public ShieldMode getMode() { return mode; }
    public void setMode(ShieldMode mode) { this.mode = mode; }
    
    public int getCurrentEnergy() { return currentEnergy; }
    public int getEnergyCapacity() { return energyCapacity; }
    public Location getCenter() { return center.clone(); }
    public UUID getOwnerId() { return ownerId; }
    
    public int getBlocksProtected() { return blocksProtected; }
    public int getProjectilesBlocked() { return projectilesBlocked; }
    public int getPlayersRepelled() { return playersRepelled; }
    
    public long getUptime() {
        return isActive ? System.currentTimeMillis() - activationTime : 0;
    }
    
    // EnergyConsumer implementation
    @Override
    public int getEnergyRequired() {
        return isActive ? energyPerTick : 0;
    }
    
    @Override
    public void receiveEnergy(int amount) {
        currentEnergy = Math.min(energyCapacity, currentEnergy + amount);
    }
    
    @Override
    public boolean hasEnoughEnergy() {
        return currentEnergy >= energyPerTick;
    }
    
    public enum ShieldMode {
        DEFENSIVE("Защитный"),
        AGGRESSIVE("Агрессивный"),
        STEALTH("Скрытый");
        
        private final String displayName;
        
        ShieldMode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
