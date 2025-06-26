package com.lorfororl.equipment;

import com.lorfororl.LorForOrlPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PowerArmorSystem {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, PowerArmorData> armorData;
    private BukkitRunnable armorTask;
    
    public PowerArmorSystem(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.armorData = new HashMap<>();
    }
    
    public void startTasks() {
        armorTask = new BukkitRunnable() {
            @Override
            public void run() {
                updatePowerArmor();
            }
        };
        armorTask.runTaskTimer(plugin, 0L, 10L); // Каждые 0.5 секунды
    }
    
    public void shutdown() {
        if (armorTask != null) {
            armorTask.cancel();
        }
    }
    
    private void updatePowerArmor() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (hasPowerArmor(player)) {
                PowerArmorData data = armorData.computeIfAbsent(player.getUniqueId(), k -> new PowerArmorData());
                updateArmorEffects(player, data);
            } else {
                armorData.remove(player.getUniqueId());
            }
        }
    }
    
    private boolean hasPowerArmor(Player player) {
        ItemStack chestplate = player.getInventory().getChestplate();
        return chestplate != null && 
               plugin.getLaboratoryItems().isLaboratoryItem(chestplate) &&
               "power_armor".equals(plugin.getLaboratoryItems().getLaboratoryItemType(chestplate));
    }
    
    private void updateArmorEffects(Player player, PowerArmorData data) {
        int energy = plugin.getEnergyManager().getPlayerEnergy(player);
        
        if (energy <= 0) {
            // Без энергии - только базовая защита
            applyLowPowerMode(player, data);
            return;
        }
        
        // Потребляем энергию в зависимости от режима
        int energyCost = calculateEnergyCost(data);
        if (!plugin.getEnergyManager().consumePlayerEnergy(player, energyCost)) {
            data.setMode(PowerArmorMode.LOW_POWER);
        }
        
        // Применяем эффекты в зависимости от режима
        switch (data.getMode()) {
            case NORMAL:
                applyNormalMode(player, data);
                break;
            case COMBAT:
                applyCombatMode(player, data);
                break;
            case STEALTH:
                applyStealthMode(player, data);
                break;
            case SHIELD:
                applyShieldMode(player, data);
                break;
            case LOW_POWER:
                applyLowPowerMode(player, data);
                break;
        }
        
        // Визуальные эффекты
        showArmorEffects(player, data);
        
        // Обновляем HUD
        updateArmorHUD(player, data);
    }
    
    private int calculateEnergyCost(PowerArmorData data) {
        switch (data.getMode()) {
            case NORMAL: return 1;
            case COMBAT: return 3;
            case STEALTH: return 4;
            case SHIELD: return 6;
            case LOW_POWER: return 0;
            default: return 1;
        }
    }
    
    private void applyNormalMode(Player player, PowerArmorData data) {
        // Базовые эффекты
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 0, false, false));
        
        // Регенерация здоровья
        if (player.getHealth() < player.getMaxHealth()) {
            player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 0.2));
        }
        
        // Защита от радиации
        double radiation = plugin.getRadiationManager().getPlayerRadiation(player);
        if (radiation > 0) {
            plugin.getRadiationManager().setPlayerRadiation(player, radiation * 0.95);
        }
    }
    
    private void applyCombatMode(Player player, PowerArmorData data) {
        // Боевые эффекты
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 30, 1, false, false));
        
        // Автоматическое лечение в бою
        if (player.getHealth() < player.getMaxHealth() * 0.5) {
            player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 0.5));
        }
        
        // Урон по площади при атаке
        if (data.shouldTriggerAreaDamage()) {
            triggerAreaDamage(player);
            data.resetAreaDamageTimer();
        }
    }
    
    private void applyStealthMode(Player player, PowerArmorData data) {
        // Эффекты скрытности
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 30, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 30, 0, false, false));
    
        // Бесшумное передвижение
        if (player.isSneaking()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 30, 0, false, false));
        }
    
        // Снижение урона от падения
        if (player.getFallDistance() > 3) {
            player.setFallDistance(player.getFallDistance() * 0.5f);
        }
    }
    
    
    private void applyShieldMode(Player player, PowerArmorData data) {
        // Максимальная защита
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 3, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 30, 0, false, false));
        
        // Энергетический щит
        if (data.shouldTriggerShield()) {
            createEnergyShield(player);
            data.resetShieldTimer();
        }
        
        // Отражение снарядов
        reflectProjectiles(player);
        
        // Замедление для стабильности
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 0, false, false));
    }
    
    private void applyLowPowerMode(Player player, PowerArmorData data) {
        // Минимальные эффекты без энергии
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 0, false, false));
        
        // Отключаем полет
        if (player.isFlying()) {
            player.setFlying(false);
        }
        player.setAllowFlight(false);
        
        // Предупреждение о низкой энергии
        if (System.currentTimeMillis() % 3000 < 500) {
            player.sendActionBar(Component.text("§c⚡ КРИТИЧЕСКИ НИЗКИЙ ЗАРЯД БРОНИ! ⚡"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
        }
    }
    
    private void triggerAreaDamage(Player player) {
        Location center = player.getLocation();
        
        for (Entity entity : center.getWorld().getNearbyEntities(center, 3, 3, 3)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity target = (LivingEntity) entity;
                target.damage(4.0, player);
                
                // Эффект отбрасывания
                Vector knockback = target.getLocation().subtract(center).toVector().normalize();
                knockback.multiply(1.5);
                knockback.setY(0.5);
                target.setVelocity(knockback);
            }
        }
        
        // Визуальные эффекты
        center.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, center, 5, 2, 1, 2, 0.1);
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
    }
    
    private void createEnergyShield(Player player) {
        Location center = player.getLocation().add(0, 1, 0);
        
        // Создаем визуальный щит
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 60) { // 3 секунды
                    cancel();
                    return;
                }
                
                // Создаем сферу частиц
                for (int i = 0; i < 20; i++) {
                    double angle = (2 * Math.PI * i) / 20;
                    double x = center.getX() + 2 * Math.cos(angle);
                    double z = center.getZ() + 2 * Math.sin(angle);
                    
                    Location particleLoc = new Location(center.getWorld(), x, center.getY(), z);
                    center.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, particleLoc, 1, 0.1, 0.1, 0.1, 0.1);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Отталкиваем врагов
        for (Entity entity : center.getWorld().getNearbyEntities(center, 3, 3, 3)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                Vector pushback = entity.getLocation().subtract(center).toVector().normalize();
                pushback.multiply(2.0);
                pushback.setY(0.5);
                entity.setVelocity(pushback);
            }
        }
    }
    
    private void reflectProjectiles(Player player) {
        // Здесь можно добавить логику отражения снарядов
        // Пока что просто защищаем от урона снарядов
    }
    
    private void showArmorEffects(Player player, PowerArmorData data) {
        Location loc = player.getLocation().add(0, 1, 0);
        
        switch (data.getMode()) {
            case NORMAL:
                if (System.currentTimeMillis() % 2000 < 100) {
                    loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 3, 0.5, 0.5, 0.5, 0.1);
                }
                break;
            case COMBAT:
                loc.getWorld().spawnParticle(Particle.CRIT, loc, 2, 0.3, 0.3, 0.3, 0.1);
                break;
            case STEALTH:
                if (System.currentTimeMillis() % 1000 < 100) {
                    loc.getWorld().spawnParticle(Particle.LARGE_SMOKE, loc, 1, 0.2, 0.2, 0.2, 0.05);
                }
                break;
            case SHIELD:
                loc.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, loc, 5, 1, 1, 1, 0.1);
                break;
        }
    }
    
    private void updateArmorHUD(Player player, PowerArmorData data) {
        int energy = plugin.getEnergyManager().getPlayerEnergy(player);
        String modeColor = getModeColor(data.getMode());
        String energyBar = createEnergyBar(energy, 1000); // Максимум 1000 энергии
        
        String hud = String.format("§6⚡ %s §7| %s%s §7| %s", 
            energyBar, modeColor, data.getMode().getDisplayName(), getArmorStatus(data));
        
        player.sendActionBar(Component.text(hud));
    }
    
    private String getModeColor(PowerArmorMode mode) {
        switch (mode) {
            case NORMAL: return "§a";
            case COMBAT: return "§c";
            case STEALTH: return "§5";
            case SHIELD: return "§e";
            case LOW_POWER: return "§8";
            default: return "§7";
        }
    }
    
    private String createEnergyBar(int current, int max) {
        int bars = 10;
        int filled = (int) ((double) current / max * bars);
        
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < bars; i++) {
            if (i < filled) {
                bar.append("§a█");
            } else {
                bar.append("§7█");
            }
        }
        
        return bar.toString() + " §f" + current + "/" + max;
    }
    
    private String getArmorStatus(PowerArmorData data) {
        return "§aОНЛАЙН"; // Можно добавить различные статусы
    }
    
    public boolean switchMode(Player player, PowerArmorMode newMode) {
        if (!hasPowerArmor(player)) return false;
        
        PowerArmorData data = armorData.get(player.getUniqueId());
        if (data == null) return false;
        
        // Проверяем требования энергии для режима
        int requiredEnergy = getRequiredEnergyForMode(newMode);
        if (plugin.getEnergyManager().getPlayerEnergy(player) < requiredEnergy) {
            player.sendMessage("§cНедостаточно энергии для переключения в режим " + newMode.getDisplayName());
            return false;
        }
        
        data.setMode(newMode);
        player.sendMessage("§aРежим силовой брони изменен на: " + getModeColor(newMode) + newMode.getDisplayName());
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
        
        return true;
    }
    
    private int getRequiredEnergyForMode(PowerArmorMode mode) {
        switch (mode) {
            case NORMAL: return 10;
            case COMBAT: return 50;
            case STEALTH: return 75;
            case SHIELD: return 150;
            case LOW_POWER: return 0;
            default: return 10;
        }
    }
    
    public PowerArmorData getArmorData(Player player) {
        return armorData.get(player.getUniqueId());
    }
    
    public enum PowerArmorMode {
        NORMAL("Обычный"),
        COMBAT("Боевой"),
        STEALTH("Скрытность"),
        SHIELD("Щит"),
        LOW_POWER("Низкий заряд");
        
        private final String displayName;
        
        PowerArmorMode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    public static class PowerArmorData {
        private PowerArmorMode mode = PowerArmorMode.NORMAL;
        private long lastAreaDamage = 0;
        private long lastJetBoost = 0;
        private long lastShield = 0;
        
        public PowerArmorMode getMode() { return mode; }
        public void setMode(PowerArmorMode mode) { this.mode = mode; }
        
        public boolean shouldTriggerAreaDamage() {
            return System.currentTimeMillis() - lastAreaDamage > 5000; // 5 секунд кулдаун
        }
        
        public void resetAreaDamageTimer() {
            lastAreaDamage = System.currentTimeMillis();
        }
        
        public boolean shouldTriggerJetBoost() {
            return System.currentTimeMillis() - lastJetBoost > 3000; // 3 секунды кулдаун
        }
        
        public void resetJetBoostTimer() {
            lastJetBoost = System.currentTimeMillis();
        }
        
        public boolean shouldTriggerShield() {
            return System.currentTimeMillis() - lastShield > 10000; // 10 секунд кулдаун
        }
        
        public void resetShieldTimer() {
            lastShield = System.currentTimeMillis();
        }
    }
}
