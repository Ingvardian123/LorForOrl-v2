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

public class RailgunSystem {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, RailgunData> railgunData;
    private BukkitRunnable systemTask;
    
    public RailgunSystem(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.railgunData = new HashMap<>();
    }
    
    public void startTasks() {
        systemTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateRailgunSystems();
            }
        };
        systemTask.runTaskTimer(plugin, 0L, 5L); // Каждые 0.25 секунды для плавности
    }
    
    public void shutdown() {
        if (systemTask != null) {
            systemTask.cancel();
        }
    }
    
    private void updateRailgunSystems() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (hasRailgunEquipped(player)) {
                RailgunData data = railgunData.computeIfAbsent(player.getUniqueId(), k -> new RailgunData());
                updatePlayerRailgun(player, data);
            } else {
                railgunData.remove(player.getUniqueId());
            }
        }
    }
    
    private boolean hasRailgunEquipped(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand == null || !mainHand.hasItemMeta()) return false;
        
        return plugin.getAdvancedItems().isAdvancedItem(mainHand) &&
               "railgun".equals(plugin.getAdvancedItems().getAdvancedItemType(mainHand));
    }
    
    private void updatePlayerRailgun(Player player, RailgunData data) {
        // Обновляем кулдауны
        data.updateTimers();
        
        // Проверяем энергию для текущего режима
        int requiredEnergy = getEnergyRequirement(data.getMode());
        int playerEnergy = plugin.getEnergyManager().getPlayerEnergy(player);
        
        if (playerEnergy < requiredEnergy && data.getMode() != RailgunMode.STANDARD) {
            data.setMode(RailgunMode.STANDARD);
            player.sendMessage("§cНедостаточно энергии! Переключение на стандартный режим.");
        }
        
        // Показываем эффекты зарядки
        if (data.isCharging()) {
            showChargingEffects(player, data);
        }
        
        // Обновляем HUD
        updateRailgunHUD(player, data);
    }
    
    public boolean switchMode(Player player, RailgunMode newMode) {
        if (!hasRailgunEquipped(player)) {
            player.sendMessage("§cУ вас нет рельсотрона!");
            return false;
        }
        
        RailgunData data = railgunData.get(player.getUniqueId());
        if (data == null) return false;
        
        if (data.isOnCooldown()) {
            player.sendMessage("§cНельзя переключать режим во время перезарядки!");
            return false;
        }
        
        int requiredEnergy = getEnergyRequirement(newMode);
        if (plugin.getEnergyManager().getPlayerEnergy(player) < requiredEnergy) {
            player.sendMessage("§cНедостаточно энергии для режима " + newMode.getDisplayName() + "!");
            player.sendMessage("§eТребуется: " + requiredEnergy + " энергии");
            return false;
        }
        
        data.setMode(newMode);
        player.sendMessage("§aРежим рельсотрона изменен на: §6" + newMode.getDisplayName());
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
        
        // Эффекты переключения режима
        showModeSwithEffects(player, newMode);
        
        return true;
    }
    
    public boolean startCharging(Player player) {
        if (!hasRailgunEquipped(player)) return false;
        
        RailgunData data = railgunData.get(player.getUniqueId());
        if (data == null) return false;
        
        if (data.isOnCooldown()) {
            player.sendMessage("§cРельсотрон еще перезаряжается!");
            return false;
        }
        
        if (data.isCharging()) {
            player.sendMessage("§cРельсотрон уже заряжается!");
            return false;
        }
        
        int energyCost = getEnergyCost(data.getMode());
        if (!plugin.getEnergyManager().consumePlayerEnergy(player, energyCost)) {
            player.sendMessage("§cНедостаточно энергии! Требуется: " + energyCost);
            return false;
        }
        
        data.startCharging();
        player.sendMessage("§eЗарядка рельсотрона... Удерживайте ПКМ для выстрела.");
        
        return true;
    }
    
    public boolean fire(Player player) {
        if (!hasRailgunEquipped(player)) return false;
        
        RailgunData data = railgunData.get(player.getUniqueId());
        if (data == null) return false;
        
        if (!data.isCharging()) {
            player.sendMessage("§cСначала зарядите рельсотрон!");
            return false;
        }
        
        // Стреляем
        executeShot(player, data);
        
        // Устанавливаем кулдаун
        data.setCooldown(getCooldownTime(data.getMode()));
        data.stopCharging();
        
        return true;
    }
    
    private void executeShot(Player player, RailgunData data) {
        Location muzzle = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(1.5));
        Vector direction = player.getEyeLocation().getDirection();
        
        // Звуковые эффекты
        muzzle.getWorld().playSound(muzzle, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 3.0f, 2.0f);
        muzzle.getWorld().playSound(muzzle, Sound.ENTITY_WITHER_SHOOT, 2.0f, 0.5f);
        
        // Отдача
        Vector recoil = direction.clone().multiply(-0.8);
        recoil.setY(Math.max(0.2, recoil.getY()));
        player.setVelocity(player.getVelocity().add(recoil));
        
        // Создаем снаряд
        createRailgunProjectile(muzzle, direction, data.getMode(), player);
        
        // Эффекты выстрела
        showFireEffects(muzzle, data.getMode());
    }
    
    private void createRailgunProjectile(Location start, Vector direction, RailgunMode mode, Player shooter) {
        new BukkitRunnable() {
            private Location current = start.clone();
            private int distance = 0;
            private final int maxDistance = getMaxRange(mode);
            
            @Override
            public void run() {
                if (distance >= maxDistance) {
                    cancel();
                    return;
                }
                
                // Проверяем столкновения с блоками
                if (current.getBlock().getType().isSolid()) {
                    handleBlockImpact(current, mode, shooter);
                    cancel();
                    return;
                }
                
                // Проверяем попадания по сущностям
                for (org.bukkit.entity.Entity entity : current.getWorld().getNearbyEntities(current, 1.5, 1.5, 1.5)) {
                    if (entity instanceof org.bukkit.entity.LivingEntity && !entity.equals(shooter)) {
                        handleEntityImpact((org.bukkit.entity.LivingEntity) entity, mode, shooter);
                        
                        if (mode != RailgunMode.PIERCING) {
                            cancel();
                            return;
                        }
                    }
                }
                
                // Визуальные эффекты полета
                showProjectileEffects(current, mode);
                
                // Двигаемся дальше
                current.add(direction.clone().multiply(1.0));
                distance++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void handleBlockImpact(Location impact, RailgunMode mode, Player shooter) {
        switch (mode) {
            case EXPLOSIVE:
                impact.getWorld().createExplosion(impact, 6.0f, false, true);
                break;
            case SCATTER:
                createScatterExplosion(impact);
                break;
            case OVERCHARGE:
                createOverchargeExplosion(impact);
                break;
            case EMP:
                createEMPBlast(impact);
                break;
            default:
                impact.getWorld().spawnParticle(Particle.EXPLOSION, impact, 20, 2, 2, 2, 0.1);
                break;
        }
        
        impact.getWorld().playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);
    }
    
    private void handleEntityImpact(org.bukkit.entity.LivingEntity target, RailgunMode mode, Player shooter) {
        double damage = getDamage(mode);
        target.damage(damage, shooter);
        
        // Дополнительные эффекты по режимам
        switch (mode) {
            case PIERCING:
                // Пробивающий урон игнорирует броню
                target.damage(damage * 0.5, shooter);
                break;
            case OVERCHARGE:
                target.setFireTicks(200); // 10 секунд горения
                break;
            case EMP:
                if (target instanceof Player) {
                    Player playerTarget = (Player) target;
                    disablePlayerSystems(playerTarget);
                }
                break;
        }
        
        // Эффект отбрасывания
        Vector knockback = target.getLocation().subtract(shooter.getLocation()).toVector().normalize();
        knockback.multiply(2.0);
        knockback.setY(0.5);
        target.setVelocity(knockback);
    }
    
    private void createScatterExplosion(Location center) {
        // Создаем 8 мелких взрывов в радиусе
        for (int i = 0; i < 8; i++) {
            double angle = (2 * Math.PI * i) / 8;
            double radius = 2 + Math.random() * 3;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            double y = center.getY() + (Math.random() - 0.5) * 2;
            
            Location explosionLoc = new Location(center.getWorld(), x, y, z);
            center.getWorld().createExplosion(explosionLoc, 3.0f, false, false);
        }
    }
    
    private void createOverchargeExplosion(Location center) {
        // Мощный взрыв с огненными эффектами
        center.getWorld().createExplosion(center, 8.0f, false, true);
        
        // Огненные частицы
        for (int i = 0; i < 100; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double radius = Math.random() * 8;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            double y = center.getY() + Math.random() * 5;
            
            Location fireLoc = new Location(center.getWorld(), x, y, z);
            center.getWorld().spawnParticle(Particle.FLAME, fireLoc, 1, 0, 0, 0, 0.1);
        }
    }
    
    private void createEMPBlast(Location center) {
        // Визуальный EMP эффект
        for (int i = 0; i < 200; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double radius = Math.random() * 15;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            double y = center.getY() + Math.random() * 8 - 4;
            
            Location empLoc = new Location(center.getWorld(), x, y, z);
            center.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, empLoc, 1, 0, 0, 0, 0.2);
        }
        
        // Отключаем системы у всех игроков в радиусе
        for (Player player : center.getWorld().getPlayers()) {
            if (player.getLocation().distance(center) <= 15) {
                disablePlayerSystems(player);
            }
        }
    }
    
    private void disablePlayerSystems(Player player) {
        // Отключаем половину энергии
        int currentEnergy = plugin.getEnergyManager().getPlayerEnergy(player);
        plugin.getEnergyManager().consumePlayerEnergy(player, currentEnergy / 2);
        
        // Отключаем силовую броню на время
        if (plugin.getPowerArmorSystem().getArmorData(player) != null) {
            plugin.getPowerArmorSystem().getArmorData(player).setMode(
                com.lorfororl.equipment.PowerArmorSystem.PowerArmorMode.LOW_POWER);
        }
        
        player.sendMessage("§c⚡ EMP-импульс отключил ваши системы!");
        player.playSound(player.getLocation(), Sound.ENTITY_CREEPER_HURT, 1.0f, 0.5f);
    }
    
    private void showChargingEffects(Player player, RailgunData data) {
        Location muzzle = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(1.5));
        
        // Частицы зарядки
        Particle chargeParticle = getChargeParticle(data.getMode());
        muzzle.getWorld().spawnParticle(chargeParticle, muzzle, 5, 0.1, 0.1, 0.1, 0.1);
        
        // Звук зарядки
        if (System.currentTimeMillis() % 500 < 50) {
            float pitch = 0.5f + (data.getChargeProgress() * 1.5f);
            player.playSound(muzzle, Sound.BLOCK_NOTE_BLOCK_HARP, 0.3f, pitch);
        }
    }
    
    private void showModeSwithEffects(Player player, RailgunMode mode) {
        Location center = player.getLocation().add(0, 1, 0);
        
        // Цветные частицы в зависимости от режима
        Particle modeParticle = getModeParticle(mode);
        center.getWorld().spawnParticle(modeParticle, center, 20, 1, 1, 1, 0.1);
        
        // Звуковой эффект
        player.playSound(center, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, getModeSound(mode));
    }
    
    private void showFireEffects(Location muzzle, RailgunMode mode) {
        // Вспышка выстрела
        muzzle.getWorld().spawnParticle(Particle.FLASH, muzzle, 1, 0, 0, 0, 0);
        
        // Дым
        muzzle.getWorld().spawnParticle(Particle.LARGE_SMOKE, muzzle, 10, 0.5, 0.5, 0.5, 0.1);
        
        // Специфичные для режима эффекты
        Particle modeParticle = getModeParticle(mode);
        muzzle.getWorld().spawnParticle(modeParticle, muzzle, 15, 0.3, 0.3, 0.3, 0.2);
    }
    
    private void showProjectileEffects(Location location, RailgunMode mode) {
        Particle trailParticle = getTrailParticle(mode);
        location.getWorld().spawnParticle(trailParticle, location, 2, 0.05, 0.05, 0.05, 0.1);
        
        // Дополнительные эффекты для некоторых режимов
        if (mode == RailgunMode.OVERCHARGE) {
            location.getWorld().spawnParticle(Particle.FLAME, location, 1, 0.1, 0.1, 0.1, 0.05);
        }
    }
    
    private void updateRailgunHUD(Player player, RailgunData data) {
        StringBuilder hud = new StringBuilder();
        
        // Режим
        hud.append("§6🔫 ").append(getModeColor(data.getMode())).append(data.getMode().getDisplayName());
        
        // Статус
        if (data.isOnCooldown()) {
            long remaining = data.getRemainingCooldown() / 1000;
            hud.append(" §c[").append(remaining).append("s]");
        } else if (data.isCharging()) {
            int progress = (int) (data.getChargeProgress() * 100);
            hud.append(" §e[").append(progress).append("%]");
        } else {
            hud.append(" §a[ГОТОВ]");
        }
        
        // Энергия
        int energy = plugin.getEnergyManager().getPlayerEnergy(player);
        int required = getEnergyRequirement(data.getMode());
        String energyColor = energy >= required ? "§a" : "§c";
        hud.append(" ").append(energyColor).append("⚡").append(energy).append("/").append(required);
        
        player.sendActionBar(net.kyori.adventure.text.Component.text(hud.toString()));
    }
    
    // Вспомогательные методы для получения параметров режимов
    private int getEnergyRequirement(RailgunMode mode) {
        switch (mode) {
            case STANDARD: return 25;
            case PIERCING: return 40;
            case EXPLOSIVE: return 60;
            case SCATTER: return 80;
            case OVERCHARGE: return 120;
            case EMP: return 100;
            default: return 25;
        }
    }
    
    private int getEnergyCost(RailgunMode mode) {
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
    
    private long getCooldownTime(RailgunMode mode) {
        switch (mode) {
            case STANDARD: return 3000;
            case PIERCING: return 4000;
            case EXPLOSIVE: return 6000;
            case SCATTER: return 8000;
            case OVERCHARGE: return 12000;
            case EMP: return 10000;
            default: return 3000;
        }
    }
    
    private double getDamage(RailgunMode mode) {
        switch (mode) {
            case STANDARD: return 15.0;
            case PIERCING: return 20.0;
            case EXPLOSIVE: return 12.0;
            case SCATTER: return 8.0;
            case OVERCHARGE: return 25.0;
            case EMP: return 10.0;
            default: return 15.0;
        }
    }
    
    private int getMaxRange(RailgunMode mode) {
        switch (mode) {
            case STANDARD: return 50;
            case PIERCING: return 75;
            case EXPLOSIVE: return 40;
            case SCATTER: return 30;
            case OVERCHARGE: return 60;
            case EMP: return 45;
            default: return 50;
        }
    }
    
    private Particle getChargeParticle(RailgunMode mode) {
        switch (mode) {
            case EXPLOSIVE: return Particle.FLAME;
            case OVERCHARGE: return Particle.DRAGON_BREATH;
            case EMP: return Particle.ENCHANT;
            default: return Particle.ELECTRIC_SPARK;
        }
    }
    
    private Particle getModeParticle(RailgunMode mode) {
        switch (mode) {
            case PIERCING: return Particle.CRIT;
            case EXPLOSIVE: return Particle.FLAME;
            case SCATTER: return Particle.FIREWORK;
            case OVERCHARGE: return Particle.DRAGON_BREATH;
            case EMP: return Particle.ENCHANT;
            default: return Particle.ELECTRIC_SPARK;
        }
    }
    
    private Particle getTrailParticle(RailgunMode mode) {
        switch (mode) {
            case PIERCING: return Particle.CRIT;
            case EXPLOSIVE: return Particle.FLAME;
            case SCATTER: return Particle.FIREWORK;
            case OVERCHARGE: return Particle.DRAGON_BREATH;
            case EMP: return Particle.ENCHANT;
            default: return Particle.ELECTRIC_SPARK;
        }
    }
    
    private String getModeColor(RailgunMode mode) {
        switch (mode) {
            case STANDARD: return "§f";
            case PIERCING: return "§e";
            case EXPLOSIVE: return "§c";
            case SCATTER: return "§6";
            case OVERCHARGE: return "§5";
            case EMP: return "§b";
            default: return "§7";
        }
    }
    
    private float getModeSound(RailgunMode mode) {
        switch (mode) {
            case STANDARD: return 1.0f;
            case PIERCING: return 1.2f;
            case EXPLOSIVE: return 0.8f;
            case SCATTER: return 0.6f;
            case OVERCHARGE: return 0.5f;
            case EMP: return 1.5f;
            default: return 1.0f;
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
        private long chargeStart = 0;
        private boolean charging = false;
        private final long maxChargeTime = 2000; // 2 секунды максимальной зарядки
        
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
        
        public void startCharging() {
            this.charging = true;
            this.chargeStart = System.currentTimeMillis();
        }
        
        public void stopCharging() {
            this.charging = false;
            this.chargeStart = 0;
        }
        
        public boolean isCharging() {
            return charging;
        }
        
        public float getChargeProgress() {
            if (!charging) return 0.0f;
            long elapsed = System.currentTimeMillis() - chargeStart;
            return Math.min(1.0f, (float) elapsed / maxChargeTime);
        }
        
        public void updateTimers() {
            // Автоматически останавливаем зарядку если прошло слишком много времени
            if (charging && System.currentTimeMillis() - chargeStart > maxChargeTime * 2) {
                stopCharging();
            }
        }
    }
}
