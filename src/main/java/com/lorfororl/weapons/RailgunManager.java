package com.lorfororl.weapons;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RailgunManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, RailgunData> railgunData;
    private BukkitRunnable railgunTask;
    
    public RailgunManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.railgunData = new HashMap<>();
    }
    
    public void startTasks() {
        railgunTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateRailguns();
            }
        };
        railgunTask.runTaskTimer(plugin, 0L, 20L); // Каждую секунду
    }
    
    public void shutdown() {
        if (railgunTask != null) {
            railgunTask.cancel();
        }
    }
    
    private void updateRailguns() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (hasRailgun(player)) {
                RailgunData data = railgunData.computeIfAbsent(player.getUniqueId(), k -> new RailgunData());
                updateRailgunData(player, data);
            } else {
                railgunData.remove(player.getUniqueId());
            }
        }
    }
    
    private boolean hasRailgun(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        return mainHand != null && 
               plugin.getAdvancedItems().isAdvancedItem(mainHand) &&
               "railgun".equals(plugin.getAdvancedItems().getAdvancedItemType(mainHand));
    }
    
    private void updateRailgunData(Player player, RailgunData data) {
        // Обновляем кулдауны
        data.updateCooldowns();
        
        // Проверяем энергию
        int energy = plugin.getEnergyManager().getPlayerEnergy(player);
        if (energy < getEnergyCostForMode(data.getMode())) {
            data.setMode(RailgunMode.STANDARD);
        }
    }
    
    public boolean fireRailgun(Player player, RailgunMode mode) {
        if (!hasRailgun(player)) {
            player.sendMessage("§cУ вас нет рельсотрона!");
            return false;
        }
        
        RailgunData data = railgunData.get(player.getUniqueId());
        if (data == null) return false;
        
        if (data.isOnCooldown()) {
            long remaining = data.getRemainingCooldown() / 1000;
            player.sendMessage("§cРельсотрон перезаряжается! Осталось: " + remaining + " сек.");
            return false;
        }
        
        int energyCost = getEnergyCostForMode(mode);
        if (!plugin.getEnergyManager().consumePlayerEnergy(player, energyCost)) {
            player.sendMessage("§cНедостаточно энергии! Требуется: " + energyCost);
            return false;
        }
        
        // Стреляем
        fireProjectile(player, mode);
        
        // Устанавливаем кулдаун
        data.setCooldown(getCooldownForMode(mode));
        data.setMode(mode);
        
        return true;
    }
    
    private void fireProjectile(Player player, RailgunMode mode) {
        Location start = player.getEyeLocation();
        Vector direction = start.getDirection();
        
        // Звуковые эффекты
        player.getWorld().playSound(start, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 2.0f);
        player.getWorld().playSound(start, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
        
        // Создаем луч
        createRailgunBeam(start, direction, mode, 50); // Дальность 50 блоков
        
        // Отдача
        Vector recoil = direction.clone().multiply(-0.5);
        recoil.setY(Math.max(0.1, recoil.getY()));
        player.setVelocity(player.getVelocity().add(recoil));
    }
    
    private void createRailgunBeam(Location start, Vector direction, RailgunMode mode, int range) {
        new BukkitRunnable() {
            private int distance = 0;
            private Location current = start.clone();
            
            @Override
            public void run() {
                if (distance >= range) {
                    cancel();
                    return;
                }
                
                // Проверяем столкновения
                if (current.getBlock().getType().isSolid()) {
                    // Взрыв при попадании
                    createImpactEffect(current, mode);
                    cancel();
                    return;
                }
                
                // Проверяем попадание по мобам
                for (org.bukkit.entity.Entity entity : current.getWorld().getNearbyEntities(current, 1, 1, 1)) {
                    if (entity instanceof org.bukkit.entity.LivingEntity && !entity.equals(start.getWorld().getPlayers().stream().findFirst().orElse(null))) {
                        org.bukkit.entity.LivingEntity target = (org.bukkit.entity.LivingEntity) entity;
                        dealDamage(target, mode);
                        
                        if (mode != RailgunMode.PIERCING) {
                            createImpactEffect(current, mode);
                            cancel();
                            return;
                        }
                    }
                }
                
                // Визуальные эффекты луча
                createBeamEffect(current, mode);
                
                // Двигаемся дальше
                current.add(direction.clone().multiply(0.5));
                distance++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void createBeamEffect(Location location, RailgunMode mode) {
        Particle particle = getParticleForMode(mode);
        location.getWorld().spawnParticle(particle, location, 3, 0.1, 0.1, 0.1, 0.1);
        
        // Дополнительные эффекты для некоторых режимов
        if (mode == RailgunMode.OVERCHARGE) {
            location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location, 5, 0.2, 0.2, 0.2, 0.2);
        }
    }
    
    private void createImpactEffect(Location location, RailgunMode mode) {
        switch (mode) {
            case EXPLOSIVE:
                location.getWorld().createExplosion(location, 4.0f, false, true);
                break;
            case SCATTER:
                // Создаем несколько мелких взрывов
                for (int i = 0; i < 5; i++) {
                    Location scatterLoc = location.clone().add(
                        (Math.random() - 0.5) * 4,
                        (Math.random() - 0.5) * 4,
                        (Math.random() - 0.5) * 4
                    );
                    scatterLoc.getWorld().createExplosion(scatterLoc, 2.0f, false, false);
                }
                break;
            case EMP:
                // EMP эффект - отключаем электронику в радиусе
                createEMPEffect(location);
                break;
            default:
                location.getWorld().spawnParticle(Particle.EXPLOSION, location, 10, 1, 1, 1, 0.1);
                break;
        }
    }
    
    private void dealDamage(org.bukkit.entity.LivingEntity target, RailgunMode mode) {
        double damage = getDamageForMode(mode);
        target.damage(damage);
        
        // Дополнительные эффекты
        switch (mode) {
            case OVERCHARGE:
                target.setFireTicks(100); // 5 секунд горения
                break;
            case EMP:
                if (target instanceof Player) {
                    Player playerTarget = (Player) target;
                    // Временно отключаем энергию
                    plugin.getEnergyManager().consumePlayerEnergy(playerTarget, 
                        plugin.getEnergyManager().getPlayerEnergy(playerTarget) / 2);
                }
                break;
        }
    }
    
    private void createEMPEffect(Location center) {
        // Визуальный эффект EMP
        for (int i = 0; i < 50; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double radius = Math.random() * 10;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            double y = center.getY() + Math.random() * 5;
            
            Location empLoc = new Location(center.getWorld(), x, y, z);
            center.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, empLoc, 1, 0, 0, 0, 0.1);
        }
        
        // Отключаем энергию у игроков в радиусе
        for (Player player : center.getWorld().getPlayers()) {
            if (player.getLocation().distance(center) <= 10) {
                int currentEnergy = plugin.getEnergyManager().getPlayerEnergy(player);
                plugin.getEnergyManager().consumePlayerEnergy(player, currentEnergy / 3);
                player.sendMessage("§c⚡ EMP-импульс отключил часть вашей энергии!");
            }
        }
    }
    
    private Particle getParticleForMode(RailgunMode mode) {
        switch (mode) {
            case PIERCING: return Particle.CRIT;
            case EXPLOSIVE: return Particle.FLAME;
            case SCATTER: return Particle.FIREWORK;
            case OVERCHARGE: return Particle.DRAGON_BREATH;
            case EMP: return Particle.ENCHANT;
            default: return Particle.ELECTRIC_SPARK;
        }
    }
    
    private int getEnergyCostForMode(RailgunMode mode) {
        switch (mode) {
            case STANDARD: return 50;
            case PIERCING: return 75;
            case EXPLOSIVE: return 100;
            case SCATTER: return 125;
            case OVERCHARGE: return 200;
            case EMP: return 150;
            default: return 50;
        }
    }
    
    private long getCooldownForMode(RailgunMode mode) {
        switch (mode) {
            case STANDARD: return 3000; // 3 секунды
            case PIERCING: return 4000; // 4 секунды
            case EXPLOSIVE: return 6000; // 6 секунд
            case SCATTER: return 8000; // 8 секунд
            case OVERCHARGE: return 12000; // 12 секунд
            case EMP: return 10000; // 10 секунд
            default: return 3000;
        }
    }
    
    private double getDamageForMode(RailgunMode mode) {
        switch (mode) {
            case STANDARD: return 15.0;
            case PIERCING: return 20.0;
            case EXPLOSIVE: return 12.0; // Меньше прямого урона, но есть взрыв
            case SCATTER: return 8.0; // Множественные попадания
            case OVERCHARGE: return 25.0;
            case EMP: return 10.0; // Основной эффект - отключение энергии
            default: return 15.0;
        }
    }
    
    public RailgunData getRailgunData(Player player) {
        return railgunData.get(player.getUniqueId());
    }
    
    public enum RailgunMode {
        STANDARD("Стандартный"),
        PIERCING("Пробивающий"),
        EXPLOSIVE("Взрывной"),
        SCATTER("Дробовик"),
        OVERCHARGE("Перегрузка"),
        EMP("ЭМИ");
        
        private final String displayName;
        
        RailgunMode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    public static class RailgunData {
        private RailgunMode mode = RailgunMode.STANDARD;
        private long lastShot = 0;
        private long cooldownDuration = 0;
        
        public RailgunMode getMode() { return mode; }
        public void setMode(RailgunMode mode) { this.mode = mode; }
        
        public void setCooldown(long duration) {
            this.lastShot = System.currentTimeMillis();
            this.cooldownDuration = duration;
        }
        
        public boolean isOnCooldown() {
            return System.currentTimeMillis() - lastShot < cooldownDuration;
        }
        
        public long getRemainingCooldown() {
            long remaining = cooldownDuration - (System.currentTimeMillis() - lastShot);
            return Math.max(0, remaining);
        }
        
        public void updateCooldowns() {
            // Метод для обновления кулдаунов, если нужно
        }
    }
}
