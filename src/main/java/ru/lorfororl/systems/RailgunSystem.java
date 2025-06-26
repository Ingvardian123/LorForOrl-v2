package ru.lorfororl.systems;

import com.lorfororl.managers.EnergyManager;
import com.lorfororl.managers.NotificationManager;
import com.lorfororl.managers.ParticleSystemManager;
import com.lorfororl.managers.SoundManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RailgunSystem {
    private final EnergyManager energyManager;
    private final NotificationManager notificationManager;
    private final ParticleSystemManager particleManager;
    private final SoundManager soundManager;
    private final Map<UUID, Long> lastShotTime;
    
    private static final int ENERGY_COST = 500;
    private static final int COOLDOWN_MS = 5000; // 5 секунд
    private static final double MAX_RANGE = 100.0;
    private static final double DAMAGE = 20.0;
    
    public RailgunSystem(EnergyManager energyManager, NotificationManager notificationManager,
                        ParticleSystemManager particleManager, SoundManager soundManager) {
        this.energyManager = energyManager;
        this.notificationManager = notificationManager;
        this.particleManager = particleManager;
        this.soundManager = soundManager;
        this.lastShotTime = new HashMap<>();
    }
    
    public boolean fireRailgun(Player player) {
        if (!hasRailgun(player)) {
            notificationManager.sendMessage(player, "§cУ вас нет рельсотрона!");
            return false;
        }
        
        if (!energyManager.hasEnoughEnergy(player, ENERGY_COST)) {
            notificationManager.sendMessage(player, "§cНедостаточно энергии для выстрела! Требуется: " + ENERGY_COST);
            return false;
        }
        
        if (isOnCooldown(player)) {
            long remainingCooldown = getRemainingCooldown(player);
            notificationManager.sendMessage(player, "§cРельсотрон перезаряжается! Осталось: " + (remainingCooldown / 1000) + " сек.");
            return false;
        }
        
        // Выполняем выстрел
        performRailgunShot(player);
        
        // Потребляем энергию и устанавливаем кулдаун
        energyManager.consumeEnergy(player, ENERGY_COST);
        lastShotTime.put(player.getUniqueId(), System.currentTimeMillis());
        
        return true;
    }
    
    private void performRailgunShot(Player player) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();
        
        // Звуковой эффект выстрела
        soundManager.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.5f);
        soundManager.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 2.0f);
        
        // Создаем луч частиц
        createRailgunBeam(eyeLocation, direction);
        
        // Ищем цели по траектории
        Entity target = findTarget(eyeLocation, direction);
        
        if (target instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) target;
            
            // Наносим урон
            livingTarget.damage(DAMAGE, player);
            
            // Эффекты попадания
            Location targetLocation = target.getLocation();
            particleManager.spawnParticles(targetLocation, Particle.EXPLOSION, 10, 1.0, 1.0, 1.0, 0.1);
            particleManager.spawnParticles(targetLocation, Particle.ELECTRIC_SPARK, 20, 0.5, 0.5, 0.5, 0.2);
            
            soundManager.playSound(targetLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            
            notificationManager.sendMessage(player, "§aПопадание! Урон: " + DAMAGE);
            
            // Отбрасываем цель
            Vector knockback = direction.multiply(2.0);
            knockback.setY(Math.max(knockback.getY(), 0.5));
            livingTarget.setVelocity(knockback);
        } else {
            notificationManager.sendMessage(player, "§cПромах!");
        }
    }
    
    private void createRailgunBeam(Location start, Vector direction) {
        Location current = start.clone();
        
        for (int i = 0; i < MAX_RANGE; i++) {
            // Основной луч
            particleManager.spawnParticles(current, Particle.ELECTRIC_SPARK, 3, 0.1, 0.1, 0.1, 0.0);
            particleManager.spawnParticles(current, Particle.END_ROD, 1, 0.0, 0.0, 0.0, 0.0);
            
            // Проверяем препятствия
            if (current.getBlock().getType().isSolid()) {
                // Эффект попадания в блок
                particleManager.spawnParticles(current, Particle.EXPLOSION, 5, 0.5, 0.5, 0.5, 0.0);
                break;
            }
            
            current.add(direction);
        }
    }
    
    private Entity findTarget(Location start, Vector direction) {
        Location current = start.clone();
        
        for (int i = 0; i < MAX_RANGE; i++) {
            List<Entity> nearbyEntities = current.getWorld().getNearbyEntities(current, 1.0, 1.0, 1.0);
            
            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity && !entity.equals(start.getWorld().getPlayers().get(0))) {
                    return entity;
                }
            }
            
            if (current.getBlock().getType().isSolid()) {
                break;
            }
            
            current.add(direction);
        }
        
        return null;
    }
    
    private boolean hasRailgun(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        return isRailgun(mainHand);
    }
    
    private boolean isRailgun(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName() && meta.getDisplayName().contains("Рельсотрон");
    }
    
    private boolean isOnCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!lastShotTime.containsKey(playerId)) {
            return false;
        }
        
        long lastShot = lastShotTime.get(playerId);
        return (System.currentTimeMillis() - lastShot) < COOLDOWN_MS;
    }
    
    private long getRemainingCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!lastShotTime.containsKey(playerId)) {
            return 0;
        }
        
        long lastShot = lastShotTime.get(playerId);
        long elapsed = System.currentTimeMillis() - lastShot;
        return Math.max(0, COOLDOWN_MS - elapsed);
    }
    
    public ItemStack createRailgun() {
        ItemStack railgun = new ItemStack(Material.CROSSBOW);
        ItemMeta meta = railgun.getItemMeta();
        
        meta.setDisplayName("§6§lРельсотрон");
        meta.setLore(java.util.Arrays.asList(
            "§7Мощное энергетическое оружие",
            "§7Урон: §c" + DAMAGE,
            "§7Дальность: §e" + MAX_RANGE + " блоков",
            "§7Энергопотребление: §b" + ENERGY_COST,
            "§7Перезарядка: §a" + (COOLDOWN_MS / 1000) + " сек.",
            "",
            "§eПКМ - выстрел"
        ));
        
        railgun.setItemMeta(meta);
        return railgun;
    }
    
    public void giveRailgun(Player player) {
        ItemStack railgun = createRailgun();
        player.getInventory().addItem(railgun);
        notificationManager.sendMessage(player, "§aВы получили рельсотрон!");
    }
    
    public long getRemainingCooldownSeconds(Player player) {
        return getRemainingCooldown(player) / 1000;
    }
    
    public void clearCooldown(Player player) {
        lastShotTime.remove(player.getUniqueId());
    }
    
    public void shutdown() {
        lastShotTime.clear();
    }
}
